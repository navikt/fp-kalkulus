package no.nav.folketrygdloven.kalkulus.rest;

import static no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.UtbetalingsgradTjeneste.finnPerioderForArbeid;
import static no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.UtbetalingsgradTjeneste.finnPerioderForStatus;
import static no.nav.folketrygdloven.kalkulus.sikkerhet.KalkulusBeskyttetRessursAttributtMiljøvariabel.DRIFT;
import static no.nav.k9.felles.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Function;
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
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.UtbetalingsgradTjeneste;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.fastsett.MapBeregningsgrunnlagFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.HåndterBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.UtbetalingsgradGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.TilkommetInntektDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktørArbeidDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektspostDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.tilkommetInntekt.SplittBGPerioder;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.tilkommetInntekt.TilkommetInntektPeriodeTjeneste;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.tilkommetInntekt.TilkommetInntektsforholdTjeneste;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulator.tid.Virkedager;
import no.nav.folketrygdloven.kalkulus.beregning.input.KalkulatorInputTjeneste;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Saksnummer;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.felles.v1.KalkulatorInputDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.PeriodeÅrsak;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseTyperKalkulusStøtterKontrakt;
import no.nav.folketrygdloven.kalkulus.mapFraEntitet.BehandlingslagerTilKalkulusMapper;
import no.nav.folketrygdloven.kalkulus.mappers.MapFraKalkulator;
import no.nav.folketrygdloven.kalkulus.mappers.MapIAYTilKalulator;
import no.nav.folketrygdloven.kalkulus.request.v1.simulerTilkommetInntekt.regelinput.SimulerTilkommetInntektRequest;
import no.nav.folketrygdloven.kalkulus.response.v1.simulerTilkommetInntekt.SimulertTilkommetInntekt;
import no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag.BeregningsgrunnlagRepository;
import no.nav.folketrygdloven.kalkulus.tjeneste.kobling.KoblingRepository;
import no.nav.folketrygdloven.regelmodelloversetter.KalkulusRegler;
import no.nav.k9.felles.sikkerhet.abac.AbacDataAttributter;
import no.nav.k9.felles.sikkerhet.abac.AbacDto;
import no.nav.k9.felles.sikkerhet.abac.BeskyttetRessurs;

