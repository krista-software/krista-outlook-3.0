package app.krista.extensions.essentials.collaboration.outlook3.catalog.validators;

import app.krista.extensions.essentials.collaboration.outlook3.catalog.extresp.FieldTypes;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.extresp.OutlookResources;
import app.krista.extensions.essentials.collaboration.outlook3.impl.util.EntityHelperUtil;

import java.util.Map;

public class PageNumberValidator implements Validator {


    @Override
    public Boolean validate(String resourceId, Map<ValidationResource, String> context) {
        try {
            return isNumberValid(resourceId);
        } catch (RuntimeException cause) {
            return false;
        }
    }

    private Boolean isNumberValid(String resourceId) {
        double number = Double.parseDouble(resourceId);
        return number > 0 && number <= 100;//Maximum limit for Page Number is 100
    }

    @Override
    public String getFetchFieldName() {
        return OutlookResources.PAGE_NUMBER;
    }

    @Override
    public String getFieldType() {
        return FieldTypes.NUMBER_FIELD;
    }

    @Override
    public String getFetchStepMessage() {
        return "Please enter valid Page Number.";
    }

    @Override
    public String getConfirmationStepMessage(String resourceId, Map<ValidationResource, String> context) {
        return String.format("The provided Page number : %s should be greater than 0 and less than or equal to 15.",
                EntityHelperUtil.removeTrailingZeros(Double.parseDouble(resourceId)));
    }

    @Override
    public String getErrMessage(String resourceId) {
        return String.format("The provided Page number : %s should be greater than 0 and less than or equal to 15.",
                EntityHelperUtil.removeTrailingZeros(Double.parseDouble(resourceId)));
    }

}
