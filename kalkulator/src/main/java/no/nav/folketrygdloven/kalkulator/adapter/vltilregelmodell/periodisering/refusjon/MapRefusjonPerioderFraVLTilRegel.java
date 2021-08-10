package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.refusjon;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Gradering;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Refusjonskrav;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.ArbeidsforholdOgInntektsmelding;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.PeriodeModell;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.PeriodisertBruttoBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.RefusjonskravFrist;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapArbeidsforholdFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.FinnAnsettelsesPeriode;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.FinnFørsteDagEtterBekreftetPermisjon;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.FinnStartdatoPermisjon;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.MapPeriodisertBruttoBeregningsgrunnlag;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.MapRefusjonskravFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.MapSplittetPeriodeFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.felles.BeregningstidspunktTjeneste;
import no.nav.folketrygdloven.kalkulator.felles.FinnYrkesaktiviteterForBeregningTjeneste;
import no.nav.folketrygdloven.kalkulator.felles.InntektsmeldingMedRefusjonTjeneste;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.konfig.Konfigverdier;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringerDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;

public abstract class MapRefusjonPerioderFraVLTilRegel {


    protected MapRefusjonPerioderFraVLTilRegel() {
    }

    public PeriodeModell map(BeregningsgrunnlagInput input,
                             BeregningsgrunnlagDto beregningsgrunnlag) {
        LocalDate skjæringstidspunkt = beregningsgrunnlag.getSkjæringstidspunkt();
        var beregningsgrunnlagPeriode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        var andeler = beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatusOgAndelList();
        var eksisterendePerioder = beregningsgrunnlag.getBeregningsgrunnlagPerioder().stream()
                .map(MapSplittetPeriodeFraVLTilRegel::map).collect(Collectors.toList());

        var regelInntektsmeldinger = mapInntektsmeldinger(new Input(input, andeler));

        List<PeriodisertBruttoBeregningsgrunnlag> periodiseringBruttoBg = MapPeriodisertBruttoBeregningsgrunnlag.map(beregningsgrunnlag);

        return PeriodeModell.builder()
                .medSkjæringstidspunkt(skjæringstidspunkt)
                .medGrunnbeløp(beregningsgrunnlag.getGrunnbeløp().getVerdi())
                .medInntektsmeldinger(regelInntektsmeldinger)
                .medEksisterendePerioder(eksisterendePerioder)
                .medPeriodisertBruttoBeregningsgrunnlag(periodiseringBruttoBg)
                .build();
    }

    private List<ArbeidsforholdOgInntektsmelding> mapInntektsmeldinger(Input inputAndeler) {
        var referanse = inputAndeler.getBeregningsgrunnlagInput().getKoblingReferanse();
        var grunnlag = inputAndeler.getBeregningsgrunnlagInput().getBeregningsgrunnlagGrunnlag();
        var iayGrunnlag = inputAndeler.getBeregningsgrunnlagInput().getIayGrunnlag();
        Collection<YrkesaktivitetDto> yrkesaktiviteterSomErRelevant = FinnYrkesaktiviteterForBeregningTjeneste.finnYrkesaktiviteter(referanse, iayGrunnlag, grunnlag);
        Collection<InntektsmeldingDto> inntektsmeldinger = iayGrunnlag.getInntektsmeldinger()
                .map(InntektsmeldingAggregatDto::getInntektsmeldingerSomSkalBrukes)
                .orElse(Collections.emptyList());
        return inntektsmeldinger.stream()
                .filter(this::harRefusjon)
                .filter(im -> erFraRelevantArbeidsgiver(im, yrkesaktiviteterSomErRelevant))
                .map(im -> mapRefusjonForInntektsmelding(im, inputAndeler))
                .collect(Collectors.toList());
    }


    private boolean erFraRelevantArbeidsgiver(InntektsmeldingDto im, Collection<YrkesaktivitetDto> yrkesaktiviteterSomErRelevant) {
        return !yrkesaktiviteterSomErRelevant.isEmpty() && yrkesaktiviteterSomErRelevant.stream().anyMatch(ya -> ya.gjelderFor(im));
    }

