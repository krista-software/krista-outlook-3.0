package app.krista.extensions.essentials.collaboration.outlook3.catalog.validators;

import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.extresp.FieldTypes;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.extresp.OutlookResources;
import app.krista.extensions.essentials.collaboration.outlook3.service.Account;
import app.krista.extensions.essentials.collaboration.outlook3.service.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Validator for email message IDs in the Outlook extension.
 *
 * <p>This validator verifies that a message ID corresponds to an actual email message
 * accessible through the user's Outlook account. It performs a live lookup via the
 * Microsoft Graph API to confirm the message exists and is accessible to the user.</p>
 *
 * <p>The validator handles authentication errors by re-throwing MustAuthorizeException
 * to trigger re-authentication flows, and treats other exceptions (such as network errors
 * or invalid message IDs) as validation failures.</p>
 */
public class MessageIdValidator implements Validator {

    private final Account account;

    private static final Logger logger = LoggerFactory.getLogger(MessageIdValidator.class);

    public MessageIdValidator(Account account) {
        this.account = account;
    }

    /**
     * Validates a message ID by attempting to retrieve the corresponding email from Outlook.
     *
     * <p>This method performs a live lookup via Microsoft Graph API to verify that:
     * <ul>
     *   <li>The message ID is valid and properly formatted</li>
     *   <li>The message exists in the user's mailbox</li>
     *   <li>The user has permission to access the message</li>
     * </ul>
     * </p>
     *
     * @param resourceId the message ID to validate
     * @param context additional validation context (not used by this validator)
     * @return true if the message exists and is accessible, false otherwise
     * @throws MustAuthorizeException if authentication fails, requiring user re-authorization
     */
    @Override
    public Boolean validate(String resourceId, Map<ValidationResource, String> context) {
        try {
            Email email = account.getEmail(resourceId);
            return email != null && email.getEmailId() != null;
        } catch (MustAuthorizeException cause) {
            logger.error("Exception thrown while authorizing user : {} {}", cause.getMessage(), cause);
            throw cause;
        } catch (Exception cause) {
            logger.error("Exception thrown while validating message ID: {}", cause.getMessage(), cause);
            return false;
        }
    }

    @Override
    public String getFetchFieldName() {
        return OutlookResources.MESSAGE_ID;
    }

    @Override
    public String getFieldType() {
        return FieldTypes.TEXT_FIELD;
    }

    @Override
    public String getFetchStepMessage() {
        return "Please enter a valid Message ID.";
    }

    @Override
    public String getConfirmationStepMessage(String resourceId, Map<ValidationResource, String> context) {
        return String.format("Message not found with ID: %s. Please verify the Message ID and try again.", resourceId);
    }

    @Override
    public String getErrMessage(String resourceId) {
        return String.format("Invalid Message ID: %s", resourceId);
    }
}
