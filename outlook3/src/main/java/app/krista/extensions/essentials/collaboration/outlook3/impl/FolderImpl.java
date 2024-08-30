package app.krista.extensions.essentials.collaboration.outlook3.impl;

import app.krista.extensions.essentials.collaboration.outlook3.impl.connectors.GraphServiceClientProvider;
import app.krista.extensions.essentials.collaboration.outlook3.impl.util.Constants;
import app.krista.extensions.essentials.collaboration.outlook3.impl.util.Validators;
import app.krista.extensions.essentials.collaboration.outlook3.service.Account;
import app.krista.extensions.essentials.collaboration.outlook3.service.Email;
import app.krista.extensions.essentials.collaboration.outlook3.service.Folder;
import com.microsoft.graph.models.MailFolder;
import com.microsoft.graph.models.MailFolderMoveParameterSet;
import com.microsoft.graph.models.Message;
import com.microsoft.graph.options.HeaderOption;
import com.microsoft.graph.requests.*;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static app.krista.extensions.essentials.collaboration.outlook3.impl.util.Constants.*;


public class FolderImpl implements Folder {
    private final Account account;
    private final GraphServiceClientProvider provider;
    private final MailFolder mailFolder;

    public FolderImpl(Account account, GraphServiceClientProvider provider, MailFolder mailFolder) {
        this.account = account;
        this.provider = provider;
        this.mailFolder = mailFolder;
    }

    @Override
    public String getFolderId() {
        return mailFolder.id;
    }

    @Override
    public String getFolderName() {
        return mailFolder.displayName;
    }

    @Override
    public Folder getParent() {
        return mailFolder.parentFolderId == null ? null : account.getFolder(mailFolder.parentFolderId);
    }

    @Override
    public List<String> getFolderPath() {
        Folder parentFolder = getParent();
        List<String> names = parentFolder != null ? parentFolder.getFolderPath() : new ArrayList<>(1);
        names.add(getFolderName());
        return names;
    }

    @Override
    public List<String> getChildFolderNames() {
        List<String> folderNames = new ArrayList<>();
        MailFolderCollectionPage childFolderPage = getChildFolderPage();

        while (childFolderPage != null && !childFolderPage.getCurrentPage().isEmpty()) {
            for (MailFolder folder : childFolderPage.getCurrentPage()) {
                folderNames.add(folder.displayName);
            }
            if (childFolderPage.getNextPage() != null) {
                childFolderPage = childFolderPage.getNextPage().buildRequest().get();
            }
        }
        return folderNames;
    }

    @Override
    public Folder getChildFolderByName(String childFolderName) {
        if (Validators.isStringNullOrBlank(childFolderName)) {
            throw new IllegalArgumentException(Constants.CHILD_FOLDER_NAME_IS_EMPTY_OR_NULL);
        }

        MailFolderCollectionPage childFolderPage = getChildFolderPage();
        while (childFolderPage != null && !childFolderPage.getCurrentPage().isEmpty()) {
            for (MailFolder folder : childFolderPage.getCurrentPage()) {
                if (folder.displayName != null && Objects.equals(childFolderName.toLowerCase(), folder.displayName.toLowerCase())) {
                    return new FolderImpl(account, provider, folder);
                }
            }

            MailFolderCollectionRequestBuilder nextPage = childFolderPage.getNextPage();
            if (nextPage != null) {
                childFolderPage = nextPage.buildRequest().get();
            } else {
                break;
            }
        }
        return null;
    }

    @Override
    public Folder getChildFolderByName(List<String> childFolderPath) {
        if (Validators.isListNullOrEmpty(childFolderPath)) {
            throw new IllegalArgumentException(Constants.FOLDER_PATH_IS_EMPTY_OR_NULL);
        }

        Folder childFolder = getChildFolderByName(childFolderPath.get(0));
        if (childFolderPath.size() > 1 && childFolder != null) {
            return childFolder.getChildFolderByName(childFolderPath.subList(1, childFolderPath.size()));
        }
        return childFolder;
    }

    @Override
    public List<String> getChildFolderIds() {
        List<String> folderIds = new ArrayList<>();
        MailFolderCollectionPage childFolderPage = getChildFolderPage();

        while (childFolderPage != null && !childFolderPage.getCurrentPage().isEmpty()) {
            for (MailFolder folder : childFolderPage.getCurrentPage()) {
                folderIds.add(folder.id);
            }
            if (childFolderPage.getNextPage() != null) {
                childFolderPage = childFolderPage.getNextPage().buildRequest().get();
            }
        }
        return folderIds;
    }

    @Nullable
    private MailFolderCollectionPage getChildFolderPage() {
        return Objects.requireNonNull(getFolderRequestBuilder(null))
                .childFolders().buildRequest().get();
    }

    @Override
    public Folder getChildFolder(String childFolderId) {
        if (Validators.isStringNullOrBlank(childFolderId)) {
            throw new IllegalArgumentException(Constants.CHILD_FOLDER_ID_IS_EMPTY_OR_NULL);
        }

        return new FolderImpl(account, provider, Objects.requireNonNull(getFolderRequestBuilder(null))
                .childFolders(childFolderId).buildRequest().get());
    }

