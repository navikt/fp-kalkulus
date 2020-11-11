package no.nav.folketrygdloven.kalkulus.rest;

import static no.nav.folketrygdloven.kalkulus.sikkerhet.KalkulusBeskyttetRessursAttributt.DRIFT;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.CREATE;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag.MigrerFaktaTjeneste;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;

@Produces(MediaType.APPLICATION_JSON)
@OpenAPIDefinition(tags = @Tag(name = "forvaltning"))
@Path("/forvaltning")
@ApplicationScoped
@Transactional
public class ForvaltningKalkulusRestTjeneste {

    private MigrerFaktaTjeneste migrerFaktaTjeneste;

    public ForvaltningKalkulusRestTjeneste() {
        // for CDI
    }

    @Inject
    public ForvaltningKalkulusRestTjeneste(MigrerFaktaTjeneste migrerFaktaTjeneste) {
        this.migrerFaktaTjeneste = migrerFaktaTjeneste;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/migrerFakta")
    @Operation(description = "Migrerer fakta", tags = "forvaltning", summary = ("Migrerer fakta"))
    @BeskyttetRessurs(action = CREATE, resource = DRIFT, sporingslogg = false)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response migrerFakta() {
        migrerFaktaTjeneste.migrerFakta();
        return Response.ok().build();
    }

}
