/*
 * Outlook 3.0 Extension for Krista
 * Copyright (C) 2025 Krista Software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package app.krista.extensions.essentials.collaboration.outlook3.impl;

import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extension.executor.ExtensionResponse;
import app.krista.extension.executor.ExtensionResponseBuilder;
import app.krista.extension.executor.Invoker;
import app.krista.extension.request.RoutingInfo;
import app.krista.extension.request.protos.http.HttpProtocol;
import app.krista.extensions.essentials.collaboration.outlook3.OutlookAttributes;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.entities.ExtensionResponseMeta;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.validators.OutlookCredentialValidator;
import app.krista.extensions.essentials.collaboration.outlook3.impl.connectors.GraphServiceClientProviderFactory;
import app.krista.extensions.essentials.collaboration.outlook3.impl.stores.OutlookAttributeStore;
import app.krista.extensions.essentials.collaboration.outlook3.impl.util.AuthenticationResponse;
import app.krista.extensions.essentials.collaboration.outlook3.impl.util.Constants;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.model.field.NamedValuedField;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static app.krista.extensions.essentials.collaboration.outlook3.impl.util.Constants.*;

@Service
public class SaveConfigurationImpl {

    public static final Logger LOGGER = LoggerFactory.getLogger(SaveConfigurationImpl.class);
    private final GraphServiceClientProviderFactory providerFactory;
    private final OutlookAttributeStore outlookAttributeStore;
    private final SubscriptionCleanupService subscriptionCleanupService;
    private final String baseRoutingUrl;
    private final String invokerId;
    private final TestConnectionServiceImpl testConnectionServiceImpl;
    private final AuthorizationContext authorizationContext;
    private String publicClientId;
    private String publicClientSecret;
    private final Invoker invoker;

    @Inject
    public SaveConfigurationImpl(GraphServiceClientProviderFactory providerFactory,
                                 OutlookAttributeStore outlookAttributeStore,
                                 SubscriptionCleanupService subscriptionCleanupService,
                                 Invoker invoker, TestConnectionServiceImpl testConnectionServiceImpl, AuthorizationContext authorizationContext) {
        this.providerFactory = providerFactory;
        this.outlookAttributeStore = outlookAttributeStore;
        this.subscriptionCleanupService = subscriptionCleanupService;
        this.baseRoutingUrl = invoker.getRoutingInfo().getRoutingURL(HttpProtocol.PROTOCOL_NAME, RoutingInfo.Type.APPLIANCE);
        this.invokerId = invoker.getInvokerId();
        this.testConnectionServiceImpl = testConnectionServiceImpl;
        this.authorizationContext = authorizationContext;
        this.invoker = invoker;
        loadPublicConfig();
    }

    private void loadPublicConfig() {
        Properties properties = new Properties();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            properties.load(inputStream);
            this.publicClientId = properties.getProperty("public.clientId");
            this.publicClientSecret = properties.getProperty("public.clientSecret");
        } catch (IOException cause) {
            throw new RuntimeException("Failed to load public configuration file.", cause);
        }
    }

    public ExtensionResponse saveConfiguration(JsonObject authPayload) {
        long startTime = System.currentTimeMillis();
        OutlookAttributes attributes = OutlookAttributes.create(authPayload, baseRoutingUrl);
        if (Constants.PRIVATE.equals(attributes.getAuthType())) {
            OutlookCredentialValidator validator = new OutlookCredentialValidator();
            validator.validateToken(attributes.getClientId(), attributes.getClientSecret(), attributes.getTenantId());
        }

        String testConnectionResult = testConnectionServiceImpl.testConnection(attributes);
        AuthenticationResponse testResponse = Constants.GSON.fromJson(testConnectionResult, AuthenticationResponse.class);
        if (!testResponse.isSuccess()) {
            String authContextId = providerFactory.createAttributes(attributes);
            List<NamedValuedField> details = getNamedValuedFields(authorizationContext.getAuthorizedAccount().getAccountId(), attributes, authContextId);
            throw new MustAuthorizeException(Constants.AUTHORIZATION_PROMPT, details);
        }

        String saveResult = saveCredentials(authPayload);
        AuthenticationResponse authenticationResponse = GSON.fromJson(saveResult, AuthenticationResponse.class);
        if (authenticationResponse.isSuccess()) {
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
            // Handle subscription cleanup for credential changes
            subscriptionCleanupService.handleCredentialChange(attributes, baseRoutingUrl, invokerId);
            // Save new credentials
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
