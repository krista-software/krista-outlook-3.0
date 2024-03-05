package app.krista.extensions.essentials.collaboration.outlook3;

import app.krista.extension.authorization.RequestAuthenticator;
import app.krista.extension.executor.Invoker;
import app.krista.extension.impl.anno.Extension;
import app.krista.extension.impl.anno.Field;
import app.krista.extension.impl.anno.InvokerRequest;
import app.krista.extension.impl.anno.StaticResource;
import app.krista.extension.request.RoutingInfo;
import app.krista.extension.request.protos.http.HttpProtocol;
import app.krista.extensions.essentials.collaboration.outlook3.impl.MailSubscription;
import app.krista.extensions.essentials.collaboration.outlook3.impl.connectors.GraphServiceClientProviderFactory;
import app.krista.extensions.essentials.collaboration.outlook3.impl.stores.OutlookAttributeStore;
import app.krista.extensions.essentials.collaboration.outlook3.impl.util.Validators;
import com.microsoft.graph.models.MailFolder;
import com.microsoft.graph.requests.MailFolderCollectionPage;

import javax.inject.Inject;
import java.util.Map;

@Field.Boolean(value = OutlookAttributes.LOGIN_WITH_MICROSOFT, required = false)
@Field.Text(value = OutlookAttributes.EMAIL)
@Field.Text(value = OutlookAttributes.CLIENT_ID, required = false)
@Field.Text(value = OutlookAttributes.CLIENT_SECRET, isSecured = true, required = false)
@Field.Text(value = OutlookAttributes.TENANT_ID, required = false)
@Field.Boolean(value = OutlookAttributes.ALLOW_ALERT_MAIL, required = false)
@Extension(version = "3.0.0-rc1")
@StaticResource(path = "docs", file = "docs")
public class OutlookExtension {

    private final String routingUrl;
    private final String routingId;
    private final OutlookAttributes outlookAttributes;
    private final GraphServiceClientProviderFactory clientProviderFactory;
    private final OutlookRequestAuthenticator requestAuthenticator;

    @Inject
    public OutlookExtension(Invoker invoker, OutlookAttributes outlookAttributes, GraphServiceClientProviderFactory clientProviderFactory, OutlookAttributeStore outlookAttributeStore) {
        this(invoker, outlookAttributes,
                clientProviderFactory,
                new OutlookRequestAuthenticator(outlookAttributeStore, outlookAttributes));
    }

    public OutlookExtension(Invoker invoker, OutlookAttributes outlookAttributes, GraphServiceClientProviderFactory clientProviderFactory, OutlookRequestAuthenticator requestAuthenticator) {
        this.routingUrl = invoker.getRoutingInfo().getRoutingURL(HttpProtocol.PROTOCOL_NAME, RoutingInfo.Type.APPLIANCE);
        this.outlookAttributes = outlookAttributes;
        this.clientProviderFactory = clientProviderFactory;
        this.requestAuthenticator = requestAuthenticator;
        this.routingId = invoker.getRoutingInfo().getRoutingId();
    }

    private static boolean areAllCredentialsBlank(OutlookAttributes attributes) {
        return Validators.isStringNullOrBlank(attributes.getClientId()) &&
                Validators.isStringNullOrBlank(attributes.getClientSecret()) &&
                Validators.isStringNullOrBlank(attributes.getTenantId());
    }

    private static boolean isAnyParameterValueBlank(OutlookAttributes attributes) {
        return Validators.isStringNullOrBlank(attributes.getClientId()) ||
                Validators.isStringNullOrBlank(attributes.getClientSecret()) ||
                Validators.isStringNullOrBlank(attributes.getTenantId());
    }

    @InvokerRequest(InvokerRequest.Type.AUTHENTICATOR)
    public RequestAuthenticator getRequestAuthenticator() {
        return requestAuthenticator;
    }

    @InvokerRequest(InvokerRequest.Type.CUSTOM_TABS)
    public Map<String, String> customTabs() {
        return Map.of("Documentation", "static/docs");
    }

    @InvokerRequest(InvokerRequest.Type.VALIDATE_ATTRIBUTES)
    public void validateConnection(Map<String, Object> connectionAttributes) {
        OutlookAttributes attributes = OutlookAttributes.create(routingUrl, connectionAttributes, routingId);
        if (Boolean.FALSE.equals(attributes.isLoginWithMicrosoft())) {
            if (areAllCredentialsBlank(attributes)) {
                throw new IllegalArgumentException("Please provide all the parameter values.");
            }
        } else if (!isAnyParameterValueBlank(attributes)) {
            throw new IllegalArgumentException("Only Mail id is required to login with Microsoft.");
        }
        testConnection(attributes);
        if (attributes.isAllowAlertMail()) {
            MailSubscription.createSubscription(routingUrl, clientProviderFactory.create(attributes));
        } else {
            MailSubscription.deleteSubscription(clientProviderFactory.create(attributes), routingUrl);
        }
    }

    @InvokerRequest(InvokerRequest.Type.TEST_CONNECTION)
    public void testConnection() {
        testConnection(outlookAttributes);
        if (outlookAttributes.isAllowAlertMail()) {
            MailSubscription.createSubscription(routingUrl, clientProviderFactory.create());
        } else {
            MailSubscription.deleteSubscription(clientProviderFactory.create(), routingUrl);
        }
    }

    private void testConnection(OutlookAttributes attributes) {
        if (attributes.getMailId() != null && !attributes.getMailId().isBlank()) {
            final MailFolderCollectionPage mailFolderCollectionPage = clientProviderFactory.create(attributes)
                    .getGraphServiceClientForAdmin()
                    .me().mailFolders().buildRequest().get();
            if (mailFolderCollectionPage != null) {
                for (MailFolder mailFolder : mailFolderCollectionPage.getCurrentPage()) {
                    System.out.println("Mail Folder: " + mailFolder.displayName);
                }
            }
        }
    }

    @InvokerRequest(InvokerRequest.Type.INVOKER_UPDATED)
    public void attributesUpdated(Map<String, Object> oldAttributes, Map<String, Object> newAttributes) {
        outlookAttributes.update(newAttributes);
    }

    @InvokerRequest(InvokerRequest.Type.INVOKER_REMOVED)
    public void invokerRemoved() {
        MailSubscription.deleteSubscription(clientProviderFactory.create(), routingUrl);
    }

}
