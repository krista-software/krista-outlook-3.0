package app.krista.extensions.essentials.collaboration.outlook3.impl.util;

import com.google.gson.*;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

public class Constants {

    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(OffsetDateTime.class, (JsonSerializer<OffsetDateTime>) (src, typeOfSrc, context) -> new JsonPrimitive(src.toEpochSecond() * 1000))
            .registerTypeAdapter(OffsetDateTime.class, (JsonDeserializer<OffsetDateTime>) (json, typeOfT, context) -> OffsetDateTime.ofInstant(Instant.ofEpochMilli(json.getAsLong()), ZoneOffset.UTC))
            .create();
    public static final String INVALID_STATE_PARAMETERS = "Unable to process your request due to invalid session data. Please try again or contact support if the issue persists.";
    public static final String WS_CONTACT = "wsContact";
    public static final String COMMA = ",";
    public static final String EMPTY_STRING = "";
    public static final String INVALID_MAIL_ADDRESS = "The email address you entered is not valid. Please check the format (e.g., user@example.com) and try again.";
    public static final String ONE_INVALID_MAIL = "One or more email addresses are invalid. Please verify all recipient email addresses and ensure they follow the correct format (e.g., user@example.com).";
    public static final String REPLY_TO_ALL_REQUEST_FAILED = "We couldn't send your reply to all recipients. Please verify that all recipient email addresses are valid and try again.";
    public static final String INVALID_MESSAGE_ID = "The message you're trying to access could not be found. It may have been deleted or moved. Please refresh your inbox and try again.";
    public static final String INCORRECT_FOLDER_NAME = "The folder name you specified doesn't exist in your mailbox. Please check the folder name and try again. Common folders include: Inbox, Sent Items, Drafts, and Archive.";
    public static final String READ = "read";
    public static final String UNREAD = "unread";
    public static final String MARK_MESSAGE_REQUEST_FAILED = "We couldn't update the message status. The message may have been deleted. Please try again.";
    public static final String FETCH_MAIL_FAILED_NO_FOLDER = "The folder you're trying to access doesn't exist in your mailbox. Please verify the folder name or create the folder first. Common folders include: Inbox, Sent Items, Drafts, and Archive.";
    public static final String SUCCESS = "success";
    public static final String PREFER = "Prefer";
    public static final String SUBSCRIPTION_ID = "subscriptionId";
    public static final String LIFECYCLE_EVENT = "lifecycleEvent";
    public static final String SUBSCRIPTION_REMOVED = "subscriptionRemoved";
    public static final String REAUTHORIZATION_REQUIRED = "reauthorizationRequired";
    public static final String SEARCH_STRING_IS_EMPTY_OR_NULL = "Please enter a search term to find messages. The search field cannot be empty.";
    public static final String FOLDER_NAME_LIST_IS_EMPTY_OR_NULL = "Please specify at least one folder name to continue. The folder list cannot be empty.";
    public static final String FOLDER_NAME_NOT_FOUND = "The folder you specified doesn't exist in your mailbox. Please check the folder name or create it first. You can view available folders in your email client.";
    public static final String FOLDER_NAME_IS_EMPTY_OR_NULL = "Please enter a folder name to continue. The folder name field cannot be empty.";
    public static final String MESSAGE_ID_IS_EMPTY_OR_NULL = "Please provide a valid message ID to continue. The message ID field cannot be empty.";
    public static final String FOLDER_ID_IS_NULL_OR_EMPTY = "Please provide a valid folder ID to continue. The folder ID field cannot be empty.";
    public static final String EMAIL_ADDRESS_IS_EMPTY_OR_NULL = "Please enter an email address to continue. The email address field cannot be empty.";
    public static final String RECIPIENT_IS_EMPTY_OR_NULL = "Please add at least one recipient to send this message. The recipient field cannot be empty.";
    public static final String BODY_CONTENT_TYPE_HTML = "outlook.body-content-type=\"html\"";
    public static final String BODY_CONTENT_TYPE_TEXT = "outlook.body-content-type=\"text\"";
    public static final String UNAUTHORISED_USER = "You don't have permission to access this resource. Please contact your administrator to request access or verify your account credentials.";
    public static final String USER_AUTHENTICATED_SUCCESSFULLY_SAVE_THE_CHANGES = "Authentication successful! Please click 'Save' to apply your changes and complete the setup.";
    public static final String ERROR_OCCURRED_DURING_AUTHORIZATION = "We encountered an issue while trying to authenticate your account. Please try again or contact your administrator if the problem persists.";
    public static final String ERROR_DESCRIPTION = "error_description";
    public static final String AUTH_URL_QUERY_PARAMS = "&access_type=offline&approval_prompt=force&prompt=select_account";
    public static final String FAILED_TO_SEARCH_FOR_LABELS = "We couldn't retrieve your folder labels. Please try refreshing your mailbox and try again.";
    public static final String SEARCH_CONDITIONS_NOT_FOUND = "No search criteria were provided. Please specify at least one search condition (e.g., sender, subject, date range) and try again.";
    public static final String TEXT = "Text";
    public static final String MESSAGE_ID = "messageId";
    public static final String MAIL_RECEIVED = "mailReceived";
    public static final String VALIDATION_TOKEN = "validationToken";
    public static final String RESOURCE_DATA = "resourceData";
    public static final String VALUE = "value";
    public static final String ID = "id";
    public static final String HASH = "#";
    public static final String ERROR_OCCURRED_DURING_UPLOADING_ATTACHMENT = "We couldn't upload one or more attachments. Please check that:\n• The file size doesn't exceed the limit (typically 25MB per file)\n• The file is not corrupted or in use by another program\nThen try again.";
    public static final String APPLICATION_X_BINARY = "application/x-binary";
    public static final String MICROSOFT_GRAPH_FILE_ATTACHMENT = "#microsoft.graph.fileAttachment";
    public static final String FAILED_TO_READ_THE_GIVEN_FILE = "We couldn't read the file you're trying to attach. Please verify that:\n• The file exists and hasn't been moved or deleted\n• You have permission to access the file\n• The file is not corrupted\nThen try again.";
    public static final String ERROR_OCCURRED_DURING_READ_FILE_ATTACHMENT = "We encountered an error while reading the attachment. The file may be corrupted or inaccessible. Please try downloading the attachment again or contact the sender for a new copy.";
    public static final String BR_TAG = "<br>";
    public static final String NEW_LINE = "\n";
    public static final String FORWARD_MAIL_REQUEST_FAILED = "We couldn't forward your message. Please verify that all recipient email addresses are valid and try again.";
    public static final String HTML = "HTML";
    public static final String SEND_MAIL_REQUEST_FAILED = "We couldn't send your message. Please verify that:\n• All recipient email addresses are valid\n• Your mailbox has sufficient storage space\nThen try sending again.";
    public static final String SEND_MAIL_WITH_TABLE_REQUEST_FAILED = "We couldn't send your message with the table. Please verify that the table data is properly formatted and try again.";
    public static final String REPLY_TO_MAIL_REQUEST_FAILED = "We couldn't send your reply. Please verify that the original message still exists and try again.";
    public static final String DATA = "Data";
    public static final String INVALID_TASK_ID = "The task ID you provided is not valid or the task has expired. Please start a new operation or verify the task ID and try again.";
    public static final String FORWARD_SLASH = "/";
    public static final String REQUIRED_SCOPE = "openid offline_access Mail.Send Mail.ReadWrite Mail.Send.Shared Mail.ReadWrite.Shared MailboxSettings.ReadWrite";
    public static final String SCOPE_SEPARATOR = " ";
    public static final String ORG_AUTHORITY = "https://login.microsoftonline.com/organizations/";
    public static final String AUTHORITY = "https://login.microsoftonline.com/";
    public static final String USER_ID = "userId";
    public static final String CLIENT_USER_ID = "clientUserId";
    public static final String AUTH_CONTEXT_ID = "authContextId";
    public static final String UNSUPPORTED_AUTH = "The authentication type you selected is not supported. Please choose either 'Public' (for user authentication) or 'Private' (for service account authentication) and try again.";
    public static final String REFRESH_TOKEN_EXPIRED = "Your session has expired and you need to sign in again. Please click the 'Authorize' button to reconnect your account.";
    public static final String FAILED_TO_GET_ACCOUNT = "We couldn't retrieve your account information. Please verify that:\n• You're signed in with the correct account\n• Your account has the necessary permissions\nThen try again or contact your administrator.";
    public static final String EXTENSION_FORWARD_PATH = "/rest/outlook/v3/oauth/callback";
    public static final String ERROR_OCCURRED_DURING_FETCHING_ATTACHMENTS = "We couldn't download one or more attachments. The attachment may be too large or corrupted. Please try again.";
    public static final String FAILED_TO_MARK_THE_MESSAGE_AS_READ = "We couldn't update the message status. The message may have been deleted or moved. Please refresh your inbox and try again.";
    public static final String FAILED_TO_MOVE_MESSAGE = "We couldn't move the message to the specified folder. Please verify that:\n• The destination folder exists\n• The message hasn't been deleted\n• You have permission to move messages\nThen try again.";
    public static final String MICROSOFT_GRAPH_ITEM_ATTACHMENT_ITEM = "microsoft.graph.itemattachment/item";
    public static final String WEB_LINK = "webLink";
    public static final String A_TAG = "</a>";
    public static final String FOLDER_PATH_IS_EMPTY_OR_NULL = "Please provide a folder path to continue. The folder path field cannot be empty.";
    public static final String CHILD_FOLDER_ID_IS_EMPTY_OR_NULL = "Please provide a subfolder ID to continue. The subfolder ID field cannot be empty.";
    public static final String INCORRECT_PAGE_NUMBER = "The page number you entered is not valid. Please enter a positive number (e.g., 1, 2, 3) and try again.";
    public static final String PAGE_SIZE_UP_TO_15_MESSAGES_IS_CURRENTLY_SUPPORTED_FOR_FETCH_MAIL_BY_LABEL_REQUEST = "You can retrieve up to 15 messages per page. Please enter a number between 1 and 15 for the page size.";
    public static final String INCORRECT_PAGE_SIZE_VALUE_FOR_FETCHING_MAILS = "The page size you entered is not valid. Please enter a positive number that doesn't exceed the maximum allowed (typically 50 messages per page).";
    public static final String CHILD_FOLDER_NAME_IS_EMPTY_OR_NULL = "Please provide a subfolder name to continue. The subfolder name field cannot be empty.";
    public static final String CREATED = "created";
    public static final String REST_OUTLOOK_MAIL_NOTIFICATION = "/rest/outlook/mailNotification";
    public static final String YYYY_MM_DD_T_HH_MM_SS_SSSSSSS_Z = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSS'Z'";
    public static final String ME_MAIL_FOLDERS_INBOX_MESSAGES = "/mailFolders('Inbox')/messages";
    public static final String REST_OUTLOOK_LIFECYCLE_NOTIFICATION = "/rest/outlook/lifecycleNotification";
    public static final String SENT_ITEMS = "Sent Items";
    public static final String INBOX = "Inbox";
    public static final String FOLDER_WITH_ID = "The folder with ID '";
    public static final String NOT_FOUND = "' could not be found in your mailbox. It may have been deleted or renamed. Please verify the folder ID or refresh your folder list.";
    public static final String NO_MESSAGE_FOUND_FOR_MESSAGE_ID = "We couldn't find a message with ID: ";
    public static final String GOT_ERROR_FOR_AUTHENTICATION_SO_SENDING_FOR_RE_AUTHENTICATION = "Your authentication has expired. Please sign in again to continue.";
    public static final String GOT_ERROR_FOR_AUTHENTICATION_SO_SENDING_FOR_AUTHENTICATION = "Authentication is required to access this resource. Please sign in to continue.";
    public static final String PLEASE_PROVIDE_LIST_OF_ENTITY_VALUES = "Please provide a list of values to continue. The data field cannot be empty.";
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
    public static final String AUTHORIZATION_PROMPT = "To complete the setup, please:\n1. Click the 'Authorize' button to connect your Microsoft account\n2. Sign in and grant the requested permissions\n3. Click 'Validate Attributes' to verify the connection\n4. Click 'Save' to apply your changes\n\nThis ensures your account is properly connected before saving.";
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
    public static final String FAILED_TO_SAVE_ATTRIBUTES = "We couldn't save your configuration. Please verify that all required fields are filled in correctly and try again. If the issue persists, contact your administrator.";
    public static final String IS_SUCCESSFUL = "Is Successful";
    public static final String IS_FORWARDED = "Is Forwarded";
    public static final String DELTA_TOKEN = "Delta Token";
    public static final String MAIL_SELECT_FIELDS = "id,subject,body,bodyPreview,receivedDateTime,sentDateTime,from,toRecipients,ccRecipients,bccRecipients,replyTo,isRead,hasAttachments,importance,conversationId,uniqueBody";
    public static final String SELECT_QUERY = "$select";
    public static final String MAILBOX_SETTINGS = "mailboxSettings";
    public static final String UTC = "UTC";
    public static final String DATE_TIME_FORMAT_PATTERN = "dd MMMM yyyy HH:mm";
    public static final String ORIGINAL_MESSAGE_MARKER = "-----Original Message-----";
    public static final String HTML_BR_CLEANUP_REGEX = "(<br\\s*/?>\\s*)+$";
    public static final String WHITESPACE_CLEANUP_REGEX = "\\s+$";

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

