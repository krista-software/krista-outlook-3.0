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

import com.google.gson.JsonObject;

public class Notification {

    private final NotificationType type;
    private final JsonObject notificationObject;

    public Notification(NotificationType type, JsonObject notificationObject) {
        this.type = type;
        this.notificationObject = notificationObject;
    }

    public NotificationType getType() {
        return type;
    }

    public JsonObject getNotificationObject() {
        return notificationObject;
    }

    public enum NotificationType {
        SUBSCRIPTION("Subscription"), LIFECYCLE("LifeCycle");

        private final String text;

        /**
         * @param text the string value associated with this notification type
         */
        NotificationType(final String text) {
            this.text = text;
        }

        /* (non-Javadoc)
         * @see java.lang.Enum#toString()
         */
        @Override
        public String toString() {
            return text;
        }
    }
}
