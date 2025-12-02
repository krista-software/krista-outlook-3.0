package app.krista.extensions.essentials.collaboration.outlook3.impl;

import app.krista.extensions.essentials.collaboration.outlook3.catalog.entities.MailDetails;
import app.krista.extensions.essentials.collaboration.outlook3.impl.connectors.KristaMediaClient;
import app.krista.extensions.essentials.collaboration.outlook3.impl.util.Constants;
import app.krista.extensions.essentials.collaboration.outlook3.impl.util.EntityHelperUtil;
import app.krista.extensions.essentials.collaboration.outlook3.service.Attachment;
import app.krista.extensions.essentials.collaboration.outlook3.service.Email;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.model.base.File;
import com.microsoft.graph.models.FileAttachment;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static app.krista.extensions.essentials.collaboration.outlook3.impl.util.EntityHelperUtil.getAttachmentsByParsingIntoJsonMapper;
import static app.krista.extensions.essentials.collaboration.outlook3.impl.util.EntityHelperUtil.getCommaSeparatedEmail;

/**
 * Service class responsible for handling email-related operations including conversion between
 * Microsoft Graph Email objects and Krista MailDetails entities, file attachment processing,
 * and integration with Krista's media storage system.
 *
 * <p>This handler manages the transformation of email data from Microsoft Graph API format
 * to Krista's internal format, handles file uploads with fallback mechanisms, and processes
 * both file and item attachments.</p>
 */
@Service
public class MailHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(MailHandler.class);
    private final KristaMediaClient kristaMediaClient;

    // This unused parameter is needed for authentication of wait for event requests
    private AuthorizationContext authorizationContext;

    @Inject
    public MailHandler(KristaMediaClient kristaMediaClient) {
        this.kristaMediaClient = kristaMediaClient;
    }

    public void setAuthorizationContext(AuthorizationContext authContext) {
        this.authorizationContext = authContext;
    }

    /**
     * Converts a Microsoft Graph Email object to a Krista MailDetails entity.
     *
     * <p>This method extracts all relevant email properties including sender, recipients,
     * subject, content, attachments, and metadata. It handles both file and item attachments,
     * converting them to Krista's internal format. Draft emails without sender information
     * are handled gracefully by setting the from field to null.</p>
     *
     * @param email    the Microsoft Graph Email object to convert; if null, returns null
     * @param useEmail flag indicating whether to use email-specific attachment processing
     * @return a MailDetails object containing all email information, or null if input email is null
     */
    public MailDetails fromEmail(Email email, Boolean useEmail) {
        if (email == null) {
            return null;
        }
        MailDetails mailDetails = new MailDetails();
        mailDetails.from = email.getSenderEmailAddress() != null
                ? email.getSenderEmailAddress().getMailAddress()
                : null; //Draft mails does not have sender email address
        mailDetails.to = getCommaSeparatedEmail(email.getToEmailAddresses());
        mailDetails.message = email.getContent();
        mailDetails.subject = email.getSubject();
        final List<Attachment> fileAttachments = email.getFileAttachments(useEmail);
        mailDetails.fileAttachment = fileAttachments.stream()
                .map(attachment -> toKristaFiles(attachment.download()))
                .collect(Collectors.toList());
        mailDetails.itemAttachment = email.getItemAttachments(useEmail);
        mailDetails.messageID = email.getEmailId();
        mailDetails.cc = getCommaSeparatedEmail(email.getCcEmailAddresses());
        mailDetails.bcc = getCommaSeparatedEmail(email.getBccEmailAddresses());
        mailDetails.isRead = email.getRead();
        mailDetails.replyTo = getCommaSeparatedEmail(email.getReplyToEmailAddresses());
        mailDetails.sendDateAndTime = email.getSendDateAndTime();
        mailDetails.receivedDateAndTime = email.getReceivedDateAndTime();
        mailDetails.categories = email.getCategories();
        mailDetails.conversationID = email.getConversationId();
        mailDetails.uniqueBody = email.getUniqueBody();
        mailDetails.sensitivity = email.getSensitivity();

        return mailDetails;
    }

    /**
     * Converts a Java File object to a Krista File entity with automatic fallback mechanism.
     *
     * <p>This method attempts to upload the file using the standard upload mechanism first.
     * If that fails (e.g., due to file size limitations), it automatically falls back to
     * zip compression upload. This ensures maximum compatibility with various file sizes
     * and formats.</p>
     *
     * @param file the Java File object to convert to Krista format
     * @return a Krista File entity representing the uploaded file
     * @throws RuntimeException if both regular and zip upload attempts fail, with a user-friendly
     *                          error message indicating possible causes (file too large, corrupted, or unsupported format)
     */
    public File toKristaFiles(java.io.File file) {
        long fileSize = file.length();
        LOGGER.debug("Converting file to Krista format: fileName={}, size={} bytes", file.getName(), fileSize);
        try {
            return kristaMediaClient.toKristaFile(file);
        } catch (Exception cause) {
            LOGGER.debug("Regular upload failed, trying zip upload: fileName={}, size={}, error={}",
                    file.getName(), fileSize, cause.getMessage());

            try {
                return kristaMediaClient.toKristaZipFile(file);
            } catch (Exception zipCause) {
                LOGGER.error("Both regular and zip upload failed: fileName={}, size={}, regularError={}, zipError={}",
                        file.getName(), fileSize, cause.getMessage(), zipCause.getMessage(), zipCause);
                throw new RuntimeException("We couldn't upload the file '" + file.getName() + "'. The file may be too large, corrupted, or in an unsupported format. Please verify the file and try again.", zipCause);
            }
        }
    }

    /**
     * Converts a list of Krista File entities to Microsoft Graph Attachment objects.
     *
     * <p>This method processes each file by reading its content, determining its MIME type,
     * and creating FileAttachment objects suitable for Microsoft Graph API. The files are
     * first parsed through a JSON mapper to ensure proper format, then converted to
     * Microsoft Graph's FileAttachment format with appropriate metadata.</p>
     *
     * @param attachments list of Krista File entities to convert to Microsoft Graph attachments
     * @return list of Microsoft Graph Attachment objects ready for API submission
     * @throws IllegalStateException if file processing fails during attachment creation,
     *                               wrapping the underlying IOException with a descriptive error message
     */
    public List<com.microsoft.graph.models.Attachment> toAttachment(List<File> attachments) {
        List<com.microsoft.graph.models.Attachment> attachmentsList = new LinkedList<>();
        List<File> filesToAttach = getAttachmentsByParsingIntoJsonMapper(attachments);
        for (File file : filesToAttach) {
            FileAttachment fileAttachments = new FileAttachment();
            fileAttachments.name = file.getFileName();
            try {
                java.io.File ioFile = getFileObject(file);
                fileAttachments.contentType = EntityHelperUtil.getFileType(ioFile);
                fileAttachments.contentBytes = EntityHelperUtil.readContentOfTheFile(ioFile);
                fileAttachments.oDataType = Constants.MICROSOFT_GRAPH_FILE_ATTACHMENT;
            } catch (IOException ioException) {
                LOGGER.error("Failed to process attachment: fileName={}, error={}", file.getFileName(), ioException.getMessage(), ioException);
                throw new IllegalStateException(Constants.ERROR_OCCURRED_DURING_UPLOADING_ATTACHMENT, ioException.getCause());
            }
            attachmentsList.add(fileAttachments);
        }
        return attachmentsList;
    }

    private java.io.File getFileObject(File file) throws IOException {
        return kristaMediaClient.toJavaFile(file);
    }

}
