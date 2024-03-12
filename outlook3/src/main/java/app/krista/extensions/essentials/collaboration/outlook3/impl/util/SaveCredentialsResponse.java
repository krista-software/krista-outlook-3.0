package app.krista.extensions.essentials.collaboration.outlook3.impl.util;

public class SaveCredentialsResponse {
    private boolean isSaved;
    private boolean errorWhileSaving;

    public SaveCredentialsResponse(boolean isSaved, boolean errorWhileSaving) {
        this.isSaved = isSaved;
        this.errorWhileSaving = errorWhileSaving;
    }

}
