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
import no.nav.folketrygdloven.kalkulator.endringsresultat.ErEndringIBeregning;
import no.nav.folketrygdloven.kalkulator.guitjenester.BeregningsgrunnlagDtoTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulus.beregning.BeregningTilInputTjeneste;
import no.nav.folketrygdloven.kalkulus.beregning.KalkulatorInputTjeneste;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.KoblingReferanse;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.YtelseTyperKalkulusStøtter;
import no.nav.folketrygdloven.kalkulus.kobling.KoblingTjeneste;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseTyperKalkulusStøtterKontrakt;
import no.nav.folketrygdloven.kalkulus.request.v1.ErEndringIBeregningRequest;
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
public class UtledKalkulusRestTjeneste {

    private KoblingTjeneste koblingTjeneste;
    private KalkulatorInputTjeneste kalkulatorInputTjeneste;

    public UtledKalkulusRestTjeneste() {
        // for CDI
    }

    @Inject
    public UtledKalkulusRestTjeneste(KoblingTjeneste koblingTjeneste, BeregningsgrunnlagRepository beregningsgrunnlagRepository, KalkulatorInputTjeneste kalkulatorInputTjeneste) {
        this.koblingTjeneste = koblingTjeneste;
        this.kalkulatorInputTjeneste = kalkulatorInputTjeneste;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "Utleder om det er endring i beregning mellom beregningsgrunnlag for to behandlinger", summary = ("Returnerer om det er endring i beregning mellom behandlinger."), tags = "beregningsgrunnlag")
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @Path("/erEndring")
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response erEndringIBeregning(@NotNull @Valid ErEndringIBeregningRequestAbacDto spesifikasjon) {
        var ytelseTyperKalkulusStøtter = YtelseTyperKalkulusStøtter.fraKode(spesifikasjon.getYtelseSomSkalBeregnes().getKode());
        var koblingReferanse1 = new KoblingReferanse(spesifikasjon.getKoblingReferanse1());
        var koblingReferanse2 = new KoblingReferanse(spesifikasjon.getKoblingReferanse2());
        Long koblingId1 = koblingTjeneste.hentKoblingId(koblingReferanse1, ytelseTyperKalkulusStøtter);
        Long koblingId2 = koblingTjeneste.hentKoblingId(koblingReferanse2, ytelseTyperKalkulusStøtter);
        BeregningsgrunnlagDto beregningsgrunnlag1 = kalkulatorInputTjeneste.lagInputMedBeregningsgrunnlag(koblingId1).getBeregningsgrunnlag();
        BeregningsgrunnlagDto beregningsgrunnlag2 = kalkulatorInputTjeneste.lagInputMedBeregningsgrunnlag(koblingId2).getBeregningsgrunnlag();
        boolean erEndring = ErEndringIBeregning.vurder(Optional.ofNullable(beregningsgrunnlag1), Optional.ofNullable(beregningsgrunnlag2));
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
