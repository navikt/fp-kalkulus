package no.nav.folketrygdloven.kalkulus.rest;

import static no.nav.folketrygdloven.kalkulus.sikkerhet.KalkulusBeskyttetRessursAttributtMiljøvariabel.DRIFT;
import static no.nav.k9.felles.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;

import java.util.Collections;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktørArbeidDto;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.tilkommetInntekt.TilkommetInntektsforholdTjeneste;
import no.nav.folketrygdloven.kalkulus.beregning.input.KalkulatorInputTjeneste;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseTyperKalkulusStøtterKontrakt;
import no.nav.folketrygdloven.kalkulus.mapFraEntitet.BehandlingslagerTilKalkulusMapper;
import no.nav.folketrygdloven.kalkulus.mappers.MapFraKalkulator;
import no.nav.folketrygdloven.kalkulus.mappers.MapIAYTilKalulator;
import no.nav.folketrygdloven.kalkulus.request.v1.simulerTilkommetInntekt.regelinput.SimulerTilkommetInntektRequest;
import no.nav.folketrygdloven.kalkulus.response.v1.simulerTilkommetInntekt.SimulertTilkommetInntekt;
import no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag.BeregningsgrunnlagRepository;
import no.nav.folketrygdloven.kalkulus.tjeneste.kobling.KoblingRepository;
import no.nav.k9.felles.sikkerhet.abac.AbacDataAttributter;
import no.nav.k9.felles.sikkerhet.abac.AbacDto;
import no.nav.k9.felles.sikkerhet.abac.BeskyttetRessurs;

@Produces(MediaType.APPLICATION_JSON)
@OpenAPIDefinition(tags = @Tag(name = "simulerTilkommetInntekt"))
@Path("/kalkulus/v1")
@ApplicationScoped
@Transactional
public class SimulerTilkommetInntektRestTjeneste {

    private static final Logger log = LoggerFactory.getLogger(SimulerTilkommetInntektRestTjeneste.class);

    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;

    private KalkulatorInputTjeneste kalkulatorInputTjeneste;

    private KoblingRepository koblingRepository;

    public SimulerTilkommetInntektRestTjeneste() {
        // for CDI
    }

    @Inject
    public SimulerTilkommetInntektRestTjeneste(BeregningsgrunnlagRepository beregningsgrunnlagRepository,
                                               KalkulatorInputTjeneste kalkulatorInputTjeneste, KoblingRepository koblingRepository) {
        this.kalkulatorInputTjeneste = kalkulatorInputTjeneste;
        this.beregningsgrunnlagRepository = beregningsgrunnlagRepository;
        this.koblingRepository = koblingRepository;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("simulerTilkommetInntekt")
    @Operation(description = "Simulerer tilkommet inntekt vurdering for periode", tags = "simulerTilkommetInntekt", summary = ("Simulerer aksjonspukt for tilkommet inntekt"))
    @BeskyttetRessurs(action = READ, property = DRIFT)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response simulerTilkommetInntekt(@NotNull @Valid SimulerTilkommetInntektRequestAbacDto spesifikasjon) {

        var koblinger = koblingRepository.hentKoblingerOpprettetIPeriode(spesifikasjon.getPeriode(), YtelseTyperKalkulusStøtterKontrakt.PLEIEPENGER_SYKT_BARN);

        var koblingIder = koblinger.stream().map(KoblingEntitet::getId).collect(Collectors.toSet());
        var beregningsgrunnlag = beregningsgrunnlagRepository.hentSisteBeregningsgrunnlagGrunnlagEntitetForKoblinger(koblingIder, BeregningsgrunnlagTilstand.FASTSATT, null);

        var inputer = kalkulatorInputTjeneste.hentForKoblinger(koblingIder);


        var gruppertPåSaksnummer = koblinger.stream().collect(Collectors.groupingBy(KoblingEntitet::getSaksnummer));


        var antallSaksnummerMedTilkommetInntekt = gruppertPåSaksnummer.entrySet().stream().filter(e ->
                e.getValue().stream().anyMatch(k ->
                {
                    var bg = beregningsgrunnlag.stream().filter(b -> b.getKoblingId().equals(k.getId())).findFirst();
                    if (bg.isEmpty()) {
                        return false;
                    }
                    var input = inputer.get(k.getId());

                    var iay = MapIAYTilKalulator.mapGrunnlag(input.getIayGrunnlag());

                    var mappetGrunnlag = BehandlingslagerTilKalkulusMapper.mapGrunnlag(bg.get());
                    return !TilkommetInntektsforholdTjeneste.finnTilkommetInntektsforholdTidslinje(
                            mappetGrunnlag.getBeregningsgrunnlag().get().getSkjæringstidspunkt(),
                            iay.getAktørArbeidFraRegister().map(AktørArbeidDto::hentAlleYrkesaktiviteter).orElse(Collections.emptyList()),
                            mappetGrunnlag.getBeregningsgrunnlag().get().getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList(),
                            MapFraKalkulator.mapFraDto(
                                    YtelseTyperKalkulusStøtterKontrakt.PLEIEPENGER_SYKT_BARN,
                                    input,
                                    iay,
                                    bg)

                    ).filterValue(v -> !v.isEmpty()).isEmpty();
                })).count();


        return Response.ok(new SimulertTilkommetInntekt(antallSaksnummerMedTilkommetInntekt, gruppertPåSaksnummer.size())).build();

    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
    public static class SimulerTilkommetInntektRequestAbacDto extends SimulerTilkommetInntektRequest implements AbacDto {

        public SimulerTilkommetInntektRequestAbacDto() {
            // Jackson
        }

        @Override
        public AbacDataAttributter abacAttributter() {
            final var abacDataAttributter = AbacDataAttributter.opprett();
            return abacDataAttributter;
        }
    }


}
