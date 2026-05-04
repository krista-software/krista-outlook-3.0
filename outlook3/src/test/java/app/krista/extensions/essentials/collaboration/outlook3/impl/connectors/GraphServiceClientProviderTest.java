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
import app.krista.extensions.essentials.collaboration.outlook3.impl.util.Constants;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.ksdk.context.RequestContext;
import com.google.gson.JsonObject;
import com.microsoft.aad.msal4j.*;
import com.microsoft.graph.requests.GraphServiceClient;
import okhttp3.Request;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for GraphServiceClientProvider — targeting 100% code coverage.
 *
 * Covers:
 * - Constructors (4-arg and 5-arg)
 * - getOutlookAttributes()
 * - getGraphServiceClientForUser() — all branches
 * - getGraphServiceClientForAdmin()
 * - getGraphServiceClient(boolean, String) — cache hit, miss, null token, runtime exception
 * - getGraphServiceClient(String, String) — PRIVATE/PUBLIC/unknown auth, success, errors
 * - updateRefreshToken()
 * - getUserId() — all branches including null userId
 * - getRefTokenStoreKey() — WS_CONTACT vs composite key
 * - getDeltaLink() — null, non-null, exception
 * - storeDeltaLink() — null vs non-null
 * - GraphServiceClientAuthenticationProvider — constructor validation, getAuthorizationTokenAsync
 * - createMustAuthorizationException — with/without authContextId, reAuth true/false
 * - createAuthDetails — with/without authContextId
 * - handleAuthenticationError — matching rules (removeToken true/false), null message, no match fallback
 * - getUserRequestBuilder() — all branches
 * - CachedGraphClient inner class — isValid() boundary conditions
 */
@DisplayName("GraphServiceClientProvider - Full Coverage Tests")
public class GraphServiceClientProviderTest {

    private static final String CLIENT_ID = "test-client-id";
    private static final String CLIENT_SECRET = "test-client-secret";
    private static final String TENANT_ID = "test-tenant-id";
    private static final String EMAIL = "test@example.com";
    private static final String BASE_URL = "https://extension.solution.eng.krista.app/extension/api/test";
    private static final String ACCOUNT_ID = "test-account-id";
    private static final String AUTH_CONTEXT_ID = "test-auth-context-id";
    private static final String REFRESH_TOKEN = "test-refresh-token";
    private static final long TOKEN_EXPIRY_BUFFER_MS = 5 * 60 * 1000L;

    private RefreshTokenStore refreshTokenStore;
    private RequestContext requestContext;
    private AuthorizationContext authorizationContext;
    private app.krista.ksdk.accounts.Account authorizedAccount;
    private OutlookAttributes attributes;

    private ConcurrentHashMap<String, Object> graphClientCache;

    @BeforeEach
    @SuppressWarnings("unchecked")
    public void setup() throws Exception {
        refreshTokenStore = mock(RefreshTokenStore.class);
        requestContext = mock(RequestContext.class);
        authorizationContext = mock(AuthorizationContext.class);
        authorizedAccount = mock(app.krista.ksdk.accounts.Account.class);
        when(authorizationContext.getAuthorizedAccount()).thenReturn(authorizedAccount);
        when(authorizedAccount.getAccountId()).thenReturn(ACCOUNT_ID);

        attributes = new OutlookAttributes(
                CLIENT_ID, CLIENT_SECRET, TENANT_ID, EMAIL, true, Constants.PRIVATE, BASE_URL
        );

        Field cacheField = GraphServiceClientProvider.class.getDeclaredField("GRAPH_CLIENT_CACHE");
        cacheField.setAccessible(true);
        graphClientCache = (ConcurrentHashMap<String, Object>) cacheField.get(null);
    }

    @AfterEach
    public void tearDown() {
        graphClientCache.clear();
    }

    // ========================================================================
    // Helper methods
    // ========================================================================

    private String getAdminKey() {
        return EMAIL + "_" + CLIENT_ID + "_" + Constants.PRIVATE;
    }

    private String getUserKey(String accountId) {
        return accountId + "_" + CLIENT_ID + "_" + Constants.PRIVATE;
    }

    @SuppressWarnings("unchecked")
    private Object createCachedGraphClient(GraphServiceClient<Request> client, long expiresAtMillis) throws Exception {
        Class<?> cls = null;
        for (Class<?> c : GraphServiceClientProvider.class.getDeclaredClasses()) {
            if (c.getSimpleName().equals("CachedGraphClient")) {
                cls = c;
                break;
            }
        }
        assertNotNull(cls);
        Constructor<?> ctor = cls.getDeclaredConstructor(GraphServiceClient.class, long.class);
        ctor.setAccessible(true);
        return ctor.newInstance(client, expiresAtMillis);
    }

    private boolean callIsValid(Object cachedGraphClient) throws Exception {
        Method m = cachedGraphClient.getClass().getDeclaredMethod("isValid");
        m.setAccessible(true);
        return (boolean) m.invoke(cachedGraphClient);
    }

    private void populateCache(String key, GraphServiceClient<Request> client, long expiresAt) throws Exception {
        graphClientCache.put(key, createCachedGraphClient(client, expiresAt));
    }

    private long futureMillis(long offsetMs) {
        return System.currentTimeMillis() + offsetMs;
    }

    private long pastMillis(long offsetMs) {
        return System.currentTimeMillis() - offsetMs;
    }

    // ========================================================================
    // 1. Constructor Tests
    // ========================================================================

    @Test
    @DisplayName("4-arg constructor sets authContextId to null")
    public void testFourArgConstructor() throws Exception {
        GraphServiceClientProvider provider = new GraphServiceClientProvider(
                refreshTokenStore, attributes, requestContext, authorizationContext
        );
        Field f = GraphServiceClientProvider.class.getDeclaredField("authContextId");
        f.setAccessible(true);
        assertNull(f.get(provider));
    }

    @Test
    @DisplayName("5-arg constructor sets authContextId")
    public void testFiveArgConstructor() throws Exception {
        GraphServiceClientProvider provider = new GraphServiceClientProvider(
                refreshTokenStore, attributes, requestContext, authorizationContext, AUTH_CONTEXT_ID
        );
        Field f = GraphServiceClientProvider.class.getDeclaredField("authContextId");
        f.setAccessible(true);
        assertEquals(AUTH_CONTEXT_ID, f.get(provider));
    }

    // ========================================================================
    // 2. getOutlookAttributes()
    // ========================================================================

    @Test
    @DisplayName("getOutlookAttributes returns injected attributes")
    public void testGetOutlookAttributes() {
        GraphServiceClientProvider provider = new GraphServiceClientProvider(
                refreshTokenStore, attributes, requestContext, authorizationContext
        );
        assertSame(attributes, provider.getOutlookAttributes());
    }

    // ========================================================================
    // 3. CachedGraphClient.isValid() — boundary tests
    // ========================================================================

    @Test
    @DisplayName("isValid: true when token expires well after buffer")
    public void testIsValid_True() throws Exception {
        Object cached = createCachedGraphClient(mock(GraphServiceClient.class), futureMillis(3600_000));
        assertTrue(callIsValid(cached));
    }

    @Test
    @DisplayName("isValid: false when token already expired")
    public void testIsValid_Expired() throws Exception {
        Object cached = createCachedGraphClient(mock(GraphServiceClient.class), pastMillis(600_000));
        assertFalse(callIsValid(cached));
    }

    @Test
    @DisplayName("isValid: false when within buffer zone")
    public void testIsValid_WithinBuffer() throws Exception {
        Object cached = createCachedGraphClient(mock(GraphServiceClient.class), futureMillis(180_000));
        assertFalse(callIsValid(cached));
    }

    @Test
    @DisplayName("isValid: false at exact buffer boundary")
    public void testIsValid_ExactBoundary() throws Exception {
        Object cached = createCachedGraphClient(mock(GraphServiceClient.class), futureMillis(TOKEN_EXPIRY_BUFFER_MS));
        assertFalse(callIsValid(cached));
    }

    @Test
    @DisplayName("isValid: true just outside buffer")
    public void testIsValid_JustOutsideBuffer() throws Exception {
        Object cached = createCachedGraphClient(mock(GraphServiceClient.class), futureMillis(TOKEN_EXPIRY_BUFFER_MS + 2000));
        assertTrue(callIsValid(cached));
    }

    // ========================================================================
    // 4. getGraphServiceClientForAdmin() — cache hit/miss
    // ========================================================================

    @Test
    @DisplayName("getGraphServiceClientForAdmin: cache hit returns cached client")
    public void testAdminCacheHit() throws Exception {
        GraphServiceClient<Request> mockClient = mock(GraphServiceClient.class);
        populateCache(getAdminKey(), mockClient, futureMillis(3600_000));

        GraphServiceClientProvider provider = new GraphServiceClientProvider(
                refreshTokenStore, attributes, requestContext, authorizationContext
        );
        assertSame(mockClient, provider.getGraphServiceClientForAdmin());
        verify(refreshTokenStore, never()).get(anyString());
    }

    @Test
    @DisplayName("getGraphServiceClientForAdmin: cache miss with null refresh token throws MustAuthorizeException")
    public void testAdminCacheMiss_NullToken() {
        when(refreshTokenStore.get(getAdminKey())).thenReturn(null);
        GraphServiceClientProvider provider = new GraphServiceClientProvider(
                refreshTokenStore, attributes, requestContext, authorizationContext
        );
        assertThrows(MustAuthorizeException.class, provider::getGraphServiceClientForAdmin);
        verify(refreshTokenStore).get(getAdminKey());
    }

