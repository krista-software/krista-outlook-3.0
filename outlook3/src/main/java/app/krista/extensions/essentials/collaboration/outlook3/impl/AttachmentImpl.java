package app.krista.extensions.essentials.collaboration.outlook3.impl;

import app.krista.extensions.essentials.collaboration.outlook3.impl.util.Constants;
import app.krista.extensions.essentials.collaboration.outlook3.service.Attachment;
import com.microsoft.graph.models.FileAttachment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class AttachmentImpl implements Attachment {

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
        String fileName = getName();
        File file = new File(fileName);
        try (FileOutputStream out = new FileOutputStream(fileName)) {
            out.write(bytes);
            return file;
        } catch (IOException ioException) {
            throw new IllegalStateException(Constants.ERROR_OCCURRED_DURING_FETCHING_ATTACHMENTS, ioException.getCause());
        }
    }

}