    private ArbeidsforholdOgInntektsmelding mapRefusjonForInntektsmelding(InntektsmeldingDto im, Input inputAndeler) {
        Optional<BeregningsgrunnlagPrStatusOgAndelDto> matchendeAndel = finnAndelForInntektsmelding(im, inputAndeler);
        Set<YrkesaktivitetDto> yrkesaktiviteter = finnYrkesaktiviteterForInntektsmelding(im, inputAndeler);
        BeregningsgrunnlagInput beregningsgrunnlagInput = inputAndeler.getBeregningsgrunnlagInput();
        BeregningsgrunnlagDto beregningsgrunnlag = beregningsgrunnlagInput.getBeregningsgrunnlag();
        LocalDate skjæringstidspunktBeregning = beregningsgrunnlag.getSkjæringstidspunkt();
        List<AktivitetsAvtaleDto> alleAnsattperioderForInntektsmeldingEtterStartAvBeregning = finnAnsattperioderForInntektsmelding(yrkesaktiviteter, skjæringstidspunktBeregning);
        Periode ansettelsesPeriode = FinnAnsettelsesPeriode.getMinMaksPeriode(alleAnsattperioderForInntektsmeldingEtterStartAvBeregning, skjæringstidspunktBeregning);
        LocalDate startdatoPermisjon = utledStartdatoPermisjon(
                inputAndeler,
                skjæringstidspunktBeregning,
                im,
                yrkesaktiviteter).orElse(skjæringstidspunktBeregning);

        ArbeidsforholdOgInntektsmelding.Builder builder = ArbeidsforholdOgInntektsmelding.builder();
        matchendeAndel.map(BeregningsgrunnlagPrStatusOgAndelDto::getAndelsnr).ifPresent(builder::medAndelsnr);

        List<Refusjonskrav> refusjoner = MapRefusjonskravFraVLTilRegel.periodiserRefusjonsbeløp(
                im,
                startdatoPermisjon,
                beregningsgrunnlagInput.getBeregningsgrunnlagGrunnlag().getRefusjonOverstyringer(),
                finnGyldigeRefusjonPerioder(startdatoPermisjon, beregningsgrunnlagInput.getYtelsespesifiktGrunnlag(), im, beregningsgrunnlag));

        mapFristData(beregningsgrunnlagInput, im, builder);

        Arbeidsforhold arbeidsforhold = lagArbeidsforhold(im);
        return builder.medAnsettelsesperiode(ansettelsesPeriode)
                .medArbeidsforhold(arbeidsforhold)
                .medUtbetalingsgrader(mapUtbetalingsgrader(im, inputAndeler.getBeregningsgrunnlagInput()))
                .medStartdatoPermisjon(skjæringstidspunktBeregning)
                .medRefusjonskrav(refusjoner)
                .build();

    }

    protected abstract List<Gradering> mapUtbetalingsgrader(InntektsmeldingDto im, BeregningsgrunnlagInput beregningsgrunnlagInput);

    /**
     * Finner gyldige perioder for refusjon
     * <p>
     * For foreldrepenger er alle perioder gyldige
     *
     * @param startdatoPermisjon      Startdato permisjon
     * @param ytelsespesifiktGrunnlag Ytelsesspesifikt grunnlag
     * @param inntektsmelding         Inntektsmelding
     * @param beregningsgrunnlag
     * @return Gyldige perioder for refusjon
     */
    protected abstract List<Intervall> finnGyldigeRefusjonPerioder(LocalDate startdatoPermisjon, YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag, InntektsmeldingDto inntektsmelding, BeregningsgrunnlagDto beregningsgrunnlag);

    // Kun relevant for FP og SVP, burde det ligge ein annen stad enn felles implementasjon?
    protected void mapFristData(BeregningsgrunnlagInput input, InntektsmeldingDto inntektsmelding, ArbeidsforholdOgInntektsmelding.Builder builder) {
        var førsteIMMap = InntektsmeldingMedRefusjonTjeneste.finnFørsteInntektsmeldingMedRefusjon(input);
        LocalDate innsendingsdatoFørsteInntektsmeldingMedRefusjon = førsteIMMap.get(inntektsmelding.getArbeidsgiver());
        if (innsendingsdatoFørsteInntektsmeldingMedRefusjon != null) {
            builder.medInnsendingsdatoFørsteInntektsmeldingMedRefusjon(innsendingsdatoFørsteInntektsmeldingMedRefusjon);
            mapFørsteGyldigeDatoForRefusjon(input.getBeregningsgrunnlagGrunnlag().getRefusjonOverstyringer(), inntektsmelding.getArbeidsgiver()).ifPresent(builder::medOverstyrtRefusjonsFrist);
            builder.medRefusjonskravFrist(new RefusjonskravFrist(Konfigverdier.FRIST_MÅNEDER_ETTER_REFUSJON, BeregningsgrunnlagHjemmel.REFUSJONSKRAV_FRIST));
        }
    }

