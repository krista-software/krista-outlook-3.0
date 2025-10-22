package app.krista.extensions.essentials.collaboration.outlook3.api;

import app.krista.extension.executor.Invoker;
import app.krista.extension.request.RoutingInfo;
import app.krista.extension.request.protos.http.HttpProtocol;
import app.krista.extensions.essentials.collaboration.outlook3.OutlookAttributes;
import app.krista.extensions.essentials.collaboration.outlook3.impl.AccountImpl;
import app.krista.extensions.essentials.collaboration.outlook3.impl.MailSubscription;
import app.krista.extensions.essentials.collaboration.outlook3.impl.SaveConfigurationImpl;
import app.krista.extensions.essentials.collaboration.outlook3.impl.TestConnectionServiceImpl;
import app.krista.extensions.essentials.collaboration.outlook3.impl.connectors.GraphServiceClientProvider;
import app.krista.extensions.essentials.collaboration.outlook3.impl.connectors.GraphServiceClientProviderFactory;
import app.krista.extensions.essentials.collaboration.outlook3.impl.connectors.OAuthService;
import app.krista.extensions.essentials.collaboration.outlook3.impl.stores.OutlookAttributeStore;
import app.krista.extensions.essentials.collaboration.outlook3.impl.stores.RefreshTokenStore;
import app.krista.extensions.essentials.collaboration.outlook3.impl.util.*;
import app.krista.extensions.essentials.collaboration.outlook3.service.Account;
import app.krista.extensions.essentials.collaboration.outlook3.service.Email;
import app.krista.extensions.essentials.collaboration.outlook3.service.Folder;
import app.krista.extensions.util.EventHandler;
import app.krista.ksdk.authentication.AuthorizationListener;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.model.base.FreeForm;
import com.github.scribejava.core.exceptions.OAuthException;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.microsoft.graph.http.GraphServiceException;
import com.microsoft.graph.models.Message;
import com.microsoft.graph.options.HeaderOption;
import com.microsoft.graph.options.QueryOption;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.concurrent.ExecutionException;

import static app.krista.extensions.essentials.collaboration.outlook3.impl.util.Constants.*;
import static app.krista.extensions.essentials.collaboration.outlook3.impl.util.EncryptionUtil.KRISTA_PREFIX;
import static com.github.scribejava.core.model.OAuthConstants.CODE;
import static com.github.scribejava.core.model.OAuthConstants.STATE;

@Path("/")
public final class OutlookApiResource {

    public static final String USER_AUTHENTICATED_SUCCESSFULLY_PLEASE_PROCEED_WITH_REQUEST = "User Authenticated Successfully. Please proceed with the request.";
    private static final Logger LOGGER = LoggerFactory.getLogger(OutlookApiResource.class);
    private static final Set<String> triggeredMailIds = new LinkedHashSet<>();
    private static final int MESSAGE_ID_CAPACITY = 1000;
    private final OutlookAttributeStore outlookAttributeStore;
    private final RefreshTokenStore refreshTokenStore;
    private final GraphServiceClientProviderFactory providerFactory;
    private final EventHandler eventHandler;
    private final NotificationProcessQueue notificationProcessQueue;
    private final AuthorizationContext context;
    private final AuthorizationListener authorizationListener;
    private final String baseRoutingUrl;
    private final String invokerId;
    private final Invoker invoker;
    private final TestConnectionServiceImpl testConnectionService;
    private final SaveConfigurationImpl saveConfigurationImpl;

    @Inject
    public OutlookApiResource(OutlookAttributeStore outlookAttributeStore, RefreshTokenStore refreshTokenStore,
                              GraphServiceClientProviderFactory providerFactory, EventHandler eventHandler,
                              Invoker invoker, AuthorizationContext context, AuthorizationListener authorizationListener,
                              TestConnectionServiceImpl testConnectionService, SaveConfigurationImpl saveConfigurationImpl) {
        this.outlookAttributeStore = outlookAttributeStore;
        this.refreshTokenStore = refreshTokenStore;
        this.providerFactory = providerFactory;
        this.eventHandler = eventHandler;
        this.context = context;
        this.authorizationListener = authorizationListener;
        this.saveConfigurationImpl = saveConfigurationImpl;
        this.notificationProcessQueue = new NotificationProcessQueue(providerFactory, invoker);
        this.baseRoutingUrl = invoker.getRoutingInfo().getRoutingURL(HttpProtocol.PROTOCOL_NAME, RoutingInfo.Type.APPLIANCE);
        this.invokerId = invoker.getInvokerId();
        this.invoker = invoker;
        this.testConnectionService = testConnectionService;
    }

