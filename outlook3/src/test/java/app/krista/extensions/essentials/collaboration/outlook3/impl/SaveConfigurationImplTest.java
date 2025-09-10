/*
package app.krista.extensions.essentials.collaboration.outlook3.impl;

import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extension.executor.ExtensionResponse;
import app.krista.extension.executor.Invoker;
import app.krista.extension.request.RoutingInfo;
import app.krista.extension.request.protos.http.HttpProtocol;
import app.krista.extensions.essentials.collaboration.outlook3.OutlookAttributes;
import app.krista.extensions.essentials.collaboration.outlook3.impl.connectors.GraphServiceClientProviderFactory;
import app.krista.extensions.essentials.collaboration.outlook3.impl.stores.OutlookAttributeStore;
import app.krista.extensions.essentials.collaboration.outlook3.impl.util.SaveConfigurationImpl;
import app.krista.ksdk.context.AuthorizationContext;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static app.krista.extensions.essentials.collaboration.outlook3.impl.util.Constants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

*/
/**
 * Tests for the SaveConfigurationImpl implementation.
 * <p>
 * IMPORTANT: The credentials used in this test class are for testing purposes only.
 * These may be valid or invalid depending on your environment.
 *//*

public class SaveConfigurationImplTest {

    private static final String BASE_URL = "https://extension.solution.eng.krista.app/extension/api/xAcwhzRYmXuSToCNf4ropQ_e_e";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_CLIENT_ID = "test-client-id";
    private static final String TEST_CLIENT_SECRET = "test-client-secret";
    private static final String TEST_TENANT_ID = "test-tenant-id";
    private static final String TEST_INVOKER_ID = "test-invoker-id";
    private static final String TEST_ACCOUNT_ID = "test-account-id";

    // Instance variables for mocks
    private GraphServiceClientProviderFactory providerFactory;
    private OutlookAttributeStore outlookAttributeStore;
    private Invoker invoker;
    private RoutingInfo routingInfo;
    private TestConnectionServiceImpl testConnectionService;
    private AuthorizationContext authorizationContext;
    private app.krista.ksdk.accounts.Account account;

    private SaveConfigurationImpl saveConfigurationImpl;

    @BeforeEach
    public void setup() {
        // Create mocks - remove stubOnly() to allow verification
        providerFactory = mock(GraphServiceClientProviderFactory.class);
        outlookAttributeStore = mock(OutlookAttributeStore.class);
        invoker = mock(Invoker.class);
        routingInfo = mock(RoutingInfo.class);
        testConnectionService = mock(TestConnectionServiceImpl.class);
        authorizationContext = mock(AuthorizationContext.class);
        account = mock(app.krista.ksdk.accounts.Account.class);

        // Set up the routing info mock
        when(invoker.getRoutingInfo()).thenReturn(routingInfo);
        when(routingInfo.getRoutingURL(HttpProtocol.PROTOCOL_NAME, RoutingInfo.Type.APPLIANCE))
                .thenReturn(BASE_URL);
        when(invoker.getInvokerId()).thenReturn(TEST_INVOKER_ID);

        // Setup authorization context
        when(authorizationContext.getAuthorizedAccount()).thenReturn(account);
        when(account.getAccountId()).thenReturn(TEST_ACCOUNT_ID);

        // Initialize the object under test
        saveConfigurationImpl = new SaveConfigurationImpl(
                providerFactory,
                outlookAttributeStore,
                invoker,
                testConnectionService,
                authorizationContext
        );
    }

    @Test
    public void testOutlookPublicConfigurationSuccess() {
        // Setup successful test connection
        String successResponse = "{\"isSuccess\": true, \"message\": \"Connection successful\"}";
        when(testConnectionService.testConnection(any(OutlookAttributes.class))).thenReturn(successResponse);
        when(outlookAttributeStore.save(any(OutlookAttributes.class), eq(TEST_INVOKER_ID))).thenReturn(true);

        ExtensionResponse response = saveConfigurationImpl.outlookPublicConfiguration(TEST_EMAIL, true);

        assertEquals(ExtensionResponse.Result.SUCCESS, response.getResult());
        assertTrue((Boolean) response.getResponseValue().get("Is Configuration Successful"));
    }

    @Test
    public void testOutlookPrivateConfigurationSuccess() {
        // Setup successful test connection
        String successResponse = "{\"isSuccess\": true, \"message\": \"Connection successful\"}";
        when(testConnectionService.testConnection(any(OutlookAttributes.class))).thenReturn(successResponse);
        when(outlookAttributeStore.save(any(OutlookAttributes.class), eq(TEST_INVOKER_ID))).thenReturn(true);

        ExtensionResponse response = saveConfigurationImpl.outlookPrivateConfiguration(
                TEST_EMAIL, TEST_CLIENT_ID, TEST_CLIENT_SECRET, TEST_TENANT_ID, true);

        assertEquals(ExtensionResponse.Result.SUCCESS, response.getResult());
        assertTrue((Boolean) response.getResponseValue().get("Is Configuration Successful"));
    }

    @Test
    public void testSaveConfigurationFailure() {
        // Setup successful test connection but failed save
        String successResponse = "{\"isSuccess\": true, \"message\": \"Connection successful\"}";
        when(testConnectionService.testConnection(any(OutlookAttributes.class))).thenReturn(successResponse);
        when(outlookAttributeStore.save(any(OutlookAttributes.class), eq(TEST_INVOKER_ID))).thenReturn(false);

        JsonObject payload = new JsonObject();
        payload.addProperty("authType", "Public");
        payload.addProperty("email", TEST_EMAIL);
        payload.addProperty("allowMailAlert", true);

        ExtensionResponse response = saveConfigurationImpl.saveConfiguration(payload);

        assertEquals(ExtensionResponse.Result.FAILURE, response.getResult());
        // Check that the error contains the expected message
        assertTrue(response.getError().toString().contains("Failed to Configure Outlook Attributes"));
    }

    @Test
    public void testSaveConfigurationThrowsMustAuthorizeException() {
        // Setup failed test connection
        String failureResponse = "{\"isSuccess\": false, \"message\": \"Authentication required\"}";
        when(testConnectionService.testConnection(any(OutlookAttributes.class))).thenReturn(failureResponse);
        when(providerFactory.createAttributes(any(OutlookAttributes.class))).thenReturn("test-auth-context-id");

        JsonObject payload = new JsonObject();
        payload.addProperty("authType", "Public");
        payload.addProperty("email", TEST_EMAIL);
        payload.addProperty("allowMailAlert", true);

        assertThrows(MustAuthorizeException.class, () -> {
            saveConfigurationImpl.saveConfiguration(payload);
        });
    }

    @Test
    public void testOutlookPublicConfigurationWithNullEmail() {
        // The method should throw an exception when null email is provided
        // because OutlookAttributes.create() cannot handle null email
        assertThrows(UnsupportedOperationException.class, () -> {
            saveConfigurationImpl.outlookPublicConfiguration(null, true);
        });
    }

    @Test
    public void testOutlookPrivateConfigurationWithEmptyCredentials() {
        // Setup failed test connection for empty credentials
        String failureResponse = "{\"isSuccess\": false, \"message\": \"Invalid credentials\"}";
        when(testConnectionService.testConnection(any(OutlookAttributes.class))).thenReturn(failureResponse);
        when(providerFactory.createAttributes(any(OutlookAttributes.class))).thenReturn("test-auth-context-id");

        assertThrows(MustAuthorizeException.class, () -> {
            saveConfigurationImpl.outlookPrivateConfiguration("", "", "", "", false);
        });
    }

    @Test
    public void testSaveConfigurationWithValidJsonPayload() {
        // Setup successful test connection
        String successResponse = "{\"isSuccess\": true, \"message\": \"Connection successful\"}";
        when(testConnectionService.testConnection(any(OutlookAttributes.class))).thenReturn(successResponse);
        when(outlookAttributeStore.save(any(OutlookAttributes.class), eq(TEST_INVOKER_ID))).thenReturn(true);

        JsonObject payload = new JsonObject();
        payload.addProperty(AUTH_TYPE, PRIVATE);
        payload.addProperty(EMAIL, TEST_EMAIL);
        payload.addProperty(CLIENT_ID, TEST_CLIENT_ID);
        payload.addProperty(CLIENT_SECRET, TEST_CLIENT_SECRET);
        payload.addProperty(TENANT_ID, TEST_TENANT_ID);
        payload.addProperty(ALLOW_MAIL_ALERT, false);


        ExtensionResponse response = saveConfigurationImpl.saveConfiguration(payload);

        assertEquals(ExtensionResponse.Result.SUCCESS, response.getResult());
        assertTrue((Boolean) response.getResponseValue().get(IS_CONFIGURATION_SUCCESSFUL));
        verify(outlookAttributeStore).save(any(OutlookAttributes.class), eq(TEST_INVOKER_ID));
    }
}*/
