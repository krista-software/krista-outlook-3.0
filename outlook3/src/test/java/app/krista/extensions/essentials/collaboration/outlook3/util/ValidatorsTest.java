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

import app.krista.extensions.essentials.collaboration.outlook3.impl.util.Validators;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Real-world unit tests for Validators utility class.
 * Tests cover email validation, string validation, list validation, and map operations.
 */
@DisplayName("Validators Utility Tests")
class ValidatorsTest {

    // ==================== Email Validation Tests ====================

    @Test
    @DisplayName("Should validate correct email addresses")
    void testValidEmailAddresses() {
        // Real-world email formats
        assertTrue(Validators.isEmailValid("user@example.com"));
        assertTrue(Validators.isEmailValid("john.doe@company.com"));
        assertTrue(Validators.isEmailValid("admin@subdomain.example.com"));
        assertTrue(Validators.isEmailValid("support+tag@service.io"));
        assertTrue(Validators.isEmailValid("user123@test-domain.org"));
        assertTrue(Validators.isEmailValid("first.last@example.co.uk"));
    }

    @Test
    @DisplayName("Should reject invalid email addresses")
    void testInvalidEmailAddresses() {
        // Common invalid email formats
        assertFalse(Validators.isEmailValid("notanemail"));
        assertFalse(Validators.isEmailValid("@example.com"));
        assertFalse(Validators.isEmailValid("user@"));
        assertFalse(Validators.isEmailValid("user @example.com")); // space
        assertFalse(Validators.isEmailValid("user@.com"));
        assertFalse(Validators.isEmailValid("user..name@example.com")); // double dot
        assertFalse(Validators.isEmailValid(""));
    }

    @Test
    @DisplayName("Should handle null email address")
    void testNullEmailAddress() {
        assertFalse(Validators.isEmailValid(null));
    }

    @Test
    @DisplayName("Should validate corporate email addresses")
    void testCorporateEmailAddresses() {
        // Real-world corporate email scenarios
        assertTrue(Validators.isEmailValid("employee@microsoft.com"));
        assertTrue(Validators.isEmailValid("contractor@vendor.company.com"));
        assertTrue(Validators.isEmailValid("admin@it-department.org"));
    }

    // ==================== String Validation Tests ====================

    @Test
    @DisplayName("Should identify null strings")
    void testNullString() {
        assertTrue(Validators.isStringNullOrBlank(null));
    }

    @Test
    @DisplayName("Should identify blank strings")
    void testBlankStrings() {
        assertTrue(Validators.isStringNullOrBlank(""));
        assertTrue(Validators.isStringNullOrBlank("   "));
        assertTrue(Validators.isStringNullOrBlank("\t"));
        assertTrue(Validators.isStringNullOrBlank("\n"));
        assertTrue(Validators.isStringNullOrBlank("  \t  \n  "));
    }

    @Test
    @DisplayName("Should identify valid non-blank strings")
    void testValidStrings() {
        assertFalse(Validators.isStringNullOrBlank("Hello"));
        assertFalse(Validators.isStringNullOrBlank("  Hello  ")); // has content
        assertFalse(Validators.isStringNullOrBlank("123"));
        assertFalse(Validators.isStringNullOrBlank("a"));
    }

    @Test
    @DisplayName("Should validate real-world message IDs")
    void testRealWorldMessageIds() {
        // Outlook message IDs are never blank
        String messageId = "AQMkADY4ZTFiMGIxLWU1YjUtNDEwMS04Y2Q0LTJkNzY3YjU5ZjE2MwBGAAADqJ";
        assertFalse(Validators.isStringNullOrBlank(messageId));
    }

    // ==================== List Validation Tests ====================

    @Test
    @DisplayName("Should identify null lists")
    void testNullList() {
        assertTrue(Validators.isListNullOrEmpty(null));
    }

    @Test
    @DisplayName("Should identify empty lists")
    void testEmptyList() {
        assertTrue(Validators.isListNullOrEmpty(new ArrayList<>()));
        assertTrue(Validators.isListNullOrEmpty(List.of()));
        assertTrue(Validators.isListNullOrEmpty(Collections.emptyList()));
    }