    private Optional<LocalDate> mapFørsteGyldigeDatoForRefusjon(Optional<BeregningRefusjonOverstyringerDto> refusjonOverstyringer, Arbeidsgiver arbeidsgiver) {
        return refusjonOverstyringer.stream().flatMap(s -> s.getRefusjonOverstyringer().stream())
                .filter(o -> o.getArbeidsgiver().equals(arbeidsgiver))
                .map(BeregningRefusjonOverstyringDto::getFørsteMuligeRefusjonFom)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    private List<AktivitetsAvtaleDto> finnAnsattperioderForInntektsmelding(Set<YrkesaktivitetDto> yrkesaktiviteter, LocalDate skjæringstidspunktBeregning) {
        return yrkesaktiviteter.stream()
                .flatMap(ya -> ya.getAlleAnsettelsesperioder().stream())
                .filter(a -> !a.getPeriode().getTomDato().isBefore(BeregningstidspunktTjeneste.finnBeregningstidspunkt(skjæringstidspunktBeregning)))
                .collect(Collectors.toList());
    }

    private Set<YrkesaktivitetDto> finnYrkesaktiviteterForInntektsmelding(InntektsmeldingDto im, Input inputAndeler) {
        var referanse = inputAndeler.getBeregningsgrunnlagInput().getKoblingReferanse();
        var iayGrunnlag = inputAndeler.getBeregningsgrunnlagInput().getIayGrunnlag();
        var grunnlag = inputAndeler.getBeregningsgrunnlagInput().getBeregningsgrunnlagGrunnlag();
        return FinnYrkesaktiviteterForBeregningTjeneste.finnYrkesaktiviteter(referanse, iayGrunnlag, grunnlag)
                .stream()
                .filter(ya -> ya.gjelderFor(im))
                .collect(Collectors.toSet());
    }

    private Optional<BeregningsgrunnlagPrStatusOgAndelDto> finnAndelForInntektsmelding(InntektsmeldingDto im, Input inputAndeler) {
        return inputAndeler.getAndeler().stream()
                .filter(andel -> andel.getArbeidsgiver().isPresent())
                .filter(andel -> andel.getArbeidsgiver().get().equals(im.getArbeidsgiver()) &&
                        (!im.getArbeidsforholdRef().gjelderForSpesifiktArbeidsforhold() || im.getArbeidsforholdRef().equals(andel.getArbeidsforholdRef().orElse(InternArbeidsforholdRefDto.nullRef()))))
                .findFirst();
    }


    private boolean harRefusjon(InntektsmeldingDto im) {
        return (im.getRefusjonBeløpPerMnd() != null && !im.getRefusjonBeløpPerMnd().erNullEllerNulltall()) || !im.getEndringerRefusjon().isEmpty();
    }


    protected Optional<LocalDate> utledStartdatoPermisjon(Input input,
                                                          LocalDate skjæringstidspunktBeregning,
                                                          InntektsmeldingDto inntektsmelding,
                                                          Set<YrkesaktivitetDto> yrkesaktiviteter) {
        Optional<LocalDate> førsteDatoEtterBekreftetPermisjon = yrkesaktiviteter.stream()
                .map(ya -> finnFørsteSøktePermisjonsdag(input.getBeregningsgrunnlagInput(), ya))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .min(Comparator.naturalOrder());
        if (førsteDatoEtterBekreftetPermisjon.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(FinnStartdatoPermisjon.finnStartdatoPermisjon(Optional.of(inntektsmelding), skjæringstidspunktBeregning, førsteDatoEtterBekreftetPermisjon.get()));
    }

    private Optional<LocalDate> finnFørsteSøktePermisjonsdag(BeregningsgrunnlagInput input, YrkesaktivitetDto ya) {
        LocalDate skjæringstidspunktBeregning = input.getBeregningsgrunnlag().getSkjæringstidspunkt();
        Periode ansettelsesPeriode = FinnAnsettelsesPeriode.getMinMaksPeriode(ya.getAlleAnsettelsesperioder(), skjæringstidspunktBeregning);
        Optional<LocalDate> førstedagEtterBekreftetPermisjonOpt = FinnFørsteDagEtterBekreftetPermisjon.finn(input.getIayGrunnlag(), ya, ansettelsesPeriode,
                skjæringstidspunktBeregning);
        if (førstedagEtterBekreftetPermisjonOpt.isEmpty()) {
            return Optional.empty();
        }
        return førstedagEtterBekreftetPermisjonOpt;
    }

    private Arbeidsforhold lagArbeidsforhold(InntektsmeldingDto im) {
        return MapArbeidsforholdFraVLTilRegel.mapArbeidsforhold(
                im.getArbeidsgiver(),
                im.getArbeidsforholdRef());
    }

    public static class Input {
        private final BeregningsgrunnlagInput beregningsgrunnlagInput;
        private final List<BeregningsgrunnlagPrStatusOgAndelDto> andeler;

        public Input(BeregningsgrunnlagInput input, List<BeregningsgrunnlagPrStatusOgAndelDto> andeler) {
            this.beregningsgrunnlagInput = input;
            this.andeler = Collections.unmodifiableList(andeler);
        }

        public Collection<InntektsmeldingDto> getInntektsmeldinger() {
            return beregningsgrunnlagInput.getInntektsmeldinger();
        }

        public BeregningsgrunnlagInput getBeregningsgrunnlagInput() {
            return beregningsgrunnlagInput;
        }

        public List<BeregningsgrunnlagPrStatusOgAndelDto> getAndeler() {
            return andeler;
        }
    }

}
