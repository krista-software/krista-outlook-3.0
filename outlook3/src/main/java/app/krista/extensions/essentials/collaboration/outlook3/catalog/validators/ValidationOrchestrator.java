package app.krista.extensions.essentials.collaboration.outlook3.catalog.validators;

import app.krista.extensions.essentials.collaboration.outlook3.service.Account;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Orchestrator service that coordinates validation across multiple validator types.
 *
 * <p>This service maintains a registry of all validators and provides a unified interface
 * for validating multiple resources in a single operation. It automatically initializes
 * validators for all supported resource types (message IDs, folder names, email addresses,
 * categories, pagination parameters, etc.) and delegates validation to the appropriate
 * validator based on the resource type.</p>
 *
 * <p>The orchestrator returns detailed validation results including error messages,
 * confirmation messages, and field metadata for each failed validation, enabling
 * interactive validation flows with user feedback.</p>
 */
@Service
public class ValidationOrchestrator {

    @Inject
    public ValidationOrchestrator(Account account) {
        validators.put(Validator.ValidationResource.MESSAGE_ID, new MessageIdValidator(account));
        validators.put(Validator.ValidationResource.FOLDER_NAME, new FolderNameValidator(account));
        validators.put(Validator.ValidationResource.LABEL, new LabelValidator(account));
        validators.put(Validator.ValidationResource.CC, new CCEmaiIValidator());
        validators.put(Validator.ValidationResource.TO, new TOEmaiIValidator());
        validators.put(Validator.ValidationResource.BCC, new BCCEmaiIValidator());
        validators.put(Validator.ValidationResource.REPLY_TO, new ReplyToEmaiIValidator());
        validators.put(Validator.ValidationResource.CATEGORY, new CatagoryValidator(account));
        validators.put(Validator.ValidationResource.PAGE_NUMBER, new PageNumberValidator());
        validators.put(Validator.ValidationResource.PAGE_SIZE, new PageSizeValidator());
    }

    /**
     * Immutable data class representing the result of a validation operation.
     *
     * <p>This class encapsulates all information needed to handle a validation failure,
     * including error messages, confirmation messages, and field metadata for re-prompting
     * the user or displaying validation feedback.</p>
     */
    public static class ValidationResult {
        private final String confirmStepMessage;
        private final String fetchFieldName;
        private final String fetchStepMessage;
        private final String errMessage;
        private final String fieldType;

        public ValidationResult(String confirmStepMessage, String fetchFieldName, String fetchStepMessage, String errMessage, String fieldType) {
            this.confirmStepMessage = confirmStepMessage;
            this.fetchFieldName = fetchFieldName;
            this.fetchStepMessage = fetchStepMessage;
            this.errMessage = errMessage;
            this.fieldType = fieldType;

        }

        public String getConfirmStepMessage() {
            return confirmStepMessage;
        }

        public String getErrMessage() {
            return errMessage;
        }

        public String getFetchFieldName() {
            return fetchFieldName;
        }

        public String getFieldType(){
            return fieldType;
        }

        public String getFetchStepMessage() {
            return fetchStepMessage;
        }
    }

    private final Map<Validator.ValidationResource, Validator> validators = new HashMap<>();

    /**
     * Validates multiple resources and returns detailed results for any validation failures.
     *
     * <p>This method iterates through all provided resources, delegates validation to the
     * appropriate validator for each resource type, and collects validation results for
     * any resources that fail validation. Resources that pass validation are not included
     * in the results.</p>
     *
     * <p>The validation context (all resources) is passed to each validator, allowing
     * validators to perform cross-field validation if needed.</p>
     *
     * @param resources a map of validation resources to their values to be validated
     * @return a list of ValidationResult objects for resources that failed validation;
     *         empty list if all validations pass
     */
    public List<ValidationResult> validate(Map<Validator.ValidationResource, String> resources) {
        List<ValidationResult> results = new ArrayList<>();
        for (Map.Entry<Validator.ValidationResource, String> entry : resources.entrySet()) {
            Validator validator = validators.get(entry.getKey());
            assert validator != null;
            if (!validator.validate(entry.getValue(), resources)) {
                results.add(new ValidationResult(validator.getConfirmationStepMessage(entry.getValue(), resources),
                        validator.getFetchFieldName(), validator.getFetchStepMessage(),
                        validator.getErrMessage(entry.getValue()), validator.getFieldType()));
            }
        }
        return results;
    }
}
