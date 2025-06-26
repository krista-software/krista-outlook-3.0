package app.krista.extensions.essentials.collaboration.outlook3.impl;

import app.krista.extension.executor.ExtensionResponse;
import app.krista.extension.executor.Invoker;
import app.krista.extension.request.RoutingInfo;
import app.krista.extension.request.protos.http.HttpProtocol;
import app.krista.extensions.essentials.collaboration.outlook3.OutlookAttributes;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.MessagingArea;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.errorhandlers.ErrorHandlingStateManager;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.errorhandlers.ExtensionResponseGenerator;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.extresp.ExtensionResponseFactory;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.extresp.TelemetryHelper;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.validators.ValidationOrchestrator;
import app.krista.extensions.essentials.collaboration.outlook3.impl.connectors.GraphServiceClientProvider;
import app.krista.extensions.essentials.collaboration.outlook3.impl.connectors.GraphServiceClientProviderFactory;
import app.krista.extensions.essentials.collaboration.outlook3.impl.util.Constants;
import app.krista.extensions.essentials.collaboration.outlook3.service.Account;
import app.krista.extensions.essentials.collaboration.outlook3.service.Email;
import app.krista.extensions.util.EventHandler;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.ksdk.context.RequestContext;
import app.krista.ksdk.entities.Entities;
import app.krista.model.base.File;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockSettings;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import app.krista.extensions.essentials.collaboration.outlook3.service.Folder;
import app.krista.model.base.EntityValue;

/**
 * Tests for the MessagingArea implementation.
 * <p>
 * IMPORTANT: The credentials used in this test class (Client ID, Client Secret, Tenant ID, Email)
 * are for testing purposes only. These may be valid or invalid depending on your environment.
 * To run these tests successfully, please replace them with your own valid credentials
 * in the setup() method where OutlookAttributes is instantiated.
 */
public class MessagingAreaTest {

    // Your actual base URL and callback path... This can be changed as per the invoker
    private static final String BASE_URL = "https://extension.solution.eng.krista.app/extension/api/xAcwhzRYmXuSToCNf4ropQ_e_e";
    private static final String CALLBACK_PATH = "/rest/outlook/callback";
    private static final String FULL_CALLBACK_URL = BASE_URL + CALLBACK_PATH;

    // Use instance variables for mocks that are initialized in setup()
    private Account account;
    private RequestContext requestContext;
    private AuthorizationContext authorizationContext;
    private EventHandler eventHandler;
    private MailHandler mailHandler;
    private MessagingAreaImpl messagingAreaImpl2;
    private ExtensionResponseGenerator responseGenerator;
    private ErrorHandlingStateManager internalStateManager;
    private ValidationOrchestrator validationOrchestrator;
    private Entities registry;
    private GraphServiceClientProviderFactory providerFactory;
    private Invoker invoker;
    private RoutingInfo routingInfo;
    private GraphServiceClientProvider graphServiceClientProvider;

    private MessagingArea messagingArea;
    private MessagingAreaImpl messagingAreaImpl;
    private TestConnectionServiceImpl testConnectionService;
    private TelemetryHelper telemetryHelper;

