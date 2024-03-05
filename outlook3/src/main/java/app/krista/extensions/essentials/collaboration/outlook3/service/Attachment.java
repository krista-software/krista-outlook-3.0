package app.krista.extensions.essentials.collaboration.outlook3.service;

import app.krista.model.base.File;

public interface Attachment {

    /**
     * Returns name of the given attachment
     *
     * @return name of the given attachment
     */
    String getName();

    /**
     * Returns size of the given attachment
     *
     * @return size of the given attachment
     */
    int getSize();

    /**
     * Returns mime type of the given attachment
     *
     * @return mime type of the given attachment
     */
    String getMimeType();

    /**
     * Returns {@link File} object of outlook.
     *
     * @return {@link File} object of outlook.
     */
    java.io.File download();

}
