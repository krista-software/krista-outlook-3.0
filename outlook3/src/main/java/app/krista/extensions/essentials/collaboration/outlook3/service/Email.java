package app.krista.extensions.essentials.collaboration.outlook3.service;

import java.util.List;

public interface Email {

    /**
     * Returns id of a given email
     *
     * @return id of a given email
     */
    String getEmailId();

    /**
     * Returns subject of a given email
     *
     * @return subject of a given email
     */
    String getSubject();

    /**
     * Returns sender email address of a given email
     *
     * @return sender email address of a given email
     */
    EmailAddress getSenderEmailAddress();

    /**
     * Returns to email addresses of a given email
     *
     * @return to email addresses of a given email
     */
    List<EmailAddress> getToEmailAddresses();

    /**
     * Returns reply to email addresses of a given email
     *
     * @return reply to email addresses of a given email
     */
    List<EmailAddress> getReplyToEmailAddresses();

    /**
     * Returns cc email addresses of a given email
     *
     * @return cc email addresses of a given email
     */
    List<EmailAddress> getCcEmailAddresses();

    /**
     * Returns bcc email addresses of a given email
     *
     * @return bcc email addresses of a given email
     */
    List<EmailAddress> getBccEmailAddresses();

    /**
     * Returns read receipt of a given email
     *
     * @return read receipt of a given email
     */
    Boolean getRead();

    /**
     * Returns send date and time of a given email
     *
     * @return send date and time of a given email
     */
    Long getSendDateAndTime();

    /**
     * Returns received date and time of a given email
     *
     * @return received date and time of a given email
     */
    Long getReceivedDateAndTime();

    /**
     * Returns content type of given email
     *
     * @return content type of given email
     */
    String getContentType();

    /**
     * Returns content of given email
     *
     * @return content of given email
     */
    String getContent();

    /**
     * This request mark given message as read
     */
    void markAsRead();

    /**
     * This request mark given message as unread
     */
    void markAsUnread();

    /**
     * This request move message to given folder
     *
     * @param folder {@link Folder} object of outlook
     */
    String moveToFolder(Folder folder);

    /**
     * Returns {@link Email} object of outlook
     *
     * @param message     message to be sent
     * @param attachments attachments to be attach
     * @return {@link Email} object of outlook
     */

    Email replyText(String message, List<com.microsoft.graph.models.Attachment> attachments, String bodyType);

    /**
     * Returns {@link Email} object of outlook
     *
     * @param message       message to be sent
     * @param attachments   attachments to be attach
     * @param toRecipients  list of to email addresses
     * @param ccRecipients  list of cc email addresses
     * @param bccRecipients list of bcc email addresses
     * @param replyTo       reply to email address
     * @return {@link Email} object of outlook
     */
    Email replyText(String message, List<com.microsoft.graph.models.Attachment> attachments, List<EmailAddress> toRecipients, List<EmailAddress> ccRecipients,
                    List<EmailAddress> bccRecipients, List<EmailAddress> replyTo, String bodyType);

    /**
     * Returns {@link Email} object of outlook
     *
     * @param message       message to be sent
     * @param attachments   attachments to be attach
     * @param toRecipients  list of to email addresses
     * @param ccRecipients  list of cc email addresses
     * @param bccRecipients list of bcc email addresses
     * @param replyTo       reply to email address
     * @param bodyType
     * @return {@link Email} object of outlook
     */
    Email replyToAll(String message, List<com.microsoft.graph.models.Attachment> attachments, List<EmailAddress> toRecipients, List<EmailAddress> ccRecipients,
                     List<EmailAddress> bccRecipients, List<EmailAddress> replyTo, String bodyType);

    /**
     * Return {@link Email} object of outlook
     *
     * @param message     message to be sent
     * @param attachments attachments to be added
     * @param bodyType
     * @return {@link Email} object of outlook
     */
    Email replyToAll(String message, List<com.microsoft.graph.models.Attachment> attachments, String bodyType);

    /**
     * This request forward given mail to given to address
     *
     * @param message message to be sent
     * @param to      email to which email get forwarded
     */
    void forward(String message, List<EmailAddress> to, String bodyType);

    /**
     * Returns {@link List<Attachment>} list object of outlook file attachments
     *
     * @return {@link List<Attachment>} list object of outlook file attachments
     */
    List<Attachment> getFileAttachments(Boolean useEmail);

    /**
     * Returns {@link List<String>} list links of outlook item attachments
     *
     * @return {@link List<String>} list links of outlook item attachments
     */
    List<String> getItemAttachments(Boolean useEmail);

    /**
     * Returns the categories of a given email
     *
     * @return categories of a given email
     */
    List<String> getCategories();

    /**
     * Adds a category tag to the email
     *
     * @param category name of the category to be added
     */
    void updateCategory(String category);
}
