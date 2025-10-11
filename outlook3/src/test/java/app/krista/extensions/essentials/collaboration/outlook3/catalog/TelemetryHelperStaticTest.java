package app.krista.extensions.essentials.collaboration.outlook3.catalog;

import app.krista.extensions.essentials.collaboration.outlook3.catalog.extresp.TelemetryHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Real-world unit tests for TelemetryHelper static methods.
 * Tests cover safe tag map creation with null handling.
 */
@DisplayName("TelemetryHelper Static Methods Tests")
class TelemetryHelperStaticTest {

    // ==================== Basic Tag Map Creation ====================

    @Test
    @DisplayName("Should create tag map with single key-value pair")
    void testSingleKeyValue() {
        Map<String, String> tags = TelemetryHelper.safeTagMap("operation", "sendMail");

        assertEquals(1, tags.size());
        assertEquals("sendMail", tags.get("operation"));
    }

    @Test
    @DisplayName("Should create tag map with multiple key-value pairs")
    void testMultipleKeyValues() {
        Map<String, String> tags = TelemetryHelper.safeTagMap(
                "operation", "sendMail",
                "user", "john@example.com",
                "status", "success"
        );

        assertEquals(3, tags.size());
        assertEquals("sendMail", tags.get("operation"));
        assertEquals("john@example.com", tags.get("user"));
        assertEquals("success", tags.get("status"));
    }

    @Test
    @DisplayName("Should create empty map with no arguments")
    void testEmptyMap() {
        Map<String, String> tags = TelemetryHelper.safeTagMap();

        assertNotNull(tags);
        assertEquals(0, tags.size());
    }

    // ==================== Null Value Handling ====================

    @Test
    @DisplayName("Should replace null value with NA")
    void testNullValueReplacement() {
        Map<String, String> tags = TelemetryHelper.safeTagMap("key", null);

        assertEquals(1, tags.size());
        assertEquals("NA", tags.get("key"));
    }

    @Test
    @DisplayName("Should replace multiple null values with NA")
    void testMultipleNullValues() {
        Map<String, String> tags = TelemetryHelper.safeTagMap(
                "key1", null,
                "key2", "value2",
                "key3", null
        );

        assertEquals(3, tags.size());
        assertEquals("NA", tags.get("key1"));
        assertEquals("value2", tags.get("key2"));
        assertEquals("NA", tags.get("key3"));
    }

    @Test
    @DisplayName("Should handle all null values")
    void testAllNullValues() {
        Map<String, String> tags = TelemetryHelper.safeTagMap(
                "key1", null,
                "key2", null,
                "key3", null
        );

        assertEquals(3, tags.size());
        assertEquals("NA", tags.get("key1"));
        assertEquals("NA", tags.get("key2"));
        assertEquals("NA", tags.get("key3"));
    }

    // ==================== Real-World Telemetry Scenarios ====================

    @Test
    @DisplayName("Should create tags for email send operation")
    void testEmailSendTags() {
        Map<String, String> tags = TelemetryHelper.safeTagMap(
                "operation", "sendMail",
                "to", "recipient@example.com",
                "has_attachments", "true",
                "allow_retry", "false"
        );

        assertEquals(4, tags.size());
        assertEquals("sendMail", tags.get("operation"));
        assertEquals("recipient@example.com", tags.get("to"));
        assertEquals("true", tags.get("has_attachments"));
        assertEquals("false", tags.get("allow_retry"));
    }

    @Test
    @DisplayName("Should create tags for email fetch operation")
    void testEmailFetchTags() {
        Map<String, String> tags = TelemetryHelper.safeTagMap(
                "operation", "fetchInbox",
                "page_number", "1",
                "page_size", "10",
                "allow_retry", "true"
        );

        assertEquals(4, tags.size());
        assertEquals("fetchInbox", tags.get("operation"));
        assertEquals("1", tags.get("page_number"));
        assertEquals("10", tags.get("page_size"));
        assertEquals("true", tags.get("allow_retry"));
    }

    @Test
    @DisplayName("Should create tags for move message operation")
    void testMoveMessageTags() {
        Map<String, String> tags = TelemetryHelper.safeTagMap(
                "operation", "moveMessage",
                "message_id", "AQMkADY4ZTFi...",
                "destination_folder", "Archive",
                "allow_retry", "true"
        );

        assertEquals(4, tags.size());
        assertEquals("moveMessage", tags.get("operation"));
        assertEquals("AQMkADY4ZTFi...", tags.get("message_id"));
        assertEquals("Archive", tags.get("destination_folder"));
        assertEquals("true", tags.get("allow_retry"));
    }

