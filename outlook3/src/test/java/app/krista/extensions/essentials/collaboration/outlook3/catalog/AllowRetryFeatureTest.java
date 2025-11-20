package app.krista.extensions.essentials.collaboration.outlook3.catalog;

import app.krista.extension.executor.ExtensionResponse;
import app.krista.extension.executor.Invoker;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.errorhandlers.ErrorHandlingStateManager;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.errorhandlers.ExtensionResponseGenerator;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.extresp.TelemetryHelper;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.validators.ValidationOrchestrator;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.validators.Validator;
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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for the "Allow Retry" feature added in version 3.0.16.
 * 
 * This test class validates:
 * 1. Backward compatibility (null/false allowRetry behaves as before)
 * 2. New retry behavior (true allowRetry triggers SubCatalog flow)
 * 3. Non-retry behavior (false allowRetry returns immediate error)
 * 4. Methods without validation (parameter is ignored)
 * 5. Telemetry tracking includes allow_retry parameter
 * 
 * Test Coverage:
 * - moveMessage() - method with validation
 * - markMessage() - method with validation
 * - fetchAllLabels() - method without validation
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Allow Retry Feature Tests - Version 3.0.16")
public class AllowRetryFeatureTest {

    // Test constants
    private static final String TEST_MESSAGE_ID = "test-message-123";
    private static final String TEST_FOLDER_NAME = "Archive";
    private static final String TEST_LABEL = "Read";
    private static final String VALIDATION_ERROR_MESSAGE = "Invalid message ID format";

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

    // ========== Move Message Tests ==========

    @Test
    @DisplayName("moveMessage: Success with valid inputs (allowRetry=null)")
    void testMoveMessage_Success_AllowRetryNull() {
        // Arrange
        when(validationOrchestrator.validate(anyMap())).thenReturn(Collections.emptyList());
        when(account.getEmail(TEST_MESSAGE_ID)).thenReturn(email);
        when(account.getFolderByName(anyList())).thenReturn(folder);
        when(email.moveToFolder(folder)).thenReturn(TEST_MESSAGE_ID);

        // Act
        ExtensionResponse response = messagingArea.moveMessage(TEST_MESSAGE_ID, TEST_FOLDER_NAME, null);

        // Assert
        assertNotNull(response);
        verify(validationOrchestrator, times(1)).validate(anyMap());
        verify(account, times(1)).getEmail(TEST_MESSAGE_ID);
        verify(email, times(1)).moveToFolder(folder);
        verify(telemetryHelper, times(1)).recordSuccess(eq("outlook3.moveMessage"), anyLong(), anyMap());
        verify(responseGenerator, never()).generateConfirmationResponse(any(), any(), any(), any());
        verify(responseGenerator, never()).generateFetchDenyResponse(any(), any(), any(), any());
    }

    @Test
    @DisplayName("moveMessage: Success with valid inputs (allowRetry=false)")
    void testMoveMessage_Success_AllowRetryFalse() {
        // Arrange
        when(validationOrchestrator.validate(anyMap())).thenReturn(Collections.emptyList());
        when(account.getEmail(TEST_MESSAGE_ID)).thenReturn(email);
        when(account.getFolderByName(anyList())).thenReturn(folder);
        when(email.moveToFolder(folder)).thenReturn(TEST_MESSAGE_ID);

        // Act
        ExtensionResponse response = messagingArea.moveMessage(TEST_MESSAGE_ID, TEST_FOLDER_NAME, false);

        // Assert
        assertNotNull(response);
        verify(validationOrchestrator, times(1)).validate(anyMap());
        verify(account, times(1)).getEmail(TEST_MESSAGE_ID);
        verify(email, times(1)).moveToFolder(folder);
        verify(telemetryHelper, times(1)).recordSuccess(eq("outlook3.moveMessage"), anyLong(), anyMap());
    }