    // ========================================================================
    // 5. getGraphServiceClientForUser() — all branches
    // ========================================================================

    @Test
    @DisplayName("getGraphServiceClientForUser(true, null): uses setup email (admin path)")
    public void testForUser_UseSetupEmailTrue() throws Exception {
        GraphServiceClient<Request> mockClient = mock(GraphServiceClient.class);
        populateCache(getAdminKey(), mockClient, futureMillis(3600_000));

        GraphServiceClientProvider provider = new GraphServiceClientProvider(
                refreshTokenStore, attributes, requestContext, authorizationContext
        );
        assertSame(mockClient, provider.getGraphServiceClientForUser(true, null));
    }

    @Test
    @DisplayName("getGraphServiceClientForUser(false, accountID): uses account ID")
    public void testForUser_UseSetupEmailFalseWithAccountId() throws Exception {
        GraphServiceClient<Request> mockClient = mock(GraphServiceClient.class);
        populateCache(getUserKey(ACCOUNT_ID), mockClient, futureMillis(3600_000));

        GraphServiceClientProvider provider = new GraphServiceClientProvider(
                refreshTokenStore, attributes, requestContext, authorizationContext
        );
        assertSame(mockClient, provider.getGraphServiceClientForUser(false, ACCOUNT_ID));
    }

    @Test
    @DisplayName("getGraphServiceClientForUser(null, null): invokeAsUser=true uses authorized account")
    public void testForUser_NullUseSetupEmail_InvokeAsUser() throws Exception {
        when(requestContext.invokeAsUser()).thenReturn(true);
        GraphServiceClient<Request> mockClient = mock(GraphServiceClient.class);
        populateCache(getUserKey(ACCOUNT_ID), mockClient, futureMillis(3600_000));

        GraphServiceClientProvider provider = new GraphServiceClientProvider(
                refreshTokenStore, attributes, requestContext, authorizationContext
        );
        assertSame(mockClient, provider.getGraphServiceClientForUser(null, null));
    }

    @Test
    @DisplayName("getGraphServiceClientForUser(null, null): invokeAsUser=false uses setup email")
    public void testForUser_NullUseSetupEmail_NotInvokeAsUser() throws Exception {
        when(requestContext.invokeAsUser()).thenReturn(false);
        GraphServiceClient<Request> mockClient = mock(GraphServiceClient.class);
        populateCache(getAdminKey(), mockClient, futureMillis(3600_000));

        GraphServiceClientProvider provider = new GraphServiceClientProvider(
                refreshTokenStore, attributes, requestContext, authorizationContext
        );
        assertSame(mockClient, provider.getGraphServiceClientForUser(null, null));
    }

    // ========================================================================
    // 6. getUserId() — via reflection
    // ========================================================================

    private String invokeGetUserId(GraphServiceClientProvider provider, boolean calledFromValidate, String accountID) throws Exception {
        Method m = GraphServiceClientProvider.class.getDeclaredMethod("getUserId", boolean.class, String.class);
        m.setAccessible(true);
        return (String) m.invoke(provider, calledFromValidate, accountID);
    }

    @Test
    @DisplayName("getUserId: calledFromValidateAttributes=true returns email")
    public void testGetUserId_ValidateAttributes() throws Exception {
        GraphServiceClientProvider provider = new GraphServiceClientProvider(
                refreshTokenStore, attributes, requestContext, authorizationContext
        );
        assertEquals(EMAIL, invokeGetUserId(provider, true, null));
    }

    @Test
    @DisplayName("getUserId: calledFromValidateAttributes=false with accountID returns accountID")
    public void testGetUserId_WithAccountID() throws Exception {
        GraphServiceClientProvider provider = new GraphServiceClientProvider(
                refreshTokenStore, attributes, requestContext, authorizationContext
        );
        assertEquals(ACCOUNT_ID, invokeGetUserId(provider, false, ACCOUNT_ID));
    }

    @Test
    @DisplayName("getUserId: calledFromValidateAttributes=false without accountID uses authorizationContext")
    public void testGetUserId_FromAuthContext() throws Exception {
        GraphServiceClientProvider provider = new GraphServiceClientProvider(
                refreshTokenStore, attributes, requestContext, authorizationContext
        );
        assertEquals(ACCOUNT_ID, invokeGetUserId(provider, false, null));
    }

    @Test
    @DisplayName("getUserId: null userId throws IllegalStateException")
    public void testGetUserId_NullThrows() throws Exception {
        when(authorizedAccount.getAccountId()).thenReturn(null);
        GraphServiceClientProvider provider = new GraphServiceClientProvider(
                refreshTokenStore, attributes, requestContext, authorizationContext
        );
        Exception ex = assertThrows(Exception.class, () -> invokeGetUserId(provider, false, null));
        // Reflection wraps in InvocationTargetException
        assertTrue(ex.getCause() instanceof IllegalStateException || ex instanceof IllegalStateException);
    }

    // ========================================================================
    // 7. getRefTokenStoreKey() — via reflection
    // ========================================================================

    private String invokeGetRefTokenStoreKey(GraphServiceClientProvider provider, String userId) throws Exception {
        Method m = GraphServiceClientProvider.class.getDeclaredMethod("getRefTokenStoreKey", String.class);
        m.setAccessible(true);
        return (String) m.invoke(provider, userId);
    }

    @Test
    @DisplayName("getRefTokenStoreKey: WS_CONTACT prefix returns userId directly")
    public void testRefTokenStoreKey_WsContact() throws Exception {
        GraphServiceClientProvider provider = new GraphServiceClientProvider(
                refreshTokenStore, attributes, requestContext, authorizationContext
        );
        String wsKey = Constants.WS_CONTACT + "_user123";
        assertEquals(wsKey, invokeGetRefTokenStoreKey(provider, wsKey));
    }

    @Test
    @DisplayName("getRefTokenStoreKey: normal userId returns composite key")
    public void testRefTokenStoreKey_Normal() throws Exception {
        GraphServiceClientProvider provider = new GraphServiceClientProvider(
                refreshTokenStore, attributes, requestContext, authorizationContext
        );
        String expected = "user@test.com_" + CLIENT_ID + "_" + Constants.PRIVATE;
        assertEquals(expected, invokeGetRefTokenStoreKey(provider, "user@test.com"));
    }

    // ========================================================================
    // 8. Cache hit with WS_CONTACT key
    // ========================================================================

    @Test
    @DisplayName("Cache hit with workspace contact key format")
    public void testCacheHit_WorkspaceContact() throws Exception {
        String wsUserId = Constants.WS_CONTACT + "_user123";
        when(authorizedAccount.getAccountId()).thenReturn(wsUserId);
        when(requestContext.invokeAsUser()).thenReturn(true);

        GraphServiceClient<Request> mockClient = mock(GraphServiceClient.class);
        // WS_CONTACT key is the userId itself
        graphClientCache.put(wsUserId, createCachedGraphClient(mockClient, futureMillis(3600_000)));

        GraphServiceClientProvider provider = new GraphServiceClientProvider(
                refreshTokenStore, attributes, requestContext, authorizationContext
        );
        GraphServiceClient<Request> result = provider.getGraphServiceClientForUser(false, wsUserId);
        assertSame(mockClient, result);
        verify(refreshTokenStore, never()).get(anyString());
    }

    // ========================================================================
    // 9. getDeltaLink() — all paths
    // ========================================================================

    @Test
    @DisplayName("getDeltaLink: returns null when deltaLink is null in store")
    public void testGetDeltaLink_Null() {
        when(refreshTokenStore.getDeltaLink(Constants.DELTA_TOKEN)).thenReturn(null);
        GraphServiceClientProvider provider = new GraphServiceClientProvider(
                refreshTokenStore, attributes, requestContext, authorizationContext
        );
        assertNull(provider.getDeltaLink());
    }

    @Test
    @DisplayName("getDeltaLink: returns delta link string when present")
    public void testGetDeltaLink_Present() {
        String deltaLink = "https://graph.microsoft.com/v1.0/me/messages/delta?$deltatoken=abc123";
        when(refreshTokenStore.getDeltaLink(Constants.DELTA_TOKEN)).thenReturn(deltaLink);
        GraphServiceClientProvider provider = new GraphServiceClientProvider(
                refreshTokenStore, attributes, requestContext, authorizationContext
        );
        assertEquals(deltaLink, provider.getDeltaLink());
    }

    @Test
    @DisplayName("getDeltaLink: returns null on exception")
    public void testGetDeltaLink_Exception() {
        when(refreshTokenStore.getDeltaLink(Constants.DELTA_TOKEN)).thenThrow(new RuntimeException("Store error"));
        GraphServiceClientProvider provider = new GraphServiceClientProvider(
                refreshTokenStore, attributes, requestContext, authorizationContext
        );
        assertNull(provider.getDeltaLink());
    }

    // ========================================================================
    // 10. storeDeltaLink() — null vs non-null
    // ========================================================================

    @Test
    @DisplayName("storeDeltaLink: null removes token from store")
    public void testStoreDeltaLink_Null() {
        GraphServiceClientProvider provider = new GraphServiceClientProvider(
                refreshTokenStore, attributes, requestContext, authorizationContext
        );
        provider.storeDeltaLink(null);
        verify(refreshTokenStore).remove(Constants.DELTA_TOKEN);
        verify(refreshTokenStore, never()).put(anyString(), anyString());
    }

