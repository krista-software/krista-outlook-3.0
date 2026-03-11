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

package app.krista.extensions.essentials.collaboration.outlook3.impl;

import app.krista.extensions.essentials.collaboration.outlook3.impl.connectors.GraphServiceClientProvider;
import app.krista.extensions.essentials.collaboration.outlook3.impl.connectors.GraphServiceClientProviderFactory;
import app.krista.extensions.essentials.collaboration.outlook3.impl.util.Constants;
import app.krista.extensions.essentials.collaboration.outlook3.impl.util.Validators;
import app.krista.extensions.essentials.collaboration.outlook3.service.Attachment;
import app.krista.extensions.essentials.collaboration.outlook3.service.Email;
import app.krista.extensions.essentials.collaboration.outlook3.service.EmailAddress;
import app.krista.extensions.essentials.collaboration.outlook3.service.Folder;
import com.microsoft.graph.http.GraphServiceException;
import com.microsoft.graph.models.*;
import com.microsoft.graph.options.HeaderOption;
import com.microsoft.graph.requests.AttachmentCollectionPage;
import com.microsoft.graph.requests.MessageRequestBuilder;
import com.microsoft.graph.requests.UserRequestBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.InternalServerErrorException;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

import static app.krista.extensions.essentials.collaboration.outlook3.impl.util.Constants.*;

