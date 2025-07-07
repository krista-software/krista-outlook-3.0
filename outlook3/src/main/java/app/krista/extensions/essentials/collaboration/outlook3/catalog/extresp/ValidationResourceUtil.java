package app.krista.extensions.essentials.collaboration.outlook3.catalog.extresp;

import app.krista.extensions.essentials.collaboration.outlook3.catalog.validators.Validator;

import java.util.HashMap;
import java.util.Map;

public class ValidationResourceUtil {

    private ValidationResourceUtil() {
    }

    public static Map<Validator.ValidationResource, String> prepareValidateLabelMap(String label, Double pageNumber, Double pageSize) {
        Map<Validator.ValidationResource, String> map = new HashMap<>();
        map.put(Validator.ValidationResource.LABEL, label);
        if (isNotNull(pageNumber)) {
            map.put(Validator.ValidationResource.PAGE_NUMBER, pageNumber.toString());
        }
        if (isNotNull(pageSize)) {
            map.put(Validator.ValidationResource.PAGE_SIZE, pageSize.toString());
        }
        return map;
    }

    private static boolean isNotNull(Double input) {
        return input != null;
    }

    public static Map<Validator.ValidationResource, String> prepareValidateFetchInboxMap(Double pageNumber, Double pageSize) {
        Map<Validator.ValidationResource, String> map = new HashMap<>();

        // Add page number to validation map if it's OUTSIDE valid range (1-15 inclusive)
        if (isNotNull(pageNumber) && (pageNumber < 1 || pageNumber > 15)) {
            map.put(Validator.ValidationResource.PAGE_NUMBER, pageNumber.toString());
        }

        // Add page size to validation map if it's OUTSIDE valid range (1-15 inclusive)
        if (isNotNull(pageSize) && (pageSize < 1 || pageSize > 15)) {
            map.put(Validator.ValidationResource.PAGE_SIZE, pageSize.toString());
        }
        return map;
    }
}
