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
