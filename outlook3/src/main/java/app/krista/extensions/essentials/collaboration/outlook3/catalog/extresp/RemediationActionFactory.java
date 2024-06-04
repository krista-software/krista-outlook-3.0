package app.krista.extensions.essentials.collaboration.outlook3.catalog.extresp;

import app.krista.extension.executor.RemediationAction;
import app.krista.extension.executor.impl.AskAPersonAction;
import app.krista.extension.executor.impl.InformAPersonAction;
import app.krista.model.field.NamedField;
import app.krista.model.field.NamedValuedField;

import java.util.List;

public class RemediationActionFactory {

    private RemediationActionFactory(){}

    public static RemediationAction createAskAction(String message,
                                           List<NamedField> fields) {
        return AskAPersonAction.create(message, RemediationAction.RecipientType.ACTIVE_USER, fields);
    }

    public static RemediationAction createInformAction(String message,
                                                       List<NamedValuedField> fields) {
        return InformAPersonAction.create(message, RemediationAction.RecipientType.ACTIVE_USER, fields);
    }

    public static RemediationAction createInformActionALLParticipants(String message,
                                                       List<NamedValuedField> fields) {
        return InformAPersonAction.create(message, RemediationAction.RecipientType.ALL_PARTICIPANTS, fields);
    }
}