    //save configuration constants
    public static final String IS_CONFIGURATION_SUCCESSFUL = "Is Configuration Successful";
    public static final String EXTENSION_RESPONSE_META  = "Extension Response Meta";

    public static final String BASE_URL_FORMAT = "https://login.microsoftonline.com/%s/oauth2/v2.0/token";
    public static final String REQUEST_BODY_SCOPE_AND_GRANT_TYPE =
            "&scope=https://graph.microsoft.com/.default&grant_type=client_credentials";

    public static final String ERROR_INVALID_CLIENT_ID = "AADSTS700016";
    public static final String ERROR_INVALID_TENANT_ID = "AADSTS900023";

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

    public static final Map<String, String> WINDOWS_TO_IANA = Map.ofEntries(
            Map.entry("Dateline Standard Time", "Etc/GMT+12"),
            Map.entry("UTC-11", "Etc/GMT+11"),
            Map.entry("Aleutian Standard Time", "America/Adak"),
            Map.entry("Hawaiian Standard Time", "Pacific/Honolulu"),
            Map.entry("Marquesas Standard Time", "Pacific/Marquesas"),
            Map.entry("Alaskan Standard Time", "America/Anchorage"),
            Map.entry("UTC-09", "Etc/GMT+9"),
            Map.entry("Pacific Standard Time (Mexico)", "America/Tijuana"),
            Map.entry("UTC-08", "Etc/GMT+8"),
            Map.entry("Pacific Standard Time", "America/Los_Angeles"),
            Map.entry("US Mountain Standard Time", "America/Phoenix"),
            Map.entry("Mountain Standard Time (Mexico)", "America/Chihuahua"),
            Map.entry("Mountain Standard Time", "America/Denver"),
            Map.entry("Central America Standard Time", "America/Guatemala"),
            Map.entry("Central Standard Time", "America/Chicago"),
            Map.entry("Easter Island Standard Time", "Pacific/Easter"),
            Map.entry("Central Standard Time (Mexico)", "America/Mexico_City"),
            Map.entry("Canada Central Standard Time", "America/Regina"),
            Map.entry("SA Pacific Standard Time", "America/Bogota"),
            Map.entry("Eastern Standard Time (Mexico)", "America/Cancun"),
            Map.entry("Eastern Standard Time", "America/New_York"),
            Map.entry("Haiti Standard Time", "America/Port-au-Prince"),
            Map.entry("Cuba Standard Time", "America/Havana"),
            Map.entry("US Eastern Standard Time", "America/Indianapolis"),
            Map.entry("Turks And Caicos Standard Time", "America/Grand_Turk"),
            Map.entry("Paraguay Standard Time", "America/Asuncion"),
            Map.entry("Atlantic Standard Time", "America/Halifax"),
            Map.entry("Venezuela Standard Time", "America/Caracas"),
            Map.entry("Central Brazilian Standard Time", "America/Cuiaba"),
            Map.entry("SA Western Standard Time", "America/La_Paz"),
            Map.entry("Pacific SA Standard Time", "America/Santiago"),
            Map.entry("Newfoundland Standard Time", "America/St_Johns"),
            Map.entry("Tocantins Standard Time", "America/Araguaina"),
            Map.entry("E. South America Standard Time", "America/Sao_Paulo"),
            Map.entry("SA Eastern Standard Time", "America/Cayenne"),
            Map.entry("Argentina Standard Time", "America/Argentina/Buenos_Aires"),
            Map.entry("Greenland Standard Time", "America/Godthab"),
            Map.entry("Montevideo Standard Time", "America/Montevideo"),
            Map.entry("Magallanes Standard Time", "America/Punta_Arenas"),
            Map.entry("Saint Pierre Standard Time", "America/Miquelon"),
            Map.entry("Bahia Standard Time", "America/Bahia"),
            Map.entry("UTC-02", "Etc/GMT+2"),
            Map.entry("Azores Standard Time", "Atlantic/Azores"),
            Map.entry("Cape Verde Standard Time", "Atlantic/Cape_Verde"),
            Map.entry("UTC", "Etc/UTC"),
            Map.entry("GMT Standard Time", "Europe/London"),
            Map.entry("Greenwich Standard Time", "Atlantic/Reykjavik"),
            Map.entry("Morocco Standard Time", "Africa/Casablanca"),
            Map.entry("W. Europe Standard Time", "Europe/Berlin"),
            Map.entry("Central Europe Standard Time", "Europe/Budapest"),
            Map.entry("Romance Standard Time", "Europe/Paris"),
            Map.entry("Central European Standard Time", "Europe/Warsaw"),
            Map.entry("W. Central Africa Standard Time", "Africa/Lagos"),
            Map.entry("Jordan Standard Time", "Asia/Amman"),
            Map.entry("GTB Standard Time", "Europe/Bucharest"),
            Map.entry("Middle East Standard Time", "Asia/Beirut"),
            Map.entry("Egypt Standard Time", "Africa/Cairo"),
            Map.entry("E. Europe Standard Time", "Europe/Chisinau"),
            Map.entry("Syria Standard Time", "Asia/Damascus"),
            Map.entry("West Bank Standard Time", "Asia/Hebron"),
            Map.entry("South Africa Standard Time", "Africa/Johannesburg"),
            Map.entry("FLE Standard Time", "Europe/Kiev"),
            Map.entry("Israel Standard Time", "Asia/Jerusalem"),
            Map.entry("Kaliningrad Standard Time", "Europe/Kaliningrad"),
            Map.entry("Sudan Standard Time", "Africa/Khartoum"),
            Map.entry("Libya Standard Time", "Africa/Tripoli"),
            Map.entry("Namibia Standard Time", "Africa/Windhoek"),
            Map.entry("Arabic Standard Time", "Asia/Baghdad"),
            Map.entry("Turkey Standard Time", "Europe/Istanbul"),
            Map.entry("Arab Standard Time", "Asia/Riyadh"),
            Map.entry("Belarus Standard Time", "Europe/Minsk"),
            Map.entry("Russian Standard Time", "Europe/Moscow"),
            Map.entry("E. Africa Standard Time", "Africa/Nairobi"),
            Map.entry("Iran Standard Time", "Asia/Tehran"),
            Map.entry("Arabian Standard Time", "Asia/Dubai"),
            Map.entry("Astrakhan Standard Time", "Europe/Astrakhan"),
            Map.entry("Azerbaijan Standard Time", "Asia/Baku"),
            Map.entry("Russia Time Zone 3", "Europe/Samara"),
            Map.entry("Mauritius Standard Time", "Indian/Mauritius"),
            Map.entry("Saratov Standard Time", "Europe/Saratov"),
            Map.entry("Georgian Standard Time", "Asia/Tbilisi"),
            Map.entry("Caucasus Standard Time", "Asia/Yerevan"),
            Map.entry("Afghanistan Standard Time", "Asia/Kabul"),
            Map.entry("West Asia Standard Time", "Asia/Tashkent"),
            Map.entry("Ekaterinburg Standard Time", "Asia/Yekaterinburg"),
            Map.entry("Pakistan Standard Time", "Asia/Karachi"),
            Map.entry("Qyzylorda Standard Time", "Asia/Qyzylorda"),
            Map.entry("India Standard Time", "Asia/Kolkata"),
            Map.entry("Sri Lanka Standard Time", "Asia/Colombo"),
            Map.entry("Nepal Standard Time", "Asia/Kathmandu"),
            Map.entry("Central Asia Standard Time", "Asia/Almaty"),
            Map.entry("Bangladesh Standard Time", "Asia/Dhaka"),
            Map.entry("Omsk Standard Time", "Asia/Omsk"),
            Map.entry("Myanmar Standard Time", "Asia/Yangon"),
            Map.entry("SE Asia Standard Time", "Asia/Bangkok"),
            Map.entry("Altai Standard Time", "Asia/Barnaul"),
            Map.entry("W. Mongolia Standard Time", "Asia/Hovd"),
            Map.entry("North Asia Standard Time", "Asia/Krasnoyarsk"),
            Map.entry("N. Central Asia Standard Time", "Asia/Novosibirsk"),
            Map.entry("Tomsk Standard Time", "Asia/Tomsk"),
            Map.entry("China Standard Time", "Asia/Shanghai"),
            Map.entry("North Asia East Standard Time", "Asia/Irkutsk"),
            Map.entry("Singapore Standard Time", "Asia/Singapore"),
            Map.entry("W. Australia Standard Time", "Australia/Perth"),
            Map.entry("Taipei Standard Time", "Asia/Taipei"),
            Map.entry("Ulaanbaatar Standard Time", "Asia/Ulaanbaatar"),
            Map.entry("North Korea Standard Time", "Asia/Pyongyang"),
            Map.entry("Korea Standard Time", "Asia/Seoul"),
            Map.entry("Transbaikal Standard Time", "Asia/Chita"),
            Map.entry("Yakutsk Standard Time", "Asia/Yakutsk"),
            Map.entry("Tokyo Standard Time", "Asia/Tokyo")
    );

    private Constants() {
    }
}
