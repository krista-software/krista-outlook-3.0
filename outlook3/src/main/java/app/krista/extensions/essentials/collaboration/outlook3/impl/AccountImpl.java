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
        return providerFactory.create();
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
        } catch (Exception cause) {
            LOGGER.error("Error getting email with ID {}: {}", emailId, cause.getMessage(), cause);
            return null;
        }
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

        LOGGER.error("Email with ID: {} could not be retrieved after {} attempts", emailId, maxAttempts);
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

    @Override
    public List<String> fetchNotificationDeltaQuery() {
        OffsetDateTime startOfTodayUtc = OffsetDateTime.now(ZoneOffset.UTC).minusDays(1);

        String deltaLink = getProvider().getDeltaLink();
        LOGGER.info("Search for the existing delta link: {}", deltaLink);
        MessageDeltaCollectionPage deltaCollectionPage = null;
        UserRequestBuilder userRequestBuilder = getUserRequestBuilder(null, null);

        if (StringUtils.isNotEmpty(deltaLink)) {
            try {
                LOGGER.info("Fetching delta link using last checkpoint : {}", deltaLink);
                deltaCollectionPage = new MessageDeltaCollectionRequestBuilder(deltaLink, userRequestBuilder.getClient(), null).buildRequest().get();
            } catch (GraphServiceException gse) {
                // Handle 410 Gone - delta token expired or sync state not found
                if (gse.getResponseCode() == 410 ||
                    (gse.getMessage() != null && gse.getMessage().contains("SyncStateNotFound"))) {
                    LOGGER.warn("Delta token expired or sync state not found (HTTP 410). Error: {}. Clearing expired token and restarting delta query from scratch.", gse.getMessage());

                    // Clear the expired delta token
                    getProvider().storeDeltaLink(null);

                    // Restart delta query from the beginning
                    LOGGER.info("Restarting delta query without checkpoint.");
                    deltaCollectionPage = userRequestBuilder.mailFolders("Inbox").messages().delta().buildRequest().get();
                } else {
                    // Re-throw other GraphServiceExceptions
                    LOGGER.error("GraphServiceException while fetching delta: {}", gse.getMessage(), gse);
                    throw gse;
                }
            }
        } else {
            LOGGER.info("Fetching delta link using start.");
            deltaCollectionPage = userRequestBuilder.mailFolders("Inbox").messages().delta().buildRequest().get();
        }

        LOGGER.info("Notification from current page being started.");
        List<String> messageIds = new ArrayList<>();

        if (deltaCollectionPage == null) {
            return messageIds;
        }
        for (Message message : deltaCollectionPage.getCurrentPage()) {
            filterMessages(message, startOfTodayUtc, messageIds);
        }

        deltaCollectionPage = getMessageDeltaCollectionFromNextPages(deltaLink, deltaCollectionPage, startOfTodayUtc, messageIds);

        LOGGER.info("All the Notification being fetched Storing new delta link.");
        if (deltaCollectionPage != null) {
            getProvider().storeDeltaLink(deltaCollectionPage.deltaLink);
        }
        return messageIds;
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

    @Nullable
    private MessageDeltaCollectionPage getMessageDeltaCollectionFromNextPages(String deltaLink, MessageDeltaCollectionPage deltaCollectionPage, OffsetDateTime startOfTodayUtc, List<String> messageIds) {
        LOGGER.info("Notification being fetched from current page moving to next page.");
        while (deltaCollectionPage != null && deltaCollectionPage.getNextPage() != null) {
            deltaCollectionPage = deltaCollectionPage.getNextPage().buildRequest().get();
            if (deltaLink != null) {
                if (deltaCollectionPage != null) {
                    for (Message message : deltaCollectionPage.getCurrentPage()) {
                        filterMessages(message, startOfTodayUtc, messageIds);
                    }
                } else {
                    break;
                }
            }
        }
        return deltaCollectionPage;
    }
}
