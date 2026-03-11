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

package app.krista.extensions.essentials.collaboration.outlook3.impl.util;

import app.krista.extensions.essentials.collaboration.outlook3.service.Email;
import app.krista.extensions.essentials.collaboration.outlook3.service.EmailAddress;
import app.krista.ksdk.entities.Entities;
import app.krista.model.base.EntityValue;
import app.krista.model.base.File;
import app.krista.model.entity.EntityAttributeField;
import com.google.gson.reflect.TypeToken;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.*;

import static app.krista.extensions.essentials.collaboration.outlook3.impl.util.Constants.*;

/**
 * Comprehensive utility class providing helper methods for entity data transformation,
 * email formatting, HTML table generation, and file operations in the Outlook extension.
 *
 * <p>This class offers a wide range of static utility methods including:
 * <ul>
 *   <li>Entity data conversion and validation</li>
 *   <li>HTML table generation from entity lists</li>
 *   <li>Email address parsing and formatting</li>
 *   <li>Message content formatting with thread history</li>
 *   <li>File type detection and content reading</li>
 *   <li>Date/time formatting and number formatting</li>
 * </ul>
 * All methods are static and the class cannot be instantiated.</p>
 */
public class EntityHelperUtil {
    private EntityHelperUtil() {
    }

    /**
     * Converts a list of EntityValue objects into a list of maps with formatted data.
     *
     * <p>This method transforms entity values into a more accessible map format, applying
     * special formatting for percentage fields and validating data for display. If an entity
     * registry is provided, it uses entity definitions to apply field-specific transformations
     * such as converting percentage values to human-readable format (e.g., 0.75 to "75 %").</p>
     *
     * @param entityValues the list of EntityValue objects to convert
     * @param registry the entity registry for accessing entity definitions; can be null
     * @return a list of maps where each map represents an entity's field data in display format
     */
    public static List<Map<String, Object>> getEntityDataAsList(List<EntityValue> entityValues, Entities registry) {
        List<Map<String, Object>> entityDataToMap = new ArrayList<>();
        for (EntityValue entity : entityValues) {
            if (registry != null) {
                Map<String, EntityAttributeField> attributeFields = registry.getEntityDefinition(entity).getAttributeFields();
                prepareEntity(entity, attributeFields);
            }
            Map<String, Object> validEntityMapData = getValidatedData(entity.getFields());
            entityDataToMap.add(validEntityMapData);
        }
        return entityDataToMap;
    }

    private static void prepareEntity(EntityValue entity, Map<String, EntityAttributeField> attributeFields) {
        for (Map.Entry<String, EntityAttributeField> keyValue : attributeFields.entrySet()) {
            EntityAttributeField field = keyValue.getValue();
            if (field.getFieldType().equals("com.krista.fields.Percentage")) {
                Double percent = (Double) entity.getFields().get(keyValue.getKey()) * 100;
                String value = EntityHelperUtil.removeTrailingZeros(percent) + " %";
                entity.getFields().put(keyValue.getKey(), value);
            }
        }
    }


    /**
     * Transforms entity field values into user-friendly, non-technical format.
     *
     * <p>This method converts technical boolean values (true/false) into readable
     * text ("Yes"/"No") while preserving all other values unchanged. The transformation
     * maintains the original map's key-value structure in a LinkedHashMap to preserve
     * insertion order.</p>
     *
     * @param entityMap the map of entity field names to their values
     * @return a new map with boolean values converted to "Yes"/"No" and other values unchanged
     */
    public static Map<String, Object> getValidatedData(Map<String, Object> entityMap) {
        Map<String, Object> validatedEntityMap = new LinkedHashMap<>();
        entityMap.forEach((key, value) ->
                validatedEntityMap.put(key, value.equals(Boolean.TRUE) ? "Yes" : value.equals(Boolean.FALSE) ? "No" : value)
        );
        return validatedEntityMap;
    }

    /**
     * Intelligently formats a long value as a date or time string based on the field name.
     **
     * @param longValue the timestamp value in milliseconds to format
     * @param key the field name used to determine if this is a date or time field
     * @return a formatted date/time string if the key matches date/time patterns, otherwise the string value of longValue
     */
    public static String fetchDateTime(Object longValue, String key) {
        for (String dateKey : Constants.VALID_DATE_KEYS) {
            if (key.toLowerCase().contains(dateKey)) {
                return new SimpleDateFormat("MMM dd, yyyy hh:mm a").format(new Date((long) longValue));
            }
        }
        for (String timeKey : Constants.VALID_TIME_KEYS) {
            if (key.toLowerCase().contains(timeKey)) {
                return new SimpleDateFormat("hh:mm a").format(new Date((long) longValue));
            }
        }
        return String.valueOf(longValue);
    }

