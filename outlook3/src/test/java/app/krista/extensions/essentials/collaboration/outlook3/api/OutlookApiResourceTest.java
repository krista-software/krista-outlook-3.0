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
package app.krista.extensions.essentials.collaboration.outlook3.api;

import app.krista.extension.event.WaitForEventListener;
import app.krista.extension.executor.Invoker;
import app.krista.extension.request.RoutingInfo;
import app.krista.extension.request.protos.http.HttpProtocol;
import app.krista.extensions.essentials.collaboration.outlook3.OutlookAttributes;
import app.krista.extensions.essentials.collaboration.outlook3.impl.AccountImpl;
import app.krista.extensions.essentials.collaboration.outlook3.impl.MailSubscription;
import app.krista.extensions.essentials.collaboration.outlook3.impl.SaveConfigurationImpl;
import app.krista.extensions.essentials.collaboration.outlook3.impl.TestConnectionServiceImpl;
import app.krista.extensions.essentials.collaboration.outlook3.impl.connectors.GraphServiceClientProvider;
import app.krista.extensions.essentials.collaboration.outlook3.impl.connectors.GraphServiceClientProviderFactory;
import app.krista.extensions.essentials.collaboration.outlook3.impl.connectors.OAuthService;
import app.krista.extensions.essentials.collaboration.outlook3.impl.stores.OutlookAttributeStore;
import app.krista.extensions.essentials.collaboration.outlook3.impl.stores.RefreshTokenStore;
import app.krista.extensions.essentials.collaboration.outlook3.impl.util.Constants;
import app.krista.extensions.essentials.collaboration.outlook3.impl.util.EncryptionUtil;
import app.krista.extensions.essentials.collaboration.outlook3.service.Folder;
import app.krista.extensions.util.EventHandler;
import app.krista.ksdk.authentication.AuthorizationListener;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.model.base.FreeForm;
import com.github.scribejava.core.exceptions.OAuthException;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.microsoft.graph.http.GraphServiceException;
import com.microsoft.graph.models.EmailAddress;
import com.microsoft.graph.models.ItemBody;
import com.microsoft.graph.models.Message;
import com.microsoft.graph.models.Recipient;
import com.microsoft.graph.requests.MailFolderCollectionRequest;
import com.microsoft.graph.requests.MailFolderCollectionRequestBuilder;
import com.microsoft.graph.requests.UserRequestBuilder;
import org.junit.jupiter.api.*;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static app.krista.extensions.essentials.collaboration.outlook3.impl.util.Constants.*;
import static app.krista.extensions.essentials.collaboration.outlook3.impl.util.EncryptionUtil.KRISTA_PREFIX;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("OutlookApiResource - Full Coverage Tests")
class OutlookApiResourceTest {

    private static final String TEST_ROUTING_URL = "http://test-routing-url";
    private static final String TEST_INVOKER_ID = "test-invoker-id";
    private static final String TEST_CODE = "test-auth-code";
    private static final String TEST_REFRESH_TOKEN = "test-refresh-token";

    private OutlookAttributeStore outlookAttributeStore;
    private RefreshTokenStore refreshTokenStore;
    private GraphServiceClientProviderFactory providerFactory;
    private EventHandler eventHandler;
    private Invoker invoker;
    private AuthorizationContext context;
    private AuthorizationListener authorizationListener;
    private TestConnectionServiceImpl testConnectionService;
    private SaveConfigurationImpl saveConfigurationImpl;
    private OutlookApiResource resource;

    @BeforeEach
    void setUp() {
        outlookAttributeStore = mock(OutlookAttributeStore.class);
        refreshTokenStore = mock(RefreshTokenStore.class);
        providerFactory = mock(GraphServiceClientProviderFactory.class);
        eventHandler = mock(EventHandler.class);
        invoker = mock(Invoker.class);
        context = mock(AuthorizationContext.class);
        authorizationListener = mock(AuthorizationListener.class);
        testConnectionService = mock(TestConnectionServiceImpl.class);
        saveConfigurationImpl = mock(SaveConfigurationImpl.class);

        RoutingInfo routingInfo = mock(RoutingInfo.class);
        when(invoker.getRoutingInfo()).thenReturn(routingInfo);
        when(routingInfo.getRoutingURL(HttpProtocol.PROTOCOL_NAME, RoutingInfo.Type.APPLIANCE))
                .thenReturn(TEST_ROUTING_URL);
        when(invoker.getInvokerId()).thenReturn(TEST_INVOKER_ID);

        resource = new OutlookApiResource(outlookAttributeStore, refreshTokenStore, providerFactory,
                eventHandler, invoker, context, authorizationListener, testConnectionService, saveConfigurationImpl);

        clearTriggeredMailIds();
    }

    @AfterEach
    void tearDown() {
        clearTriggeredMailIds();
    }

