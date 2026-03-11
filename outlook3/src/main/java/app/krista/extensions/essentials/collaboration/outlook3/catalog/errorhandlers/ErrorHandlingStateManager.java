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
