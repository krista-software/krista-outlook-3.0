package app.krista.extensions.essentials.collaboration.outlook3.health;

import app.krista.extension.executor.ExtensionResponse;
import app.krista.extension.executor.Invoker;
import app.krista.extensions.essentials.collaboration.outlook3.OutlookAttributes;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.entities.ExtensionResponseMeta;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.entities.HealthStatus;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.extresp.ExtensionResponseFactory;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.extresp.TelemetryHelper;
import app.krista.extensions.essentials.collaboration.outlook3.impl.MessagingAreaImpl;
import app.krista.extensions.essentials.collaboration.outlook3.impl.stores.OutlookAttributeStore;
import app.krista.extensions.essentials.collaboration.outlook3.service.Account;
import org.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for checking the health of Outlook authentication services.
 * <p>
 * The AuthenticationHealthCheck service provides methods to gather authentication metrics
 * and determine the overall health of the Outlook authentication system. It collects
 * information about configuration status, token validity, and system metrics.
 * <p>
 * Key features include:
 * <ul>
 *   <li>Authentication configuration validation</li>
 *   <li>Token status verification</li>
 *   <li>System resource monitoring</li>
 *   <li>Detailed logging for troubleshooting</li>
 * </ul>
 */
@Service
public class HealthCheck {

    private static final Logger LOGGER = LoggerFactory.getLogger(HealthCheck.class);

    // Timestamp when the service was started, used for uptime calculation.
    private static final Instant START_TIME = Instant.now();

    private static long lastHealthCheckTime = 0;
    private final OutlookAttributeStore attributeStore;
    private final Account account;
    private final MessagingAreaImpl messagingAreaImpl;
    private final Invoker invoker;
    private final TelemetryHelper telemetryHelper;

    /**
     * Constructor for AuthenticationHealthCheck.
     *
     * @param attributeStore The OutlookAttributeStore
     */
    @Inject
    public HealthCheck(OutlookAttributeStore attributeStore, Invoker invoker,
                       Account account, MessagingAreaImpl messagingAreaImpl, TelemetryHelper telemetryHelper) {
        this.attributeStore = attributeStore;
        this.account = account;
        this.messagingAreaImpl = messagingAreaImpl;
        this.invoker = invoker;
        this.telemetryHelper = telemetryHelper;
        LOGGER.info("AuthenticationHealthCheck service initialized");
    }


    /**
     * Checks the health of the authentication services.
     *
     * @return ExtensionResponse containing health metrics and status information
     */
    public ExtensionResponse checkHealth() {
        long startTime = System.currentTimeMillis();
        String operationId = "auth_health_check_" + startTime;
        LOGGER.debug("Performing authentication health check with operation ID: {}", operationId);
        lastHealthCheckTime = lastHealthCheckTime == 0 ? System.currentTimeMillis() : lastHealthCheckTime;
        Map<String, Object> healthData = new HashMap<>();
        String healthSummaryMessage = "";

        // Record the health check attempt
        telemetryHelper.incrementCount("outlook3.healthCheck.attempt");

        try {
            // Collect system metrics
            healthData = updateSystemMetrics();
            healthSummaryMessage = healthSummaryMessage + "Collected system metrics.\n";
            healthData.put("LastHealthCheckTime", lastHealthCheckTime);

            // Record system metrics in telemetry
            recordSystemMetricsTelemetry(healthData);

            // Check authentication configuration
            OutlookAttributes attributes = attributeStore.load(invoker.getInvokerId());
            checkAuthenticationConfiguration(attributes, healthData);

            healthSummaryMessage = healthSummaryMessage + "Checked authentication configuration.\n";

            // Record successful health check completion time
            telemetryHelper.recordSuccess("outlook3.healthCheck.overall", startTime,
                    TelemetryHelper.safeTagMap("invoker_id", invoker.getInvokerId(), "status", "success"));

            return toExtensionResponse(healthData, null, startTime, healthSummaryMessage);
        } catch (Exception exception) {
            LOGGER.error("Authentication health check failed for operation {}: {}",
                    operationId, exception.getMessage(), exception);
            healthSummaryMessage = healthSummaryMessage + "Error message:" + exception.getMessage() + "\n";

            // Record failed health check with error details
            telemetryHelper.recordError("outlook3.healthCheck.overall", startTime, exception,
                    TelemetryHelper.safeTagMap(
                            "invoker_id", invoker.getInvokerId(),
                            "status", "error",
                            "error_message", exception.getMessage(),
                            "error_type", exception.getClass().getSimpleName()
                    ));

            return toExtensionResponse(healthData, exception, startTime, healthSummaryMessage);
        }
    }

