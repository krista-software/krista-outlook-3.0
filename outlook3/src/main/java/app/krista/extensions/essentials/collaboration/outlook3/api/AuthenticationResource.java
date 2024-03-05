package app.krista.extensions.essentials.collaboration.outlook3.api;

import app.krista.extension.executor.Invoker;
import app.krista.extensions.essentials.collaboration.outlook3.OutlookAttributes;
import app.krista.extensions.essentials.collaboration.outlook3.impl.connectors.GraphServiceClientProvider;
import app.krista.extensions.essentials.collaboration.outlook3.impl.connectors.GraphServiceClientProviderFactory;
import app.krista.extensions.essentials.collaboration.outlook3.impl.stores.OutlookAttributeStore;
import app.krista.extensions.essentials.collaboration.outlook3.impl.stores.RefreshTokenStore;
import app.krista.extensions.essentials.collaboration.outlook3.impl.util.Constants;
import app.krista.extensions.essentials.collaboration.outlook3.impl.util.Notification;
import app.krista.extensions.essentials.collaboration.outlook3.impl.util.NotificationProcessQueue;
import app.krista.extensions.util.EventHandler;
import app.krista.ksdk.authentication.AuthorizationListener;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.model.base.FreeForm;
import com.github.scribejava.core.exceptions.OAuthException;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.microsoft.graph.http.GraphServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static com.github.scribejava.core.model.OAuthConstants.CODE;
import static com.github.scribejava.core.model.OAuthConstants.STATE;

@Path("/")
public final class AuthenticationResource {

    public static final String USER_AUTHENTICATED_SUCCESSFULLY_PLEASE_PROCEED_WITH_REQUEST = "User Authenticated Successfully. Please proceed with the request.";
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationResource.class);
    private static final Set<String> triggeredMailIds = new LinkedHashSet<>();
    private static final int MESSAGE_ID_CAPACITY = 1000;
    private final OutlookAttributeStore outlookAttributeStore;
    private final RefreshTokenStore refreshTokenStore;
    private final GraphServiceClientProviderFactory providerFactory;
    private final EventHandler eventHandler;
    private final NotificationProcessQueue notificationProcessQueue;
    private final AuthorizationContext context;
    private final AuthorizationListener authorizationListener;


    @Inject
    public AuthenticationResource(OutlookAttributeStore outlookAttributeStore, RefreshTokenStore refreshTokenStore, GraphServiceClientProviderFactory providerFactory, EventHandler eventHandler, Invoker invoker, AuthorizationContext context, AuthorizationListener authorizationListener) {
        this.outlookAttributeStore = outlookAttributeStore;
        this.refreshTokenStore = refreshTokenStore;
        this.providerFactory = providerFactory;
        this.eventHandler = eventHandler;
        this.context = context;
        this.authorizationListener = authorizationListener;
        this.notificationProcessQueue = new NotificationProcessQueue(providerFactory, invoker);
    }

    @GET
    @Path("v3/oauth/callback|callback")
    public String getCallBack(@QueryParam(CODE) String code, @QueryParam(STATE) String state) {
        Objects.requireNonNull(code);
        String[] parts = state.split(Constants.HASH);
        if (parts[0].isBlank() || parts.length > 3) {
            throw new BadRequestException(Constants.INVALID_STATE_PARAMETERS);
        }
        String key = parts[0];
        String authContextId = parts.length == 3 ? parts[1] : null;
        try {
            GraphServiceClientProvider clientProvider = getGraphServiceClientProvider(authContextId);
            OAuth2AccessToken accessToken = clientProvider.getOutlookAttributes().getOAuth20Service().getAccessToken(code);
            refreshTokenStore.put(key, accessToken.getRefreshToken());
            if (!key.startsWith(Constants.WS_CONTACT) && !hasUserAccess(clientProvider)) {
                refreshTokenStore.remove(key);
                return Constants.UNAUTHORISED_USER + " User email '" + key + "' configured in the setup does not match with authenticated user.";
            }
            if (context.isAuthenticated()) {
                authorizationListener.authorized();
                return USER_AUTHENTICATED_SUCCESSFULLY_PLEASE_PROCEED_WITH_REQUEST;
            }
            return Constants.USER_AUTHENTICATED_SUCCESSFULLY_SAVE_THE_CHANGES;
        } catch (OAuthException cause) {
            throw new IllegalStateException(getErrorDescription(cause), cause.getCause());
        } catch (IOException | ExecutionException cause) {
            throw new IllegalStateException(Constants.ERROR_OCCURRED_DURING_AUTHORIZATION, cause.getCause());
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(Constants.ERROR_OCCURRED_DURING_AUTHORIZATION, interruptedException.getCause());
        } finally {
            if (authContextId != null) {
                outlookAttributeStore.remove(authContextId);
            }
        }
    }

    private GraphServiceClientProvider getGraphServiceClientProvider(String authContextId) {
        GraphServiceClientProvider clientProvider;
        if (authContextId == null) {
            clientProvider = providerFactory.create();
        } else {
            OutlookAttributes effectiveOutlookAttributes = outlookAttributeStore.load(authContextId);
            clientProvider = providerFactory.create(effectiveOutlookAttributes);
        }
        return clientProvider;
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
            LOGGER.debug(cause.getMessage());
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
        for (String messageId : new ArrayList<>(triggeredMailIds).subList(0, messageIDCount)) {
            triggeredMailIds.remove(messageId);
        }
    }

    @POST
    @Path("/mailNotification")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response subscriptionValidation(@QueryParam(Constants.VALIDATION_TOKEN) String validationToken) {
        return Response.status(200).type(MediaType.TEXT_PLAIN).entity(validationToken.trim()).build();
    }

    @POST
    @Path("/mailNotification")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response subscriptionNotification(JsonObject notification) {
        JsonArray array = notification.get(Constants.VALUE).getAsJsonArray();
        for (int i = 0; i < array.size(); i++) {
            String messageId = array.get(i).getAsJsonObject().get(Constants.RESOURCE_DATA).getAsJsonObject().get(Constants.ID).getAsString();
            //when mailNotification endpoint is called two times or more by Microsoft GraphAPI on same messageId which causes mail receive alert wait for event triggered two times or more, to avoid such case we are using isDuplicateMessageID to break the loop.
            if (isDuplicateMessageID(messageId)) {
                break;
            }
            FreeForm freeForm = new FreeForm();
            freeForm.put(Constants.MESSAGE_ID, Constants.TEXT, messageId);
            eventHandler.handleEvent(Constants.MAIL_RECEIVED, freeForm);
        }
        return Response.status(200).build();
    }

    @POST
    @Path("/lifecycleNotification")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response lifecycleValidation(@QueryParam(Constants.VALIDATION_TOKEN) String validationToken) {
        return Response.status(200).type(MediaType.TEXT_PLAIN).entity(validationToken.trim()).build();
    }

    @POST
    @Path("/lifecycleNotification")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response lifecycleNotification(JsonObject notification) {
        this.notificationProcessQueue.add(new Notification(Notification.NotificationType.LIFECYCLE, notification));
        return Response.status(202).build();
    }
}