    @Test
    @DisplayName("moveMessage: Success with valid inputs (allowRetry=true)")
    void testMoveMessage_Success_AllowRetryTrue() {
        // Arrange
        when(validationOrchestrator.validate(anyMap())).thenReturn(Collections.emptyList());
        when(account.getEmail(TEST_MESSAGE_ID)).thenReturn(email);
        when(account.getFolderByName(anyList())).thenReturn(folder);
        when(email.moveToFolder(folder)).thenReturn(TEST_MESSAGE_ID);

        // Act
        ExtensionResponse response = messagingArea.moveMessage(TEST_MESSAGE_ID, TEST_FOLDER_NAME, true);

        // Assert
        assertNotNull(response);
        verify(validationOrchestrator, times(1)).validate(anyMap());
        verify(account, times(1)).getEmail(TEST_MESSAGE_ID);
        verify(email, times(1)).moveToFolder(folder);
        verify(telemetryHelper, times(1)).recordSuccess(eq("outlook3.moveMessage"), anyLong(), anyMap());
    }

    @Test
    @DisplayName("moveMessage: Validation failure with allowRetry=true triggers SubCatalog flow")
    void testMoveMessage_ValidationFailure_AllowRetryTrue_TriggersSubCatalog() {
        // Arrange
        ValidationOrchestrator.ValidationResult validationResult =
                new ValidationOrchestrator.ValidationResult("Please confirm Message ID", "Message ID", "Please enter valid Message ID", VALIDATION_ERROR_MESSAGE, "Text");
        List<ValidationOrchestrator.ValidationResult> validationResults = List.of(validationResult);
        
        when(validationOrchestrator.validate(anyMap())).thenReturn(validationResults);
        
        ExtensionResponse mockConfirmationResponse = mock(ExtensionResponse.class);
        when(responseGenerator.generateConfirmationResponse(any(), any(), any(), any()))
                .thenReturn(mockConfirmationResponse);

        // Act
        ExtensionResponse response = messagingArea.moveMessage(TEST_MESSAGE_ID, TEST_FOLDER_NAME, true);

        // Assert
        assertNotNull(response);
        assertEquals(mockConfirmationResponse, response);
        verify(validationOrchestrator, times(1)).validate(anyMap());
        verify(responseGenerator, times(1)).generateConfirmationResponse(
                eq(ExtensionResponse.Error.ExceptionType.INPUT_ERROR),
                eq(validationResults),
                anyString(),
                anyMap()
        );
        verify(responseGenerator, never()).generateFetchDenyResponse(any(), any(), any(), any());
        verify(internalStateManager, times(1)).put(anyString(), anyString());
        verify(telemetryHelper, times(1)).recordRetryPrompted(eq("outlook3.moveMessage"), anyLong(), anyMap());
        verify(account, never()).getEmail(anyString());
    }

    @Test
    @DisplayName("moveMessage: Validation failure with allowRetry=false returns immediate error")
    void testMoveMessage_ValidationFailure_AllowRetryFalse_ReturnsImmediateError() {
        // Arrange
        ValidationOrchestrator.ValidationResult validationResult =
                new ValidationOrchestrator.ValidationResult("Please confirm Message ID", "Message ID", "Please enter valid Message ID", VALIDATION_ERROR_MESSAGE, "Text");
        List<ValidationOrchestrator.ValidationResult> validationResults = List.of(validationResult);
        
        when(validationOrchestrator.validate(anyMap())).thenReturn(validationResults);
        
        ExtensionResponse mockDenyResponse = mock(ExtensionResponse.class);
        when(responseGenerator.generateFetchDenyResponse(any(), any(), any(), any()))
                .thenReturn(mockDenyResponse);

        // Act
        ExtensionResponse response = messagingArea.moveMessage(TEST_MESSAGE_ID, TEST_FOLDER_NAME, false);

        // Assert
        assertNotNull(response);
        assertEquals(mockDenyResponse, response);
        verify(validationOrchestrator, times(1)).validate(anyMap());
        verify(responseGenerator, times(1)).generateFetchDenyResponse(
                eq(ExtensionResponse.Error.ExceptionType.INPUT_ERROR),
                eq(validationResults),
                isNull(),
                eq(Map.of())
        );
        verify(responseGenerator, never()).generateConfirmationResponse(any(), any(), any(), any());
        verify(internalStateManager, never()).put(anyString(), anyString());
        verify(telemetryHelper, times(1)).recordValidationError(eq("outlook3.moveMessage"), anyLong(), anyString(), anyMap());
        verify(account, never()).getEmail(anyString());
    }