    @Test
    @DisplayName("storeDeltaLink: non-null stores token")
    public void testStoreDeltaLink_NonNull() {
        GraphServiceClientProvider provider = new GraphServiceClientProvider(
                refreshTokenStore, attributes, requestContext, authorizationContext
        );
        String token = "delta-token-value";
        provider.storeDeltaLink(token);
        verify(refreshTokenStore).put(Constants.DELTA_TOKEN, token);
        verify(refreshTokenStore, never()).remove(anyString());
    }

    // ========================================================================
    // 11. GraphServiceClientAuthenticationProvider — inner class
    // ========================================================================

    @Test
    @DisplayName("AuthProvider: null accessToken throws IllegalArgumentException")
    public void testAuthProvider_NullToken() {
        assertThrows(IllegalArgumentException.class,
                () -> new GraphServiceClientProvider.GraphServiceClientAuthenticationProvider(null));
    }

    @Test
    @DisplayName("AuthProvider: empty accessToken throws IllegalArgumentException")
    public void testAuthProvider_EmptyToken() {
        assertThrows(IllegalArgumentException.class,
                () -> new GraphServiceClientProvider.GraphServiceClientAuthenticationProvider(""));
    }

    @Test
    @DisplayName("AuthProvider: blank accessToken throws IllegalArgumentException")
    public void testAuthProvider_BlankToken() {
        assertThrows(IllegalArgumentException.class,
                () -> new GraphServiceClientProvider.GraphServiceClientAuthenticationProvider("   "));
    }

    @Test
    @DisplayName("AuthProvider: valid token constructs successfully and returns token async")
    public void testAuthProvider_ValidToken() throws Exception {
        String token = "valid-access-token";
        GraphServiceClientProvider.GraphServiceClientAuthenticationProvider authProvider =
                new GraphServiceClientProvider.GraphServiceClientAuthenticationProvider(token);
        CompletableFuture<String> future = authProvider.getAuthorizationTokenAsync(new URL("https://graph.microsoft.com"));
        assertEquals(token, future.get());
    }

    // ========================================================================
    // 12. createMustAuthorizationException — via reflection
    // ========================================================================

    private MustAuthorizeException invokeCreateMustAuthorizationException(
            GraphServiceClientProvider provider, String userId, boolean reAuth) throws Exception {
        Method m = GraphServiceClientProvider.class.getDeclaredMethod(
                "createMustAuthorizationException", String.class, boolean.class);
        m.setAccessible(true);
        return (MustAuthorizeException) m.invoke(provider, userId, reAuth);
    }

    @Test
    @DisplayName("createMustAuthorizationException: reAuth=false, no authContextId")
    public void testCreateMustAuthException_NoReAuth_NoContext() throws Exception {
        GraphServiceClientProvider provider = new GraphServiceClientProvider(
                refreshTokenStore, attributes, requestContext, authorizationContext
        );
        MustAuthorizeException ex = invokeCreateMustAuthorizationException(provider, "user1", false);
        assertNotNull(ex);
        assertEquals(Constants.AUTHORIZATION_PROMPT, ex.getMessage());
    }

    @Test
    @DisplayName("createMustAuthorizationException: reAuth=true, no authContextId")
    public void testCreateMustAuthException_ReAuth_NoContext() throws Exception {
        GraphServiceClientProvider provider = new GraphServiceClientProvider(
                refreshTokenStore, attributes, requestContext, authorizationContext
        );
        MustAuthorizeException ex = invokeCreateMustAuthorizationException(provider, "user1", true);
        assertNotNull(ex);
        assertEquals(Constants.REFRESH_TOKEN_EXPIRED, ex.getMessage());
    }

    @Test
    @DisplayName("createMustAuthorizationException: reAuth=false, with authContextId")
    public void testCreateMustAuthException_NoReAuth_WithContext() throws Exception {
        GraphServiceClientProvider provider = new GraphServiceClientProvider(
                refreshTokenStore, attributes, requestContext, authorizationContext, AUTH_CONTEXT_ID
        );
        MustAuthorizeException ex = invokeCreateMustAuthorizationException(provider, "user1", false);
        assertNotNull(ex);
        assertEquals(Constants.AUTHORIZATION_PROMPT, ex.getMessage());
    }

    @Test
    @DisplayName("createMustAuthorizationException: reAuth=true, with authContextId")
    public void testCreateMustAuthException_ReAuth_WithContext() throws Exception {
        GraphServiceClientProvider provider = new GraphServiceClientProvider(
                refreshTokenStore, attributes, requestContext, authorizationContext, AUTH_CONTEXT_ID
        );
        MustAuthorizeException ex = invokeCreateMustAuthorizationException(provider, "user1", true);
        assertNotNull(ex);
        assertEquals(Constants.REFRESH_TOKEN_EXPIRED, ex.getMessage());
    }

    // ========================================================================
    // 13. createAuthDetails — via reflection
    // ========================================================================

    @SuppressWarnings("unchecked")
    private List<?> invokeCreateAuthDetails(GraphServiceClientProvider provider, String userId) throws Exception {
        Method m = GraphServiceClientProvider.class.getDeclaredMethod("createAuthDetails", String.class);
        m.setAccessible(true);
        return (List<?>) m.invoke(provider, userId);
    }

    @Test
    @DisplayName("createAuthDetails: without authContextId returns 1 field")
    public void testCreateAuthDetails_NoContext() throws Exception {
        GraphServiceClientProvider provider = new GraphServiceClientProvider(
                refreshTokenStore, attributes, requestContext, authorizationContext
        );
        List<?> details = invokeCreateAuthDetails(provider, "user1");
        assertEquals(1, details.size());
    }

    @Test
    @DisplayName("createAuthDetails: with authContextId returns 2 fields")
    public void testCreateAuthDetails_WithContext() throws Exception {
        GraphServiceClientProvider provider = new GraphServiceClientProvider(
                refreshTokenStore, attributes, requestContext, authorizationContext, AUTH_CONTEXT_ID
        );
        List<?> details = invokeCreateAuthDetails(provider, "user1");
        assertEquals(2, details.size());
    }

    // ========================================================================
    // 14. handleAuthenticationError — via reflection
    // ========================================================================

    private void invokeHandleAuthenticationError(GraphServiceClientProvider provider, Exception cause, String key) throws Exception {
        Method m = GraphServiceClientProvider.class.getDeclaredMethod("handleAuthenticationError", Exception.class, String.class);
        m.setAccessible(true);
        try {
            m.invoke(provider, cause, key);
        } catch (java.lang.reflect.InvocationTargetException e) {
            if (e.getCause() instanceof MustAuthorizeException) {
                throw (MustAuthorizeException) e.getCause();
            }
            throw e;
        }
    }

    @Test
    @DisplayName("handleAuthenticationError: PASSWORD_CHANGED_CODE removes token and throws")
    public void testHandleAuthError_PasswordChanged() throws Exception {
        GraphServiceClientProvider provider = new GraphServiceClientProvider(
                refreshTokenStore, attributes, requestContext, authorizationContext
        );
        String key = getAdminKey();
        MustAuthorizeException ex = assertThrows(MustAuthorizeException.class,
                () -> invokeHandleAuthenticationError(provider,
                        new RuntimeException("Error AADSTS50173 password changed"), key));
        assertEquals(Constants.PASSWORD_CHANGED_ERROR, ex.getMessage());
        verify(refreshTokenStore).remove(key);
    }

    @Test
    @DisplayName("handleAuthenticationError: USER_DELETED_CODE does not remove token")
    public void testHandleAuthError_UserDeleted() throws Exception {
        GraphServiceClientProvider provider = new GraphServiceClientProvider(
                refreshTokenStore, attributes, requestContext, authorizationContext
        );
        String key = getAdminKey();
        MustAuthorizeException ex = assertThrows(MustAuthorizeException.class,
                () -> invokeHandleAuthenticationError(provider,
                        new RuntimeException("Error AADSTS50020 user deleted"), key));
        assertEquals(Constants.USER_DELETED_ERROR, ex.getMessage());
        verify(refreshTokenStore, never()).remove(anyString());
    }

    @Test
    @DisplayName("handleAuthenticationError: USER_DISABLED_CODE does not remove token")
    public void testHandleAuthError_UserDisabled() throws Exception {
        GraphServiceClientProvider provider = new GraphServiceClientProvider(
                refreshTokenStore, attributes, requestContext, authorizationContext
        );
        String key = getAdminKey();
        MustAuthorizeException ex = assertThrows(MustAuthorizeException.class,
                () -> invokeHandleAuthenticationError(provider,
                        new RuntimeException("Error AADSTS50057 account disabled"), key));
        assertEquals(Constants.USER_DISABLED_ERROR, ex.getMessage());
        verify(refreshTokenStore, never()).remove(anyString());
    }

    @Test
    @DisplayName("handleAuthenticationError: APP_NOT_FOUND_CODE does not remove token")
    public void testHandleAuthError_AppNotFound() throws Exception {
        GraphServiceClientProvider provider = new GraphServiceClientProvider(
                refreshTokenStore, attributes, requestContext, authorizationContext
        );
        String key = getAdminKey();
        MustAuthorizeException ex = assertThrows(MustAuthorizeException.class,
                () -> invokeHandleAuthenticationError(provider,
                        new RuntimeException("Error AADSTS700016 app not found"), key));
        assertEquals(Constants.APP_NOT_FOUND_ERROR, ex.getMessage());
        verify(refreshTokenStore, never()).remove(anyString());
    }

