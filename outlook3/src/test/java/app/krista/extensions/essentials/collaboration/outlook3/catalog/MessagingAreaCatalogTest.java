package app.krista.extensions.essentials.collaboration.outlook3.catalog;

import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extension.executor.ExtensionResponse;
import app.krista.extension.executor.Invoker;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.entities.MailDetails;
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

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for MessagingArea catalog requests.
 * Tests cover all catalog request methods including success scenarios, failure scenarios,
 * validation handling, and the Allow Retry feature.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MessagingArea Catalog Request Tests")
public class MessagingAreaCatalogTest {

    // Test constants
    private static final String TEST_MESSAGE_ID = "AQMkADY4ZTFiMGIxLWU1YjUtNDEwMS04Y2Q0";
    private static final String TEST_FOLDER_NAME = "Archive";
    private static final String TEST_LABEL = "Read";
    private static final String TEST_EMAIL_FROM = "sender@example.com";
    private static final String TEST_EMAIL_TO = "recipient@example.com";
    private static final String TEST_SUBJECT = "Test Subject";

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

    // ========== Fetch All Labels Tests ==========

    @Test
    @DisplayName("fetchAllLabels: Success - Returns list of labels")
    void testFetchAllLabels_Success() {
        // Arrange
        List<String> expectedLabels = Arrays.asList("Inbox", "Sent", "Archive", "Drafts");
        when(account.getFolderNames()).thenReturn(expectedLabels);

        // Act
        ExtensionResponse response = messagingArea.fetchAllLabels();

        // Assert
        assertNotNull(response);
        assertEquals(ExtensionResponse.Result.SUCCESS, response.getResult());
        assertTrue(response.getResponseValue().containsKey("Labels"));
        
        @SuppressWarnings("unchecked")
        List<String> actualLabels = (List<String>) response.getResponseValue().get("Labels");
        assertEquals(4, actualLabels.size());
        assertEquals(expectedLabels, actualLabels);

        verify(account, times(1)).getFolderNames();
        verify(telemetryHelper, times(1)).incrementCount("outlook3.fetchAllLabels");
        verify(telemetryHelper, times(1)).recordSuccess(eq("outlook3.fetchAllLabels"), anyLong(), anyMap());
    }

    @Test
    @DisplayName("fetchAllLabels: Success - Empty label list")
    void testFetchAllLabels_EmptyList() {
        // Arrange
        List<String> emptyLabels = Collections.emptyList();
        when(account.getFolderNames()).thenReturn(emptyLabels);

        // Act
        ExtensionResponse response = messagingArea.fetchAllLabels();

        // Assert
        assertNotNull(response);
        assertEquals(ExtensionResponse.Result.SUCCESS, response.getResult());
        
        @SuppressWarnings("unchecked")
        List<String> actualLabels = (List<String>) response.getResponseValue().get("Labels");
        assertTrue(actualLabels.isEmpty());

        verify(account, times(1)).getFolderNames();
    }

    @Test
    @DisplayName("fetchAllLabels: Authorization Exception - Invoke As User")
    void testFetchAllLabels_AuthorizationException_InvokeAsUser() {
        // Arrange
        MustAuthorizeException authException = new MustAuthorizeException("Authorization required");
        when(account.getFolderNames()).thenThrow(authException);
        when(requestContext.invokeAsUser()).thenReturn(true);

        // Act & Assert
        assertThrows(MustAuthorizeException.class, () -> messagingArea.fetchAllLabels());

        verify(telemetryHelper, times(1)).recordValidationError(
                eq("outlook3.fetchAllLabels"), anyLong(), anyString(), anyMap());
    }

    @Test
    @DisplayName("fetchAllLabels: Authorization Exception - Not Invoke As User")
    void testFetchAllLabels_AuthorizationException_NotInvokeAsUser() {
        // Arrange
        MustAuthorizeException authException = new MustAuthorizeException("Authorization required");
        when(account.getFolderNames()).thenThrow(authException);
        when(requestContext.invokeAsUser()).thenReturn(false);

        // Act
        ExtensionResponse response = messagingArea.fetchAllLabels();

        // Assert
        assertNotNull(response);
        assertEquals(ExtensionResponse.Result.FAILURE, response.getResult());
        verify(telemetryHelper, times(1)).recordValidationError(
                eq("outlook3.fetchAllLabels"), anyLong(), anyString(), anyMap());
    }