    @Test
    @DisplayName("Should identify non-empty lists")
    void testNonEmptyLists() {
        assertFalse(Validators.isListNullOrEmpty(List.of("item")));
        assertFalse(Validators.isListNullOrEmpty(Arrays.asList("a", "b", "c")));
        assertFalse(Validators.isListNullOrEmpty(new ArrayList<>(List.of(1, 2, 3))));
    }

    @Test
    @DisplayName("Should validate email recipient lists")
    void testEmailRecipientLists() {
        // Real-world scenario: checking if email has recipients
        List<String> recipients = Arrays.asList("user1@example.com", "user2@example.com");
        assertFalse(Validators.isListNullOrEmpty(recipients));

        List<String> noRecipients = new ArrayList<>();
        assertTrue(Validators.isListNullOrEmpty(noRecipients));
    }

    @Test
    @DisplayName("Should validate attachment lists")
    void testAttachmentLists() {
        // Real-world scenario: checking if email has attachments
        List<String> attachments = List.of("document.pdf", "image.png");
        assertFalse(Validators.isListNullOrEmpty(attachments));

        List<String> noAttachments = Collections.emptyList();
        assertTrue(Validators.isListNullOrEmpty(noAttachments));
    }

    // ==================== Map Attribute Tests ====================

    @Test
    @DisplayName("Should add non-null attribute to map")
    void testAddNonNullAttribute() {
        Map<String, Object> attributeMap = new HashMap<>();
        Validators.addAttributeIfNotNull(attributeMap, "email", "user@example.com");

        assertEquals(1, attributeMap.size());
        assertEquals("user@example.com", attributeMap.get("email"));
    }

    @Test
    @DisplayName("Should not add null attribute to map")
    void testDoNotAddNullAttribute() {
        Map<String, Object> attributeMap = new HashMap<>();
        Validators.addAttributeIfNotNull(attributeMap, "clientId", null);

        assertEquals(0, attributeMap.size());
        assertFalse(attributeMap.containsKey("clientId"));
    }

    @Test
    @DisplayName("Should add multiple attributes selectively")
    void testAddMultipleAttributesSelectively() {
        Map<String, Object> attributeMap = new HashMap<>();

        // Real-world scenario: building Outlook configuration
        Validators.addAttributeIfNotNull(attributeMap, "email", "admin@company.com");
        Validators.addAttributeIfNotNull(attributeMap, "clientId", "abc123");
        Validators.addAttributeIfNotNull(attributeMap, "clientSecret", null); // not added
        Validators.addAttributeIfNotNull(attributeMap, "tenantId", null); // not added
        Validators.addAttributeIfNotNull(attributeMap, "allowMailAlert", true);

        assertEquals(3, attributeMap.size());
        assertTrue(attributeMap.containsKey("email"));
        assertTrue(attributeMap.containsKey("clientId"));
        assertTrue(attributeMap.containsKey("allowMailAlert"));
        assertFalse(attributeMap.containsKey("clientSecret"));
        assertFalse(attributeMap.containsKey("tenantId"));
    }

    @Test
    @DisplayName("Should handle different object types in map")
    void testDifferentObjectTypes() {
        Map<String, Object> attributeMap = new HashMap<>();

        Validators.addAttributeIfNotNull(attributeMap, "name", "John Doe");
        Validators.addAttributeIfNotNull(attributeMap, "age", 30);
        Validators.addAttributeIfNotNull(attributeMap, "active", true);
        Validators.addAttributeIfNotNull(attributeMap, "salary", 75000.50);
        Validators.addAttributeIfNotNull(attributeMap, "tags", Arrays.asList("admin", "user"));

        assertEquals(5, attributeMap.size());
        assertEquals("John Doe", attributeMap.get("name"));
        assertEquals(30, attributeMap.get("age"));
        assertEquals(true, attributeMap.get("active"));
        assertEquals(75000.50, attributeMap.get("salary"));
        assertEquals(Arrays.asList("admin", "user"), attributeMap.get("tags"));
    }

