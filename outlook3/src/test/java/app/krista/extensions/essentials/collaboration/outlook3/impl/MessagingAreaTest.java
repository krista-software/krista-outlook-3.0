package app.krista.extensions.essentials.collaboration.outlook3.impl;

import app.krista.extension.executor.ExtensionResponse;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.MessagingArea;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.errorhandlers.ErrorHandlingStateManager;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.errorhandlers.ExtensionResponseGenerator;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.validators.ValidationOrchestrator;
import app.krista.extensions.essentials.collaboration.outlook3.service.Account;
import app.krista.extensions.essentials.collaboration.outlook3.service.Email;
import app.krista.extensions.util.EventHandler;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.ksdk.context.RequestContext;
import app.krista.ksdk.entities.Entities;
import app.krista.model.base.File;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class MessagingAreaTest {

    Account account = mock(Account.class);
    RequestContext requestContext = mock(RequestContext.class);
    AuthorizationContext authorizationContext = mock(AuthorizationContext.class);
    EventHandler eventHandler = mock(EventHandler.class);
    MailHandler mailHandler = mock(MailHandler.class);
    MessagingAreaImpl messagingAreaImpl2 = mock(MessagingAreaImpl.class);
    ExtensionResponseGenerator responseGenerator = mock(ExtensionResponseGenerator.class);
    ErrorHandlingStateManager internalStateManager = mock(ErrorHandlingStateManager.class);
    ValidationOrchestrator validationOrchestrator = mock(ValidationOrchestrator.class);
    Entities registry = mock(Entities.class);

    MessagingArea messagingArea = new MessagingArea(account, requestContext, authorizationContext, eventHandler, mailHandler, messagingAreaImpl2, responseGenerator, internalStateManager, validationOrchestrator);
    MessagingAreaImpl messagingAreaImpl = new MessagingAreaImpl(account, mailHandler, registry);

    @Test()
    @SuppressWarnings("unchecked")
    public void testFetchAllLabels() {
        List<String> labels = Arrays.asList("Archive", "Conversation History", "Deleted Items", "Drafts", "Inbox", "Junk Email", "Outbox", "Sent Items");
        doReturn(labels).when(account).getFolderNames();
        ExtensionResponse response1 = messagingArea.fetchAllLabels();
        List<String> allLabels = (List<String>)response1.getResponseValue().get("Labels");
        assertTrue(allLabels.contains("Archive"));
    }


    @Test
    public void testReplyToAll() {
        String messageID = null;
        String message = null;
        String bodyType = "HTML";
        List<File> attachments = null;
        messageID = UUID.randomUUID().toString();
        Email email = mock(EmailImpl.class);
        when(account.getEmail(messageID)).thenReturn(email);
        ExtensionResponse response4 = messagingAreaImpl.replyToAll(attachments, messageID, message, bodyType);
        assertEquals(response4.getResponseValue().get("Is Successful"), true);
    }

    @Test
    public void testForwardMail() {
        String messageID = null;
        String to = null;
        String message = null;
        String bodyType = "HTML";
        ExtensionResponse response1 = messagingAreaImpl.forwardMail(messageID, to, message, bodyType);
        assertEquals(response1.getResponseValue().get("Is Forwarded"), false);
        messageID = "";
        ExtensionResponse response2 = messagingAreaImpl.forwardMail(messageID, to, message, bodyType);
        assertEquals(response2.getResponseValue().get("Is Forwarded"), false);
        messageID = "   ";
        ExtensionResponse response3 = messagingAreaImpl.forwardMail(messageID, to, message, bodyType);
        assertEquals(response3.getResponseValue().get("Is Forwarded"), false);
    }

    @Test
    public void testFetchMailDetailsByQuery() {
        String query = null;
        ExtensionResponse response1 = messagingArea.fetchMailDetailsByQuery(query);
        assertEquals(response1.getResponseValue().get("Mails"), List.of());
        query = "";
        ExtensionResponse response2 = messagingArea.fetchMailDetailsByQuery(query);
        assertEquals(response2.getResponseValue().get("Mails"), List.of());
        query = "   ";
        ExtensionResponse response3 = messagingArea.fetchMailDetailsByQuery(query);
        assertEquals(response3.getResponseValue().get("Mails"), List.of());
    }

    @Test
    public void testReplyToMail() {
        String messageID = null;
        String message = null;
        List<File> attachments = null;
        String bodyType = "HTML";
        ExtensionResponse response1 = messagingAreaImpl.replyToMail(attachments, messageID, message, bodyType);
        assertEquals(response1.getResponseValue().get("Message"), "Invalid message id");
        messageID = "";
        ExtensionResponse response2 = messagingAreaImpl.replyToMail(attachments, messageID, message, bodyType);
        assertEquals(response2.getResponseValue().get("Message"), "Invalid message id");
        messageID = "  ";
        ExtensionResponse response3 = messagingAreaImpl.replyToMail(attachments, messageID, message, bodyType);
        assertEquals(response3.getResponseValue().get("Message"), "Invalid message id");
    }

    @Test
    public void testFetchMailsByLabel() {
        String label = "aaaa";
        ExtensionResponse response = messagingAreaImpl.fetchMailByLabel(label, 1.0, 1.0);
        assertEquals(response.getResponseValue().get("Mails"), List.of());
    }

}