    @Test
    @DisplayName("fetchAllLabels: System Exception")
    void testFetchAllLabels_SystemException() {
        // Arrange
        when(account.getFolderNames()).thenThrow(new RuntimeException("System error"));

        // Act
        ExtensionResponse response = messagingArea.fetchAllLabels();

        // Assert
        assertNotNull(response);
        assertEquals(ExtensionResponse.Result.FAILURE, response.getResult());
        verify(telemetryHelper, times(1)).recordError(
                eq("outlook3.fetchAllLabels"), anyLong(), any(Exception.class), anyMap());
    }

    // ========== Fetch Mail By Message ID Tests ==========

    @Test
    @DisplayName("fetchMailByMessageId: Success - Valid message ID")
    void testFetchMailByMessageId_Success() {
        // Arrange
        MailDetails mockMailDetails = createMockMailDetails();
        when(validationOrchestrator.validate(anyMap())).thenReturn(Collections.emptyList());
        when(account.getEmail(TEST_MESSAGE_ID)).thenReturn(email);
        when(mailHandler.fromEmail(email, null)).thenReturn(mockMailDetails);

        // Act
        ExtensionResponse response = messagingArea.fetchMailByMessageId(TEST_MESSAGE_ID, null);

        // Assert
        assertNotNull(response);
        assertEquals(ExtensionResponse.Result.SUCCESS, response.getResult());
        assertTrue(response.getResponseValue().containsKey("Mail"));
        
        MailDetails actualMail = (MailDetails) response.getResponseValue().get("Mail");
        assertEquals(TEST_MESSAGE_ID, actualMail.messageID);
        assertEquals(TEST_EMAIL_FROM, actualMail.from);
        assertEquals(TEST_SUBJECT, actualMail.subject);

        verify(validationOrchestrator, times(1)).validate(anyMap());
        verify(account, times(1)).getEmail(TEST_MESSAGE_ID);
        verify(telemetryHelper, times(1)).recordSuccess(
                eq("outlook3.fetchMailByMessageId"), anyLong(), anyMap());
    }

    @Test
    @DisplayName("fetchMailByMessageId: Validation Error - Allow Retry True")
    void testFetchMailByMessageId_ValidationError_AllowRetryTrue() {
        // Arrange
        List<ValidationOrchestrator.ValidationResult> validationResults = Arrays.asList(
                new ValidationOrchestrator.ValidationResult(
                        "Please confirm Message ID",
                        "Message ID",
                        "Please enter valid Message ID",
                        "Invalid message ID format",
                        "Text"
                )
        );
        when(validationOrchestrator.validate(anyMap())).thenReturn(validationResults);

        ExtensionResponse mockResponse = mock(ExtensionResponse.class);
        when(responseGenerator.generateConfirmationResponse(
                any(), anyList(), anyString(), anyMap())).thenReturn(mockResponse);

        // Act
        ExtensionResponse response = messagingArea.fetchMailByMessageId(TEST_MESSAGE_ID, true);

        // Assert
        assertNotNull(response);
        verify(responseGenerator, times(1)).generateConfirmationResponse(
                eq(ExtensionResponse.Error.ExceptionType.INPUT_ERROR),
                eq(validationResults),
                anyString(),
                anyMap());
        verify(internalStateManager, times(1)).put(anyString(), anyString());
        verify(telemetryHelper, times(1)).recordRetryPrompted(
                eq("outlook3.fetchMailByMessageId"), anyLong(), anyMap());
    }