    @Test
    @DisplayName("Should build Outlook attributes map for public auth")
    void testBuildOutlookPublicAuthAttributes() {
        // Real-world scenario: building attributes for public authentication
        Map<String, Object> attributeMap = new HashMap<>();

        String email = "user@company.com";
        Boolean allowMailAlert = true;
        String authType = "PUBLIC";

        Validators.addAttributeIfNotNull(attributeMap, "email", email);
        Validators.addAttributeIfNotNull(attributeMap, "allowMailAlert", allowMailAlert);
        Validators.addAttributeIfNotNull(attributeMap, "authType", authType);
        Validators.addAttributeIfNotNull(attributeMap, "clientId", null); // public auth doesn't need this
        Validators.addAttributeIfNotNull(attributeMap, "clientSecret", null);
        Validators.addAttributeIfNotNull(attributeMap, "tenantId", null);

        assertEquals(3, attributeMap.size());
        assertEquals("user@company.com", attributeMap.get("email"));
        assertEquals(true, attributeMap.get("allowMailAlert"));
        assertEquals("PUBLIC", attributeMap.get("authType"));
    }

    @Test
    @DisplayName("Should build Outlook attributes map for private auth")
    void testBuildOutlookPrivateAuthAttributes() {
        // Real-world scenario: building attributes for private authentication
        Map<String, Object> attributeMap = new HashMap<>();

        String email = "admin@enterprise.com";
        String clientId = "client-123-abc";
        String clientSecret = "secret-xyz-789";
        String tenantId = "tenant-456-def";
        Boolean allowMailAlert = false;
        String authType = "PRIVATE";

        Validators.addAttributeIfNotNull(attributeMap, "email", email);
        Validators.addAttributeIfNotNull(attributeMap, "clientId", clientId);
        Validators.addAttributeIfNotNull(attributeMap, "clientSecret", clientSecret);
        Validators.addAttributeIfNotNull(attributeMap, "tenantId", tenantId);
        Validators.addAttributeIfNotNull(attributeMap, "allowMailAlert", allowMailAlert);
        Validators.addAttributeIfNotNull(attributeMap, "authType", authType);

        assertEquals(6, attributeMap.size());
        assertEquals("admin@enterprise.com", attributeMap.get("email"));
        assertEquals("client-123-abc", attributeMap.get("clientId"));
        assertEquals("secret-xyz-789", attributeMap.get("clientSecret"));
        assertEquals("tenant-456-def", attributeMap.get("tenantId"));
        assertEquals(false, attributeMap.get("allowMailAlert"));
        assertEquals("PRIVATE", attributeMap.get("authType"));
    }

    // ==================== Integration Scenarios ====================

    @Test
    @DisplayName("Should validate email form submission")
    void testEmailFormValidation() {
        // Real-world scenario: validating email form before sending
        String to = "recipient@example.com";
        String subject = "Meeting Reminder";
        String body = "Don't forget about tomorrow's meeting.";
        List<String> attachments = Arrays.asList("agenda.pdf");

        // Validate all fields
        assertFalse(Validators.isStringNullOrBlank(to));
        assertTrue(Validators.isEmailValid(to));
        assertFalse(Validators.isStringNullOrBlank(subject));
        assertFalse(Validators.isStringNullOrBlank(body));
        assertFalse(Validators.isListNullOrEmpty(attachments));
    }

    @Test
    @DisplayName("Should detect incomplete email form")
    void testIncompleteEmailForm() {
        // Real-world scenario: detecting missing required fields
        String to = "";
        String subject = null;
        String body = "   ";
        List<String> attachments = null;

        // All validations should fail
        assertTrue(Validators.isStringNullOrBlank(to));
        assertTrue(Validators.isStringNullOrBlank(subject));
        assertTrue(Validators.isStringNullOrBlank(body));
        assertTrue(Validators.isListNullOrEmpty(attachments));
    }

    @Test
    @DisplayName("Should validate bulk email recipients")
    void testBulkEmailRecipients() {
        // Real-world scenario: validating multiple recipients
        List<String> recipients = Arrays.asList(
                "user1@company.com",
                "user2@company.com",
                "user3@company.com"
        );

        assertFalse(Validators.isListNullOrEmpty(recipients));

        // Validate each email
        for (String email : recipients) {
            assertTrue(Validators.isEmailValid(email),
                    "Email should be valid: " + email);
        }
    }
}

