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

import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extension.executor.ExtensionResponse;
import app.krista.extension.executor.Invoker;
import app.krista.extensions.essentials.collaboration.outlook3.api.OutlookApiResource;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.entities.MailDetails;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.errorhandlers.ErrorHandlingStateManager;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.errorhandlers.ExtensionResponseGenerator;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.extresp.TelemetryHelper;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.validators.ValidationOrchestrator;
import app.krista.extensions.essentials.collaboration.outlook3.impl.MailHandler;
import app.krista.extensions.essentials.collaboration.outlook3.impl.MessagingAreaImpl;
import app.krista.extensions.essentials.collaboration.outlook3.impl.TestConnectionServiceImpl;
import app.krista.extensions.essentials.collaboration.outlook3.impl.util.Constants;
import app.krista.extensions.essentials.collaboration.outlook3.service.Account;
import app.krista.extensions.essentials.collaboration.outlook3.service.Email;
import app.krista.extensions.essentials.collaboration.outlook3.service.Folder;
import app.krista.extensions.util.EventHandler;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.ksdk.context.RequestContext;
import app.krista.model.base.EntityValue;
import app.krista.model.base.File;
import app.krista.model.base.FreeForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MessagingArea Full Coverage Tests")
public class MessagingAreaTest {

    private static final String TEST_MESSAGE_ID = "AQMkADY4ZTFiMGIx";
    private static final String TEST_FOLDER_NAME = "Archive";
    private static final String TEST_LABEL = "Read";
    private static final String TEST_TO = "to@example.com";
    private static final String TEST_CC = "cc@example.com";
    private static final String TEST_BCC = "bcc@example.com";
    private static final String TEST_REPLY_TO = "replyto@example.com";
    private static final String TEST_SUBJECT = "Test Subject";
    private static final String TEST_MESSAGE = "<p>Test Message</p>";
    private static final String TEST_CATEGORY = "Blue Category";
    private static final String TEST_QUERY = "subject:test";

    @Mock private Account account;
    @Mock private RequestContext requestContext;
    @Mock private AuthorizationContext authorizationContext;
    @Mock private EventHandler eventHandler;
    @Mock private MailHandler mailHandler;
    @Mock private MessagingAreaImpl messagingAreaImpl;
    @Mock private ExtensionResponseGenerator responseGenerator;
    @Mock private ErrorHandlingStateManager internalStateManager;
    @Mock private ValidationOrchestrator validationOrchestrator;
    @Mock private Invoker invoker;
    @Mock private TestConnectionServiceImpl testConnectionService;
    @Mock private TelemetryHelper telemetryHelper;
    @Mock private Email email;
    @Mock private Folder folder;

    private MessagingArea messagingArea;

    @BeforeEach
    void setUp() {
        messagingArea = new MessagingArea(account, requestContext, authorizationContext,
                eventHandler, mailHandler, messagingAreaImpl, responseGenerator,
                internalStateManager, validationOrchestrator, invoker,
                testConnectionService, telemetryHelper);
    }

    private MailDetails createMockMailDetails() {
        MailDetails md = new MailDetails();
        md.messageID = TEST_MESSAGE_ID;
        md.from = "sender@example.com";
        md.to = TEST_TO;
        md.subject = TEST_SUBJECT;
        md.sendDateAndTime = System.currentTimeMillis();
        return md;
    }

    private List<ValidationOrchestrator.ValidationResult> validationResults() {
        return List.of(new ValidationOrchestrator.ValidationResult(
                "Confirm", "Field", "Enter valid", "Invalid", "Text"));
    }

    // ========== Fetch All Labels - Additional Branch Coverage ==========

    @Test
    @DisplayName("fetchAllLabels: Success - Null labels list covers null branch")
    void testFetchAllLabels_NullLabels() {
        when(account.getFolderNames()).thenReturn(null);
        // labels is null → safeTagMap uses 0 → then Map.of("Labels", null) may throw
        // Actually Map.of does not accept null values → NPE caught as system error
        ExtensionResponse resp = messagingArea.fetchAllLabels();
        assertEquals(ExtensionResponse.Result.FAILURE, resp.getResult());
    }

    // ========== Fetch Mail By Message ID - Additional Branch Coverage ==========

    @Test
    @DisplayName("fetchMailByMessageId: Success - Null mailDetails covers null branch")
    void testFetchMailByMessageId_NullMailDetails() {
        when(validationOrchestrator.validate(anyMap())).thenReturn(Collections.emptyList());
        when(account.getEmail(TEST_MESSAGE_ID)).thenReturn(email);
        when(mailHandler.fromEmail(email, null)).thenReturn(null);
        // mailDetails is null → Map.of("Mail", null) throws NPE → caught as system error
        ExtensionResponse resp = messagingArea.fetchMailByMessageId(TEST_MESSAGE_ID, null);
        assertEquals(ExtensionResponse.Result.FAILURE, resp.getResult());
    }

    // ========== Reply To All With CC and BCC ==========

    @Test
    @DisplayName("replyToAllWithCCAndBCC: Success - with attachments")
    void testReplyToAllWithCCAndBCC_Success() {
        when(validationOrchestrator.validate(anyMap())).thenReturn(Collections.emptyList());
        ExtensionResponse mockResp = mock(ExtensionResponse.class);
        when(mockResp.getResult()).thenReturn(ExtensionResponse.Result.SUCCESS);
        List<File> attachments = List.of(mock(File.class));
        when(messagingAreaImpl.replyToAllWithCCAndBCC(eq(attachments), eq(TEST_MESSAGE_ID),
                eq(TEST_TO), eq(TEST_CC), eq(TEST_BCC), eq(TEST_REPLY_TO),
                eq(TEST_MESSAGE), eq("HTML"))).thenReturn(mockResp);

        ExtensionResponse response = messagingArea.replyToAllWithCCAndBCC(
                TEST_MESSAGE_ID, TEST_TO, TEST_CC, TEST_BCC, TEST_REPLY_TO,
                TEST_MESSAGE, attachments, "HTML", null);

        assertEquals(ExtensionResponse.Result.SUCCESS, response.getResult());
        verify(telemetryHelper).recordSuccess(eq("outlook3.replyToAllWithCCAndBCC"), anyLong(), anyMap());
    }

    @Test
    @DisplayName("replyToAllWithCCAndBCC: Validation - Allow Retry True")
    void testReplyToAllWithCCAndBCC_ValidationRetryTrue() {
        when(validationOrchestrator.validate(anyMap())).thenReturn(validationResults());
        ExtensionResponse mockResp = mock(ExtensionResponse.class);
        when(responseGenerator.generateConfirmationResponse(any(), anyList(), anyString(), anyMap()))
                .thenReturn(mockResp);

        ExtensionResponse response = messagingArea.replyToAllWithCCAndBCC(
                TEST_MESSAGE_ID, TEST_TO, TEST_CC, TEST_BCC, TEST_REPLY_TO,
                TEST_MESSAGE, null, "HTML", true);

        assertNotNull(response);
        verify(internalStateManager).put(anyString(), anyString());
        verify(telemetryHelper).recordRetryPrompted(eq("outlook3.replyToAllWithCCAndBCC"), anyLong(), anyMap());
    }

    @Test
    @DisplayName("replyToAllWithCCAndBCC: Validation - Allow Retry False")
    void testReplyToAllWithCCAndBCC_ValidationRetryFalse() {
        when(validationOrchestrator.validate(anyMap())).thenReturn(validationResults());
        ExtensionResponse mockResp = mock(ExtensionResponse.class);
        when(responseGenerator.generateFetchDenyResponse(any(), anyList(), any(), anyMap()))
                .thenReturn(mockResp);

        ExtensionResponse response = messagingArea.replyToAllWithCCAndBCC(
                TEST_MESSAGE_ID, TEST_TO, TEST_CC, TEST_BCC, TEST_REPLY_TO,
                TEST_MESSAGE, null, "HTML", false);

        assertNotNull(response);
        verify(internalStateManager, never()).put(anyString(), anyString());
        verify(telemetryHelper).recordValidationError(eq("outlook3.replyToAllWithCCAndBCC"), anyLong(), anyString(), anyMap());
    }

    @Test
    @DisplayName("replyToAllWithCCAndBCC: Auth Exception - Invoke As User")
    void testReplyToAllWithCCAndBCC_AuthInvokeAsUser() {
        when(validationOrchestrator.validate(anyMap())).thenThrow(new MustAuthorizeException("Auth required"));
        when(requestContext.invokeAsUser()).thenReturn(true);

        assertThrows(MustAuthorizeException.class, () -> messagingArea.replyToAllWithCCAndBCC(
                TEST_MESSAGE_ID, TEST_TO, TEST_CC, TEST_BCC, TEST_REPLY_TO,
                TEST_MESSAGE, null, "HTML", null));
    }

    @Test
    @DisplayName("replyToAllWithCCAndBCC: Auth Exception - Not Invoke As User")
    void testReplyToAllWithCCAndBCC_AuthNotInvokeAsUser() {
        when(validationOrchestrator.validate(anyMap())).thenThrow(new MustAuthorizeException("Auth required"));
        when(requestContext.invokeAsUser()).thenReturn(false);

        ExtensionResponse response = messagingArea.replyToAllWithCCAndBCC(
                TEST_MESSAGE_ID, TEST_TO, TEST_CC, TEST_BCC, TEST_REPLY_TO,
                TEST_MESSAGE, null, "HTML", null);

        assertEquals(ExtensionResponse.Result.FAILURE, response.getResult());
    }

    @Test
    @DisplayName("replyToAllWithCCAndBCC: System Exception")
    void testReplyToAllWithCCAndBCC_SystemException() {
        when(validationOrchestrator.validate(anyMap())).thenThrow(new RuntimeException("System error"));

        ExtensionResponse response = messagingArea.replyToAllWithCCAndBCC(
                TEST_MESSAGE_ID, TEST_TO, TEST_CC, TEST_BCC, TEST_REPLY_TO,
                TEST_MESSAGE, null, "HTML", null);

        assertEquals(ExtensionResponse.Result.FAILURE, response.getResult());
        verify(telemetryHelper).recordError(eq("outlook3.replyToAllWithCCAndBCC"), anyLong(), any(Exception.class), anyMap());
    }

    // ========== Reply To All ==========

    @Test
    @DisplayName("replyToAll: Success")
    void testReplyToAll_Success() {
        when(validationOrchestrator.validate(anyMap())).thenReturn(Collections.emptyList());
        ExtensionResponse mockResp = mock(ExtensionResponse.class);
        when(mockResp.getResult()).thenReturn(ExtensionResponse.Result.SUCCESS);
        when(messagingAreaImpl.replyToAll(any(), eq(TEST_MESSAGE_ID), eq(TEST_MESSAGE), eq("HTML")))
                .thenReturn(mockResp);

        ExtensionResponse response = messagingArea.replyToAll(
                TEST_MESSAGE_ID, TEST_MESSAGE, null, "HTML", null);

        assertEquals(ExtensionResponse.Result.SUCCESS, response.getResult());
        verify(telemetryHelper).recordSuccess(eq("outlook3.replyToAll"), anyLong(), anyMap());
    }

