package app.krista.extensions.essentials.collaboration.outlook3.impl.connectors;

import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extensions.essentials.collaboration.outlook3.OutlookAttributes;
import app.krista.extensions.essentials.collaboration.outlook3.impl.stores.RefreshTokenStore;
import app.krista.extensions.essentials.collaboration.outlook3.impl.util.AuthErrorRule;
import app.krista.extensions.essentials.collaboration.outlook3.impl.util.Constants;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.ksdk.context.RequestContext;
import app.krista.model.field.NamedValuedField;
import com.google.gson.JsonObject;
import com.microsoft.aad.msal4j.*;
import com.microsoft.graph.authentication.IAuthenticationProvider;
import com.microsoft.graph.core.ClientException;
import com.microsoft.graph.requests.GraphServiceClient;
import com.microsoft.graph.requests.UserRequestBuilder;
import okhttp3.Request;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static app.krista.extensions.essentials.collaboration.outlook3.impl.util.Constants.*;

public class GraphServiceClientProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(GraphServiceClientProvider.class);
    private final RefreshTokenStore refreshTokenStore;
    private final OutlookAttributes attributes;
    private final RequestContext requestContext;
    private final AuthorizationContext authorizationContext;
    private final String authContextId;


    @Inject
    public GraphServiceClientProvider(RefreshTokenStore refreshTokenStore, OutlookAttributes attributes,
                                      RequestContext requestContext, AuthorizationContext authorizationContext) {
        this(refreshTokenStore, attributes, requestContext, authorizationContext, null);
    }

    @Inject
    public GraphServiceClientProvider(RefreshTokenStore refreshTokenStore, OutlookAttributes attributes,
                                      RequestContext requestContext, AuthorizationContext authorizationContext,
                                      String authContextId) {
        this.refreshTokenStore = refreshTokenStore;
        this.attributes = attributes;
        this.requestContext = requestContext;
        this.authorizationContext = authorizationContext;
        this.authContextId = authContextId;
    }

    public OutlookAttributes getOutlookAttributes() {
        return attributes;
    }

    public GraphServiceClient<Request> getGraphServiceClientForUser(Boolean useSetupEmail, String accountID) throws IOException {
        if (useSetupEmail != null) {
            return getGraphServiceClient(useSetupEmail, useSetupEmail ? null : accountID);
        } else {
            return getGraphServiceClient(!requestContext.invokeAsUser(), null);
        }
    }

    public GraphServiceClient<Request> getGraphServiceClientForAdmin() {
        return getGraphServiceClient(true, null);
    }

    private GraphServiceClient<Request> getGraphServiceClient(boolean useSetupEmail, String accountID) {
        try {
            String userId = getUserId(useSetupEmail, accountID);
            String refreshTokenStoreKey = getRefTokenStoreKey(userId);
            String refreshToken = refreshTokenStore.get(refreshTokenStoreKey);
            if (refreshToken == null) {
                throw createMustAuthorizationException(refreshTokenStoreKey, false);
            }
            return getGraphServiceClient(refreshTokenStoreKey, refreshToken);
        } catch (RuntimeException cause) {
            throw cause;
        }
    }

    private GraphServiceClient<Request> getGraphServiceClient(String refreshTokenStoreKey, String refreshToken) {
        try {
            String[] scopes = Constants.REQUIRED_SCOPE.split(Constants.SCOPE_SEPARATOR);
            Set<String> scopeSet = Arrays.stream(scopes).collect(Collectors.toSet());
            RefreshTokenParameters refreshTokenParameters = RefreshTokenParameters.builder(scopeSet, refreshToken).build();
            IClientCredential clientCredential = ClientCredentialFactory.createFromSecret(attributes.getClientSecret());
            String authority;
            if (PRIVATE.equals(attributes.getAuthType())) {
                authority = Constants.AUTHORITY + attributes.getTenantId();
            } else if (PUBLIC.equals(attributes.getAuthType())) {
                authority = Constants.ORG_AUTHORITY;
            } else {
                throw new IllegalArgumentException("No authentication type found.");
            }
            ConfidentialClientApplication confidentialClientApplication = ConfidentialClientApplication.builder
                    (attributes.getClientId(), clientCredential).authority(authority).build();
            IAuthenticationResult authenticationResult = confidentialClientApplication.acquireToken(refreshTokenParameters).get();
            final String cachedTokenContent = confidentialClientApplication.tokenCache().serialize();
            updateRefreshToken(refreshTokenStoreKey, Constants.GSON.fromJson(cachedTokenContent, JsonObject.class));
            return GraphServiceClient.builder()
                    .authenticationProvider(new GraphServiceClientAuthenticationProvider(authenticationResult.accessToken()))
                    .buildClient();
        } catch (ClientException | ExecutionException | MalformedURLException cause) {
            handleAuthenticationError(cause, refreshTokenStoreKey);
            // This line will never be reached as handleAuthenticationError always throws an exception
            return null;
        } catch (InterruptedException cause) {
            Thread.currentThread().interrupt();
            throw createMustAuthorizationException(refreshTokenStoreKey, true);
        }
    }

    private void updateRefreshToken(String userId, JsonObject jsonObject) {
        JsonObject refreshToken = jsonObject.get("RefreshToken").getAsJsonObject();
        String firstKey = refreshToken.keySet().iterator().next();
        JsonObject tokenData = refreshToken.getAsJsonObject(firstKey);
        String secret = tokenData.get("secret").getAsString();
        refreshTokenStore.put(userId, secret);
        LOGGER.info("Updated refresh token.");
    }

    /**
     * This request returns user request builder
     *
     * @return userRequestBuilder
     */

    public UserRequestBuilder getUserRequestBuilder(Boolean useSetupEmail, String accountID) {
        try {
            if (useSetupEmail != null) {
                return useSetupEmail
                        ? getGraphServiceClientForUser(true, accountID).users(attributes.getEmail())
                        : getGraphServiceClientForUser(false, null).me();
            }
            return requestContext.invokeAsUser()
                    ? getGraphServiceClientForUser(null, null).me()
                    : getGraphServiceClientForUser(null, null).users(attributes.getEmail());
        } catch (IOException cause) {
            throw new IllegalStateException(cause);
        }
    }

    private MustAuthorizeException createMustAuthorizationException(String userId, boolean reAuthentication) {
        LOGGER.debug(reAuthentication ? Constants.GOT_ERROR_FOR_AUTHENTICATION_SO_SENDING_FOR_RE_AUTHENTICATION : Constants.GOT_ERROR_FOR_AUTHENTICATION_SO_SENDING_FOR_AUTHENTICATION);
        List<NamedValuedField> details = new ArrayList<>();
        NamedValuedField userIdField = new NamedValuedField(Constants.USER_ID, Constants.TEXT, userId, new HashMap<>(), new HashMap<>());
        details.add(userIdField);
        if (authContextId != null) {
            NamedValuedField contextIdField = new NamedValuedField(Constants.AUTH_CONTEXT_ID, Constants.TEXT, authContextId, new HashMap<>(), new HashMap<>());
            details.add(contextIdField);
        }
        return new MustAuthorizeException(reAuthentication ? Constants.REFRESH_TOKEN_EXPIRED : Constants.AUTHORIZATION_PROMPT, details);
    }

    private String getUserId(boolean calledFromValidateAttributes, String accountID) {
        String userId;
        if (calledFromValidateAttributes) {
            userId = attributes.getEmail();
        } else {
            userId = (accountID != null) ? accountID : authorizationContext.getAuthorizedAccount().getAccountId();
        }
        if (userId == null) {
            throw new IllegalStateException(Constants.FAILED_TO_GET_ACCOUNT);
        }
        return userId;
    }

    @NotNull
    private String getRefTokenStoreKey(String userId) {
        if (userId.startsWith(Constants.WS_CONTACT)) {
            return userId;
        } else {
            return userId + Constants.UNDER_SCORE + attributes.getClientId() + Constants.UNDER_SCORE + attributes.getAuthType();
        }
    }

    public String getDeltaLink() {
        Object deltaLink = refreshTokenStore.getDeltaLink(Constants.DELTA_TOKEN);
        return (String) deltaLink;
    }

    public void storeDeltaLink(String deltaToken) {
        refreshTokenStore.put(Constants.DELTA_TOKEN, deltaToken);
    }

    public static class GraphServiceClientAuthenticationProvider implements IAuthenticationProvider {

        private final String accessToken;

        public GraphServiceClientAuthenticationProvider(String accessToken) {
            if (accessToken == null || accessToken.isEmpty() || accessToken.isBlank()) {
                throw new IllegalArgumentException("Empty access token provided " + accessToken);
            }
            this.accessToken = accessToken;
        }

        @NotNull
        @Override
        public CompletableFuture<String> getAuthorizationTokenAsync(@NotNull URL requestUrl) {
            return CompletableFuture.completedFuture(accessToken);
        }
    }

    private List<NamedValuedField> createAuthDetails(String userId) {
        List<NamedValuedField> details = new ArrayList<>();
        NamedValuedField userIdField = new NamedValuedField(USER_ID, TEXT, userId, new HashMap<>(), new HashMap<>());
        details.add(userIdField);
        if (authContextId != null) {
            NamedValuedField contextIdField = new NamedValuedField(AUTH_CONTEXT_ID, TEXT, authContextId, new HashMap<>(), new HashMap<>());
            details.add(contextIdField);
        }
        return details;
    }

    /**
     * Handles authentication errors and throws appropriate MustAuthorizeException
     *
     * @param cause                The exception that occurred during authentication
     * @param refreshTokenStoreKey The key used to store the refresh token
     * @throws MustAuthorizeException with appropriate error message based on the cause
     */
    private void handleAuthenticationError(Exception cause, String refreshTokenStoreKey) {
        String errorMessage = cause.getMessage();
        LOGGER.error(" handleAuthenticationError() -> errorMessage ::: {} ", errorMessage);
        if (errorMessage != null) {
            for (AuthErrorRule rule : AUTH_ERROR_RULES) {
                if (rule.matches(errorMessage)) {
                    if (rule.shouldRemoveToken()) {
                        refreshTokenStore.remove(refreshTokenStoreKey);
                    }
                    throw new MustAuthorizeException(rule.getUserMessage(), createAuthDetails(refreshTokenStoreKey));
                }
            }
        }
        // Default fallback
        throw createMustAuthorizationException(refreshTokenStoreKey, true);
    }
}
