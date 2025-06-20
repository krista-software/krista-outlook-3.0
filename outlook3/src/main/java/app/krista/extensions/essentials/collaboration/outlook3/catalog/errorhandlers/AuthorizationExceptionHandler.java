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

        // 1. Refresh Token Expiration
        if (errorMessage.contains(REFRESH_TOKEN_EXPIRED)) {
            return ExtensionResponseFactory.create(REFRESH_TOKEN_EXPIRED_ERROR, ExtensionResponse.Error.ExceptionType.AUTHENTICATION_ERROR,
                    List.of(), null, null, ExtensionResponse.Result.SUCCESS);
        }
        // 2. Password Changed or Reset
        else if (errorMessage.contains(PASSWORD_CHANGED_ERROR)) {
            return ExtensionResponseFactory.create(PASSWORD_CHANGED_ERROR, ExtensionResponse.Error.ExceptionType.AUTHENTICATION_ERROR,
                    List.of(), null, null, ExtensionResponse.Result.SUCCESS);
        }
        // 3. User Deleted in Domain
        else if (errorMessage.contains(USER_DELETED_ERROR)) {
            return ExtensionResponseFactory.create(USER_DELETED_ERROR, ExtensionResponse.Error.ExceptionType.AUTHENTICATION_ERROR,
                    List.of(), null, null, ExtensionResponse.Result.SUCCESS);
        }
        // 4. User Disabled
        else if (errorMessage.contains(USER_DISABLED_ERROR)) {
            return ExtensionResponseFactory.create(USER_DISABLED_ERROR, ExtensionResponse.Error.ExceptionType.AUTHENTICATION_ERROR,
                    List.of(), null, null, ExtensionResponse.Result.FAILURE);
        }
        // 5. Permission Revoked
        else if (errorMessage.contains(PERMISSIONS_REVOKED_ERROR)) {
            return ExtensionResponseFactory.create(PERMISSIONS_REVOKED_ERROR, ExtensionResponse.Error.ExceptionType.AUTHENTICATION_ERROR,
                    List.of(), null, null, ExtensionResponse.Result.SUCCESS);
        }
        // 6. Application Not Found
        else if (errorMessage.contains(APP_NOT_FOUND_ERROR)) {
            return ExtensionResponseFactory.create(APP_NOT_FOUND_ERROR, ExtensionResponse.Error.ExceptionType.AUTHENTICATION_ERROR,
                    List.of(), null, null, ExtensionResponse.Result.SUCCESS);
        }
        // 7. Tenant Not Found
        else if (errorMessage.contains(TENANT_NOT_FOUND_CODE) || errorMessage.contains(KEYWORD_TENANT_NOT_FOUND)) {
            return ExtensionResponseFactory.create(TENANT_NOT_FOUND_ERROR, ExtensionResponse.Error.ExceptionType.AUTHENTICATION_ERROR,
                    List.of(), null, null, ExtensionResponse.Result.SUCCESS);
        }
        // 8. Network or Service Unavailable
        else if (errorMessage.contains(SERVICE_UNAVAILABLE_CODE) || errorMessage.contains(KEYWORD_SERVICE_UNAVAILABLE) ||
                errorMessage.contains(KEYWORD_NETWORK_ERROR)) {
            return ExtensionResponseFactory.create(SERVICE_UNAVAILABLE_ERROR, ExtensionResponse.Error.ExceptionType.AUTHENTICATION_ERROR,
                    List.of(), null, null, ExtensionResponse.Result.SUCCESS);
        }
        // 9. Invalid Client Secret
        else if (errorMessage.contains(INVALID_CLIENT_SECRET_CODE) || errorMessage.contains(KEYWORD_INVALID_CLIENT_SECRET)) {
            return ExtensionResponseFactory.create(INVALID_CLIENT_SECRET_ERROR, ExtensionResponse.Error.ExceptionType.AUTHENTICATION_ERROR,
                    List.of(), null, null, ExtensionResponse.Result.SUCCESS);
        }

        // If it's not one of the specific cases we handle, provide general guidance
        return ExtensionResponseFactory.create("Authentication error: " + errorMessage, ExtensionResponse.Error.ExceptionType.AUTHENTICATION_ERROR,
                List.of(), null, null, ExtensionResponse.Result.SUCCESS);
    }

}
