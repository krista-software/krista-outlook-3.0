package app.krista.extensions.essentials.collaboration.outlook3.api;

import org.jvnet.hk2.annotations.ContractsProvided;
import org.jvnet.hk2.annotations.Service;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.Set;

@Service
@ApplicationPath("outlook")
@ContractsProvided(Application.class)
public class OutlookApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        return Set.of(AuthenticationResource.class);
    }

}
