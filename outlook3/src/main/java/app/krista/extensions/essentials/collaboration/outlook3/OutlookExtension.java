package app.krista.extensions.essentials.collaboration.outlook3;

import app.krista.extension.authorization.RequestAuthenticator;
import app.krista.extension.executor.Invoker;
import app.krista.extension.impl.anno.Extension;
import app.krista.extension.impl.anno.InvokerRequest;
import app.krista.extension.impl.anno.StaticResource;
import app.krista.extension.request.RoutingInfo;
import app.krista.extension.request.protos.http.HttpProtocol;
import app.krista.extensions.essentials.collaboration.outlook3.impl.MailSubscription;
import app.krista.extensions.essentials.collaboration.outlook3.impl.connectors.GraphServiceClientProviderFactory;
import app.krista.extensions.essentials.collaboration.outlook3.impl.stores.OutlookAttributeStore;

import javax.inject.Inject;
import java.util.Map;

@Extension(version = "3.0.1-rc1")
@StaticResource(path = "docs", file = "docs")
public class OutlookExtension {

    private final OutlookRequestAuthenticator requestAuthenticator;
    private final GraphServiceClientProviderFactory providerFactory;
    private final String routingUrl;

    @Inject
    public OutlookExtension(Invoker invoker, OutlookAttributeStore attributeStore, GraphServiceClientProviderFactory providerFactory) {
        this.requestAuthenticator = new OutlookRequestAuthenticator(attributeStore, invoker.getInvokerId());
        this.providerFactory = providerFactory;
        this.routingUrl = invoker.getRoutingInfo().getRoutingURL(HttpProtocol.PROTOCOL_NAME, RoutingInfo.Type.APPLIANCE);
    }

    @InvokerRequest(InvokerRequest.Type.AUTHENTICATOR)
    public RequestAuthenticator getRequestAuthenticator() {
        return requestAuthenticator;
    }

    @InvokerRequest(InvokerRequest.Type.CUSTOM_TABS)
    public Map<String, String> customTabs() {
        return Map.of("Authentication", "rest/outlook/docs/", "Documentation", "static/docs");
    }

    @InvokerRequest(InvokerRequest.Type.TEST_CONNECTION)
    public void testConnection() {
        throw new IllegalArgumentException("Please use Authentication tab for test connection.");
    }

    @InvokerRequest(InvokerRequest.Type.INVOKER_REMOVED)
    public void invokerRemoved() {
        MailSubscription.deleteSubscription(routingUrl, providerFactory.create());
    }

}
