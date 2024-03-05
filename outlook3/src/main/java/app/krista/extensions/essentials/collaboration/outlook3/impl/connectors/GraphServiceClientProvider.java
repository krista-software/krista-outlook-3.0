package app.krista.extensions.essentials.collaboration.outlook3.impl.connectors;

import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extensions.essentials.collaboration.outlook3.OutlookAttributes;
import app.krista.extensions.essentials.collaboration.outlook3.impl.stores.RefreshTokenStore;
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

public class GraphServiceClientProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(GraphServiceClientProvider.class);
    private final RefreshTokenStore refreshTokenStore;
    private final OutlookAttributes outlookAttributes;
    private final RequestContext requestContext;
    private final AuthorizationContext authorizationContext;
    private final String authContextId;

    @Inject
    public GraphServiceClientProvider(RefreshTokenStore refreshTokenStore, OutlookAttributes outlookAttributes, RequestContext requestContext, AuthorizationContext authorizationContext) {
        this(refreshTokenStore, outlookAttributes, requestContext, authorizationContext, null);
    }

    public GraphServiceClientProvider(RefreshTokenStore refreshTokenStore, OutlookAttributes outlookAttributes, RequestContext requestContext, AuthorizationContext authorizationContext, String authContextId) {
        this.refreshTokenStore = refreshTokenStore;
        this.outlookAttributes = outlookAttributes;
        this.requestContext = requestContext;
        this.authorizationContext = authorizationContext;
        this.authContextId = authContextId;
    }

    public OutlookAttributes getOutlookAttributes() {
        return outlookAttributes;
    }

    public GraphServiceClient<Request> getGraphServiceClientForUser(Boolean useEmail, String accountID) throws IOException {
        if (useEmail != null) {
            return getGraphServiceClient(useEmail, useEmail ? null : accountID);
        } else {
            return getGraphServiceClient(!requestContext.invokeAsUser(), null);
        }
    }

    public GraphServiceClient<Request> getGraphServiceClientForAdmin() {
        return getGraphServiceClient(true, null);
    }

    private GraphServiceClient<Request> getGraphServiceClient(boolean useEmail, String accountID) {
        try {
            String userId = getUserId(useEmail, accountID);
            String refreshToken = refreshTokenStore.get(userId);
            if (refreshToken == null) {
                throw createMustAuthorizationException(userId, false);
            }
            return getGraphServiceClient(userId, refreshToken);
        } catch (RuntimeException cause) {
            throw cause;
        }
    }

    private GraphServiceClient<Request> getGraphServiceClient(String userId, String refreshToken) {
        try {
            String[] scopes = Constants.REQUIRED_SCOPE.split(Constants.SCOPE_SEPARATOR);
            Set<String> scopeSet = Arrays.stream(scopes).collect(Collectors.toSet());
            RefreshTokenParameters refreshTokenParameters = RefreshTokenParameters.builder(scopeSet, refreshToken).build();
            IClientCredential clientCredential = ClientCredentialFactory.createFromSecret(outlookAttributes.getClientSecret());
            String authority;
            if (outlookAttributes.getTenantId() != null) {
                authority = Constants.AUTHORITY + outlookAttributes.getTenantId();
            } else {
                authority = Constants.ORG_AUTHORITY;
            }
            ConfidentialClientApplication confidentialClientApplication = ConfidentialClientApplication.builder
                    (outlookAttributes.getClientId(), clientCredential).authority(authority).build();

            IAuthenticationResult authenticationResult = confidentialClientApplication.acquireToken(refreshTokenParameters).get();
            final String cachedTokenContent = confidentialClientApplication.tokenCache().serialize();
            updateRefreshToken(userId, Constants.GSON.fromJson(cachedTokenContent, JsonObject.class));
            return GraphServiceClient.builder()
                    .authenticationProvider(new GraphServiceClientAuthenticationProvider(authenticationResult.accessToken()))
                    .buildClient();
        } catch (ClientException | ExecutionException | MalformedURLException cause) {
            throw createMustAuthorizationException(userId, true);
        } catch (InterruptedException cause) {
            Thread.currentThread().interrupt();
            throw createMustAuthorizationException(userId, true);
        }
    }

    private void updateRefreshToken(String userId, JsonObject jsonObject) {
        JsonObject refreshToken = jsonObject.get("RefreshToken").getAsJsonObject();
        String firstKey = refreshToken.keySet().iterator().next();
        JsonObject tokenData = refreshToken.getAsJsonObject(firstKey);
        String secret = tokenData.get("secret").getAsString();
        refreshTokenStore.put(userId, secret);
        LOGGER.info("Updated the refresh token.");
    }

    /**
     * This request returns user request builder
     *
     * @return userRequestBuilder
     */

    public UserRequestBuilder getUserRequestBuilder(Boolean useEmail, String accountID) {
        try {
            if (useEmail != null) {
                return !useEmail
                        ? getGraphServiceClientForUser(useEmail, null).me()
                        : getGraphServiceClientForUser(useEmail, accountID).users(outlookAttributes.getMailId());
            }
            return requestContext.invokeAsUser()
                    ? getGraphServiceClientForUser(null, null).me()
                    : getGraphServiceClientForUser(null, null).users(outlookAttributes.getMailId());
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
            userId = outlookAttributes.getMailId();
        } else {
            userId = (accountID != null) ? accountID : authorizationContext.getAuthorizedAccount().getAccountId();
        }
        if (userId == null) {
            throw new IllegalStateException(Constants.FAILED_TO_GET_ACCOUNT);
        }
        return userId;
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

}