    @BeforeEach
    public void setup() {
        // Create mocks with settings to avoid inline mocking
        MockSettings settings = withSettings().stubOnly();

        account = mock(Account.class, settings);
        requestContext = mock(RequestContext.class, settings);
        authorizationContext = mock(AuthorizationContext.class, settings);
        eventHandler = mock(EventHandler.class, settings);
        mailHandler = mock(MailHandler.class, settings);
        messagingAreaImpl2 = mock(MessagingAreaImpl.class, settings);
        responseGenerator = mock(ExtensionResponseGenerator.class, settings);
        internalStateManager = mock(ErrorHandlingStateManager.class, settings);
        validationOrchestrator = mock(ValidationOrchestrator.class, settings);
        registry = mock(Entities.class, settings);
        providerFactory = mock(GraphServiceClientProviderFactory.class, settings);
        invoker = mock(Invoker.class, settings);
        routingInfo = mock(RoutingInfo.class, settings);
        graphServiceClientProvider = mock(GraphServiceClientProvider.class, settings);
        testConnectionService = mock(TestConnectionServiceImpl.class, settings);
        telemetryHelper = mock(TelemetryHelper.class, settings);

        // Setup the routing info mock
        when(invoker.getRoutingInfo()).thenReturn(routingInfo);
        when(routingInfo.getRoutingURL(HttpProtocol.PROTOCOL_NAME, RoutingInfo.Type.APPLIANCE))
                .thenReturn(BASE_URL);
        when(invoker.getInvokerId()).thenReturn("xAcwhzRYmXuSToCNf4ropQ_e_e");

        // Setup the provider factory mock with your credentials
        OutlookAttributes attributes = new OutlookAttributes(
                "ec0745c8-7635-4b31-97cc-d217944dd620",  // Client ID
                "REDACTED_SECRET",  // Client Secret
                "3694f6b4-b5f1-47ef-852f-a0b4a459ab44",  // Tenant ID
                "service.automation@kristasoft.com",  // Email
                true,  // Allow mail alert
                Constants.PRIVATE,  // Auth type
                BASE_URL  // Routing URL
        );

        when(graphServiceClientProvider.getOutlookAttributes()).thenReturn(attributes);
        when(providerFactory.create()).thenReturn(graphServiceClientProvider);
        when(providerFactory.create(anyString())).thenReturn(graphServiceClientProvider);

        // Initialize the objects under test
        messagingArea = new MessagingArea(
                account,
                requestContext,
                authorizationContext,
                eventHandler,
                mailHandler,
                messagingAreaImpl2,
                responseGenerator,
                internalStateManager,
                validationOrchestrator,
                invoker,
                testConnectionService,
                telemetryHelper
        );

        messagingAreaImpl = new MessagingAreaImpl(account, mailHandler, registry);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testFetchAllLabels() {
        List<String> labels = Arrays.asList("Archive", "Conversation History", "Deleted Items", "Drafts", "Inbox", "Junk Email", "Outbox", "Sent Items");
        doReturn(labels).when(account).getFolderNames();
        ExtensionResponse response1 = messagingArea.fetchAllLabels();
        List<String> allLabels = (List<String>) response1.getResponseValue().get("Labels");
        assertTrue(allLabels.contains("Archive"));
    }


    @Test
    public void testReplyToAll() {
        String messageID = UUID.randomUUID().toString();
        String message = null;
        String bodyType = "HTML";
        List<File> attachments = null;
        Email email = mock(EmailImpl.class, withSettings().stubOnly());
        when(account.getEmail(messageID)).thenReturn(email);
        ExtensionResponse response4 = messagingAreaImpl.replyToAll(attachments, messageID, message, bodyType);
        assertEquals(response4.getResponseValue().get("Is Successful"), true);
    }

    @Test
    public void testForwardMail() {
        String messageID = null;
        String to = null;
        String message = null;
        String bodyType = "HTML";
        ExtensionResponse response1 = messagingAreaImpl.forwardMail(messageID, to, message, bodyType);
        assertEquals(response1.getResponseValue().get("Is Forwarded"), false);
        messageID = "";
        ExtensionResponse response2 = messagingAreaImpl.forwardMail(messageID, to, message, bodyType);
        assertEquals(response2.getResponseValue().get("Is Forwarded"), false);
        messageID = "   ";
        ExtensionResponse response3 = messagingAreaImpl.forwardMail(messageID, to, message, bodyType);
        assertEquals(response3.getResponseValue().get("Is Forwarded"), false);
    }

    @Test
    public void testFetchMailDetailsByQuery() {
        String query = null;
        ExtensionResponse response1 = messagingArea.fetchMailDetailsByQuery(query);
        assertEquals(response1.getResponseValue().get("Mails"), List.of());
        query = "";
        ExtensionResponse response2 = messagingArea.fetchMailDetailsByQuery(query);
        assertEquals(response2.getResponseValue().get("Mails"), List.of());
        query = "   ";
        ExtensionResponse response3 = messagingArea.fetchMailDetailsByQuery(query);
        assertEquals(response3.getResponseValue().get("Mails"), List.of());
    }

    @Test
    public void testReplyToMail() {
        String messageID = null;
        String message = null;
        List<File> attachments = null;
        String bodyType = "HTML";
        ExtensionResponse response1 = messagingAreaImpl.replyToMail(attachments, messageID, message, bodyType);
        assertEquals(response1.getResponseValue().get("Message"), "Invalid message id");
        messageID = "";
        ExtensionResponse response2 = messagingAreaImpl.replyToMail(attachments, messageID, message, bodyType);
        assertEquals(response2.getResponseValue().get("Message"), "Invalid message id");
        messageID = "  ";
        ExtensionResponse response3 = messagingAreaImpl.replyToMail(attachments, messageID, message, bodyType);
        assertEquals(response3.getResponseValue().get("Message"), "Invalid message id");
    }

    @Test
    public void testFetchMailsByLabel() {
        String label = "aaaa";
        ExtensionResponse response = messagingAreaImpl.fetchMailByLabel(label, 1.0, 1.0);
        assertEquals(response.getResponseValue().get("Mails"), List.of());
    }

    @Test
    public void testFetchSent() {
        Double pageNumber = 1.0;
        Double pageSize = 10.0;
        List<Email> emails = List.of(mock(Email.class));
        when(account.getSentFolder()).thenReturn(mock(Folder.class));
        when(account.getSentFolder().getEmails(pageNumber, pageSize)).thenReturn(emails);

        ExtensionResponse response = messagingArea.fetchSent(pageNumber, pageSize);

        assertEquals(1, ((List<?>) response.getResponseValue().get("Sent Mails")).size());
    }

    @Test
    public void testListCategories() {
        List<String> categories = Arrays.asList("Important", "Personal", "Work");
        when(account.getCategoryNames()).thenReturn(categories);

        ExtensionResponse response = messagingArea.listCategories();

        List<String> returnedCategories = (List<String>) response.getResponseValue().get("Category Names");
        assertEquals(3, returnedCategories.size());
        assertTrue(returnedCategories.contains("Important"));
    }

    @Test
    public void testTestConnection() {
        when(testConnectionService.testConnection(anyString()))
            .thenReturn(ExtensionResponseFactory.create(Map.of(
                "Is Connection Successful", true,
                "Test Connection Summary", Map.of(
                    "Summary", "Connection successful",
                    "Email", "service.automation@kristasoft.com",
                    "Allow Mail Alert", "true",
                    "Tenant ID", "3694f6b4-b5f1-47ef-852f-a0b4a459ab44",
                    "Client ID", "ec0745c8-7635-4b31-97cc-d217944dd620",
                    "Auth Type", "private",
                    "Mailbox Accessible", "true"
                )
            )));

        ExtensionResponse response = messagingArea.testConnection();

        assertEquals(true, response.getResponseValue().get("Is Connection Successful"));
        Map<String, String> summary = (Map<String, String>) response.getResponseValue().get("Test Connection Summary");
        assertEquals("Connection successful", summary.get("Summary"));
    }
}
