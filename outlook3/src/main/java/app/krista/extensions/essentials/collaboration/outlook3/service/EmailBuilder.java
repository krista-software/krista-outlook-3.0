package app.krista.extensions.essentials.collaboration.outlook3.service;

import com.microsoft.graph.models.Attachment;

import java.util.List;

public interface EmailBuilder {

    EmailBuilder withTo(List<EmailAddress> emailAddress);

    default EmailBuilder withTo(String emailAddress) {
        return withTo(List.of(new EmailAddress("", emailAddress)));
    }

    default EmailBuilder withTo(String name, String emailAddress) {
        return withTo(List.of(new EmailAddress(name, emailAddress)));
    }

    EmailBuilder withCc(List<EmailAddress> emailAddress);

    default EmailBuilder withCc(String emailAddress) {
        return withCc(List.of(new EmailAddress("", emailAddress)));
    }

    default EmailBuilder withCc(String name, String emailAddress) {
        return withCc(List.of(new EmailAddress(name, emailAddress)));
    }

    EmailBuilder withBcc(List<EmailAddress> emailAddress);

    default EmailBuilder withBcc(String emailAddress) {
        return withBcc(List.of(new EmailAddress("", emailAddress)));
    }

    default EmailBuilder withBcc(String name, String emailAddress) {
        return withBcc(List.of(new EmailAddress(name, emailAddress)));
    }

    EmailBuilder withContent(String contentType, String content);

    EmailBuilder withText(String textContent);

    EmailBuilder withAttachment(List<Attachment> attachments);

    void send();

    EmailBuilder withReplyTo(List<EmailAddress> replyToAddress);

}
