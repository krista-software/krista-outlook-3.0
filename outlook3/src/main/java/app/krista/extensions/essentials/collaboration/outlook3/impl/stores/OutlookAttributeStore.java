package app.krista.extensions.essentials.collaboration.outlook3.impl.stores;

import app.krista.extension.executor.Invoker;
import app.krista.extension.request.RoutingInfo;
import app.krista.extension.request.protos.http.HttpProtocol;
import app.krista.extensions.essentials.collaboration.outlook3.OutlookAttributes;
import app.krista.extensions.essentials.collaboration.outlook3.impl.util.Constants;
import app.krista.extensions.util.KeyValueStore;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import java.util.Map;
import java.util.UUID;

@Service
public class OutlookAttributeStore {

    private final Invoker invoker;
    private final KeyValueStore store;

    @Inject
    public OutlookAttributeStore(Invoker invoker, KeyValueStore store) {
        this.invoker = invoker;
        this.store = store;
    }

    public OutlookAttributes load(String authContextId) {
        Map attributes = Constants.GSON.fromJson(String.valueOf(store.get(authContextId)), Map.class);
        return new OutlookAttributes(invoker.getRoutingInfo().getRoutingURL(HttpProtocol.PROTOCOL_NAME, RoutingInfo.Type.APPLIANCE),
                (String) attributes.get(OutlookAttributes.CLIENT_ID),
                (String) attributes.get(OutlookAttributes.CLIENT_SECRET),
                (String) attributes.get(OutlookAttributes.EMAIL),
                (Boolean) attributes.get(OutlookAttributes.ALLOW_ALERT_MAIL),
                (String) attributes.get(OutlookAttributes.TENANT_ID),
                (Boolean) attributes.get(OutlookAttributes.LOGIN_WITH_MICROSOFT));
    }

    public String save(OutlookAttributes outlookAttributes) {
        if (outlookAttributes == null) {
            throw new IllegalArgumentException("Outlook Attributes cannot be null");
        }

        String authContextId = UUID.randomUUID().toString();
        Map<String, Object> attributeMap = OutlookAttributes.getAttributesMap(outlookAttributes);
        store.put(authContextId, Constants.GSON.toJson(attributeMap));
        return authContextId;
    }

    /**
     * Removes given authContext id from KeyValueStore
     *
     * @param authContextId authContext id
     */
    public void remove(String authContextId) {
        store.remove(authContextId);
    }

}
