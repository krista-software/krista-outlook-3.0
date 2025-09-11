package app.krista.extensions.essentials.collaboration.outlook3.impl.util;

import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extension.executor.ExtensionResponse;
import app.krista.extension.executor.ExtensionResponseBuilder;
import app.krista.extension.executor.Invoker;
import app.krista.extension.request.RoutingInfo;
import app.krista.extension.request.protos.http.HttpProtocol;
import app.krista.extensions.essentials.collaboration.outlook3.OutlookAttributes;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.entities.ExtensionResponseMeta;
import app.krista.extensions.essentials.collaboration.outlook3.impl.TestConnectionServiceImpl;
import app.krista.extensions.essentials.collaboration.outlook3.impl.connectors.GraphServiceClientProviderFactory;
import app.krista.extensions.essentials.collaboration.outlook3.impl.stores.OutlookAttributeStore;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.model.field.NamedValuedField;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.io.IOException;
import java.io.InputStream;

import static app.krista.extensions.essentials.collaboration.outlook3.impl.util.Constants.*;

@Service
public class SaveConfigurationImpl {

    public static final Logger LOGGER = LoggerFactory.getLogger(SaveConfigurationImpl.class);
    private final GraphServiceClientProviderFactory providerFactory;
    private final OutlookAttributeStore outlookAttributeStore;
    private final String baseRoutingUrl;
    private final String invokerId;
    private final TestConnectionServiceImpl testConnectionServiceImpl;
    private final AuthorizationContext authorizationContext;
    private String publicClientId;
    private String publicClientSecret;

    @Inject
    public SaveConfigurationImpl(GraphServiceClientProviderFactory providerFactory,
                                 OutlookAttributeStore outlookAttributeStore,
                                 Invoker invoker, TestConnectionServiceImpl testConnectionServiceImpl, AuthorizationContext authorizationContext) {
        this.providerFactory = providerFactory;
        this.outlookAttributeStore = outlookAttributeStore;
        this.baseRoutingUrl = invoker.getRoutingInfo().getRoutingURL(HttpProtocol.PROTOCOL_NAME, RoutingInfo.Type.APPLIANCE);
        this.invokerId = invoker.getInvokerId();
        this.testConnectionServiceImpl = testConnectionServiceImpl;
        this.authorizationContext = authorizationContext;
        loadPublicConfig();
    }

