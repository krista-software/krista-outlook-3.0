package app.krista.extensions.essentials.collaboration.outlook3.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static app.krista.extensions.essentials.collaboration.outlook3.impl.util.Constants.AUTH_CONTEXT_ID;

public final class AuthHelper {

    private final static Logger LOGGER = LoggerFactory.getLogger(AuthHelper.class);

    public static String getAuthContextId(String state) {
        int authContextIdIndex = state.indexOf(AUTH_CONTEXT_ID);
        if (authContextIdIndex == -1) {
            LOGGER.debug("AuthContextId not found in state parameter");
            return null;
        }
        String authContextIdToEnd = state.substring(authContextIdIndex);
        int hashIndex = authContextIdToEnd.indexOf("#");
        String authContextId;
        if (hashIndex != -1) {
            // If there is a "#", end the extraction at this index
            authContextId = authContextIdToEnd.substring(0, hashIndex);
        } else {
            // If there is no "#", use the entire substring
            authContextId = authContextIdToEnd;
        }
        return authContextId;
    }
}
