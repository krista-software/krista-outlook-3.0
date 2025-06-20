package app.krista.extensions.essentials.collaboration.outlook3.impl;

import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extension.executor.ExtensionResponse;
import app.krista.extension.executor.ExtensionResponseBuilder;
import app.krista.extension.executor.Invoker;
import app.krista.extension.request.RoutingInfo;
import app.krista.extension.request.protos.http.HttpProtocol;
import app.krista.extensions.essentials.collaboration.outlook3.OutlookAttributes;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.entities.ExtensionResponseMeta;
import app.krista.extensions.essentials.collaboration.outlook3.impl.connectors.GraphServiceClientProviderFactory;
import app.krista.extensions.essentials.collaboration.outlook3.impl.connectors.OAuthService;
import app.krista.extensions.essentials.collaboration.outlook3.impl.stores.OutlookAttributeStore;
import app.krista.extensions.essentials.collaboration.outlook3.impl.util.AuthenticationResponse;
import app.krista.extensions.essentials.collaboration.outlook3.impl.util.Constants;
import app.krista.model.field.NamedValuedField;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.microsoft.graph.http.GraphServiceException;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static app.krista.extensions.essentials.collaboration.outlook3.impl.util.Constants.*;

@Service
public class TestConnectionServiceImpl {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestConnectionServiceImpl.class);

    private final GraphServiceClientProviderFactory providerFactory;
    private final OutlookAttributeStore outlookAttributeStore;
    private final String baseRoutingUrl;

    @Inject
    public TestConnectionServiceImpl(GraphServiceClientProviderFactory providerFactory,
                                     OutlookAttributeStore outlookAttributeStore,
                                     Invoker invoker) {
        this.providerFactory = providerFactory;
        this.outlookAttributeStore = outlookAttributeStore;
        this.baseRoutingUrl = invoker.getRoutingInfo().getRoutingURL(HttpProtocol.PROTOCOL_NAME, RoutingInfo.Type.APPLIANCE);
    }

    public String testConnection(OutlookAttributes outlookAttributes) {
        LOGGER.info("Testing connection with Microsoft Graph API");
        String authContextId = providerFactory.createAttributes(outlookAttributes);
        String authUrl = null;

        try {
            LOGGER.info("Verifying API access for email: {}", outlookAttributes.getEmail());
            providerFactory.create(authContextId).getGraphServiceClientForAdmin().me().mailFolders().buildRequest().get();

            if (outlookAttributes.isAllowMailAlert()) {
                LOGGER.info("Mail alerts enabled, creating subscription");
                boolean subscriptionCreated = MailSubscription.createOrUpdateSubscription(baseRoutingUrl, providerFactory.create(authContextId));
                if (!subscriptionCreated) {
                    LOGGER.error("Failed to create mail subscription");
                    return createTestConnectionResponse(false, "Connection successful but failed to create mail subscription.", null);
                }
                LOGGER.info("Mail subscription created successfully");
            } else {
                LOGGER.info("Mail alerts disabled, removing any existing subscriptions");
                boolean subscriptionDeleted = MailSubscription.deleteSubscription(baseRoutingUrl, providerFactory.create(authContextId));
                if (!subscriptionDeleted) {
                    LOGGER.error("Failed to delete mail subscription");
                }
            }
            LOGGER.info("Test connection successful");
            return createTestConnectionResponse(true, null, null);
        } catch (GraphServiceException cause) {
            LOGGER.error("Graph API connection failed: {}", cause.getMessage());
            return createTestConnectionResponse(false, "An error occurred during test connection.", null);
        } catch (MustAuthorizeException cause) {
            LOGGER.info("Authorization required, generating auth URL");
            String state = createStateParameter(cause, outlookAttributes);
            OAuth20Service oAuth20Service = new OAuthService(outlookAttributes).getOAuth20Service();
            authUrl = oAuth20Service.getAuthorizationUrl(state) + AUTH_URL_QUERY_PARAMS;
            return createTestConnectionResponse(false, AUTHORIZATION_PROMPT, authUrl);
        } finally {
            if (authUrl == null) {
                outlookAttributeStore.remove(authContextId);
            }
        }
    }

    private String createTestConnectionResponse(boolean isSuccess, String errorMessage, String url) {
        if (isSuccess) {
            return GSON.toJson(new AuthenticationResponse(true, null, null));
        } else if (url != null) {
            return GSON.toJson(new AuthenticationResponse(false, errorMessage, url));
        } else
            return GSON.toJson(new AuthenticationResponse(false,
                    Objects.requireNonNullElse(errorMessage, "Unknown Error"), null));
    }

    private String createStateParameter(MustAuthorizeException cause, OutlookAttributes outlookAttributes) {
        String userId = (String) cause.getDetails().getFirst().getValue();
        return createStateParameter(cause, outlookAttributes, userId);
    }

    private String createStateParameter(MustAuthorizeException cause, OutlookAttributes outlookAttributes, String userId) {
        Optional<NamedValuedField> authContextIdField = cause.getDetails().stream()
                .filter(namedValuedField -> Objects.equals(namedValuedField.getName(), Constants.AUTH_CONTEXT_ID))
                .findFirst();
        String state = userId;
        if (authContextIdField.isPresent()) {
            String authContextId = (String) authContextIdField.get().getValue();
            state += Constants.HASH + authContextId;
        }
        if (Constants.PUBLIC.equals(outlookAttributes.getAuthType())) {
            state += Constants.HASH + outlookAttributes.getForwardPath();
        }
        return state;
    }

    /**
     * Returns a failure response for the test connection operation, including error details,
     * timing information, and relevant connection parameters.
     *
     * @param email          the email address used for the connection test
     * @param allowMailAlert indicates if mail alert is allowed
     * @param tenantID       the tenant ID used for authentication
     * @param clientID       the client ID used for authentication
     * @return an ExtensionResponse representing the failure result
     */
    public ExtensionResponse testConnection(Boolean useStoredConfiguration, String invokerId, String email,
                                            Boolean allowMailAlert, String tenantID, String clientID,
                                            String clientSecret) {
        long startTime = System.currentTimeMillis();
        OutlookAttributes outlookAttributes = getOutlookAttributes(useStoredConfiguration, invokerId, email, allowMailAlert, tenantID, clientID, clientSecret);
        String testConnectionJsonResponse = testConnection(outlookAttributes);
        AuthenticationResponse authenticationResponse = GSON.fromJson(testConnectionJsonResponse, AuthenticationResponse.class);
        if (authenticationResponse.isSuccess()) {
            return createSuccessResponse(email, allowMailAlert, outlookAttributes.getTenantId(), outlookAttributes.getClientId(), startTime, outlookAttributes.getAuthType());
        }
        return createFailureResponse(authenticationResponse, startTime, email, allowMailAlert, tenantID, clientID);
    }

    private ExtensionResponse createFailureResponse(AuthenticationResponse authenticationResponse, long startTime,
                                                    String email, Boolean allowMailAlert, String tenantID,
                                                    String clientID) {
        ExtensionResponseMeta extensionResponseMeta = new ExtensionResponseMeta();
        extensionResponseMeta.message = authenticationResponse.getErrorMessage();
        extensionResponseMeta.technicalDetailedErrorReport = "";
        extensionResponseMeta.responseType = "FAILED";
        extensionResponseMeta.timeTakenInSeconds = (double) (System.currentTimeMillis() - startTime) / 1000;
        boolean allowMailAlertIsSuccessful = authenticationResponse.getErrorMessage().contains("Connection successful but failed to create mail subscription");
        Map<String, Object> testConnectionResponse = new HashMap<>(Map.of("Is Connection Successful", false,
                "Summary", authenticationResponse.getErrorMessage(),
                "Email", email, "Allow Mail Alert",
                allowMailAlert == null || allowMailAlert ? "Not Verified" :
                        allowMailAlertIsSuccessful ? "Successfully Verified" : "Failed to Verify",
                "Tenant ID", tenantID,
                "Client ID", clientID,
                "Mailbox Accessible", true));
        testConnectionResponse.put("Extension Response Meta", extensionResponseMeta);
        ExtensionResponse.Error error = new ExtensionResponse.Error(authenticationResponse.getErrorMessage(), System.currentTimeMillis(),
                ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR, "");
        return new ExtensionResponse(ExtensionResponse.Result.FAILURE,
                testConnectionResponse, error, null, null);
    }

    private ExtensionResponse createSuccessResponse(String email, Boolean allowMailAlert, String tenantID, String clientID, long startTime, String authType) {
        LOGGER.info("Connection tested successfully in {} ms", (System.currentTimeMillis() - startTime));
        LOGGER.info(" email : {} , allowMailAlert : {} , tenantID : {} , clientID : {} ", email, allowMailAlert, tenantID, clientID);
        ExtensionResponseMeta extensionResponseMeta = new ExtensionResponseMeta();
        extensionResponseMeta.message = "Connection tested successfully.";
        extensionResponseMeta.technicalDetailedErrorReport = "";
        extensionResponseMeta.responseType = "SUCCESS";
        extensionResponseMeta.timeTakenInSeconds = (double) (System.currentTimeMillis() - startTime) / 1000;

        Map<String, Object> testConnectionResponse = Map.of("Is Connection Successful", true,
                "Summary", "Connection tested successfully.",
                "Email", email, "Allow Mail Alert",
                allowMailAlert == null || allowMailAlert ? "Not Verified" : "Successfully Verified",
                "Tenant ID", tenantID == null ? "Not Verified" : "Successfully Verified",
                "Client ID", clientID,
                "Auth Type", authType,
                "Extension Response Meta", extensionResponseMeta,
                "Mailbox Accessible", true);

        ExtensionResponse extensionResponse = new ExtensionResponseBuilder().success(testConnectionResponse).build();
        LOGGER.info("Extension response created successfully in {} ms", (System.currentTimeMillis() - startTime));
        return extensionResponse;
    }

    private OutlookAttributes getOutlookAttributes(Boolean useStoredConfiguration, String invokerId, String email, Boolean allowMailAlert, String tenantID, String clientID, String clientSecret) {
        OutlookAttributes outlookAttributes = null;
        if (useStoredConfiguration) {
            outlookAttributes = outlookAttributeStore.load(invokerId);
        } else {
            outlookAttributes = new OutlookAttributes(clientID, clientSecret, tenantID, email, allowMailAlert, Constants.PUBLIC, baseRoutingUrl);
        }
        return outlookAttributes;
    }
}