package app.krista.extensions.essentials.collaboration.outlook3.health;

import app.krista.extension.executor.ExtensionResponse;
import app.krista.extension.executor.ExtensionResponseBuilder;
import app.krista.extension.executor.Invoker;
import app.krista.extensions.essentials.collaboration.outlook3.OutlookAttributes;
import app.krista.extensions.essentials.collaboration.outlook3.impl.connectors.GraphServiceClientProviderFactory;
import app.krista.extensions.essentials.collaboration.outlook3.impl.stores.OutlookAttributeStore;
import app.krista.extensions.essentials.collaboration.outlook3.impl.stores.RefreshTokenStore;
import app.krista.extensions.essentials.collaboration.outlook3.impl.util.Constants;
import app.krista.extensions.essentials.collaboration.outlook3.service.Account;
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
import java.util.HashMap;
import java.util.Map;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.entities.ExtensionResponseMeta;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.entities.HealthStatus;

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
    /** Logger for this class. */
    private static final Logger LOGGER = LoggerFactory.getLogger(HealthCheck.class);

    /** Timestamp when the service was started, used for uptime calculation. */
    private static Instant START_TIME = Instant.now();

    // Metric names
    private static final String HEALTH_CHECK_REQUEST_COUNT = "health_check_request_count";
    private static final String HEALTH_CHECK_SUCCESS_COUNT = "health_check_success_count";
    private static final String HEALTH_CHECK_FAILURE_COUNT = "health_check_failure_count";
    private static final String HEALTH_CHECK_LATENCY_MS = "health_check_latency_ms";
    private static final String HEALTH_CHECK_ERROR_TYPE = "health_check_error_type";

    // System Metrics
    private static final String OUTLOOK_MEMORY_USAGE_MB = "outlook_memory_usage_mb";
    private static final String OUTLOOK_MEMORY_AVAILABLE_MB = "outlook_memory_available_mb";
    private static final String OUTLOOK_CPU_USAGE_PERCENTAGE = "outlook_cpu_usage_percentage";
    private static final String OUTLOOK_ACTIVE_THREADS = "outlook_active_threads";
    private static final String OUTLOOK_UPTIME_HOURS = "outlook_uptime_hours";
    private static final String OUTLOOK_AUTH_STATUS = "outlook_auth_status";
    private final GraphServiceClientProviderFactory providerFactory;
    private final OutlookAttributeStore attributeStore;
    private final RefreshTokenStore refreshTokenStore;
    private final String invokerId;
   // private final TelemetryMetrics telemetryMetrics;
    private final Account account;


    /**
     * Constructor for AuthenticationHealthCheck.
     *
     * @param providerFactory The GraphServiceClientProviderFactory
     * @param attributeStore The OutlookAttributeStore
     * @param refreshTokenStore The RefreshTokenStore
     */
    @Inject
    public HealthCheck(
            GraphServiceClientProviderFactory providerFactory,
            OutlookAttributeStore attributeStore,
            RefreshTokenStore refreshTokenStore,
            Invoker invoker, Account account) {
        this.providerFactory = providerFactory;
        this.attributeStore = attributeStore;
        this.refreshTokenStore = refreshTokenStore;
        this.invokerId = invoker.getInvokerId();
        //this.telemetryMetrics = telemetryMetrics;
        this.account = account;
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

        // Increment request counter
      //  telemetryMetrics.incrementCounter(HEALTH_CHECK_REQUEST_COUNT);

        try {
            Map<String, Object> healthData = new HashMap<>();

            // Get memory metrics
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

            // Load attributes
            OutlookAttributes attributes = attributeStore.load(invokerId);
            if (attributes == null) {
                // Set status for not configured state
                healthData.put("Message", "Outlook is not configured");
                
                // Add system metrics even for not configured state
                addSystemMetrics(healthData, usedMemory, availableMemory, maxMemory, 
                        cpuUsage, threadCount, uptimeHours);
                
                healthData.put("LastHealthCheckTime", System.currentTimeMillis());

                // Record telemetry for not configured state
//                telemetryMetrics.incrementCounter(OUTLOOK_AUTH_STATUS, 1,
//                        Map.of("status", "NOT_CONFIGURED"));

                return toExtensionResponse(healthData);
            }

            // Check if refresh token exists for private auth
            String authType = attributes.getAuthType();
            boolean hasRefreshToken;

            healthData.put("AuthType", authType);
            healthData.put("Email", attributes.getEmail());
            account.getFolderNames();
            hasRefreshToken = true;
            healthData.put("HasRefreshToken", hasRefreshToken);
            healthData.put("TokenValid", true);
            // Add system metrics
            addSystemMetrics(healthData, usedMemory, availableMemory, maxMemory, 
                    cpuUsage, threadCount, uptimeHours);

            // Add token metrics if available

            
            healthData.put("LastHealthCheckTime", System.currentTimeMillis());

            // Record telemetry metrics
//            try {
//                telemetryMetrics.observeGauge(OUTLOOK_MEMORY_USAGE_MB, usedMemory);
//                telemetryMetrics.observeGauge(OUTLOOK_MEMORY_AVAILABLE_MB, availableMemory);
//                telemetryMetrics.observeGauge(OUTLOOK_CPU_USAGE_PERCENTAGE, cpuUsage);
//                telemetryMetrics.observeGauge(OUTLOOK_ACTIVE_THREADS, threadCount);
//                telemetryMetrics.observeGauge(OUTLOOK_UPTIME_HOURS, uptimeHours);
//            } catch (Exception e) {
//                // Log but don't fail the health check if telemetry recording fails
//                LOGGER.warn("Error recording telemetry metrics: {}", e.getMessage());
//            }

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            // Record success metrics
//            telemetryMetrics.incrementCounter(HEALTH_CHECK_SUCCESS_COUNT);
//            telemetryMetrics.recordDuration(HEALTH_CHECK_LATENCY_MS, duration);

            return toExtensionResponse(healthData);
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            // Record failure metrics
//            try {
//                telemetryMetrics.incrementCounter(HEALTH_CHECK_FAILURE_COUNT);
//                telemetryMetrics.incrementCounter(HEALTH_CHECK_ERROR_TYPE, 1,
//                        Map.of("error_type", e.getClass().getSimpleName()));
//                telemetryMetrics.recordDuration(HEALTH_CHECK_LATENCY_MS, duration);
//            } catch (Exception telemetryError) {
//                // Log but don't suppress the original exception if telemetry recording fails
//                LOGGER.warn("Error recording failure telemetry: {}", telemetryError.getMessage());
//            }

            LOGGER.error("Authentication health check failed for operation {}: {}",
                    operationId, e.getMessage(), e);
            
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("Status", "ERROR");
            errorData.put("ErrorMessage", e.getMessage());
            errorData.put("ErrorType", e.getClass().getSimpleName());
            errorData.put("LastHealthCheckTime", System.currentTimeMillis());
            
            return toExtensionResponse(errorData);
        }
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
        String systemStatus = determineSystemStatus(cpuUsage, usedMemory, maxMemory);
        healthData.put("SystemStatus", systemStatus);
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
     * @param cpuUsage CPU usage as a percentage
     * @param usedMemory Used memory in megabytes
     * @param maxMemory Maximum available memory in megabytes
     * @return A string representing the system status
     */
    String determineSystemStatus(double cpuUsage, long usedMemory, long maxMemory) {
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
    double getApproximateCpuUsage() {
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
     * @param healthData Map containing health metrics and status information
     * @return ExtensionResponse containing the health status and success indicator
     */
    public ExtensionResponse toExtensionResponse(Map<String, Object> healthData) {
        long startTime = System.currentTimeMillis();
        String operationId = "health_response_" + startTime;

        LOGGER.debug("Creating extension response for health data with operation ID: {}", operationId);
        
        try {
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

            
            // Determine if the system is healthy
            boolean isHealthy = "HEALTHY".equals(healthStatus.systemStatus);
            
            double timeTakenInSeconds = (System.currentTimeMillis() - startTime) / 1000.0;
            
            Map<String, Object> extensionResponse = getExtensionResponse(isHealthy, healthStatus, timeTakenInSeconds);
            
            long endTime = System.currentTimeMillis();
            LOGGER.info("Extension response created successfully in {} ms", (endTime - startTime));
            
            return new ExtensionResponseBuilder().success(extensionResponse).build();
        } catch (Exception e) {
            LOGGER.error("Error creating extension response for operation {}: {}",
                    operationId, e.getMessage(), e);
            
            String errorDetails = String.format(
                    "Operation ID: %s, Error Type: %s, Message: %s",
                    operationId,
                    e.getClass().getSimpleName(),
                    e.getMessage());
            
            // Create a proper error object
            ExtensionResponse.Error error = new ExtensionResponse.Error(
                    "Error processing health check data: " + e.getMessage(),
                    System.currentTimeMillis(),
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR,
                    errorDetails
            );
            
            // Create a default health status for error case
            HealthStatus healthStatus = new HealthStatus();
            healthStatus.extensionName = "Outlook";
            healthStatus.systemStatus = "ERROR";
            healthStatus.lastHealthCheckTime = System.currentTimeMillis();
            
            long endTime = System.currentTimeMillis();
            Map<String, Object> extensionResponse = getExtensionResponse(false, healthStatus, (endTime - startTime) / 1000.0);
            
            // Use the error object directly
            return new ExtensionResponse(ExtensionResponse.Result.FAILURE, extensionResponse, error, null, null);
        }
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
     * @param isHealthy Whether the system is healthy
     * @param healthStatus The health status entity
     * @param timeTakenInSeconds Time taken to perform the health check
     * @return A map containing the extension response
     */
    private Map<String, Object> getExtensionResponse(boolean isHealthy, HealthStatus healthStatus, double timeTakenInSeconds) {
        String responseMessage = isHealthy
                ? "Health check completed successfully. All systems operational."
                : "Health check completed with issues. System status: " + healthStatus.systemStatus;
        
        ExtensionResponseMeta extensionResponseMeta = new ExtensionResponseMeta();
        extensionResponseMeta.message = responseMessage;
        extensionResponseMeta.technicalDetailedErrorReport = isHealthy ? "" : "System is not healthy";
        extensionResponseMeta.responseType = isHealthy ? "SUCCESS" : "FAILED";
        extensionResponseMeta.timeTakenInSeconds = timeTakenInSeconds;
        
        return Map.of(
                "Health Status", healthStatus,
                "Is Healthy", isHealthy,
                "Extension Response Meta", extensionResponseMeta
        );
    }
}
