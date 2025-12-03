package no.nav.folketrygdloven.kalkulus.web.app.metrics;

import io.swagger.v3.oas.annotations.Operation;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import no.nav.vedtak.log.metrics.MetricsUtil;

@Path("/metrics")
@Produces(MediaType.TEXT_PLAIN)
@ApplicationScoped
public class PrometheusRestService {

    @GET
    @Operation(tags = "metrics", hidden = true)
    @Path("/prometheus")
    public String prometheus() {
        return MetricsUtil.scrape();
    }
}
