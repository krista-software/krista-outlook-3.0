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

import static app.krista.extensions.essentials.collaboration.outlook3.impl.util.Constants.*;

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
        String authType = attributes.get(AUTH_TYPE).toString();
        String baseUrl = invoker.getRoutingInfo().getRoutingURL(HttpProtocol.PROTOCOL_NAME, RoutingInfo.Type.APPLIANCE);
        if (Constants.PUBLIC.equals(authType)) {
            return new OutlookAttributes((String) attributes.get(CLIENT_ID), (String) attributes.get(CLIENT_SECRET),
                    null, (String) attributes.get(EMAIL),
                    (boolean) attributes.get(ALLOW_MAIL_ALERT), authType, baseUrl);
        } else {
            return new OutlookAttributes((String) attributes.get(CLIENT_ID), (String) attributes.get(CLIENT_SECRET),
                    (String) attributes.get(Constants.TENANT_ID), (String) attributes.get(EMAIL),
                    (boolean) attributes.get(ALLOW_MAIL_ALERT), authType, baseUrl);
        }
    }

    public String save(OutlookAttributes attributes) {
        if (attributes == null) {
            throw new IllegalArgumentException("Outlook Attributes cannot be null");
        }

        String authContextId = UUID.randomUUID().toString();
        Map<String, Object> attributeMap = attributes.toMap();
        store.put(authContextId, Constants.GSON.toJson(attributeMap));
        return authContextId;
    }

    public boolean save(OutlookAttributes attributes, String invokerId) {
        if (attributes == null) {
            throw new IllegalArgumentException("Outlook Attributes cannot be null");
        }
        Map<String, Object> attributeMap = attributes.toMap();
        store.put(invokerId, Constants.GSON.toJson(attributeMap));
        return true;
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
