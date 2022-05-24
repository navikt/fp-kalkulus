package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.refusjon;

import static no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.refusjon.Utfall.GODKJENT;
import static no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.refusjon.Utfall.IKKE_VURDERT;
import static no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.refusjon.Utfall.UNDERKJENT;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Refusjonskrav;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.ArbeidsforholdOgInntektsmelding;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.RefusjonskravFrist;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.refusjon.PeriodeModellRefusjon;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.refusjon.Utfall;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapArbeidsforholdFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.FinnAnsettelsesPeriode;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.FinnFørsteDagEtterPermisjon;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.FinnStartdatoPermisjon;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.MapRefusjonskravFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.MapSplittetPeriodeFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.felles.BeregningstidspunktTjeneste;
import no.nav.folketrygdloven.kalkulator.felles.FinnYrkesaktiviteterForBeregningTjeneste;
import no.nav.folketrygdloven.kalkulator.felles.frist.ArbeidsgiverRefusjonskravTjeneste;
import no.nav.folketrygdloven.kalkulator.felles.frist.KravOgUtfall;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.konfig.Konfigverdier;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringerDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktørYtelseDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.permisjon.PermisjonFilter;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;

public abstract class MapRefusjonPerioderFraVLTilRegel {

    protected ArbeidsgiverRefusjonskravTjeneste arbeidsgiverRefusjonskravTjeneste;

    MapRefusjonPerioderFraVLTilRegel() {
    }

    protected MapRefusjonPerioderFraVLTilRegel(ArbeidsgiverRefusjonskravTjeneste arbeidsgiverRefusjonskravTjeneste) {
        this.arbeidsgiverRefusjonskravTjeneste = arbeidsgiverRefusjonskravTjeneste;
    }


    public PeriodeModellRefusjon map(BeregningsgrunnlagInput input,
                                     BeregningsgrunnlagDto beregningsgrunnlag) {
        LocalDate skjæringstidspunkt = beregningsgrunnlag.getSkjæringstidspunkt();
        var beregningsgrunnlagPeriode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        var andeler = beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatusOgAndelList();
        var eksisterendePerioder = beregningsgrunnlag.getBeregningsgrunnlagPerioder().stream()
                .map(MapSplittetPeriodeFraVLTilRegel::map).collect(Collectors.toList());

        var regelInntektsmeldinger = mapInntektsmeldinger(new Input(input, andeler));

        validerIngenOverlappendeReferanseOgRefusjon(regelInntektsmeldinger);

        return PeriodeModellRefusjon.builder()
                .medSkjæringstidspunkt(skjæringstidspunkt)
                .medInntektsmeldinger(regelInntektsmeldinger)
                .medEksisterendePerioder(eksisterendePerioder)
                .medUtfallPrArbeidsgiver(mapRefusjonVurderingUtfallPrArbeidsgiver(input))
                .build();
    }

    private void validerIngenOverlappendeReferanseOgRefusjon(List<ArbeidsforholdOgInntektsmelding> regelInntektsmeldinger) {
        List<Map.Entry<String, List<ArbeidsforholdOgInntektsmelding>>> inntektsmeldingerMedOverlappendeRefusjonOgReferanser = regelInntektsmeldinger.stream()
                .collect(Collectors.groupingBy(a -> a.getArbeidsforhold().getArbeidsgiverId()))
                .entrySet()
                .stream().filter(e -> e.getValue().size() > 1 && harMatchendeReferanserMedOverlappendeRefusjon(e.getValue()))
                .collect(Collectors.toList());

        if (!inntektsmeldingerMedOverlappendeRefusjonOgReferanser.isEmpty()) {
            throw new IllegalStateException("Fant inntektsmeldinger med matchende referanser og overlappende refusjon: " + inntektsmeldingerMedOverlappendeRefusjonOgReferanser);
        }
    }

    private boolean harMatchendeReferanserMedOverlappendeRefusjon(List<ArbeidsforholdOgInntektsmelding> inntekstsmeldinger) {
        return inntekstsmeldinger.stream().anyMatch(im -> harMatchendeReferanserOgOverlappendeRefusjon(inntekstsmeldinger, im.getArbeidsforhold().getArbeidsforholdId()));
    }

