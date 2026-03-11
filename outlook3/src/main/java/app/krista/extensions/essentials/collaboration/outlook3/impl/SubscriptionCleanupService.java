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

package app.krista.extensions.essentials.collaboration.outlook3.impl;

import app.krista.extensions.essentials.collaboration.outlook3.OutlookAttributes;
import app.krista.extensions.essentials.collaboration.outlook3.impl.connectors.GraphServiceClientProviderFactory;
import app.krista.extensions.essentials.collaboration.outlook3.impl.stores.OutlookAttributeStore;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * Service responsible for cleaning up old subscriptions when user credentials change.
 * 
 * <p>This service handles the following scenarios:
 * <ul>
 *   <li>Deleting old subscriptions when email address changes</li>
 *   <li>Creating new subscriptions after credential updates</li>
 *   <li>Logging all subscription cleanup operations</li>
 * </ul>
 * </p>
 * 
 * <p>This ensures that orphaned subscriptions are not left in Microsoft Graph
 * when users update their credentials or change their email address.</p>
 */
@Service
public class SubscriptionCleanupService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionCleanupService.class);

    private final GraphServiceClientProviderFactory providerFactory;
    private final OutlookAttributeStore outlookAttributeStore;

    @Inject
    public SubscriptionCleanupService(GraphServiceClientProviderFactory providerFactory,
                                      OutlookAttributeStore outlookAttributeStore) {
        this.providerFactory = providerFactory;
        this.outlookAttributeStore = outlookAttributeStore;
    }

    /**
     * Handles subscription cleanup when credentials are updated.
     * 
     * <p>This method:
     * <ul>
     *   <li>Checks if the email address has changed</li>
     *   <li>Deletes the old subscription if email changed</li>
     *   <li>Creates a new subscription for the new email if mail alerts are enabled</li>
     * </ul>
     * </p>
     *
     * @param newAttributes The new credentials being saved
     * @param baseRoutingUrl The base routing URL for subscriptions
     * @param invokerId The invoker ID for loading old credentials
     */
    public void handleCredentialChange(OutlookAttributes newAttributes,
                                       String baseRoutingUrl,
                                       String invokerId) {
        // Load existing credentials to check if we need to delete old subscription
        OutlookAttributes oldAttributes = outlookAttributeStore.load(invokerId);

        LOGGER.info("Processing credential change - New Email: {}, Old Email: {}",
                newAttributes.getEmail(),
                oldAttributes != null ? oldAttributes.getEmail() : "NONE (First time setup)");

        // Check if email has changed
        if (hasEmailChanged(oldAttributes, newAttributes)) {
            LOGGER.info("Email changed detected - initiating subscription cleanup");
            deleteOldSubscription(oldAttributes, baseRoutingUrl);
            createNewSubscriptionIfNeeded(newAttributes, baseRoutingUrl);
        } else if (oldAttributes == null) {
            LOGGER.info("First time setup detected - no old subscription to delete");
        } else {
            LOGGER.info("Email unchanged - no subscription cleanup needed");
        }

        LOGGER.info("========== SUBSCRIPTION CLEANUP COMPLETE ==========");
    }

    /**
     * Checks if the email address has changed between old and new credentials.
     *
     * @param oldAttributes The old credentials (may be null)
     * @param newAttributes The new credentials
     * @return true if email has changed, false otherwise
     */
    private boolean hasEmailChanged(OutlookAttributes oldAttributes, OutlookAttributes newAttributes) {
        return oldAttributes != null && !oldAttributes.getEmail().equals(newAttributes.getEmail());
    }

    /**
     * Deletes the subscription associated with the old email address.
     *
     * @param oldAttributes The old credentials containing the email to delete subscription for
     * @param baseRoutingUrl The base routing URL for the subscription
     */
    private void deleteOldSubscription(OutlookAttributes oldAttributes, String baseRoutingUrl) {
        LOGGER.info("Deleting old subscription for email: {}", oldAttributes.getEmail());

        // Create provider with old credentials to delete old subscription
        String oldAuthContextId = providerFactory.createAttributes(oldAttributes);

        try {
            LOGGER.debug("Attempting to delete subscription for old email: {}", oldAttributes.getEmail());
            boolean deleted = MailSubscription.deleteSubscription(baseRoutingUrl,
                    providerFactory.create(oldAuthContextId));

            if (deleted) {
                LOGGER.info("Successfully deleted old subscription for email: {}", oldAttributes.getEmail());
            } else {
                LOGGER.warn("Failed to delete old subscription for email: {}", oldAttributes.getEmail());
            }
        } catch (Exception e) {
            LOGGER.error("Error deleting old subscription for email: {}", oldAttributes.getEmail(), e);
            // Continue even if deletion fails - new credentials should still be saved
        } finally {
            // Clean up old auth context
            LOGGER.debug("Cleaning up old auth context: {}", oldAuthContextId);
            outlookAttributeStore.remove(oldAuthContextId);
        }
    }

    /**
     * Creates a new subscription for the new email if mail alerts are enabled.
     *
     * @param newAttributes The new credentials
     * @param baseRoutingUrl The base routing URL for the subscription
     */
    private void createNewSubscriptionIfNeeded(OutlookAttributes newAttributes, String baseRoutingUrl) {
        if (!newAttributes.isAllowMailAlert()) {
            LOGGER.info("Mail alerts disabled - skipping new subscription creation for email: {}",
                    newAttributes.getEmail());
            return;
        }

        LOGGER.info("Creating new subscription after email change - Email: {}, Mail Alerts Enabled: {}",
                newAttributes.getEmail(), true);

        String newAuthContextId = providerFactory.createAttributes(newAttributes);

        try {
            LOGGER.debug("Attempting to create subscription for new email: {}", newAttributes.getEmail());
            boolean subscriptionCreated = MailSubscription.createOrUpdateSubscription(
                    baseRoutingUrl,
                    providerFactory.create(newAuthContextId)
            );

            if (subscriptionCreated) {
                LOGGER.info("New subscription created successfully for email: {}", newAttributes.getEmail());
            } else {
                LOGGER.warn("Failed to create new subscription for email: {}", newAttributes.getEmail());
            }
        } catch (Exception e) {
            LOGGER.error("Error creating new subscription for email: {}", newAttributes.getEmail(), e);
        }
    }
}

