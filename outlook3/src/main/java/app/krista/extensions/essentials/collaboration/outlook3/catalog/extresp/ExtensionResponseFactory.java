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
