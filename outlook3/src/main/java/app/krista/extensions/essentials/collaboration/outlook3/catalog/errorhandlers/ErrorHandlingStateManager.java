package app.krista.extensions.essentials.collaboration.outlook3.catalog.errorhandlers;

import app.krista.extensions.util.KeyValueStore;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

import static app.krista.extensions.essentials.collaboration.outlook3.impl.util.Constants.GSON;

@Service
public class ErrorHandlingStateManager {

    private final KeyValueStore internalState;
    private static final Map<String, Object> internalState2 = new HashMap<>();

    @Inject
    public ErrorHandlingStateManager(KeyValueStore internalState) {
        this.internalState = internalState;

    }

    public void put(String key, Object value) {
        internalState.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> get(String key) {
        final String map = String.valueOf(internalState.get(key));
        return GSON.fromJson(map, Map.class);
    }

    public void putMetaInfo(String key, Object value) {
        internalState2.put(key, value);
    }

    public Object getMetaInfo(String key) {
        return internalState2.get(key);
    }
}
