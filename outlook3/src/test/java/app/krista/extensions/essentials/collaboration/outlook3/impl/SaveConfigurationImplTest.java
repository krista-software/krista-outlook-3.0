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

import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extension.executor.ExtensionResponse;
import app.krista.extension.executor.Invoker;
import app.krista.extension.request.RoutingInfo;
import app.krista.extension.request.protos.http.HttpProtocol;
import app.krista.extensions.essentials.collaboration.outlook3.OutlookAttributes;
import app.krista.extensions.essentials.collaboration.outlook3.impl.connectors.GraphServiceClientProvider;
import app.krista.extensions.essentials.collaboration.outlook3.impl.connectors.GraphServiceClientProviderFactory;
import app.krista.extensions.essentials.collaboration.outlook3.impl.stores.OutlookAttributeStore;
import app.krista.ksdk.context.AuthorizationContext;
import com.google.gson.JsonObject;
import com.microsoft.graph.requests.GraphServiceClient;
import com.microsoft.graph.requests.MailFolderCollectionRequest;
import com.microsoft.graph.requests.MailFolderCollectionRequestBuilder;
import com.microsoft.graph.requests.UserRequestBuilder;
import okhttp3.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockSettings;

import static app.krista.extensions.essentials.collaboration.outlook3.impl.util.Constants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for the SaveConfigurationImpl implementation.
 * <p>
 * IMPORTANT: The credentials used in this test class (Client ID, Client Secret, Tenant ID, Email)
 * are for testing purposes only. These may be valid or invalid depending on your environment.
 * To run these tests successfully, please replace them with your own valid credentials
 * in the setup() method where OutlookAttributes is instantiated.
 */
public class SaveConfigurationImplTest {

    // Test constants
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_CLIENT_ID = "test-client-id";
    private static final String TEST_CLIENT_SECRET = "test-client-secret";
    private static final String TEST_TENANT_ID = "test-tenant-id";
    private static final String TEST_INVOKER_ID = "test-invoker-id";
    private static final String TEST_ACCOUNT_ID = "test-account-id";
    private static final String TEST_BASE_URL = "https://test.example.com";

    // Use instance variables for mocks that are initialized in setup()
    private GraphServiceClientProviderFactory providerFactory;
    private OutlookAttributeStore outlookAttributeStore;
    private SubscriptionCleanupService subscriptionCleanupService;
    private Invoker invoker;
    private TestConnectionServiceImpl testConnectionServiceImpl;
    private AuthorizationContext authorizationContext;
    private RoutingInfo routingInfo;
    private GraphServiceClientProvider graphProvider;
    private GraphServiceClient<Request> graphServiceClient;
    private UserRequestBuilder userRequestBuilder;
    private MailFolderCollectionRequestBuilder mailFolderRequestBuilder;
    private MailFolderCollectionRequest mailFolderRequest;

    private SaveConfigurationImpl saveConfiguration;

    @BeforeEach
    public void setup() {
        // Create mocks - use stubOnly() for mocks that don't need verification
        MockSettings stubOnlySettings = withSettings().stubOnly();
        MockSettings regularSettings = withSettings();

        providerFactory = mock(GraphServiceClientProviderFactory.class, stubOnlySettings);
        outlookAttributeStore = mock(OutlookAttributeStore.class, regularSettings); // Needs verification
        subscriptionCleanupService = mock(SubscriptionCleanupService.class, regularSettings); // Needs verification
        invoker = mock(Invoker.class, stubOnlySettings);
        testConnectionServiceImpl = mock(TestConnectionServiceImpl.class, stubOnlySettings);
        authorizationContext = mock(AuthorizationContext.class, stubOnlySettings);
        routingInfo = mock(RoutingInfo.class, stubOnlySettings);
        graphProvider = mock(GraphServiceClientProvider.class, stubOnlySettings);
        graphServiceClient = mock(GraphServiceClient.class, stubOnlySettings);
        userRequestBuilder = mock(UserRequestBuilder.class, stubOnlySettings);
        mailFolderRequestBuilder = mock(MailFolderCollectionRequestBuilder.class, stubOnlySettings);
        mailFolderRequest = mock(MailFolderCollectionRequest.class, stubOnlySettings);

        // Setup the invoker mock
        when(invoker.getRoutingInfo()).thenReturn(routingInfo);
        when(routingInfo.getRoutingURL(HttpProtocol.PROTOCOL_NAME, RoutingInfo.Type.APPLIANCE))
                .thenReturn(TEST_BASE_URL);
        when(invoker.getInvokerId()).thenReturn(TEST_INVOKER_ID);

        // Setup authorization context mock - create a mock account object
        // Create a mock for the Account interface from ksdk.accounts
        app.krista.ksdk.accounts.Account mockAccount = mock(app.krista.ksdk.accounts.Account.class, stubOnlySettings);
        when(mockAccount.getAccountId()).thenReturn(TEST_ACCOUNT_ID);
        when(authorizationContext.getAuthorizedAccount()).thenReturn(mockAccount);

        // Initialize the object under test
        saveConfiguration = new SaveConfigurationImpl(
                providerFactory,
                outlookAttributeStore,
                subscriptionCleanupService,
                invoker,
                testConnectionServiceImpl,
                authorizationContext
        );
    }

