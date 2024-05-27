package app.krista.extensions.essentials.collaboration.outlook3.catalog.validators;

import app.krista.extensions.essentials.collaboration.outlook3.impl.AccountImpl;
import app.krista.extensions.essentials.collaboration.outlook3.impl.EmailImpl;
import app.krista.extensions.essentials.collaboration.outlook3.service.Account;
import com.microsoft.graph.models.Message;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    }

    public class ValidationResult {
        private final String confirmStepMessage;
        private final String fetchFieldName;
        private final String fetchStepMessage;
        private final String errMessage;

        public ValidationResult(String confirmStepMessage, String fetchFieldName, String fetchStepMessage, String errMessage) {
            this.confirmStepMessage = confirmStepMessage;
            this.fetchFieldName = fetchFieldName;
            this.fetchStepMessage = fetchStepMessage;
            this.errMessage = errMessage;
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

        public String getFetchStepMessage() {
            return fetchStepMessage;
        }
    }

    private final Map<Validator.ValidationResource, Validator> validators = new HashMap<>();
    public List<ValidationResult> validate(Map<Validator.ValidationResource, String> resources) {
        List<ValidationResult> results = new ArrayList<>();
        for(Map.Entry<Validator.ValidationResource, String> entry : resources.entrySet()) {
            Validator validator = validators.get(entry.getKey());
            assert validator != null;
            if(!validator.validate(entry.getValue(), resources)) {
                results.add(new ValidationResult(validator.getConfirmationStepMessage(entry.getValue(), resources),
                        validator.getFetchFieldName(), validator.getFetchStepMessage(),
                        validator.getErrMessage(entry.getValue())));
            }
        }
        return results;
    }
}
