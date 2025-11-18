package app.krista.extensions.essentials.collaboration.outlook3.catalog.validators;

import java.util.Map;

/**
 * Core interface defining the contract for input validation in the Outlook extension.
 *
 * <p>Validators implement this interface to provide validation logic for specific resource types
 * such as email addresses, folder names, message IDs, and pagination parameters. Each validator
 * is responsible for:
 * <ul>
 *   <li>Validating resource values against defined constraints</li>
 *   <li>Providing user-friendly error messages for validation failures</li>
 *   <li>Generating confirmation and fetch step messages for interactive flows</li>
 *   <li>Defining field metadata (name and type) for the validated resource</li>
 * </ul>
 * </p>
 */
public interface Validator {

    /**
     * Enumeration of all validation resource types supported by the Outlook extension.
     * Each enum value represents a specific type of input that can be validated.
     */
    public enum ValidationResource {
        MESSAGE_ID,
        FOLDER_NAME,
        CC,
        TO,
        BCC,
        REPLY_TO,
        QUERY,
        LABEL,
        CATEGORY,
        PAGE_NUMBER,
        PAGE_SIZE
    }

    /**
     * Validates a resource value against defined constraints.
     *
     * @param resourceId the identifier or value of the resource to validate
     * @param context additional validation context containing related resource values
     * @return true if validation passes, false if validation fails
     */
    Boolean validate(String resourceId, Map<ValidationResource, String> context);

    /**
     * Returns the field name used when fetching this resource from the user.
     *
     * @return the field name for user input collection
     */
    String getFetchFieldName();

    /**
     * Returns the field type for this validation resource.
     *
     * @return the field type identifier
     */
    String getFieldType();

    /**
     * Returns the message displayed when prompting the user to provide this resource.
     *
     * @return the fetch step message for user guidance
     */
    String getFetchStepMessage();

    /**
     * Returns a confirmation message after successful validation.
     *
     * @param resourceId the validated resource identifier or value
     * @param context the validation context containing related resource values
     * @return a confirmation message to display to the user
     */
    String getConfirmationStepMessage(String resourceId, Map<ValidationResource, String> context);

    /**
     * Returns an error message when validation fails.
     *
     * @param resourceId the resource identifier or value that failed validation
     * @return a user-friendly error message explaining the validation failure
     */
    String getErrMessage(String resourceId);
}
