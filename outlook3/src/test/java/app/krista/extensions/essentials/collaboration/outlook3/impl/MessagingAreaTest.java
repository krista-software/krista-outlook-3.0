package app.krista.extensions.essentials.collaboration.outlook3.impl;

import app.krista.extensions.essentials.collaboration.outlook3.catalog.MessagingArea;
import app.krista.extensions.essentials.collaboration.outlook3.service.Email;
import app.krista.extensions.util.EventHandler;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.ksdk.context.RequestContext;
import app.krista.ksdk.files.FileRepository;
import app.krista.model.base.File;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class MessagingAreaTest {

    AccountImpl account = mock(AccountImpl.class);
    RequestContext requestContext = mock(RequestContext.class);
    AuthorizationContext authorizationContext = mock(AuthorizationContext.class);
    EventHandler eventHandler = mock(EventHandler.class);
    MailHandler mailHandler = mock(MailHandler.class);
    MessagingArea messagingArea = new MessagingArea(account, requestContext, authorizationContext, eventHandler, mailHandler,null);

    @Test()
    public void testFetchAllLabels() {
        List<String> labels = Arrays.asList("Archive", "Conversation History", "Deleted Items", "Drafts", "Inbox", "Junk Email", "Outbox", "Sent Items");
        doReturn(labels).when(account).getFolderNames();
        List<String> allLabels = messagingArea.fetchAllLabels();
        assertTrue(allLabels.contains("Archive"));
    }

    @Test
    public void testFetchMailByMessageId() {
        String messageID = null;
        assertNull(messagingArea.fetchMailByMessageId(messageID));
        messageID = "";
        assertNull(messagingArea.fetchMailByMessageId(messageID));
        messageID = "  ";
        assertNull(messagingArea.fetchMailByMessageId(messageID));
    }

    @Test
    public void testMoveMessage() {
        String messageID = null;
        String folderName = null;
        assertEquals(messagingArea.moveMessage(messageID, folderName), "Invalid message id");
        messageID = "";
        assertEquals(messagingArea.moveMessage(messageID, folderName), "Invalid message id");
        messageID = "    ";
        assertEquals(messagingArea.moveMessage(messageID, folderName), "Invalid message id");
    }

    @Test
    public void testReplyToAll() {
        String messageID = null;
        String message = null;
        String bodyType = "HTML";
        List<File> attachments = null;
        assertEquals(messagingArea.replyToAll(messageID, message, attachments, bodyType), false);
        messageID = "";
        assertEquals(messagingArea.replyToAll(messageID, message, attachments, bodyType), false);
        messageID = " ";
        assertEquals(messagingArea.replyToAll(messageID, message, attachments, bodyType), false);
        messageID = UUID.randomUUID().toString();
        Email email = mock(EmailImpl.class);
        when(account.getEmail(messageID)).thenReturn(email);
        assertEquals(messagingArea.replyToAll(messageID, message, attachments, bodyType), true);
    }

    @Test
    public void testForwardMail() {
        String messageID = null;
        String to = null;
        String message = null;
        String bodyType = "HTML";
        assertEquals(messagingArea.forwardMail(messageID, to, message, bodyType), false);
        messageID = "";
        assertEquals(messagingArea.forwardMail(messageID, to, message, bodyType), false);
        messageID = "   ";
        assertEquals(messagingArea.forwardMail(messageID, to, message, bodyType), false);
    }

    @Test
    public void testFetchMailDetailsByQuery() {
        String query = null;
        assertEquals(messagingArea.fetchMailDetailsByQuery(query), List.of());
        query = "";
        assertEquals(messagingArea.fetchMailDetailsByQuery(query), List.of());
        query = "   ";
        assertEquals(messagingArea.fetchMailDetailsByQuery(query), List.of());
    }

    @Test
    public void testReplyToMail() {
        String messageID = null;
        String message = null;
        List<File> attachments = null;
        String bodyType = "HTML";
        assertEquals(messagingArea.replyToMail(messageID, message, attachments, bodyType), "Invalid message id");
        messageID = "";
        assertEquals(messagingArea.replyToMail(messageID, message, attachments, bodyType), "Invalid message id");
        messageID = "  ";
        assertEquals(messagingArea.replyToMail(messageID, message, attachments, bodyType), "Invalid message id");
    }

    @Test
    public void testFetchMailsByLabel() {
        String label = "aaaa";
        assertEquals(messagingArea.fetchMailsByLabel(label, 1.0, 1.0), List.of());
    }

}
