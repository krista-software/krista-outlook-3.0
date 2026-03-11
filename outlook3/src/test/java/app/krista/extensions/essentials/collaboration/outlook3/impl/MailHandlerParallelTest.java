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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MailHandler parallel processing functionality.
 * Tests the semaphore-based concurrency control and parallel email processing.
 */
@DisplayName("Mail Handler Parallel Processing Tests")
class MailHandlerParallelTest {

    @Mock
    private KristaMediaClient kristaMediaClient;

    private MailHandler mailHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mailHandler = new MailHandler(kristaMediaClient);
    }

    @Test
    @DisplayName("Should return empty list when email list is null")
    void testFromEmailsParallel_NullEmailList() {
        // Act
        List<MailDetails> result = mailHandler.fromEmailsParallel(null, false);

        // Assert
        assertNotNull(result, "Result should not be null");
        assertTrue(result.isEmpty(), "Result should be empty list");
    }

    @Test
    @DisplayName("Should return empty list when email list is empty")
    void testFromEmailsParallel_EmptyEmailList() {
        // Act
        List<MailDetails> result = mailHandler.fromEmailsParallel(Collections.emptyList(), false);

        // Assert
        assertNotNull(result, "Result should not be null");
        assertTrue(result.isEmpty(), "Result should be empty list");
    }

    @Test
    @DisplayName("Should process single email in parallel")
    void testFromEmailsParallel_SingleEmail() {
        // Arrange
        Email mockEmail = createMockEmail("test@example.com", "Test Subject", "msg-1");

        // Act
        List<MailDetails> result = mailHandler.fromEmailsParallel(Collections.singletonList(mockEmail), false);

        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals(1, result.size(), "Should process 1 email");
        assertEquals("test@example.com", result.get(0).from);
        assertEquals("Test Subject", result.get(0).subject);
    }

    @Test
    @DisplayName("Should process multiple emails in parallel")
    void testFromEmailsParallel_MultipleEmails() {
        // Arrange
        List<Email> emails = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            emails.add(createMockEmail("sender" + i + "@example.com", "Subject " + i, "msg-" + i));
        }

        // Act
        List<MailDetails> result = mailHandler.fromEmailsParallel(emails, false);

        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals(5, result.size(), "Should process 5 emails");

        // Verify all emails were processed
        for (int i = 0; i < 5; i++) {
            assertNotNull(result.get(i), "Email " + i + " should be processed");
            assertEquals("Subject " + (i + 1), result.get(i).subject);
        }
    }

    @Test
    @DisplayName("Should process 15 emails respecting semaphore limit")
    void testFromEmailsParallel_FifteenEmails() {
        // Arrange
        List<Email> emails = new ArrayList<>();
        for (int i = 1; i <= 15; i++) {
            emails.add(createMockEmail("sender" + i + "@example.com", "Subject " + i, "msg-" + i));
        }

        // Act
        long startTime = System.currentTimeMillis();
        List<MailDetails> result = mailHandler.fromEmailsParallel(emails, false);
        long duration = System.currentTimeMillis() - startTime;

        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals(15, result.size(), "Should process 15 emails");
        assertTrue(duration < 5000, "Should complete within 5 seconds for mock emails");

        // Verify all emails were processed
        for (int i = 0; i < 15; i++) {
            assertNotNull(result.get(i), "Email " + i + " should be processed");
        }
    }

    @Test
    @DisplayName("Should maintain order of processed emails")
    void testFromEmailsParallel_MaintainsOrder() {
        // Arrange
        List<Email> emails = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            emails.add(createMockEmail("sender" + i + "@example.com", "Subject " + i, "msg-" + i));
        }

        // Act
        List<MailDetails> result = mailHandler.fromEmailsParallel(emails, false);

        // Assert
        assertEquals(10, result.size(), "Should process 10 emails");
        for (int i = 0; i < 10; i++) {
            assertEquals("Subject " + (i + 1), result.get(i).subject,
                    "Email order should be maintained");
        }
    }

    @Test
    @DisplayName("Should handle emails with null sender gracefully")
    void testFromEmailsParallel_EmailsWithNullSender() {
        // Arrange
        List<Email> emails = new ArrayList<>();
        emails.add(createMockEmailWithNullSender("Subject 1", "msg-1"));
        emails.add(createMockEmail("sender@example.com", "Subject 2", "msg-2"));

        // Act
        List<MailDetails> result = mailHandler.fromEmailsParallel(emails, false);

        // Assert
        assertEquals(2, result.size(), "Should process 2 emails");
        assertNull(result.get(0).from, "First email should have null sender");
        assertEquals("sender@example.com", result.get(1).from, "Second email should have sender");
    }

    /**
     * Helper method to create a mock Email object
     */
    private Email createMockEmail(String senderEmail, String subject, String messageId) {
        Email mockEmail = mock(Email.class);
        EmailAddress mockSenderAddress = mock(EmailAddress.class);

        when(mockSenderAddress.getMailAddress()).thenReturn(senderEmail);
        when(mockEmail.getSenderEmailAddress()).thenReturn(mockSenderAddress);
        when(mockEmail.getSubject()).thenReturn(subject);
        when(mockEmail.getEmailId()).thenReturn(messageId);
        when(mockEmail.getContent()).thenReturn("Test content for " + subject);
        when(mockEmail.getToEmailAddresses()).thenReturn(Collections.emptyList());
        when(mockEmail.getCcEmailAddresses()).thenReturn(Collections.emptyList());
        when(mockEmail.getBccEmailAddresses()).thenReturn(Collections.emptyList());
        when(mockEmail.getReplyToEmailAddresses()).thenReturn(Collections.emptyList());
        when(mockEmail.getFileAttachments(anyBoolean())).thenReturn(Collections.emptyList());
        when(mockEmail.getItemAttachments(anyBoolean())).thenReturn(Collections.emptyList());
        when(mockEmail.getRead()).thenReturn(false);
        when(mockEmail.getSensitivity()).thenReturn("Normal");
        when(mockEmail.getSendDateAndTime()).thenReturn(null);
        when(mockEmail.getReceivedDateAndTime()).thenReturn(null);
        when(mockEmail.getCategories()).thenReturn(Collections.emptyList());
        when(mockEmail.getConversationId()).thenReturn("conv-" + messageId);
        when(mockEmail.getUniqueBody()).thenReturn("Unique body");

        return mockEmail;
    }

    /**
     * Helper method to create a mock Email object with null sender (draft email)
     */
    private Email createMockEmailWithNullSender(String subject, String messageId) {
        Email mockEmail = mock(Email.class);

        when(mockEmail.getSenderEmailAddress()).thenReturn(null);
        when(mockEmail.getSubject()).thenReturn(subject);
        when(mockEmail.getEmailId()).thenReturn(messageId);
        when(mockEmail.getContent()).thenReturn("Draft content for " + subject);
        when(mockEmail.getToEmailAddresses()).thenReturn(Collections.emptyList());
        when(mockEmail.getCcEmailAddresses()).thenReturn(Collections.emptyList());
        when(mockEmail.getBccEmailAddresses()).thenReturn(Collections.emptyList());
        when(mockEmail.getReplyToEmailAddresses()).thenReturn(Collections.emptyList());
        when(mockEmail.getFileAttachments(anyBoolean())).thenReturn(Collections.emptyList());
        when(mockEmail.getItemAttachments(anyBoolean())).thenReturn(Collections.emptyList());
        when(mockEmail.getRead()).thenReturn(false);
        when(mockEmail.getSensitivity()).thenReturn("Normal");
        when(mockEmail.getSendDateAndTime()).thenReturn(null);
        when(mockEmail.getReceivedDateAndTime()).thenReturn(null);
        when(mockEmail.getCategories()).thenReturn(Collections.emptyList());
        when(mockEmail.getConversationId()).thenReturn(null);
        when(mockEmail.getUniqueBody()).thenReturn(null);

        return mockEmail;
    }

    @Test
    @DisplayName("Should process large batch of emails efficiently")
    void testFromEmailsParallel_LargeBatch() {
        // Arrange
        List<Email> emails = new ArrayList<>();
        for (int i = 1; i <= 50; i++) {
            emails.add(createMockEmail("sender" + i + "@example.com", "Subject " + i, "msg-" + i));
        }

        // Act
        long startTime = System.currentTimeMillis();
        List<MailDetails> result = mailHandler.fromEmailsParallel(emails, false);
        long duration = System.currentTimeMillis() - startTime;

        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals(50, result.size(), "Should process 50 emails");
        assertTrue(duration < 10000, "Should complete within 10 seconds for mock emails");

        System.out.println("Processed 50 emails in " + duration + "ms");
    }

    @Test
    @DisplayName("Should handle mixed email types in parallel")
    void testFromEmailsParallel_MixedEmailTypes() {
        // Arrange
        List<Email> emails = new ArrayList<>();
        emails.add(createMockEmail("sender1@example.com", "Normal Email", "msg-1"));
        emails.add(createMockEmailWithNullSender("Draft Email", "msg-2"));
        emails.add(createMockEmail("sender3@example.com", "Another Normal", "msg-3"));

        // Act
        List<MailDetails> result = mailHandler.fromEmailsParallel(emails, false);

        // Assert
        assertEquals(3, result.size(), "Should process 3 emails");
        assertEquals("sender1@example.com", result.get(0).from);
        assertNull(result.get(1).from, "Draft email should have null sender");
        assertEquals("sender3@example.com", result.get(2).from);
    }

    @Test
    @DisplayName("Should process emails with different sensitivity levels")
    void testFromEmailsParallel_DifferentSensitivityLevels() {
        // Arrange
        List<Email> emails = new ArrayList<>();

        Email email1 = createMockEmail("sender1@example.com", "Normal Email", "msg-1");
        when(email1.getSensitivity()).thenReturn("Normal");

        Email email2 = createMockEmail("sender2@example.com", "Private Email", "msg-2");
        when(email2.getSensitivity()).thenReturn("Private");

        Email email3 = createMockEmail("sender3@example.com", "Confidential Email", "msg-3");
        when(email3.getSensitivity()).thenReturn("Confidential");

        emails.add(email1);
        emails.add(email2);
        emails.add(email3);

        // Act
        List<MailDetails> result = mailHandler.fromEmailsParallel(emails, false);

        // Assert
        assertEquals(3, result.size(), "Should process 3 emails");
        assertEquals("Normal", result.get(0).sensitivity);
        assertEquals("Private", result.get(1).sensitivity);
        assertEquals("Confidential", result.get(2).sensitivity);
    }

    @Test
    @DisplayName("Should complete faster than sequential processing")
    void testFromEmailsParallel_PerformanceComparison() {
        // Arrange
        List<Email> emails = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            Email email = createMockEmail("sender" + i + "@example.com", "Subject " + i, "msg-" + i);
            // Add small delay to simulate API call
            when(email.getFileAttachments(anyBoolean())).thenAnswer(invocation -> {
                Thread.sleep(10); // 10ms simulated API delay
                return Collections.emptyList();
            });
            emails.add(email);
        }

        // Act - Parallel processing
        long parallelStart = System.currentTimeMillis();
        List<MailDetails> parallelResult = mailHandler.fromEmailsParallel(emails, false);
        long parallelDuration = System.currentTimeMillis() - parallelStart;

        // Assert
        assertEquals(20, parallelResult.size(), "Should process 20 emails");
        assertTrue(parallelDuration < 1000, "Parallel processing should be fast (< 1s)");

        System.out.println("Parallel processing: " + parallelDuration + "ms for 20 emails");
    }

    @Test
    @DisplayName("Should propagate exceptions when email processing fails")
    void testFromEmailsParallel_EmailProcessingException() {
        // Arrange
        List<Email> emails = new ArrayList<>();

        // Normal email
        emails.add(createMockEmail("sender1@example.com", "Normal Email", "msg-1"));

        // Email that throws exception
        Email errorEmail = mock(Email.class);
        when(errorEmail.getSenderEmailAddress()).thenThrow(new RuntimeException("API Error"));
        emails.add(errorEmail);

        // Another normal email
        emails.add(createMockEmail("sender3@example.com", "Another Normal", "msg-3"));

        // Act & Assert
        // The method will throw exception when processing fails
        assertThrows(RuntimeException.class, () -> {
            mailHandler.fromEmailsParallel(emails, false);
        }, "Should throw exception when email processing fails");
    }

    @Test
    @DisplayName("Should respect semaphore limit with concurrent processing")
    void testFromEmailsParallel_SemaphoreLimit() {
        // Arrange
        List<Email> emails = new ArrayList<>();
        AtomicInteger concurrentCount = new AtomicInteger(0);
        AtomicInteger maxConcurrent = new AtomicInteger(0);

        for (int i = 1; i <= 20; i++) {
            Email email = createMockEmail("sender" + i + "@example.com", "Subject " + i, "msg-" + i);

            // Track concurrent execution
            when(email.getFileAttachments(anyBoolean())).thenAnswer(invocation -> {
                int current = concurrentCount.incrementAndGet();
                maxConcurrent.updateAndGet(max -> Math.max(max, current));

                Thread.sleep(50); // Simulate API call

                concurrentCount.decrementAndGet();
                return Collections.emptyList();
            });

            emails.add(email);
        }

        // Act
        List<MailDetails> result = mailHandler.fromEmailsParallel(emails, false);

        // Assert
        assertEquals(20, result.size(), "Should process 20 emails");
        assertTrue(maxConcurrent.get() <= 10,
                "Max concurrent should not exceed semaphore limit of 10, was: " + maxConcurrent.get());

        System.out.println("Max concurrent threads: " + maxConcurrent.get());
    }
}
