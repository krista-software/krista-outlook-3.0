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

import app.krista.extensions.essentials.collaboration.outlook3.impl.connectors.GraphServiceClientProvider;
import app.krista.extensions.essentials.collaboration.outlook3.impl.connectors.GraphServiceClientProviderFactory;
import app.krista.extensions.essentials.collaboration.outlook3.impl.util.Constants;
import app.krista.extensions.essentials.collaboration.outlook3.impl.util.Validators;
import app.krista.extensions.essentials.collaboration.outlook3.service.Account;
import app.krista.extensions.essentials.collaboration.outlook3.service.Email;
import app.krista.extensions.essentials.collaboration.outlook3.service.EmailBuilder;
import app.krista.extensions.essentials.collaboration.outlook3.service.Folder;
import com.microsoft.graph.http.GraphServiceException;
import com.microsoft.graph.models.MailFolder;
import com.microsoft.graph.models.Message;
import com.microsoft.graph.models.OutlookCategory;
import com.microsoft.graph.models.Recipient;
import com.microsoft.graph.options.HeaderOption;
import com.microsoft.graph.options.QueryOption;
import com.microsoft.graph.requests.*;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.jvnet.hk2.annotations.ContractsProvided;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static app.krista.extensions.essentials.collaboration.outlook3.impl.util.Constants.BODY_CONTENT_TYPE_HTML;
import static app.krista.extensions.essentials.collaboration.outlook3.impl.util.Constants.SENSITIVITY_PROP_FILTER;