    /**
     * Generates HTML content combining a message with an entity data table.
     *
     * @param message the message text to display above the table; newlines are converted to HTML breaks
     * @param entityList the list of entity values to display in table format
     * @param removeEntityFieldFromTable list of field names to exclude from the table; can be null
     * @param registry the entity registry for accessing entity definitions; can be null
     * @return a complete HTML string with styled message and entity data table
     * @throws IllegalArgumentException if entityList is null or empty
     */
    public static String getMessageContent(String message, List<EntityValue> entityList, List<String> removeEntityFieldFromTable, Entities registry) {
        StringBuilder htmlContent = new StringBuilder();
        formHTMLForTable(htmlContent, message);
        if (entityList != null && !entityList.isEmpty()) {
            List<Map<String, Object>> entitiesData = getEntityDataAsList(entityList, registry);
            if (removeEntityFieldFromTable != null && !removeEntityFieldFromTable.isEmpty()) {
                for (String field : removeEntityFieldFromTable) {
                    entitiesData.forEach(data -> data.remove(field));
                }
            }

            List<String> headerKeys = new ArrayList<>(entitiesData.getFirst().keySet());
            int headerSize = headerKeys.size();
            addTableHeader(htmlContent, headerKeys, headerSize);
            addTableData(htmlContent, entitiesData, headerKeys, headerSize);
            return htmlContent.toString();
        } else {
            throw new IllegalArgumentException(Constants.PLEASE_PROVIDE_LIST_OF_ENTITY_VALUES);
        }
    }

    private static void formHTMLForTable(StringBuilder htmlContent, String message) {
        htmlContent.append(Constants.HTML_HEAD);
        htmlContent.append(Constants.HTML_TABLE_STYLE);
        htmlContent.append(Constants.CLOSE_HEAD_TAG);
        htmlContent.append(Constants.BODY_DIV_TAG);
        htmlContent.append(message.replace(Constants.NEW_LINE, Constants.BR_TAG));
        htmlContent.append(Constants.CLOSE_DIV_TAG);
        htmlContent.append(Constants.TABLE_START);
    }

    private static void addTableData(StringBuilder htmlContent, List<Map<String, Object>> entitiesData, List<String> headerKeys, int headerSize) {

        for (Map<String, Object> entitiesDatum : entitiesData) {
            htmlContent.append(Constants.TR_TAG);
            for (int cellIndex = 0; cellIndex < headerSize; cellIndex++) {
                htmlContent.append(Constants.TD_TAG);
                Object cellData = entitiesDatum.getOrDefault(headerKeys.get(cellIndex), "");
                String value = (cellData instanceof Long)
                        ? EntityHelperUtil.fetchDateTime(cellData, headerKeys.get(cellIndex))
                        : (cellData instanceof Double)
                        ? removeTrailingZeros((Double) cellData)
                        : String.valueOf(cellData);
                htmlContent.append(value);
                htmlContent.append(Constants.CLOSE_TD_TAG);
            }
            htmlContent.append(Constants.CLOSE_TR_TAG);
        }
        htmlContent.append(Constants.CLOSE_TABLE_TAG);
        htmlContent.append(Constants.CLOSE_BODY_TAG);
    }

    private static void addTableHeader(StringBuilder htmlContent, List<String> headerKeys, int headerSize) {
        htmlContent.append(Constants.TR_TAG);
        for (int hdrCell = 0; hdrCell < headerSize; hdrCell++) {
            htmlContent.append(Constants.TH_TAG);
            htmlContent.append(headerKeys.get(hdrCell));
            htmlContent.append(Constants.CLOSE_TH_TAG);
        }
        htmlContent.append(Constants.CLOSE_TR_TAG);
    }

