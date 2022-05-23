package no.nav.folketrygdloven.kalkulus.rest.forvaltning;

import com.fasterxml.jackson.annotation.JsonValue;

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
import no.nav.k9.felles.sikkerhet.abac.AbacDataAttributter;
import no.nav.k9.felles.sikkerhet.abac.AbacDto;
import no.nav.k9.felles.sikkerhet.abac.BeskyttetRessurs;
import no.nav.k9.felles.sikkerhet.abac.BeskyttetRessursActionAttributt;

@Path(RegelsporingForvaltningRestTjeneste.BASE_PATH)
@OpenAPIDefinition(tags = @Tag(name = "regelsporing-forvaltning"))
@ApplicationScoped
@Transactional
public class RegelsporingForvaltningRestTjeneste {

    static final String BASE_PATH = "/regelsporing-forvaltning";

    private EntityManager entityManager;


    public RegelsporingForvaltningRestTjeneste() {
    }

    @Inject
    public RegelsporingForvaltningRestTjeneste(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @POST
    @Path("/fjernPeriodeRegelSporinger")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "Sletter inaktive regelsporinger", summary = ("Sletter inaktive regelsporinger"), tags = "regelsporing-forvaltning")
    // Setter READ for å slippe å lage egne abac-regler for denne midlertidige funksjonaliteten
    @BeskyttetRessurs(action = BeskyttetRessursActionAttributt.READ, property = "abac.attributt.drift")
    public Response fjernPeriodeRegelSporinger(@NotNull @Parameter(description = "maksAntall", required = true) @Valid FjernRegelsporingLimit maksAntall) {

        // Sletter periode-sporing
        var perioderQuery = entityManager.createNativeQuery("delete from REGEL_SPORING_PERIODE " +
                        "where id in (SELECT ID FROM REGEL_SPORING_PERIODE WHERE AKTIV = FALSE LIMIT :limit)")
                .setParameter("limit", maksAntall);

        var antallSlettet = perioderQuery.executeUpdate();

        entityManager.flush();

        return Response.ok(antallSlettet).build();
    }

    @POST
    @Path("/fjernGrunnlagRegelSporinger")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "Sletter inaktive regelsporinger", summary = ("Sletter inaktive regelsporinger"), tags = "regelsporing-forvaltning")
    // Setter READ for å slippe å lage egne abac-regler for denne midlertidige funksjonaliteten
    @BeskyttetRessurs(action = BeskyttetRessursActionAttributt.READ, property = "abac.attributt.drift")
    public Response fjernGrunnlagRegelSporinger(@NotNull @Parameter(description = "maksAntall", required = true) @Valid FjernRegelsporingLimit maksAntall) {

        // Sletter grunnlag-sporing
        var grunnlagQuery = entityManager.createNativeQuery("delete from REGEL_SPORING_GRUNNLAG " +
                        "where id in (SELECT ID FROM REGEL_SPORING_GRUNNLAG WHERE AKTIV = FALSE LIMIT :limit)")
                .setParameter("limit", maksAntall.getLimit());

        var antallSlettet = grunnlagQuery.executeUpdate();

        entityManager.flush();

        return Response.ok(antallSlettet).build();
    }

}
