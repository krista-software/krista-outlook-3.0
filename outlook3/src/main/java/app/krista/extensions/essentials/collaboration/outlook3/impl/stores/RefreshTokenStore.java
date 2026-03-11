/*
 * Outlook 3.0 Extension for Krista
 * Copyright (C) 2025 Krista Software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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

    public Object getDeltaLink(String key) {
        return keyValueStore.get(key);
    }

    public void remove(String key) {
        keyValueStore.remove(key);
    }

}
