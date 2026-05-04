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
import com.microsoft.graph.requests.GraphServiceClient;
import okhttp3.Request;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test suite for access token caching functionality in GraphServiceClientProvider.
 *
 * Tests cover:
 * - CachedGraphClient inner class (isValid() logic with TOKEN_EXPIRY_BUFFER_MS)
 * - Cache hit: returns cached client without calling KeyValueStore
 * - Cache miss: fetches refresh token from KeyValueStore
 * - Cache expiry: expired entries trigger fresh token acquisition
 * - Cache invalidation: auth errors remove stale cache entries
 * - Static cache shared across provider instances
 */
@DisplayName("GraphServiceClientProvider - Access Token Caching Tests")
public class GraphServiceClientProviderCachingTest {

    private static final String CLIENT_ID = "test-client-id";
    private static final String CLIENT_SECRET = "test-client-secret";
    private static final String TENANT_ID = "test-tenant-id";
    private static final String EMAIL = "test@example.com";
    private static final String ACCOUNT_ID = "test-account-id";
    private static final String BASE_URL = "https://extension.solution.eng.krista.app/extension/api/test";
    private static final String REFRESH_TOKEN = "test-refresh-token";
    private static final long TOKEN_EXPIRY_BUFFER_MS = 5 * 60 * 1000L;

    private RefreshTokenStore refreshTokenStore;
    private RequestContext requestContext;
    private AuthorizationContext authorizationContext;
    private OutlookAttributes attributes;
    private GraphServiceClientProvider provider;

    private ConcurrentHashMap<String, Object> graphClientCache;

    @BeforeEach
    @SuppressWarnings("unchecked")
    public void setup() throws Exception {
        refreshTokenStore = mock(RefreshTokenStore.class);
        requestContext = mock(RequestContext.class);
        authorizationContext = mock(AuthorizationContext.class);
        when(requestContext.invokeAsUser()).thenReturn(true);

        attributes = new OutlookAttributes(
                CLIENT_ID, CLIENT_SECRET, TENANT_ID, EMAIL, true, Constants.PRIVATE, BASE_URL
        );

        provider = new GraphServiceClientProvider(
                refreshTokenStore, attributes, requestContext, authorizationContext
        );

        // Access the static GRAPH_CLIENT_CACHE via reflection
        Field cacheField = GraphServiceClientProvider.class.getDeclaredField("GRAPH_CLIENT_CACHE");
        cacheField.setAccessible(true);
        graphClientCache = (ConcurrentHashMap<String, Object>) cacheField.get(null);
    }

    @AfterEach
    public void tearDown() {
        // Clear the static cache after each test to prevent cross-test contamination
        graphClientCache.clear();
    }

    private String getAdminRefreshTokenKey() {
        return EMAIL + "_" + CLIENT_ID + "_" + Constants.PRIVATE;
    }

    private String getUserRefreshTokenKey(String accountId) {
        return accountId + "_" + CLIENT_ID + "_" + Constants.PRIVATE;
    }

    /**
     * Creates a CachedGraphClient instance via reflection (private inner class).
     */
    @SuppressWarnings("unchecked")
    private Object createCachedGraphClient(GraphServiceClient<Request> client, long expiresAtMillis) throws Exception {
        Class<?> cachedGraphClientClass = null;
        for (Class<?> cls : GraphServiceClientProvider.class.getDeclaredClasses()) {
            if (cls.getSimpleName().equals("CachedGraphClient")) {
                cachedGraphClientClass = cls;
                break;
            }
        }
        assertNotNull(cachedGraphClientClass, "CachedGraphClient inner class should exist");

        Constructor<?> constructor = cachedGraphClientClass.getDeclaredConstructor(GraphServiceClient.class, long.class);
        constructor.setAccessible(true);
        return constructor.newInstance(client, expiresAtMillis);
    }

    /**
     * Invokes isValid() on a CachedGraphClient instance via reflection.
     */
    private boolean callIsValid(Object cachedGraphClient) throws Exception {
        Method isValidMethod = cachedGraphClient.getClass().getDeclaredMethod("isValid");
        isValidMethod.setAccessible(true);
        return (boolean) isValidMethod.invoke(cachedGraphClient);
    }

    /**
     * Extracts the 'client' field from a CachedGraphClient via reflection.
     */
    @SuppressWarnings("unchecked")
    private GraphServiceClient<Request> extractClient(Object cachedGraphClient) throws Exception {
        Field clientField = cachedGraphClient.getClass().getDeclaredField("client");
        clientField.setAccessible(true);
        return (GraphServiceClient<Request>) clientField.get(cachedGraphClient);
    }