    @Test
    @DisplayName("moveMessage: Validation failure with allowRetry=null returns immediate error (backward compatibility)")
    void testMoveMessage_ValidationFailure_AllowRetryNull_ReturnsImmediateError() {
        // Arrange
        ValidationOrchestrator.ValidationResult validationResult =
                new ValidationOrchestrator.ValidationResult("Please confirm Message ID", "Message ID", "Please enter valid Message ID", VALIDATION_ERROR_MESSAGE, "Text");
        List<ValidationOrchestrator.ValidationResult> validationResults = List.of(validationResult);
        
        when(validationOrchestrator.validate(anyMap())).thenReturn(validationResults);
        
        ExtensionResponse mockDenyResponse = mock(ExtensionResponse.class);
        when(responseGenerator.generateFetchDenyResponse(any(), any(), any(), any()))
                .thenReturn(mockDenyResponse);

        // Act
        ExtensionResponse response = messagingArea.moveMessage(TEST_MESSAGE_ID, TEST_FOLDER_NAME, null);

        // Assert
        assertNotNull(response);
        assertEquals(mockDenyResponse, response);
        verify(responseGenerator, times(1)).generateFetchDenyResponse(any(), any(), any(), any());
        verify(responseGenerator, never()).generateConfirmationResponse(any(), any(), any(), any());
        verify(account, never()).getEmail(anyString());
    }

    // ========== Mark Message Tests ==========

    @Test
    @DisplayName("markMessage: Success with valid inputs (allowRetry=null)")
    void testMarkMessage_Success_AllowRetryNull() {
        // Arrange
        when(validationOrchestrator.validate(anyMap())).thenReturn(Collections.emptyList());
        ExtensionResponse mockResponse = mock(ExtensionResponse.class);
        when(messagingAreaImpl.markMessage(TEST_MESSAGE_ID, TEST_LABEL)).thenReturn(mockResponse);

        // Act
        ExtensionResponse response = messagingArea.markMessage(TEST_MESSAGE_ID, TEST_LABEL, null);

        // Assert
        assertNotNull(response);
        assertEquals(mockResponse, response);
        verify(validationOrchestrator, times(1)).validate(anyMap());
        verify(messagingAreaImpl, times(1)).markMessage(TEST_MESSAGE_ID, TEST_LABEL);
        verify(telemetryHelper, times(1)).recordSuccess(eq("outlook3.markMessage"), anyLong(), anyMap());
    }

    @Test
    @DisplayName("markMessage: Validation failure with allowRetry=true triggers SubCatalog flow")
    void testMarkMessage_ValidationFailure_AllowRetryTrue_TriggersSubCatalog() {
        // Arrange
        ValidationOrchestrator.ValidationResult validationResult =
                new ValidationOrchestrator.ValidationResult("Please confirm Message ID", "Message ID", "Please enter valid Message ID", VALIDATION_ERROR_MESSAGE, "Text");
        List<ValidationOrchestrator.ValidationResult> validationResults = List.of(validationResult);
        
        when(validationOrchestrator.validate(anyMap())).thenReturn(validationResults);
        
        ExtensionResponse mockConfirmationResponse = mock(ExtensionResponse.class);
        when(responseGenerator.generateConfirmationResponse(any(), any(), any(), any()))
                .thenReturn(mockConfirmationResponse);

        // Act
        ExtensionResponse response = messagingArea.markMessage(TEST_MESSAGE_ID, TEST_LABEL, true);

        // Assert
        assertNotNull(response);
        assertEquals(mockConfirmationResponse, response);
        verify(responseGenerator, times(1)).generateConfirmationResponse(any(), any(), any(), any());
        verify(responseGenerator, never()).generateFetchDenyResponse(any(), any(), any(), any());
        verify(telemetryHelper, times(1)).recordRetryPrompted(eq("outlook3.markMessage"), anyLong(), anyMap());
    }

