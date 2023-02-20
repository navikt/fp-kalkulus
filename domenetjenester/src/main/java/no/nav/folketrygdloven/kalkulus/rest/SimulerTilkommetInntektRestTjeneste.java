package no.nav.folketrygdloven.kalkulus.rest;

import static no.nav.folketrygdloven.kalkulus.sikkerhet.KalkulusBeskyttetRessursAttributtMiljøvariabel.DRIFT;
import static no.nav.k9.felles.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

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

        var saksnummerMedReduksjon = bgPrSaksnummer.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> mapReduksjoner(koblinger, inputer, e)));

        var dagsatsFeiltoleranse = spesifikasjon.getDagsatsFeiltoleranse() != null ? spesifikasjon.getDagsatsFeiltoleranse() : 0L;

        var antallSaksnummerMedReduksjon = saksnummerMedReduksjon.entrySet().stream()
                .filter(e -> e.getValue().stream().anyMatch(r -> (r.gammelDagsats - r.gradertDagsats) > dagsatsFeiltoleranse))
                .count();
        var antallSaksnummerMedAksjonspunkt = saksnummerMedReduksjon.entrySet().stream().filter(e -> !e.getValue().isEmpty()).count();
        return Response.ok(new SimulertTilkommetInntekt(antallSaksnummerMedAksjonspunkt, antallSaksnummerMedReduksjon, bgPrSaksnummer.size())).build();
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
        return nyttBg.getBeregningsgrunnlagPerioder().stream()
                .filter(p -> !p.getTilkomneInntekter().isEmpty())
                .map(p -> {
                    var dagsatsFørGradering = p.getDagsats();
                    var dagsatsEtterGradering = finnDagsatsEtterGradering(regelResultatPerioder, p, dagsatsFørGradering);
                    var virkedager = Virkedager.beregnVirkedager(p.getPeriode().getFomDato(), p.getPeriode().getTomDato());
                    return new ReduksjonVedGradering(dagsatsFørGradering, dagsatsEtterGradering,
                            p.getPeriode().getFomDato(),
                            p.getPeriode().getTomDato(),
                            virkedager);
                }).toList();
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
                    iay);

        } else {
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

    private BigDecimal finnInntektForArbeidsgiver(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto arbeidsforholdRef,
                                                  Intervall periode, InntektArbeidYtelseGrunnlagDto iay) {
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
        var inntektspost = inntektsposter.stream().filter(i -> i.getPeriode().inkluderer(periode.getFomDato())).findFirst();
        if (inntektspost.isPresent()) {
            return inntektspost.get().getBeløp().multipliser(12).getVerdi();
        }
        return BigDecimal.ZERO;
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

    public record ReduksjonVedGradering(long gammelDagsats, long gradertDagsats,
                                        LocalDate fom,
                                        LocalDate tom,
                                        int antallVirkedager) {
    }


}
