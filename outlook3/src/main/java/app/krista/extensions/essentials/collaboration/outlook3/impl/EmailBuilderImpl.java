package app.krista.extensions.essentials.collaboration.outlook3.impl;

import app.krista.extensions.essentials.collaboration.outlook3.impl.connectors.GraphServiceClientProvider;
import app.krista.extensions.essentials.collaboration.outlook3.impl.connectors.GraphServiceClientProviderFactory;
import app.krista.extensions.essentials.collaboration.outlook3.impl.util.Constants;
import app.krista.extensions.essentials.collaboration.outlook3.service.EmailAddress;
import app.krista.extensions.essentials.collaboration.outlook3.service.EmailBuilder;
import com.microsoft.graph.models.*;
import com.microsoft.graph.requests.AttachmentCollectionPage;
import com.microsoft.graph.requests.UserRequestBuilder;

import java.util.List;
import java.util.stream.Collectors;

public class EmailBuilderImpl implements EmailBuilder {
    private final Message message;
    private GraphServiceClientProvider provider;
    private final GraphServiceClientProviderFactory providerFactory;

    public EmailBuilderImpl(GraphServiceClientProvider provider, Message message, GraphServiceClientProviderFactory providerFactory) {
        this.provider = provider;
        this.message = message;
        this.providerFactory = providerFactory;
    }

    public static EmailBuilderImpl create(GraphServiceClientProvider provider) {
        return new EmailBuilderImpl(provider, new Message(), null);
    }

    public GraphServiceClientProvider getProvider() {
        if (provider == null) {
            this.provider = providerFactory.create();
        }
        return provider;
    }

    @Override
    public EmailBuilderImpl withTo(List<EmailAddress> emailAddress) {
        if (!emailAddress.isEmpty()) {
            message.toRecipients = emailAddress.stream().map(this::toRecipient).collect(Collectors.toList());
        }
        return this;
    }

    @Override
    public EmailBuilderImpl withCc(List<EmailAddress> emailAddress) {
        if (!emailAddress.isEmpty()) {
            message.ccRecipients = emailAddress.stream().map(this::toRecipient).collect(Collectors.toList());
        }
        return this;
    }

    @Override
    public EmailBuilderImpl withBcc(List<EmailAddress> emailAddress) {
        if (!emailAddress.isEmpty()) {
            message.bccRecipients = emailAddress.stream().map(this::toRecipient).collect(Collectors.toList());
        }
        return this;
    }

    @Override
    public EmailBuilderImpl withContent(String contentType, String content) {
        message.body = new ItemBody();
        message.body.content = content;
        message.body.contentType = ("HTML".equals(contentType)) ? BodyType.HTML : BodyType.TEXT;
        return this;
    }

    @Override
    public EmailBuilderImpl withText(String textContent) {
        message.subject = textContent;
        return this;
    }

    @Override
    public EmailBuilderImpl withAttachment(List<Attachment> attachments) {
        message.attachments = new AttachmentCollectionPage(attachments, null);
        return this;
    }

    @Override
    public void send() {
        getUserRequestBuilder().sendMail(UserSendMailParameterSet.newBuilder().withMessage(message).withSaveToSentItems(true).build()).buildRequest().post();
    }

    @Override
    public EmailBuilderImpl withReplyTo(List<EmailAddress> replyToAddress) {
        if (!replyToAddress.isEmpty()) {
            message.replyTo = replyToAddress.stream().map(this::toRecipient).collect(Collectors.toList());
        }
        return this;
    }

    public Recipient toRecipient(EmailAddress emailAddress) {
        if (emailAddress == null) {
            throw new IllegalArgumentException(Constants.EMAIL_ADDRESS_IS_EMPTY_OR_NULL);
        }
        Recipient recipient = new Recipient();
        com.microsoft.graph.models.EmailAddress recipientEmailAddress = new com.microsoft.graph.models.EmailAddress();
        recipientEmailAddress.address = emailAddress.getMailAddress();
        recipient.emailAddress = recipientEmailAddress;
        return recipient;
    }

    private UserRequestBuilder getUserRequestBuilder() {
        return getProvider().getUserRequestBuilder(null, null);
    }

}
