package app.krista.extensions.essentials.collaboration.outlook3.catalog;

import app.krista.extension.executor.ExtensionResponse;
import app.krista.extension.impl.anno.CatalogRequest;
import app.krista.extension.impl.anno.Field;
import app.krista.extension.impl.anno.SubCatalogRequest;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.entities.MailDetails;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.errorhandlers.ErrorHandlingStateManager;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.errorhandlers.ExtensionResponseGenerator;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.extresp.ExtensionResponseFactory;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.validators.ValidationOrchestrator;
import app.krista.extensions.essentials.collaboration.outlook3.impl.MailHandler;
import app.krista.extensions.essentials.collaboration.outlook3.impl.util.Constants;
import app.krista.extensions.essentials.collaboration.outlook3.service.Account;
import app.krista.extensions.essentials.collaboration.outlook3.service.Email;
import app.krista.extensions.essentials.collaboration.outlook3.service.Folder;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class MessagingAreaSubCatalogRequests {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessagingAreaSubCatalogRequests.class);

    private final MailHandler mailHandler;

    private final Account account;

    private final ExtensionResponseGenerator responseGenerator;
    private final ErrorHandlingStateManager internalStateManager;

    @Inject
    public MessagingAreaSubCatalogRequests(MailHandler mailHandler, Account account, ExtensionResponseGenerator responseGenerator, ErrorHandlingStateManager internalStateManager) {
        this.mailHandler = mailHandler;
        this.account = account;
        this.responseGenerator = responseGenerator;
        this.internalStateManager = internalStateManager;
    }

    @SubCatalogRequest(
            name = "confirmReenterFetchMail",
            description = "Checks if user wants to re enter message Id and if yes, sends prompt to do so",
            type = CatalogRequest.Type.QUERY_SYSTEM
    )
    @SuppressWarnings("unchecked")
    public ExtensionResponse confirmReenterFetchMail(@Field.Desc(name = "inputMap",
            type = "{ Reenter: Boolean, stateId: Text, Message ID: Text}", required = true) Map<String, Object> map) {
        Boolean reenter = (Boolean) map.get("Reenter");
        String stateId = (String) map.get("stateId");
        Map<String, Object> state = (Map<String, Object>) internalStateManager.get(stateId);
        List<ValidationOrchestrator.ValidationResult> validationResults =
                (List<ValidationOrchestrator.ValidationResult>) state.get("Validation Results");
        if (!reenter) {
            return responseGenerator.generateFetchDenyResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    validationResults, null,
                    Map.of());
        }
        System.out.println("Reenter was true. Hence continuing.");
        return responseGenerator.generateFetchResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                validationResults, "handleReenterFetchMail",
                Map.of("stateId", stateId));
    }

    @SubCatalogRequest(
            name = "handleReenterFetchMail",
            description = "Handle reenter message id",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Desc(name = "Mail", type = "Entity(Mail Details)", required = false)
    public app.krista.extension.executor.ExtensionResponse handleReenterFetchMail(
            @Field.Desc(name = "inputMap", type = "{Valid Message ID: Text, stateId: Text, Message ID: Text}") Map<String, Object> map) {
        System.out.println("SubCatalogRequest handleReenterMessageId start: " + map);
        String messageID = (String) map.get("Message ID");

        try {
            MailDetails mailDetails = mailHandler.fromEmail(account.getEmail(messageID), null);
            if (mailDetails != null) {
                LOGGER.info("fetchMailByMessageId: ID {}, from {}, subject {}, timestamp {}",
                        mailDetails.messageID, mailDetails.from, mailDetails.subject, new Date(mailDetails.sendDateAndTime));
            }
            return ExtensionResponseFactory.create(Map.of("Mail", mailDetails));
        } catch (Exception cause) {
            return ExtensionResponseFactory.create(cause, "Failed to fetch mail",
                    ExtensionResponse.Error.ExceptionType.LOGIC_ERROR);
        }
    }

    @SubCatalogRequest(
            name = "confirmReenterMoveMessage",
            description = "Checks if user wants to re enter message Id and if yes, sends prompt to do so",
            type = CatalogRequest.Type.QUERY_SYSTEM
    )
    @SuppressWarnings("unchecked")
    public ExtensionResponse confirmReenterMoveMessage(@Field.Desc(name = "inputMap",
            type = "{ Reenter: Boolean, stateId: Text, Message ID: Text, Folder Name: Text}", required = true) Map<String, Object> map) {
        Boolean reenter = (Boolean) map.get("Reenter");
        String stateId = (String) map.get("stateId");
        Map<String, Object> state = (Map<String, Object>) internalStateManager.get(stateId);
        List<ValidationOrchestrator.ValidationResult> validationResults =
                (List<ValidationOrchestrator.ValidationResult>) state.get("Validation Results");
        if (!reenter) {
            return responseGenerator.generateFetchDenyResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    validationResults, null,
                    Map.of());
        }
        System.out.println("Reenter was true. Hence continuing.");
        return responseGenerator.generateFetchResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                validationResults, "handleReenterFetchMail",
                Map.of("stateId", stateId, "Message ID", map.get("Message ID"), "Folder Name", map.get("Folder Name")));
    }

    @SubCatalogRequest(
            name = "handleReenterMoveMessage",
            description = "Handle reenter message id",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field(name = "Message ID", type = "Text", required = false)
    public app.krista.extension.executor.ExtensionResponse handleReenterMoveMessage(
            @Field.Desc(name = "inputMap", type = "{stateId: Text, Message ID: Text, Folder Name: Text}") Map<String, Object> map) {
        System.out.println("SubCatalogRequest handleReenterMessageId start: " + map);
        String messageID = (String) map.get("Message ID");
        String folderName = (String) map.get("Folder Name");

        try {
            Email email = account.getEmail(messageID);
            if (email == null) {
                throw new IllegalStateException(Constants.INVALID_MESSAGE_ID + messageID);
            }
            Folder folder = account.getFolderByName(List.of(folderName.split(Constants.FORWARD_SLASH)));
            if (folder == null) {
                throw new IllegalStateException(Constants.INCORRECT_FOLDER_NAME + folderName);
            }
            return ExtensionResponseFactory.create(Map.of("Message ID", email.moveToFolder(folder)));
        } catch (Exception cause) {
            return ExtensionResponseFactory.create(cause, "Failed to Move Message",
                    ExtensionResponse.Error.ExceptionType.LOGIC_ERROR);
        }
    }
}
