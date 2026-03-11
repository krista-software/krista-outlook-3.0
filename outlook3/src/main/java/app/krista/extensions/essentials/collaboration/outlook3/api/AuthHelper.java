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

package app.krista.extensions.essentials.collaboration.outlook3.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static app.krista.extensions.essentials.collaboration.outlook3.impl.util.Constants.AUTH_CONTEXT_ID;

public final class AuthHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthHelper.class);

    private AuthHelper() {
    }

    public static String getAuthContextId(String state) {
        int authContextIdIndex = state.indexOf(AUTH_CONTEXT_ID);
        if (authContextIdIndex == -1) {
            LOGGER.debug("AuthContextId not found in state parameter");
            return null;
        }
        String authContextIdToEnd = state.substring(authContextIdIndex);
        int hashIndex = authContextIdToEnd.indexOf("#");
        String authContextId;
        if (hashIndex != -1) {
            // If there is a "#", end the extraction at this index
            authContextId = authContextIdToEnd.substring(0, hashIndex);
        } else {
            // If there is no "#", use the entire substring
            authContextId = authContextIdToEnd;
        }
        return authContextId;
    }
}
