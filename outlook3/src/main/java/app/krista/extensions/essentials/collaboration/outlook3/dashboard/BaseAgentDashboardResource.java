package app.krista.extensions.essentials.collaboration.outlook3.dashboard;

import app.krista.extension.executor.Invoker;
import app.krista.extension.request.RoutingInfo;
import app.krista.extension.request.protos.http.HttpProtocol;
import com.google.gson.Gson;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


/**
 * Base class for Custom Agent dashboard REST resources.
 * Provides common metadata, health, and logs APIs for @CustomAgent extensions.
 *
 * <p>Extension developers should extend this class and add extension-specific
 * dashboard endpoints as needed.</p>
 */
public abstract class BaseAgentDashboardResource {

    protected static final Gson gson = new Gson();
    protected final Invoker invoker;
    protected final long startTime;
    protected final String extensionVersion;

    /**
     * Constructor for base dashboard resource.
     *
     * @param invoker the invoker instance injected by the framework
     */
    protected BaseAgentDashboardResource(Invoker invoker) {

        this.invoker = invoker;
        this.startTime = System.currentTimeMillis();
        this.extensionVersion = loadExtensionVersion();
    }

    /**
     * Load extension version from META-INF/krista/extension.json file.
     * Falls back to release.properties if extension.json is not found.
     *
     * @return extension version string, or "1.0.0" if not found
     */
    private String loadExtensionVersion() {

        // Try to load from extension.json first
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("META-INF/krista/extension.json")) {

            if (input != null) {

                StringBuilder jsonBuilder = new StringBuilder();
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = input.read(buffer)) != -1) {

                    jsonBuilder.append(new String(buffer, 0, bytesRead));
                }

                String json = jsonBuilder.toString();

