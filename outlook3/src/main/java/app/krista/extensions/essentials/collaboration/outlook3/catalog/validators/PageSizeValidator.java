package app.krista.extensions.essentials.collaboration.outlook3.catalog.validators;

import app.krista.extensions.essentials.collaboration.outlook3.catalog.extresp.FieldTypes;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.extresp.OutlookResources;
import app.krista.extensions.essentials.collaboration.outlook3.impl.util.EntityHelperUtil;

import java.util.Map;

public class PageSizeValidator implements Validator {


    @Override
    public Boolean validate(String resourceId, Map<ValidationResource, String> context) {
        try {
            return isNumberValid(resourceId);
        } catch (RuntimeException cause) {
            return false;
        }
    }

    private Boolean isNumberValid(String resourceId) {
        double value = Double.parseDouble(resourceId);
        return value > 0 && value <= 15; // Valid range: greater than 0 and less than or equal to 15
    }

    @Override
    public String getFetchFieldName() {
        return OutlookResources.PAGE_SIZE;
    }

    @Override
    public String getFieldType() {
        return FieldTypes.NUMBER_FIELD;
    }

    @Override
    public String getFetchStepMessage() {
        return "Please enter valid Page Size.";
    }

    @Override
    public String getConfirmationStepMessage(String resourceId, Map<ValidationResource, String> context) {
        return String.format("The provided Page size : %s should be greater than 0 and less than or equal to 15.",
                EntityHelperUtil.removeTrailingZeros(Double.parseDouble(resourceId)));
    }

    @Override
    public String getErrMessage(String resourceId) {
        return String.format("The provided Page size : %s should be greater than 0 and less than or equal to 15.",
                EntityHelperUtil.removeTrailingZeros(Double.parseDouble(resourceId)));
    }

}
