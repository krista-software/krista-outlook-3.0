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

import java.util.List;

/**
 * This is outlook user account
 */
public interface Account {

    /**
     * Returns {@link Folder} object of outlook from the name path provided. Ex Inbox/childFolder
     *
     * @param name names of the folder
     * @return {@link Folder} object
     */
    Folder getFolderByName(List<String> name);

    /**
     * Returns {@link Folder} object of outlook from the name provided. Ex Inbox
     *
     * @param name      names of the folder
     * @param accountID  account Id
     * @return {@link Folder} object
     */
    Folder getFolderByName(String name, Boolean useEmail, String accountID);

    /**
     * Returns {@link Folder} object of Sent outlook folder.
     *
     * @return {@link Folder} object
     */
    Folder getSentFolder();

    /**
     * Returns {@link Folder} object of Inbox outlook folder.
     *
     * @return {@link Folder} object
     */
    Folder getInboxFolder(Boolean useEmail, String accountID);

    /**
     * Returns {@link Folder} object of outlook from the id provided.
     *
     * @param folderId id of the folder
     * @return {@link Folder} object
     */
    Folder getFolder(String folderId);

    /**
     * Returns list of all folder names
     *
     * @return List of folder names
     */
    List<String> getFolderNames();

    /**
     * Returns list of all folder Ids
     *
     * @return List of folder Ids
     */
    List<String> getFolderIds();

    /**
     * Returns {@link Email} object of outlook from the email id provided.
     *
     * @param emailId id of the email
     * @return {@link Email} object
     */
    Email getEmail(String emailId);

    /**
     * Returns {@link Email} object with retry mechanism to handle eventual consistency scenarios.
     * Intended for webhook/delta flows where the message may not be immediately available.
     *
     * @param emailId id of the email
     * @return {@link Email} object or null if not found after retries
     */
    Email getEmailWithRetry(String emailId);

    /**
     * Returns {@link List<Email>} of outlook from the search string provided.
     *
     * @param searchString String to search email from outlook
     * @return {@link List<Email>}
     */
    List<Email> searchEmails(String searchString);

    /**
     * Returns {@link EmailBuilder}, which then use to send a mail
     *
     * @return {@link EmailBuilder} object
     */
    EmailBuilder newEmail();

    /**
     * Returns list of all category names defined in the Outlook account
     *
     * @return List of category names
     */
    List<String> getCategoryNames();

    /**
     * Creates a new category in the Outlook account
     *
     * @param category name of the category to create
     */
    void createCategory(String category);

    /**
     * Fetches the delta of notifications since the last query
     * Uses Microsoft Graph delta query to efficiently track changes
     *
     * @return List of message IDs that have changed since the last query
     */
    List<String> fetchNotificationDeltaQuery();
}
