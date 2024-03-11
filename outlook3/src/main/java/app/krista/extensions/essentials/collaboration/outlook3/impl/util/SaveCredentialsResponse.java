package app.krista.extensions.essentials.collaboration.outlook3.impl.util;

public class SaveCredentialsResponse {
    private final boolean isSaved;
    private final boolean errorWhileSaving;

    public SaveCredentialsResponse(boolean isSaved, boolean errorWhileSaving) {
        this.isSaved = isSaved;
        this.errorWhileSaving = errorWhileSaving;
    }
}