    @GET
    @Path("callback")
    public String getCallBackForV2Auth(@QueryParam(CODE) String code, @QueryParam(STATE) String state) {
        return getAuthenticationResponseMessage(code, state);
    }

    @GET
    @Path("v3/oauth/callback")
    public String getCallbackForV3Auth(@QueryParam(CODE) String code, @QueryParam(STATE) String state) {
        return getAuthenticationResponseMessage(code, state);
    }

    @NotNull
    private String getAuthenticationResponseMessage(String code, String state) {
        LOGGER.info("Authentication callback triggered with Code: [{}], State: [{}]", code, state);

        if (code == null) {
            LOGGER.debug("Missing authorization code in callback. Rejecting authentication.");
            return "Authentication Failed. Please re-authorize.";
        }

        String[] parts = state.split(Constants.HASH);
        if (parts[0].isBlank()) {
            LOGGER.error("State parameter is invalid or empty.");
            throw new BadRequestException(Constants.INVALID_STATE_PARAMETERS);
        }

        String key = parts[0];
        String clientKey = null;

        if (parts.length >= 4 || (parts.length == 3 && key.startsWith(WS_CONTACT))) {
            clientKey = parts[1];
        }
        String authContextId = AuthHelper.getAuthContextId(state);

        LOGGER.debug("Parsed key: [{}] from state. AuthContextId resolved: [{}]", key, authContextId);

        try {
            LOGGER.info("Retrieving GraphServiceClientProvider using AuthContextId: {}", authContextId);
            GraphServiceClientProvider clientProvider = getGraphServiceClientProvider(authContextId);

            LOGGER.debug("Fetching Outlook attributes for authentication...");
            OutlookAttributes outlookAttributes = clientProvider.getOutlookAttributes();

            LOGGER.debug("Initializing OAuth20Service for token exchange...");
            OAuth20Service oAuth20Service = new OAuthService(outlookAttributes).getOAuth20Service();

            LOGGER.info("Requesting access token from Microsoft Graph...");
            OAuth2AccessToken accessToken = oAuth20Service.getAccessToken(code);
            LOGGER.info("Access token retrieved successfully.");

            refreshTokenStore.put(key, accessToken.getRefreshToken());
            if (clientKey != null) {
                refreshTokenStore.put(clientKey, accessToken.getRefreshToken());
            }
            LOGGER.debug("Refresh token stored for key: {}", key);

            if (!key.startsWith(Constants.WS_CONTACT) && !hasUserAccess(clientProvider)) {
                LOGGER.debug("User access check failed. Authenticated user does not match setup user.");
                refreshTokenStore.remove(key);
                return Constants.UNAUTHORISED_USER + " User email '" + key.split(UNDER_SCORE)[0] + "' configured in the setup does not match with authenticated user.";
            }

            if (context.isAuthenticated()) {
                LOGGER.info("User is authenticated. Notifying authorization listener.");
                authorizationListener.authorized();
                return USER_AUTHENTICATED_SUCCESSFULLY_PLEASE_PROCEED_WITH_REQUEST;
            }

            LOGGER.info("User authenticated successfully but not yet in active context.");
            return Constants.USER_AUTHENTICATED_SUCCESSFULLY_SAVE_THE_CHANGES;

        } catch (OAuthException cause) {
            LOGGER.error("OAuthException occurred during token exchange: {}", cause.getMessage(), cause);
            throw new IllegalStateException(getErrorDescription(cause), cause.getCause());

        } catch (IOException | ExecutionException cause) {
            LOGGER.error("Exception occurred during authorization (I/O or Execution): {}", cause.getMessage(), cause);
            throw new IllegalStateException(Constants.ERROR_OCCURRED_DURING_AUTHORIZATION, cause.getCause());

        } catch (InterruptedException interruptedException) {
            LOGGER.error("Authorization thread interrupted: {}", interruptedException.getMessage(), interruptedException);
            Thread.currentThread().interrupt();
            throw new IllegalStateException(Constants.ERROR_OCCURRED_DURING_AUTHORIZATION, interruptedException.getCause());

        } finally {
            LOGGER.debug("Cleaning up outlookAttributeStore for authContextId: {}", authContextId);
            if (authContextId != null) {
                outlookAttributeStore.remove(authContextId);
            }
        }
    }

