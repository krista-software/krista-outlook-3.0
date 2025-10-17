package app.krista.extensions.essentials.collaboration.outlook3.catalog;

import app.krista.extension.executor.*;
import app.krista.extension.impl.anno.*;
import app.krista.extension.request.RoutingInfo;
import app.krista.extension.request.protos.http.HttpProtocol;
import app.krista.extensions.essentials.collaboration.outlook3.OutlookAttributes;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.entities.ExtensionResponseMeta;
import app.krista.extensions.essentials.collaboration.outlook3.health.HealthCheck;
import app.krista.extensions.essentials.collaboration.outlook3.impl.AccountImpl;
import app.krista.extensions.essentials.collaboration.outlook3.impl.FolderMonitoringSubscription;
import app.krista.extensions.essentials.collaboration.outlook3.impl.SaveConfigurationImpl;
import app.krista.extensions.essentials.collaboration.outlook3.impl.connectors.GraphServiceClientProviderFactory;
import app.krista.extensions.essentials.collaboration.outlook3.impl.stores.OutlookAttributeStore;
import app.krista.extensions.essentials.collaboration.outlook3.impl.util.Constants;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Domain(id = "catEntryDomain_5fa2fc97-4b17-44cf-b98f-aa91a459a091",
        name = "Collaboration",
        ecosystemId = "catEntryEcosystem_84b53163-327b-4b1b-8c96-9334d292f9f5",
        ecosystemName = "Essentials",
        ecosystemVersion = "dabc7c4e-cb1c-4dbf-bba6-be1c9a75438d")
public class SetupArea {

    public static final Logger LOGGER = LoggerFactory.getLogger(SetupArea.class);

    private final HealthCheck healthCheck;
    private final SaveConfigurationImpl saveConfigurationImpl;
    private final String baseRoutingUrl;
    private final Invoker invoker;
    private final OutlookAttributeStore outlookAttributeStore;
    private final GraphServiceClientProviderFactory providerFactory;


    @Inject
    public SetupArea(HealthCheck healthCheck, SaveConfigurationImpl saveConfigurationImpl, Invoker invoker,
                     OutlookAttributeStore outlookAttributeStore, GraphServiceClientProviderFactory providerFactory) {
        this.healthCheck = healthCheck;
        this.saveConfigurationImpl = saveConfigurationImpl;
        this.baseRoutingUrl = invoker.getRoutingInfo().getRoutingURL(HttpProtocol.PROTOCOL_NAME, RoutingInfo.Type.APPLIANCE);
        this.invoker = invoker;
        this.outlookAttributeStore = outlookAttributeStore;
        this.providerFactory = providerFactory;
    }