    // ========================================================================
    // CachedGraphClient.isValid() Tests
    // ========================================================================

    @Test
    @DisplayName("CachedGraphClient.isValid: Should return true when token is not expired and outside buffer")
    public void testIsValid_TokenNotExpired() throws Exception {
        GraphServiceClient<Request> mockClient = mock(GraphServiceClient.class);
        long expiresAt = System.currentTimeMillis() + 60 * 60 * 1000L; // 1 hour from now
        Object cached = createCachedGraphClient(mockClient, expiresAt);

        assertTrue(callIsValid(cached));
    }

    @Test
    @DisplayName("CachedGraphClient.isValid: Should return false when token is expired")
    public void testIsValid_TokenExpired() throws Exception {
        GraphServiceClient<Request> mockClient = mock(GraphServiceClient.class);
        long expiresAt = System.currentTimeMillis() - 10 * 60 * 1000L; // 10 minutes ago
        Object cached = createCachedGraphClient(mockClient, expiresAt);

        assertFalse(callIsValid(cached));
    }

    @Test
    @DisplayName("CachedGraphClient.isValid: Should return false when token is within 5-minute buffer")
    public void testIsValid_TokenWithinBuffer() throws Exception {
        GraphServiceClient<Request> mockClient = mock(GraphServiceClient.class);
        long expiresAt = System.currentTimeMillis() + 3 * 60 * 1000L; // 3 minutes from now
        Object cached = createCachedGraphClient(mockClient, expiresAt);

        assertFalse(callIsValid(cached));
    }

    @Test
    @DisplayName("CachedGraphClient.isValid: Should return true when token expires just outside the buffer")
    public void testIsValid_TokenJustOutsideBuffer() throws Exception {
        GraphServiceClient<Request> mockClient = mock(GraphServiceClient.class);
        long expiresAt = System.currentTimeMillis() + TOKEN_EXPIRY_BUFFER_MS + 2000L; // buffer + 2 seconds
        Object cached = createCachedGraphClient(mockClient, expiresAt);

        assertTrue(callIsValid(cached));
    }

    @Test
    @DisplayName("CachedGraphClient.isValid: Should return false when token expires exactly at buffer boundary")
    public void testIsValid_TokenExactlyAtBuffer() throws Exception {
        GraphServiceClient<Request> mockClient = mock(GraphServiceClient.class);
        // expiresAt - TOKEN_EXPIRY_BUFFER_MS == currentTimeMillis → not < → false
        long expiresAt = System.currentTimeMillis() + TOKEN_EXPIRY_BUFFER_MS;
        Object cached = createCachedGraphClient(mockClient, expiresAt);

        assertFalse(callIsValid(cached));
    }

    // ========================================================================
    // Cache Hit Tests
    // ========================================================================

    @Test
    @DisplayName("Cache hit: Should return cached client for admin without calling KeyValueStore")
    public void testCacheHit_Admin_NoKeyValueStoreCall() throws Exception {
        GraphServiceClient<Request> mockClient = mock(GraphServiceClient.class);
        String cacheKey = getAdminRefreshTokenKey();

        long expiresAt = System.currentTimeMillis() + 60 * 60 * 1000L;
        graphClientCache.put(cacheKey, createCachedGraphClient(mockClient, expiresAt));

        GraphServiceClient<Request> result = provider.getGraphServiceClientForAdmin();

        assertSame(mockClient, result);
        verify(refreshTokenStore, never()).get(anyString());
    }

    @Test
    @DisplayName("Cache hit: Should return cached client for user (useSetupEmail=true) without calling KeyValueStore")
    public void testCacheHit_UserSetupEmail_NoKeyValueStoreCall() throws Exception {
        GraphServiceClient<Request> mockClient = mock(GraphServiceClient.class);
        String cacheKey = getAdminRefreshTokenKey();

        long expiresAt = System.currentTimeMillis() + 60 * 60 * 1000L;
        graphClientCache.put(cacheKey, createCachedGraphClient(mockClient, expiresAt));

        GraphServiceClient<Request> result = provider.getGraphServiceClientForUser(true, null);

        assertSame(mockClient, result);
        verify(refreshTokenStore, never()).get(anyString());
    }