    @Override
    public List<Email> getEmails(Double pageNumber, Double pageSize) {
        return getEmailList(pageNumber, pageSize, HTML);
    }

    private @Nullable List<Email> getEmailList(Double pageNumber, Double pageSize, String bodyType) {
        int intPageNumber = validatePageNumber(pageNumber);
        int intPageSize = validatePageSize(pageSize);
        int skipParameter = (intPageNumber - 1) * intPageSize;
        String preference = BODY_CONTENT_TYPE_HTML; // Setting Default Preference to HTML
        if (bodyType.equalsIgnoreCase(TEXT)) {
            preference = BODY_CONTENT_TYPE_TEXT;
        }

        MessageCollectionPage messages = Objects.requireNonNull(getFolderRequestBuilder(null))
                .messages()
                .buildRequest(new HeaderOption(Constants.PREFER, preference))
                .top(intPageSize)
                .skip(skipParameter)
                .get();
        return messages != null ? messages.getCurrentPage().stream().map(e -> new EmailImpl(provider, e)).collect(Collectors.toList()) : null;
    }

    @Override
    public List<Email> getEmails(Double pageNumber, Double pageSize, Map<String, Object> preferences) {
        if (preferences.get("Mail Body").toString().equalsIgnoreCase("Text")) {
            return getEmailList(pageNumber, pageSize, TEXT);
        }
        return getEmailList(pageNumber, pageSize, HTML);
    }

    @Override
    public List<Email> getEmails(Boolean useEmail) {
        List<Email> emails = new ArrayList<>();
        MessageCollectionPage messages = Objects.requireNonNull(getFolderRequestBuilder(useEmail)).messages()
                .buildRequest(new HeaderOption(Constants.PREFER, BODY_CONTENT_TYPE_HTML))
                .get();

        int mailCount = 0;
        while (messages != null && !messages.getCurrentPage().isEmpty() && mailCount < 500) {
            mailCount += messages.getCurrentPage().size();
            for (Message message : messages.getCurrentPage()) {
                emails.add(new EmailImpl(provider, message));
            }

            MessageCollectionRequestBuilder nextPage = messages.getNextPage();
            if (nextPage != null) {
                messages = nextPage.buildRequest().get();
            } else {
                break;
            }
        }
        return emails;
    }

    @Override
    public void moveInto(Folder folder) {
        Objects.requireNonNull(folder);
        MailFolderMoveParameterSet parameterSet = new MailFolderMoveParameterSet();
        parameterSet.destinationId = folder.getFolderId();
        Objects.requireNonNull(getFolderRequestBuilder(null))
                .move(parameterSet).buildRequest().post();
    }

    @Override
    public int getMessageCount() {
        int totalCount = 0;
        MessageCollectionPage messagePage = Objects.requireNonNull(getFolderRequestBuilder(null))
                .messages()
                .buildRequest()
                .get();

        while (messagePage != null && !messagePage.getCurrentPage().isEmpty()) {
            totalCount += messagePage.getCurrentPage().size();
            if (messagePage.getNextPage() != null) {
                messagePage = messagePage.getNextPage().buildRequest().get();
            }
        }
        return totalCount;
    }

    @Override
    public int getUnreadMessageCount() {
        int unreadCount = 0;
        MessageCollectionPage messagePage = Objects.requireNonNull(getFolderRequestBuilder(null)).
                messages().buildRequest().get();

        while (messagePage != null && !messagePage.getCurrentPage().isEmpty()) {
            for (Message message : messagePage.getCurrentPage()) {
                if (message.isRead != null && message.isRead) {
                    unreadCount++;
                }
            }
            if (messagePage.getNextPage() != null) {
                messagePage = messagePage.getNextPage().buildRequest().get();
            }
        }
        return unreadCount;
    }

    private MailFolderRequestBuilder getFolderRequestBuilder(Boolean useEmail) {
        return mailFolder.id != null
                ? provider.getUserRequestBuilder(useEmail, null).mailFolders(mailFolder.id)
                : null;
    }

    private int validatePageSize(Double pageSize) {
        if (pageSize == null) {
            return 15;
        } else if (pageSize < 1) {
            throw new IllegalArgumentException(Constants.INCORRECT_PAGE_SIZE_VALUE_FOR_FETCHING_MAILS);
        } else if (pageSize > 15) {
            throw new IllegalArgumentException(Constants.PAGE_SIZE_UP_TO_15_MESSAGES_IS_CURRENTLY_SUPPORTED_FOR_FETCH_MAIL_BY_LABEL_REQUEST);
        } else {
            return pageSize.intValue();
        }
    }

    private int validatePageNumber(Double pageNumber) {
        if (pageNumber == null) {
            return 1;
        } else if (pageNumber < 1) {
            throw new IllegalArgumentException(Constants.INCORRECT_PAGE_NUMBER);
        } else {
            return pageNumber.intValue();
        }
    }
}
