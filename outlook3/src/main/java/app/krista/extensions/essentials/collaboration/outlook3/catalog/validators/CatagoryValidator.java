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

import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.extresp.FieldTypes;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.extresp.OutlookResources;
import app.krista.extensions.essentials.collaboration.outlook3.service.Account;
import app.krista.extensions.essentials.collaboration.outlook3.service.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class CatagoryValidator implements Validator {

    private final Account account;
    private static final Logger logger = LoggerFactory.getLogger(CatagoryValidator.class);

    public CatagoryValidator(Account account) {
        this.account = account;
    }

    @Override
    public Boolean validate(String resourceId, Map<ValidationResource, String> context) {
        try {
            for (Map.Entry<ValidationResource, String> set : context.entrySet()) {
                if (set.getKey().name().equals("MESSAGE_ID")) {
                    return isCategoryExist(resourceId, set.getValue());
                }
            }
            return false;
        } catch (MustAuthorizeException cause) {
            logger.info(cause.getMessage());
            throw cause;
        } catch (Exception cause) {
            logger.info(cause.getMessage());
            return false;
        }
    }

    private Boolean isCategoryExist(String category, String messageID) {
        Email email = account.getEmail(messageID);
        List<String> existingCategories = email.getCategories();
        return !existingCategories.isEmpty() && existingCategories.contains(category);
    }

    @Override
    public String getFetchFieldName() {
        return OutlookResources.CATEGORY;
    }

    @Override
    public String getFieldType() {
        return FieldTypes.TEXT_FIELD;
    }

    @Override
    public String getFetchStepMessage() {
        return "Please enter valid Category.";
    }

    @Override
    public String getConfirmationStepMessage(String resourceId, Map<ValidationResource, String> context) {
        return String.format("The provided Category: %s does not exist.", resourceId);
    }

    @Override
    public String getErrMessage(String resourceId) {
        return String.format("Invalid Category: %s", resourceId);
    }
}
