package app.krista.extensions.essentials.collaboration.outlook3.service;

import java.util.List;

/**
 *  Outlook Folder Object
 */
public interface Folder {

    /**
     * Returns id of given outlook folder
     *
     * @return id of given outlook folder
     */
    String getFolderId();

    /**
     * Returns name of given outlook folder
     *
     * @return name of given outlook folder
     */
    String getFolderName();

    /**
     * Returns {@link Folder} object of outlook folder which is parent of a given folder
     *
     * @return {@link Folder} object of outlook folder
     */
    Folder getParent();

    /**
     * Returns {@link List<String>} list of path of given folder
     *
     * @return {@link List<String>} list of path of given folder
     */
    List<String> getFolderPath();

    /**
     * Returns {@link List<String>} list of child folder names of given folder
     *
     * @return {@link List<String>} list of child folder names of given folder
     */
    List<String> getChildFolderNames();

    /**
     * Returns {@link Folder} object of child folder of given folder name
     *
     * @return {@link Folder} object of child folder of given folder name
     */
    Folder getChildFolderByName(String childFolderName);

    /**
     * Returns {@link Folder} object of child folder of given folder name path
     *
     * @return {@link Folder} object of child folder of given folder name path
     */
    Folder getChildFolderByName(List<String> childFolderPath);

    /**
     * Returns {@link List<String>} list of child folder ids
     *
     * @return {@link List<String>} list of child folder ids
     */
    List<String> getChildFolderIds();

    /**
     * Returns {@link Folder} object of child folder of given child folder id
     *
     * @return {@link Folder} object of child folder of given child folder id
     */
    Folder getChildFolder(String childFolderId);

    /**
     * Returns {@link List<Email>} list of emails from given page number of given size
     *
     * @return {@link List<Email>} list of emails from given page number of given size
     */
    List<Email> getEmails(Double pageNumber, Double pageSize);

    List<Email> getEmails(Boolean useEmail);

    void moveInto(Folder folder);

    int getMessageCount();

    int getUnreadMessageCount();

}
