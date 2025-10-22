package app.krista.extensions.essentials.collaboration.outlook3.impl;

import app.krista.extensions.essentials.collaboration.outlook3.impl.connectors.GraphServiceClientProvider;
import app.krista.extensions.essentials.collaboration.outlook3.impl.util.Constants;
import com.microsoft.graph.core.ClientException;
import com.microsoft.graph.models.Subscription;
import com.microsoft.graph.requests.SubscriptionCollectionPage;
import com.microsoft.graph.requests.SubscriptionCollectionRequestBuilder;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

/**
 * This class manages folder monitoring subscriptions for enhanced email alerts.
 * It creates subscriptions that monitor ALL folders and trigger on both created and updated events.
 */
public class FolderMonitoringSubscription {

    public static final long MAIL_ALERT_SUBSCRIPTION_VALIDITY = 3 * 24 * 60 * 60 * (long) 1000;
    public static final long TWENTY_FIVE_HOURS_IN_MILLIS = 25 * 60 * 60 * (long) 1000;
    private static final Logger LOGGER = LoggerFactory.getLogger(FolderMonitoringSubscription.class);

    private FolderMonitoringSubscription() {
    }

    /**
     * This method will create a folder monitoring subscription with retry mechanism
     *
     * @param routingUrl routing url to get the folder monitoring alert
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
                        LOGGER.info("Found existing folder monitoring subscription. Checking if renewal needed.");
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
            LOGGER.info("No existing folder monitoring subscription found. Creating new subscription.");
            return createSubscriptionWithRetry(provider, subscription);

        } catch (ClientException | ParseException cause) {
            LOGGER.error("Exception occurred while creating or updating folder monitoring subscription: {}", cause.getMessage(), cause);
            return false;
        }
    }

    /**
     * Checks whether the given subscription is nearing expiration and renews it if required.
     *
     * @param provider       {@link GraphServiceClientProvider} object to renew the subscription
     * @param oldSubscription the existing subscription to check
     * @return boolean indicating if renewal was successful or not needed
     */
    private static boolean handleSubscriptionRenewal(GraphServiceClientProvider provider, Subscription oldSubscription) throws ParseException {
        long expirationTime = oldSubscription.expirationDateTime.toEpochSecond() * 1000;
        long currentTime = System.currentTimeMillis();

        if (expirationTime - currentTime < TWENTY_FIVE_HOURS_IN_MILLIS) {
            LOGGER.info("Folder monitoring subscription nearing expiration. Renewing subscription ID: {}", oldSubscription.id);
            return renewSubscription(provider, oldSubscription.id);
        } else {
            LOGGER.info("Folder monitoring subscription is still valid. No renewal needed. Expires at: {}", oldSubscription.expirationDateTime);
            return true;
        }
    }

    /**
     * Creates a new subscription with exponential backoff retry mechanism
     *
     * @param provider     {@link GraphServiceClientProvider} object to create the subscription
     * @param subscription the subscription object to create
     * @return boolean indicating if creation was successful
     */
    private static boolean createSubscriptionWithRetry(GraphServiceClientProvider provider, Subscription subscription) {
        int maxRetries = 3;

        for (int attempt = 0; attempt < maxRetries; attempt++) {
            try {
                LOGGER.info("Attempting to create folder monitoring subscription (attempt {} of {})", attempt + 1, maxRetries);

                Subscription createdSubscription = provider.getGraphServiceClientForAdmin()
                        .subscriptions()
                        .buildRequest()
                        .post(subscription);

                LOGGER.info("Folder monitoring subscription created successfully. ID: {}, Expires: {}",
                        createdSubscription.id, createdSubscription.expirationDateTime);
                return true;

            } catch (ClientException cause) {
                LOGGER.error("Failed to create folder monitoring subscription (attempt {} of {}): {}",
                        attempt + 1, maxRetries, cause.getMessage());

                if (attempt < maxRetries - 1) {
                    try {
                        int waitTime = (int) (Math.pow(2, attempt) * 1000);
                        LOGGER.info("Waiting for {} ms before retry attempt {}", waitTime, attempt + 1);
                        Thread.sleep(waitTime);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return false;
                    }
                }
            }
        }

        return false;
    }

