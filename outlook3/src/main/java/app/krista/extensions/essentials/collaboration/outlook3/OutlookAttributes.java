package app.krista.extensions.essentials.collaboration.outlook3;

import app.krista.extension.executor.Invoker;
import app.krista.extension.request.RoutingInfo;
import app.krista.extension.request.protos.http.HttpProtocol;
import app.krista.extension.util.InvokerAttributes;
import app.krista.extensions.essentials.collaboration.outlook3.impl.util.Constants;
import com.github.scribejava.apis.MicrosoftAzureActiveDirectory20Api;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.gson.JsonObject;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static app.krista.extensions.essentials.collaboration.outlook3.impl.util.Constants.GSON;

@Service
public class OutlookAttributes {

    public static final String CLIENT_ID = "clientId";
    public static final String CLIENT_SECRET = "clientSecret";
    public static final String TENANT_ID = "tenantId";
    public static final String EMAIL = "email";
    public static final String ALLOW_ALERT_MAIL = "allowAlertMail";
    public static final String DEFAULT_CALLBACK_PATH = "/extension/api/rest/v3/oauth/callback";
    public static final String SPECIFIC_CALLBACK_PATH = "/rest/outlook/callback";
    public static final Logger LOGGER = LoggerFactory.getLogger(OutlookAttributes.class);
    public static final String LOGIN_WITH_MICROSOFT = "loginWithMicrosoft";
    private static final String CONFIG_FILE = "microsoft-config.json";
    public static final String LOCAL_EXTN_URL = "https://extension.local.eng.krista.app";
    public static final String LOCAL_EXTN_REPLACE_URL = "http://localhost:8765";
    private final String routingUrl;
    private final String routingId;
    private Boolean loginWithMicrosoft;
    private String clientId;
    private String clientSecret;
    private String mailId;
    private Boolean allowAlertMail;
    private String tenantId;
    private OAuth20Service oAuth20Service;


    @Inject
    public OutlookAttributes(Invoker invoker) {
        this(invoker.getRoutingInfo().getRoutingURL(HttpProtocol.PROTOCOL_NAME, RoutingInfo.Type.APPLIANCE),
                invoker.getAttributes(), invoker.getRoutingInfo().getRoutingId());
    }

    public OutlookAttributes(String routingUrl, Map<String, Object> attributes, String routingId) {
        this(routingUrl, InvokerAttributes.getStringOrNull(attributes, CLIENT_ID),
                InvokerAttributes.getStringOrNull(attributes, CLIENT_SECRET),
                InvokerAttributes.getStringOrNull(attributes, EMAIL),
                InvokerAttributes.getBooleanOrDefault(attributes, ALLOW_ALERT_MAIL, false),
                InvokerAttributes.getStringOrNull(attributes, TENANT_ID),
                routingId,
                InvokerAttributes.getBooleanOrDefault(attributes, LOGIN_WITH_MICROSOFT, false));
    }

    public OutlookAttributes(String routingUrl, String clientId, String clientSecret, String mailId,
                             boolean allowAlertMail, String tenantId, String routingId, boolean loginWithMicrosoft) {
        this.routingUrl = routingUrl;
        this.clientId = clientId != null ? clientId : getAttributeValueFromFile(CLIENT_ID);
        this.clientSecret = clientSecret != null ? clientSecret : getAttributeValueFromFile(CLIENT_SECRET);
        this.mailId = mailId;
        this.allowAlertMail = allowAlertMail;
        this.tenantId = tenantId;
        this.routingId = routingId;
        this.loginWithMicrosoft = loginWithMicrosoft;
    }

