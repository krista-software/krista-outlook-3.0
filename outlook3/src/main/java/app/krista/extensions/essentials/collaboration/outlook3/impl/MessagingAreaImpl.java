package app.krista.extensions.essentials.collaboration.outlook3.impl;

import app.krista.extension.executor.ExtensionResponse;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.extresp.ExtensionResponseFactory;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.extresp.RemediationActionFactory;
import app.krista.extensions.essentials.collaboration.outlook3.impl.connectors.GraphServiceClientProvider;
import app.krista.extensions.essentials.collaboration.outlook3.impl.connectors.GraphServiceClientProviderFactory;
import app.krista.extensions.essentials.collaboration.outlook3.impl.util.Constants;
import app.krista.extensions.essentials.collaboration.outlook3.impl.util.EntityHelperUtil;
import app.krista.extensions.essentials.collaboration.outlook3.impl.util.Validators;
import app.krista.extensions.essentials.collaboration.outlook3.service.Account;
import app.krista.extensions.essentials.collaboration.outlook3.service.Email;
import app.krista.extensions.essentials.collaboration.outlook3.service.EmailBuilder;
import app.krista.extensions.essentials.collaboration.outlook3.service.Folder;
import app.krista.ksdk.entities.Entities;
import app.krista.model.base.EntityValue;
import app.krista.model.base.File;
import com.microsoft.graph.http.GraphServiceException;
import com.microsoft.graph.models.User;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static app.krista.extensions.essentials.collaboration.outlook3.impl.util.EntityHelperUtil.toEmailAddresses;


