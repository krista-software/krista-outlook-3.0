package app.krista.extensions.essentials.collaboration.outlook3.impl.util;

import app.krista.extension.executor.Invoker;
import app.krista.extension.request.RoutingInfo;
import app.krista.extension.request.protos.http.HttpProtocol;
import app.krista.extensions.essentials.collaboration.outlook3.impl.MailSubscription;
import app.krista.extensions.essentials.collaboration.outlook3.impl.connectors.GraphServiceClientProviderFactory;
import com.google.gson.JsonArray;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NotificationProcessQueue {

    private final GraphServiceClientProviderFactory providerFactory;
    private final String routingUrl;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public NotificationProcessQueue(GraphServiceClientProviderFactory providerFactory, Invoker invoker) {
        this.providerFactory = providerFactory;
        this.routingUrl = invoker.getRoutingInfo().getRoutingURL(HttpProtocol.PROTOCOL_NAME, RoutingInfo.Type.APPLIANCE);
    }

    public void add(Notification notification) {
        executorService.submit(() -> this.offer(notification));
    }

    private void offer(Notification notification) {
        JsonArray array = notification.getNotificationObject().get(Constants.VALUE).getAsJsonArray();
        for (int i = 0; i < array.size(); i++) {
            String subscriptionId = array.get(i).getAsJsonObject().get(Constants.SUBSCRIPTION_ID).getAsString();
            String lifecycleEvent = array.get(i).getAsJsonObject().get(Constants.LIFECYCLE_EVENT).getAsString();
            if (lifecycleEvent.equalsIgnoreCase(Constants.SUBSCRIPTION_REMOVED)) {
                MailSubscription.createOrUpdateSubscription(this.getRoutingUrl(), providerFactory.create());
            } else if (lifecycleEvent.equalsIgnoreCase(Constants.REAUTHORIZATION_REQUIRED)) {
                MailSubscription.renewSubscription(providerFactory.create(), subscriptionId);
            }
        }
    }

    public String getRoutingUrl() {
        return routingUrl;
    }
}