    private void loadPublicConfig() {
        Properties properties = new Properties();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            properties.load(inputStream);
            this.publicClientId = properties.getProperty("public.clientId");
            this.publicClientSecret = properties.getProperty("public.clientSecret");
            System.out.println("Public Client ID: " + publicClientId + " Public Client Secret: " + publicClientSecret);
        } catch (IOException cause) {
            throw new RuntimeException("Failed to load public configuration file.", cause);
        }
    }

    public ExtensionResponse outlookPublicConfiguration(String email, boolean mailAlert) {
        JsonObject publicPayload = new JsonObject();
        publicPayload.addProperty(AUTH_TYPE, PUBLIC);
        publicPayload.addProperty(EMAIL, email);
        publicPayload.addProperty(CLIENT_ID, publicClientId);
        publicPayload.addProperty(CLIENT_SECRET, publicClientSecret);
        publicPayload.addProperty(ALLOW_MAIL_ALERT, mailAlert);

        return saveConfiguration(publicPayload);
    }

    public ExtensionResponse outlookPrivateConfiguration(String email, String clientId, String clientSecret, String tenantId, boolean mailAlert) {
        JsonObject privatePayload = new JsonObject();
        privatePayload.addProperty(AUTH_TYPE, PRIVATE);
        privatePayload.addProperty(EMAIL, email);
        privatePayload.addProperty(CLIENT_ID, clientId);
        privatePayload.addProperty(CLIENT_SECRET, clientSecret);
        privatePayload.addProperty(TENANT_ID, tenantId);
        privatePayload.addProperty(ALLOW_MAIL_ALERT, mailAlert);

        return saveConfiguration(privatePayload);
    }

    public ExtensionResponse saveConfiguration(JsonObject authPayload) {
        LOGGER.info("Saving Outlook Attributes: {}", authPayload);
        long startTime = System.currentTimeMillis();
        OutlookAttributes attributes = OutlookAttributes.create(authPayload, baseRoutingUrl);
        String testConnectionResult = testConnectionServiceImpl.testConnection(attributes);

        AuthenticationResponse testResponse = Constants.GSON.fromJson(testConnectionResult, AuthenticationResponse.class);

        if (!testResponse.isSuccess()) {
            String authContextId = providerFactory.createAttributes(attributes);
            List<NamedValuedField> details = getNamedValuedFields(authorizationContext.getAuthorizedAccount().getAccountId(), attributes, authContextId);
            throw new MustAuthorizeException(Constants.AUTHORIZATION_PROMPT, details);
        }

        LOGGER.info("Saving Outlook Attributes: {}", attributes);
        String saveResult = saveCredentials(authPayload);

        if (saveResult.contains("true")) {
            ExtensionResponseMeta extensionResponseMeta = new ExtensionResponseMeta();
            extensionResponseMeta.message = "Outlook Attributes Saved Successfully";
            extensionResponseMeta.technicalDetailedErrorReport = "";
            extensionResponseMeta.responseType = SUCCESS;
            extensionResponseMeta.timeTakenInSeconds = (double) ((System.currentTimeMillis() - startTime) / 1000);
            Map<String, Object> responseData = new HashMap<>();
            responseData.put(IS_CONFIGURATION_SUCCESSFUL, true);
            responseData.put(EXTENSION_RESPONSE_META, extensionResponseMeta);
            return new ExtensionResponseBuilder().success(responseData).build();
        }
        return new ExtensionResponseBuilder().failure("Failed to Configure Outlook Attributes").build();
    }

    @NotNull
    private static List<NamedValuedField> getNamedValuedFields(String accountId, OutlookAttributes attributes, String authContextId) {
        List<NamedValuedField> details = new ArrayList<>();
        NamedValuedField clientUserIdField = new NamedValuedField(Constants.CLIENT_USER_ID, Constants.TEXT, accountId, new HashMap<>(), new HashMap<>());
        details.add(clientUserIdField);
        String userId = attributes.getEmail() + Constants.UNDER_SCORE + attributes.getClientId() + Constants.UNDER_SCORE + attributes.getAuthType();
        NamedValuedField userIdField = new NamedValuedField(Constants.USER_ID, Constants.TEXT, userId, new HashMap<>(), new HashMap<>());
        details.add(userIdField);
        if (authContextId != null) {
            NamedValuedField contextIdField = new NamedValuedField(Constants.AUTH_CONTEXT_ID, Constants.TEXT, authContextId, new HashMap<>(), new HashMap<>());
            details.add(contextIdField);
        }
        return details;
    }

    public String saveCredentials(JsonObject authPayload) {
        OutlookAttributes attributes = OutlookAttributes.create(authPayload, baseRoutingUrl);
        String authContextId = providerFactory.createAttributes(attributes);
        try {
            providerFactory.create(authContextId).getGraphServiceClientForAdmin()
                    .users(attributes.getEmail())
                    .mailFolders().buildRequest().get();
            boolean isSaved = outlookAttributeStore.save(attributes, invokerId);
            return isSaved
                    ? Constants.GSON.toJson(new AuthenticationResponse(true, null, null))
                    : Constants.GSON.toJson(new AuthenticationResponse(false, FAILED_TO_SAVE_ATTRIBUTES, null));
        } catch (Exception cause) {
            LOGGER.error(FAILED_TO_SAVE_ATTRIBUTES + ": {} ", cause.getMessage(), cause);
            return Constants.GSON.toJson(new AuthenticationResponse(false, FAILED_TO_SAVE_ATTRIBUTES, null));
        } finally {
            outlookAttributeStore.remove(authContextId);
        }
    }

}
