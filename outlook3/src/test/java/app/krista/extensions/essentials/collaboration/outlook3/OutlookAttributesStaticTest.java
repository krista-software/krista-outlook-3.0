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

package app.krista.extensions.essentials.collaboration.outlook3;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Real-world unit tests for OutlookAttributes static methods.
 * Tests cover JSON attribute creation for public and private authentication modes.
 */
@DisplayName("OutlookAttributes Static Methods Tests")
class OutlookAttributesStaticTest {

    // ==================== Public Authentication JSON Creation ====================

    @Test
    @DisplayName("Should create JSON attributes for public authentication")
    void testCreateJsonAttributesPublic() {
        JsonObject json = OutlookAttributes.createJsonAttributes(
                "user@example.com",
                null,
                null,
                null,
                true,
                "Public",
                "https://app.krista.ai"
        );

        assertNotNull(json);
        assertEquals("Public", json.get("authType").getAsString());
        assertEquals("user@example.com", json.get("email").getAsString());
        assertTrue(json.get("allowMailAlert").getAsBoolean());
        assertEquals("https://app.krista.ai", json.get("baseUrl").getAsString());

        // Public auth should not have these fields
        assertFalse(json.has("clientId"));
        assertFalse(json.has("clientSecret"));
        assertFalse(json.has("tenantId"));
    }

    @Test
    @DisplayName("Should create JSON attributes for public auth with mail alert disabled")
    void testCreateJsonAttributesPublicNoMailAlert() {
        JsonObject json = OutlookAttributes.createJsonAttributes(
                "admin@company.com",
                null,
                null,
                null,
                false,
                "Public",
                "https://enterprise.krista.ai"
        );

        assertNotNull(json);
        assertEquals("Public", json.get("authType").getAsString());
        assertEquals("admin@company.com", json.get("email").getAsString());
        assertFalse(json.get("allowMailAlert").getAsBoolean());
        assertEquals("https://enterprise.krista.ai", json.get("baseUrl").getAsString());
    }

    // ==================== Private Authentication JSON Creation ====================

    @Test
    @DisplayName("Should create JSON attributes for private authentication")
    void testCreateJsonAttributesPrivate() {
        JsonObject json = OutlookAttributes.createJsonAttributes(
                "admin@enterprise.com",
                "client-id-123",
                "client-secret-xyz",
                "tenant-id-456",
                false,
                "Private",
                "https://enterprise.krista.ai"
        );

        assertNotNull(json);
        assertEquals("Private", json.get("authType").getAsString());
        assertEquals("admin@enterprise.com", json.get("email").getAsString());
        assertFalse(json.get("allowMailAlert").getAsBoolean());
        assertEquals("https://enterprise.krista.ai", json.get("baseUrl").getAsString());

        // Private auth should have these fields
        assertEquals("client-id-123", json.get("clientId").getAsString());
        assertEquals("client-secret-xyz", json.get("clientSecret").getAsString());
        assertEquals("tenant-id-456", json.get("tenantId").getAsString());
    }

    @Test
    @DisplayName("Should create JSON attributes for private auth with mail alert enabled")
    void testCreateJsonAttributesPrivateWithMailAlert() {
        JsonObject json = OutlookAttributes.createJsonAttributes(
                "service@company.com",
                "app-client-id",
                "app-client-secret",
                "company-tenant-id",
                true,
                "Private",
                "https://api.company.com"
        );

        assertNotNull(json);
        assertEquals("Private", json.get("authType").getAsString());
        assertEquals("service@company.com", json.get("email").getAsString());
        assertTrue(json.get("allowMailAlert").getAsBoolean());
        assertEquals("https://api.company.com", json.get("baseUrl").getAsString());
        assertEquals("app-client-id", json.get("clientId").getAsString());
        assertEquals("app-client-secret", json.get("clientSecret").getAsString());
        assertEquals("company-tenant-id", json.get("tenantId").getAsString());
    }

    // ==================== Real-World Configuration Scenarios ====================

    @Test
    @DisplayName("Should create configuration for individual user")
    void testIndividualUserConfiguration() {
        // Scenario: Individual user using public authentication
        JsonObject json = OutlookAttributes.createJsonAttributes(
                "john.doe@gmail.com",
                null,
                null,
                null,
                true,
                "Public",
                "https://app.krista.ai"
        );

        assertEquals("Public", json.get("authType").getAsString());
        assertEquals("john.doe@gmail.com", json.get("email").getAsString());
        assertTrue(json.get("allowMailAlert").getAsBoolean());
        assertFalse(json.has("clientId"));
    }

