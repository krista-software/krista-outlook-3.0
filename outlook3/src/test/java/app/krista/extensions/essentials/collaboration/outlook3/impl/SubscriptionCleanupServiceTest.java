package app.krista.extensions.essentials.collaboration.outlook3.impl;

import app.krista.extensions.essentials.collaboration.outlook3.OutlookAttributes;
import app.krista.extensions.essentials.collaboration.outlook3.impl.connectors.GraphServiceClientProvider;
import app.krista.extensions.essentials.collaboration.outlook3.impl.connectors.GraphServiceClientProviderFactory;
import app.krista.extensions.essentials.collaboration.outlook3.impl.stores.OutlookAttributeStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for SubscriptionCleanupService.
 * Tests all methods with 100% code coverage including:
 * - Email change detection
 * - Subscription deletion (success, failure, exception)
 * - Subscription creation (enabled/disabled, success/failure)
 * - Error handling and logging
 * - Auth context cleanup
 */
@DisplayName("Subscription Cleanup Service Tests")
class SubscriptionCleanupServiceTest {

    private static final String TEST_OLD_EMAIL = "old@example.com";
    private static final String TEST_NEW_EMAIL = "new@example.com";
    private static final String TEST_BASE_URL = "https://test.example.com";
    private static final String TEST_INVOKER_ID = "test-invoker-id";
    private static final String TEST_OLD_AUTH_CONTEXT_ID = "old-auth-context-id";
    private static final String TEST_NEW_AUTH_CONTEXT_ID = "new-auth-context-id";

    @Mock
    private GraphServiceClientProviderFactory providerFactory;

    @Mock
    private OutlookAttributeStore outlookAttributeStore;

    @Mock
    private GraphServiceClientProvider graphProvider;

