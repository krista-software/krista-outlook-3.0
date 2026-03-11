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

package app.krista.extensions.essentials.collaboration.outlook3.impl.connectors;

import app.krista.extensions.essentials.collaboration.outlook3.OutlookAttributes;
import app.krista.extensions.essentials.collaboration.outlook3.impl.stores.OutlookAttributeStore;
import app.krista.extensions.essentials.collaboration.outlook3.impl.stores.RefreshTokenStore;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.ksdk.context.RequestContext;
import org.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;

@Service
public class GraphServiceClientProviderFactory {

    private final RefreshTokenStore refreshTokenStore;
    private final OutlookAttributeStore outlookAttributeStore;
    private final RequestContext requestContext;
    private final AuthorizationContext authorizationContext;

    @Inject
    public GraphServiceClientProviderFactory(RefreshTokenStore refreshTokenStore,
                                             OutlookAttributeStore outlookAttributeStore,
                                             RequestContext requestContext,
                                             AuthorizationContext authorizationContext) {
        this.refreshTokenStore = refreshTokenStore;
        this.outlookAttributeStore = outlookAttributeStore;
        this.requestContext = requestContext;
        this.authorizationContext = authorizationContext;
    }

    public String createAttributes(OutlookAttributes attributes) {
        return outlookAttributeStore.save(attributes);
    }

    @NotNull
    private GraphServiceClientProvider getGraphServiceClientProvider(OutlookAttributes attributes,
                                                                     String authContextId) {
        return new GraphServiceClientProvider(refreshTokenStore, attributes, requestContext, authorizationContext,
                authContextId);
    }

    public GraphServiceClientProvider create(String authContextId) {
        final OutlookAttributes attributes = outlookAttributeStore.load(authContextId);
        return getGraphServiceClientProvider(attributes, authContextId);
    }

    public GraphServiceClientProvider create() {
        return create(requestContext);
    }

    public GraphServiceClientProvider create(RequestContext context) {
        String key = context.getInvokerId();
        OutlookAttributes attributes = outlookAttributeStore.load(key);
        return getGraphServiceClientProvider(attributes, null);
    }

}
