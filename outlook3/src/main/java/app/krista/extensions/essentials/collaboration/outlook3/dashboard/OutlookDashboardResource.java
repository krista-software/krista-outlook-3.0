package app.krista.extensions.essentials.collaboration.outlook3.dashboard;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import app.krista.extension.executor.Invoker;
import app.krista.extension.request.RoutingInfo;
import app.krista.extension.request.protos.http.HttpProtocol;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Outlook Custom Agent Dashboard Resource.
 * Extends BaseAgentDashboardResource to provide common dashboard APIs
 * and adds Outlook-specific endpoints.
 */
@Path("/")
public class OutlookDashboardResource extends BaseAgentDashboardResource {

    @Inject
    public OutlookDashboardResource(Invoker invoker) {

        super(invoker);
    }

    /**
     * Provides Outlook external system information for the metadata API.
     */
    @Override
    protected Map<String, Object> getExternalSystemInfo() {

        Map<String, Object> info = new HashMap<>();
        info.put("name", "Microsoft Graph API");
        info.put("baseUrl", "https://graph.microsoft.com/v1.0");
        return info;
    }

    /**
     * Returns the hosting URL for the extension.
     * Required by @CustomAgent annotation for Studio integration.
     */
    @GET
    @Path("/hostUrl")
    @Produces(MediaType.APPLICATION_JSON)
    public String getHostingUrl() {

        Map<String, Object> wrapper = new HashMap<>();
        Map<String, String> payload = new HashMap<>();
        String routingURL = invoker.getRoutingInfo().getRoutingURL(HttpProtocol.PROTOCOL_NAME, RoutingInfo.Type.APPLIANCE);
        payload.put("hostingUrl", routingURL + "/rest/");
        wrapper.put("payload", payload);
        return gson.toJson(wrapper);
    }

    /**
     * Serves the main dashboard page using the common template.
     */
    @GET
    @Path("/")
    @Produces(MediaType.TEXT_HTML)
    public Response getIndex() {

        return serveStaticFile("dashboard-template/index.html", MediaType.TEXT_HTML);
    }

    /**
     * Serves the dashboard page (alias for root path).
     */
    @GET
    @Path("/dashboard")
    @Produces(MediaType.TEXT_HTML)
    public Response getDashboard() {

        return serveStaticFile("dashboard-template/index.html", MediaType.TEXT_HTML);
    }

    /**
     * Serves static files from the classpath.
     *
     * @param resourcePath path to the resource file
     * @param mediaType media type of the file
     * @return Response with file content or error
     */
    private Response serveStaticFile(String resourcePath, String mediaType) {

        try {

            InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath);
            if (is == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("File not found: " + resourcePath)
                        .build();
            }

            byte[] bytes = is.readAllBytes();
            String content = new String(bytes, StandardCharsets.UTF_8);

            return Response.ok(content)
                    .type(mediaType)
                    .build();
        } catch (Exception e) {

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error reading file: " + e.getMessage())
                    .build();
        }
    }

}
