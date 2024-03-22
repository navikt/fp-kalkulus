package no.nav.folketrygdloven.kalkulus.app.konfig;

import java.util.Set;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

import no.nav.folketrygdloven.kalkulus.app.metrics.PrometheusRestService;
import no.nav.folketrygdloven.kalkulus.app.selftest.NaisRestTjeneste;

@ApplicationPath(InternalApplication.API_URI)
public class InternalApplication extends Application {

    public static final String API_URI = "/internal";

    public InternalApplication() {

    }

    @Override
    public Set<Class<?>> getClasses() {
        return Set.of(PrometheusRestService.class,
            NaisRestTjeneste.class);
    }

}