@Service
public class MessagingAreaImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessagingAreaImpl.class);
    private static final String SENDING_MESSAGE = "Sending message {}";
    private static final String MAILS = "Mails";
    private static final String RESPONSE_MESSAGE = "Message";
    private static final String CATEGORY_CANNOT_BE_EMPTY = "Category cannot be empty.";
    private static final String MESSAGE_ID_CANNOT_BE_EMPTY = "Message ID cannot be empty.";
    private final Account account;
    private final MailHandler mailHandler;
    private Entities registry;
    private final GraphServiceClientProvider provider;

    @Inject
    public MessagingAreaImpl(Account account, MailHandler mailHandler, Entities registry, GraphServiceClientProviderFactory providerFactory) {
        this.account = account;
        this.mailHandler = mailHandler;
        this.registry = registry;
        this.provider = providerFactory.create();
    }


    public ExtensionResponse replyToAllWithCCAndBCC(List<File> attachments, String messageId, String to, String cc, String bcc, String replyTo, String message, String bodyType) {
        try {
            Email email = account.getEmail(messageId);
            if (email == null) {
                LOGGER.error(Constants.INVALID_MESSAGE_ID);
                return ExtensionResponseFactory.create(Constants.INVALID_MESSAGE_ID, ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                        List.of(RemediationActionFactory.createInformActionALLParticipants(Constants.INVALID_MESSAGE_ID, List.of())),
                        null, Map.of());
            }
            bodyType = getBodyType(bodyType);
            String originalDate = getReceivedDateAndTime(email);
            message = EntityHelperUtil.formatMessageWithThread(email, message, bodyType, originalDate);
            LOGGER.info(SENDING_MESSAGE, message);
            email.replyToAll(message, mailHandler.toAttachment(attachments), toEmailAddresses(to), toEmailAddresses(cc),
                    toEmailAddresses(bcc), toEmailAddresses(replyTo), bodyType);
            return ExtensionResponseFactory.create(Map.of(Constants.IS_SUCCESSFUL, true));
        } catch (Exception cause) {
            LOGGER.error("Failed to Reply all with Cc and Bcc: {}", cause.getMessage(), cause);
            return ExtensionResponseFactory.create(cause, "Failed to Reply all with Cc and Bcc",
                    ExtensionResponse.Error.ExceptionType.INPUT_ERROR);
        }
    }

    private String getReceivedDateAndTime(Email email) {
        User user;
        try {
            user = provider.getGraphServiceClientForUser(true, null)
                    .me()
                    .buildRequest()
                    .select(Constants.MAILBOX_SETTINGS)
                    .get();
        } catch (IOException cause) {
            LOGGER.error("Failed to get user mailbox settings: {}", cause.getMessage(), cause);
            return null;
        }

        String javaTimeZone = EntityHelperUtil.mapWindowsTimeZoneToJava(
                Objects.requireNonNull(user != null && user.mailboxSettings != null ? user.mailboxSettings.timeZone : Constants.UTC));

        Long dateToUse = email.getReceivedDateAndTime() != null ? email.getReceivedDateAndTime() : email.getSendDateAndTime();
        if (dateToUse == null) return "";

        ZonedDateTime zonedDateTime = Instant.ofEpochMilli(dateToUse).atZone(ZoneId.of(javaTimeZone));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constants.DATE_TIME_FORMAT_PATTERN);
        return formatter.format(zonedDateTime);
    }

    public ExtensionResponse replyToAll(List<File> attachments, String messageId, String message, String bodyType) {
        try {
            Email email = account.getEmail(messageId);
            if (email == null) {
                LOGGER.debug(Constants.INVALID_MESSAGE_ID);
                return ExtensionResponseFactory.create(Constants.INVALID_MESSAGE_ID, ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                        List.of(RemediationActionFactory.createInformActionALLParticipants(Constants.INVALID_MESSAGE_ID, List.of())),
                        null, Map.of());
            }
            bodyType = getBodyType(bodyType);
            String originalDate = getReceivedDateAndTime(email);
            message = EntityHelperUtil.formatMessageWithThread(email, message, bodyType, originalDate);
            LOGGER.info(SENDING_MESSAGE, message);
            email.replyToAll(message, mailHandler.toAttachment(attachments), bodyType);
            return ExtensionResponseFactory.create(Map.of(Constants.IS_SUCCESSFUL, true));
        } catch (Exception cause) {
            LOGGER.error("Failed to reply all: {}", cause.getMessage(), cause);
            return ExtensionResponseFactory.create("Failed to reply all", ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    List.of(RemediationActionFactory.createInformActionALLParticipants("Failed to reply all", List.of())),
                    null, Map.of());
        }
    }

    public ExtensionResponse forwardMail(String messageId, String to, String message, String bodyType) {
        try {
            LOGGER.info("Forwarding mail with messageId: {}; to {}; with message: {}", messageId, to, message);

            Email email = account.getEmail(messageId);
            LOGGER.info("email: {}", email);

            if (email == null) {
                LOGGER.error(Constants.INVALID_MESSAGE_ID + ": {}", messageId);
                return ExtensionResponseFactory.create(Map.of(Constants.IS_FORWARDED, false));
            }
            bodyType = getBodyType(bodyType);
            String originalDate = getReceivedDateAndTime(email);
            message = EntityHelperUtil.formatMessageWithThread(email, message, bodyType, originalDate);
            LOGGER.info(SENDING_MESSAGE, message);
            email.forward(message, toEmailAddresses(to), bodyType);
            return ExtensionResponseFactory.create(Map.of(Constants.IS_FORWARDED, true));
        } catch (GraphServiceException graphServiceException) {
            String errorMessage = graphServiceException.getMessage();
            LOGGER.error("Failed to forward mail: {}", errorMessage, graphServiceException);
            if (errorMessage != null && errorMessage.contains(Constants.ONE_INVALID_MAIL)) {
                return ExtensionResponseFactory.create(Constants.INVALID_MAIL_ADDRESS, ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                        List.of(RemediationActionFactory.createInformActionALLParticipants(Constants.INVALID_MAIL_ADDRESS, List.of())),
                        null, Map.of());

            }
            return ExtensionResponseFactory.create(Constants.FORWARD_MAIL_REQUEST_FAILED, ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    List.of(RemediationActionFactory.createInformActionALLParticipants(Constants.FORWARD_MAIL_REQUEST_FAILED, List.of())),
                    null, Map.of());
        }
    }

    public ExtensionResponse sendMail(String subject, String message, List<File> attachments, String to, String cc, String bcc, String replyTo, String bodyType) {
        try {
            LOGGER.info("Sending email to {}", to);

            EmailBuilder builder = account.newEmail();
            builder.withText(subject);
            bodyType = getBodyType(bodyType);
            message = getFormattedMessage(message, bodyType);
            builder.withContent(bodyType, message);
            builder.withTo(toEmailAddresses(to));
            builder.withCc(toEmailAddresses(cc));
            builder.withBcc(toEmailAddresses(bcc));
            builder.withReplyTo(toEmailAddresses(replyTo));
            if (attachments != null && !attachments.isEmpty()) {
                builder.withAttachment(mailHandler.toAttachment(attachments));
            }

            LOGGER.info("Sending email: " + ReflectionToStringBuilder.toString(builder));

            builder.send();
            return ExtensionResponseFactory.create(Map.of(RESPONSE_MESSAGE, Constants.SUCCESS));
        } catch (GraphServiceException graphServiceException) {
            String errorMessage = graphServiceException.getMessage();
            LOGGER.error("Failed to send mail: {}", errorMessage, graphServiceException);
            if (errorMessage != null && errorMessage.contains(Constants.ONE_INVALID_MAIL)) {
                return ExtensionResponseFactory.create(Constants.INVALID_MAIL_ADDRESS, ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                        List.of(RemediationActionFactory.createInformActionALLParticipants(Constants.INVALID_MAIL_ADDRESS, List.of())),
                        null, Map.of());
            }
            return ExtensionResponseFactory.create(Constants.SEND_MAIL_REQUEST_FAILED, ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    List.of(RemediationActionFactory.createInformActionALLParticipants(Constants.SEND_MAIL_REQUEST_FAILED, List.of())),
                    null, Map.of());
        }
    }

    public ExtensionResponse sendMailWithTable(String subject, String message, List<File> attachments, String to, String cc, String bcc, String replyTo, List<EntityValue> entityList, List<String> removeEntityFieldFromTable) {
        try {
            LOGGER.info("Sending email to {}; with body: {}", to, message);

            EmailBuilder builder = account.newEmail();
            builder.withText(subject);
            String content = EntityHelperUtil.getMessageContent(message, entityList, removeEntityFieldFromTable, registry);
            builder.withContent(Constants.HTML, content);
            builder.withTo(toEmailAddresses(to));
            builder.withCc(toEmailAddresses(cc));
            builder.withBcc(toEmailAddresses(bcc));
            builder.withReplyTo(toEmailAddresses(replyTo));
            if (attachments != null && !attachments.isEmpty()) {
                builder.withAttachment(mailHandler.toAttachment(attachments));
            }
            builder.send();
            return ExtensionResponseFactory.create(Map.of(RESPONSE_MESSAGE, Constants.SUCCESS));
        } catch (GraphServiceException cause) {
            String errorMessage = cause.getMessage();
            LOGGER.error("Failed to send mail with table: {}", errorMessage, cause);
            if (errorMessage != null && errorMessage.contains(Constants.ONE_INVALID_MAIL)) {
                return ExtensionResponseFactory.create(Constants.INVALID_MAIL_ADDRESS, ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                        List.of(RemediationActionFactory.createInformActionALLParticipants(Constants.INVALID_MAIL_ADDRESS, List.of())),
                        null, Map.of());
            }
            return ExtensionResponseFactory.create(Constants.SEND_MAIL_WITH_TABLE_REQUEST_FAILED, ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    List.of(RemediationActionFactory.createInformActionALLParticipants(Constants.SEND_MAIL_WITH_TABLE_REQUEST_FAILED, List.of())),
                    null, Map.of());
        }
    }

    public ExtensionResponse markMessage(String messageID, String label) {
        try {
            Email email = account.getEmail(messageID);
            if (email == null) {
                return ExtensionResponseFactory.create("Unable to mark message, no email found with messageID : " + messageID, ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                        List.of(RemediationActionFactory.createInformActionALLParticipants("Unable to mark message, no email found with messageID : " + messageID, List.of())),
                        null, Map.of());
            }
            if (label.equalsIgnoreCase(Constants.READ)) {
                email.markAsRead();
                LOGGER.info("Marked message {} for messageID {}", label, messageID);
            } else if (label.equalsIgnoreCase(Constants.UNREAD)) {
                email.markAsUnread();
                LOGGER.info("Marked message {} for messageID {}", label, messageID);
            } else {
                return ExtensionResponseFactory.create("", ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                        List.of(RemediationActionFactory.createInformActionALLParticipants("Unable to mark message (un)read invalid label " + label + " for messageID: " + messageID, List.of())),
                        null, Map.of());
            }
            return ExtensionResponseFactory.create(Map.of("Response", Constants.SUCCESS));
        } catch (GraphServiceException graphServiceException) {
            LOGGER.error("Failed to mark message: {}", graphServiceException.getMessage(), graphServiceException);
            return ExtensionResponseFactory.create(Constants.MARK_MESSAGE_REQUEST_FAILED, ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    List.of(RemediationActionFactory.createInformActionALLParticipants(Constants.MARK_MESSAGE_REQUEST_FAILED, List.of())),
                    null, Map.of());
        }
    }


    public ExtensionResponse replyToMailWithCCAndBCC(List<File> attachments, String messageId, String to, String cc, String bcc, String replyTo, String message, String bodyType) {
        try {
            Email email = account.getEmail(messageId);
            if (email == null) {
                return ExtensionResponseFactory.create(Constants.INVALID_MESSAGE_ID, ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                        List.of(RemediationActionFactory.createInformActionALLParticipants(Constants.INVALID_MESSAGE_ID, List.of())),
                        null, Map.of());
            }
            bodyType = getBodyType(bodyType);
            String originalDate = getReceivedDateAndTime(email);
            message = EntityHelperUtil.formatMessageWithThread(email, message, bodyType, originalDate);
            LOGGER.info(SENDING_MESSAGE, message);
            email.replyText(message, mailHandler.toAttachment(attachments), toEmailAddresses(to), toEmailAddresses(cc),
                    toEmailAddresses(bcc), toEmailAddresses(replyTo), bodyType);
            return ExtensionResponseFactory.create(Map.of(RESPONSE_MESSAGE, Constants.SUCCESS));
        } catch (GraphServiceException graphServiceException) {
            String errorMessage = graphServiceException.getMessage();
            LOGGER.error("Failed to reply to mail: {}", errorMessage, graphServiceException);
            if (errorMessage != null && errorMessage.contains(Constants.ONE_INVALID_MAIL)) {
                return ExtensionResponseFactory.create(Constants.INVALID_MAIL_ADDRESS, ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                        List.of(RemediationActionFactory.createInformActionALLParticipants(Constants.INVALID_MAIL_ADDRESS, List.of())),
                        null, Map.of());
            }
            return ExtensionResponseFactory.create(Constants.REPLY_TO_MAIL_REQUEST_FAILED, ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    List.of(RemediationActionFactory.createInformActionALLParticipants(Constants.REPLY_TO_MAIL_REQUEST_FAILED, List.of())),
                    null, Map.of());
        }
    }

    public ExtensionResponse replyToMail(List<File> attachments, String messageId, String message, String bodyType) {
        try {
            Email email = account.getEmail(messageId);
            if (email == null) {
                return ExtensionResponseFactory.create(Map.of(RESPONSE_MESSAGE, Constants.INVALID_MESSAGE_ID));
            }
            bodyType = getBodyType(bodyType);
            String originalDate = getReceivedDateAndTime(email);
            message = EntityHelperUtil.formatMessageWithThread(email, message, bodyType, originalDate);
            LOGGER.info(SENDING_MESSAGE, message);
            email.replyText(message, mailHandler.toAttachment(attachments), bodyType);
            return ExtensionResponseFactory.create(Map.of(RESPONSE_MESSAGE, Constants.SUCCESS));
        } catch (GraphServiceException graphServiceException) {
            String errorMessage = graphServiceException.getMessage();
            LOGGER.error("Failed to reply to mail: {}", errorMessage, graphServiceException);
            if (errorMessage != null && errorMessage.contains(Constants.ONE_INVALID_MAIL)) {
                return ExtensionResponseFactory.create(Constants.INVALID_MAIL_ADDRESS, ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                        List.of(RemediationActionFactory.createInformActionALLParticipants(Constants.INVALID_MAIL_ADDRESS, List.of())),
                        null, Map.of());
            }
            return ExtensionResponseFactory.create(Constants.REPLY_TO_MAIL_REQUEST_FAILED, ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    List.of(RemediationActionFactory.createInformActionALLParticipants(Constants.REPLY_TO_MAIL_REQUEST_FAILED, List.of())),
                    null, Map.of());
        }
    }

    public ExtensionResponse addCategoryToMessage(String messageID, String category, Boolean createCategory) {
        if (Validators.isStringNullOrBlank(category)) {
            return ExtensionResponseFactory.create(CATEGORY_CANNOT_BE_EMPTY, ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    List.of(RemediationActionFactory.createInformActionALLParticipants(CATEGORY_CANNOT_BE_EMPTY, List.of())),
                    null, Map.of());
        }
        if (Validators.isStringNullOrBlank(messageID)) {
            return ExtensionResponseFactory.create(MESSAGE_ID_CANNOT_BE_EMPTY, ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    List.of(RemediationActionFactory.createInformActionALLParticipants(MESSAGE_ID_CANNOT_BE_EMPTY, List.of())),
                    null, Map.of());
        }
        if (Boolean.TRUE.equals(createCategory)) {
            account.createCategory(category);
        }
        Email email = account.getEmail(messageID);
        return ExtensionResponseFactory.create(Map.of("Category Added", email.addCategory(category)));
    }

    public ExtensionResponse fetchMailByLabel(String label, Double pageNumber, Double pageSize) {
        LOGGER.info("Fetching mails by label: label={}, pageNumber={}, pageSize={}", label, pageNumber, pageSize);

        try {
            Folder folder = account.getFolderByName(List.of(label.split(Constants.FORWARD_SLASH)));
            if (folder == null) {
                LOGGER.error(Constants.FETCH_MAIL_FAILED_NO_FOLDER);
                return ExtensionResponseFactory.create(Map.of(MAILS, Collections.emptyList()));
            }
            List<Email> emails = folder.getEmails(pageNumber, pageSize);
            return ExtensionResponseFactory.create(Map.of(MAILS, emails.stream().map(email -> mailHandler.fromEmail(email, null)).collect(Collectors.toList())));
        } catch (GraphServiceException graphServiceException) {
            LOGGER.error(Constants.FETCH_MAIL_FAILED_NO_FOLDER + graphServiceException.getCause(), graphServiceException);
            return ExtensionResponseFactory.create(Map.of(MAILS, Collections.emptyList()));
        }
    }

    public static String getFormattedMessage(String message, String bodyType) {
        return EntityHelperUtil.formattedMessage(message, bodyType);
    }


    public ExtensionResponse removeCategoryFromMessage(String messageID, String category) {
        if (Validators.isStringNullOrBlank(category)) {
            return ExtensionResponseFactory.create(CATEGORY_CANNOT_BE_EMPTY, ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    List.of(RemediationActionFactory.createInformActionALLParticipants(CATEGORY_CANNOT_BE_EMPTY, List.of())),
                    null, Map.of());
        }
        if (Validators.isStringNullOrBlank(messageID)) {
            return ExtensionResponseFactory.create(MESSAGE_ID_CANNOT_BE_EMPTY, ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    List.of(RemediationActionFactory.createInformActionALLParticipants(MESSAGE_ID_CANNOT_BE_EMPTY, List.of())),
                    null, Map.of());
        }
        Email email = account.getEmail(messageID);
        return ExtensionResponseFactory.create(Map.of("Category Removed", email.removeCategory(category)));
    }

    @NotNull
    public static String getBodyType(String bodyType) {
        return (bodyType == null) ? Constants.HTML : bodyType;
    }

    public ExtensionResponse fetchNotificationDelta() {
        return ExtensionResponseFactory.create(Map.of("Message Ids", account.fetchNotificationDeltaQuery()));
    }

    public ExtensionResponse markMessageCategoryAndStatus(String messageID, String label, String category) {
        try {
            Email email = account.getEmail(messageID);
            if (email == null) {
                return ExtensionResponseFactory.create("Unable to update message, no email found with messageID : " + messageID, ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                        List.of(RemediationActionFactory.createInformActionALLParticipants("Unable to update message, no email found with messageID : " + messageID, List.of())),
                        null, Map.of());
            }

            // Update category if provided
            if (category != null && !category.isEmpty()) {
                boolean categoryAdded = email.addCategory(category);
                if (!categoryAdded) {
                    LOGGER.error("Failed to add category {} to message {}", category, messageID);
                } else {
                    LOGGER.info("Added category {} to message {}", category, messageID);
                }
            }

            // Update read/unread status if provided
            if (label != null) {
                if (label.equalsIgnoreCase(Constants.READ)) {
                    email.markAsRead();
                    LOGGER.info("Marked message {} for messageID {}", label, messageID);
                } else if (label.equalsIgnoreCase(Constants.UNREAD)) {
                    email.markAsUnread();
                    LOGGER.info("Marked message {} for messageID {}", label, messageID);
                } else {
                    LOGGER.error("Invalid label {} for messageID {}", label, messageID);
                }
            }

            return ExtensionResponseFactory.create(Map.of("Response", Constants.SUCCESS));
        } catch (GraphServiceException graphServiceException) {
            LOGGER.error("Failed to update message category and status: {}", graphServiceException.getMessage(), graphServiceException);
            return ExtensionResponseFactory.create("Failed to update message category and status", ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    List.of(RemediationActionFactory.createInformActionALLParticipants("Failed to update message category and status", List.of())),
                    null, Map.of());
        }
    }
}
