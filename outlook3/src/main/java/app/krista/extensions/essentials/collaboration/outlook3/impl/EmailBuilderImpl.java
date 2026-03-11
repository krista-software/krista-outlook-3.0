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
import app.krista.extensions.essentials.collaboration.outlook3.service.EmailAddress;
import app.krista.extensions.essentials.collaboration.outlook3.service.EmailBuilder;
import com.microsoft.graph.core.ClientException;
import com.microsoft.graph.models.*;
import com.microsoft.graph.requests.AttachmentCollectionPage;
import com.microsoft.graph.requests.UserRequestBuilder;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
    public void withContent(String contentType, String content) {
        message.body = new ItemBody();
        message.body.content = content;
        message.body.contentType = ("HTML".equals(contentType)) ? BodyType.HTML : BodyType.TEXT;
    }

    @Override
    public void withText(String textContent) {
        message.subject = textContent;
    }

    @Override
    public void withAttachment(List<Attachment> attachments) {
        message.attachments = new AttachmentCollectionPage(attachments, null);
    }

    @Override
    public void send() {
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                sendMailRequest();
                return;
            } catch (ClientException ex) {
                if (!isRetryable(ex) || attempt == 3) throw ex;
                sleep(2000L << (attempt - 1));
            }
        }
    }

    private void sendMailRequest() {
        getUserRequestBuilder()
                .sendMail(UserSendMailParameterSet.newBuilder()
                        .withMessage(message)
                        .withSaveToSentItems(true)
                        .build())
                .buildRequest()
                .post();
    }

    private boolean isRetryable(ClientException ex) {
        return ex instanceof com.microsoft.graph.http.GraphServiceException &&
                IntStream.of(502, 503, 504).anyMatch(code ->
                        code == ((com.microsoft.graph.http.GraphServiceException) ex).getResponseCode());
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void withReplyTo(List<EmailAddress> replyToAddress) {
        if (!replyToAddress.isEmpty()) {
            message.replyTo = replyToAddress.stream().map(this::toRecipient).collect(Collectors.toList());
        }
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
