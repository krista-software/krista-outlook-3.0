package app.krista.extensions.essentials.collaboration.outlook3.catalog;

import app.krista.extension.executor.ExtensionResponse;
import app.krista.extension.impl.anno.Attribute;
import app.krista.extension.impl.anno.CatalogRequest;
import app.krista.extension.impl.anno.Field;
import app.krista.extension.impl.anno.SubCatalogRequest;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.entities.MailDetails;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.errorhandlers.ErrorHandlingStateManager;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.errorhandlers.ExtensionResponseGenerator;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.extresp.ExtensionResponseFactory;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.extresp.OutlookResources;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.extresp.RemediationActionFactory;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.extresp.SubCatalogConstants;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.validators.ValidationOrchestrator;
import app.krista.extensions.essentials.collaboration.outlook3.impl.MailHandler;
import app.krista.extensions.essentials.collaboration.outlook3.impl.MessagingAreaImpl;
import app.krista.extensions.essentials.collaboration.outlook3.impl.util.Constants;
import app.krista.extensions.essentials.collaboration.outlook3.service.Account;
import app.krista.extensions.essentials.collaboration.outlook3.service.Email;
import app.krista.extensions.essentials.collaboration.outlook3.service.Folder;
import app.krista.model.base.EntityValue;
import app.krista.model.base.File;
import org.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MessagingAreaSubCatalogRequests {
    public static final String HANDLE_REENTER_REPLY_TO_ALL_WITH_FIELDS = "handleReenterReplyToAllWithFields";
    public static final String HANDLE_REENTER_REPLY_TO_MAIL_WITH_FIELDS = "handleReenterReplyToMailWithFields";
    public static final String HANDLE_REENTER_SEND_MAIL = "handleReenterSendMail";
    public static final String HANDLE_REENTER_SEND_MAIL_WITH_TABLE = "handleReenterSendMailWithTable";
    public static final String HANDLE_REENTER_REPLY_TO_ALL = "handleReenterReplyToAll";
    public static final String HANDLE_REENTER_REPLY_TO_MAIL = "handleReenterReplyToMail";
    public static final String HANDLE_REENTER_FORWARD_MAIL = "handleReenterForwardMail";
    public static final String HANDLE_REENTER_MARK_MESSAGE = "handleReenterMarkMessage";
    public static final String REENTER = "Reenter";
    public static final String REENTER_WAS_TRUE_HENCE_CONTINUING = "Reenter was true. Hence continuing.";
    private static final Logger LOGGER = LoggerFactory.getLogger(MessagingAreaSubCatalogRequests.class);
    private final MailHandler mailHandler;
    private final Account account;

    private final MessagingAreaImpl messagingAreaImpl;
    private final ExtensionResponseGenerator responseGenerator;
    private final ErrorHandlingStateManager internalStateManager;

    @Inject
    public MessagingAreaSubCatalogRequests(MailHandler mailHandler, Account account
            , ExtensionResponseGenerator responseGenerator, ErrorHandlingStateManager internalStateManager
            , MessagingAreaImpl messagingAreaImpl) {
        this.mailHandler = mailHandler;
        this.account = account;
        this.responseGenerator = responseGenerator;
        this.internalStateManager = internalStateManager;
        this.messagingAreaImpl = messagingAreaImpl;
    }

    @NotNull
    private static List<ValidationOrchestrator.ValidationResult> getValidationResults(Map<String, Object> state) {
        List<ValidationOrchestrator.ValidationResult> validationResults = new ArrayList<>();
        List<?> results = (List<?>) state.get(SubCatalogConstants.VALIDATION_RESULTS);
        for (Object item : results) {
            validationResults.add(Constants.GSON.fromJson(Constants.GSON.toJson(item), ValidationOrchestrator.ValidationResult.class));
        }
        return validationResults;
    }

    @NotNull
    private static Map<String, Object> addMetaDataToLinkedHashMap(Map<String, Object> map, String stateId) {
        Map<String, Object> metaData = new LinkedHashMap<>();
        metaData.put(OutlookResources.STATE_ID, stateId);
        metaData.putAll(map);
        return metaData;
    }

    @NotNull
    private static List<File> getFileList(Map<String, Object> state) {
        List<File> fileList = new ArrayList<>();
        if (state.containsKey(OutlookResources.ATTACHMENTS)) {
            List<?> results = (List<?>) state.get(OutlookResources.ATTACHMENTS);
            for (Object item : results) {
                fileList.add(Constants.GSON.fromJson(Constants.GSON.toJson(item), File.class));
            }
        }
        return fileList;
    }

    @NotNull
    private static List<String> getEntityFieldList(Map<String, Object> state) {
        List<String> removeEntityFieldFromTable = new ArrayList<>();
        if (state.containsKey(OutlookResources.REMOVE_ENTITY_FIELD_FROM_TABLE)) {
            List<?> results = (List<?>) state.get(OutlookResources.REMOVE_ENTITY_FIELD_FROM_TABLE);
            for (Object item : results) {
                removeEntityFieldFromTable.add(Constants.GSON.fromJson(Constants.GSON.toJson(item), String.class));
            }
        }
        return removeEntityFieldFromTable;
    }

    @SubCatalogRequest(
            name = SubCatalogConstants.CONFIRM_REENTER_FETCH_MAIL,
            description = "Checks if user wants to re enter message Id and if yes, sends prompt to do so",
            type = CatalogRequest.Type.QUERY_SYSTEM
    )
    @SuppressWarnings("unchecked")
    public ExtensionResponse confirmReenterFetchMail(@Field.Desc(name = "inputMap",
            type = "{ Reenter: Boolean, stateId: Text, Message ID: Text}", required = true) Map<String, Object> map) {
        Boolean reenter = (Boolean) map.get(REENTER);
        String stateId = (String) map.get(OutlookResources.STATE_ID);
        Map<String, Object> state = internalStateManager.get(stateId);

        List<ValidationOrchestrator.ValidationResult> validationResults = getValidationResults(state);
        if (Boolean.FALSE.equals(reenter)) {
            return responseGenerator.generateFetchDenyResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    validationResults, null,
                    Map.of());
        }
        LOGGER.info(REENTER_WAS_TRUE_HENCE_CONTINUING);
        return responseGenerator.generateFetchResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                validationResults, "handleReenterFetchMail",
                Map.of(OutlookResources.STATE_ID, stateId));
    }

    @SubCatalogRequest(
            name = "handleReenterFetchMail",
            description = "Handle reenter message id",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Desc(name = "Mail", type = "Entity(Mail Details)", required = false)
    public app.krista.extension.executor.ExtensionResponse handleReenterFetchMail(
            @Field.Desc(name = "inputMap", type = "{Valid Message ID: Text, stateId: Text, Message ID: Text}") Map<String, Object> map) {
        LOGGER.info("SubCatalogRequest handleReenterMessageId start: {}", map);
        String messageID = (String) map.get(OutlookResources.MESSAGE_ID);

        try {
            MailDetails mailDetails = mailHandler.fromEmail(account.getEmail(messageID), null);
            if (mailDetails != null) {
                LOGGER.info("fetchMailByMessageId: ID {}, from {}, subject {}, timestamp {}",
                        mailDetails.messageID, mailDetails.from, mailDetails.subject, new Date(mailDetails.sendDateAndTime));
            }
            return ExtensionResponseFactory.create(Map.of("Mail", mailDetails));
        } catch (Exception cause) {
            return ExtensionResponseFactory.create(cause, "Failed to fetch mail by message id",
                    ExtensionResponse.Error.ExceptionType.LOGIC_ERROR);
        }
    }

    @SubCatalogRequest(
            name = SubCatalogConstants.CONFIRM_REENTER_MOVE_MESSAGE,
            description = "Checks if user wants to re enter message Id and folder name if yes, sends prompt to do so",
            type = CatalogRequest.Type.QUERY_SYSTEM
    )
    @SuppressWarnings("unchecked")
    public ExtensionResponse confirmReenterMoveMessage(@Field.Desc(name = "inputMap",
            type = "{ Reenter: Boolean, stateId: Text, Message ID: Text, Folder Name: Text}", required = true) Map<String, Object> map) {
        Boolean reenter = (Boolean) map.get(REENTER);
        String stateId = (String) map.get(OutlookResources.STATE_ID);
        Map<String, Object> state = internalStateManager.get(stateId);
        List<ValidationOrchestrator.ValidationResult> validationResults = getValidationResults(state);
        if (Boolean.FALSE.equals(reenter)) {
            return responseGenerator.generateFetchDenyResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    validationResults, null,
                    Map.of());
        }
        LOGGER.info(REENTER_WAS_TRUE_HENCE_CONTINUING);
        return responseGenerator.generateFetchResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                validationResults, "handleReenterMoveMessage",
                Map.of(OutlookResources.STATE_ID, stateId, OutlookResources.MESSAGE_ID, map.get(OutlookResources.MESSAGE_ID), OutlookResources.FOLDER_NAME, map.get(OutlookResources.FOLDER_NAME)));
    }

    @SubCatalogRequest(
            name = "handleReenterMoveMessage",
            description = "Handle reenter message id",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field(name = "Message ID", type = "Text", required = false)
    public app.krista.extension.executor.ExtensionResponse handleReenterMoveMessage(
            @Field.Desc(name = "inputMap", type = "{stateId: Text, Message ID: Text, Folder Name: Text}") Map<String, Object> map) {
        LOGGER.info("SubCatalogRequest handleReenterMessageId start: {}", map);
        String messageID = (String) map.get(OutlookResources.MESSAGE_ID);
        String folderName = (String) map.get(OutlookResources.FOLDER_NAME);

        try {
            Email email = account.getEmail(messageID);
            if (email == null) {
                return ExtensionResponseFactory.create(Constants.INVALID_MESSAGE_ID + messageID, ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                        List.of(RemediationActionFactory.createInformActionALLParticipants(Constants.INVALID_MESSAGE_ID + messageID, List.of())),
                        null, Map.of());
            }
            Folder folder = account.getFolderByName(List.of(folderName.split(Constants.FORWARD_SLASH)));
            if (folder == null) {
                return ExtensionResponseFactory.create(Constants.INCORRECT_FOLDER_NAME + folderName, ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                        List.of(RemediationActionFactory.createInformActionALLParticipants(Constants.INCORRECT_FOLDER_NAME + folderName, List.of())),
                        null, Map.of());
            }
            return ExtensionResponseFactory.create(Map.of(OutlookResources.MESSAGE_ID, email.moveToFolder(folder)));
        } catch (Exception cause) {
            return ExtensionResponseFactory.create(cause, "Failed to Move Message",
                    ExtensionResponse.Error.ExceptionType.LOGIC_ERROR);
        }
    }

    @SubCatalogRequest(
            name = SubCatalogConstants.CONFIRM_REENTER_REPLY_TO_ALL_WITH_FIELDS,
            description = "Checks if user wants to re enter reply to all and if yes, sends prompt to do so",
            type = CatalogRequest.Type.QUERY_SYSTEM
    )
    @SuppressWarnings("unchecked")
    public ExtensionResponse confirmReenterReplyToAllWithFields(@Field.Desc(name = "inputMap",
            type = "{ Reenter: Boolean, stateId: Text, Message ID: Text, To: Text,  Cc: Text, Bcc: Text, Reply To: Text, Message: RichText, Attachments:  File, BodyType: PickOne(Text|HTML) }", required = true) Map<String, Object> map) {
        Boolean reenter = (Boolean) map.get(REENTER);
        String stateId = (String) map.get(OutlookResources.STATE_ID);
        Map<String, Object> state = internalStateManager.get(stateId);
        List<ValidationOrchestrator.ValidationResult> validationResults = getValidationResults(state);
        if (Boolean.FALSE.equals(reenter)) {
            return responseGenerator.generateFetchDenyResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    validationResults, null,
                    Map.of());
        }
        LOGGER.info(REENTER_WAS_TRUE_HENCE_CONTINUING);
        return responseGenerator.generateFetchResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                validationResults, HANDLE_REENTER_REPLY_TO_ALL_WITH_FIELDS,
                addMetaDataToLinkedHashMap(map, stateId));
    }

    @SubCatalogRequest(
            name = HANDLE_REENTER_REPLY_TO_ALL_WITH_FIELDS,
            description = "Handle Reply To ALL With CC BCC",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Boolean(name = "Is Successful", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    @SuppressWarnings("unchecked")
    public app.krista.extension.executor.ExtensionResponse handleReenterReplyToAllWithFields(
            @Field.Desc(name = "inputMap", type = "{ stateId: Text, Message ID: Text, To: Text,  Cc: Text, Bcc: Text, Reply To: Text, Message: RichText, Attachments: File, BodyType: PickOne(Text|HTML) }") Map<String, Object> map
    ) {
        LOGGER.info("SubCatalogRequest handleReenterReplayToALLWithCcBcc start: {}", map);
        try {
            List<File> attachments = (List<File>) map.get(OutlookResources.ATTACHMENTS);
            String messageId = (String) map.get(OutlookResources.MESSAGE_ID);
            String bodyType = (String) map.get(OutlookResources.BODY_TYPE);
            String message = (String) map.get(OutlookResources.MESSAGE);
            String cc = (String) map.get(OutlookResources.CC);
            String to = (String) map.get(OutlookResources.TO);
            String bcc = (String) map.get(OutlookResources.BCC);
            String replyTo = (String) map.get(OutlookResources.REPLY_TO);

            return messagingAreaImpl.replyToAllWithCCAndBCC(attachments, messageId, to, cc, bcc, replyTo, message, bodyType);
        } catch (Exception cause) {
            return ExtensionResponseFactory.create(cause, "Failed to Reply all with Cc and Bcc",
                    ExtensionResponse.Error.ExceptionType.LOGIC_ERROR);
        }
    }

    @SubCatalogRequest(
            name = SubCatalogConstants.CONFIRM_REENTER_REPLY_TO_ALL,
            description = "Checks if user wants to re enter reply to all and if yes, sends prompt to do so",
            type = CatalogRequest.Type.QUERY_SYSTEM
    )
    @SuppressWarnings("unchecked")
    public ExtensionResponse confirmReenterReplyToAll(@Field.Desc(name = "inputMap",
            type = "{ Reenter: Boolean, stateId: Text, Message ID: Text, Message: RichText, Attachments: File, BodyType: PickOne(Text|HTML) }", required = true) Map<String, Object> map) {
        Boolean reenter = (Boolean) map.get(REENTER);
        String stateId = (String) map.get(OutlookResources.STATE_ID);
        Map<String, Object> state = internalStateManager.get(stateId);
        List<ValidationOrchestrator.ValidationResult> validationResults = getValidationResults(state);
        if (Boolean.FALSE.equals(reenter)) {
            return responseGenerator.generateFetchDenyResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    validationResults, null,
                    Map.of());
        }
        LOGGER.info(REENTER_WAS_TRUE_HENCE_CONTINUING);
        return responseGenerator.generateFetchResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                validationResults, HANDLE_REENTER_REPLY_TO_ALL,
                addMetaDataToLinkedHashMap(map, stateId));
    }

    @SubCatalogRequest(
            name = HANDLE_REENTER_REPLY_TO_ALL,
            description = "Handle Reply To ALL",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Boolean(name = "Is Successful", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    @SuppressWarnings("unchecked")
    public app.krista.extension.executor.ExtensionResponse handleReenterReplyToAll(
            @Field.Desc(name = "inputMap", type = "{ stateId: Text, Message ID: Text, Message: RichText, Attachments: File, BodyType: PickOne(Text|HTML) }") Map<String, Object> map
    ) {
        LOGGER.info("SubCatalogRequest handleReenterReplyToALL start: {}", map);
        try {
            List<File> attachments = (List<File>) map.get(OutlookResources.ATTACHMENTS);
            String messageId = (String) map.get(OutlookResources.MESSAGE_ID);
            String bodyType = (String) map.get(OutlookResources.BODY_TYPE);
            String message = (String) map.get(OutlookResources.MESSAGE);

            return messagingAreaImpl.replyToAll(attachments, messageId, message, bodyType);
        } catch (Exception cause) {
            return ExtensionResponseFactory.create(cause, "Failed to Reply all Message",
                    ExtensionResponse.Error.ExceptionType.LOGIC_ERROR);
        }
    }

    @SubCatalogRequest(
            name = SubCatalogConstants.CONFIRM_REENTER_FORWARD_MAIL,
            description = "Checks if user wants to re enter forward mail and if yes, sends prompt to do so",
            type = CatalogRequest.Type.QUERY_SYSTEM
    )
    @SuppressWarnings("unchecked")
    public ExtensionResponse confirmReenterForwardMail(@Field.Desc(name = "inputMap",
            type = "{ Reenter: Boolean, stateId: Text, Message ID: Text, To: Text, Message: RichText, BodyType: PickOne(Text|HTML) }", required = true) Map<String, Object> map) {
        Boolean reenter = (Boolean) map.get(REENTER);
        String stateId = (String) map.get(OutlookResources.STATE_ID);
        Map<String, Object> state = internalStateManager.get(stateId);
        List<ValidationOrchestrator.ValidationResult> validationResults = getValidationResults(state);
        if (Boolean.FALSE.equals(reenter)) {
            return responseGenerator.generateFetchDenyResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    validationResults, null,
                    Map.of());
        }
        LOGGER.info(REENTER_WAS_TRUE_HENCE_CONTINUING);
        return responseGenerator.generateFetchResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                validationResults, HANDLE_REENTER_FORWARD_MAIL, addMetaDataToLinkedHashMap(map, stateId));
    }

    @SubCatalogRequest(
            name = HANDLE_REENTER_FORWARD_MAIL,
            description = "Handle Forward Mail",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field(name = "Is Forwarded", type = "Switch", required = false)
    public app.krista.extension.executor.ExtensionResponse handleReenterForwardMail(
            @Field.Desc(name = "inputMap", type = "{ stateId: Text, Message ID: Text, To: Text, Message: RichText, BodyType: PickOne(Text|HTML) }") Map<String, Object> map
    ) {
        LOGGER.info("SubCatalogRequest handleReenterForwardMail start: {}", map);
        try {

            String messageId = (String) map.get(OutlookResources.MESSAGE_ID);
            String to = (String) map.get(OutlookResources.TO);
            String bodyType = (String) map.get(OutlookResources.BODY_TYPE);
            String message = (String) map.get(OutlookResources.MESSAGE);

            return messagingAreaImpl.forwardMail(messageId, to, message, bodyType);
        } catch (Exception cause) {
            return ExtensionResponseFactory.create(cause, "Failed to Forward Mail",
                    ExtensionResponse.Error.ExceptionType.LOGIC_ERROR);
        }
    }

    @SubCatalogRequest(
            name = SubCatalogConstants.CONFIRM_REENTER_SEND_MAIL,
            description = "Checks if user wants to re send mail and if yes, sends prompt to do so",
            type = CatalogRequest.Type.QUERY_SYSTEM
    )
    @SuppressWarnings("unchecked")
    public ExtensionResponse confirmReenterSendMail(@Field.Desc(name = "inputMap",
            type = "{ Reenter: Boolean, stateId: Text, Subject: Text, To: Text,  Cc: Text, Bcc: Text, Reply To: Text, Message: RichText, Attachments: File, BodyType: PickOne(Text|HTML) }", required = true) Map<String, Object> map) {
        Boolean reenter = (Boolean) map.get(REENTER);
        String stateId = (String) map.get(OutlookResources.STATE_ID);
        Map<String, Object> state = internalStateManager.get(stateId);
        List<ValidationOrchestrator.ValidationResult> validationResults = getValidationResults(state);
        if (Boolean.FALSE.equals(reenter)) {
            return responseGenerator.generateFetchDenyResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    validationResults, null,
                    Map.of());
        }
        LOGGER.info(REENTER_WAS_TRUE_HENCE_CONTINUING);
        return responseGenerator.generateFetchResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                validationResults, HANDLE_REENTER_SEND_MAIL, addMetaDataToLinkedHashMap(map, stateId)
        );
    }

    @SubCatalogRequest(
            name = HANDLE_REENTER_SEND_MAIL,
            description = "Handle Send Mail",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field(name = "Message", type = "Text", required = false)
    @SuppressWarnings("unchecked")
    public app.krista.extension.executor.ExtensionResponse handleReenterSendMail(
            @Field.Desc(name = "inputMap", type = "{ stateId: Text, Subject: Text, To: Text,  Cc: Text, Bcc: Text, Reply To: Text, Message: RichText, Attachments: File, BodyType: PickOne(Text|HTML) }") Map<String, Object> map
    ) {
        LOGGER.info("SubCatalogRequest handleReenterReplayToALL start: {}", map);
        try {
            List<File> attachments = (List<File>) map.get(OutlookResources.ATTACHMENTS);
            String subject = (String) map.get(OutlookResources.SUBJECT);
            String bodyType = (String) map.get(OutlookResources.BODY_TYPE);
            String message = (String) map.get(OutlookResources.MESSAGE);
            String cc = (String) map.get(OutlookResources.CC);
            String to = (String) map.get(OutlookResources.TO);
            String bcc = (String) map.get(OutlookResources.BCC);
            String replyTo = (String) map.get(OutlookResources.REPLY_TO);

            return messagingAreaImpl.sendMail(subject, message, attachments, to, cc, bcc, replyTo, bodyType);
        } catch (Exception cause) {
            return ExtensionResponseFactory.create(cause, "Failed to Send Mail",
                    ExtensionResponse.Error.ExceptionType.LOGIC_ERROR);
        }
    }

    @SubCatalogRequest(
            name = SubCatalogConstants.CONFIRM_REENTER_SEND_MAIL_WITH_TABLE,
            description = "Checks if user wants to re send mail with table and if yes, sends prompt to do so",
            type = CatalogRequest.Type.QUERY_SYSTEM
    )
    @SuppressWarnings("unchecked")
    public ExtensionResponse confirmReenterSendMailWithTable(@Field.Desc(name = "inputMap",
            type = "{ Reenter: Boolean, stateId: Text, Subject: Text, To: Text,  Cc: Text, Bcc: Text, Reply To: Text, Message: RichText }", required = true) Map<String, Object> map) {
        Boolean reenter = (Boolean) map.get(REENTER);
        String stateId = (String) map.get(OutlookResources.STATE_ID);
        Map<String, Object> state = internalStateManager.get(stateId);
        List<ValidationOrchestrator.ValidationResult> validationResults = getValidationResults(state);
        if (Boolean.FALSE.equals(reenter)) {
            return responseGenerator.generateFetchDenyResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    validationResults, null,
                    Map.of());
        }
        LOGGER.info(REENTER_WAS_TRUE_HENCE_CONTINUING);
        return responseGenerator.generateFetchResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                validationResults, HANDLE_REENTER_SEND_MAIL_WITH_TABLE,
                addMetaDataToLinkedHashMap(map, stateId)
        );
    }

    @SubCatalogRequest(
            name = HANDLE_REENTER_SEND_MAIL_WITH_TABLE,
            description = "Handle Send Mail With Table",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field(name = "Message", type = "Text", required = false)
    @SuppressWarnings("unchecked")
    public app.krista.extension.executor.ExtensionResponse handleReenterSendMailWithTable(
            @Field.Desc(name = "inputMap", type = "{ stateId: Text, Subject: Text, To: Text,  Cc: Text, Bcc: Text, Reply To: Text, Message: RichText }") Map<String, Object> map
    ) {
        LOGGER.info("SubCatalogRequest handleReenterReplayToALL start: {}", map);
        try {
            String stateId = (String) map.get(OutlookResources.STATE_ID);
            Map<String, Object> state = internalStateManager.get(stateId);
            Map<String, Object> metaInfo = (Map<String, Object>) internalStateManager.getMetaInfo(stateId);
            List<File> attachments = getFileList(state);
            String subject = (String) map.get(OutlookResources.SUBJECT);
            List<String> removeEntityFieldFromTable = getEntityFieldList(state);
            List<EntityValue> entityList = (List<EntityValue>) metaInfo.get(OutlookResources.ENTITY_LIST);
            String message = (String) map.get(OutlookResources.MESSAGE);
            String cc = (String) map.get(OutlookResources.CC);
            String to = (String) map.get(OutlookResources.TO);
            String bcc = (String) map.get(OutlookResources.BCC);
            String replyTo = (String) map.get(OutlookResources.REPLY_TO);

            return messagingAreaImpl.sendMailWithTable(subject, message, attachments, to, cc, bcc, replyTo, entityList, removeEntityFieldFromTable);
        } catch (Exception cause) {
            return ExtensionResponseFactory.create(cause, "Failed to Send Mail With Table",
                    ExtensionResponse.Error.ExceptionType.LOGIC_ERROR);
        }
    }

    @SubCatalogRequest(
            name = SubCatalogConstants.CONFIRM_REENTER_MARK_MESSAGE,
            description = "Checks if user wants to re mark message and if yes, sends prompt to do so",
            type = CatalogRequest.Type.QUERY_SYSTEM
    )
    @SuppressWarnings("unchecked")
    public ExtensionResponse confirmReenterMarkMessage(@Field.Desc(name = "inputMap",
            type = "{ Reenter: Boolean, stateId: Text, Message ID: Text, Label: PickOne(Read|Unread) }", required = true) Map<String, Object> map) {
        Boolean reenter = (Boolean) map.get(REENTER);
        String stateId = (String) map.get(OutlookResources.STATE_ID);
        Map<String, Object> state = internalStateManager.get(stateId);
        List<ValidationOrchestrator.ValidationResult> validationResults = getValidationResults(state);
        if (Boolean.FALSE.equals(reenter)) {
            return responseGenerator.generateFetchDenyResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    validationResults, null,
                    Map.of());
        }
        LOGGER.info(REENTER_WAS_TRUE_HENCE_CONTINUING);
        return responseGenerator.generateFetchResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                validationResults, HANDLE_REENTER_MARK_MESSAGE,
                Map.of(OutlookResources.STATE_ID, stateId, OutlookResources.MESSAGE_ID, map.get(OutlookResources.MESSAGE_ID), OutlookResources.LABEL, map.get(OutlookResources.LABEL)));
    }

    @SubCatalogRequest(
            name = HANDLE_REENTER_MARK_MESSAGE,
            description = "Handle reenter mark message",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field(name = "Response", type = "Text", required = false)
    public app.krista.extension.executor.ExtensionResponse handleReenterMarkMessage(
            @Field.Desc(name = "inputMap", type = "{ stateId: Text, Message ID: Text, Label: PickOne(Read|Unread) }") Map<String, Object> map) {
        LOGGER.info("SubCatalogRequest handleReenterMarkMessage start: {}", map);
        String messageID = (String) map.get(OutlookResources.MESSAGE_ID);
        String label = (String) map.get(OutlookResources.LABEL);

        try {
            return messagingAreaImpl.markMessage(messageID, label);
        } catch (Exception cause) {
            return ExtensionResponseFactory.create(cause, "Failed to Mark Message",
                    ExtensionResponse.Error.ExceptionType.LOGIC_ERROR);
        }
    }


    @SubCatalogRequest(
            name = SubCatalogConstants.CONFIRM_REENTER_REPLY_TO_MAIL_WITH_FIELDS,
            description = "Checks if user wants to re enter reply to mail and if yes, sends prompt to do so",
            type = CatalogRequest.Type.QUERY_SYSTEM
    )
    @SuppressWarnings("unchecked")
    public ExtensionResponse confirmReenterReplyToMailWithFields(@Field.Desc(name = "inputMap",
            type = "{ Reenter: Boolean, stateId: Text, Message ID: Text, To: Text,  Cc: Text, Bcc: Text, Reply To: Text, Message: RichText, Attachments: File, BodyType: PickOne(Text|HTML) }", required = true) Map<String, Object> map) {
        Boolean reenter = (Boolean) map.get(REENTER);
        String stateId = (String) map.get(OutlookResources.STATE_ID);
        Map<String, Object> state = internalStateManager.get(stateId);
        List<ValidationOrchestrator.ValidationResult> validationResults = getValidationResults(state);
        if (Boolean.FALSE.equals(reenter)) {
            return responseGenerator.generateFetchDenyResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    validationResults, null,
                    Map.of());
        }
        LOGGER.info(REENTER_WAS_TRUE_HENCE_CONTINUING);
        return responseGenerator.generateFetchResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                validationResults, HANDLE_REENTER_REPLY_TO_MAIL_WITH_FIELDS, addMetaDataToLinkedHashMap(map, stateId)
        );
    }

    @SubCatalogRequest(
            name = HANDLE_REENTER_REPLY_TO_MAIL_WITH_FIELDS,
            description = "Handle Reply To Mail with cc, bcc, to, Reply to",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field(name = "Message", type = "Text", required = false, attributes = {}, options = {})
    @SuppressWarnings("unchecked")
    public app.krista.extension.executor.ExtensionResponse handleReenterReplyToMailWithFields(
            @Field.Desc(name = "inputMap", type = "{ stateId: Text, Message ID: Text, To: Text,  Cc: Text, Bcc: Text, Reply To: Text, Message: RichText, Attachments: File, BodyType: PickOne(Text|HTML) }") Map<String, Object> map
    ) {
        LOGGER.info("SubCatalogRequest handleReenterReplayToMailWithCcBcc start: {}", map);
        try {
            List<File> attachments = (List<File>) map.get(OutlookResources.ATTACHMENTS);
            String messageId = (String) map.get(OutlookResources.MESSAGE_ID);
            String bodyType = (String) map.get(OutlookResources.BODY_TYPE);
            String message = (String) map.get(OutlookResources.MESSAGE);
            String cc = (String) map.get(OutlookResources.CC);
            String to = (String) map.get(OutlookResources.TO);
            String bcc = (String) map.get(OutlookResources.BCC);
            String replyTo = (String) map.get(OutlookResources.REPLY_TO);

            return messagingAreaImpl.replyToMailWithCCAndBCC(attachments, messageId, to, cc, bcc, replyTo, message, bodyType);
        } catch (Exception cause) {
            return ExtensionResponseFactory.create(cause, "Failed to Reply Mail With CC and BCC",
                    ExtensionResponse.Error.ExceptionType.LOGIC_ERROR);
        }
    }


    @SubCatalogRequest(
            name = SubCatalogConstants.CONFIRM_REENTER_REPLY_TO_MAIL,
            description = "Checks if user wants to re enter reply to mail and if yes, sends prompt to do so",
            type = CatalogRequest.Type.QUERY_SYSTEM
    )
    @SuppressWarnings("unchecked")
    public ExtensionResponse confirmReenterReplyToMail(@Field.Desc(name = "inputMap",
            type = "{ Reenter: Boolean, stateId: Text, Message ID: Text, Message: RichText, Attachments: File, BodyType: PickOne(Text|HTML) }", required = true) Map<String, Object> map) {
        Boolean reenter = (Boolean) map.get(REENTER);
        String stateId = (String) map.get(OutlookResources.STATE_ID);
        Map<String, Object> state = internalStateManager.get(stateId);
        List<ValidationOrchestrator.ValidationResult> validationResults = getValidationResults(state);
        if (Boolean.FALSE.equals(reenter)) {
            return responseGenerator.generateFetchDenyResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    validationResults, null,
                    Map.of());
        }
        LOGGER.info(REENTER_WAS_TRUE_HENCE_CONTINUING);
        return responseGenerator.generateFetchResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                validationResults, HANDLE_REENTER_REPLY_TO_MAIL, addMetaDataToLinkedHashMap(map, stateId)
        );
    }

    @SubCatalogRequest(
            name = HANDLE_REENTER_REPLY_TO_MAIL,
            description = "Handle Reply To Mail",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field(name = "Message", type = "Text", required = false)
    @SuppressWarnings("unchecked")
    public app.krista.extension.executor.ExtensionResponse handleReenterReplyToMail(
            @Field.Desc(name = "inputMap", type = "{ stateId: Text, Message ID: Text, Message: RichText, Attachments: File, BodyType: PickOne(Text|HTML) }") Map<String, Object> map
    ) {
        LOGGER.info("SubCatalogRequest handleReenterReplayToMail start: {}", map);
        try {
            List<File> attachments = (List<File>) map.get(OutlookResources.ATTACHMENTS);
            String messageID = (String) map.get(OutlookResources.MESSAGE_ID);
            String bodyType = (String) map.get(OutlookResources.BODY_TYPE);
            String message = (String) map.get(OutlookResources.MESSAGE);

            return messagingAreaImpl.replyToMail(attachments, messageID, bodyType, message);
        } catch (Exception cause) {
            return ExtensionResponseFactory.create(cause, "Failed to Reply Mail",
                    ExtensionResponse.Error.ExceptionType.LOGIC_ERROR);
        }
    }


    @SubCatalogRequest(
            name = SubCatalogConstants.CONFIRM_REENTER_FETCH_MAIL_BY_LABEL,
            description = "Checks if user wants to re enter fetch mail by Label and if yes, sends prompt to do so",
            type = CatalogRequest.Type.QUERY_SYSTEM
    )
    @SuppressWarnings("unchecked")
    public ExtensionResponse confirmReenterFetchMailByLabel(@Field.Desc(name = "inputMap",
            type = "{ Reenter: Boolean, stateId: Text, Label: Text, Page Number: Number, Page Size: Number }", required = true) Map<String, Object> map) {
        Boolean reenter = (Boolean) map.get(REENTER);
        String stateId = (String) map.get(OutlookResources.STATE_ID);
        Map<String, Object> state = internalStateManager.get(stateId);
        List<ValidationOrchestrator.ValidationResult> validationResults = getValidationResults(state);
        if (Boolean.FALSE.equals(reenter)) {
            return responseGenerator.generateFetchDenyResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    validationResults, null,
                    Map.of());
        }
        LOGGER.info(REENTER_WAS_TRUE_HENCE_CONTINUING);
        return responseGenerator.generateFetchResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                validationResults, "handleReenterFetchMailByLabel", Map.of(OutlookResources.LABEL, map.get(OutlookResources.LABEL)));
    }

    @SubCatalogRequest(
            name = "handleReenterFetchMailByLabel",
            description = "Handle reenter fetch mail by label",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Desc(name = "Mails", type = "[ Entity(Mail Details) ]", required = false)
    public app.krista.extension.executor.ExtensionResponse handleReenterFetchMailByLabel(
            @Field.Desc(name = "inputMap", type = "{ stateId: Text, Label: Text, Page Number: Number, Page Size: Number }") Map<String, Object> map) {
        LOGGER.info("SubCatalogRequest handleReenterFetchMailByLabel start: {}", map);
        String label = (String) map.get(OutlookResources.LABEL);
        Double pageNumber = (Double) map.get(OutlookResources.PAGE_NUMBER);
        Double pageSize = (Double) map.get(OutlookResources.PAGE_SIZE);

        try {
            return messagingAreaImpl.fetchMailByLabel(label, pageNumber, pageSize);
        } catch (Exception cause) {
            return ExtensionResponseFactory.create(cause, "Failed to fetch mail by label",
                    ExtensionResponse.Error.ExceptionType.LOGIC_ERROR);
        }
    }


    @SubCatalogRequest(
            name = SubCatalogConstants.CONFIRM_REENTER_ADD_CATEGORY_TO_MESSAGE,
            description = "Checks if user wants to re enter add category to message and if yes, sends prompt to do so",
            type = CatalogRequest.Type.QUERY_SYSTEM
    )
    @SuppressWarnings("unchecked")
    public ExtensionResponse confirmReenterAddCategoryToMessage(@Field.Desc(name = "inputMap",
            type = "{ Reenter: Boolean, stateId: Text, Message ID: Text, Category: Text, Create Category: Boolean }", required = true) Map<String, Object> map) {
        Boolean reenter = (Boolean) map.get(REENTER);
        String stateId = (String) map.get(OutlookResources.STATE_ID);
        Map<String, Object> state = internalStateManager.get(stateId);
        List<ValidationOrchestrator.ValidationResult> validationResults = getValidationResults(state);
        if (Boolean.FALSE.equals(reenter)) {
            return responseGenerator.generateFetchDenyResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    validationResults, null,
                    Map.of());
        }
        LOGGER.info(REENTER_WAS_TRUE_HENCE_CONTINUING);
        return responseGenerator.generateFetchResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                validationResults, "handleReenterAddCategoryToMessage",
                addMetaDataToLinkedHashMap(map, stateId));
    }

    @SubCatalogRequest(
            name = "handleReenterAddCategoryToMessage",
            description = "Handle reenter Add Category Message",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Boolean(name = "Category Added", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    public app.krista.extension.executor.ExtensionResponse handleReenterAddCategoryToMessage(
            @Field.Desc(name = "inputMap", type = "{ stateId: Text, Message ID: Text, Category: Text, Create Category: Boolean }") Map<String, Object> map) {
        LOGGER.info("SubCatalogRequest handleReenterFetchMailByLabel start: {}", map);
        String messageID = (String) map.get(OutlookResources.MESSAGE_ID);
        String category = (String) map.get(OutlookResources.CATEGORY);
        Boolean createCategory = (Boolean) map.get(OutlookResources.CREATE_CATEGORY);

        try {
            return messagingAreaImpl.addCategoryToMessage(messageID, category, createCategory);
        } catch (Exception cause) {
            return ExtensionResponseFactory.create(cause, "Failed to add category to message",
                    ExtensionResponse.Error.ExceptionType.LOGIC_ERROR);
        }
    }


    @SubCatalogRequest(
            name = SubCatalogConstants.CONFIRM_REENTER_REMOVE_CATEGORY,
            description = "Checks if user wants to re enter remove category and if yes, sends prompt to do so",
            type = CatalogRequest.Type.QUERY_SYSTEM
    )
    @SuppressWarnings("unchecked")
    public ExtensionResponse confirmReenterRemoveCategory(@Field.Desc(name = "inputMap",
            type = "{ Reenter: Boolean, stateId: Text, Message ID: Text, Category: Text }", required = true) Map<String, Object> map) {
        Boolean reenter = (Boolean) map.get(REENTER);
        String stateId = (String) map.get(OutlookResources.STATE_ID);
        Map<String, Object> state = internalStateManager.get(stateId);
        List<ValidationOrchestrator.ValidationResult> validationResults = getValidationResults(state);
        if (Boolean.FALSE.equals(reenter)) {
            return responseGenerator.generateFetchDenyResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    validationResults, null,
                    Map.of());
        }
        LOGGER.info(REENTER_WAS_TRUE_HENCE_CONTINUING);
        return responseGenerator.generateFetchResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                validationResults, "handleReenterRemoveCategory",
                Map.of(OutlookResources.STATE_ID, stateId, OutlookResources.MESSAGE_ID, map.get(OutlookResources.MESSAGE_ID), OutlookResources.CATEGORY, map.get(OutlookResources.CATEGORY)));
    }

    @SubCatalogRequest(
            name = "handleReenterRemoveCategory",
            description = "Handle reenter remove category",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Boolean(name = "Category Removed", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    public app.krista.extension.executor.ExtensionResponse handleReenterRemoveCategory(
            @Field.Desc(name = "inputMap", type = "{ stateId: Text, Message ID: Text, Category: Text }") Map<String, Object> map) {
        LOGGER.info("SubCatalogRequest handleReenterRemoveCategory start: {}", map);
        String messageID = (String) map.get(OutlookResources.MESSAGE_ID);
        String category = (String) map.get(OutlookResources.CATEGORY);

        try {
            return messagingAreaImpl.removeCategoryFromMessage(messageID, category);
        } catch (Exception cause) {
            return ExtensionResponseFactory.create(cause, "Failed to remove category",
                    ExtensionResponse.Error.ExceptionType.LOGIC_ERROR);
        }
    }

    @SubCatalogRequest(
            name = SubCatalogConstants.CONFIRM_REENTER_FETCH_INBOX,
            description = "Checks if user wants to re enter fetch Inbox Page Size or Number and if yes, sends prompt to do so",
            type = CatalogRequest.Type.QUERY_SYSTEM
    )
    @SuppressWarnings("unchecked")
    public ExtensionResponse confirmReenterFetchInbox(@Field.Desc(name = "inputMap",
            type = "{ Reenter: Boolean, stateId: Text, Page Number: Number, Page Size: Number }", required = true) Map<String, Object> map) {
        Boolean reenter = (Boolean) map.get(REENTER);
        String stateId = (String) map.get(OutlookResources.STATE_ID);
        Map<String, Object> state = internalStateManager.get(stateId);
        List<ValidationOrchestrator.ValidationResult> validationResults = getValidationResults(state);
        if (Boolean.FALSE.equals(reenter)) {
            return responseGenerator.generateFetchDenyResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    validationResults, null,
                    Map.of());
        }
        LOGGER.info(REENTER_WAS_TRUE_HENCE_CONTINUING);
        return responseGenerator.generateFetchResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                validationResults, "handleReenterFetchInbox", Map.of()
        );
    }

    @SubCatalogRequest(
            name = "handleReenterFetchInbox",
            description = "Handle reenter fetch Inbox",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Desc(name = "Inbox Mails", type = "[ Entity(Mail Details) ]", required = false)
    public app.krista.extension.executor.ExtensionResponse handleReenterFetchInbox(
            @Field.Desc(name = "inputMap", type = "{ stateId: Text, Page Number: Number, Page Size: Number }") Map<String, Object> map) {
        LOGGER.info("SubCatalogRequest handleReenterFetchInbox start: {}", map);
        Double pageNumber = (Double) map.get(OutlookResources.PAGE_NUMBER);
        Double pageSize = (Double) map.get(OutlookResources.PAGE_SIZE);

        try {
            List<Email> emails = account.getInboxFolder(null, null).getEmails(pageNumber, pageSize);
            return ExtensionResponseFactory.create(Map.of("Inbox Mails", emails.stream().map(email -> mailHandler.fromEmail(email, null)).collect(Collectors.toList())));
        } catch (Exception cause) {
            return ExtensionResponseFactory.create(cause, "Failed to fetch Inbox",
                    ExtensionResponse.Error.ExceptionType.LOGIC_ERROR);
        }
    }

    @SubCatalogRequest(
            name = SubCatalogConstants.CONFIRM_REENTER_FETCH_INBOX_WITH_PREFERENCE,
            description = "Checks if user want to re enter fetch Inbox with preferences Page Size or Number or Preference and if yes, sends prompt to do so",
            type = CatalogRequest.Type.QUERY_SYSTEM
    )
    @SuppressWarnings("unchecked")
    public ExtensionResponse confirmReenterFetchInboxWithPreferences(
            @Field.Desc(name = "inputMap",
                    type = "{ Reenter: Boolean, stateId: Text, Page Number: Number, Page Size: Number, Preference: { Mail Body: PickOne(Text|Html)} }",
                    required = true) Map<String, Object> map) {
        LOGGER.info("SubCatalogRequest confirmReenterFetchInboxWithPreferences start: {}", map);
        Boolean reenter = (Boolean) map.get(REENTER);
        String stateId = (String) map.get(OutlookResources.STATE_ID);
        Map<String, Object> state = internalStateManager.get(stateId);
        List<ValidationOrchestrator.ValidationResult> validationResults = getValidationResults(state);
        if (Boolean.FALSE.equals(reenter)) {
            return responseGenerator.generateFetchDenyResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    validationResults, null, Map.of());
        }
        LOGGER.info(REENTER_WAS_TRUE_HENCE_CONTINUING);
        return responseGenerator.generateFetchResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                validationResults, "handleReenterFetchInboxWithPreference", Map.of());
    }

    @SubCatalogRequest(name = "handleReenterFetchInboxWithPreference",
            description = "Handle reenter fetch inbox with preference",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Desc(name = "Mails", type = "[ Entity(Mail Details) ]", required = false)
    @SuppressWarnings("unchecked")
    public app.krista.extension.executor.ExtensionResponse handleReenterFetchInboxWithPreference(
            @Field.Desc(name = "inputMap",
                    type = "{ stateId: Text, Page Number: Number, Page Size: Number, Preference: { Mail Body: PickOne(Text|Html) } }",
                    required = false) Map<String, Object> map) {
        LOGGER.info("SubCatalogRequest handleReenterFetchInboxWithPreference start: {}", map);
        Double pageNumber = (Double) map.get(OutlookResources.PAGE_NUMBER);
        Double pageSize = (Double) map.get(OutlookResources.PAGE_SIZE);
        Map<String, Object> preference = (Map<String, Object>) map.get(OutlookResources.PREFERENCE);
        try {
            List<Email> emails = account.getInboxFolder(null, null).getEmails(pageNumber, pageSize, preference);
            return ExtensionResponseFactory.create(Map.of("Mails", emails.stream().map(email -> mailHandler.fromEmail(email, null)).collect(Collectors.toList())));
        } catch (Exception cause) {
            return ExtensionResponseFactory.create(cause, "Failed to fetch inbox mails with given preferences",
                    ExtensionResponse.Error.ExceptionType.LOGIC_ERROR);
        }

    }


    @SubCatalogRequest(
            name = SubCatalogConstants.CONFIRM_REENTER_FETCH_SENT,
            description = "Checks if user wants to re enter fetch Sent mail Page Size or Number and if yes, sends prompt to do so",
            type = CatalogRequest.Type.QUERY_SYSTEM
    )
    @SuppressWarnings("unchecked")
    public ExtensionResponse confirmReenterFetchSent(@Field.Desc(name = "inputMap",
            type = "{ Reenter: Boolean, stateId: Text, Page Number: Number, Page Size: Number }", required = true) Map<String, Object> map) {
        Boolean reenter = (Boolean) map.get(REENTER);
        String stateId = (String) map.get(OutlookResources.STATE_ID);
        Map<String, Object> state = internalStateManager.get(stateId);
        List<ValidationOrchestrator.ValidationResult> validationResults = getValidationResults(state);
        if (Boolean.FALSE.equals(reenter)) {
            return responseGenerator.generateFetchDenyResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    validationResults, null,
                    Map.of());
        }
        LOGGER.info(REENTER_WAS_TRUE_HENCE_CONTINUING);
        return responseGenerator.generateFetchResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                validationResults, "handleReenterFetchSent", Map.of()
        );
    }

    @SubCatalogRequest(
            name = "handleReenterFetchSent",
            description = "Handle reenter fetch sent mail",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Desc(name = "Sent Mails", type = "[ Entity(Mail Details) ]", required = false)
    public app.krista.extension.executor.ExtensionResponse handleReenterFetchSent(
            @Field.Desc(name = "inputMap", type = "{ stateId: Text, Page Number: Number, Page Size: Number }") Map<String, Object> map) {
        LOGGER.info("SubCatalogRequest handleReenterFetchMailInbox start: {}", map);
        Double pageNumber = (Double) map.get(OutlookResources.PAGE_NUMBER);
        Double pageSize = (Double) map.get(OutlookResources.PAGE_SIZE);

        try {
            List<Email> emails = account.getSentFolder().getEmails(pageNumber, pageSize);
            return ExtensionResponseFactory.create(Map.of("Sent Mails", emails.stream().map(email -> mailHandler.fromEmail(email, null)).collect(Collectors.toList())));

        } catch (Exception cause) {
            return ExtensionResponseFactory.create(cause, "Failed to fetch Sent Mails",
                    ExtensionResponse.Error.ExceptionType.LOGIC_ERROR);
        }
    }


}