    private void checkAuthenticationConfiguration(OutlookAttributes attributes, Map<String, Object> healthData) {
        if (attributes == null) {
            healthData.put("Message", "Outlook is not configured");
            healthData.put("AuthType", "Not Configured");
            healthData.put("Email", "Not Configured");
            healthData.put("HasRefreshToken", false);
            healthData.put("TokenValid", false);

            telemetryHelper.incrementCount("outlook3.healthCheck.auth.notConfigured");
        } else {
            String authType = attributes.getAuthType();
            boolean hasRefreshToken = isHasRefreshToken();
            healthData.put("AuthType", authType);
            healthData.put("Email", attributes.getEmail());
            healthData.put("HasRefreshToken", hasRefreshToken);
            healthData.put("TokenValid", hasRefreshToken);

            Map<String, String> authTags = TelemetryHelper.safeTagMap(
                    "invoker_id", invoker.getInvokerId(),
                    "email", attributes.getEmail(),
                    "auth_type", authType,
                    "has_refresh_token", String.valueOf(hasRefreshToken),
                    "token_valid", String.valueOf(hasRefreshToken)
            );

            telemetryHelper.recordCounter("outlook3.healthCheck.auth.configured", 1, authTags);

            if (hasRefreshToken) {
                telemetryHelper.recordCounter("outlook3.healthCheck.auth.tokenValid", 1, authTags);
            } else {
                telemetryHelper.recordCounter("outlook3.healthCheck.auth.tokenInvalid", 1, authTags);
            }
        }
    }

    private void recordSystemMetricsTelemetry(Map<String, Object> healthData) {
        telemetryHelper.recordGauge("outlook3.healthCheck.memory.used",
                convertToDouble(healthData.get("CurrentMemoryUsageMB")));
        telemetryHelper.recordGauge("outlook3.healthCheck.memory.available",
                convertToDouble(healthData.get("AvailableMemoryMB")));
        telemetryHelper.recordGauge("outlook3.healthCheck.memory.total",
                convertToDouble(healthData.get("TotalMemoryMB")));
        telemetryHelper.recordGauge("outlook3.healthCheck.cpu.usage",
                convertToDouble(healthData.get("CpuUsagePercentage")));
        telemetryHelper.recordGauge("outlook3.healthCheck.threads.active",
                convertToDouble(healthData.get("ActiveThreads")));
        telemetryHelper.recordGauge("outlook3.healthCheck.uptime.hours",
                convertToDouble(healthData.get("UptimeHours")));
    }

    private boolean isHasRefreshToken() {
        boolean hasRefreshToken;
        try {
            //verifying the refresh token will throw an exception if it's invalid
            account.getFolderNames();
            hasRefreshToken = true;
        } catch (Exception e) {
            hasRefreshToken = false;
        }
        return hasRefreshToken;
    }

    private Map<String, Object> updateSystemMetrics() {
        Map<String, Object> healthData = new HashMap<>();
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        long usedMemory = memoryBean.getHeapMemoryUsage().getUsed() / (1024 * 1024);
        long maxMemory = memoryBean.getHeapMemoryUsage().getMax() / (1024 * 1024);
        long availableMemory = maxMemory - usedMemory;

        // Get CPU usage (approximate)
        double cpuUsage = getApproximateCpuUsage();

        // Get thread metrics
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        int threadCount = threadBean.getThreadCount();
        // Calculate uptime
        Duration uptime = Duration.between(START_TIME, Instant.now());
        double uptimeHours = uptime.toMillis() / (1000.0 * 60 * 60);
        uptimeHours = Math.round(uptimeHours * 100.0) / 100.0;
        addSystemMetrics(healthData, usedMemory, availableMemory, maxMemory,
                cpuUsage, threadCount, uptimeHours);
        return healthData;
    }

