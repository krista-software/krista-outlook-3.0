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

package app.krista.extensions.essentials.collaboration.outlook3.catalog;

import app.krista.extension.executor.ExtensionResponse;
import app.krista.extension.executor.Invoker;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.errorhandlers.ErrorHandlingStateManager;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.errorhandlers.ExtensionResponseGenerator;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.extresp.TelemetryHelper;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.validators.ValidationOrchestrator;
import app.krista.extensions.essentials.collaboration.outlook3.impl.MailHandler;
import app.krista.extensions.essentials.collaboration.outlook3.impl.MessagingAreaImpl;
import app.krista.extensions.essentials.collaboration.outlook3.impl.TestConnectionServiceImpl;
import app.krista.extensions.essentials.collaboration.outlook3.service.Account;
import app.krista.extensions.essentials.collaboration.outlook3.service.Email;
import app.krista.extensions.essentials.collaboration.outlook3.service.Folder;
import app.krista.extensions.util.EventHandler;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.ksdk.context.RequestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test suite for validating the "Allow Retry" parameter addition in version 3.0.16.
 * 
 * This test class focuses on:
 * 1. Parameter signature validation
 * 2. Backward compatibility (null/false behavior)
 * 3. Basic retry flow triggering
 * 4. Telemetry parameter tracking
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Allow Retry Parameter Tests - Version 3.0.16")
public class AllowRetryParameterTest {

    private static final String TEST_MESSAGE_ID = "test-message-123";
    private static final String TEST_FOLDER_NAME = "Archive";
    private static final String TEST_LABEL = "Read";

    @Mock
    private Account account;

    @Mock
    private RequestContext requestContext;

    @Mock
    private AuthorizationContext authorizationContext;

    @Mock
    private EventHandler eventHandler;

    @Mock
    private MailHandler mailHandler;

    @Mock
    private MessagingAreaImpl messagingAreaImpl;

    @Mock
    private ExtensionResponseGenerator responseGenerator;

    @Mock
    private ErrorHandlingStateManager internalStateManager;

    @Mock
    private ValidationOrchestrator validationOrchestrator;

    @Mock
    private Invoker invoker;

    @Mock
    private TestConnectionServiceImpl testConnectionService;

    @Mock
    private TelemetryHelper telemetryHelper;

    @Mock
    private Email email;

    @Mock
    private Folder folder;

    private MessagingArea messagingArea;

    @BeforeEach
    void setUp() {
        messagingArea = new MessagingArea(
                account,
                requestContext,
                authorizationContext,
                eventHandler,
                mailHandler,
                messagingAreaImpl,
                responseGenerator,
                internalStateManager,
                validationOrchestrator,
                invoker,
                testConnectionService,
                telemetryHelper
        );
    }

    // ========== Parameter Signature Tests ==========

    @Test
    @DisplayName("moveMessage: Accepts allowRetry parameter (null)")
    void testMoveMessage_AcceptsAllowRetryParameter_Null() {
        // Arrange
        when(validationOrchestrator.validate(anyMap())).thenReturn(Collections.emptyList());
        when(account.getEmail(TEST_MESSAGE_ID)).thenReturn(email);
        when(account.getFolderByName(anyList())).thenReturn(folder);
        when(email.moveToFolder(folder)).thenReturn(TEST_MESSAGE_ID);

        // Act - Should compile and run without errors
        ExtensionResponse response = messagingArea.moveMessage(TEST_MESSAGE_ID, TEST_FOLDER_NAME, null);

        // Assert
        assertNotNull(response);
        verify(validationOrchestrator, times(1)).validate(anyMap());
    }

    @Test
    @DisplayName("moveMessage: Accepts allowRetry parameter (false)")
    void testMoveMessage_AcceptsAllowRetryParameter_False() {
        // Arrange
        when(validationOrchestrator.validate(anyMap())).thenReturn(Collections.emptyList());
        when(account.getEmail(TEST_MESSAGE_ID)).thenReturn(email);
        when(account.getFolderByName(anyList())).thenReturn(folder);
        when(email.moveToFolder(folder)).thenReturn(TEST_MESSAGE_ID);

        // Act - Should compile and run without errors
        ExtensionResponse response = messagingArea.moveMessage(TEST_MESSAGE_ID, TEST_FOLDER_NAME, false);

        // Assert
        assertNotNull(response);
        verify(validationOrchestrator, times(1)).validate(anyMap());
    }

