package app.krista.extensions.essentials.collaboration.outlook3.catalog.extresp;

import app.krista.extensions.essentials.collaboration.outlook3.catalog.validators.Validator;

import java.util.Collections;
import java.util.Map;

public class ValidationResourceUtil
{
    private ValidationResourceUtil(){}

    public static Map<Validator.ValidationResource, String> prepareValidateLabelMap(String label, Double pageNumber, Double pageSize){
        if(isNull(pageNumber) && isNull(pageSize)){
            return Map.of(Validator.ValidationResource.LABEL, label);
        } else if (isNull(pageNumber)) {
            return Map.of(Validator.ValidationResource.LABEL, label, Validator.ValidationResource.PAGE_SIZE, pageSize.toString());
        } else if (isNull(pageSize)) {
            return Map.of(Validator.ValidationResource.LABEL, label, Validator.ValidationResource.PAGE_NUMBER, pageNumber.toString());
        } else {
            return Map.of(Validator.ValidationResource.LABEL, label,
                    Validator.ValidationResource.PAGE_NUMBER, pageNumber.toString(),
                    Validator.ValidationResource.PAGE_SIZE, pageSize.toString());
        }
    }

    private static boolean isNull(Double input){
        return input == null;
    }

    public static Map<Validator.ValidationResource, String> prepareValidateFetchInboxMap(Double pageNumber, Double pageSize) {
        if(!isNull(pageNumber) && !isNull(pageSize)){
            return Map.of(Validator.ValidationResource.PAGE_NUMBER, pageNumber.toString(),
                    Validator.ValidationResource.PAGE_SIZE, pageSize.toString());
        } else if (!isNull(pageSize)) {
            return Map.of(Validator.ValidationResource.PAGE_SIZE, pageSize.toString());
        } else if(!isNull(pageNumber)){
            return Map.of(Validator.ValidationResource.PAGE_NUMBER, pageNumber.toString());
        }
        return Collections.emptyMap();
    }
}
