package app.krista.extensions.essentials.collaboration.outlook3.catalog;

import java.util.Map;
import java.util.List;
import app.krista.extension.executor.*;
import app.krista.extension.impl.anno.CatalogRequest;
import app.krista.extension.impl.anno.*;
import app.krista.extensions.essentials.collaboration.outlook3.health.HealthCheck;
import app.krista.ksdk.entities.EntityReference;
import app.krista.model.base.*;
import javax.inject.Inject;

@Domain(id = "catEntryDomain_5fa2fc97-4b17-44cf-b98f-aa91a459a091",
        name = "Collaboration",
        ecosystemId = "catEntryEcosystem_84b53163-327b-4b1b-8c96-9334d292f9f5",
        ecosystemName = "Essentials",
        ecosystemVersion = "dabc7c4e-cb1c-4dbf-bba6-be1c9a75438d")
public class SetupArea {

    private final HealthCheck healthCheck;

    @Inject
    public SetupArea(HealthCheck healthCheck) {
        this.healthCheck = healthCheck;
    }

    @CatalogRequest(
            id = "localDomainRequest_6fa660ef-642a-495e-9d92-49948812253b",
            name = "Health Check",
            description = "Request Description",
            area = "Setup",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Desc(name = "Extension Response Meta", type = "Entity(Extension Response Meta)", required = false)
    public ExtensionResponse healthCheck() {
        return healthCheck.checkHealth();
    }
}