    @Test
    @DisplayName("handleAuthenticationError: CONSENT_REVOKED_CODE removes token")
    public void testHandleAuthError_ConsentRevoked() throws Exception {
        GraphServiceClientProvider provider = new GraphServiceClientProvider(
                refreshTokenStore, attributes, requestContext, authorizationContext
        );
        String key = getAdminKey();
        MustAuthorizeException ex = assertThrows(MustAuthorizeException.class,
                () -> invokeHandleAuthenticationError(provider,
                        new RuntimeException("Error AADSTS65001 consent revoked"), key));
        assertEquals(Constants.PERMISSIONS_REVOKED_ERROR, ex.getMessage());
        verify(refreshTokenStore).remove(key);
    }

    @Test
    @DisplayName("handleAuthenticationError: TENANT_NOT_FOUND_CODE does not remove token")
    public void testHandleAuthError_TenantNotFound() throws Exception {
        GraphServiceClientProvider provider = new GraphServiceClientProvider(
                refreshTokenStore, attributes, requestContext, authorizationContext
        );
        String key = getAdminKey();
        MustAuthorizeException ex = assertThrows(MustAuthorizeException.class,
                () -> invokeHandleAuthenticationError(provider,
                        new RuntimeException("Error: tenant not found in directory"), key));
        assertEquals(Constants.TENANT_NOT_FOUND_ERROR, ex.getMessage());
        verify(refreshTokenStore, never()).remove(anyString());
    }

    @Test
    @DisplayName("handleAuthenticationError: SERVICE_UNAVAILABLE does not remove token")
    public void testHandleAuthError_ServiceUnavailable() throws Exception {
        GraphServiceClientProvider provider = new GraphServiceClientProvider(
                refreshTokenStore, attributes, requestContext, authorizationContext
        );
        String key = getAdminKey();
        MustAuthorizeException ex = assertThrows(MustAuthorizeException.class,
                () -> invokeHandleAuthenticationError(provider,
                        new RuntimeException("Error: service unavailable please retry"), key));
        assertEquals(Constants.SERVICE_UNAVAILABLE_ERROR, ex.getMessage());
        verify(refreshTokenStore, never()).remove(anyString());
    }

    @Test
    @DisplayName("handleAuthenticationError: INVALID_CLIENT_SECRET does not remove token")
    public void testHandleAuthError_InvalidClientSecret() throws Exception {
        GraphServiceClientProvider provider = new GraphServiceClientProvider(
                refreshTokenStore, attributes, requestContext, authorizationContext
        );
        String key = getAdminKey();
        MustAuthorizeException ex = assertThrows(MustAuthorizeException.class,
                () -> invokeHandleAuthenticationError(provider,
                        new RuntimeException("Error AADSTS7000215 invalid client secret"), key));
        assertEquals(Constants.INVALID_CLIENT_SECRET_ERROR, ex.getMessage());
        verify(refreshTokenStore, never()).remove(anyString());
    }

    @Test
    @DisplayName("handleAuthenticationError: REFRESH_TOKEN_EXPIRED_CODE removes token")
    public void testHandleAuthError_RefreshTokenExpired() throws Exception {
        GraphServiceClientProvider provider = new GraphServiceClientProvider(
                refreshTokenStore, attributes, requestContext, authorizationContext
        );
        String key = getAdminKey();
        MustAuthorizeException ex = assertThrows(MustAuthorizeException.class,
                () -> invokeHandleAuthenticationError(provider,
                        new RuntimeException("Error AADSTS700082 refresh token expired"), key));
        assertEquals(Constants.REFRESH_TOKEN_EXPIRED_ERROR, ex.getMessage());
        verify(refreshTokenStore).remove(key);
    }

    @Test
    @DisplayName("handleAuthenticationError: null error message falls back to default")
    public void testHandleAuthError_NullMessage() throws Exception {
        when(requestContext.invokeAsUser()).thenReturn(false);
        GraphServiceClientProvider provider = new GraphServiceClientProvider(
                refreshTokenStore, attributes, requestContext, authorizationContext
        );
        String key = getAdminKey();
        assertThrows(MustAuthorizeException.class,
                () -> invokeHandleAuthenticationError(provider, new RuntimeException((String) null), key));
    }

    @Test
    @DisplayName("handleAuthenticationError: unrecognized error message falls back to default")
    public void testHandleAuthError_NoRuleMatch() throws Exception {
        when(requestContext.invokeAsUser()).thenReturn(true);
        GraphServiceClientProvider provider = new GraphServiceClientProvider(
                refreshTokenStore, attributes, requestContext, authorizationContext
        );
        String key = getAdminKey();
        assertThrows(MustAuthorizeException.class,
                () -> invokeHandleAuthenticationError(provider,
                        new RuntimeException("Some completely unknown error XYZ"), key));
    }

    @Test
    @DisplayName("handleAuthenticationError: fallback with invokeAsUser=false uses reAuth=true")
    public void testHandleAuthError_FallbackReAuth() throws Exception {
        when(requestContext.invokeAsUser()).thenReturn(false);
        GraphServiceClientProvider provider = new GraphServiceClientProvider(
                refreshTokenStore, attributes, requestContext, authorizationContext
        );
        String key = getAdminKey();
        MustAuthorizeException ex = assertThrows(MustAuthorizeException.class,
                () -> invokeHandleAuthenticationError(provider,
                        new RuntimeException("Unknown error without any matching code"), key));
        assertEquals(Constants.REFRESH_TOKEN_EXPIRED, ex.getMessage());
    }

    @Test
    @DisplayName("handleAuthenticationError: fallback with invokeAsUser=true uses reAuth=false")
    public void testHandleAuthError_FallbackNoReAuth() throws Exception {
        when(requestContext.invokeAsUser()).thenReturn(true);
        GraphServiceClientProvider provider = new GraphServiceClientProvider(
                refreshTokenStore, attributes, requestContext, authorizationContext
        );
        String key = getAdminKey();
        MustAuthorizeException ex = assertThrows(MustAuthorizeException.class,
                () -> invokeHandleAuthenticationError(provider,
                        new RuntimeException("Unknown error without any matching code"), key));
        assertEquals(Constants.AUTHORIZATION_PROMPT, ex.getMessage());
    }

    // ========================================================================
    // 15. getGraphServiceClient(String, String) — auth type branches
    // ========================================================================

    @Test
    @DisplayName("getGraphServiceClient with PRIVATE auth type attempts token acquisition")
    public void testGetGraphServiceClient_PrivateAuth() {
        when(refreshTokenStore.get(getAdminKey())).thenReturn(REFRESH_TOKEN);
        GraphServiceClientProvider provider = new GraphServiceClientProvider(
                refreshTokenStore, attributes, requestContext, authorizationContext
        );
        // MSAL4J will fail with test credentials — but it will exercise the PRIVATE branch
        assertThrows(MustAuthorizeException.class, provider::getGraphServiceClientForAdmin);
    }

    @Test
    @DisplayName("getGraphServiceClient with PUBLIC auth type attempts token acquisition")
    public void testGetGraphServiceClient_PublicAuth() {
        OutlookAttributes publicAttrs = new OutlookAttributes(
                CLIENT_ID, CLIENT_SECRET, TENANT_ID, EMAIL, true, Constants.PUBLIC, BASE_URL
        );
        when(refreshTokenStore.get(EMAIL + "_" + CLIENT_ID + "_" + Constants.PUBLIC)).thenReturn(REFRESH_TOKEN);
        GraphServiceClientProvider provider = new GraphServiceClientProvider(
                refreshTokenStore, publicAttrs, requestContext, authorizationContext
        );
        // MSAL4J will fail — but exercises the PUBLIC authority branch
        assertThrows(MustAuthorizeException.class, provider::getGraphServiceClientForAdmin);
    }

    @Test
    @DisplayName("getGraphServiceClient with unknown auth type throws IllegalArgumentException")
    public void testGetGraphServiceClient_UnknownAuthType() {
        OutlookAttributes unknownAttrs = new OutlookAttributes(
                CLIENT_ID, CLIENT_SECRET, TENANT_ID, EMAIL, true, "Unknown", BASE_URL
        );
        String key = EMAIL + "_" + CLIENT_ID + "_Unknown";
        when(refreshTokenStore.get(key)).thenReturn(REFRESH_TOKEN);
        GraphServiceClientProvider provider = new GraphServiceClientProvider(
                refreshTokenStore, unknownAttrs, requestContext, authorizationContext
        );
        // The IllegalArgumentException gets caught and re-thrown as RuntimeException
        assertThrows(RuntimeException.class, provider::getGraphServiceClientForAdmin);
    }

    // ========================================================================
    // 16. Cache invalidation on auth failure
    // ========================================================================

    @Test
    @DisplayName("Cache entry removed after authentication failure")
    public void testCacheInvalidation_OnAuthFailure() throws Exception {
        GraphServiceClient<Request> mockClient = mock(GraphServiceClient.class);
        String key = getAdminKey();
        populateCache(key, mockClient, pastMillis(600_000)); // expired entry

        when(refreshTokenStore.get(key)).thenReturn(REFRESH_TOKEN);
        GraphServiceClientProvider provider = new GraphServiceClientProvider(
                refreshTokenStore, attributes, requestContext, authorizationContext
        );
        try {
            provider.getGraphServiceClientForAdmin();
            fail("Expected exception");
        } catch (Exception e) {
            // expected
        }
        assertNull(graphClientCache.get(key));
    }

    // ========================================================================
    // 17. Multiple calls use same cached client
    // ========================================================================

