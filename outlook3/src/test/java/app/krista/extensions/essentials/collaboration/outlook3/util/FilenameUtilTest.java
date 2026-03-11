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

import app.krista.extensions.essentials.collaboration.outlook3.impl.util.FilenameUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Real-world unit tests for FilenameUtil utility class.
 * Tests cover filename sanitization, special character handling, and cross-platform safety.
 */
@DisplayName("FilenameUtil Tests")
class FilenameUtilTest {

    // ==================== Basic Filename Sanitization ====================

    @Test
    @DisplayName("Should preserve valid simple filenames")
    void testValidSimpleFilenames() {
        assertEquals("document.pdf", FilenameUtil.toSafeFilename("document.pdf"));
        assertEquals("image.png", FilenameUtil.toSafeFilename("image.png"));
        assertEquals("report.docx", FilenameUtil.toSafeFilename("report.docx"));
        assertEquals("data.xlsx", FilenameUtil.toSafeFilename("data.xlsx"));
    }

    @Test
    @DisplayName("Should preserve filenames with spaces")
    void testFilenamesWithSpaces() {
        assertEquals("My Document.pdf", FilenameUtil.toSafeFilename("My Document.pdf"));
        assertEquals("Project Report 2024.docx", FilenameUtil.toSafeFilename("Project Report 2024.docx"));
        assertEquals("Meeting Notes.txt", FilenameUtil.toSafeFilename("Meeting Notes.txt"));
    }

    @Test
    @DisplayName("Should preserve filenames with underscores and hyphens")
    void testFilenamesWithUnderscoresAndHyphens() {
        assertEquals("project_report.pdf", FilenameUtil.toSafeFilename("project_report.pdf"));
        assertEquals("meeting-notes-2024.docx", FilenameUtil.toSafeFilename("meeting-notes-2024.docx"));
        assertEquals("file_name-with-both.txt", FilenameUtil.toSafeFilename("file_name-with-both.txt"));
    }

    // ==================== Special Character Handling ====================

    @Test
    @DisplayName("Should replace path separator characters")
    void testPathSeparatorReplacement() {
        // Windows and Unix path separators should be replaced
        assertEquals("folder_file.pdf", FilenameUtil.toSafeFilename("folder/file.pdf"));
        assertEquals("folder_file.pdf", FilenameUtil.toSafeFilename("folder\\file.pdf"));
        assertEquals("path_to_file.txt", FilenameUtil.toSafeFilename("path/to/file.txt"));
    }

    @Test
    @DisplayName("Should replace reserved Windows characters")
    void testReservedWindowsCharacters() {
        // < > : " / \ | ? *
        assertEquals("file_name.pdf", FilenameUtil.toSafeFilename("file<name.pdf"));
        assertEquals("file_name.pdf", FilenameUtil.toSafeFilename("file>name.pdf"));
        assertEquals("file_name.pdf", FilenameUtil.toSafeFilename("file:name.pdf"));
        assertEquals("file_name.pdf", FilenameUtil.toSafeFilename("file\"name.pdf"));
        assertEquals("file_name.pdf", FilenameUtil.toSafeFilename("file|name.pdf"));
        assertEquals("file_name.pdf", FilenameUtil.toSafeFilename("file?name.pdf"));
        assertEquals("file_name.pdf", FilenameUtil.toSafeFilename("file*name.pdf"));
    }

    @Test
    @DisplayName("Should handle control characters")
    void testControlCharacters() {
        assertEquals("file_name.pdf", FilenameUtil.toSafeFilename("file\nname.pdf"));
        assertEquals("file_name.pdf", FilenameUtil.toSafeFilename("file\tname.pdf"));
        assertEquals("file_name.pdf", FilenameUtil.toSafeFilename("file\rname.pdf"));
    }

    // ==================== Unicode and International Characters ====================

    @Test
    @DisplayName("Should replace non-ASCII characters")
    void testNonAsciiCharacters() {
        // Unicode characters should be replaced with underscores
        assertEquals("file_name.pdf", FilenameUtil.toSafeFilename("file™name.pdf"));
        assertEquals("file_name.pdf", FilenameUtil.toSafeFilename("file©name.pdf"));
        assertEquals("file_name.pdf", FilenameUtil.toSafeFilename("file®name.pdf"));
    }

    @Test
    @DisplayName("Should handle accented characters")
    void testAccentedCharacters() {
        // After normalization, accented characters are replaced
        String result = FilenameUtil.toSafeFilename("résumé.pdf");
        assertTrue(result.matches("[A-Za-z0-9._ -]+"));
        assertTrue(result.endsWith(".pdf"));
    }

    @Test
    @DisplayName("Should handle emoji and special symbols")
    void testEmojiAndSymbols() {
        String result = FilenameUtil.toSafeFilename("file😀name.pdf");
        assertTrue(result.matches("[A-Za-z0-9._ -]+"));
        assertTrue(result.endsWith(".pdf"));
    }

