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

    void withContent(String contentType, String content);

    void withText(String textContent);

    void withAttachment(List<Attachment> attachments);

    void send();

    void withReplyTo(List<EmailAddress> replyToAddress);

}
