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
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.Objects;

/**
 * This class is used for Mail Alerts
 */
public class MailSubscription {

    public static final long MAIL_ALERT_SUBSCRIPTION_VALIDITY = 3 * 24 * 60 * 60 * (long) 1000;
    public static final long TWENTY_FIVE_HOURS_IN_MILLIS = 25 * 60 * 60 * (long) 1000;
    private static final Logger LOGGER = LoggerFactory.getLogger(MailSubscription.class);

    private MailSubscription() {
    }

    /**
     * This method will create a mail alert subscription with retry mechanism
     *
     * @param routingUrl routing url to get the mail alert
     * @param provider   {@link GraphServiceClientProvider} object to get the subscriptions from microsoft
     * @return boolean indicating if subscription was created successfully
     */
    public static boolean createOrUpdateSubscription(String routingUrl, GraphServiceClientProvider provider) {
        try {
            // Step 1: Prepare subscription parameters
            Subscription subscription = setSubscriptionParameters(routingUrl, provider.getOutlookAttributes().getEmail());

            // Step 2: Fetch existing subscriptions
            SubscriptionCollectionPage collectionPage = provider.getGraphServiceClientForAdmin()
                    .subscriptions()
                    .buildRequest()
                    .get();

            // Step 3: Check if subscription already exists
            while (collectionPage != null && !collectionPage.getCurrentPage().isEmpty()) {
                for (Subscription oldSubscription : collectionPage.getCurrentPage()) {
                    if (Objects.requireNonNull(oldSubscription.notificationUrl).equals(subscription.notificationUrl)) {
                        LOGGER.info("Found existing subscription. Checking if renewal needed.");
                        return handleSubscriptionRenewal(provider, oldSubscription);
                    }
                }

                SubscriptionCollectionRequestBuilder nextPage = collectionPage.getNextPage();
                if (nextPage != null) {
                    collectionPage = nextPage.buildRequest().get();
                } else {
                    break;
                }
            }

            // Step 4: No matching subscription found, creating new one with retry mechanism
            LOGGER.info("No existing subscription found. Creating new subscription.");
            return createSubscriptionWithRetry(provider, subscription);

        } catch (ClientException | ParseException cause) {
            LOGGER.error("Exception occurred while creating or updating subscription: {}", cause.getMessage(), cause);
            return false;
        }
    }

    /**
     * Checks whether the given subscription is nearing expiration and renews it if required.
     * <p>
     * Microsoft Graph subscriptions expire after a maximum of 72 hours. This method calculates
     * the time remaining and initiates a renewal if more than 25 hours have elapsed since creation.
     *
     * @param provider        The {@link GraphServiceClientProvider} instance used to perform the renewal.
     * @param oldSubscription The existing {@link Subscription} object to evaluate.
     * @return {@code true} if the subscription is valid (either renewed or still active),
     * {@code false} if renewal was needed but failed.
     */
    public static boolean handleSubscriptionRenewal(GraphServiceClientProvider provider, Subscription oldSubscription) {
        // Calculate time remaining until expiration
        final long expirationEpoch = Objects.requireNonNull(oldSubscription.expirationDateTime).toEpochSecond() * 1000;
        final long currentTimeInMillis = Instant.now().toEpochMilli();
        final long remainingTime = expirationEpoch - currentTimeInMillis;

        // Calculate elapsed time since creation
        final long elapsedTime = MAIL_ALERT_SUBSCRIPTION_VALIDITY - remainingTime;

        // Determine if renewal is required
        final boolean requireRenew = elapsedTime > TWENTY_FIVE_HOURS_IN_MILLIS;
        LOGGER.info("Subscription created {} ms ago. Remaining time: {} ms. Renewal required: {}", elapsedTime, remainingTime, requireRenew);

        if (requireRenew) {
            LOGGER.info("Renewing subscription with ID: {}", oldSubscription.id);
            return renewSubscription(provider, oldSubscription.id);
        }

        return true; // Subscription exists and doesn't need renewal
    }

