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