package app.krista.extensions.essentials.collaboration.outlook3.impl.stores;

import app.krista.extensions.util.KeyValueStore;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;

@Service
public final class RefreshTokenStore {

    private final KeyValueStore keyValueStore;

    @Inject
    public RefreshTokenStore(KeyValueStore keyValueStore) {
        this.keyValueStore = keyValueStore;
    }

    public void put(String key, String refToken) {
        keyValueStore.put(key, refToken);
    }

    public String get(String key) {
        return keyValueStore.get(key, String.class);
    }

    public void remove(String key) {
        keyValueStore.remove(key);
    }

}
