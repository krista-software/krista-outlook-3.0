package app.krista.extensions.essentials.collaboration.outlook3.impl;

import app.krista.extensions.essentials.collaboration.outlook3.catalog.entities.MailDetails;
import app.krista.extensions.essentials.collaboration.outlook3.impl.util.Constants;
import app.krista.extensions.essentials.collaboration.outlook3.impl.util.EntityHelperUtil;
import app.krista.extensions.essentials.collaboration.outlook3.service.Attachment;
import app.krista.extensions.essentials.collaboration.outlook3.service.Email;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.ksdk.files.FileHandle;
import app.krista.ksdk.files.FileRepository;
import app.krista.model.base.File;
import com.microsoft.graph.models.FileAttachment;
import org.apache.commons.io.FilenameUtils;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static app.krista.extensions.essentials.collaboration.outlook3.impl.util.EntityHelperUtil.getAttachmentsByParsingIntoJsonMapper;
import static app.krista.extensions.essentials.collaboration.outlook3.impl.util.EntityHelperUtil.getCommaSeparatedEmail;
import static java.lang.System.getProperty;

@Service
public class MailHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(MailHandler.class);
    private final FileRepository repository;

    // This unused parameter is needed for authentication of wait for event requests
    private AuthorizationContext authorizationContext;

    @Inject
    public MailHandler(FileRepository repository) {
        this.repository = repository;
    }

    public void setAuthorizationContext(AuthorizationContext authContext) {
        this.authorizationContext = authContext;
    }

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
        return mailDetails;
    }

    public File toKristaFiles(java.io.File file) {
        final java.io.File tmpDir = new java.io.File(System.getProperty("java.io.tmpDir", "/tmp"));
        final java.io.File output = new java.io.File(String.format("%s%s%s", tmpDir.getAbsolutePath(),
                java.io.File.separatorChar, file.getName()));

        FileHandle outputFileHandle = null;
        try (FileInputStream inputStream = new FileInputStream(file)) {
            outputFileHandle = this.repository.createNewFileByName(FilenameUtils.getName(output.getName()));
            outputFileHandle.setContent(inputStream);
        } catch (IOException cause) {
            throw new RuntimeException("Failed to store content to file handle", cause);
        } finally {
            if (outputFileHandle != null) {
                outputFileHandle.close();
            }
        }
        return outputFileHandle.getFile();
    }

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
                throw new IllegalStateException(Constants.ERROR_OCCURRED_DURING_UPLOADING_ATTACHMENT, ioException.getCause());
            }
            attachmentsList.add(fileAttachments);
        }
        return attachmentsList;
    }

    public void validateTempFile(final java.io.File file) {

        if (file.exists()) {
            LOGGER.warn("Temp {} file exists. Deleting! ", file.getAbsolutePath());
            final boolean deleted = file.delete();
            if (!deleted) {
                LOGGER.error("Failed to delete temp file at {} ! Trying to proceed", file.getAbsoluteFile());
            }
        } else {
            LOGGER.trace("Temp file at {} does not yet exist", file.getAbsoluteFile());
        }

    }

    private java.io.File getFileObject(File file) throws IOException {

        final FileHandle inputFileHandle = this.repository.getFile(file);
        final java.io.File tmpDir = new java.io.File(getProperty("java.io.tmpDir", "/tmp"));

        if (tmpDir.exists() && !tmpDir.canWrite()) {
            LOGGER.error("Unable to write to tmpDir: {}", tmpDir.getAbsolutePath());
            throw new IllegalArgumentException("Unable write to temporary file.");
        }

        final java.io.File input = new java.io.File(String
                .format("%s%s%s", tmpDir.getAbsolutePath(), java.io.File.separatorChar, file.getFileName()));

        this.validateTempFile(input);

        try (InputStream inputStream = inputFileHandle.getContent();
             FileOutputStream outputStream = new FileOutputStream(input)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } catch (IOException cause) {
            throw new RuntimeException("Failed to store content of input file in temp file at " + input.getAbsolutePath(), cause);
        }
        return input;
    }

}