    @Test
    @DisplayName("replyToAll: Validation - Allow Retry True")
    void testReplyToAll_ValidationRetryTrue() {
        when(validationOrchestrator.validate(anyMap())).thenReturn(validationResults());
        when(responseGenerator.generateConfirmationResponse(any(), anyList(), anyString(), anyMap()))
                .thenReturn(mock(ExtensionResponse.class));

        assertNotNull(messagingArea.replyToAll(TEST_MESSAGE_ID, TEST_MESSAGE, null, "HTML", true));
        verify(internalStateManager).put(anyString(), anyString());
        verify(telemetryHelper).recordRetryPrompted(eq("outlook3.replyToAll"), anyLong(), anyMap());
    }

    @Test
    @DisplayName("replyToAll: Validation - Allow Retry False")
    void testReplyToAll_ValidationRetryFalse() {
        when(validationOrchestrator.validate(anyMap())).thenReturn(validationResults());
        when(responseGenerator.generateFetchDenyResponse(any(), anyList(), any(), anyMap()))
                .thenReturn(mock(ExtensionResponse.class));

        assertNotNull(messagingArea.replyToAll(TEST_MESSAGE_ID, TEST_MESSAGE, null, "HTML", false));
        verify(telemetryHelper).recordValidationError(eq("outlook3.replyToAll"), anyLong(), anyString(), anyMap());
    }

    @Test
    @DisplayName("replyToAll: Auth Exception - Invoke As User")
    void testReplyToAll_AuthInvokeAsUser() {
        when(validationOrchestrator.validate(anyMap())).thenThrow(new MustAuthorizeException("Auth"));
        when(requestContext.invokeAsUser()).thenReturn(true);
        assertThrows(MustAuthorizeException.class, () ->
                messagingArea.replyToAll(TEST_MESSAGE_ID, TEST_MESSAGE, null, "HTML", null));
    }

    @Test
    @DisplayName("replyToAll: Auth Exception - Not Invoke As User")
    void testReplyToAll_AuthNotInvokeAsUser() {
        when(validationOrchestrator.validate(anyMap())).thenThrow(new MustAuthorizeException("Auth"));
        when(requestContext.invokeAsUser()).thenReturn(false);
        ExtensionResponse resp = messagingArea.replyToAll(TEST_MESSAGE_ID, TEST_MESSAGE, null, "HTML", null);
        assertEquals(ExtensionResponse.Result.FAILURE, resp.getResult());
    }

    @Test
    @DisplayName("replyToAll: System Exception")
    void testReplyToAll_SystemException() {
        when(validationOrchestrator.validate(anyMap())).thenThrow(new RuntimeException("Error"));
        ExtensionResponse resp = messagingArea.replyToAll(TEST_MESSAGE_ID, TEST_MESSAGE, null, "HTML", null);
        assertEquals(ExtensionResponse.Result.FAILURE, resp.getResult());
        verify(telemetryHelper).recordError(eq("outlook3.replyToAll"), anyLong(), any(Exception.class), anyMap());
    }

    // ========== Fetch Sent ==========

    @Test
    @DisplayName("fetchSent: Success - Null params (empty validation map)")
    void testFetchSent_Success_NullParams() {
        when(account.getSentFolder()).thenReturn(folder);
        when(folder.getEmails(null, null)).thenReturn(List.of(email));
        when(mailHandler.fromEmail(email, null)).thenReturn(createMockMailDetails());

        ExtensionResponse response = messagingArea.fetchSent(null, null, null);

        assertEquals(ExtensionResponse.Result.SUCCESS, response.getResult());
        assertTrue(response.getResponseValue().containsKey("Sent Mails"));
        verify(telemetryHelper).recordSuccess(eq("outlook3.fetchSent"), anyLong(), anyMap());
    }

    @Test
    @DisplayName("fetchSent: Success - Non-empty validation map but validation passes")
    void testFetchSent_ValidationPasses() {
        when(validationOrchestrator.validate(anyMap())).thenReturn(Collections.emptyList());
        when(account.getSentFolder()).thenReturn(folder);
        when(folder.getEmails(-1.0, 10.0)).thenReturn(List.of(email));
        when(mailHandler.fromEmail(email, null)).thenReturn(createMockMailDetails());

        ExtensionResponse response = messagingArea.fetchSent(-1.0, 10.0, null);

        assertEquals(ExtensionResponse.Result.SUCCESS, response.getResult());
    }

    @Test
    @DisplayName("fetchSent: Validation Fails - Allow Retry True")
    void testFetchSent_ValidationRetryTrue() {
        when(validationOrchestrator.validate(anyMap())).thenReturn(validationResults());
        when(responseGenerator.generateConfirmationResponse(any(), anyList(), anyString(), anyMap()))
                .thenReturn(mock(ExtensionResponse.class));

        assertNotNull(messagingArea.fetchSent(-1.0, 10.0, true));
        verify(internalStateManager).put(anyString(), anyString());
        verify(telemetryHelper).recordRetryPrompted(eq("outlook3.fetchSent"), anyLong(), anyMap());
    }

    @Test
    @DisplayName("fetchSent: Validation Fails - Allow Retry False")
    void testFetchSent_ValidationRetryFalse() {
        when(validationOrchestrator.validate(anyMap())).thenReturn(validationResults());
        when(responseGenerator.generateFetchDenyResponse(any(), anyList(), any(), anyMap()))
                .thenReturn(mock(ExtensionResponse.class));

        assertNotNull(messagingArea.fetchSent(-1.0, 10.0, false));
        verify(telemetryHelper).recordValidationError(eq("outlook3.fetchSent"), anyLong(), anyString(), anyMap());
    }

    @Test
    @DisplayName("fetchSent: Auth Exception - Invoke As User")
    void testFetchSent_AuthInvokeAsUser() {
        when(account.getSentFolder()).thenThrow(new MustAuthorizeException("Auth"));
        when(requestContext.invokeAsUser()).thenReturn(true);
        assertThrows(MustAuthorizeException.class, () -> messagingArea.fetchSent(null, null, null));
    }

    @Test
    @DisplayName("fetchSent: Auth Exception - Not Invoke As User")
    void testFetchSent_AuthNotInvokeAsUser() {
        when(account.getSentFolder()).thenThrow(new MustAuthorizeException("Auth"));
        when(requestContext.invokeAsUser()).thenReturn(false);
        assertEquals(ExtensionResponse.Result.FAILURE, messagingArea.fetchSent(null, null, null).getResult());
    }

    @Test
    @DisplayName("fetchSent: System Exception")
    void testFetchSent_SystemException() {
        when(account.getSentFolder()).thenThrow(new RuntimeException("Error"));
        assertEquals(ExtensionResponse.Result.FAILURE, messagingArea.fetchSent(null, null, null).getResult());
        verify(telemetryHelper).recordError(eq("outlook3.fetchSent"), anyLong(), any(Exception.class), anyMap());
    }

    // ========== Forward Mail ==========

    @Test
    @DisplayName("forwardMail: Success")
    void testForwardMail_Success() {
        when(validationOrchestrator.validate(anyMap())).thenReturn(Collections.emptyList());
        ExtensionResponse mockResp = mock(ExtensionResponse.class);
        when(mockResp.getResult()).thenReturn(ExtensionResponse.Result.SUCCESS);
        when(messagingAreaImpl.forwardMail(TEST_MESSAGE_ID, TEST_TO, TEST_MESSAGE, "HTML"))
                .thenReturn(mockResp);

        ExtensionResponse resp = messagingArea.forwardMail(TEST_MESSAGE_ID, TEST_TO, TEST_MESSAGE, "HTML", null);
        assertEquals(ExtensionResponse.Result.SUCCESS, resp.getResult());
        verify(telemetryHelper).recordSuccess(eq("outlook3.forwardMail"), anyLong(), anyMap());
    }

    @Test
    @DisplayName("forwardMail: Validation - Allow Retry True")
    void testForwardMail_ValidationRetryTrue() {
        when(validationOrchestrator.validate(anyMap())).thenReturn(validationResults());
        when(responseGenerator.generateConfirmationResponse(any(), anyList(), anyString(), anyMap()))
                .thenReturn(mock(ExtensionResponse.class));

        assertNotNull(messagingArea.forwardMail(TEST_MESSAGE_ID, TEST_TO, TEST_MESSAGE, "HTML", true));
        verify(internalStateManager).put(anyString(), anyString());
        verify(telemetryHelper).recordRetryPrompted(eq("outlook3.forwardMail"), anyLong(), anyMap());
    }

    @Test
    @DisplayName("forwardMail: Validation - Allow Retry False")
    void testForwardMail_ValidationRetryFalse() {
        when(validationOrchestrator.validate(anyMap())).thenReturn(validationResults());
        when(responseGenerator.generateFetchDenyResponse(any(), anyList(), any(), anyMap()))
                .thenReturn(mock(ExtensionResponse.class));

        assertNotNull(messagingArea.forwardMail(TEST_MESSAGE_ID, TEST_TO, TEST_MESSAGE, "HTML", false));
        verify(telemetryHelper).recordValidationError(eq("outlook3.forwardMail"), anyLong(), anyString(), anyMap());
    }

    @Test
    @DisplayName("forwardMail: Auth Exception - Invoke As User")
    void testForwardMail_AuthInvokeAsUser() {
        when(validationOrchestrator.validate(anyMap())).thenThrow(new MustAuthorizeException("Auth"));
        when(requestContext.invokeAsUser()).thenReturn(true);
        assertThrows(MustAuthorizeException.class, () ->
                messagingArea.forwardMail(TEST_MESSAGE_ID, TEST_TO, TEST_MESSAGE, "HTML", null));
    }

    @Test
    @DisplayName("forwardMail: Auth Exception - Not Invoke As User")
    void testForwardMail_AuthNotInvokeAsUser() {
        when(validationOrchestrator.validate(anyMap())).thenThrow(new MustAuthorizeException("Auth"));
        when(requestContext.invokeAsUser()).thenReturn(false);
        assertEquals(ExtensionResponse.Result.FAILURE,
                messagingArea.forwardMail(TEST_MESSAGE_ID, TEST_TO, TEST_MESSAGE, "HTML", null).getResult());
    }

    @Test
    @DisplayName("forwardMail: System Exception")
    void testForwardMail_SystemException() {
        when(validationOrchestrator.validate(anyMap())).thenThrow(new RuntimeException("Error"));
        assertEquals(ExtensionResponse.Result.FAILURE,
                messagingArea.forwardMail(TEST_MESSAGE_ID, TEST_TO, TEST_MESSAGE, "HTML", null).getResult());
        verify(telemetryHelper).recordError(eq("outlook3.forwardMail"), anyLong(), any(Exception.class), anyMap());
    }

    // ========== Fetch Mail Details By Query ==========