    @SuppressWarnings("unchecked")
    private void clearTriggeredMailIds() {
        try {
            Field field = OutlookApiResource.class.getDeclaredField("triggeredMailIds");
            field.setAccessible(true);
            ((Set<String>) field.get(null)).clear();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ==================== Constructor ====================

    @Test
    @DisplayName("Constructor initializes all fields correctly")
    void testConstructor() {
        assertNotNull(resource);
    }

    // ==================== V2/V3 Auth Callbacks ====================

    @Test
    @DisplayName("V2 auth callback - null code returns authentication failed")
    void testV2Auth_NullCode() {
        String result = resource.getCallBackForV2Auth(null, "some-state");
        assertEquals("Authentication Failed. Please re-authorize.", result);
    }

    @Test
    @DisplayName("V2 auth callback - blank state throws BadRequestException")
    void testV2Auth_BlankState() {
        assertThrows(BadRequestException.class, () ->
                resource.getCallBackForV2Auth(TEST_CODE, "#extra"));
    }

    @Test
    @DisplayName("V2 auth callback - success with authenticated context")
    void testV2Auth_SuccessAuthenticated() {
        try (MockedStatic<AuthHelper> authHelperMock = mockStatic(AuthHelper.class);
             MockedConstruction<OAuthService> ignored = mockConstruction(OAuthService.class, (oauthMock, ctx) -> {
                 OAuth20Service svc = Mockito.mock(OAuth20Service.class);
                 OAuth2AccessToken token = Mockito.mock(OAuth2AccessToken.class);
                 when(token.getRefreshToken()).thenReturn(TEST_REFRESH_TOKEN);
                 when(svc.getAccessToken(TEST_CODE)).thenReturn(token);
                 when(oauthMock.getOAuth20Service()).thenReturn(svc);
             })) {

            String state = "user@test.com_setup#authContextIdtestCtx";
            authHelperMock.when(() -> AuthHelper.getAuthContextId(state)).thenReturn("authContextIdtestCtx");

            GraphServiceClientProvider clientProvider = mock(GraphServiceClientProvider.class, RETURNS_DEEP_STUBS);
            OutlookAttributes attrs = mock(OutlookAttributes.class);
            when(clientProvider.getOutlookAttributes()).thenReturn(attrs);
            when(providerFactory.create("authContextIdtestCtx")).thenReturn(clientProvider);
            when(context.isAuthenticated()).thenReturn(true);

            String result = resource.getCallBackForV2Auth(TEST_CODE, state);

            assertEquals(OutlookApiResource.USER_AUTHENTICATED_SUCCESSFULLY_PLEASE_PROCEED_WITH_REQUEST, result);
            verify(authorizationListener).authorized();
            verify(refreshTokenStore).put(eq("user@test.com_setup"), eq(TEST_REFRESH_TOKEN));
            verify(outlookAttributeStore).remove("authContextIdtestCtx");
        }
    }

    @Test
    @DisplayName("V2 auth callback - success not authenticated returns save changes message")
    void testV2Auth_SuccessNotAuthenticated() {
        try (MockedStatic<AuthHelper> authHelperMock = mockStatic(AuthHelper.class);
             MockedConstruction<OAuthService> ignored = mockConstruction(OAuthService.class, (oauthMock, ctx) -> {
                 OAuth20Service svc = Mockito.mock(OAuth20Service.class);
                 OAuth2AccessToken token = Mockito.mock(OAuth2AccessToken.class);
                 when(token.getRefreshToken()).thenReturn(TEST_REFRESH_TOKEN);
                 when(svc.getAccessToken(TEST_CODE)).thenReturn(token);
                 when(oauthMock.getOAuth20Service()).thenReturn(svc);
             })) {

            String state = "user@test.com_setup#somepart";
            authHelperMock.when(() -> AuthHelper.getAuthContextId(state)).thenReturn(null);

            GraphServiceClientProvider clientProvider = mock(GraphServiceClientProvider.class, RETURNS_DEEP_STUBS);
            OutlookAttributes attrs = mock(OutlookAttributes.class);
            when(clientProvider.getOutlookAttributes()).thenReturn(attrs);
            when(providerFactory.create()).thenReturn(clientProvider);
            when(context.isAuthenticated()).thenReturn(false);

            String result = resource.getCallBackForV2Auth(TEST_CODE, state);

            assertEquals(Constants.USER_AUTHENTICATED_SUCCESSFULLY_SAVE_THE_CHANGES, result);
            verify(authorizationListener, never()).authorized();
            // authContextId is null, so remove should not be called
            verify(outlookAttributeStore, never()).remove(anyString());
        }
    }

    @Test
    @DisplayName("V3 auth callback delegates to authentication response")
    void testV3Auth_Delegates() {
        String result = resource.getCallbackForV3Auth(null, "some-state");
        assertEquals("Authentication Failed. Please re-authorize.", result);
    }

    @Test
    @DisplayName("V3 auth callback - success path also works")
    void testV3Auth_SuccessPath() {
        try (MockedStatic<AuthHelper> authHelperMock = mockStatic(AuthHelper.class);
             MockedConstruction<OAuthService> ignored = mockConstruction(OAuthService.class, (oauthMock, ctx) -> {
                 OAuth20Service svc = Mockito.mock(OAuth20Service.class);
                 OAuth2AccessToken token = Mockito.mock(OAuth2AccessToken.class);
                 when(token.getRefreshToken()).thenReturn(TEST_REFRESH_TOKEN);
                 when(svc.getAccessToken(TEST_CODE)).thenReturn(token);
                 when(oauthMock.getOAuth20Service()).thenReturn(svc);
             })) {

            String state = "user@test.com_setup#part";
            authHelperMock.when(() -> AuthHelper.getAuthContextId(state)).thenReturn(null);

            GraphServiceClientProvider clientProvider = mock(GraphServiceClientProvider.class, RETURNS_DEEP_STUBS);
            when(clientProvider.getOutlookAttributes()).thenReturn(mock(OutlookAttributes.class));
            when(providerFactory.create()).thenReturn(clientProvider);
            when(context.isAuthenticated()).thenReturn(false);

            String result = resource.getCallbackForV3Auth(TEST_CODE, state);
            assertEquals(Constants.USER_AUTHENTICATED_SUCCESSFULLY_SAVE_THE_CHANGES, result);
        }
    }

    @Test
    @DisplayName("Auth callback - parts.length >= 4 extracts clientKey")
    void testAuth_PartsLength4_ExtractsClientKey() {
        try (MockedStatic<AuthHelper> authHelperMock = mockStatic(AuthHelper.class);
             MockedConstruction<OAuthService> ignored = mockConstruction(OAuthService.class, (oauthMock, ctx) -> {
                 OAuth20Service svc = Mockito.mock(OAuth20Service.class);
                 OAuth2AccessToken token = Mockito.mock(OAuth2AccessToken.class);
                 when(token.getRefreshToken()).thenReturn(TEST_REFRESH_TOKEN);
                 when(svc.getAccessToken(TEST_CODE)).thenReturn(token);
                 when(oauthMock.getOAuth20Service()).thenReturn(svc);
             })) {

            String state = "user@test.com_setup#clientKeyVal#part3#part4";
            authHelperMock.when(() -> AuthHelper.getAuthContextId(state)).thenReturn(null);

            GraphServiceClientProvider clientProvider = mock(GraphServiceClientProvider.class, RETURNS_DEEP_STUBS);
            when(clientProvider.getOutlookAttributes()).thenReturn(mock(OutlookAttributes.class));
            when(providerFactory.create()).thenReturn(clientProvider);
            when(context.isAuthenticated()).thenReturn(false);

            resource.getCallBackForV2Auth(TEST_CODE, state);

            verify(refreshTokenStore).put("user@test.com_setup", TEST_REFRESH_TOKEN);
            verify(refreshTokenStore).put("clientKeyVal", TEST_REFRESH_TOKEN);
        }
    }

    @Test
    @DisplayName("Auth callback - WS_CONTACT key with 3 parts extracts clientKey and skips user access check")
    void testAuth_WsContactWith3Parts_ExtractsClientKey() {
        try (MockedStatic<AuthHelper> authHelperMock = mockStatic(AuthHelper.class);
             MockedConstruction<OAuthService> ignored = mockConstruction(OAuthService.class, (oauthMock, ctx) -> {
                 OAuth20Service svc = Mockito.mock(OAuth20Service.class);
                 OAuth2AccessToken token = Mockito.mock(OAuth2AccessToken.class);
                 when(token.getRefreshToken()).thenReturn(TEST_REFRESH_TOKEN);
                 when(svc.getAccessToken(TEST_CODE)).thenReturn(token);
                 when(oauthMock.getOAuth20Service()).thenReturn(svc);
             })) {

            String state = "wsContact_user#clientKeyVal#part3";
            authHelperMock.when(() -> AuthHelper.getAuthContextId(state)).thenReturn(null);

            GraphServiceClientProvider clientProvider = mock(GraphServiceClientProvider.class, RETURNS_DEEP_STUBS);
            when(clientProvider.getOutlookAttributes()).thenReturn(mock(OutlookAttributes.class));
            when(providerFactory.create()).thenReturn(clientProvider);
            when(context.isAuthenticated()).thenReturn(true);

            String result = resource.getCallBackForV2Auth(TEST_CODE, state);

            assertEquals(OutlookApiResource.USER_AUTHENTICATED_SUCCESSFULLY_PLEASE_PROCEED_WITH_REQUEST, result);
            verify(refreshTokenStore).put("wsContact_user", TEST_REFRESH_TOKEN);
            verify(refreshTokenStore).put("clientKeyVal", TEST_REFRESH_TOKEN);
        }
    }

    @Test
    @DisplayName("Auth callback - user access check fails returns unauthorized message")
    void testAuth_UserAccessCheckFails() {
        try (MockedStatic<AuthHelper> authHelperMock = mockStatic(AuthHelper.class);
             MockedConstruction<OAuthService> ignored = mockConstruction(OAuthService.class, (oauthMock, ctx) -> {
                 OAuth20Service svc = Mockito.mock(OAuth20Service.class);
                 OAuth2AccessToken token = Mockito.mock(OAuth2AccessToken.class);
                 when(token.getRefreshToken()).thenReturn(TEST_REFRESH_TOKEN);
                 when(svc.getAccessToken(TEST_CODE)).thenReturn(token);
                 when(oauthMock.getOAuth20Service()).thenReturn(svc);
             })) {

            String state = "user@test.com_setup#somepart";
            authHelperMock.when(() -> AuthHelper.getAuthContextId(state)).thenReturn(null);

            GraphServiceClientProvider clientProvider = mock(GraphServiceClientProvider.class);
            when(clientProvider.getOutlookAttributes()).thenReturn(mock(OutlookAttributes.class));

            // Explicitly mock the chain to avoid deep stub ClassCastException
            UserRequestBuilder userReqBuilder = mock(UserRequestBuilder.class);
            MailFolderCollectionRequestBuilder mailFoldersBuilder = mock(MailFolderCollectionRequestBuilder.class);
            MailFolderCollectionRequest mailFoldersRequest = mock(MailFolderCollectionRequest.class);
            when(clientProvider.getUserRequestBuilder(null, null)).thenReturn(userReqBuilder);
            when(userReqBuilder.mailFolders()).thenReturn(mailFoldersBuilder);
            when(mailFoldersBuilder.buildRequest()).thenReturn(mailFoldersRequest);
            GraphServiceException gse = mock(GraphServiceException.class);
            when(mailFoldersRequest.get()).thenThrow(gse);

            when(providerFactory.create()).thenReturn(clientProvider);

            String result = resource.getCallBackForV2Auth(TEST_CODE, state);

            assertTrue(result.contains(Constants.UNAUTHORISED_USER));
            assertTrue(result.contains("user@test.com"));
            verify(refreshTokenStore).remove("user@test.com_setup");
        }
    }

    @Test
    @DisplayName("Auth callback - 3 parts without WS_CONTACT key does not extract clientKey")
    void testAuth_ThreePartsNonWsContact_NoClientKey() {
        try (MockedStatic<AuthHelper> authHelperMock = mockStatic(AuthHelper.class);
             MockedConstruction<OAuthService> ignored = mockConstruction(OAuthService.class, (oauthMock, ctx) -> {
                 OAuth20Service svc = Mockito.mock(OAuth20Service.class);
                 OAuth2AccessToken token = Mockito.mock(OAuth2AccessToken.class);
                 when(token.getRefreshToken()).thenReturn(TEST_REFRESH_TOKEN);
                 when(svc.getAccessToken(TEST_CODE)).thenReturn(token);
                 when(oauthMock.getOAuth20Service()).thenReturn(svc);
             })) {

            // 3 parts but key does NOT start with WS_CONTACT → clientKey stays null
            String state = "user@test.com_setup#part2#part3";
            authHelperMock.when(() -> AuthHelper.getAuthContextId(state)).thenReturn(null);

            GraphServiceClientProvider clientProvider = mock(GraphServiceClientProvider.class, RETURNS_DEEP_STUBS);
            when(clientProvider.getOutlookAttributes()).thenReturn(mock(OutlookAttributes.class));
            when(providerFactory.create()).thenReturn(clientProvider);
            when(context.isAuthenticated()).thenReturn(false);

            resource.getCallBackForV2Auth(TEST_CODE, state);

            // Only one put for the key, no second put for clientKey
            verify(refreshTokenStore, times(1)).put(anyString(), eq(TEST_REFRESH_TOKEN));
            verify(refreshTokenStore).put("user@test.com_setup", TEST_REFRESH_TOKEN);
        }
    }

    // ==================== OAuthException Error Description ====================

    @Test
    @DisplayName("Auth callback - OAuthException with JSON error description")
    void testAuth_OAuthException_WithErrorDescription() {
        try (MockedStatic<AuthHelper> authHelperMock = mockStatic(AuthHelper.class);
             MockedConstruction<OAuthService> ignored = mockConstruction(OAuthService.class, (oauthMock, ctx) -> {
                 OAuth20Service svc = Mockito.mock(OAuth20Service.class);
                 when(svc.getAccessToken(TEST_CODE))
                         .thenThrow(new OAuthException("{\"error_description\": \"Invalid grant\"}"));
                 when(oauthMock.getOAuth20Service()).thenReturn(svc);
             })) {

            String state = "user@test.com_setup#part";
            authHelperMock.when(() -> AuthHelper.getAuthContextId(state)).thenReturn("ctxId");

            GraphServiceClientProvider clientProvider = mock(GraphServiceClientProvider.class);
            when(clientProvider.getOutlookAttributes()).thenReturn(mock(OutlookAttributes.class));
            when(providerFactory.create("ctxId")).thenReturn(clientProvider);

            IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                    resource.getCallBackForV2Auth(TEST_CODE, state));

            assertTrue(ex.getMessage().contains("Invalid grant"));
            verify(outlookAttributeStore).remove("ctxId");
        }
    }

    @Test
    @DisplayName("Auth callback - OAuthException with null message")
    void testAuth_OAuthException_NullMessage() {
        try (MockedStatic<AuthHelper> authHelperMock = mockStatic(AuthHelper.class);
             MockedConstruction<OAuthService> ignored = mockConstruction(OAuthService.class, (oauthMock, ctx) -> {
                 OAuth20Service svc = Mockito.mock(OAuth20Service.class);
                 OAuthException oauthEx = Mockito.mock(OAuthException.class);
                 when(oauthEx.getMessage()).thenReturn(null);
                 when(svc.getAccessToken(TEST_CODE)).thenThrow(oauthEx);
                 when(oauthMock.getOAuth20Service()).thenReturn(svc);
             })) {

            String state = "user@test.com_setup#part";
            authHelperMock.when(() -> AuthHelper.getAuthContextId(state)).thenReturn(null);

            GraphServiceClientProvider clientProvider = mock(GraphServiceClientProvider.class);
            when(clientProvider.getOutlookAttributes()).thenReturn(mock(OutlookAttributes.class));
            when(providerFactory.create()).thenReturn(clientProvider);

            IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                    resource.getCallBackForV2Auth(TEST_CODE, state));

            assertEquals(Constants.ERROR_OCCURRED_DURING_AUTHORIZATION, ex.getMessage());
        }
    }

