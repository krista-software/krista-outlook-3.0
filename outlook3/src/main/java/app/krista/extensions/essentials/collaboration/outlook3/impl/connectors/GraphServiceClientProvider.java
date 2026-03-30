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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static app.krista.extensions.essentials.collaboration.outlook3.impl.util.Constants.*;

/**
 * Provider class responsible for creating and managing Microsoft Graph Service Client instances
 * with OAuth 2.0 authentication and token refresh capabilities.
 *
 * <p>This class handles the complete lifecycle of Graph API authentication including:
 * <ul>
 *   <li>Acquiring and refreshing OAuth 2.0 access tokens using MSAL4J</li>
 *   <li>Managing refresh token storage and retrieval</li>
 *   <li>Supporting both private (single-tenant) and public (multi-tenant) authentication</li>
 *   <li>Handling authentication errors with user-friendly messages and re-authentication flows</li>
 *   <li>Managing delta tokens for change tracking in Microsoft Graph API</li>
 * </ul>
 * </p>
 */
public class GraphServiceClientProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(GraphServiceClientProvider.class);

    /** Buffer before token expiry to trigger a fresh token acquisition (5 minutes). */
    private static final long TOKEN_EXPIRY_BUFFER_MS = 5 * 60 * 1000L;

    /**
     * Cached GraphServiceClient entries keyed by refreshTokenStoreKey.
     * Access tokens typically last 1 hour — this avoids calling Azure AD on every request.
     * With the 5-minute buffer, tokens are refreshed approximately every 55 minutes.
     */
    private static final ConcurrentHashMap<String, CachedGraphClient> GRAPH_CLIENT_CACHE = new ConcurrentHashMap<>();

    private static class CachedGraphClient {
        final GraphServiceClient<Request> client;
        final long expiresAtMillis;

        CachedGraphClient(GraphServiceClient<Request> client, long expiresAtMillis) {
            this.client = client;
            this.expiresAtMillis = expiresAtMillis;
        }

        boolean isValid() {
            return System.currentTimeMillis() < (expiresAtMillis - TOKEN_EXPIRY_BUFFER_MS);
        }
    }

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

    /**
     * Creates a Graph Service Client configured for a specific user context.
     *
     * <p>This method determines whether to use the setup email (admin context) or the
     * authenticated user's account based on the provided parameters. It handles both
     * explicit user specification and automatic context detection from the request.</p>
     *
     * @param useSetupEmail flag indicating whether to use the setup email (true) or user account (false);
     *                      if null, automatically determines based on request context
     * @param accountID     the specific account ID to use; only applicable when useSetupEmail is false
     * @return a configured GraphServiceClient instance for the specified user
     * @throws IOException if client creation fails due to network or authentication issues
     */
    public GraphServiceClient<Request> getGraphServiceClientForUser(Boolean useSetupEmail, String accountID) throws IOException {
        if (useSetupEmail != null) {
            return getGraphServiceClient(useSetupEmail, useSetupEmail ? null : accountID);
        } else {
            return getGraphServiceClient(!requestContext.invokeAsUser(), null);
        }
    }

    /**
     * Creates a Graph Service Client configured for administrative operations.
     *
     * <p>This method returns a client authenticated with the setup email credentials,
     * providing administrative access to Microsoft Graph API resources.</p>
     *
     * @return a configured GraphServiceClient instance with administrative privileges
     */
    public GraphServiceClient<Request> getGraphServiceClientForAdmin() {
        return getGraphServiceClient(true, null);
    }

    /**
     * Internal method to create a Graph Service Client with token refresh handling.
     *
     * <p>This method retrieves the refresh token from storage, validates its existence,
     * and creates an authenticated Graph Service Client. If the refresh token is missing,
     * it triggers the re-authentication flow.</p>
     *
     * @param useSetupEmail flag indicating whether to use setup email or user account
     * @param accountID     the specific account ID to use when not using setup email
     * @return a configured GraphServiceClient instance
     * @throws MustAuthorizeException if refresh token is missing or authentication fails
     * @throws RuntimeException       for other unexpected errors during client creation
     */
    private GraphServiceClient<Request> getGraphServiceClient(boolean useSetupEmail, String accountID) {
        try {
            String userId = getUserId(useSetupEmail, accountID);
            String refreshTokenStoreKey = getRefTokenStoreKey(userId);

            // Check cache first — avoid KeyValueStore REST call if token is still valid
            CachedGraphClient cached = GRAPH_CLIENT_CACHE.get(refreshTokenStoreKey);
            if (cached != null && cached.isValid()) {
                LOGGER.info("Using cached GraphServiceClient for key: {} (expires in {}s)",
                        refreshTokenStoreKey, (cached.expiresAtMillis - System.currentTimeMillis()) / 1000);
                return cached.client;
            }

            // Cache miss or expired — fetch refresh token from KeyValueStore
            String refreshToken = refreshTokenStore.get(refreshTokenStoreKey);
            if (refreshToken == null) {
                throw createMustAuthorizationException(refreshTokenStoreKey, false);
            }
            return getGraphServiceClient(refreshTokenStoreKey, refreshToken);
        } catch (RuntimeException cause) {
            LOGGER.error("getGraphServiceClient() -> Exception occurred: {}", cause.getMessage(), cause);
            throw cause;
        }
    }

    /**
     * Core method to acquire access token and create Graph Service Client using refresh token.
     *
     * <p>This method performs the following operations:
     * <ul>
     *   <li>Builds a confidential client application with appropriate authority (private/public)</li>
     *   <li>Acquires a new access token using the refresh token via MSAL4J</li>
     *   <li>Updates the stored refresh token with the newly issued one</li>
     *   <li>Creates and returns an authenticated GraphServiceClient</li>
     * </ul>
     * Handles various authentication errors including token expiration, revocation, and network issues.</p>
     *
     * @param refreshTokenStoreKey the key used to store and retrieve the refresh token
     * @param refreshToken         the current refresh token to use for acquiring access token
     * @return a configured GraphServiceClient instance with valid access token
     * @throws MustAuthorizeException   if authentication fails due to expired/invalid tokens or user account issues
     * @throws IllegalArgumentException if authentication type is not recognized
     */
    private GraphServiceClient<Request> getGraphServiceClient(String refreshTokenStoreKey, String refreshToken) {
        LOGGER.debug("Creating GraphServiceClient for key: {} (cache miss or expired)", refreshTokenStoreKey);

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

            GraphServiceClient<Request> graphClient = GraphServiceClient.builder()
                    .authenticationProvider(new GraphServiceClientAuthenticationProvider(authenticationResult.accessToken()))
                    .buildClient();

            // Cache the GraphServiceClient with token expiry
            long expiresAtMillis = authenticationResult.expiresOnDate().getTime();
            GRAPH_CLIENT_CACHE.put(refreshTokenStoreKey, new CachedGraphClient(graphClient, expiresAtMillis));
            LOGGER.info("Successfully acquired and cached access token for key: {} (expires in {}s)",
                    refreshTokenStoreKey, (expiresAtMillis - System.currentTimeMillis()) / 1000);

            return graphClient;
        } catch (ClientException | ExecutionException | MalformedURLException cause) {
            LOGGER.error("getGraphServiceClient() -> Exception occurred: {}", cause.getMessage(), cause);
            // Remove stale cache entry on auth failure
            GRAPH_CLIENT_CACHE.remove(refreshTokenStoreKey);
            handleAuthenticationError(cause, refreshTokenStoreKey);
            // This line will never be reached as handleAuthenticationError always throws an exception
            return null;
        } catch (InterruptedException cause) {
            LOGGER.error("getGraphServiceClient() -> InterruptedException occurred: {}", cause.getMessage(), cause);
            Thread.currentThread().interrupt();
            throw createMustAuthorizationException(refreshTokenStoreKey, true);
        }
    }

    /**
     * Extracts and updates the refresh token from MSAL4J token cache response.
     *
     * <p>This method parses the serialized token cache JSON, extracts the new refresh token,
     * and updates it in the refresh token store. This ensures subsequent API calls can use
     * the latest refresh token without requiring user re-authentication.</p>
     *
     * @param userId     the user identifier used as the key for storing the refresh token
     * @param jsonObject the serialized token cache JSON object from MSAL4J containing the new refresh token
     */
    private void updateRefreshToken(String userId, JsonObject jsonObject) {
        JsonObject refreshToken = jsonObject.get("RefreshToken").getAsJsonObject();
        String firstKey = refreshToken.keySet().iterator().next();
        JsonObject tokenData = refreshToken.getAsJsonObject(firstKey);
        String secret = tokenData.get("secret").getAsString();
        refreshTokenStore.put(userId, secret);
        LOGGER.info("Updated refresh token.");
    }

    /**
     * Creates a UserRequestBuilder for accessing Microsoft Graph user-specific endpoints.
     *
     * <p>This method determines the appropriate user context (setup email vs. authenticated user)
     * and returns a builder configured to access either the /me endpoint (for authenticated users)
     * or the /users/{email} endpoint (for administrative operations).</p>
     *
     * @param useSetupEmail flag indicating whether to use setup email (true) or authenticated user (false);
     *                      if null, automatically determines based on request context
     * @param accountID     the specific account ID to use; only applicable when useSetupEmail is false
     * @return a UserRequestBuilder configured for the appropriate user context
     * @throws RuntimeException if connection to Microsoft services fails, wrapping the underlying IOException
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
        } catch (IOException e) {
            LOGGER.error("Failed to get user request builder: {}", e.getMessage(), e);
            throw new RuntimeException("We couldn't establish a connection to Microsoft services. Please try again.", e);
        }
    }

    /**
     * Creates a MustAuthorizeException to trigger user authentication or re-authentication flow.
     *
     * <p>This method constructs an exception with appropriate details including user ID and
     * optional authentication context ID. The exception message varies based on whether this
     * is an initial authentication or a re-authentication scenario.</p>
     *
     * @param userId           the user identifier to include in the authorization details
     * @param reAuthentication flag indicating if this is a re-authentication (true) or initial auth (false)
     * @return a MustAuthorizeException configured with appropriate message and user details
     */
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

    /**
     * Determines the user ID based on the operation context and provided parameters.
     *
     * <p>This method resolves the user identifier from different sources:
     * <ul>
     *   <li>Setup email when called from attribute validation</li>
     *   <li>Provided account ID when explicitly specified</li>
     *   <li>Authorized account from authorization context otherwise</li>
     * </ul>
     * </p>
     *
     * @param calledFromValidateAttributes flag indicating if called during attribute validation
     * @param accountID                    the explicitly provided account ID, or null to use authorization context
     * @return the resolved user identifier
     * @throws IllegalStateException if user ID cannot be determined from any source
     */
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

    /**
     * Constructs the refresh token storage key from user ID and application configuration.
     *
     * <p>This method creates a composite key that uniquely identifies a refresh token by
     * combining the user ID, client ID, and authentication type. Special handling is provided
     * for workspace contacts which use the user ID directly as the key.</p>
     *
     * @param userId the user identifier
     * @return a composite key in the format: userId_clientId_authType, or just userId for workspace contacts
     */
    @NotNull
    private String getRefTokenStoreKey(String userId) {
        if (userId.startsWith(Constants.WS_CONTACT)) {
            return userId;
        } else {
            return userId + Constants.UNDER_SCORE + attributes.getClientId() + Constants.UNDER_SCORE + attributes.getAuthType();
        }
    }

    /**
     * Retrieves the stored delta link for Microsoft Graph API change tracking.
     *
     * <p>Delta links are used to track changes in Microsoft Graph resources efficiently
     * by only retrieving items that have changed since the last query.</p>
     *
     * @return the stored delta link token, or null if not previously stored
     */
    public String getDeltaLink() {
        try {
            Object deltaLink = refreshTokenStore.getDeltaLink(Constants.DELTA_TOKEN);
            if (deltaLink == null) {
                LOGGER.info("Delta token not found in storage - will initiate fresh delta sync");
                return null;
            }
            String deltaLinkStr = (String) deltaLink;
            LOGGER.info("Delta token retrieved from storage successfully (length: {} chars)", deltaLinkStr.length());
            LOGGER.debug("Delta token value: {}", deltaLinkStr);
            return deltaLinkStr;
        } catch (Exception e) {
            LOGGER.error("Failed to retrieve delta token from storage: {}", e.getMessage(), e);
            LOGGER.warn("Will proceed with fresh delta sync due to retrieval failure");
            return null;
        }
    }

    /**
     * Stores a delta link token for future change tracking queries.
     *
     * <p>This method persists the delta token received from Microsoft Graph API responses,
     * enabling efficient incremental synchronization in subsequent requests.</p>
     *
     * <p>If the deltaToken is null, the existing token is removed from storage. This is used
     * when clearing expired tokens during HTTP 410 error recovery.</p>
     *
     * @param deltaToken the delta link token to store for future use, or null to remove the existing token
     */
    public void storeDeltaLink(String deltaToken) {
        if (deltaToken == null) {
            LOGGER.info("Clearing delta token from storage (expired token cleanup or reset)");
            refreshTokenStore.remove(Constants.DELTA_TOKEN);
            LOGGER.info("Delta token successfully removed from storage");
        } else {
            refreshTokenStore.put(Constants.DELTA_TOKEN, deltaToken);
            LOGGER.info("Delta token successfully stored - next sync will use incremental delta query");
        }
    }

    /**
     * Custom authentication provider implementation for Microsoft Graph Service Client.
     *
     * <p>This provider supplies the access token to Microsoft Graph API requests by implementing
     * the IAuthenticationProvider interface. It validates the access token on construction and
     * provides it asynchronously to the Graph SDK for request authentication.</p>
     */
    public static class GraphServiceClientAuthenticationProvider implements IAuthenticationProvider {

        private final String accessToken;

        public GraphServiceClientAuthenticationProvider(String accessToken) {
            if (accessToken == null || accessToken.isEmpty() || accessToken.isBlank()) {
                throw new IllegalArgumentException("Empty access token provided " + accessToken);
            }
            this.accessToken = accessToken;
        }

        /**
         * Provides the access token asynchronously for Microsoft Graph API requests.
         *
         * <p>This method is called by the Microsoft Graph SDK for each API request to obtain
         * the bearer token for authentication. The token is returned as a completed future
         * since it's already available in memory.</p>
         *
         * @param requestUrl the URL of the request being authenticated (not used in this implementation)
         * @return a CompletableFuture containing the access token
         */
        @NotNull
        @Override
        public CompletableFuture<String> getAuthorizationTokenAsync(@NotNull URL requestUrl) {
            return CompletableFuture.completedFuture(accessToken);
        }
    }

    /**
     * Creates a list of authorization details for MustAuthorizeException.
     *
     * <p>This method constructs the details payload that will be passed to the authorization
     * flow, including the user ID and optional authentication context ID. These details help
     * the authorization system identify which user and context require authentication.</p>
     *
     * @param userId the user identifier to include in the authorization details
     * @return a list of NamedValuedField objects containing user ID and optional context ID
     */
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
        throw createMustAuthorizationException(refreshTokenStoreKey, !requestContext.invokeAsUser());
    }
}