    @Test
    @DisplayName("fetchMailDetailsByQuery: Success")
    void testFetchMailDetailsByQuery_Success() {
        when(account.searchEmails(TEST_QUERY)).thenReturn(List.of(email));
        when(mailHandler.fromEmail(email, null)).thenReturn(createMockMailDetails());

        ExtensionResponse resp = messagingArea.fetchMailDetailsByQuery(TEST_QUERY, null);
        assertEquals(ExtensionResponse.Result.SUCCESS, resp.getResult());
        assertTrue(resp.getResponseValue().containsKey("Mails"));
        verify(telemetryHelper).recordSuccess(eq("outlook3.fetchMailDetailsByQuery"), anyLong(), anyMap());
    }

    @Test
    @DisplayName("fetchMailDetailsByQuery: Auth Exception - Invoke As User")
    void testFetchMailDetailsByQuery_AuthInvokeAsUser() {
        when(account.searchEmails(TEST_QUERY)).thenThrow(new MustAuthorizeException("Auth"));
        when(requestContext.invokeAsUser()).thenReturn(true);
        assertThrows(MustAuthorizeException.class, () ->
                messagingArea.fetchMailDetailsByQuery(TEST_QUERY, null));
    }

    @Test
    @DisplayName("fetchMailDetailsByQuery: Auth Exception - Not Invoke As User")
    void testFetchMailDetailsByQuery_AuthNotInvokeAsUser() {
        when(account.searchEmails(TEST_QUERY)).thenThrow(new MustAuthorizeException("Auth"));
        when(requestContext.invokeAsUser()).thenReturn(false);
        assertEquals(ExtensionResponse.Result.FAILURE,
                messagingArea.fetchMailDetailsByQuery(TEST_QUERY, null).getResult());
    }

    @Test
    @DisplayName("fetchMailDetailsByQuery: System Exception")
    void testFetchMailDetailsByQuery_SystemException() {
        when(account.searchEmails(TEST_QUERY)).thenThrow(new RuntimeException("Error"));
        ExtensionResponse resp = messagingArea.fetchMailDetailsByQuery(TEST_QUERY, null);
        assertEquals(ExtensionResponse.Result.FAILURE, resp.getResult());
        verify(telemetryHelper).recordError(eq("outlook3.fetchMailDetailsByQuery"), anyLong(), any(Exception.class), anyMap());
    }

    // ========== Send Mail ==========

    @Test
    @DisplayName("sendMail: Success")
    void testSendMail_Success() {
        when(validationOrchestrator.validate(anyMap())).thenReturn(Collections.emptyList());
        ExtensionResponse mockResp = mock(ExtensionResponse.class);
        when(mockResp.getResult()).thenReturn(ExtensionResponse.Result.SUCCESS);
        when(messagingAreaImpl.sendMail(TEST_SUBJECT, TEST_MESSAGE, null, TEST_TO, null, null, null, "HTML"))
                .thenReturn(mockResp);

        ExtensionResponse resp = messagingArea.sendMail(
                TEST_SUBJECT, TEST_MESSAGE, null, TEST_TO, null, null, null, "HTML", null);
        assertEquals(ExtensionResponse.Result.SUCCESS, resp.getResult());
        verify(telemetryHelper).recordSuccess(eq("outlook3.sendMail"), anyLong(), anyMap());
    }

    @Test
    @DisplayName("sendMail: Validation - Allow Retry True")
    void testSendMail_ValidationRetryTrue() {
        when(validationOrchestrator.validate(anyMap())).thenReturn(validationResults());
        when(responseGenerator.generateConfirmationResponse(any(), anyList(), anyString(), anyMap()))
                .thenReturn(mock(ExtensionResponse.class));

        assertNotNull(messagingArea.sendMail(TEST_SUBJECT, TEST_MESSAGE, null, TEST_TO, null, null, null, "HTML", true));
        verify(internalStateManager).put(anyString(), anyString());
        verify(telemetryHelper).recordRetryPrompted(eq("outlook3.sendMail"), anyLong(), anyMap());
    }

    @Test
    @DisplayName("sendMail: Validation - Allow Retry False")
    void testSendMail_ValidationRetryFalse() {
        when(validationOrchestrator.validate(anyMap())).thenReturn(validationResults());
        when(responseGenerator.generateFetchDenyResponse(any(), anyList(), any(), anyMap()))
                .thenReturn(mock(ExtensionResponse.class));

        assertNotNull(messagingArea.sendMail(TEST_SUBJECT, TEST_MESSAGE, null, TEST_TO, null, null, null, "HTML", false));
        verify(telemetryHelper).recordValidationError(eq("outlook3.sendMail"), anyLong(), anyString(), anyMap());
    }

    @Test
    @DisplayName("sendMail: Auth Exception - Invoke As User")
    void testSendMail_AuthInvokeAsUser() {
        when(validationOrchestrator.validate(anyMap())).thenThrow(new MustAuthorizeException("Auth"));
        when(requestContext.invokeAsUser()).thenReturn(true);
        assertThrows(MustAuthorizeException.class, () ->
                messagingArea.sendMail(TEST_SUBJECT, TEST_MESSAGE, null, TEST_TO, null, null, null, "HTML", null));
    }

    @Test
    @DisplayName("sendMail: Auth Exception - Not Invoke As User")
    void testSendMail_AuthNotInvokeAsUser() {
        when(validationOrchestrator.validate(anyMap())).thenThrow(new MustAuthorizeException("Auth"));
        when(requestContext.invokeAsUser()).thenReturn(false);
        assertEquals(ExtensionResponse.Result.FAILURE,
                messagingArea.sendMail(TEST_SUBJECT, TEST_MESSAGE, null, TEST_TO, null, null, null, "HTML", null).getResult());
    }

    @Test
    @DisplayName("sendMail: System Exception")
    void testSendMail_SystemException() {
        when(validationOrchestrator.validate(anyMap())).thenThrow(new RuntimeException("Error"));
        assertEquals(ExtensionResponse.Result.FAILURE,
                messagingArea.sendMail(TEST_SUBJECT, TEST_MESSAGE, null, TEST_TO, null, null, null, "HTML", null).getResult());
        verify(telemetryHelper).recordError(eq("outlook3.sendMail"), anyLong(), any(Exception.class), anyMap());
    }

    // ========== Send Mail With Table ==========

    @Test
    @DisplayName("sendMailWithTable: Success")
    void testSendMailWithTable_Success() {
        when(validationOrchestrator.validate(anyMap())).thenReturn(Collections.emptyList());
        List<EntityValue> entityList = List.of(mock(EntityValue.class));
        ExtensionResponse mockResp = mock(ExtensionResponse.class);
        when(mockResp.getResult()).thenReturn(ExtensionResponse.Result.SUCCESS);
        when(messagingAreaImpl.sendMailWithTable(eq(TEST_SUBJECT), eq(TEST_MESSAGE), isNull(),
                eq(TEST_TO), isNull(), isNull(), isNull(), anyList(), isNull()))
                .thenReturn(mockResp);

        ExtensionResponse resp = messagingArea.sendMailWithTable(
                TEST_SUBJECT, TEST_MESSAGE, null, TEST_TO, null, null, null, entityList, null, null);
        assertEquals(ExtensionResponse.Result.SUCCESS, resp.getResult());
        verify(telemetryHelper).recordSuccess(eq("outlook3.sendMailWithTable"), anyLong(), anyMap());
    }

    @Test
    @DisplayName("sendMailWithTable: Validation - Allow Retry True")
    void testSendMailWithTable_ValidationRetryTrue() {
        when(validationOrchestrator.validate(anyMap())).thenReturn(validationResults());
        when(responseGenerator.generateConfirmationResponse(any(), anyList(), anyString(), anyMap()))
                .thenReturn(mock(ExtensionResponse.class));
        List<EntityValue> entityList = List.of(mock(EntityValue.class));

        assertNotNull(messagingArea.sendMailWithTable(
                TEST_SUBJECT, TEST_MESSAGE, null, TEST_TO, null, null, null, entityList, null, true));
        verify(internalStateManager).put(anyString(), anyString());
        verify(internalStateManager).putMetaInfo(anyString(), anyMap());
        verify(telemetryHelper).recordRetryPrompted(eq("outlook3.sendMailWithTable"), anyLong(), anyMap());
    }

    @Test
    @DisplayName("sendMailWithTable: Validation - Allow Retry False")
    void testSendMailWithTable_ValidationRetryFalse() {
        when(validationOrchestrator.validate(anyMap())).thenReturn(validationResults());
        when(responseGenerator.generateFetchDenyResponse(any(), anyList(), any(), anyMap()))
                .thenReturn(mock(ExtensionResponse.class));

        assertNotNull(messagingArea.sendMailWithTable(
                TEST_SUBJECT, TEST_MESSAGE, null, TEST_TO, null, null, null,
                List.of(mock(EntityValue.class)), null, false));
        verify(telemetryHelper).recordValidationError(eq("outlook3.sendMailWithTable"), anyLong(), anyString(), anyMap());
    }

    @Test
    @DisplayName("sendMailWithTable: Auth Exception - Invoke As User")
    void testSendMailWithTable_AuthInvokeAsUser() {
        when(validationOrchestrator.validate(anyMap())).thenThrow(new MustAuthorizeException("Auth"));
        when(requestContext.invokeAsUser()).thenReturn(true);
        assertThrows(MustAuthorizeException.class, () ->
                messagingArea.sendMailWithTable(TEST_SUBJECT, TEST_MESSAGE, null, TEST_TO, null, null, null,
                        List.of(mock(EntityValue.class)), null, null));
    }

    @Test
    @DisplayName("sendMailWithTable: Auth Exception - Not Invoke As User")
    void testSendMailWithTable_AuthNotInvokeAsUser() {
        when(validationOrchestrator.validate(anyMap())).thenThrow(new MustAuthorizeException("Auth"));
        when(requestContext.invokeAsUser()).thenReturn(false);
        assertEquals(ExtensionResponse.Result.FAILURE,
                messagingArea.sendMailWithTable(TEST_SUBJECT, TEST_MESSAGE, null, TEST_TO, null, null, null,
                        List.of(mock(EntityValue.class)), null, null).getResult());
    }

    @Test
    @DisplayName("sendMailWithTable: System Exception")
    void testSendMailWithTable_SystemException() {
        when(validationOrchestrator.validate(anyMap())).thenThrow(new RuntimeException("Error"));
        assertEquals(ExtensionResponse.Result.FAILURE,
                messagingArea.sendMailWithTable(TEST_SUBJECT, TEST_MESSAGE, null, TEST_TO, null, null, null,
                        List.of(mock(EntityValue.class)), null, null).getResult());
        verify(telemetryHelper).recordError(eq("outlook3.sendMailWithTable"), anyLong(), any(Exception.class), anyMap());
    }

    // ========== Fetch Inbox ==========

    @Test
    @DisplayName("fetchInbox: Success - Null params (empty validation map)")
    void testFetchInbox_Success_NullParams() {
        when(account.getInboxFolder(null, null)).thenReturn(folder);
        when(folder.getEmails((Double) null, (Double) null)).thenReturn(List.of(email));
        when(mailHandler.fromEmail(email, null)).thenReturn(createMockMailDetails());

        ExtensionResponse resp = messagingArea.fetchInbox(null, null, null);
        assertEquals(ExtensionResponse.Result.SUCCESS, resp.getResult());
        assertTrue(resp.getResponseValue().containsKey("Inbox Mails"));
        verify(telemetryHelper).recordSuccess(eq("outlook3.fetchInbox"), anyLong(), anyMap());
    }

