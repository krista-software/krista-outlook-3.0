package app.krista.extensions.essentials.collaboration.outlook3.catalog.validators;

import app.krista.extensions.essentials.collaboration.outlook3.catalog.extresp.FieldTypes;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.extresp.OutlookResources;
import app.krista.extensions.essentials.collaboration.outlook3.service.Account;
import app.krista.extensions.essentials.collaboration.outlook3.service.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class CatagoryValidator implements Validator {

    private final Account account;
    private static final Logger logger = LoggerFactory.getLogger(CatagoryValidator.class);

    public CatagoryValidator(Account account) {
        this.account = account;
    }

    @Override
    public Boolean validate(String resourceId, Map<ValidationResource, String> context) {
        try {
            for (Map.Entry<ValidationResource, String> set : context.entrySet()) {
                if (set.getKey().name().equals("MESSAGE_ID")) {
                    return isCategoryExist(resourceId, set.getValue());
                }
            }
            return false;
        } catch (Exception cause) {
            logger.info(cause.getMessage());
            return false;
        }
    }

    private Boolean isCategoryExist(String category, String messageID) {
        Email email = account.getEmail(messageID);
        List<String> existingCategories = email.getCategories();
        return !existingCategories.isEmpty() && existingCategories.contains(category);
    }

    @Override
    public String getFetchFieldName() {
        return OutlookResources.CATEGORY;
    }

    @Override
    public String getFieldType() {
        return FieldTypes.TEXT_FIELD;
    }

    @Override
    public String getFetchStepMessage() {
        return "Please enter valid Category.";
    }

    @Override
    public String getConfirmationStepMessage(String resourceId, Map<ValidationResource, String> context) {
        return String.format("The provided Category: %s does not exist.", resourceId);
    }

    @Override
    public String getErrMessage(String resourceId) {
        return String.format("Invalid Category: %s", resourceId);
    }
}
