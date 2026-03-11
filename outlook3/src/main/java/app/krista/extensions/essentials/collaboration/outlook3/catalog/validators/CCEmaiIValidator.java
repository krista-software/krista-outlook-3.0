/*
 * Outlook 3.0 Extension for Krista
 * Copyright (C) 2025 Krista Software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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

public class CCEmaiIValidator implements Validator {

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
        return OutlookResources.CC;
    }

    @Override
    public String getFieldType() {
        return FieldTypes.TEXT_FIELD;
    }

    @Override
    public String getFetchStepMessage() {
        return "Please enter valid Email Address.";
    }

    @Override
    public String getConfirmationStepMessage(String resourceId, Map<ValidationResource, String> context) {
        return String.format("The provided Email Address : %s does not exist.", toStringMailIds());
    }

    @Override
    public String getErrMessage(String resourceId) {
        toEmailAddresses(resourceId);
        return String.format("Invalid 'Cc' Email Ids: %s", toStringMailIds());
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

    private static String toStringMailIds() {
        String mailIds = CCEmaiIValidator.emailAddresses.stream()
                .map(EmailAddress::getMailAddress)
                .collect(Collectors.joining(", "));
        CCEmaiIValidator.emailAddresses.clear();
        return mailIds;
    }
}