    @Test
    @DisplayName("fetchInbox: Success - Non-empty validation map, validation passes")
    void testFetchInbox_ValidationPasses() {
        when(validationOrchestrator.validate(anyMap())).thenReturn(Collections.emptyList());
        when(account.getInboxFolder(null, null)).thenReturn(folder);
        when(folder.getEmails(-1.0, 10.0)).thenReturn(List.of(email));
        when(mailHandler.fromEmail(email, null)).thenReturn(createMockMailDetails());

        ExtensionResponse resp = messagingArea.fetchInbox(-1.0, 10.0, null);
        assertEquals(ExtensionResponse.Result.SUCCESS, resp.getResult());
    }

    @Test
    @DisplayName("fetchInbox: Validation Fails - Allow Retry True")
    void testFetchInbox_ValidationRetryTrue() {
        when(validationOrchestrator.validate(anyMap())).thenReturn(validationResults());
        when(responseGenerator.generateConfirmationResponse(any(), anyList(), anyString(), anyMap()))
                .thenReturn(mock(ExtensionResponse.class));

        assertNotNull(messagingArea.fetchInbox(-1.0, 10.0, true));
        verify(internalStateManager).put(anyString(), anyString());
        verify(telemetryHelper).recordRetryPrompted(eq("outlook3.fetchInbox"), anyLong(), anyMap());
    }

    @Test
    @DisplayName("fetchInbox: Validation Fails - Allow Retry False")
    void testFetchInbox_ValidationRetryFalse() {
        when(validationOrchestrator.validate(anyMap())).thenReturn(validationResults());
        when(responseGenerator.generateFetchDenyResponse(any(), anyList(), any(), anyMap()))
                .thenReturn(mock(ExtensionResponse.class));

        assertNotNull(messagingArea.fetchInbox(-1.0, 10.0, false));
        verify(telemetryHelper).recordValidationError(eq("outlook3.fetchInbox"), anyLong(), anyString(), anyMap());
    }

    @Test
    @DisplayName("fetchInbox: Auth Exception - Invoke As User")
    void testFetchInbox_AuthInvokeAsUser() {
        when(account.getInboxFolder(null, null)).thenThrow(new MustAuthorizeException("Auth"));
        when(requestContext.invokeAsUser()).thenReturn(true);
        assertThrows(MustAuthorizeException.class, () -> messagingArea.fetchInbox(null, null, null));
    }

    @Test
    @DisplayName("fetchInbox: Auth Exception - Not Invoke As User")
    void testFetchInbox_AuthNotInvokeAsUser() {
        when(account.getInboxFolder(null, null)).thenThrow(new MustAuthorizeException("Auth"));
        when(requestContext.invokeAsUser()).thenReturn(false);
        assertEquals(ExtensionResponse.Result.FAILURE, messagingArea.fetchInbox(null, null, null).getResult());
    }

    @Test
    @DisplayName("fetchInbox: System Exception")
    void testFetchInbox_SystemException() {
        when(account.getInboxFolder(null, null)).thenThrow(new RuntimeException("Error"));
        assertEquals(ExtensionResponse.Result.FAILURE, messagingArea.fetchInbox(null, null, null).getResult());
        verify(telemetryHelper).recordError(eq("outlook3.fetchInbox"), anyLong(), any(Exception.class), anyMap());
    }

    // ========== Fetch Inbox With Preferences ==========

    @Test
    @DisplayName("fetchInboxWithPreferences: Success - Null preference defaults to Html")
    void testFetchInboxWithPreferences_NullPreference() {
        when(account.getInboxFolder(null, null)).thenReturn(folder);
        when(folder.getEmails(isNull(), isNull(), anyMap())).thenReturn(List.of(email));
        when(mailHandler.fromEmail(email, null)).thenReturn(createMockMailDetails());

        ExtensionResponse resp = messagingArea.fetchInboxWithPreferences(null, null, null, null);
        assertEquals(ExtensionResponse.Result.SUCCESS, resp.getResult());
        assertTrue(resp.getResponseValue().containsKey("Mails"));
    }

    @Test
    @DisplayName("fetchInboxWithPreferences: Success - Empty preference defaults to Html")
    void testFetchInboxWithPreferences_EmptyPreference() {
        when(account.getInboxFolder(null, null)).thenReturn(folder);
        when(folder.getEmails(isNull(), isNull(), anyMap())).thenReturn(List.of(email));
        when(mailHandler.fromEmail(email, null)).thenReturn(createMockMailDetails());

        ExtensionResponse resp = messagingArea.fetchInboxWithPreferences(null, null, new HashMap<>(), null);
        assertEquals(ExtensionResponse.Result.SUCCESS, resp.getResult());
    }

    @Test
    @DisplayName("fetchInboxWithPreferences: Success - Custom preference")
    void testFetchInboxWithPreferences_CustomPreference() {
        Map<String, Object> pref = new HashMap<>();
        pref.put("Mail Body", "Text");
        when(account.getInboxFolder(null, null)).thenReturn(folder);
        when(folder.getEmails(isNull(), isNull(), anyMap())).thenReturn(List.of(email));
        when(mailHandler.fromEmail(email, null)).thenReturn(createMockMailDetails());

        ExtensionResponse resp = messagingArea.fetchInboxWithPreferences(null, null, pref, null);
        assertEquals(ExtensionResponse.Result.SUCCESS, resp.getResult());
    }

    @Test
    @DisplayName("fetchInboxWithPreferences: Validation passes after non-empty map")
    void testFetchInboxWithPreferences_ValidationPasses() {
        when(validationOrchestrator.validate(anyMap())).thenReturn(Collections.emptyList());
        when(account.getInboxFolder(null, null)).thenReturn(folder);
        when(folder.getEmails(eq(-1.0), eq(10.0), anyMap())).thenReturn(List.of(email));
        when(mailHandler.fromEmail(email, null)).thenReturn(createMockMailDetails());

        ExtensionResponse resp = messagingArea.fetchInboxWithPreferences(-1.0, 10.0, null, null);
        assertEquals(ExtensionResponse.Result.SUCCESS, resp.getResult());
    }

    @Test
    @DisplayName("fetchInboxWithPreferences: Validation Fails - Allow Retry True")
    void testFetchInboxWithPreferences_ValidationRetryTrue() {
        when(validationOrchestrator.validate(anyMap())).thenReturn(validationResults());
        when(responseGenerator.generateConfirmationResponse(any(), anyList(), anyString(), anyMap()))
                .thenReturn(mock(ExtensionResponse.class));

        assertNotNull(messagingArea.fetchInboxWithPreferences(-1.0, 10.0, null, true));
        verify(internalStateManager).put(anyString(), anyString());
        verify(telemetryHelper).recordRetryPrompted(eq("outlook3.fetchInboxWithPreferences"), anyLong(), anyMap());
    }

    @Test
    @DisplayName("fetchInboxWithPreferences: Validation Fails - Allow Retry False")
    void testFetchInboxWithPreferences_ValidationRetryFalse() {
        when(validationOrchestrator.validate(anyMap())).thenReturn(validationResults());
        when(responseGenerator.generateFetchDenyResponse(any(), anyList(), any(), anyMap()))
                .thenReturn(mock(ExtensionResponse.class));

        assertNotNull(messagingArea.fetchInboxWithPreferences(-1.0, 10.0, null, false));
        verify(telemetryHelper).recordValidationError(eq("outlook3.fetchInboxWithPreferences"), anyLong(), anyString(), anyMap());
    }

    @Test
    @DisplayName("fetchInboxWithPreferences: Auth Exception - Invoke As User")
    void testFetchInboxWithPreferences_AuthInvokeAsUser() {
        when(account.getInboxFolder(null, null)).thenThrow(new MustAuthorizeException("Auth"));
        when(requestContext.invokeAsUser()).thenReturn(true);
        assertThrows(MustAuthorizeException.class, () ->
                messagingArea.fetchInboxWithPreferences(null, null, null, null));
    }

    @Test
    @DisplayName("fetchInboxWithPreferences: Auth Exception - Not Invoke As User")
    void testFetchInboxWithPreferences_AuthNotInvokeAsUser() {
        when(account.getInboxFolder(null, null)).thenThrow(new MustAuthorizeException("Auth"));
        when(requestContext.invokeAsUser()).thenReturn(false);
        assertEquals(ExtensionResponse.Result.FAILURE,
                messagingArea.fetchInboxWithPreferences(null, null, null, null).getResult());
    }

    @Test
    @DisplayName("fetchInboxWithPreferences: System Exception")
    void testFetchInboxWithPreferences_SystemException() {
        when(account.getInboxFolder(null, null)).thenThrow(new RuntimeException("Error"));
        assertEquals(ExtensionResponse.Result.FAILURE,
                messagingArea.fetchInboxWithPreferences(null, null, null, null).getResult());
        verify(telemetryHelper).recordError(eq("outlook3.fetchInboxWithPreferences"), anyLong(), any(Exception.class), anyMap());
    }

    // ========== Reply To Mail With CC and BCC ==========

    @Test
    @DisplayName("replyToMailWithCCAndBCC: Success - with attachments")
    void testReplyToMailWithCCAndBCC_Success() {
        when(validationOrchestrator.validate(anyMap())).thenReturn(Collections.emptyList());
        ExtensionResponse mockResp = mock(ExtensionResponse.class);
        when(mockResp.getResult()).thenReturn(ExtensionResponse.Result.SUCCESS);
        List<File> attachments = List.of(mock(File.class));
        when(messagingAreaImpl.replyToMailWithCCAndBCC(eq(attachments), eq(TEST_MESSAGE_ID),
                eq(TEST_TO), eq(TEST_CC), eq(TEST_BCC), eq(TEST_REPLY_TO),
                eq(TEST_MESSAGE), eq("HTML"))).thenReturn(mockResp);

        ExtensionResponse resp = messagingArea.replyToMailWithCCAndBCC(
                TEST_MESSAGE_ID, TEST_MESSAGE, attachments, TEST_TO, TEST_CC, TEST_BCC, TEST_REPLY_TO, "HTML", null);
        assertEquals(ExtensionResponse.Result.SUCCESS, resp.getResult());
        verify(telemetryHelper).recordSuccess(eq("outlook3.replyToMailWithCCAndBCC"), anyLong(), anyMap());
    }