    @Test
    @DisplayName("fetchMailByMessageId: Validation Error - Allow Retry False")
    void testFetchMailByMessageId_ValidationError_AllowRetryFalse() {
        // Arrange
        List<ValidationOrchestrator.ValidationResult> validationResults = Arrays.asList(
                new ValidationOrchestrator.ValidationResult(
                        "Please confirm Message ID",
                        "Message ID",
                        "Please enter valid Message ID",
                        "Invalid message ID format",
                        "Text"
                )
        );
        when(validationOrchestrator.validate(anyMap())).thenReturn(validationResults);

        ExtensionResponse mockResponse = mock(ExtensionResponse.class);
        when(responseGenerator.generateFetchDenyResponse(
                any(), anyList(), any(), anyMap())).thenReturn(mockResponse);

        // Act
        ExtensionResponse response = messagingArea.fetchMailByMessageId(TEST_MESSAGE_ID, false);

        // Assert
        assertNotNull(response);
        verify(responseGenerator, times(1)).generateFetchDenyResponse(
                eq(ExtensionResponse.Error.ExceptionType.INPUT_ERROR),
                eq(validationResults),
                isNull(),
                anyMap());
        verify(internalStateManager, never()).put(anyString(), anyString());
        verify(telemetryHelper, times(1)).recordValidationError(
                eq("outlook3.fetchMailByMessageId"), anyLong(), anyString(), anyMap());
    }

    @Test
    @DisplayName("fetchMailByMessageId: Validation Error - Allow Retry Null (defaults to false)")
    void testFetchMailByMessageId_ValidationError_AllowRetryNull() {
        // Arrange
        List<ValidationOrchestrator.ValidationResult> validationResults = Arrays.asList(
                new ValidationOrchestrator.ValidationResult(
                        "Please confirm Message ID",
                        "Message ID",
                        "Please enter valid Message ID",
                        "Invalid message ID format",
                        "Text"
                )
        );
        when(validationOrchestrator.validate(anyMap())).thenReturn(validationResults);

        ExtensionResponse mockResponse = mock(ExtensionResponse.class);
        when(responseGenerator.generateFetchDenyResponse(
                any(), anyList(), any(), anyMap())).thenReturn(mockResponse);

        // Act
        ExtensionResponse response = messagingArea.fetchMailByMessageId(TEST_MESSAGE_ID, null);

        // Assert
        assertNotNull(response);
        verify(responseGenerator, times(1)).generateFetchDenyResponse(
                eq(ExtensionResponse.Error.ExceptionType.INPUT_ERROR),
                eq(validationResults),
                isNull(),
                anyMap());
        verify(internalStateManager, never()).put(anyString(), anyString());
    }

    // ========== Move Message Tests ==========

    @Test
    @DisplayName("moveMessage: Success - Valid message ID and folder")
    void testMoveMessage_Success() {
        // Arrange
        when(validationOrchestrator.validate(anyMap())).thenReturn(Collections.emptyList());
        when(account.getEmail(TEST_MESSAGE_ID)).thenReturn(email);
        when(account.getFolderByName(anyList())).thenReturn(folder);
        when(email.moveToFolder(folder)).thenReturn(TEST_MESSAGE_ID);

        // Act
        ExtensionResponse response = messagingArea.moveMessage(TEST_MESSAGE_ID, TEST_FOLDER_NAME, null);

        // Assert
        assertNotNull(response);
        assertEquals(ExtensionResponse.Result.SUCCESS, response.getResult());
        assertTrue(response.getResponseValue().containsKey("Message ID"));
        assertEquals(TEST_MESSAGE_ID, response.getResponseValue().get("Message ID"));

        verify(validationOrchestrator, times(1)).validate(anyMap());
        verify(account, times(1)).getEmail(TEST_MESSAGE_ID);
        verify(account, times(1)).getFolderByName(anyList());
        verify(email, times(1)).moveToFolder(folder);
        verify(telemetryHelper, times(1)).recordSuccess(
                eq("outlook3.moveMessage"), anyLong(), anyMap());
    }

    @Test
    @DisplayName("moveMessage: Validation Error - Allow Retry True")
    void testMoveMessage_ValidationError_AllowRetryTrue() {
        // Arrange
        List<ValidationOrchestrator.ValidationResult> validationResults = Arrays.asList(
                new ValidationOrchestrator.ValidationResult(
                        "Please confirm Message ID",
                        "Message ID",
                        "Please enter valid Message ID",
                        "Invalid message ID",
                        "Text"
                ),
                new ValidationOrchestrator.ValidationResult(
                        "Please confirm Folder Name",
                        "Folder Name",
                        "Please enter valid Folder Name",
                        "Invalid folder name",
                        "Text"
                )
        );
        when(validationOrchestrator.validate(anyMap())).thenReturn(validationResults);

        ExtensionResponse mockResponse = mock(ExtensionResponse.class);
        when(responseGenerator.generateConfirmationResponse(
                any(), anyList(), anyString(), anyMap())).thenReturn(mockResponse);

        // Act
        ExtensionResponse response = messagingArea.moveMessage(TEST_MESSAGE_ID, TEST_FOLDER_NAME, true);

        // Assert
        assertNotNull(response);
        verify(responseGenerator, times(1)).generateConfirmationResponse(
                eq(ExtensionResponse.Error.ExceptionType.INPUT_ERROR),
                eq(validationResults),
                anyString(),
                anyMap());
        verify(internalStateManager, times(1)).put(anyString(), anyString());
        verify(telemetryHelper, times(1)).recordRetryPrompted(
                eq("outlook3.moveMessage"), anyLong(), anyMap());
        verify(account, never()).getEmail(anyString());
    }

