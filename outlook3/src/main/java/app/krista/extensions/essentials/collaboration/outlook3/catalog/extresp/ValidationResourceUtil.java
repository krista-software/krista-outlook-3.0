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
        if (isNotNull(pageNumber)) {
            map.put(Validator.ValidationResource.PAGE_NUMBER, pageNumber.toString());
        }
        if (isNotNull(pageSize)) {
            map.put(Validator.ValidationResource.PAGE_SIZE, pageSize.toString());
        }
        return map;
    }

    public static Map<Validator.ValidationResource, String> prepareValidateFetchInboxWithPrefMap(
            Double pageNumber, Double pageSize, Map<String, Object> prefMap) {
        Map<Validator.ValidationResource, String> map = prepareValidateFetchInboxMap(pageNumber, pageSize);
        if (prefMap != null && !prefMap.isEmpty()) {
            String prefBodyType = (String) prefMap.get("Mail Body");
            if (!"html".equalsIgnoreCase(prefBodyType) && !"text".equalsIgnoreCase(prefBodyType)) {
                map.put(Validator.ValidationResource.PREFERENCES, prefBodyType);
            }
        }
        return map;
    }
}