    @Test
    @DisplayName("replyToMailWithCCAndBCC: Validation - Allow Retry True")
    void testReplyToMailWithCCAndBCC_ValidationRetryTrue() {
        when(validationOrchestrator.validate(anyMap())).thenReturn(validationResults());
        when(responseGenerator.generateConfirmationResponse(any(), anyList(), anyString(), anyMap()))
                .thenReturn(mock(ExtensionResponse.class));

        assertNotNull(messagingArea.replyToMailWithCCAndBCC(
                TEST_MESSAGE_ID, TEST_MESSAGE, null, TEST_TO, TEST_CC, TEST_BCC, TEST_REPLY_TO, "HTML", true));
        verify(internalStateManager).put(anyString(), anyString());
        verify(telemetryHelper).recordRetryPrompted(eq("outlook3.replyToMailWithCCAndBCC"), anyLong(), anyMap());
    }

    @Test
    @DisplayName("replyToMailWithCCAndBCC: Validation - Allow Retry False")
    void testReplyToMailWithCCAndBCC_ValidationRetryFalse() {
        when(validationOrchestrator.validate(anyMap())).thenReturn(validationResults());
        when(responseGenerator.generateFetchDenyResponse(any(), anyList(), any(), anyMap()))
                .thenReturn(mock(ExtensionResponse.class));

        assertNotNull(messagingArea.replyToMailWithCCAndBCC(
                TEST_MESSAGE_ID, TEST_MESSAGE, null, TEST_TO, TEST_CC, TEST_BCC, TEST_REPLY_TO, "HTML", false));
        verify(telemetryHelper).recordValidationError(eq("outlook3.replyToMailWithCCAndBCC"), anyLong(), anyString(), anyMap());
    }

    @Test
    @DisplayName("replyToMailWithCCAndBCC: Auth Exception - Invoke As User")
    void testReplyToMailWithCCAndBCC_AuthInvokeAsUser() {
        when(validationOrchestrator.validate(anyMap())).thenThrow(new MustAuthorizeException("Auth"));
        when(requestContext.invokeAsUser()).thenReturn(true);
        assertThrows(MustAuthorizeException.class, () -> messagingArea.replyToMailWithCCAndBCC(
                TEST_MESSAGE_ID, TEST_MESSAGE, null, TEST_TO, TEST_CC, TEST_BCC, TEST_REPLY_TO, "HTML", null));
    }

    @Test
    @DisplayName("replyToMailWithCCAndBCC: Auth Exception - Not Invoke As User")
    void testReplyToMailWithCCAndBCC_AuthNotInvokeAsUser() {
        when(validationOrchestrator.validate(anyMap())).thenThrow(new MustAuthorizeException("Auth"));
        when(requestContext.invokeAsUser()).thenReturn(false);
        assertEquals(ExtensionResponse.Result.FAILURE,
                messagingArea.replyToMailWithCCAndBCC(TEST_MESSAGE_ID, TEST_MESSAGE, null,
                        TEST_TO, TEST_CC, TEST_BCC, TEST_REPLY_TO, "HTML", null).getResult());
    }

    @Test
    @DisplayName("replyToMailWithCCAndBCC: System Exception")
    void testReplyToMailWithCCAndBCC_SystemException() {
        when(validationOrchestrator.validate(anyMap())).thenThrow(new RuntimeException("Error"));
        assertEquals(ExtensionResponse.Result.FAILURE,
                messagingArea.replyToMailWithCCAndBCC(TEST_MESSAGE_ID, TEST_MESSAGE, null,
                        TEST_TO, TEST_CC, TEST_BCC, TEST_REPLY_TO, "HTML", null).getResult());
        verify(telemetryHelper).recordError(eq("outlook3.replyToMailWithCCAndBCC"), anyLong(), any(Exception.class), anyMap());
    }

    // ========== Reply To Mail ==========

    @Test
    @DisplayName("replyToMail: Success - with attachments")
    void testReplyToMail_Success() {
        when(validationOrchestrator.validate(anyMap())).thenReturn(Collections.emptyList());
        ExtensionResponse mockResp = mock(ExtensionResponse.class);
        when(mockResp.getResult()).thenReturn(ExtensionResponse.Result.SUCCESS);
        List<File> attachments = List.of(mock(File.class));
        when(messagingAreaImpl.replyToMail(eq(attachments), eq(TEST_MESSAGE_ID), eq(TEST_MESSAGE), eq("HTML")))
                .thenReturn(mockResp);

        ExtensionResponse resp = messagingArea.replyToMail(TEST_MESSAGE_ID, TEST_MESSAGE, attachments, "HTML", null);
        assertEquals(ExtensionResponse.Result.SUCCESS, resp.getResult());
        verify(telemetryHelper).recordSuccess(eq("outlook3.replyToMail"), anyLong(), anyMap());
    }

    @Test
    @DisplayName("replyToMail: Validation - Allow Retry True")
    void testReplyToMail_ValidationRetryTrue() {
        when(validationOrchestrator.validate(anyMap())).thenReturn(validationResults());
        when(responseGenerator.generateConfirmationResponse(any(), anyList(), anyString(), anyMap()))
                .thenReturn(mock(ExtensionResponse.class));

        assertNotNull(messagingArea.replyToMail(TEST_MESSAGE_ID, TEST_MESSAGE, null, "HTML", true));
        verify(internalStateManager).put(anyString(), anyString());
        verify(telemetryHelper).recordRetryPrompted(eq("outlook3.replyToMail"), anyLong(), anyMap());
    }

    @Test
    @DisplayName("replyToMail: Validation - Allow Retry False")
    void testReplyToMail_ValidationRetryFalse() {
        when(validationOrchestrator.validate(anyMap())).thenReturn(validationResults());
        when(responseGenerator.generateFetchDenyResponse(any(), anyList(), any(), anyMap()))
                .thenReturn(mock(ExtensionResponse.class));

        assertNotNull(messagingArea.replyToMail(TEST_MESSAGE_ID, TEST_MESSAGE, null, "HTML", false));
        verify(telemetryHelper).recordValidationError(eq("outlook3.replyToMail"), anyLong(), anyString(), anyMap());
    }

    @Test
    @DisplayName("replyToMail: Auth Exception - Invoke As User")
    void testReplyToMail_AuthInvokeAsUser() {
        when(validationOrchestrator.validate(anyMap())).thenThrow(new MustAuthorizeException("Auth"));
        when(requestContext.invokeAsUser()).thenReturn(true);
        assertThrows(MustAuthorizeException.class, () ->
                messagingArea.replyToMail(TEST_MESSAGE_ID, TEST_MESSAGE, null, "HTML", null));
    }

    @Test
    @DisplayName("replyToMail: Auth Exception - Not Invoke As User")
    void testReplyToMail_AuthNotInvokeAsUser() {
        when(validationOrchestrator.validate(anyMap())).thenThrow(new MustAuthorizeException("Auth"));
        when(requestContext.invokeAsUser()).thenReturn(false);
        assertEquals(ExtensionResponse.Result.FAILURE,
                messagingArea.replyToMail(TEST_MESSAGE_ID, TEST_MESSAGE, null, "HTML", null).getResult());
    }

    @Test
    @DisplayName("replyToMail: System Exception")
    void testReplyToMail_SystemException() {
        when(validationOrchestrator.validate(anyMap())).thenThrow(new RuntimeException("Error"));
        assertEquals(ExtensionResponse.Result.FAILURE,
                messagingArea.replyToMail(TEST_MESSAGE_ID, TEST_MESSAGE, null, "HTML", null).getResult());
        verify(telemetryHelper).recordError(eq("outlook3.replyToMail"), anyLong(), any(Exception.class), anyMap());
    }

    // ========== Fetch Mails By Label ==========

    @Test
    @DisplayName("fetchMailsByLabel: Success")
    void testFetchMailsByLabel_Success() {
        when(validationOrchestrator.validate(anyMap())).thenReturn(Collections.emptyList());
        ExtensionResponse mockResp = mock(ExtensionResponse.class);
        when(mockResp.getResult()).thenReturn(ExtensionResponse.Result.SUCCESS);
        when(messagingAreaImpl.fetchMailByLabel("Inbox", 1.0, 10.0)).thenReturn(mockResp);

        ExtensionResponse resp = messagingArea.fetchMailsByLabel("Inbox", 1.0, 10.0, null);
        assertEquals(ExtensionResponse.Result.SUCCESS, resp.getResult());
        verify(telemetryHelper).recordSuccess(eq("outlook3.fetchMailsByLabel"), anyLong(), anyMap());
    }

    @Test
    @DisplayName("fetchMailsByLabel: Validation - Allow Retry True")
    void testFetchMailsByLabel_ValidationRetryTrue() {
        when(validationOrchestrator.validate(anyMap())).thenReturn(validationResults());
        when(responseGenerator.generateConfirmationResponse(any(), anyList(), anyString(), anyMap()))
                .thenReturn(mock(ExtensionResponse.class));

        assertNotNull(messagingArea.fetchMailsByLabel("Inbox", 1.0, 10.0, true));
        verify(internalStateManager).put(anyString(), anyString());
        verify(telemetryHelper).recordRetryPrompted(eq("outlook3.fetchMailsByLabel"), anyLong(), anyMap());
    }

    @Test
    @DisplayName("fetchMailsByLabel: Validation - Allow Retry False")
    void testFetchMailsByLabel_ValidationRetryFalse() {
        when(validationOrchestrator.validate(anyMap())).thenReturn(validationResults());
        when(responseGenerator.generateFetchDenyResponse(any(), anyList(), any(), anyMap()))
                .thenReturn(mock(ExtensionResponse.class));

        assertNotNull(messagingArea.fetchMailsByLabel("Inbox", 1.0, 10.0, false));
        verify(telemetryHelper).recordValidationError(eq("outlook3.fetchMailsByLabel"), anyLong(), anyString(), anyMap());
    }

    @Test
    @DisplayName("fetchMailsByLabel: Auth Exception - Invoke As User")
    void testFetchMailsByLabel_AuthInvokeAsUser() {
        when(validationOrchestrator.validate(anyMap())).thenThrow(new MustAuthorizeException("Auth"));
        when(requestContext.invokeAsUser()).thenReturn(true);
        assertThrows(MustAuthorizeException.class, () -> messagingArea.fetchMailsByLabel("Inbox", 1.0, 10.0, null));
    }

    @Test
    @DisplayName("fetchMailsByLabel: Auth Exception - Not Invoke As User")
    void testFetchMailsByLabel_AuthNotInvokeAsUser() {
        when(validationOrchestrator.validate(anyMap())).thenThrow(new MustAuthorizeException("Auth"));
        when(requestContext.invokeAsUser()).thenReturn(false);
        assertEquals(ExtensionResponse.Result.FAILURE,
                messagingArea.fetchMailsByLabel("Inbox", 1.0, 10.0, null).getResult());
    }

    @Test
    @DisplayName("fetchMailsByLabel: System Exception")
    void testFetchMailsByLabel_SystemException() {
        when(validationOrchestrator.validate(anyMap())).thenThrow(new RuntimeException("Error"));
        assertEquals(ExtensionResponse.Result.FAILURE,
                messagingArea.fetchMailsByLabel("Inbox", 1.0, 10.0, null).getResult());
        verify(telemetryHelper).recordError(eq("outlook3.fetchMailsByLabel"), anyLong(), any(Exception.class), anyMap());
    }

    // ========== Add Category To Message ==========

