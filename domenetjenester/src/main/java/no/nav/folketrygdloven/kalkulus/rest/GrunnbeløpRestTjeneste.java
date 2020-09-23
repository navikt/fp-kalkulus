package no.nav.folketrygdloven.kalkulus.rest;

import static no.nav.folketrygdloven.kalkulus.sikkerhet.KalkulusBeskyttetRessursAttributt.BEREGNINGSGRUNNLAG;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.BeregningSats;
import no.nav.folketrygdloven.kalkulus.mapTilKontrakt.MapBeregningSats;
import no.nav.folketrygdloven.kalkulus.rest.abac.HentGrunnbeløpRequestAbacDto;
import no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag.BeregningsgrunnlagRepository;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;

@Produces(MediaType.APPLICATION_JSON)
@OpenAPIDefinition(tags = @Tag(name = "grunnbelop"))
@Path("/kalkulus/v1")
@ApplicationScoped
@Transactional
public class GrunnbeløpRestTjeneste {

    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;

    public GrunnbeløpRestTjeneste() {
        // for CDI
    }

    @Inject
    public GrunnbeløpRestTjeneste(BeregningsgrunnlagRepository beregningsgrunnlagRepository) {
        this.beregningsgrunnlagRepository = beregningsgrunnlagRepository;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "Hent grunnbeløp for angitt dato", summary = ("Returnerer grunnbeløp for dato."), tags = "grunnbelop")
    @BeskyttetRessurs(action = READ, resource = BEREGNINGSGRUNNLAG)
    @Path("/grunnbelop")
    public Response hentGrunnbeløp(@NotNull @Valid HentGrunnbeløpRequestAbacDto spesifikasjon) {
        BeregningSats grunnbeløp = beregningsgrunnlagRepository.finnGrunnbeløp(spesifikasjon.getDato());
        final Response response = Response.ok(MapBeregningSats.map(grunnbeløp)).build();
        return response;
    }

}
