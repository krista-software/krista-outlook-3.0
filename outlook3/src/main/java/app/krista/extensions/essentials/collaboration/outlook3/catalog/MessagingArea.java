package app.krista.extensions.essentials.collaboration.outlook3.catalog;

import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extension.executor.ExtensionResponse;
import app.krista.extension.impl.anno.Attribute;
import app.krista.extension.impl.anno.CatalogRequest;
import app.krista.extension.impl.anno.Domain;
import app.krista.extension.impl.anno.Field;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.entities.MailDetails;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.errorhandlers.ErrorHandlingStateManager;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.errorhandlers.ExtensionResponseGenerator;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.extresp.*;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.validators.ValidationOrchestrator;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.validators.Validator;
import app.krista.extensions.essentials.collaboration.outlook3.impl.MailHandler;
import app.krista.extensions.essentials.collaboration.outlook3.impl.MessagingAreaImpl;
import app.krista.extensions.essentials.collaboration.outlook3.impl.util.Constants;
import app.krista.extensions.essentials.collaboration.outlook3.service.Account;
import app.krista.extensions.essentials.collaboration.outlook3.service.Email;
import app.krista.extensions.essentials.collaboration.outlook3.service.Folder;
import app.krista.extensions.util.EventHandler;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.ksdk.context.RequestContext;
import app.krista.model.base.EntityValue;
import app.krista.model.base.File;
import app.krista.model.base.FreeForm;
import com.kristasoft.common.holders.ThreadLocalProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Domain(id = "catEntryDomain_5fa2fc97-4b17-44cf-b98f-aa91a459a091",
        name = "Collaboration",
        ecosystemId = "catEntryEcosystem_84b53163-327b-4b1b-8c96-9334d292f9f5",
        ecosystemName = "Essentials",
        ecosystemVersion = "ef7472cb-3aaa-4570-965b-6b5b6a25e7de")
