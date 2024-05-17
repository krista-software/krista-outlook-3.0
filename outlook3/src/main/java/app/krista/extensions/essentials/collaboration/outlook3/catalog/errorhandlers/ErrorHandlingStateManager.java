package app.krista.extensions.essentials.collaboration.outlook3.catalog.errorhandlers;

import org.jvnet.hk2.annotations.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ErrorHandlingStateManager {

    private static final Map<String, Object> internalState = new HashMap<>();

    public ErrorHandlingStateManager() {

    }

    public void put(String key, Object value) {
        internalState.put(key, value);
    }

    public Object get(String key) {
        return internalState.get(key);
    }
}
