package app.krista.extensions.essentials.collaboration.outlook3.catalog;

import app.krista.extension.impl.anno.Attribute;
import app.krista.extension.impl.anno.CatalogRequest;
import app.krista.extension.impl.anno.Domain;
import app.krista.extension.impl.anno.Field;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.entities.MailDetails;
import app.krista.extensions.essentials.collaboration.outlook3.impl.AccountImpl;
import app.krista.extensions.essentials.collaboration.outlook3.impl.MailHandler;
import app.krista.extensions.essentials.collaboration.outlook3.impl.util.Constants;
import app.krista.extensions.essentials.collaboration.outlook3.impl.util.EntityHelperUtil;
import app.krista.extensions.essentials.collaboration.outlook3.impl.util.Validators;
import app.krista.extensions.essentials.collaboration.outlook3.service.Account;
import app.krista.extensions.essentials.collaboration.outlook3.service.Email;
import app.krista.extensions.essentials.collaboration.outlook3.service.EmailBuilder;
import app.krista.extensions.essentials.collaboration.outlook3.service.Folder;
import app.krista.extensions.util.EventHandler;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.ksdk.context.RequestContext;
import app.krista.ksdk.files.FileRepository;
import app.krista.model.base.EntityValue;
import app.krista.model.base.File;
import app.krista.model.base.FreeForm;
import com.microsoft.graph.http.GraphServiceException;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.InternalServerErrorException;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static app.krista.extensions.essentials.collaboration.outlook3.impl.util.EntityHelperUtil.toEmailAddresses;

@Domain(id = "catEntryDomain_5fa2fc97-4b17-44cf-b98f-aa91a459a091",
        name = "Collaboration",
        ecosystemId = "catEntryEcosystem_84b53163-327b-4b1b-8c96-9334d292f9f5",
        ecosystemName = "Essentials",
        ecosystemVersion = "fe952090-0ea0-424e-96b2-300bc53d1b7d")
public class MessagingArea {

    private static final Logger logger = LoggerFactory.getLogger(MessagingArea.class);
    private final Account account;
    private final RequestContext requestContext;
    private final AuthorizationContext authorizationContext;
    private final EventHandler eventHandler;
    private final MailHandler mailHandler;
    ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Inject
    public MessagingArea(AccountImpl account, RequestContext requestContext, AuthorizationContext authorizationContext,
                         EventHandler eventHandler, FileRepository fileRepository) {
        this.account = account;
        this.requestContext = requestContext;
        this.authorizationContext = authorizationContext;
        this.eventHandler = eventHandler;
        this.mailHandler = new MailHandler(fileRepository);
    }

