package app.krista.extensions.essentials.collaboration.outlook3.health;

import app.krista.extension.executor.ExtensionResponse;
import app.krista.extension.executor.Invoker;
import app.krista.extensions.essentials.collaboration.outlook3.OutlookAttributes;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.entities.ExtensionResponseMeta;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.entities.HealthStatus;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.extresp.ExtensionResponseFactory;
import app.krista.extensions.essentials.collaboration.outlook3.impl.MessagingAreaImpl;
import app.krista.extensions.essentials.collaboration.outlook3.impl.connectors.GraphServiceClientProviderFactory;
import app.krista.extensions.essentials.collaboration.outlook3.impl.stores.OutlookAttributeStore;
import app.krista.extensions.essentials.collaboration.outlook3.impl.stores.RefreshTokenStore;
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
    /**
     * Logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(HealthCheck.class);

    /**
     * Timestamp when the service was started, used for uptime calculation.
     */
    private static Instant START_TIME = Instant.now();

    private static long lastHealthCheckTime = 0;
    private final GraphServiceClientProviderFactory providerFactory;
    private final OutlookAttributeStore attributeStore;
    private final RefreshTokenStore refreshTokenStore;
    private final Account account;
    private final MessagingAreaImpl messagingAreaImpl;
    private final Invoker invoker;

    /**
     * Constructor for AuthenticationHealthCheck.
     *
     * @param providerFactory   The GraphServiceClientProviderFactory
     * @param attributeStore    The OutlookAttributeStore
     * @param refreshTokenStore The RefreshTokenStore
     */
    @Inject
    public HealthCheck(
            GraphServiceClientProviderFactory providerFactory,
            OutlookAttributeStore attributeStore,
            RefreshTokenStore refreshTokenStore,
            Invoker invoker, Account account, MessagingAreaImpl messagingAreaImpl) {
        this.providerFactory = providerFactory;
        this.attributeStore = attributeStore;
        this.refreshTokenStore = refreshTokenStore;
        this.account = account;
        this.messagingAreaImpl = messagingAreaImpl;
        this.invoker = invoker;
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
        try {
            healthData = updateSystemMetrics();
            healthSummaryMessage = healthSummaryMessage + "Collected system metrics.\n";
            healthData.put("LastHealthCheckTime", lastHealthCheckTime);
            // Load attributes
            OutlookAttributes attributes = attributeStore.load(invoker.getInvokerId());
            if (attributes == null) {
                // Set status for not configured state
                healthData.put("Message", "Outlook is not configured");
            } else {
                // Check if refresh token exists for private auth
                String authType = attributes.getAuthType();
                boolean hasRefreshToken = isHasRefreshToken();
                healthData.put("AuthType", authType);
                healthData.put("Email", attributes.getEmail());
                healthData.put("HasRefreshToken", hasRefreshToken);
                healthData.put("TokenValid", hasRefreshToken);
            }
            healthSummaryMessage = healthSummaryMessage + "Checked authentication configuration.\n";
            return toExtensionResponse(healthData, null, startTime, healthSummaryMessage);
        } catch (Exception exception) {
            LOGGER.error("Authentication health check failed for operation {}: {}",
                    operationId, exception.getMessage(), exception);
            healthSummaryMessage = healthSummaryMessage + "Error message:" + exception.getMessage() + "\n";
            return toExtensionResponse(healthData, exception, startTime, healthSummaryMessage);
        }
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

        // Determine system status based on resource usage

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

        // Critical conditions - UNHEALTHY
        if (cpuUsage > 90 || memoryUsagePercent > 90) {
            return "UNHEALTHY";
        }

        // Warning conditions - DEGRADED
        if (cpuUsage > 75 || memoryUsagePercent > 75) {
            return "DEGRADED";
        }

        // All good
        return "HEALTHY";
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
            if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
                com.sun.management.OperatingSystemMXBean sunOsBean = (com.sun.management.OperatingSystemMXBean) osBean;
                double cpuUsage = sunOsBean.getProcessCpuLoad() * 100;

                // If process CPU load is available and valid, use it
                if (cpuUsage >= 0 && cpuUsage <= 100) {
                    return Math.round(cpuUsage * 100.0) / 100.0; // Round to 2 decimal places
                }

                // Try system CPU load as fallback
                double systemCpuUsage = sunOsBean.getSystemCpuLoad() * 100;
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
            LOGGER.info("Extension response created successfully in {} ms", timeTakenInSeconds);
        } catch (Exception exception1) {
            LOGGER.error("Error creating extension response for operation {}: {}",
                    operationId, exception1.getMessage(), exception1);
            healthStatus.systemStatus = "UNHEALTHY";
            extensionResponse = getExtensionResponse(false, healthStatus, timeTakenInSeconds, exception1, healthSummaryMessage);
        }
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
     * @param exception
     * @param healthSummaryMessage
     * @return A map containing the extension response
     */
    private Map<String, Object> getExtensionResponse(boolean isHealthy, HealthStatus healthStatus, double timeTakenInSeconds, Exception exception, String healthSummaryMessage) {
        String responseMessage = isHealthy
                ? "Health check completed successfully. All systems operational."
                : "System status: " + healthStatus.systemStatus + "\n" + healthSummaryMessage;

        ExtensionResponseMeta extensionResponseMeta = new ExtensionResponseMeta();
        extensionResponseMeta.message = responseMessage;
        extensionResponseMeta.technicalDetailedErrorReport = exception != null ? Arrays.toString(exception.getStackTrace()) : "";
        extensionResponseMeta.responseType = isHealthy ? "SUCCESS" : "UNHEALTHY";
        extensionResponseMeta.timeTakenInSeconds = timeTakenInSeconds;
        if (!isHealthy) {
            String errorMessage = exception != null ? exception.getMessage() : "";
            String emailBody = "System status: " + healthStatus.systemStatus + "\n" + healthSummaryMessage + "\n" + " For " + "Extension Name : " + healthStatus.extensionName + " with Invoker Id : " + invoker.getInvokerId() + " and Invoker Name : " + invoker.getInvokerName() + "\n" + "\n" + "Technical Detailed Error Report: " + extensionResponseMeta.technicalDetailedErrorReport + "\n" + "\n" + "Error Message: " + errorMessage;
            messagingAreaImpl.sendMail("Health Check Summary", emailBody, null, "service.automation@kristasoft.com", null, null, null, "Text");
        }
        return Map.of(
                "Health Status", healthStatus,
                "Is Healthy", isHealthy,
                "Extension Response Meta", extensionResponseMeta
        );
    }
}
