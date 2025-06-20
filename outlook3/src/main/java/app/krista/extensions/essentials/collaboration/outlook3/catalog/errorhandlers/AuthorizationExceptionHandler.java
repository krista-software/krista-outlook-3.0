package app.krista.extensions.essentials.collaboration.outlook3.catalog.errorhandlers;

import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extension.executor.ExtensionResponse;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.extresp.ExtensionResponseFactory;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.extresp.RemediationActionFactory;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.extresp.StateMapperUtil;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.extresp.SubCatalogConstants;
import app.krista.extensions.essentials.collaboration.outlook3.impl.util.Constants;
import app.krista.model.field.NamedValuedField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static app.krista.extensions.essentials.collaboration.outlook3.catalog.MessagingAreaSubCatalogRequests.CONFIRM_REENTER_AUTHORIZATION;
import static app.krista.extensions.essentials.collaboration.outlook3.impl.util.Constants.*;

/**
 * Handles Microsoft 365 authorization exceptions by providing user-friendly error messages
 * and appropriate remediation actions based on the specific error type.
 * This class centralizes all OAuth error handling for the Outlook integration.
 */
public class AuthorizationExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizationExceptionHandler.class);
    private final static ErrorHandlingStateManager internalStateManager = null;


    // Centralized list of error messages with user guidance
    private static final List<NamedValuedField> GENERAL_GUIDANCE = List.of(
            new NamedValuedField("Common Solutions", "Text", "1. Try signing out and signing back in", null, null),
            new NamedValuedField("If Problem Persists", "Text", "2. Clear your browser cache and cookies", null, null),
            new NamedValuedField("Need Help?", "Text", "3. Contact your system administrator with the error details", null, null)
    );

    private static final List<NamedValuedField> PASSWORD_CHANGED_GUIDANCE = List.of(
            new NamedValuedField("Action Required", "Text", "Please re-authenticate your Microsoft 365 account", null, null),
            new NamedValuedField("Possible Cause", "Text", "Your password has been changed or reset", null, null),
            new NamedValuedField("Resolution", "Text", "Sign in with your new password when prompted", null, null)
    );

    private static final List<NamedValuedField> USER_DELETED_GUIDANCE = List.of(
            new NamedValuedField("Action Required", "Text", "Contact your Microsoft 365 administrator", null, null),
            new NamedValuedField("Possible Cause", "Text", "Your user account has been deleted from the domain", null, null),
            new NamedValuedField("Resolution", "Text", "Your administrator needs to restore your account or create a new one", null, null)
    );

    private static final List<NamedValuedField> USER_DISABLED_GUIDANCE = List.of(
            new NamedValuedField("Action Required", "Text", "Contact your Microsoft 365 administrator", null, null),
            new NamedValuedField("Possible Cause", "Text", "Your user account has been disabled", null, null),
            new NamedValuedField("Resolution", "Text", "Your administrator needs to re-enable your account", null, null)
    );

    private static final List<NamedValuedField> PERMISSIONS_REVOKED_GUIDANCE = List.of(
            new NamedValuedField("Action Required", "Text", "Re-authorize the application", null, null),
            new NamedValuedField("Possible Cause", "Text", "You or an administrator has revoked the application's permissions", null, null),
            new NamedValuedField("Resolution", "Text", "Grant consent to the application when prompted during re-authentication", null, null)
    );

    private static final List<NamedValuedField> APP_NOT_FOUND_GUIDANCE = List.of(
            new NamedValuedField("Action Required", "Text", "Contact your system administrator", null, null),
            new NamedValuedField("Possible Cause", "Text", "The application registration in Azure AD has been deleted or is misconfigured", null, null),
            new NamedValuedField("Resolution", "Text", "The administrator needs to verify the application registration in Azure AD", null, null)
    );

    private static final List<NamedValuedField> TENANT_NOT_FOUND_GUIDANCE = List.of(
            new NamedValuedField("Action Required", "Text", "Verify tenant ID configuration", null, null),
            new NamedValuedField("Possible Cause", "Text", "The Microsoft 365 tenant ID is incorrect or the tenant has been deleted", null, null),
            new NamedValuedField("Resolution", "Text", "Contact your administrator to verify the tenant ID in the configuration", null, null)
    );

    private static final List<NamedValuedField> SERVICE_UNAVAILABLE_GUIDANCE = List.of(
            new NamedValuedField("Action Required", "Text", "Try again later", null, null),
            new NamedValuedField("Possible Cause", "Text", "Microsoft 365 services are temporarily unavailable or there's a network connectivity issue", null, null),
            new NamedValuedField("Resolution", "Text", "Check your network connection and try again. If the problem persists, verify Microsoft 365 service status", null, null)
    );

    private static final List<NamedValuedField> INVALID_CLIENT_SECRET_GUIDANCE = List.of(
            new NamedValuedField("Action Required", "Text", "Contact your system administrator", null, null),
            new NamedValuedField("Possible Cause", "Text", "The client secret for the application has expired or is incorrect", null, null),
            new NamedValuedField("Resolution", "Text", "The administrator needs to update the client secret in both Azure AD and the application configuration", null, null)
    );

    private static final List<NamedValuedField> REFRESH_TOKEN_EXPIRED_GUIDANCE = List.of(
            new NamedValuedField("Action Required", "Text", "Please re-authenticate your Microsoft 365 account", null, null),
            new NamedValuedField("Possible Cause", "Text", "Your authentication token has expired", null, null),
            new NamedValuedField("Resolution", "Text", "Click the authentication link that will be provided", null, null)
    );

    public static ExtensionResponse handleAuthorizationException(MustAuthorizeException exception) {
        LOGGER.error("Authorization exception: {}", exception.getMessage());
        String errorMessage = exception.getMessage();
        String stateId = UUID.randomUUID().toString();

        // 1. Refresh Token Expiration
        if (errorMessage.contains(REFRESH_TOKEN_EXPIRED)) {
            return ExtensionResponseFactory.create(REFRESH_TOKEN_EXPIRED, ExtensionResponse.Error.ExceptionType.AUTHENTICATION_ERROR,
                    List.of(RemediationActionFactory.createInformActionALLParticipants(REFRESH_TOKEN_EXPIRED_ERROR, REFRESH_TOKEN_EXPIRED_GUIDANCE)),
                    CONFIRM_REENTER_AUTHORIZATION, StateMapperUtil.addAuthorizationMetaDataToMap(stateId), ExtensionResponse.Result.SUCCESS);
        }
        // 2. Password Changed or Reset
        else if (errorMessage.contains(PASSWORD_CHANGED_ERROR)) {
            return ExtensionResponseFactory.create(PASSWORD_CHANGED_ERROR, ExtensionResponse.Error.ExceptionType.AUTHENTICATION_ERROR,
                    List.of(RemediationActionFactory.createInformActionALLParticipants(PASSWORD_CHANGED_ERROR, PASSWORD_CHANGED_GUIDANCE)),
                    CONFIRM_REENTER_AUTHORIZATION, StateMapperUtil.addAuthorizationMetaDataToMap(stateId), ExtensionResponse.Result.SUCCESS);
        }
        // 3. User Deleted in Domain
        else if (errorMessage.contains(USER_DELETED_ERROR)) {
            return ExtensionResponseFactory.create(USER_DELETED_ERROR, ExtensionResponse.Error.ExceptionType.AUTHENTICATION_ERROR,
                    List.of(RemediationActionFactory.createInformActionALLParticipants(USER_DELETED_ERROR, USER_DELETED_GUIDANCE)),
                    CONFIRM_REENTER_AUTHORIZATION, StateMapperUtil.addAuthorizationMetaDataToMap(stateId), ExtensionResponse.Result.SUCCESS);
        }
        // 4. User Disabled
        else if (errorMessage.contains(USER_DISABLED_ERROR)) {
            return ExtensionResponseFactory.create(USER_DISABLED_ERROR, ExtensionResponse.Error.ExceptionType.AUTHENTICATION_ERROR,
                    List.of(RemediationActionFactory.createInformActionALLParticipants(USER_DISABLED_ERROR, USER_DISABLED_GUIDANCE)),
                    CONFIRM_REENTER_AUTHORIZATION, StateMapperUtil.addAuthorizationMetaDataToMap(stateId), ExtensionResponse.Result.FAILURE);
        }
        // 5. Permission Revoked
        else if (errorMessage.contains(PERMISSIONS_REVOKED_ERROR)) {
            return ExtensionResponseFactory.create(PERMISSIONS_REVOKED_ERROR, ExtensionResponse.Error.ExceptionType.AUTHENTICATION_ERROR,
                    List.of(RemediationActionFactory.createInformActionALLParticipants(PERMISSIONS_REVOKED_ERROR, PERMISSIONS_REVOKED_GUIDANCE)),
                    CONFIRM_REENTER_AUTHORIZATION, StateMapperUtil.addAuthorizationMetaDataToMap(stateId), ExtensionResponse.Result.SUCCESS);
        }
        // 6. Application Not Found
        else if (errorMessage.contains(APP_NOT_FOUND_ERROR)) {
            return ExtensionResponseFactory.create(APP_NOT_FOUND_ERROR, ExtensionResponse.Error.ExceptionType.AUTHENTICATION_ERROR,
                    List.of(RemediationActionFactory.createInformActionALLParticipants(APP_NOT_FOUND_ERROR, APP_NOT_FOUND_GUIDANCE)),
                    CONFIRM_REENTER_AUTHORIZATION, StateMapperUtil.addAuthorizationMetaDataToMap(stateId), ExtensionResponse.Result.SUCCESS);
        }
        // 7. Tenant Not Found
        else if (errorMessage.contains(TENANT_NOT_FOUND_CODE) || errorMessage.contains(KEYWORD_TENANT_NOT_FOUND)) {
            return ExtensionResponseFactory.create(TENANT_NOT_FOUND_ERROR, ExtensionResponse.Error.ExceptionType.AUTHENTICATION_ERROR,
                    List.of(RemediationActionFactory.createInformActionALLParticipants(TENANT_NOT_FOUND_ERROR, TENANT_NOT_FOUND_GUIDANCE)),
                    CONFIRM_REENTER_AUTHORIZATION, StateMapperUtil.addAuthorizationMetaDataToMap(stateId), ExtensionResponse.Result.SUCCESS);
        }
        // 8. Network or Service Unavailable
        else if (errorMessage.contains(SERVICE_UNAVAILABLE_CODE) || errorMessage.contains(KEYWORD_SERVICE_UNAVAILABLE) ||
                errorMessage.contains(KEYWORD_NETWORK_ERROR)) {
            return ExtensionResponseFactory.create(SERVICE_UNAVAILABLE_ERROR, ExtensionResponse.Error.ExceptionType.AUTHENTICATION_ERROR,
                    List.of(RemediationActionFactory.createInformActionALLParticipants(SERVICE_UNAVAILABLE_ERROR, SERVICE_UNAVAILABLE_GUIDANCE)),
                    CONFIRM_REENTER_AUTHORIZATION, StateMapperUtil.addAuthorizationMetaDataToMap(stateId), ExtensionResponse.Result.SUCCESS);
        }
        // 9. Invalid Client Secret
        else if (errorMessage.contains(INVALID_CLIENT_SECRET_CODE) || errorMessage.contains(KEYWORD_INVALID_CLIENT_SECRET)) {
            return ExtensionResponseFactory.create(INVALID_CLIENT_SECRET_ERROR, ExtensionResponse.Error.ExceptionType.AUTHENTICATION_ERROR,
                    List.of(RemediationActionFactory.createInformActionALLParticipants(INVALID_CLIENT_SECRET_ERROR, INVALID_CLIENT_SECRET_GUIDANCE)),
                    CONFIRM_REENTER_AUTHORIZATION, StateMapperUtil.addAuthorizationMetaDataToMap(stateId), ExtensionResponse.Result.SUCCESS);
        }

        // If it's not one of the specific cases we handle, provide general guidance
        return ExtensionResponseFactory.create("Authentication error: " + errorMessage, ExtensionResponse.Error.ExceptionType.AUTHENTICATION_ERROR,
                List.of(RemediationActionFactory.createInformActionALLParticipants("Authentication error occurred. Please try again or contact your administrator.", GENERAL_GUIDANCE)),
                CONFIRM_REENTER_AUTHORIZATION, StateMapperUtil.addAuthorizationMetaDataToMap(stateId), ExtensionResponse.Result.SUCCESS);
    }

}