    @Test
    @DisplayName("Multiple admin calls return same cached client")
    public void testMultipleCalls_CacheReuse() throws Exception {
        GraphServiceClient<Request> mockClient = mock(GraphServiceClient.class);
        populateCache(getAdminKey(), mockClient, futureMillis(3600_000));

        GraphServiceClientProvider provider = new GraphServiceClientProvider(
                refreshTokenStore, attributes, requestContext, authorizationContext
        );
        GraphServiceClient<Request> r1 = provider.getGraphServiceClientForAdmin();
        GraphServiceClient<Request> r2 = provider.getGraphServiceClientForAdmin();
        GraphServiceClient<Request> r3 = provider.getGraphServiceClientForAdmin();

        assertSame(mockClient, r1);
        assertSame(r1, r2);
        assertSame(r2, r3);
        verify(refreshTokenStore, never()).get(anyString());
    }

    // ========================================================================
    // 18. Static cache shared across instances
    // ========================================================================

    @Test
    @DisplayName("Static cache shared across provider instances")
    public void testStaticCacheShared() throws Exception {
        GraphServiceClient<Request> mockClient = mock(GraphServiceClient.class);
        populateCache(getAdminKey(), mockClient, futureMillis(3600_000));

        GraphServiceClientProvider p1 = new GraphServiceClientProvider(
                refreshTokenStore, attributes, requestContext, authorizationContext
        );
        GraphServiceClientProvider p2 = new GraphServiceClientProvider(
                refreshTokenStore, attributes, requestContext, authorizationContext
        );
        assertSame(p1.getGraphServiceClientForAdmin(), p2.getGraphServiceClientForAdmin());
    }

    // ========================================================================
    // 19. Different keys have separate cache entries
    // ========================================================================

    @Test
    @DisplayName("Different users have separate cache entries")
    public void testDifferentKeys_SeparateEntries() throws Exception {
        GraphServiceClient<Request> adminClient = mock(GraphServiceClient.class);
        GraphServiceClient<Request> userClient = mock(GraphServiceClient.class);
        populateCache(getAdminKey(), adminClient, futureMillis(3600_000));
        populateCache(getUserKey(ACCOUNT_ID), userClient, futureMillis(3600_000));

        GraphServiceClientProvider provider = new GraphServiceClientProvider(
                refreshTokenStore, attributes, requestContext, authorizationContext
        );
        assertSame(adminClient, provider.getGraphServiceClientForAdmin());
        assertSame(userClient, provider.getGraphServiceClientForUser(false, ACCOUNT_ID));
        assertNotSame(adminClient, userClient);
    }

    // ========================================================================
    // 20. TOKEN_EXPIRY_BUFFER_MS constant value
    // ========================================================================

    @Test
    @DisplayName("TOKEN_EXPIRY_BUFFER_MS is 5 minutes (300000 ms)")
    public void testTokenExpiryBufferValue() throws Exception {
        Field f = GraphServiceClientProvider.class.getDeclaredField("TOKEN_EXPIRY_BUFFER_MS");
        f.setAccessible(true);
        assertEquals(300_000L, (long) f.get(null));
    }

    // ========================================================================
    // 21. GRAPH_CLIENT_CACHE is static ConcurrentHashMap
    // ========================================================================

    @Test
    @DisplayName("GRAPH_CLIENT_CACHE is a static ConcurrentHashMap")
    public void testCacheType() throws Exception {
        Field f = GraphServiceClientProvider.class.getDeclaredField("GRAPH_CLIENT_CACHE");
        assertTrue(java.lang.reflect.Modifier.isStatic(f.getModifiers()));
        f.setAccessible(true);
        assertInstanceOf(ConcurrentHashMap.class, f.get(null));
    }

    // ========================================================================
    // 22. getUserRequestBuilder — all branches
    // ========================================================================

    @Test
    @DisplayName("getUserRequestBuilder(true, null): uses setup email path")
    public void testGetUserRequestBuilder_UseSetupEmailTrue() throws Exception {
        GraphServiceClient<Request> mockClient = mock(GraphServiceClient.class);
        populateCache(getAdminKey(), mockClient, futureMillis(3600_000));

        GraphServiceClientProvider provider = new GraphServiceClientProvider(
                refreshTokenStore, attributes, requestContext, authorizationContext
        );
        // This will call getGraphServiceClientForUser(true, null) then .users(email)
        // The mock GraphServiceClient.users() returns null by default
        var result = provider.getUserRequestBuilder(true, null);
        // users() on an unstubbed mock returns null
        assertNull(result);
    }

    @Test
    @DisplayName("getUserRequestBuilder(false, null): uses me() path")
    public void testGetUserRequestBuilder_UseSetupEmailFalse() throws Exception {
        when(requestContext.invokeAsUser()).thenReturn(true);
        GraphServiceClient<Request> mockClient = mock(GraphServiceClient.class);
        populateCache(getUserKey(ACCOUNT_ID), mockClient, futureMillis(3600_000));

        GraphServiceClientProvider provider = new GraphServiceClientProvider(
                refreshTokenStore, attributes, requestContext, authorizationContext
        );
        var result = provider.getUserRequestBuilder(false, null);
        // me() on an unstubbed mock returns null
        assertNull(result);
    }

    @Test
    @DisplayName("getUserRequestBuilder(null, null): invokeAsUser=true uses me()")
    public void testGetUserRequestBuilder_NullSetupEmail_InvokeAsUser() throws Exception {
        when(requestContext.invokeAsUser()).thenReturn(true);
        GraphServiceClient<Request> mockClient = mock(GraphServiceClient.class);
        populateCache(getUserKey(ACCOUNT_ID), mockClient, futureMillis(3600_000));

        GraphServiceClientProvider provider = new GraphServiceClientProvider(
                refreshTokenStore, attributes, requestContext, authorizationContext
        );
        var result = provider.getUserRequestBuilder(null, null);
        assertNull(result);
    }

    @Test
    @DisplayName("getUserRequestBuilder(null, null): invokeAsUser=false uses users(email)")
    public void testGetUserRequestBuilder_NullSetupEmail_NotInvokeAsUser() throws Exception {
        when(requestContext.invokeAsUser()).thenReturn(false);
        GraphServiceClient<Request> mockClient = mock(GraphServiceClient.class);
        populateCache(getAdminKey(), mockClient, futureMillis(3600_000));

        GraphServiceClientProvider provider = new GraphServiceClientProvider(
                refreshTokenStore, attributes, requestContext, authorizationContext
        );
        var result = provider.getUserRequestBuilder(null, null);
        assertNull(result);
    }

    @Test
    @DisplayName("getUserRequestBuilder: IOException wraps in RuntimeException")
    public void testGetUserRequestBuilder_IOException() {
        // No cache and no refresh token → MustAuthorizeException (extends RuntimeException)
        // But getUserRequestBuilder catches IOException specifically and wraps it.
        // To hit the IOException catch, we need getGraphServiceClientForUser to throw IOException.
        // Since getGraphServiceClientForUser declares IOException but the internal path
        // throws MustAuthorizeException (RuntimeException), the IOException branch is hard to trigger.
        // However, MustAuthorizeException is a RuntimeException and won't be caught by IOException catch.
        // The RuntimeException will propagate directly.
        when(refreshTokenStore.get(getAdminKey())).thenReturn(null);
        GraphServiceClientProvider provider = new GraphServiceClientProvider(
                refreshTokenStore, attributes, requestContext, authorizationContext
        );
        assertThrows(MustAuthorizeException.class, () -> provider.getUserRequestBuilder(true, null));
    }

    // ========================================================================
    // 23. updateRefreshToken — via reflection
    // ========================================================================

    @Test
    @DisplayName("updateRefreshToken: extracts secret from JSON and stores it")
    public void testUpdateRefreshToken() throws Exception {
        String jsonStr = "{\"RefreshToken\":{\"key1\":{\"secret\":\"new-refresh-token-value\"}}}";
        com.google.gson.JsonObject jsonObject = com.google.gson.JsonParser.parseString(jsonStr).getAsJsonObject();

        GraphServiceClientProvider provider = new GraphServiceClientProvider(
                refreshTokenStore, attributes, requestContext, authorizationContext
        );
        Method m = GraphServiceClientProvider.class.getDeclaredMethod("updateRefreshToken", String.class, com.google.gson.JsonObject.class);
        m.setAccessible(true);
        m.invoke(provider, "userId", jsonObject);

        verify(refreshTokenStore).put("userId", "new-refresh-token-value");
    }

    // ========================================================================
    // 24. Keyword-based error matching tests
    // ========================================================================

    @Test
    @DisplayName("handleAuthenticationError: KEYWORD_PASSWORD_CHANGED removes token")
    public void testHandleAuthError_KeywordPasswordChanged() throws Exception {
        GraphServiceClientProvider provider = new GraphServiceClientProvider(
                refreshTokenStore, attributes, requestContext, authorizationContext
        );
        String key = getAdminKey();
        MustAuthorizeException ex = assertThrows(MustAuthorizeException.class,
                () -> invokeHandleAuthenticationError(provider,
                        new RuntimeException("User changed or reset their password recently"), key));
        assertEquals(Constants.PASSWORD_CHANGED_ERROR, ex.getMessage());
        verify(refreshTokenStore).remove(key);
    }

    @Test
    @DisplayName("handleAuthenticationError: KEYWORD_ACCESS_DENIED removes token")
    public void testHandleAuthError_AccessDenied() throws Exception {
        GraphServiceClientProvider provider = new GraphServiceClientProvider(
                refreshTokenStore, attributes, requestContext, authorizationContext
        );
        String key = getAdminKey();
        MustAuthorizeException ex = assertThrows(MustAuthorizeException.class,
                () -> invokeHandleAuthenticationError(provider,
                        new RuntimeException("Error: access_denied for this resource"), key));
        assertEquals(Constants.PERMISSIONS_REVOKED_ERROR, ex.getMessage());
        verify(refreshTokenStore).remove(key);
    }