    @Test
    @DisplayName("addCategoryToMessage: Success")
    void testAddCategoryToMessage_Success() {
        when(validationOrchestrator.validate(anyMap())).thenReturn(Collections.emptyList());
        ExtensionResponse mockResp = mock(ExtensionResponse.class);
        when(mockResp.getResult()).thenReturn(ExtensionResponse.Result.SUCCESS);
        when(messagingAreaImpl.addCategoryToMessage(TEST_MESSAGE_ID, TEST_CATEGORY, true)).thenReturn(mockResp);

        ExtensionResponse resp = messagingArea.addCategoryToMessage(TEST_MESSAGE_ID, TEST_CATEGORY, true, null);
        assertEquals(ExtensionResponse.Result.SUCCESS, resp.getResult());
        verify(telemetryHelper).recordSuccess(eq("outlook3.addCategoryToMessage"), anyLong(), anyMap());
    }

    @Test
    @DisplayName("addCategoryToMessage: Validation - Allow Retry True")
    void testAddCategoryToMessage_ValidationRetryTrue() {
        when(validationOrchestrator.validate(anyMap())).thenReturn(validationResults());
        when(responseGenerator.generateConfirmationResponse(any(), anyList(), anyString(), anyMap()))
                .thenReturn(mock(ExtensionResponse.class));

        assertNotNull(messagingArea.addCategoryToMessage(TEST_MESSAGE_ID, TEST_CATEGORY, true, true));
        verify(internalStateManager).put(anyString(), anyString());
        verify(telemetryHelper).recordRetryPrompted(eq("outlook3.addCategoryToMessage"), anyLong(), anyMap());
    }

    @Test
    @DisplayName("addCategoryToMessage: Validation - Allow Retry False")
    void testAddCategoryToMessage_ValidationRetryFalse() {
        when(validationOrchestrator.validate(anyMap())).thenReturn(validationResults());
        when(responseGenerator.generateFetchDenyResponse(any(), anyList(), any(), anyMap()))
                .thenReturn(mock(ExtensionResponse.class));

        assertNotNull(messagingArea.addCategoryToMessage(TEST_MESSAGE_ID, TEST_CATEGORY, true, false));
        verify(telemetryHelper).recordValidationError(eq("outlook3.addCategoryToMessage"), anyLong(), anyString(), anyMap());
    }

    @Test
    @DisplayName("addCategoryToMessage: Auth Exception - Invoke As User")
    void testAddCategoryToMessage_AuthInvokeAsUser() {
        when(validationOrchestrator.validate(anyMap())).thenThrow(new MustAuthorizeException("Auth"));
        when(requestContext.invokeAsUser()).thenReturn(true);
        assertThrows(MustAuthorizeException.class, () ->
                messagingArea.addCategoryToMessage(TEST_MESSAGE_ID, TEST_CATEGORY, true, null));
    }

    @Test
    @DisplayName("addCategoryToMessage: Auth Exception - Not Invoke As User")
    void testAddCategoryToMessage_AuthNotInvokeAsUser() {
        when(validationOrchestrator.validate(anyMap())).thenThrow(new MustAuthorizeException("Auth"));
        when(requestContext.invokeAsUser()).thenReturn(false);
        assertEquals(ExtensionResponse.Result.FAILURE,
                messagingArea.addCategoryToMessage(TEST_MESSAGE_ID, TEST_CATEGORY, true, null).getResult());
    }

    @Test
    @DisplayName("addCategoryToMessage: System Exception")
    void testAddCategoryToMessage_SystemException() {
        when(validationOrchestrator.validate(anyMap())).thenThrow(new RuntimeException("Error"));
        assertEquals(ExtensionResponse.Result.FAILURE,
                messagingArea.addCategoryToMessage(TEST_MESSAGE_ID, TEST_CATEGORY, true, null).getResult());
        verify(telemetryHelper).recordError(eq("outlook3.addCategoryToMessage"), anyLong(), any(Exception.class), anyMap());
    }

    // ========== Remove Category From Message ==========

    @Test
    @DisplayName("removeCategoryFromMessage: Success")
    void testRemoveCategoryFromMessage_Success() {
        when(validationOrchestrator.validate(anyMap())).thenReturn(Collections.emptyList());
        ExtensionResponse mockResp = mock(ExtensionResponse.class);
        when(mockResp.getResult()).thenReturn(ExtensionResponse.Result.SUCCESS);
        when(messagingAreaImpl.removeCategoryFromMessage(TEST_MESSAGE_ID, TEST_CATEGORY)).thenReturn(mockResp);

        ExtensionResponse resp = messagingArea.removeCategoryFromMessage(TEST_MESSAGE_ID, TEST_CATEGORY, null);
        assertEquals(ExtensionResponse.Result.SUCCESS, resp.getResult());
        verify(telemetryHelper).recordSuccess(eq("outlook3.removeCategoryFromMessage"), anyLong(), anyMap());
    }

    @Test
    @DisplayName("removeCategoryFromMessage: Validation - Allow Retry True")
    void testRemoveCategoryFromMessage_ValidationRetryTrue() {
        when(validationOrchestrator.validate(anyMap())).thenReturn(validationResults());
        when(responseGenerator.generateConfirmationResponse(any(), anyList(), anyString(), anyMap()))
                .thenReturn(mock(ExtensionResponse.class));

        assertNotNull(messagingArea.removeCategoryFromMessage(TEST_MESSAGE_ID, TEST_CATEGORY, true));
        verify(internalStateManager).put(anyString(), anyString());
        verify(telemetryHelper).recordRetryPrompted(eq("outlook3.removeCategoryFromMessage"), anyLong(), anyMap());
    }

    @Test
    @DisplayName("removeCategoryFromMessage: Validation - Allow Retry False")
    void testRemoveCategoryFromMessage_ValidationRetryFalse() {
        when(validationOrchestrator.validate(anyMap())).thenReturn(validationResults());
        when(responseGenerator.generateFetchDenyResponse(any(), anyList(), any(), anyMap()))
                .thenReturn(mock(ExtensionResponse.class));

        assertNotNull(messagingArea.removeCategoryFromMessage(TEST_MESSAGE_ID, TEST_CATEGORY, false));
        verify(telemetryHelper).recordValidationError(eq("outlook3.removeCategoryFromMessage"), anyLong(), anyString(), anyMap());
    }

    @Test
    @DisplayName("removeCategoryFromMessage: Auth Exception - Invoke As User")
    void testRemoveCategoryFromMessage_AuthInvokeAsUser() {
        when(validationOrchestrator.validate(anyMap())).thenThrow(new MustAuthorizeException("Auth"));
        when(requestContext.invokeAsUser()).thenReturn(true);
        assertThrows(MustAuthorizeException.class, () ->
                messagingArea.removeCategoryFromMessage(TEST_MESSAGE_ID, TEST_CATEGORY, null));
    }

    @Test
    @DisplayName("removeCategoryFromMessage: Auth Exception - Not Invoke As User")
    void testRemoveCategoryFromMessage_AuthNotInvokeAsUser() {
        when(validationOrchestrator.validate(anyMap())).thenThrow(new MustAuthorizeException("Auth"));
        when(requestContext.invokeAsUser()).thenReturn(false);
        assertEquals(ExtensionResponse.Result.FAILURE,
                messagingArea.removeCategoryFromMessage(TEST_MESSAGE_ID, TEST_CATEGORY, null).getResult());
    }

    @Test
    @DisplayName("removeCategoryFromMessage: System Exception")
    void testRemoveCategoryFromMessage_SystemException() {
        when(validationOrchestrator.validate(anyMap())).thenThrow(new RuntimeException("Error"));
        assertEquals(ExtensionResponse.Result.FAILURE,
                messagingArea.removeCategoryFromMessage(TEST_MESSAGE_ID, TEST_CATEGORY, null).getResult());
        verify(telemetryHelper).recordError(eq("outlook3.removeCategoryFromMessage"), anyLong(), any(Exception.class), anyMap());
    }

    // ========== Mark Message Category And Status ==========

    @Test
    @DisplayName("markMessageCategoryAndStatus: Success")
    void testMarkMessageCategoryAndStatus_Success() {
        when(validationOrchestrator.validate(anyMap())).thenReturn(Collections.emptyList());
        ExtensionResponse mockResp = mock(ExtensionResponse.class);
        when(mockResp.getResult()).thenReturn(ExtensionResponse.Result.SUCCESS);
        when(messagingAreaImpl.markMessageCategoryAndStatus(TEST_MESSAGE_ID, "Read", TEST_CATEGORY))
                .thenReturn(mockResp);

        ExtensionResponse resp = messagingArea.markMessageCategoryAndStatus(
                TEST_MESSAGE_ID, "Read", TEST_CATEGORY, null);
        assertEquals(ExtensionResponse.Result.SUCCESS, resp.getResult());
        verify(telemetryHelper).recordSuccess(eq("mark_message_category_and_status"), anyLong(), anyMap());
    }

    @Test
    @DisplayName("markMessageCategoryAndStatus: Validation - Allow Retry True")
    void testMarkMessageCategoryAndStatus_ValidationRetryTrue() {
        when(validationOrchestrator.validate(anyMap())).thenReturn(validationResults());
        when(responseGenerator.generateConfirmationResponse(any(), anyList(), anyString(), anyMap()))
                .thenReturn(mock(ExtensionResponse.class));

        assertNotNull(messagingArea.markMessageCategoryAndStatus(
                TEST_MESSAGE_ID, "Read", TEST_CATEGORY, true));
        verify(internalStateManager).put(anyString(), anyString());
        verify(telemetryHelper).recordRetryPrompted(eq("mark_message_category_and_status"), anyLong(), anyMap());
    }

    @Test
    @DisplayName("markMessageCategoryAndStatus: Validation - Allow Retry False")
    void testMarkMessageCategoryAndStatus_ValidationRetryFalse() {
        when(validationOrchestrator.validate(anyMap())).thenReturn(validationResults());
        when(responseGenerator.generateFetchDenyResponse(any(), anyList(), any(), anyMap()))
                .thenReturn(mock(ExtensionResponse.class));

        assertNotNull(messagingArea.markMessageCategoryAndStatus(
                TEST_MESSAGE_ID, "Read", TEST_CATEGORY, false));
        verify(telemetryHelper).recordValidationError(eq("mark_message_category_and_status"), anyLong(), anyString(), anyMap());
    }

    @Test
    @DisplayName("markMessageCategoryAndStatus: Auth Exception - Invoke As User")
    void testMarkMessageCategoryAndStatus_AuthInvokeAsUser() {
        when(validationOrchestrator.validate(anyMap())).thenThrow(new MustAuthorizeException("Auth"));
        when(requestContext.invokeAsUser()).thenReturn(true);
        assertThrows(MustAuthorizeException.class, () ->
                messagingArea.markMessageCategoryAndStatus(TEST_MESSAGE_ID, "Read", TEST_CATEGORY, null));
    }

