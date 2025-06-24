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
     * Handles the MustAuthorizeException by providing user-friendly error messages
     * and appropriate remediation actions based on the specific error type.
     *
     * @param exception The MustAuthorizeException to handle.
     * @return An ExtensionResponse with the appropriate error message and remediation action.
     */
    public static ExtensionResponse handleAuthorizationException(MustAuthorizeException exception) {
        LOGGER.error("Authorization exception: {}", exception.getMessage());
        String errorMessage = exception.getMessage();

        if (containsAny(errorMessage, REFRESH_TOKEN_EXPIRED)) {
            return buildAuthResponse(REFRESH_TOKEN_EXPIRED_ERROR, ExtensionResponse.Result.SUCCESS);
        } else if (containsAny(errorMessage, PASSWORD_CHANGED_ERROR)) {
            return buildAuthResponse(PASSWORD_CHANGED_ERROR, ExtensionResponse.Result.SUCCESS);
        } else if (containsAny(errorMessage, USER_DELETED_ERROR)) {
            return buildAuthResponse(USER_DELETED_ERROR, ExtensionResponse.Result.SUCCESS);
        } else if (containsAny(errorMessage, USER_DISABLED_ERROR)) {
            return buildAuthResponse(USER_DISABLED_ERROR, ExtensionResponse.Result.FAILURE);
        } else if (containsAny(errorMessage, PERMISSIONS_REVOKED_ERROR)) {
            return buildAuthResponse(PERMISSIONS_REVOKED_ERROR, ExtensionResponse.Result.SUCCESS);
        } else if (containsAny(errorMessage, APP_NOT_FOUND_ERROR)) {
            return buildAuthResponse(APP_NOT_FOUND_ERROR, ExtensionResponse.Result.SUCCESS);
        } else if (containsAny(errorMessage, TENANT_NOT_FOUND_CODE, KEYWORD_TENANT_NOT_FOUND)) {
            return buildAuthResponse(TENANT_NOT_FOUND_ERROR, ExtensionResponse.Result.SUCCESS);
        } else if (containsAny(errorMessage, SERVICE_UNAVAILABLE_CODE, KEYWORD_SERVICE_UNAVAILABLE, KEYWORD_NETWORK_ERROR)) {
            return buildAuthResponse(SERVICE_UNAVAILABLE_ERROR, ExtensionResponse.Result.SUCCESS);
        } else if (containsAny(errorMessage, INVALID_CLIENT_SECRET_CODE, KEYWORD_INVALID_CLIENT_SECRET)) {
            return buildAuthResponse(INVALID_CLIENT_SECRET_ERROR, ExtensionResponse.Result.SUCCESS);
        }

        return buildAuthResponse("Authentication error: " + errorMessage, ExtensionResponse.Result.SUCCESS);
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

}
