package no.nav.folketrygdloven.kalkulus.app.healthcheck;

import static jakarta.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;

import java.util.List;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.v3.oas.annotations.Operation;
import no.nav.vedtak.server.LivenessAware;
import no.nav.vedtak.server.ReadinessAware;

@Path("/health")
@RequestScoped
public class HealthCheckRestService {

    private static final CacheControl CC = cacheControl();
    private static final Logger LOG = LoggerFactory.getLogger(HealthCheckRestService.class);
    private static final String RESPONSE_OK = "OK";

    private List<LivenessAware> live;
    private List<ReadinessAware> ready;

    HealthCheckRestService() {
        // CDI
    }

    @Inject
    public HealthCheckRestService(@Any Instance<LivenessAware> livenessAware,
                                  @Any Instance<ReadinessAware> readinessAware) {
        this(livenessAware.stream().toList(), readinessAware.stream().toList());
    }

    public HealthCheckRestService(List<LivenessAware> live, List<ReadinessAware> ready) {
        this.live = live;
        this.ready = ready;
    }

    private static CacheControl cacheControl() {
        var cc = new CacheControl();
        cc.setNoCache(true);
        cc.setNoStore(true);
        cc.setMustRevalidate(true);
        return cc;
    }

    @GET
    @Path("/isAlive")
    @Operation(description = "Sjekker om poden lever", tags = "nais", hidden = true)
    public Response isAlive() {
        if (live.stream().allMatch(LivenessAware::isAlive)) {
            return Response.ok(RESPONSE_OK).cacheControl(CC).build();
        }
        LOG.info("/isAlive NOK.");
        return Response.serverError().cacheControl(CC).build();
    }

    @GET
    @Path("/isReady")
    @Operation(description = "Sjekker om poden er klar", tags = "nais", hidden = true)
    public Response isReady() {
        if (ready.stream().allMatch(ReadinessAware::isReady)) {
            return Response.ok(RESPONSE_OK).cacheControl(CC).build();
        }
        LOG.info("/isReady NOK.");
        return Response.status(SERVICE_UNAVAILABLE).cacheControl(CC).build();
    }

}
