/*
 * Outlook 3.0 Extension for Krista
 * Copyright (C) 2025 Krista Software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