    @Test
    @DisplayName("moveMessage: Validation Error - Allow Retry False")
    void testMoveMessage_ValidationError_AllowRetryFalse() {
        // Arrange
        List<ValidationOrchestrator.ValidationResult> validationResults = Arrays.asList(
                new ValidationOrchestrator.ValidationResult(
                        "Please confirm Folder Name",
                        "Folder Name",
                        "Please enter valid Folder Name",
                        "Folder not found",
                        "Text"
                )
        );
        when(validationOrchestrator.validate(anyMap())).thenReturn(validationResults);

        ExtensionResponse mockResponse = mock(ExtensionResponse.class);
        when(responseGenerator.generateFetchDenyResponse(
                any(), anyList(), any(), anyMap())).thenReturn(mockResponse);

        // Act
        ExtensionResponse response = messagingArea.moveMessage(TEST_MESSAGE_ID, TEST_FOLDER_NAME, false);

        // Assert
        assertNotNull(response);
        verify(responseGenerator, times(1)).generateFetchDenyResponse(
                eq(ExtensionResponse.Error.ExceptionType.INPUT_ERROR),
                eq(validationResults),
                isNull(),
                anyMap());
        verify(internalStateManager, never()).put(anyString(), anyString());
        verify(telemetryHelper, times(1)).recordValidationError(
                eq("outlook3.moveMessage"), anyLong(), anyString(), anyMap());
        verify(account, never()).getEmail(anyString());
    }

    @Test
    @DisplayName("moveMessage: Authorization Exception - Invoke As User")
    void testMoveMessage_AuthorizationException_InvokeAsUser() {
        // Arrange
        when(validationOrchestrator.validate(anyMap())).thenReturn(Collections.emptyList());
        MustAuthorizeException authException = new MustAuthorizeException("Authorization required");
        when(account.getEmail(TEST_MESSAGE_ID)).thenThrow(authException);
        when(requestContext.invokeAsUser()).thenReturn(true);

        // Act & Assert
        assertThrows(MustAuthorizeException.class, () ->
                messagingArea.moveMessage(TEST_MESSAGE_ID, TEST_FOLDER_NAME, null));

        verify(telemetryHelper, times(1)).recordValidationError(
                eq("outlook3.moveMessage"), anyLong(), anyString(), anyMap());
    }

    @Test
    @DisplayName("moveMessage: Authorization Exception - Not Invoke As User")
    void testMoveMessage_AuthorizationException_NotInvokeAsUser() {
        // Arrange
        when(validationOrchestrator.validate(anyMap())).thenReturn(Collections.emptyList());
        MustAuthorizeException authException = new MustAuthorizeException("Authorization required");
        when(account.getEmail(TEST_MESSAGE_ID)).thenThrow(authException);
        when(requestContext.invokeAsUser()).thenReturn(false);

        // Act
        ExtensionResponse response = messagingArea.moveMessage(TEST_MESSAGE_ID, TEST_FOLDER_NAME, true);

        // Assert
        assertNotNull(response);
        assertEquals(ExtensionResponse.Result.FAILURE, response.getResult());
        verify(telemetryHelper, times(1)).recordValidationError(
                eq("outlook3.moveMessage"), anyLong(), anyString(), anyMap());
    }

