package app.krista.extensions.essentials.collaboration.outlook3.catalog.errorhandlers;

import app.krista.extension.executor.ExtensionResponse;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.MessagingAreaSubCatalogRequests;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.extresp.ExtensionResponseFactory;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.extresp.NamedFieldFactory;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.extresp.RemediationActionFactory;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.validators.ValidationOrchestrator;
import app.krista.model.field.NamedField;
import org.jvnet.hk2.annotations.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ExtensionResponseGenerator {

    public ExtensionResponse generateConfirmationResponse(
            ExtensionResponse.Error.ExceptionType exceptionType,
            List<ValidationOrchestrator.ValidationResult> validationResults,
            String subCatalogRequestName,
            Map<String, Object> state) {
        String stepMessage = "";
        String errMessage = "";
        List<NamedField> fields = List.of(NamedFieldFactory.createSwitchField(MessagingAreaSubCatalogRequests.REENTER));
        for(ValidationOrchestrator.ValidationResult validationResult : validationResults) {
            stepMessage += validationResult.getConfirmStepMessage() + "\n";
            errMessage += validationResult.getErrMessage() + " ";
        }
        return ExtensionResponseFactory.create(errMessage, exceptionType,
                List.of(RemediationActionFactory.createAskAction(stepMessage, fields)),
                        subCatalogRequestName, state);
    }

    public ExtensionResponse generateFetchResponse(
            ExtensionResponse.Error.ExceptionType exceptionType,
            List<ValidationOrchestrator.ValidationResult> validationResults,
            String subCatalogRequestName,
            Map<String, Object> state
    ) {
        String stepMessage = "";
        String errMessage = "";
        List<NamedField> fields = new ArrayList<>();
        System.out.println("Validation failed for : " + validationResults.size());
        for(ValidationOrchestrator.ValidationResult validationResult : validationResults) {
            stepMessage += validationResult.getFetchStepMessage() + "\n";
            errMessage += validationResult.getErrMessage() + " ";
            fields.add(NamedFieldFactory.createField(validationResult.getFetchFieldName(), validationResult.getFieldType()));
        }
        return ExtensionResponseFactory.create(errMessage, exceptionType,
                List.of(RemediationActionFactory.createAskAction(stepMessage, fields)),
                subCatalogRequestName, state);
    }

    public ExtensionResponse generateFetchDenyResponse(
            ExtensionResponse.Error.ExceptionType exceptionType,
            List<ValidationOrchestrator.ValidationResult> validationResults,
            String subCatalogRequestName,
            Map<String, Object> state
    ) {
        String stepMessage = "Not taking updated values for ";
        String errMessage = "";
        for(ValidationOrchestrator.ValidationResult validationResult : validationResults) {
            stepMessage += validationResult.getFetchFieldName() + " ";
            errMessage += validationResult.getErrMessage() + "\n";
        }
        stepMessage += ".";
        return ExtensionResponseFactory.create(errMessage, exceptionType,
                List.of(RemediationActionFactory.createInformActionALLParticipants(stepMessage, List.of())),
                subCatalogRequestName, state);
    }
}
