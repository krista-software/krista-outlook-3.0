package app.krista.extensions.essentials.collaboration.outlook3.impl.util;

import org.apache.commons.validator.routines.EmailValidator;

import java.util.List;
import java.util.Map;

public class Validators {

    private Validators() {
    }

    public static boolean isEmailValid(String emailAddress) {
        EmailValidator emailValidator = EmailValidator.getInstance();
        return emailValidator.isValid(emailAddress);
    }

    public static boolean isStringNullOrBlank(String input) {
        return (input == null || input.isBlank());
    }

    public static <T> boolean isListNullOrEmpty(List<T> input) {
        return (input == null || input.isEmpty());
    }


    public static void addAttributeIfNotNull(Map<String, Object> attributeMap, String attributeName,
                                             Object attributeValue) {
        if (attributeValue != null) {
            attributeMap.put(attributeName, attributeValue);
        }
    }
}
