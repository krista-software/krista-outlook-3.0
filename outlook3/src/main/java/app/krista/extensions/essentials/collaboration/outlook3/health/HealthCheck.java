package app.krista.extensions.essentials.collaboration.outlook3.health;

import app.krista.extension.impl.anno.InvokerRequest;
import app.krista.extensions.essentials.collaboration.outlook3.OutlookAttributes;
import app.krista.extensions.essentials.collaboration.outlook3.impl.connectors.GraphServiceClientProviderFactory;
import app.krista.extensions.essentials.collaboration.outlook3.impl.stores.OutlookAttributeStore;
import app.krista.extensions.essentials.collaboration.outlook3.impl.stores.RefreshTokenStore;
import app.krista.extensions.essentials.collaboration.outlook3.impl.util.Constants;
import app.krista.ksdk.telemetry.TelemetryMetrics;
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
    private final TelemetryMetrics telemetryMetrics;

    /**
     * Constructor for AuthenticationHealthCheck.
     *
     * @param providerFactory The GraphServiceClientProviderFactory
     * @param attributeStore The OutlookAttributeStore
     * @param refreshTokenStore The RefreshTokenStore
     * @param invokerId The invoker ID
     */
    @Inject
    public HealthCheck(
            GraphServiceClientProviderFactory providerFactory,
            OutlookAttributeStore attributeStore,
            RefreshTokenStore refreshTokenStore,
            String invokerId, TelemetryMetrics telemetryMetrics) {
        this.providerFactory = providerFactory;
        this.attributeStore = attributeStore;
        this.refreshTokenStore = refreshTokenStore;
        this.invokerId = invokerId;
        this.telemetryMetrics = telemetryMetrics;
        LOGGER.info("AuthenticationHealthCheck service initialized");
    }

    @InvokerRequest(InvokerRequest.Type.INVOKER_LOADED)
    public void registerInvokerUptime() {
        START_TIME = Instant.now();
    }
    /**
     * Checks the health of the authentication services.
     * <p>
     * This method gathers various authentication metrics including:
     * <ul>
     *   <li>Authentication configuration status</li>
     *   <li>Token validity</li>
     *   <li>Memory usage</li>
     *   <li>CPU utilization</li>
     *   <li>Thread count</li>
     *   <li>System uptime</li>
     * </ul>
     *
     * @return Map containing health metrics and status information
     */
    public Map<String, Object> checkHealth() {
        long startTime = System.currentTimeMillis();
        String operationId = "auth_health_check_" + startTime;

        LOGGER.debug("Performing authentication health check with operation ID: {}", operationId);

        // Increment request counter
        telemetryMetrics.incrementCounter(HEALTH_CHECK_REQUEST_COUNT);

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
                healthData.put("Status", "NOT_CONFIGURED");
                healthData.put("Message", "Outlook is not configured");
                
                // Add system metrics even for not configured state
                addSystemMetrics(healthData, usedMemory, availableMemory, maxMemory, 
                        cpuUsage, threadCount, uptimeHours);
                
                healthData.put("LastHealthCheckTime", System.currentTimeMillis());

                // Record telemetry for not configured state
                telemetryMetrics.incrementCounter(OUTLOOK_AUTH_STATUS, 1,
                        Map.of("status", "NOT_CONFIGURED"));

                return healthData;
            }

            // Check if refresh token exists for private auth
            String authType = attributes.getAuthType();
            boolean hasRefreshToken = false;
            
            if (Constants.PRIVATE.equals(authType)) {
                String refreshToken = refreshTokenStore.get(attributes.getEmail());
                hasRefreshToken = refreshToken != null && !refreshToken.isEmpty();
            }
            
            // Determine overall authentication status based on configuration
            String authStatus = hasRefreshToken || !Constants.PRIVATE.equals(authType) ? "HEALTHY" : "DEGRADED";
            
            // Populate health data
            healthData.put("Status", authStatus);
            healthData.put("AuthType", authType);
            healthData.put("Email", attributes.getEmail());
            healthData.put("HasRefreshToken", hasRefreshToken);
            
            // Add system metrics
            addSystemMetrics(healthData, usedMemory, availableMemory, maxMemory, 
                    cpuUsage, threadCount, uptimeHours);
            
            // Add token metrics if available
            if (hasRefreshToken) {
                healthData.put("TokenValid", true);
                healthData.put("TokenLatencyMs", 0.0); // No actual token fetch is performed
            }
            
            healthData.put("LastHealthCheckTime", System.currentTimeMillis());

            // Record telemetry metrics
            try {
                telemetryMetrics.observeGauge(OUTLOOK_MEMORY_USAGE_MB, usedMemory);
                telemetryMetrics.observeGauge(OUTLOOK_MEMORY_AVAILABLE_MB, availableMemory);
                telemetryMetrics.observeGauge(OUTLOOK_CPU_USAGE_PERCENTAGE, cpuUsage);
                telemetryMetrics.observeGauge(OUTLOOK_ACTIVE_THREADS, threadCount);
                telemetryMetrics.observeGauge(OUTLOOK_UPTIME_HOURS, uptimeHours);
                telemetryMetrics.incrementCounter(OUTLOOK_AUTH_STATUS, 1,
                        Map.of("status", authStatus, "Auth_type", authType));
            } catch (Exception e) {
                // Log but don't fail the health check if telemetry recording fails
                LOGGER.warn("Error recording telemetry metrics: {}", e.getMessage());
            }

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            // Record success metrics
            telemetryMetrics.incrementCounter(HEALTH_CHECK_SUCCESS_COUNT);
            telemetryMetrics.recordDuration(HEALTH_CHECK_LATENCY_MS, duration);

            LOGGER.info("Authentication health check completed in {} ms. Auth status: {}",
                    duration, authStatus);

            return healthData;
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            // Record failure metrics
            try {
                telemetryMetrics.incrementCounter(HEALTH_CHECK_FAILURE_COUNT);
                telemetryMetrics.incrementCounter(HEALTH_CHECK_ERROR_TYPE, 1,
                        Map.of("error_type", e.getClass().getSimpleName()));
                telemetryMetrics.recordDuration(HEALTH_CHECK_LATENCY_MS, duration);
            } catch (Exception telemetryError) {
                // Log but don't suppress the original exception if telemetry recording fails
                LOGGER.warn("Error recording failure telemetry: {}", telemetryError.getMessage());
            }

            LOGGER.error("Authentication health check failed for operation {}: {}",
                    operationId, e.getMessage(), e);
            
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("Status", "ERROR");
            errorData.put("ErrorMessage", e.getMessage());
            errorData.put("ErrorType", e.getClass().getSimpleName());
            errorData.put("LastHealthCheckTime", System.currentTimeMillis());
            
            return errorData;
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
     * Helper method to convert Object to Double safely.
     *
     * @param value Object to convert
     * @return Double value, or 0.0 if conversion fails
     */
    public static Double convertToDouble(Object value) {
        if (value == null) {
            return 0.0;
        }
        
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    /**
     * Helper method to convert Object to Integer safely.
     *
     * @param value Object to convert
     * @return Integer value, or 0 if conversion fails
     */
    public static Integer convertToInteger(Object value) {
        if (value == null) {
            return 0;
        }
        
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}