package app.krista.extensions.essentials.collaboration.outlook3;

import app.krista.extensions.essentials.collaboration.outlook3.impl.util.Constants;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static app.krista.extensions.essentials.collaboration.outlook3.impl.util.Constants.*;
import static app.krista.extensions.essentials.collaboration.outlook3.impl.util.Validators.addAttributeIfNotNull;

@Service
public final class OutlookAttributes {

    @SerializedName(EMAIL)
    private final String email;

    @SerializedName(ALLOW_MAIL_ALERT)
    private final boolean allowMailAlert;

    @SerializedName(CLIENT_ID)
    private final String clientId;

    @SerializedName(CLIENT_SECRET)
    private final String clientSecret;

    private final String authType;
    @SerializedName(TENANT_ID)
    private final String tenantId;
    private final String baseUrl;
    private String publicClientId;
    private String publicClientSecret;


    @Inject
    public OutlookAttributes(String clientId, String clientSecret, String tenantId, String email, boolean allowMailAlert,
                             String authType, String baseUrl) {
        loadPublicConfig();
        this.clientId = clientId == null ? publicClientId : clientId;
        this.clientSecret = clientSecret == null ? publicClientSecret : clientSecret;
        this.tenantId = tenantId;
        this.email = email;
        this.allowMailAlert = allowMailAlert;
        this.authType = authType;
        this.baseUrl = baseUrl.replace(LOCAL_EXTN_URL, LOCAL_EXTN_REPLACE_URL);
    }

    public static OutlookAttributes create(JsonObject authPayload, String baseurl) {
        String authType = authPayload.get("authType").getAsString();
        if (Constants.PUBLIC.equals(authType)) {
            return new OutlookAttributes(null, null, null, authPayload.get(EMAIL).getAsString(),
                    authPayload.get(ALLOW_MAIL_ALERT).getAsBoolean(), authType, baseurl);
        } else if (Constants.PRIVATE.equals(authType)) {
            return new OutlookAttributes(authPayload.get(CLIENT_ID).getAsString(), authPayload.get(CLIENT_SECRET).getAsString(),
                    authPayload.get(TENANT_ID).getAsString(), authPayload.get(EMAIL).getAsString(), authPayload.get(ALLOW_MAIL_ALERT).getAsBoolean(),
                    authType, baseurl);
        } else {
            throw new IllegalArgumentException("Unsupported authentication type provided. Please select either Public or Private.");
        }
    }

    private void loadPublicConfig() {
        Properties properties = new Properties();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            properties.load(inputStream);
            this.publicClientId = properties.getProperty("public.clientId");
            this.publicClientSecret = properties.getProperty("public.clientSecret");
        } catch (IOException cause) {
            throw new RuntimeException("Failed to load public configuration file.", cause);
        }
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

    public String getCallbackEndPoint() {
        if (Constants.PUBLIC.equals(authType)) {
            return baseUrl.replaceAll("/extension/api/.*", DEFAULT_CALLBACK_PATH);
        } else if (Constants.PRIVATE.equals(authType)) {
            return baseUrl + "/rest/outlook/callback";
        } else {
            return EMPTY_STRING;
        }
    }

    public String getForwardPath() {
        if (Constants.PUBLIC.equals(authType)) {
            return baseUrl + EXTENSION_FORWARD_PATH;
        } else {
            return EMPTY_STRING;
        }
    }

    public String getEmail() {
        return email;
    }

    public boolean isAllowMailAlert() {
        return allowMailAlert;
    }

    public String getAuthType() {
        return authType;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> attributesMap = new HashMap<>();
        addAttributeIfNotNull(attributesMap, CLIENT_ID, getClientId());
        addAttributeIfNotNull(attributesMap, CLIENT_SECRET, getClientSecret());
        addAttributeIfNotNull(attributesMap, TENANT_ID, getTenantId());
        addAttributeIfNotNull(attributesMap, ALLOW_MAIL_ALERT, isAllowMailAlert());
        addAttributeIfNotNull(attributesMap, EMAIL, getEmail());
        addAttributeIfNotNull(attributesMap, AUTH_TYPE, getAuthType());
        return attributesMap;
    }

}