    @CatalogRequest(
            id = "localDomainRequest_e39048cc-1795-4eee-8400-8fc3061c4e87",
            name = "Fetch All Labels",
            description = "Returns list of labels",
            area = "Messaging",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Desc(name = "Labels", type = "[ Text ]", required = false)
    public List<String> fetchAllLabels() {
        return account.getFolderNames();
    }

    @CatalogRequest(
            id = "localDomainRequest_ac177adc-e633-4ca1-baf8-7ce7efc5c0e5",
            name = "Fetch Mail By Message Id",
            description = "Accepts message Id as input and returns mail. In case of invalid input, this will return empty data.",
            area = "Messaging",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Desc(name = "Mail", type = "Entity(Mail Details)", required = false)
    public MailDetails fetchMailByMessageId(
            @Field(name = "Message ID", type = "Text") String messageID) {
        logger.info("fetchMailByMessageId: start {}", messageID);

        MailDetails mailDetails = mailHandler.fromEmail(account.getEmail(messageID), null);
        if (mailDetails != null) {
            logger.info("fetchMailByMessageId: ID {}, from {}, subject {}, timestamp {}",
                    mailDetails.messageID, mailDetails.from, mailDetails.subject, new Date(mailDetails.sendDateAndTime));
        }
        return mailDetails;
    }

    @CatalogRequest(
            id = "localDomainRequest_95edb739-f511-453c-b0ec-647daf0df206",
            name = "Move Message",
            description = "Accepts message ID, and folder name as input and move one message from source folder to another folder and returns response message.",
            area = "Messaging",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field(name = "Message ID", type = "Text", required = false)
    public String moveMessage(
            @Field(name = "Message ID", type = "Text") String messageID,
            @Field(name = "Folder Name", type = "Text") String folderName) {

        logger.info("Moving message with ID {} to folder: {}", messageID, folderName);

        Email email = account.getEmail(messageID);
        if (email == null) {
            return Constants.INVALID_MESSAGE_ID;
        }
        Folder folder = account.getFolderByName(List.of(folderName.split(Constants.FORWARD_SLASH)));
        if (folder == null) {
            return Constants.INCORRECT_FOLDER_NAME;
        }
        return email.moveToFolder(folder);
    }

    @CatalogRequest(
            id = "localDomainRequest_02494c43-a71f-47bb-935a-37736a4ecac0",
            name = "Reply To All With CC and BCC",
            description = "In this request, the user can respond to everyone on the thread. Other recipients will see a message user 'Reply All' to, whether they're in the 'To' or 'Cc' fields. 'To', Cc' and 'Bcc' fields are optional to update the existing users.",
            area = "Messaging",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Boolean(name = "Is Successful", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    public Boolean replyToAllWithCCAndBCC(
            @Field(name = "Message Id", type = "Text") String messageId,
            @Field(name = "To", type = "Text", required = false) String to,
            @Field(name = "Cc", type = "Text", required = false) String cc,
            @Field(name = "Bcc", type = "Text", required = false) String bcc,
            @Field(name = "Reply To", type = "Text", required = false) String replyTo,
            @Field(name = "Message", type = "RichText", required = false) String message,
            @Field.File(name = "Attachments", multipleFileUpload = true, required = false) List<File> attachments,
            @Field.PickOne(name = "BodyType", values = {"Text", "HTML"}, required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) String bodyType) {

        logger.info("replyToAll: messageId: {}; message: {}", messageId, message);
        try {
            if (attachments != null) {
                logger.info("replyToAll attachments: {}", ReflectionToStringBuilder.toString(attachments));
            }

            Email email = account.getEmail(messageId);
            if (email == null) {
                logger.error("Invalid message id");
                return false;
            }
            bodyType = (bodyType == null) ? Constants.HTML : bodyType;
            message = EntityHelperUtil.formattedMessage(message, bodyType);
            logger.info("Sending message {}", message);
            email.replyToAll(message, mailHandler.toAttachment(attachments), toEmailAddresses(to), toEmailAddresses(cc),
                    toEmailAddresses(bcc), toEmailAddresses(replyTo), bodyType);
            return true;
        } catch (GraphServiceException graphServiceException) {
            String errorMessage = graphServiceException.getMessage();
            if (errorMessage != null && errorMessage.contains(Constants.ONE_INVALID_MAIL)) {
                throw new IllegalArgumentException(Constants.INVALID_MAIL_ADDRESS, graphServiceException.getCause());
            }
            throw new InternalServerErrorException(Constants.REPLY_TO_ALL_REQUEST_FAILED, graphServiceException.getCause());
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_19b1a828-2f61-49e7-a75a-9956f7d12c5c",
            name = "Reply To All",
            description = "In this request, the user can respond to everyone on the thread. Other recipients will see a message user 'Reply All' to, whether they're in the 'To' or 'Cc' fields.",
            area = "Messaging",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Boolean(name = "Is Successful", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    public Boolean replyToAll(
            @Field(name = "Message Id", type = "Text") String messageId,
            @Field(name = "Message", type = "RichText", required = false) String message,
            @Field.File(name = "Attachments", multipleFileUpload = true, required = false) List<File> attachments,
            @Field.PickOne(name = "BodyType", values = {"Text", "HTML"}, required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) String bodyType) {

        try {
            logger.info("replyToAll: messageId: {}; message: {}", messageId, message);
            if (attachments != null) {
                logger.info("replyToAll attachments: {}", ReflectionToStringBuilder.toString(attachments));
            }

            Email email = account.getEmail(messageId);
            if (email == null) {
                logger.debug(Constants.INVALID_MESSAGE_ID);
                return false;
            }
            bodyType = (bodyType == null) ? Constants.HTML : bodyType;
            message = EntityHelperUtil.formattedMessage(message, bodyType);
            logger.info("Sending message {}", message);
            email.replyToAll(message, mailHandler.toAttachment(attachments), bodyType);
            return true;
        } catch (GraphServiceException graphServiceException) {
            String errorMessage = graphServiceException.getMessage();
            if (errorMessage != null && errorMessage.contains(Constants.ONE_INVALID_MAIL)) {
                throw new IllegalArgumentException(Constants.INVALID_MAIL_ADDRESS,
                        graphServiceException.getCause());
            }
            throw new InternalServerErrorException(Constants.REPLY_TO_ALL_REQUEST_FAILED, graphServiceException.getCause());
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_cf7ac51e-752b-44e8-a55a-2a5dd4dbfbef",
            name = "Fetch Sent",
            description = "Accepts page number, and page size as input and returns list of mails from sent folder. Page number, and page size are optional parameters.",
            area = "Messaging",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Desc(name = "Sent Mails", type = "[ Entity(Mail Details) ]", required = false)
    public List<MailDetails> fetchSent(
            @Field(name = "Page Number", type = "Number", required = false) Double pageNumber,
            @Field(name = "Page Size", type = "Number", required = false) Double pageSize) {

        logger.info("fetchSent: pageNumber: {}; pageSize: {}", pageNumber, pageSize);

        List<Email> emails = account.getSentFolder().getEmails(pageNumber, pageSize);
        return emails.stream().map(email -> mailHandler.fromEmail(email, null)).collect(Collectors.toList());
    }

    @CatalogRequest(
            id = "localDomainRequest_7519e728-cca1-447f-8c58-ad4e80eefb00",
            name = "Forward Mail",
            description = "This request allows a sender to forward the received email to other recipients.",
            area = "Messaging",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field(name = "Is Forwarded", type = "Switch", required = false)
    public Boolean forwardMail(
            @Field(name = "Message Id", type = "Text") String messageId,
            @Field(name = "To", type = "Text") String to,
            @Field(name = "Message", type = "RichText", required = false) String message,
            @Field.PickOne(name = "BodyType", values = {"Text", "HTML"}, required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) String bodyType) {

        try {
            logger.info("Forwarding mail with messageId: {}; to {}; with message: {}", messageId, to, message);

            Email email = account.getEmail(messageId);
            logger.info("email: {}", email);

            if (email == null) {
                logger.error(Constants.INVALID_MESSAGE_ID + ": {}", messageId);
                return false;
            }
            bodyType = (bodyType == null) ? Constants.HTML : bodyType;
            message = EntityHelperUtil.formattedMessage(message, bodyType);
            logger.info("Sending message {}", message);
            email.forward(message, toEmailAddresses(to), bodyType);
            return true;
        } catch (GraphServiceException graphServiceException) {
            String errorMessage = graphServiceException.getMessage();
            if (errorMessage != null && errorMessage.contains(Constants.ONE_INVALID_MAIL)) {
                throw new IllegalArgumentException(Constants.INVALID_MAIL_ADDRESS,
                        graphServiceException.getCause());
            }
            throw new InternalServerErrorException(Constants.FORWARD_MAIL_REQUEST_FAILED, graphServiceException.getCause());
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_45b7479d-c009-4b0f-85e6-0fe38d9fc35d",
            name = "Fetch Mail Details By Query",
            description = "Accepts search query as input and returns list of mails",
            area = "Messaging",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Desc(name = "Mails", type = "[ Entity(Mail Details) ]", required = false)
    public List<MailDetails> fetchMailDetailsByQuery(
            @Field(name = "Query", type = "Text") String query) {

        logger.info("fetchMailDetailsByQuery: {}", query);

        List<Email> emails = account.searchEmails(query);
        return emails.stream().map(email -> mailHandler.fromEmail(email, null)).collect(Collectors.toList());
    }

    @CatalogRequest(
            id = "localDomainRequest_6d34be22-e420-4087-b55d-0659f899b140",
            name = "Send Mail",
            description = "Accepts subject, message, attachments, to, bcc, cc, reply to as input and returns response message. Attachments, bcc, cc, and reply to are optional inputs.",
            area = "Messaging",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field(name = "Message", type = "Text", required = false)
    public String sendMail(
            @Field(name = "Subject", type = "Text") String subject,
            @Field(name = "Message", type = "RichText") String message,
            @Field(name = "Attachments", type = "File", required = false) List<File> attachments,
            @Field(name = "To", type = "Text") String to,
            @Field(name = "Bcc", type = "Text", required = false) String bcc,
            @Field(name = "Cc", type = "Text", required = false) String cc,
            @Field(name = "Reply To", type = "Text", required = false) String replyTo,
            @Field.PickOne(name = "BodyType", values = {"Text", "HTML"}, required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) String bodyType) {
        try {
            logger.info("Sending email to {}; with body: {}", to, message);

            EmailBuilder builder = account.newEmail();
            builder.withText(subject);
            bodyType = (bodyType == null) ? Constants.HTML : bodyType;
            message = EntityHelperUtil.formattedMessage(message, bodyType);
            logger.info("Sending message {}", message);
            builder.withContent(bodyType, message);
            builder.withTo(toEmailAddresses(to));
            builder.withCc(toEmailAddresses(cc));
            builder.withBcc(toEmailAddresses(bcc));
            builder.withReplyTo(toEmailAddresses(replyTo));
            if (attachments != null && !attachments.isEmpty()) {
                builder.withAttachment(mailHandler.toAttachment(attachments));
            }

            logger.info("Sending email: " + ReflectionToStringBuilder.toString(builder));

            builder.send();
            return Constants.SUCCESS;
        } catch (GraphServiceException graphServiceException) {
            String errorMessage = graphServiceException.getMessage();
            if (errorMessage != null && errorMessage.contains(Constants.ONE_INVALID_MAIL)) {
                throw new IllegalArgumentException(Constants.INVALID_MAIL_ADDRESS, graphServiceException.getCause());
            }
            throw new InternalServerErrorException(Constants.SEND_MAIL_REQUEST_FAILED, graphServiceException.getCause());
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_8a3966c3-8d95-4ebe-a294-4458200f3392",
            name = "Send Mail With Table",
            description = "Accepts subject, message, attachments, to, bcc, cc, List of Entities, reply to as input and returns response message. Attachments, bcc, cc, and reply to are optional inputs.",
            area = "Messaging",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Text(name = "Message", required = false, attributes = {@Attribute(name = "visualWidth", value = "M")})
    public String sendMailWithTable(
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
            logger.info("Sending email to {}; with body: {}", to, message);

            EmailBuilder builder = account.newEmail();
            builder.withText(subject);
            String content = EntityHelperUtil.getMessageContent(message, entityList, removeEntityFieldFromTable);
            builder.withContent(Constants.HTML, content);
            builder.withTo(toEmailAddresses(to));
            builder.withCc(toEmailAddresses(cc));
            builder.withBcc(toEmailAddresses(bcc));
            builder.withReplyTo(toEmailAddresses(replyTo));
            if (attachments != null && !attachments.isEmpty()) {
                builder.withAttachment(mailHandler.toAttachment(attachments));
            }
            builder.send();
            return Constants.SUCCESS;
        } catch (GraphServiceException cause) {
            String errorMessage = cause.getMessage();
            if (errorMessage != null && errorMessage.contains(Constants.ONE_INVALID_MAIL)) {
                throw new IllegalArgumentException(Constants.INVALID_MAIL_ADDRESS, cause.getCause());
            }
            throw new InternalServerErrorException(Constants.SEND_MAIL_REQUEST_FAILED, cause.getCause());
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_bbdc1184-9dc1-4448-8bdb-ec6c9ee913a7",
            name = "Fetch Inbox",
            description = "Accepts page number, and page size as input and returns list of mail. Page number, and page size are optional parameters.",
            area = "Messaging",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Desc(name = "Inbox Mails", type = "[ Entity(Mail Details) ]", required = false)
    public List<MailDetails> fetchInbox(
            @Field(name = "Page Number", type = "Number", required = false) Double pageNumber,
            @Field(name = "Page Size", type = "Number", required = false) Double pageSize) {
        logger.info("fetchInbox: pageNumber: {}; pageSize: {}", pageNumber, pageSize);
        List<Email> emails = account.getInboxFolder(null, null).getEmails(pageNumber, pageSize);
        return emails.stream().map(email -> mailHandler.fromEmail(email, null)).collect(Collectors.toList());
    }

    @CatalogRequest(
            id = "localDomainRequest_20716cd4-a8a9-43ab-ae5c-4bef25ed4623",
            name = "Mark Message",
            description = "Accepts message ID, and label as input and mark mail as read/unread and returns response message",
            area = "Messaging",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field(name = "Response", type = "Text", required = false)
    public String markMessage(
            @Field(name = "Message ID", type = "Text") String messageID,
            @Field.Desc(name = "Label", type = "PickOne(Read|Unread)") String label) {

        try {
            Email email = account.getEmail(messageID);
            if (email == null) {
                throw new RuntimeException("Unable to mark message, no email found with messageID {}" + messageID);
            }
            if (label.equalsIgnoreCase(Constants.READ)) {
                email.markAsRead();
                logger.info("Marked message {} for messageID {}", label, messageID);
            } else if (label.equalsIgnoreCase(Constants.UNREAD)) {
                email.markAsUnread();
                logger.info("Marked message {} for messageID {}", label, messageID);
            } else {
                throw new RuntimeException("Unable to mark message (un)read invalid label " + label + " for messageID: " + messageID);
            }
            return Constants.SUCCESS;
        } catch (GraphServiceException graphServiceException) {
            throw new IllegalArgumentException(Constants.MARK_MESSAGE_REQUEST_FAILED, graphServiceException.getCause());
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_37baf692-3fd8-424d-afc4-63bdbe6de2f4",
            name = "Reply To Mail With CC and BCC",
            description = "Accepts message ID, message, attachments, cc, bcc and reply to as inputs and returns response message.",
            area = "Messaging",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field(name = "Message", type = "Text", required = false, attributes = {}, options = {})
    public String replyToMailWithCCAndBCC(
            @Field(name = "Message Id", type = "Text") String messageId,
            @Field(name = "Message", type = "RichText", required = false) String message,
            @Field.File(name = "Attachments", multipleFileUpload = true, required = false) List<File> attachments,
            @Field(name = "To", type = "Text", required = false) String to,
            @Field(name = "Cc", type = "Text", required = false) String cc,
            @Field(name = "Bcc", type = "Text", required = false) String bcc,
            @Field(name = "Reply To", type = "Text", required = false) String replyTo,
            @Field.PickOne(name = "BodyType", values = {"Text", "HTML"}, required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) String bodyType) {
        logger.info("markMessage: messageID: {}; category: {}", messageId, bodyType);
        try {
            if (attachments != null) {
                logger.info("replyToMailWithCCAndBCC attachments: {}", ReflectionToStringBuilder.toString(attachments));
            }
            Email email = account.getEmail(messageId);
            if (email == null) {
                throw new IllegalArgumentException(Constants.INVALID_MESSAGE_ID);
            }
            bodyType = (bodyType == null) ? Constants.HTML : bodyType;
            message = EntityHelperUtil.formattedMessage(message, bodyType);
            logger.info("Sending message {}", message);
            email.replyText(message, mailHandler.toAttachment(attachments), toEmailAddresses(to), toEmailAddresses(cc),
                    toEmailAddresses(bcc), toEmailAddresses(replyTo), bodyType);
            return Constants.SUCCESS;
        } catch (GraphServiceException graphServiceException) {
            String errorMessage = graphServiceException.getMessage();
            if (errorMessage != null && errorMessage.contains(Constants.ONE_INVALID_MAIL)) {
                throw new IllegalArgumentException(Constants.INVALID_MAIL_ADDRESS, graphServiceException.getCause());
            }
            throw new InternalServerErrorException(Constants.REPLY_TO_MAIL_REQUEST_FAILED, graphServiceException.getCause());
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_c8485079-5592-4d29-9818-41d99368a35d",
            name = "Reply To Mail",
            description = "Accepts message ID, message, and attachments as input and returns response message. Attachment is optional input.",
            area = "Messaging",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field(name = "Message", type = "Text", required = false)
    public String replyToMail(
            @Field(name = "Message ID", type = "Text") String messageID,
            @Field(name = "Message", type = "RichText", required = false) String message,
            @Field(name = "Attachments", type = "File", required = false) List<File> attachments,
            @Field.PickOne(name = "BodyType", values = {"Text", "HTML"}, required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) String bodyType) {

        logger.info("replyToMail: messageID: {}; message: {}", messageID, message);
        if (attachments != null) {
            logger.info("replyToMail attachments: {}", ReflectionToStringBuilder.toString(attachments));
        }

        try {
            Email email = account.getEmail(messageID);
            if (email == null) {
                return Constants.INVALID_MESSAGE_ID;
            }
            bodyType = (bodyType == null) ? Constants.HTML : bodyType;
            message = EntityHelperUtil.formattedMessage(message, bodyType);
            logger.info("Sending message {}", message);
            email.replyText(message, mailHandler.toAttachment(attachments), bodyType);
            return Constants.SUCCESS;
        } catch (GraphServiceException graphServiceException) {
            String errorMessage = graphServiceException.getMessage();
            if (errorMessage != null && errorMessage.contains(Constants.ONE_INVALID_MAIL)) {
                throw new IllegalArgumentException(Constants.INVALID_MAIL_ADDRESS,
                        graphServiceException.getCause());
            }
            throw new InternalServerErrorException(Constants.REPLY_TO_MAIL_REQUEST_FAILED, graphServiceException.getCause());
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
        final String taskId = UUID.randomUUID().toString();
        boolean useEmail = !requestContext.invokeAsUser();
        String accountID = authorizationContext.getAuthorizedAccount().getAccountId();
        executorService.submit(() -> {
            try {
                List<Email> emails = account.getInboxFolder(useEmail, accountID).getEmails(useEmail);
                List<MailDetails> mailDetails = emails.stream().map(email -> mailHandler.fromEmail(email, useEmail))
                        .collect(Collectors.toList());
                FreeForm freeForm = new FreeForm();
                freeForm.put(Constants.DATA, "[ Entity(Mail Details) ]", mailDetails);
                eventHandler.handleEvent(taskId, freeForm);
            } catch (Exception cause) {
                throw new IllegalStateException(cause);
            }
        });
        return taskId;
    }

    @SuppressWarnings("unchecked")
    @CatalogRequest(
            id = "localDomainRequest_0643ea55-dbbb-4f47-9136-9e523c3eadc9",
            name = "Get Result",
            description = "Accept task ID as input and return mails. Get this task ID from fetchInboxAsync request",
            area = "Messaging",
            type = CatalogRequest.Type.WAIT_FOR_EVENT)
    @Field.Desc(name = "Mail Details", type = "[ Entity(Mail Details) ]", required = false)
    public List<MailDetails> getResult(
            @Field(name = "eventName", type = "Text") String eventName,
            @Field(name = "eventData", type = "FreeForm") FreeForm eventData,
            @Field(name = "Task ID", type = "Text") String taskID) {

        logger.info("getResult: eventName: {}; eventData: {}; taskID: {}", eventName, eventData, taskID);
        if (eventName.equals(taskID)) {
            return (List<MailDetails>) eventData.get(Constants.DATA);
        }
        logger.error("Invalid task ID: {}", taskID);
        throw new IllegalStateException(Constants.INVALID_TASK_ID);
    }

    @CatalogRequest(
            id = "localDomainRequest_82e8a567-a80f-4ff0-876b-8bc14072f322",
            name = "Fetch Mails By Label",
            description = "Accepts label, page number, and page size as input and returns list of mail. Page number, and page size are optional input.",
            area = "Messaging",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Desc(name = "Mails", type = "[ Entity(Mail Details) ]", required = false)
    public List<MailDetails> fetchMailsByLabel(
            @Field(name = "Label", type = "Text") String label,
            @Field(name = "Page Number", type = "Number", required = false) Double pageNumber,
            @Field(name = "Page Size", type = "Number", required = false) Double pageSize) {
        logger.info("fetchMailsByLabel: label: {}, pageNumber: {}; pageSize: {}", label, pageNumber, pageSize);
        try {
            Folder folder = account.getFolderByName(List.of(label.split(Constants.FORWARD_SLASH)));
            if (folder == null) {
                logger.error(Constants.FETCH_MAIL_FAILED_NO_FOLDER);
                return List.of();
            }
            List<Email> emails = folder.getEmails(pageNumber, pageSize);
            return emails.stream().map(email -> mailHandler.fromEmail(email, null)).collect(Collectors.toList());
        } catch (GraphServiceException graphServiceException) {
            logger.error(Constants.FETCH_MAIL_FAILED_NO_FOLDER + graphServiceException.getCause(), graphServiceException);
            return List.of();
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_d0e3cd12-9a47-40de-a213-a0d0c1ba0507",
            name = "Mail Received Alert",
            description = "Mail Received Alert",
            area = "Messaging",
            type = CatalogRequest.Type.WAIT_FOR_EVENT)
    @Field.Desc(name = "Mail Details", type = "Entity(Mail Details)", required = false)
    public MailDetails mailReceivedAlert(
            @Field(name = "eventName", type = "Text") String eventName,
            @Field(name = "eventData", type = "FreeForm") FreeForm eventData) {
        if (eventName.equalsIgnoreCase(Constants.MAIL_RECEIVED)) {
            return fetchMailByMessageId((String) eventData.get(Constants.MESSAGE_ID));
        }
        return null;
    }

    @CatalogRequest(
            id = "localDomainRequest_90b24da6-d02f-4fcb-9632-ef8e6ae1550a",
            name = "Fetch Latest Mail",
            description = "Returns the latest email received, in the last two minutes",
            area = "Messaging",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Desc(name = "New Email", type = "Entity(Mail Details)", required = false)
    public MailDetails fetchLatestMail() {
        logger.info("fetchLatestMail: start");
        List<MailDetails> mailDetailsList = fetchInbox(1.0, 1.0);
        MailDetails mailDetails = mailDetailsList.isEmpty() ? null : mailDetailsList.get(0);
        if (mailDetails != null && mailDetails.sendDateAndTime != null) {
            long change = System.currentTimeMillis() - mailDetails.sendDateAndTime;
            if (change <= 120_000) {
                return mailDetails;
            }
        }
        return null;
    }

    @CatalogRequest(
            id = "localDomainRequest_5d7cf884-e31b-4806-ac68-b6f0c767585e",
            name = "List Categories",
            description = "Get a list of the supported Outlook categories",
            area = "Messaging",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Desc(name = "Category Names", type = "[ Text ]", required = false)
    public List<String> listCategories() {
        logger.debug("listCategories(): start");
        List<String> categoryNames = null;

        try {
            categoryNames = account.getCategoryNames();
            logger.debug("listCategories(): {}", categoryNames);
        } catch (Exception cause) {
            String userFacingErrorMessage = new StringBuilder()
                    .append("Unable to get a list of category names")
                    .toString();
            logger.error(userFacingErrorMessage + "; " + cause.getMessage(), cause);
            throw new RuntimeException(userFacingErrorMessage, cause);
        }
        return categoryNames;
    }

    @CatalogRequest(
            id = "localDomainRequest_e35e5d82-b464-4fe4-875a-33f0dfe48265",
            name = "Add Category To Message",
            description = "This request will add category to the given mail ID.",
            area = "Messaging",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Boolean(name = "Category Added", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    public Boolean addCategoryToMessage(
            @Field.Text(name = "Message ID", required = true, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) String messageID,
            @Field.Text(name = "Category", required = true, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) String category) {
        if (Validators.isStringNullOrBlank(category)) {
            throw new RuntimeException("Category cannot be empty.");
        }
        if (Validators.isStringNullOrBlank(messageID)) {
            throw new RuntimeException("Message ID cannot be empty.");
        }
        Email email = account.getEmail(messageID);
        return email.addCategory(category);
    }

    @CatalogRequest(
            id = "localDomainRequest_6cdb6cf3-4ca6-46f0-a682-75be3bd37c98",
            name = "Remove Category From Message",
            description = "This request will remove the given category for the given Message Id",
            area = "Messaging",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Boolean(name = "Category Removed", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    public Boolean removeCategoryFromMessage(
            @Field.Text(name = "Message ID", required = true, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) String messageID,
            @Field.Text(name = "Category", required = true, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) String category) {
        if (Validators.isStringNullOrBlank(category)) {
            throw new RuntimeException("Category cannot be empty.");
        }
        if (Validators.isStringNullOrBlank(messageID)) {
            throw new RuntimeException("Message ID cannot be empty.");
        }
        Email email = account.getEmail(messageID);
        return email.removeCategory(category);
    }

}
