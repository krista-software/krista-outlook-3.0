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

package app.krista.extensions.essentials.collaboration.outlook3.catalog.errorhandlers;

import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extension.executor.ExtensionResponse;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.extresp.ExtensionResponseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static app.krista.extensions.essentials.collaboration.outlook3.impl.util.Constants.*;

/**
 * Handles Microsoft 365 authorization exceptions by providing user-friendly error messages
 * and appropriate remediation actions based on the specific error type.
 * This class centralizes all OAuth error handling for the Outlook integration.
 */
public class AuthorizationExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizationExceptionHandler.class);

    /**
     * Handles the {@link MustAuthorizeException} by providing user-friendly error messages
     * and appropriate remediation actions based on the specific error type.
     *
     * <p>If the request is invoked in a user context, this method rethrows the exception.
     * Otherwise, it handles the exception by generating an {@link ExtensionResponse}
     * with a suitable error message and remediation instructions.
     *
     * @param exception      the {@code MustAuthorizeException} to handle
     * @param requestContext {@code true} if the request is invoked as a user, otherwise {@code false}
     * @return an {@link ExtensionResponse} containing the appropriate error message and remediation action
     * @throws MustAuthorizeException if {@code requestContext} is {@code true}
     */
    public static ExtensionResponse handleAuthorizationException(MustAuthorizeException exception, boolean requestContext) throws MustAuthorizeException {
        LOGGER.error("Authorization exception: {}", exception.getMessage());

        if (requestContext) {
            throw exception;
        }

        String errorMessage = exception.getMessage();

        // Mapping of keywords to user-friendly error messages
        List<AuthErrorMapping> mappings = List.of(
                new AuthErrorMapping(REFRESH_TOKEN_EXPIRED_ERROR, REFRESH_TOKEN_EXPIRED),
                new AuthErrorMapping(PASSWORD_CHANGED_ERROR),
                new AuthErrorMapping(USER_DELETED_ERROR),
                new AuthErrorMapping(USER_DISABLED_ERROR),
                new AuthErrorMapping(PERMISSIONS_REVOKED_ERROR),
                new AuthErrorMapping(APP_NOT_FOUND_ERROR),
                new AuthErrorMapping(TENANT_NOT_FOUND_ERROR, TENANT_NOT_FOUND_CODE, KEYWORD_TENANT_NOT_FOUND),
                new AuthErrorMapping(SERVICE_UNAVAILABLE_ERROR, SERVICE_UNAVAILABLE_CODE, KEYWORD_SERVICE_UNAVAILABLE, KEYWORD_NETWORK_ERROR),
                new AuthErrorMapping(INVALID_CLIENT_SECRET_ERROR, INVALID_CLIENT_SECRET_CODE, KEYWORD_INVALID_CLIENT_SECRET),
                new AuthErrorMapping(AUTHORIZATION_PROMPT, AUTHORIZATION_PROMPT) // prompt used as both match and display
        );

        for (AuthErrorMapping mapping : mappings) {
            if (mapping.matches(errorMessage)) {
                return buildAuthResponse(mapping.messageToDisplay, ExtensionResponse.Result.FAILURE);
            }
        }

        return buildAuthResponse("Authentication error: " + errorMessage, ExtensionResponse.Result.FAILURE);
    }

    private static boolean containsAny(String source, String... keywords) {
        for (String keyword : keywords) {
            if (source != null && source.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private static ExtensionResponse buildAuthResponse(String errorMessage, ExtensionResponse.Result result) {
        return ExtensionResponseFactory.create(
                errorMessage,
                ExtensionResponse.Error.ExceptionType.AUTHENTICATION_ERROR,
                List.of(), null, null, result
        );
    }

    private static class AuthErrorMapping {
        private final String messageToDisplay;
        private final String[] keywords;

        AuthErrorMapping(String messageToDisplay, String... keywords) {
            this.messageToDisplay = messageToDisplay;
            this.keywords = keywords.length > 0 ? keywords : new String[]{messageToDisplay}; // fallback
        }

        boolean matches(String source) {
            return containsAny(source, keywords);
        }
    }

}