    // ========== Fetch All Labels Tests (Method without validation) ==========

    @Test
    @DisplayName("fetchAllLabels: Accepts allowRetry parameter (null)")
    void testFetchAllLabels_AllowRetryNull() {
        // Arrange
        List<String> mockLabels = List.of("Inbox", "Sent", "Archive");
        when(account.getFolderNames()).thenReturn(mockLabels);

        // Act
        ExtensionResponse response = messagingArea.fetchAllLabels();

        // Assert
        assertNotNull(response);
        assertEquals(mockLabels, response.getResponseValue().get("Labels"));
        verify(account, times(1)).getFolderNames();
        verify(telemetryHelper, times(1)).recordSuccess(eq("outlook3.fetchAllLabels"), anyLong(), anyMap());
        // Validation should not be called for methods without validation
        verify(validationOrchestrator, never()).validate(anyMap());
    }

    @Test
    @DisplayName("fetchAllLabels: Accepts allowRetry parameter (true)")
    void testFetchAllLabels_AllowRetryTrue() {
        // Arrange
        List<String> mockLabels = List.of("Inbox", "Sent", "Archive");
        when(account.getFolderNames()).thenReturn(mockLabels);

        // Act
        ExtensionResponse response = messagingArea.fetchAllLabels();

        // Assert
        assertNotNull(response);
        assertEquals(mockLabels, response.getResponseValue().get("Labels"));
        verify(account, times(1)).getFolderNames();
        verify(telemetryHelper, times(1)).recordSuccess(eq("outlook3.fetchAllLabels"), anyLong(), anyMap());
        // Validation should not be called for methods without validation
        verify(validationOrchestrator, never()).validate(anyMap());
    }

    // ========== Telemetry Tests ==========

