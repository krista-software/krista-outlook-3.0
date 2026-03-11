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

package app.krista.extensions.essentials.collaboration.outlook3.util;

import app.krista.extensions.essentials.collaboration.outlook3.impl.util.EntityHelperUtil;
import app.krista.extensions.essentials.collaboration.outlook3.service.EmailAddress;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Real-world unit tests for EntityHelperUtil utility class.
 * Tests cover email address handling, data validation, and formatting operations.
 */
@DisplayName("EntityHelperUtil Tests")
class EntityHelperUtilTest {

    // ==================== Email Address Conversion Tests ====================

    @Test
    @DisplayName("Should convert single email address to comma-separated string")
    void testSingleEmailAddress() {
        List<EmailAddress> emails = List.of(
                new EmailAddress("John Doe", "john.doe@example.com")
        );

        String result = EntityHelperUtil.getCommaSeparatedEmail(emails);
        assertEquals("john.doe@example.com", result);
    }

    @Test
    @DisplayName("Should convert multiple email addresses to comma-separated string")
    void testMultipleEmailAddresses() {
        List<EmailAddress> emails = Arrays.asList(
                new EmailAddress("John Doe", "john.doe@example.com"),
                new EmailAddress("Jane Smith", "jane.smith@example.com"),
                new EmailAddress("Bob Johnson", "bob.johnson@example.com")
        );

        String result = EntityHelperUtil.getCommaSeparatedEmail(emails);
        assertEquals("john.doe@example.com,jane.smith@example.com,bob.johnson@example.com", result);
    }

    @Test
    @DisplayName("Should handle null email list")
    void testNullEmailList() {
        String result = EntityHelperUtil.getCommaSeparatedEmail(null);
        assertEquals("", result);
    }

    @Test
    @DisplayName("Should handle empty email list")
    void testEmptyEmailList() {
        String result = EntityHelperUtil.getCommaSeparatedEmail(new ArrayList<>());
        assertEquals("", result);
    }

    @Test
    @DisplayName("Should handle email addresses without display names")
    void testEmailAddressesWithoutDisplayNames() {
        List<EmailAddress> emails = Arrays.asList(
                new EmailAddress("", "user1@company.com"),
                new EmailAddress("", "user2@company.com")
        );

        String result = EntityHelperUtil.getCommaSeparatedEmail(emails);
        assertEquals("user1@company.com,user2@company.com", result);
    }

    // ==================== String to Email Address Conversion Tests ====================

    @Test
    @DisplayName("Should convert single email string to EmailAddress list")
    void testSingleEmailStringToList() {
        String emailString = "john.doe@example.com";
        List<EmailAddress> result = EntityHelperUtil.toEmailAddresses(emailString);

        assertEquals(1, result.size());
        assertEquals("john.doe@example.com", result.get(0).getMailAddress());
    }

    @Test
    @DisplayName("Should convert comma-separated emails to EmailAddress list")
    void testCommaSeparatedEmailsToList() {
        String emailString = "john@example.com,jane@example.com,bob@example.com";
        List<EmailAddress> result = EntityHelperUtil.toEmailAddresses(emailString);

        assertEquals(3, result.size());
        assertEquals("john@example.com", result.get(0).getMailAddress());
        assertEquals("jane@example.com", result.get(1).getMailAddress());
        assertEquals("bob@example.com", result.get(2).getMailAddress());
    }

