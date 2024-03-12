package app.krista.extensions.essentials.collaboration.outlook3;

import app.krista.extension.authorization.RequestAuthenticator;
import app.krista.extension.executor.Invoker;
import app.krista.extension.impl.anno.Extension;
import app.krista.extension.impl.anno.InvokerRequest;
import app.krista.extension.impl.anno.StaticResource;
import app.krista.extensions.essentials.collaboration.outlook3.impl.stores.OutlookAttributeStore;

import javax.inject.Inject;
import java.util.Map;

@Extension(version = "3.0.0-rc1")
@StaticResource(path = "docs", file = "docs")
public class OutlookExtension {

    private final OutlookRequestAuthenticator requestAuthenticator;

    @Inject
    public OutlookExtension(Invoker invoker, OutlookAttributeStore attributeStore) {
        this.requestAuthenticator = new OutlookRequestAuthenticator(attributeStore, invoker.getInvokerId());
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
        throw new IllegalArgumentException("Please authorize in authentication tab.");
    }

    // TODO: 12/03/24 Need to implement delete subscription here.
    @InvokerRequest(InvokerRequest.Type.INVOKER_REMOVED)
    public void invokerRemoved() {
//        MailSubscription.deleteSubscription(clientProviderFactory.create(), routingUrl);
    }

}
