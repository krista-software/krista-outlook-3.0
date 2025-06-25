package app.krista.extensions.essentials.collaboration.outlook3.impl.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

public class Constants {

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final String INVALID_STATE_PARAMETERS = "Invalid state parameters!";
    public static final String WS_CONTACT = "wsContact";
    public static final String COMMA = ",";
    public static final String EMPTY_STRING = "";
    public static final String INVALID_MAIL_ADDRESS = "Please provide correct email id.";
    public static final String ONE_INVALID_MAIL = "At least one recipient is not valid";
    public static final String REPLY_TO_ALL_REQUEST_FAILED = "Reply to all request failed";
    public static final String INVALID_MESSAGE_ID = "Invalid message id";
    public static final String INCORRECT_FOLDER_NAME = "Incorrect folder name";
    public static final String READ = "read";
    public static final String UNREAD = "unread";
    public static final String MARK_MESSAGE_REQUEST_FAILED = "Mark message request failed";
    public static final String FETCH_MAIL_FAILED_NO_FOLDER = "Fetch mails request failed. Given folder does not exist.";
    public static final String SUCCESS = "success";
    public static final String PREFER = "Prefer";
    public static final String SUBSCRIPTION_ID = "subscriptionId";
    public static final String LIFECYCLE_EVENT = "lifecycleEvent";
    public static final String SUBSCRIPTION_REMOVED = "subscriptionRemoved";
    public static final String REAUTHORIZATION_REQUIRED = "reauthorizationRequired";
    public static final String SEARCH_STRING_IS_EMPTY_OR_NULL = "Search string is empty or null.";
    public static final String FOLDER_NAME_LIST_IS_EMPTY_OR_NULL = "Folder name list is empty or null.";
    public static final String FOLDER_NAME_NOT_FOUND = "Folder name not found.";
    public static final String FOLDER_NAME_IS_EMPTY_OR_NULL = "Folder name is empty or null.";
    public static final String MESSAGE_ID_IS_EMPTY_OR_NULL = "Message ID is empty or null.";
    public static final String FOLDER_ID_IS_NULL_OR_EMPTY = "Folder Id is null or empty.";
    public static final String EMAIL_ADDRESS_IS_EMPTY_OR_NULL = "Email address is empty or null.";
    public static final String RECIPIENT_IS_EMPTY_OR_NULL = "Recipient is empty or null.";
    public static final String BODY_CONTENT_TYPE_HTML = "outlook.body-content-type=\"html\"";
    public static final String BODY_CONTENT_TYPE_TEXT = "outlook.body-content-type=\"text\"";
    public static final String UNAUTHORISED_USER = "Unauthorised user.";
    public static final String USER_AUTHENTICATED_SUCCESSFULLY_SAVE_THE_CHANGES = "User authenticated successfully. Save the changes.";
    public static final String ERROR_OCCURRED_DURING_AUTHORIZATION = "Error occurred during authorization";
    public static final String ERROR_DESCRIPTION = "error_description";
    public static final String AUTH_URL_QUERY_PARAMS = "&access_type=offline&approval_prompt=force&prompt=select_account";
    public static final String FAILED_TO_SEARCH_FOR_LABELS = "Failed to search for labels.";
    public static final String SEARCH_CONDITIONS_NOT_FOUND = "Search conditions not found.";
    public static final String TEXT = "Text";
    public static final String MESSAGE_ID = "messageId";
    public static final String MAIL_RECEIVED = "mailReceived";
    public static final String VALIDATION_TOKEN = "validationToken";
    public static final String RESOURCE_DATA = "resourceData";
    public static final String VALUE = "value";
    public static final String ID = "id";
    public static final String HASH = "#";
    public static final String ERROR_OCCURRED_DURING_UPLOADING_ATTACHMENT = "Error occurred during uploading attachment";
    public static final String APPLICATION_X_BINARY = "application/x-binary";
    public static final String MICROSOFT_GRAPH_FILE_ATTACHMENT = "#microsoft.graph.fileAttachment";
    public static final String FAILED_TO_READ_THE_GIVEN_FILE = "Failed to read the given file!";
    public static final String ERROR_OCCURRED_DURING_READ_FILE_ATTACHMENT = "Error occurred during read file attachment";
    public static final String BR_TAG = "<br>";
    public static final String NEW_LINE = "\n";
    public static final String FORWARD_MAIL_REQUEST_FAILED = "Forward mail request failed ";
    public static final String HTML = "HTML";
    public static final String SEND_MAIL_REQUEST_FAILED = "Send mail request failed ";
    public static final String SEND_MAIL_WITH_TABLE_REQUEST_FAILED = "Send mail with table request failed ";
    public static final String REPLY_TO_MAIL_REQUEST_FAILED = "Reply to mail request failed ";
    public static final String DATA = "Data";
    public static final String INVALID_TASK_ID = "Invalid task ID";
    public static final String FORWARD_SLASH = "/";
    public static final String REQUIRED_SCOPE = "openid offline_access Mail.Send Mail.ReadWrite Mail.Send.Shared Mail.ReadWrite.Shared MailboxSettings.ReadWrite";
    public static final String SCOPE_SEPARATOR = " ";
    public static final String ORG_AUTHORITY = "https://login.microsoftonline.com/organizations/";
    public static final String AUTHORITY = "https://login.microsoftonline.com/";
    public static final String USER_ID = "userId";
    public static final String AUTH_CONTEXT_ID = "authContextId";
    public static final String UNSUPPORTED_AUTH = "Unsupported authentication type provided. Please select either Public or Private.";
    public static final String REFRESH_TOKEN_EXPIRED = "Refresh Token is no longer valid. Please reauthorize yourself";
    public static final String FAILED_TO_GET_ACCOUNT = "Failed to get account";
    public static final String EXTENSION_FORWARD_PATH = "/rest/outlook/v3/oauth/callback";
    public static final String ERROR_OCCURRED_DURING_FETCHING_ATTACHMENTS = "Error occurred during fetching attachments ";
    public static final String FAILED_TO_MARK_THE_MESSAGE_AS_READ = "Failed to mark the message as read!";
    public static final String FAILED_TO_MOVE_MESSAGE = "Failed to move message!";
    public static final String MICROSOFT_GRAPH_ITEM_ATTACHMENT_ITEM = "microsoft.graph.itemattachment/item";
    public static final String WEB_LINK = "webLink";
    public static final String A_TAG = "</a>";
    public static final String FOLDER_PATH_IS_EMPTY_OR_NULL = "Folder path is empty or null.";
    public static final String CHILD_FOLDER_ID_IS_EMPTY_OR_NULL = "Child folder ID is empty or null.";
    public static final String INCORRECT_PAGE_NUMBER = "Incorrect page number value for fetching mails.";
    public static final String PAGE_SIZE_UP_TO_15_MESSAGES_IS_CURRENTLY_SUPPORTED_FOR_FETCH_MAIL_BY_LABEL_REQUEST = "Page size up to 15 messages is currently supported for fetch mail by label request.";
    public static final String INCORRECT_PAGE_SIZE_VALUE_FOR_FETCHING_MAILS = "Incorrect page size value for fetching mails.";
    public static final String CHILD_FOLDER_NAME_IS_EMPTY_OR_NULL = "Child folder name is empty or null.";
    public static final String CREATED = "created";
    public static final String REST_OUTLOOK_MAIL_NOTIFICATION = "/rest/outlook/mailNotification";
    public static final String YYYY_MM_DD_T_HH_MM_SS_SSSSSSS_Z = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSS'Z'";
    public static final String ME_MAIL_FOLDERS_INBOX_MESSAGES = "/mailFolders('Inbox')/messages";
    public static final String REST_OUTLOOK_LIFECYCLE_NOTIFICATION = "/rest/outlook/lifecycleNotification";
    public static final String SENT_ITEMS = "Sent Items";
    public static final String INBOX = "Inbox";
    public static final String FOLDER_WITH_ID = "Folder with id :";
    public static final String NOT_FOUND = " not found.";
    public static final String NO_MESSAGE_FOUND_FOR_MESSAGE_ID = "No message found for message Id :";
    public static final String GOT_ERROR_FOR_AUTHENTICATION_SO_SENDING_FOR_RE_AUTHENTICATION = "Got error for Authentication. So sending for re authentication.";
    public static final String GOT_ERROR_FOR_AUTHENTICATION_SO_SENDING_FOR_AUTHENTICATION = "Got error for Authentication. So sending for authentication";
    public static final String PLEASE_PROVIDE_LIST_OF_ENTITY_VALUES = "Please Provide List of Entity Values.";
    public static final List<String> VALID_DATE_KEYS = List.of("$$date", "$date", "$$from", "$$to", "$$from_date", "$$to_date", "$$end", "$$start", "$$fromDate", "$$toDate", "$$start_date", "$$end_date", "$$startDate", "$$endDate", "$$approved", "$$approved_on", "$$approvedOn", "$$dob", "$$dateOfBirth", "$$date_of_birth", "date", "dates");
    public static final List<String> VALID_TIME_KEYS = List.of("$$time", "$time", "time", "$$startTime", "$$endTime", "$$start_time", "$$end_time");
    public static final String HTML_HEAD = "<html><head>";
    public static final String HTML_TABLE_STYLE = "<style>table {width: 100%; border-collapse: collapse;} th, td {border: 1px solid #dddddd; text-align: left; padding: 8px;}</style>";
    public static final String CLOSE_HEAD_TAG = "</head>";
    public static final String BODY_DIV_TAG = "<body><div><br/>";
    public static final String CLOSE_DIV_TAG = "</div><br/>";
    public static final String TABLE_START = "<table>";
    public static final String TR_TAG = "<tr>";
    public static final String CLOSE_TR_TAG = "</tr>";
    public static final String TH_TAG = "<th>";
    public static final String CLOSE_TH_TAG = "</th>";
    public static final String TD_TAG = "<td>";
    public static final String CLOSE_TD_TAG = "</td>";
    public static final String CLOSE_TABLE_TAG = "</table><br/>";
    public static final String CLOSE_BODY_TAG = "</body></html>";
    public static final String AUTHORIZATION_PROMPT = "Please authorize yourself and click 'Validate Attributes' before saving, so we can proceed safely.";
    public static final String LOCAL_EXTN_URL = "https://extension.local.eng.krista.app";
    public static final String DEFAULT_CALLBACK_PATH = "/extension/api/rest/v3/oauth/callback";
    public static final String LOCAL_EXTN_REPLACE_URL = "http://localhost:8765";
    public static final String PUBLIC = "Public";
    public static final String PRIVATE = "Private";
    public static final String TENANT_ID = "tenantId";
    public static final String CLIENT_ID = "clientId";
    public static final String CLIENT_SECRET = "clientSecret";
    public static final String ALLOW_MAIL_ALERT = "allowMailAlert";
    public static final String AUTH_TYPE = "authType";
    public static final String EMAIL = "email";
    public static final String UNDER_SCORE = "_";
    public static final String FAILED_TO_SAVE_ATTRIBUTES = "Failed to save attributes";
    public static final String IS_SUCCESSFUL = "Is Successful";
    public static final String IS_FORWARDED = "Is Forwarded";
    public static final String DELTA_TOKEN = "Delta Token";

