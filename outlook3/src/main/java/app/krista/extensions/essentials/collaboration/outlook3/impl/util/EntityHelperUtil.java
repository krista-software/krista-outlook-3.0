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

public class EntityHelperUtil {
    private EntityHelperUtil() {
    }

    /**
     * This function will convert {@link EntityValue} into {@link Map}
     *
     * @param entityValues input {@link List} of {@link EntityValue}
     * @return {@link List} of {@link Map}
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
     * This function will get Entity Values map  in non-technical form
     *
     * @param entityMap {@link Map} of entity values
     * @return {@link Map} of entity values in non-technical format
     */
    public static Map<String, Object> getValidatedData(Map<String, Object> entityMap) {
        Map<String, Object> validatedEntityMap = new LinkedHashMap<>();
        entityMap.forEach((key, value) ->
                validatedEntityMap.put(key, value.equals(Boolean.TRUE) ? "Yes" : value.equals(Boolean.FALSE) ? "No" : value)
        );
        return validatedEntityMap;
    }

    /**
     * This function finds input data is type of Date or Time
     *
     * @param longValue value to check Date or Time
     * @param key       key used to define Date or Time
     * @return {@link String} parsed with Date or Time if Found
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
     * This Function appends message to Entity list content Into Table Format
     *
     * @param message                    A message with is not part of Table
     * @param entityList                 Data to be added into table
     * @param removeEntityFieldFromTable this field contains fieldParameters which are excluded to be added into Table
     * @return {@link String} complete HTML format Rich Text String
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

            List<String> headerKeys = new ArrayList<>(entitiesData.get(0).keySet());
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

        for (int i = 0; i < entitiesData.size(); i++) {
            htmlContent.append(Constants.TR_TAG);
            Map<String, Object> rowData = entitiesData.get(i);
            for (int cellIndex = 0; cellIndex < headerSize; cellIndex++) {
                htmlContent.append(Constants.TD_TAG);
                Object cellData = rowData.getOrDefault(headerKeys.get(cellIndex), "");
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

    private static StringBuilder addTableHeader(StringBuilder htmlContent, List<String> headerKeys, int headerSize) {
        htmlContent.append(Constants.TR_TAG);
        for (int hdrCell = 0; hdrCell < headerSize; hdrCell++) {
            htmlContent.append(Constants.TH_TAG);
            htmlContent.append(headerKeys.get(hdrCell));
            htmlContent.append(Constants.CLOSE_TH_TAG);
        }
        htmlContent.append(Constants.CLOSE_TR_TAG);
        return htmlContent;
    }

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

    public static String getFileType(java.io.File file) throws IOException {
        String contentType = URLConnection.guessContentTypeFromName(file.getName());
        return contentType != null ? contentType : Constants.APPLICATION_X_BINARY;
    }

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
     * This function removes zeros from number when number contains Only zeros after decimal
     *
     * @param number any double value to parse
     * @return
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

    public static String formatMessageWithThread(Email email, String message, String bodyType, String originalDate) {
        if (message == null) {
            message = "";
        }

        String originalContent = formattedMessage(email.getContent(), bodyType);
        String originalSender = email.getSenderEmailAddress() != null ? formatEmailWithDisplayName(email.getSenderEmailAddress()) : "Unknown Sender";
        String originalTo = formatEmailListWithDisplayNames(email.getToEmailAddresses());
        String originalSubject = email.getSubject();

        if (Constants.HTML.equals(bodyType)) {
            return formatHtmlThreadMessage(message, originalContent, originalSender, originalTo, originalSubject, originalDate);
        } else {
            return formatTextThreadMessage(message, originalContent, originalSender, originalTo, originalSubject, originalDate);
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
            if (emailString.length() > 0) {
                emailString.append(", ");
            }
            emailString.append(formatEmailWithDisplayName(email));
        }
        return emailString.toString();
    }

    private static String formatHtmlThreadMessage(String newMessage, String originalContent, String originalSender,
                                                  String originalTo, String originalSubject, String originalDate) {
        originalContent = originalContent.replaceAll("(?m)^\\s*$\\n", "");
        originalContent = originalContent.replaceAll("(?i)(<br\\s*/?>\\s*){2,}", "<br>");
        originalContent = originalContent.replaceAll("(?i)(</?br\\s*/?>\\s*){2,}", "<br>");
        return newMessage +
                "<br>" +
                "<hr style='border: 1px solid #ccc;'>" +
                "<b>From:</b> " + escapeAngleBrackets(originalSender) + "<br>" +
                "<b>Sent:</b> " + originalDate + "<br>" +
                "<b>To:</b> " + escapeAngleBrackets(originalTo) + "<br>" +
                "<b>Subject:</b> " + escapeAngleBrackets(originalSubject) + "<br><br>" +
                originalContent;
    }

    private static String escapeAngleBrackets(String input) {
        if (input == null) return "";
        return input.replace("<", "&lt;").replace(">", "&gt;");
    }

    private static String formatTextThreadMessage(String newMessage, String originalContent,
                                                  String originalSender, String originalTo,
                                                  String originalSubject, String originalDate) {
        StringBuilder threadMessage = new StringBuilder();
        String newMsg = newMessage == null ? "" : cleanOriginalContent(newMessage.trim());

        if (!newMsg.isEmpty()) {
            threadMessage.append(newMsg).append("\n\n");
        }
        threadMessage.append("-----Original Message-----\n");
        threadMessage.append("From: ").append(originalSender).append("\n");
        threadMessage.append("Sent: ").append(originalDate).append("\n");
        threadMessage.append("To: ").append(originalTo).append("\n");
        threadMessage.append("Subject: ").append(originalSubject).append("\n\n");

        String origContent = cleanOriginalContent(originalContent);
        threadMessage.append(origContent);
        return threadMessage.toString();
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
