package app.krista.extensions.essentials.collaboration.outlook3.impl;

import app.krista.extensions.essentials.collaboration.outlook3.catalog.entities.MailDetails;
import app.krista.extensions.essentials.collaboration.outlook3.impl.connectors.KristaMediaClient;
import app.krista.extensions.essentials.collaboration.outlook3.service.Attachment;
import app.krista.extensions.essentials.collaboration.outlook3.service.Email;
import app.krista.extensions.essentials.collaboration.outlook3.service.EmailAddress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MailHandler class.
 * Tests the conversion of Email objects to MailDetails entities,
 * with special focus on the sensitivity field feature.
 */
@DisplayName("Mail Handler Tests")
class MailHandlerTest {

    @Mock
    private KristaMediaClient kristaMediaClient;

    @Mock
    private Email mockEmail;

    @Mock
    private EmailAddress mockSenderAddress;

    @Mock
    private Attachment mockAttachment;

    private MailHandler mailHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mailHandler = new MailHandler(kristaMediaClient);
    }

    @Test
    @DisplayName("Should return null when email is null")
    void testFromEmail_NullEmail() {
        // Act
        MailDetails result = mailHandler.fromEmail(null, false);

        // Assert
        assertNull(result, "MailDetails should be null when email is null");
    }

    @Test
    @DisplayName("Should convert email with Normal sensitivity")
    void testFromEmail_WithNormalSensitivity() {
        // Arrange
        setupBasicEmailMock();
        when(mockEmail.getSensitivity()).thenReturn("Normal");

        // Act
        MailDetails result = mailHandler.fromEmail(mockEmail, false);

        // Assert
        assertNotNull(result);
        assertEquals("Normal", result.sensitivity, "Sensitivity should be Normal");
        verify(mockEmail).getSensitivity();
    }

    @Test
    @DisplayName("Should convert email with Personal sensitivity")
    void testFromEmail_WithPersonalSensitivity() {
        // Arrange
        setupBasicEmailMock();
        when(mockEmail.getSensitivity()).thenReturn("Personal");

        // Act
        MailDetails result = mailHandler.fromEmail(mockEmail, false);

        // Assert
        assertNotNull(result);
        assertEquals("Personal", result.sensitivity, "Sensitivity should be Personal");
    }

    @Test
    @DisplayName("Should convert email with Private sensitivity")
    void testFromEmail_WithPrivateSensitivity() {
        // Arrange
        setupBasicEmailMock();
        when(mockEmail.getSensitivity()).thenReturn("Private");

        // Act
        MailDetails result = mailHandler.fromEmail(mockEmail, false);

        // Assert
        assertNotNull(result);
        assertEquals("Private", result.sensitivity, "Sensitivity should be Private");
    }

    @Test
    @DisplayName("Should convert email with Confidential sensitivity")
    void testFromEmail_WithConfidentialSensitivity() {
        // Arrange
        setupBasicEmailMock();
        when(mockEmail.getSensitivity()).thenReturn("Confidential");

        // Act
        MailDetails result = mailHandler.fromEmail(mockEmail, false);

        // Assert
        assertNotNull(result);
        assertEquals("Confidential", result.sensitivity, "Sensitivity should be Confidential");
    }

    @Test
    @DisplayName("Should handle null sensitivity gracefully")
    void testFromEmail_WithNullSensitivity() {
        // Arrange
        setupBasicEmailMock();
        when(mockEmail.getSensitivity()).thenReturn(null);

        // Act
        MailDetails result = mailHandler.fromEmail(mockEmail, false);

        // Assert
        assertNotNull(result);
        assertNull(result.sensitivity, "Sensitivity should be null when not set");
    }

    @Test
    @DisplayName("Should convert all email fields correctly including sensitivity")
    void testFromEmail_AllFieldsIncludingSensitivity() {
        // Arrange
        setupCompleteEmailMock();
        when(mockEmail.getSensitivity()).thenReturn("Private");

        // Act
        MailDetails result = mailHandler.fromEmail(mockEmail, false);

        // Assert
        assertNotNull(result);
        assertEquals("sender@example.com", result.from);
        assertEquals("recipient@example.com", result.to);
        assertEquals("Test Subject", result.subject);
        assertEquals("Test Message", result.message);
        assertEquals("msg-123", result.messageID);
        assertEquals("Private", result.sensitivity);
        assertEquals(true, result.isRead);
    }

    /**
     * Helper method to set up basic email mock with minimal required fields
     */
    private void setupBasicEmailMock() {
        when(mockEmail.getSenderEmailAddress()).thenReturn(mockSenderAddress);
        when(mockSenderAddress.getMailAddress()).thenReturn("sender@example.com");
        when(mockEmail.getToEmailAddresses()).thenReturn(Collections.emptyList());
        when(mockEmail.getCcEmailAddresses()).thenReturn(Collections.emptyList());
        when(mockEmail.getBccEmailAddresses()).thenReturn(Collections.emptyList());
        when(mockEmail.getReplyToEmailAddresses()).thenReturn(Collections.emptyList());
        when(mockEmail.getFileAttachments(anyBoolean())).thenReturn(Collections.emptyList());
        when(mockEmail.getItemAttachments(anyBoolean())).thenReturn(Collections.emptyList());
        when(mockEmail.getSubject()).thenReturn("Test Subject");
        when(mockEmail.getContent()).thenReturn("Test Message");
        when(mockEmail.getEmailId()).thenReturn("msg-123");
        when(mockEmail.getRead()).thenReturn(false);
        when(mockEmail.getSendDateAndTime()).thenReturn(null);
        when(mockEmail.getReceivedDateAndTime()).thenReturn(null);
        when(mockEmail.getCategories()).thenReturn(Collections.emptyList());
        when(mockEmail.getConversationId()).thenReturn("conv-123");
        when(mockEmail.getUniqueBody()).thenReturn("Unique body");
    }

    /**
     * Helper method to set up complete email mock with all fields populated
     */
    private void setupCompleteEmailMock() {
        EmailAddress toAddress = mock(EmailAddress.class);
        when(toAddress.getMailAddress()).thenReturn("recipient@example.com");

        when(mockEmail.getSenderEmailAddress()).thenReturn(mockSenderAddress);
        when(mockSenderAddress.getMailAddress()).thenReturn("sender@example.com");
        when(mockEmail.getToEmailAddresses()).thenReturn(Collections.singletonList(toAddress));
        when(mockEmail.getCcEmailAddresses()).thenReturn(Collections.emptyList());
        when(mockEmail.getBccEmailAddresses()).thenReturn(Collections.emptyList());
        when(mockEmail.getReplyToEmailAddresses()).thenReturn(Collections.emptyList());
        when(mockEmail.getFileAttachments(anyBoolean())).thenReturn(Collections.emptyList());
        when(mockEmail.getItemAttachments(anyBoolean())).thenReturn(Collections.emptyList());
        when(mockEmail.getSubject()).thenReturn("Test Subject");
        when(mockEmail.getContent()).thenReturn("Test Message");
        when(mockEmail.getEmailId()).thenReturn("msg-123");
        when(mockEmail.getRead()).thenReturn(true);
        when(mockEmail.getSendDateAndTime()).thenReturn(null);
        when(mockEmail.getReceivedDateAndTime()).thenReturn(null);
        when(mockEmail.getCategories()).thenReturn(Collections.emptyList());
        when(mockEmail.getConversationId()).thenReturn("conv-123");
        when(mockEmail.getUniqueBody()).thenReturn("Unique body");
    }

    @Test
    @DisplayName("Should handle draft email without sender address")
    void testFromEmail_DraftEmailWithoutSender() {
        // Arrange
        when(mockEmail.getSenderEmailAddress()).thenReturn(null);
        when(mockEmail.getToEmailAddresses()).thenReturn(Collections.emptyList());
        when(mockEmail.getCcEmailAddresses()).thenReturn(Collections.emptyList());
        when(mockEmail.getBccEmailAddresses()).thenReturn(Collections.emptyList());
        when(mockEmail.getReplyToEmailAddresses()).thenReturn(Collections.emptyList());
        when(mockEmail.getFileAttachments(anyBoolean())).thenReturn(Collections.emptyList());
        when(mockEmail.getItemAttachments(anyBoolean())).thenReturn(Collections.emptyList());
        when(mockEmail.getSubject()).thenReturn("Draft Subject");
        when(mockEmail.getContent()).thenReturn("Draft Content");
        when(mockEmail.getEmailId()).thenReturn("draft-123");
        when(mockEmail.getRead()).thenReturn(false);
        when(mockEmail.getSensitivity()).thenReturn("Normal");
        when(mockEmail.getSendDateAndTime()).thenReturn(null);
        when(mockEmail.getReceivedDateAndTime()).thenReturn(null);
        when(mockEmail.getCategories()).thenReturn(Collections.emptyList());
        when(mockEmail.getConversationId()).thenReturn(null);
        when(mockEmail.getUniqueBody()).thenReturn(null);

        // Act
        MailDetails result = mailHandler.fromEmail(mockEmail, false);

        // Assert
        assertNotNull(result);
        assertNull(result.from, "From address should be null for draft emails");
        assertEquals("Normal", result.sensitivity);
        assertEquals("Draft Subject", result.subject);
    }

    @Test
    @DisplayName("Should verify sensitivity is called during email conversion")
    void testFromEmail_VerifySensitivityMethodCalled() {
        // Arrange
        setupBasicEmailMock();
        when(mockEmail.getSensitivity()).thenReturn("Confidential");

        // Act
        MailDetails result = mailHandler.fromEmail(mockEmail, false);

        // Assert
        assertNotNull(result);
        verify(mockEmail, times(1)).getSensitivity();
        assertEquals("Confidential", result.sensitivity);
    }

    @Test
    @DisplayName("Should handle email with multiple recipients and sensitivity")
    void testFromEmail_MultipleRecipientsWithSensitivity() {
        // Arrange
        EmailAddress toAddress1 = mock(EmailAddress.class);
        EmailAddress toAddress2 = mock(EmailAddress.class);
        when(toAddress1.getMailAddress()).thenReturn("recipient1@example.com");
        when(toAddress2.getMailAddress()).thenReturn("recipient2@example.com");

        when(mockEmail.getSenderEmailAddress()).thenReturn(mockSenderAddress);
        when(mockSenderAddress.getMailAddress()).thenReturn("sender@example.com");
        when(mockEmail.getToEmailAddresses()).thenReturn(Arrays.asList(toAddress1, toAddress2));
        when(mockEmail.getCcEmailAddresses()).thenReturn(Collections.emptyList());
        when(mockEmail.getBccEmailAddresses()).thenReturn(Collections.emptyList());
        when(mockEmail.getReplyToEmailAddresses()).thenReturn(Collections.emptyList());
        when(mockEmail.getFileAttachments(anyBoolean())).thenReturn(Collections.emptyList());
        when(mockEmail.getItemAttachments(anyBoolean())).thenReturn(Collections.emptyList());
        when(mockEmail.getSubject()).thenReturn("Multi-recipient Email");
        when(mockEmail.getContent()).thenReturn("Content");
        when(mockEmail.getEmailId()).thenReturn("msg-456");
        when(mockEmail.getRead()).thenReturn(true);
        when(mockEmail.getSensitivity()).thenReturn("Private");
        when(mockEmail.getSendDateAndTime()).thenReturn(null);
        when(mockEmail.getReceivedDateAndTime()).thenReturn(null);
        when(mockEmail.getCategories()).thenReturn(Collections.emptyList());
        when(mockEmail.getConversationId()).thenReturn("conv-456");
        when(mockEmail.getUniqueBody()).thenReturn("Unique");

        // Act
        MailDetails result = mailHandler.fromEmail(mockEmail, false);

        // Assert
        assertNotNull(result);
        assertEquals("Private", result.sensitivity);
        assertTrue(result.to.contains("recipient1@example.com"));
        assertTrue(result.to.contains("recipient2@example.com"));
    }
}

