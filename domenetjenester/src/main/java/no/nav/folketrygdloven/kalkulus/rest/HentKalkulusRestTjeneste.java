package no.nav.folketrygdloven.kalkulus.rest;

import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.FAGSAK;

import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.KoblingReferanse;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.YtelseTyperKalkulusStøtter;
import no.nav.folketrygdloven.kalkulus.kobling.KoblingTjeneste;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseTyperKalkulusStøtterKontrakt;
import no.nav.folketrygdloven.kalkulus.mapTilKontrakt.MapBeregningsgrunnlag;
import no.nav.folketrygdloven.kalkulus.request.v1.HentBeregningsgrunnlagRequest;
import no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag.BeregningsgrunnlagRepository;
import no.nav.vedtak.felles.jpa.Transaction;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;

@Produces(MediaType.APPLICATION_JSON)
@OpenAPIDefinition(tags = @Tag(name = "beregningsgrunnlag"))
@Path("/kalkulus/v1")
@ApplicationScoped
@Transaction
public class HentKalkulusRestTjeneste {


    private KoblingTjeneste koblingTjeneste;
    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;

    public HentKalkulusRestTjeneste() {
        // for CDI
    }

    @Inject
    public HentKalkulusRestTjeneste(KoblingTjeneste koblingTjeneste, BeregningsgrunnlagRepository beregningsgrunnlagRepository) {
        this.koblingTjeneste = koblingTjeneste;
        this.beregningsgrunnlagRepository = beregningsgrunnlagRepository;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "Hent beregningsgrunnlag for angitt behandling", summary = ("Returnerer beregningsgrunnlag for behandling."), tags = "beregningsgrunnlag")
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @Path("/fastsatt")
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response hentFastsattBeregningsgrunnlag(@NotNull @Valid HentBeregningsgrunnlagRequestAbacDto spesifikasjon) {
        var koblingReferanse = new KoblingReferanse(spesifikasjon.getKoblingReferanse());
        var ytelseTyperKalkulusStøtter = YtelseTyperKalkulusStøtter.fraKode(spesifikasjon.getYtelseSomSkalBeregnes().getKode());
        Long koblingId = koblingTjeneste.hentKoblingId(koblingReferanse, ytelseTyperKalkulusStøtter);
        Optional<BeregningsgrunnlagGrunnlagEntitet> beregningsgrunnlagGrunnlagEntitet = beregningsgrunnlagRepository.hentBeregningsgrunnlagGrunnlagEntitet(koblingId);
        return beregningsgrunnlagGrunnlagEntitet.stream()
                .filter(grunnlag -> grunnlag.getBeregningsgrunnlagTilstand().equals(BeregningsgrunnlagTilstand.FASTSATT))
                .flatMap(gr -> gr.getBeregningsgrunnlag().stream())
                .map(MapBeregningsgrunnlag::map)
                .map(bgDto -> Response.ok(bgDto).build())
                .findFirst()
                .orElse(Response.noContent().build());
    }

    /**
     * Json bean med Abac.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
    public static class HentBeregningsgrunnlagRequestAbacDto extends HentBeregningsgrunnlagRequest implements AbacDto {


        @JsonCreator
        public HentBeregningsgrunnlagRequestAbacDto(@JsonProperty(value = "eksternReferanse", required = true) @Valid @NotNull UUID eksternReferanse,
                                               @JsonProperty(value = "ytelseSomSkalBeregnes", required = true) @NotNull @Valid YtelseTyperKalkulusStøtterKontrakt ytelseSomSkalBeregnes) {
            super(eksternReferanse, ytelseSomSkalBeregnes);
        }

        @Override
        public AbacDataAttributter abacAttributter() {
            final var abacDataAttributter = AbacDataAttributter.opprett();
            abacDataAttributter.leggTil(StandardAbacAttributtType.BEHANDLING_UUID, getKoblingReferanse());
            return abacDataAttributter;
        }
    }
}
