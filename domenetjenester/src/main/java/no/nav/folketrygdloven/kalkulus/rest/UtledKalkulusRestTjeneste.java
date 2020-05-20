package no.nav.folketrygdloven.kalkulus.rest;

import static no.nav.folketrygdloven.kalkulus.sikkerhet.KalkulusBeskyttetRessursAttributt.BEREGNINGSGRUNNLAG;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import no.nav.folketrygdloven.kalkulator.endringsresultat.ErEndringIBeregning;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulus.beregning.KalkulatorInputTjeneste;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.KoblingReferanse;
import no.nav.folketrygdloven.kalkulus.felles.FellesRestTjeneste;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.YtelseTyperKalkulusStøtter;
import no.nav.folketrygdloven.kalkulus.felles.metrikker.MetrikkerTjeneste;
import no.nav.folketrygdloven.kalkulus.kobling.KoblingTjeneste;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseTyperKalkulusStøtterKontrakt;
import no.nav.folketrygdloven.kalkulus.request.v1.ErEndringIBeregningRequest;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;

@Produces(MediaType.APPLICATION_JSON)
@OpenAPIDefinition(tags = @Tag(name = "beregningsgrunnlag"))
@Path("/kalkulus/v1")
@ApplicationScoped
@Transactional
public class UtledKalkulusRestTjeneste extends FellesRestTjeneste {

    private KoblingTjeneste koblingTjeneste;
    private KalkulatorInputTjeneste kalkulatorInputTjeneste;

    public UtledKalkulusRestTjeneste() {
        // for CDI
    }

    @Inject
    public UtledKalkulusRestTjeneste(KoblingTjeneste koblingTjeneste, KalkulatorInputTjeneste kalkulatorInputTjeneste, MetrikkerTjeneste metrikkerTjeneste) {
        super(metrikkerTjeneste);
        this.koblingTjeneste = koblingTjeneste;
        this.kalkulatorInputTjeneste = kalkulatorInputTjeneste;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "Utleder om det er endring i beregning mellom beregningsgrunnlag for to behandlinger", summary = ("Returnerer om det er endring i beregning mellom behandlinger."), tags = "beregningsgrunnlag")
    @BeskyttetRessurs(action = READ, resource = BEREGNINGSGRUNNLAG)
    @Path("/erEndring")
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response erEndringIBeregning(@NotNull @Valid ErEndringIBeregningRequestAbacDto spesifikasjon) {
        var startTx = Instant.now();
        var ytelseTyperKalkulusStøtter = YtelseTyperKalkulusStøtter.fraKode(spesifikasjon.getYtelseSomSkalBeregnes().getKode());
        var koblingReferanse1 = new KoblingReferanse(spesifikasjon.getKoblingReferanse1());
        var koblingReferanse2 = new KoblingReferanse(spesifikasjon.getKoblingReferanse2());
        Optional<Long> koblingId1 = koblingTjeneste.hentKoblingHvisFinnes(koblingReferanse1, ytelseTyperKalkulusStøtter);
        Optional<Long> koblingId2 = koblingTjeneste.hentKoblingHvisFinnes(koblingReferanse2, ytelseTyperKalkulusStøtter);
        Optional<BeregningsgrunnlagDto> beregningsgrunnlag1 = koblingId1.flatMap(k -> kalkulatorInputTjeneste.lagInputMedBeregningsgrunnlag(k).getBeregningsgrunnlagHvisFinnes());
        Optional<BeregningsgrunnlagDto> beregningsgrunnlag2 = koblingId2.flatMap(k -> kalkulatorInputTjeneste.lagInputMedBeregningsgrunnlag(k).getBeregningsgrunnlagHvisFinnes());
        boolean erEndring = ErEndringIBeregning.vurder(beregningsgrunnlag1, beregningsgrunnlag2);
        logMetrikk("/kalkulus/v1/erEndring", Duration.between(startTx, Instant.now()));

        return Response.ok(erEndring).build();
    }

    /**
     * Json bean med Abac.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
    public static class ErEndringIBeregningRequestAbacDto extends ErEndringIBeregningRequest implements AbacDto {


        @JsonCreator
        public ErEndringIBeregningRequestAbacDto(@JsonProperty(value = "eksternReferanse1", required = true) @Valid @NotNull UUID eksternReferanse1,
                                                 @JsonProperty(value = "eksternReferanse2", required = true) @Valid @NotNull UUID eksternReferanse2,
                                               @JsonProperty(value = "ytelseSomSkalBeregnes", required = true) @NotNull @Valid YtelseTyperKalkulusStøtterKontrakt ytelseSomSkalBeregnes) {
            super(eksternReferanse1, eksternReferanse2, ytelseSomSkalBeregnes);
        }

        @Override
        public AbacDataAttributter abacAttributter() {
            final var abacDataAttributter = AbacDataAttributter.opprett();
            abacDataAttributter.leggTil(StandardAbacAttributtType.BEHANDLING_UUID, getKoblingReferanse1());
            abacDataAttributter.leggTil(StandardAbacAttributtType.BEHANDLING_UUID, getKoblingReferanse2());
            return abacDataAttributter;
        }
    }
}
