package app.krista.extensions.essentials.collaboration.outlook3.impl;

import app.krista.extensions.essentials.collaboration.outlook3.impl.util.Constants;
import app.krista.extensions.essentials.collaboration.outlook3.service.Attachment;
import app.krista.extensions.essentials.collaboration.outlook3.impl.util.FilenameUtil;
import com.microsoft.graph.models.FileAttachment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class AttachmentImpl implements Attachment {

    private static final Logger LOGGER = LoggerFactory.getLogger(AttachmentImpl.class);
    private final com.microsoft.graph.models.Attachment attachment;

    public AttachmentImpl(com.microsoft.graph.models.Attachment attachment) {
        this.attachment = attachment;
    }

    @Override
    public String getName() {
        return attachment.name;
    }

    @Override
    public int getSize() {
        return attachment.size != null ? attachment.size : 0;
    }

    @Override
    public String getMimeType() {
        return attachment.contentType;
    }

    @Override
    public File download() {
        byte[] bytes = ((FileAttachment) attachment).contentBytes;
        String originalName = getName();
        String safeName = FilenameUtil.toSafeFilename(originalName);
        File file = new File(safeName);
        try (FileOutputStream out = new FileOutputStream(file)) {
            out.write(bytes);
            return file;
        } catch (IOException first) {
            // Fallback to a UUID-based safe name to avoid aborting the whole batch
            String fallbackName = FilenameUtil.uuidFallback(originalName);
            File fallback = new File(fallbackName);
            try (FileOutputStream out = new FileOutputStream(fallback)) {
                out.write(bytes);
                LOGGER.debug("Attachment filename caused I/O error, used fallback name: original='{}', safe='{}', fallback='{}', error='{}'",
                        originalName, safeName, fallbackName, first.getMessage());
                return fallback;
            } catch (IOException cause) {
                LOGGER.error("Failed to download attachment after fallback: original='{}', safe='{}', error='{}'", originalName, safeName, cause.getMessage(), cause);
                throw new IllegalArgumentException(Constants.ERROR_OCCURRED_DURING_FETCHING_ATTACHMENTS, cause);
            }
        }
    }

}
