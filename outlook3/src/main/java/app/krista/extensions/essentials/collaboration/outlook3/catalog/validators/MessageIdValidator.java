package app.krista.extensions.essentials.collaboration.outlook3.catalog.validators;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.extresp.FieldTypes;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.extresp.OutlookResources;
import app.krista.extensions.essentials.collaboration.outlook3.service.Account;

import java.util.Map;

public class MessageIdValidator implements Validator {

    private final Account account;

    public MessageIdValidator(Account account) {
        this.account = account;
    }

    @Override
    public Boolean validate(String resourceId, Map<ValidationResource, String> context) {
            account.getEmail(resourceId);
            return true;
    }

    @Override
    public String getFetchFieldName() {
        return OutlookResources.MESSAGE_ID;
    }

    @Override
    public String getFieldType() {
        return FieldTypes.TEXT_FIELD;
    }

    @Override
    public String getFetchStepMessage() {
        return "Please enter valid Message ID.";
    }

    @Override
    public String getConfirmationStepMessage(String resourceId, Map<ValidationResource, String> context) {
        return String.format("The provided Message ID: %s does not exist.", resourceId);
    }

    @Override
    public String getErrMessage(String resourceId) {
        return String.format("Invalid Message ID: %s", resourceId);
    }
}