    public static Map<String, Object> getAttributesMap(OutlookAttributes outlookAttributes) {
        Map<String, Object> attributeMap = new HashMap<>();
        addAttributeIfNotNull(attributeMap, OutlookAttributes.CLIENT_ID, outlookAttributes.getClientId());
        addAttributeIfNotNull(attributeMap, OutlookAttributes.CLIENT_SECRET, outlookAttributes.getClientSecret());
        addAttributeIfNotNull(attributeMap, OutlookAttributes.TENANT_ID, outlookAttributes.getTenantId());
        addAttributeIfNotNull(attributeMap, OutlookAttributes.EMAIL, outlookAttributes.getMailId());
        addAttributeIfNotNull(attributeMap, OutlookAttributes.ALLOW_ALERT_MAIL, outlookAttributes.isAllowAlertMail());
        addAttributeIfNotNull(attributeMap, OutlookAttributes.LOGIN_WITH_MICROSOFT, outlookAttributes.isLoginWithMicrosoft());
        return attributeMap;
    }

    private static void addAttributeIfNotNull(Map<String, Object> attributeMap, String attributeName, Object attributeValue) {
        if (attributeValue != null) {
            attributeMap.put(attributeName, attributeValue);
        }
    }

    public static OutlookAttributes create(String routingUrl, Map<String, Object> attributes, String routingId) {
        return new OutlookAttributes(routingUrl, attributes, routingId);
    }

    public static String convertToString(InputStream inputStream) throws IOException {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
            return bufferedReader.lines().collect(Collectors.joining("\n"));
        }
    }

    public static String getAttributeValueFromFile(String key) {
        try (InputStream resourceAsStream = OutlookAttributes.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            String configString = convertToString(resourceAsStream);
            JsonObject configObject = GSON.fromJson(configString, JsonObject.class);
            return configObject.get("credentials").getAsJsonObject().get(key).getAsString();
        } catch (IOException e) {
            LOGGER.info("{} value not found in the credentials config file.", key);
            return null;
        }
    }

    public void update(Map<String, Object> newAttributes) {
        loginWithMicrosoft = InvokerAttributes.getBooleanOrDefault(newAttributes, LOGIN_WITH_MICROSOFT, false);
        if (loginWithMicrosoft) {
            clientId = getAttributeValueFromFile(CLIENT_ID);
            clientSecret = getAttributeValueFromFile(CLIENT_SECRET);
        } else {
            clientId = InvokerAttributes.getStringOrNull(newAttributes, CLIENT_ID);
            clientSecret = InvokerAttributes.getStringOrNull(newAttributes, CLIENT_SECRET);
            tenantId = InvokerAttributes.getStringOrNull(newAttributes, TENANT_ID);
        }
        mailId = InvokerAttributes.getStringOrNull(newAttributes, EMAIL);
        allowAlertMail = InvokerAttributes.getBooleanOrDefault(newAttributes, ALLOW_ALERT_MAIL, false);
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getMailId() {
        return mailId;
    }

    public boolean isAllowAlertMail() {
        return allowAlertMail != null && allowAlertMail;
    }

    public boolean isLoginWithMicrosoft() {
        return loginWithMicrosoft != null && loginWithMicrosoft;
    }

    public String getDefaultCallbackUrl() {
        return routingUrl
                .replace(LOCAL_EXTN_URL, LOCAL_EXTN_REPLACE_URL)
                .replaceAll("/extension/api/.*", DEFAULT_CALLBACK_PATH);
    }

    public String getSpecificCallbackUrl() {
        return routingUrl
                .replace(LOCAL_EXTN_URL, LOCAL_EXTN_REPLACE_URL) +
                SPECIFIC_CALLBACK_PATH;
    }

    public String getForwardUrl() {
        return routingUrl
                .replace(LOCAL_EXTN_URL, LOCAL_EXTN_REPLACE_URL)
                + Constants.EXTENSION_FORWARD_PATH;
    }

    public String getRoutingId() {
        return routingId;
    }

    public synchronized OAuth20Service getOAuth20Service() {
        if (oAuth20Service != null) {
            oAuth20Service = new ServiceBuilder(clientId)
                    .apiSecret(clientSecret)
                    .defaultScope(Constants.REQUIRED_SCOPE)
                    .callback(loginWithMicrosoft ? getDefaultCallbackUrl() : getSpecificCallbackUrl())
                    .build(loginWithMicrosoft ? MicrosoftAzureActiveDirectory20Api.instance()
                            : MicrosoftAzureActiveDirectory20Api.custom(tenantId));

        }
        return oAuth20Service;
    }
}
