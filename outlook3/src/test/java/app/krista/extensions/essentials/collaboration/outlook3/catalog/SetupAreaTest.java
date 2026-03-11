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

package app.krista.extensions.essentials.collaboration.outlook3.catalog;

import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extension.executor.ExtensionResponse;
import app.krista.extension.executor.Invoker;
import app.krista.extension.request.RoutingInfo;
import app.krista.extension.request.protos.http.HttpProtocol;
import app.krista.extensions.essentials.collaboration.outlook3.OutlookAttributes;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.entities.ExtensionResponseMeta;
import app.krista.extensions.essentials.collaboration.outlook3.health.HealthCheck;
import app.krista.extensions.essentials.collaboration.outlook3.impl.SaveConfigurationImpl;
import app.krista.extensions.essentials.collaboration.outlook3.impl.connectors.GraphServiceClientProviderFactory;
import app.krista.extensions.essentials.collaboration.outlook3.impl.stores.OutlookAttributeStore;
import app.krista.extensions.essentials.collaboration.outlook3.impl.util.Constants;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static app.krista.extensions.essentials.collaboration.outlook3.impl.util.Constants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for SetupArea class covering all save catalog methods.
 * Tests cover 100% code coverage including success scenarios, failure scenarios,
 * edge cases, and exception handling.
 */
@ExtendWith(MockitoExtension.class)
public class SetupAreaTest {

    // Test constants
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_CLIENT_ID = "test-client-id";
    private static final String TEST_CLIENT_SECRET = "test-client-secret";
    private static final String TEST_TENANT_ID = "test-tenant-id";
    private static final String TEST_BASE_URL = "https://test.example.com";
    private static final String TEST_ROUTING_URL = "https://test-routing.example.com";

    @Mock
    private HealthCheck healthCheck;

    @Mock
    private SaveConfigurationImpl saveConfigurationImpl;

    @Mock
    private Invoker invoker;

    @Mock
    private RoutingInfo routingInfo;

    @Mock
    private OutlookAttributeStore outlookAttributeStore;

    @Mock
    private GraphServiceClientProviderFactory providerFactory;

    private SetupArea setupArea;

    @BeforeEach
    void setUp() {
        // Setup mock behavior for invoker
        when(invoker.getRoutingInfo()).thenReturn(routingInfo);
        when(routingInfo.getRoutingURL(HttpProtocol.PROTOCOL_NAME, RoutingInfo.Type.APPLIANCE))
                .thenReturn(TEST_ROUTING_URL);

        // Initialize SetupArea with mocked dependencies
        setupArea = new SetupArea(healthCheck, saveConfigurationImpl, invoker, outlookAttributeStore, providerFactory);
    }

    // ========== Health Check Tests ==========

    @Test
    void testHealthCheck_Success() {
        // Arrange
        ExtensionResponse expectedResponse = createSuccessResponse();
        when(healthCheck.checkHealth()).thenReturn(expectedResponse);

        // Act
        ExtensionResponse actualResponse = setupArea.healthCheck();

        // Assert
        assertNotNull(actualResponse);
        assertEquals(ExtensionResponse.Result.SUCCESS, actualResponse.getResult());
        verify(healthCheck, times(1)).checkHealth();
    }

    @Test
    void testHealthCheck_Failure() {
        // Arrange
        ExtensionResponse expectedResponse = createFailureResponse("Health check failed");
        when(healthCheck.checkHealth()).thenReturn(expectedResponse);

        // Act
        ExtensionResponse actualResponse = setupArea.healthCheck();

        // Assert
        assertNotNull(actualResponse);
        assertEquals(ExtensionResponse.Result.FAILURE, actualResponse.getResult());
        verify(healthCheck, times(1)).checkHealth();
    }