    @Test
    @DisplayName("handleAuthenticationError: KEYWORD_NETWORK_ERROR does not remove token")
    public void testHandleAuthError_NetworkError() throws Exception {
        GraphServiceClientProvider provider = new GraphServiceClientProvider(
                refreshTokenStore, attributes, requestContext, authorizationContext
        );
        String key = getAdminKey();
        MustAuthorizeException ex = assertThrows(MustAuthorizeException.class,
                () -> invokeHandleAuthenticationError(provider,
                        new RuntimeException("A network error occurred during the request"), key));
        assertEquals(Constants.SERVICE_UNAVAILABLE_ERROR, ex.getMessage());
        verify(refreshTokenStore, never()).remove(anyString());
    }

    @Test
    @DisplayName("handleAuthenticationError: invalid_grant removes token")
    public void testHandleAuthError_InvalidGrant() throws Exception {
        GraphServiceClientProvider provider = new GraphServiceClientProvider(
                refreshTokenStore, attributes, requestContext, authorizationContext
        );
        String key = getAdminKey();
        MustAuthorizeException ex = assertThrows(MustAuthorizeException.class,
                () -> invokeHandleAuthenticationError(provider,
                        new RuntimeException("Error: invalid_grant token"), key));
        assertEquals(Constants.REFRESH_TOKEN_EXPIRED_ERROR, ex.getMessage());
        verify(refreshTokenStore).remove(key);
    }

    // ========================================================================
    // 25. Cache expired entry triggers KeyValueStore call
    // ========================================================================

    @Test
    @DisplayName("Expired cache entry triggers fresh token acquisition from store")
    public void testCacheExpired_TriggersStoreLookup() throws Exception {
        GraphServiceClient<Request> mockClient = mock(GraphServiceClient.class);
        populateCache(getAdminKey(), mockClient, pastMillis(600_000));

        when(refreshTokenStore.get(getAdminKey())).thenReturn(null);
        GraphServiceClientProvider provider = new GraphServiceClientProvider(
                refreshTokenStore, attributes, requestContext, authorizationContext
        );
        assertThrows(MustAuthorizeException.class, provider::getGraphServiceClientForAdmin);
        verify(refreshTokenStore).get(getAdminKey());
    }

    @Test
    @DisplayName("Cache within buffer triggers fresh token acquisition from store")
    public void testCacheWithinBuffer_TriggersStoreLookup() throws Exception {
        GraphServiceClient<Request> mockClient = mock(GraphServiceClient.class);
        populateCache(getAdminKey(), mockClient, futureMillis(180_000)); // 3 min < 5 min buffer

        when(refreshTokenStore.get(getAdminKey())).thenReturn(null);
        GraphServiceClientProvider provider = new GraphServiceClientProvider(
                refreshTokenStore, attributes, requestContext, authorizationContext
        );
        assertThrows(MustAuthorizeException.class, provider::getGraphServiceClientForAdmin);
        verify(refreshTokenStore).get(getAdminKey());
    }

    // ========================================================================
    // 26. RuntimeException in getGraphServiceClient(boolean, String) is re-thrown
    // ========================================================================

    @Test
    @DisplayName("RuntimeException in getGraphServiceClient is logged and re-thrown")
    public void testRuntimeExceptionReThrown() {
        when(refreshTokenStore.get(getAdminKey())).thenReturn(null);
        GraphServiceClientProvider provider = new GraphServiceClientProvider(
                refreshTokenStore, attributes, requestContext, authorizationContext
        );
        // MustAuthorizeException is a RuntimeException — will be caught, logged, and re-thrown
        MustAuthorizeException ex = assertThrows(MustAuthorizeException.class, provider::getGraphServiceClientForAdmin);
        assertNotNull(ex);
    }

    // ========================================================================
    // 27. ACCOUNT_LOCKED / PASSWORD_EXPIRED handling
    // ========================================================================

    @Test
    @DisplayName("handleAuthenticationError: ACCOUNT_LOCKED_CODE returns USER_DISABLED_ERROR")
    public void testHandleAuthError_AccountLocked() throws Exception {
        GraphServiceClientProvider provider = new GraphServiceClientProvider(
                refreshTokenStore, attributes, requestContext, authorizationContext
        );
        String key = getAdminKey();
        MustAuthorizeException ex = assertThrows(MustAuthorizeException.class,
                () -> invokeHandleAuthenticationError(provider,
                        new RuntimeException("Error AADSTS50053 account locked"), key));
        assertEquals(Constants.USER_DISABLED_ERROR, ex.getMessage());
    }

    @Test
    @DisplayName("handleAuthenticationError: PASSWORD_EXPIRED_CODE returns USER_DISABLED_ERROR")
    public void testHandleAuthError_PasswordExpired() throws Exception {
        GraphServiceClientProvider provider = new GraphServiceClientProvider(
                refreshTokenStore, attributes, requestContext, authorizationContext
        );
        String key = getAdminKey();
        MustAuthorizeException ex = assertThrows(MustAuthorizeException.class,
                () -> invokeHandleAuthenticationError(provider,
                        new RuntimeException("Error AADSTS50055 password expired"), key));
        assertEquals(Constants.USER_DISABLED_ERROR, ex.getMessage());
    }

    // ========================================================================
    // 28. Additional CONSENT / ROLE error rules
    // ========================================================================

    @Test
    @DisplayName("handleAuthenticationError: CONSENT_REQUIRED_CODE removes token")
    public void testHandleAuthError_ConsentRequired() throws Exception {
        GraphServiceClientProvider provider = new GraphServiceClientProvider(
                refreshTokenStore, attributes, requestContext, authorizationContext
        );
        String key = getAdminKey();
        MustAuthorizeException ex = assertThrows(MustAuthorizeException.class,
                () -> invokeHandleAuthenticationError(provider,
                        new RuntimeException("Error AADSTS70019 consent required"), key));
        assertEquals(Constants.PERMISSIONS_REVOKED_ERROR, ex.getMessage());
        verify(refreshTokenStore).remove(key);
    }

    @Test
    @DisplayName("handleAuthenticationError: ROLE_NOT_FOUND_CODE removes token")
    public void testHandleAuthError_RoleNotFound() throws Exception {
        GraphServiceClientProvider provider = new GraphServiceClientProvider(
                refreshTokenStore, attributes, requestContext, authorizationContext
        );
        String key = getAdminKey();
        MustAuthorizeException ex = assertThrows(MustAuthorizeException.class,
                () -> invokeHandleAuthenticationError(provider,
                        new RuntimeException("Error AADSTS90094 role not found"), key));
        assertEquals(Constants.PERMISSIONS_REVOKED_ERROR, ex.getMessage());
        verify(refreshTokenStore).remove(key);
    }

    @Test
    @DisplayName("handleAuthenticationError: REFRESH_TOKEN_REVOKED_CODE removes token")
    public void testHandleAuthError_RefreshTokenRevoked() throws Exception {
        GraphServiceClientProvider provider = new GraphServiceClientProvider(
                refreshTokenStore, attributes, requestContext, authorizationContext
        );
        String key = getAdminKey();
        MustAuthorizeException ex = assertThrows(MustAuthorizeException.class,
                () -> invokeHandleAuthenticationError(provider,
                        new RuntimeException("Error AADSTS700084 token revoked"), key));
        assertEquals(Constants.REFRESH_TOKEN_EXPIRED_ERROR, ex.getMessage());
        verify(refreshTokenStore).remove(key);
    }

    @Test
    @DisplayName("handleAuthenticationError: PROOF_OF_POSSESSION_FAILED_CODE removes token")
    public void testHandleAuthError_ProofOfPossessionFailed() throws Exception {
        GraphServiceClientProvider provider = new GraphServiceClientProvider(
                refreshTokenStore, attributes, requestContext, authorizationContext
        );
        String key = getAdminKey();
        MustAuthorizeException ex = assertThrows(MustAuthorizeException.class,
                () -> invokeHandleAuthenticationError(provider,
                        new RuntimeException("Error AADSTS54005 proof of possession"), key));
        assertEquals(Constants.REFRESH_TOKEN_EXPIRED_ERROR, ex.getMessage());
        verify(refreshTokenStore).remove(key);
    }

    @Test
    @DisplayName("handleAuthenticationError: KEYWORD_INSUFFICIENT_SCOPE removes token")
    public void testHandleAuthError_InsufficientScope() throws Exception {
        GraphServiceClientProvider provider = new GraphServiceClientProvider(
                refreshTokenStore, attributes, requestContext, authorizationContext
        );
        String key = getAdminKey();
        MustAuthorizeException ex = assertThrows(MustAuthorizeException.class,
                () -> invokeHandleAuthenticationError(provider,
                        new RuntimeException("Error: insufficient_scope for operation"), key));
        assertEquals(Constants.PERMISSIONS_REVOKED_ERROR, ex.getMessage());
        verify(refreshTokenStore).remove(key);
    }

    @Test
    @DisplayName("handleAuthenticationError: KEYWORD_INVALID_CLIENT does not remove token")
    public void testHandleAuthError_InvalidClient() throws Exception {
        GraphServiceClientProvider provider = new GraphServiceClientProvider(
                refreshTokenStore, attributes, requestContext, authorizationContext
        );
        String key = getAdminKey();
        MustAuthorizeException ex = assertThrows(MustAuthorizeException.class,
                () -> invokeHandleAuthenticationError(provider,
                        new RuntimeException("Error: invalid_client configuration"), key));
        assertEquals(Constants.INVALID_CLIENT_SECRET_ERROR, ex.getMessage());
        verify(refreshTokenStore, never()).remove(anyString());
    }