    @Test
    @DisplayName("moveMessage: System Exception")
    void testMoveMessage_SystemException() {
        // Arrange
        when(validationOrchestrator.validate(anyMap())).thenReturn(Collections.emptyList());
        when(account.getEmail(TEST_MESSAGE_ID)).thenThrow(new RuntimeException("System error"));

        // Act
        ExtensionResponse response = messagingArea.moveMessage(TEST_MESSAGE_ID, TEST_FOLDER_NAME, true);

        // Assert
        assertNotNull(response);
        assertEquals(ExtensionResponse.Result.FAILURE, response.getResult());
        verify(telemetryHelper, times(1)).recordError(
                eq("outlook3.moveMessage"), anyLong(), any(Exception.class), anyMap());
    }

    // ========== Mark Message Tests ==========

    @Test
    @DisplayName("markMessage: Success - Mark as Read")
    void testMarkMessage_Success_MarkAsRead() {
        // Arrange
        when(validationOrchestrator.validate(anyMap())).thenReturn(Collections.emptyList());
        ExtensionResponse mockResponse = mock(ExtensionResponse.class);
        when(mockResponse.getResult()).thenReturn(ExtensionResponse.Result.SUCCESS);
        when(messagingAreaImpl.markMessage(TEST_MESSAGE_ID, "Read")).thenReturn(mockResponse);

        // Act
        ExtensionResponse response = messagingArea.markMessage(TEST_MESSAGE_ID, "Read", null);

        // Assert
        assertNotNull(response);
        assertEquals(ExtensionResponse.Result.SUCCESS, response.getResult());

        verify(validationOrchestrator, times(1)).validate(anyMap());
        verify(messagingAreaImpl, times(1)).markMessage(TEST_MESSAGE_ID, "Read");
        verify(telemetryHelper, times(1)).recordSuccess(
                eq("outlook3.markMessage"), anyLong(), anyMap());
    }

    @Test
    @DisplayName("markMessage: Success - Mark as Unread")
    void testMarkMessage_Success_MarkAsUnread() {
        // Arrange
        when(validationOrchestrator.validate(anyMap())).thenReturn(Collections.emptyList());
        ExtensionResponse mockResponse = mock(ExtensionResponse.class);
        when(mockResponse.getResult()).thenReturn(ExtensionResponse.Result.SUCCESS);
        when(messagingAreaImpl.markMessage(TEST_MESSAGE_ID, "Unread")).thenReturn(mockResponse);

        // Act
        ExtensionResponse response = messagingArea.markMessage(TEST_MESSAGE_ID, "Unread", false);

        // Assert
        assertNotNull(response);
        assertEquals(ExtensionResponse.Result.SUCCESS, response.getResult());

        verify(messagingAreaImpl, times(1)).markMessage(TEST_MESSAGE_ID, "Unread");
    }

    @Test
    @DisplayName("markMessage: Validation Error - Allow Retry True")
    void testMarkMessage_ValidationError_AllowRetryTrue() {
        // Arrange
        List<ValidationOrchestrator.ValidationResult> validationResults = Arrays.asList(
                new ValidationOrchestrator.ValidationResult(
                        "Please confirm Message ID",
                        "Message ID",
                        "Please enter valid Message ID",
                        "Invalid message ID",
                        "Text"
                )
        );
        when(validationOrchestrator.validate(anyMap())).thenReturn(validationResults);

        ExtensionResponse mockResponse = mock(ExtensionResponse.class);
        when(responseGenerator.generateConfirmationResponse(
                any(), anyList(), anyString(), anyMap())).thenReturn(mockResponse);

        // Act
        ExtensionResponse response = messagingArea.markMessage(TEST_MESSAGE_ID, TEST_LABEL, true);

        // Assert
        assertNotNull(response);
        verify(responseGenerator, times(1)).generateConfirmationResponse(
                eq(ExtensionResponse.Error.ExceptionType.INPUT_ERROR),
                eq(validationResults),
                anyString(),
                anyMap());
        verify(internalStateManager, times(1)).put(anyString(), anyString());
        verify(telemetryHelper, times(1)).recordRetryPrompted(
                eq("outlook3.markMessage"), anyLong(), anyMap());
        verify(messagingAreaImpl, never()).markMessage(anyString(), anyString());
    }