    @Test
    @DisplayName("Should create configuration for enterprise deployment")
    void testEnterpriseDeploymentConfiguration() {
        // Scenario: Enterprise using private authentication with Azure AD
        JsonObject json = OutlookAttributes.createJsonAttributes(
                "outlook-integration@acme-corp.com",
                "azure-app-id-abc123",
                "azure-app-secret-xyz789",
                "acme-corp-tenant-id",
                false,
                "Private",
                "https://integrations.acme-corp.com"
        );

        assertEquals("Private", json.get("authType").getAsString());
        assertEquals("outlook-integration@acme-corp.com", json.get("email").getAsString());
        assertFalse(json.get("allowMailAlert").getAsBoolean());
        assertEquals("azure-app-id-abc123", json.get("clientId").getAsString());
        assertEquals("azure-app-secret-xyz789", json.get("clientSecret").getAsString());
        assertEquals("acme-corp-tenant-id", json.get("tenantId").getAsString());
    }

    @Test
    @DisplayName("Should create configuration for service account")
    void testServiceAccountConfiguration() {
        // Scenario: Service account for automated email processing
        JsonObject json = OutlookAttributes.createJsonAttributes(
                "email-service@company.com",
                "service-client-id",
                "service-client-secret",
                "company-tenant",
                false,
                "Private",
                "https://services.company.com"
        );

        assertEquals("Private", json.get("authType").getAsString());
        assertEquals("email-service@company.com", json.get("email").getAsString());
        assertFalse(json.get("allowMailAlert").getAsBoolean());
        assertTrue(json.has("clientId"));
        assertTrue(json.has("clientSecret"));
        assertTrue(json.has("tenantId"));
    }

    // ==================== Multi-Tenant Scenarios ====================

    @Test
    @DisplayName("Should create configuration for tenant A")
    void testTenantAConfiguration() {
        JsonObject json = OutlookAttributes.createJsonAttributes(
                "admin@tenant-a.com",
                "tenant-a-client-id",
                "tenant-a-client-secret",
                "tenant-a-id",
                true,
                "Private",
                "https://tenant-a.krista.ai"
        );

        assertEquals("tenant-a-id", json.get("tenantId").getAsString());
        assertEquals("admin@tenant-a.com", json.get("email").getAsString());
        assertEquals("https://tenant-a.krista.ai", json.get("baseUrl").getAsString());
    }

    @Test
    @DisplayName("Should create configuration for tenant B")
    void testTenantBConfiguration() {
        JsonObject json = OutlookAttributes.createJsonAttributes(
                "admin@tenant-b.com",
                "tenant-b-client-id",
                "tenant-b-client-secret",
                "tenant-b-id",
                false,
                "Private",
                "https://tenant-b.krista.ai"
        );

        assertEquals("tenant-b-id", json.get("tenantId").getAsString());
        assertEquals("admin@tenant-b.com", json.get("email").getAsString());
        assertEquals("https://tenant-b.krista.ai", json.get("baseUrl").getAsString());
    }

    // ==================== Mail Alert Configuration ====================

    @Test
    @DisplayName("Should enable mail alerts for monitoring account")
    void testMailAlertsEnabled() {
        JsonObject json = OutlookAttributes.createJsonAttributes(
                "monitoring@company.com",
                null,
                null,
                null,
                true,
                "Public",
                "https://app.krista.ai"
        );

        assertTrue(json.get("allowMailAlert").getAsBoolean());
    }

    @Test
    @DisplayName("Should disable mail alerts for batch processing account")
    void testMailAlertsDisabled() {
        JsonObject json = OutlookAttributes.createJsonAttributes(
                "batch-processor@company.com",
                "batch-client-id",
                "batch-client-secret",
                "batch-tenant-id",
                false,
                "Private",
                "https://batch.company.com"
        );

        assertFalse(json.get("allowMailAlert").getAsBoolean());
    }

    // ==================== Base URL Variations ====================

    @Test
    @DisplayName("Should handle production base URL")
    void testProductionBaseUrl() {
        JsonObject json = OutlookAttributes.createJsonAttributes(
                "user@example.com",
                null,
                null,
                null,
                true,
                "Public",
                "https://app.krista.ai"
        );

        assertEquals("https://app.krista.ai", json.get("baseUrl").getAsString());
    }

    @Test
    @DisplayName("Should handle staging base URL")
    void testStagingBaseUrl() {
        JsonObject json = OutlookAttributes.createJsonAttributes(
                "test@example.com",
                null,
                null,
                null,
                true,
                "Public",
                "https://staging.krista.ai"
        );

        assertEquals("https://staging.krista.ai", json.get("baseUrl").getAsString());
    }

    @Test
    @DisplayName("Should handle localhost base URL")
    void testLocalhostBaseUrl() {
        JsonObject json = OutlookAttributes.createJsonAttributes(
                "dev@example.com",
                null,
                null,
                null,
                true,
                "Public",
                "http://localhost:8080"
        );

        assertEquals("http://localhost:8080", json.get("baseUrl").getAsString());
    }

