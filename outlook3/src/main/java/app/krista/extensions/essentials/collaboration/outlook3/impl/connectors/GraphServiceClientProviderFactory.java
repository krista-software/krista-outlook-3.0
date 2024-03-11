package app.krista.extensions.essentials.collaboration.outlook3.impl.connectors;

import app.krista.extensions.essentials.collaboration.outlook3.OutlookAttributes;
import app.krista.extensions.essentials.collaboration.outlook3.OutlookExtension;
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

    @Inject
    public GraphServiceClientProviderFactory(RefreshTokenStore refreshTokenStore, OutlookAttributeStore outlookAttributeStore,
                                             RequestContext requestContext, AuthorizationContext authorizationContext) {
        this.refreshTokenStore = refreshTokenStore;
        this.outlookAttributeStore = outlookAttributeStore;
        this.requestContext = requestContext;
        this.authorizationContext = authorizationContext;
    }

    public GraphServiceClientProvider create(OutlookAttributes attributes) {
        outlookAttributeStore.save(attributes, requestContext.getInvokerId());
        return new GraphServiceClientProvider(refreshTokenStore, attributes, requestContext, authorizationContext);
    }

    public GraphServiceClientProvider create() {
        String key = requestContext.getInvokerId();
        System.out.println("Invoker Id: " + key);
        OutlookAttributes attributes = outlookAttributeStore.load(key);
        return new GraphServiceClientProvider(refreshTokenStore, attributes, requestContext, authorizationContext);
    }

}
