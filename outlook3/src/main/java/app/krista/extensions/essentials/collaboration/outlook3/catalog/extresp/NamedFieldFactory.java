package app.krista.extensions.essentials.collaboration.outlook3.catalog.extresp;

import app.krista.model.field.NamedField;

import java.util.Map;

public class NamedFieldFactory {

    private NamedFieldFactory() {
    }

    public static NamedField createTextField(String fieldName) {
        return new NamedField(fieldName, FieldTypes.TEXT_FIELD, Map.of(), Map.of());
    }

    public static NamedField createSwitchField(String fieldName) {
        return new NamedField(fieldName, FieldTypes.SWITCH_FIELD, Map.of(), Map.of());
    }

}