    @Test
    @DisplayName("markMessageCategoryAndStatus: Auth Exception - Not Invoke As User")
    void testMarkMessageCategoryAndStatus_AuthNotInvokeAsUser() {
        when(validationOrchestrator.validate(anyMap())).thenThrow(new MustAuthorizeException("Auth"));
        when(requestContext.invokeAsUser()).thenReturn(false);
        assertEquals(ExtensionResponse.Result.FAILURE,
                messagingArea.markMessageCategoryAndStatus(TEST_MESSAGE_ID, "Read", TEST_CATEGORY, null).getResult());
    }

    @Test
    @DisplayName("markMessageCategoryAndStatus: System Exception")
    void testMarkMessageCategoryAndStatus_SystemException() {
        when(validationOrchestrator.validate(anyMap())).thenThrow(new RuntimeException("Error"));
        assertEquals(ExtensionResponse.Result.FAILURE,
                messagingArea.markMessageCategoryAndStatus(TEST_MESSAGE_ID, "Read", TEST_CATEGORY, null).getResult());
        verify(telemetryHelper).recordError(eq("mark_message_category_and_status"), anyLong(), any(Exception.class), anyMap());
    }

    // ========== Fetch Latest Mail ==========

    @Test
    @DisplayName("fetchLatestMail: Success")
    void testFetchLatestMail_Success() {
        MailDetails md = createMockMailDetails();
        when(account.getInboxFolder(null, null)).thenReturn(folder);
        when(folder.getEmails(1.0, 1.0)).thenReturn(List.of(email));
        when(mailHandler.fromEmail(email, null)).thenReturn(md);

        ExtensionResponse resp = messagingArea.fetchLatestMail();
        assertEquals(ExtensionResponse.Result.SUCCESS, resp.getResult());
        assertEquals(md, resp.getResponseValue().get("New Email"));
        verify(telemetryHelper).recordSuccess(eq("outlook3.fetchLatestMail"), anyLong(), anyMap());
    }

    @Test
    @DisplayName("fetchLatestMail: Empty inbox returns error")
    void testFetchLatestMail_EmptyInbox() {
        when(account.getInboxFolder(null, null)).thenReturn(folder);
        when(folder.getEmails(1.0, 1.0)).thenReturn(Collections.emptyList());

        ExtensionResponse resp = messagingArea.fetchLatestMail();
        assertEquals(ExtensionResponse.Result.FAILURE, resp.getResult());
    }

    @Test
    @DisplayName("fetchLatestMail: Auth Exception - Invoke As User")
    void testFetchLatestMail_AuthInvokeAsUser() {
        when(account.getInboxFolder(null, null)).thenThrow(new MustAuthorizeException("Auth"));
        when(requestContext.invokeAsUser()).thenReturn(true);
        assertThrows(MustAuthorizeException.class, () -> messagingArea.fetchLatestMail());
    }

    @Test
    @DisplayName("fetchLatestMail: System Exception")
    void testFetchLatestMail_SystemException() {
        when(account.getInboxFolder(null, null)).thenThrow(new RuntimeException("Error"));
        assertEquals(ExtensionResponse.Result.FAILURE, messagingArea.fetchLatestMail().getResult());
        verify(telemetryHelper).recordError(eq("outlook3.fetchLatestMail"), anyLong(), any(Exception.class), anyMap());
    }

    // ========== List Categories ==========

    @Test
    @DisplayName("listCategories: Success")
    void testListCategories_Success() {
        when(account.getCategoryNames()).thenReturn(List.of("Blue", "Red", "Green"));

        ExtensionResponse resp = messagingArea.listCategories();
        assertEquals(ExtensionResponse.Result.SUCCESS, resp.getResult());
        @SuppressWarnings("unchecked")
        List<String> categories = (List<String>) resp.getResponseValue().get("Category Names");
        assertEquals(3, categories.size());
        verify(telemetryHelper).recordSuccess(eq("outlook3.listCategories"), anyLong(), anyMap());
    }

    @Test
    @DisplayName("listCategories: Auth Exception - Invoke As User")
    void testListCategories_AuthInvokeAsUser() {
        when(account.getCategoryNames()).thenThrow(new MustAuthorizeException("Auth"));
        when(requestContext.invokeAsUser()).thenReturn(true);
        assertThrows(MustAuthorizeException.class, () -> messagingArea.listCategories());
    }

    @Test
    @DisplayName("listCategories: Auth Exception - Not Invoke As User")
    void testListCategories_AuthNotInvokeAsUser() {
        when(account.getCategoryNames()).thenThrow(new MustAuthorizeException("Auth"));
        when(requestContext.invokeAsUser()).thenReturn(false);
        assertEquals(ExtensionResponse.Result.FAILURE, messagingArea.listCategories().getResult());
    }

    @Test
    @DisplayName("listCategories: System Exception")
    void testListCategories_SystemException() {
        when(account.getCategoryNames()).thenThrow(new RuntimeException("Error"));
        assertEquals(ExtensionResponse.Result.FAILURE, messagingArea.listCategories().getResult());
        verify(telemetryHelper).recordError(eq("outlook3.listCategories"), anyLong(), any(Exception.class), anyMap());
    }

    // ========== Get Notification Delta ==========

    @Test
    @DisplayName("getNotificationDelta: Success")
    void testGetNotificationDelta_Success() {
        ExtensionResponse mockResp = mock(ExtensionResponse.class);
        when(mockResp.getResult()).thenReturn(ExtensionResponse.Result.SUCCESS);
        when(messagingAreaImpl.fetchNotificationDelta()).thenReturn(mockResp);

        ExtensionResponse resp = messagingArea.getNotificationDelta();
        assertEquals(ExtensionResponse.Result.SUCCESS, resp.getResult());
        verify(telemetryHelper).recordSuccess(eq("outlook3.getNotificationDelta"), anyLong(), anyMap());
    }

    @Test
    @DisplayName("getNotificationDelta: Auth Exception - Invoke As User")
    void testGetNotificationDelta_AuthInvokeAsUser() {
        when(messagingAreaImpl.fetchNotificationDelta()).thenThrow(new MustAuthorizeException("Auth"));
        when(requestContext.invokeAsUser()).thenReturn(true);
        assertThrows(MustAuthorizeException.class, () -> messagingArea.getNotificationDelta());
    }

    @Test
    @DisplayName("getNotificationDelta: Auth Exception - Not Invoke As User")
    void testGetNotificationDelta_AuthNotInvokeAsUser() {
        when(messagingAreaImpl.fetchNotificationDelta()).thenThrow(new MustAuthorizeException("Auth"));
        when(requestContext.invokeAsUser()).thenReturn(false);
        assertEquals(ExtensionResponse.Result.FAILURE, messagingArea.getNotificationDelta().getResult());
    }

    @Test
    @DisplayName("getNotificationDelta: System Exception returns SUCCESS with empty list")
    void testGetNotificationDelta_SystemException() {
        when(messagingAreaImpl.fetchNotificationDelta()).thenThrow(new RuntimeException("Error"));
        ExtensionResponse resp = messagingArea.getNotificationDelta();
        assertEquals(ExtensionResponse.Result.SUCCESS, resp.getResult());
        assertTrue(((List<?>) resp.getResponseValue().get("Message Ids")).isEmpty());
    }

    // ========== Mail Received Alert ==========

    @Test
    @DisplayName("mailReceivedAlert: Success")
    void testMailReceivedAlert_Success() {
        FreeForm eventData = mock(FreeForm.class);
        MailDetails md = createMockMailDetails();
        when(eventData.get(Constants.MESSAGE_ID)).thenReturn(TEST_MESSAGE_ID);
        when(account.getEmailWithRetry(TEST_MESSAGE_ID)).thenReturn(email);
        when(mailHandler.fromEmail(email, null)).thenReturn(md);

        ExtensionResponse resp = messagingArea.mailReceivedAlert(Constants.MAIL_RECEIVED, eventData);
        assertEquals(ExtensionResponse.Result.SUCCESS, resp.getResult());
        assertEquals(md, resp.getResponseValue().get("Mail Details"));
        verify(telemetryHelper).recordSuccess(eq("outlook3.mailReceivedAlert"), anyLong(), anyMap());
    }

    @Test
    @DisplayName("mailReceivedAlert: Null mail details throws ISE")
    void testMailReceivedAlert_NullMailDetails() {
        FreeForm eventData = mock(FreeForm.class);
        when(eventData.get(Constants.MESSAGE_ID)).thenReturn(TEST_MESSAGE_ID);
        when(account.getEmailWithRetry(TEST_MESSAGE_ID)).thenReturn(email);
        when(mailHandler.fromEmail(email, null)).thenReturn(null);

        assertThrows(IllegalStateException.class, () ->
                messagingArea.mailReceivedAlert(Constants.MAIL_RECEIVED, eventData));
    }

    @Test
    @DisplayName("mailReceivedAlert: Invalid event name throws ISE")
    void testMailReceivedAlert_InvalidEventName() {
        FreeForm eventData = mock(FreeForm.class);
        assertThrows(IllegalStateException.class, () ->
                messagingArea.mailReceivedAlert("INVALID_EVENT", eventData));
    }

    @Test
    @DisplayName("mailReceivedAlert: Auth Exception - Invoke As User")
    void testMailReceivedAlert_AuthInvokeAsUser() {
        FreeForm eventData = mock(FreeForm.class);
        when(eventData.get(Constants.MESSAGE_ID)).thenReturn(TEST_MESSAGE_ID);
        when(account.getEmailWithRetry(TEST_MESSAGE_ID)).thenThrow(new MustAuthorizeException("Auth"));
        when(requestContext.invokeAsUser()).thenReturn(true);

        assertThrows(MustAuthorizeException.class, () ->
                messagingArea.mailReceivedAlert(Constants.MAIL_RECEIVED, eventData));
    }

    @Test
    @DisplayName("mailReceivedAlert: Auth Exception - Not Invoke As User")
    void testMailReceivedAlert_AuthNotInvokeAsUser() {
        FreeForm eventData = mock(FreeForm.class);
        when(eventData.get(Constants.MESSAGE_ID)).thenReturn(TEST_MESSAGE_ID);
        when(account.getEmailWithRetry(TEST_MESSAGE_ID)).thenThrow(new MustAuthorizeException("Auth"));
        when(requestContext.invokeAsUser()).thenReturn(false);

        ExtensionResponse resp = messagingArea.mailReceivedAlert(Constants.MAIL_RECEIVED, eventData);
        assertEquals(ExtensionResponse.Result.FAILURE, resp.getResult());
    }

    @Test
    @DisplayName("mailReceivedAlert: System Exception throws ISE")
    void testMailReceivedAlert_SystemException() {
        FreeForm eventData = mock(FreeForm.class);
        when(eventData.get(Constants.MESSAGE_ID)).thenReturn(TEST_MESSAGE_ID);
        when(account.getEmailWithRetry(TEST_MESSAGE_ID)).thenThrow(new RuntimeException("Error"));

        assertThrows(IllegalStateException.class, () ->
                messagingArea.mailReceivedAlert(Constants.MAIL_RECEIVED, eventData));
    }

    // ========== Receive Notification Of Email Change ==========

