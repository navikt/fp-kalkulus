package no.nav.folketrygdloven.kalkulus.rest.forvaltning;

import static java.lang.Boolean.TRUE;
import static no.nav.folketrygdloven.kalkulus.sikkerhet.KalkulusBeskyttetRessursAttributtMiljøvariabel.DRIFT;
import static no.nav.k9.felles.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.MDC;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import no.nav.folketrygdloven.kalkulator.input.StegProsesseringInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.steg.BeregningsgrunnlagTjeneste;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.beregning.input.KalkulatorInputTjeneste;
import no.nav.folketrygdloven.kalkulus.beregning.input.StegProsessInputTjeneste;
import no.nav.folketrygdloven.kalkulus.beregning.v1.YtelsespesifiktGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.KoblingReferanse;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Saksnummer;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.felles.v1.KalkulatorInputDto;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.felles.v1.PersonIdent;
import no.nav.folketrygdloven.kalkulus.kobling.KoblingTjeneste;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningSteg;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseTyperKalkulusStøtterKontrakt;
import no.nav.folketrygdloven.kalkulus.request.v1.forvaltning.OppdaterYtelsesspesifiktGrunnlagForRequest;
import no.nav.folketrygdloven.kalkulus.request.v1.forvaltning.OppdaterYtelsesspesifiktGrunnlagListeRequest;
import no.nav.folketrygdloven.kalkulus.response.v1.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.response.v1.forvaltning.AndelDifferanse;
import no.nav.folketrygdloven.kalkulus.response.v1.forvaltning.EndretPeriodeListeRespons;
import no.nav.folketrygdloven.kalkulus.response.v1.forvaltning.EndretPeriodeRespons;
import no.nav.folketrygdloven.kalkulus.response.v1.forvaltning.PeriodeDifferanse;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateSegmentCombinator;
import no.nav.fpsak.tidsserie.LocalDateTimeline;
import no.nav.fpsak.tidsserie.StandardCombinators;
import no.nav.k9.felles.sikkerhet.abac.AbacDataAttributter;
import no.nav.k9.felles.sikkerhet.abac.AbacDto;
import no.nav.k9.felles.sikkerhet.abac.BeskyttetRessurs;
import no.nav.k9.felles.sikkerhet.abac.StandardAbacAttributtType;

@Produces(MediaType.APPLICATION_JSON)
@OpenAPIDefinition(tags = @Tag(name = "forvaltning"))
@Path("/kalkulus/v1")
@ApplicationScoped
@Transactional
public class MidlertidigForvaltningRestTjeneste {

    private static final BeregningsgrunnlagTjeneste beregningsgrunnlagTjeneste = new BeregningsgrunnlagTjeneste();

    private KoblingTjeneste koblingTjeneste;

    private StegProsessInputTjeneste inputTjeneste;

    private KalkulatorInputTjeneste kalkulatorInputTjeneste;

    private EntityManager entityManager;


    public MidlertidigForvaltningRestTjeneste() {
        // for CDI
    }

