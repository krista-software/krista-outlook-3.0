package app.krista.extensions.essentials.collaboration.outlook3.catalog;

import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extension.executor.ExtensionResponse;
import app.krista.extension.executor.Invoker;
import app.krista.extension.impl.anno.Attribute;
import app.krista.extension.impl.anno.CatalogRequest;
import app.krista.extension.impl.anno.Domain;
import app.krista.extension.impl.anno.Field;
import app.krista.extensions.essentials.collaboration.outlook3.api.OutlookApiResource;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.entities.MailDetails;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.errorhandlers.ErrorHandlingStateManager;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.errorhandlers.ExtensionResponseGenerator;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.extresp.*;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.validators.ValidationOrchestrator;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.validators.Validator;
import app.krista.extensions.essentials.collaboration.outlook3.impl.MailHandler;
import app.krista.extensions.essentials.collaboration.outlook3.impl.MessagingAreaImpl;
import app.krista.extensions.essentials.collaboration.outlook3.impl.TestConnectionServiceImpl;
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
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static app.krista.extensions.essentials.collaboration.outlook3.catalog.errorhandlers.AuthorizationExceptionHandler.*;
import static app.krista.extensions.essentials.collaboration.outlook3.catalog.extresp.TelemetryHelper.safeTagMap;

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
    private final MessagingAreaImpl messagingAreaImpl;
    private final ExtensionResponseGenerator responseGenerator;
    private final ErrorHandlingStateManager internalStateManager;
    private final ValidationOrchestrator validationOrchestrator;
    final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final RequestContext requestContext;
    private final AuthorizationContext authorizationContext;
    private final TestConnectionServiceImpl testConnectionService;
    private final Invoker invoker;
    private final TelemetryHelper telemetryHelper;

    @Inject
    public MessagingArea(Account account, RequestContext requestContext, AuthorizationContext authorizationContext,
                         EventHandler eventHandler, MailHandler mailHandler,
                         MessagingAreaImpl messagingAreaImpl, ExtensionResponseGenerator responseGenerator,
                         ErrorHandlingStateManager internalStateManager, ValidationOrchestrator validationOrchestrator,
                         Invoker invoker, TestConnectionServiceImpl testConnectionService, TelemetryHelper telemetryHelper) {
        this.account = account;
        this.requestContext = requestContext;
        this.authorizationContext = authorizationContext;
        this.eventHandler = eventHandler;
        this.mailHandler = mailHandler;
        this.messagingAreaImpl = messagingAreaImpl;
        this.responseGenerator = responseGenerator;
        this.internalStateManager = internalStateManager;
        this.validationOrchestrator = validationOrchestrator;
        this.invoker = invoker;
        this.testConnectionService = testConnectionService;
        this.telemetryHelper = telemetryHelper;
    }

    private static String validateString(String input) {
        return input == null ? "" : input;
    }

    @CatalogRequest(
            id = "localDomainRequest_e39048cc-1795-4eee-8400-8fc3061c4e87",
            name = "Fetch All Labels",
            description = "Returns list of labels",
            area = "Messaging",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Desc(name = "Labels", type = "[ Text ]", required = false)
    public ExtensionResponse fetchAllLabels(
            @Field.Boolean(name = "Allow Retry", required = false,
                    attributes = {@Attribute(name = "visualWidth", value = "S")}) Boolean allowRetry) {
        long startTime = System.currentTimeMillis();
        try {
            telemetryHelper.incrementCount("outlook3.fetchAllLabels");

            List<String> labels = account.getFolderNames();
            ExtensionResponse response = ExtensionResponseFactory.create(Map.of("Labels", labels));

            telemetryHelper.recordSuccess("outlook3.fetchAllLabels", startTime,
                    TelemetryHelper.safeTagMap("label_count", String.valueOf(labels != null ? labels.size() : 0)));

            return response;
        } catch (MustAuthorizeException cause) {
            telemetryHelper.recordValidationError("outlook3.fetchAllLabels", startTime, cause.getMessage(), Map.of());
            return handleAuthorizationException(cause, requestContext.invokeAsUser());
        } catch (Exception cause) {
            LOGGER.error("Error occurred while fetching all labels: {}", cause.getMessage());

            telemetryHelper.recordError("outlook3.fetchAllLabels", startTime, cause, Map.of());
            return ExtensionResponseFactory.create(
                    "Error occurred while fetching all labels",
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR,
                    List.of(RemediationActionFactory.createInformActionALLParticipants(
                            "Error occurred while fetching mail by message ID", List.of())),
                    null, null);
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_ac177adc-e633-4ca1-baf8-7ce7efc5c0e5",
            name = "Fetch Mail By Message Id",
            description = "Accepts message Id as input and returns mail. In case of invalid input, this will return empty data.",
            area = "Messaging",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Desc(name = "Mail", type = "Entity(Mail Details)", required = false)
    public ExtensionResponse fetchMailByMessageId(
            @Field(name = "Message ID", type = "Text") String messageID,
            @Field.Boolean(name = "Allow Retry", required = false,
                    attributes = {@Attribute(name = "visualWidth", value = "S")}) Boolean allowRetry) {
        long startTime = System.currentTimeMillis();
        try {
            LOGGER.info("Executing fetchMailByMessageId with messageID: {}, allowRetry: {}", messageID, allowRetry);
            telemetryHelper.incrementCount("outlook3.fetchMailByMessageId");

            List<ValidationOrchestrator.ValidationResult> validationResults =
                    validationOrchestrator.validate(Map.of(Validator.ValidationResource.MESSAGE_ID, messageID));

            if (validationResults.isEmpty()) {
                MailDetails mailDetails = mailHandler.fromEmail(account.getEmail(messageID), null);
                if (mailDetails != null) {
                    LOGGER.info("fetchMailByMessageId: ID {}, from {}, subject {}, timestamp {}",
                            mailDetails.messageID, mailDetails.from, mailDetails.subject, new Date(mailDetails.sendDateAndTime));
                }

                telemetryHelper.recordSuccess("outlook3.fetchMailByMessageId", startTime,
                        TelemetryHelper.safeTagMap("message_id", messageID, "mail_found", String.valueOf(mailDetails != null),
                                "allow_retry", String.valueOf(allowRetry)));

                return ExtensionResponseFactory.create(Map.of("Mail", mailDetails));
            } else {
                if (Boolean.TRUE.equals(allowRetry)) {
                    telemetryHelper.recordRetryPrompted("outlook3.fetchMailByMessageId", startTime,
                            TelemetryHelper.safeTagMap("message_id", messageID, "validation_count", String.valueOf(validationResults.size()),
                                    "allow_retry", String.valueOf(allowRetry)));

                    String stateId = UUID.randomUUID().toString();
                    internalStateManager.put(stateId, Constants.GSON.toJson(Map.of(
                            OutlookResources.MESSAGE_ID, messageID,
                            SubCatalogConstants.VALIDATION_RESULTS, validationResults
                    ), Map.class));

                    return responseGenerator.generateConfirmationResponse(
                            ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                            SubCatalogConstants.CONFIRM_REENTER_FETCH_MAIL,
                            Map.of(OutlookResources.STATE_ID, stateId));
                } else {
                    telemetryHelper.recordValidationError("outlook3.fetchMailByMessageId", startTime,
                            "Validation failed without retry",
                            TelemetryHelper.safeTagMap("message_id", messageID, "validation_count", String.valueOf(validationResults.size()),
                                    "allow_retry", String.valueOf(allowRetry)));

                    return responseGenerator.generateFetchDenyResponse(
                            ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                            validationResults,
                            null,
                            Map.of());
                }
            }
        } catch (MustAuthorizeException cause) {
            telemetryHelper.recordValidationError("outlook3.fetchMailByMessageId", startTime, cause.getMessage(),
                    safeTagMap("message_id", messageID, "allow_retry", String.valueOf(allowRetry)));

            return handleAuthorizationException(cause, requestContext.invokeAsUser());
        } catch (Exception cause) {
            LOGGER.error("Error occurred while fetch mail by message id :{}", cause.getMessage());

            telemetryHelper.recordError("outlook3.fetchMailByMessageId", startTime, cause,
                    safeTagMap("message_id", messageID, "allow_retry", String.valueOf(allowRetry)));

            return ExtensionResponseFactory.create("Error occurred while fetch mail by message id",
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR,
                    List.of(RemediationActionFactory.createInformActionALLParticipants(
                            "Error occurred while fetch mail by message id", List.of())),
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
            @Field(name = "Folder Name", type = "Text") String folderName,
            @Field.Boolean(name = "Allow Retry", required = false,
                    attributes = {@Attribute(name = "visualWidth", value = "S")}) Boolean allowRetry) {
        long startTime = System.currentTimeMillis();
        try {
            LOGGER.info("Moving message with ID {} to folder: {}, allowRetry: {}", messageID, folderName, allowRetry);

            telemetryHelper.incrementCount("outlook3.moveMessage");

            List<ValidationOrchestrator.ValidationResult> validationResults = validationOrchestrator.validate(Map.of(
                    Validator.ValidationResource.MESSAGE_ID, messageID,
                    Validator.ValidationResource.FOLDER_NAME, folderName));

            if (validationResults.isEmpty()) {
                Email email = account.getEmail(messageID);
                Folder folder = account.getFolderByName(List.of(folderName.split(Constants.FORWARD_SLASH)));

                telemetryHelper.recordSuccess("outlook3.moveMessage", startTime,
                        TelemetryHelper.safeTagMap("message_id", messageID, "folder_name", folderName,
                                "allow_retry", String.valueOf(allowRetry)));

                return ExtensionResponseFactory.create(Map.of(OutlookResources.MESSAGE_ID, email.moveToFolder(folder)));
            } else {
                // Only trigger SubCatalog flow if allowRetry is true
                if (Boolean.TRUE.equals(allowRetry)) {
                    telemetryHelper.recordRetryPrompted("outlook3.moveMessage", startTime,
                            TelemetryHelper.safeTagMap("message_id", messageID, "folder_name", folderName,
                                    "allow_retry", String.valueOf(allowRetry),
                                    "validation_count", String.valueOf(validationResults.size())));

                    String stateId = UUID.randomUUID().toString();
                    internalStateManager.put(stateId, Constants.GSON.toJson(Map.of(
                            OutlookResources.MESSAGE_ID, messageID,
                            SubCatalogConstants.VALIDATION_RESULTS, validationResults)));

                    return responseGenerator.generateConfirmationResponse(
                            ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                            validationResults,
                            SubCatalogConstants.CONFIRM_REENTER_MOVE_MESSAGE,
                            Map.of(
                                    OutlookResources.STATE_ID, stateId,
                                    OutlookResources.MESSAGE_ID, messageID,
                                    OutlookResources.FOLDER_NAME, folderName));
                } else {
                    // Return validation error directly without retry option
                    telemetryHelper.recordValidationError("outlook3.moveMessage", startTime,
                            "Validation failed without retry",
                            TelemetryHelper.safeTagMap("message_id", messageID, "folder_name", folderName,
                                    "allow_retry", String.valueOf(allowRetry),
                                    "validation_count", String.valueOf(validationResults.size())));

                    return responseGenerator.generateFetchDenyResponse(
                            ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                            validationResults,
                            null,
                            Map.of());
                }
            }
        } catch (MustAuthorizeException cause) {
            telemetryHelper.recordValidationError("outlook3.moveMessage", startTime, cause.getMessage(),
                    safeTagMap("message_id", messageID, "folder_name", folderName,
                            "allow_retry", String.valueOf(allowRetry)));
            return handleAuthorizationException(cause, requestContext.invokeAsUser());

        } catch (Exception cause) {
            LOGGER.error("Error occurred while moving message to folder: {}", cause.getMessage());

            telemetryHelper.recordError("outlook3.moveMessage", startTime, cause,
                    safeTagMap("message_id", messageID, "folder_name", folderName,
                            "allow_retry", String.valueOf(allowRetry)));

            return ExtensionResponseFactory.create("Error occurred while moving message to folder",
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR,
                    List.of(RemediationActionFactory.createInformActionALLParticipants(
                            "Error occurred while moving message to folder", List.of())),
                    null, null);
        }
    }


    @CatalogRequest(
            id = "localDomainRequest_02494c43-a71f-47bb-935a-37736a4ecac0",
            name = "Reply To All With CC and BCC",
            description = "In this request, the user can respond to everyone on the thread. Other recipients will see a message user 'Reply All' to, whether they're in the 'To' or 'Cc' fields. 'To', Cc' and 'Bcc' fields are optional to update the existing users.",
            area = "Messaging",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Boolean(name = "Is Successful", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")})
    public ExtensionResponse replyToAllWithCCAndBCC(
            @Field(name = "Message Id", type = "Text") String messageId,
            @Field(name = "To", type = "Text", required = false) String to,
            @Field(name = "Cc", type = "Text", required = false) String cc,
            @Field(name = "Bcc", type = "Text", required = false) String bcc,
            @Field(name = "Reply To", type = "Text", required = false) String replyTo,
            @Field(name = "Message", type = "RichText", required = false) String message,
            @Field.File(name = "Attachments", multipleFileUpload = true, required = false) List<File> attachments,
            @Field.PickOne(name = "BodyType", values = {"Text", "HTML"}, required = false,
                    attributes = {@Attribute(name = "visualWidth", value = "S")}) String bodyType,
            @Field.Boolean(name = "Allow Retry", required = false,
                    attributes = {@Attribute(name = "visualWidth", value = "S")}) Boolean allowRetry) {

        long startTime = System.currentTimeMillis();
        try {
            LOGGER.info("replyToAll: messageId: {}; message: {}, allowRetry: {}", messageId, message, allowRetry);
            telemetryHelper.incrementCount("outlook3.replyToAllWithCCAndBCC");

            List<ValidationOrchestrator.ValidationResult> validationResults =
                    validationOrchestrator.validate(Map.of(
                            Validator.ValidationResource.MESSAGE_ID, messageId,
                            Validator.ValidationResource.TO, validateString(to),
                            Validator.ValidationResource.CC, validateString(cc),
                            Validator.ValidationResource.BCC, validateString(bcc),
                            Validator.ValidationResource.REPLY_TO, validateString(replyTo)));

            if (validationResults.isEmpty()) {
                telemetryHelper.recordSuccess("outlook3.replyToAllWithCCAndBCC", startTime,
                        TelemetryHelper.safeTagMap("message_id", messageId,
                                "has_attachments", String.valueOf(attachments != null && !attachments.isEmpty()),
                                "body_type", bodyType,
                                "allow_retry", String.valueOf(allowRetry)));

                return messagingAreaImpl.replyToAllWithCCAndBCC(
                        attachments, messageId, to, cc, bcc, replyTo, message, bodyType);
            } else {
                if (Boolean.TRUE.equals(allowRetry)) {
                    telemetryHelper.recordRetryPrompted("outlook3.replyToAllWithCCAndBCC", startTime,
                            TelemetryHelper.safeTagMap("message_id", messageId,
                                    "validation_count", String.valueOf(validationResults.size()),
                                    "allow_retry", String.valueOf(allowRetry)));

                    String stateId = UUID.randomUUID().toString();
                    internalStateManager.put(stateId, Constants.GSON.toJson(
                            Map.of(SubCatalogConstants.VALIDATION_RESULTS, validationResults)));

                    return responseGenerator.generateConfirmationResponse(
                            ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                            validationResults,
                            SubCatalogConstants.CONFIRM_REENTER_REPLY_TO_ALL_WITH_FIELDS,
                            StateMapperUtil.addReplyToALLFieldsMetaToMap(messageId, to, cc, bcc, replyTo, message, attachments, bodyType, stateId));
                } else {
                    telemetryHelper.recordValidationError("outlook3.replyToAllWithCCAndBCC", startTime,
                            "Validation failed without retry",
                            TelemetryHelper.safeTagMap("message_id", messageId,
                                    "validation_count", String.valueOf(validationResults.size()),
                                    "allow_retry", String.valueOf(allowRetry)));

                    return responseGenerator.generateFetchDenyResponse(
                            ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                            validationResults,
                            null,
                            Map.of());
                }
            }
        } catch (MustAuthorizeException cause) {
            telemetryHelper.recordValidationError("outlook3.replyToAllWithCCAndBCC", startTime, cause.getMessage(),
                    safeTagMap("message_id", messageId, "allow_retry", String.valueOf(allowRetry)));
            return handleAuthorizationException(cause, requestContext.invokeAsUser());
        } catch (Exception cause) {
            LOGGER.error("Error occurred while Reply To All With CC and BCC :{}", cause.getMessage());
            telemetryHelper.recordError("outlook3.replyToAllWithCCAndBCC", startTime, cause,
                    safeTagMap("message_id", messageId, "allow_retry", String.valueOf(allowRetry)));
            return ExtensionResponseFactory.create("Error occurred while Reply To All With CC and BCC",
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR,
                    List.of(RemediationActionFactory.createInformActionALLParticipants(
                            "Error occurred while Reply To All With CC and BCC", List.of())),
                    null, null);
        }
    }


    @CatalogRequest(
            id = "localDomainRequest_19b1a828-2f61-49e7-a75a-9956f7d12c5c",
            name = "Reply To All",
            description = "In this request, the user can respond to everyone on the thread. Other recipients will see a message user 'Reply All' to, whether they're in the 'To' or 'Cc' fields.",
            area = "Messaging",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Boolean(name = "Is Successful", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")})
    public ExtensionResponse replyToAll(
            @Field(name = "Message Id", type = "Text") String messageId,
            @Field(name = "Message", type = "RichText", required = false) String message,
            @Field.File(name = "Attachments", multipleFileUpload = true, required = false) List<File> attachments,
            @Field.PickOne(name = "BodyType", values = {"Text", "HTML"}, required = false,
                    attributes = {@Attribute(name = "visualWidth", value = "S")}) String bodyType,
            @Field.Boolean(name = "Allow Retry", required = false,
                    attributes = {@Attribute(name = "visualWidth", value = "S")}) Boolean allowRetry) {

        long startTime = System.currentTimeMillis();
        try {
            telemetryHelper.incrementCount("outlook3.replyToAll");

            LOGGER.info("replyToAll: messageId: {}; message: {}, allowRetry: {}", messageId, message, allowRetry);

            List<ValidationOrchestrator.ValidationResult> validationResults =
                    validationOrchestrator.validate(Map.of(Validator.ValidationResource.MESSAGE_ID, messageId));

            if (validationResults.isEmpty()) {
                telemetryHelper.recordSuccess("outlook3.replyToAll", startTime,
                        TelemetryHelper.safeTagMap("message_id", messageId, "allow_retry", String.valueOf(allowRetry)));
                return messagingAreaImpl.replyToAll(attachments, messageId, message, bodyType);
            } else {
                if (Boolean.TRUE.equals(allowRetry)) {
                    telemetryHelper.recordRetryPrompted("outlook3.replyToAll", startTime,
                            TelemetryHelper.safeTagMap("message_id", messageId, "validation_count", String.valueOf(validationResults.size()),
                                    "allow_retry", String.valueOf(allowRetry)));

                    String stateId = UUID.randomUUID().toString();
                    internalStateManager.put(stateId, Constants.GSON.toJson(Map.of(
                            OutlookResources.MESSAGE_ID, messageId,
                            SubCatalogConstants.VALIDATION_RESULTS, validationResults)));

                    return responseGenerator.generateConfirmationResponse(
                            ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                            SubCatalogConstants.CONFIRM_REENTER_REPLY_TO_ALL,
                            StateMapperUtil.addReplyToALLMetaToMap(messageId, message, attachments, bodyType, stateId));
                } else {
                    telemetryHelper.recordValidationError("outlook3.replyToAll", startTime,
                            "Validation failed without retry",
                            TelemetryHelper.safeTagMap("message_id", messageId, "validation_count", String.valueOf(validationResults.size()),
                                    "allow_retry", String.valueOf(allowRetry)));

                    return responseGenerator.generateFetchDenyResponse(
                            ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                            validationResults,
                            null,
                            Map.of());
                }
            }
        } catch (MustAuthorizeException cause) {
            telemetryHelper.recordValidationError("outlook3.replyToAll", startTime, cause.getMessage(),
                    safeTagMap("message_id", messageId, "allow_retry", String.valueOf(allowRetry)));
            return handleAuthorizationException(cause, requestContext.invokeAsUser());
        } catch (Exception cause) {
            LOGGER.error("Error occurred while Reply To All :{}", cause.getMessage());

            telemetryHelper.recordError("outlook3.replyToAll", startTime, cause,
                    safeTagMap("message_id", messageId, "allow_retry", String.valueOf(allowRetry)));
            return ExtensionResponseFactory.create("Error occurred while Reply To All",
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR,
                    List.of(RemediationActionFactory.createInformActionALLParticipants(
                            "Error occurred while Reply To All", List.of())),
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
            @Field(name = "Page Size", type = "Number", required = false) Double pageSize,
            @Field.Boolean(name = "Allow Retry", required = false,
                    attributes = {@Attribute(name = "visualWidth", value = "S")}) Boolean allowRetry) {
        long startTime = System.currentTimeMillis();
        try {
            telemetryHelper.incrementCount("outlook3.fetchSent");
            LOGGER.info("fetchSent: pageNumber: {}; pageSize: {}, allowRetry: {}", pageNumber, pageSize, allowRetry);

            Map<Validator.ValidationResource, String> validationResourceMap = ValidationResourceUtil.prepareValidateFetchInboxMap(pageNumber, pageSize);
            if (validationResourceMap.isEmpty()) {
                telemetryHelper.recordSuccess("outlook3.fetchSent", startTime,
                        safeTagMap("page_number", String.valueOf(pageNumber),
                                "page_size", String.valueOf(pageSize),
                                "allow_retry", String.valueOf(allowRetry)));
                return fetchSentResponse(pageNumber, pageSize);
            } else {
                List<ValidationOrchestrator.ValidationResult> validationResults = validationOrchestrator.validate(validationResourceMap);
                if (validationResults.isEmpty()) {
                    telemetryHelper.recordSuccess("outlook3.fetchSent", startTime,
                            safeTagMap("page_number", String.valueOf(pageNumber),
                                    "page_size", String.valueOf(pageSize),
                                    "validation_skipped", "true",
                                    "allow_retry", String.valueOf(allowRetry)));
                    return fetchSentResponse(pageNumber, pageSize);
                } else {
                    if (Boolean.TRUE.equals(allowRetry)) {
                        telemetryHelper.recordRetryPrompted("outlook3.fetchSent", startTime,
                                safeTagMap("page_number", String.valueOf(pageNumber),
                                        "page_size", String.valueOf(pageSize),
                                        "validation_count", String.valueOf(validationResults.size()),
                                        "allow_retry", String.valueOf(allowRetry)));
                        String stateId = UUID.randomUUID().toString();
                        internalStateManager.put(stateId, Constants.GSON.toJson(Map.of(SubCatalogConstants.VALIDATION_RESULTS, validationResults)));
                        return responseGenerator.generateConfirmationResponse(
                                ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                                SubCatalogConstants.CONFIRM_REENTER_FETCH_SENT, StateMapperUtil.addPageMetaDataToMap(pageNumber, pageSize, stateId));
                    } else {
                        telemetryHelper.recordValidationError("outlook3.fetchSent", startTime,
                                "Validation failed without retry",
                                safeTagMap("page_number", String.valueOf(pageNumber),
                                        "page_size", String.valueOf(pageSize),
                                        "validation_count", String.valueOf(validationResults.size()),
                                        "allow_retry", String.valueOf(allowRetry)));
                        return responseGenerator.generateFetchDenyResponse(
                                ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                                validationResults,
                                null,
                                Map.of());
                    }
                }
            }
        } catch (MustAuthorizeException cause) {
            telemetryHelper.recordValidationError("outlook3.fetchSent", startTime, cause.getMessage(),
                    safeTagMap("page_number", String.valueOf(pageNumber),
                            "page_size", String.valueOf(pageSize),
                            "allow_retry", String.valueOf(allowRetry)));
            return handleAuthorizationException(cause, requestContext.invokeAsUser());
        } catch (Exception cause) {
            LOGGER.error("Error occurred while fetch sent:{}", cause.getMessage());
            telemetryHelper.recordError("outlook3.fetchSent", startTime, cause,
                    safeTagMap("page_number", String.valueOf(pageNumber),
                            "page_size", String.valueOf(pageSize),
                            "allow_retry", String.valueOf(allowRetry)));
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
            @Field.PickOne(name = "BodyType", values = {"Text", "HTML"}, required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}) String bodyType,
            @Field.Boolean(name = "Allow Retry", required = false,
                    attributes = {@Attribute(name = "visualWidth", value = "S")}) Boolean allowRetry) {
        long startTime = System.currentTimeMillis();
        try {
            LOGGER.info("forwardMail: messageId: {}, to: {}, allowRetry: {}", messageId, to, allowRetry);
            telemetryHelper.incrementCount("outlook3.forwardMail");

            List<ValidationOrchestrator.ValidationResult> validationResults =
                    validationOrchestrator.validate(Map.of(Validator.ValidationResource.MESSAGE_ID, messageId, Validator.ValidationResource.TO, to));

            if (validationResults.isEmpty()) {
                telemetryHelper.recordSuccess("outlook3.forwardMail", startTime,
                        safeTagMap("message_id", messageId, "to", to, "allow_retry", String.valueOf(allowRetry)));
                return messagingAreaImpl.forwardMail(messageId, to, message, bodyType);
            } else {
                if (Boolean.TRUE.equals(allowRetry)) {
                    telemetryHelper.recordRetryPrompted("outlook3.forwardMail", startTime,
                            safeTagMap("message_id", messageId, "to", to,
                                    "validation_count", String.valueOf(validationResults.size()),
                                    "allow_retry", String.valueOf(allowRetry)));
                    String stateId = UUID.randomUUID().toString();
                    internalStateManager.put(stateId, Constants.GSON.toJson(Map.of(SubCatalogConstants.VALIDATION_RESULTS, validationResults)));
                    return responseGenerator.generateConfirmationResponse(
                            ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults, SubCatalogConstants.CONFIRM_REENTER_FORWARD_MAIL,
                            StateMapperUtil.addForwardMailMetaToMap(messageId, message, to, bodyType, stateId));
                } else {
                    telemetryHelper.recordValidationError("outlook3.forwardMail", startTime,
                            "Validation failed without retry",
                            safeTagMap("message_id", messageId, "to", to,
                                    "validation_count", String.valueOf(validationResults.size()),
                                    "allow_retry", String.valueOf(allowRetry)));
                    return responseGenerator.generateFetchDenyResponse(
                            ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                            validationResults,
                            null,
                            Map.of());
                }
            }
        } catch (MustAuthorizeException cause) {
            telemetryHelper.recordValidationError("outlook3.forwardMail", startTime, cause.getMessage(),
                    safeTagMap("message_id", messageId, "to", to, "allow_retry", String.valueOf(allowRetry)));
            return handleAuthorizationException(cause, requestContext.invokeAsUser());
        } catch (Exception cause) {
            LOGGER.error("Error occurred while forward mail :{}", cause.getMessage());
            telemetryHelper.recordError("outlook3.forwardMail", startTime, cause,
                    safeTagMap("message_id", messageId, "to", to, "allow_retry", String.valueOf(allowRetry)));
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
            @Field(name = "Query", type = "Text") String query,
            @Field.Boolean(name = "Allow Retry", required = false,
                    attributes = {@Attribute(name = "visualWidth", value = "S")}) Boolean allowRetry) {
        long startTime = System.currentTimeMillis();
        try {
            telemetryHelper.incrementCount("outlook3.fetchMailDetailsByQuery");
            LOGGER.info("fetchMailDetailsByQuery: {}, allowRetry: {}", query, allowRetry);
            List<Email> emails = account.searchEmails(query);
            List<MailDetails> response = emails.stream().map(email -> mailHandler.fromEmail(email, null)).collect(Collectors.toList());
            telemetryHelper.recordSuccess("outlook3.fetchMailDetailsByQuery", startTime,
                    safeTagMap("query", query, "result_count", String.valueOf(response.size()), "allow_retry", String.valueOf(allowRetry)));
            return ExtensionResponseFactory.create(Map.of("Mails", response));
        } catch (MustAuthorizeException cause) {
            telemetryHelper.recordValidationError("outlook3.fetchMailDetailsByQuery", startTime, cause.getMessage(),
                    safeTagMap("query", query, "allow_retry", String.valueOf(allowRetry)));
            return handleAuthorizationException(cause, requestContext.invokeAsUser());
        } catch (Exception cause) {
            telemetryHelper.recordError("outlook3.fetchMailDetailsByQuery", startTime, cause,
                    safeTagMap("query", query, "allow_retry", String.valueOf(allowRetry)));
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
            @Field.PickOne(name = "BodyType", values = {"Text", "HTML"}, required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}) String bodyType,
            @Field.Boolean(name = "Allow Retry", required = false,
                    attributes = {@Attribute(name = "visualWidth", value = "S")}) Boolean allowRetry) {
        long startTime = System.currentTimeMillis();
        try {
            LOGGER.info("sendMail: to: {}, subject: {}, allowRetry: {}", to, subject, allowRetry);
            telemetryHelper.incrementCount("outlook3.sendMail");

            List<ValidationOrchestrator.ValidationResult> validationResults = validationOrchestrator.validate(Map.of(
                    Validator.ValidationResource.TO, to,
                    Validator.ValidationResource.CC, validateString(cc),
                    Validator.ValidationResource.BCC, validateString(bcc),
                    Validator.ValidationResource.REPLY_TO, validateString(replyTo)));

            if (validationResults.isEmpty()) {
                ExtensionResponse response = messagingAreaImpl.sendMail(subject, message, attachments, to, cc, bcc, replyTo, bodyType);
                telemetryHelper.recordSuccess("outlook3.sendMail", startTime, safeTagMap("to", to, "allow_retry", String.valueOf(allowRetry)));
                return response;
            } else {
                if (Boolean.TRUE.equals(allowRetry)) {
                    telemetryHelper.recordRetryPrompted("outlook3.sendMail", startTime,
                            safeTagMap("to", to, "validation_count", String.valueOf(validationResults.size()),
                                    "allow_retry", String.valueOf(allowRetry)));
                    String stateId = UUID.randomUUID().toString();
                    internalStateManager.put(stateId, Constants.GSON.toJson(Map.of(SubCatalogConstants.VALIDATION_RESULTS, validationResults)));
                    return responseGenerator.generateConfirmationResponse(
                            ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                            SubCatalogConstants.CONFIRM_REENTER_SEND_MAIL,
                            StateMapperUtil.addSendMailMetaToMap(subject, to, cc, bcc, replyTo, message, attachments, bodyType, stateId));
                } else {
                    telemetryHelper.recordValidationError("outlook3.sendMail", startTime,
                            "Validation failed without retry",
                            safeTagMap("to", to, "validation_count", String.valueOf(validationResults.size()),
                                    "allow_retry", String.valueOf(allowRetry)));
                    return responseGenerator.generateFetchDenyResponse(
                            ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                            validationResults,
                            null,
                            Map.of());
                }
            }
        } catch (MustAuthorizeException cause) {
            telemetryHelper.recordValidationError("outlook3.sendMail", startTime, cause.getMessage(),
                    safeTagMap("to", to, "allow_retry", String.valueOf(allowRetry)));
            return handleAuthorizationException(cause, requestContext.invokeAsUser());
        } catch (Exception cause) {
            LOGGER.error("Error occurred while send mail :{}", cause.getMessage());
            telemetryHelper.recordError("outlook3.sendMail", startTime, cause,
                    safeTagMap("to", to, "allow_retry", String.valueOf(allowRetry)));
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
            @Field.Desc(name = "Remove Entity Field From Table", type = "[ Text ]", required = false) List<String> removeEntityFieldFromTable,
            @Field.Boolean(name = "Allow Retry", required = false,
                    attributes = {@Attribute(name = "visualWidth", value = "S")}) Boolean allowRetry) {
        long startTime = System.currentTimeMillis();
        try {
            LOGGER.info("sendMailWithTable: to: {}, subject: {}, allowRetry: {}", to, subject, allowRetry);
            telemetryHelper.incrementCount("outlook3.sendMailWithTable");

            List<ValidationOrchestrator.ValidationResult> validationResults = validationOrchestrator.validate(Map.of(
                    Validator.ValidationResource.TO, to,
                    Validator.ValidationResource.CC, validateString(cc),
                    Validator.ValidationResource.BCC, validateString(bcc),
                    Validator.ValidationResource.REPLY_TO, validateString(replyTo)));

            if (validationResults.isEmpty()) {
                ExtensionResponse response = messagingAreaImpl.sendMailWithTable(subject, message, attachments, to, cc, bcc, replyTo, entityList, removeEntityFieldFromTable);
                telemetryHelper.recordSuccess("outlook3.sendMailWithTable", startTime,
                        safeTagMap("to", to, "allow_retry", String.valueOf(allowRetry)));
                return response;
            } else {
                if (Boolean.TRUE.equals(allowRetry)) {
                    telemetryHelper.recordRetryPrompted("outlook3.sendMailWithTable", startTime, safeTagMap(
                            "to", to, "validation_count", String.valueOf(validationResults.size()),
                            "allow_retry", String.valueOf(allowRetry)));
                    String stateId = UUID.randomUUID().toString();
                    internalStateManager.put(stateId, Constants.GSON.toJson(StateMapperUtil.addSendMailWithTableAttachmentToMap(attachments, entityList, removeEntityFieldFromTable, validationResults)));
                    internalStateManager.putMetaInfo(stateId, Map.of(OutlookResources.ENTITY_LIST, entityList));
                    return responseGenerator.generateConfirmationResponse(
                            ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                            SubCatalogConstants.CONFIRM_REENTER_SEND_MAIL_WITH_TABLE,
                            StateMapperUtil.addSendMailWithTableMetaToMap(subject, to, cc, bcc, replyTo, message, stateId));
                } else {
                    telemetryHelper.recordValidationError("outlook3.sendMailWithTable", startTime,
                            "Validation failed without retry",
                            safeTagMap("to", to, "validation_count", String.valueOf(validationResults.size()),
                                    "allow_retry", String.valueOf(allowRetry)));
                    return responseGenerator.generateFetchDenyResponse(
                            ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                            validationResults,
                            null,
                            Map.of());
                }
            }
        } catch (MustAuthorizeException cause) {
            telemetryHelper.recordValidationError("outlook3.sendMailWithTable", startTime, cause.getMessage(),
                    safeTagMap("to", to, "allow_retry", String.valueOf(allowRetry)));
            return handleAuthorizationException(cause, requestContext.invokeAsUser());
        } catch (Exception cause) {
            LOGGER.error("Error occurred while Send Mail With Table :{}", cause.getMessage());
            telemetryHelper.recordError("outlook3.sendMailWithTable", startTime, cause,
                    safeTagMap("to", to, "allow_retry", String.valueOf(allowRetry)));
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
            @Field(name = "Page Size", type = "Number", required = false) Double pageSize,
            @Field.Boolean(name = "Allow Retry", required = false,
                    attributes = {@Attribute(name = "visualWidth", value = "S")}) Boolean allowRetry) {
        long startTime = System.currentTimeMillis();
        try {
            telemetryHelper.incrementCount("outlook3.fetchInbox");
            LOGGER.info("fetchInbox: pageNumber: {}; pageSize: {}, allowRetry: {}", pageNumber, pageSize, allowRetry);

            Map<Validator.ValidationResource, String> validationResourceMap = ValidationResourceUtil.prepareValidateFetchInboxMap(pageNumber, pageSize);
            if (validationResourceMap.isEmpty()) {
                ExtensionResponse response = fetchInboxResponse(pageNumber, pageSize);
                telemetryHelper.recordSuccess("outlook3.fetchInbox", startTime,
                        safeTagMap("page_number", String.valueOf(pageNumber), "page_size", String.valueOf(pageSize),
                                "allow_retry", String.valueOf(allowRetry)));
                return response;
            }

            List<ValidationOrchestrator.ValidationResult> validationResults = validationOrchestrator.validate(validationResourceMap);
            if (validationResults.isEmpty()) {
                ExtensionResponse response = fetchInboxResponse(pageNumber, pageSize);
                telemetryHelper.recordSuccess("outlook3.fetchInbox", startTime,
                        safeTagMap("page_number", String.valueOf(pageNumber), "page_size", String.valueOf(pageSize),
                                "allow_retry", String.valueOf(allowRetry)));
                return response;
            }

            if (Boolean.TRUE.equals(allowRetry)) {
                telemetryHelper.recordRetryPrompted("outlook3.fetchInbox", startTime,
                        safeTagMap("page_number", String.valueOf(pageNumber), "page_size", String.valueOf(pageSize),
                                "validation_count", String.valueOf(validationResults.size()),
                                "allow_retry", String.valueOf(allowRetry)));
                String stateId = UUID.randomUUID().toString();
                Map<String, Object> stateMap = new HashMap<>();
                stateMap.put(SubCatalogConstants.VALIDATION_RESULTS, validationResults);
                if (pageNumber != null) stateMap.put(OutlookResources.PAGE_NUMBER, pageNumber);
                if (pageSize != null) stateMap.put(OutlookResources.PAGE_SIZE, pageSize);
                internalStateManager.put(stateId, Constants.GSON.toJson(stateMap));

                return responseGenerator.generateConfirmationResponse(
                        ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                        SubCatalogConstants.CONFIRM_REENTER_FETCH_INBOX, StateMapperUtil.addPageMetaDataToMap(pageNumber, pageSize, stateId));
            } else {
                telemetryHelper.recordValidationError("outlook3.fetchInbox", startTime,
                        "Validation failed without retry",
                        safeTagMap("page_number", String.valueOf(pageNumber), "page_size", String.valueOf(pageSize),
                                "validation_count", String.valueOf(validationResults.size()),
                                "allow_retry", String.valueOf(allowRetry)));
                return responseGenerator.generateFetchDenyResponse(
                        ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                        validationResults,
                        null,
                        Map.of());
            }

        } catch (MustAuthorizeException cause) {
            telemetryHelper.recordValidationError("outlook3.fetchInbox", startTime, cause.getMessage(),
                    safeTagMap("page_number", String.valueOf(pageNumber), "page_size", String.valueOf(pageSize),
                            "allow_retry", String.valueOf(allowRetry)));
            return handleAuthorizationException(cause, requestContext.invokeAsUser());
        } catch (Exception cause) {
            LOGGER.error("Error occurred while fetch inbox :{}", cause.getMessage());
            telemetryHelper.recordError("outlook3.fetchInbox", startTime, cause,
                    safeTagMap("page_number", String.valueOf(pageNumber), "page_size", String.valueOf(pageSize),
                            "allow_retry", String.valueOf(allowRetry)));
            return ExtensionResponseFactory.create("Error occurred while fetch inbox ", ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR,
                    List.of(RemediationActionFactory.createInformActionALLParticipants("Error occurred while fetch inbox ", List.of())),
                    null, null);
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_d9a9cf34-899a-4c66-b3ef-9c7457446035",
            name = "Fetch Inbox With Preferences",
            description = "This request is used to fetch Inbox emails with the selected preferences.",
            area = "Messaging",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Desc(name = "Mails", type = "[ Entity(Mail Details) ]", required = false)
    public ExtensionResponse fetchInboxWithPreferences(
            @Field(name = "Page Number", type = "Number", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}) Double pageNumber,
            @Field(name = "Page Size", type = "Number", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}) Double pageSize,
            @Field.Desc(name = "Preference", type = "{ Mail Body: PickOne(Text|Html) }", required = false) Map<String, Object> preference,
            @Field.Boolean(name = "Allow Retry", required = false,
                    attributes = {@Attribute(name = "visualWidth", value = "S")}) Boolean allowRetry) {

        long startTime = System.currentTimeMillis();
        telemetryHelper.incrementCount("outlook3.fetchInboxWithPreferences");

        if (preference == null || preference.isEmpty()) {
            preference = new HashMap<>();
            preference.put("Mail Body", "Html");
        }

        LOGGER.info("fetchInboxWithPreference: pageNumber: {}; pageSize: {}; preference: {}, allowRetry: {}", pageNumber, pageSize, preference, allowRetry);

        try {
            Map<Validator.ValidationResource, String> validationResourceMap = ValidationResourceUtil.prepareValidateFetchInboxMap(pageNumber, pageSize);

            if (validationResourceMap.isEmpty()) {
                ExtensionResponse response = fetchInboxResponseWithPref(pageNumber, pageSize, preference);
                telemetryHelper.recordSuccess("outlook3.fetchInboxWithPreferences", startTime, safeTagMap(
                        "page_number", String.valueOf(pageNumber),
                        "page_size", String.valueOf(pageSize),
                        "preference", preference.toString(),
                        "allow_retry", String.valueOf(allowRetry)
                ));
                return response;
            }

            List<ValidationOrchestrator.ValidationResult> validationResults = validationOrchestrator.validate(validationResourceMap);

            if (validationResults.isEmpty()) {
                ExtensionResponse response = fetchInboxResponseWithPref(pageNumber, pageSize, preference);
                telemetryHelper.recordSuccess("outlook3.fetchInboxWithPreferences", startTime, safeTagMap(
                        "page_number", String.valueOf(pageNumber),
                        "page_size", String.valueOf(pageSize),
                        "preference", preference.toString(),
                        "allow_retry", String.valueOf(allowRetry)
                ));
                return response;
            }

            if (Boolean.TRUE.equals(allowRetry)) {
                telemetryHelper.recordRetryPrompted("outlook3.fetchInboxWithPreferences", startTime, safeTagMap(
                        "page_number", String.valueOf(pageNumber),
                        "page_size", String.valueOf(pageSize),
                        "preference", preference.toString(),
                        "validation_count", String.valueOf(validationResults.size()),
                        "allow_retry", String.valueOf(allowRetry)
                ));

                String stateId = UUID.randomUUID().toString();
                internalStateManager.put(stateId, Constants.GSON.toJson(Map.of(SubCatalogConstants.VALIDATION_RESULTS, validationResults)));

                return responseGenerator.generateConfirmationResponse(
                        ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                        SubCatalogConstants.CONFIRM_REENTER_FETCH_INBOX_WITH_PREFERENCE,
                        StateMapperUtil.addFetchInboxWithPrefMetaDataToMap(pageNumber, pageSize, preference, stateId)
                );
            } else {
                telemetryHelper.recordValidationError("outlook3.fetchInboxWithPreferences", startTime,
                        "Validation failed without retry",
                        safeTagMap(
                                "page_number", String.valueOf(pageNumber),
                                "page_size", String.valueOf(pageSize),
                                "preference", preference.toString(),
                                "validation_count", String.valueOf(validationResults.size()),
                                "allow_retry", String.valueOf(allowRetry)
                        ));
                return responseGenerator.generateFetchDenyResponse(
                        ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                        validationResults,
                        null,
                        Map.of());
            }
        } catch (MustAuthorizeException cause) {
            telemetryHelper.recordValidationError("outlook3.fetchInboxWithPreferences", startTime, cause.getMessage(), safeTagMap(
                    "page_number", String.valueOf(pageNumber),
                    "page_size", String.valueOf(pageSize),
                    "preference", preference.toString(),
                    "allow_retry", String.valueOf(allowRetry)
            ));
            return handleAuthorizationException(cause, requestContext.invokeAsUser());
        } catch (Exception cause) {
            LOGGER.error("Error occurred while fetch inbox :{}", cause.getMessage());
            telemetryHelper.recordError("outlook3.fetchInboxWithPreferences", startTime, cause, safeTagMap(
                    "page_number", String.valueOf(pageNumber),
                    "page_size", String.valueOf(pageSize),
                    "preference", preference.toString(),
                    "allow_retry", String.valueOf(allowRetry)
            ));
            return ExtensionResponseFactory.create("Error occurred while fetch inbox ", ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR,
                    List.of(RemediationActionFactory.createInformActionALLParticipants("Error occurred while fetch inbox with given preferences ", List.of())),
                    null, null);
        }
    }

    private ExtensionResponse fetchInboxResponse(Double pageNumber, Double pageSize) {
        List<Email> emails = account.getInboxFolder(null, null).getEmails(pageNumber, pageSize);
        return ExtensionResponseFactory.create(Map.of("Inbox Mails", emails.stream().map(email -> mailHandler.fromEmail(email, null)).collect(Collectors.toList())));
    }

    private ExtensionResponse fetchInboxResponseWithPref(Double pageNumber, Double pageSize, Map<String, Object> pref) {
        List<Email> emails = account.getInboxFolder(null, null).getEmails(pageNumber, pageSize, pref);
        return ExtensionResponseFactory.create(Map.of("Mails", emails.stream().map(email -> mailHandler.fromEmail(email, null)).collect(Collectors.toList())));

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
            @Field.Desc(name = "Label", type = "PickOne(Read|Unread)") String label,
            @Field.Boolean(name = "Allow Retry", required = false,
                    attributes = {@Attribute(name = "visualWidth", value = "S")}) Boolean allowRetry) {

        long startTime = System.currentTimeMillis();
        telemetryHelper.incrementCount("outlook3.markMessage");

        try {
            List<ValidationOrchestrator.ValidationResult> validationResults =
                    validationOrchestrator.validate(Map.of(Validator.ValidationResource.MESSAGE_ID, messageID));

            if (validationResults.isEmpty()) {
                ExtensionResponse response = messagingAreaImpl.markMessage(messageID, label);
                telemetryHelper.recordSuccess("outlook3.markMessage", startTime, Map.of(
                        "message_id", messageID,
                        "label", label,
                        "allow_retry", String.valueOf(allowRetry)
                ));
                return response;
            }

            // Only trigger SubCatalog flow if allowRetry is true
            if (Boolean.TRUE.equals(allowRetry)) {
                telemetryHelper.recordRetryPrompted("outlook3.markMessage", startTime, safeTagMap(
                        "message_id", messageID,
                        "label", label,
                        "allow_retry", String.valueOf(allowRetry),
                        "validation_count", String.valueOf(validationResults.size())
                ));

                String stateId = UUID.randomUUID().toString();
                internalStateManager.put(stateId, Constants.GSON.toJson(Map.of(
                        OutlookResources.MESSAGE_ID, messageID,
                        SubCatalogConstants.VALIDATION_RESULTS, validationResults)));

                return responseGenerator.generateConfirmationResponse(
                        ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                        SubCatalogConstants.CONFIRM_REENTER_MARK_MESSAGE, Map.of(
                                OutlookResources.STATE_ID, stateId,
                                OutlookResources.LABEL, label,
                                OutlookResources.MESSAGE_ID, messageID));
            } else {
                // Return validation error directly without retry option
                telemetryHelper.recordValidationError("outlook3.markMessage", startTime,
                        "Validation failed without retry",
                        safeTagMap("message_id", messageID, "label", label,
                                "allow_retry", String.valueOf(allowRetry),
                                "validation_count", String.valueOf(validationResults.size())));

                return responseGenerator.generateFetchDenyResponse(
                        ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                        validationResults,
                        null,
                        Map.of());
            }
        } catch (MustAuthorizeException cause) {
            telemetryHelper.recordValidationError("outlook3.markMessage", startTime, cause.getMessage(), safeTagMap(
                    "message_id", messageID,
                    "label", label,
                    "allow_retry", String.valueOf(allowRetry)
            ));
            return handleAuthorizationException(cause, requestContext.invokeAsUser());
        } catch (Exception cause) {
            LOGGER.error("Error occurred while mark message :{}", cause.getMessage());
            telemetryHelper.recordError("outlook3.markMessage", startTime, cause, safeTagMap(
                    "message_id", messageID,
                    "label", label,
                    "allow_retry", String.valueOf(allowRetry)
            ));
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
    @Field(name = "Message", type = "Text", required = false)
    public ExtensionResponse replyToMailWithCCAndBCC(
            @Field(name = "Message Id", type = "Text") String messageId,
            @Field(name = "Message", type = "RichText", required = false) String message,
            @Field.File(name = "Attachments", multipleFileUpload = true, required = false) List<File> attachments,
            @Field(name = "To", type = "Text", required = false) String to,
            @Field(name = "Cc", type = "Text", required = false) String cc,
            @Field(name = "Bcc", type = "Text", required = false) String bcc,
            @Field(name = "Reply To", type = "Text", required = false) String replyTo,
            @Field.PickOne(name = "BodyType", values = {"Text", "HTML"}, required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}) String bodyType,
            @Field.Boolean(name = "Allow Retry", required = false,
                    attributes = {@Attribute(name = "visualWidth", value = "S")}) Boolean allowRetry) {

        long startTime = System.currentTimeMillis();
        telemetryHelper.incrementCount("outlook3.replyToMailWithCCAndBCC");

        try {
            LOGGER.info("replyToMailWithCCAndBCC: messageId: {}; bodyType: {}, allowRetry: {}", messageId, bodyType, allowRetry);

            List<ValidationOrchestrator.ValidationResult> validationResults =
                    validationOrchestrator.validate(Map.of(
                            Validator.ValidationResource.MESSAGE_ID, messageId,
                            Validator.ValidationResource.TO, validateString(to),
                            Validator.ValidationResource.CC, validateString(cc),
                            Validator.ValidationResource.BCC, validateString(bcc),
                            Validator.ValidationResource.REPLY_TO, validateString(replyTo)));

            if (validationResults.isEmpty()) {
                ExtensionResponse response = messagingAreaImpl.replyToMailWithCCAndBCC(attachments, messageId, to, cc, bcc, replyTo, message, bodyType);
                telemetryHelper.recordSuccess("outlook3.replyToMailWithCCAndBCC", startTime, safeTagMap(
                        "message_id", messageId,
                        "has_attachments", String.valueOf(attachments != null && !attachments.isEmpty()),
                        "to", to != null ? "true" : "false",
                        "cc", cc != null ? "true" : "false",
                        "bcc", bcc != null ? "true" : "false",
                        "allow_retry", String.valueOf(allowRetry)
                ));
                return response;
            }

            if (Boolean.TRUE.equals(allowRetry)) {
                telemetryHelper.recordRetryPrompted("outlook3.replyToMailWithCCAndBCC", startTime, safeTagMap(
                        "message_id", messageId,
                        "validation_count", String.valueOf(validationResults.size()),
                        "allow_retry", String.valueOf(allowRetry)
                ));

                String stateId = UUID.randomUUID().toString();
                internalStateManager.put(stateId, Constants.GSON.toJson(Map.of(SubCatalogConstants.VALIDATION_RESULTS, validationResults)));

                return responseGenerator.generateConfirmationResponse(
                        ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                        SubCatalogConstants.CONFIRM_REENTER_REPLY_TO_MAIL_WITH_FIELDS,
                        StateMapperUtil.addReplyToALLFieldsMetaToMap(messageId, to, cc, bcc, replyTo, message, attachments, bodyType, stateId));
            } else {
                telemetryHelper.recordValidationError("outlook3.replyToMailWithCCAndBCC", startTime,
                        "Validation failed without retry",
                        safeTagMap("message_id", messageId,
                                "validation_count", String.valueOf(validationResults.size()),
                                "allow_retry", String.valueOf(allowRetry)));

                return responseGenerator.generateFetchDenyResponse(
                        ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                        validationResults,
                        null,
                        Map.of());
            }

        } catch (MustAuthorizeException cause) {
            telemetryHelper.recordValidationError("outlook3.replyToMailWithCCAndBCC", startTime, cause.getMessage(),
                    safeTagMap("message_id", messageId, "allow_retry", String.valueOf(allowRetry)));
            return handleAuthorizationException(cause, requestContext.invokeAsUser());
        } catch (Exception cause) {
            LOGGER.error("Error occurred while Reply To Mail With CC and BCC :{}", cause.getMessage());
            telemetryHelper.recordError("outlook3.replyToMailWithCCAndBCC", startTime, cause,
                    safeTagMap("message_id", messageId, "allow_retry", String.valueOf(allowRetry)));
            cause.printStackTrace();
            return ExtensionResponseFactory.create("Error occurred while Reply To Mail With CC and BCC ", ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR,
                    List.of(RemediationActionFactory.createInformActionALLParticipants("Error occurred while Reply To Mail With CC and BCC ", List.of())), null, null);
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
            @Field.PickOne(name = "BodyType", values = {"Text", "HTML"}, required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}) String bodyType,
            @Field.Boolean(name = "Allow Retry", required = false,
                    attributes = {@Attribute(name = "visualWidth", value = "S")}) Boolean allowRetry) {

        long startTime = System.currentTimeMillis();
        telemetryHelper.incrementCount("outlook3.replyToMail");

        try {
            LOGGER.info("replyToMail: messageID: {}; message: {}, allowRetry: {}", messageID, message, allowRetry);

            List<ValidationOrchestrator.ValidationResult> validationResults =
                    validationOrchestrator.validate(Map.of(Validator.ValidationResource.MESSAGE_ID, messageID));

            if (validationResults.isEmpty()) {
                ExtensionResponse response = messagingAreaImpl.replyToMail(attachments, messageID, message, bodyType);
                telemetryHelper.recordSuccess("outlook3.replyToMail", startTime, safeTagMap(
                        "message_id", messageID,
                        "has_attachments", String.valueOf(attachments != null && !attachments.isEmpty()),
                        "allow_retry", String.valueOf(allowRetry)
                ));
                return response;
            }

            if (Boolean.TRUE.equals(allowRetry)) {
                telemetryHelper.recordRetryPrompted("outlook3.replyToMail", startTime, safeTagMap(
                        "message_id", messageID,
                        "validation_count", String.valueOf(validationResults.size()),
                        "allow_retry", String.valueOf(allowRetry)
                ));

                String stateId = UUID.randomUUID().toString();
                internalStateManager.put(stateId, Constants.GSON.toJson(Map.of(
                        OutlookResources.MESSAGE_ID, messageID,
                        SubCatalogConstants.VALIDATION_RESULTS, validationResults)));

                return responseGenerator.generateConfirmationResponse(
                        ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                        SubCatalogConstants.CONFIRM_REENTER_REPLY_TO_MAIL,
                        StateMapperUtil.addReplyToALLMetaToMap(messageID, message, attachments, bodyType, stateId));
            } else {
                telemetryHelper.recordValidationError("outlook3.replyToMail", startTime,
                        "Validation failed without retry",
                        safeTagMap("message_id", messageID,
                                "validation_count", String.valueOf(validationResults.size()),
                                "allow_retry", String.valueOf(allowRetry)));

                return responseGenerator.generateFetchDenyResponse(
                        ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                        validationResults,
                        null,
                        Map.of());
            }

        } catch (MustAuthorizeException cause) {
            telemetryHelper.recordValidationError("outlook3.replyToMail", startTime, cause.getMessage(),
                    safeTagMap("message_id", messageID, "allow_retry", String.valueOf(allowRetry)));
            return handleAuthorizationException(cause, requestContext.invokeAsUser());
        } catch (Exception cause) {
            LOGGER.error("Error occurred while Reply To Mail :{}", cause.getMessage());
            telemetryHelper.recordError("outlook3.replyToMail", startTime, cause,
                    safeTagMap("message_id", messageID, "allow_retry", String.valueOf(allowRetry)));
            return ExtensionResponseFactory.create("Error occurred while Reply To Mail", ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR,
                    List.of(RemediationActionFactory.createInformActionALLParticipants("Error occurred while Reply To Mail", List.of())), null, null);
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
        long startTime = System.currentTimeMillis();
        telemetryHelper.incrementCount("outlook3.fetchInboxAsync");

        try {
            final String taskId = UUID.randomUUID().toString();
            boolean useSetupMail = !requestContext.invokeAsUser();
            String accountID = authorizationContext.getAuthorizedAccount().getAccountId();
            Map<Class<?>, Object> threadLocals = ThreadLocalProxy.getAll();

            executorService.submit(() -> {
                long asyncStartTime = System.currentTimeMillis();
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
                    LOGGER.info("Adding fetched results to event handler.");
                    eventHandler.handleEvent(taskId, freeForm);

                    telemetryHelper.recordSuccess("outlook3.fetchInboxAsync", asyncStartTime, Map.of(
                            "task_id", taskId,
                            "email_count", String.valueOf(mailDetails.size())
                    ));
                } catch (MustAuthorizeException cause) {
                    telemetryHelper.recordValidationError("outlook3.fetchInboxAsync", asyncStartTime, cause.getMessage(), safeTagMap("task_id", taskId));
                    LOGGER.error("Authorization error: {}", cause.getMessage(), cause);
                    throw cause;
                } catch (Exception cause) {
                    telemetryHelper.recordError("outlook3.fetchInboxAsync", asyncStartTime, cause, safeTagMap("task_id", taskId));
                    LOGGER.error("Unexpected error in async inbox fetch: {}", cause.getMessage(), cause);
                    throw new IllegalStateException(cause);
                } finally {
                    ThreadLocalProxy.removeAll();
                }
            });

            return taskId;
        } catch (IllegalStateException cause) {
            telemetryHelper.recordError("outlook3.fetchInboxAsync", startTime, cause, safeTagMap("phase", "submit"));
            LOGGER.error("Illegal state during inbox fetch async: {}", cause.getMessage(), cause);
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
        LOGGER.info("getResult: taskID: {}", taskID);
        long startTime = System.currentTimeMillis();

        if (eventName.equals(taskID)) {
            List<MailDetails> mailDetails = (List<MailDetails>) eventData.get(Constants.DATA);
            telemetryHelper.recordSuccess("outlook3.getResult", startTime, Map.of(
                    "task_id", taskID,
                    "mail_count", String.valueOf(mailDetails.size())
            ));
            return ExtensionResponseFactory.create(Map.of("Mail Details", mailDetails));
        }

        LOGGER.error("Get Result :::: Invalid task ID ::: {}", taskID);
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
            @Field(name = "Page Size", type = "Number", required = false) Double pageSize,
            @Field.Boolean(name = "Allow Retry", required = false,
                    attributes = {@Attribute(name = "visualWidth", value = "S")}) Boolean allowRetry) {

        long startTime = System.currentTimeMillis();
        LOGGER.info("fetchMailsByLabel: label: {}, pageNumber: {}, pageSize: {}, allowRetry: {}", label, pageNumber, pageSize, allowRetry);

        try {
            Map<Validator.ValidationResource, String> validationMap = ValidationResourceUtil.prepareValidateLabelMap(label, pageNumber, pageSize);
            List<ValidationOrchestrator.ValidationResult> validationResults = validationOrchestrator.validate(validationMap);

            if (validationResults.isEmpty()) {
                telemetryHelper.recordSuccess("outlook3.fetchMailsByLabel", startTime, safeTagMap(
                        "label", label,
                        "page_number", String.valueOf(pageNumber),
                        "page_size", String.valueOf(pageSize),
                        "allow_retry", String.valueOf(allowRetry)
                ));
                return messagingAreaImpl.fetchMailByLabel(label, pageNumber, pageSize);
            }

            if (Boolean.TRUE.equals(allowRetry)) {
                telemetryHelper.recordRetryPrompted("outlook3.fetchMailsByLabel", startTime, safeTagMap(
                        "label", label,
                        "page_number", String.valueOf(pageNumber),
                        "page_size", String.valueOf(pageSize),
                        "validation_count", String.valueOf(validationResults.size()),
                        "allow_retry", String.valueOf(allowRetry)
                ));

                String stateId = UUID.randomUUID().toString();
                internalStateManager.put(stateId, Constants.GSON.toJson(Map.of(SubCatalogConstants.VALIDATION_RESULTS, validationResults)));
                return responseGenerator.generateConfirmationResponse(
                        ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                        SubCatalogConstants.CONFIRM_REENTER_FETCH_MAIL_BY_LABEL,
                        StateMapperUtil.addFetchMailByLableMetaToMap(label, pageNumber, pageSize, stateId));
            } else {
                telemetryHelper.recordValidationError("outlook3.fetchMailsByLabel", startTime,
                        "Validation failed without retry",
                        safeTagMap(
                                "label", label,
                                "page_number", String.valueOf(pageNumber),
                                "page_size", String.valueOf(pageSize),
                                "validation_count", String.valueOf(validationResults.size()),
                                "allow_retry", String.valueOf(allowRetry)
                        ));
                return responseGenerator.generateFetchDenyResponse(
                        ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                        validationResults,
                        null,
                        Map.of());
            }

        } catch (MustAuthorizeException cause) {
            telemetryHelper.recordValidationError("outlook3.fetchMailsByLabel", startTime, cause.getMessage(), safeTagMap(
                    "label", label,
                    "page_number", String.valueOf(pageNumber),
                    "page_size", String.valueOf(pageSize),
                    "allow_retry", String.valueOf(allowRetry)
            ));
            return handleAuthorizationException(cause, requestContext.invokeAsUser());
        } catch (Exception cause) {
            LOGGER.error("Error occurred while Fetch Mails By Label: {}", cause.getMessage());
            telemetryHelper.recordError("outlook3.fetchMailsByLabel", startTime, cause, safeTagMap(
                    "label", label,
                    "page_number", String.valueOf(pageNumber),
                    "page_size", String.valueOf(pageSize),
                    "allow_retry", String.valueOf(allowRetry)
            ));
            return ExtensionResponseFactory.create("Error occurred while Fetch Mails By Label",
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR,
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
        long startTime = System.currentTimeMillis();

        try {
            if (eventName.equalsIgnoreCase(Constants.MAIL_RECEIVED)) {
                String messageId = (String) eventData.get(Constants.MESSAGE_ID);
                LOGGER.info("Processing Mail for Message Id :  {}", messageId);

                MailDetails mailDetails = mailHandler.fromEmail(account.getEmail(messageId), null);

                if (mailDetails != null) {
                    LOGGER.info("Allow Alert Mail Triggered : ID {}", mailDetails.messageID);

                    telemetryHelper.recordSuccess("outlook3.mailReceivedAlert", startTime, Map.of(
                            "message_id", messageId,
                            "subject", mailDetails.subject,
                            "from", mailDetails.from
                    ));

                    return ExtensionResponseFactory.create(Map.of("Mail Details", mailDetails));
                } else {
                    LOGGER.error("Mail details not available for message id: {}", messageId);
                    telemetryHelper.recordError("outlook3.mailReceivedAlert", startTime, new IllegalStateException("Mail details null"), safeTagMap(
                            "message_id", messageId));
                    throw new IllegalStateException("Mail details not available");
                }
            } else {
                LOGGER.error("Invalid event name: {}", eventName);
                telemetryHelper.recordValidationError("outlook3.mailReceivedAlert", startTime, "Invalid event name", safeTagMap(
                        "event_name", eventName
                ));
                throw new IllegalStateException("Invalid event name");
            }
        } catch (MustAuthorizeException cause) {
            LOGGER.error("Authorization error: {}", cause.getMessage());
            telemetryHelper.recordValidationError("outlook3.mailReceivedAlert", startTime, cause.getMessage(), safeTagMap(
                    "event_name", eventName
            ));
            return handleAuthorizationException(cause, requestContext.invokeAsUser());
        } catch (Exception cause) {
            LOGGER.error("Error occurred while Mail Received Alert:{}", cause.getMessage());
            telemetryHelper.recordError("outlook3.mailReceivedAlert", startTime, cause, safeTagMap("event_name", eventName));
            throw new IllegalStateException("Error occurred while Mail Received Alert");
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_83b957ac-6629-4ddc-b90b-7e1e218585a9",
            name = "Email Folder Alert",
            description = "Enhanced email alert with folder monitoring - triggers when emails arrive in or are moved into monitored folders",
            area = "Messaging",
            type = CatalogRequest.Type.WAIT_FOR_EVENT)
    @Field(name = "Email Details", type = "FreeForm", required = false, attributes = {}, options = {})
    public ExtensionResponse emailFolderAlert(
            @Field(name = "eventName", type = "Text") String eventName,
            @Field(name = "eventData", type = "FreeForm") FreeForm eventData) {
        long startTime = System.currentTimeMillis();

        try {
            if (eventName.equalsIgnoreCase(Constants.EMAIL_CHANGE_NOTIFICATION)) {
                String messageId = (String) eventData.get(Constants.MESSAGE_ID);
                String folderName = (String) eventData.get(Constants.FOLDER_NAME);
                String changeType = (String) eventData.get(Constants.CHANGE_TYPE);
                LOGGER.info("eventData: " + eventData);

                LOGGER.info("Processing Email Folder Alert - MessageId: {}, Folder: {}, ChangeType: {}",
                        messageId, folderName, changeType);

                // Return the comprehensive event data directly
                telemetryHelper.recordSuccess("outlook3.emailFolderAlert", startTime, Map.of(
                        "message_id", messageId,
                        "folder_name", folderName != null ? folderName : "unknown",
                        "change_type", changeType != null ? changeType : "unknown",
                        "subject", eventData.get(Constants.SUBJECT) != null ? eventData.get(Constants.SUBJECT).toString() : "N/A"
                ));

                return ExtensionResponseFactory.create(Map.of("Email Details", eventData));
            } else {
                LOGGER.error("Invalid event name for Email Folder Alert: {}", eventName);
                telemetryHelper.recordValidationError("outlook3.emailFolderAlert", startTime, "Invalid event name", safeTagMap(
                        "event_name", eventName
                ));
                throw new IllegalStateException("Invalid event name. Expected: " + Constants.EMAIL_CHANGE_NOTIFICATION);
            }
        } catch (MustAuthorizeException cause) {
            LOGGER.error("Authorization error in Email Folder Alert: {}", cause.getMessage());
            telemetryHelper.recordValidationError("outlook3.emailFolderAlert", startTime, cause.getMessage(), safeTagMap(
                    "event_name", eventName
            ));
            return handleAuthorizationException(cause, requestContext.invokeAsUser());
        } catch (Exception cause) {
            LOGGER.error("Error occurred while processing Email Folder Alert: {}", cause.getMessage());
            telemetryHelper.recordError("outlook3.emailFolderAlert", startTime, cause, safeTagMap("event_name", eventName));
            throw new IllegalStateException("Error occurred while processing Email Folder Alert");
        }    }


    @CatalogRequest(
            id = "localDomainRequest_90b24da6-d02f-4fcb-9632-ef8e6ae1550a",
            name = "Fetch Latest Mail",
            description = "Returns the latest email received",
            area = "Messaging",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Desc(name = "New Email", type = "Entity(Mail Details)", required = false)
    @SuppressWarnings("unchecked")
    public ExtensionResponse fetchLatestMail() {
        long startTime = System.currentTimeMillis();

        try {
            LOGGER.info("fetchLatestMail: start");
            List<MailDetails> mailDetailsList = (List<MailDetails>) fetchInbox(1.0, 1.0, null).getResponseValue().get("Inbox Mails");
            MailDetails mailDetails = mailDetailsList.isEmpty() ? null : mailDetailsList.getFirst();

            if (mailDetails != null) {
                telemetryHelper.recordSuccess("outlook3.fetchLatestMail", startTime, Map.of(
                        "message_id", mailDetails.messageID,
                        "subject", mailDetails.subject
                ));
                return ExtensionResponseFactory.create(Map.of("New Email", mailDetails));
            } else {
                telemetryHelper.recordError("outlook3.fetchLatestMail", startTime, new IllegalStateException("No mail found"), Map.of());
                return ExtensionResponseFactory.create("Error occurred while Fetch Latest Mail", ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR,
                        List.of(RemediationActionFactory.createInformActionALLParticipants("Error occurred while Fetch Latest Mail", List.of())), null, null);
            }
        } catch (MustAuthorizeException cause) {
            telemetryHelper.recordValidationError("outlook3.fetchLatestMail", startTime, cause.getMessage(), Map.of());
            return handleAuthorizationException(cause, requestContext.invokeAsUser());
        } catch (Exception cause) {
            LOGGER.error("Error occurred while Fetch Latest Mail:{}", cause.getMessage());
            telemetryHelper.recordError("outlook3.fetchLatestMail", startTime, cause, Map.of());
            return ExtensionResponseFactory.create("Error occurred while Fetch Latest Mail", ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR,
                    List.of(RemediationActionFactory.createInformActionALLParticipants("Error occurred while Fetch Latest Mail", List.of())), null, null);
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
        long startTime = System.currentTimeMillis();

        try {
            LOGGER.info("Fetching Categories");
            List<String> categoryNames = account.getCategoryNames();
            LOGGER.info("listCategories(): {}", categoryNames);

            telemetryHelper.recordSuccess("outlook3.listCategories", startTime, Map.of(
                    "category_count", String.valueOf(categoryNames.size())
            ));

            return ExtensionResponseFactory.create(Map.of("Category Names", categoryNames));
        } catch (MustAuthorizeException cause) {
            telemetryHelper.recordValidationError("outlook3.listCategories", startTime, cause.getMessage(), Map.of());
            return handleAuthorizationException(cause, requestContext.invokeAsUser());
        } catch (Exception cause) {
            LOGGER.error("Unable to fetch categories {}", cause.getMessage(), cause);
            telemetryHelper.recordError("outlook3.listCategories", startTime, cause, Map.of());
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
    @Field.Boolean(name = "Category Added", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")})
    public ExtensionResponse addCategoryToMessage(
            @Field.Text(name = "Message ID", attributes = {@Attribute(name = "visualWidth", value = "S")}) String messageID,
            @Field.Text(name = "Category", attributes = {@Attribute(name = "visualWidth", value = "S")}) String category,
            @Field.Boolean(name = "Create Category", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}) Boolean createCategory,
            @Field.Boolean(name = "Allow Retry", required = false,
                    attributes = {@Attribute(name = "visualWidth", value = "S")}) Boolean allowRetry) {

        long startTime = System.currentTimeMillis();
        try {
            LOGGER.info("addCategoryToMessage: messageID: {}, category: {}, allowRetry: {}", messageID, category, allowRetry);
            List<ValidationOrchestrator.ValidationResult> validationResults = validationOrchestrator.validate(Map.of(Validator.ValidationResource.MESSAGE_ID, messageID));

            if (validationResults.isEmpty()) {
                telemetryHelper.recordSuccess("outlook3.addCategoryToMessage", startTime,
                        safeTagMap("message_id", messageID, "category", category, "create_category", String.valueOf(createCategory),
                                "allow_retry", String.valueOf(allowRetry)));
                return messagingAreaImpl.addCategoryToMessage(messageID, category, createCategory);
            } else {
                if (Boolean.TRUE.equals(allowRetry)) {
                    String stateId = UUID.randomUUID().toString();
                    internalStateManager.put(stateId, Constants.GSON.toJson(Map.of(OutlookResources.MESSAGE_ID, messageID, SubCatalogConstants.VALIDATION_RESULTS, validationResults)));
                    telemetryHelper.recordRetryPrompted("outlook3.addCategoryToMessage", startTime,
                            safeTagMap("message_id", messageID, "category", category, "validation_count", String.valueOf(validationResults.size()),
                                    "allow_retry", String.valueOf(allowRetry)));
                    return responseGenerator.generateConfirmationResponse(
                            ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                            SubCatalogConstants.CONFIRM_REENTER_ADD_CATEGORY_TO_MESSAGE,
                            StateMapperUtil.addCategoryToMessageMetaToMap(messageID, category, createCategory, stateId));
                } else {
                    telemetryHelper.recordValidationError("outlook3.addCategoryToMessage", startTime,
                            "Validation failed without retry",
                            safeTagMap("message_id", messageID, "category", category,
                                    "validation_count", String.valueOf(validationResults.size()),
                                    "allow_retry", String.valueOf(allowRetry)));
                    return responseGenerator.generateFetchDenyResponse(
                            ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                            validationResults,
                            null,
                            Map.of());
                }
            }
        } catch (MustAuthorizeException cause) {
            telemetryHelper.recordValidationError("outlook3.addCategoryToMessage", startTime, cause.getMessage(),
                    safeTagMap("message_id", messageID, "category", category, "allow_retry", String.valueOf(allowRetry)));
            return handleAuthorizationException(cause, requestContext.invokeAsUser());
        } catch (Exception cause) {
            LOGGER.error("Error occurred while Add Category To Message:{}", cause.getMessage());
            telemetryHelper.recordError("outlook3.addCategoryToMessage", startTime, cause,
                    safeTagMap("message_id", messageID, "category", category, "allow_retry", String.valueOf(allowRetry)));
            return ExtensionResponseFactory.create("Error occurred while Add Category To Message", ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR,
                    List.of(RemediationActionFactory.createInformActionALLParticipants("Error occurred while Add Category To Message", List.of())), null, null);
        }
    }


    @CatalogRequest(
            id = "localDomainRequest_6cdb6cf3-4ca6-46f0-a682-75be3bd37c98",
            name = "Remove Category From Message",
            description = "This request will remove the given category for the given Message Id",
            area = "Messaging",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Boolean(name = "Category Removed", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")})
    public ExtensionResponse removeCategoryFromMessage(
            @Field.Text(name = "Message ID", attributes = {@Attribute(name = "visualWidth", value = "S")}) String messageID,
            @Field.Text(name = "Category", attributes = {@Attribute(name = "visualWidth", value = "S")}) String category,
            @Field.Boolean(name = "Allow Retry", required = false,
                    attributes = {@Attribute(name = "visualWidth", value = "S")}) Boolean allowRetry) {

        long startTime = System.currentTimeMillis();
        String baseMetric = "outlook3.removeCategoryFromMessage";

        try {
            LOGGER.info("removeCategoryFromMessage: messageID: {}, category: {}, allowRetry: {}", messageID, category, allowRetry);
            List<ValidationOrchestrator.ValidationResult> validationResults = validationOrchestrator.validate(
                    Map.of(Validator.ValidationResource.MESSAGE_ID, messageID, Validator.ValidationResource.CATEGORY, category));

            if (validationResults.isEmpty()) {
                ExtensionResponse response = messagingAreaImpl.removeCategoryFromMessage(messageID, category);
                telemetryHelper.recordSuccess(baseMetric, startTime,
                        safeTagMap("message_id", messageID, "category", category, "allow_retry", String.valueOf(allowRetry)));
                return response;
            } else {
                if (Boolean.TRUE.equals(allowRetry)) {
                    String stateId = UUID.randomUUID().toString();
                    internalStateManager.put(stateId, Constants.GSON.toJson(Map.of(OutlookResources.MESSAGE_ID, messageID,
                            SubCatalogConstants.VALIDATION_RESULTS, validationResults)));

                    telemetryHelper.recordRetryPrompted(baseMetric, startTime, safeTagMap("message_id", messageID, "category", category,
                            "validation_count", String.valueOf(validationResults.size()),
                            "allow_retry", String.valueOf(allowRetry)));

                    return responseGenerator.generateConfirmationResponse(
                            ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                            SubCatalogConstants.CONFIRM_REENTER_REMOVE_CATEGORY, Map.of(
                                    OutlookResources.STATE_ID, stateId,
                                    OutlookResources.MESSAGE_ID, messageID,
                                    OutlookResources.CATEGORY, category));
                } else {
                    telemetryHelper.recordValidationError(baseMetric, startTime,
                            "Validation failed without retry",
                            safeTagMap("message_id", messageID, "category", category,
                                    "validation_count", String.valueOf(validationResults.size()),
                                    "allow_retry", String.valueOf(allowRetry)));
                    return responseGenerator.generateFetchDenyResponse(
                            ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                            validationResults,
                            null,
                            Map.of());
                }
            }
        } catch (MustAuthorizeException cause) {
            telemetryHelper.recordValidationError(baseMetric, startTime, cause.getMessage(),
                    safeTagMap("message_id", messageID, "category", category, "allow_retry", String.valueOf(allowRetry)));
            return handleAuthorizationException(cause, requestContext.invokeAsUser());
        } catch (Exception cause) {
            LOGGER.error("Error occurred while Remove Category From Message:{}", cause.getMessage());
            telemetryHelper.recordError(baseMetric, startTime, cause,
                    safeTagMap("message_id", messageID, "category", category, "allow_retry", String.valueOf(allowRetry)));
            return ExtensionResponseFactory.create("Error occurred while Remove Category From Message",
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR,
                    List.of(RemediationActionFactory.createInformActionALLParticipants("Error occurred while Remove Category From Message", List.of())),
                    null, null);
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_121d8318-08ab-4c51-acf4-e45d302ac018",
            name = "Get Notification Delta",
            description = "This request is used to retrieve delta notifications that were missed by the alert event.",
            area = "Messaging",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Desc(name = "Message Ids", type = "[ Text ]", required = false)
    public ExtensionResponse getNotificationDelta() {
        long startTime = System.currentTimeMillis();
        String baseMetric = "outlook3.getNotificationDelta";

        try {
            ExtensionResponse response = messagingAreaImpl.fetchNotificationDelta();
            telemetryHelper.recordSuccess(baseMetric, startTime, Map.of());
            return response;
        } catch (MustAuthorizeException cause) {
            telemetryHelper.recordValidationError(baseMetric, startTime, cause.getMessage(), Map.of());
            return handleAuthorizationException(cause, requestContext.invokeAsUser());
        } catch (Exception cause) {
            LOGGER.error("Error occurred while fetching notification delta:{}", cause.getMessage());
            telemetryHelper.recordError(baseMetric, startTime, cause, Map.of());
            return new ExtensionResponse(ExtensionResponse.Result.SUCCESS, Map.of("Message Ids", List.of()), null, null, null);
        }
    }


    @CatalogRequest(
            id = "localDomainRequest_796c1290-9922-4d27-805e-5d971303be1a",
            name = "Send Alert Using Notification Delta",
            description = "This request is used to send an alert to the Mail Received Alert request and accepts the Message ID as input.",
            area = "Messaging",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    public void sendAlertUsingNotificationDelta(
            @Field.Text(name = "Message Id", attributes = {@Attribute(name = "visualWidth", value = "S")}) String messageId) {
        long startTime = System.currentTimeMillis();
        String baseMetric = "outlook3.sendAlertUsingNotificationDelta";

        try {
            FreeForm freeForm = new FreeForm();
            freeForm.put(Constants.MESSAGE_ID, Constants.TEXT, messageId);
            LOGGER.info("Sending notification delta alert to Krista: {} ", messageId);
            eventHandler.handleEvent(Constants.MAIL_RECEIVED, freeForm);
            telemetryHelper.recordSuccess(baseMetric, startTime, safeTagMap("message_id", messageId));
        } catch (MustAuthorizeException cause) {
            telemetryHelper.recordValidationError(baseMetric, startTime, cause.getMessage(), safeTagMap("message_id", messageId));
            LOGGER.error(cause.getMessage());
            throw cause;
        } catch (Exception cause) {
            telemetryHelper.recordError(baseMetric, startTime, cause, safeTagMap("message_id", messageId));
            LOGGER.error("Error occurred while sending alert using notification delta:{}", cause.getMessage());
        }
    }


    @CatalogRequest(
            id = "localDomainRequest_2694e61a-1d82-4f55-8a74-a66eba60fe63",
            name = "Mark Message Category And Status",
            description = "Applies a category to the message and updates its read/unread status.",
            area = "Messaging",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Text(name = "Response", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")})
    public ExtensionResponse markMessageCategoryAndStatus(
            @Field.Text(name = "Message ID", attributes = {@Attribute(name = "visualWidth", value = "S")}) String messageID,
            @Field.PickOne(name = "Label", values = {"Read", "Unread"}, required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}) String label,
            @Field.Text(name = "Category", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}) String category,
            @Field.Boolean(name = "Allow Retry", required = false,
                    attributes = {@Attribute(name = "visualWidth", value = "S")}) Boolean allowRetry) {
        long startTime = System.currentTimeMillis();
        String baseMetric = "mark_message_category_and_status";
        Map<String, String> tags = safeTagMap("message_id", messageID, "allow_retry", String.valueOf(allowRetry));

        try {
            LOGGER.info("markMessageCategoryAndStatus: messageID: {}, label: {}, category: {}, allowRetry: {}", messageID, label, category, allowRetry);
            List<ValidationOrchestrator.ValidationResult> validationResults = validationOrchestrator.validate(Map.of(Validator.ValidationResource.MESSAGE_ID, messageID));

            if (validationResults.isEmpty()) {
                ExtensionResponse response = messagingAreaImpl.markMessageCategoryAndStatus(messageID, label, category);
                telemetryHelper.recordSuccess(baseMetric, startTime, tags);
                return response;
            } else {
                if (Boolean.TRUE.equals(allowRetry)) {
                    telemetryHelper.recordRetryPrompted(baseMetric, startTime,
                            safeTagMap("message_id", messageID, "validation_count", String.valueOf(validationResults.size()),
                                    "allow_retry", String.valueOf(allowRetry)));
                    String stateId = UUID.randomUUID().toString();
                    internalStateManager.put(stateId, Constants.GSON.toJson(Map.of(
                            OutlookResources.MESSAGE_ID, messageID,
                            SubCatalogConstants.VALIDATION_RESULTS, validationResults
                    )));
                    return responseGenerator.generateConfirmationResponse(
                            ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                            SubCatalogConstants.CONFIRM_REENTER_MARK_MESSAGE, Map.of(
                                    OutlookResources.STATE_ID, stateId,
                                    OutlookResources.LABEL, label,
                                    OutlookResources.MESSAGE_ID, messageID
                            ));
                } else {
                    telemetryHelper.recordValidationError(baseMetric, startTime,
                            "Validation failed without retry",
                            safeTagMap("message_id", messageID, "validation_count", String.valueOf(validationResults.size()),
                                    "allow_retry", String.valueOf(allowRetry)));
                    return responseGenerator.generateFetchDenyResponse(
                            ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                            validationResults,
                            null,
                            Map.of());
                }
            }
        } catch (MustAuthorizeException cause) {
            telemetryHelper.recordValidationError(baseMetric, startTime, cause.getMessage(), tags);
            return handleAuthorizationException(cause, requestContext.invokeAsUser());
        } catch (Exception cause) {
            telemetryHelper.recordError(baseMetric, startTime, cause, tags);
            LOGGER.error("Error occurred while updating message category and status: {}", cause.getMessage());
            return ExtensionResponseFactory.create("Error occurred while updating message category and status",
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR,
                    List.of(RemediationActionFactory.createInformActionALLParticipants("Error occurred while updating message category and status", List.of())),
                    null, null);
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_2a61e367-6599-4f66-addf-4dc4a5529b8d",
            name = "Check If Triggered Mail Ids Exist",
            description = "Checks whether a specific mail ID exists in the triggered mail IDs set.",
            area = "Messaging",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Boolean(name = "IsExist", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")})
    public Boolean checkIfTriggeredMailIdsExist(
            @Field.Text(name = "MessageId", attributes = {@Attribute(name = "visualWidth", value = "S")}) String messageId) {
        try {
            LOGGER.info("Checking if message ID exists in triggered mail IDs: {}", messageId);

            if (messageId == null || messageId.trim().isEmpty()) {
                LOGGER.error("Message ID is null or empty");
                return false;
            }

            boolean exists = OutlookApiResource.isMessageIdTriggered(messageId);

            LOGGER.info("Message ID {} {} in triggered mail IDs set", messageId, exists ? "exists" : "does not exist");
            return exists;
        } catch (Exception cause) {
            LOGGER.error("Error occurred while checking triggered mail IDs: {}", cause.getMessage(), cause);
            return false;
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_946ac0ec-e822-4911-9edd-c4b1b985b69c",
            name = "Test Connection",
            description = "This test connection request validates the connection using stored or provided configuration parameters. It performs comprehensive connectivity tests including OAuth token acquisition, mailbox connectivity, and scope validation to ensure the integration is working properly.",
            area = "Messaging",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Boolean(name = "Is Connection Successful", required = false, attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'Returns true if all connection tests passed successfully, including OAuth authentication, API connectivity, and mailbox access.'")})
    @Field.Desc(name = "Test Connection Summary", type = "{ Summary: Text, Email: Text, Allow Mail Alert: Text, Tenant ID: Text, Client ID: Text, Auth Type: Text, Mailbox Accessible: Text }", required = false)
    @Field.Desc(name = "Extension Response Meta", type = "Entity(Extension Response Meta)", required = false)
    public ExtensionResponse testConnection() {
        return testConnectionService.testConnection(invoker.getInvokerId());
    }

}