    @Test
    @DisplayName("Should create tags with null optional parameters")
    void testTagsWithNullOptionalParams() {
        // Real-world scenario: optional CC and BCC are null
        Map<String, String> tags = TelemetryHelper.safeTagMap(
                "operation", "sendMail",
                "to", "recipient@example.com",
                "cc", null,
                "bcc", null,
                "allow_retry", "false"
        );

        assertEquals(5, tags.size());
        assertEquals("sendMail", tags.get("operation"));
        assertEquals("recipient@example.com", tags.get("to"));
        assertEquals("NA", tags.get("cc"));
        assertEquals("NA", tags.get("bcc"));
        assertEquals("false", tags.get("allow_retry"));
    }

    // ==================== Validation Error Tracking ====================

    @Test
    @DisplayName("Should create tags for validation error")
    void testValidationErrorTags() {
        Map<String, String> tags = TelemetryHelper.safeTagMap(
                "operation", "sendMail",
                "error_type", "validation",
                "field", "to",
                "validation_count", "1",
                "allow_retry", "true"
        );

        assertEquals(5, tags.size());
        assertEquals("sendMail", tags.get("operation"));
        assertEquals("validation", tags.get("error_type"));
        assertEquals("to", tags.get("field"));
        assertEquals("1", tags.get("validation_count"));
        assertEquals("true", tags.get("allow_retry"));
    }

    @Test
    @DisplayName("Should create tags for retry prompted scenario")
    void testRetryPromptedTags() {
        Map<String, String> tags = TelemetryHelper.safeTagMap(
                "operation", "markMessage",
                "message_id", "msg123",
                "validation_count", "2",
                "allow_retry", "true",
                "retry_prompted", "true"
        );

        assertEquals(5, tags.size());
        assertEquals("markMessage", tags.get("operation"));
        assertEquals("msg123", tags.get("message_id"));
        assertEquals("2", tags.get("validation_count"));
        assertEquals("true", tags.get("allow_retry"));
        assertEquals("true", tags.get("retry_prompted"));
    }

    // ==================== Success Tracking ====================

    @Test
    @DisplayName("Should create tags for successful operation")
    void testSuccessfulOperationTags() {
        Map<String, String> tags = TelemetryHelper.safeTagMap(
                "operation", "fetchMailByMessageId",
                "message_id", "msg456",
                "allow_retry", "false",
                "status", "success"
        );

        assertEquals(4, tags.size());
        assertEquals("fetchMailByMessageId", tags.get("operation"));
        assertEquals("msg456", tags.get("message_id"));
        assertEquals("false", tags.get("allow_retry"));
        assertEquals("success", tags.get("status"));
    }

    // ==================== Boolean Parameter Tracking ====================

    @Test
    @DisplayName("Should track allow retry parameter states")
    void testAllowRetryParameterStates() {
        // Test true state
        Map<String, String> tagsTrue = TelemetryHelper.safeTagMap(
                "operation", "sendMail",
                "allow_retry", "true"
        );
        assertEquals("true", tagsTrue.get("allow_retry"));

        // Test false state
        Map<String, String> tagsFalse = TelemetryHelper.safeTagMap(
                "operation", "sendMail",
                "allow_retry", "false"
        );
        assertEquals("false", tagsFalse.get("allow_retry"));

        // Test null state (converted to "NA")
        Map<String, String> tagsNull = TelemetryHelper.safeTagMap(
                "operation", "sendMail",
                "allow_retry", null
        );
        assertEquals("NA", tagsNull.get("allow_retry"));
    }

    // ==================== Pagination Tracking ====================

    @Test
    @DisplayName("Should create tags for pagination parameters")
    void testPaginationTags() {
        Map<String, String> tags = TelemetryHelper.safeTagMap(
                "operation", "fetchInbox",
                "page_number", "2",
                "page_size", "15",
                "total_pages", "5",
                "allow_retry", "true"
        );

        assertEquals(5, tags.size());
        assertEquals("fetchInbox", tags.get("operation"));
        assertEquals("2", tags.get("page_number"));
        assertEquals("15", tags.get("page_size"));
        assertEquals("5", tags.get("total_pages"));
        assertEquals("true", tags.get("allow_retry"));
    }

    // ==================== Category Management Tracking ====================

    @Test
    @DisplayName("Should create tags for add category operation")
    void testAddCategoryTags() {
        Map<String, String> tags = TelemetryHelper.safeTagMap(
                "operation", "addCategoryToMessage",
                "message_id", "msg789",
                "category", "Important",
                "allow_retry", "true"
        );

        assertEquals(4, tags.size());
        assertEquals("addCategoryToMessage", tags.get("operation"));
        assertEquals("msg789", tags.get("message_id"));
        assertEquals("Important", tags.get("category"));
        assertEquals("true", tags.get("allow_retry"));
    }

