package app.krista.extensions.essentials.collaboration.outlook3.impl.util;

import app.krista.extensions.essentials.collaboration.outlook3.service.EmailAddress;
import app.krista.model.base.EntityValue;
import app.krista.model.base.File;
import com.google.gson.reflect.TypeToken;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static app.krista.extensions.essentials.collaboration.outlook3.impl.util.Constants.GSON;

public class EntityHelperUtil {
    private EntityHelperUtil() {
    }

    /**
     * This function will convert {@link EntityValue} into {@link Map}
     *
     * @param entityValues input {@link List} of {@link EntityValue}
     * @return {@link List} of {@link Map}
     */
    public static List<Map<String, Object>> getEntityDataAsList(List<EntityValue> entityValues) {
        List<Map<String, Object>> entityDataToMap = new ArrayList<>();
        for (EntityValue entity : entityValues) {
            Map<String, Object> validEntityMapData = getValidatedData(entity.getFields());
            entityDataToMap.add(validEntityMapData);
        }
        return entityDataToMap;
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
    public static String getMessageContent(String message, List<EntityValue> entityList, List<String> removeEntityFieldFromTable) {
        StringBuilder htmlContent = new StringBuilder();
        formHTMLForTable(htmlContent, message);
        if (entityList != null && !entityList.isEmpty()) {
            List<Map<String, Object>> entitiesData = getEntityDataAsList(entityList);
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

    private static StringBuilder addTableData(StringBuilder htmlContent, List<Map<String, Object>> entitiesData, List<String> headerKeys, int headerSize) {

        for (int i = 0; i < entitiesData.size(); i++) {
            htmlContent.append(Constants.TR_TAG);
            Map<String, Object> rowData = entitiesData.get(i);
            for (int cellIndex = 0; cellIndex < headerSize; cellIndex++) {
                htmlContent.append(Constants.TD_TAG);
                Object cellData = rowData.getOrDefault(headerKeys.get(cellIndex), "");
                String value = (cellData instanceof Long) ? EntityHelperUtil.fetchDateTime(cellData, headerKeys.get(cellIndex)) : String.valueOf(cellData);
                htmlContent.append(value);
                htmlContent.append(Constants.CLOSE_TD_TAG);
            }
            htmlContent.append(Constants.CLOSE_TR_TAG);
        }
        htmlContent.append(Constants.CLOSE_TABLE_TAG);
        htmlContent.append(Constants.CLOSE_BODY_TAG);
        return htmlContent;
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
                message = (beforeHtmlText.replace(Constants.NEW_LINE, Constants.BR_TAG) + htmlText + afterHtmlText.replace(Constants.NEW_LINE, Constants.BR_TAG));
            } else {
                message = message.replace("\n", "<br>");
            }
            return message;
        }
        return message;
    }

    private static boolean isHTML(String input) {
        Pattern pattern = Pattern.compile("<[^>]*>");
        Matcher matcher = pattern.matcher(input);
        return matcher.find();
    }
}