    private GraphServiceClientProvider getGraphServiceClientProvider(String authContextId) {
        return authContextId == null
                ? providerFactory.create()
                : providerFactory.create(authContextId);
    }

    private String getErrorDescription(OAuthException cause) {
        String errorDescription = Constants.ERROR_OCCURRED_DURING_AUTHORIZATION;
        if (cause.getMessage() != null && !cause.getMessage().isBlank()) {
            var exceptionMap = Constants.GSON.fromJson(cause.getMessage(), Map.class);
            if (exceptionMap.containsKey(Constants.ERROR_DESCRIPTION)) {
                errorDescription = errorDescription + exceptionMap.get(Constants.ERROR_DESCRIPTION);
            }
        }
        return errorDescription;
    }

    private boolean hasUserAccess(GraphServiceClientProvider clientProvider) {
        try {
            clientProvider.getUserRequestBuilder(null, null).mailFolders().buildRequest().get();
            return true;
        } catch (GraphServiceException cause) {
            LOGGER.error(" User access verification failed. User does not have permission to access Microsoft Graph or " +
                    "authentication is invalid : {}", cause.getMessage(), cause);
            return false;
        }
    }

    /**
     * This method finds duplicate messageId from recent 1000 mail received and add new messageId to set and Remove the oldest 100 mail IDs to make space for new ones.
     *
     * @param messageId This is messageID sent by GraphApi
     * @return true if duplicate is found
     */
    private boolean isDuplicateMessageID(String messageId) {

        if (!triggeredMailIds.contains(messageId)) {
            if (triggeredMailIds.size() >= MESSAGE_ID_CAPACITY) {
                removeOldestMessageIds((MESSAGE_ID_CAPACITY + 100) - triggeredMailIds.size());
            }
            triggeredMailIds.add(messageId);
            return false;
        }
        return true;
    }

    private void removeOldestMessageIds(int messageIDCount) {
        final Iterator<String> iterator = triggeredMailIds.iterator();
        int i = 0;
        while (iterator.hasNext() && i < messageIDCount) {
            iterator.next();
            iterator.remove();
            i++;
        }
    }

    @POST
    @Path("/mailNotification")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response subscriptionValidation(@QueryParam(Constants.VALIDATION_TOKEN) String validationToken) {
        return Response.status(200).type(MediaType.TEXT_PLAIN).entity(validationToken.trim()).build();
    }

    /**
     * Original simple mail notification endpoint - triggers for ALL emails in Inbox.
     * This endpoint maintains backward compatibility with existing "Mail Received Alert" workflows.
     * Microsoft sends the notification twice when there is no acknowledgement from krista that it received the mail.
     *
     * @param notification the JSON payload from Microsoft Graph API containing mail event details
     * @return a 200 OK response after processing or ignoring the notification
     */
    @POST
    @Path("/mailNotification")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response subscriptionNotification(JsonObject notification) {
        JsonArray array = notification.get(Constants.VALUE).getAsJsonArray();
        LOGGER.info("Krista received a new alert to process: {} ", array);

        for (int i = 0; i < array.size(); i++) {
            String messageId = array.get(i).getAsJsonObject().get(Constants.RESOURCE_DATA).getAsJsonObject().get(Constants.ID).getAsString();
            if (isDuplicateMessageID(messageId)) {
                LOGGER.info("Duplicate alert detected, rejecting: {} ", messageId);
                break;
            }
            FreeForm freeForm = new FreeForm();
            freeForm.put(Constants.MESSAGE_ID, Constants.TEXT, messageId);
            eventHandler.handleEvent(Constants.MAIL_RECEIVED, freeForm);
        }
        MailSubscription.createOrUpdateSubscription(baseRoutingUrl, providerFactory.create());
        LOGGER.info("Acknowledgement sent...");
        return Response.status(200).build();
    }

