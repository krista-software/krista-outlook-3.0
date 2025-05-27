package app.krista.extensions.essentials.collaboration.outlook3.impl;

import app.krista.extension.authorization.MustAuthorizeException;
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
import com.microsoft.graph.options.HeaderOption;
import com.microsoft.graph.options.Option;
import com.microsoft.graph.options.QueryOption;
import com.microsoft.graph.requests.*;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.jvnet.hk2.annotations.ContractsProvided;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@ContractsProvided(Account.class)
public class AccountImpl implements Account {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountImpl.class);

    private final GraphServiceClientProvider provider;

    @Inject
    public AccountImpl(GraphServiceClientProviderFactory factory) {
        this(factory.create());
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
        Folder folder = getFolderByName(folderNames.get(0), null, null);
        if (folderNames.size() > 1 && folder != null) {
            return folder.getChildFolderByName(folderNames.subList(1, folderNames.size()));
        }
        return folder;
    }

    @Override
    public Folder getFolderByName(String folderName, Boolean useEmail, String accountID) {
        LOGGER.info("getFolderByName for folderName {}, useEmail {}, accountID {}", folderName, useEmail, accountID);
        if (Validators.isStringNullOrBlank(folderName)) {
            LOGGER.warn(Constants.FOLDER_NAME_IS_EMPTY_OR_NULL);
            throw new IllegalArgumentException(Constants.FOLDER_NAME_IS_EMPTY_OR_NULL);
        }
        MailFolderCollectionPage page = getFoldersRequestBuilder(useEmail, accountID).buildRequest().get();
        while (page != null && !page.getCurrentPage().isEmpty()) {
            for (MailFolder folderInPage : page.getCurrentPage()) {
                if (folderInPage.displayName != null && Objects.equals(folderName.toLowerCase(), folderInPage.displayName.toLowerCase())) {
                    return new FolderImpl(this, provider, folderInPage);
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
            return new FolderImpl(this, provider, mailFolder);
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
    public Email getEmail(String emailMessageId) {
        try {
            if (Validators.isStringNullOrBlank(emailMessageId)) {
                throw new IllegalArgumentException(Constants.MESSAGE_ID_IS_EMPTY_OR_NULL);
            }
            Message message = getUserRequestBuilder(null, null).messages(emailMessageId).buildRequest(new HeaderOption(Constants.PREFER, Constants.BODY_CONTENT_TYPE_HTML)).get();
            return new EmailImpl(provider, message);
        } catch (MustAuthorizeException cause) {
            LOGGER.error(cause.getMessage(), cause);
            throw cause;
        } catch (RuntimeException cause) {
            LOGGER.error(Constants.NO_MESSAGE_FOUND_FOR_MESSAGE_ID, cause);
            throw new IllegalStateException(Constants.NO_MESSAGE_FOUND_FOR_MESSAGE_ID, cause);
        }
    }

    @Override
    public List<Email> searchEmails(String searchString) {
        if (Validators.isStringNullOrBlank(searchString)) {
            throw new IllegalArgumentException(Constants.SEARCH_STRING_IS_EMPTY_OR_NULL);
        }
        // Check for special characters to add escape character
        searchString = searchString.replaceAll("([\"\\\\])", "\\\\$1");
        LinkedList<Option> requestOptions = new LinkedList<>();
        requestOptions.add(new QueryOption("$search", "\"" + searchString + "\""));
        requestOptions.add(new HeaderOption(Constants.PREFER, Constants.BODY_CONTENT_TYPE_HTML));
        MessageCollectionPage messages = getUserRequestBuilder(null, null).messages().buildRequest(requestOptions).top(15).get();
        if (messages == null)
            return List.of();
        return messages.getCurrentPage().stream().map(e -> new EmailImpl(provider, e)).collect(Collectors.toList());
    }

    @Override
    public EmailBuilder newEmail() {
        return EmailBuilderImpl.create(provider);
    }

    private MailFolderCollectionRequestBuilder getFoldersRequestBuilder(Boolean useEmail, String accountID) {
        return getUserRequestBuilder(useEmail, accountID).mailFolders();
    }

    private UserRequestBuilder getUserRequestBuilder(Boolean useEmail, String accountID) {
        return provider.getUserRequestBuilder(useEmail, accountID);
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
    public OutlookCategory createCategory(String category) {
        LOGGER.info("Creating category with display name: {}", category);
        OutlookCategory outlookCategory = new OutlookCategory();
        outlookCategory.displayName = category;

        try {
            return getUserRequestBuilder(null, null).outlook().masterCategories().buildRequest()
                    .post(outlookCategory);
        } catch (GraphServiceException cause) {
            LOGGER.error("Failed to create new category with name: {}", category, cause);
            return null;
        }
    }

    @Override
    public List<String> fetchNotificationDeltaQuery() {
        String deltaLink = provider.getDeltaLink();
        LOGGER.info("Search for the existing delta link: {}", deltaLink);
        MessageDeltaCollectionPage deltaCollectionPage = null;
        UserRequestBuilder userRequestBuilder = getUserRequestBuilder(null, null);
        if (StringUtils.isNotEmpty(deltaLink)) {
            LOGGER.info("Fetching delta link using last checkpoint : {}", deltaLink);
            deltaCollectionPage = new MessageDeltaCollectionRequestBuilder(deltaLink, userRequestBuilder.getClient(), null).buildRequest().get();
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
            messageIds.add(message.id);
        }


        deltaCollectionPage = getMessageDeltaCollectionFromNextPages(deltaLink, deltaCollectionPage, messageIds);

        LOGGER.info("All the Notification being fetched Storing new delta link.");
        if (deltaCollectionPage != null) {
            provider.storeDeltaLink(deltaCollectionPage.deltaLink);
        }
        return messageIds;
    }

    @Nullable
    private MessageDeltaCollectionPage getMessageDeltaCollectionFromNextPages(String deltaLink, MessageDeltaCollectionPage deltaCollectionPage, List<String> messageIds) {
        LOGGER.info("Notification being fetched from current page moving to next page.");
        while (deltaCollectionPage != null && deltaCollectionPage.getNextPage() != null) {
            deltaCollectionPage = deltaCollectionPage.getNextPage().buildRequest().get();
            if(deltaLink != null) {
                if (deltaCollectionPage != null) {
                    for (Message message : deltaCollectionPage.getCurrentPage()) {
                        messageIds.add(message.id);
                    }
                } else {
                    break;
                }
            }
        }
        return deltaCollectionPage;
    }
}