    // ==================== Edge Cases ====================

    @Test
    @DisplayName("Should handle null filename")
    void testNullFilename() {
        assertEquals("attachment", FilenameUtil.toSafeFilename(null));
    }

    @Test
    @DisplayName("Should handle empty filename")
    void testEmptyFilename() {
        assertEquals("attachment", FilenameUtil.toSafeFilename(""));
        assertEquals("attachment", FilenameUtil.toSafeFilename("   "));
    }

    @Test
    @DisplayName("Should handle filename with only extension")
    void testFilenameWithOnlyExtension() {
        // When filename starts with dot, the dot is trimmed and extension becomes the name
        assertEquals("pdf", FilenameUtil.toSafeFilename(".pdf"));
        assertEquals("txt", FilenameUtil.toSafeFilename(".txt"));
    }

    @Test
    @DisplayName("Should handle filename without extension")
    void testFilenameWithoutExtension() {
        assertEquals("document", FilenameUtil.toSafeFilename("document"));
        assertEquals("readme", FilenameUtil.toSafeFilename("readme"));
    }

    @Test
    @DisplayName("Should handle filename with multiple dots")
    void testFilenameWithMultipleDots() {
        assertEquals("file.backup.pdf", FilenameUtil.toSafeFilename("file.backup.pdf"));
        assertEquals("archive.tar.gz", FilenameUtil.toSafeFilename("archive.tar.gz"));
    }

    // ==================== Length Truncation ====================

    @Test
    @DisplayName("Should truncate very long filenames")
    void testLongFilename() {
        // Create a filename longer than 100 characters
        String longName = "a".repeat(150) + ".pdf";
        String result = FilenameUtil.toSafeFilename(longName);

        // Should be truncated to 100 chars + extension
        assertTrue(result.length() <= 104); // 100 + ".pdf"
        assertTrue(result.endsWith(".pdf"));
    }

    @Test
    @DisplayName("Should preserve extension when truncating")
    void testTruncationPreservesExtension() {
        String longName = "VeryLongDocumentNameThatExceedsTheMaximumAllowedLengthForFilenamesAndNeedsToBeT" +
                "runcatedToFitWithinTheReasonableLimitsSetByTheSystem.docx";
        String result = FilenameUtil.toSafeFilename(longName);

        assertTrue(result.endsWith(".docx"));
        assertTrue(result.length() <= 105); // 100 + ".docx"
    }

    // ==================== Multiple Spaces and Underscores ====================

    @Test
    @DisplayName("Should collapse multiple spaces")
    void testMultipleSpaces() {
        assertEquals("file name.pdf", FilenameUtil.toSafeFilename("file    name.pdf"));
        assertEquals("multiple spaces here.txt", FilenameUtil.toSafeFilename("multiple  spaces   here.txt"));
    }

    @Test
    @DisplayName("Should collapse multiple underscores")
    void testMultipleUnderscores() {
        assertEquals("file_name.pdf", FilenameUtil.toSafeFilename("file___name.pdf"));
        assertEquals("multiple_underscores.txt", FilenameUtil.toSafeFilename("multiple____underscores.txt"));
    }

    // ==================== Real-World Email Attachment Scenarios ====================

    @Test
    @DisplayName("Should sanitize Outlook email attachment names")
    void testOutlookAttachmentNames() {
        // Real-world Outlook attachment scenarios - spaces are preserved
        assertEquals("Q4 Report.xlsx", FilenameUtil.toSafeFilename("Q4 Report.xlsx"));
        assertEquals("Meeting Notes 2024-01-15.docx", FilenameUtil.toSafeFilename("Meeting Notes 2024-01-15.docx"));
        assertEquals("Invoice _12345.pdf", FilenameUtil.toSafeFilename("Invoice #12345.pdf"));
    }

    @Test
    @DisplayName("Should handle attachments with problematic characters")
    void testProblematicAttachmentNames() {
        // Common problematic attachment names - spaces preserved, special chars replaced
        assertEquals("Contract v2.0.pdf", FilenameUtil.toSafeFilename("Contract v2.0.pdf"));
        assertEquals("Budget 2024 _.xlsx", FilenameUtil.toSafeFilename("Budget 2024 ?.xlsx"));
        assertEquals("Presentation _Final_.pptx", FilenameUtil.toSafeFilename("Presentation (Final).pptx"));
    }

    @Test
    @DisplayName("Should handle forwarded email attachments")
    void testForwardedEmailAttachments() {
        // Forwarded emails often have FW: or RE: prefixes - colon replaced, spaces preserved
        assertEquals("FW_ Important Document.pdf", FilenameUtil.toSafeFilename("FW: Important Document.pdf"));
        assertEquals("RE_ Meeting Agenda.docx", FilenameUtil.toSafeFilename("RE: Meeting Agenda.docx"));
    }

    // ==================== UUID Fallback Tests ====================

