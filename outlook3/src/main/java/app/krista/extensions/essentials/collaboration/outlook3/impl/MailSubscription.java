package app.krista.extensions.essentials.collaboration.outlook3.impl;

import app.krista.extensions.essentials.collaboration.outlook3.impl.connectors.GraphServiceClientProvider;
import app.krista.extensions.essentials.collaboration.outlook3.impl.util.Constants;
import com.microsoft.graph.core.ClientException;
import com.microsoft.graph.models.Subscription;
import com.microsoft.graph.requests.SubscriptionCollectionPage;
import com.microsoft.graph.requests.SubscriptionCollectionRequestBuilder;
import com.microsoft.graph.serializer.OffsetDateTimeSerializer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;


/**
 * This class is used for Mail Alerts
 */
public class MailSubscription {

    public static final long MAIL_ALERT_SUBSCRIPTION_VALIDITY = 3 * 24 * 60 * 60 * (long) 1000;
    public static final long TWO_FIVE_HOURS_IN_MILLIS = 25 * 60 * 60 * (long) 1000;
    private static final Logger LOGGER = LoggerFactory.getLogger(MailSubscription.class);

    private MailSubscription() {
    }

    /**
     * This method will create a mail alert subscription
     *
     * @param routingUrl routing url to get the mail alert
     * @param provider   {@link GraphServiceClientProvider} object to get the subscriptions from microsoft
     */
    public static void createOrUpdateSubscription(String routingUrl, GraphServiceClientProvider provider) {
        try {
            Subscription subscription = setSubscriptionParameters(routingUrl, provider.getOutlookAttributes().getEmail());

            SubscriptionCollectionPage collectionPage = provider.getGraphServiceClientForAdmin().subscriptions().buildRequest().get();
            while (collectionPage != null && !collectionPage.getCurrentPage().isEmpty()) {
                for (Subscription oldSubscription : collectionPage.getCurrentPage()) {
                    if (Objects.requireNonNull(oldSubscription.notificationUrl).equals(subscription.notificationUrl)) {
                        final long expirationEpoch = oldSubscription.expirationDateTime.toEpochSecond() * 1000;
                        final long currentTimeInMillis = Instant.now().toEpochMilli();
                        final long remainingTime = expirationEpoch - currentTimeInMillis;
                        LOGGER.info("Requires microsoft Renewal after {} hours", (((remainingTime / 1000) / 60) / 60));
                        final long elapsedTime = MAIL_ALERT_SUBSCRIPTION_VALIDITY - remainingTime;
                        LOGGER.info("Requires interval renewal after {} minutes", (((elapsedTime - TWO_FIVE_HOURS_IN_MILLIS) / 1000) / 60));
                        final boolean requireRenew = elapsedTime > TWO_FIVE_HOURS_IN_MILLIS;
                        if (requireRenew) {
                            LOGGER.info("Renewing subscription Mail alert subscription for id: {}", oldSubscription.id);
                            renewSubscription(provider, oldSubscription.id);
                        }
                        return;
                    }
                }
                SubscriptionCollectionRequestBuilder nextPage = collectionPage.getNextPage();
                if (nextPage != null) {
                    collectionPage = nextPage.buildRequest().get();
                } else {
                    break;
                }
            }
            LOGGER.info("Creating new subscription...");
            provider.getGraphServiceClientForAdmin().subscriptions().buildRequest().post(subscription);
        } catch (ClientException | ParseException cause) {
            throw new RuntimeException(cause);
        }
    }

    /**
     * This method is used to set subscription parameters for creating a mail alert subscription
     *
     * @param routingUrl routing url to get the mail alert
     * @return {@link Subscription} object with subscription parameters
     */
    @NotNull
    private static Subscription setSubscriptionParameters(String routingUrl, String alertUserMailId) throws ParseException {
        LOGGER.info("setting subscription parameters");
        Subscription subscription = new Subscription();
        subscription.changeType = Constants.CREATED;
        subscription.notificationUrl = routingUrl + Constants.REST_OUTLOOK_MAIL_NOTIFICATION;
        subscription.lifecycleNotificationUrl = routingUrl + Constants.REST_OUTLOOK_LIFECYCLE_NOTIFICATION;
        subscription.resource = "/users/" + alertUserMailId + Constants.ME_MAIL_FOLDERS_INBOX_MESSAGES;
        long threeDaysInMillis = System.currentTimeMillis() + MAIL_ALERT_SUBSCRIPTION_VALIDITY;
        Date date = new Date(threeDaysInMillis);
        DateFormat dateFormat = new SimpleDateFormat(Constants.YYYY_MM_DD_T_HH_MM_SS_SSSSSSS_Z);
        subscription.expirationDateTime = OffsetDateTimeSerializer.deserialize(dateFormat.format(date));
        return subscription;
    }

    /**
     * This method is used to set the expiry date for the existing subscription
     *
     * @param provider       {@link GraphServiceClientProvider} object to get the subscriptions list
     * @param subscriptionId id of the existing subscription
     */
    public static void renewSubscription(GraphServiceClientProvider provider, String subscriptionId) {
        LOGGER.info("Renewing the subscription with id {} for user {}", subscriptionId,
                provider.getOutlookAttributes().getEmail());
        try {
            Subscription subscription = new Subscription();
            long threeDaysInMillis = System.currentTimeMillis() + MAIL_ALERT_SUBSCRIPTION_VALIDITY;
            Date date = new Date(threeDaysInMillis);
            DateFormat dateFormat = new SimpleDateFormat(Constants.YYYY_MM_DD_T_HH_MM_SS_SSSSSSS_Z);
            subscription.expirationDateTime = OffsetDateTimeSerializer.deserialize(dateFormat.format(date));
            provider.getGraphServiceClientForAdmin().subscriptions(subscriptionId).buildRequest().patch(subscription);
        } catch (ClientException | ParseException cause) {
            throw new RuntimeException(cause);
        }
    }

    /**
     * This method is used to delete the mail alert subscription
     *
     * @param provider   {@link GraphServiceClientProvider} object to get the subscriptions
     * @param routingUrl url to remove the subscription alert
     */
    public static void deleteSubscription(String routingUrl, GraphServiceClientProvider provider) {
        LOGGER.info("Deleting subscription...");
        try {
            SubscriptionCollectionPage collectionPage = provider.getGraphServiceClientForAdmin().subscriptions().buildRequest().get();
            if (collectionPage != null && !collectionPage.getCurrentPage().isEmpty()) {
                for (Subscription subscription : collectionPage.getCurrentPage()) {
                    if (Objects.requireNonNull(subscription.notificationUrl).equals(routingUrl + Constants.REST_OUTLOOK_MAIL_NOTIFICATION)) {
                        LOGGER.info("Deleting the old subscription for user {} with id {}",
                                provider.getOutlookAttributes().getEmail(), subscription.id);
                        provider.getGraphServiceClientForAdmin().subscriptions(Objects.requireNonNull(subscription.id)).buildRequest().delete();
                    }
                }
            }
        } catch (ClientException cause) {
            LOGGER.info(cause.getMessage());
        }
    }
}
