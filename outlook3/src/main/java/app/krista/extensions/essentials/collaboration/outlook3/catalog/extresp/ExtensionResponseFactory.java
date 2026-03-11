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

import app.krista.extension.executor.ExtensionResponse;
import app.krista.extension.executor.RemediationAction;
import app.krista.extension.executor.RemediationActions;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ExtensionResponseFactory {

    private ExtensionResponseFactory() {
    }

    public static ExtensionResponse create(Map<String, Object> values) {
        return new ExtensionResponse(ExtensionResponse.Result.SUCCESS, values,
                null, null, null);
    }

    public static ExtensionResponse create(Exception cause,
                                           String message,
                                           ExtensionResponse.Error.ExceptionType exceptionType) {
        ExtensionResponse.Error error = new ExtensionResponse.Error(message, System.currentTimeMillis(),
                exceptionType, Arrays.toString(cause.getStackTrace()));
        RemediationActions remediationActions = new RemediationActions(List.of(RemediationActionFactory.createInformAction(message, List.of())), null);
        return new ExtensionResponse(ExtensionResponse.Result.FAILURE,
                null, error, remediationActions, Map.of());
    }

    public static ExtensionResponse create(Exception cause,
                                           String message,
                                           ExtensionResponse.Error.ExceptionType exceptionType,
                                           List<RemediationAction> actions,
                                           String subCatalogRequestName,
                                           Map<String, Object> state) {
        RemediationActions remediationActions = new RemediationActions(actions, subCatalogRequestName);
        ExtensionResponse.Error error = new ExtensionResponse.Error(message, System.currentTimeMillis(),
                exceptionType, Arrays.toString(cause.getStackTrace()));
        return new ExtensionResponse(ExtensionResponse.Result.FAILURE, null, error,
                remediationActions, state);
    }

    public static ExtensionResponse create(String message,
                                           ExtensionResponse.Error.ExceptionType exceptionType,
                                           List<RemediationAction> actions,
                                           String subCatalogRequestName,
                                           Map<String, Object> state) {
        RemediationActions remediationActions = new RemediationActions(actions, subCatalogRequestName);
        ExtensionResponse.Error error = new ExtensionResponse.Error(message, System.currentTimeMillis(),
                exceptionType, "");
        return new ExtensionResponse(ExtensionResponse.Result.FAILURE, null, error,
                remediationActions, state);
    }

    public static ExtensionResponse create(String message, ExtensionResponse.Error.ExceptionType exceptionType,
                                           List<RemediationAction> actions, String subCatalogRequestName,
                                           Map<String, Object> state, ExtensionResponse.Result result) {
        RemediationActions remediationActions = new RemediationActions(actions, subCatalogRequestName);
        ExtensionResponse.Error error = new ExtensionResponse.Error(message, System.currentTimeMillis(), exceptionType, "");
        return new ExtensionResponse(result, null, error, remediationActions, state);
    }
}