public class EmailImpl implements Email {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailImpl.class);
    private GraphServiceClientProvider provider;
    private GraphServiceClientProviderFactory providerFactory;
    private final Message message;

    public EmailImpl(GraphServiceClientProviderFactory factory, Message message) {
        this.providerFactory = factory;
        this.message = message;
    }

    @Inject
    public EmailImpl(GraphServiceClientProvider provider, Message message) {
        this.provider = provider;
        this.message = message;
    }

    public GraphServiceClientProvider getProvider() {
        if (provider == null) {
            this.provider = providerFactory.create();
        }
        return provider;
    }

    @Override
    public String getEmailId() {
        return message.id;
    }

    @Override
    public String getSubject() {
        return message.subject;
    }

    @Override
    public EmailAddress getSenderEmailAddress() {
        return message.from != null ? toEmailAddress(message.from) : null;
    }

    @Override
    public List<EmailAddress> getToEmailAddresses() {
        return message.toRecipients != null ? message.toRecipients.stream().map(this::toEmailAddress).collect(Collectors.toList()) : null;
    }

    @Override
    public List<EmailAddress> getReplyToEmailAddresses() {
        return message.replyTo != null ? message.replyTo.stream().map(this::toEmailAddress).collect(Collectors.toList()) : null;
    }

    @Override
    public List<EmailAddress> getCcEmailAddresses() {
        return message.ccRecipients != null ? message.ccRecipients.stream().map(this::toEmailAddress).collect(Collectors.toList()) : null;
    }

    @Override
    public List<EmailAddress> getBccEmailAddresses() {
        return message.bccRecipients != null ? message.bccRecipients.stream().map(this::toEmailAddress).collect(Collectors.toList()) : null;
    }

    @Override
    public Boolean getRead() {
        return message.isRead;
    }

    @Override
    public Long getSendDateAndTime() {
        return message.sentDateTime != null ? message.sentDateTime.toEpochSecond() * 1000 : null;
    }

    @Override
    public Long getReceivedDateAndTime() {
        return message.receivedDateTime != null ? message.receivedDateTime.toEpochSecond() * 1000 : null;
    }

    @Override
    public String getContentType() {
        if (message.body != null) {
            return (message.body.contentType != null) ? message.body.contentType.name() : null;
        } else {
            return null;
        }
    }

    @Override
    public String getContent() {
        String content = message.body != null ? message.body.content : null;
        if (content != null) {
            Document document = Jsoup.parse(content);
            document.outputSettings(new Document.OutputSettings().prettyPrint(false));
            document.select("br").append("\\n");
            document.select("p").prepend("\\n\\n");
            document.select("img").remove();
            content = document.html().replaceAll("\\\\n", "\n");
        }
        return content;
    }

    @Override
    public void markAsRead() {
        markMessage(true);
    }

    @Override
    public void markAsUnread() {
        markMessage(false);
    }

    private void markMessage(boolean isRead) {
        Message markMessage = new Message();
        markMessage.isRead = isRead;
        MessageRequestBuilder messageRequestBuilder = getMessageRequestBuilder(null);
        if (messageRequestBuilder != null) {
            Message markedMessage = messageRequestBuilder.buildRequest().patch(markMessage);
            if (markedMessage == null) {
                throw new IllegalStateException(Constants.FAILED_TO_MARK_THE_MESSAGE_AS_READ);
            }
        } else {
            throw new IllegalStateException(Constants.FAILED_TO_MARK_THE_MESSAGE_AS_READ);
        }
    }

    @Override
    public String moveToFolder(Folder folder) {
        Objects.requireNonNull(folder);
        MessageRequestBuilder messageRequestBuilder = getMessageRequestBuilder(null);
        if (messageRequestBuilder == null) {
            throw new IllegalStateException(Constants.FAILED_TO_MOVE_MESSAGE);
        }

        MessageMoveParameterSet parameterSet = new MessageMoveParameterSet();
        parameterSet.destinationId = folder.getFolderId();
        Message movedMessage = messageRequestBuilder.move(parameterSet).buildRequest(new HeaderOption(Constants.PREFER, Constants.BODY_CONTENT_TYPE_HTML)).post();
        if (movedMessage == null) {
            throw new IllegalStateException(Constants.FAILED_TO_MOVE_MESSAGE);
        } else {
            return movedMessage.id;
        }
    }

    @Override
    public void replyText(String message, List<com.microsoft.graph.models.Attachment> attachments, String bodyType) {
        replyText(message, attachments, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), bodyType);

    }

    @Override
    public Email replyText(String message, List<com.microsoft.graph.models.Attachment> attachments, List<EmailAddress> toRecipients, List<EmailAddress> ccRecipients, List<EmailAddress> bccRecipients, List<EmailAddress> replyTo, String bodyType) {
        try {
            LOGGER.info("Getting message request builder for messageId: {}", this.message.id);

            MessageRequestBuilder messageRequestBuilder = getMessageRequestBuilder(null);
            if (messageRequestBuilder == null) {
                LOGGER.error("MessageRequestBuilder is null for messageId: {}", this.message.id);
                throw new IllegalStateException(Constants.REPLY_TO_MAIL_REQUEST_FAILED);
            }

            Message replyMessage = setReplyMessageValues(message, attachments, toRecipients, ccRecipients, bccRecipients, replyTo, bodyType);

            LOGGER.info("Reply message created successfully for messageId: {}, replyMessageBody: {}",
                    this.message.id, replyMessage.body != null ? replyMessage.body.content : "null");

            LOGGER.info("Calling Graph API reply for messageId: {}", this.message.id);
            messageRequestBuilder.reply(MessageReplyParameterSet.newBuilder().withMessage(replyMessage).build()).buildRequest().post();
            Email resultEmail = new EmailImpl(getProvider(), replyMessage);
            LOGGER.info("Created result EmailImpl for messageId: {}, resultEmailId: {}",
                    this.message.id, resultEmail.getEmailId());

            return resultEmail;
        } catch (GraphServiceException graphServiceException) {
            String errorMessage = graphServiceException.getMessage();
            LOGGER.error("GraphServiceException in replyText - messageId: {}, error: {}, statusCode: {}",
                    this.message.id, errorMessage, graphServiceException.getResponseCode(), graphServiceException);

            if (errorMessage != null && errorMessage.contains(Constants.ONE_INVALID_MAIL)) {
                LOGGER.error("Invalid mail address detected in replyText - messageId: {}", this.message.id);
                throw new IllegalArgumentException(Constants.INVALID_MAIL_ADDRESS, graphServiceException.getCause());
            }

            LOGGER.error("General GraphServiceException in replyText - messageId: {}", this.message.id);
            graphServiceException.printStackTrace();
            throw new InternalServerErrorException(Constants.REPLY_TO_MAIL_REQUEST_FAILED, graphServiceException.getCause());

        } catch (Exception cause) {
            LOGGER.error("Unexpected exception in replyText - messageId: {}, error: {}, errorType: {}",
                    this.message.id, cause.getMessage(), cause.getClass().getSimpleName(), cause);
            cause.printStackTrace();
            throw cause;
        }
    }

    @Override
    public Email replyToAll(String message, List<com.microsoft.graph.models.Attachment> attachments, List<EmailAddress> toRecipients, List<EmailAddress> ccRecipients, List<EmailAddress> bccRecipients, List<EmailAddress> replyTo, String bodyType) {
        LOGGER.info("Starting replyToAll - messageId: {}, bodyType: {}, attachmentCount: {}, toCount: {}, ccCount: {}, bccCount: {}, replyToCount: {}, messageLength: {}",
                this.message.id, bodyType,
                attachments != null ? attachments.size() : 0,
                toRecipients != null ? toRecipients.size() : 0,
                ccRecipients != null ? ccRecipients.size() : 0,
                bccRecipients != null ? bccRecipients.size() : 0,
                replyTo != null ? replyTo.size() : 0,
                message != null ? message.length() : 0);

        try {
            LOGGER.info("Getting message request builder for messageId: {}", this.message.id);
            MessageRequestBuilder messageRequestBuilder = getMessageRequestBuilder(null);
            if (messageRequestBuilder == null) {
                LOGGER.error("MessageRequestBuilder is null for messageId: {}", this.message.id);
                throw new IllegalStateException(Constants.REPLY_TO_ALL_REQUEST_FAILED);
            }

            LOGGER.info("Setting reply message values for messageId: {}", this.message.id);
            Message replyMessage = setReplyMessageValues(message, attachments, toRecipients, ccRecipients, bccRecipients, replyTo, bodyType);
            messageRequestBuilder.replyAll(MessageReplyAllParameterSet.newBuilder().withMessage(replyMessage).build()).buildRequest().post();
            if (getProvider() == null || getProvider().getOutlookAttributes() == null) {
                LOGGER.error("Provider or OutlookAttributes is null for messageId: {}", this.message.id);
            }
            return new EmailImpl(getProvider(), replyMessage);
        } catch (GraphServiceException graphServiceException) {
            String errorMessage = graphServiceException.getMessage();
            if (errorMessage != null && errorMessage.contains(Constants.ONE_INVALID_MAIL)) {
                throw new IllegalArgumentException(Constants.INVALID_MAIL_ADDRESS, graphServiceException.getCause());
            }
            throw new InternalServerErrorException(Constants.REPLY_TO_ALL_REQUEST_FAILED, graphServiceException.getCause());
        }
    }

    @Override
    public void replyToAll(String message, List<com.microsoft.graph.models.Attachment> attachments, String bodyType) {
        replyToAll(message, attachments, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), bodyType);
    }

    @NotNull
    private Message setReplyMessageValues(String message, List<com.microsoft.graph.models.Attachment> attachments, List<EmailAddress> toRecipients, List<EmailAddress> ccRecipients, List<EmailAddress> bccRecipients, List<EmailAddress> replyTo, String bodyType) {
        ItemBody body = new ItemBody();
        body.content = message;
        body.contentType = ("HTML".equals(bodyType)) ? BodyType.HTML : BodyType.TEXT;
        Message replyMessage = new Message();
        replyMessage.body = body;
        replyMessage.attachments = new AttachmentCollectionPage(attachments, null);
        updateRecipients(toRecipients, ccRecipients, bccRecipients, replyTo, replyMessage);
        return replyMessage;
    }

    private void updateRecipients(List<EmailAddress> toRecipients, List<EmailAddress> ccRecipients, List<EmailAddress> bccRecipients, List<EmailAddress> replyTo, Message replyMessage) {
        if (!toRecipients.isEmpty()) {
            replyMessage.toRecipients = getRecipientsCollection(toRecipients);
        }
        if (ccRecipients != null && !ccRecipients.isEmpty()) {
            replyMessage.ccRecipients = getRecipientsCollection(ccRecipients);
        }
        if (bccRecipients != null && !bccRecipients.isEmpty()) {
            replyMessage.bccRecipients = getRecipientsCollection(bccRecipients);
        }
        if (replyTo != null && !replyTo.isEmpty()) {
            replyMessage.replyTo = getRecipientsCollection(replyTo);
        }
    }

    @NotNull
    private List<Recipient> getRecipientsCollection(List<EmailAddress> toRecipients) {
        return toRecipients.stream().map(this::toRecipient).collect(Collectors.toList());
    }


    @Override
    public void forward(String message, List<EmailAddress> to, String bodyType) {
        LOGGER.info("Forwarding email to {} with message {}", to, message);
        MessageRequestBuilder messageRequestBuilder = getMessageRequestBuilder(null);
        if (messageRequestBuilder == null) {
            throw new IllegalStateException(Constants.FORWARD_MAIL_REQUEST_FAILED);
        }

        if (Validators.isListNullOrEmpty(to)) {
            LOGGER.debug(Constants.RECIPIENT_IS_EMPTY_OR_NULL);
            throw new IllegalArgumentException(Constants.RECIPIENT_IS_EMPTY_OR_NULL);
        }

        ItemBody body = createItemBody(message, bodyType);
        Message replyMessage = createReplyMessage(body);
        List<Recipient> toRecipient = getRecipientsCollection(to);

        messageRequestBuilder
                .forward(MessageForwardParameterSet.newBuilder().withToRecipients(toRecipient)
                        .withMessage(replyMessage).build())
                .buildRequest().post();
    }

    private Message createReplyMessage(ItemBody body) {
        Message replyMessage = new Message();
        replyMessage.body = body;
        return replyMessage;
    }

    private ItemBody createItemBody(String message, String bodyType) {
        ItemBody body = new ItemBody();
        body.content = (message == null || message.isBlank()) ? "" : message;
        body.contentType = ("HTML".equals(bodyType)) ? BodyType.HTML : BodyType.TEXT;

        return body;
    }

    @Override
    public List<Attachment> getFileAttachments(Boolean useEmail) {
        List<Attachment> attachmentList = new ArrayList<>();
        MessageRequestBuilder messageRequestBuilder = getMessageRequestBuilder(useEmail);
        if (messageRequestBuilder == null) {
            return attachmentList;
        }

        AttachmentCollectionPage attachments = messageRequestBuilder.attachments().buildRequest().get();
        while (attachments != null && !attachments.getCurrentPage().isEmpty()) {
            attachments.getCurrentPage().forEach(attachment -> {
                if (attachment instanceof FileAttachment) {
                    attachmentList.add(new AttachmentImpl(attachment));
                }
            });
            if (attachments.getNextPage() != null) {
                attachments = attachments.getNextPage().buildRequest().get();
            } else {
                break;
            }
        }
        return attachmentList;
    }

    @Override
    public List<String> getItemAttachments(Boolean useEmail) {
        List<String> attachmentList = new LinkedList<>();
        MessageRequestBuilder messageRequestBuilder = getMessageRequestBuilder(useEmail);
        if (messageRequestBuilder == null) {
            return attachmentList;
        }

        AttachmentCollectionPage attachments = messageRequestBuilder.attachments().buildRequest().get();
        while (attachments != null && !attachments.getCurrentPage().isEmpty()) {
            attachments.getCurrentPage().forEach(attachment -> addAttachmentToAttachmentList(useEmail, attachmentList, attachment));
            if (attachments.getNextPage() != null) {
                attachments = attachments.getNextPage().buildRequest().get();
            } else {
                break;
            }
        }
        return attachmentList;
    }

    @Override
    public List<String> getCategories() {
        return message.categories;
    }

    private void addAttachmentToAttachmentList(Boolean useEmail, List<String> attachmentList, com.microsoft.graph.models.Attachment attachment) {
        if (attachment instanceof ItemAttachment) {
            com.microsoft.graph.models.Attachment attachedMediaItem = getAttachedMediaItem(useEmail, attachment);
            if (attachedMediaItem != null) {
                OutlookItem item = ((ItemAttachment) attachedMediaItem).item;
                var map = Constants.GSON.fromJson(Constants.GSON.toJson(item), Map.class);
                if (map != null && !map.isEmpty() && map.containsKey(Constants.WEB_LINK)) {
                    String webLink = "<a href=\"" + map.get(Constants.WEB_LINK) + "\">" + attachedMediaItem.name + Constants.A_TAG;
                    attachmentList.add(webLink);
                }
            }
        }
    }

    @Nullable
    private com.microsoft.graph.models.Attachment getAttachedMediaItem(Boolean useEmail, com.microsoft.graph.models.Attachment attachment) {
        MessageRequestBuilder messageRequestBuilder = getMessageRequestBuilder(useEmail);
        if (messageRequestBuilder == null) {
            return null;
        }

        return messageRequestBuilder.attachments(Objects.requireNonNull(attachment.id)).buildRequest().expand(Constants.MICROSOFT_GRAPH_ITEM_ATTACHMENT_ITEM).get();
    }

    private MessageRequestBuilder getMessageRequestBuilder(Boolean useEmail) {
        if (message == null) {
            LOGGER.error("Message object is null in getMessageRequestBuilder");
            return null;
        }
        if (message.id == null || message.id.isEmpty()) {
            LOGGER.error("Message id is null or empty - messageId: '{}'", message.id);
            return null;
        }
        LOGGER.info("Getting user request builder - useEmail: {}", useEmail);
        if (getProvider() == null) {
            LOGGER.error("Provider is null in getMessageRequestBuilder");
            return null;
        }
        UserRequestBuilder userRequestBuilder = getProvider().getUserRequestBuilder(useEmail, null);
        if (userRequestBuilder == null) {
            LOGGER.error("UserRequestBuilder is null from provider - useEmail: {}", useEmail);
            return null;
        }
        LOGGER.info("UserRequestBuilder obtained successfully, getting messages builder for messageId: {}", message.id);
        return userRequestBuilder.messages(message.id);
    }

    private EmailAddress toEmailAddress(Recipient recipient) {
        if (recipient == null) {
            LOGGER.debug(Constants.RECIPIENT_IS_EMPTY_OR_NULL);
            return null;
        }
        return recipient.emailAddress != null ? new EmailAddress(recipient.emailAddress.name, recipient.emailAddress.address) : null;
    }

    private Recipient toRecipient(EmailAddress emailAddress) {
        if (emailAddress == null) {
            LOGGER.debug(Constants.EMAIL_ADDRESS_IS_EMPTY_OR_NULL);
            return null;
        }
        Recipient recipient = new Recipient();
        com.microsoft.graph.models.EmailAddress recipientEmailAddress = new com.microsoft.graph.models.EmailAddress();
        recipientEmailAddress.address = emailAddress.getMailAddress();
        recipient.emailAddress = recipientEmailAddress;
        return recipient;
    }

    @Override
    public boolean addCategory(String category) {
        List<String> existingCategories = getUniqueCategories();
        if (!existingCategories.contains(category)) {
            existingCategories.add(category);
        }
        return updateCategory(existingCategories);
    }

    @Override
    public boolean removeCategory(String category) {
        List<String> existingCategories = getUniqueCategories();
        return existingCategories.remove(category) && updateCategory(existingCategories);
    }

    private boolean updateCategory(List<String> existingCategories) {
        Message patchMessage = new Message();
        patchMessage.categories = existingCategories;
        MessageRequestBuilder messageRequestBuilder = getMessageRequestBuilder(null);
        if (messageRequestBuilder != null) {
            try {
                messageRequestBuilder.buildRequest().patch(patchMessage);
                return true;
            } catch (Exception cause) {
                LOGGER.error(cause.getMessage(), cause);
                return false;
            }
        }
        return false;
    }

    @NotNull
    public List<String> getUniqueCategories() {
        List<String> existingCategories = message.categories;
        if (existingCategories == null) {
            existingCategories = new ArrayList<>();
        }
        // Remove duplicates by converting to a Set and back to a List
        Set<String> uniqueCategories = new LinkedHashSet<>(existingCategories);
        existingCategories = new ArrayList<>(uniqueCategories);
        return existingCategories;
    }

    @Override
    public String getConversationId() {
        return message.conversationId;
    }

    @Override
    public String getUniqueBody() {
        String content = (message.uniqueBody != null) ? message.uniqueBody.content : null;
        if (content != null) {
            Document document = Jsoup.parse(content);
            document.outputSettings(new Document.OutputSettings().prettyPrint(false));
            document.select("br").append("\\n");
            document.select("p").prepend("\\n\\n");
            document.select("img").remove();
            return document.html().replaceAll("\\\\n", "\n");
        }
        return null;
    }

    /**
     * Retrieves the sensitivity level of the email message.
     */
    @Override
    public String getSensitivity() {
        var props = (message.singleValueExtendedProperties != null) ? message.singleValueExtendedProperties.getCurrentPage() : null;
        if (props == null) {
            LOGGER.info("No sensitivity property found, defaulting to Normal");
            return NORMAL;
        }
        return props.stream()
                .peek(p -> LOGGER.info("Extended Property - ID: {}, Value: {}", p.id, p.value))
                .filter(p -> p.id != null && p.id.toLowerCase().contains("0x36"))  // Sensitivity field
                .findFirst()
                .map(p -> mapSensitivity(p.value))
                .orElseGet(() -> {
                    LOGGER.info("No sensitivity property found, defaulting to Normal");
                    return NORMAL;
                });
    }

    /**
     * Maps the MAPI sensitivity integer value to a human-readable string representation.
     */
    private String mapSensitivity(String val) {
        return switch (val) {
            case "1" -> PERSONAL;
            case "2" -> PRIVATE;
            case "3" -> CONFIDENTIAL;
            default -> NORMAL;   // val = "0" or unknown
        };
    }
}