    /**
     * Validation endpoint for folder monitoring subscription.
     * Microsoft Graph sends a validation request when creating a subscription.
     *
     * @param validationToken the validation token from Microsoft Graph
     * @return a 200 OK response with the validation token
     */
    @POST
    @Path("/folderMonitoringNotification")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response folderMonitoringValidation(@QueryParam(Constants.VALIDATION_TOKEN) String validationToken) {
        LOGGER.info("Folder monitoring validation received: {}", validationToken);
        return Response.status(200).type(MediaType.TEXT_PLAIN).entity(validationToken.trim()).build();
    }

    /**
     * Enhanced folder monitoring notification endpoint - triggers for emails in monitored folders.
     * This endpoint supports the new "Email Folder Alert" feature with rich metadata.
     * Filters notifications based on configured monitored folders and provides comprehensive email data.
     *
     * @param notification the JSON payload from Microsoft Graph API containing mail event details
     * @return a 200 OK response after processing or ignoring the notification
     */
    @POST
    @Path("/folderMonitoringNotification")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response folderMonitoringNotification(JsonObject notification) {
        JsonArray array = notification.get(Constants.VALUE).getAsJsonArray();
        System.out.println("folderMonitoringNotification: " + array);
        LOGGER.info("Krista received a new folder monitoring alert to process: {} ", array);

        for (int i = 0; i < array.size(); i++) {
            JsonObject notificationItem = array.get(i).getAsJsonObject();
            String messageId = notificationItem.get(Constants.RESOURCE_DATA).getAsJsonObject().get(Constants.ID).getAsString();
            String changeType = notificationItem.has(Constants.CHANGE_TYPE) ? notificationItem.get(Constants.CHANGE_TYPE).getAsString() : Constants.CREATED;
            String subscriptionId = notificationItem.has(Constants.SUBSCRIPTION_ID) ? notificationItem.get(Constants.SUBSCRIPTION_ID).getAsString() : "";

            // Note: For folder monitoring, we don't use the duplicate check because:
            // 1. We monitor both "created" and "updated" events
            // 2. The same email can trigger multiple times (e.g., when moved between folders)
            // 3. We want to process each event to track folder changes
            LOGGER.info("Processing folder monitoring notification - MessageId: {}, ChangeType: {}", messageId, changeType);

            // Process the notification with full message details and folder filtering
            processEnhancedEmailNotification(messageId, changeType, subscriptionId, i);
        }
        // Note: Folder monitoring subscription is managed separately via FolderMonitoringSubscription
        LOGGER.info("Folder monitoring acknowledgement sent...");
        return Response.status(200).build();
    }