    @Test
    @DisplayName("markMessage: Validation Error - Allow Retry False")
    void testMarkMessage_ValidationError_AllowRetryFalse() {
        // Arrange
        List<ValidationOrchestrator.ValidationResult> validationResults = Arrays.asList(
                new ValidationOrchestrator.ValidationResult(
                        "Please confirm Label",
                        "Label",
                        "Please enter valid Label",
                        "Invalid label",
                        "Text"
                )
        );
        when(validationOrchestrator.validate(anyMap())).thenReturn(validationResults);

        ExtensionResponse mockResponse = mock(ExtensionResponse.class);
        when(responseGenerator.generateFetchDenyResponse(
                any(), anyList(), any(), anyMap())).thenReturn(mockResponse);

        // Act
        ExtensionResponse response = messagingArea.markMessage(TEST_MESSAGE_ID, TEST_LABEL, false);

        // Assert
        assertNotNull(response);
        verify(responseGenerator, times(1)).generateFetchDenyResponse(
                eq(ExtensionResponse.Error.ExceptionType.INPUT_ERROR),
                eq(validationResults),
                isNull(),
                anyMap());
        verify(internalStateManager, never()).put(anyString(), anyString());
        verify(telemetryHelper, times(1)).recordValidationError(
                eq("outlook3.markMessage"), anyLong(), anyString(), anyMap());
        verify(messagingAreaImpl, never()).markMessage(anyString(), anyString());
    }

    @Test
    @DisplayName("markMessage: Authorization Exception - Invoke As User")
    void testMarkMessage_AuthorizationException_InvokeAsUser() {
        // Arrange
        when(validationOrchestrator.validate(anyMap())).thenReturn(Collections.emptyList());
        MustAuthorizeException authException = new MustAuthorizeException("Authorization required");
        when(messagingAreaImpl.markMessage(TEST_MESSAGE_ID, TEST_LABEL)).thenThrow(authException);
        when(requestContext.invokeAsUser()).thenReturn(true);

        // Act & Assert
        assertThrows(MustAuthorizeException.class, () ->
                messagingArea.markMessage(TEST_MESSAGE_ID, TEST_LABEL, null));

        verify(telemetryHelper, times(1)).recordValidationError(
                eq("outlook3.markMessage"), anyLong(), anyString(), anyMap());
    }

    @Test
    @DisplayName("markMessage: Authorization Exception - Not Invoke As User")
    void testMarkMessage_AuthorizationException_NotInvokeAsUser() {
        // Arrange
        when(validationOrchestrator.validate(anyMap())).thenReturn(Collections.emptyList());
        MustAuthorizeException authException = new MustAuthorizeException("Authorization required");
        when(messagingAreaImpl.markMessage(TEST_MESSAGE_ID, TEST_LABEL)).thenThrow(authException);
        when(requestContext.invokeAsUser()).thenReturn(false);

        // Act
        ExtensionResponse response = messagingArea.markMessage(TEST_MESSAGE_ID, TEST_LABEL, false);

        // Assert
        assertNotNull(response);
        assertEquals(ExtensionResponse.Result.FAILURE, response.getResult());
        verify(telemetryHelper, times(1)).recordValidationError(
                eq("outlook3.markMessage"), anyLong(), anyString(), anyMap());
    }

    @Test
    @DisplayName("markMessage: System Exception")
    void testMarkMessage_SystemException() {
        // Arrange
        when(validationOrchestrator.validate(anyMap())).thenReturn(Collections.emptyList());
        when(messagingAreaImpl.markMessage(TEST_MESSAGE_ID, TEST_LABEL))
                .thenThrow(new RuntimeException("System error"));

        // Act
        ExtensionResponse response = messagingArea.markMessage(TEST_MESSAGE_ID, TEST_LABEL, true);

        // Assert
        assertNotNull(response);
        assertEquals(ExtensionResponse.Result.FAILURE, response.getResult());
        verify(telemetryHelper, times(1)).recordError(
                eq("outlook3.markMessage"), anyLong(), any(Exception.class), anyMap());
    }

    // ========== Helper Methods ==========

    private MailDetails createMockMailDetails() {
        MailDetails mailDetails = new MailDetails();
        mailDetails.messageID = TEST_MESSAGE_ID;
        mailDetails.from = TEST_EMAIL_FROM;
        mailDetails.to = TEST_EMAIL_TO;
        mailDetails.subject = TEST_SUBJECT;
        mailDetails.sendDateAndTime = System.currentTimeMillis();
        return mailDetails;
    }
}