    @Test
    public void testSaveConfiguration_PublicSuccess() {
        // Create public configuration payload
        JsonObject publicPayload = createTestPublicAuthPayload();

        // Setup successful test connection - return proper AuthenticationResponse JSON
        String successResponse = "{\"isSuccess\":true,\"errorMessage\":null,\"url\":null}";
        when(testConnectionServiceImpl.testConnection(any(OutlookAttributes.class)))
                .thenReturn(successResponse);
        when(outlookAttributeStore.save(any(OutlookAttributes.class), eq(TEST_INVOKER_ID)))
                .thenReturn(true);
        setupGraphServiceClientMocks();

        ExtensionResponse response = saveConfiguration.saveConfiguration(publicPayload);

        assertNotNull(response);
        assertEquals(ExtensionResponse.Result.SUCCESS, response.getResult());
        assertTrue((Boolean) response.getResponseValue().get(IS_CONFIGURATION_SUCCESSFUL));
    }

    @Test
    public void testSaveConfiguration_PrivateInvalidCredentials() {
        // Create private configuration payload with invalid test credentials
        JsonObject privatePayload = createTestAuthPayload();

        // The OutlookCredentialValidator will throw IllegalArgumentException for invalid credentials
        // This is expected behavior when using test credentials
        assertThrows(IllegalArgumentException.class, () -> {
            saveConfiguration.saveConfiguration(privatePayload);
        });
    }

    @Test
    public void testSaveConfiguration_TestConnectionFails_ThrowsMustAuthorizeException() {
        // Use public configuration to avoid credential validation
        JsonObject authPayload = createTestPublicAuthPayload();
        String failureResponse = "{\"isSuccess\":false,\"errorMessage\":\"Test connection failed\",\"url\":null}";
        when(testConnectionServiceImpl.testConnection(any(OutlookAttributes.class)))
                .thenReturn(failureResponse);
        when(providerFactory.createAttributes(any(OutlookAttributes.class)))
                .thenReturn("test-auth-context-id");

        assertThrows(MustAuthorizeException.class, () -> {
            saveConfiguration.saveConfiguration(authPayload);
        });
    }

    @Test
    public void testSaveConfiguration_SaveCredentialsFails() {
        // Use public configuration to avoid credential validation
        JsonObject authPayload = createTestPublicAuthPayload();
        String successResponse = "{\"isSuccess\":true,\"errorMessage\":null,\"url\":null}";
        when(testConnectionServiceImpl.testConnection(any(OutlookAttributes.class)))
                .thenReturn(successResponse);
        when(outlookAttributeStore.save(any(OutlookAttributes.class), eq(TEST_INVOKER_ID)))
                .thenReturn(false);
        setupGraphServiceClientMocks();

        ExtensionResponse response = saveConfiguration.saveConfiguration(authPayload);

        assertNotNull(response);
        assertEquals(ExtensionResponse.Result.FAILURE, response.getResult());
    }

    @Test
    public void testSaveCredentials_Success() {
        JsonObject authPayload = createTestAuthPayload();
        when(outlookAttributeStore.save(any(OutlookAttributes.class), eq(TEST_INVOKER_ID)))
                .thenReturn(true);
        setupGraphServiceClientMocks();

        String result = saveConfiguration.saveCredentials(authPayload);

        assertNotNull(result);
        assertTrue(result.contains("true"));
        verify(outlookAttributeStore).remove(anyString());
    }

    @Test
    public void testSaveCredentials_GraphServiceException() {
        JsonObject authPayload = createTestAuthPayload();
        when(providerFactory.createAttributes(any(OutlookAttributes.class)))
                .thenReturn("test-auth-context-id");
        when(providerFactory.create(anyString())).thenReturn(graphProvider);
        when(graphProvider.getGraphServiceClientForAdmin()).thenReturn(graphServiceClient);
        when(graphServiceClient.users(anyString())).thenReturn(userRequestBuilder);
        when(userRequestBuilder.mailFolders()).thenReturn(mailFolderRequestBuilder);
        when(mailFolderRequestBuilder.buildRequest()).thenThrow(new RuntimeException("Graph API error"));

        String result = saveConfiguration.saveCredentials(authPayload);

        assertNotNull(result);
        assertTrue(result.contains("false"));
        verify(outlookAttributeStore).remove(anyString());
    }

    @Test
    public void testSaveCredentials_NullEmail() {
        JsonObject authPayload = createTestAuthPayload();
        authPayload.addProperty(EMAIL, (String) null);

        // This should throw UnsupportedOperationException because JsonNull.getAsString() is not supported
        assertThrows(UnsupportedOperationException.class, () -> {
            saveConfiguration.saveCredentials(authPayload);
        });
    }

    @Test
    public void testSaveCredentials_EmptyEmail() {
        JsonObject authPayload = createTestAuthPayload();
        authPayload.addProperty(EMAIL, "");

        String result = saveConfiguration.saveCredentials(authPayload);

        assertNotNull(result);
        assertTrue(result.contains("false"));
    }