    // Error messages for ExtensionResponse
    public static final String USER_DELETED_ERROR = "Your Microsoft account no longer exists. Please contact your administrator.";
    public static final String USER_DISABLED_ERROR = "Your Microsoft account has been blocked or locked. Please contact your administrator.";
    public static final String APP_NOT_FOUND_ERROR = "We couldn’t find the application in your Microsoft setup. Please contact your administrator.";
    public static final String PERMISSIONS_REVOKED_ERROR = "Your access to Microsoft has been removed. Please contact your administrator.";
    public static final String REFRESH_TOKEN_EXPIRED_ERROR = "Your session has expired. Please contact your administrator.";
    public static final String TENANT_NOT_FOUND_ERROR = "We couldn’t find your Microsoft organization. Please contact your administrator.";
    public static final String SERVICE_UNAVAILABLE_ERROR = "Microsoft is temporarily unavailable. Please contact your administrator.";
    public static final String INVALID_CLIENT_SECRET_ERROR = "Something’s wrong with the application’s connection. Please contact your administrator.";
    public static final String PASSWORD_CHANGED_ERROR = "Your Microsoft password was changed. Please contact your administrator.";

    // Error codes
    public static final String USER_DELETED_CODE = "AADSTS50020";
    public static final String USER_DISABLED_CODE = "AADSTS50057";
    public static final String ACCOUNT_LOCKED_CODE = "AADSTS50053";
    public static final String PASSWORD_EXPIRED_CODE = "AADSTS50055";
    public static final String CONSENT_REVOKED_CODE = "AADSTS65001";
    public static final String CONSENT_REQUIRED_CODE = "AADSTS70019";
    public static final String ROLE_NOT_FOUND_CODE = "AADSTS90094";
    public static final String APP_NOT_FOUND_CODE = "AADSTS700016";
    public static final String TENANT_NOT_FOUND_CODE = "AADSTS90002";
    public static final String SERVICE_UNAVAILABLE_CODE = "AADSTS50000";
    public static final String INVALID_CLIENT_SECRET_CODE = "AADSTS7000215";
    public static final String PASSWORD_CHANGED_CODE = "AADSTS50173";
    public static final String REFRESH_TOKEN_EXPIRED_CODE = "AADSTS700082";
    public static final String REFRESH_TOKEN_REVOKED_CODE = "AADSTS700084";
    public static final String PROOF_OF_POSSESSION_FAILED_CODE = "AADSTS54005";