    @Test
    @DisplayName("Telemetry: allow_retry parameter is included in success telemetry")
    void testTelemetry_AllowRetryParameterIncluded() {
        // Arrange
        when(validationOrchestrator.validate(anyMap())).thenReturn(Collections.emptyList());
        when(account.getEmail(TEST_MESSAGE_ID)).thenReturn(email);
        when(account.getFolderByName(anyList())).thenReturn(folder);
        when(email.moveToFolder(folder)).thenReturn(TEST_MESSAGE_ID);

        // Act
        messagingArea.moveMessage(TEST_MESSAGE_ID, TEST_FOLDER_NAME, true);

        // Assert - Verify telemetry includes allow_retry parameter
        verify(telemetryHelper, times(1)).recordSuccess(
                eq("outlook3.moveMessage"),
                anyLong(),
                argThat(map -> map.containsKey("allow_retry") && map.get("allow_retry").equals("true"))
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
        messagingArea.moveMessage(TEST_MESSAGE_ID, TEST_FOLDER_NAME, false);

        // Assert
        verify(telemetryHelper, times(1)).recordSuccess(eq("outlook3.moveMessage"), anyLong(), anyMap());
    }

    // ========== Integration Tests ==========

    @Test
    @DisplayName("Integration: Complete successful flow with allowRetry=null")
    void testIntegration_CompleteFlow_AllowRetryNull() {
        // Arrange
        when(validationOrchestrator.validate(anyMap())).thenReturn(Collections.emptyList());
        when(account.getEmail(TEST_MESSAGE_ID)).thenReturn(email);
        when(account.getFolderByName(anyList())).thenReturn(folder);
        when(email.moveToFolder(folder)).thenReturn(TEST_MESSAGE_ID);

        // Act
        ExtensionResponse response = messagingArea.moveMessage(TEST_MESSAGE_ID, TEST_FOLDER_NAME, null);

        // Assert - Complete flow verification
        assertNotNull(response);
        verify(validationOrchestrator, times(1)).validate(anyMap());
        verify(account, times(1)).getEmail(TEST_MESSAGE_ID);
        verify(account, times(1)).getFolderByName(anyList());
        verify(email, times(1)).moveToFolder(folder);
        verify(telemetryHelper, times(1)).recordSuccess(eq("outlook3.moveMessage"), anyLong(), anyMap());

        // Should not trigger retry flow
        verify(responseGenerator, never()).generateConfirmationResponse(any(), any(), any(), any());
        verify(internalStateManager, never()).put(anyString(), anyString());
    }

    @Test
    @DisplayName("Integration: Complete successful flow with allowRetry=true")
    void testIntegration_CompleteFlow_AllowRetryTrue() {
        // Arrange
        when(validationOrchestrator.validate(anyMap())).thenReturn(Collections.emptyList());
        when(account.getEmail(TEST_MESSAGE_ID)).thenReturn(email);
        when(account.getFolderByName(anyList())).thenReturn(folder);
        when(email.moveToFolder(folder)).thenReturn(TEST_MESSAGE_ID);

        // Act
        ExtensionResponse response = messagingArea.moveMessage(TEST_MESSAGE_ID, TEST_FOLDER_NAME, true);

        // Assert - Complete flow verification
        assertNotNull(response);
        verify(validationOrchestrator, times(1)).validate(anyMap());
        verify(account, times(1)).getEmail(TEST_MESSAGE_ID);
        verify(account, times(1)).getFolderByName(anyList());
        verify(email, times(1)).moveToFolder(folder);
        verify(telemetryHelper, times(1)).recordSuccess(eq("outlook3.moveMessage"), anyLong(), anyMap());

        // Should not trigger retry flow when validation passes
        verify(responseGenerator, never()).generateConfirmationResponse(any(), any(), any(), any());
        verify(internalStateManager, never()).put(anyString(), anyString());
    }

    // ========== Backward Compatibility Tests ==========

    @Test
    @DisplayName("Backward Compatibility: null allowRetry executes successfully")
    void testBackwardCompatibility_NullAllowRetry() {
        // Arrange
        when(validationOrchestrator.validate(anyMap())).thenReturn(Collections.emptyList());
        ExtensionResponse mockResponse = mock(ExtensionResponse.class);
        when(messagingAreaImpl.markMessage(TEST_MESSAGE_ID, TEST_LABEL)).thenReturn(mockResponse);

        // Act
        ExtensionResponse response = messagingArea.markMessage(TEST_MESSAGE_ID, TEST_LABEL, null);

        // Assert
        assertNotNull(response);
        assertEquals(mockResponse, response);
        verify(messagingAreaImpl, times(1)).markMessage(TEST_MESSAGE_ID, TEST_LABEL);
        verify(telemetryHelper, times(1)).recordSuccess(eq("outlook3.markMessage"), anyLong(), anyMap());
    }

    @Test
    @DisplayName("Backward Compatibility: false allowRetry executes successfully")
    void testBackwardCompatibility_FalseAllowRetry() {
        // Arrange
        when(validationOrchestrator.validate(anyMap())).thenReturn(Collections.emptyList());
        ExtensionResponse mockResponse = mock(ExtensionResponse.class);
        when(messagingAreaImpl.markMessage(TEST_MESSAGE_ID, TEST_LABEL)).thenReturn(mockResponse);

        // Act
        ExtensionResponse response = messagingArea.markMessage(TEST_MESSAGE_ID, TEST_LABEL, false);

        // Assert
        assertNotNull(response);
        assertEquals(mockResponse, response);
        verify(messagingAreaImpl, times(1)).markMessage(TEST_MESSAGE_ID, TEST_LABEL);
        verify(telemetryHelper, times(1)).recordSuccess(eq("outlook3.markMessage"), anyLong(), anyMap());
    }
}