    private boolean harMatchendeReferanserOgOverlappendeRefusjon(List<ArbeidsforholdOgInntektsmelding> inntektsmeldinger, String referanse) {
        List<ArbeidsforholdOgInntektsmelding> matchendeReferanser = inntektsmeldinger.stream().filter(im2 ->
                        referanse == null || im2.getArbeidsforhold().getArbeidsforholdId() == null || referanse.equals(im2.getArbeidsforhold().getArbeidsforholdId()))
                .collect(Collectors.toList());
        return harInntektsmeldinglisteOverlappendeRefusjonsperioder(matchendeReferanser);
    }

    private boolean harInntektsmeldinglisteOverlappendeRefusjonsperioder(List<ArbeidsforholdOgInntektsmelding> matchendeReferanser) {
        boolean harIngenOverlapp = matchendeReferanser.stream().map(im2 ->
                        new LocalDateTimeline<>(im2.getRefusjoner().stream()
                                .map(Refusjonskrav::getPeriode)
                                .map(p -> LocalDateSegment.emptySegment(p.getFom(), p.getTom()))
                                .collect(Collectors.toList())))
                .reduce(new LocalDateTimeline<>(Collections.emptyList()), LocalDateTimeline::intersection)
                .isEmpty();
        return !harIngenOverlapp;
    }