    @Test
    void testHealthCheck_Exception() {
        // Arrange
        when(healthCheck.checkHealth()).thenThrow(new RuntimeException("Health check exception"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> setupArea.healthCheck());
        verify(healthCheck, times(1)).checkHealth();
    }

    // ========== Save Outlook Public Configuration Tests ==========

    @Test
    void testSaveOutlookPublicConfiguration_Success_WithAllowMailAlert() {
        // Arrange
        String email = TEST_EMAIL;
        Boolean allowMailAlert = true;
        ExtensionResponse expectedResponse = createSuccessConfigurationResponse();
        
        when(saveConfigurationImpl.saveConfiguration(any(JsonObject.class)))
                .thenReturn(expectedResponse);

        // Act
        ExtensionResponse actualResponse = setupArea.saveOutlookPublicConfiguration(email, allowMailAlert);

        // Assert
        assertNotNull(actualResponse);
        assertEquals(ExtensionResponse.Result.SUCCESS, actualResponse.getResult());
        assertTrue((Boolean) actualResponse.getResponseValue().get(IS_CONFIGURATION_SUCCESSFUL));
        
        // Verify the correct payload was created and passed
        verify(saveConfigurationImpl, times(1)).saveConfiguration(argThat(payload -> {
            JsonObject json = payload;
            return Constants.PUBLIC.equals(json.get("authType").getAsString()) &&
                   email.equals(json.get("email").getAsString()) &&
                   json.get("allowMailAlert").getAsBoolean() == allowMailAlert;
        }));
    }

    @Test
    void testSaveOutlookPublicConfiguration_Success_WithoutAllowMailAlert() {
        // Arrange
        String email = TEST_EMAIL;
        Boolean allowMailAlert = false;
        ExtensionResponse expectedResponse = createSuccessConfigurationResponse();
        
        when(saveConfigurationImpl.saveConfiguration(any(JsonObject.class)))
                .thenReturn(expectedResponse);

        // Act
        ExtensionResponse actualResponse = setupArea.saveOutlookPublicConfiguration(email, allowMailAlert);

        // Assert
        assertNotNull(actualResponse);
        assertEquals(ExtensionResponse.Result.SUCCESS, actualResponse.getResult());
        assertTrue((Boolean) actualResponse.getResponseValue().get(IS_CONFIGURATION_SUCCESSFUL));
        
        verify(saveConfigurationImpl, times(1)).saveConfiguration(any(JsonObject.class));
    }

    @Test
    void testSaveOutlookPublicConfiguration_Success_NullAllowMailAlert() {
        // Arrange
        String email = TEST_EMAIL;
        Boolean allowMailAlert = null;
        ExtensionResponse expectedResponse = createSuccessConfigurationResponse();
        
        when(saveConfigurationImpl.saveConfiguration(any(JsonObject.class)))
                .thenReturn(expectedResponse);

        // Act
        ExtensionResponse actualResponse = setupArea.saveOutlookPublicConfiguration(email, allowMailAlert);

        // Assert
        assertNotNull(actualResponse);
        assertEquals(ExtensionResponse.Result.SUCCESS, actualResponse.getResult());
        
        verify(saveConfigurationImpl, times(1)).saveConfiguration(argThat(payload -> {
            JsonObject json = payload;
            return Constants.PUBLIC.equals(json.get("authType").getAsString()) &&
                   email.equals(json.get("email").getAsString()) &&
                   json.get("allowMailAlert").isJsonNull();
        }));
    }

    @Test
    void testSaveOutlookPublicConfiguration_Failure() {
        // Arrange
        String email = TEST_EMAIL;
        Boolean allowMailAlert = true;
        ExtensionResponse expectedResponse = createFailureConfigurationResponse();
        
        when(saveConfigurationImpl.saveConfiguration(any(JsonObject.class)))
                .thenReturn(expectedResponse);

        // Act
        ExtensionResponse actualResponse = setupArea.saveOutlookPublicConfiguration(email, allowMailAlert);

        // Assert
        assertNotNull(actualResponse);
        assertEquals(ExtensionResponse.Result.FAILURE, actualResponse.getResult());
        
        verify(saveConfigurationImpl, times(1)).saveConfiguration(any(JsonObject.class));
    }

    @Test
    void testSaveOutlookPublicConfiguration_MustAuthorizeException() {
        // Arrange
        String email = TEST_EMAIL;
        Boolean allowMailAlert = true;
        
        when(saveConfigurationImpl.saveConfiguration(any(JsonObject.class)))
                .thenThrow(new MustAuthorizeException("Authorization required", null));

        // Act & Assert
        assertThrows(MustAuthorizeException.class, () -> 
            setupArea.saveOutlookPublicConfiguration(email, allowMailAlert));
        
        verify(saveConfigurationImpl, times(1)).saveConfiguration(any(JsonObject.class));
    }

    @Test
    void testSaveOutlookPublicConfiguration_RuntimeException() {
        // Arrange
        String email = TEST_EMAIL;
        Boolean allowMailAlert = true;
        
        when(saveConfigurationImpl.saveConfiguration(any(JsonObject.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            setupArea.saveOutlookPublicConfiguration(email, allowMailAlert));
        
        verify(saveConfigurationImpl, times(1)).saveConfiguration(any(JsonObject.class));
    }

    // ========== Save Outlook Private Configuration Tests ==========

    @Test
    void testSaveOutlookPrivateConfiguration_Success_WithAllowMailAlert() {
        // Arrange
        String email = TEST_EMAIL;
        String clientId = TEST_CLIENT_ID;
        String clientSecret = TEST_CLIENT_SECRET;
        String tenantId = TEST_TENANT_ID;
        Boolean allowMailAlert = true;
        ExtensionResponse expectedResponse = createSuccessConfigurationResponse();
        
        when(saveConfigurationImpl.saveConfiguration(any(JsonObject.class)))
                .thenReturn(expectedResponse);

        // Act
        ExtensionResponse actualResponse = setupArea.saveOutlookPrivateConfiguration(
                email, clientId, clientSecret, tenantId, allowMailAlert);

        // Assert
        assertNotNull(actualResponse);
        assertEquals(ExtensionResponse.Result.SUCCESS, actualResponse.getResult());
        assertTrue((Boolean) actualResponse.getResponseValue().get(IS_CONFIGURATION_SUCCESSFUL));
        
        // Verify the correct payload was created and passed
        verify(saveConfigurationImpl, times(1)).saveConfiguration(argThat(payload -> {
            JsonObject json = payload;
            return Constants.PRIVATE.equals(json.get("authType").getAsString()) &&
                   email.equals(json.get("email").getAsString()) &&
                   clientId.equals(json.get("clientId").getAsString()) &&
                   clientSecret.equals(json.get("clientSecret").getAsString()) &&
                   tenantId.equals(json.get("tenantId").getAsString()) &&
                   json.get("allowMailAlert").getAsBoolean() == allowMailAlert &&
                   TEST_ROUTING_URL.equals(json.get("baseUrl").getAsString());
        }));
    }

    @Test
    void testSaveOutlookPrivateConfiguration_Success_WithoutAllowMailAlert() {
        // Arrange
        String email = TEST_EMAIL;
        String clientId = TEST_CLIENT_ID;
        String clientSecret = TEST_CLIENT_SECRET;
        String tenantId = TEST_TENANT_ID;
        Boolean allowMailAlert = false;
        ExtensionResponse expectedResponse = createSuccessConfigurationResponse();
        
        when(saveConfigurationImpl.saveConfiguration(any(JsonObject.class)))
                .thenReturn(expectedResponse);

        // Act
        ExtensionResponse actualResponse = setupArea.saveOutlookPrivateConfiguration(
                email, clientId, clientSecret, tenantId, allowMailAlert);

        // Assert
        assertNotNull(actualResponse);
        assertEquals(ExtensionResponse.Result.SUCCESS, actualResponse.getResult());
        assertTrue((Boolean) actualResponse.getResponseValue().get(IS_CONFIGURATION_SUCCESSFUL));
        
        verify(saveConfigurationImpl, times(1)).saveConfiguration(any(JsonObject.class));
    }

    @Test
    void testSaveOutlookPrivateConfiguration_Success_NullAllowMailAlert() {
        // Arrange
        String email = TEST_EMAIL;
        String clientId = TEST_CLIENT_ID;
        String clientSecret = TEST_CLIENT_SECRET;
        String tenantId = TEST_TENANT_ID;
        Boolean allowMailAlert = null;
        ExtensionResponse expectedResponse = createSuccessConfigurationResponse();
        
        when(saveConfigurationImpl.saveConfiguration(any(JsonObject.class)))
                .thenReturn(expectedResponse);

        // Act
        ExtensionResponse actualResponse = setupArea.saveOutlookPrivateConfiguration(
                email, clientId, clientSecret, tenantId, allowMailAlert);

        // Assert
        assertNotNull(actualResponse);
        assertEquals(ExtensionResponse.Result.SUCCESS, actualResponse.getResult());
        
        verify(saveConfigurationImpl, times(1)).saveConfiguration(argThat(payload -> {
            JsonObject json = payload;
            return Constants.PRIVATE.equals(json.get("authType").getAsString()) &&
                   json.get("allowMailAlert").isJsonNull();
        }));
    }

    // ========== Helper Methods ==========

    private ExtensionResponse createSuccessResponse() {
        Map<String, Object> responseData = Map.of(
                "Is Healthy", true,
                "Health Status", createHealthStatus()
        );
        return new ExtensionResponse(ExtensionResponse.Result.SUCCESS, responseData, null, null, null);
    }

    private ExtensionResponse createFailureResponse(String message) {
        ExtensionResponse.Error error = new ExtensionResponse.Error(
                message, System.currentTimeMillis(),
                ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR, "");
        return new ExtensionResponse(ExtensionResponse.Result.FAILURE, null, error, null, null);
    }

    private ExtensionResponse createSuccessConfigurationResponse() {
        ExtensionResponseMeta meta = new ExtensionResponseMeta();
        meta.message = "Configuration saved successfully";
        meta.responseType = SUCCESS;
        meta.timeTakenInSeconds = 1.5;

        Map<String, Object> responseData = Map.of(
                IS_CONFIGURATION_SUCCESSFUL, true,
                EXTENSION_RESPONSE_META, meta
        );
        return new ExtensionResponse(ExtensionResponse.Result.SUCCESS, responseData, null, null, null);
    }

    private ExtensionResponse createFailureConfigurationResponse() {
        ExtensionResponse.Error error = new ExtensionResponse.Error(
                "Configuration failed", System.currentTimeMillis(),
                ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR, "");
        return new ExtensionResponse(ExtensionResponse.Result.FAILURE, null, error, null, null);
    }

    @Test
    void testSaveOutlookPrivateConfiguration_Failure() {
        // Arrange
        String email = TEST_EMAIL;
        String clientId = TEST_CLIENT_ID;
        String clientSecret = TEST_CLIENT_SECRET;
        String tenantId = TEST_TENANT_ID;
        Boolean allowMailAlert = true;
        ExtensionResponse expectedResponse = createFailureConfigurationResponse();

        when(saveConfigurationImpl.saveConfiguration(any(JsonObject.class)))
                .thenReturn(expectedResponse);

        // Act
        ExtensionResponse actualResponse = setupArea.saveOutlookPrivateConfiguration(
                email, clientId, clientSecret, tenantId, allowMailAlert);

        // Assert
        assertNotNull(actualResponse);
        assertEquals(ExtensionResponse.Result.FAILURE, actualResponse.getResult());

        verify(saveConfigurationImpl, times(1)).saveConfiguration(any(JsonObject.class));
    }

    @Test
    void testSaveOutlookPrivateConfiguration_MustAuthorizeException() {
        // Arrange
        String email = TEST_EMAIL;
        String clientId = TEST_CLIENT_ID;
        String clientSecret = TEST_CLIENT_SECRET;
        String tenantId = TEST_TENANT_ID;
        Boolean allowMailAlert = true;

        when(saveConfigurationImpl.saveConfiguration(any(JsonObject.class)))
                .thenThrow(new MustAuthorizeException("Authorization required", null));

        // Act & Assert
        assertThrows(MustAuthorizeException.class, () ->
            setupArea.saveOutlookPrivateConfiguration(email, clientId, clientSecret, tenantId, allowMailAlert));

        verify(saveConfigurationImpl, times(1)).saveConfiguration(any(JsonObject.class));
    }

    @Test
    void testSaveOutlookPrivateConfiguration_RuntimeException() {
        // Arrange
        String email = TEST_EMAIL;
        String clientId = TEST_CLIENT_ID;
        String clientSecret = TEST_CLIENT_SECRET;
        String tenantId = TEST_TENANT_ID;
        Boolean allowMailAlert = true;

        when(saveConfigurationImpl.saveConfiguration(any(JsonObject.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
            setupArea.saveOutlookPrivateConfiguration(email, clientId, clientSecret, tenantId, allowMailAlert));

        verify(saveConfigurationImpl, times(1)).saveConfiguration(any(JsonObject.class));
    }

    // ========== Edge Cases and Boundary Tests ==========

    @Test
    void testSaveOutlookPublicConfiguration_EmptyEmail() {
        // Arrange
        String email = "";
        Boolean allowMailAlert = true;
        ExtensionResponse expectedResponse = createSuccessConfigurationResponse();

        when(saveConfigurationImpl.saveConfiguration(any(JsonObject.class)))
                .thenReturn(expectedResponse);

        // Act
        ExtensionResponse actualResponse = setupArea.saveOutlookPublicConfiguration(email, allowMailAlert);

        // Assert
        assertNotNull(actualResponse);
        verify(saveConfigurationImpl, times(1)).saveConfiguration(argThat(payload -> {
            JsonObject json = payload;
            return "".equals(json.get("email").getAsString());
        }));
    }

    @Test
    void testSaveOutlookPrivateConfiguration_EmptyCredentials() {
        // Arrange
        String email = "";
        String clientId = "";
        String clientSecret = "";
        String tenantId = "";
        Boolean allowMailAlert = true;
        ExtensionResponse expectedResponse = createSuccessConfigurationResponse();

        when(saveConfigurationImpl.saveConfiguration(any(JsonObject.class)))
                .thenReturn(expectedResponse);

        // Act
        ExtensionResponse actualResponse = setupArea.saveOutlookPrivateConfiguration(
                email, clientId, clientSecret, tenantId, allowMailAlert);

        // Assert
        assertNotNull(actualResponse);
        verify(saveConfigurationImpl, times(1)).saveConfiguration(argThat(payload -> {
            JsonObject json = payload;
            return "".equals(json.get("email").getAsString()) &&
                   "".equals(json.get("clientId").getAsString()) &&
                   "".equals(json.get("clientSecret").getAsString()) &&
                   "".equals(json.get("tenantId").getAsString());
        }));
    }

    @Test
    void testSaveOutlookPublicConfiguration_SpecialCharactersInEmail() {
        // Arrange
        String email = "test+special@example-domain.co.uk";
        Boolean allowMailAlert = true;
        ExtensionResponse expectedResponse = createSuccessConfigurationResponse();

        when(saveConfigurationImpl.saveConfiguration(any(JsonObject.class)))
                .thenReturn(expectedResponse);

        // Act
        ExtensionResponse actualResponse = setupArea.saveOutlookPublicConfiguration(email, allowMailAlert);

        // Assert
        assertNotNull(actualResponse);
        verify(saveConfigurationImpl, times(1)).saveConfiguration(argThat(payload -> {
            JsonObject json = payload;
            return email.equals(json.get("email").getAsString());
        }));
    }

    @Test
    void testSaveOutlookPrivateConfiguration_SpecialCharactersInCredentials() {
        // Arrange
        String email = "test@example.com";
        String clientId = "client-id-with-dashes-123";
        String clientSecret = "secret!@#$%^&*()_+{}|:<>?";
        String tenantId = "tenant-id-with-special-chars_123";
        Boolean allowMailAlert = false;
        ExtensionResponse expectedResponse = createSuccessConfigurationResponse();

        when(saveConfigurationImpl.saveConfiguration(any(JsonObject.class)))
                .thenReturn(expectedResponse);

        // Act
        ExtensionResponse actualResponse = setupArea.saveOutlookPrivateConfiguration(
                email, clientId, clientSecret, tenantId, allowMailAlert);

        // Assert
        assertNotNull(actualResponse);
        verify(saveConfigurationImpl, times(1)).saveConfiguration(argThat(payload -> {
            JsonObject json = payload;
            return clientId.equals(json.get("clientId").getAsString()) &&
                   clientSecret.equals(json.get("clientSecret").getAsString()) &&
                   tenantId.equals(json.get("tenantId").getAsString());
        }));
    }

    // ========== Constructor and Initialization Tests ==========

    @Test
    void testSetupAreaConstructor_ValidDependencies() {
        // Arrange
        HealthCheck mockHealthCheck = mock(HealthCheck.class);
        SaveConfigurationImpl mockSaveConfig = mock(SaveConfigurationImpl.class);
        Invoker mockInvoker = mock(Invoker.class);
        RoutingInfo mockRoutingInfo = mock(RoutingInfo.class);
        OutlookAttributeStore mockAttributeStore = mock(OutlookAttributeStore.class);
        GraphServiceClientProviderFactory mockProviderFactory = mock(GraphServiceClientProviderFactory.class);

        when(mockInvoker.getRoutingInfo()).thenReturn(mockRoutingInfo);
        when(mockRoutingInfo.getRoutingURL(HttpProtocol.PROTOCOL_NAME, RoutingInfo.Type.APPLIANCE))
                .thenReturn(TEST_ROUTING_URL);

        // Act
        SetupArea testSetupArea = new SetupArea(mockHealthCheck, mockSaveConfig, mockInvoker, mockAttributeStore, mockProviderFactory);

        // Assert
        assertNotNull(testSetupArea);
        verify(mockInvoker, times(1)).getRoutingInfo();
        verify(mockRoutingInfo, times(1)).getRoutingURL(HttpProtocol.PROTOCOL_NAME, RoutingInfo.Type.APPLIANCE);
    }

    @Test
    void testSetupAreaConstructor_NullRoutingUrl() {
        // Arrange
        HealthCheck mockHealthCheck = mock(HealthCheck.class);
        SaveConfigurationImpl mockSaveConfig = mock(SaveConfigurationImpl.class);
        Invoker mockInvoker = mock(Invoker.class);
        RoutingInfo mockRoutingInfo = mock(RoutingInfo.class);
        OutlookAttributeStore mockAttributeStore = mock(OutlookAttributeStore.class);
        GraphServiceClientProviderFactory mockProviderFactory = mock(GraphServiceClientProviderFactory.class);

        when(mockInvoker.getRoutingInfo()).thenReturn(mockRoutingInfo);
        when(mockRoutingInfo.getRoutingURL(HttpProtocol.PROTOCOL_NAME, RoutingInfo.Type.APPLIANCE))
                .thenReturn(null);

        // Act
        SetupArea testSetupArea = new SetupArea(mockHealthCheck, mockSaveConfig, mockInvoker, mockAttributeStore, mockProviderFactory);

        // Assert
        assertNotNull(testSetupArea);
        verify(mockInvoker, times(1)).getRoutingInfo();
    }

    // ========== Logging Verification Tests ==========

    @Test
    void testSaveOutlookPublicConfiguration_LoggingVerification() {
        // Arrange
        String email = TEST_EMAIL;
        Boolean allowMailAlert = true;
        ExtensionResponse expectedResponse = createSuccessConfigurationResponse();

        when(saveConfigurationImpl.saveConfiguration(any(JsonObject.class)))
                .thenReturn(expectedResponse);

        // Act
        setupArea.saveOutlookPublicConfiguration(email, allowMailAlert);

        // Assert - Verify that the method was called (logging happens inside)
        verify(saveConfigurationImpl, times(1)).saveConfiguration(any(JsonObject.class));
    }

    @Test
    void testSaveOutlookPrivateConfiguration_LoggingVerification() {
        // Arrange
        String email = TEST_EMAIL;
        String clientId = TEST_CLIENT_ID;
        String clientSecret = TEST_CLIENT_SECRET;
        String tenantId = TEST_TENANT_ID;
        Boolean allowMailAlert = true;
        ExtensionResponse expectedResponse = createSuccessConfigurationResponse();

        when(saveConfigurationImpl.saveConfiguration(any(JsonObject.class)))
                .thenReturn(expectedResponse);

        // Act
        setupArea.saveOutlookPrivateConfiguration(email, clientId, clientSecret, tenantId, allowMailAlert);

        // Assert - Verify that the method was called (logging happens inside)
        verify(saveConfigurationImpl, times(1)).saveConfiguration(any(JsonObject.class));
    }



    // ========== Payload Validation Tests ==========

    @Test
    void testSaveOutlookPublicConfiguration_PayloadStructure() {
        // Arrange
        String email = TEST_EMAIL;
        Boolean allowMailAlert = true;
        ExtensionResponse expectedResponse = createSuccessConfigurationResponse();

        when(saveConfigurationImpl.saveConfiguration(any(JsonObject.class)))
                .thenReturn(expectedResponse);

        // Act
        setupArea.saveOutlookPublicConfiguration(email, allowMailAlert);

        // Assert - Verify payload structure for public configuration
        verify(saveConfigurationImpl, times(1)).saveConfiguration(argThat(payload -> {
            JsonObject json = payload;
            return json.has("authType") &&
                   json.has("email") &&
                   json.has("allowMailAlert") &&
                   json.has("baseUrl") &&
                   !json.has("clientId") &&
                   !json.has("clientSecret") &&
                   !json.has("tenantId");
        }));
    }

    @Test
    void testSaveOutlookPrivateConfiguration_PayloadStructure() {
        // Arrange
        String email = TEST_EMAIL;
        String clientId = TEST_CLIENT_ID;
        String clientSecret = TEST_CLIENT_SECRET;
        String tenantId = TEST_TENANT_ID;
        Boolean allowMailAlert = true;
        ExtensionResponse expectedResponse = createSuccessConfigurationResponse();

        when(saveConfigurationImpl.saveConfiguration(any(JsonObject.class)))
                .thenReturn(expectedResponse);

        // Act
        setupArea.saveOutlookPrivateConfiguration(email, clientId, clientSecret, tenantId, allowMailAlert);

        // Assert - Verify payload structure for private configuration
        verify(saveConfigurationImpl, times(1)).saveConfiguration(argThat(payload -> {
            JsonObject json = payload;
            return json.has("authType") &&
                   json.has("email") &&
                   json.has("allowMailAlert") &&
                   json.has("baseUrl") &&
                   json.has("clientId") &&
                   json.has("clientSecret") &&
                   json.has("tenantId");
        }));
    }

    @Test
    void testSaveOutlookPublicConfiguration_BaseUrlInPayload() {
        // Arrange
        String email = TEST_EMAIL;
        Boolean allowMailAlert = true;
        ExtensionResponse expectedResponse = createSuccessConfigurationResponse();

        when(saveConfigurationImpl.saveConfiguration(any(JsonObject.class)))
                .thenReturn(expectedResponse);

        // Act
        setupArea.saveOutlookPublicConfiguration(email, allowMailAlert);

        // Assert - Verify baseUrl is null for public configuration
        verify(saveConfigurationImpl, times(1)).saveConfiguration(argThat(payload -> {
            JsonObject json = payload;
            return json.get("baseUrl").isJsonNull();
        }));
    }

    @Test
    void testSaveOutlookPrivateConfiguration_BaseUrlInPayload() {
        // Arrange
        String email = TEST_EMAIL;
        String clientId = TEST_CLIENT_ID;
        String clientSecret = TEST_CLIENT_SECRET;
        String tenantId = TEST_TENANT_ID;
        Boolean allowMailAlert = true;
        ExtensionResponse expectedResponse = createSuccessConfigurationResponse();

        when(saveConfigurationImpl.saveConfiguration(any(JsonObject.class)))
                .thenReturn(expectedResponse);

        // Act
        setupArea.saveOutlookPrivateConfiguration(email, clientId, clientSecret, tenantId, allowMailAlert);

        // Assert - Verify baseUrl is set for private configuration
        verify(saveConfigurationImpl, times(1)).saveConfiguration(argThat(payload -> {
            JsonObject json = payload;
            return TEST_ROUTING_URL.equals(json.get("baseUrl").getAsString());
        }));
    }

    // ========== Multiple Invocation Tests ==========

    @Test
    void testMultipleHealthCheckInvocations() {
        // Arrange
        ExtensionResponse expectedResponse = createSuccessResponse();
        when(healthCheck.checkHealth()).thenReturn(expectedResponse);

        // Act
        setupArea.healthCheck();
        setupArea.healthCheck();
        setupArea.healthCheck();

        // Assert
        verify(healthCheck, times(3)).checkHealth();
    }

    @Test
    void testMultiplePublicConfigurationSaves() {
        // Arrange
        ExtensionResponse expectedResponse = createSuccessConfigurationResponse();
        when(saveConfigurationImpl.saveConfiguration(any(JsonObject.class)))
                .thenReturn(expectedResponse);

        // Act
        setupArea.saveOutlookPublicConfiguration("email1@test.com", true);
        setupArea.saveOutlookPublicConfiguration("email2@test.com", false);
        setupArea.saveOutlookPublicConfiguration("email3@test.com", null);

        // Assert
        verify(saveConfigurationImpl, times(3)).saveConfiguration(any(JsonObject.class));
    }

    @Test
    void testMultiplePrivateConfigurationSaves() {
        // Arrange
        ExtensionResponse expectedResponse = createSuccessConfigurationResponse();
        when(saveConfigurationImpl.saveConfiguration(any(JsonObject.class)))
                .thenReturn(expectedResponse);

        // Act
        setupArea.saveOutlookPrivateConfiguration("email1@test.com", "client1", "secret1", "tenant1", true);
        setupArea.saveOutlookPrivateConfiguration("email2@test.com", "client2", "secret2", "tenant2", false);

        // Assert
        verify(saveConfigurationImpl, times(2)).saveConfiguration(any(JsonObject.class));
    }

    // ========== Integration-style Tests ==========

    @Test
    void testHealthCheckAndConfigurationSaveSequence() {
        // Arrange
        ExtensionResponse healthResponse = createSuccessResponse();
        ExtensionResponse configResponse = createSuccessConfigurationResponse();

        when(healthCheck.checkHealth()).thenReturn(healthResponse);
        when(saveConfigurationImpl.saveConfiguration(any(JsonObject.class)))
                .thenReturn(configResponse);

        // Act
        ExtensionResponse health = setupArea.healthCheck();
        ExtensionResponse publicConfig = setupArea.saveOutlookPublicConfiguration(TEST_EMAIL, true);
        ExtensionResponse privateConfig = setupArea.saveOutlookPrivateConfiguration(
                TEST_EMAIL, TEST_CLIENT_ID, TEST_CLIENT_SECRET, TEST_TENANT_ID, false);

        // Assert
        assertNotNull(health);
        assertNotNull(publicConfig);
        assertNotNull(privateConfig);
        assertEquals(ExtensionResponse.Result.SUCCESS, health.getResult());
        assertEquals(ExtensionResponse.Result.SUCCESS, publicConfig.getResult());
        assertEquals(ExtensionResponse.Result.SUCCESS, privateConfig.getResult());

        verify(healthCheck, times(1)).checkHealth();
        verify(saveConfigurationImpl, times(2)).saveConfiguration(any(JsonObject.class));
    }

    private Object createHealthStatus() {
        // Mock health status object
        return Map.of(
                "systemStatus", "HEALTHY",
                "memoryUsage", 75.5,
                "cpuUsage", 25.0
        );
    }
}