    /**
     * Process enhanced email notification by fetching full message details and filtering by monitored folders.
     * This method is used by the new folder monitoring endpoint.
     *
     * @param messageId      the ID of the message
     * @param changeType     the type of change (created or updated)
     * @param subscriptionId the subscription ID
     * @param notificationId the notification index
     */
    private void processEnhancedEmailNotification(String messageId, String changeType, String subscriptionId, int notificationId) {
        try {
            GraphServiceClientProvider provider = providerFactory.create();
            OutlookAttributes attributes = outlookAttributeStore.load(invoker.getInvokerId());

            if (attributes == null) {
                LOGGER.warn("No Outlook attributes found for invoker, skipping notification processing");
                return;
            }

            List<String> monitoredFolders = attributes.getMonitoredFolders();

            // Fetch full message details including parentFolderId
            Message message;
            try {
                message = provider.getUserRequestBuilder(null, null)
                        .messages(messageId)
                        .buildRequest(
                                new HeaderOption(Constants.PREFER, Constants.BODY_CONTENT_TYPE_HTML),
                                new QueryOption(Constants.SELECT_QUERY, Constants.MAIL_SELECT_FIELDS + ",parentFolderId")
                        )
                        .get();
            } catch (GraphServiceException gse) {
                // Handle the case where the message was deleted before we could fetch it
                if (gse.getMessage() != null && gse.getMessage().contains("ErrorItemNotFound")) {
                    LOGGER.info("Message {} was deleted or moved before we could process it (ChangeType: {}). This is normal for delete operations.",
                            messageId, changeType);
                    return;
                }
                // Re-throw other GraphServiceExceptions
                throw gse;
            }

            if (message == null) {
                LOGGER.info("Message not found for ID: {}", messageId);
                return;
            }

            String parentFolderId = message.parentFolderId;
            LOGGER.info("Message {} is in folder: {}", messageId, parentFolderId);

            // If no monitored folders configured, trigger for all messages (backward compatibility)
            boolean shouldTrigger = monitoredFolders.isEmpty();
            String folderName = "";

            if (!monitoredFolders.isEmpty() && parentFolderId != null) {
                // Get folder details to check if it's in monitored list
                try {
                    AccountImpl account = new AccountImpl(provider);
                    Folder folder = account.getFolder(parentFolderId);
                    final String currentFolderName = folder.getFolderName();
                    folderName = currentFolderName;

                    // Check if folder name matches any monitored folder (case-insensitive)
                    shouldTrigger = monitoredFolders.stream()
                            .anyMatch(monitored -> monitored.equalsIgnoreCase(currentFolderName));

                    LOGGER.info("Folder '{}' monitored: {}", folderName, shouldTrigger);
                } catch (Exception e) {
                    LOGGER.error("Error getting folder details for ID {}: {}", parentFolderId, e.getMessage());
                }
            }

            if (shouldTrigger) {
                // Build comprehensive event payload
                FreeForm freeForm = buildEmailNotificationPayload(provider, message, changeType, subscriptionId, notificationId, folderName, parentFolderId);

                LOGGER.info("Triggering enhanced email folder notification for message {} in folder '{}'", messageId, folderName);
                eventHandler.handleEvent(Constants.EMAIL_CHANGE_NOTIFICATION, freeForm);
            } else {
                LOGGER.info("Message {} in folder '{}' not in monitored folders, skipping", messageId, folderName);
            }

        } catch (Exception e) {
            LOGGER.error("Error processing email notification for message {}: {}", messageId, e.getMessage(), e);
        }
    }

