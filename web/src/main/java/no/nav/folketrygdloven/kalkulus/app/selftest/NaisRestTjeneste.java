package no.nav.folketrygdloven.kalkulus.app.selftest;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.v3.oas.annotations.Operation;
import no.nav.folketrygdloven.kalkulus.app.konfig.ApplicationServiceStarter;
import no.nav.folketrygdloven.kalkulus.app.konfig.InternalApplication;

@Path("/")
@Produces(TEXT_PLAIN)
@RequestScoped
public class NaisRestTjeneste {

    private static final String RESPONSE_CACHE_KEY = "Cache-Control";
    private static final String RESPONSE_CACHE_VAL = "must-revalidate,no-cache,no-store";
    private static final String RESPONSE_OK = "OK";

    private static Logger logger = LoggerFactory.getLogger(InternalApplication.class);

    private ApplicationServiceStarter starterService;
    private SelftestService selftestService;

    public NaisRestTjeneste() {
        // CDI
    }

    @Inject
    public NaisRestTjeneste(ApplicationServiceStarter starterService, SelftestService selftestService) {
        this.starterService = starterService;
        this.selftestService = selftestService;
    }

    @GET
    @Path("isAlive")
    @Operation(description = "sjekker om poden lever", tags = "nais", hidden = true)
    public Response isAlive() {
        return Response
                .ok(RESPONSE_OK)
                .header(RESPONSE_CACHE_KEY, RESPONSE_CACHE_VAL)
                .build();
    }

    @GET
    @Path("isReady")
    @Operation(description = "sjekker om poden er klar", tags = "nais", hidden = true)
    public Response isReady() {
        if (!selftestService.kritiskTjenesteFeilet()) {
            logger.debug("Application is ready.");
            return Response
                .ok(RESPONSE_OK)
                .header(RESPONSE_CACHE_KEY, RESPONSE_CACHE_VAL)
                .build();
        } else {
            return Response
                .status(Response.Status.SERVICE_UNAVAILABLE)
                .header(RESPONSE_CACHE_KEY, RESPONSE_CACHE_VAL)
                .build();
        }
    }

    @GET
    @Path("preStop")
    @Operation(description = "kalles på før stopp", tags = "nais", hidden = true)
    public Response preStop() {
        starterService.stopServices();
        return Response.ok(RESPONSE_OK).build();
    }

}
