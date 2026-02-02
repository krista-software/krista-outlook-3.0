package app.krista.extensions.essentials.collaboration.outlook3;

import app.krista.extension.authorization.RequestAuthenticator;
import app.krista.extension.executor.Invoker;
import app.krista.extension.impl.anno.Extension;
import app.krista.extension.impl.anno.InvokerRequest;
import app.krista.extension.impl.anno.Java;
import app.krista.extension.impl.anno.StaticResource;
import app.krista.extension.request.RoutingInfo;
import app.krista.extension.request.protos.http.HttpProtocol;
import app.krista.extensions.essentials.collaboration.outlook3.impl.MailSubscription;
import app.krista.extensions.essentials.collaboration.outlook3.impl.connectors.GraphServiceClientProviderFactory;
import app.krista.extensions.essentials.collaboration.outlook3.impl.stores.OutlookAttributeStore;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.ksdk.telemetry.TelemetryMetrics;

import javax.inject.Inject;
import java.util.Map;

@Java(version = Java.Version.JAVA_21)
@Extension(version = "3.0.29", name = "Outlook")
@StaticResource(path = "docs", file = "docs")
public class OutlookExtension {

    private final OutlookRequestAuthenticator requestAuthenticator;
    private final GraphServiceClientProviderFactory providerFactory;
    private final String routingUrl;
    private final TelemetryMetrics telemetryMetrics;

    @Inject
    public OutlookExtension(Invoker invoker, OutlookAttributeStore attributeStore, GraphServiceClientProviderFactory providerFactory,
                            AuthorizationContext authorizationContext, TelemetryMetrics telemetryMetrics) {
        this.requestAuthenticator = new OutlookRequestAuthenticator(attributeStore, invoker.getInvokerId(), authorizationContext);
        this.providerFactory = providerFactory;
        this.routingUrl = invoker.getRoutingInfo().getRoutingURL(HttpProtocol.PROTOCOL_NAME, RoutingInfo.Type.APPLIANCE);
        this.telemetryMetrics = telemetryMetrics;
    }

    @InvokerRequest(InvokerRequest.Type.AUTHENTICATOR)
    public RequestAuthenticator getRequestAuthenticator() {
        return requestAuthenticator;
    }

    @InvokerRequest(InvokerRequest.Type.CUSTOM_TABS)
    public Map<String, String> customTabs() {
        telemetryMetrics.incrementCounter("outlook3.custom_tabs.opened", 1, Map.of(
                "tab", "Documentation",
                "action", "open"
        ));
        return Map.of("Authentication", "rest/outlook/docs/", "Documentation", "static/docs");
    }

    @InvokerRequest(InvokerRequest.Type.TEST_CONNECTION)
    public void testConnection() {
        throw new IllegalArgumentException("To test your connection, please navigate to the 'Authentication' tab and click the 'Authorize' button to verify your Microsoft account credentials.");
    }

    @InvokerRequest(InvokerRequest.Type.INVOKER_REMOVED)
    public void invokerRemoved() {
        MailSubscription.deleteSubscription(routingUrl, providerFactory.create());
    }

    // This functionality is not yet ready for production deployment
    /*
    @InvokerRequest(InvokerRequest.Type.PREPARE_CHANGE_ROUTING_ID)
    public void prepareChangeRoutingId(String newRoutingId)  {
        MailSubscription.deleteSubscription(routingUrl, providerFactory.create());
        MailSubscription.createOrUpdateSubscription(routingUrl, providerFactory.create());
    }
    */
}