    @CatalogRequest(
            id = "localDomainRequest_6fa660ef-642a-495e-9d92-49948812253b",
            name = "Health Check",
            description = "This 'Health Check' request verifies the health status of the appliance by calling the health check API. It returns a boolean response indicating overall health status along with detailed system resource information including memory usage, CPU utilization, and other vital metrics.",
            area = "Setup",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Desc(name = "Extension Response Meta", type = "Entity(Extension Response Meta)", required = false)
    @Field.Desc(name = "Health Status", type = "Entity(Health Status)", required = false)
    @Field.Boolean(name = "Is Healthy", required = false, attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'Returns true if the appliance health check is successful and all systems are operating normally, otherwise false with details'")}, options = {})
    public ExtensionResponse healthCheck() {
        return healthCheck.checkHealth();
    }

    @CatalogRequest(
            id = "localDomainRequest_ba0e6c90-40ed-49d4-92d9-b88f7281d094",
            name = "Save Outlook Public Configuration",
            description = "Configure Outlook authentication with Public access. Requires only Email and optional Mail Alert setting.",
            area = "Setup",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Boolean(name = "Is Configuration Successful", required = false, attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'Returns true if Outlook Private configuration is successful.'")})
    @Field.Desc(name = "Extension Response Meta", type = "Entity(Extension Response Meta)",required = false)
    public ExtensionResponse saveOutlookPublicConfiguration(
            @Field(name = "Email", type = "Email", required = true, attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'The primary Outlook email address used for authentication and integration.'")}) String email,
            @Field.Boolean(name = "Allow Mail Alert", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) Boolean allowMailAlert) {
        LOGGER.info("Saving Outlook Public Configuration: {}", email);
        JsonObject publicPayload = OutlookAttributes.createJsonAttributes(email, null, null, null, allowMailAlert, Constants.PUBLIC, null);
        return saveConfigurationImpl.saveConfiguration(publicPayload);
    }

    @CatalogRequest(
            id = "localDomainRequest_40918025-16e1-4b3e-9e5c-9bf4552a7af5",
            name = "Save Outlook Private Configuration",
            description = "Configure Outlook authentication with Private access. Requires Email, Client ID, Client Secret, and Tenant ID.",
            area = "Setup",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Boolean(name = "Is Configuration Successful", required = false, attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'Returns true if Outlook Private configuration is successful.'")}, options = {})
    @Field.Desc(name = "Extension Response Meta", type = "Entity(Extension Response Meta)",required = false)
    public ExtensionResponse saveOutlookPrivateConfiguration(
            @Field(name = "Email", type = "Email", required = true, attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'The primary Outlook email address used for authentication and integration.'")}) String email,
            @Field.Text(name = "Client ID", required = true, attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'The Application (Client) ID from Azure AD used to authenticate Outlook integration.'")}) String clientID,
            @Field.Text(name = "Client Secret", required = true, attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'The Client Secret generated in Azure AD for Outlook integration. Keep this value secure.'")}) String clientSecret,
            @Field.Text(name = "Tenant ID", required = true, attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'The Directory (Tenant) ID of your Microsoft 365 organization required for Outlook authentication.'")}) String tenantID,
            @Field.Boolean(name = "Allow Mail Alert", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) Boolean allowMailAlert) {
        LOGGER.info("Saving Outlook Private Configuration: {}", email);
        JsonObject privatePayload = OutlookAttributes.createJsonAttributes(email, clientID, clientSecret, tenantID, allowMailAlert, Constants.PRIVATE, baseRoutingUrl);
        return saveConfigurationImpl.saveConfiguration(privatePayload);
    }

    @CatalogRequest(
            id = "localDomainRequest_8c3f4d5e-6a7b-8c9d-0e1f-2a3b4c5d6e7f",
            name = "Set Monitored Folders",
            description = "Configure which Outlook folders to monitor for email notifications. When emails arrive in or are moved into these folders, Krista will be notified and can trigger workflows. Leave empty to monitor all folders.",
            area = "Setup",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Boolean(name = "Is Configuration Successful", required = false, attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'Returns true if monitored folders configuration is successful.'")})
    @Field.Desc(name = "Extension Response Meta", type = "Entity(Extension Response Meta)", required = false)
    public ExtensionResponse setMonitoredFolders(
            @Field.Text(name = "Folder Names", required = false, attributes = {@Attribute(name = "visualWidth", value = "M"), @Attribute(name = "toolTip", value = "'Comma-separated list of folder names to monitor (e.g., \"Krista Inbox, Action Items, Need Human Review\"). Leave empty to monitor all folders.'")}) String folderNames) {
        LOGGER.info("Setting monitored folders: {}", folderNames);

        try {
            OutlookAttributes currentAttributes = outlookAttributeStore.load(invoker.getInvokerId());
            if (currentAttributes == null) {
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("Is Configuration Successful", false);
                responseData.put("Extension Response Meta", createErrorMeta("No Outlook configuration found. Please configure Outlook first."));
                return new ExtensionResponseBuilder().success(responseData).build();
            }

            // Parse folder names
            List<String> monitoredFolders = new ArrayList<>();
            if (folderNames != null && !folderNames.trim().isEmpty()) {
                String[] folders = folderNames.split(",");
                for (String folder : folders) {
                    String trimmed = folder.trim();
                    if (!trimmed.isEmpty()) {
                        monitoredFolders.add(trimmed);
                    }
                }
            }

            // Create updated attributes with monitored folders
            OutlookAttributes updatedAttributes = new OutlookAttributes(
                    currentAttributes.getClientId(),
                    currentAttributes.getClientSecret(),
                    currentAttributes.getTenantId(),
                    currentAttributes.getEmail(),
                    currentAttributes.isAllowMailAlert(),
                    currentAttributes.getAuthType(),
                    baseRoutingUrl,
                    monitoredFolders
            );

            // Save updated attributes
            outlookAttributeStore.save(updatedAttributes, invoker.getInvokerId());

            String message = monitoredFolders.isEmpty()
                    ? "Monitoring all folders (no specific folders configured)"
                    : "Monitoring " + monitoredFolders.size() + " folder(s): " + String.join(", ", monitoredFolders);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("Is Configuration Successful", true);
            responseData.put("Extension Response Meta", createSuccessMeta(message));
            return new ExtensionResponseBuilder().success(responseData).build();

        } catch (Exception e) {
            LOGGER.error("Error setting monitored folders: {}", e.getMessage(), e);
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("Is Configuration Successful", false);
            responseData.put("Extension Response Meta", createErrorMeta("Error: " + e.getMessage()));
            return new ExtensionResponseBuilder().success(responseData).build();
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_9d4e5f6a-7b8c-9d0e-1f2a-3b4c5d6e7f8g",
            name = "Get Monitored Folders",
            description = "Retrieve the list of currently monitored Outlook folders.",
            area = "Setup",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Text(name = "Monitored Folders", required = false, attributes = {@Attribute(name = "visualWidth", value = "M"), @Attribute(name = "toolTip", value = "'Comma-separated list of currently monitored folder names'")})
    @Field.Desc(name = "Extension Response Meta", type = "Entity(Extension Response Meta)", required = false)
    public ExtensionResponse getMonitoredFolders() {
        LOGGER.info("Getting monitored folders");

        try {
            OutlookAttributes attributes = outlookAttributeStore.load(invoker.getInvokerId());
            if (attributes == null) {
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("Monitored Folders", "");
                responseData.put("Extension Response Meta", createErrorMeta("No Outlook configuration found"));
                return new ExtensionResponseBuilder().success(responseData).build();
            }

            List<String> monitoredFolders = attributes.getMonitoredFolders();
            String folderList = monitoredFolders.isEmpty()
                    ? "All folders (no specific folders configured)"
                    : String.join(", ", monitoredFolders);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("Monitored Folders", folderList);
            responseData.put("Extension Response Meta", createSuccessMeta("Retrieved monitored folders successfully"));
            return new ExtensionResponseBuilder().success(responseData).build();

        } catch (Exception e) {
            LOGGER.error("Error getting monitored folders: {}", e.getMessage(), e);
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("Monitored Folders", "");
            responseData.put("Extension Response Meta", createErrorMeta("Error: " + e.getMessage()));
            return new ExtensionResponseBuilder().success(responseData).build();
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_0e5f6a7b-8c9d-0e1f-2a3b-4c5d6e7f8g9h",
            name = "List All Folders",
            description = "Retrieve all available Outlook folders that can be monitored.",
            area = "Setup",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Text(name = "Available Folders", required = false, attributes = {@Attribute(name = "visualWidth", value = "L"), @Attribute(name = "toolTip", value = "'List of all available Outlook folders'")})
    @Field.Desc(name = "Extension Response Meta", type = "Entity(Extension Response Meta)", required = false)
    public ExtensionResponse listAllFolders() {
        LOGGER.info("Listing all Outlook folders");

        try {
            AccountImpl account = new AccountImpl(providerFactory);
            List<String> folderNames = account.getFolderNames();

            String folderList = String.join(", ", folderNames);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("Available Folders", folderList);
            responseData.put("Extension Response Meta", createSuccessMeta("Retrieved " + folderNames.size() + " folder(s)"));
            return new ExtensionResponseBuilder().success(responseData).build();

        } catch (Exception e) {
            LOGGER.error("Error listing folders: {}", e.getMessage(), e);
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("Available Folders", "");
            responseData.put("Extension Response Meta", createErrorMeta("Error: " + e.getMessage()));
            return new ExtensionResponseBuilder().success(responseData).build();
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_1a2b3c4d-5e6f-7g8h-9i0j-1k2l3m4n5o6p",
            name = "Enable Folder Monitoring",
            description = "Enable enhanced folder monitoring subscription. This creates a subscription that monitors all folders and triggers on both created and updated events.",
            area = "Setup",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Boolean(name = "Is Successful", required = false, attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'Returns true if folder monitoring subscription was created successfully'")})
    @Field.Desc(name = "Extension Response Meta", type = "Entity(Extension Response Meta)", required = false)
    public ExtensionResponse enableFolderMonitoring() {
        LOGGER.info("Enabling folder monitoring subscription");

        try {
            boolean success = FolderMonitoringSubscription
                    .createOrUpdateSubscription(baseRoutingUrl, providerFactory.create());

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("Is Successful", success);

            if (success) {
                responseData.put("Extension Response Meta", createSuccessMeta("Folder monitoring subscription enabled successfully"));
            } else {
                responseData.put("Extension Response Meta", createErrorMeta("Failed to enable folder monitoring subscription"));
            }

            return new ExtensionResponseBuilder().success(responseData).build();

        } catch (Exception e) {
            LOGGER.error("Error enabling folder monitoring: {}", e.getMessage(), e);
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("Is Successful", false);
            responseData.put("Extension Response Meta", createErrorMeta("Error: " + e.getMessage()));
            return new ExtensionResponseBuilder().success(responseData).build();
        }
    }

    private ExtensionResponseMeta createSuccessMeta(String message) {
        ExtensionResponseMeta meta = new ExtensionResponseMeta();
        meta.message = message;
        meta.responseType = "SUCCESS";
        meta.timeTakenInSeconds = 0.0;
        return meta;
    }

    private ExtensionResponseMeta createErrorMeta(String message) {
        ExtensionResponseMeta meta = new ExtensionResponseMeta();
        meta.message = message;
        meta.responseType = "ERROR";
        meta.timeTakenInSeconds = 0.0;
        return meta;
    }
}
