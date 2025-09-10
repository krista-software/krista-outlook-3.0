package app.krista.extensions.essentials.collaboration.outlook3.catalog;

import app.krista.extension.executor.*;
import app.krista.extension.impl.anno.CatalogRequest;
import app.krista.extension.impl.anno.*;
import app.krista.extensions.essentials.collaboration.outlook3.health.HealthCheck;
import app.krista.extensions.essentials.collaboration.outlook3.impl.util.SaveConfigurationImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

@Domain(id = "catEntryDomain_5fa2fc97-4b17-44cf-b98f-aa91a459a091",
        name = "Collaboration",
        ecosystemId = "catEntryEcosystem_84b53163-327b-4b1b-8c96-9334d292f9f5",
        ecosystemName = "Essentials",
        ecosystemVersion = "dabc7c4e-cb1c-4dbf-bba6-be1c9a75438d")
public class SetupArea {

    public static final Logger LOGGER = LoggerFactory.getLogger(SetupArea.class);

    private final HealthCheck healthCheck;
    private final SaveConfigurationImpl saveConfigurationImpl;

    @Inject
    public SetupArea(HealthCheck healthCheck, SaveConfigurationImpl saveConfigurationImpl) {
        this.healthCheck = healthCheck;
        this.saveConfigurationImpl = saveConfigurationImpl;
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
    @Field.Boolean(name = "Is Configuration Successful", required = false, attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'Returns true if Outlook Private configuration is successful.'")}, options = {})
    @Field.Desc(name = "Extension Response Meta", type = "Entity(Extension Response Meta)",required = false)
    public ExtensionResponse saveOutlookPublicConfiguration(
            @Field(name = "Email", type = "Email", required = true, attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'The primary Outlook email address used for authentication and integration.'")}, options = {}) String email,
            @Field.Boolean(name = "Allow Mail Alert", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) Boolean allowMailAlert) {
        return saveConfigurationImpl.outlookPublicConfiguration(email,allowMailAlert);
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
            @Field(name = "Email", type = "Email", required = true, attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'The primary Outlook email address used for authentication and integration.'")}, options = {}) String email,
            @Field.Text(name = "Client ID", required = true, attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'The Application (Client) ID from Azure AD used to authenticate Outlook integration.'")}, options = {}) String clientID,
            @Field.Text(name = "Client Secret", required = true, attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'The Client Secret generated in Azure AD for Outlook integration. Keep this value secure.'")}, options = {}) String clientSecret,
            @Field.Text(name = "Tenant ID", required = true, attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'The Directory (Tenant) ID of your Microsoft 365 organization required for Outlook authentication.'")}, options = {}) String tenantID,
            @Field.Boolean(name = "Allow Mail Alert", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) Boolean allowMailAlert) {
        return saveConfigurationImpl.outlookPrivateConfiguration(email,clientID,clientSecret,tenantID,allowMailAlert);
    }
}
