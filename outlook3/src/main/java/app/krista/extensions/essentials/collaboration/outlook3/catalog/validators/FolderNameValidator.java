package app.krista.extensions.essentials.collaboration.outlook3.catalog.validators;

import app.krista.extensions.essentials.collaboration.outlook3.catalog.extresp.FieldTypes;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.extresp.OutlookResources;
import app.krista.extensions.essentials.collaboration.outlook3.impl.util.Constants;
import app.krista.extensions.essentials.collaboration.outlook3.service.Account;
import app.krista.extensions.essentials.collaboration.outlook3.service.Folder;

import java.util.List;
import java.util.Map;

public class FolderNameValidator implements Validator {

    private final Account account;

    public FolderNameValidator(Account account) {
        this.account = account;
    }

    @Override
    public Boolean validate(String resourceId, Map<ValidationResource, String> context) {
        try {
            Folder folder = account.getFolderByName(List.of(resourceId.split(Constants.FORWARD_SLASH)));
            return folder != null;
        }catch (IllegalArgumentException cause){
            return false;
        }
    }

    @Override
    public String getFetchFieldName() {
        return OutlookResources.FOLDER_NAME;
    }

    @Override
    public String getFieldType() {
        return FieldTypes.TEXT_FIELD;
    }

    @Override
    public String getFetchStepMessage() {
        return "Please enter valid Folder Name.";
    }

    @Override
    public String getConfirmationStepMessage(String resourceId, Map<ValidationResource, String> context) {
        return String.format("The provided Folder Name: %s does not exist.", resourceId);
    }

    @Override
    public String getErrMessage(String resourceId) {
        return String.format("Invalid Folder Name: %s", resourceId);
    }
}