    /**
     * Converts a list of EmailAddress objects into a comma-separated string of email addresses.
     *
     * <p>This method extracts the mail address from each EmailAddress object and joins them
     * with commas. If the input list is null or empty, returns an empty string.</p>
     *
     * @param emails the list of EmailAddress objects to convert
     * @return a comma-separated string of email addresses, or empty string if input is null/empty
     */
    public static String getCommaSeparatedEmail(List<EmailAddress> emails) {
        if (emails == null || emails.isEmpty()) {
            return Constants.EMPTY_STRING;
        }
        StringBuilder emailString = new StringBuilder();
        for (EmailAddress email : emails) {
            emailString.append(email.getMailAddress()).append(Constants.COMMA);
        }
        emailString.setLength(emailString.length() - 1);
        return emailString.toString();
    }

    /**
     * Re-parses a list of File objects through Gson to ensure proper type conversion.
     *
     * <p>This method works around Gson type conversion issues by serializing the file list
     * to JSON and then deserializing it back to a properly typed ArrayList of File objects.
     * This ensures that all file objects have the correct runtime type expected by the system.</p>
     *
     * @param attachments the list of File objects to re-parse
     * @return a properly typed ArrayList of File objects, or empty list if input is null/empty
     */
    public static List<File> getAttachmentsByParsingIntoJsonMapper(List<File> attachments) {
        if (attachments == null || attachments.isEmpty()) {
            return List.of();
        }

        // Hack to resolve Gson conversation
        Type fileListType = new TypeToken<ArrayList<File>>() {
        }.getType();
        attachments = GSON.fromJson(GSON.toJson(attachments), fileListType);
        return attachments;
    }

    /**
     * Determines the MIME type of a file based on its filename.
     *
     * <p>This method uses URLConnection's content type guessing mechanism to determine
     * the file's MIME type from its extension. If the type cannot be determined,
     * defaults to "application/x-binary".</p>
     *
     * @param file the file whose MIME type should be determined
     * @return the MIME type string, or "application/x-binary" if type cannot be determined
     */
    public static String getFileType(java.io.File file) {
        String contentType = URLConnection.guessContentTypeFromName(file.getName());
        return contentType != null ? contentType : Constants.APPLICATION_X_BINARY;
    }

    /**
     * Reads the entire content of a file into a byte array.
     *
     * @param ioFile the file to read
     * @return a byte array containing the complete file content
     * @throws IllegalArgumentException if the file cannot be read or is empty
     * @throws IllegalStateException if an error occurs during file reading, wrapping the underlying exception
     */
    public static byte[] readContentOfTheFile(java.io.File ioFile) {
        byte[] bFile = new byte[(int) ioFile.length()];
        try (FileInputStream fileInputStream = new FileInputStream(ioFile)) {
            //convert file into array of bytes
            int count;
            count = fileInputStream.read(bFile);
            if (count > 0) {
                return bFile;
            } else {
                throw new IllegalArgumentException(Constants.FAILED_TO_READ_THE_GIVEN_FILE);
            }
        } catch (Exception cause) {
            throw new IllegalStateException(Constants.ERROR_OCCURRED_DURING_READ_FILE_ATTACHMENT, cause.getCause());
        }
    }

    /**
     * Parses a comma-separated string of email addresses into a list of EmailAddress objects.
     *
     * @param emailAddressesString a comma-separated string of email addresses
     * @return a list of EmailAddress objects for all valid emails, or empty list if input is null/blank
     */
    public static List<EmailAddress> toEmailAddresses(String emailAddressesString) {
        if (emailAddressesString == null || emailAddressesString.isBlank()) {
            return List.of();
        }
        List<EmailAddress> emailAddresses = new ArrayList<>();
        for (String emailAddressString : emailAddressesString.split(Constants.COMMA)) {
            if (!Validators.isStringNullOrBlank(emailAddressString) && Validators.isEmailValid(emailAddressString)) {
                emailAddresses.add(new EmailAddress(Constants.EMPTY_STRING, emailAddressString));
            }
        }
        return emailAddresses;
    }

    /**
     * Formats a message by converting newlines to HTML breaks for HTML body type.
     *
     * @param message the message content to format
     * @param bodyType the body type ("HTML" or other); determines formatting behavior
     * @return the formatted message with newlines converted to breaks if HTML, otherwise unchanged
     */
    public static String formattedMessage(String message, String bodyType) {
        if (Constants.HTML.equals(bodyType) && message != null) {
            if (message.contains("<") && message.contains(">")) {
                String beforeHtmlText = message.substring(0, message.indexOf("<"));
                String htmlText = message.substring(message.indexOf("<"), (message.lastIndexOf(">") + 1));
                String afterHtmlText = message.substring((message.lastIndexOf(">") + 1));
                message = (beforeHtmlText.replace(NEW_LINE, BR_TAG) + htmlText + afterHtmlText.replace(Constants.NEW_LINE, Constants.BR_TAG));
            } else {
                message = message.replace(NEW_LINE, BR_TAG);
            }
            return message;
        }
        return message;
    }

