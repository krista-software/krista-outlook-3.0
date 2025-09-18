package app.krista.extensions.essentials.collaboration.outlook3.impl.util;

import app.krista.extension.executor.Invoker;
import app.krista.extension.request.RoutingInfo;
import app.krista.extension.request.protos.http.HttpProtocol;
import app.krista.extensions.essentials.collaboration.outlook3.impl.MailSubscription;
import app.krista.extensions.essentials.collaboration.outlook3.impl.connectors.GraphServiceClientProviderFactory;
import app.krista.ksdk.context.RequestContext;
import com.google.gson.JsonArray;
import com.kristasoft.common.holders.ThreadLocalProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NotificationProcessQueue {

    private final GraphServiceClientProviderFactory providerFactory;
    private final String routingUrl;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private static final Logger logger = LoggerFactory.getLogger(NotificationProcessQueue.class);
    private final String invokerId;

    public NotificationProcessQueue(GraphServiceClientProviderFactory providerFactory, Invoker invoker) {
        this.providerFactory = providerFactory;
        this.routingUrl = invoker.getRoutingInfo().getRoutingURL(HttpProtocol.PROTOCOL_NAME, RoutingInfo.Type.APPLIANCE);
        invokerId = invoker.getInvokerId();
    }

    public void add(Notification notification) {
        Map<Class<?>, Object> threadLocals = ThreadLocalProxy.getAll();
        executorService.submit(() -> this.offer(notification, threadLocals));
    }

    private void offer(Notification notification, Map<Class<?>, Object> threadLocals) {
        ThreadLocalProxy.setAll(threadLocals);
        JsonArray array = notification.getNotificationObject().get(Constants.VALUE).getAsJsonArray();

        for (int i = 0; i < array.size(); i++) {
            try {
                String subscriptionId = array.get(i).getAsJsonObject().get(Constants.SUBSCRIPTION_ID).getAsString();
                String lifecycleEvent = array.get(i).getAsJsonObject().get(Constants.LIFECYCLE_EVENT).getAsString();

                logger.info("Received lifecycle event: {} for subscriptionId: {}", lifecycleEvent, subscriptionId);

                if (Constants.SUBSCRIPTION_REMOVED.equalsIgnoreCase(lifecycleEvent)) {
                    logger.info("Handling SUBSCRIPTION_REMOVED event. Recreating subscription...");
                    MailSubscription.createOrUpdateSubscription(this.getRoutingUrl(), providerFactory.create());
                } else if (Constants.REAUTHORIZATION_REQUIRED.equalsIgnoreCase(lifecycleEvent)) {
                    logger.info("Handling REAUTHORIZATION_REQUIRED event. Renewing subscription...");
                    boolean renewed = MailSubscription.renewSubscription(providerFactory.create(ThreadLocalProxy.getThreadLocal(RequestContext.class).get()), subscriptionId);
                    logger.info("Renewal status for subscription {}: {}", subscriptionId, renewed);
                } else {
                    logger.warn("Unhandled lifecycle event: {} for subscriptionId: {}", lifecycleEvent, subscriptionId);
                }

            } catch (Exception cause) {
                logger.error("Error processing lifecycle event for item {}: {}", i, cause.getMessage(), cause);
            } finally {
                ThreadLocalProxy.removeAll();
            }
        }
    }



    public String getRoutingUrl() {
        return routingUrl;
    }
}
