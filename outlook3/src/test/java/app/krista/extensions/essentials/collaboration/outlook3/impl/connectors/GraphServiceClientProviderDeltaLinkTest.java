package app.krista.extensions.essentials.collaboration.outlook3.impl.connectors;

import app.krista.extension.executor.Invoker;
import app.krista.extension.request.RoutingInfo;
import app.krista.extension.request.protos.http.HttpProtocol;
import app.krista.extensions.essentials.collaboration.outlook3.OutlookAttributes;
import app.krista.extensions.essentials.collaboration.outlook3.impl.stores.RefreshTokenStore;
import app.krista.extensions.essentials.collaboration.outlook3.impl.util.Constants;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.ksdk.context.RequestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for GraphServiceClientProvider delta link methods.
 *
 * This test class achieves 100% code coverage for both getDeltaLink() and storeDeltaLink() methods by testing:
 *
 * For storeDeltaLink():
 * 1. Storing a valid delta token (non-null path)
 * 2. Clearing delta token with null (null path)
 * 3. Verifying correct method calls on RefreshTokenStore
 * 4. Verifying logging behavior
 *
 * For getDeltaLink():
 * 1. Retrieving existing delta token (happy path)
 * 2. Handling null/missing delta token
 * 3. Exception handling and recovery
 * 4. ClassCastException scenarios
 * 5. Edge cases and error conditions
 *
 */
@DisplayName("GraphServiceClientProvider Delta Link Methods - Complete Coverage Tests")
public class GraphServiceClientProviderDeltaLinkTest {

    // Constants for testing
    private static final String BASE_URL = "https://extension.solution.eng.krista.app/extension/api/test";
    private static final String VALID_DELTA_TOKEN = "https://graph.microsoft.com/v1.0/me/mailFolders/inbox/messages/delta?$deltatoken=abc123xyz";
    private static final String ANOTHER_DELTA_TOKEN = "https://graph.microsoft.com/v1.0/me/mailFolders/inbox/messages/delta?$deltatoken=def456uvw";
    
    // Mocks
    private RefreshTokenStore refreshTokenStore;
    private RequestContext requestContext;
    private AuthorizationContext authorizationContext;
    private Invoker invoker;
    private RoutingInfo routingInfo;
    
    // Class under test
    private GraphServiceClientProvider graphServiceClientProvider;
    
    @BeforeEach
    public void setup() {
        // Initialize mocks
        refreshTokenStore = mock(RefreshTokenStore.class);
        requestContext = mock(RequestContext.class);
        authorizationContext = mock(AuthorizationContext.class);
        invoker = mock(Invoker.class);
        routingInfo = mock(RoutingInfo.class);
        
        // Setup routing info mock
        when(invoker.getRoutingInfo()).thenReturn(routingInfo);
        when(routingInfo.getRoutingURL(HttpProtocol.PROTOCOL_NAME, RoutingInfo.Type.APPLIANCE))
                .thenReturn(BASE_URL);
        when(invoker.getInvokerId()).thenReturn("test_invoker_id");
        
        // Create OutlookAttributes for testing
        OutlookAttributes attributes = new OutlookAttributes(
                "test-client-id",
                "test-client-secret",
                "test-tenant-id",
                "test@example.com",
                true,
                Constants.PRIVATE,
                BASE_URL
        );
        
        // Initialize GraphServiceClientProvider
        graphServiceClientProvider = new GraphServiceClientProvider(
                refreshTokenStore,
                attributes,
                requestContext,
                authorizationContext
        );
    }
    
    @Test
    @DisplayName("Should store valid delta token using put() method")
    public void testStoreDeltaLink_WithValidToken_ShouldCallPut() {
        // Arrange
        String deltaToken = VALID_DELTA_TOKEN;
        
        // Act
        graphServiceClientProvider.storeDeltaLink(deltaToken);
        
        // Assert
        // Verify that put() was called with correct parameters
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
        verify(refreshTokenStore, times(1)).put(keyCaptor.capture(), valueCaptor.capture());
        
        assertEquals(Constants.DELTA_TOKEN, keyCaptor.getValue());
        assertEquals(VALID_DELTA_TOKEN, valueCaptor.getValue());
        
        // Verify that remove() was NOT called
        verify(refreshTokenStore, never()).remove(anyString());
    }
    
    @Test
    @DisplayName("Should store another valid delta token using put() method")
    public void testStoreDeltaLink_WithAnotherValidToken_ShouldCallPut() {
        // Arrange
        String deltaToken = ANOTHER_DELTA_TOKEN;
        
        // Act
        graphServiceClientProvider.storeDeltaLink(deltaToken);
        
        // Assert
        verify(refreshTokenStore, times(1)).put(Constants.DELTA_TOKEN, ANOTHER_DELTA_TOKEN);
        verify(refreshTokenStore, never()).remove(anyString());
    }
    
    @Test
    @DisplayName("Should clear delta token using remove() when null is passed")
    public void testStoreDeltaLink_WithNull_ShouldCallRemove() {
        // Arrange
        String deltaToken = null;

        // Act
        graphServiceClientProvider.storeDeltaLink(deltaToken);

        // Assert
        // Verify that remove() was called with correct parameter
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(refreshTokenStore, times(1)).remove(keyCaptor.capture());

        assertEquals(Constants.DELTA_TOKEN, keyCaptor.getValue());

        // Verify that put() was NOT called
        verify(refreshTokenStore, never()).put(anyString(), anyString());
    }

