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