    @Test
    @DisplayName("Auth callback - OAuthException with blank message")
    void testAuth_OAuthException_BlankMessage() {
        try (MockedStatic<AuthHelper> authHelperMock = mockStatic(AuthHelper.class);
             MockedConstruction<OAuthService> ignored = mockConstruction(OAuthService.class, (oauthMock, ctx) -> {
                 OAuth20Service svc = Mockito.mock(OAuth20Service.class);
                 OAuthException oauthEx = Mockito.mock(OAuthException.class);
                 when(oauthEx.getMessage()).thenReturn("   ");
                 when(svc.getAccessToken(TEST_CODE)).thenThrow(oauthEx);
                 when(oauthMock.getOAuth20Service()).thenReturn(svc);
             })) {

            String state = "user@test.com_setup#part";
            authHelperMock.when(() -> AuthHelper.getAuthContextId(state)).thenReturn(null);

            GraphServiceClientProvider clientProvider = mock(GraphServiceClientProvider.class);
            when(clientProvider.getOutlookAttributes()).thenReturn(mock(OutlookAttributes.class));
            when(providerFactory.create()).thenReturn(clientProvider);

            IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                    resource.getCallBackForV2Auth(TEST_CODE, state));

            assertEquals(Constants.ERROR_OCCURRED_DURING_AUTHORIZATION, ex.getMessage());
        }
    }

    @Test
    @DisplayName("Auth callback - OAuthException with JSON missing error_description key")
    void testAuth_OAuthException_NoErrorDescriptionKey() {
        try (MockedStatic<AuthHelper> authHelperMock = mockStatic(AuthHelper.class);
             MockedConstruction<OAuthService> ignored = mockConstruction(OAuthService.class, (oauthMock, ctx) -> {
                 OAuth20Service svc = Mockito.mock(OAuth20Service.class);
                 when(svc.getAccessToken(TEST_CODE))
                         .thenThrow(new OAuthException("{\"error\": \"bad_request\"}"));
                 when(oauthMock.getOAuth20Service()).thenReturn(svc);
             })) {

            String state = "user@test.com_setup#part";
            authHelperMock.when(() -> AuthHelper.getAuthContextId(state)).thenReturn(null);

            GraphServiceClientProvider clientProvider = mock(GraphServiceClientProvider.class);
            when(clientProvider.getOutlookAttributes()).thenReturn(mock(OutlookAttributes.class));
            when(providerFactory.create()).thenReturn(clientProvider);

            IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                    resource.getCallBackForV2Auth(TEST_CODE, state));

            assertEquals(Constants.ERROR_OCCURRED_DURING_AUTHORIZATION, ex.getMessage());
        }
    }

    // ==================== IOException / ExecutionException / InterruptedException ====================

    @Test
    @DisplayName("Auth callback - IOException throws IllegalStateException")
    void testAuth_IOException() {
        try (MockedStatic<AuthHelper> authHelperMock = mockStatic(AuthHelper.class);
             MockedConstruction<OAuthService> ignored = mockConstruction(OAuthService.class, (oauthMock, ctx) -> {
                 OAuth20Service svc = Mockito.mock(OAuth20Service.class);
                 when(svc.getAccessToken(TEST_CODE)).thenThrow(new IOException("IO error"));
                 when(oauthMock.getOAuth20Service()).thenReturn(svc);
             })) {

            String state = "user@test.com_setup#part";
            authHelperMock.when(() -> AuthHelper.getAuthContextId(state)).thenReturn(null);

            GraphServiceClientProvider clientProvider = mock(GraphServiceClientProvider.class);
            when(clientProvider.getOutlookAttributes()).thenReturn(mock(OutlookAttributes.class));
            when(providerFactory.create()).thenReturn(clientProvider);

            IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                    resource.getCallBackForV2Auth(TEST_CODE, state));

            assertEquals(Constants.ERROR_OCCURRED_DURING_AUTHORIZATION, ex.getMessage());
        }
    }

    @Test
    @DisplayName("Auth callback - ExecutionException throws IllegalStateException")
    void testAuth_ExecutionException() {
        try (MockedStatic<AuthHelper> authHelperMock = mockStatic(AuthHelper.class);
             MockedConstruction<OAuthService> ignored = mockConstruction(OAuthService.class, (oauthMock, ctx) -> {
                 OAuth20Service svc = Mockito.mock(OAuth20Service.class);
                 when(svc.getAccessToken(TEST_CODE)).thenThrow(new ExecutionException("Exec error", null));
                 when(oauthMock.getOAuth20Service()).thenReturn(svc);
             })) {

            String state = "user@test.com_setup#part";
            authHelperMock.when(() -> AuthHelper.getAuthContextId(state)).thenReturn(null);

            GraphServiceClientProvider clientProvider = mock(GraphServiceClientProvider.class);
            when(clientProvider.getOutlookAttributes()).thenReturn(mock(OutlookAttributes.class));
            when(providerFactory.create()).thenReturn(clientProvider);

            IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                    resource.getCallBackForV2Auth(TEST_CODE, state));

            assertEquals(Constants.ERROR_OCCURRED_DURING_AUTHORIZATION, ex.getMessage());
        }
    }

    @Test
    @DisplayName("Auth callback - InterruptedException interrupts thread and throws")
    void testAuth_InterruptedException() {
        try (MockedStatic<AuthHelper> authHelperMock = mockStatic(AuthHelper.class);
             MockedConstruction<OAuthService> ignored = mockConstruction(OAuthService.class, (oauthMock, ctx) -> {
                 OAuth20Service svc = Mockito.mock(OAuth20Service.class);
                 when(svc.getAccessToken(TEST_CODE)).thenThrow(new InterruptedException("Interrupted"));
                 when(oauthMock.getOAuth20Service()).thenReturn(svc);
             })) {

            String state = "user@test.com_setup#part";
            authHelperMock.when(() -> AuthHelper.getAuthContextId(state)).thenReturn(null);

            GraphServiceClientProvider clientProvider = mock(GraphServiceClientProvider.class);
            when(clientProvider.getOutlookAttributes()).thenReturn(mock(OutlookAttributes.class));
            when(providerFactory.create()).thenReturn(clientProvider);

            IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                    resource.getCallBackForV2Auth(TEST_CODE, state));

            assertEquals(Constants.ERROR_OCCURRED_DURING_AUTHORIZATION, ex.getMessage());
            assertTrue(Thread.currentThread().isInterrupted());
            // Clear interrupted status for other tests
            Thread.interrupted();
        }
    }

    // ==================== Subscription Endpoints ====================

    @Test
    @DisplayName("Subscription validation returns trimmed validation token")
    void testSubscriptionValidation() {
        Response response = resource.subscriptionValidation("  test-token  ");
        assertEquals(200, response.getStatus());
        assertEquals("test-token", response.getEntity());
    }

    @Test
    @DisplayName("Subscription notification processes single message and renews subscription")
    void testSubscriptionNotification_SingleMessage() {
        try (MockedStatic<MailSubscription> mailSubMock = mockStatic(MailSubscription.class)) {
            JsonObject notification = createMailNotification("msg-001");
            GraphServiceClientProvider provider = mock(GraphServiceClientProvider.class);
            when(providerFactory.create()).thenReturn(provider);
            mailSubMock.when(() -> MailSubscription.createOrUpdateSubscription(eq(TEST_ROUTING_URL), eq(provider)))
                    .thenReturn(true);

            Response response = resource.subscriptionNotification(notification);

            assertEquals(200, response.getStatus());
            verify(eventHandler).handleEvent(eq(Constants.MAIL_RECEIVED), any(FreeForm.class));
            assertTrue(OutlookApiResource.isMessageIdTriggered("msg-001"));
        }
    }

    @Test
    @DisplayName("Subscription notification rejects duplicate message ID")
    void testSubscriptionNotification_DuplicateMessage() {
        try (MockedStatic<MailSubscription> mailSubMock = mockStatic(MailSubscription.class)) {
            GraphServiceClientProvider provider = mock(GraphServiceClientProvider.class);
            when(providerFactory.create()).thenReturn(provider);
            mailSubMock.when(() -> MailSubscription.createOrUpdateSubscription(anyString(), any()))
                    .thenReturn(true);

            resource.subscriptionNotification(createMailNotification("msg-dup"));
            resource.subscriptionNotification(createMailNotification("msg-dup"));

            // handleEvent called only once (duplicate rejected)
            verify(eventHandler, times(1)).handleEvent(eq(Constants.MAIL_RECEIVED), any(FreeForm.class));
        }
    }

    @Test
    @DisplayName("Subscription notification processes multiple unique messages")
    void testSubscriptionNotification_MultipleMessages() {
        try (MockedStatic<MailSubscription> mailSubMock = mockStatic(MailSubscription.class)) {
            JsonObject notification = new JsonObject();
            JsonArray array = new JsonArray();
            for (int i = 0; i < 3; i++) {
                JsonObject item = new JsonObject();
                JsonObject resourceData = new JsonObject();
                resourceData.addProperty("id", "multi-msg-" + i);
                item.add("resourceData", resourceData);
                array.add(item);
            }
            notification.add("value", array);

            when(providerFactory.create()).thenReturn(mock(GraphServiceClientProvider.class));
            mailSubMock.when(() -> MailSubscription.createOrUpdateSubscription(anyString(), any()))
                    .thenReturn(true);

            Response response = resource.subscriptionNotification(notification);

            assertEquals(200, response.getStatus());
            verify(eventHandler, times(3)).handleEvent(eq(Constants.MAIL_RECEIVED), any(FreeForm.class));
        }
    }

    // ==================== Folder Monitoring Endpoints ====================

    @Test
    @DisplayName("Folder monitoring validation returns trimmed validation token")
    void testFolderMonitoringValidation() {
        Response response = resource.folderMonitoringValidation("  folder-token  ");
        assertEquals(200, response.getStatus());
        assertEquals("folder-token", response.getEntity());
    }

    @Test
    @DisplayName("Folder monitoring - null attributes skips processing")
    void testFolderMonitoring_NullAttributes() {
        JsonObject notification = createFolderNotification("msg-100", "created", "sub-1");
        when(providerFactory.create()).thenReturn(mock(GraphServiceClientProvider.class));
        when(outlookAttributeStore.load(TEST_INVOKER_ID)).thenReturn(null);

        Response response = resource.folderMonitoringNotification(notification);

        assertEquals(200, response.getStatus());
        verify(eventHandler, never()).handleEvent(anyString(), any());
    }

    @Test
    @DisplayName("Folder monitoring - ErrorItemNotFound skips processing gracefully")
    void testFolderMonitoring_ErrorItemNotFound() {
        JsonObject notification = createFolderNotification("msg-101", "created", "sub-1");
        GraphServiceClientProvider provider = mock(GraphServiceClientProvider.class, RETURNS_DEEP_STUBS);
        when(providerFactory.create()).thenReturn(provider);

        OutlookAttributes attrs = mock(OutlookAttributes.class);
        when(outlookAttributeStore.load(TEST_INVOKER_ID)).thenReturn(attrs);

        GraphServiceException gse = mock(GraphServiceException.class);
        when(gse.getMessage()).thenReturn("ErrorItemNotFound: The resource could not be found.");
        when(provider.getUserRequestBuilder(null, null).messages(anyString())
                .buildRequest(any(), any(), any()).get()).thenThrow(gse);

        Response response = resource.folderMonitoringNotification(notification);

        assertEquals(200, response.getStatus());
        verify(eventHandler, never()).handleEvent(anyString(), any());
    }

    @Test
    @DisplayName("Folder monitoring - GraphServiceException with null message re-throws")
    void testFolderMonitoring_GraphExceptionNullMessage() {
        JsonObject notification = createFolderNotification("msg-110", "created", "sub-1");
        GraphServiceClientProvider provider = mock(GraphServiceClientProvider.class, RETURNS_DEEP_STUBS);
        when(providerFactory.create()).thenReturn(provider);

        OutlookAttributes attrs = mock(OutlookAttributes.class);
        when(outlookAttributeStore.load(TEST_INVOKER_ID)).thenReturn(attrs);

        GraphServiceException gse = mock(GraphServiceException.class);
        when(gse.getMessage()).thenReturn(null);
        when(provider.getUserRequestBuilder(null, null).messages(anyString())
                .buildRequest(any(), any(), any()).get()).thenThrow(gse);

        Response response = resource.folderMonitoringNotification(notification);

        assertEquals(200, response.getStatus());
        verify(eventHandler, never()).handleEvent(anyString(), any());
    }

    @Test
    @DisplayName("Folder monitoring - other GraphServiceException is caught by outer handler")
    void testFolderMonitoring_OtherGraphException() {
        JsonObject notification = createFolderNotification("msg-102", "created", "sub-1");
        GraphServiceClientProvider provider = mock(GraphServiceClientProvider.class, RETURNS_DEEP_STUBS);
        when(providerFactory.create()).thenReturn(provider);

        OutlookAttributes attrs = mock(OutlookAttributes.class);
        when(outlookAttributeStore.load(TEST_INVOKER_ID)).thenReturn(attrs);

        GraphServiceException gse = mock(GraphServiceException.class);
        when(gse.getMessage()).thenReturn("AccessDenied");
        when(provider.getUserRequestBuilder(null, null).messages(anyString())
                .buildRequest(any(), any(), any()).get()).thenThrow(gse);

        Response response = resource.folderMonitoringNotification(notification);

        assertEquals(200, response.getStatus());
        verify(eventHandler, never()).handleEvent(anyString(), any());
    }

    @Test
    @DisplayName("Folder monitoring - null message skips processing")
    void testFolderMonitoring_NullMessage() {
        JsonObject notification = createFolderNotification("msg-103", "created", "sub-1");
        GraphServiceClientProvider provider = mock(GraphServiceClientProvider.class, RETURNS_DEEP_STUBS);
        when(providerFactory.create()).thenReturn(provider);

        OutlookAttributes attrs = mock(OutlookAttributes.class);
        when(attrs.getMonitoredFolders()).thenReturn(List.of());
        when(outlookAttributeStore.load(TEST_INVOKER_ID)).thenReturn(attrs);

        when(provider.getUserRequestBuilder(null, null).messages(anyString())
                .buildRequest(any(), any(), any()).get()).thenReturn(null);

        Response response = resource.folderMonitoringNotification(notification);

        assertEquals(200, response.getStatus());
        verify(eventHandler, never()).handleEvent(anyString(), any());
    }

    @Test
    @DisplayName("Folder monitoring - empty monitored folders triggers for all messages")
    void testFolderMonitoring_EmptyMonitoredFolders_TriggersAll() {
        JsonObject notification = createFolderNotification("msg-104", "created", "sub-1");
        GraphServiceClientProvider provider = mock(GraphServiceClientProvider.class, RETURNS_DEEP_STUBS);
        when(providerFactory.create()).thenReturn(provider);

        OutlookAttributes attrs = mock(OutlookAttributes.class);
        when(attrs.getMonitoredFolders()).thenReturn(List.of());
        when(outlookAttributeStore.load(TEST_INVOKER_ID)).thenReturn(attrs);

        Message message = createTestMessage("msg-104", "Test Subject", "folder-123");
        when(provider.getUserRequestBuilder(null, null).messages(anyString())
                .buildRequest(any(), any(), any()).get()).thenReturn(message);

        Response response = resource.folderMonitoringNotification(notification);

        assertEquals(200, response.getStatus());
        verify(eventHandler).handleEvent(eq(Constants.EMAIL_CHANGE_NOTIFICATION), any(FreeForm.class));
    }

    @Test
    @DisplayName("Folder monitoring - matching folder triggers event")
    void testFolderMonitoring_MatchingFolder() {
        try (MockedConstruction<AccountImpl> ignored = mockConstruction(AccountImpl.class, (acctMock, ctx) -> {
            Folder folder = Mockito.mock(Folder.class);
            when(folder.getFolderName()).thenReturn("Inbox");
            when(acctMock.getFolder("folder-456")).thenReturn(folder);
        })) {
            JsonObject notification = createFolderNotification("msg-105", "updated", "sub-2");
            GraphServiceClientProvider provider = mock(GraphServiceClientProvider.class, RETURNS_DEEP_STUBS);
            when(providerFactory.create()).thenReturn(provider);

            OutlookAttributes attrs = mock(OutlookAttributes.class);
            when(attrs.getMonitoredFolders()).thenReturn(List.of("Inbox", "Sent"));
            when(outlookAttributeStore.load(TEST_INVOKER_ID)).thenReturn(attrs);

            Message message = createTestMessage("msg-105", "Subject", "folder-456");
            when(provider.getUserRequestBuilder(null, null).messages(anyString())
                    .buildRequest(any(), any(), any()).get()).thenReturn(message);

            Response response = resource.folderMonitoringNotification(notification);

            assertEquals(200, response.getStatus());
            verify(eventHandler).handleEvent(eq(Constants.EMAIL_CHANGE_NOTIFICATION), any(FreeForm.class));
        }
    }

    @Test
    @DisplayName("Folder monitoring - non-matching folder skips event")
    void testFolderMonitoring_NonMatchingFolder() {
        try (MockedConstruction<AccountImpl> ignored = mockConstruction(AccountImpl.class, (acctMock, ctx) -> {
            Folder folder = Mockito.mock(Folder.class);
            when(folder.getFolderName()).thenReturn("Junk");
            when(acctMock.getFolder("folder-789")).thenReturn(folder);
        })) {
            JsonObject notification = createFolderNotification("msg-106", "created", "sub-3");
            GraphServiceClientProvider provider = mock(GraphServiceClientProvider.class, RETURNS_DEEP_STUBS);
            when(providerFactory.create()).thenReturn(provider);

            OutlookAttributes attrs = mock(OutlookAttributes.class);
            when(attrs.getMonitoredFolders()).thenReturn(List.of("Inbox"));
            when(outlookAttributeStore.load(TEST_INVOKER_ID)).thenReturn(attrs);

            Message message = createTestMessage("msg-106", "Subject", "folder-789");
            when(provider.getUserRequestBuilder(null, null).messages(anyString())
                    .buildRequest(any(), any(), any()).get()).thenReturn(message);

            Response response = resource.folderMonitoringNotification(notification);

            assertEquals(200, response.getStatus());
            verify(eventHandler, never()).handleEvent(anyString(), any());
        }
    }

    @Test
    @DisplayName("Folder monitoring - folder lookup exception continues without trigger")
    void testFolderMonitoring_FolderLookupException() {
        try (MockedConstruction<AccountImpl> ignored = mockConstruction(AccountImpl.class, (acctMock, ctx) -> {
            when(acctMock.getFolder(anyString())).thenThrow(new RuntimeException("Folder error"));
        })) {
            JsonObject notification = createFolderNotification("msg-107", "created", "sub-4");
            GraphServiceClientProvider provider = mock(GraphServiceClientProvider.class, RETURNS_DEEP_STUBS);
            when(providerFactory.create()).thenReturn(provider);

            OutlookAttributes attrs = mock(OutlookAttributes.class);
            when(attrs.getMonitoredFolders()).thenReturn(List.of("Inbox"));
            when(outlookAttributeStore.load(TEST_INVOKER_ID)).thenReturn(attrs);

            Message message = createTestMessage("msg-107", "Subject", "folder-000");
            when(provider.getUserRequestBuilder(null, null).messages(anyString())
                    .buildRequest(any(), any(), any()).get()).thenReturn(message);

            Response response = resource.folderMonitoringNotification(notification);

            assertEquals(200, response.getStatus());
            verify(eventHandler, never()).handleEvent(anyString(), any());
        }
    }

    @Test
    @DisplayName("Folder monitoring - null parentFolderId with monitored folders does not trigger")
    void testFolderMonitoring_NullParentFolderId() {
        JsonObject notification = createFolderNotification("msg-108", "created", "sub-5");
        GraphServiceClientProvider provider = mock(GraphServiceClientProvider.class, RETURNS_DEEP_STUBS);
        when(providerFactory.create()).thenReturn(provider);

        OutlookAttributes attrs = mock(OutlookAttributes.class);
        when(attrs.getMonitoredFolders()).thenReturn(List.of("Inbox"));
        when(outlookAttributeStore.load(TEST_INVOKER_ID)).thenReturn(attrs);

        Message message = createTestMessage("msg-108", "Subject", null);
        when(provider.getUserRequestBuilder(null, null).messages(anyString())
                .buildRequest(any(), any(), any()).get()).thenReturn(message);

        Response response = resource.folderMonitoringNotification(notification);

        assertEquals(200, response.getStatus());
        verify(eventHandler, never()).handleEvent(anyString(), any());
    }

    @Test
    @DisplayName("Folder monitoring - general exception is caught and logged")
    void testFolderMonitoring_GeneralException() {
        JsonObject notification = createFolderNotification("msg-300", "created", "sub-20");
        when(providerFactory.create()).thenThrow(new RuntimeException("Unexpected error"));

        Response response = resource.folderMonitoringNotification(notification);

        assertEquals(200, response.getStatus());
        verify(eventHandler, never()).handleEvent(anyString(), any());
    }

    @Test
    @DisplayName("Folder monitoring - notification without optional changeType/subscriptionId uses defaults")
    void testFolderMonitoring_MissingOptionalFields() {
        JsonObject notification = new JsonObject();
        JsonArray array = new JsonArray();
        JsonObject item = new JsonObject();
        JsonObject resourceData = new JsonObject();
        resourceData.addProperty("id", "msg-109");
        item.add("resourceData", resourceData);
        array.add(item);
        notification.add("value", array);

        when(providerFactory.create()).thenReturn(mock(GraphServiceClientProvider.class));
        when(outlookAttributeStore.load(TEST_INVOKER_ID)).thenReturn(null);

        Response response = resource.folderMonitoringNotification(notification);
        assertEquals(200, response.getStatus());
    }

    // ==================== Build Email Notification Payload ====================

    @Test
    @DisplayName("Email payload - all null message fields produce empty strings")
    void testBuildEmailPayload_AllNullFields() {
        JsonObject notification = createFolderNotification("msg-200", "created", "sub-10");
        GraphServiceClientProvider provider = mock(GraphServiceClientProvider.class, RETURNS_DEEP_STUBS);
        when(providerFactory.create()).thenReturn(provider);

        OutlookAttributes attrs = mock(OutlookAttributes.class);
        when(attrs.getMonitoredFolders()).thenReturn(List.of());
        when(outlookAttributeStore.load(TEST_INVOKER_ID)).thenReturn(attrs);

        Message message = new Message();
        message.id = "msg-200";
        message.subject = null;
        message.from = null;
        message.toRecipients = null;
        message.ccRecipients = null;
        message.bccRecipients = null;
        message.body = null;
        message.hasAttachments = null;
        message.parentFolderId = null;

        when(provider.getUserRequestBuilder(null, null).messages(anyString())
                .buildRequest(any(), any(), any()).get()).thenReturn(message);

        Response response = resource.folderMonitoringNotification(notification);

        assertEquals(200, response.getStatus());
        verify(eventHandler).handleEvent(eq(Constants.EMAIL_CHANGE_NOTIFICATION), any(FreeForm.class));
    }

    @Test
    @DisplayName("Email payload - fully populated message fields")
    void testBuildEmailPayload_PopulatedFields() {
        JsonObject notification = createFolderNotification("msg-201", "created", "sub-11");
        GraphServiceClientProvider provider = mock(GraphServiceClientProvider.class, RETURNS_DEEP_STUBS);
        when(providerFactory.create()).thenReturn(provider);

        OutlookAttributes attrs = mock(OutlookAttributes.class);
        when(attrs.getMonitoredFolders()).thenReturn(List.of());
        when(outlookAttributeStore.load(TEST_INVOKER_ID)).thenReturn(attrs);

        Message message = createPopulatedMessage();
        when(provider.getUserRequestBuilder(null, null).messages(anyString())
                .buildRequest(any(), any(), any()).get()).thenReturn(message);

        Response response = resource.folderMonitoringNotification(notification);

        assertEquals(200, response.getStatus());
        verify(eventHandler).handleEvent(eq(Constants.EMAIL_CHANGE_NOTIFICATION), any(FreeForm.class));
    }

    @Test
    @DisplayName("Email payload - from with null emailAddress")
    void testBuildEmailPayload_FromNullEmailAddress() {
        JsonObject notification = createFolderNotification("msg-202", "created", "sub-12");
        GraphServiceClientProvider provider = mock(GraphServiceClientProvider.class, RETURNS_DEEP_STUBS);
        when(providerFactory.create()).thenReturn(provider);

        OutlookAttributes attrs = mock(OutlookAttributes.class);
        when(attrs.getMonitoredFolders()).thenReturn(List.of());
        when(outlookAttributeStore.load(TEST_INVOKER_ID)).thenReturn(attrs);

        Message message = new Message();
        message.id = "msg-202";
        message.parentFolderId = "folder-1";
        message.from = new Recipient();
        message.from.emailAddress = null;
        message.toRecipients = new ArrayList<>();
        message.ccRecipients = new ArrayList<>();
        message.bccRecipients = new ArrayList<>();
        message.hasAttachments = false;

        when(provider.getUserRequestBuilder(null, null).messages(anyString())
                .buildRequest(any(), any(), any()).get()).thenReturn(message);

        Response response = resource.folderMonitoringNotification(notification);
        assertEquals(200, response.getStatus());
        verify(eventHandler).handleEvent(eq(Constants.EMAIL_CHANGE_NOTIFICATION), any(FreeForm.class));
    }

    @Test
    @DisplayName("Email payload - from with null address in emailAddress")
    void testBuildEmailPayload_FromNullAddress() {
        JsonObject notification = createFolderNotification("msg-203", "created", "sub-13");
        GraphServiceClientProvider provider = mock(GraphServiceClientProvider.class, RETURNS_DEEP_STUBS);
        when(providerFactory.create()).thenReturn(provider);

        OutlookAttributes attrs = mock(OutlookAttributes.class);
        when(attrs.getMonitoredFolders()).thenReturn(List.of());
        when(outlookAttributeStore.load(TEST_INVOKER_ID)).thenReturn(attrs);

        Message message = new Message();
        message.id = "msg-203";
        message.parentFolderId = "folder-1";
        message.from = new Recipient();
        message.from.emailAddress = new EmailAddress();
        message.from.emailAddress.address = null;
        message.hasAttachments = true;
        message.body = new ItemBody();
        message.body.content = null;

        when(provider.getUserRequestBuilder(null, null).messages(anyString())
                .buildRequest(any(), any(), any()).get()).thenReturn(message);

        Response response = resource.folderMonitoringNotification(notification);
        assertEquals(200, response.getStatus());
        verify(eventHandler).handleEvent(eq(Constants.EMAIL_CHANGE_NOTIFICATION), any(FreeForm.class));
    }

    @Test
    @DisplayName("Email payload - recipients with mixed valid/invalid entries (filter coverage)")
    void testBuildEmailPayload_MixedRecipients() {
        JsonObject notification = createFolderNotification("msg-204", "created", "sub-14");
        GraphServiceClientProvider provider = mock(GraphServiceClientProvider.class, RETURNS_DEEP_STUBS);
        when(providerFactory.create()).thenReturn(provider);

        OutlookAttributes attrs = mock(OutlookAttributes.class);
        when(attrs.getMonitoredFolders()).thenReturn(List.of());
        when(outlookAttributeStore.load(TEST_INVOKER_ID)).thenReturn(attrs);

        Message message = new Message();
        message.id = "msg-204";
        message.parentFolderId = "folder-mix";

        // To: one valid, one with null emailAddress, one with null address
        Recipient validTo = new Recipient();
        validTo.emailAddress = new EmailAddress();
        validTo.emailAddress.address = "valid@test.com";
        Recipient nullEmailTo = new Recipient();
        nullEmailTo.emailAddress = null;
        Recipient nullAddrTo = new Recipient();
        nullAddrTo.emailAddress = new EmailAddress();
        nullAddrTo.emailAddress.address = null;
        message.toRecipients = List.of(validTo, nullEmailTo, nullAddrTo);

        // CC: one valid, one with null emailAddress, one with non-null emailAddress but null address
        Recipient validCc = new Recipient();
        validCc.emailAddress = new EmailAddress();
        validCc.emailAddress.address = "cc@test.com";
        Recipient nullCc = new Recipient();
        nullCc.emailAddress = null;
        Recipient nullAddrCc = new Recipient();
        nullAddrCc.emailAddress = new EmailAddress();
        nullAddrCc.emailAddress.address = null;
        message.ccRecipients = List.of(validCc, nullCc, nullAddrCc);

        // BCC: one with null emailAddress, one with null address, one valid
        Recipient nullEmailBcc = new Recipient();
        nullEmailBcc.emailAddress = null;
        Recipient nullAddrBcc = new Recipient();
        nullAddrBcc.emailAddress = new EmailAddress();
        nullAddrBcc.emailAddress.address = null;
        Recipient validBcc = new Recipient();
        validBcc.emailAddress = new EmailAddress();
        validBcc.emailAddress.address = "bcc@test.com";
        message.bccRecipients = List.of(nullEmailBcc, nullAddrBcc, validBcc);

        message.hasAttachments = false;

        when(provider.getUserRequestBuilder(null, null).messages(anyString())
                .buildRequest(any(), any(), any()).get()).thenReturn(message);

        Response response = resource.folderMonitoringNotification(notification);
        assertEquals(200, response.getStatus());
        verify(eventHandler).handleEvent(eq(Constants.EMAIL_CHANGE_NOTIFICATION), any(FreeForm.class));
    }

    // ==================== Lifecycle Endpoints ====================

    @Test
    @DisplayName("Lifecycle validation returns trimmed validation token")
    void testLifecycleValidation() {
        Response response = resource.lifecycleValidation("  lifecycle-token  ");
        assertEquals(200, response.getStatus());
        assertEquals("lifecycle-token", response.getEntity());
    }

    @Test
    @DisplayName("Lifecycle notification queues notification and returns 202")
    void testLifecycleNotification() {
        JsonObject notification = new JsonObject();
        notification.addProperty("type", "lifecycle");

        Response response = resource.lifecycleNotification(notification);

        assertEquals(202, response.getStatus());
    }

    @Test
    @DisplayName("Folder lifecycle validation returns trimmed validation token")
    void testFolderLifecycleValidation() {
        Response response = resource.folderLifecycleValidation("  folder-lifecycle-token  ");
        assertEquals(200, response.getStatus());
        assertEquals("folder-lifecycle-token", response.getEntity());
    }

    @Test
    @DisplayName("Folder lifecycle notification queues notification and returns 202")
    void testFolderLifecycleNotification() {
        JsonObject notification = new JsonObject();
        notification.addProperty("type", "folderLifecycle");

        Response response = resource.folderLifecycleNotification(notification);

        assertEquals(202, response.getStatus());
    }

    // ==================== Custom Tabs ====================

    @Test
    @DisplayName("Custom tabs - empty subPath loads index.html")
    void testCustomTabs_EmptySubPath() {
        InputStream result = resource.customTabs("");
        // Resource may not exist in test classpath; just ensure no exception
        assertDoesNotThrow(() -> resource.customTabs(""));
    }

    @Test
    @DisplayName("Custom tabs - non-empty subPath loads specified resource")
    void testCustomTabs_NonEmptySubPath() {
        assertDoesNotThrow(() -> resource.customTabs("style.css"));
    }

    // ==================== Save Credentials ====================

    @Test
    @DisplayName("Save credentials - no decryption needed for public auth")
    void testSaveCredentials_NoDecryption() {
        JsonObject payload = new JsonObject();
        payload.addProperty("authType", "Public");
        when(saveConfigurationImpl.saveCredentials(payload)).thenReturn("saved");

        String result = resource.saveCredentials(payload);

        assertEquals("saved", result);
    }

    @Test
    @DisplayName("Save credentials - decrypts private auth client secret")
    void testSaveCredentials_WithDecryption() {
        try (MockedStatic<EncryptionUtil> encMock = mockStatic(EncryptionUtil.class)) {
            String encrypted = KRISTA_PREFIX + "encryptedValue";
            encMock.when(() -> EncryptionUtil.decrypt(encrypted)).thenReturn("decryptedSecret");

            JsonObject payload = new JsonObject();
            payload.addProperty(AUTH_TYPE, Constants.PRIVATE);
            payload.addProperty(CLIENT_SECRET, encrypted);
            when(saveConfigurationImpl.saveCredentials(any())).thenReturn("saved");

            String result = resource.saveCredentials(payload);

            assertEquals("saved", result);
            assertEquals("decryptedSecret", payload.get(CLIENT_SECRET).getAsString());
        }
    }

    @Test
    @DisplayName("Decrypt - authType present but not Private, secret unchanged")
    void testDecrypt_NonPrivateAuthType() {
        JsonObject payload = new JsonObject();
        payload.addProperty(AUTH_TYPE, "Public");
        payload.addProperty(CLIENT_SECRET, KRISTA_PREFIX + "encrypted");
        when(saveConfigurationImpl.saveCredentials(any())).thenReturn("ok");

        resource.saveCredentials(payload);

        assertTrue(payload.get(CLIENT_SECRET).getAsString().startsWith(KRISTA_PREFIX));
    }

    @Test
    @DisplayName("Decrypt - Private auth type but clientSecret without KRISTA_PREFIX, no decrypt")
    void testDecrypt_PrivateNoKristaPrefix() {
        JsonObject payload = new JsonObject();
        payload.addProperty(AUTH_TYPE, Constants.PRIVATE);
        payload.addProperty(CLIENT_SECRET, "plainSecret");
        when(saveConfigurationImpl.saveCredentials(any())).thenReturn("ok");

        resource.saveCredentials(payload);

        assertEquals("plainSecret", payload.get(CLIENT_SECRET).getAsString());
    }

    @Test
    @DisplayName("Decrypt - Private auth type but no clientSecret key in payload")
    void testDecrypt_PrivateNoClientSecretKey() {
        JsonObject payload = new JsonObject();
        payload.addProperty(AUTH_TYPE, Constants.PRIVATE);
        when(saveConfigurationImpl.saveCredentials(any())).thenReturn("ok");

        assertDoesNotThrow(() -> resource.saveCredentials(payload));
    }

    @Test
    @DisplayName("Decrypt - no authType key in payload")
    void testDecrypt_NoAuthTypeKey() {
        JsonObject payload = new JsonObject();
        when(saveConfigurationImpl.saveCredentials(any())).thenReturn("ok");

        assertDoesNotThrow(() -> resource.saveCredentials(payload));
    }

    // ==================== Test Connection ====================

    @Test
    @DisplayName("Test connection creates OutlookAttributes and invokes test")
    void testTestConnection() {
        try (MockedStatic<OutlookAttributes> attrMock = mockStatic(OutlookAttributes.class)) {
            JsonObject payload = new JsonObject();
            payload.addProperty("authType", "Public");
            OutlookAttributes attrs = mock(OutlookAttributes.class);
            attrMock.when(() -> OutlookAttributes.create(payload, TEST_ROUTING_URL)).thenReturn(attrs);
            when(testConnectionService.testConnection(attrs, false)).thenReturn("success");

            String result = resource.testConnection(payload);

            assertEquals("success", result);
        }
    }

    @Test
    @DisplayName("Test connection with private auth and encrypted secret decrypts before test")
    void testTestConnection_WithDecryption() {
        try (MockedStatic<EncryptionUtil> encMock = mockStatic(EncryptionUtil.class);
             MockedStatic<OutlookAttributes> attrMock = mockStatic(OutlookAttributes.class)) {

            String encrypted = KRISTA_PREFIX + "secret";
            encMock.when(() -> EncryptionUtil.decrypt(encrypted)).thenReturn("decrypted");

            JsonObject payload = new JsonObject();
            payload.addProperty(AUTH_TYPE, Constants.PRIVATE);
            payload.addProperty(CLIENT_SECRET, encrypted);

            OutlookAttributes attrs = mock(OutlookAttributes.class);
            attrMock.when(() -> OutlookAttributes.create(any(JsonObject.class), eq(TEST_ROUTING_URL)))
                    .thenReturn(attrs);
            when(testConnectionService.testConnection(attrs, false)).thenReturn("ok");

            String result = resource.testConnection(payload);

            assertEquals("ok", result);
            assertEquals("decrypted", payload.get(CLIENT_SECRET).getAsString());
        }
    }

    // ==================== Get Credentials ====================

    @Test
    @DisplayName("Get credentials - null attributes returns empty string")
    void testGetCredentials_NullAttributes() {
        when(outlookAttributeStore.load(TEST_INVOKER_ID)).thenReturn(null);

        String result = resource.getCredentials("Public");

        assertEquals("", result);
    }

    @Test
    @DisplayName("Get credentials - auth type mismatch returns empty string")
    void testGetCredentials_AuthTypeMismatch() {
        OutlookAttributes attrs = mock(OutlookAttributes.class);
        when(attrs.getAuthType()).thenReturn("Private");
        when(outlookAttributeStore.load(TEST_INVOKER_ID)).thenReturn(attrs);

        String result = resource.getCredentials("Public");

        assertEquals("", result);
    }

    @Test
    @DisplayName("Get credentials - auth type matches returns encrypted JSON")
    void testGetCredentials_AuthTypeMatch() {
        try (MockedStatic<EncryptionUtil> encMock = mockStatic(EncryptionUtil.class)) {
            OutlookAttributes attrs = mock(OutlookAttributes.class);
            when(attrs.getAuthType()).thenReturn("Public");
            Map<String, Object> map = new HashMap<>();
            map.put(CLIENT_SECRET, "plainSecret");
            map.put("clientId", "client-123");
            when(attrs.toMap()).thenReturn(map);
            when(outlookAttributeStore.load(TEST_INVOKER_ID)).thenReturn(attrs);
            encMock.when(() -> EncryptionUtil.encrypt("plainSecret")).thenReturn(KRISTA_PREFIX + "encrypted");

            String result = resource.getCredentials("Public");

            assertNotEquals("", result);
            assertTrue(result.contains(KRISTA_PREFIX + "encrypted"));
        }
    }

    // ==================== Get Auth Key ====================

    @Test
    @DisplayName("Get auth key - null attributes returns empty string")
    void testGetAuthKey_NullAttributes() {
        when(outlookAttributeStore.load(TEST_INVOKER_ID)).thenReturn(null);

        String result = resource.getAuthKey();

        assertEquals("", result);
    }

    @Test
    @DisplayName("Get auth key - null auth type defaults to PUBLIC")
    void testGetAuthKey_NullAuthType() {
        OutlookAttributes attrs = mock(OutlookAttributes.class);
        when(attrs.getAuthType()).thenReturn(null);
        when(outlookAttributeStore.load(TEST_INVOKER_ID)).thenReturn(attrs);

        String result = resource.getAuthKey();

        assertTrue(result.contains(Constants.PUBLIC));
    }

    @Test
    @DisplayName("Get auth key - non-null auth type returns it")
    void testGetAuthKey_NonNullAuthType() {
        OutlookAttributes attrs = mock(OutlookAttributes.class);
        when(attrs.getAuthType()).thenReturn("Private");
        when(outlookAttributeStore.load(TEST_INVOKER_ID)).thenReturn(attrs);

        String result = resource.getAuthKey();

        assertTrue(result.contains("Private"));
    }

    // ==================== Listeners ====================

    @Test
    @DisplayName("Get listeners returns invoker event listeners")
    void testGetListeners() {
        List<WaitForEventListener> listeners = List.of();
        when(invoker.listEventListeners()).thenReturn(listeners);

        Response response = resource.getListeners();

        assertEquals(200, response.getStatus());
        assertEquals(listeners, response.getEntity());
    }

    @Test
    @DisplayName("Clear listeners unregisters all and returns success")
    void testClearListeners() {
        WaitForEventListener listener1 = mock(WaitForEventListener.class);
        WaitForEventListener listener2 = mock(WaitForEventListener.class);
        when(listener1.getListenerId()).thenReturn("listener-1");
        when(listener2.getListenerId()).thenReturn("listener-2");
        when(invoker.listEventListeners()).thenReturn(List.of(listener1, listener2));
        when(invoker.unregisterEventListener(anyString())).thenReturn(CompletableFuture.completedFuture(null));

        Response response = resource.clearListeners();

        assertEquals(200, response.getStatus());
        verify(invoker).unregisterEventListener("listener-1");
        verify(invoker).unregisterEventListener("listener-2");
    }

    @Test
    @DisplayName("Clear listeners with empty list does not call unregister")
    void testClearListeners_EmptyList() {
        when(invoker.listEventListeners()).thenReturn(List.of());

        Response response = resource.clearListeners();

        assertEquals(200, response.getStatus());
        verify(invoker, never()).unregisterEventListener(anyString());
    }

    // ==================== isMessageIdTriggered ====================

    @Test
    @DisplayName("isMessageIdTriggered returns false for unknown ID")
    void testIsMessageIdTriggered_Unknown() {
        assertFalse(OutlookApiResource.isMessageIdTriggered("unknown-id"));
    }

    @Test
    @DisplayName("isMessageIdTriggered returns true after message is processed")
    void testIsMessageIdTriggered_AfterProcessing() {
        try (MockedStatic<MailSubscription> mailSubMock = mockStatic(MailSubscription.class)) {
            when(providerFactory.create()).thenReturn(mock(GraphServiceClientProvider.class));
            mailSubMock.when(() -> MailSubscription.createOrUpdateSubscription(anyString(), any()))
                    .thenReturn(true);

            resource.subscriptionNotification(createMailNotification("tracked-id"));

            assertTrue(OutlookApiResource.isMessageIdTriggered("tracked-id"));
        }
    }

    // ==================== LRU Eviction ====================

    @Test
    @DisplayName("Triggered mail IDs set evicts oldest entry when capacity exceeded")
    @SuppressWarnings("unchecked")
    void testRemoveEldestEntry_EvictsOldest() throws Exception {
        Field field = OutlookApiResource.class.getDeclaredField("triggeredMailIds");
        field.setAccessible(true);
        Set<String> set = (Set<String>) field.get(null);

        for (int i = 0; i < 1001; i++) {
            set.add("evict-msg-" + i);
        }

        assertFalse(set.contains("evict-msg-0"), "Oldest entry should be evicted");
        assertTrue(set.contains("evict-msg-1"), "Second entry should remain");
        assertTrue(set.contains("evict-msg-1000"), "Newest entry should remain");
        assertEquals(1000, set.size());
    }

    // ==================== Helper Methods ====================

    private JsonObject createMailNotification(String messageId) {
        JsonObject notification = new JsonObject();
        JsonArray array = new JsonArray();
        JsonObject item = new JsonObject();
        JsonObject resourceData = new JsonObject();
        resourceData.addProperty("id", messageId);
        item.add("resourceData", resourceData);
        array.add(item);
        notification.add("value", array);
        return notification;
    }

    private JsonObject createFolderNotification(String messageId, String changeType, String subscriptionId) {
        JsonObject notification = new JsonObject();
        JsonArray array = new JsonArray();
        JsonObject item = new JsonObject();
        JsonObject resourceData = new JsonObject();
        resourceData.addProperty("id", messageId);
        item.add("resourceData", resourceData);
        item.addProperty("changeType", changeType);
        item.addProperty("subscriptionId", subscriptionId);
        array.add(item);
        notification.add("value", array);
        return notification;
    }

    private Message createTestMessage(String id, String subject, String parentFolderId) {
        Message message = new Message();
        message.id = id;
        message.subject = subject;
        message.parentFolderId = parentFolderId;
        message.hasAttachments = false;
        return message;
    }

    private Message createPopulatedMessage() {
        Message message = new Message();
        message.id = "msg-201";
        message.subject = "Test Subject";
        message.parentFolderId = "folder-pop";

        message.from = new Recipient();
        message.from.emailAddress = new EmailAddress();
        message.from.emailAddress.address = "sender@test.com";

        Recipient to1 = new Recipient();
        to1.emailAddress = new EmailAddress();
        to1.emailAddress.address = "to1@test.com";
        Recipient to2 = new Recipient();
        to2.emailAddress = new EmailAddress();
        to2.emailAddress.address = "to2@test.com";
        message.toRecipients = List.of(to1, to2);

        Recipient cc1 = new Recipient();
        cc1.emailAddress = new EmailAddress();
        cc1.emailAddress.address = "cc@test.com";
        message.ccRecipients = List.of(cc1);

        Recipient bcc1 = new Recipient();
        bcc1.emailAddress = new EmailAddress();
        bcc1.emailAddress.address = "bcc@test.com";
        message.bccRecipients = List.of(bcc1);

        message.body = new ItemBody();
        message.body.content = "<html><body>Test body</body></html>";

        message.hasAttachments = true;

        return message;
    }
}
