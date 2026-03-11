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

public class PageNumberValidator implements Validator {


    @Override
    public Boolean validate(String resourceId, Map<ValidationResource, String> context) {
        try {
            return isNumberValid(resourceId);
        } catch (RuntimeException cause) {
            return false;
        }
    }

    private Boolean isNumberValid(String resourceId) {
        double number = Double.parseDouble(resourceId);
        return number > 0;
    }

    @Override
    public String getFetchFieldName() {
        return OutlookResources.PAGE_NUMBER;
    }

    @Override
    public String getFieldType() {
        return FieldTypes.NUMBER_FIELD;
    }

    @Override
    public String getFetchStepMessage() {
        return "Please enter valid Page Number.";
    }

    @Override
    public String getConfirmationStepMessage(String resourceId, Map<ValidationResource, String> context) {
        return String.format("The provided Page number : %s should be greater than 0.",
                EntityHelperUtil.removeTrailingZeros(Double.parseDouble(resourceId)));
    }

    @Override
    public String getErrMessage(String resourceId) {
        return String.format("The provided Page number : %s should be greater than 0.",
                EntityHelperUtil.removeTrailingZeros(Double.parseDouble(resourceId)));
    }

}