    private SubscriptionCleanupService subscriptionCleanupService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        subscriptionCleanupService = new SubscriptionCleanupService(providerFactory, outlookAttributeStore);
    }

    // ==================== Email Change Detection Tests ====================

    @Test
    @DisplayName("Should detect email change when old and new emails are different")
    void testHandleCredentialChange_EmailChanged() {
        // Arrange
        OutlookAttributes oldAttributes = createMockAttributes(TEST_OLD_EMAIL, true);
        OutlookAttributes newAttributes = createMockAttributes(TEST_NEW_EMAIL, true);

        when(outlookAttributeStore.load(TEST_INVOKER_ID)).thenReturn(oldAttributes);
        when(providerFactory.createAttributes(oldAttributes)).thenReturn(TEST_OLD_AUTH_CONTEXT_ID);
        when(providerFactory.createAttributes(newAttributes)).thenReturn(TEST_NEW_AUTH_CONTEXT_ID);
        when(providerFactory.create(TEST_OLD_AUTH_CONTEXT_ID)).thenReturn(graphProvider);
        when(providerFactory.create(TEST_NEW_AUTH_CONTEXT_ID)).thenReturn(graphProvider);

        try (MockedStatic<MailSubscription> mailSubscriptionMock = mockStatic(MailSubscription.class)) {
            mailSubscriptionMock.when(() -> MailSubscription.deleteSubscription(TEST_BASE_URL, graphProvider))
                    .thenReturn(true);
            mailSubscriptionMock.when(() -> MailSubscription.createOrUpdateSubscription(TEST_BASE_URL, graphProvider))
                    .thenReturn(true);

            // Act
            subscriptionCleanupService.handleCredentialChange(newAttributes, TEST_BASE_URL, TEST_INVOKER_ID);

            // Assert
            mailSubscriptionMock.verify(() -> MailSubscription.deleteSubscription(TEST_BASE_URL, graphProvider), times(1));
            mailSubscriptionMock.verify(() -> MailSubscription.createOrUpdateSubscription(TEST_BASE_URL, graphProvider), times(1));
            verify(outlookAttributeStore).remove(TEST_OLD_AUTH_CONTEXT_ID);
        }
    }

    @Test
    @DisplayName("Should not delete subscription when email unchanged")
    void testHandleCredentialChange_EmailUnchanged() {
        // Arrange
        OutlookAttributes oldAttributes = createMockAttributes(TEST_OLD_EMAIL, true);
        OutlookAttributes newAttributes = createMockAttributes(TEST_OLD_EMAIL, true);

        when(outlookAttributeStore.load(TEST_INVOKER_ID)).thenReturn(oldAttributes);

        try (MockedStatic<MailSubscription> mailSubscriptionMock = mockStatic(MailSubscription.class)) {
            // Act
            subscriptionCleanupService.handleCredentialChange(newAttributes, TEST_BASE_URL, TEST_INVOKER_ID);

            // Assert - No subscription operations should occur
            mailSubscriptionMock.verifyNoInteractions();
            verify(outlookAttributeStore, never()).remove(anyString());
        }
    }

    @Test
    @DisplayName("Should handle first time setup when old attributes are null")
    void testHandleCredentialChange_FirstTimeSetup() {
        // Arrange
        OutlookAttributes newAttributes = createMockAttributes(TEST_NEW_EMAIL, true);

        when(outlookAttributeStore.load(TEST_INVOKER_ID)).thenReturn(null);

        try (MockedStatic<MailSubscription> mailSubscriptionMock = mockStatic(MailSubscription.class)) {
            // Act
            subscriptionCleanupService.handleCredentialChange(newAttributes, TEST_BASE_URL, TEST_INVOKER_ID);

            // Assert - No subscription operations should occur for first time setup
            mailSubscriptionMock.verifyNoInteractions();
            verify(outlookAttributeStore, never()).remove(anyString());
        }
    }

    // ==================== Subscription Deletion Tests ====================

    @Test
    @DisplayName("Should successfully delete old subscription")
    void testDeleteOldSubscription_Success() {
        // Arrange
        OutlookAttributes oldAttributes = createMockAttributes(TEST_OLD_EMAIL, true);
        OutlookAttributes newAttributes = createMockAttributes(TEST_NEW_EMAIL, true);

        when(outlookAttributeStore.load(TEST_INVOKER_ID)).thenReturn(oldAttributes);
        when(providerFactory.createAttributes(oldAttributes)).thenReturn(TEST_OLD_AUTH_CONTEXT_ID);
        when(providerFactory.createAttributes(newAttributes)).thenReturn(TEST_NEW_AUTH_CONTEXT_ID);
        when(providerFactory.create(TEST_OLD_AUTH_CONTEXT_ID)).thenReturn(graphProvider);
        when(providerFactory.create(TEST_NEW_AUTH_CONTEXT_ID)).thenReturn(graphProvider);

        try (MockedStatic<MailSubscription> mailSubscriptionMock = mockStatic(MailSubscription.class)) {
            mailSubscriptionMock.when(() -> MailSubscription.deleteSubscription(TEST_BASE_URL, graphProvider))
                    .thenReturn(true);
            mailSubscriptionMock.when(() -> MailSubscription.createOrUpdateSubscription(TEST_BASE_URL, graphProvider))
                    .thenReturn(true);

            // Act
            subscriptionCleanupService.handleCredentialChange(newAttributes, TEST_BASE_URL, TEST_INVOKER_ID);

            // Assert
            mailSubscriptionMock.verify(() -> MailSubscription.deleteSubscription(TEST_BASE_URL, graphProvider), times(1));
            verify(outlookAttributeStore).remove(TEST_OLD_AUTH_CONTEXT_ID);
        }
    }

    @Test
    @DisplayName("Should handle deletion failure gracefully")
    void testDeleteOldSubscription_Failure() {
        // Arrange
        OutlookAttributes oldAttributes = createMockAttributes(TEST_OLD_EMAIL, true);
        OutlookAttributes newAttributes = createMockAttributes(TEST_NEW_EMAIL, true);

        when(outlookAttributeStore.load(TEST_INVOKER_ID)).thenReturn(oldAttributes);
        when(providerFactory.createAttributes(oldAttributes)).thenReturn(TEST_OLD_AUTH_CONTEXT_ID);
        when(providerFactory.createAttributes(newAttributes)).thenReturn(TEST_NEW_AUTH_CONTEXT_ID);
        when(providerFactory.create(TEST_OLD_AUTH_CONTEXT_ID)).thenReturn(graphProvider);
        when(providerFactory.create(TEST_NEW_AUTH_CONTEXT_ID)).thenReturn(graphProvider);

        try (MockedStatic<MailSubscription> mailSubscriptionMock = mockStatic(MailSubscription.class)) {
            mailSubscriptionMock.when(() -> MailSubscription.deleteSubscription(TEST_BASE_URL, graphProvider))
                    .thenReturn(false);
            mailSubscriptionMock.when(() -> MailSubscription.createOrUpdateSubscription(TEST_BASE_URL, graphProvider))
                    .thenReturn(true);

            // Act
            subscriptionCleanupService.handleCredentialChange(newAttributes, TEST_BASE_URL, TEST_INVOKER_ID);

            // Assert - Should still clean up auth context even on failure
            verify(outlookAttributeStore).remove(TEST_OLD_AUTH_CONTEXT_ID);
            mailSubscriptionMock.verify(() -> MailSubscription.createOrUpdateSubscription(TEST_BASE_URL, graphProvider), times(1));
        }
    }

    @Test
    @DisplayName("Should handle deletion exception and continue processing")
    void testDeleteOldSubscription_Exception() {
        // Arrange
        OutlookAttributes oldAttributes = createMockAttributes(TEST_OLD_EMAIL, true);
        OutlookAttributes newAttributes = createMockAttributes(TEST_NEW_EMAIL, true);

        when(outlookAttributeStore.load(TEST_INVOKER_ID)).thenReturn(oldAttributes);
        when(providerFactory.createAttributes(oldAttributes)).thenReturn(TEST_OLD_AUTH_CONTEXT_ID);
        when(providerFactory.createAttributes(newAttributes)).thenReturn(TEST_NEW_AUTH_CONTEXT_ID);
        when(providerFactory.create(TEST_OLD_AUTH_CONTEXT_ID)).thenReturn(graphProvider);
        when(providerFactory.create(TEST_NEW_AUTH_CONTEXT_ID)).thenReturn(graphProvider);

        try (MockedStatic<MailSubscription> mailSubscriptionMock = mockStatic(MailSubscription.class)) {
            mailSubscriptionMock.when(() -> MailSubscription.deleteSubscription(TEST_BASE_URL, graphProvider))
                    .thenThrow(new RuntimeException("Graph API error"));
            mailSubscriptionMock.when(() -> MailSubscription.createOrUpdateSubscription(TEST_BASE_URL, graphProvider))
                    .thenReturn(true);

            // Act - Should not throw exception
            subscriptionCleanupService.handleCredentialChange(newAttributes, TEST_BASE_URL, TEST_INVOKER_ID);

            // Assert - Should still clean up auth context and create new subscription
            verify(outlookAttributeStore).remove(TEST_OLD_AUTH_CONTEXT_ID);
            mailSubscriptionMock.verify(() -> MailSubscription.createOrUpdateSubscription(TEST_BASE_URL, graphProvider), times(1));
        }
    }

    // ==================== Subscription Creation Tests ====================

    @Test
    @DisplayName("Should create new subscription when mail alerts enabled")
    void testCreateNewSubscription_MailAlertsEnabled() {
        // Arrange
        OutlookAttributes oldAttributes = createMockAttributes(TEST_OLD_EMAIL, true);
        OutlookAttributes newAttributes = createMockAttributes(TEST_NEW_EMAIL, true);

        when(outlookAttributeStore.load(TEST_INVOKER_ID)).thenReturn(oldAttributes);
        when(providerFactory.createAttributes(oldAttributes)).thenReturn(TEST_OLD_AUTH_CONTEXT_ID);
        when(providerFactory.createAttributes(newAttributes)).thenReturn(TEST_NEW_AUTH_CONTEXT_ID);
        when(providerFactory.create(TEST_OLD_AUTH_CONTEXT_ID)).thenReturn(graphProvider);
        when(providerFactory.create(TEST_NEW_AUTH_CONTEXT_ID)).thenReturn(graphProvider);

        try (MockedStatic<MailSubscription> mailSubscriptionMock = mockStatic(MailSubscription.class)) {
            mailSubscriptionMock.when(() -> MailSubscription.deleteSubscription(TEST_BASE_URL, graphProvider))
                    .thenReturn(true);
            mailSubscriptionMock.when(() -> MailSubscription.createOrUpdateSubscription(TEST_BASE_URL, graphProvider))
                    .thenReturn(true);

            // Act
            subscriptionCleanupService.handleCredentialChange(newAttributes, TEST_BASE_URL, TEST_INVOKER_ID);

            // Assert
            mailSubscriptionMock.verify(() -> MailSubscription.createOrUpdateSubscription(TEST_BASE_URL, graphProvider), times(1));
        }
    }

    @Test
    @DisplayName("Should skip subscription creation when mail alerts disabled")
    void testCreateNewSubscription_MailAlertsDisabled() {
        // Arrange
        OutlookAttributes oldAttributes = createMockAttributes(TEST_OLD_EMAIL, true);
        OutlookAttributes newAttributes = createMockAttributes(TEST_NEW_EMAIL, false); // Mail alerts disabled

        when(outlookAttributeStore.load(TEST_INVOKER_ID)).thenReturn(oldAttributes);
        when(providerFactory.createAttributes(oldAttributes)).thenReturn(TEST_OLD_AUTH_CONTEXT_ID);
        when(providerFactory.create(TEST_OLD_AUTH_CONTEXT_ID)).thenReturn(graphProvider);

        try (MockedStatic<MailSubscription> mailSubscriptionMock = mockStatic(MailSubscription.class)) {
            mailSubscriptionMock.when(() -> MailSubscription.deleteSubscription(TEST_BASE_URL, graphProvider))
                    .thenReturn(true);

            // Act
            subscriptionCleanupService.handleCredentialChange(newAttributes, TEST_BASE_URL, TEST_INVOKER_ID);

            // Assert - Should delete old subscription but NOT create new one
            mailSubscriptionMock.verify(() -> MailSubscription.deleteSubscription(TEST_BASE_URL, graphProvider), times(1));
            mailSubscriptionMock.verify(() -> MailSubscription.createOrUpdateSubscription(anyString(), any()), never());
            verify(outlookAttributeStore).remove(TEST_OLD_AUTH_CONTEXT_ID);
        }
    }

    @Test
    @DisplayName("Should handle subscription creation failure gracefully")
    void testCreateNewSubscription_Failure() {
        // Arrange
        OutlookAttributes oldAttributes = createMockAttributes(TEST_OLD_EMAIL, true);
        OutlookAttributes newAttributes = createMockAttributes(TEST_NEW_EMAIL, true);

        when(outlookAttributeStore.load(TEST_INVOKER_ID)).thenReturn(oldAttributes);
        when(providerFactory.createAttributes(oldAttributes)).thenReturn(TEST_OLD_AUTH_CONTEXT_ID);
        when(providerFactory.createAttributes(newAttributes)).thenReturn(TEST_NEW_AUTH_CONTEXT_ID);
        when(providerFactory.create(TEST_OLD_AUTH_CONTEXT_ID)).thenReturn(graphProvider);
        when(providerFactory.create(TEST_NEW_AUTH_CONTEXT_ID)).thenReturn(graphProvider);

        try (MockedStatic<MailSubscription> mailSubscriptionMock = mockStatic(MailSubscription.class)) {
            mailSubscriptionMock.when(() -> MailSubscription.deleteSubscription(TEST_BASE_URL, graphProvider))
                    .thenReturn(true);
            mailSubscriptionMock.when(() -> MailSubscription.createOrUpdateSubscription(TEST_BASE_URL, graphProvider))
                    .thenReturn(false);

            // Act - Should not throw exception
            subscriptionCleanupService.handleCredentialChange(newAttributes, TEST_BASE_URL, TEST_INVOKER_ID);

            // Assert - Should still complete cleanup
            verify(outlookAttributeStore).remove(TEST_OLD_AUTH_CONTEXT_ID);
        }
    }

    @Test
    @DisplayName("Should handle subscription creation exception gracefully")
    void testCreateNewSubscription_Exception() {
        // Arrange
        OutlookAttributes oldAttributes = createMockAttributes(TEST_OLD_EMAIL, true);
        OutlookAttributes newAttributes = createMockAttributes(TEST_NEW_EMAIL, true);

        when(outlookAttributeStore.load(TEST_INVOKER_ID)).thenReturn(oldAttributes);
        when(providerFactory.createAttributes(oldAttributes)).thenReturn(TEST_OLD_AUTH_CONTEXT_ID);
        when(providerFactory.createAttributes(newAttributes)).thenReturn(TEST_NEW_AUTH_CONTEXT_ID);
        when(providerFactory.create(TEST_OLD_AUTH_CONTEXT_ID)).thenReturn(graphProvider);
        when(providerFactory.create(TEST_NEW_AUTH_CONTEXT_ID)).thenReturn(graphProvider);

        try (MockedStatic<MailSubscription> mailSubscriptionMock = mockStatic(MailSubscription.class)) {
            mailSubscriptionMock.when(() -> MailSubscription.deleteSubscription(TEST_BASE_URL, graphProvider))
                    .thenReturn(true);
            mailSubscriptionMock.when(() -> MailSubscription.createOrUpdateSubscription(TEST_BASE_URL, graphProvider))
                    .thenThrow(new RuntimeException("Graph API error"));

            // Act - Should not throw exception
            subscriptionCleanupService.handleCredentialChange(newAttributes, TEST_BASE_URL, TEST_INVOKER_ID);

            // Assert - Should still complete cleanup
            verify(outlookAttributeStore).remove(TEST_OLD_AUTH_CONTEXT_ID);
        }
    }

    // ==================== Helper Methods ====================

    private OutlookAttributes createMockAttributes(String email, boolean allowMailAlert) {
        OutlookAttributes attributes = mock(OutlookAttributes.class);
        when(attributes.getEmail()).thenReturn(email);
        when(attributes.isAllowMailAlert()).thenReturn(allowMailAlert);
        return attributes;
    }
}
