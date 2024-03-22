package app.krista.extensions.essentials.collaboration.outlook3.impl;

import app.krista.extensions.essentials.collaboration.outlook3.impl.util.Validators;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EmailValidationTest {
    public EmailValidationTest() {

    }

    @Test
    public void testValidEmail() {
        assertTrue(Validators.isEmailValid("example@example.com"));
        assertTrue(Validators.isEmailValid("example+test@example.com"));
        assertTrue(Validators.isEmailValid("example-test@example.com"));
    }

    @Test
    public void testInvalidEmail() {
        assertFalse(Validators.isEmailValid("invalid-email"));
        assertFalse(Validators.isEmailValid("example..@example.com"));
    }
}
