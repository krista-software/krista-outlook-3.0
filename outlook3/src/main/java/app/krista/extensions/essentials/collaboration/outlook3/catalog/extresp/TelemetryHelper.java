package app.krista.extensions.essentials.collaboration.outlook3.catalog.extresp;

import app.krista.ksdk.telemetry.TelemetryMetrics;
import javax.inject.Inject;
import org.jvnet.hk2.annotations.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper class for recording telemetry metrics related to extension responses.
 */
@Service
public class TelemetryHelper {

    private final TelemetryMetrics telemetry;

    @Inject
    public TelemetryHelper(TelemetryMetrics telemetry) {
        this.telemetry = telemetry;
    }

    public void recordSuccess(String baseMetric, long startTime, Map<String, String> additionalTags) {
        telemetry.incrementCounter(baseMetric + ".success", 1);
        telemetry.recordDuration(baseMetric + ".duration", System.currentTimeMillis() - startTime,
                withStatus("success", sanitizeTags(additionalTags)));
    }

    public void recordRetryPrompted(String baseMetric, long startTime, Map<String, String> additionalTags) {
        telemetry.incrementCounter(baseMetric + ".retry_prompted", 1);
        telemetry.recordDuration(baseMetric + ".duration", System.currentTimeMillis() - startTime,
                withStatus("retry_prompted", sanitizeTags(additionalTags)));
    }

    public void recordValidationError(String baseMetric, long startTime, String errorMessage, Map<String, String> additionalTags) {
        telemetry.incrementCounter(baseMetric + ".mustAuthorizeException", 1);
        telemetry.recordDuration(baseMetric + ".duration", System.currentTimeMillis() - startTime,
                withStatus("validation_error", sanitizeTags(additionalTags), "error_type", "validation", "error_message", errorMessage != null ? errorMessage : "Unknown error"));
    }

    public void recordError(String baseMetric, long startTime, Exception ex, Map<String, String> additionalTags) {
        telemetry.incrementCounter(baseMetric + ".error", 1);
        telemetry.recordDuration(baseMetric + ".duration", System.currentTimeMillis() - startTime,
                withStatus("error", sanitizeTags(additionalTags),
                        "error_type", "general",
                        "error_message", ex.getMessage() != null ? ex.getMessage() : "Unknown error",
                        "error_class", ex.getClass().getSimpleName()));
    }

    public void incrementCount(String baseMetric) {
        telemetry.incrementCounter(baseMetric + ".count", 1);
    }

    /**
     * Sanitizes a map of tags by replacing null values with the string "null"
     *
     * @param tags The map of tags to sanitize
     * @return A new map with sanitized values
     */
    private Map<String, String> sanitizeTags(Map<String, String> tags) {
        if (tags == null) {
            return Map.of();
        }

        Map<String, String> sanitizedTags = new java.util.HashMap<>();
        for (Map.Entry<String, String> entry : tags.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sanitizedTags.put(key, value != null ? value : "NA");
        }
        return sanitizedTags;
    }

    private Map<String, String> withStatus(String status, Map<String, String> baseTags, String... moreTags) {
        Map<String, String> tags = new java.util.HashMap<>(baseTags);
        tags.put("status", status);
        for (int i = 0; i < moreTags.length - 1; i += 2) {
            String value = moreTags[i + 1];
            tags.put(moreTags[i], value != null ? value : "null");
        }
        return tags;
    }

    public static Map<String, String> safeTagMap(String... keysAndValues) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < keysAndValues.length - 1; i += 2) {
            map.put(keysAndValues[i], keysAndValues[i + 1] != null ? keysAndValues[i + 1] : "NA");
        }
        return map;
    }
}