    /**
     * Build comprehensive email notification payload with all required fields
     *
     * @param provider       the GraphServiceClientProvider
     * @param message        the Microsoft Graph message object
     * @param changeType     the type of change
     * @param subscriptionId the subscription ID
     * @param notificationId the notification ID
     * @param folderName     the folder name
     * @param folderId       the folder ID
     * @return FreeForm object with all notification data
     */
    private FreeForm buildEmailNotificationPayload(GraphServiceClientProvider provider, Message message, String changeType,
                                                    String subscriptionId, int notificationId, String folderName, String folderId) {
        LOGGER.info("Building email notification payload - MessageId: {}, ChangeType: {}, Folder: {} ({}), Subject: {}",
                message.id, changeType, folderName, folderId, message.subject);

        FreeForm freeForm = new FreeForm();

        // Basic notification info
        freeForm.put(Constants.NOTIFICATION_ID, Constants.TEXT, String.valueOf(notificationId));
        freeForm.put(Constants.SUBSCRIPTION_ID, Constants.TEXT, subscriptionId);
        freeForm.put(Constants.CHANGE_TYPE, Constants.TEXT, changeType);
        freeForm.put(Constants.MESSAGE_ID, Constants.TEXT, message.id);

        // Folder info
        freeForm.put(Constants.FOLDER_ID, Constants.TEXT, folderId != null ? folderId : "");
        freeForm.put(Constants.FOLDER_NAME, Constants.TEXT, folderName != null ? folderName : "");

        // Message details
        freeForm.put(Constants.SUBJECT, Constants.TEXT, message.subject != null ? message.subject : "");

        // From
        if (message.from != null && message.from.emailAddress != null) {
            freeForm.put(Constants.FROM, Constants.TEXT, message.from.emailAddress.address != null ? message.from.emailAddress.address : "");
        } else {
            freeForm.put(Constants.FROM, Constants.TEXT, "");
        }

        // To recipients
        if (message.toRecipients != null && !message.toRecipients.isEmpty()) {
            String toAddresses = message.toRecipients.stream()
                    .filter(r -> r.emailAddress != null && r.emailAddress.address != null)
                    .map(r -> r.emailAddress.address)
                    .collect(Collectors.joining(", "));
            freeForm.put(Constants.TO, Constants.TEXT, toAddresses);
        } else {
            freeForm.put(Constants.TO, Constants.TEXT, "");
        }

        // CC recipients
        if (message.ccRecipients != null && !message.ccRecipients.isEmpty()) {
            String ccAddresses = message.ccRecipients.stream()
                    .filter(r -> r.emailAddress != null && r.emailAddress.address != null)
                    .map(r -> r.emailAddress.address)
                    .collect(Collectors.joining(", "));
            freeForm.put(Constants.CC, Constants.TEXT, ccAddresses);
        } else {
            freeForm.put(Constants.CC, Constants.TEXT, "");
        }

        // BCC recipients
        if (message.bccRecipients != null && !message.bccRecipients.isEmpty()) {
            String bccAddresses = message.bccRecipients.stream()
                    .filter(r -> r.emailAddress != null && r.emailAddress.address != null)
                    .map(r -> r.emailAddress.address)
                    .collect(Collectors.joining(", "));
            freeForm.put(Constants.BCC, Constants.TEXT, bccAddresses);
        } else {
            freeForm.put(Constants.BCC, Constants.TEXT, "");
        }

        // Body
        if (message.body != null && message.body.content != null) {
            freeForm.put(Constants.BODY, Constants.TEXT, message.body.content);
        } else {
            freeForm.put(Constants.BODY, Constants.TEXT, "");
        }

        // Store attachment metadata (hasAttachments flag)
        // Note: We don't fetch actual attachment files here because this runs in a webhook context
        // without user authentication. The catalog method will fetch attachments when triggered.
        freeForm.put(Constants.ATTACHMENTS, Constants.TEXT,
                message.hasAttachments != null && message.hasAttachments ? "true" : "false");

        return freeForm;
    }

    @POST
    @Path("/lifecycleNotification")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response lifecycleValidation(@QueryParam(Constants.VALIDATION_TOKEN) String validationToken) {
        LOGGER.info("Lifecycle validation received: {} ", validationToken);
        return Response.status(200).type(MediaType.TEXT_PLAIN).entity(validationToken.trim()).build();
    }

    @POST
    @Path("/lifecycleNotification")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response lifecycleNotification(JsonObject notification) {
        LOGGER.info(" Lifecycle notification received: {} ", notification);
        this.notificationProcessQueue.add(new Notification(Notification.NotificationType.LIFECYCLE, notification));
        return Response.status(202).build();
    }

    /**
     * Validation endpoint for folder monitoring lifecycle subscription.
     * Microsoft Graph sends a validation request when creating a lifecycle subscription.
     *
     * @param validationToken the validation token from Microsoft Graph
     * @return a 200 OK response with the validation token
     */
    @POST
    @Path("/folderLifecycleNotification")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response folderLifecycleValidation(@QueryParam(Constants.VALIDATION_TOKEN) String validationToken) {
        LOGGER.info("Folder lifecycle validation received: {}", validationToken);
        return Response.status(200).type(MediaType.TEXT_PLAIN).entity(validationToken.trim()).build();
    }