    /**
     * Creates a subscription with retry mechanism
     *
     * @param provider     GraphServiceClientProvider to use
     * @param subscription Subscription to create
     * @return boolean indicating if subscription was created successfully
     */
    private static boolean createSubscriptionWithRetry(GraphServiceClientProvider provider, Subscription subscription) {
        int maxRetries = 5;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            LOGGER.info("Attempting to create subscription - attempt {}/{}", attempt, maxRetries);
            try {
                // Use synchronous approach for better error handling
                Subscription newSubscription = provider.getGraphServiceClientForAdmin()
                        .subscriptions()
                        .buildRequest()
                        .post(subscription);

                LOGGER.info("Subscription created successfully ::: {} ", newSubscription.id);
                return true; // Success
            } catch (ClientException cause) {
                String errorMessage = cause.getMessage();

                if (errorMessage.contains("ValidationError") &&
                        errorMessage.contains("Notification endpoint must respond with 200 OK")) {
                    LOGGER.error("Validation error: Microsoft Graph API cannot reach notification endpoint: {}",
                            subscription.notificationUrl);
                } else {
                    LOGGER.error("Error on attempt {}/{}: {}", attempt, maxRetries, errorMessage);
                }

                // If this is the last attempt, return failure
                if (attempt == maxRetries) {
                    LOGGER.error("All {} retry attempts failed", maxRetries);
                    return false;
                }

                try {
                    // Exponential backoff: 2^attempt * 1000 ms (2s, 4s, 8s, 16s, 32s)
                    int waitTime = (int) (Math.pow(2, attempt) * 1000);
                    LOGGER.info("Waiting for {} ms before retry attempt {}", waitTime, attempt + 1);
                    Thread.sleep(waitTime);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
        }

        return false; // Should never reach here, but just in case
    }

    /**
     * This method is used to set subscription parameters for creating a mail alert subscription
     *
     * @param routingUrl      routing url to get the mail alert
     * @param alertUserMailId email address of the user
     * @return {@link Subscription} object with subscription parameters
     */
    @NotNull
    private static Subscription setSubscriptionParameters(String routingUrl, String alertUserMailId) throws ParseException {
        Subscription subscription = new Subscription();
        subscription.changeType = Constants.CREATED;
        subscription.notificationUrl = routingUrl + Constants.REST_OUTLOOK_MAIL_NOTIFICATION;
        subscription.lifecycleNotificationUrl = routingUrl + Constants.REST_OUTLOOK_LIFECYCLE_NOTIFICATION;
        subscription.resource = "/users/" + alertUserMailId + Constants.ME_MAIL_FOLDERS_INBOX_MESSAGES;
        subscription.expirationDateTime = getExpirationDateTime();
        return subscription;
    }

    /**
     * This method is used to set the expiry date for the existing subscription
     *
     * @param provider       {@link GraphServiceClientProvider} object to get the subscriptions list
     * @param subscriptionId id of the existing subscription
     * @return boolean indicating if renewal was successful
     */
    public static boolean renewSubscription(GraphServiceClientProvider provider, String subscriptionId) {
        LOGGER.info("Starting subscription renewal for ID: {}", subscriptionId);

        final int maxRetries = 3;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                Subscription renewedSubscription = renewSubscriptionOnce(provider, subscriptionId, attempt, maxRetries);

                LOGGER.info("Subscription renewed successfully. ID: {}", renewedSubscription.id);
                LOGGER.debug("Renewed subscription details: Expiration: {}, Resource: {}, ChangeType: {}",
                        renewedSubscription.expirationDateTime,
                        renewedSubscription.resource,
                        renewedSubscription.changeType);

                return true;
            } catch (ClientException | ParseException cause) {
                String errorMessage = cause.getMessage();
                LOGGER.error("Failed to renew subscription ID: {} on attempt {}/{}. Error: {}", subscriptionId, attempt, maxRetries, errorMessage, cause);

                if (attempt == maxRetries) {
                    LOGGER.error("All {} renewal attempts failed for subscription ID: {}", maxRetries, subscriptionId);
                    return false;
                }

                try {
                    int waitTime = (int) (Math.pow(2, attempt) * 1000L);
                    LOGGER.info("Waiting for {} ms before retry attempt {}", waitTime, attempt + 1);
                    Thread.sleep(waitTime);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    LOGGER.error("Subscription renewal retry interrupted for subscription ID: {}", subscriptionId, ie);
                    return false;
                }
            }
        }

        return false; // Should never reach here, but just in case
    }

    private static Subscription renewSubscriptionOnce(GraphServiceClientProvider provider,
                                                      String subscriptionId,
                                                      int attempt,
                                                      int maxRetries) throws ClientException, ParseException {
        // Calculate new expiration datetime
        OffsetDateTime newExpirationDate = getExpirationDateTime();
        LOGGER.debug("Calculated new expiration date: {}", newExpirationDate);

        // Prepare subscription object with new expiration time
        Subscription subscription = new Subscription();
        subscription.expirationDateTime = newExpirationDate;
        LOGGER.debug("Created Subscription object with updated expiration");

        // Initiate Graph API call to patch the subscription
        LOGGER.info("Sending patch request to Microsoft Graph for subscription ID: {}, attempt {}/{}",
                subscriptionId, attempt, maxRetries);

        return provider.getGraphServiceClientForAdmin()
                .subscriptions(subscriptionId)
                .buildRequest()
                .patch(subscription);
    }

    /**
     * Calculate the expiration date for the subscription
     * Microsoft requires an expiration date, and it can be at most 3 days in the future
     */
    private static OffsetDateTime getExpirationDateTime() throws ParseException {
        long threeDaysInMillis = System.currentTimeMillis() + MAIL_ALERT_SUBSCRIPTION_VALIDITY;
        Date date = new Date(threeDaysInMillis);
        DateFormat dateFormat = new SimpleDateFormat(Constants.YYYY_MM_DD_T_HH_MM_SS_SSSSSSS_Z);
        return OffsetDateTimeSerializer.deserialize(dateFormat.format(date));
    }

    /**
     * This method is used to delete the mail alert subscription
     *
     * @param provider   {@link GraphServiceClientProvider} object to get the subscriptions
     * @param routingUrl url to remove the subscription alert
     * @return boolean indicating if deletion was successful
     */
    public static boolean deleteSubscription(String routingUrl, GraphServiceClientProvider provider) {
        LOGGER.info("Deleting subscription.");

        try {
            SubscriptionCollectionPage collectionPage = provider.getGraphServiceClientForAdmin()
                    .subscriptions()
                    .buildRequest()
                    .get();

            if (collectionPage != null && !collectionPage.getCurrentPage().isEmpty()) {
                boolean foundSubscription = false;

                for (Subscription subscription : collectionPage.getCurrentPage()) {
                    if (Objects.requireNonNull(subscription.notificationUrl)
                            .equals(routingUrl + Constants.REST_OUTLOOK_MAIL_NOTIFICATION)) {

                        provider.getGraphServiceClientForAdmin().subscriptions(Objects.requireNonNull(subscription.id))
                                .buildRequest().delete();

                        LOGGER.info("Subscription deleted successfully");
                        foundSubscription = true;
                    }
                }
                return true; // Return true if we found and deleted a subscription, or if there was nothing to delete
            } else {
                return true; // No subscriptions to delete is still a success
            }
        } catch (ClientException cause) {
            LOGGER.error("Error deleting subscription: {}", cause.getMessage());
            return false; // Return false on error
        }
    }
}