    /**
     * Helper method to add system metrics to the health data map.
     */
    private void addSystemMetrics(Map<String, Object> healthData, long usedMemory,
                                  long availableMemory, long maxMemory, double cpuUsage,
                                  int threadCount, double uptimeHours) {
        healthData.put("CurrentMemoryUsageMB", (double) usedMemory);
        healthData.put("AvailableMemoryMB", (double) availableMemory);
        healthData.put("TotalMemoryMB", (double) maxMemory);
        healthData.put("CpuUsagePercentage", cpuUsage);
        healthData.put("ActiveThreads", (double) threadCount);
        healthData.put("UptimeHours", uptimeHours);
    }

    /**
     * Determines the overall system status based on various metrics.
     * <p>
     * This method evaluates the system's health by analyzing CPU usage and memory
     * utilization against predefined thresholds. The status is categorized as:
     * <ul>
     *   <li>HEALTHY - All metrics are within normal ranges</li>
     *   <li>DEGRADED - Some metrics are approaching critical levels</li>
     *   <li>UNHEALTHY - One or more metrics have exceeded critical thresholds</li>
     * </ul>
     *
     * @param cpuUsage   CPU usage as a percentage
     * @param usedMemory Used memory in megabytes
     * @param maxMemory  Maximum available memory in megabytes
     * @return A string representing the system status
     */
    private String determineSystemStatus(double cpuUsage, long usedMemory, long maxMemory) {
        // Memory usage percentage
        double memoryUsagePercent = (double) usedMemory / maxMemory * 100;

        String status;
        // Critical conditions - UNHEALTHY
        if (cpuUsage > 90 || memoryUsagePercent > 90) {
            status = "UNHEALTHY";
        }
        // Warning conditions - DEGRADED
        else if (cpuUsage > 75 || memoryUsagePercent > 75) {
            status = "DEGRADED";
        }
        // All good
        else {
            status = "HEALTHY";
        }

        return status;
    }

    /**
     * Gets an approximate CPU usage percentage.
     * <p>
     * This method provides an estimate of the current CPU usage by using the
     * system load average reported by the operating system. The load average
     * is normalized by the number of available processors to get a percentage.
     *
     * @return CPU usage as a percentage (0-100)
     */
    private double getApproximateCpuUsage() {
        try {
            // Try to get CPU usage from OperatingSystemMXBean
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();

            // Check if we have access to more detailed CPU metrics
            if (osBean instanceof com.sun.management.OperatingSystemMXBean sunOsBean) {
                double cpuUsage = sunOsBean.getProcessCpuLoad() * 100;

                // If process CPU load is available and valid, use it
                if (cpuUsage >= 0 && cpuUsage <= 100) {
                    return Math.round(cpuUsage * 100.0) / 100.0; // Round to 2 decimal places
                }

                // Try system CPU load as fallback
                double systemCpuUsage = sunOsBean.getCpuLoad() * 100;
                if (systemCpuUsage >= 0 && systemCpuUsage <= 100) {
                    return Math.round(systemCpuUsage * 100.0) / 100.0;
                }
            }

            // Fallback to load average approach
            double load = osBean.getSystemLoadAverage();
            int processors = Runtime.getRuntime().availableProcessors();

            if (load < 0) {
                // Not supported on this platform, return a reasonable default
                return 15.0; // Default to 15% for a healthy system
            }

            // Convert load average to percentage based on available processors
            double usage = (load / processors) * 100;
            return Math.min(Math.round(usage * 100.0) / 100.0, 100.0); // Cap at 100%
        } catch (Exception e) {
            LOGGER.warn("Error getting CPU usage", e);
            return 15.0; // Default value on error - reasonable for healthy system
        }
    }

