package app.krista.extensions.essentials.collaboration.outlook3.catalog.validators;

import app.krista.extensions.essentials.collaboration.outlook3.catalog.extresp.OutlookResources;
import app.krista.extensions.essentials.collaboration.outlook3.impl.util.Constants;
import app.krista.extensions.essentials.collaboration.outlook3.impl.util.Validators;
import app.krista.extensions.essentials.collaboration.outlook3.service.EmailAddress;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BCCEmaiIValidator implements Validator{

    private static final List<EmailAddress> emailAddresses = new ArrayList<>();

    @Override
    public Boolean validate(String resourceId, Map<ValidationResource, String> context) {
        try {
            return toEmailAddresses(resourceId).isEmpty();
        } catch (RuntimeException cause) {
            return false;
        }
    }

    @Override
    public String getFetchFieldName() {
        return OutlookResources.BCC;
    }

    @Override
    public String getFetchStepMessage() {
        return "Please enter valid Email Address.";
    }

    @Override
    public String getConfirmationStepMessage(String resourceId, Map<ValidationResource, String> context) {
        return String.format("The provided Email Address : %s does not exist. Please check.", toStringMailIds());
    }

    @Override
    public String getErrMessage(String resourceId) {
        toEmailAddresses(resourceId);
        return String.format("Invalid 'Bcc' Email Ids: %s", toStringMailIds());
    }


    private static List<EmailAddress> toEmailAddresses(String emailAddressesString) {
        if (emailAddressesString == null || emailAddressesString.isBlank()) {
            return List.of();
        }
        for (String emailAddressString : emailAddressesString.split(Constants.COMMA)) {
            if (Validators.isStringNullOrBlank(emailAddressString) || !Validators.isEmailValid(emailAddressString)) {
                emailAddresses.add(new EmailAddress(Constants.EMPTY_STRING, emailAddressString));
            }
        }
        return emailAddresses;
    }

    private static String toStringMailIds(){
        String mailIds = BCCEmaiIValidator.emailAddresses.stream()
                .map(EmailAddress::getMailAddress)
                .collect(Collectors.joining(", "));
        BCCEmaiIValidator.emailAddresses.clear();
        return mailIds;
    }
}
