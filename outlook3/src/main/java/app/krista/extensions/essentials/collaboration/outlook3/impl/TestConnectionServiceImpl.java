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
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.model.field.NamedValuedField;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.microsoft.graph.http.GraphServiceException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
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
    private final AuthorizationContext authorizationContext;

    @Inject
    public TestConnectionServiceImpl(GraphServiceClientProviderFactory providerFactory,
                                     OutlookAttributeStore outlookAttributeStore,
                                     Invoker invoker, AuthorizationContext authorizationContext) {
        this.providerFactory = providerFactory;
        this.outlookAttributeStore = outlookAttributeStore;
        this.baseRoutingUrl = invoker.getRoutingInfo().getRoutingURL(HttpProtocol.PROTOCOL_NAME, RoutingInfo.Type.APPLIANCE);
        this.authorizationContext = authorizationContext;
    }

    public String testConnection(OutlookAttributes outlookAttributes) {
        return testConnection(outlookAttributes, true);
    }

    public String testConnection(OutlookAttributes outlookAttributes, boolean isFromCatalog) {
        LOGGER.info("Testing connection with Microsoft Graph API");
        String authContextId = providerFactory.createAttributes(outlookAttributes);
        String authUrl = null;
        try {
            LOGGER.info("Verifying API access for email: {}", outlookAttributes.getEmail());
            if(isFromCatalog) {
                providerFactory.create(authContextId).getGraphServiceClientForUser(false, authorizationContext.getAuthorizedAccount().getAccountId()).me().mailFolders().buildRequest().get();
            }
            else {
                providerFactory.create(authContextId).getGraphServiceClientForAdmin().me().mailFolders().buildRequest().get();
            }
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
                if (MailSubscription.deleteSubscription(baseRoutingUrl, providerFactory.create(authContextId))) {
                    LOGGER.info("Subscription successfully deleted and store updated");
                } else {
                    LOGGER.error("Failed to delete mail subscription for baseRoutingUrl: {}", baseRoutingUrl);
                }
            }
            LOGGER.info("Test connection successful");
            return createTestConnectionResponse(true, null, null);
        } catch (GraphServiceException cause) {
            LOGGER.error("Graph API connection failed: {}", cause.getMessage(), cause);
            return createTestConnectionResponse(false, "An error occurred during test connection.", null);
        } catch (MustAuthorizeException cause) {
            LOGGER.error("Authorization required, generating auth URL : {} ", cause.getMessage(), cause);
            String state = createStateParameter(cause, outlookAttributes);
            OAuth20Service oAuth20Service = new OAuthService(outlookAttributes).getOAuth20Service();
            authUrl = oAuth20Service.getAuthorizationUrl(state) + AUTH_URL_QUERY_PARAMS;
            return createTestConnectionResponse(false, AUTHORIZATION_PROMPT, authUrl);
        } catch (IOException e) {
            throw new RuntimeException(e);
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

    private ExtensionResponse createExtensionResponse(AuthenticationResponse authenticationResponse, OutlookAttributes outlookAttributes, long startTime) {
        Map<String, Object> testConnectionResponse = createTestConnectionResponse(authenticationResponse, outlookAttributes, startTime);
        ExtensionResponse extensionResponse = new ExtensionResponseBuilder().success(testConnectionResponse).build();
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
        OutlookAttributes outlookAttributes = outlookAttributeStore.load(invokerId);
        ExtensionResponseMeta extensionResponse = new ExtensionResponseMeta();
        ExtensionResponse extensionResponseMeta = getExtensionResponse(outlookAttributes, extensionResponse, startTime);
        if (extensionResponseMeta != null) {
            return extensionResponseMeta;
        }
        String testConnectionJsonResponse = testConnection(outlookAttributes);
        AuthenticationResponse authenticationResponse = GSON.fromJson(testConnectionJsonResponse, AuthenticationResponse.class);
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