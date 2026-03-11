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

package app.krista.extensions.essentials.collaboration.outlook3.catalog.extresp;

import app.krista.extensions.essentials.collaboration.outlook3.catalog.validators.Validator;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for preparing validation resource maps used in input validation workflows.
 *
 * <p>This class provides static helper methods to construct validation maps containing
 * parameters that need to be validated, such as labels, page numbers, and page sizes.
 * These maps are used by validators to perform input validation and generate appropriate
 * error messages.</p>
 */
public class ValidationResourceUtil {

    private ValidationResourceUtil() {
    }

    /**
     * Prepares a validation map for label-based operations with optional pagination parameters.
     *
     * <p>This method creates a map containing the label and any non-null pagination parameters
     * (page number and page size) that need to be validated. The map is used by validators to
     * check if the provided values meet the required constraints.</p>
     *
     * @param label the label value to validate
     * @param pageNumber the page number for pagination; included in map only if not null
     * @param pageSize the page size for pagination; included in map only if not null
     * @return a map containing validation resources with their corresponding values
     */
    public static Map<Validator.ValidationResource, String> prepareValidateLabelMap(String label, Double pageNumber, Double pageSize) {
        Map<Validator.ValidationResource, String> map = new HashMap<>();
        map.put(Validator.ValidationResource.LABEL, label);
        if (isNotNull(pageNumber)) {
            map.put(Validator.ValidationResource.PAGE_NUMBER, pageNumber.toString());
        }
        if (isNotNull(pageSize)) {
            map.put(Validator.ValidationResource.PAGE_SIZE, pageSize.toString());
        }
        return map;
    }

    private static boolean isNotNull(Double input) {
        return input != null;
    }

    /**
     * Prepares a validation map for inbox fetch operations with pagination parameters.
     *
     * <p>This method creates a map containing only the pagination parameters that are INVALID
     * and need validation. Unlike prepareValidateLabelMap, this method only includes parameters
     * that fall outside their valid ranges:
     * <ul>
     *   <li>Page number: included if less than 1</li>
     *   <li>Page size: included if less than 1 or greater than 15</li>
     * </ul>
     * This approach allows validators to focus only on invalid values.</p>
     *
     * @param pageNumber the page number for pagination; included only if less than 1
     * @param pageSize the page size for pagination; included only if outside range [1, 15]
     * @return a map containing only invalid validation resources with their corresponding values
     */
    public static Map<Validator.ValidationResource, String> prepareValidateFetchInboxMap(Double pageNumber, Double pageSize) {
        Map<Validator.ValidationResource, String> map = new HashMap<>();

        if (isNotNull(pageNumber) && (pageNumber < 1)) {
            map.put(Validator.ValidationResource.PAGE_NUMBER, pageNumber.toString());
        }

        // Add page size to validation map if it's OUTSIDE valid range (1-15 inclusive)
        if (isNotNull(pageSize) && (pageSize < 1 || pageSize > 15)) {
            map.put(Validator.ValidationResource.PAGE_SIZE, pageSize.toString());
        }
        return map;
    }
}
