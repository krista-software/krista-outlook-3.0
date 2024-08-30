package app.krista.extensions.essentials.collaboration.outlook3.catalog.extresp;

import app.krista.extensions.essentials.collaboration.outlook3.catalog.validators.ValidationOrchestrator;
import app.krista.model.base.EntityValue;
import app.krista.model.base.File;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StateMapperUtil {

    private StateMapperUtil() {
    }

    @NotNull
    public static Map<String, Object> addReplyToALLFieldsMetaToMap(String messageId, String to, String cc, String bcc, String replyTo, String message, List<File> attachments, String bodyType, String stateId) {
        Map<String, Object> metaData = new LinkedHashMap<>();
        metaData.put(OutlookResources.STATE_ID, stateId);
        metaData.put(OutlookResources.MESSAGE_ID, messageId);
        metaData.put(OutlookResources.TO, to);
        metaData.put(OutlookResources.CC, cc);
        metaData.put(OutlookResources.BCC, bcc);
        metaData.put(OutlookResources.REPLY_TO, replyTo);
        metaData.put(OutlookResources.MESSAGE, message);
        metaData.put(OutlookResources.ATTACHMENTS, attachments);
        metaData.put(OutlookResources.BODY_TYPE, bodyType);

        return metaData;
    }

    @NotNull
    public static Map<String, Object> addReplyToALLMetaToMap(String messageId, String message, List<File> attachments, String bodyType, String stateId) {
        Map<String, Object> metaData = new LinkedHashMap<>();
        metaData.put(OutlookResources.STATE_ID, stateId);
        metaData.put(OutlookResources.MESSAGE_ID, messageId);
        metaData.put(OutlookResources.MESSAGE, message);
        metaData.put(OutlookResources.ATTACHMENTS, attachments);
        metaData.put(OutlookResources.BODY_TYPE, bodyType);

        return metaData;
    }

    @NotNull
    public static Map<String, Object> addForwardMailMetaToMap(String messageId, String message, String to, String bodyType, String stateId) {
        Map<String, Object> metaData = new LinkedHashMap<>();
        metaData.put(OutlookResources.STATE_ID, stateId);
        metaData.put(OutlookResources.MESSAGE_ID, messageId);
        metaData.put(OutlookResources.MESSAGE, message);
        metaData.put(OutlookResources.TO, to);
        metaData.put(OutlookResources.BODY_TYPE, bodyType);

        return metaData;
    }

    public static Map<String, Object> addSendMailMetaToMap(String subject, String to, String cc, String bcc, String replyTo, String message, List<File> attachments, String bodyType, String stateId) {
        Map<String, Object> metaData = new LinkedHashMap<>();
        metaData.put(OutlookResources.STATE_ID, stateId);
        metaData.put(OutlookResources.SUBJECT, subject);
        metaData.put(OutlookResources.MESSAGE, message);
        metaData.put(OutlookResources.ATTACHMENTS, attachments);
        metaData.put(OutlookResources.TO, to);
        metaData.put(OutlookResources.BCC, bcc);
        metaData.put(OutlookResources.CC, cc);
        metaData.put(OutlookResources.REPLY_TO, replyTo);
        metaData.put(OutlookResources.BODY_TYPE, bodyType);

        return metaData;
    }

    public static Map<String, Object> addSendMailWithTableMetaToMap(String subject, String to, String cc, String bcc, String replyTo, String message, String stateId) {
        Map<String, Object> metaData = new LinkedHashMap<>();
        metaData.put(OutlookResources.STATE_ID, stateId);
        metaData.put(OutlookResources.SUBJECT, subject);
        metaData.put(OutlookResources.MESSAGE, message);
        metaData.put(OutlookResources.TO, to);
        metaData.put(OutlookResources.BCC, bcc);
        metaData.put(OutlookResources.CC, cc);
        metaData.put(OutlookResources.REPLY_TO, replyTo);
        return metaData;
    }

    public static Map<String, Object> addSendMailWithTableAttachmentToMap(List<File> attachments, List<EntityValue> entityList, List<String> removeEntityFieldFromTable, List<ValidationOrchestrator.ValidationResult> validationResults) {
        Map<String, Object> metaData = new LinkedHashMap<>();
        metaData.put(SubCatalogConstants.VALIDATION_RESULTS, validationResults);
        metaData.put(OutlookResources.ATTACHMENTS, attachments);
        metaData.put(OutlookResources.REMOVE_ENTITY_FIELD_FROM_TABLE, removeEntityFieldFromTable);
        metaData.put(OutlookResources.ENTITY_LIST, entityList);
        return metaData;
    }

    public static Map<String, Object> addFetchMailByLableMetaToMap(String label, Double pageNumber, Double pageSize, String stateId) {
        Map<String, Object> metaData = new LinkedHashMap<>();
        metaData.put(OutlookResources.STATE_ID, stateId);
        metaData.put(OutlookResources.LABEL, label);
        metaData.put(OutlookResources.PAGE_NUMBER, pageNumber);
        metaData.put(OutlookResources.PAGE_SIZE, pageSize);
        return metaData;
    }

    public static Map<String, Object> addCategoryToMessageMetaToMap(String messageID, String category, Boolean createCategory, String stateId) {
        Map<String, Object> metaData = new LinkedHashMap<>();
        metaData.put(OutlookResources.STATE_ID, stateId);
        metaData.put(OutlookResources.MESSAGE_ID, messageID);
        metaData.put(OutlookResources.CATEGORY, category);
        metaData.put(OutlookResources.CREATE_CATEGORY, createCategory);
        return metaData;
    }

    public static Map<String, Object> addPageMetaDataToMap(Double pageNumber, Double pageSize, String stateId) {
        Map<String, Object> metaData = new LinkedHashMap<>();
        metaData.put(OutlookResources.STATE_ID, stateId);
        metaData.put(OutlookResources.PAGE_NUMBER, pageNumber);
        metaData.put(OutlookResources.PAGE_SIZE, pageSize);
        return metaData;
    }

    public static Map<String, Object> addFetchInboxWithPrefMetaDataToMap(Double pageNumber, Double pageSize, Map<String, Object> pref, String stateId) {
        Map<String, Object> metaData = addPageMetaDataToMap(pageNumber, pageSize, stateId);
        metaData.put(OutlookResources.PREFERENCE, pref);
        return metaData;
    }
}