    @Test
    @DisplayName("Should handle custom domain base URL")
    void testCustomDomainBaseUrl() {
        JsonObject json = OutlookAttributes.createJsonAttributes(
                "admin@company.com",
                "company-client-id",
                "company-client-secret",
                "company-tenant-id",
                false,
                "Private",
                "https://krista.company.com"
        );

        assertEquals("https://krista.company.com", json.get("baseUrl").getAsString());
    }

    // ==================== Email Address Variations ====================

    @Test
    @DisplayName("Should handle corporate email address")
    void testCorporateEmailAddress() {
        JsonObject json = OutlookAttributes.createJsonAttributes(
                "employee@microsoft.com",
                null,
                null,
                null,
                true,
                "Public",
                "https://app.krista.ai"
        );

        assertEquals("employee@microsoft.com", json.get("email").getAsString());
    }

    @Test
    @DisplayName("Should handle personal email address")
    void testPersonalEmailAddress() {
        JsonObject json = OutlookAttributes.createJsonAttributes(
                "user@outlook.com",
                null,
                null,
                null,
                true,
                "Public",
                "https://app.krista.ai"
        );

        assertEquals("user@outlook.com", json.get("email").getAsString());
    }

    @Test
    @DisplayName("Should handle email with subdomain")
    void testEmailWithSubdomain() {
        JsonObject json = OutlookAttributes.createJsonAttributes(
                "admin@it.company.com",
                "it-client-id",
                "it-client-secret",
                "it-tenant-id",
                false,
                "Private",
                "https://it.company.com"
        );

        assertEquals("admin@it.company.com", json.get("email").getAsString());
    }

    // ==================== JSON Structure Validation ====================

    @Test
    @DisplayName("Should create valid JSON structure for public auth")
    void testValidJsonStructurePublic() {
        JsonObject json = OutlookAttributes.createJsonAttributes(
                "user@example.com",
                null,
                null,
                null,
                true,
                "Public",
                "https://app.krista.ai"
        );

        // Verify all required fields are present
        assertTrue(json.has("authType"));
        assertTrue(json.has("email"));
        assertTrue(json.has("allowMailAlert"));
        assertTrue(json.has("baseUrl"));

        // Verify field types
        assertTrue(json.get("authType").isJsonPrimitive());
        assertTrue(json.get("email").isJsonPrimitive());
        assertTrue(json.get("allowMailAlert").isJsonPrimitive());
        assertTrue(json.get("baseUrl").isJsonPrimitive());
    }

    @Test
    @DisplayName("Should create valid JSON structure for private auth")
    void testValidJsonStructurePrivate() {
        JsonObject json = OutlookAttributes.createJsonAttributes(
                "admin@company.com",
                "client-123",
                "secret-456",
                "tenant-789",
                false,
                "Private",
                "https://company.com"
        );

        // Verify all required fields are present
        assertTrue(json.has("authType"));
        assertTrue(json.has("email"));
        assertTrue(json.has("allowMailAlert"));
        assertTrue(json.has("baseUrl"));
        assertTrue(json.has("clientId"));
        assertTrue(json.has("clientSecret"));
        assertTrue(json.has("tenantId"));

        // Verify field types
        assertTrue(json.get("authType").isJsonPrimitive());
        assertTrue(json.get("email").isJsonPrimitive());
        assertTrue(json.get("allowMailAlert").isJsonPrimitive());
        assertTrue(json.get("baseUrl").isJsonPrimitive());
        assertTrue(json.get("clientId").isJsonPrimitive());
        assertTrue(json.get("clientSecret").isJsonPrimitive());
        assertTrue(json.get("tenantId").isJsonPrimitive());
    }

    // ==================== Integration Test Scenarios ====================

    @Test
    @DisplayName("Should create configuration for initial setup")
    void testInitialSetupConfiguration() {
        // Scenario: User setting up Outlook integration for first time
        JsonObject json = OutlookAttributes.createJsonAttributes(
                "newuser@example.com",
                null,
                null,
                null,
                true,
                "Public",
                "https://app.krista.ai"
        );

        assertNotNull(json);
        assertEquals(4, json.size()); // authType, email, allowMailAlert, baseUrl
    }

    @Test
    @DisplayName("Should create configuration for migration from public to private")
    void testMigrationConfiguration() {
        // Scenario: Migrating from public to private authentication
        JsonObject json = OutlookAttributes.createJsonAttributes(
                "user@company.com",
                "new-client-id",
                "new-client-secret",
                "new-tenant-id",
                true,
                "Private",
                "https://company.krista.ai"
        );

        assertEquals("Private", json.get("authType").getAsString());
        assertTrue(json.has("clientId"));
        assertTrue(json.has("clientSecret"));
        assertTrue(json.has("tenantId"));
    }
}

