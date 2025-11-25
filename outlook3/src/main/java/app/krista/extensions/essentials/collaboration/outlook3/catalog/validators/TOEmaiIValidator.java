package app.krista.extensions.essentials.collaboration.outlook3.catalog.validators;

import app.krista.extensions.essentials.collaboration.outlook3.catalog.extresp.FieldTypes;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.extresp.OutlookResources;
import app.krista.extensions.essentials.collaboration.outlook3.impl.util.Constants;
import app.krista.extensions.essentials.collaboration.outlook3.impl.util.Validators;
import app.krista.extensions.essentials.collaboration.outlook3.service.EmailAddress;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TOEmaiIValidator implements Validator {

    private final List<EmailAddress> invalidEmailAddresses = new ArrayList<>();

    @Override
    public Boolean validate(String resourceId, Map<ValidationResource, String> context) {
        invalidEmailAddresses.clear();

        if (resourceId == null || resourceId.isBlank()) {
            return false;
        }

        List<String> allAddresses = new ArrayList<>();
        for (String emailAddressString : resourceId.split(Constants.COMMA)) {
            String trimmed = emailAddressString.trim();
            if (!trimmed.isEmpty()) {
                allAddresses.add(trimmed);
            }
        }

        if (allAddresses.isEmpty()) {
            return false;
        }

        long validCount = 0;
        for (String address : allAddresses) {
            if (Validators.isEmailValid(address)) {
                validCount++;
            } else {
                invalidEmailAddresses.add(new EmailAddress(Constants.EMPTY_STRING, address));
            }
        }

        return validCount > 0;
    }

    @Override
    public String getFetchFieldName() {
        return OutlookResources.TO;
    }

    @Override
    public String getFieldType() {
        return FieldTypes.TEXT_FIELD;
    }

    @Override
    public String getFetchStepMessage() {
        return "Please enter at least one valid Email Address for 'To' field.";
    }

    @Override
    public String getConfirmationStepMessage(String resourceId, Map<ValidationResource, String> context) {
        return String.format("The provided 'To' Email Address(es) are invalid: %s", toStringMailIds());
    }

    @Override
    public String getErrMessage(String resourceId) {
        if (resourceId == null || resourceId.isBlank()) {
            return "'To' field is required and must contain at least one valid email address.";
        }
        return String.format("Invalid 'To' Email Address(es): %s. At least one valid email is required.", toStringMailIds());
    }

    private String toStringMailIds() {
        String mailIds = invalidEmailAddresses.stream()
                .map(EmailAddress::getMailAddress)
                .collect(Collectors.joining(", "));
        return mailIds.isEmpty() ? "(empty or whitespace only)" : mailIds;
    }
}