                int versionIndex = json.indexOf("\"version\"");
                if (versionIndex != -1) {

                    int colonIndex = json.indexOf(":", versionIndex);
                    int openQuoteIndex = json.indexOf("\"", colonIndex + 1);
                    int closeQuoteIndex = json.indexOf("\"", openQuoteIndex + 1);

                    if (openQuoteIndex != -1 && closeQuoteIndex != -1) {

                        String version = json.substring(openQuoteIndex + 1, closeQuoteIndex);
                        if (!version.isEmpty()) {

                            return version;
                        }
                    }
                }
            }
        } catch (Exception e) {

            System.err.println("Could not load extension version from extension.json: " + e.getMessage());
        }

        // Fallback to release.properties
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("release.properties")) {

            if (input != null) {

                Properties props = new Properties();
                props.load(input);
                String version = props.getProperty("extension.version");
                if (version != null && !version.isEmpty()) {

                    return version;
                }
            }
        } catch (Exception e) {

            System.err.println("Could not load extension version from release.properties: " + e.getMessage());
        }

        return "1.0.0";
    }

    /**
     * Metadata API endpoint.
     * Returns extension metadata including name, ID, version, invoker info, routing info.
     *
     * @return JSON response with extension metadata
     */
    @GET
    @Path("/api/metadata")
    @Produces(MediaType.APPLICATION_JSON)
    public String getMetadata() {

        Map<String, Object> metadata = new HashMap<>();

        // Invoker information
        metadata.put("invokerId", invoker.getInvokerId());
        metadata.put("invokerName", invoker.getInvokerName());
        metadata.put("workspaceId", invoker.getWorkspaceId());

        // Routing information
        RoutingInfo routingInfo = invoker.getRoutingInfo();
        metadata.put("routingId", routingInfo.getRoutingId());
        metadata.put("nodeName", routingInfo.getNodeName());

        // Extension information (from Extension interface)
        metadata.put("extensionId", invoker.getExtension().getExtensionId());
        metadata.put("extensionVersion", extensionVersion);

        // Timestamps
        metadata.put("startedAt", Instant.ofEpochMilli(startTime).toString());

        // External system info (extension-specific)
        Map<String, Object> externalSystem = getExternalSystemInfo();
        if (externalSystem != null && !externalSystem.isEmpty()) {
            metadata.put("externalSystem", externalSystem);
        }

        // Invoker attributes
        metadata.put("attributes", invoker.getAttributes());

        Map<String, Object> wrapper = new HashMap<>();
        wrapper.put("payload", metadata);
        return gson.toJson(wrapper);
    }

    /**
     * Health API endpoint.
     * Returns extension health status, uptime, metrics, and external system health.
     *
     * @return JSON response with health information
     */
    @GET
    @Path("/api/health")
    @Produces(MediaType.APPLICATION_JSON)
    public String getHealth() {

        Map<String, Object> health = new HashMap<>();

        // Overall status
        health.put("status", determineHealthStatus());

        // Uptime
        long uptimeMillis = System.currentTimeMillis() - startTime;
        health.put("uptime", uptimeMillis);
        health.put("lastHeartbeat", Instant.now().toString());

        // Request metrics (extension-specific)
        Map<String, Object> metrics = getRequestMetrics();
        if (metrics != null && !metrics.isEmpty()) {
            health.put("metrics", metrics);
        }

        // External system health (extension-specific)
        Map<String, Object> externalHealth = checkExternalSystemHealth();
        if (externalHealth != null && !externalHealth.isEmpty()) {
            health.put("externalSystemHealth", externalHealth);
        }

        // Process health
        health.put("processHealth", getProcessHealth());

        Map<String, Object> wrapper = new HashMap<>();
        wrapper.put("payload", health);
        return gson.toJson(wrapper);
    }

    /**
     * Logs API endpoint.
     * Reads log files directly from the local filesystem instead of routing through the platform RPC.
     *
     * @param type log type: TRANSACTION_LOG (default) or DEBUG_LOG
     * @param tail optional number of last lines to return (0 = all lines)
     * @return JSON response with log file content
     */
    @GET
    @Path("/api/logs")
    @Produces(MediaType.APPLICATION_JSON)
    public String getLogs(
            @QueryParam("type") @DefaultValue("TRANSACTION_LOG") String type,
            @QueryParam("tail") @DefaultValue("0") int tail) {

        Map<String, Object> payload = new HashMap<>();
        payload.put("logType", type);

        String fileName = "DEBUG_LOG".equals(type) ? "debug.log" : "transaction.log";
        File logFile = resolveLogFile(fileName);

        if (logFile == null || !logFile.exists() || logFile.length() == 0) {

            payload.put("logs", "");
            Map<String, Object> wrapper = new HashMap<>();
            wrapper.put("payload", payload);
            return gson.toJson(wrapper);
        }

        try {

            String content;
            if (tail > 0) {
                content = readTailLines(logFile, tail);
            } else {
                content = readFileContent(logFile);
            }
            payload.put("logs", content);
        } catch (Exception e) {

            payload.put("logs", "Error reading log file: " + e.getMessage());
        }

        Map<String, Object> wrapper = new HashMap<>();
        wrapper.put("payload", payload);
        return gson.toJson(wrapper);
    }

    /**
     * Resolves the log file path using the same logic as DiagnosticFiles.
     * Checks logDir system property first, then falls back to the container path.
     *
     * @param fileName the log file name (e.g., "transaction.log" or "debug.log")
     * @return the resolved File, or null if no valid path is found
     */
    private File resolveLogFile(String fileName) {

        String logDir = System.getProperty("logDir");
        if (logDir != null && !logDir.isBlank()) {

            File file = new File(logDir + File.separatorChar + fileName);
            if (file.exists()) {
                return file;
            }
        }

        File containerFile = new File("/opt/krista/var/log/" + fileName);
        if (containerFile.exists()) {
            return containerFile;
        }

        return null;
    }

    /**
     * Reads the entire content of a log file.
     *
     * @param file the file to read
     * @return file content as a string
     */
    private String readFileContent(File file) throws Exception {

        byte[] bytes = java.nio.file.Files.readAllBytes(file.toPath());
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     * Reads the last N lines from a file by scanning backwards from the end.
     *
     * @param file the file to read
     * @param lines number of lines to return
     * @return the last N lines as a string
     */
    private String readTailLines(File file, int lines) throws Exception {

        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {

            long fileLength = raf.length();
            if (fileLength == 0) {
                return "";
            }

            int lineCount = 0;
            long pos = fileLength - 1;

            // Skip trailing newline if present
            raf.seek(pos);
            if (raf.readByte() == '\n') {
                pos--;
            }

            // Scan backwards counting newlines
            while (pos >= 0 && lineCount < lines) {

                raf.seek(pos);
                if (raf.readByte() == '\n') {
                    lineCount++;
                }
                pos--;
            }

            // Position to start of the content we want
            long startPos = (pos < 0) ? 0 : pos + 2;
            raf.seek(startPos);

            byte[] buffer = new byte[(int) (fileLength - startPos)];
            raf.readFully(buffer);
            return new String(buffer, StandardCharsets.UTF_8);
        }
    }

    /**
     * Returns external system information for the metadata API.
     * Override this method to provide extension-specific external system details.
     *
     * @return map containing external system info (name, baseUrl, etc.)
     */
    protected abstract Map<String, Object> getExternalSystemInfo();

    /**
     * Determines the overall health status of the extension.
     * Override this method to implement custom health logic.
     *
     * @return health status: HEALTHY, DEGRADED, UNHEALTHY, or UNKNOWN
     */
    protected String determineHealthStatus() {

        try {

            if (invoker.isRemoved()) {
                return "UNHEALTHY";
            }

            Map<String, Object> externalHealth = checkExternalSystemHealth();
            if (externalHealth != null && externalHealth.containsKey("status")) {
                String externalStatus = (String) externalHealth.get("status");
                if ("DISCONNECTED".equals(externalStatus) || "ERROR".equals(externalStatus)) {
                    return "DEGRADED";
                }
            }

            return "HEALTHY";
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }

    /**
     * Returns request metrics for the health API.
     * Override this method to provide extension-specific metrics.
     *
     * @return map containing request metrics
     */
    protected Map<String, Object> getRequestMetrics() {

        return new HashMap<>();
    }

    /**
     * Checks external system health.
     * Override this method to implement actual health checks for external systems.
     *
     * @return map containing external system health info
     */
    protected Map<String, Object> checkExternalSystemHealth() {

        return new HashMap<>();
    }

    /**
     * Returns process health information.
     * Provides JVM memory and thread information.
     *
     * @return map containing process health metrics
     */
    protected Map<String, Object> getProcessHealth() {

        Map<String, Object> processHealth = new HashMap<>();

        Runtime runtime = Runtime.getRuntime();
        long memoryUsed = runtime.totalMemory() - runtime.freeMemory();
        long memoryMax = runtime.maxMemory();

        processHealth.put("memoryUsed", memoryUsed);
        processHealth.put("memoryMax", memoryMax);
        processHealth.put("memoryUsedPercent", (memoryUsed * 100) / memoryMax);
        processHealth.put("threadCount", Thread.activeCount());

        return processHealth;
    }

    /**
     * Formats uptime in milliseconds to human-readable format.
     *
     * @param uptimeMillis uptime in milliseconds
     * @return formatted uptime string (e.g., "2h 15m 30s")
     */
    protected String formatUptime(long uptimeMillis) {

        long seconds = uptimeMillis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return String.format("%dd %dh %dm", days, hours % 24, minutes % 60);
        } else if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes % 60, seconds % 60);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds % 60);
        } else {
            return String.format("%ds", seconds);
        }
    }

}