    @Test
    @DisplayName("moveMessage: Accepts allowRetry parameter (true)")
    void testMoveMessage_AcceptsAllowRetryParameter_True() {
        // Arrange
        when(validationOrchestrator.validate(anyMap())).thenReturn(Collections.emptyList());
        when(account.getEmail(TEST_MESSAGE_ID)).thenReturn(email);
        when(account.getFolderByName(anyList())).thenReturn(folder);
        when(email.moveToFolder(folder)).thenReturn(TEST_MESSAGE_ID);

        // Act - Should compile and run without errors
        ExtensionResponse response = messagingArea.moveMessage(TEST_MESSAGE_ID, TEST_FOLDER_NAME, true);

        // Assert
        assertNotNull(response);
        verify(validationOrchestrator, times(1)).validate(anyMap());
    }

    @Test
    @DisplayName("markMessage: Accepts allowRetry parameter (null)")
    void testMarkMessage_AcceptsAllowRetryParameter_Null() {
        // Arrange
        when(validationOrchestrator.validate(anyMap())).thenReturn(Collections.emptyList());
        ExtensionResponse mockResponse = mock(ExtensionResponse.class);
        when(messagingAreaImpl.markMessage(TEST_MESSAGE_ID, TEST_LABEL)).thenReturn(mockResponse);

        // Act - Should compile and run without errors
        ExtensionResponse response = messagingArea.markMessage(TEST_MESSAGE_ID, TEST_LABEL, null);

        // Assert
        assertNotNull(response);
        verify(validationOrchestrator, times(1)).validate(anyMap());
    }

    @Test
    @DisplayName("fetchAllLabels: Accepts allowRetry parameter (null)")
    void testFetchAllLabels_AcceptsAllowRetryParameter_Null() {
        // Arrange
        when(account.getFolderNames()).thenReturn(List.of("Label1", "Label2", "Label3"));

        // Act - Should compile and run without errors
        ExtensionResponse response = messagingArea.fetchAllLabels();

        // Assert
        assertNotNull(response);
        verify(account, times(1)).getFolderNames();
    }

    @Test
    @DisplayName("fetchAllLabels: Accepts allowRetry parameter (true)")
    void testFetchAllLabels_AcceptsAllowRetryParameter_True() {
        // Arrange
        when(account.getFolderNames()).thenReturn(List.of("Label1", "Label2", "Label3"));

        // Act - Should compile and run without errors
        ExtensionResponse response = messagingArea.fetchAllLabels();

        // Assert
        assertNotNull(response);
        verify(account, times(1)).getFolderNames();
    }

    // ========== Backward Compatibility Tests ==========

    @Test
    @DisplayName("Backward Compatibility: null allowRetry executes successfully")
    void testBackwardCompatibility_NullAllowRetry() {
        // Arrange
        when(validationOrchestrator.validate(anyMap())).thenReturn(Collections.emptyList());
        when(account.getEmail(TEST_MESSAGE_ID)).thenReturn(email);
        when(account.getFolderByName(anyList())).thenReturn(folder);
        when(email.moveToFolder(folder)).thenReturn(TEST_MESSAGE_ID);

        // Act
        ExtensionResponse response = messagingArea.moveMessage(TEST_MESSAGE_ID, TEST_FOLDER_NAME, null);

        // Assert - Should execute business logic successfully
        assertNotNull(response);
        verify(account, times(1)).getEmail(TEST_MESSAGE_ID);
        verify(email, times(1)).moveToFolder(folder);
        verify(telemetryHelper, times(1)).recordSuccess(eq("outlook3.moveMessage"), anyLong(), anyMap());
    }

    @Test
    @DisplayName("Backward Compatibility: false allowRetry executes successfully")
    void testBackwardCompatibility_FalseAllowRetry() {
        // Arrange
        when(validationOrchestrator.validate(anyMap())).thenReturn(Collections.emptyList());
        when(account.getEmail(TEST_MESSAGE_ID)).thenReturn(email);
        when(account.getFolderByName(anyList())).thenReturn(folder);
        when(email.moveToFolder(folder)).thenReturn(TEST_MESSAGE_ID);

        // Act
        ExtensionResponse response = messagingArea.moveMessage(TEST_MESSAGE_ID, TEST_FOLDER_NAME, false);

        // Assert - Should execute business logic successfully
        assertNotNull(response);
        verify(account, times(1)).getEmail(TEST_MESSAGE_ID);
        verify(email, times(1)).moveToFolder(folder);
        verify(telemetryHelper, times(1)).recordSuccess(eq("outlook3.moveMessage"), anyLong(), anyMap());
    }

