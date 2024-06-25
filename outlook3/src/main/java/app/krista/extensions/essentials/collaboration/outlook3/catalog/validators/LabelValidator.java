package app.krista.extensions.essentials.collaboration.outlook3.catalog.validators;

import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.extresp.FieldTypes;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.extresp.OutlookResources;
import app.krista.extensions.essentials.collaboration.outlook3.impl.util.Constants;
import app.krista.extensions.essentials.collaboration.outlook3.service.Account;
import app.krista.extensions.essentials.collaboration.outlook3.service.Folder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class LabelValidator implements Validator {

    private final Account account;
    private static final Logger logger = LoggerFactory.getLogger(LabelValidator.class);

    public LabelValidator(Account account) {
        this.account = account;
    }

    @Override
    public Boolean validate(String resourceId, Map<ValidationResource, String> context) {
        try {
            Folder folder = account.getFolderByName(List.of(resourceId.split(Constants.FORWARD_SLASH)));
            return folder != null;
        } catch (MustAuthorizeException cause) {
            logger.info(cause.getMessage());
            throw cause;
        } catch (IllegalArgumentException cause) {
            return false;
        } catch (Exception cause) {
            logger.info(cause.getMessage());
            return false;
        }
    }

    @Override
    public String getFetchFieldName() {
        return OutlookResources.LABEL;
    }

    @Override
    public String getFieldType() {
        return FieldTypes.TEXT_FIELD;
    }

    @Override
    public String getFetchStepMessage() {
        return "Please enter valid Label Name.";
    }

    @Override
    public String getConfirmationStepMessage(String resourceId, Map<ValidationResource, String> context) {
        return String.format("The provided Label Name: %s does not exist.", resourceId);
    }

    @Override
    public String getErrMessage(String resourceId) {
        return String.format("Invalid Label Name: %s", resourceId);
    }
}
