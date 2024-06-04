package app.krista.extensions.essentials.collaboration.outlook3.catalog.validators;

import app.krista.extensions.essentials.collaboration.outlook3.catalog.extresp.OutlookResources;
import app.krista.extensions.essentials.collaboration.outlook3.service.Account;
import app.krista.extensions.essentials.collaboration.outlook3.service.Email;

import java.util.List;
import java.util.Map;

public class CatagoryValidator implements Validator {

    private final Account account;

    public CatagoryValidator(Account account) {
        this.account = account;
    }

    @Override
    public Boolean validate(String resourceId, Map<ValidationResource, String> context) {
        try {
            for(Map.Entry<ValidationResource, String> set : context.entrySet()){
                if(set.getKey().name().equals("MESSAGE_ID")){
                    return isCategoryExist(resourceId,set.getValue());
                }
            }
            return false;
        } catch (RuntimeException cause) {
            return false;
        }
    }

    private Boolean isCategoryExist(String category, String messageID) {
        try {
            Email email = account.getEmail(messageID);
            List<String> existingCategories = email.getCategories();
            return !existingCategories.isEmpty() && existingCategories.contains(category);
        }catch (RuntimeException cause){
            throw new RuntimeException(cause);

        }
    }

    @Override
    public String getFetchFieldName() {
        return OutlookResources.CATEGORY;
    }

    @Override
    public String getFetchStepMessage() {
        return "Please enter valid Category.";
    }

    @Override
    public String getConfirmationStepMessage(String resourceId, Map<ValidationResource, String> context) {
        return String.format("The provided Category: %s does not exist. Please check.", resourceId);
    }

    @Override
    public String getErrMessage(String resourceId) {
        return String.format("Invalid Category: %s", resourceId);
    }
}
