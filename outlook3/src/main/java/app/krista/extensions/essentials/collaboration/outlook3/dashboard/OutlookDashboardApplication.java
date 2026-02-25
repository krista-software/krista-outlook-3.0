package app.krista.extensions.essentials.collaboration.outlook3.dashboard;

import org.jvnet.hk2.annotations.ContractsProvided;
import org.jvnet.hk2.annotations.Service;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.Set;

/**
 * JAX-RS Application for the Outlook Custom Agent Dashboard.
 * Registers the dashboard resource at the root path.
 */
@Service
@ApplicationPath("/")
@ContractsProvided(Application.class)
public class OutlookDashboardApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {

        return Set.of(OutlookDashboardResource.class);
    }

}