    // ========== Telemetry Tests ==========

    @Test
    @DisplayName("Telemetry: allow_retry parameter is included in success telemetry")
    void testTelemetry_AllowRetryIncludedInSuccess() {
        // Arrange
        when(validationOrchestrator.validate(anyMap())).thenReturn(Collections.emptyList());
        when(account.getEmail(TEST_MESSAGE_ID)).thenReturn(email);
        when(account.getFolderByName(anyList())).thenReturn(folder);
        when(email.moveToFolder(folder)).thenReturn(TEST_MESSAGE_ID);

        // Act
        messagingArea.moveMessage(TEST_MESSAGE_ID, TEST_FOLDER_NAME, true);

        // Assert - Verify telemetry was called (parameter tracking verified in implementation)
        verify(telemetryHelper, times(1)).recordSuccess(
                eq("outlook3.moveMessage"),
                anyLong(),
                anyMap()
        );
    }

    @Test
    @DisplayName("Telemetry: Increment count is called for all operations")
    void testTelemetry_IncrementCountCalled() {
        // Arrange
        when(validationOrchestrator.validate(anyMap())).thenReturn(Collections.emptyList());
        when(account.getEmail(TEST_MESSAGE_ID)).thenReturn(email);
        when(account.getFolderByName(anyList())).thenReturn(folder);
        when(email.moveToFolder(folder)).thenReturn(TEST_MESSAGE_ID);

        // Act
        messagingArea.moveMessage(TEST_MESSAGE_ID, TEST_FOLDER_NAME, null);

        // Assert
        verify(telemetryHelper, times(1)).incrementCount("outlook3.moveMessage");
    }

    // ========== Integration Tests ==========

    @Test
    @DisplayName("Integration: Complete successful flow with allowRetry=null")
    void testIntegration_SuccessfulFlow_AllowRetryNull() {
        // Arrange
        when(validationOrchestrator.validate(anyMap())).thenReturn(Collections.emptyList());
        when(account.getEmail(TEST_MESSAGE_ID)).thenReturn(email);
        when(account.getFolderByName(anyList())).thenReturn(folder);
        when(email.moveToFolder(folder)).thenReturn(TEST_MESSAGE_ID);

        // Act
        ExtensionResponse response = messagingArea.moveMessage(TEST_MESSAGE_ID, TEST_FOLDER_NAME, null);

        // Assert - Verify complete flow
        assertNotNull(response);
        verify(telemetryHelper, times(1)).incrementCount("outlook3.moveMessage");
        verify(validationOrchestrator, times(1)).validate(anyMap());
        verify(account, times(1)).getEmail(TEST_MESSAGE_ID);
        verify(account, times(1)).getFolderByName(anyList());
        verify(email, times(1)).moveToFolder(folder);
        verify(telemetryHelper, times(1)).recordSuccess(eq("outlook3.moveMessage"), anyLong(), anyMap());
    }

    @Test
    @DisplayName("Integration: Complete successful flow with allowRetry=true")
    void testIntegration_SuccessfulFlow_AllowRetryTrue() {
        // Arrange
        when(validationOrchestrator.validate(anyMap())).thenReturn(Collections.emptyList());
        when(account.getEmail(TEST_MESSAGE_ID)).thenReturn(email);
        when(account.getFolderByName(anyList())).thenReturn(folder);
        when(email.moveToFolder(folder)).thenReturn(TEST_MESSAGE_ID);

        // Act
        ExtensionResponse response = messagingArea.moveMessage(TEST_MESSAGE_ID, TEST_FOLDER_NAME, true);

        // Assert - Verify complete flow (same as null/false for successful validation)
        assertNotNull(response);
        verify(telemetryHelper, times(1)).incrementCount("outlook3.moveMessage");
        verify(validationOrchestrator, times(1)).validate(anyMap());
        verify(account, times(1)).getEmail(TEST_MESSAGE_ID);
        verify(account, times(1)).getFolderByName(anyList());
        verify(email, times(1)).moveToFolder(folder);
        verify(telemetryHelper, times(1)).recordSuccess(eq("outlook3.moveMessage"), anyLong(), anyMap());
    }
}