    @Test
    @DisplayName("Should handle null email string")
    void testNullEmailString() {
        List<EmailAddress> result = EntityHelperUtil.toEmailAddresses(null);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should handle empty email string")
    void testEmptyEmailString() {
        List<EmailAddress> result = EntityHelperUtil.toEmailAddresses("");
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should handle blank email string")
    void testBlankEmailString() {
        List<EmailAddress> result = EntityHelperUtil.toEmailAddresses("   ");
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should filter out invalid email addresses")
    void testFilterInvalidEmails() {
        String emailString = "valid@example.com,invalid-email,another@example.com,@invalid.com";
        List<EmailAddress> result = EntityHelperUtil.toEmailAddresses(emailString);

        // Only valid emails should be included
        assertEquals(2, result.size());
        assertEquals("valid@example.com", result.get(0).getMailAddress());
        assertEquals("another@example.com", result.get(1).getMailAddress());
    }

    @Test
    @DisplayName("Should handle emails with spaces")
    void testEmailsWithSpaces() {
        String emailString = "john@example.com, jane@example.com , bob@example.com";
        List<EmailAddress> result = EntityHelperUtil.toEmailAddresses(emailString);

        // Spaces around commas are NOT trimmed, so " jane@example.com " and " bob@example.com" are invalid
        // Only the first email without leading/trailing space is valid
        assertEquals(1, result.size());
        assertEquals("john@example.com", result.get(0).getMailAddress());
    }

    // ==================== Data Validation Tests ====================

    @Test
    @DisplayName("Should convert boolean true to Yes")
    void testBooleanTrueToYes() {
        Map<String, Object> entityMap = new HashMap<>();
        entityMap.put("isActive", Boolean.TRUE);
        entityMap.put("isVerified", true);

        Map<String, Object> result = EntityHelperUtil.getValidatedData(entityMap);

        assertEquals("Yes", result.get("isActive"));
        assertEquals("Yes", result.get("isVerified"));
    }

    @Test
    @DisplayName("Should convert boolean false to No")
    void testBooleanFalseToNo() {
        Map<String, Object> entityMap = new HashMap<>();
        entityMap.put("isActive", Boolean.FALSE);
        entityMap.put("isVerified", false);

        Map<String, Object> result = EntityHelperUtil.getValidatedData(entityMap);

        assertEquals("No", result.get("isActive"));
        assertEquals("No", result.get("isVerified"));
    }

    @Test
    @DisplayName("Should preserve non-boolean values")
    void testPreserveNonBooleanValues() {
        Map<String, Object> entityMap = new HashMap<>();
        entityMap.put("name", "John Doe");
        entityMap.put("age", 30);
        entityMap.put("salary", 75000.50);

        Map<String, Object> result = EntityHelperUtil.getValidatedData(entityMap);

        assertEquals("John Doe", result.get("name"));
        assertEquals(30, result.get("age"));
        assertEquals(75000.50, result.get("salary"));
    }

    @Test
    @DisplayName("Should handle mixed data types")
    void testMixedDataTypes() {
        Map<String, Object> entityMap = new LinkedHashMap<>();
        entityMap.put("name", "Alice");
        entityMap.put("isActive", true);
        entityMap.put("age", 28);
        entityMap.put("hasAccess", false);
        entityMap.put("department", "Engineering");

        Map<String, Object> result = EntityHelperUtil.getValidatedData(entityMap);

        assertEquals("Alice", result.get("name"));
        assertEquals("Yes", result.get("isActive"));
        assertEquals(28, result.get("age"));
        assertEquals("No", result.get("hasAccess"));
        assertEquals("Engineering", result.get("department"));
    }

    // ==================== Trailing Zeros Removal Tests ====================

    @Test
    @DisplayName("Should remove trailing zeros from whole numbers")
    void testRemoveTrailingZerosWholeNumbers() {
        assertEquals("10", EntityHelperUtil.removeTrailingZeros(10.0));
        assertEquals("100", EntityHelperUtil.removeTrailingZeros(100.0));
        assertEquals("5", EntityHelperUtil.removeTrailingZeros(5.00000));
    }

    @Test
    @DisplayName("Should preserve significant decimal places")
    void testPreserveSignificantDecimals() {
        assertEquals("10.5", EntityHelperUtil.removeTrailingZeros(10.5));
        assertEquals("3.14", EntityHelperUtil.removeTrailingZeros(3.14));
        assertEquals("99.99", EntityHelperUtil.removeTrailingZeros(99.99));
    }

    @Test
    @DisplayName("Should remove only trailing zeros")
    void testRemoveOnlyTrailingZeros() {
        assertEquals("10.1", EntityHelperUtil.removeTrailingZeros(10.10));
        assertEquals("5.5", EntityHelperUtil.removeTrailingZeros(5.50));
        assertEquals("3.123", EntityHelperUtil.removeTrailingZeros(3.12300));
    }

    @Test
    @DisplayName("Should handle zero value")
    void testZeroValue() {
        assertEquals("0", EntityHelperUtil.removeTrailingZeros(0.0));
        assertEquals("0", EntityHelperUtil.removeTrailingZeros(0.00000));
    }

    @Test
    @DisplayName("Should handle negative numbers")
    void testNegativeNumbers() {
        assertEquals("-10", EntityHelperUtil.removeTrailingZeros(-10.0));
        assertEquals("-5.5", EntityHelperUtil.removeTrailingZeros(-5.50));
        assertEquals("-3.14", EntityHelperUtil.removeTrailingZeros(-3.14));
    }

    @Test
    @DisplayName("Should handle very small decimals")
    void testVerySmallDecimals() {
        assertEquals("0.001", EntityHelperUtil.removeTrailingZeros(0.001));
        assertEquals("0.1", EntityHelperUtil.removeTrailingZeros(0.10));
        // Very small decimals may be in scientific notation
        String result = EntityHelperUtil.removeTrailingZeros(0.0001);
        assertTrue(result.equals("0.0001") || result.equals("1.0E-4"));
    }

    // ==================== Message Formatting Tests ====================

    @Test
    @DisplayName("Should format HTML message with newlines")
    void testFormatHtmlMessageWithNewlines() {
        String message = "Line 1\nLine 2\nLine 3";
        String result = EntityHelperUtil.formattedMessage(message, "HTML");

        assertEquals("Line 1<br>Line 2<br>Line 3", result);
    }

    @Test
    @DisplayName("Should preserve HTML tags in message")
    void testPreserveHtmlTags() {
        String message = "Hello <b>World</b>\nNew line";
        String result = EntityHelperUtil.formattedMessage(message, "HTML");

        assertTrue(result.contains("<b>World</b>"));
        assertTrue(result.contains("<br>"));
    }

    @Test
    @DisplayName("Should handle text before and after HTML tags")
    void testTextAroundHtmlTags() {
        String message = "Before\n<div>Content</div>\nAfter";
        String result = EntityHelperUtil.formattedMessage(message, "HTML");

        assertTrue(result.contains("Before<br>"));
        assertTrue(result.contains("<div>Content</div>"));
        assertTrue(result.contains("<br>After"));
    }

    @Test
    @DisplayName("Should not format non-HTML messages")
    void testNonHtmlMessage() {
        String message = "Line 1\nLine 2";
        String result = EntityHelperUtil.formattedMessage(message, "TEXT");

        assertEquals("Line 1\nLine 2", result);
    }

    @Test
    @DisplayName("Should handle null message")
    void testNullMessage() {
        String result = EntityHelperUtil.formattedMessage(null, "HTML");
        assertNull(result);
    }

    @Test
    @DisplayName("Should handle null body type")
    void testNullBodyType() {
        String message = "Test message\nNew line";
        String result = EntityHelperUtil.formattedMessage(message, null);

        assertEquals("Test message\nNew line", result);
    }

    // ==================== Real-World Email Scenarios ====================

    @Test
    @DisplayName("Should handle TO recipients in email")
    void testEmailToRecipients() {
        String toRecipients = "john@example.com,jane@example.com,bob@example.com";
        List<EmailAddress> emailList = EntityHelperUtil.toEmailAddresses(toRecipients);

        assertEquals(3, emailList.size());

        // Convert back to comma-separated string
        String result = EntityHelperUtil.getCommaSeparatedEmail(emailList);
        assertEquals("john@example.com,jane@example.com,bob@example.com", result);
    }

    @Test
    @DisplayName("Should handle CC recipients in email")
    void testEmailCcRecipients() {
        List<EmailAddress> ccList = Arrays.asList(
                new EmailAddress("Manager", "manager@company.com"),
                new EmailAddress("HR", "hr@company.com")
        );

        String result = EntityHelperUtil.getCommaSeparatedEmail(ccList);
        assertEquals("manager@company.com,hr@company.com", result);
    }

    @Test
    @DisplayName("Should handle BCC recipients in email")
    void testEmailBccRecipients() {
        String bccRecipients = "audit@company.com,compliance@company.com";
        List<EmailAddress> bccList = EntityHelperUtil.toEmailAddresses(bccRecipients);

        assertEquals(2, bccList.size());
        assertEquals("audit@company.com", bccList.get(0).getMailAddress());
        assertEquals("compliance@company.com", bccList.get(1).getMailAddress());
    }

    @Test
    @DisplayName("Should format email body for HTML")
    void testFormatEmailBodyHtml() {
        String emailBody = "Dear Team,\n\nPlease review the attached document.\n\nBest regards,\nJohn";
        String formatted = EntityHelperUtil.formattedMessage(emailBody, "HTML");

        assertTrue(formatted.contains("<br>"));
        assertFalse(formatted.contains("\n"));
    }

    @Test
    @DisplayName("Should preserve plain text email body")
    void testPreservePlainTextEmailBody() {
        String emailBody = "Dear Team,\n\nPlease review the attached document.\n\nBest regards,\nJohn";
        String formatted = EntityHelperUtil.formattedMessage(emailBody, "TEXT");

        assertEquals(emailBody, formatted);
        assertTrue(formatted.contains("\n"));
    }

    // ==================== Entity Data Validation Scenarios ====================

    @Test
    @DisplayName("Should validate user profile data")
    void testValidateUserProfileData() {
        Map<String, Object> userProfile = new LinkedHashMap<>();
        userProfile.put("name", "John Doe");
        userProfile.put("email", "john@example.com");
        userProfile.put("isActive", true);
        userProfile.put("isAdmin", false);
        userProfile.put("age", 30);

        Map<String, Object> validated = EntityHelperUtil.getValidatedData(userProfile);

        assertEquals("John Doe", validated.get("name"));
        assertEquals("john@example.com", validated.get("email"));
        assertEquals("Yes", validated.get("isActive"));
        assertEquals("No", validated.get("isAdmin"));
        assertEquals(30, validated.get("age"));
    }

    @Test
    @DisplayName("Should format financial data")
    void testFormatFinancialData() {
        Map<String, Object> financialData = new LinkedHashMap<>();
        financialData.put("revenue", 1000000.00);
        financialData.put("profit", 250000.50);
        financialData.put("isProfitable", true);

        Map<String, Object> validated = EntityHelperUtil.getValidatedData(financialData);

        assertEquals(1000000.00, validated.get("revenue"));
        assertEquals(250000.50, validated.get("profit"));
        assertEquals("Yes", validated.get("isProfitable"));
    }

    @Test
    @DisplayName("Should handle bulk email distribution list")
    void testBulkEmailDistributionList() {
        String distributionList = "team1@company.com,team2@company.com,team3@company.com," +
                "team4@company.com,team5@company.com";

        List<EmailAddress> emails = EntityHelperUtil.toEmailAddresses(distributionList);

        assertEquals(5, emails.size());

        // Verify all emails are valid
        for (EmailAddress email : emails) {
            assertTrue(email.getMailAddress().contains("@company.com"));
        }
    }

    @Test
    @DisplayName("Should clean up invalid emails from distribution list")
    void testCleanupInvalidEmailsFromDistribution() {
        String mixedList = "valid1@company.com,invalid-email,valid2@company.com," +
                "notanemail,valid3@company.com,@invalid.com";

        List<EmailAddress> emails = EntityHelperUtil.toEmailAddresses(mixedList);

        // Only 3 valid emails should remain
        assertEquals(3, emails.size());
        assertEquals("valid1@company.com", emails.get(0).getMailAddress());
        assertEquals("valid2@company.com", emails.get(1).getMailAddress());
        assertEquals("valid3@company.com", emails.get(2).getMailAddress());
    }

    @Test
    @DisplayName("Should format percentage values")
    void testFormatPercentageValues() {
        // Test removing trailing zeros from percentages
        assertEquals("50", EntityHelperUtil.removeTrailingZeros(50.0));
        assertEquals("33.33", EntityHelperUtil.removeTrailingZeros(33.33));
        assertEquals("100", EntityHelperUtil.removeTrailingZeros(100.00));
        assertEquals("0.5", EntityHelperUtil.removeTrailingZeros(0.50));
    }
}