    // Authentication error keywords for detection
    public static final String KEYWORD_USER_DELETED = "user has been deleted";
    public static final String KEYWORD_ACCOUNT_DISABLED = "account is disabled";
    public static final String KEYWORD_TENANT_NOT_FOUND = "tenant not found";
    public static final String KEYWORD_SERVICE_UNAVAILABLE = "service unavailable";
    public static final String KEYWORD_NETWORK_ERROR = "network error";
    public static final String KEYWORD_INVALID_CLIENT_SECRET = "invalid client secret";
    public static final String KEYWORD_INVALID_CLIENT = "invalid_client";
    public static final String KEYWORD_INSUFFICIENT_SCOPE = "insufficient_scope";
    public static final String KEYWORD_ACCESS_DENIED = "access_denied";
    public static final String KEYWORD_PASSWORD_CHANGED = "changed or reset their password";
    public static final String GENERIC_INVALID_GRANT_CODE = "invalid_grant";

    public static final List<AuthErrorRule> AUTH_ERROR_RULES = List.of(
            new AuthErrorRule(List.of(PASSWORD_CHANGED_CODE, KEYWORD_PASSWORD_CHANGED), PASSWORD_CHANGED_ERROR, true),
            new AuthErrorRule(List.of(USER_DELETED_CODE, KEYWORD_USER_DELETED), USER_DELETED_ERROR, false),
            new AuthErrorRule(List.of(USER_DISABLED_CODE, ACCOUNT_LOCKED_CODE, PASSWORD_EXPIRED_CODE, KEYWORD_ACCOUNT_DISABLED), USER_DISABLED_ERROR, false),
            new AuthErrorRule(List.of(APP_NOT_FOUND_CODE), APP_NOT_FOUND_ERROR, false),
            new AuthErrorRule(List.of(CONSENT_REVOKED_CODE, CONSENT_REQUIRED_CODE, ROLE_NOT_FOUND_CODE, KEYWORD_INSUFFICIENT_SCOPE, KEYWORD_ACCESS_DENIED), PERMISSIONS_REVOKED_ERROR, true),
            new AuthErrorRule(List.of(TENANT_NOT_FOUND_CODE, KEYWORD_TENANT_NOT_FOUND), TENANT_NOT_FOUND_ERROR, false),
            new AuthErrorRule(List.of(SERVICE_UNAVAILABLE_CODE, KEYWORD_SERVICE_UNAVAILABLE, KEYWORD_NETWORK_ERROR), SERVICE_UNAVAILABLE_ERROR, false),
            new AuthErrorRule(List.of(INVALID_CLIENT_SECRET_CODE, KEYWORD_INVALID_CLIENT_SECRET, KEYWORD_INVALID_CLIENT), INVALID_CLIENT_SECRET_ERROR, false),
            new AuthErrorRule(List.of(REFRESH_TOKEN_EXPIRED_CODE, REFRESH_TOKEN_REVOKED_CODE, PROOF_OF_POSSESSION_FAILED_CODE, GENERIC_INVALID_GRANT_CODE), REFRESH_TOKEN_EXPIRED_ERROR, true)
    );

    private Constants() {
    }
}