    /**
     * Removes trailing zeros from the decimal portion of a number.
     *
     * @param number the double value to format
     * @return a string representation of the number without trailing zeros after the decimal point
     */
    public static String removeTrailingZeros(double number) {
        String stringValue = String.valueOf(number);
        if (stringValue.contains(".")) {
            String[] parts = stringValue.split("\\.");
            String decimalPart = parts[1].replaceAll("0*$", "");
            if (decimalPart.isEmpty()) {
                return parts[0];
            } else {
                return parts[0] + "." + decimalPart;
            }
        }
        return stringValue;
    }

    /**
     * Formats a reply/forward message by combining new content with original email thread history.
     *
     * <p>This method creates a properly formatted email message that includes:
     * <ul>
     *   <li>The new message content (extracted from any existing thread history)</li>
     *   <li>A separator and "Original Message" marker</li>
     *   <li>The original email's metadata (from, to, date, subject)</li>
     *   <li>The original email's content</li>
     * </ul>
     * The formatting adapts to the body type (HTML or plain text).</p>
     *
     * @param email the original Email object containing sender, recipients, and content
     * @param message the new message content (may include existing thread history to be cleaned)
     * @param bodyType the body type ("HTML" or text) determining formatting style
     * @param originalDate the formatted date string of the original email
     * @return a formatted message combining new content with original email thread history
     */
    public static String formatMessageWithThread(Email email, String message, String bodyType, String originalDate) {
        // Extract only the new message content, removing any existing thread history
        String newMessageOnly = extractNewMessageContent(message, bodyType);

        // Format the new message using simple, reliable approach (from commit 6bd834a728)
        String formattedNewMessage = formattedMessage(newMessageOnly, bodyType);
        // Create the thread history separately
        String threadHistory = createThreadHistory(email, bodyType, originalDate);
        if (Constants.HTML.equals(bodyType)) {
            return formattedNewMessage + "<br>" + threadHistory;
        } else {
            return formattedNewMessage + "\n\n" + threadHistory;
        }
    }

    /**
     * Extracts content before the original message marker, removing any existing thread history.
     *
     * @param message the input message that may contain thread history
     * @param bodyType the body type (HTML or Text) for appropriate cleaning
     * @return the content before the original message marker, or the entire message if no marker found.
     *         Returns empty string if the marker is at the beginning of the message.
     */
    private static String extractNewMessageContent(String message, String bodyType) {
        if (message == null || message.trim().isEmpty()) {
            return message != null ? message : "";
        }
        int originalMessageIndex = message.indexOf(ORIGINAL_MESSAGE_MARKER);

        if (originalMessageIndex > 0) {
            String newMessageOnly = message.substring(0, originalMessageIndex).trim();

            if (Constants.HTML.equals(bodyType)) {
                newMessageOnly = newMessageOnly.replaceAll(HTML_BR_CLEANUP_REGEX, "").trim();
            } else {
                newMessageOnly = newMessageOnly.replaceAll(WHITESPACE_CLEANUP_REGEX, "").trim();
            }
            return newMessageOnly;
        }
        return message;
    }

    private static String createThreadHistory(Email email, String bodyType, String originalDate) {
        String originalContent = formattedMessage(email.getContent(), bodyType);
        String originalSender = email.getSenderEmailAddress() != null ? formatEmailWithDisplayName(email.getSenderEmailAddress()) : "Unknown Sender";
        String originalTo = formatEmailListWithDisplayNames(email.getToEmailAddresses());
        String originalSubject = email.getSubject();

        if (Constants.HTML.equals(bodyType)) {
            return createHtmlThreadHistory(originalContent, originalSender, originalTo, originalSubject, originalDate);
        } else {
            return createTextThreadHistory(originalContent, originalSender, originalTo, originalSubject, originalDate);
        }
    }

