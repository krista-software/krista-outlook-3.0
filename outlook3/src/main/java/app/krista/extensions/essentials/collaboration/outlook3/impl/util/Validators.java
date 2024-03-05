package app.krista.extensions.essentials.collaboration.outlook3.impl.util;

import app.krista.extensions.essentials.collaboration.outlook3.OutlookAttributes;
import org.apache.commons.validator.routines.EmailValidator;

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

    public static boolean areAllCredentialsBlank(OutlookAttributes attributes) {
        return Validators.isStringNullOrBlank(attributes.getClientId()) &&
                Validators.isStringNullOrBlank(attributes.getClientSecret()) &&
                Validators.isStringNullOrBlank(attributes.getTenantId());
    }

    public static boolean isAnyParameterValueBlank(OutlookAttributes attributes) {
        return Validators.isStringNullOrBlank(attributes.getClientId()) ||
                Validators.isStringNullOrBlank(attributes.getClientSecret()) ||
                Validators.isStringNullOrBlank(attributes.getTenantId());
    }
}