    /**
     * Lifecycle notification endpoint for folder monitoring subscription.
     * Handles subscription lifecycle events like reauthorization required or subscription removed.
     *
     * @param notification the JSON payload from Microsoft Graph API containing lifecycle event details
     * @return a 202 Accepted response after queuing the notification
     */
    @POST
    @Path("/folderLifecycleNotification")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response folderLifecycleNotification(JsonObject notification) {
        LOGGER.info("Folder lifecycle notification received: {}", notification);
        this.notificationProcessQueue.add(new Notification(Notification.NotificationType.LIFECYCLE, notification));
        return Response.status(202).build();
    }

    @GET
    @Path("/docs/{subPath:.*}")
    public InputStream customTabs(@PathParam("subPath") String subPath) {
        String filePath = "ui/outlookoauth/" + (subPath.isEmpty() ? "index.html" : subPath);
        return getClass().getClassLoader().getResourceAsStream(filePath);
    }

    @POST
    @Path("/saveCredentials")
    @Produces("text/plain")
    public String saveCredentials(JsonObject authPayload) {
        decryptClientSecretIfPrivate(authPayload);
        return saveConfigurationImpl.saveCredentials(authPayload);
    }


    @POST
    @Path("/testConnection")
    @Produces("text/plain")
    public String testConnection(JsonObject authPayload) {
        decryptClientSecretIfPrivate(authPayload);
        OutlookAttributes outlookAttributes = OutlookAttributes.create(authPayload, baseRoutingUrl);
        return testConnectionService.testConnection(outlookAttributes, false);
    }

    private void decryptClientSecretIfPrivate(JsonObject authPayload) {
        if (authPayload.has(AUTH_TYPE) && Constants.PRIVATE.equals(authPayload.get(AUTH_TYPE).getAsString())) {
            if (authPayload.has(CLIENT_SECRET) && authPayload.get(CLIENT_SECRET).getAsString().contains(KRISTA_PREFIX)) {
                String encryptedSecret = authPayload.get(CLIENT_SECRET).getAsString();
                String decryptedSecret = EncryptionUtil.decrypt(encryptedSecret);
                authPayload.addProperty(CLIENT_SECRET, decryptedSecret);
            }
        }
    }

    @GET
    @javax.ws.rs.Path("/getCredentials")
    @Produces("text/plain")
    public String getCredentials(@QueryParam(AUTH_TYPE) String authType) {
        LOGGER.info("Loading attributes for auth-type: {} for invoker {}", authType, invokerId);
        final OutlookAttributes loadedAttributes = outlookAttributeStore.load(invokerId);
        if (loadedAttributes != null && authType.equals(loadedAttributes.getAuthType())) {
            Map<String, Object> attributesMap = loadedAttributes.toMap();
            String encryptedSecret = EncryptionUtil.encrypt((String) attributesMap.get(CLIENT_SECRET));
            attributesMap.put(CLIENT_SECRET, encryptedSecret);
            return GSON.toJson(attributesMap);
        }
        return "";
    }

    @GET
    @javax.ws.rs.Path("/getAuthKey")
    @Produces("text/plain")
    public String getAuthKey() {
        LOGGER.info("Loading stored authentication key.");
        final OutlookAttributes attributes = outlookAttributeStore.load(invokerId);
        if (attributes != null) {
            final String authType = attributes.getAuthType();
            return GSON.toJson(Objects.requireNonNullElse(authType, Constants.PUBLIC));
        } else {
            return "";
        }
    }

    @GET
    @Path("/listeners")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getListeners() {
        return Response.ok().entity(invoker.listEventListeners()).build();
    }

    @DELETE
    @Path("/clearListeners")
    @Produces(MediaType.APPLICATION_JSON)
    public Response clearListeners() {
        invoker.listEventListeners().forEach(l -> invoker.unregisterEventListener(l.getListenerId()));
        return Response.ok("Success").build();
    }

    /**
     * Checks if a message ID exists in the triggered mail IDs set
     *
     * @param messageId The message ID to check
     * @return true if the message ID exists in the set
     */
    public static boolean isMessageIdTriggered(String messageId) {
        return triggeredMailIds.contains(messageId);
    }

}