    @Test
    @DisplayName("Cache hit: Should return cached client for user (useSetupEmail=false) with accountID")
    public void testCacheHit_UserWithAccountID_NoKeyValueStoreCall() throws Exception {
        GraphServiceClient<Request> mockClient = mock(GraphServiceClient.class);
        String cacheKey = getUserRefreshTokenKey(ACCOUNT_ID);

        long expiresAt = System.currentTimeMillis() + 60 * 60 * 1000L;
        graphClientCache.put(cacheKey, createCachedGraphClient(mockClient, expiresAt));

        GraphServiceClient<Request> result = provider.getGraphServiceClientForUser(false, ACCOUNT_ID);

        assertSame(mockClient, result);
        verify(refreshTokenStore, never()).get(anyString());
    }

    // ========================================================================
    // Cache Miss Tests
    // ========================================================================

    @Test
    @DisplayName("Cache miss: Should call KeyValueStore when no cached entry exists")
    public void testCacheMiss_CallsKeyValueStore() {
        String cacheKey = getAdminRefreshTokenKey();
        graphClientCache.remove(cacheKey);
        when(refreshTokenStore.get(cacheKey)).thenReturn(null);

        assertThrows(MustAuthorizeException.class, () -> provider.getGraphServiceClientForAdmin());
        verify(refreshTokenStore, times(1)).get(cacheKey);
    }

    @Test
    @DisplayName("Cache miss: Should throw MustAuthorizeException when refresh token is null")
    public void testCacheMiss_NullRefreshToken_ThrowsMustAuthorizeException() {
        String cacheKey = getAdminRefreshTokenKey();
        when(refreshTokenStore.get(cacheKey)).thenReturn(null);

        MustAuthorizeException exception = assertThrows(
                MustAuthorizeException.class,
                () -> provider.getGraphServiceClientForAdmin()
        );

        assertNotNull(exception.getMessage());
    }

    // ========================================================================
    // Cache Expiry Tests
    // ========================================================================

    @Test
    @DisplayName("Cache expired: Should call KeyValueStore when cached entry has expired")
    public void testCacheExpired_CallsKeyValueStore() throws Exception {
        GraphServiceClient<Request> mockClient = mock(GraphServiceClient.class);
        String cacheKey = getAdminRefreshTokenKey();

        long expiredAt = System.currentTimeMillis() - 10 * 60 * 1000L;
        graphClientCache.put(cacheKey, createCachedGraphClient(mockClient, expiredAt));

        when(refreshTokenStore.get(cacheKey)).thenReturn(null);

        assertThrows(MustAuthorizeException.class, () -> provider.getGraphServiceClientForAdmin());
        verify(refreshTokenStore, times(1)).get(cacheKey);
    }

    @Test
    @DisplayName("Cache within buffer: Should call KeyValueStore when cached entry is within expiry buffer")
    public void testCacheWithinBuffer_CallsKeyValueStore() throws Exception {
        GraphServiceClient<Request> mockClient = mock(GraphServiceClient.class);
        String cacheKey = getAdminRefreshTokenKey();

        long expiresAt = System.currentTimeMillis() + 3 * 60 * 1000L; // 3 min (within 5 min buffer)
        graphClientCache.put(cacheKey, createCachedGraphClient(mockClient, expiresAt));

        when(refreshTokenStore.get(cacheKey)).thenReturn(null);

        assertThrows(MustAuthorizeException.class, () -> provider.getGraphServiceClientForAdmin());
        verify(refreshTokenStore, times(1)).get(cacheKey);
    }

    // ========================================================================
    // Cache Invalidation Tests
    // ========================================================================

    @Test
    @DisplayName("Cache invalidation: Stale cache entry should be removed after authentication failure")
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    public void testCacheInvalidation_OnAuthFailure() throws Exception {
        GraphServiceClient<Request> mockClient = mock(GraphServiceClient.class);
        String cacheKey = getAdminRefreshTokenKey();

        // Pre-populate cache with an expired entry
        long expiredAt = System.currentTimeMillis() - 10 * 60 * 1000L;
        graphClientCache.put(cacheKey, createCachedGraphClient(mockClient, expiredAt));

        // Return a refresh token so the code proceeds to MSAL4J token acquisition (which will fail)
        when(refreshTokenStore.get(cacheKey)).thenReturn(REFRESH_TOKEN);

        // The MSAL4J call will fail with fake credentials, causing cache invalidation
        try {
            provider.getGraphServiceClientForAdmin();
            fail("Expected exception to be thrown");
        } catch (Exception e) {
            // Expected — authentication will fail with fake credentials
        }

        // Verify the stale cache entry was removed after auth failure
        assertNull(graphClientCache.get(cacheKey),
                "Cache entry should be removed after authentication failure");
    }

    // ========================================================================
    // Multiple Calls Tests
    // ========================================================================