@Produces(MediaType.APPLICATION_JSON)
@OpenAPIDefinition(tags = @Tag(name = "simulerTilkommetInntekt"))
@Path("/kalkulus/v1")
@ApplicationScoped
@Transactional
public class SimulerTilkommetInntektRestTjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(SimulerTilkommetInntektRestTjeneste.class);

    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;

    private KalkulatorInputTjeneste kalkulatorInputTjeneste;

    private KoblingRepository koblingRepository;

    private MapBeregningsgrunnlagFraVLTilRegel mapBeregningsgrunnlagFraVLTilRegel;


    public SimulerTilkommetInntektRestTjeneste() {
        // for CDI
    }

    @Inject
    public SimulerTilkommetInntektRestTjeneste(BeregningsgrunnlagRepository beregningsgrunnlagRepository,
                                               KalkulatorInputTjeneste kalkulatorInputTjeneste, KoblingRepository koblingRepository, MapBeregningsgrunnlagFraVLTilRegel mapBeregningsgrunnlagFraVLTilRegel) {
        this.kalkulatorInputTjeneste = kalkulatorInputTjeneste;
        this.beregningsgrunnlagRepository = beregningsgrunnlagRepository;
        this.koblingRepository = koblingRepository;
        this.mapBeregningsgrunnlagFraVLTilRegel = mapBeregningsgrunnlagFraVLTilRegel;
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
        var beregningsgrunnlag = beregningsgrunnlagRepository.hentBeregningsgrunnlagGrunnlagEntiteter(koblingIder);
        var inputer = kalkulatorInputTjeneste.hentForKoblinger(beregningsgrunnlag.stream().map(BeregningsgrunnlagGrunnlagEntitet::getKoblingId).collect(Collectors.toSet()));
        var bgPrSaksnummer = beregningsgrunnlag.stream()
                .filter(bg -> bg.getBeregningsgrunnlagTilstand().equals(BeregningsgrunnlagTilstand.FASTSATT))
                .collect(Collectors.toMap(finnSaksnummer(koblinger),
                        List::of, velgNyestePrSkjæringstidspunkt()));

        var saksnummerMedReduksjonsinfo = bgPrSaksnummer.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> mapReduksjoner(koblinger, inputer, e)));

        return Response.ok(new SimulertTilkommetInntekt(
                finnAntallSakerMedTilkommetInntektAksjonspunkt(saksnummerMedReduksjonsinfo),
                finnAntallSakerMedManuellFordeling(saksnummerMedReduksjonsinfo),
                finnAntallSakerMedReduksjon(spesifikasjon, saksnummerMedReduksjonsinfo),
                bgPrSaksnummer.size(),
                finnAntallSakerPrTilkommetStatus(saksnummerMedReduksjonsinfo))).build();
    }

    private static long finnAntallSakerMedTilkommetInntektAksjonspunkt(Map<Saksnummer, List<ReduksjonVedGradering>> saksnummerMedReduksjon) {
        return saksnummerMedReduksjon.entrySet().stream().filter(e -> !e.getValue().isEmpty()).count();
    }

    private static long finnAntallSakerMedReduksjon(SimulerTilkommetInntektRequestAbacDto spesifikasjon, Map<Saksnummer, List<ReduksjonVedGradering>> saksnummerMedReduksjon) {
        var dagsatsFeiltoleranse = spesifikasjon.getDagsatsFeiltoleranse() != null ? spesifikasjon.getDagsatsFeiltoleranse() : 0L;

        var antallSaksnummerMedReduksjon = saksnummerMedReduksjon.entrySet().stream()
                .filter(e -> e.getValue().stream().anyMatch(r -> (r.gammelDagsats - r.gradertDagsats) > dagsatsFeiltoleranse))
                .count();
        return antallSaksnummerMedReduksjon;
    }

    private static long finnAntallSakerMedManuellFordeling(Map<Saksnummer, List<ReduksjonVedGradering>> saksnummerMedReduksjon) {
        var antallSaksnummerMedManuellFordelingOgAksjonspunkt = saksnummerMedReduksjon.entrySet().stream()
                .filter(e -> !e.getValue().isEmpty() && e.getValue().stream().anyMatch(ReduksjonVedGradering::erFordelt)).count();
        return antallSaksnummerMedManuellFordelingOgAksjonspunkt;
    }

    private static Map<String, Integer> finnAntallSakerPrTilkommetStatus(Map<Saksnummer, List<ReduksjonVedGradering>> saksnummerMedReduksjon) {
        var antallPrStatus = saksnummerMedReduksjon.entrySet().stream()
                .filter(e -> !e.getValue().isEmpty())
                .collect(
                        Collectors.toMap(
                                e -> e.getValue().stream().flatMap(r -> r.tilkommetStatuser().stream()).distinct()
                                        .map(AktivitetStatus::getKode)
                                        .sorted(Comparator.naturalOrder())
                                        .reduce("", (s1, s2) -> {
                                            if (!Objects.equals(s1, "")) {
                                                return s1 + "_" + s2;
                                            }
                                            return s2;
                                        }),
                                e -> 1,
                                Integer::sum)
                );
        return antallPrStatus;
    }

    private List<ReduksjonVedGradering> mapReduksjoner(List<KoblingEntitet> koblinger, Map<Long, KalkulatorInputDto> inputer, Map.Entry<Saksnummer, List<BeregningsgrunnlagGrunnlagEntitet>> e) {
        return e.getValue().stream().map(bg ->
        {
            var kobling = koblinger.stream().filter(it -> it.getId().equals(bg.getKoblingId())).findFirst().orElseThrow();
            var input = inputer.get(kobling.getId());
            return finnPerioderMedReduksjonForBeregningsgrunnlag(bg, kobling, input);
        }).flatMap(Collection::stream).toList();
    }

    private static BinaryOperator<List<BeregningsgrunnlagGrunnlagEntitet>> velgNyestePrSkjæringstidspunkt() {
        return (eksisterendeListe, value) -> {
            var res = new ArrayList<>(eksisterendeListe);
            var bg = value.get(0);
            var eksisterendeMedSammeStp = finnEksisterendeMedSammeStp(eksisterendeListe, bg);
            if (eksisterendeMedSammeStp.isPresent()) {
                velgSistOpprettet(res, bg, eksisterendeMedSammeStp);
            }
            return res;
        };
    }

    private static void velgSistOpprettet(ArrayList<BeregningsgrunnlagGrunnlagEntitet> res, BeregningsgrunnlagGrunnlagEntitet bg, Optional<BeregningsgrunnlagGrunnlagEntitet> eksisterendeMedSammeStp) {
        if (eksisterendeMedSammeStp.get().getOpprettetTidspunkt().isBefore(bg.getOpprettetTidspunkt())) {
            res.remove(eksisterendeMedSammeStp.get());
            res.add(bg);
        }
    }

    private static Optional<BeregningsgrunnlagGrunnlagEntitet> finnEksisterendeMedSammeStp(List<BeregningsgrunnlagGrunnlagEntitet> eksisterendeListe, BeregningsgrunnlagGrunnlagEntitet bg) {
        return eksisterendeListe.stream().filter(it -> it.getBeregningsgrunnlag().get().getSkjæringstidspunkt().equals(bg.getBeregningsgrunnlag().get().getSkjæringstidspunkt())).findFirst();
    }

    private List<ReduksjonVedGradering> finnPerioderMedReduksjonForBeregningsgrunnlag(BeregningsgrunnlagGrunnlagEntitet bg, KoblingEntitet k, KalkulatorInputDto input) {
        var iay = MapIAYTilKalulator.mapGrunnlag(input.getIayGrunnlag());
        var mappetGrunnlag = BehandlingslagerTilKalkulusMapper.mapBeregningsgrunnlag(bg.getBeregningsgrunnlag().get());
        var ytelsespesifiktGrunnlag = MapFraKalkulator.mapFraDto(
                YtelseTyperKalkulusStøtterKontrakt.PLEIEPENGER_SYKT_BARN,
                input,
                iay,
                Optional.of(bg));
        var nyttBg = splittBeregningsgrunnlagOgLagTilkommet(iay, mappetGrunnlag, ytelsespesifiktGrunnlag);
        settInntektPåTilkomneInntektsforhold(iay, ytelsespesifiktGrunnlag, nyttBg);
        var beregningsgrunnlagInput = lagHåndteringBeregningsgrunnlagInput(k, input, bg);
        return finnReduksjon(beregningsgrunnlagInput, nyttBg);
    }

    private List<ReduksjonVedGradering> finnReduksjon(HåndterBeregningsgrunnlagInput beregningsgrunnlagInput, BeregningsgrunnlagDto nyttBg) {
        var regelResultatPerioder = kjørRegel(beregningsgrunnlagInput, nyttBg);
        var erFordelt = erFordelt(nyttBg);
        return nyttBg.getBeregningsgrunnlagPerioder().stream()
                .filter(p -> !p.getTilkomneInntekter().isEmpty())
                .map(p -> {
                    var dagsatsFørGradering = p.getDagsats();
                    var dagsatsEtterGradering = finnDagsatsEtterGradering(regelResultatPerioder, p, dagsatsFørGradering);
                    var virkedager = getBeregnVirkedager(p.getPeriode());
                    return new ReduksjonVedGradering(dagsatsFørGradering, dagsatsEtterGradering,
                            p.getPeriode().getFomDato(),
                            p.getPeriode().getTomDato(),
                            virkedager,
                            erFordelt,
                            p.getTilkomneInntekter().stream().map(TilkommetInntektDto::getAktivitetStatus).collect(Collectors.toSet()));
                }).toList();
    }

    private static boolean erFordelt(BeregningsgrunnlagDto nyttBg) {
        return nyttBg.getBeregningsgrunnlagPerioder().stream()
                .filter(p -> !p.getTilkomneInntekter().isEmpty())
                .flatMap(p -> p.getBeregningsgrunnlagPrStatusOgAndelList().stream())
                .anyMatch(a -> a.getFordeltPrÅr() != null || a.getManueltFordeltPrÅr() != null);
    }

    private List<BeregningsgrunnlagPeriode> kjørRegel(HåndterBeregningsgrunnlagInput beregningsgrunnlagInput, BeregningsgrunnlagDto nyttBg) {
        var regelBg = mapBeregningsgrunnlagFraVLTilRegel.map(beregningsgrunnlagInput, nyttBg);
        Beregningsgrunnlag.builder(regelBg).leggTilToggle("GRADERING_MOT_INNTEKT", true);
        var regelPerioder = regelBg.getBeregningsgrunnlagPerioder().stream()
                .filter(p -> !p.getTilkommetInntektsforholdListe().isEmpty())
                .toList();
        regelPerioder.forEach(KalkulusRegler::finnGrenseverdi);
        return regelPerioder;
    }

    private static Long finnDagsatsEtterGradering(List<BeregningsgrunnlagPeriode> regelPerioder, BeregningsgrunnlagPeriodeDto p, Long dagsatsFørGradering) {
        return regelPerioder.stream().filter(it -> it.getPeriodeFom().equals(p.getPeriode().getFomDato())).findFirst()
                .map(BeregningsgrunnlagPeriode::getGrenseverdi)
                .map(gr -> gr.divide(BigDecimal.valueOf(260), 2, RoundingMode.HALF_UP))
                .map(BigDecimal::longValue)
                .orElse(dagsatsFørGradering);
    }

    private static BeregningsgrunnlagDto splittBeregningsgrunnlagOgLagTilkommet(InntektArbeidYtelseGrunnlagDto iay, BeregningsgrunnlagDto mappetGrunnlag, YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {
        var tilkommetTidslinje = TilkommetInntektsforholdTjeneste.finnTilkommetInntektsforholdTidslinje(
                mappetGrunnlag.getSkjæringstidspunkt(),
                iay.getAktørArbeidFraRegister().map(AktørArbeidDto::hentAlleYrkesaktiviteter).orElse(Collections.emptyList()),
                mappetGrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList(),
                ytelsespesifiktGrunnlag
        ).filterValue(v -> !v.isEmpty());
        return SplittBGPerioder.splittPerioder(mappetGrunnlag,
                tilkommetTidslinje,
                TilkommetInntektPeriodeTjeneste::opprettTilkommetInntekt,
                SplittBGPerioder.getSettAvsluttetPeriodeårsakMapper(tilkommetTidslinje, Collections.emptyList(), PeriodeÅrsak.TILKOMMET_INNTEKT_AVSLUTTET));
    }

    private static Function<BeregningsgrunnlagGrunnlagEntitet, Saksnummer> finnSaksnummer(List<KoblingEntitet> koblinger) {
        return bg -> koblinger.stream()
                .filter(k -> k.getId().equals(bg.getKoblingId()))
                .findFirst().map(KoblingEntitet::getSaksnummer).orElseThrow();
    }

    private void settInntektPåTilkomneInntektsforhold(InntektArbeidYtelseGrunnlagDto iay, YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag, BeregningsgrunnlagDto nyttBg) {
        nyttBg.getBeregningsgrunnlagPerioder().stream()
                .filter(p -> !p.getTilkomneInntekter().isEmpty())
                .forEach(p -> {
                    var tilkomneInntekter = p.getTilkomneInntekter();
                    var nyeInntektsforhold = tilkomneInntekter.stream().map(
                            it -> mapMedInntekt(iay, ytelsespesifiktGrunnlag, p, it)
                    ).toList();
                    var oppdater = BeregningsgrunnlagPeriodeDto.oppdater(p);
                    nyeInntektsforhold.forEach(oppdater::leggTilTilkommetInntekt);
                });
    }

    private TilkommetInntektDto mapMedInntekt(InntektArbeidYtelseGrunnlagDto iay, YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag, BeregningsgrunnlagPeriodeDto p, TilkommetInntektDto it) {
        BigDecimal inntekt;
        if (it.getArbeidsgiver().isPresent()) {
            inntekt = finnInntektForArbeidsgiver(
                    it.getArbeidsgiver().get(),
                    it.getArbeidsforholdRef(),
                    p.getPeriode(),
                    iay,
                    ytelsespesifiktGrunnlag);

        } else if (it.getAktivitetStatus().equals(AktivitetStatus.FRILANSER)) {
            inntekt = finnInntektForFrilans(
                    p.getPeriode(),
                    iay,
                    ytelsespesifiktGrunnlag);
        } else {
            LOG.info("Fant tilkommet inntekt for status {}. Setter 0 i brutto inntekt.", it.getAktivitetStatus().getKode());
            inntekt = BigDecimal.ZERO;
        }
        return new TilkommetInntektDto(
                it.getAktivitetStatus(), it.getArbeidsgiver().orElse(null),
                it.getArbeidsforholdRef(),
                inntekt,
                utledTilkommetFraBrutto(inntekt, it, p.getPeriode(), ytelsespesifiktGrunnlag),
                true
        );
    }


    private static BigDecimal utledTilkommetFraBrutto(BigDecimal inntekt,
                                                      TilkommetInntektDto inntektsforhold,
                                                      Intervall periode, YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {
        if (ytelsespesifiktGrunnlag instanceof UtbetalingsgradGrunnlag) {
            if (inntektsforhold.getAktivitetStatus().erArbeidstaker() && inntektsforhold.getArbeidsgiver().isPresent()) {
                var utbetalingsgradProsent = UtbetalingsgradTjeneste.finnUtbetalingsgradForArbeid(
                        inntektsforhold.getArbeidsgiver().get(),
                        inntektsforhold.getArbeidsforholdRef(),
                        periode,
                        ytelsespesifiktGrunnlag,
                        true);
                var utbetalingsgrad = utbetalingsgradProsent.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
                return inntekt.multiply(BigDecimal.ONE.subtract(utbetalingsgrad));
            } else {
                return BigDecimal.ZERO;
            }
        }
        throw new IllegalStateException("Kun gyldig ved utbetalingsgradgrunnlag");
    }

    private static BigDecimal utledBruttoInntektFraTilkommetForArbeidstaker(BigDecimal tilkommetInntekt,
                                                                            Arbeidsgiver arbeidsgiver,
                                                                            InternArbeidsforholdRefDto internArbeidsforholdRefDto,
                                                                            Intervall periode, YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {
        if (ytelsespesifiktGrunnlag instanceof UtbetalingsgradGrunnlag utbetalingsgradGrunnlag) {
            var snittFraværVektet = finnVektetSnittfraværForArbeidstaker(arbeidsgiver, internArbeidsforholdRefDto, periode, utbetalingsgradGrunnlag);
            var utbetalingsgrad = snittFraværVektet.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
            if (utbetalingsgrad.equals(BigDecimal.ONE)) {
                return BigDecimal.ZERO;
            }
            return tilkommetInntekt.divide(BigDecimal.ONE.subtract(utbetalingsgrad), 10, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    private static BigDecimal utledBruttoInntektFraTilkommetForFrilans(BigDecimal tilkommetInntekt,
                                                                       Intervall periode, YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {
        if (ytelsespesifiktGrunnlag instanceof UtbetalingsgradGrunnlag utbetalingsgradGrunnlag) {
            var snittFraværVektet = finnVektetSnittfraværForFrilans(periode, utbetalingsgradGrunnlag);
            var utbetalingsgrad = snittFraværVektet.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
            if (utbetalingsgrad.equals(BigDecimal.ONE)) {
                return BigDecimal.ZERO;
            }
            return tilkommetInntekt.divide(BigDecimal.ONE.subtract(utbetalingsgrad), 10, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }


    private static BigDecimal finnVektetSnittfraværForArbeidstaker(Arbeidsgiver arbeidsgiver,
                                                                   InternArbeidsforholdRefDto internArbeidsforholdRefDto,
                                                                   Intervall periode, UtbetalingsgradGrunnlag utbetalingsgradGrunnlag) {
        var utbetalingsgradPrAktivitetDtos = finnPerioderForArbeid(utbetalingsgradGrunnlag,
                arbeidsgiver,
                internArbeidsforholdRefDto,
                true);
        return finnVektetSnittfravær(periode, utbetalingsgradPrAktivitetDtos);
    }

    private static BigDecimal finnVektetSnittfraværForFrilans(Intervall periode, UtbetalingsgradGrunnlag utbetalingsgradGrunnlag) {
        var utbetalingsgradPrAktivitetDtos = finnPerioderForStatus(AktivitetStatus.FRILANSER, utbetalingsgradGrunnlag);
        return finnVektetSnittfravær(periode, utbetalingsgradPrAktivitetDtos.map(List::of).orElse(Collections.emptyList()));
    }


    private static BigDecimal finnVektetSnittfravær(Intervall periode, List<UtbetalingsgradPrAktivitetDto> utbetalingsgradPrAktivitetDtos) {
        return utbetalingsgradPrAktivitetDtos
                .stream()
                .flatMap(utb -> utb.getPeriodeMedUtbetalingsgrad().stream())
                .filter(p -> p.getPeriode().inkluderer(periode.getFomDato()))
                .map(p -> {
                    var virkedager = getBeregnVirkedager(p.getPeriode());
                    return BigDecimal.valueOf(virkedager).multiply(p.getUtbetalingsgrad());
                })
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO)
                .divide(BigDecimal.valueOf(getBeregnVirkedager(periode)), 10, RoundingMode.HALF_UP);
    }

    private BigDecimal finnInntektForArbeidsgiver(Arbeidsgiver arbeidsgiver,
                                                  InternArbeidsforholdRefDto arbeidsforholdRef,
                                                  Intervall periode,
                                                  InntektArbeidYtelseGrunnlagDto iay, YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {
        var im = iay.getInntektsmeldinger().stream()
                .flatMap(ims -> ims.getAlleInntektsmeldinger().stream())
                .filter(it -> it.getArbeidsgiver().equals(arbeidsgiver) && it.getArbeidsforholdRef().gjelderFor(arbeidsforholdRef))
                .findFirst();

        if (im.isPresent()) {
            return im.get().getInntektBeløp().multipliser(12).getVerdi();
        }
        var inntektFilterDto = new InntektFilterDto(iay.getAktørInntektFraRegister());
        var inntektsposter = inntektFilterDto.filterBeregningsgrunnlag()
                .filter(arbeidsgiver)
                .getFiltrertInntektsposter();

        var aktuellePoster = inntektsposter.stream().filter(i -> i.getPeriode().overlapper(
                        Intervall.fraOgMedTilOgMed(periode.getFomDato().minusMonths(3), periode.getFomDato().plusMonths(3))))
                .toList();

        var antallPoster = aktuellePoster.size();
        if (antallPoster == 0) {
            LOG.info("Fant ingen inntektsposter for arbeidsgiver {} i periode {}", arbeidsgiver, periode);
            return BigDecimal.ZERO;
        }
        return aktuellePoster.stream().map(post -> finnBruttoInntektFraInntektspost(arbeidsgiver, arbeidsforholdRef, ytelsespesifiktGrunnlag, post))
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO)
                .divide(BigDecimal.valueOf(antallPoster), 10, RoundingMode.HALF_UP);
    }

    private BigDecimal finnInntektForFrilans(Intervall periode,
                                             InntektArbeidYtelseGrunnlagDto iay,
                                             YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {
        var inntektFilterDto = new InntektFilterDto(iay.getAktørInntektFraRegister());
        var frilansArbeidstaker = iay.getAktørArbeidFraRegister().map(AktørArbeidDto::hentAlleYrkesaktiviteter)
                .orElse(Collections.emptyList())
                .stream()
                .filter(ya -> ya.getArbeidType().equals(ArbeidType.FRILANSER_OPPDRAGSTAKER_MED_MER))
                .map(YrkesaktivitetDto::getArbeidsgiver)
                .distinct();
        return frilansArbeidstaker.map(a -> finnBeregnetÅrsinntekForArbeidSomFrilanser(periode, ytelsespesifiktGrunnlag, inntektFilterDto, a))
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);


    }

    private static BigDecimal finnBeregnetÅrsinntekForArbeidSomFrilanser(Intervall periode, YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag, InntektFilterDto inntektFilterDto, Arbeidsgiver a) {
        var inntektsposter = inntektFilterDto.filterBeregningsgrunnlag()
                .filter(a)
                .getFiltrertInntektsposter();

        var aktuellePoster = inntektsposter.stream().filter(i -> i.getPeriode().overlapper(
                        Intervall.fraOgMedTilOgMed(periode.getFomDato().minusMonths(1), periode.getTomDato().plusMonths(1))))
                .toList();

        var antallPoster = aktuellePoster.size();
        if (antallPoster == 0) {
            LOG.info("Fant ingen inntektsposter for arbeidsgiver {} i periode {}", a, periode);
            return BigDecimal.ZERO;
        }
        return aktuellePoster.stream().map(post -> finnBruttoInntektFraInntektspostForFrilans(ytelsespesifiktGrunnlag, post))
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO)
                .divide(BigDecimal.valueOf(antallPoster), 10, RoundingMode.HALF_UP);
    }

    private static BigDecimal finnBruttoInntektFraInntektspost(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto arbeidsforholdRef, YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag, InntektspostDto post) {
        var postPeriode = post.getPeriode();
        var virkedagerIPeriode = getBeregnVirkedager(postPeriode);
        if (virkedagerIPeriode == 0) {
            LOG.info("Fant inntektspost uten virkedager for arbeidsgiver {} i periode {}", arbeidsgiver, post.getPeriode());
            return BigDecimal.ZERO;
        }
        var bruttoInntekt = utledBruttoInntektFraTilkommetForArbeidstaker(post.getBeløp().getVerdi(), arbeidsgiver, arbeidsforholdRef, postPeriode, ytelsespesifiktGrunnlag);
        return bruttoInntekt.divide(BigDecimal.valueOf(virkedagerIPeriode), 10, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(260));
    }

    private static BigDecimal finnBruttoInntektFraInntektspostForFrilans(YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag, InntektspostDto post) {
        var postPeriode = post.getPeriode();
        var virkedagerIPeriode = getBeregnVirkedager(postPeriode);
        if (virkedagerIPeriode == 0) {
            return BigDecimal.ZERO;
        }
        var bruttoInntekt = utledBruttoInntektFraTilkommetForFrilans(post.getBeløp().getVerdi(), postPeriode, ytelsespesifiktGrunnlag);
        return bruttoInntekt.divide(BigDecimal.valueOf(virkedagerIPeriode), 10, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(260));
    }


    private static int getBeregnVirkedager(Intervall postPeriode) {
        return Virkedager.beregnVirkedager(postPeriode.getFomDato(), postPeriode.getTomDato());
    }

    private HåndterBeregningsgrunnlagInput lagHåndteringBeregningsgrunnlagInput(KoblingEntitet kobling,
                                                                                KalkulatorInputDto input,
                                                                                BeregningsgrunnlagGrunnlagEntitet aktivGrunnlagEntitet) {
        BeregningsgrunnlagInput beregningsgrunnlagInput = MapFraKalkulator.mapFraKalkulatorInputTilBeregningsgrunnlagInput(kobling, input, Optional.of(aktivGrunnlagEntitet), Collections.emptyList());
        return new HåndterBeregningsgrunnlagInput(beregningsgrunnlagInput, BeregningsgrunnlagTilstand.FASTSATT_INN);
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

    public record ReduksjonVedGradering(long gammelDagsats,
                                        long gradertDagsats,
                                        LocalDate fom,
                                        LocalDate tom,
                                        int antallVirkedager,
                                        boolean erFordelt,
                                        Set<AktivitetStatus> tilkommetStatuser) {
    }


}