    protected Map<no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.refusjon.Arbeidsgiver, LocalDateTimeline<Utfall>> mapRefusjonVurderingUtfallPrArbeidsgiver(BeregningsgrunnlagInput input) {

        InntektArbeidYtelseGrunnlagDto iayGrunnlag = input.getIayGrunnlag();
        BeregningAktivitetAggregatDto gjeldendeAktiviteter = input.getBeregningsgrunnlagGrunnlag().getGjeldendeAktiviteter();
        Optional<BeregningRefusjonOverstyringerDto> refusjonOverstyringer = input.getBeregningsgrunnlagGrunnlag().getRefusjonOverstyringer();
        var filter = new YrkesaktivitetFilterDto(iayGrunnlag.getArbeidsforholdInformasjon(), iayGrunnlag.getAktørArbeidFraRegister());
        var fristvurdertTidslinjePrArbeidsgiver = arbeidsgiverRefusjonskravTjeneste.lagFristTidslinjePrArbeidsgiver(
                filter.getYrkesaktiviteterForBeregning(),
                input.getKravPrArbeidsgiver(),
                gjeldendeAktiviteter,
                input.getSkjæringstidspunktForBeregning(),
                refusjonOverstyringer,
                input.getFagsakYtelseType());
        return fristvurdertTidslinjePrArbeidsgiver.entrySet().stream()
                .map(e -> new HashMap.SimpleEntry<>(mapArbeidsgiver(e.getKey()), mapTilUtfallTidslinje(e.getValue())))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private LocalDateTimeline<Utfall> mapTilUtfallTidslinje(LocalDateTimeline<KravOgUtfall> kravOgUtfallTidslinje) {
        List<LocalDateSegment<Utfall>> utfallSegmenter = kravOgUtfallTidslinje.stream().map(s -> new LocalDateSegment<>(
                        s.getLocalDateInterval(),
                        mapUtfall(s)))
                .collect(Collectors.toList());
        return new LocalDateTimeline<>(utfallSegmenter);
    }

    private Utfall mapUtfall(LocalDateSegment<KravOgUtfall> s) {
        return switch (s.getValue().utfall()) {
            case GODKJENT -> GODKJENT;
            case UNDERKJENT -> UNDERKJENT;
            default -> IKKE_VURDERT;
        };
    }

    private no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.refusjon.Arbeidsgiver mapArbeidsgiver(Arbeidsgiver arbeidsgiver) {
        return arbeidsgiver.getErVirksomhet() ?
                no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.refusjon.Arbeidsgiver.medOrgnr(arbeidsgiver.getIdentifikator()) :
                no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.refusjon.Arbeidsgiver.medAktørId(arbeidsgiver.getIdentifikator());
    }

    private List<ArbeidsforholdOgInntektsmelding> mapInntektsmeldinger(Input inputAndeler) {
        var referanse = inputAndeler.getBeregningsgrunnlagInput().getKoblingReferanse();
        var grunnlag = inputAndeler.getBeregningsgrunnlagInput().getBeregningsgrunnlagGrunnlag();
        var iayGrunnlag = inputAndeler.getBeregningsgrunnlagInput().getIayGrunnlag();
        Collection<YrkesaktivitetDto> yrkesaktiviteterSomErRelevant = FinnYrkesaktiviteterForBeregningTjeneste.finnYrkesaktiviteter(iayGrunnlag, grunnlag, referanse.getSkjæringstidspunktBeregning());
        var alleYtelser = iayGrunnlag.getAktørYtelseFraRegister().map(AktørYtelseDto::getAlleYtelser).orElse(Collections.emptyList());
        var permisjonFilter = new PermisjonFilter(alleYtelser, yrkesaktiviteterSomErRelevant);
        permisjonFilter.medFom(referanse.getSkjæringstidspunktBeregning());
        return inputAndeler.getInntektsmeldinger().stream()
                .filter(this::harRefusjon)
                .filter(im -> erFraRelevantArbeidsgiver(im, yrkesaktiviteterSomErRelevant))
                .map(im -> mapRefusjonForInntektsmelding(im, inputAndeler, permisjonFilter))
                .collect(Collectors.toList());
    }


    private boolean erFraRelevantArbeidsgiver(InntektsmeldingDto im, Collection<YrkesaktivitetDto> yrkesaktiviteterSomErRelevant) {
        return !yrkesaktiviteterSomErRelevant.isEmpty() && yrkesaktiviteterSomErRelevant.stream().anyMatch(ya -> ya.gjelderFor(im));
    }

    private ArbeidsforholdOgInntektsmelding mapRefusjonForInntektsmelding(InntektsmeldingDto im, Input inputAndeler, PermisjonFilter permisjonFilter) {
        Optional<BeregningsgrunnlagPrStatusOgAndelDto> matchendeAndel = finnAndelForInntektsmelding(im, inputAndeler);
        Set<YrkesaktivitetDto> yrkesaktiviteter = finnYrkesaktiviteterForInntektsmelding(im, inputAndeler);
        BeregningsgrunnlagInput beregningsgrunnlagInput = inputAndeler.getBeregningsgrunnlagInput();
        LocalDate skjæringstidspunktOpptjening = beregningsgrunnlagInput.getSkjæringstidspunktOpptjening();
        List<AktivitetsAvtaleDto> alleAnsattperioderForInntektsmeldingEtterStartAvBeregning = finnAnsattperioderForYrkesaktiviteter(yrkesaktiviteter, skjæringstidspunktOpptjening);
        Periode ansettelsesPeriode = FinnAnsettelsesPeriode.getMinMaksPeriode(alleAnsattperioderForInntektsmeldingEtterStartAvBeregning, skjæringstidspunktOpptjening);
        LocalDate startdatoPermisjon = utledStartdatoPermisjon(
                inputAndeler,
                skjæringstidspunktOpptjening,
                im,
                yrkesaktiviteter,
                permisjonFilter).orElse(skjæringstidspunktOpptjening);

        ArbeidsforholdOgInntektsmelding.Builder builder = ArbeidsforholdOgInntektsmelding.builder();
        matchendeAndel.map(BeregningsgrunnlagPrStatusOgAndelDto::getAndelsnr).ifPresent(builder::medAndelsnr);

        List<Refusjonskrav> refusjoner = MapRefusjonskravFraVLTilRegel.periodiserRefusjonsbeløp(
                im,
                startdatoPermisjon,
                beregningsgrunnlagInput.getBeregningsgrunnlagGrunnlag().getRefusjonOverstyringer(),
                finnGyldigeRefusjonPerioder(startdatoPermisjon,
                        beregningsgrunnlagInput.getYtelsespesifiktGrunnlag(),
                        im,
                        alleAnsattperioderForInntektsmeldingEtterStartAvBeregning,
                        yrkesaktiviteter,
                        permisjonFilter
                ));
        builder.medRefusjonskravFrist(new RefusjonskravFrist(
                Konfigverdier.FRIST_MÅNEDER_ETTER_REFUSJON,
                BeregningsgrunnlagHjemmel.REFUSJONSKRAV_FRIST));
        Arbeidsforhold arbeidsforhold = lagArbeidsforhold(im, matchendeAndel);
        return builder.medAnsettelsesperiode(ansettelsesPeriode)
                .medArbeidsforhold(arbeidsforhold)
                .medStartdatoPermisjon(startdatoPermisjon)
                .medRefusjonskrav(refusjoner)
                .build();

    }

    /**
     * Finner gyldige perioder for refusjon
     * <p>
     * For foreldrepenger er alle perioder gyldige
     *
     * @param startdatoPermisjon                                        Startdato permisjon
     * @param ytelsespesifiktGrunnlag                                   Ytelsesspesifikt grunnlag
     * @param inntektsmelding                                           Inntektsmelding
     * @param alleAnsattperioderForInntektsmeldingEtterStartAvBeregning
     * @param yrkesaktiviteter
     * @param permisjonFilter
     * @return Gyldige perioder for refusjon
     */
    protected abstract List<Intervall> finnGyldigeRefusjonPerioder(LocalDate startdatoPermisjon,
                                                                   YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag,
                                                                   InntektsmeldingDto inntektsmelding,
                                                                   List<AktivitetsAvtaleDto> alleAnsattperioderForInntektsmeldingEtterStartAvBeregning, Set<YrkesaktivitetDto> yrkesaktiviteter, PermisjonFilter permisjonFilter);


    private List<AktivitetsAvtaleDto> finnAnsattperioderForYrkesaktiviteter(Set<YrkesaktivitetDto> yrkesaktiviteter, LocalDate skjæringstidspunktBeregning) {
        return yrkesaktiviteter.stream()
                .flatMap(ya -> ya.getAlleAnsettelsesperioder().stream())
                .filter(a -> !a.getPeriode().getTomDato().isBefore(BeregningstidspunktTjeneste.finnBeregningstidspunkt(skjæringstidspunktBeregning)))
                .collect(Collectors.toList());
    }

    private Set<YrkesaktivitetDto> finnYrkesaktiviteterForInntektsmelding(InntektsmeldingDto im, Input inputAndeler) {
        var referanse = inputAndeler.getBeregningsgrunnlagInput().getKoblingReferanse();
        var iayGrunnlag = inputAndeler.getBeregningsgrunnlagInput().getIayGrunnlag();
        var grunnlag = inputAndeler.getBeregningsgrunnlagInput().getBeregningsgrunnlagGrunnlag();
        return FinnYrkesaktiviteterForBeregningTjeneste.finnYrkesaktiviteter(iayGrunnlag, grunnlag, referanse.getSkjæringstidspunktBeregning())
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
                                                          Set<YrkesaktivitetDto> yrkesaktiviteter,
                                                          PermisjonFilter permisjonFilter) {
        Optional<LocalDate> førsteDatoEtterBekreftetPermisjon = yrkesaktiviteter.stream()
                .map(ya -> finnFørsteSøktePermisjonsdag(input.getBeregningsgrunnlagInput(), ya, permisjonFilter))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .min(Comparator.naturalOrder());
        if (førsteDatoEtterBekreftetPermisjon.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(FinnStartdatoPermisjon.finnStartdatoPermisjon(Optional.of(inntektsmelding), skjæringstidspunktBeregning, førsteDatoEtterBekreftetPermisjon.get()));
    }

    private Optional<LocalDate> finnFørsteSøktePermisjonsdag(BeregningsgrunnlagInput input, YrkesaktivitetDto ya, PermisjonFilter permisjonFilter) {
        LocalDate skjæringstidspunktBeregning = input.getBeregningsgrunnlag().getSkjæringstidspunkt();
        Periode ansettelsesPeriode = FinnAnsettelsesPeriode.getMinMaksPeriode(ya.getAlleAnsettelsesperioder(), skjæringstidspunktBeregning);
        Optional<LocalDate> førstedagEtterBekreftetPermisjonOpt = FinnFørsteDagEtterPermisjon.finn(ya, ansettelsesPeriode,
                skjæringstidspunktBeregning, permisjonFilter);
        if (førstedagEtterBekreftetPermisjonOpt.isEmpty()) {
            return Optional.empty();
        }
        return førstedagEtterBekreftetPermisjonOpt;
    }

    private Arbeidsforhold lagArbeidsforhold(InntektsmeldingDto im, Optional<BeregningsgrunnlagPrStatusOgAndelDto> matchendeAndel) {
        return MapArbeidsforholdFraVLTilRegel.mapArbeidsforhold(
                im.getArbeidsgiver(),
                matchendeAndel.flatMap(BeregningsgrunnlagPrStatusOgAndelDto::getBgAndelArbeidsforhold)
                        .map(BGAndelArbeidsforholdDto::getArbeidsforholdRef)
                        .orElse(im.getArbeidsforholdRef()));
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