    @Test
    @DisplayName("Should create tags for remove category operation")
    void testRemoveCategoryTags() {
        Map<String, String> tags = TelemetryHelper.safeTagMap(
                "operation", "removeCategoryFromMessage",
                "message_id", "msg101",
                "category", "Work",
                "allow_retry", "false"
        );

        assertEquals(4, tags.size());
        assertEquals("removeCategoryFromMessage", tags.get("operation"));
        assertEquals("msg101", tags.get("message_id"));
        assertEquals("Work", tags.get("category"));
        assertEquals("false", tags.get("allow_retry"));
    }

    // ==================== Label/Folder Operations ====================

    @Test
    @DisplayName("Should create tags for fetch mails by label")
    void testFetchMailsByLabelTags() {
        Map<String, String> tags = TelemetryHelper.safeTagMap(
                "operation", "fetchMailsByLabel",
                "label", "Archive",
                "page_number", "1",
                "page_size", "10",
                "allow_retry", "true"
        );

        assertEquals(5, tags.size());
        assertEquals("fetchMailsByLabel", tags.get("operation"));
        assertEquals("Archive", tags.get("label"));
        assertEquals("1", tags.get("page_number"));
        assertEquals("10", tags.get("page_size"));
        assertEquals("true", tags.get("allow_retry"));
    }

    @Test
    @DisplayName("Should create tags for fetch all labels")
    void testFetchAllLabelsTags() {
        Map<String, String> tags = TelemetryHelper.safeTagMap(
                "operation", "fetchAllLabels",
                "allow_retry", "false",
                "label_count", "15"
        );

        assertEquals(3, tags.size());
        assertEquals("fetchAllLabels", tags.get("operation"));
        assertEquals("false", tags.get("allow_retry"));
        assertEquals("15", tags.get("label_count"));
    }

    // ==================== Reply Operations ====================

    @Test
    @DisplayName("Should create tags for reply to mail")
    void testReplyToMailTags() {
        Map<String, String> tags = TelemetryHelper.safeTagMap(
                "operation", "replyToMail",
                "message_id", "original-msg-123",
                "body_type", "HTML",
                "allow_retry", "true"
        );

        assertEquals(4, tags.size());
        assertEquals("replyToMail", tags.get("operation"));
        assertEquals("original-msg-123", tags.get("message_id"));
        assertEquals("HTML", tags.get("body_type"));
        assertEquals("true", tags.get("allow_retry"));
    }

    @Test
    @DisplayName("Should create tags for forward mail")
    void testForwardMailTags() {
        Map<String, String> tags = TelemetryHelper.safeTagMap(
                "operation", "forwardMail",
                "message_id", "fwd-msg-456",
                "to", "recipient@example.com",
                "body_type", "TEXT",
                "allow_retry", "false"
        );

        assertEquals(5, tags.size());
        assertEquals("forwardMail", tags.get("operation"));
        assertEquals("fwd-msg-456", tags.get("message_id"));
        assertEquals("recipient@example.com", tags.get("to"));
        assertEquals("TEXT", tags.get("body_type"));
        assertEquals("false", tags.get("allow_retry"));
    }

    // ==================== Edge Cases ====================

    @Test
    @DisplayName("Should handle odd number of arguments gracefully")
    void testOddNumberOfArguments() {
        // If odd number of arguments, last key will have no value
        // This tests the boundary condition
        Map<String, String> tags = TelemetryHelper.safeTagMap(
                "key1", "value1",
                "key2", "value2",
                "key3" // No value for key3
        );

        // Should only process complete pairs
        assertEquals(2, tags.size());
        assertEquals("value1", tags.get("key1"));
        assertEquals("value2", tags.get("key2"));
        assertFalse(tags.containsKey("key3"));
    }

    @Test
    @DisplayName("Should handle empty string values")
    void testEmptyStringValues() {
        Map<String, String> tags = TelemetryHelper.safeTagMap(
                "key1", "",
                "key2", "value2",
                "key3", ""
        );

        assertEquals(3, tags.size());
        assertEquals("", tags.get("key1"));
        assertEquals("value2", tags.get("key2"));
        assertEquals("", tags.get("key3"));
    }

    @Test
    @DisplayName("Should preserve whitespace in values")
    void testWhitespaceInValues() {
        Map<String, String> tags = TelemetryHelper.safeTagMap(
                "key1", "  value with spaces  ",
                "key2", "\tvalue with tab"
        );

        assertEquals(2, tags.size());
        assertEquals("  value with spaces  ", tags.get("key1"));
        assertEquals("\tvalue with tab", tags.get("key2"));
    }
}

