package app.krista.extensions.essentials.collaboration.outlook3.catalog.extresp;

import app.krista.model.field.NamedField;
import app.krista.model.field.NamedValuedField;

import java.util.Map;

public class NamedFieldFactory {

    public static NamedField createTextField(String fieldName) {
        return new NamedField(fieldName, FieldTypes.TEXT_FIELD, Map.of(), Map.of());
    }

    public static NamedField createSwitchField(String fieldName) {
        return new NamedField(fieldName, FieldTypes.SWITCH_FIELD, Map.of(), Map.of());
    }

    public static NamedValuedField createTextValueField(String fieldName, String value) {
        return new NamedValuedField(fieldName, FieldTypes.TEXT_FIELD, value, Map.of(), Map.of());
    }
}
