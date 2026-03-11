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

package app.krista.extensions.essentials.collaboration.outlook3.catalog.validators;

import app.krista.extensions.essentials.collaboration.outlook3.impl.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;


public class OutlookCredentialValidator {
    private static final Logger logger = LoggerFactory.getLogger(OutlookCredentialValidator.class);
    private final HttpClient httpClient;

    public OutlookCredentialValidator() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public void validateToken(String clientId, String clientSecret, String tenantId) {
        try {
            HttpRequest request = createPostRequest(clientId, clientSecret, tenantId);
            HttpResponse<String> response = executeRequest(request);

            if (response.body() != null) {
                String body = response.body();
                logger.info("Response body: {}", body);

                if (body.contains(Constants.TENANT_NOT_FOUND_CODE) ||
                        body.contains(Constants.ERROR_INVALID_TENANT_ID) ||
                        body.contains(Constants.INVALID_CLIENT_SECRET_CODE) ||
                        body.contains(Constants.ERROR_INVALID_CLIENT_ID)) {
                    throw new IllegalArgumentException("Invalid Outlook credentials: " + body);
                }
            }
        } catch (IOException | InterruptedException e) {
            logger.error("HTTP request failed: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private HttpRequest createPostRequest(String clientId, String clientSecret, String tenantId) {
        String url = String.format(Constants.BASE_URL_FORMAT, tenantId);
        String requestBody = String.format("client_id=%s&client_secret=%s%s",
                clientId, clientSecret, Constants.REQUEST_BODY_SCOPE_AND_GRANT_TYPE);

        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .timeout(Duration.ofSeconds(30))
                .build();
    }

    private HttpResponse<String> executeRequest(HttpRequest request)
            throws IOException, InterruptedException {
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
