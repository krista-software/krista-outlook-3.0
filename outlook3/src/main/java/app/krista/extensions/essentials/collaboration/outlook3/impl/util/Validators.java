package app.krista.extensions.essentials.collaboration.outlook3.impl.util;

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
}
