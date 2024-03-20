package app.krista.extensions.essentials.collaboration.outlook3.impl.util;

public class AuthenticationResponse {
    private final boolean isSuccess;
    private final String errorMessage;
    private final String url;

    public AuthenticationResponse(boolean isSuccess, String errorMessage, String url) {
        this.isSuccess = isSuccess;
        this.errorMessage = errorMessage;
        this.url = url;
    }

    public String getUrl() {
        return this.url;
    }

    public boolean isSuccess() {
        return isSuccess;
    }
}
