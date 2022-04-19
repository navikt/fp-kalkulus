package no.nav.folketrygdloven.kalkulus.rest.forvaltning;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import no.nav.folketrygdloven.kalkulus.rest.forvaltning.dump.DebugDumpsters;
import no.nav.folketrygdloven.kalkulus.rest.forvaltning.dump.KortTekst;
import no.nav.folketrygdloven.kalkulus.sikkerhet.KalkulusBeskyttetRessursAttributt;
import no.nav.k9.felles.sikkerhet.abac.BeskyttetRessurs;
import no.nav.k9.felles.sikkerhet.abac.BeskyttetRessursActionAttributt;

@Path(DiagnostikkBeregningRestTjeneste.BASE_PATH)
@OpenAPIDefinition(tags = @Tag(name = "diagnostikk"))
@ApplicationScoped
@Transactional
public class DiagnostikkBeregningRestTjeneste {

    static final String BASE_PATH = "/diagnostikk";

    private DebugDumpsters dumpsters;
    private EntityManager entityManager;


    public DiagnostikkBeregningRestTjeneste() {
    }

    @Inject
    public DiagnostikkBeregningRestTjeneste(DebugDumpsters dumpsters, EntityManager entityManager) {
        this.dumpsters = dumpsters;
        this.entityManager = entityManager;
    }

    @POST
    @Path("/sak")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Operation(description = "Henter en dump av info for debugging og analyse av en sak. Logger hvem som har hatt innsyn i sak", summary = ("Henter en dump av info for debugging og analyse av en sak"), tags = "diagnostikk")
    @BeskyttetRessurs(action = BeskyttetRessursActionAttributt.READ, property = "abac.attributt.drift")
    public Response dumpSak(@NotNull @FormParam("saksnummer") @Parameter(description = "saksnummer", allowEmptyValue = false, required = true, schema = @Schema(type = "string", maximum = "10")) @Valid SaksnummerDto saksnummerDto,
                            @NotNull @FormParam("begrunnelse") @Parameter(description = "begrunnelse", allowEmptyValue = false, required = true, schema = @Schema(type = "string", maximum = "2000")) @Valid KortTekst begrunnelse) {

        /*
         * logg tilgang til tabell - må gjøres før dumps (siden StreamingOutput ikke kjører i scope av denne metoden på stacken,
         * og derfor ikke har nytte av @Transactional.
         */
        entityManager.persist(new DiagnostikkSakLogg(saksnummerDto.getVerdi(),
                BASE_PATH + "/sak",
                begrunnelse.getTekst()));
        entityManager.flush();

        var streamingOutput = dumpsters.dumper(saksnummerDto.getVerdi());

        return Response.ok(streamingOutput)
                .type(MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", String.format("attachment; filename=\"%s.zip\"", saksnummerDto.getVerdi().getVerdi()))
                .build();

    }


}