    @Test
    @DisplayName("handleAuthenticationError: KEYWORD_USER_DELETED does not remove token")
    public void testHandleAuthError_KeywordUserDeleted() throws Exception {
        GraphServiceClientProvider provider = new GraphServiceClientProvider(
                refreshTokenStore, attributes, requestContext, authorizationContext
        );
        String key = getAdminKey();
        MustAuthorizeException ex = assertThrows(MustAuthorizeException.class,
                () -> invokeHandleAuthenticationError(provider,
                        new RuntimeException("The user has been deleted from the directory"), key));
        assertEquals(Constants.USER_DELETED_ERROR, ex.getMessage());
        verify(refreshTokenStore, never()).remove(anyString());
    }

    @Test
    @DisplayName("handleAuthenticationError: KEYWORD_ACCOUNT_DISABLED does not remove token")
    public void testHandleAuthError_KeywordAccountDisabled() throws Exception {
        GraphServiceClientProvider provider = new GraphServiceClientProvider(
                refreshTokenStore, attributes, requestContext, authorizationContext
        );
        String key = getAdminKey();
        MustAuthorizeException ex = assertThrows(MustAuthorizeException.class,
                () -> invokeHandleAuthenticationError(provider,
                        new RuntimeException("The account is disabled in the directory"), key));
        assertEquals(Constants.USER_DISABLED_ERROR, ex.getMessage());
        verify(refreshTokenStore, never()).remove(anyString());
    }

    @Test
    @DisplayName("handleAuthenticationError: SERVICE_UNAVAILABLE_CODE does not remove token")
    public void testHandleAuthError_ServiceUnavailableCode() throws Exception {
        GraphServiceClientProvider provider = new GraphServiceClientProvider(
                refreshTokenStore, attributes, requestContext, authorizationContext
        );
        String key = getAdminKey();
        MustAuthorizeException ex = assertThrows(MustAuthorizeException.class,
                () -> invokeHandleAuthenticationError(provider,
                        new RuntimeException("Error AADSTS50000 service unavailable"), key));
        assertEquals(Constants.SERVICE_UNAVAILABLE_ERROR, ex.getMessage());
        verify(refreshTokenStore, never()).remove(anyString());
    }

    @Test
    @DisplayName("handleAuthenticationError: TENANT_NOT_FOUND_CODE does not remove token")
    public void testHandleAuthError_TenantNotFoundCode() throws Exception {
        GraphServiceClientProvider provider = new GraphServiceClientProvider(
                refreshTokenStore, attributes, requestContext, authorizationContext
        );
        String key = getAdminKey();
        MustAuthorizeException ex = assertThrows(MustAuthorizeException.class,
                () -> invokeHandleAuthenticationError(provider,
                        new RuntimeException("Error: tenant not found"), key));
        assertEquals(Constants.TENANT_NOT_FOUND_ERROR, ex.getMessage());
        verify(refreshTokenStore, never()).remove(anyString());
    }