    /**
     * Test that when email changes, the subscription cleanup service is called.
     * This is a critical test for the subscription cleanup bug fix.
     */
    @Test
    public void testSaveCredentials_EmailChanged_CallsSubscriptionCleanupService() {
        // Setup: Create new credentials with different email
        String newEmail = "new@example.com";
        JsonObject newAuthPayload = createTestAuthPayload();
        newAuthPayload.addProperty(EMAIL, newEmail);

        when(outlookAttributeStore.save(any(OutlookAttributes.class), eq(TEST_INVOKER_ID)))
                .thenReturn(true);
        setupGraphServiceClientMocks();

        // Execute
        String result = saveConfiguration.saveCredentials(newAuthPayload);

        // Verify: New credentials saved successfully
        assertNotNull(result);
        assertTrue(result.contains("true"));

        // Verify: Subscription cleanup service was called
        verify(subscriptionCleanupService).handleCredentialChange(
                any(OutlookAttributes.class),
                eq(TEST_BASE_URL),
                eq(TEST_INVOKER_ID)
        );

        // Verify: New credentials were saved
        verify(outlookAttributeStore).save(any(OutlookAttributes.class), eq(TEST_INVOKER_ID));
    }

    /**
     * Test that subscription cleanup service is always called regardless of email change.
     * The service itself handles the logic of whether to delete old subscriptions.
     */
    @Test
    public void testSaveCredentials_AlwaysCallsSubscriptionCleanupService() {
        // Mock: Setup for credentials with same email
        JsonObject authPayload = createTestAuthPayload();
        when(outlookAttributeStore.save(any(OutlookAttributes.class), eq(TEST_INVOKER_ID)))
                .thenReturn(true);
        setupGraphServiceClientMocks();

        // Execute
        String result = saveConfiguration.saveCredentials(authPayload);

        // Verify: New credentials saved successfully
        assertNotNull(result);
        assertTrue(result.contains("true"));

        // Verify: Subscription cleanup service was called
        verify(subscriptionCleanupService).handleCredentialChange(
                any(OutlookAttributes.class),
                eq(TEST_BASE_URL),
                eq(TEST_INVOKER_ID)
        );

        // Verify: New credentials were saved
        verify(outlookAttributeStore).save(any(OutlookAttributes.class), eq(TEST_INVOKER_ID));
    }

    /**
     * Test that subscription cleanup service is called even for first time setup.
     * The service handles the logic of detecting first-time setup.
     */
    @Test
    public void testSaveCredentials_FirstTimeSetup_CallsSubscriptionCleanupService() {
        // Mock: Setup for new credentials
        JsonObject authPayload = createTestAuthPayload();
        when(outlookAttributeStore.save(any(OutlookAttributes.class), eq(TEST_INVOKER_ID)))
                .thenReturn(true);
        setupGraphServiceClientMocks();

        // Execute
        String result = saveConfiguration.saveCredentials(authPayload);

        // Verify: New credentials saved successfully
        assertNotNull(result);
        assertTrue(result.contains("true"));

        // Verify: Subscription cleanup service was called
        verify(subscriptionCleanupService).handleCredentialChange(
                any(OutlookAttributes.class),
                eq(TEST_BASE_URL),
                eq(TEST_INVOKER_ID)
        );

        // Verify: New credentials were saved
        verify(outlookAttributeStore).save(any(OutlookAttributes.class), eq(TEST_INVOKER_ID));
    }

    private JsonObject createTestAuthPayload() {
        JsonObject payload = new JsonObject();
        payload.addProperty(AUTH_TYPE, PRIVATE);
        payload.addProperty(EMAIL, TEST_EMAIL);
        payload.addProperty(CLIENT_ID, TEST_CLIENT_ID);
        payload.addProperty(CLIENT_SECRET, TEST_CLIENT_SECRET);
        payload.addProperty(TENANT_ID, TEST_TENANT_ID);
        payload.addProperty(ALLOW_MAIL_ALERT, true);
        return payload;
    }

    private JsonObject createTestPublicAuthPayload() {
        JsonObject payload = new JsonObject();
        payload.addProperty(AUTH_TYPE, PUBLIC);
        payload.addProperty(EMAIL, TEST_EMAIL);
        payload.addProperty(ALLOW_MAIL_ALERT, true);
        return payload;
    }

    private void setupGraphServiceClientMocks() {
        when(providerFactory.createAttributes(any(OutlookAttributes.class)))
                .thenReturn("test-auth-context-id");
        when(providerFactory.create(anyString())).thenReturn(graphProvider);
        when(graphProvider.getGraphServiceClientForAdmin()).thenReturn(graphServiceClient);
        when(graphServiceClient.users(anyString())).thenReturn(userRequestBuilder);
        when(userRequestBuilder.mailFolders()).thenReturn(mailFolderRequestBuilder);
        when(mailFolderRequestBuilder.buildRequest()).thenReturn(mailFolderRequest);
    }
}