    private static String formatEmailWithDisplayName(EmailAddress emailAddress) {
        String name = emailAddress.getName();
        String email = emailAddress.getMailAddress();
        if (name != null && !name.trim().isEmpty() && email != null && !email.trim().isEmpty()) {
            return name + " <" + email + ">";
        }
        return email != null ? email : (name != null ? name : "Unknown");
    }

    private static String formatEmailListWithDisplayNames(List<EmailAddress> emails) {
        if (emails == null || emails.isEmpty()) {
            return Constants.EMPTY_STRING;
        }
        StringBuilder emailString = new StringBuilder();
        for (EmailAddress email : emails) {
            if (!emailString.isEmpty()) {
                emailString.append(", ");
            }
            emailString.append(formatEmailWithDisplayName(email));
        }
        return emailString.toString();
    }

    private static String createHtmlThreadHistory(String originalContent, String originalSender,
                                                  String originalTo, String originalSubject, String originalDate) {
        originalContent = originalContent.replaceAll("(?m)^\\s*$\\n", "");
        originalContent = originalContent.replaceAll("(?i)(<br\\s*/?>\\s*){2,}", "<br>");
        originalContent = originalContent.replaceAll("(?i)(</?br\\s*/?>\\s*){2,}", "<br>");
        return formatHtmlThreadHeaders(originalSender, originalTo, originalSubject, originalDate) + originalContent;
    }

    private static String formatHtmlThreadHeaders(String originalSender, String originalTo,
                                                  String originalSubject, String originalDate) {
        return "<hr style='border: 1px solid #ccc;'>" +
                "<b>From:</b> " + escapeAngleBrackets(originalSender) + "<br>" +
                "<b>Sent:</b> " + originalDate + "<br>" +
                "<b>To:</b> " + escapeAngleBrackets(originalTo) + "<br>" +
                "<b>Subject:</b> " + escapeAngleBrackets(originalSubject) + "<br><br>";
    }

    private static String createTextThreadHistory(String originalContent, String originalSender,
                                                  String originalTo, String originalSubject, String originalDate) {
        StringBuilder threadHistory = new StringBuilder();
        threadHistory.append(formatTextThreadHeaders(originalSender, originalTo, originalSubject, originalDate));
        String origContent = cleanOriginalContent(originalContent);
        threadHistory.append(origContent);
        return threadHistory.toString();
    }

    private static String formatTextThreadHeaders(String originalSender, String originalTo,
                                                  String originalSubject, String originalDate) {
        StringBuilder headers = new StringBuilder();
        headers.append("-----Original Message-----\n");
        headers.append("From: ").append(originalSender).append("\n");
        headers.append("Sent: ").append(originalDate).append("\n");
        headers.append("To: ").append(originalTo).append("\n");
        headers.append("Subject: ").append(originalSubject).append("\n\n");
        return headers.toString();
    }

    private static String escapeAngleBrackets(String input) {
        if (input == null) return "";
        return input.replace("<", "&lt;").replace(">", "&gt;");
    }

    private static String cleanOriginalContent(String originalContent) {
        if (originalContent == null) {
            return "";
        }

        String cleaned = originalContent.trim();
        cleaned = cleaned.replaceAll("(?i)<br\\s*/?>", "\n")
                .replaceAll("(?i)<[^>]+>", "")
                .replaceAll("&nbsp;", " ")
                .replaceAll("&lt;", "<")
                .replaceAll("&gt;", ">")
                .replaceAll("&amp;", "&")
                .replaceAll("&quot;", "\"");

        cleaned = cleaned.replaceAll("(?m)(\\S)(From:)", "$1\n$2");     // Insert newline if From: appears immediately after text
        cleaned = cleaned.replaceAll("(?m)\n?(From:)", "\n\n$1");       // Ensure there's a blank line before every From:
        cleaned = cleaned.replaceAll("\\n{3,}", "\n\n");                // Normalize any 3+ newlines
        cleaned = cleaned.replaceAll("(?m)^\\s+", "").replaceAll("(?m)\\s+$", "").trim();

        int originalMessageIndex = cleaned.indexOf("-----Original Message-----");
        if (originalMessageIndex > 0) {
            cleaned = cleaned.substring(0, originalMessageIndex).trim();
        }
        return cleaned;
    }

    public static String mapWindowsTimeZoneToJava(String windowsTimeZone) {
        return WINDOWS_TO_IANA.getOrDefault(windowsTimeZone, "UTC");
    }

}