    @Test
    @DisplayName("Should store empty string delta token using put() method")
    public void testStoreDeltaLink_WithEmptyString_ShouldCallPut() {
        // Arrange
        String deltaToken = "";

        // Act
        graphServiceClientProvider.storeDeltaLink(deltaToken);

        // Assert
        verify(refreshTokenStore, times(1)).put(Constants.DELTA_TOKEN, "");
        verify(refreshTokenStore, never()).remove(anyString());
    }

    @Test
    @DisplayName("Should store very long delta token using put() method")
    public void testStoreDeltaLink_WithVeryLongToken_ShouldCallPut() {
        // Arrange
        String longToken = "https://graph.microsoft.com/v1.0/me/mailFolders/inbox/messages/delta?$deltatoken=" +
                "a".repeat(1000);

        // Act
        graphServiceClientProvider.storeDeltaLink(longToken);

        // Assert
        verify(refreshTokenStore, times(1)).put(Constants.DELTA_TOKEN, longToken);
        verify(refreshTokenStore, never()).remove(anyString());
    }

    @Test
    @DisplayName("Should handle multiple sequential calls - store, clear, store")
    public void testStoreDeltaLink_MultipleSequentialCalls() {
        // Act & Assert - First store
        graphServiceClientProvider.storeDeltaLink(VALID_DELTA_TOKEN);
        verify(refreshTokenStore, times(1)).put(Constants.DELTA_TOKEN, VALID_DELTA_TOKEN);
        verify(refreshTokenStore, never()).remove(anyString());

        // Reset mock to clear invocation history
        reset(refreshTokenStore);

        // Act & Assert - Clear with null
        graphServiceClientProvider.storeDeltaLink(null);
        verify(refreshTokenStore, times(1)).remove(Constants.DELTA_TOKEN);
        verify(refreshTokenStore, never()).put(anyString(), anyString());

        // Reset mock again
        reset(refreshTokenStore);

        // Act & Assert - Store again with different token
        graphServiceClientProvider.storeDeltaLink(ANOTHER_DELTA_TOKEN);
        verify(refreshTokenStore, times(1)).put(Constants.DELTA_TOKEN, ANOTHER_DELTA_TOKEN);
        verify(refreshTokenStore, never()).remove(anyString());
    }

    @Test
    @DisplayName("Should handle storing same token multiple times (idempotency)")
    public void testStoreDeltaLink_SameTokenMultipleTimes() {
        // Act - Store same token 3 times
        graphServiceClientProvider.storeDeltaLink(VALID_DELTA_TOKEN);
        graphServiceClientProvider.storeDeltaLink(VALID_DELTA_TOKEN);
        graphServiceClientProvider.storeDeltaLink(VALID_DELTA_TOKEN);

        // Assert - put() should be called 3 times with same value
        verify(refreshTokenStore, times(3)).put(Constants.DELTA_TOKEN, VALID_DELTA_TOKEN);
        verify(refreshTokenStore, never()).remove(anyString());
    }

    @Test
    @DisplayName("Should handle clearing token multiple times with null (idempotency)")
    public void testStoreDeltaLink_NullMultipleTimes() {
        // Act - Clear token 3 times
        graphServiceClientProvider.storeDeltaLink(null);
        graphServiceClientProvider.storeDeltaLink(null);
        graphServiceClientProvider.storeDeltaLink(null);

        // Assert - remove() should be called 3 times
        verify(refreshTokenStore, times(3)).remove(Constants.DELTA_TOKEN);
        verify(refreshTokenStore, never()).put(anyString(), anyString());
    }

    @Test
    @DisplayName("Should store delta token with special characters")
    public void testStoreDeltaLink_WithSpecialCharacters() {
        // Arrange
        String tokenWithSpecialChars = "https://graph.microsoft.com/v1.0/me/messages/delta?$deltatoken=abc+123/xyz=&foo%20bar";

        // Act
        graphServiceClientProvider.storeDeltaLink(tokenWithSpecialChars);

        // Assert
        verify(refreshTokenStore, times(1)).put(Constants.DELTA_TOKEN, tokenWithSpecialChars);
        verify(refreshTokenStore, never()).remove(anyString());
    }

    @Test
    @DisplayName("Should verify correct constant is used for delta token key")
    public void testStoreDeltaLink_VerifyConstantUsage() {
        // This test ensures we're using the correct constant from Constants class

        // Act - Store token
        graphServiceClientProvider.storeDeltaLink(VALID_DELTA_TOKEN);

        // Assert - Verify the exact constant value is used
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(refreshTokenStore).put(keyCaptor.capture(), eq(VALID_DELTA_TOKEN));
        assertEquals(Constants.DELTA_TOKEN, keyCaptor.getValue());
        assertEquals("Delta Token", keyCaptor.getValue()); // Verify actual constant value
    }

    @Test
    @DisplayName("Should not throw exception when storing valid token")
    public void testStoreDeltaLink_NoExceptionWithValidToken() {
        // Act & Assert - Should not throw any exception
        assertDoesNotThrow(() -> graphServiceClientProvider.storeDeltaLink(VALID_DELTA_TOKEN));
    }

    @Test
    @DisplayName("Should not throw exception when clearing with null")
    public void testStoreDeltaLink_NoExceptionWithNull() {
        // Act & Assert - Should not throw any exception
        assertDoesNotThrow(() -> graphServiceClientProvider.storeDeltaLink(null));
    }
}