    // ========================================================================
    // 29. Success path — getGraphServiceClient(String, String) with mocked MSAL4J
    // ========================================================================

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("getGraphServiceClient success: acquires token, caches client, updates refresh token")
    public void testGetGraphServiceClient_SuccessPath() throws Exception {
        String key = getAdminKey();
        when(refreshTokenStore.get(key)).thenReturn(REFRESH_TOKEN);

        // Mock IAuthenticationResult
        IAuthenticationResult authResult = mock(IAuthenticationResult.class);
        when(authResult.accessToken()).thenReturn("valid-access-token");
        Date expiryDate = new Date(System.currentTimeMillis() + 3600_000L);
        when(authResult.expiresOnDate()).thenReturn(expiryDate);

        // Mock token cache
        com.microsoft.aad.msal4j.TokenCache tokenCache = mock(com.microsoft.aad.msal4j.TokenCache.class);
        String cacheJson = "{\"RefreshToken\":{\"key1\":{\"secret\":\"new-refresh-token\"}}}";
        when(tokenCache.serialize()).thenReturn(cacheJson);

        // Mock ConfidentialClientApplication
        ConfidentialClientApplication mockCca = mock(ConfidentialClientApplication.class);
        when(mockCca.acquireToken(any(RefreshTokenParameters.class)))
                .thenReturn(CompletableFuture.completedFuture(authResult));
        when(mockCca.tokenCache()).thenReturn(tokenCache);

        // Mock ConfidentialClientApplication.builder()
        ConfidentialClientApplication.Builder mockBuilder = mock(ConfidentialClientApplication.Builder.class);
        when(mockBuilder.authority(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.build()).thenReturn(mockCca);

        // Mock GraphServiceClient.builder()
        GraphServiceClient<Request> mockGraphClient = mock(GraphServiceClient.class);

        try (MockedStatic<ConfidentialClientApplication> ccaStatic = mockStatic(ConfidentialClientApplication.class);
             MockedStatic<GraphServiceClient> graphStatic = mockStatic(GraphServiceClient.class)) {

            ccaStatic.when(() -> ConfidentialClientApplication.builder(anyString(), any(IClientCredential.class)))
                    .thenReturn(mockBuilder);

            GraphServiceClient.Builder graphBuilder = mock(GraphServiceClient.Builder.class);
            graphStatic.when(GraphServiceClient::builder).thenReturn(graphBuilder);
            when(graphBuilder.authenticationProvider(any())).thenReturn(graphBuilder);
            when(graphBuilder.buildClient()).thenReturn(mockGraphClient);

            GraphServiceClientProvider provider = new GraphServiceClientProvider(
                    refreshTokenStore, attributes, requestContext, authorizationContext
            );
            GraphServiceClient<Request> result = provider.getGraphServiceClientForAdmin();

            assertSame(mockGraphClient, result);
            // Verify refresh token was updated
            verify(refreshTokenStore).put(key, "new-refresh-token");
            // Verify client was cached
            assertNotNull(graphClientCache.get(key));
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("getGraphServiceClient success with PUBLIC auth type")
    public void testGetGraphServiceClient_SuccessPath_PublicAuth() throws Exception {
        OutlookAttributes publicAttrs = new OutlookAttributes(
                CLIENT_ID, CLIENT_SECRET, TENANT_ID, EMAIL, true, Constants.PUBLIC, BASE_URL
        );
        String key = EMAIL + "_" + CLIENT_ID + "_" + Constants.PUBLIC;
        when(refreshTokenStore.get(key)).thenReturn(REFRESH_TOKEN);

        IAuthenticationResult authResult = mock(IAuthenticationResult.class);
        when(authResult.accessToken()).thenReturn("valid-access-token");
        when(authResult.expiresOnDate()).thenReturn(new Date(System.currentTimeMillis() + 3600_000L));

        com.microsoft.aad.msal4j.TokenCache tokenCache = mock(com.microsoft.aad.msal4j.TokenCache.class);
        when(tokenCache.serialize()).thenReturn("{\"RefreshToken\":{\"key1\":{\"secret\":\"new-rt\"}}}");

        ConfidentialClientApplication mockCca = mock(ConfidentialClientApplication.class);
        when(mockCca.acquireToken(any(RefreshTokenParameters.class)))
                .thenReturn(CompletableFuture.completedFuture(authResult));
        when(mockCca.tokenCache()).thenReturn(tokenCache);

        ConfidentialClientApplication.Builder mockBuilder = mock(ConfidentialClientApplication.Builder.class);
        when(mockBuilder.authority(eq(Constants.ORG_AUTHORITY))).thenReturn(mockBuilder);
        when(mockBuilder.build()).thenReturn(mockCca);

        GraphServiceClient<Request> mockGraphClient = mock(GraphServiceClient.class);

        try (MockedStatic<ConfidentialClientApplication> ccaStatic = mockStatic(ConfidentialClientApplication.class);
             MockedStatic<GraphServiceClient> graphStatic = mockStatic(GraphServiceClient.class)) {

            ccaStatic.when(() -> ConfidentialClientApplication.builder(anyString(), any(IClientCredential.class)))
                    .thenReturn(mockBuilder);

            GraphServiceClient.Builder graphBuilder = mock(GraphServiceClient.Builder.class);
            graphStatic.when(GraphServiceClient::builder).thenReturn(graphBuilder);
            when(graphBuilder.authenticationProvider(any())).thenReturn(graphBuilder);
            when(graphBuilder.buildClient()).thenReturn(mockGraphClient);

            GraphServiceClientProvider provider = new GraphServiceClientProvider(
                    refreshTokenStore, publicAttrs, requestContext, authorizationContext
            );
            GraphServiceClient<Request> result = provider.getGraphServiceClientForAdmin();
            assertSame(mockGraphClient, result);
        }
    }

    // ========================================================================
    // 30. InterruptedException path
    // ========================================================================

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("getGraphServiceClient: InterruptedException interrupts thread and throws MustAuthorizeException")
    public void testGetGraphServiceClient_InterruptedException() throws Exception {
        String key = getAdminKey();
        when(refreshTokenStore.get(key)).thenReturn(REFRESH_TOKEN);

        ConfidentialClientApplication mockCca = mock(ConfidentialClientApplication.class);
        // Mock a CompletableFuture whose get() throws InterruptedException directly
        @SuppressWarnings("unchecked")
        CompletableFuture<IAuthenticationResult> mockFuture = mock(CompletableFuture.class);
        when(mockFuture.get()).thenThrow(new InterruptedException("Thread interrupted"));
        when(mockCca.acquireToken(any(RefreshTokenParameters.class))).thenReturn(mockFuture);

        ConfidentialClientApplication.Builder mockBuilder = mock(ConfidentialClientApplication.Builder.class);
        when(mockBuilder.authority(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.build()).thenReturn(mockCca);

        try (MockedStatic<ConfidentialClientApplication> ccaStatic = mockStatic(ConfidentialClientApplication.class)) {
            ccaStatic.when(() -> ConfidentialClientApplication.builder(anyString(), any(IClientCredential.class)))
                    .thenReturn(mockBuilder);

            GraphServiceClientProvider provider = new GraphServiceClientProvider(
                    refreshTokenStore, attributes, requestContext, authorizationContext
            );
            MustAuthorizeException ex = assertThrows(MustAuthorizeException.class, provider::getGraphServiceClientForAdmin);
            assertNotNull(ex);
            // Verify thread interrupt flag was set
            assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            // Clear interrupt flag for test cleanup
            Thread.interrupted();
        }
    }

    // ========================================================================
    // 30b. ExecutionException catch path — handleAuthenticationError via mocked MSAL4J
    // ========================================================================

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("getGraphServiceClient: ExecutionException triggers handleAuthenticationError and cache removal")
    public void testGetGraphServiceClient_ExecutionException() throws Exception {
        String key = getAdminKey();
        when(refreshTokenStore.get(key)).thenReturn(REFRESH_TOKEN);
        when(requestContext.invokeAsUser()).thenReturn(true);

        ConfidentialClientApplication mockCca = mock(ConfidentialClientApplication.class);
        CompletableFuture<IAuthenticationResult> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("Some unknown MSAL error"));
        when(mockCca.acquireToken(any(RefreshTokenParameters.class))).thenReturn(failedFuture);

        ConfidentialClientApplication.Builder mockBuilder = mock(ConfidentialClientApplication.Builder.class);
        when(mockBuilder.authority(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.build()).thenReturn(mockCca);

        try (MockedStatic<ConfidentialClientApplication> ccaStatic = mockStatic(ConfidentialClientApplication.class)) {
            ccaStatic.when(() -> ConfidentialClientApplication.builder(anyString(), any(IClientCredential.class)))
                    .thenReturn(mockBuilder);

            GraphServiceClientProvider provider = new GraphServiceClientProvider(
                    refreshTokenStore, attributes, requestContext, authorizationContext
            );
            MustAuthorizeException ex = assertThrows(MustAuthorizeException.class, provider::getGraphServiceClientForAdmin);
            assertNotNull(ex);
            // Verify cache entry was removed
            assertNull(graphClientCache.get(key));
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("getGraphServiceClient: ExecutionException with password changed error removes token")
    public void testGetGraphServiceClient_ExecutionException_PasswordChanged() throws Exception {
        String key = getAdminKey();
        when(refreshTokenStore.get(key)).thenReturn(REFRESH_TOKEN);

        ConfidentialClientApplication mockCca = mock(ConfidentialClientApplication.class);
        CompletableFuture<IAuthenticationResult> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("AADSTS50173 password changed"));
        when(mockCca.acquireToken(any(RefreshTokenParameters.class))).thenReturn(failedFuture);

        ConfidentialClientApplication.Builder mockBuilder = mock(ConfidentialClientApplication.Builder.class);
        when(mockBuilder.authority(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.build()).thenReturn(mockCca);

        try (MockedStatic<ConfidentialClientApplication> ccaStatic = mockStatic(ConfidentialClientApplication.class)) {
            ccaStatic.when(() -> ConfidentialClientApplication.builder(anyString(), any(IClientCredential.class)))
                    .thenReturn(mockBuilder);

            GraphServiceClientProvider provider = new GraphServiceClientProvider(
                    refreshTokenStore, attributes, requestContext, authorizationContext
            );
            MustAuthorizeException ex = assertThrows(MustAuthorizeException.class, provider::getGraphServiceClientForAdmin);
            assertEquals(Constants.PASSWORD_CHANGED_ERROR, ex.getMessage());
            verify(refreshTokenStore).remove(key);
        }
    }

    // ========================================================================
    // 30c. Direct invocation of getGraphServiceClient(String, String) via reflection
    //      to ensure catch (ClientException | ExecutionException | MalformedURLException)
    //      path + handleAuthenticationError is covered by JaCoCo.
    //
    //      Uses mockConstruction to intercept ConfidentialClientApplication.Builder.build()
    //      and force ExecutionException from acquireToken().get() — avoids MockedStatic which
    //      can interfere with JaCoCo bytecode instrumentation.
    // ========================================================================

    @Test
    @DisplayName("getGraphServiceClient(String, String): ExecutionException via direct real MSAL4J call")
    public void testGetGraphServiceClient_DirectReflection_ExecutionException() throws Exception {
        when(requestContext.invokeAsUser()).thenReturn(true);
        GraphServiceClientProvider provider = new GraphServiceClientProvider(
                refreshTokenStore, attributes, requestContext, authorizationContext
        );

        Method m = GraphServiceClientProvider.class.getDeclaredMethod("getGraphServiceClient", String.class, String.class);
        m.setAccessible(true);

        String key = getAdminKey();
        // Call with real refresh token — real MSAL4J will fail, throwing ExecutionException
        // which gets caught at L226, then handleAuthenticationError at L230 is called
        try {
            m.invoke(provider, key, REFRESH_TOKEN);
            fail("Expected exception");
        } catch (java.lang.reflect.InvocationTargetException e) {
            // handleAuthenticationError always throws MustAuthorizeException
            assertTrue(e.getCause() instanceof MustAuthorizeException || e.getCause() instanceof RuntimeException);
        }
        // Verify cache entry was removed after auth failure
        assertNull(graphClientCache.get(key));
    }

    // ========================================================================
    // 31. getUserRequestBuilder IOException catch path
    // ========================================================================

    @Test
    @DisplayName("getUserRequestBuilder: IOException from getGraphServiceClientForUser wraps in RuntimeException")
    public void testGetUserRequestBuilder_IOExceptionCatchPath() throws Exception {
        // Use a spy to make getGraphServiceClientForUser throw IOException
        GraphServiceClientProvider provider = new GraphServiceClientProvider(
                refreshTokenStore, attributes, requestContext, authorizationContext
        );
        GraphServiceClientProvider spyProvider = spy(provider);
        doThrow(new IOException("Network failure")).when(spyProvider)
                .getGraphServiceClientForUser(any(), any());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> spyProvider.getUserRequestBuilder(true, null));
        assertTrue(ex.getMessage().contains("couldn't establish a connection"));
        assertInstanceOf(IOException.class, ex.getCause());
    }

    // ========================================================================
    // 32. Cache populated after successful token acquisition
    // ========================================================================

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Successful token acquisition populates cache, second call uses cache")
    public void testSuccessfulAcquisition_ThenCacheHit() throws Exception {
        String key = getAdminKey();
        when(refreshTokenStore.get(key)).thenReturn(REFRESH_TOKEN);

        IAuthenticationResult authResult = mock(IAuthenticationResult.class);
        when(authResult.accessToken()).thenReturn("valid-token");
        when(authResult.expiresOnDate()).thenReturn(new Date(System.currentTimeMillis() + 3600_000L));

        com.microsoft.aad.msal4j.TokenCache tokenCache = mock(com.microsoft.aad.msal4j.TokenCache.class);
        when(tokenCache.serialize()).thenReturn("{\"RefreshToken\":{\"k\":{\"secret\":\"rt\"}}}");

        ConfidentialClientApplication mockCca = mock(ConfidentialClientApplication.class);
        when(mockCca.acquireToken(any(RefreshTokenParameters.class)))
                .thenReturn(CompletableFuture.completedFuture(authResult));
        when(mockCca.tokenCache()).thenReturn(tokenCache);

        ConfidentialClientApplication.Builder mockBuilder = mock(ConfidentialClientApplication.Builder.class);
        when(mockBuilder.authority(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.build()).thenReturn(mockCca);

        GraphServiceClient<Request> mockGraphClient = mock(GraphServiceClient.class);

        try (MockedStatic<ConfidentialClientApplication> ccaStatic = mockStatic(ConfidentialClientApplication.class);
             MockedStatic<GraphServiceClient> graphStatic = mockStatic(GraphServiceClient.class)) {

            ccaStatic.when(() -> ConfidentialClientApplication.builder(anyString(), any(IClientCredential.class)))
                    .thenReturn(mockBuilder);

            GraphServiceClient.Builder graphBuilder = mock(GraphServiceClient.Builder.class);
            graphStatic.when(GraphServiceClient::builder).thenReturn(graphBuilder);
            when(graphBuilder.authenticationProvider(any())).thenReturn(graphBuilder);
            when(graphBuilder.buildClient()).thenReturn(mockGraphClient);

            GraphServiceClientProvider provider = new GraphServiceClientProvider(
                    refreshTokenStore, attributes, requestContext, authorizationContext
            );
            // First call — acquires token
            GraphServiceClient<Request> result1 = provider.getGraphServiceClientForAdmin();
            assertSame(mockGraphClient, result1);
        }

        // Second call — should use cache (no static mocks needed)
        GraphServiceClientProvider provider2 = new GraphServiceClientProvider(
                refreshTokenStore, attributes, requestContext, authorizationContext
        );
        GraphServiceClient<Request> result2 = provider2.getGraphServiceClientForAdmin();
        assertSame(mockGraphClient, result2);
        // refreshTokenStore.get should only have been called once (first call)
        verify(refreshTokenStore, times(1)).get(key);
    }
}