    @Inject
    public MidlertidigForvaltningRestTjeneste(KoblingTjeneste koblingTjeneste,
                                              StegProsessInputTjeneste inputTjeneste,
                                              KalkulatorInputTjeneste kalkulatorInputTjeneste,
                                              EntityManager entityManager) {
        this.koblingTjeneste = koblingTjeneste;
        this.inputTjeneste = inputTjeneste;
        this.kalkulatorInputTjeneste = kalkulatorInputTjeneste;
        this.entityManager = entityManager;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/forvaltning/simulerFastsettMedOppdatertUttak/bolk")
    @Operation(description = "Utfører fastsett-steg i beregning med oppdatert uttak", tags = "forvaltning", summary = ("Kjører fastsett steget"), responses = {
            @ApiResponse(description = "Liste med differanser i andeler fra lagret grunnlag per angitt eksternReferanse", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = EndretPeriodeListeRespons.class)))
    })
    @BeskyttetRessurs(action = READ, property = DRIFT)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response simulerFastsettMedOppdatertUttak(@NotNull @Valid OppdaterYtelsesspesifiktGrunnlagListeRequestAbacDto spesifikasjon) {
        var saksnummer = new Saksnummer(spesifikasjon.getSaksnummer());

        entityManager.persist(new DiagnostikkSakLogg(saksnummer,
                "/forvaltning/simulerFastsettMedOppdatertUttak/bolk", "simulerer fastsetting med endret uttak"
        ));
        entityManager.flush();


        MDC.put("prosess_saksnummer", saksnummer.getVerdi());
        var koblinger = koblingTjeneste.hentKoblinger(spesifikasjon.getYtelsespesifiktGrunnlagListe().stream().map(it -> new KoblingReferanse(it.getEksternReferanse())).toList(), spesifikasjon.getYtelseSomSkalBeregnes());
        var koblingIder = koblinger.stream().map(KoblingEntitet::getId).collect(Collectors.toSet());
        var stegInputPrKobling = lagInputPrKoblingId(spesifikasjon, koblinger, koblingIder);
        var responsPrReferanse = stegInputPrKobling.entrySet().stream()
                .map(e -> finnDifferansePrGrunnlag(koblinger, e)).toList();

        return Response.ok(new EndretPeriodeListeRespons(responsPrReferanse)).build();
    }

    private Map<Long, StegProsesseringInput> lagInputPrKoblingId(OppdaterYtelsesspesifiktGrunnlagListeRequestAbacDto spesifikasjon, List<KoblingEntitet> koblinger, Set<Long> koblingIder) {
        var inputMap = kalkulatorInputTjeneste.hentForKoblinger(koblingIder);
        var grunnlagPrReferanse = finnYtelsesspesifiktgrunnlagPrReferanse(spesifikasjon.getYtelsespesifiktGrunnlagListe());
        settNyYtelsesspesifikkInputPrKobling(koblinger, inputMap, grunnlagPrReferanse);
        return inputTjeneste.lagBeregningsgrunnlagInput(koblingIder, inputMap, BeregningSteg.FAST_BERGRUNN, Collections.emptyList());
    }

    private static void settNyYtelsesspesifikkInputPrKobling(List<KoblingEntitet> koblinger, Map<Long, KalkulatorInputDto> inputMap, Map<UUID, YtelsespesifiktGrunnlagDto> grunnlagPrReferanse) {
        inputMap.forEach((key, value) -> {
            var referanse = finnReferanse(koblinger, key);
            var ytelsespesifiktGrunnlagDto = grunnlagPrReferanse.get(referanse);
            value.medYtelsespesifiktGrunnlag(ytelsespesifiktGrunnlagDto);
        });
    }

    private EndretPeriodeRespons finnDifferansePrGrunnlag(List<KoblingEntitet> koblinger, Map.Entry<Long, StegProsesseringInput> e) {
        var input = e.getValue();
        var ytelsespesifiktGrunnlag = (no.nav.folketrygdloven.kalkulator.input.PleiepengerSyktBarnGrunnlag) input.getYtelsespesifiktGrunnlag();
        var endretTidslinje = finnTidslinjeForForventetEndring(ytelsespesifiktGrunnlag);
        var forrigeBeregningsgrunnlag = input.getForrigeGrunnlagFraSteg().orElseThrow().getBeregningsgrunnlag().orElseThrow();
        var forrigeGrunnlagTidslinje = lagAndelTidslinje(forrigeBeregningsgrunnlag);
        var endredeIntervaller = endretTidslinje.compress().toSegments().stream().map(it -> Intervall.fraOgMedTilOgMed(it.getFom(), it.getTom())).toList();
        input.setForlengelseperioder(endredeIntervaller);
        var nyttGrunnlagTidslinje = finnNyttGrunnlagTidslinje(input);
        var differanseTidslinje = nyttGrunnlagTidslinje.intersection(endretTidslinje, StandardCombinators::leftOnly)
                .intersection(forrigeGrunnlagTidslinje, finnDifferanseCombinator());
        var differansePerioder = differanseTidslinje.toSegments().stream().map(it -> new PeriodeDifferanse(new Periode(it.getFom(), it.getTom()), it.getValue()))
                .toList();
        return new EndretPeriodeRespons(finnReferanse(koblinger, e.getKey()), differansePerioder);
    }

    private LocalDateTimeline<List<BeregningsgrunnlagPrStatusOgAndelDto>> finnNyttGrunnlagTidslinje(StegProsesseringInput input) {
        var nyttGrunnlag = beregningsgrunnlagTjeneste.fastsettBeregningsgrunnlag(input);
        return lagAndelTidslinje(nyttGrunnlag.getBeregningsgrunnlag());
    }

    private static LocalDateTimeline<Boolean> finnTidslinjeForForventetEndring(no.nav.folketrygdloven.kalkulator.input.PleiepengerSyktBarnGrunnlag ytelsespesifiktGrunnlag) {
        var endredeSegmenter = ytelsespesifiktGrunnlag.getUtbetalingsgradPrAktivitet().stream().flatMap(a -> a.getPeriodeMedUtbetalingsgrad().stream()
                .map(it -> new LocalDateSegment<>(it.getPeriode().getFomDato(), it.getPeriode().getTomDato(), TRUE))).toList();
        return new LocalDateTimeline<Boolean>(endredeSegmenter, StandardCombinators::alwaysTrueForMatch);
    }

    private static LocalDateSegmentCombinator<List<BeregningsgrunnlagPrStatusOgAndelDto>, List<BeregningsgrunnlagPrStatusOgAndelDto>, List<AndelDifferanse>> finnDifferanseCombinator() {
        return (di, lhs, rhs) -> {
            var differanser = lhs.getValue().stream().map(nyAndel -> {
                var matchendeGammelAndel = rhs.getValue().stream().filter(gammelAndel -> gammelAndel.equals(nyAndel)).findFirst();
                return matchendeGammelAndel.map(beregningsgrunnlagPrStatusOgAndelDto -> AndelDifferanse.ny()
                        .medArbeidsgiver(nyAndel.getArbeidsgiver().map(a -> a.getErVirksomhet() ?
                                new Arbeidsgiver(a.getOrgnr(), null) :
                                new Arbeidsgiver(null, a.getAktørId().getAktørId())).orElse(null))
                        .medGammelDagsats(beregningsgrunnlagPrStatusOgAndelDto.getDagsats())
                        .medGammelDagsatsArbeidsgiver(beregningsgrunnlagPrStatusOgAndelDto.getDagsatsArbeidsgiver())
                        .medGammelDagsatsBruker(beregningsgrunnlagPrStatusOgAndelDto.getDagsatsBruker())
                        .medNyDagsats(nyAndel.getDagsats())
                        .medNyDagsatsArbeidsgiver(nyAndel.getDagsatsArbeidsgiver())
                        .medNyDagsatsBruker(nyAndel.getDagsatsBruker())
                        .build()).orElse(null);
            }).filter(Objects::nonNull).toList();
            return new LocalDateSegment<>(di, differanser);
        };
    }

    private static LocalDateTimeline<List<BeregningsgrunnlagPrStatusOgAndelDto>> lagAndelTidslinje(BeregningsgrunnlagDto beregningsgrunnlagDto) {
        var eksisterendeAndelSegmenter = beregningsgrunnlagDto
                .getBeregningsgrunnlagPerioder().stream().map(p -> new LocalDateSegment<>(p.getBeregningsgrunnlagPeriodeFom(), p.getBeregningsgrunnlagPeriodeTom(), p.getBeregningsgrunnlagPrStatusOgAndelList()))
                .toList();
        return new LocalDateTimeline<>(eksisterendeAndelSegmenter);
    }

    private static UUID finnReferanse(List<KoblingEntitet> koblinger, Long id) {
        return koblinger.stream().filter(k -> k.getId().equals(id)).findFirst().orElseThrow().getKoblingReferanse().getReferanse();
    }


    private Map<UUID, YtelsespesifiktGrunnlagDto> finnYtelsesspesifiktgrunnlagPrReferanse(List<OppdaterYtelsesspesifiktGrunnlagForRequest> liste) {
        return liste.stream().filter(i -> i.getYtelsespesifiktGrunnlag() != null).collect(Collectors.toMap(OppdaterYtelsesspesifiktGrunnlagForRequest::getEksternReferanse, OppdaterYtelsesspesifiktGrunnlagForRequest::getYtelsespesifiktGrunnlag));
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
    public static class OppdaterYtelsesspesifiktGrunnlagListeRequestAbacDto extends OppdaterYtelsesspesifiktGrunnlagListeRequest implements AbacDto {

        public OppdaterYtelsesspesifiktGrunnlagListeRequestAbacDto() {
        }

        public OppdaterYtelsesspesifiktGrunnlagListeRequestAbacDto(String saksnummer,
                                                                   UUID behandlingUuid,
                                                                   PersonIdent aktør,
                                                                   YtelseTyperKalkulusStøtterKontrakt ytelseSomSkalBeregnes,
                                                                   List<OppdaterYtelsesspesifiktGrunnlagForRequest> liste) {
            super(saksnummer, behandlingUuid, aktør, ytelseSomSkalBeregnes, liste);
        }

        @Override
        public AbacDataAttributter abacAttributter() {
            var abacDataAttributter = AbacDataAttributter.opprett();
            if (getBehandlingUuid() != null) {
                abacDataAttributter.leggTil(StandardAbacAttributtType.BEHANDLING_UUID, getBehandlingUuid());
            }
            abacDataAttributter.leggTil(StandardAbacAttributtType.SAKSNUMMER, getSaksnummer());
            return abacDataAttributter.leggTil(StandardAbacAttributtType.AKTØR_ID, getAktør().getIdent());
        }
    }

}
