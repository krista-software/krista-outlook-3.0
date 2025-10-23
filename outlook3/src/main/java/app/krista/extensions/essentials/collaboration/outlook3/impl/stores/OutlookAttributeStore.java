package app.krista.extensions.essentials.collaboration.outlook3.impl.stores;

import app.krista.extension.executor.Invoker;
import app.krista.extension.request.RoutingInfo;
import app.krista.extension.request.protos.http.HttpProtocol;
import app.krista.extensions.essentials.collaboration.outlook3.OutlookAttributes;
import app.krista.extensions.essentials.collaboration.outlook3.impl.util.Constants;
import app.krista.extensions.util.KeyValueStore;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

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

    /**
     * Loads Outlook attributes from the store using the provided key.
     * <p>
     * This method retrieves the serialized attributes from the key-value store,
     * deserializes them, and constructs an {@link OutlookAttributes} object based
     * on the authentication type (public or organizational).
     *
     * @param key The key used to retrieve the attributes from the store
     * @return The deserialized OutlookAttributes object, or null if not found or empty
     */
    @SuppressWarnings("unchecked")
    public OutlookAttributes load(String key) {
        String loadedAttributes = null;
        if (store.get(key) != null) {
            loadedAttributes = (String) store.get(key);
        }
        if (loadedAttributes == null || loadedAttributes.isBlank()) {
            return null;
        } else {
            Map<String, Object> attributes = GSON.fromJson(loadedAttributes, Map.class);
            String authType = attributes.get(AUTH_TYPE).toString();
            String baseUrl = invoker.getRoutingInfo().getRoutingURL(HttpProtocol.PROTOCOL_NAME, RoutingInfo.Type.APPLIANCE);

            // Extract monitored folders if present
            List<String> monitoredFolders = new ArrayList<>();
            if (attributes.containsKey(MONITORED_FOLDERS) && attributes.get(MONITORED_FOLDERS) instanceof List) {
                monitoredFolders = ((List<?>) attributes.get(MONITORED_FOLDERS))
                        .stream()
                        .map(Object::toString)
                        .collect(Collectors.toList());
            }

            if (Constants.PUBLIC.equals(authType)) {
                return new OutlookAttributes((String) attributes.get(CLIENT_ID), (String) attributes.get(CLIENT_SECRET),
                        null, (String) attributes.get(EMAIL), (boolean) attributes.get(ALLOW_MAIL_ALERT), authType,
                        baseUrl, monitoredFolders);
            } else {
                return new OutlookAttributes((String) attributes.get(CLIENT_ID), (String) attributes.get(CLIENT_SECRET),
                        (String) attributes.get(Constants.TENANT_ID), (String) attributes.get(EMAIL),
                        (boolean) attributes.get(ALLOW_MAIL_ALERT), authType, baseUrl, monitoredFolders);
            }
        }
    }

    /**
     * Saves Outlook attributes to the store with a generated unique identifier.
     * <p>
     * This method serializes the attributes to JSON and stores them in the key-value store
     * using a generated unique identifier as the key.
     *
     * @param attributes The OutlookAttributes to save
     * @return The generated unique identifier (authContextId) used as the key
     * @throws IllegalArgumentException if attributes is null
     */
    public String save(OutlookAttributes attributes) {
        if (attributes == null) {
            throw new IllegalArgumentException("Outlook Attributes cannot be null");
        }
        String authContextId = Constants.AUTH_CONTEXT_ID + Constants.UNDER_SCORE + UUID.randomUUID();
        Map<String, Object> attributeMap = attributes.toMap();
        store.put(authContextId, Constants.GSON.toJson(attributeMap));
        return authContextId;
    }

    /**
     * Saves Outlook attributes to the store using the provided invoker ID as the key.
     * <p>
     * This method serializes the attributes to JSON and stores them in the key-value store
     * using the specified invoker ID as the key.
     *
     * @param attributes The OutlookAttributes to save
     * @param invokerId  The invoker ID to use as the key
     * @return true if the save operation was successful
     * @throws IllegalArgumentException if attributes is null
     */
    public boolean save(OutlookAttributes attributes, String invokerId) {
        if (attributes == null) {
            throw new IllegalArgumentException("Outlook Attributes cannot be null");
        }
        Map<String, Object> attributeMap = attributes.toMap();
        store.put(invokerId, Constants.GSON.toJson(attributeMap));
        return true;
    }

    /**
     * Removes the outlook attributes for given authContext id from KeyValueStore
     *
     * @param authContextId authContext id
     */
    public void remove(String authContextId) {
        store.remove(authContextId);
    }

}
