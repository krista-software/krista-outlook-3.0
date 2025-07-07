package app.krista.extensions.essentials.collaboration.outlook3.impl.util;

import java.util.List;

public class AuthErrorRule {
    private final List<String> indicators;
    private final String userMessage;
    private final boolean removeToken;

    public AuthErrorRule(List<String> indicators, String userMessage, boolean removeToken) {
        this.indicators = indicators;
        this.userMessage = userMessage;
        this.removeToken = removeToken;
    }

    public boolean matches(String message) {
        return indicators.stream().anyMatch(message::contains);
    }

    public String getUserMessage() {
        return userMessage;
    }

    public boolean shouldRemoveToken() {
        return removeToken;
    }
}