    /**
     * Converts the health data map to an ExtensionResponse object.
     * <p>
     * This method creates a HealthStatus entity from the health data map
     * and wraps it in an ExtensionResponse along with a success/failure indicator
     * and a message. The response is suitable for returning from catalog requests.
     *
     * @param healthData           Map containing health metrics and status information
     * @param healthSummaryMessage Summary of health check results
     * @return ExtensionResponse containing the health status and success indicator
     */
    private ExtensionResponse toExtensionResponse(Map<String, Object> healthData, Exception exception, long startTime, String healthSummaryMessage) {
        String operationId = "health_response_" + startTime;
        Map<String, Object> extensionResponse = new HashMap<>();
        HealthStatus healthStatus = getHealthStatus(healthData);
        double timeTakenInSeconds = (System.currentTimeMillis() - startTime) / 1000.0;
        LOGGER.debug("Creating extension response for health data with operation ID: {}", operationId);

        try {
            // Determine if the system is healthy
            healthStatus.systemStatus = exception == null ? "HEALTHY" : "UNHEALTHY";
            boolean isHealthy = "HEALTHY".equals(healthStatus.systemStatus);
            extensionResponse = getExtensionResponse(isHealthy, healthStatus, timeTakenInSeconds, exception, healthSummaryMessage);

            // Record response creation time
            telemetryHelper.recordSuccess("outlook3.healthCheck.responseTime",
                    startTime,
                    TelemetryHelper.safeTagMap("is_healthy", String.valueOf(isHealthy)));

            LOGGER.info("Extension response created successfully in {} ms", timeTakenInSeconds);
        } catch (Exception exception1) {
            LOGGER.error("Error creating extension response for operation {}: {}",
                    operationId, exception1.getMessage(), exception1);

            // Record error in response creation
            telemetryHelper.recordError("outlook3.healthCheck.responseCreation", startTime, exception1,
                    TelemetryHelper.safeTagMap("invoker_id", invoker.getInvokerId(), "status", "error"));

            healthStatus.systemStatus = "UNHEALTHY";
            extensionResponse = getExtensionResponse(false, healthStatus, timeTakenInSeconds, exception1, healthSummaryMessage);
        }

        // Record final health status
        telemetryHelper.recordCounter("outlook3.healthCheck.result", 1,
                TelemetryHelper.safeTagMap(
                        "invoker_id", invoker.getInvokerId(),
                        "status", healthStatus.systemStatus,
                        "has_exception", String.valueOf(exception != null)
                ));

        return ExtensionResponseFactory.create(extensionResponse);
    }

    @NotNull
    private HealthStatus getHealthStatus(Map<String, Object> healthData) {
        // Create HealthStatus entity from health data
        HealthStatus healthStatus = new HealthStatus();
        healthStatus.extensionName = "Outlook";
        healthStatus.lastHealthCheckTime = (Long) healthData.getOrDefault("LastHealthCheckTime", System.currentTimeMillis());

        // Map system metrics
        healthStatus.currentMemoryUsageMB = convertToDouble(healthData.get("CurrentMemoryUsageMB"));
        healthStatus.availableMemoryMB = convertToDouble(healthData.get("AvailableMemoryMB"));
        healthStatus.totalMemoryMB = convertToDouble(healthData.get("TotalMemoryMB"));
        healthStatus.cPUUsagePercentage = convertToDouble(healthData.get("CpuUsagePercentage"));
        healthStatus.activeThreads = convertToDouble(healthData.get("ActiveThreads"));
        healthStatus.uptimeHours = convertToDouble(healthData.get("UptimeHours"));
        healthStatus.systemStatus = (String) healthData.getOrDefault("SystemStatus", "UNKNOWN");
        healthStatus.authType = (String) healthData.getOrDefault("AuthType", "UNKNOWN");
        healthStatus.email = (String) healthData.getOrDefault("Email", "UNKNOWN");
        healthStatus.hasRefreshToken = (Boolean) healthData.getOrDefault("HasRefreshToken", false);
        healthStatus.tokenValid = (Boolean) healthData.getOrDefault("TokenValid", false);
        return healthStatus;
    }

    /**
     * Helper method to convert an object to a double value.
     *
     * @param value The object to convert
     * @return The double value, or 0.0 if conversion fails
     */
    private double convertToDouble(Object value) {
        if (value == null) {
            return 0.0;
        }

        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }

        try {
            return Double.parseDouble(value.toString());
        } catch (Exception e) {
            LOGGER.warn("Failed to convert value to double: {}", value);
            return 0.0;
        }
    }

    /**
     * Creates a standardized extension response map.
     *
     * @param isHealthy            Whether the system is healthy
     * @param healthStatus         The health status entity
     * @param timeTakenInSeconds   Time taken to perform the health check
     * @param exception              exception thrown during the health check (if any), otherwise {@code null}
     * @param healthSummaryMessage   summary message describing the overall health
     * @return A map containing the extension response
     */
    private Map<String, Object> getExtensionResponse(boolean isHealthy, HealthStatus healthStatus, double timeTakenInSeconds, Exception exception, String healthSummaryMessage) {
        if (healthStatus.email.equals("Not Configured")) {
            isHealthy = false;
            ExtensionResponseMeta responseMeta = new ExtensionResponseMeta();
            responseMeta.message = "Authentication failed.Attributes not found.";
            responseMeta.technicalDetailedErrorReport = "Outlook is not configured";
            responseMeta.responseType = "UNHEALTHY";
            responseMeta.timeTakenInSeconds = timeTakenInSeconds;

            // Record not configured status
            telemetryHelper.recordCounter("outlook3.healthCheck.notConfigured", 1,
                    Map.of("invoker_id", invoker.getInvokerId()));

            return Map.of(
                    "Health Status", healthStatus,
                    "Is Healthy", isHealthy,
                    "Extension Response Meta", responseMeta
            );
        }

        String responseMessage = isHealthy
                ? "Health check completed successfully. All systems operational."
                : "System status: " + healthStatus.systemStatus + "\n" + healthSummaryMessage;

        ExtensionResponseMeta extensionResponseMeta = new ExtensionResponseMeta();
        extensionResponseMeta.message = responseMessage;
        extensionResponseMeta.technicalDetailedErrorReport = exception != null ? Arrays.toString(exception.getStackTrace()) : "";
        extensionResponseMeta.responseType = isHealthy ? "SUCCESS" : "UNHEALTHY";
        extensionResponseMeta.timeTakenInSeconds = timeTakenInSeconds;

        sendMail(isHealthy, healthStatus, exception, healthSummaryMessage, extensionResponseMeta);

        return Map.of(
                "Health Status", healthStatus,
                "Is Healthy", isHealthy,
                "Extension Response Meta", extensionResponseMeta
        );
    }

    private void sendMail(boolean isHealthy, HealthStatus healthStatus, Exception exception, String healthSummaryMessage, ExtensionResponseMeta extensionResponseMeta) {
        if (!isHealthy) {
            String errorMessage = exception != null ? exception.getMessage() : "";
            String emailBody = "System status: " + healthStatus.systemStatus + "\n" + healthSummaryMessage + "\n" + " For " + "Extension Name : " + healthStatus.extensionName + " with Invoker Id : " + invoker.getInvokerId() + " and Invoker Name : " + invoker.getInvokerName() + "\n" + "\n" + "Technical Detailed Error Report: " + extensionResponseMeta.technicalDetailedErrorReport + "\n" + "\n" + "Error Message: " + errorMessage;

            // Record unhealthy status and email alert
            telemetryHelper.recordCounter("outlook3.healthCheck.unhealthy", 1,
                    TelemetryHelper.safeTagMap(
                            "invoker_id", invoker.getInvokerId(),
                            "system_status", healthStatus.systemStatus,
                            "error_message", errorMessage
                    ));

            messagingAreaImpl.sendMail("Health Check Summary", emailBody, null, "service.automation@kristasoft.com", null, null, null, "Text");

            telemetryHelper.recordCounter("outlook3.healthCheck.alertEmailSent", 1,
                    Map.of("invoker_id", invoker.getInvokerId()));
        } else {
            // Record healthy status
            telemetryHelper.recordCounter("outlook3.healthCheck.healthy", 1,
                    Map.of("invoker_id", invoker.getInvokerId()));
        }
    }

}
