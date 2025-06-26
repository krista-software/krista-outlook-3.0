package app.krista.extensions.essentials.collaboration.outlook3.impl;

import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extension.executor.ExtensionResponse;
import app.krista.extension.executor.ExtensionResponseBuilder;
import app.krista.extension.executor.Invoker;
import app.krista.extension.request.RoutingInfo;
import app.krista.extension.request.protos.http.HttpProtocol;
import app.krista.extensions.essentials.collaboration.outlook3.OutlookAttributes;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.entities.ExtensionResponseMeta;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.extresp.ExtensionResponseFactory;
import app.krista.extensions.essentials.collaboration.outlook3.impl.connectors.GraphServiceClientProviderFactory;
import app.krista.extensions.essentials.collaboration.outlook3.impl.connectors.OAuthService;
import app.krista.extensions.essentials.collaboration.outlook3.impl.stores.OutlookAttributeStore;
import app.krista.extensions.essentials.collaboration.outlook3.impl.util.AuthenticationResponse;
import app.krista.extensions.essentials.collaboration.outlook3.impl.util.Constants;
import app.krista.ksdk.telemetry.TelemetryMetrics;
import app.krista.model.field.NamedValuedField;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.microsoft.graph.http.GraphServiceException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
    private final TelemetryMetrics telemetryMetrics;

    @Inject
    public TestConnectionServiceImpl(GraphServiceClientProviderFactory providerFactory,
                                     OutlookAttributeStore outlookAttributeStore,
                                     Invoker invoker, TelemetryMetrics telemetryMetrics) {
        this.providerFactory = providerFactory;
        this.outlookAttributeStore = outlookAttributeStore;
        this.baseRoutingUrl = invoker.getRoutingInfo().getRoutingURL(HttpProtocol.PROTOCOL_NAME, RoutingInfo.Type.APPLIANCE);
        this.telemetryMetrics = telemetryMetrics;
    }

    public String testConnection(OutlookAttributes outlookAttributes) {
        LOGGER.info("Testing connection with Microsoft Graph API");
        long startTime = System.currentTimeMillis();
        String authContextId = providerFactory.createAttributes(outlookAttributes);
        String authUrl = null;
        
        // Record test connection attempt
        telemetryMetrics.incrementCounter("outlook3.testConnection.attempt", 1, 
                safeTagMap("email", outlookAttributes.getEmail(), 
                          "auth_type", outlookAttributes.getAuthType(),
                          "allow_mail_alert", String.valueOf(outlookAttributes.isAllowMailAlert())));
        
        try {
            LOGGER.info("Verifying API access for email: {}", outlookAttributes.getEmail());
            providerFactory.create(authContextId).getGraphServiceClientForAdmin().me().mailFolders().buildRequest().get();

            if (outlookAttributes.isAllowMailAlert()) {
                LOGGER.info("Mail alerts enabled, creating subscription");
                boolean subscriptionCreated = MailSubscription.createOrUpdateSubscription(baseRoutingUrl, providerFactory.create(authContextId));
                if (!subscriptionCreated) {
                    LOGGER.error("Failed to create mail subscription");
                    
                    // Record subscription creation failure
                    telemetryMetrics.incrementCounter("outlook3.testConnection.subscription.createFailed", 1,
                            safeTagMap("email", outlookAttributes.getEmail()));
                    
                    return createTestConnectionResponse(false, "Connection successful but failed to create mail subscription.", null);
                }
                LOGGER.info("Mail subscription created successfully");
                
                // Record subscription creation success
                telemetryMetrics.incrementCounter("outlook3.testConnection.subscription.createSuccess", 1,
                        safeTagMap("email", outlookAttributes.getEmail()));
            } else {
                LOGGER.info("Mail alerts disabled, removing any existing subscriptions");
                boolean subscriptionDeleted = MailSubscription.deleteSubscription(baseRoutingUrl, providerFactory.create(authContextId));
                if (!subscriptionDeleted) {
                    LOGGER.error("Failed to delete mail subscription");
                    
                    // Record subscription deletion failure
                    telemetryMetrics.incrementCounter("outlook3.testConnection.subscription.deleteFailed", 1,
                            safeTagMap("email", outlookAttributes.getEmail()));
                } else {
                    // Record subscription deletion success
                    telemetryMetrics.incrementCounter("outlook3.testConnection.subscription.deleteSuccess", 1,
                            safeTagMap("email", outlookAttributes.getEmail()));
                }
            }
            LOGGER.info("Test connection successful");
            
            // Record successful connection
            telemetryMetrics.incrementCounter("outlook3.testConnection.success", 1,
                    safeTagMap("email", outlookAttributes.getEmail(), 
                              "auth_type", outlookAttributes.getAuthType()));
            telemetryMetrics.recordDuration("outlook3.testConnection.duration", System.currentTimeMillis() - startTime,
                    safeTagMap("email", outlookAttributes.getEmail(), 
                              "status", "success"));
            
            return createTestConnectionResponse(true, null, null);
        } catch (GraphServiceException cause) {
            LOGGER.error("Graph API connection failed: {}", cause.getMessage());
            
            // Record Graph API error
            telemetryMetrics.incrementCounter("outlook3.testConnection.graphApiError", 1,
                    safeTagMap("email", outlookAttributes.getEmail(), 
                              "error_message", cause.getMessage(),
                              "error_code", cause.getServiceError() != null ? 
                                      cause.getServiceError().code : "unknown"));
            telemetryMetrics.recordDuration("outlook3.testConnection.duration", System.currentTimeMillis() - startTime,
                    safeTagMap("email", outlookAttributes.getEmail(), 
                              "status", "error",
                              "error_type", "graph_api"));
            
            return createTestConnectionResponse(false, "An error occurred during test connection.", null);
        } catch (MustAuthorizeException cause) {
            LOGGER.info("Authorization required, generating auth URL");
            String state = createStateParameter(cause, outlookAttributes);
            OAuth20Service oAuth20Service = new OAuthService(outlookAttributes).getOAuth20Service();
            authUrl = oAuth20Service.getAuthorizationUrl(state) + AUTH_URL_QUERY_PARAMS;
            
            // Record authorization required
            telemetryMetrics.incrementCounter("outlook3.testConnection.authorizationRequired", 1,
                    safeTagMap("email", outlookAttributes.getEmail(), 
                              "auth_type", outlookAttributes.getAuthType()));
            telemetryMetrics.recordDuration("outlook3.testConnection.duration", System.currentTimeMillis() - startTime,
                    safeTagMap("email", outlookAttributes.getEmail(), 
                              "status", "auth_required"));
            
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
     * Helper method to create a map of tags with safe values (no nulls).
     */
    private Map<String, String> safeTagMap(String... keysAndValues) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < keysAndValues.length - 1; i += 2) {
            map.put(keysAndValues[i], keysAndValues[i + 1] != null ? keysAndValues[i + 1] : "NA");
        }
        return map;
    }

    private ExtensionResponse createExtensionResponse(AuthenticationResponse authenticationResponse, OutlookAttributes outlookAttributes, long startTime) {
        Map<String, Object> testConnectionResponse = createTestConnectionResponse(authenticationResponse, outlookAttributes, startTime);
        ExtensionResponse extensionResponse = new ExtensionResponseBuilder().success(testConnectionResponse).build();
        
        // Record total time to create response
        telemetryMetrics.recordDuration("outlook3.testConnection.totalTime", System.currentTimeMillis() - startTime,
                safeTagMap("email", outlookAttributes.getEmail(), 
                          "is_success", String.valueOf(authenticationResponse.isSuccess())));
        
        LOGGER.info("Extension response created in {} ms", (System.currentTimeMillis() - startTime));
        return extensionResponse;
    }

    @NotNull
    private static Map<String, Object> createTestConnectionResponse(AuthenticationResponse authenticationResponse, OutlookAttributes outlookAttributes, long startTime) {
        boolean allowMailAlertIsSuccessful = !authenticationResponse.isSuccess() && authenticationResponse.getErrorMessage() != null &&
                authenticationResponse.getErrorMessage().contains("Connection successful but failed to create mail subscription");

        ExtensionResponseMeta extensionResponseMeta = new ExtensionResponseMeta();
        extensionResponseMeta.message = authenticationResponse.isSuccess() ? "Connection successful" : "Connection failed";
        extensionResponseMeta.technicalDetailedErrorReport = "";
        extensionResponseMeta.responseType = authenticationResponse.isSuccess() ? "SUCCESS" : "FAILED";
        extensionResponseMeta.timeTakenInSeconds = (double) (System.currentTimeMillis() - startTime) / 1000;

        Map<String, Object> testConnectionSummary = Map.of(
                "Summary", authenticationResponse.isSuccess() ? "Connection successful" : authenticationResponse.getErrorMessage(),
                "Email", outlookAttributes.getEmail(),
                "Allow Mail Alert", outlookAttributes.isAllowMailAlert(),
                "Tenant ID", outlookAttributes.getTenantId() == null ? "Krista Public Tenant" : outlookAttributes.getTenantId(),
                "Client ID", outlookAttributes.getClientId(),
                "Auth Type", outlookAttributes.getAuthType(),
                "Mailbox Accessible", authenticationResponse.isSuccess(),
                "Allow Mail Alert Is Successful", allowMailAlertIsSuccessful
        );
        return Map.of("Is Connection Successful", authenticationResponse.isSuccess(),
                "Test Connection Summary", testConnectionSummary,
                "Extension Response Meta", extensionResponseMeta);
    }

    /**
     * Tests the connection to Microsoft Graph API using the provided Outlook attributes.
     * Verifies API access, manages mail alert subscriptions based on configuration, and handles authorization requirements.
     *
     * @param invokerId invokerID
     * @return a JSON string representing the result of the connection test, including success status, error message, and authorization URL if needed
     */
    public ExtensionResponse testConnection(String invokerId) {
        long startTime = System.currentTimeMillis();
        
        // Record test connection by invoker ID
        telemetryMetrics.incrementCounter("outlook3.testConnection.byInvokerId", 1,
                Map.of("invoker_id", invokerId));
        
        OutlookAttributes outlookAttributes = outlookAttributeStore.load(invokerId);
        ExtensionResponseMeta extensionResponse = new ExtensionResponseMeta();
        ExtensionResponse extensionResponseMeta = getExtensionResponse(outlookAttributes, extensionResponse, startTime);
        if (extensionResponseMeta != null) {
            // Record not configured
            telemetryMetrics.incrementCounter("outlook3.testConnection.notConfigured", 1,
                    Map.of("invoker_id", invokerId));
            telemetryMetrics.recordDuration("outlook3.testConnection.duration", System.currentTimeMillis() - startTime,
                    Map.of("invoker_id", invokerId, "status", "not_configured"));
            
            return extensionResponseMeta;
        }
        String testConnectionJsonResponse = testConnection(outlookAttributes);
        AuthenticationResponse authenticationResponse = GSON.fromJson(testConnectionJsonResponse, AuthenticationResponse.class);
        
        // Record response creation
        telemetryMetrics.recordDuration("outlook3.testConnection.responseCreation", System.currentTimeMillis() - startTime,
                safeTagMap("invoker_id", invokerId, 
                          "email", outlookAttributes.getEmail(),
                          "is_success", String.valueOf(authenticationResponse.isSuccess())));
        
        return createExtensionResponse(authenticationResponse, outlookAttributes, startTime);
    }

    @Nullable
    private static ExtensionResponse getExtensionResponse(OutlookAttributes outlookAttributes, ExtensionResponseMeta extensionResponse, long startTime) {
        if (outlookAttributes == null) {
            extensionResponse.message = "Authentication failed.Attributes not found.";
            extensionResponse.technicalDetailedErrorReport = "Outlook is not configured";
            extensionResponse.responseType = "FAILED";
            extensionResponse.timeTakenInSeconds = (double) (System.currentTimeMillis() - startTime) / 1000;
            Map<String, Object> testConnectionSummary = Map.of(
                    "Summary", "Outlook is not configured",
                    "Email", "Not Configured",
                    "Allow Mail Alert", "Not Configured",
                    "Tenant ID", "Not Configured",
                    "Client ID", "Not Configured",
                    "Auth Type", "Not Configured",
                    "Mailbox Accessible", "Not Configured",
                    "Allow Mail Alert Is Successful", "Not Configured"
            );
            return ExtensionResponseFactory.create(Map.of("Extension Response Meta", extensionResponse,
                    "Test Connection Summary", testConnectionSummary,
                    "Is Connection Successful", false));
        }
        return null;
    }
}