    @Test
    @DisplayName("Multiple calls: Should return same cached client without hitting KeyValueStore")
    public void testMultipleCalls_UsesCachedClient() throws Exception {
        GraphServiceClient<Request> mockClient = mock(GraphServiceClient.class);
        String cacheKey = getAdminRefreshTokenKey();

        long expiresAt = System.currentTimeMillis() + 60 * 60 * 1000L;
        graphClientCache.put(cacheKey, createCachedGraphClient(mockClient, expiresAt));

        GraphServiceClient<Request> result1 = provider.getGraphServiceClientForAdmin();
        GraphServiceClient<Request> result2 = provider.getGraphServiceClientForAdmin();
        GraphServiceClient<Request> result3 = provider.getGraphServiceClientForAdmin();

        assertSame(mockClient, result1);
        assertSame(mockClient, result2);
        assertSame(mockClient, result3);
        verify(refreshTokenStore, never()).get(anyString());
    }

    @Test
    @DisplayName("Static cache: Should be shared across different provider instances")
    public void testStaticCache_SharedAcrossInstances() throws Exception {
        GraphServiceClient<Request> mockClient = mock(GraphServiceClient.class);
        String cacheKey = getAdminRefreshTokenKey();

        long expiresAt = System.currentTimeMillis() + 60 * 60 * 1000L;
        graphClientCache.put(cacheKey, createCachedGraphClient(mockClient, expiresAt));

        // Create a second provider instance with same attributes
        GraphServiceClientProvider provider2 = new GraphServiceClientProvider(
                refreshTokenStore, attributes, requestContext, authorizationContext
        );

        GraphServiceClient<Request> result1 = provider.getGraphServiceClientForAdmin();
        GraphServiceClient<Request> result2 = provider2.getGraphServiceClientForAdmin();

        assertSame(mockClient, result1);
        assertSame(mockClient, result2);
        verify(refreshTokenStore, never()).get(anyString());
    }

    @Test
    @DisplayName("Different keys: Different users should have separate cache entries")
    public void testDifferentKeys_SeparateCacheEntries() throws Exception {
        GraphServiceClient<Request> adminClient = mock(GraphServiceClient.class);
        GraphServiceClient<Request> userClient = mock(GraphServiceClient.class);
        String adminKey = getAdminRefreshTokenKey();
        String userKey = getUserRefreshTokenKey(ACCOUNT_ID);

        long expiresAt = System.currentTimeMillis() + 60 * 60 * 1000L;
        graphClientCache.put(adminKey, createCachedGraphClient(adminClient, expiresAt));
        graphClientCache.put(userKey, createCachedGraphClient(userClient, expiresAt));

        GraphServiceClient<Request> adminResult = provider.getGraphServiceClientForAdmin();
        GraphServiceClient<Request> userResult = provider.getGraphServiceClientForUser(false, ACCOUNT_ID);

        assertSame(adminClient, adminResult);
        assertSame(userClient, userResult);
        assertNotSame(adminResult, userResult);
        verify(refreshTokenStore, never()).get(anyString());
    }

    // ========================================================================
    // TOKEN_EXPIRY_BUFFER_MS Constant Tests
    // ========================================================================

    @Test
    @DisplayName("TOKEN_EXPIRY_BUFFER_MS: Should be 5 minutes (300000 ms)")
    public void testTokenExpiryBufferValue() throws Exception {
        Field bufferField = GraphServiceClientProvider.class.getDeclaredField("TOKEN_EXPIRY_BUFFER_MS");
        bufferField.setAccessible(true);
        long bufferValue = (long) bufferField.get(null);

        assertEquals(5 * 60 * 1000L, bufferValue, "TOKEN_EXPIRY_BUFFER_MS should be 5 minutes");
    }

    // ========================================================================
    // ConcurrentHashMap Thread Safety Tests
    // ========================================================================

    @Test
    @DisplayName("Cache: Should use ConcurrentHashMap for thread safety")
    public void testCacheIsConcurrentHashMap() throws Exception {
        Field cacheField = GraphServiceClientProvider.class.getDeclaredField("GRAPH_CLIENT_CACHE");
        cacheField.setAccessible(true);
        Object cache = cacheField.get(null);

        assertInstanceOf(ConcurrentHashMap.class, cache, "Cache should be a ConcurrentHashMap");
    }

    @Test
    @DisplayName("Cache: Should be static (shared across all instances)")
    public void testCacheIsStatic() throws Exception {
        Field cacheField = GraphServiceClientProvider.class.getDeclaredField("GRAPH_CLIENT_CACHE");
        assertTrue(java.lang.reflect.Modifier.isStatic(cacheField.getModifiers()),
                "GRAPH_CLIENT_CACHE should be static");
    }
}