@Service
@ContractsProvided(Account.class)
public class AccountImpl implements Account {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountImpl.class);

    private GraphServiceClientProvider provider;
    private GraphServiceClientProviderFactory providerFactory;

    @Inject
    public AccountImpl(GraphServiceClientProviderFactory factory) {
        this.providerFactory = factory;
    }

    public GraphServiceClientProvider getProvider() {
        if (provider == null) {
            this.provider = providerFactory.create();
        }
        return provider;
    }

    public AccountImpl(GraphServiceClientProvider provider) {
        this.provider = provider;
    }

    /**
     * Returns {@link Folder} object of outlook from the names provided.
     * Call getChildFolder function if child folder name provided. Ex Inbox/childFolder
     *
     * @param folderNames names of the folder
     * @return {@link Folder } object or null if provided folder names is null or empty
     */

    @Override
    public Folder getFolderByName(List<String> folderNames) {
        if (Validators.isListNullOrEmpty(folderNames)) {
            throw new IllegalArgumentException(Constants.FOLDER_NAME_LIST_IS_EMPTY_OR_NULL);
        }
        Folder folder = getFolderByName(folderNames.getFirst(), null, null);
        if (folderNames.size() > 1 && folder != null) {
            return folder.getChildFolderByName(folderNames.subList(1, folderNames.size()));
        }
        return folder;
    }

    @Override
    public Folder getFolderByName(String folderName, Boolean useEmail, String accountID) {
        LOGGER.info("getFolderByName for folderName {}, useEmail {}, accountID {}", folderName, useEmail, accountID);
        if (Validators.isStringNullOrBlank(folderName)) {
            LOGGER.error(Constants.FOLDER_NAME_IS_EMPTY_OR_NULL);
            throw new IllegalArgumentException(Constants.FOLDER_NAME_IS_EMPTY_OR_NULL);
        }
        MailFolderCollectionPage page = getFoldersRequestBuilder(useEmail, accountID).buildRequest().get();
        while (page != null && !page.getCurrentPage().isEmpty()) {
            for (MailFolder folderInPage : page.getCurrentPage()) {
                if (folderInPage.displayName != null && Objects.equals(folderName.toLowerCase(), folderInPage.displayName.toLowerCase())) {
                    return new FolderImpl(this, getProvider(), folderInPage);
                }
            }
            MailFolderCollectionRequestBuilder nextPage = page.getNextPage();
            if (nextPage != null) {
                page = nextPage.buildRequest().get();
            } else {
                break;
            }
        }
        LOGGER.error("Folder name '{}' not found.", folderName);
        throw new IllegalArgumentException(Constants.FOLDER_NAME_NOT_FOUND);
    }

    @Override
    public Folder getSentFolder() {
        return getFolderByName(Constants.SENT_ITEMS, null, null);
    }

    @Override
    public Folder getInboxFolder(Boolean useEmail, String accountID) {
        return getFolderByName(Constants.INBOX, useEmail, accountID);
    }

    @Override
    public Folder getFolder(String folderId) {
        if (Validators.isStringNullOrBlank(folderId)) {
            throw new IllegalArgumentException(Constants.FOLDER_ID_IS_NULL_OR_EMPTY);
        }
        try {
            MailFolder mailFolder = getFoldersRequestBuilder(null, null).byId(folderId).buildRequest().get();
            if (mailFolder == null) {
                throw new IllegalArgumentException(Constants.FOLDER_WITH_ID + folderId + Constants.NOT_FOUND);
            }
            return new FolderImpl(this, getProvider(), mailFolder);
        } catch (RuntimeException cause) {
            throw new IllegalStateException(Constants.FOLDER_WITH_ID + folderId + Constants.NOT_FOUND, cause.getCause());
        }

    }

    @Override
    public List<String> getFolderNames() {
        List<String> folderNames = new ArrayList<>();
        MailFolderCollectionPage page = getFoldersRequestBuilder(null, null).buildRequest().get();
        while (page != null && !page.getCurrentPage().isEmpty()) {
            for (MailFolder folderInPage : page.getCurrentPage()) {
                folderNames.add(folderInPage.displayName);
                if (folderInPage.childFolderCount != null && folderInPage.childFolderCount > 0) {
                    getChildFolderNames(folderInPage.displayName, folderInPage.id, folderNames);
                }
            }
            if (page.getNextPage() != null) {
                page = page.getNextPage().buildRequest().get();
            } else {
                break;
            }
        }
        return folderNames;
    }

    private void getChildFolderNames(String parentFolderName, String id, List<String> folderNames) {
        MailFolderCollectionPage page = getUserRequestBuilder(null, null).mailFolders(id).childFolders().buildRequest().get();
        while (page != null && !page.getCurrentPage().isEmpty()) {
            for (MailFolder childFolderInPage : page.getCurrentPage()) {
                folderNames.add(parentFolderName + Constants.FORWARD_SLASH + childFolderInPage.displayName);
                if (childFolderInPage.childFolderCount != null && childFolderInPage.childFolderCount > 0) {
                    getChildFolderNames(parentFolderName + Constants.FORWARD_SLASH + childFolderInPage.displayName, childFolderInPage.id, folderNames);
                }
            }
            if (page.getNextPage() != null) {
                page = page.getNextPage().buildRequest().get();
            } else {
                break;
            }
        }
    }

    @Override
    public List<String> getFolderIds() {
        MailFolderCollectionPage page = getFoldersRequestBuilder(null, null).buildRequest().get();
        List<String> folderIds = new ArrayList<>();
        while (page != null && !page.getCurrentPage().isEmpty()) {
            for (MailFolder folderInPage : page.getCurrentPage()) {
                folderIds.add(folderInPage.id);
            }
            if (page.getNextPage() != null) {
                page = page.getNextPage().buildRequest().get();
            }
        }
        return folderIds;
    }

    @Override
    public Email getEmail(String emailId) {

        LOGGER.info("Getting email with ID: {}", emailId);
        try {
            Message message = getUserRequestBuilder(null, null)
                    .messages(emailId)
                    .buildRequest(
                            new HeaderOption(Constants.PREFER, BODY_CONTENT_TYPE_HTML),
                            new QueryOption(Constants.SELECT_QUERY, Constants.MAIL_SELECT_FIELDS),
                            new QueryOption("$expand", SENSITIVITY_PROP_FILTER)
                    )
                    .get();
            return new EmailImpl(getProvider(), message);
        } catch (GraphServiceException cause) {

            if (isExpectedGraphError(cause.getMessage())) {
                LOGGER.warn("Email not retrievable for ID {}: {}", emailId, extractGraphErrorSummary(cause.getMessage()));
            } else {
                LOGGER.error("Unexpected Graph API error getting email with ID {}: {}", emailId, cause.getMessage(), cause);
            }
            return null;
        } catch (Exception cause) {

            LOGGER.error("Error getting email with ID {}: {}", emailId, cause.getMessage(), cause);
            return null;
        }
    }

    private boolean isExpectedGraphError(String errorMessage) {

        if (errorMessage == null) {
            return false;
        }

        return errorMessage.contains("ErrorItemNotFound") ||
                errorMessage.contains("ErrorInvalidOperation") ||
                errorMessage.contains("ErrorInvalidIdMalformed");
    }

    private String extractGraphErrorSummary(String errorMessage) {

        if (errorMessage == null) {
            return "unknown";
        }

        if (errorMessage.contains("ErrorItemNotFound")) {
            return "ErrorItemNotFound (email deleted or moved)";
        }
        if (errorMessage.contains("ErrorInvalidOperation")) {
            return "ErrorInvalidOperation (conversation ID used as message ID)";
        }
        if (errorMessage.contains("ErrorInvalidIdMalformed")) {
            return "ErrorInvalidIdMalformed (truncated or malformed message ID)";
        }

        return errorMessage.length() > 100 ? errorMessage.substring(0, 100) : errorMessage;
    }

    @Override
    public Email getEmailWithRetry(String emailId) {
        LOGGER.info("Getting email with ID: {} with retry mechanism", emailId);
        int maxAttempts = 5;
        long delayMillis = 1000L;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            Email email = getEmail(emailId);
            if (email != null) {
                if (attempt > 1) {
                    LOGGER.info("Email with ID: {} retrieved successfully on attempt {}", emailId, attempt);
                }
                return email;
            }

            LOGGER.info("Email with ID: {} not available yet, attempt {}/{}. Retrying after {} ms", emailId, attempt, maxAttempts, delayMillis);
            try {
                Thread.sleep(delayMillis);
            } catch (InterruptedException interruptedException) {
                LOGGER.warn("Thread interrupted while waiting to retry getEmail for ID: {}", emailId, interruptedException);
                Thread.currentThread().interrupt();
                break;
            }
        }

        LOGGER.warn("Email with ID: {} could not be retrieved after {} attempts", emailId, maxAttempts);
        return null;
    }

    @Override
    public List<Email> searchEmails(String searchString) {
        String sanitized = searchString.replaceAll("([\"\\\\])", "\\\\$1");
        try {
            MessageCollectionPage messages = getUserRequestBuilder(null, null)
                    .messages()
                    .buildRequest(
                            new QueryOption("$search", "\"" + sanitized + "\""),
                            new HeaderOption(Constants.PREFER, BODY_CONTENT_TYPE_HTML),
                            new QueryOption(Constants.SELECT_QUERY, Constants.MAIL_SELECT_FIELDS)
                    )
                    .top(15).get();
            return (messages == null || messages.getCurrentPage() == null) ? List.of()
                    : messages.getCurrentPage().stream().map(m -> new EmailImpl(getProvider(), m)).collect(Collectors.toList());
        } catch (Exception cause) {
            LOGGER.error("Error while searching emails with query '{}': {}", searchString, cause.getMessage(), cause);
            return List.of();
        }
    }

    @Override
    public EmailBuilder newEmail() {
        return EmailBuilderImpl.create(getProvider());
    }

    private MailFolderCollectionRequestBuilder getFoldersRequestBuilder(Boolean useEmail, String accountID) {
        return getUserRequestBuilder(useEmail, accountID).mailFolders();
    }

    private UserRequestBuilder getUserRequestBuilder(Boolean useEmail, String accountID) {
        return getProvider().getUserRequestBuilder(useEmail, accountID);
    }


    @Override
    public List<String> getCategoryNames() {
        LOGGER.info("Fetching Category Names.");
        List<String> categoryNames = new ArrayList<>();

        OutlookCategoryCollectionPage categoriesPage = getUserRequestBuilder(null, null).outlook()
                .masterCategories().buildRequest().get();
        while (categoriesPage != null && !categoriesPage.getCurrentPage().isEmpty()) {
            for (OutlookCategory category : categoriesPage.getCurrentPage()) {
                categoryNames.add(category.displayName);
            }
            if (categoriesPage.getNextPage() != null) {
                categoriesPage = categoriesPage.getNextPage().buildRequest().get();
            } else {
                break;
            }
        }
        return categoryNames;

    }

    @Override
    public void createCategory(String category) {
        LOGGER.info("Creating category with display name: {}", category);
        OutlookCategory outlookCategory = new OutlookCategory();
        outlookCategory.displayName = category;

        try {
            getUserRequestBuilder(null, null).outlook().masterCategories().buildRequest()
                    .post(outlookCategory);
        } catch (GraphServiceException cause) {
            LOGGER.error("Failed to create new category with name: {} , {}", category, cause.getMessage(), cause);
        }
    }

    /**
     * Fetches notification delta query to retrieve new messages since the last checkpoint.
     *
     * <p>This method uses Microsoft Graph delta queries to efficiently retrieve only
     * messages that have changed since the last query. Delta tokens are automatically
     * managed and stored for incremental synchronization.</p>
     *
     * <p><b>Automatic Token Expiration Handling:</b><br>
     * When a delta token expires (typically after 30 days of inactivity), Microsoft Graph
     * returns HTTP 410 Gone with a "SyncStateNotFound" error. This method automatically
     * detects this condition, clears the expired token, and restarts the delta query
     * from scratch to obtain a fresh token.</p>
     *
     * <p><b>Message Filtering:</b><br>
     * Only messages received in the last 24 hours and not sent by the configured
     * email account are included in the results.</p>
     *
     * @return List of message IDs for new messages received since last query,
     *         or empty list if no new messages are found
     * @throws GraphServiceException if Microsoft Graph API call fails with an error
     *         other than HTTP 410 Gone (token expiration is handled automatically)
     * @see #filterMessages(Message, OffsetDateTime, List)
     * @since 3.0.28
     */
    @Override
    public List<String> fetchNotificationDeltaQuery() {
        OffsetDateTime startOfTodayUtc = OffsetDateTime.now(ZoneOffset.UTC).minusDays(1);
        MessageDeltaCollectionPage deltaPage = fetchDeltaPage();
        List<String> messageIds = collectMessageIds(deltaPage, startOfTodayUtc);
        storeDeltaToken(deltaPage);
        return messageIds;
    }

    /**
     * Fetches the delta page from Microsoft Graph API.
     * Attempts to use existing delta token if available, otherwise starts fresh query.
     *
     * @return MessageDeltaCollectionPage containing messages, or null if query fails
     */
    private MessageDeltaCollectionPage fetchDeltaPage() {
        String deltaLink = getProvider().getDeltaLink();
        LOGGER.info("Search for the existing delta link: {}", deltaLink);
        UserRequestBuilder requestBuilder = getUserRequestBuilder(null, null);

        return StringUtils.isNotEmpty(deltaLink)
                ? fetchWithDeltaToken(deltaLink, requestBuilder)
                : fetchFreshDelta(requestBuilder);
    }

    /**
     * Fetches delta using an existing delta token.
     * Handles token expiration (HTTP 410) by automatically restarting with fresh query.
     *
     * @param deltaLink the existing delta token
     * @param requestBuilder the user request builder
     * @return MessageDeltaCollectionPage containing messages
     * @throws GraphServiceException if API call fails with non-410 error
     */
    private MessageDeltaCollectionPage fetchWithDeltaToken(String deltaLink, UserRequestBuilder requestBuilder) {
        try {
            LOGGER.info("Fetching delta link using last checkpoint: {}", deltaLink);
            return new MessageDeltaCollectionRequestBuilder(deltaLink, requestBuilder.getClient(), null)
                    .buildRequest().get();
        } catch (GraphServiceException gse) {
            return handleDeltaQueryError(gse, requestBuilder);
        }
    }

    /**
     * Fetches a fresh delta query without using a checkpoint.
     *
     * @param requestBuilder the user request builder
     * @return MessageDeltaCollectionPage containing messages
     */
    private MessageDeltaCollectionPage fetchFreshDelta(UserRequestBuilder requestBuilder) {
        LOGGER.info("Fetching delta link using start.");
        return requestBuilder.mailFolders("Inbox").messages().delta().buildRequest().get();
    }

    /**
     * Handles errors that occur during delta query execution.
     * Automatically recovers from expired token errors (HTTP 410) by clearing token and restarting.
     *
     * @param gse the GraphServiceException that occurred
     * @param requestBuilder the user request builder
     * @return MessageDeltaCollectionPage from fresh query if token expired
     * @throws GraphServiceException if error is not token expiration
     */
    private MessageDeltaCollectionPage handleDeltaQueryError(GraphServiceException gse, UserRequestBuilder requestBuilder) {
        if (isExpiredTokenError(gse)) {
            LOGGER.info("Delta token expired (HTTP 410). Error: {}. Clearing and restarting.", gse.getMessage());
            getProvider().storeDeltaLink(null);
            return fetchFreshDelta(requestBuilder);
        } else {
            LOGGER.error("GraphServiceException while fetching delta: {}", gse.getMessage(), gse);
            throw gse;
        }
    }

    /**
     * Checks if the exception indicates an expired delta token.
     *
     * @param gse the GraphServiceException to check
     * @return true if error is HTTP 410 or contains "SyncStateNotFound" message
     */
    private boolean isExpiredTokenError(GraphServiceException gse) {
        return gse.getResponseCode() == 410 ||
                (gse.getMessage() != null && gse.getMessage().contains("SyncStateNotFound"));
    }

    /**
     * Collects message IDs from the delta page and all subsequent pages.
     *
     * @param deltaPage the initial delta page
     * @param startOfTodayUtc the cutoff time for filtering messages
     * @return List of message IDs that match the filter criteria
     */
    private List<String> collectMessageIds(MessageDeltaCollectionPage deltaPage, OffsetDateTime startOfTodayUtc) {
        LOGGER.info("Notification from current page being started.");
        List<String> messageIds = new ArrayList<>();

        if (deltaPage == null) {
            return messageIds;
        }

        for (Message message : deltaPage.getCurrentPage()) {
            filterMessages(message, startOfTodayUtc, messageIds);
        }

        getMessageDeltaCollectionFromNextPages(deltaPage, startOfTodayUtc, messageIds);
        return messageIds;
    }

    /**
     * Stores the delta token from the completed query for next incremental sync.
     *
     * @param deltaPage the final delta page containing the new delta token
     */
    private void storeDeltaToken(MessageDeltaCollectionPage deltaPage) {
        if (deltaPage != null && deltaPage.deltaLink != null) {
            LOGGER.info("Storing new delta link.");
            getProvider().storeDeltaLink(deltaPage.deltaLink);
        }
    }

    private void filterMessages(Message message, OffsetDateTime startOfTodayUtc, List<String> messageIds) {
        Map<String, Object> additionalData = message.additionalDataManager().entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        if (additionalData.containsKey("@removed")) {
            LOGGER.info("Skipping deleted message ID: {}", message.id);
            return;
        }
        OffsetDateTime receivedTime = message.receivedDateTime;

        if (receivedTime != null && !receivedTime.isBefore(startOfTodayUtc) && !isMessageFromTheConfiguredEmail(message.from != null ? toEmailAddress(message.from) : null)) {
            messageIds.add(message.id);
        } else {
            LOGGER.info("Skipping older or same sender message ID: {}", message.id);
        }
    }

    private boolean isMessageFromTheConfiguredEmail(String email) {
        String configuredEmail = getProvider().getOutlookAttributes().getEmail();
        return configuredEmail.equalsIgnoreCase(email);
    }

    private String toEmailAddress(Recipient recipient) {
        if (recipient == null) {
            return null;
        }
        return recipient.emailAddress != null ? recipient.emailAddress.address : null;
    }

    /**
     * Processes all remaining pages in the delta query result.
     *
     * @param deltaCollectionPage the current delta page
     * @param startOfTodayUtc the cutoff time for filtering messages
     * @param messageIds the list to accumulate message IDs
     * @return the final delta page (used to extract the new delta token)
     */
    @Nullable
    private MessageDeltaCollectionPage getMessageDeltaCollectionFromNextPages(MessageDeltaCollectionPage deltaCollectionPage, OffsetDateTime startOfTodayUtc, List<String> messageIds) {
        LOGGER.info("Notification being fetched from current page moving to next page.");
        while (deltaCollectionPage != null && deltaCollectionPage.getNextPage() != null) {
            deltaCollectionPage = deltaCollectionPage.getNextPage().buildRequest().get();
            if (deltaCollectionPage != null) {
                for (Message message : deltaCollectionPage.getCurrentPage()) {
                    filterMessages(message, startOfTodayUtc, messageIds);
                }
            } else {
                break;
            }
        }
        return deltaCollectionPage;
    }
}
