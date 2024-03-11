package app.krista.extensions.essentials.collaboration.outlook3.impl;

import app.krista.extensions.essentials.collaboration.outlook3.OutlookAttributes;
import app.krista.extensions.essentials.collaboration.outlook3.impl.util.Constants;
import com.github.scribejava.apis.MicrosoftAzureActiveDirectory20Api;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.oauth.OAuth20Service;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;

import static app.krista.extensions.essentials.collaboration.outlook3.impl.util.Constants.PUBLIC;

@Service
public class OAuthService {

    private final OutlookAttributes attributes;
    private OAuth20Service oAuth20Service;

    @Inject
    public OAuthService(OutlookAttributes attributes) {
        this.attributes = attributes;
    }

    public synchronized OAuth20Service getOAuth20Service() {
        if (oAuth20Service == null) {
            oAuth20Service = new ServiceBuilder(attributes.getClientId())
                    .apiSecret(attributes.getClientSecret())
                    .defaultScope(Constants.REQUIRED_SCOPE)
                    .callback(attributes.getCallbackEndPoint())
                    .build(attributes.getAuthType().equals(PUBLIC) ? MicrosoftAzureActiveDirectory20Api.instance()
                            : MicrosoftAzureActiveDirectory20Api.custom(attributes.getTenantId()));
        }
        return oAuth20Service;
    }
}