    /**
     * This method is used to set subscription parameters for creating a folder monitoring subscription
     *
     * @param routingUrl      routing url to get the folder monitoring alert
     * @param alertUserMailId email address of the user
     * @return {@link Subscription} object with subscription parameters
     */
    @NotNull
    private static Subscription setSubscriptionParameters(String routingUrl, String alertUserMailId) throws ParseException {
        LOGGER.info("Setting subscription parameters for user: {}", alertUserMailId);
        LOGGER.info("Routing URL: {}", routingUrl);

        Subscription subscription = new Subscription();
        subscription.changeType = Constants.CREATED_AND_UPDATED;
        subscription.notificationUrl = routingUrl + Constants.REST_OUTLOOK_FOLDER_MONITORING_NOTIFICATION;
        subscription.lifecycleNotificationUrl = routingUrl + Constants.REST_OUTLOOK_FOLDER_LIFECYCLE_NOTIFICATION;
        subscription.resource = "/users/" + alertUserMailId + Constants.ME_MESSAGES;
        subscription.expirationDateTime = getExpirationDateTime();

        LOGGER.info("Subscription parameters set - Notification URL: {}, Resource: {}, Expiration: {}",
                subscription.notificationUrl, subscription.resource, subscription.expirationDateTime);

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
        LOGGER.info("Starting folder monitoring subscription renewal for ID: {}", subscriptionId);

        try {
            // Calculate new expiration datetime
            OffsetDateTime newExpirationDate = getExpirationDateTime();
            LOGGER.debug("Calculated new expiration date: {}", newExpirationDate);

            // Prepare subscription object with new expiration time
            Subscription subscription = new Subscription();
            subscription.expirationDateTime = newExpirationDate;
            LOGGER.debug("Created Subscription object with updated expiration");

            // Initiate Graph API call to patch the subscription
            LOGGER.info("Sending patch request to Microsoft Graph for folder monitoring subscription ID: {}", subscriptionId);
            Subscription renewSubscription = provider.getGraphServiceClientForAdmin()
                    .subscriptions(subscriptionId)
                    .buildRequest()
                    .patch(subscription);

            // Log response details
            LOGGER.info("Folder monitoring subscription renewed successfully. ID: {}", renewSubscription.id);
            LOGGER.debug("Renewed subscription details: Expiration: {}, Resource: {}, ChangeType: {}",
                    renewSubscription.expirationDateTime,
                    renewSubscription.resource,
                    renewSubscription.changeType);

            return true;
        } catch (ClientException | ParseException cause) {
            LOGGER.error("Failed to renew folder monitoring subscription ID: {}. Error: {}", subscriptionId, cause.getMessage(), cause);
            return false;
        }
    }

    /**
     * Calculates the expiration datetime for the subscription (3 days from now)
     *
     * @return {@link OffsetDateTime} representing the expiration time
     */
    @NotNull
    private static OffsetDateTime getExpirationDateTime() throws ParseException {
        long expirationTimeMillis = System.currentTimeMillis() + MAIL_ALERT_SUBSCRIPTION_VALIDITY;
        return OffsetDateTime.ofInstant(java.time.Instant.ofEpochMilli(expirationTimeMillis), ZoneOffset.UTC);
    }

    /**
     * Deletes a folder monitoring subscription
     *
     * @param provider       {@link GraphServiceClientProvider} object to delete the subscription
     * @param subscriptionId the ID of the subscription to delete
     * @return boolean indicating if deletion was successful
     */
    public static boolean deleteSubscription(GraphServiceClientProvider provider, String subscriptionId) {
        try {
            LOGGER.info("Deleting folder monitoring subscription ID: {}", subscriptionId);
            provider.getGraphServiceClientForAdmin()
                    .subscriptions(subscriptionId)
                    .buildRequest()
                    .delete();
            LOGGER.info("Folder monitoring subscription deleted successfully. ID: {}", subscriptionId);
            return true;
        } catch (ClientException cause) {
            LOGGER.error("Failed to delete folder monitoring subscription ID: {}. Error: {}", subscriptionId, cause.getMessage(), cause);
            return false;
        }
    }
}

