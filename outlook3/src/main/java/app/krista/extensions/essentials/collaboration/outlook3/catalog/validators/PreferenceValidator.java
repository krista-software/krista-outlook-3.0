package app.krista.extensions.essentials.collaboration.outlook3.catalog.validators;

import app.krista.extensions.essentials.collaboration.outlook3.catalog.extresp.FieldTypes;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.extresp.OutlookResources;

import java.util.Map;

public class PreferenceValidator implements Validator {

    @Override
    @SuppressWarnings("unchecked")
    public Boolean validate(String resourceId, Map<ValidationResource, String> context) {
        try {
            if ("html".equalsIgnoreCase(resourceId) || "text".equalsIgnoreCase(resourceId)) {
                return Boolean.TRUE;
            } else {
                return Boolean.FALSE;
            }
        } catch (RuntimeException cause) {
            return false;
        }
    }


    @Override
    public String getFetchFieldName() {
        return OutlookResources.PREFERENCE;
    }

    @Override
    public String getFieldType() {
        return FieldTypes.TEXT_FIELD;
    }

    @Override
    public String getFetchStepMessage() {
        return "Please select valid body type.";
    }

    @Override
    public String getConfirmationStepMessage(String resourceId, Map<ValidationResource, String> context) {
        return String.format("The provided Mail body type preference is %s invalid.", resourceId);
    }

    @Override
    public String getErrMessage(String resourceId) {
        return String.format("The provided Mail body type preference is %s invalid.", resourceId);
    }

}