public class MessagingArea {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessagingArea.class);
    private final Account account;
    private final EventHandler eventHandler;
    private final MailHandler mailHandler;
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    private RequestContext requestContext;
    private AuthorizationContext authorizationContext;
    private final MessagingAreaImpl messagingAreaImpl;
    private final ExtensionResponseGenerator responseGenerator;
    private final ErrorHandlingStateManager internalStateManager;
    private final ValidationOrchestrator validationOrchestrator;

    @Inject
    public MessagingArea(Account account, RequestContext requestContext, AuthorizationContext authorizationContext,
                         EventHandler eventHandler, MailHandler mailHandler,
                         MessagingAreaImpl messagingAreaImpl, ExtensionResponseGenerator responseGenerator,
                         ErrorHandlingStateManager internalStateManager, ValidationOrchestrator validationOrchestrator) {
        this.account = account;
        this.requestContext = requestContext;
        this.authorizationContext = authorizationContext;
        this.eventHandler = eventHandler;
        this.mailHandler = mailHandler;
        this.messagingAreaImpl = messagingAreaImpl;
        this.responseGenerator = responseGenerator;
        this.internalStateManager = internalStateManager;
        this.validationOrchestrator = validationOrchestrator;
    }

    @CatalogRequest(
            id = "localDomainRequest_e39048cc-1795-4eee-8400-8fc3061c4e87",
            name = "Fetch All Labels",
            description = "Returns list of labels",
            area = "Messaging",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Desc(name = "Labels", type = "[ Text ]", required = false)
    public ExtensionResponse fetchAllLabels() {
        return ExtensionResponseFactory.create(Map.of("Labels", account.getFolderNames()));
    }

    @CatalogRequest(
            id = "localDomainRequest_ac177adc-e633-4ca1-baf8-7ce7efc5c0e5",
            name = "Fetch Mail By Message Id",
            description = "Accepts message Id as input and returns mail. In case of invalid input, this will return empty data.",
            area = "Messaging",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Desc(name = "Mail", type = "Entity(Mail Details)", required = false)
    public ExtensionResponse fetchMailByMessageId(
            @Field(name = "Message ID", type = "Text") String messageID) {
        try {
            List<ValidationOrchestrator.ValidationResult> validationResults =
                    validationOrchestrator.validate(Map.of(Validator.ValidationResource.MESSAGE_ID, messageID));

            if (validationResults.isEmpty()) {
                MailDetails mailDetails = mailHandler.fromEmail(account.getEmail(messageID), null);
                if (mailDetails != null) {
                    LOGGER.info("fetchMailByMessageId: ID {}, from {}, subject {}, timestamp {}",
                            mailDetails.messageID, mailDetails.from, mailDetails.subject, new Date(mailDetails.sendDateAndTime));
                }
                return ExtensionResponseFactory.create(Map.of("Mail", mailDetails));
            } else {
                String stateId = UUID.randomUUID().toString();
                internalStateManager.put(stateId, Constants.GSON.toJson(Map.of(OutlookResources.MESSAGE_ID, messageID,
                        SubCatalogConstants.VALIDATION_RESULTS, validationResults), Map.class));
                return responseGenerator.generateConfirmationResponse(
                        ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                        SubCatalogConstants.CONFIRM_REENTER_FETCH_MAIL, Map.of(OutlookResources.STATE_ID, stateId));
            }
        } catch (MustAuthorizeException cause) {
            LOGGER.error(cause.getMessage());
            throw cause;
        } catch (Exception cause) {
            LOGGER.error("Error occurred while fetch mail by message id :{}", cause.getMessage());
            return ExtensionResponseFactory.create("Error occurred while fetch mail by message id", ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR,
                    List.of(RemediationActionFactory.createInformActionALLParticipants("Error occurred while fetch mail by message id", List.of())),
                    null, null);
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_95edb739-f511-453c-b0ec-647daf0df206",
            name = "Move Message",
            description = "Accepts message ID, and folder name as input and move one message from source folder to another folder and returns response message.",
            area = "Messaging",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field(name = "Message ID", type = "Text", required = false)
    public ExtensionResponse moveMessage(
            @Field(name = "Message ID", type = "Text") String messageID,
            @Field(name = "Folder Name", type = "Text") String folderName) {
        try {
            LOGGER.info("Moving message with ID {} to folder: {}", messageID, folderName);
            List<ValidationOrchestrator.ValidationResult> validationResults =
                    validationOrchestrator.validate(Map.of(Validator.ValidationResource.MESSAGE_ID, messageID,
                            Validator.ValidationResource.FOLDER_NAME, folderName));

            if (validationResults.isEmpty()) {
                Email email = account.getEmail(messageID);
                Folder folder = account.getFolderByName(List.of(folderName.split(Constants.FORWARD_SLASH)));
                return ExtensionResponseFactory.create(Map.of(OutlookResources.MESSAGE_ID, email.moveToFolder(folder)));
            } else {
                String stateId = UUID.randomUUID().toString();
                internalStateManager.put(stateId, Constants.GSON.toJson(Map.of(OutlookResources.MESSAGE_ID, messageID,
                        SubCatalogConstants.VALIDATION_RESULTS, validationResults)));
                return responseGenerator.generateConfirmationResponse(
                        ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                        SubCatalogConstants.CONFIRM_REENTER_MOVE_MESSAGE, Map.of(OutlookResources.STATE_ID, stateId, OutlookResources.MESSAGE_ID, messageID, OutlookResources.FOLDER_NAME, folderName));
            }
        } catch (MustAuthorizeException cause) {
            LOGGER.error(cause.getMessage());
            throw cause;
        } catch (Exception cause) {
            LOGGER.error("Error occurred while moving message to folder :{}", cause.getMessage());
            return ExtensionResponseFactory.create("Error occurred while moving message to folder", ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR,
                    List.of(RemediationActionFactory.createInformActionALLParticipants("Error occurred while moving message to folder", List.of())),
                    null, null);
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_02494c43-a71f-47bb-935a-37736a4ecac0",
            name = "Reply To All With CC and BCC",
            description = "In this request, the user can respond to everyone on the thread. Other recipients will see a message user 'Reply All' to, whether they're in the 'To' or 'Cc' fields. 'To', Cc' and 'Bcc' fields are optional to update the existing users.",
            area = "Messaging",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Boolean(name = "Is Successful", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    public ExtensionResponse replyToAllWithCCAndBCC(
            @Field(name = "Message Id", type = "Text") String messageId,
            @Field(name = "To", type = "Text", required = false) String to,
            @Field(name = "Cc", type = "Text", required = false) String cc,
            @Field(name = "Bcc", type = "Text", required = false) String bcc,
            @Field(name = "Reply To", type = "Text", required = false) String replyTo,
            @Field(name = "Message", type = "RichText", required = false) String message,
            @Field.File(name = "Attachments", multipleFileUpload = true, required = false) List<File> attachments,
            @Field.PickOne(name = "BodyType", values = {"Text", "HTML"}, required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) String bodyType) {
        try {
            LOGGER.info("replyToAll: messageId: {}; message: {}", messageId, message);
            List<ValidationOrchestrator.ValidationResult> validationResults =
                    validationOrchestrator.validate(Map.of(Validator.ValidationResource.MESSAGE_ID, messageId,
                            Validator.ValidationResource.TO, validateString(to)
                            , Validator.ValidationResource.CC, validateString(cc)
                            , Validator.ValidationResource.BCC, validateString(bcc)
                            , Validator.ValidationResource.REPLY_TO, validateString(replyTo)));
            if (validationResults.isEmpty()) {
                return messagingAreaImpl.replyToAllWithCCAndBCC(attachments, messageId, to, cc, bcc, replyTo, message, bodyType);
            } else {
                String stateId = UUID.randomUUID().toString();
                internalStateManager.put(stateId, Constants.GSON.toJson(Map.of(SubCatalogConstants.VALIDATION_RESULTS, validationResults)));
                return responseGenerator.generateConfirmationResponse(
                        ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                        SubCatalogConstants.CONFIRM_REENTER_REPLY_TO_ALL_WITH_FIELDS, StateMapperUtil.addReplyToALLFieldsMetaToMap(messageId, to, cc, bcc, replyTo, message, attachments, bodyType, stateId));
            }
        } catch (MustAuthorizeException cause) {
            LOGGER.error(cause.getMessage());
            throw cause;
        } catch (Exception cause) {
            LOGGER.error("Error occurred while Reply To All With CC and BCC :{}", cause.getMessage());
            return ExtensionResponseFactory.create("Error occurred while Reply To All With CC and BCC", ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR,
                    List.of(RemediationActionFactory.createInformActionALLParticipants("Error occurred while Reply To All With CC and BCC", List.of())),
                    null, null);
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_19b1a828-2f61-49e7-a75a-9956f7d12c5c",
            name = "Reply To All",
            description = "In this request, the user can respond to everyone on the thread. Other recipients will see a message user 'Reply All' to, whether they're in the 'To' or 'Cc' fields.",
            area = "Messaging",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Boolean(name = "Is Successful", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    public ExtensionResponse replyToAll(
            @Field(name = "Message Id", type = "Text") String messageId,
            @Field(name = "Message", type = "RichText", required = false) String message,
            @Field.File(name = "Attachments", multipleFileUpload = true, required = false) List<File> attachments,
            @Field.PickOne(name = "BodyType", values = {"Text", "HTML"}, required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) String bodyType) {
        try {
            LOGGER.info("replyToAll: messageId: {}; message: {}", messageId, message);

            List<ValidationOrchestrator.ValidationResult> validationResults =
                    validationOrchestrator.validate(Map.of(Validator.ValidationResource.MESSAGE_ID, messageId));
            if (validationResults.isEmpty()) {
                return messagingAreaImpl.replyToAll(attachments, messageId, message, bodyType);
            } else {
                String stateId = UUID.randomUUID().toString();
                internalStateManager.put(stateId, Constants.GSON.toJson(Map.of(OutlookResources.MESSAGE_ID, messageId,
                        SubCatalogConstants.VALIDATION_RESULTS, validationResults)));
                return responseGenerator.generateConfirmationResponse(
                        ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                        SubCatalogConstants.CONFIRM_REENTER_REPLY_TO_ALL, StateMapperUtil.addReplyToALLMetaToMap(messageId, message, attachments, bodyType, stateId));
            }
        } catch (MustAuthorizeException cause) {
            LOGGER.error(cause.getMessage());
            throw cause;
        } catch (Exception cause) {
            LOGGER.error("Error occurred while Reply To All  :{}", cause.getMessage());
            return ExtensionResponseFactory.create("Error occurred while Reply To All", ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR,
                    List.of(RemediationActionFactory.createInformActionALLParticipants("Error occurred while Reply To All", List.of())),
                    null, null);
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_cf7ac51e-752b-44e8-a55a-2a5dd4dbfbef",
            name = "Fetch Sent",
            description = "Accepts page number, and page size as input and returns list of mails from sent folder. Page number, and page size are optional parameters.",
            area = "Messaging",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Desc(name = "Sent Mails", type = "[ Entity(Mail Details) ]", required = false)
    public ExtensionResponse fetchSent(
            @Field(name = "Page Number", type = "Number", required = false) Double pageNumber,
            @Field(name = "Page Size", type = "Number", required = false) Double pageSize) {
        try {
            LOGGER.info("fetchSent: pageNumber: {}; pageSize: {}", pageNumber, pageSize);
            Map<Validator.ValidationResource, String> validationResourceMap = ValidationResourceUtil.prepareValidateFetchInboxMap(pageNumber, pageSize);
            if (validationResourceMap.isEmpty()) {
                return fetchSentResponse(pageNumber, pageSize);
            } else {
                List<ValidationOrchestrator.ValidationResult> validationResults = validationOrchestrator.validate(validationResourceMap);
                if (validationResults.isEmpty()) {
                    return fetchSentResponse(pageNumber, pageSize);
                } else {
                    String stateId = UUID.randomUUID().toString();
                    internalStateManager.put(stateId, Constants.GSON.toJson(Map.of(SubCatalogConstants.VALIDATION_RESULTS, validationResults)));
                    return responseGenerator.generateConfirmationResponse(
                            ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                            SubCatalogConstants.CONFIRM_REENTER_FETCH_SENT, StateMapperUtil.addPageMetaDataToMap(pageNumber, pageSize, stateId));
                }
            }
        } catch (MustAuthorizeException cause) {
            LOGGER.error(cause.getMessage());
            throw cause;
        } catch (Exception cause) {
            LOGGER.error("Error occurred while fetch sent:{}", cause.getMessage());
            return ExtensionResponseFactory.create("Error occurred while fetch sent", ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR,
                    List.of(RemediationActionFactory.createInformActionALLParticipants("Error occurred while fetch sent", List.of())),
                    null, null);
        }
    }

    private ExtensionResponse fetchSentResponse(Double pageNumber, Double pageSize) {
        List<Email> emails = account.getSentFolder().getEmails(pageNumber, pageSize);
        return ExtensionResponseFactory.create(Map.of("Sent Mails", emails.stream().map(email -> mailHandler.fromEmail(email, null)).collect(Collectors.toList())));

    }

    @CatalogRequest(
            id = "localDomainRequest_7519e728-cca1-447f-8c58-ad4e80eefb00",
            name = "Forward Mail",
            description = "This request allows a sender to forward the received email to other recipients.",
            area = "Messaging",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field(name = "Is Forwarded", type = "Switch", required = false)
    public ExtensionResponse forwardMail(
            @Field(name = "Message Id", type = "Text") String messageId,
            @Field(name = "To", type = "Text") String to,
            @Field(name = "Message", type = "RichText", required = false) String message,
            @Field.PickOne(name = "BodyType", values = {"Text", "HTML"}, required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) String bodyType) {
        try {
            List<ValidationOrchestrator.ValidationResult> validationResults =
                    validationOrchestrator.validate(Map.of(Validator.ValidationResource.MESSAGE_ID, messageId, Validator.ValidationResource.TO, to));

            if (validationResults.isEmpty()) {
                return messagingAreaImpl.forwardMail(messageId, to, message, bodyType);
            } else {
                String stateId = UUID.randomUUID().toString();
                internalStateManager.put(stateId, Constants.GSON.toJson(Map.of(SubCatalogConstants.VALIDATION_RESULTS, validationResults)));
                return responseGenerator.generateConfirmationResponse(
                        ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults, SubCatalogConstants.CONFIRM_REENTER_FORWARD_MAIL, StateMapperUtil.addForwardMailMetaToMap(messageId, message, to, bodyType, stateId));
            }
        } catch (MustAuthorizeException cause) {
            LOGGER.error(cause.getMessage());
            throw cause;
        } catch (Exception cause) {
            LOGGER.error("Error occurred while forward mail :{}", cause.getMessage());
            return ExtensionResponseFactory.create("Error occurred while forward mail", ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR,
                    List.of(RemediationActionFactory.createInformActionALLParticipants("Error occurred while forward mail", List.of())),
                    null, null);
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_45b7479d-c009-4b0f-85e6-0fe38d9fc35d",
            name = "Fetch Mail Details By Query",
            description = "Accepts search query as input and returns list of mails",
            area = "Messaging",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Desc(name = "Mails", type = "[ Entity(Mail Details) ]", required = false)
    public ExtensionResponse fetchMailDetailsByQuery(
            @Field(name = "Query", type = "Text") String query) {
        try {
            LOGGER.info("fetchMailDetailsByQuery: {}", query);
            List<Email> emails = account.searchEmails(query);
            List<MailDetails> response = emails.stream().map(email -> mailHandler.fromEmail(email, null)).collect(Collectors.toList());
            return ExtensionResponseFactory.create(Map.of("Mails", response));
        } catch (MustAuthorizeException cause) {
            LOGGER.error(cause.getMessage());
            throw cause;
        } catch (Exception cause) {
            return ExtensionResponseFactory.create(cause, "Invalid query provided, Please check.", ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    List.of(RemediationActionFactory.createInformActionALLParticipants("Invalid query provided, Please check.", List.of())),
                    null, null);
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_6d34be22-e420-4087-b55d-0659f899b140",
            name = "Send Mail",
            description = "Accepts subject, message, attachments, to, bcc, cc, reply to as input and returns response message. Attachments, bcc, cc, and reply to are optional inputs.",
            area = "Messaging",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field(name = "Message", type = "Text", required = false)
    public ExtensionResponse sendMail(
            @Field(name = "Subject", type = "Text") String subject,
            @Field(name = "Message", type = "RichText") String message,
            @Field(name = "Attachments", type = "File", required = false) List<File> attachments,
            @Field(name = "To", type = "Text") String to,
            @Field(name = "Bcc", type = "Text", required = false) String bcc,
            @Field(name = "Cc", type = "Text", required = false) String cc,
            @Field(name = "Reply To", type = "Text", required = false) String replyTo,
            @Field.PickOne(name = "BodyType", values = {"Text", "HTML"}, required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) String bodyType) {
        try {
            List<ValidationOrchestrator.ValidationResult> validationResults =
                    validationOrchestrator.validate(Map.of(
                            Validator.ValidationResource.TO, to
                            , Validator.ValidationResource.CC, validateString(cc)
                            , Validator.ValidationResource.BCC, validateString(bcc)
                            , Validator.ValidationResource.REPLY_TO, validateString(replyTo)));
            if (validationResults.isEmpty()) {
                return messagingAreaImpl.sendMail(subject, message, attachments, to, cc, bcc, replyTo, bodyType);
            } else {
                String stateId = UUID.randomUUID().toString();
                internalStateManager.put(stateId, Constants.GSON.toJson(Map.of(SubCatalogConstants.VALIDATION_RESULTS, validationResults)));
                return responseGenerator.generateConfirmationResponse(
                        ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                        SubCatalogConstants.CONFIRM_REENTER_SEND_MAIL, StateMapperUtil.addSendMailMetaToMap(subject, to, cc, bcc, replyTo, message, attachments, bodyType, stateId));
            }
        } catch (MustAuthorizeException cause) {
            LOGGER.error(cause.getMessage());
            throw cause;
        } catch (Exception cause) {
            LOGGER.error("Error occurred while send mail :{}", cause.getMessage());
            return ExtensionResponseFactory.create("Error occurred while send mail", ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR,
                    List.of(RemediationActionFactory.createInformActionALLParticipants("Error occurred while send mail", List.of())),
                    null, null);
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_8a3966c3-8d95-4ebe-a294-4458200f3392",
            name = "Send Mail With Table",
            description = "Accepts subject, message, attachments, to, bcc, cc, List of Entities, reply to as input and returns response message. Attachments, bcc, cc, and reply to are optional inputs.",
            area = "Messaging",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Text(name = "Message", required = false, attributes = {@Attribute(name = "visualWidth", value = "M")})
    public ExtensionResponse sendMailWithTable(
            @Field.Text(name = "Subject", attributes = {@Attribute(name = "visualWidth", value = "S")}) String subject,
            @Field(name = "Message", type = "RichText", attributes = {@Attribute(name = "visualWidth", value = "L")}) String message,
            @Field.File(name = "Attachments", multipleFileUpload = true, required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}) List<File> attachments,
            @Field.Text(name = "To", attributes = {@Attribute(name = "visualWidth", value = "S")}) String to,
            @Field.Text(name = "Bcc", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}) String bcc,
            @Field.Text(name = "Cc", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}) String cc,
            @Field.Text(name = "Reply To", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}) String replyTo,
            @Field.Desc(name = "Entity List", type = "[ Entity ]") List<EntityValue> entityList,
            @Field.Desc(name = "Remove Entity Field From Table", type = "[ Text ]", required = false) List<String> removeEntityFieldFromTable) {
        try {
            List<ValidationOrchestrator.ValidationResult> validationResults =
                    validationOrchestrator.validate(Map.of(
                            Validator.ValidationResource.TO, to
                            , Validator.ValidationResource.CC, validateString(cc)
                            , Validator.ValidationResource.BCC, validateString(bcc)
                            , Validator.ValidationResource.REPLY_TO, validateString(replyTo)));
            if (validationResults.isEmpty()) {
                return messagingAreaImpl.sendMailWithTable(subject, message, attachments, to, cc, bcc, replyTo, entityList, removeEntityFieldFromTable);
            } else {
                String stateId = UUID.randomUUID().toString();
                internalStateManager.put(stateId, Constants.GSON.toJson(StateMapperUtil.addSendMailWithTableAttachmentToMap(attachments, entityList, removeEntityFieldFromTable, validationResults)));
                internalStateManager.putMetaInfo(stateId, Map.of(OutlookResources.ENTITY_LIST, entityList));
                return responseGenerator.generateConfirmationResponse(
                        ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                        SubCatalogConstants.CONFIRM_REENTER_SEND_MAIL_WITH_TABLE, StateMapperUtil.addSendMailWithTableMetaToMap(subject, to, cc, bcc, replyTo, message, stateId));
            }
        } catch (MustAuthorizeException cause) {
            LOGGER.error(cause.getMessage());
            throw cause;
        } catch (Exception cause) {
            LOGGER.error("Error occurred while Send Mail With Table :{}", cause.getMessage());
            return ExtensionResponseFactory.create("Error occurred while Send Mail With Table", ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR,
                    List.of(RemediationActionFactory.createInformActionALLParticipants("Error occurred while Send Mail With Table", List.of())),
                    null, null);
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_bbdc1184-9dc1-4448-8bdb-ec6c9ee913a7",
            name = "Fetch Inbox",
            description = "Accepts page number, and page size as input and returns list of mail. Page number, and page size are optional parameters.",
            area = "Messaging",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Desc(name = "Inbox Mails", type = "[ Entity(Mail Details) ]", required = false)
    public ExtensionResponse fetchInbox(
            @Field(name = "Page Number", type = "Number", required = false) Double pageNumber,
            @Field(name = "Page Size", type = "Number", required = false) Double pageSize) {
        try {
            LOGGER.info("fetchInbox: pageNumber: {}; pageSize: {}", pageNumber, pageSize);
            Map<Validator.ValidationResource, String> validationResourceMap = ValidationResourceUtil.prepareValidateFetchInboxMap(pageNumber, pageSize);
            if (validationResourceMap.isEmpty()) {
                return fetchInboxResponse(pageNumber, pageSize);
            } else {
                List<ValidationOrchestrator.ValidationResult> validationResults = validationOrchestrator.validate(validationResourceMap);
                if (validationResults.isEmpty()) {
                    return fetchInboxResponse(pageNumber, pageSize);
                } else {
                    String stateId = UUID.randomUUID().toString();
                    internalStateManager.put(stateId, Constants.GSON.toJson(Map.of(SubCatalogConstants.VALIDATION_RESULTS, validationResults)));
                    return responseGenerator.generateConfirmationResponse(
                            ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                            SubCatalogConstants.CONFIRM_REENTER_FETCH_INBOX, StateMapperUtil.addPageMetaDataToMap(pageNumber, pageSize, stateId));
                }
            }
        } catch (MustAuthorizeException cause) {
            LOGGER.error(cause.getMessage());
            throw cause;
        } catch (Exception cause) {
            LOGGER.error("Error occurred while fetch inbox :{}", cause.getMessage());
            return ExtensionResponseFactory.create("Error occurred while fetch inbox ", ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR,
                    List.of(RemediationActionFactory.createInformActionALLParticipants("Error occurred while fetch inbox ", List.of())),
                    null, null);
        }

    }

    private ExtensionResponse fetchInboxResponse(Double pageNumber, Double pageSize) {
        List<Email> emails = account.getInboxFolder(null, null).getEmails(pageNumber, pageSize);
        return ExtensionResponseFactory.create(Map.of("Inbox Mails", emails.stream().map(email -> mailHandler.fromEmail(email, null)).collect(Collectors.toList())));
    }

    @CatalogRequest(
            id = "localDomainRequest_20716cd4-a8a9-43ab-ae5c-4bef25ed4623",
            name = "Mark Message",
            description = "Accepts message ID, and label as input and mark mail as read/unread and returns response message",
            area = "Messaging",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field(name = "Response", type = "Text", required = false)
    public ExtensionResponse markMessage(
            @Field(name = "Message ID", type = "Text") String messageID,
            @Field.Desc(name = "Label", type = "PickOne(Read|Unread)") String label) {
        try {
            List<ValidationOrchestrator.ValidationResult> validationResults =
                    validationOrchestrator.validate(Map.of(Validator.ValidationResource.MESSAGE_ID, messageID));
            if (validationResults.isEmpty()) {
                return messagingAreaImpl.markMessage(messageID, label);
            } else {
                String stateId = UUID.randomUUID().toString();
                internalStateManager.put(stateId, Constants.GSON.toJson(Map.of(OutlookResources.MESSAGE_ID, messageID,
                        SubCatalogConstants.VALIDATION_RESULTS, validationResults)));
                return responseGenerator.generateConfirmationResponse(
                        ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                        SubCatalogConstants.CONFIRM_REENTER_MARK_MESSAGE, Map.of(
                                OutlookResources.STATE_ID, stateId
                                , OutlookResources.LABEL, label, OutlookResources.MESSAGE_ID, messageID));
            }
        } catch (MustAuthorizeException cause) {
            LOGGER.error(cause.getMessage());
            throw cause;
        } catch (Exception cause) {
            LOGGER.error("Error occurred while mark message :{}", cause.getMessage());
            return ExtensionResponseFactory.create("Error occurred while mark message ", ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR,
                    List.of(RemediationActionFactory.createInformActionALLParticipants("Error occurred while mark message ", List.of())),
                    null, null);
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_37baf692-3fd8-424d-afc4-63bdbe6de2f4",
            name = "Reply To Mail With CC and BCC",
            description = "Accepts message ID, message, attachments, cc, bcc and reply to as inputs and returns response message.",
            area = "Messaging",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field(name = "Message", type = "Text", required = false, attributes = {}, options = {})
    public ExtensionResponse replyToMailWithCCAndBCC(
            @Field(name = "Message Id", type = "Text") String messageId,
            @Field(name = "Message", type = "RichText", required = false) String message,
            @Field.File(name = "Attachments", multipleFileUpload = true, required = false) List<File> attachments,
            @Field(name = "To", type = "Text", required = false) String to,
            @Field(name = "Cc", type = "Text", required = false) String cc,
            @Field(name = "Bcc", type = "Text", required = false) String bcc,
            @Field(name = "Reply To", type = "Text", required = false) String replyTo,
            @Field.PickOne(name = "BodyType", values = {"Text", "HTML"}, required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) String bodyType) {
        try {
            LOGGER.info("markMessage: messageID: {}; category: {}", messageId, bodyType);
            List<ValidationOrchestrator.ValidationResult> validationResults =
                    validationOrchestrator.validate(Map.of(Validator.ValidationResource.MESSAGE_ID, messageId,
                            Validator.ValidationResource.TO, validateString(to)
                            , Validator.ValidationResource.CC, validateString(cc)
                            , Validator.ValidationResource.BCC, validateString(bcc)
                            , Validator.ValidationResource.REPLY_TO, validateString(replyTo)));
            if (validationResults.isEmpty()) {
                return messagingAreaImpl.replyToMailWithCCAndBCC(attachments, messageId, to, cc, bcc, replyTo, message, bodyType);
            } else {
                String stateId = UUID.randomUUID().toString();
                internalStateManager.put(stateId, Constants.GSON.toJson(Map.of(SubCatalogConstants.VALIDATION_RESULTS, validationResults)));
                return responseGenerator.generateConfirmationResponse(
                        ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                        SubCatalogConstants.CONFIRM_REENTER_REPLY_TO_MAIL_WITH_FIELDS, StateMapperUtil.addReplyToALLFieldsMetaToMap(messageId, to, cc, bcc, replyTo, message, attachments, bodyType, stateId));
            }
        } catch (MustAuthorizeException cause) {
            LOGGER.error(cause.getMessage());
            throw cause;
        } catch (Exception cause) {
            LOGGER.error("Error occurred while Reply To Mail With CC and BCC :{}", cause.getMessage());
            return ExtensionResponseFactory.create("Error occurred while Reply To Mail With CC and BCC ", ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR,
                    List.of(RemediationActionFactory.createInformActionALLParticipants("Error occurred while Reply To Mail With CC and BCC ", List.of())),
                    null, null);
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_c8485079-5592-4d29-9818-41d99368a35d",
            name = "Reply To Mail",
            description = "Accepts message ID, message, and attachments as input and returns response message. Attachment is optional input.",
            area = "Messaging",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field(name = "Message", type = "Text", required = false)
    public ExtensionResponse replyToMail(
            @Field(name = "Message ID", type = "Text") String messageID,
            @Field(name = "Message", type = "RichText", required = false) String message,
            @Field(name = "Attachments", type = "File", required = false) List<File> attachments,
            @Field.PickOne(name = "BodyType", values = {"Text", "HTML"}, required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) String bodyType) {
        try {
            LOGGER.info("replyToMail: messageID: {}; message: {}", messageID, message);
            List<ValidationOrchestrator.ValidationResult> validationResults =
                    validationOrchestrator.validate(Map.of(Validator.ValidationResource.MESSAGE_ID, messageID));
            if (validationResults.isEmpty()) {
                return messagingAreaImpl.replyToMail(attachments, messageID, message, bodyType);
            } else {
                String stateId = UUID.randomUUID().toString();
                internalStateManager.put(stateId, Constants.GSON.toJson(Map.of(OutlookResources.MESSAGE_ID, messageID,
                        SubCatalogConstants.VALIDATION_RESULTS, validationResults)));
                return responseGenerator.generateConfirmationResponse(
                        ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                        SubCatalogConstants.CONFIRM_REENTER_REPLY_TO_MAIL, StateMapperUtil.addReplyToALLMetaToMap(messageID, message, attachments, bodyType, stateId));
            }
        } catch (MustAuthorizeException cause) {
            LOGGER.error(cause.getMessage());
            throw cause;
        } catch (Exception cause) {
            LOGGER.error("Error occurred while Reply To Mail  :{}", cause.getMessage());
            return ExtensionResponseFactory.create("Error occurred while Reply To Mail", ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR,
                    List.of(RemediationActionFactory.createInformActionALLParticipants("Error occurred while Reply To Mail", List.of())),
                    null, null);
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_2319f5aa-5972-454f-8cf0-7f603a3e9eea",
            name = "Fetch Inbox Async",
            description = "Fetches inbox mails asynchronously and returns task ID. The task ID will get used in getResult request to get mails. Maximum limit is 500 mails.",
            area = "Messaging",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field(name = "Task ID", type = "Text", required = false)
    public String fetchInboxAsync() {
        try {
            final String taskId = UUID.randomUUID().toString();
            boolean useSetupMail = !requestContext.invokeAsUser();
            String accountID = authorizationContext.getAuthorizedAccount().getAccountId();
            Map<Class<?>, Object> threadLocals = ThreadLocalProxy.getAll();

            executorService.submit(() -> {
                try {
                    ThreadLocalProxy.setAll(threadLocals);
                    mailHandler.setAuthorizationContext(ThreadLocalProxy.getThreadLocal(AuthorizationContext.class).get());

                    List<Email> emails = account.getInboxFolder(useSetupMail, accountID).getEmails(useSetupMail);
                    List<MailDetails> mailDetails = emails.stream().map(email -> {
                        LOGGER.info("Wait for event request running for email {}", email.getSubject());
                        return mailHandler.fromEmail(email, useSetupMail);
                    }).collect(Collectors.toList());
                    LOGGER.info("Completed wait for event request.");
                    FreeForm freeForm = new FreeForm();
                    freeForm.put(Constants.DATA, "[ Entity(Mail Details) ]", mailDetails);
                    LOGGER.info("Adding fetched results to event handled.");
                    eventHandler.handleEvent(taskId, freeForm);
                } catch (MustAuthorizeException cause) {
                    LOGGER.error(cause.getMessage());
                    throw cause;
                } catch (Exception cause) {
                    throw new IllegalStateException(cause);
                } finally {
                    ThreadLocalProxy.removeAll();
                }
            });
            return taskId;
        } catch (IllegalStateException cause) {
            LOGGER.error("Illegal state error: {}", cause.getMessage(), cause);
            throw new IllegalStateException(cause);
        }
    }

    @SuppressWarnings("unchecked")
    @CatalogRequest(
            id = "localDomainRequest_0643ea55-dbbb-4f47-9136-9e523c3eadc9",
            name = "Get Result",
            description = "Accept task ID as input and return mails. Get this task ID from fetchInboxAsync request",
            area = "Messaging",
            type = CatalogRequest.Type.WAIT_FOR_EVENT)
    @Field.Desc(name = "Mail Details", type = "[ Entity(Mail Details) ]", required = false)
    public ExtensionResponse getResult(
            @Field(name = "eventName", type = "Text") String eventName,
            @Field(name = "eventData", type = "FreeForm") FreeForm eventData,
            @Field(name = "Task ID", type = "Text") String taskID) {

        LOGGER.info("getResult: eventName: {}; eventData: {}; taskID: {}", eventName, eventData, taskID);
        if (eventName.equals(taskID)) {
            return ExtensionResponseFactory.create(Map.of("Mail Details", (List<MailDetails>) eventData.get(Constants.DATA)));
        }
        LOGGER.error("Invalid task ID: {}", taskID);
        throw new IllegalStateException(Constants.INVALID_TASK_ID);
    }

    @CatalogRequest(
            id = "localDomainRequest_82e8a567-a80f-4ff0-876b-8bc14072f322",
            name = "Fetch Mails By Label",
            description = "Accepts label, page number, and page size as input and returns list of mail. Page number, and page size are optional input.",
            area = "Messaging",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Desc(name = "Mails", type = "[ Entity(Mail Details) ]", required = false)
    public ExtensionResponse fetchMailsByLabel(
            @Field(name = "Label", type = "Text") String label,
            @Field(name = "Page Number", type = "Number", required = false) Double pageNumber,
            @Field(name = "Page Size", type = "Number", required = false) Double pageSize) {
        try {
            LOGGER.info("fetchMailsByLabel: label: {}, pageNumber: {}; pageSize: {}", label, pageNumber, pageSize);
            List<ValidationOrchestrator.ValidationResult> validationResults =
                    validationOrchestrator.validate(ValidationResourceUtil.prepareValidateLabelMap(label, pageNumber, pageSize));

            if (validationResults.isEmpty()) {
                return messagingAreaImpl.fetchMailByLabel(label, pageNumber, pageSize);
            } else {
                String stateId = UUID.randomUUID().toString();
                internalStateManager.put(stateId, Constants.GSON.toJson(Map.of(SubCatalogConstants.VALIDATION_RESULTS, validationResults)));
                return responseGenerator.generateConfirmationResponse(
                        ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                        SubCatalogConstants.CONFIRM_REENTER_FETCH_MAIL_BY_LABEL, StateMapperUtil.addFetchMailByLableMetaToMap(label, pageNumber, pageSize, stateId));
            }
        } catch (MustAuthorizeException cause) {
            LOGGER.error(cause.getMessage());
            throw cause;
        } catch (Exception cause) {
            LOGGER.error("Error occurred while Fetch Mails By Label  :{}", cause.getMessage());
            return ExtensionResponseFactory.create("Error occurred while Fetch Mails By Label", ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR,
                    List.of(RemediationActionFactory.createInformActionALLParticipants("Error occurred while Fetch Mails By Label", List.of())),
                    null, null);
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_d0e3cd12-9a47-40de-a213-a0d0c1ba0507",
            name = "Mail Received Alert",
            description = "Mail Received Alert",
            area = "Messaging",
            type = CatalogRequest.Type.WAIT_FOR_EVENT)
    @Field.Desc(name = "Mail Details", type = "Entity(Mail Details)", required = false)
    public ExtensionResponse mailReceivedAlert(
            @Field(name = "eventName", type = "Text") String eventName,
            @Field(name = "eventData", type = "FreeForm") FreeForm eventData) {
        try {
            if (eventName.equalsIgnoreCase(Constants.MAIL_RECEIVED)) {
                MailDetails mailDetails = mailHandler.fromEmail(account.getEmail((String) eventData.get(Constants.MESSAGE_ID)), null);
                if (mailDetails != null) {
                    LOGGER.info("Allow Alert Mail Triggered : ID {}, from {}, subject {}, timestamp {}",
                            mailDetails.messageID, mailDetails.from, mailDetails.subject, new Date(mailDetails.sendDateAndTime));
                    return ExtensionResponseFactory.create(Map.of("Mail Details", mailDetails));
                } else {
                    return ExtensionResponseFactory.create("Mail details not available", ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR,
                            List.of(RemediationActionFactory.createInformActionALLParticipants("Mail details not available ", List.of())),
                            null, null);
                }
            } else {
                throw new IllegalStateException();
            }
        } catch (MustAuthorizeException cause) {
            LOGGER.error(cause.getMessage());
            throw cause;
        } catch (Exception cause) {
            LOGGER.error("Error occurred while Mail Received Alert:{}", cause.getMessage());
            return ExtensionResponseFactory.create("Error occurred while Mail Received Alert", ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR,
                    List.of(RemediationActionFactory.createInformActionALLParticipants("Error occurred while Mail Received Alert", List.of())),
                    null, null);
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_90b24da6-d02f-4fcb-9632-ef8e6ae1550a",
            name = "Fetch Latest Mail",
            description = "Returns the latest email received, in the last two minutes",
            area = "Messaging",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Desc(name = "New Email", type = "Entity(Mail Details)", required = false)
    public ExtensionResponse fetchLatestMail() {
        try {
            LOGGER.info("fetchLatestMail: start");
            List<MailDetails> mailDetailsList = (List<MailDetails>) fetchInbox(1.0, 1.0).getResponseValue().get("Inbox Mails");
            MailDetails mailDetails = mailDetailsList.isEmpty() ? null : mailDetailsList.get(0);
            if (mailDetails != null && mailDetails.sendDateAndTime != null) {
                long change = System.currentTimeMillis() - mailDetails.sendDateAndTime;
                if (change <= 120_000) {
                    return ExtensionResponseFactory.create(Map.of("New Email", mailDetails));
                } else {
                    return ExtensionResponseFactory.create("Error occurred while Fetch Latest Mail", ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR,
                            List.of(RemediationActionFactory.createInformActionALLParticipants("Error occurred while Fetch Latest Mail", List.of())),
                            null, null);
                }
            } else {
                return ExtensionResponseFactory.create("Error occurred while Fetch Latest Mail", ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR,
                        List.of(RemediationActionFactory.createInformActionALLParticipants("Error occurred while Fetch Latest Mail", List.of())),
                        null, null);
            }
        } catch (MustAuthorizeException cause) {
            LOGGER.error(cause.getMessage());
            throw cause;
        } catch (Exception cause) {
            LOGGER.error("Error occurred while Fetch Latest Mail:{}", cause.getMessage());
            return ExtensionResponseFactory.create("Error occurred while Fetch Latest Mail", ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR,
                    List.of(RemediationActionFactory.createInformActionALLParticipants("Error occurred while Fetch Latest Mail", List.of())),
                    null, null);
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_5d7cf884-e31b-4806-ac68-b6f0c767585e",
            name = "List Categories",
            description = "Get a list of the supported Outlook categories",
            area = "Messaging",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Desc(name = "Category Names", type = "[ Text ]", required = false)
    public ExtensionResponse listCategories() {
        try {
            LOGGER.info("Fetching Categories");
            List<String> categoryNames;
            categoryNames = account.getCategoryNames();
            LOGGER.info("listCategories(): {}", categoryNames);
            return ExtensionResponseFactory.create(Map.of("Category Names", categoryNames));
        } catch (MustAuthorizeException cause) {
            LOGGER.error(cause.getMessage());
            throw cause;
        } catch (Exception cause) {
            LOGGER.error("Unable to fetch categories {}", cause.getMessage(), cause);
            return ExtensionResponseFactory.create("Unable to fetch categories", ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    List.of(RemediationActionFactory.createInformActionALLParticipants("Unable to fetch categories", List.of())),
                    null, Map.of());
        }

    }


    @CatalogRequest(
            id = "localDomainRequest_e35e5d82-b464-4fe4-875a-33f0dfe48265",
            name = "Add Category To Message",
            description = "This request will add category to the given mail ID.",
            area = "Messaging",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Boolean(name = "Category Added", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    public ExtensionResponse addCategoryToMessage(
            @Field.Text(name = "Message ID", required = true, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) String messageID,
            @Field.Text(name = "Category", required = true, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) String category,
            @Field.Boolean(name = "Create Category", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) Boolean createCategory) {
        try {
            List<ValidationOrchestrator.ValidationResult> validationResults =
                    validationOrchestrator.validate(Map.of(Validator.ValidationResource.MESSAGE_ID, messageID));

            if (validationResults.isEmpty()) {
                return messagingAreaImpl.addCategoryToMessage(messageID, category, createCategory);
            } else {
                String stateId = UUID.randomUUID().toString();
                internalStateManager.put(stateId, Constants.GSON.toJson(Map.of(OutlookResources.MESSAGE_ID, messageID,
                        SubCatalogConstants.VALIDATION_RESULTS, validationResults)));
                return responseGenerator.generateConfirmationResponse(
                        ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                        SubCatalogConstants.CONFIRM_REENTER_ADD_CATEGORY_TO_MESSAGE, StateMapperUtil.addCategoryToMessageMetaToMap(messageID, category, createCategory, stateId));
            }
        } catch (MustAuthorizeException cause) {
            LOGGER.error(cause.getMessage());
            throw cause;
        } catch (Exception cause) {
            LOGGER.error("Error occurred while Add Category To Message:{}", cause.getMessage());
            return ExtensionResponseFactory.create("Error occurred while Add Category To Message", ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR,
                    List.of(RemediationActionFactory.createInformActionALLParticipants("Error occurred while Add Category To Message", List.of())),
                    null, null);
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_6cdb6cf3-4ca6-46f0-a682-75be3bd37c98",
            name = "Remove Category From Message",
            description = "This request will remove the given category for the given Message Id",
            area = "Messaging",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Boolean(name = "Category Removed", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    public ExtensionResponse removeCategoryFromMessage(
            @Field.Text(name = "Message ID", required = true, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) String messageID,
            @Field.Text(name = "Category", required = true, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) String category) {
        try {
            List<ValidationOrchestrator.ValidationResult> validationResults =
                    validationOrchestrator.validate(Map.of(Validator.ValidationResource.MESSAGE_ID, messageID,
                            Validator.ValidationResource.CATEGORY, category));
            if (validationResults.isEmpty()) {
                return messagingAreaImpl.removeCategoryFromMessage(messageID, category);
            } else {
                String stateId = UUID.randomUUID().toString();
                internalStateManager.put(stateId, Constants.GSON.toJson(Map.of(OutlookResources.MESSAGE_ID, messageID,
                        SubCatalogConstants.VALIDATION_RESULTS, validationResults)));
                return responseGenerator.generateConfirmationResponse(
                        ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                        SubCatalogConstants.CONFIRM_REENTER_REMOVE_CATEGORY, Map.of(OutlookResources.STATE_ID, stateId, OutlookResources.MESSAGE_ID, messageID, OutlookResources.CATEGORY, category));
            }
        } catch (MustAuthorizeException cause) {
            LOGGER.error(cause.getMessage());
            throw cause;
        } catch (Exception cause) {
            LOGGER.error("Error occurred while Remove Category From Message:{}", cause.getMessage());
            return ExtensionResponseFactory.create("Error occurred while Remove Category From Message", ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR,
                    List.of(RemediationActionFactory.createInformActionALLParticipants("Error occurred while Remove Category From Message", List.of())),
                    null, null);
        }
    }

    private static String validateString(String input) {
        return input == null ? "" : input;
    }
}
