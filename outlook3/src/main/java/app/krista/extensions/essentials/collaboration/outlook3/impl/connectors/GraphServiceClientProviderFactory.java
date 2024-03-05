package app.krista.extensions.essentials.collaboration.outlook3.impl.connectors;

import app.krista.extensions.essentials.collaboration.outlook3.OutlookAttributes;
import app.krista.extensions.essentials.collaboration.outlook3.impl.stores.OutlookAttributeStore;
import app.krista.extensions.essentials.collaboration.outlook3.impl.stores.RefreshTokenStore;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.ksdk.context.RequestContext;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;

@Service
public class GraphServiceClientProviderFactory {

    private final RefreshTokenStore refreshTokenStore;
    private final OutlookAttributeStore outlookAttributeStore;
    private final RequestContext requestContext;
    private final AuthorizationContext authorizationContext;
    private final GraphServiceClientProvider defaultClientProvider;

    @Inject
    public GraphServiceClientProviderFactory(RefreshTokenStore refreshTokenStore, OutlookAttributeStore outlookAttributeStore, OutlookAttributes outlookAttributes, RequestContext requestContext, AuthorizationContext authorizationContext) {
        this(refreshTokenStore, outlookAttributeStore, requestContext, authorizationContext,
                new GraphServiceClientProvider(refreshTokenStore, outlookAttributes, requestContext, authorizationContext, null));
    }

    public GraphServiceClientProviderFactory(RefreshTokenStore refreshTokenStore, OutlookAttributeStore outlookAttributeStore, RequestContext requestContext, AuthorizationContext authorizationContext, GraphServiceClientProvider defaultClientProvider) {
        this.refreshTokenStore = refreshTokenStore;
        this.outlookAttributeStore = outlookAttributeStore;
        this.requestContext = requestContext;
        this.authorizationContext = authorizationContext;
        this.defaultClientProvider = defaultClientProvider;
    }

    public GraphServiceClientProvider create(OutlookAttributes outlookAttributes) {
        String authContextId = outlookAttributeStore.save(outlookAttributes);
        return new GraphServiceClientProvider(refreshTokenStore, outlookAttributes, requestContext, authorizationContext, authContextId);
    }

    public GraphServiceClientProvider create() {
        return defaultClientProvider;
    }

}