    @Test
    @DisplayName("receiveNotificationOfEmailChange: Success")
    void testReceiveNotificationOfEmailChange_Success() {
        FreeForm eventData = mock(FreeForm.class);
        when(eventData.get(Constants.MESSAGE_ID)).thenReturn(TEST_MESSAGE_ID);
        when(eventData.get(Constants.FOLDER_NAME)).thenReturn("Inbox");
        when(eventData.get(Constants.CHANGE_TYPE)).thenReturn("created");
        when(eventData.get(Constants.ATTACHMENTS)).thenReturn("true");
        when(eventData.get(Constants.NOTIFICATION_ID)).thenReturn("notif-1");
        when(eventData.get(Constants.SUBSCRIPTION_ID)).thenReturn("sub-1");
        when(eventData.get(Constants.FOLDER_ID)).thenReturn("folder-1");
        when(eventData.get(Constants.SUBJECT)).thenReturn(TEST_SUBJECT);
        when(eventData.get(Constants.BODY)).thenReturn("Body text");
        when(eventData.get(Constants.FROM)).thenReturn("from@example.com");
        when(eventData.get(Constants.TO)).thenReturn(TEST_TO);
        when(eventData.get(Constants.CC)).thenReturn(TEST_CC);
        when(eventData.get(Constants.BCC)).thenReturn(TEST_BCC);

        ExtensionResponse resp = messagingArea.receiveNotificationOfEmailChange(
                Constants.EMAIL_CHANGE_NOTIFICATION, eventData);
        assertEquals(ExtensionResponse.Result.SUCCESS, resp.getResult());
        assertEquals("notif-1", resp.getResponseValue().get("Notification Id"));
        assertEquals(true, resp.getResponseValue().get("Attachments"));
    }

    @Test
    @DisplayName("receiveNotificationOfEmailChange: Attachments false")
    void testReceiveNotificationOfEmailChange_NoAttachments() {
        FreeForm eventData = mock(FreeForm.class);
        when(eventData.get(Constants.MESSAGE_ID)).thenReturn(TEST_MESSAGE_ID);
        when(eventData.get(Constants.FOLDER_NAME)).thenReturn("Inbox");
        when(eventData.get(Constants.CHANGE_TYPE)).thenReturn("created");
        when(eventData.get(Constants.ATTACHMENTS)).thenReturn("false");
        when(eventData.get(Constants.NOTIFICATION_ID)).thenReturn("notif-1");
        when(eventData.get(Constants.SUBSCRIPTION_ID)).thenReturn("sub-1");
        when(eventData.get(Constants.FOLDER_ID)).thenReturn("folder-1");
        when(eventData.get(Constants.SUBJECT)).thenReturn(TEST_SUBJECT);
        when(eventData.get(Constants.BODY)).thenReturn("Body text");
        when(eventData.get(Constants.FROM)).thenReturn("from@example.com");
        when(eventData.get(Constants.TO)).thenReturn(TEST_TO);
        when(eventData.get(Constants.CC)).thenReturn(TEST_CC);
        when(eventData.get(Constants.BCC)).thenReturn(TEST_BCC);

        ExtensionResponse resp = messagingArea.receiveNotificationOfEmailChange(
                Constants.EMAIL_CHANGE_NOTIFICATION, eventData);
        assertEquals(ExtensionResponse.Result.SUCCESS, resp.getResult());
        assertEquals(false, resp.getResponseValue().get("Attachments"));
    }

    @Test
    @DisplayName("receiveNotificationOfEmailChange: Invalid event name throws ISE")
    void testReceiveNotificationOfEmailChange_InvalidEventName() {
        FreeForm eventData = mock(FreeForm.class);
        assertThrows(IllegalStateException.class, () ->
                messagingArea.receiveNotificationOfEmailChange("INVALID", eventData));
    }

    @Test
    @DisplayName("receiveNotificationOfEmailChange: Auth Exception - Invoke As User")
    void testReceiveNotificationOfEmailChange_AuthInvokeAsUser() {
        FreeForm eventData = mock(FreeForm.class);
        when(eventData.get(any())).thenThrow(new MustAuthorizeException("Auth"));
        when(requestContext.invokeAsUser()).thenReturn(true);

        assertThrows(MustAuthorizeException.class, () ->
                messagingArea.receiveNotificationOfEmailChange(Constants.EMAIL_CHANGE_NOTIFICATION, eventData));
    }

    @Test
    @DisplayName("receiveNotificationOfEmailChange: Auth Exception - Not Invoke As User")
    void testReceiveNotificationOfEmailChange_AuthNotInvokeAsUser() {
        FreeForm eventData = mock(FreeForm.class);
        when(eventData.get(any())).thenThrow(new MustAuthorizeException("Auth"));
        when(requestContext.invokeAsUser()).thenReturn(false);

        ExtensionResponse resp = messagingArea.receiveNotificationOfEmailChange(
                Constants.EMAIL_CHANGE_NOTIFICATION, eventData);
        assertEquals(ExtensionResponse.Result.FAILURE, resp.getResult());
    }

    // ========== Get Result ==========

    @Test
    @DisplayName("getResult: Success - matching eventName and taskID")
    void testGetResult_Success() {
        FreeForm eventData = mock(FreeForm.class);
        List<MailDetails> mailList = List.of(createMockMailDetails());
        when(eventData.get(Constants.DATA)).thenReturn(mailList);

        ExtensionResponse resp = messagingArea.getResult("task-123", eventData, "task-123");
        assertEquals(ExtensionResponse.Result.SUCCESS, resp.getResult());
        assertEquals(mailList, resp.getResponseValue().get("Mail Details"));
    }

    @Test
    @DisplayName("getResult: Mismatched eventName throws ISE")
    void testGetResult_MismatchedEventName() {
        FreeForm eventData = mock(FreeForm.class);
        assertThrows(IllegalStateException.class, () ->
                messagingArea.getResult("event-1", eventData, "task-2"));
    }

    // ========== Send Alert Using Notification Delta ==========

    @Test
    @DisplayName("sendAlertUsingNotificationDelta: Success")
    void testSendAlertUsingNotificationDelta_Success() {
        messagingArea.sendAlertUsingNotificationDelta(TEST_MESSAGE_ID);
        verify(eventHandler).handleEvent(eq(Constants.MAIL_RECEIVED), any(FreeForm.class));
        verify(telemetryHelper).recordSuccess(eq("outlook3.sendAlertUsingNotificationDelta"), anyLong(), anyMap());
    }

    @Test
    @DisplayName("sendAlertUsingNotificationDelta: Auth Exception rethrows")
    void testSendAlertUsingNotificationDelta_AuthException() {
        doThrow(new MustAuthorizeException("Auth")).when(eventHandler).handleEvent(anyString(), any(FreeForm.class));
        assertThrows(MustAuthorizeException.class, () ->
                messagingArea.sendAlertUsingNotificationDelta(TEST_MESSAGE_ID));
    }

    @Test
    @DisplayName("sendAlertUsingNotificationDelta: General exception is caught")
    void testSendAlertUsingNotificationDelta_GeneralException() {
        doThrow(new RuntimeException("Error")).when(eventHandler).handleEvent(anyString(), any(FreeForm.class));
        // Should not throw - exception is caught and logged
        messagingArea.sendAlertUsingNotificationDelta(TEST_MESSAGE_ID);
        verify(telemetryHelper).recordError(eq("outlook3.sendAlertUsingNotificationDelta"), anyLong(), any(Exception.class), anyMap());
    }

    // ========== Fetch Inbox Async ==========

    @Test
    @DisplayName("fetchInboxAsync: IllegalStateException is caught and rethrown")
    void testFetchInboxAsync_IllegalStateException() {
        when(requestContext.invokeAsUser()).thenThrow(new IllegalStateException("ISE"));
        assertThrows(IllegalStateException.class, () -> messagingArea.fetchInboxAsync());
        verify(telemetryHelper).recordError(eq("outlook3.fetchInboxAsync"), anyLong(), any(Exception.class), anyMap());
    }

    // ========== Check If Triggered Mail Ids Exist ==========

    @Test
    @DisplayName("checkIfTriggeredMailIdsExist: Returns true when exists")
    void testCheckIfTriggeredMailIdsExist_Exists() {
        try (MockedStatic<OutlookApiResource> mocked = mockStatic(OutlookApiResource.class)) {
            mocked.when(() -> OutlookApiResource.isMessageIdTriggered(TEST_MESSAGE_ID)).thenReturn(true);
            assertTrue(messagingArea.checkIfTriggeredMailIdsExist(TEST_MESSAGE_ID));
        }
    }

    @Test
    @DisplayName("checkIfTriggeredMailIdsExist: Returns false when not exists")
    void testCheckIfTriggeredMailIdsExist_NotExists() {
        try (MockedStatic<OutlookApiResource> mocked = mockStatic(OutlookApiResource.class)) {
            mocked.when(() -> OutlookApiResource.isMessageIdTriggered(TEST_MESSAGE_ID)).thenReturn(false);
            assertFalse(messagingArea.checkIfTriggeredMailIdsExist(TEST_MESSAGE_ID));
        }
    }

    @Test
    @DisplayName("checkIfTriggeredMailIdsExist: Null messageId returns false")
    void testCheckIfTriggeredMailIdsExist_Null() {
        assertFalse(messagingArea.checkIfTriggeredMailIdsExist(null));
    }

    @Test
    @DisplayName("checkIfTriggeredMailIdsExist: Empty messageId returns false")
    void testCheckIfTriggeredMailIdsExist_Empty() {
        assertFalse(messagingArea.checkIfTriggeredMailIdsExist(""));
    }

    @Test
    @DisplayName("checkIfTriggeredMailIdsExist: Whitespace only returns false")
    void testCheckIfTriggeredMailIdsExist_Whitespace() {
        assertFalse(messagingArea.checkIfTriggeredMailIdsExist("   "));
    }

    @Test
    @DisplayName("checkIfTriggeredMailIdsExist: Exception returns false")
    void testCheckIfTriggeredMailIdsExist_Exception() {
        try (MockedStatic<OutlookApiResource> mocked = mockStatic(OutlookApiResource.class)) {
            mocked.when(() -> OutlookApiResource.isMessageIdTriggered(TEST_MESSAGE_ID))
                    .thenThrow(new RuntimeException("Error"));
            assertFalse(messagingArea.checkIfTriggeredMailIdsExist(TEST_MESSAGE_ID));
        }
    }

    // ========== Test Connection ==========

    @Test
    @DisplayName("testConnection: Delegates to service")
    void testTestConnection() {
        ExtensionResponse mockResp = mock(ExtensionResponse.class);
        when(mockResp.getResult()).thenReturn(ExtensionResponse.Result.SUCCESS);
        when(invoker.getInvokerId()).thenReturn("invoker-1");
        when(testConnectionService.testConnection("invoker-1")).thenReturn(mockResp);

        ExtensionResponse resp = messagingArea.testConnection();
        assertEquals(ExtensionResponse.Result.SUCCESS, resp.getResult());
        verify(testConnectionService).testConnection("invoker-1");
    }
}
