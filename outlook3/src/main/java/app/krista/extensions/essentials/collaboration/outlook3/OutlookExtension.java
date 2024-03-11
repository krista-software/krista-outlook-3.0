package app.krista.extensions.essentials.collaboration.outlook3;

import app.krista.extension.authorization.RequestAuthenticator;
import app.krista.extension.executor.Invoker;
import app.krista.extension.impl.anno.Extension;
import app.krista.extension.impl.anno.Field;
import app.krista.extension.impl.anno.InvokerRequest;
import app.krista.extension.impl.anno.StaticResource;
import app.krista.extension.request.RoutingInfo;
import app.krista.extension.request.protos.http.HttpProtocol;
//import app.krista.extensions.essentials.collaboration.outlook3.impl.MailSubscription;
import app.krista.extensions.essentials.collaboration.outlook3.impl.connectors.GraphServiceClientProviderFactory;
import app.krista.extensions.essentials.collaboration.outlook3.impl.stores.OutlookAttributeStore;
import app.krista.extensions.essentials.collaboration.outlook3.impl.util.Validators;

import javax.inject.Inject;
import java.util.Map;

//@Field.Boolean(value = Attributes.LOGIN_WITH_MICROSOFT, required = false)
//@Field.Text(value = Attributes.EMAIL)
//@Field.Text(value = Attributes.CLIENT_ID, required = false)
//@Field.Text(value = Attributes.CLIENT_SECRET, isSecured = true, required = false)
//@Field.Text(value = Attributes.TENANT_ID, required = false)
//@Field.Boolean(value = Attributes.ALLOW_ALERT_MAIL, required = false)
@Extension(version = "3.0.0-rc1")
@StaticResource(path = "docs", file = "docs")
public class OutlookExtension {

//    private final String routingUrl;
//    private final Attributes attributes;
//    private final GraphServiceClientProviderFactory clientProviderFactory;
    private final OutlookRequestAuthenticator requestAuthenticator;

//    @Inject
//    public OutlookExtension(Invoker invoker, Attributes attributes, GraphServiceClientProviderFactory clientProviderFactory, OutlookAttributeStore outlookAttributeStore) {
//        this(invoker, attributes,
//                clientProviderFactory,
//                new OutlookRequestAuthenticator(outlookAttributeStore, attributes));
//    }

//    public OutlookExtension(Invoker invoker, Attributes attributes, GraphServiceClientProviderFactory clientProviderFactory, OutlookRequestAuthenticator requestAuthenticator) {
//        this.routingUrl = invoker.getRoutingInfo().getRoutingURL(HttpProtocol.PROTOCOL_NAME, RoutingInfo.Type.APPLIANCE);
//        this.attributes = attributes;
//        this.clientProviderFactory = clientProviderFactory;
//        this.requestAuthenticator = requestAuthenticator;
//    }
    @Inject
    public OutlookExtension(Invoker invoker, OutlookAttributeStore attributeStore) {
        this.requestAuthenticator = new OutlookRequestAuthenticator(attributeStore);
    }


    @InvokerRequest(InvokerRequest.Type.AUTHENTICATOR)
    public RequestAuthenticator getRequestAuthenticator() {
        return requestAuthenticator;
    }

    @InvokerRequest(InvokerRequest.Type.CUSTOM_TABS)
    public Map<String, String> customTabs() {
        return Map.of("Authentication", "rest/outlook/docs/", "Documentation", "static/docs");
    }

//    @InvokerRequest(InvokerRequest.Type.VALIDATE_ATTRIBUTES)
//    public void validateConnection(Map<String, Object> connectionAttributes) {
//        Attributes attributes = Attributes.create(routingUrl, connectionAttributes);
//        if (attributes.isLoginWithMicrosoft()) {
//            if (Validators.isAnyParameterValuePresent(connectionAttributes)) {
//                throw new IllegalArgumentException("Only Mail id is required to login with Microsoft.");
//            }
//        } else {
//            if (Validators.areAllCredentialsBlank(connectionAttributes)) {
//                throw new IllegalArgumentException("Please provide all the parameter values.");
//            }
//        }
//        testConnection(attributes);
//        if (attributes.isAllowAlertMail()) {
//            MailSubscription.createSubscription(routingUrl, clientProviderFactory.create(attributes));
//        } else {
//            MailSubscription.deleteSubscription(clientProviderFactory.create(attributes), routingUrl);
//        }
//    }

//    @InvokerRequest(InvokerRequest.Type.TEST_CONNECTION)
//    public void testConnection() {
//        return;
//        testConnection(attributes);
//        if (attributes.isAllowAlertMail()) {
//            MailSubscription.createSubscription(routingUrl, clientProviderFactory.create());
//        } else {
//            MailSubscription.deleteSubscription(clientProviderFactory.create(), routingUrl);
//        }
//    }

//    private void testConnection(Attributes attributes) {
//        if (attributes.getMailId() != null && !attributes.getMailId().isBlank()) {
//            clientProviderFactory.create(attributes).getGraphServiceClientForAdmin().me().mailFolders().buildRequest()
//                    .get();
//        }
//    }

//    @InvokerRequest(InvokerRequest.Type.INVOKER_UPDATED)
//    public void attributesUpdated(Map<String, Object> oldAttributes, Map<String, Object> newAttributes) {
//        attributes.update(newAttributes);
//    }

//    @InvokerRequest(InvokerRequest.Type.INVOKER_REMOVED)
//    public void invokerRemoved() {
//        MailSubscription.deleteSubscription(clientProviderFactory.create(), routingUrl);
//    }

}
