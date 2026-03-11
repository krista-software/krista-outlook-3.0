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
import app.krista.extensions.essentials.collaboration.outlook3.impl.util.EntityHelperUtil;

import java.util.Map;

/**
 * Validator for page size parameters in pagination operations.
 *
 * <p>This validator ensures that page size values are numeric and fall within the valid
 * range of 1 to 15 (inclusive). Page sizes outside this range are rejected to prevent
 * performance issues and comply with Microsoft Graph API pagination limits.</p>
 */
public class PageSizeValidator implements Validator {


    /**
     * Validates that the page size is a valid number within the range [1, 15].
     *
     * @param resourceId the page size value to validate (as string)
     * @param context additional validation context (not used by this validator)
     * @return true if the page size is a valid number between 1 and 15 (inclusive), false otherwise
     */
    @Override
    public Boolean validate(String resourceId, Map<ValidationResource, String> context) {
        try {
            return isNumberValid(resourceId);
        } catch (RuntimeException cause) {
            return false;
        }
    }

    private Boolean isNumberValid(String resourceId) {
        double value = Double.parseDouble(resourceId);
        return value > 0 && value <= 15; // Valid range: greater than 0 and less than or equal to 15
    }

    @Override
    public String getFetchFieldName() {
        return OutlookResources.PAGE_SIZE;
    }

    @Override
    public String getFieldType() {
        return FieldTypes.NUMBER_FIELD;
    }

    @Override
    public String getFetchStepMessage() {
        return "Please enter valid Page Size.";
    }

    @Override
    public String getConfirmationStepMessage(String resourceId, Map<ValidationResource, String> context) {
        return String.format("The provided Page size : %s should be greater than 0 and less than or equal to 15.",
                EntityHelperUtil.removeTrailingZeros(Double.parseDouble(resourceId)));
    }

    @Override
    public String getErrMessage(String resourceId) {
        return String.format("The provided Page size : %s should be greater than 0 and less than or equal to 15.",
                EntityHelperUtil.removeTrailingZeros(Double.parseDouble(resourceId)));
    }

}