    @Test
    @DisplayName("Should generate UUID fallback with extension")
    void testUuidFallbackWithExtension() {
        String result = FilenameUtil.uuidFallback("document.pdf");

        assertTrue(result.startsWith("file-"));
        assertTrue(result.endsWith(".pdf"));
        assertTrue(result.matches("file-[a-f0-9]{8}\\.pdf"));
    }

    @Test
    @DisplayName("Should generate UUID fallback without extension")
    void testUuidFallbackWithoutExtension() {
        String result = FilenameUtil.uuidFallback("document");

        assertTrue(result.startsWith("file-"));
        assertTrue(result.matches("file-[a-f0-9]{8}"));
        assertFalse(result.contains("."));
    }

    @Test
    @DisplayName("Should generate UUID fallback for problematic filename")
    void testUuidFallbackForProblematicFilename() {
        String result = FilenameUtil.uuidFallback("???###.pdf");

        assertTrue(result.startsWith("file-"));
        assertTrue(result.endsWith(".pdf"));
        assertTrue(result.matches("file-[a-f0-9]{8}\\.pdf"));
    }

    @Test
    @DisplayName("Should generate unique UUID fallbacks")
    void testUniqueUuidFallbacks() {
        String result1 = FilenameUtil.uuidFallback("file.pdf");
        String result2 = FilenameUtil.uuidFallback("file.pdf");

        // Should generate different UUIDs
        assertNotEquals(result1, result2);
        assertTrue(result1.startsWith("file-"));
        assertTrue(result2.startsWith("file-"));
    }

    // ==================== Cross-Platform Safety ====================

    @Test
    @DisplayName("Should create Windows-safe filenames")
    void testWindowsSafeFilenames() {
        // Windows reserved characters: < > : " / \ | ? *
        String windowsUnsafe = "file<>:\"/\\|?*.pdf";
        String result = FilenameUtil.toSafeFilename(windowsUnsafe);

        assertFalse(result.contains("<"));
        assertFalse(result.contains(">"));
        assertFalse(result.contains(":"));
        assertFalse(result.contains("\""));
        assertFalse(result.contains("/"));
        assertFalse(result.contains("\\"));
        assertFalse(result.contains("|"));
        assertFalse(result.contains("?"));
        assertFalse(result.contains("*"));
    }

    @Test
    @DisplayName("Should create Unix-safe filenames")
    void testUnixSafeFilenames() {
        // Unix problematic characters
        String unixProblematic = "file/with/slashes.pdf";
        String result = FilenameUtil.toSafeFilename(unixProblematic);

        assertFalse(result.contains("/"));
        assertTrue(result.endsWith(".pdf"));
    }

    @Test
    @DisplayName("Should create macOS-safe filenames")
    void testMacOsSafeFilenames() {
        // macOS problematic characters (similar to Unix)
        String macProblematic = "file:with:colons.pdf";
        String result = FilenameUtil.toSafeFilename(macProblematic);

        assertFalse(result.contains(":"));
        assertTrue(result.endsWith(".pdf"));
    }

    // ==================== Integration Scenarios ====================

    @Test
    @DisplayName("Should sanitize batch of email attachments")
    void testBatchAttachmentSanitization() {
        String[] attachments = {
                "Invoice #12345.pdf",
                "Contract (Final).docx",
                "Budget 2024?.xlsx",
                "Presentation: Q4 Results.pptx",
                "Meeting/Notes.txt"
        };

        for (String attachment : attachments) {
            String sanitized = FilenameUtil.toSafeFilename(attachment);

            // All should be valid ASCII-only filenames
            assertTrue(sanitized.matches("[A-Za-z0-9._ -]+"),
                    "Sanitized filename should be ASCII-only: " + sanitized);

            // All should preserve extensions
            String originalExt = attachment.substring(attachment.lastIndexOf('.'));
            assertTrue(sanitized.endsWith(originalExt),
                    "Extension should be preserved: " + sanitized);
        }
    }

    @Test
    @DisplayName("Should handle real-world Outlook attachment scenarios")
    void testRealWorldOutlookScenarios() {
        // Scenario 1: User uploads file with special characters
        String userFile = "My Document™ (2024).pdf";
        String sanitized1 = FilenameUtil.toSafeFilename(userFile);
        assertTrue(sanitized1.matches("[A-Za-z0-9._ -]+"));

        // Scenario 2: Forwarded attachment with email subject
        String forwardedFile = "FW: RE: Important - Please Review!.docx";
        String sanitized2 = FilenameUtil.toSafeFilename(forwardedFile);
        assertTrue(sanitized2.matches("[A-Za-z0-9._ -]+"));

        // Scenario 3: File from different OS
        String crossPlatformFile = "path\\to\\file.xlsx";
        String sanitized3 = FilenameUtil.toSafeFilename(crossPlatformFile);
        assertFalse(sanitized3.contains("\\"));
    }
}

