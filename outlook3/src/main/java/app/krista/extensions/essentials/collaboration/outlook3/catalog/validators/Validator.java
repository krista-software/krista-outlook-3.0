package app.krista.extensions.essentials.collaboration.outlook3.catalog.validators;

import java.util.Map;

public interface Validator {

    public enum ValidationResource {
        MESSAGE_ID,
        FOLDER_NAME,
        CC,
        TO,
        BCC,
        REPLY_TO,
        QUERY,
        LABEL,
        CATEGORY,
        PAGE_NUMBER,
        PAGE_SIZE,
        PREFERENCES
    }

    Boolean validate(String resourceId, Map<ValidationResource, String> context);

    String getFetchFieldName();
    String getFieldType();

    String getFetchStepMessage();
    String getConfirmationStepMessage(String resourceId, Map<ValidationResource, String> context);
    String getErrMessage(String resourceId);
}
