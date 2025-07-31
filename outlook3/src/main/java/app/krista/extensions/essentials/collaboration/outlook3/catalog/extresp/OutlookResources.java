package app.krista.extensions.essentials.collaboration.outlook3.catalog.extresp;

import app.krista.model.field.util.GsonJsonMapper;

public class OutlookResources {
    private OutlookResources(){}

    public static final GsonJsonMapper JSON_MAPPER = GsonJsonMapper.getInstance();

    public static final String MESSAGE_ID = "Message ID";
    public static final String FOLDER_NAME = "Folder Name";
    public static final String CC = "Cc";
    public static final String TO = "To";
    public static final String BCC = "Bcc";
    public static final String REPLY_TO = "Reply To";
    public static final String QUERY = "Query";
    public static final String LABEL = "Label";
    public static final String CATEGORY = "Category";
    public static final String STATE_ID = "stateId";
    public static final String MESSAGE = "Message";
    public static final String ATTACHMENTS = "Attachments";
    public static final String BODY_TYPE = "BodyType";
    public static final String SUBJECT = "Subject";
    public static final String ENTITY_LIST = "Entity List";
    public static final String REMOVE_ENTITY_FIELD_FROM_TABLE = "Remove Entity Field From Table";
    public static final String PAGE_NUMBER = "Page Number";
    public static final String PAGE_SIZE = "Page Size";
    public static final String CREATE_CATEGORY = "Create Category";
    public static final String PREFERENCE = "Preference";
    public static final String INCLUDE_EMAIL_THREAD = "Include Email Thread";
}
