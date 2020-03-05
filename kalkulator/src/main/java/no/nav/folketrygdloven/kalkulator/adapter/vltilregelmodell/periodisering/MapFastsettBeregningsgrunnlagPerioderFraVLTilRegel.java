package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ArbeidsforholdOgInntektsmelding;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Gradering;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeModell;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.ReferanseType;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SplittetPeriode;
import no.nav.folketrygdloven.kalkulator.FinnYrkesaktiviteterForBeregningTjeneste;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.InntektsmeldingMedRefusjonTjeneste;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapArbeidsforholdFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.gradering.AndelGradering;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringerDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagArbeidstakerAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.AktørId;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;

public abstract class MapFastsettBeregningsgrunnlagPerioderFraVLTilRegel {

    private static final Logger logger = LoggerFactory.getLogger(MapFastsettBeregningsgrunnlagPerioderFraVLTilRegel.class);
    protected MapFastsettBeregningsgrunnlagPerioderFraVLTilRegel() {
    }

    public PeriodeModell map(BeregningsgrunnlagInput input,
                             BeregningsgrunnlagDto beregningsgrunnlag) {
        precondition(beregningsgrunnlag);
        var ref = input.getBehandlingReferanse();
        AktørId aktørId = ref.getAktørId();
        var iayGrunnlag = input.getIayGrunnlag();
        var filter = new YrkesaktivitetFilterDto(iayGrunnlag.getArbeidsforholdInformasjon(), iayGrunnlag.getAktørArbeidFraRegister(aktørId));
        var graderinger = input.getAktivitetGradering().getAndelGradering();

        LocalDate skjæringstidspunkt = beregningsgrunnlag.getSkjæringstidspunkt();

        var beregningsgrunnlagPeriode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        var andeler = beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatusOgAndelList();
        var eksisterendePerioder = beregningsgrunnlag.getBeregningsgrunnlagPerioder().stream()
                .map(MapSplittetPeriodeFraVLTilRegel::map).collect(Collectors.toList());

        var regelInntektsmeldinger = mapInntektsmeldinger(input, andeler);
        var regelAndelGraderinger = graderinger.stream()
                .filter(ag -> !AktivitetStatus.ARBEIDSTAKER.equals(ag.getAktivitetStatus()))
                .map(andelGradering -> MapAndelGradering.mapTilRegelAndelGradering(ref, andelGradering, filter))
                .collect(Collectors.toList());

        return mapPeriodeModell(input,
                beregningsgrunnlag,
                filter,
                skjæringstidspunkt,
                eksisterendePerioder,
                regelInntektsmeldinger,
                List.copyOf(regelAndelGraderinger));
    }


    protected abstract PeriodeModell mapPeriodeModell(BeregningsgrunnlagInput input,
                                                      BeregningsgrunnlagDto beregningsgrunnlag,
                                                      YrkesaktivitetFilterDto filter,
                                                      LocalDate skjæringstidspunkt,
                                                      List<SplittetPeriode> eksisterendePerioder,
                                                      List<ArbeidsforholdOgInntektsmelding> regelInntektsmeldinger,
                                                      List<no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AndelGradering> regelAndelGraderinger);

    protected abstract void mapInntektsmelding(Collection<InntektsmeldingDto> inntektsmeldinger,
                                               Collection<AndelGradering> andelGraderinger,
                                               Map<Arbeidsgiver, LocalDate> førsteIMMap,
                                               YrkesaktivitetDto ya,
                                               LocalDate startdatoPermisjon,
                                               ArbeidsforholdOgInntektsmelding.Builder builder,
                                               Optional<BeregningRefusjonOverstyringerDto> beregningRefusjonOverstyringer);

    protected void precondition(@SuppressWarnings("unused") BeregningsgrunnlagDto beregningsgrunnlag) {
        // template method
    }

    private List<ArbeidsforholdOgInntektsmelding> mapInntektsmeldinger(BeregningsgrunnlagInput input,
                                                                       List<BeregningsgrunnlagPrStatusOgAndelDto> andeler) {
        var referanse = input.getBehandlingReferanse();
        AktørId aktørId = referanse.getAktørId();
        var iayGrunnlag = input.getIayGrunnlag();
        var filter = new YrkesaktivitetFilterDto(iayGrunnlag.getArbeidsforholdInformasjon(), iayGrunnlag.getAktørArbeidFraRegister(aktørId));
        var grunnlag = input.getBeregningsgrunnlagGrunnlag();

        LocalDate skjæringstidspunktBeregning = grunnlag.getBeregningsgrunnlag()
                .orElseThrow(() -> new IllegalStateException("Skal ha beregningsgrunnlag"))
                .getSkjæringstidspunkt();


        Collection<InntektsmeldingDto> inntektsmeldinger = iayGrunnlag.getInntektsmeldinger()
                .map(InntektsmeldingAggregatDto::getInntektsmeldingerSomSkalBrukes)
                .orElse(Collections.emptyList());
        List<ArbeidsforholdOgInntektsmelding> arbeidGraderingOgInntektsmeldinger = new ArrayList<>();
        FinnYrkesaktiviteterForBeregningTjeneste.finnYrkesaktiviteter(referanse, iayGrunnlag, grunnlag)
                .stream()
                .filter(ya -> slutterEtterSkjæringstidspunktet(filter, skjæringstidspunktBeregning, ya))
                .sorted(refusjonFørstComparator(inntektsmeldinger))
                .forEach(ya -> lagArbeidsforholdOgInntektsmelding(
                        input,
                        andeler,
                        arbeidGraderingOgInntektsmeldinger,
                        ya));
        return arbeidGraderingOgInntektsmeldinger;
    }

    private boolean slutterEtterSkjæringstidspunktet(YrkesaktivitetFilterDto filter, LocalDate skjæringstidspunktBeregning, YrkesaktivitetDto ya) {
        Optional<Periode> periode = FinnAnsettelsesPeriode.finnMinMaksPeriode(filter.getAnsettelsesPerioder(ya), skjæringstidspunktBeregning);
        return periode.map(p -> !p.getTom().isBefore(skjæringstidspunktBeregning)).orElse(false);
    }

    private Boolean harAndelMedInntektsmeldingUtenReferanseOgAktivitetTilkommerEtterStp(InntektArbeidYtelseGrunnlagDto iayGrunnlag, LocalDate skjæringstidspunktBeregning,
                                                                                        Optional<BeregningsgrunnlagPrStatusOgAndelDto> matchendeAndel,
                                                                                        Periode ansettelsesPeriode, YrkesaktivitetDto ya) {
        if (matchendeAndel.isEmpty()) {
            return false;
        }
        if (tilkommerPåEllerEtterStp(iayGrunnlag, skjæringstidspunktBeregning, ansettelsesPeriode, ya)) {
            Optional<Boolean> gjelderSpesifikt = matchendeAndel.flatMap(BeregningsgrunnlagPrStatusOgAndelDto::getBgAndelArbeidsforhold)
                    .map(BGAndelArbeidsforholdDto::getArbeidsforholdRef)
                    .map(InternArbeidsforholdRefDto::gjelderForSpesifiktArbeidsforhold);
            Optional<Boolean> harInntektsmelding = matchendeAndel.flatMap(BeregningsgrunnlagPrStatusOgAndelDto::getBeregningsgrunnlagArbeidstakerAndel)
                    .map(BeregningsgrunnlagArbeidstakerAndelDto::getHarInntektsmelding);
            return !gjelderSpesifikt.orElse(false) && harInntektsmelding.orElse(false);
        }
        return false;
    }

    private boolean tilkommerPåEllerEtterStp(InntektArbeidYtelseGrunnlagDto iayGrunnlag, LocalDate skjæringstidspunktBeregning, Periode ansettelsesPeriode, YrkesaktivitetDto ya) {
        return ansettelsesPeriode != null &&
                FinnFørsteDagEtterBekreftetPermisjon.finn(iayGrunnlag, ya, ansettelsesPeriode, skjæringstidspunktBeregning)
                        .map(d -> !d.isBefore(skjæringstidspunktBeregning)).orElse(false);
    }

    private Optional<BeregningsgrunnlagPrStatusOgAndelDto> finnMatchendeAndel(List<BeregningsgrunnlagPrStatusOgAndelDto> andeler, YrkesaktivitetDto ya) {
        return andeler.stream()
                .filter(andel -> andel.gjelderSammeArbeidsforhold(ya.getArbeidsgiver(), ya.getArbeidsforholdRef()))
                .findFirst();
    }

    private Comparator<YrkesaktivitetDto> refusjonFørstComparator(Collection<InntektsmeldingDto> inntektsmeldinger) {
        return (y1, y2) -> {
            boolean y1HarRefusjon = inntektsmeldinger.stream()
                    .filter(im -> gjelderInntektsmeldingFor(y1, im))
                    .anyMatch(this::harRefusjon);
            boolean y2HarRefusjon = inntektsmeldinger.stream()
                    .filter(im -> gjelderInntektsmeldingFor(y2, im))
                    .anyMatch(this::harRefusjon);
            if (y1HarRefusjon && !y2HarRefusjon) {
                return -1;
            } else if (y2HarRefusjon && !y1HarRefusjon) {
                return 1;
            }
            return 0;
        };
    }

    private boolean harRefusjon(InntektsmeldingDto im) {
        return (im.getRefusjonBeløpPerMnd() != null && !im.getRefusjonBeløpPerMnd().erNullEllerNulltall()) || im.getEndringerRefusjon().size() > 0;
    }

    private boolean gjelderInntektsmeldingFor(YrkesaktivitetDto yrkesaktivitet, InntektsmeldingDto im) {
        return yrkesaktivitet.gjelderFor(im.getArbeidsgiver(), im.getArbeidsforholdRef());
    }

    protected Optional<LocalDate> utledStartdatoPermisjon(BeregningsgrunnlagInput input, LocalDate skjæringstidspunktBeregning, Collection<InntektsmeldingDto> inntektsmeldinger, YrkesaktivitetDto ya, Periode ansettelsesPeriode, InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        Optional<LocalDate> førstedagEtterBekreftetPermisjonOpt = finnFørsteSøktePermisjonsdag(input, ya, ansettelsesPeriode);
        if (førstedagEtterBekreftetPermisjonOpt.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(FinnStartdatoPermisjon.finnStartdatoPermisjon(ya, skjæringstidspunktBeregning, førstedagEtterBekreftetPermisjonOpt.get(), inntektsmeldinger));
    }

    protected Optional<LocalDate> finnFørsteSøktePermisjonsdag(BeregningsgrunnlagInput input, YrkesaktivitetDto ya, Periode ansettelsesPeriode) {
        var grunnlag = input.getBeregningsgrunnlagGrunnlag();
        LocalDate skjæringstidspunktBeregning = grunnlag.getBeregningsgrunnlag()
                .orElseThrow(() -> new IllegalStateException("Skal ha beregningsgrunnlag"))
                .getSkjæringstidspunkt();
        Optional<LocalDate> førstedagEtterBekreftetPermisjonOpt = FinnFørsteDagEtterBekreftetPermisjon.finn(input.getIayGrunnlag(), ya, ansettelsesPeriode, skjæringstidspunktBeregning);
        if (førstedagEtterBekreftetPermisjonOpt.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(førstedagEtterBekreftetPermisjonOpt.get());
    }

    private List<Gradering> hentGraderingerSomIkkeErLagtTilFraFør(List<ArbeidsforholdOgInntektsmelding> arbeidGraderingOgInntektsmeldinger, YrkesaktivitetDto ya, List<Gradering> graderinger) {
        List<Gradering> graderingerSomErLagtTil = arbeidGraderingOgInntektsmeldinger.stream().filter(arbeidsforholdOgInntektsmelding -> matchArbeidsforhold(ya, arbeidsforholdOgInntektsmelding))
                .flatMap(a -> a.getGraderinger().stream()).collect(Collectors.toList());
        return graderinger.stream().filter(g1 -> graderingerSomErLagtTil.stream().noneMatch(g2 -> erLike(g1, g2))).collect(Collectors.toList());
    }

    private boolean erLike(Gradering g1, Gradering g2) {
        return g1.getPeriode().equals(g2.getPeriode()) && g1.getUtbetalingsprosent().equals(g2.getUtbetalingsprosent());
    }

    protected abstract List<Gradering> mapGradering(Collection<AndelGradering> andelGraderinger, YrkesaktivitetDto ya);

    private boolean matchArbeidsforhold(YrkesaktivitetDto ya, ArbeidsforholdOgInntektsmelding arbeidsforholdOgInntektsmelding) {
        return matchArbeidsgiver(ya, arbeidsforholdOgInntektsmelding);
    }

    private boolean matchArbeidsgiver(YrkesaktivitetDto ya, ArbeidsforholdOgInntektsmelding arbeidsforholdOgInntektsmelding) {
        if (arbeidsforholdOgInntektsmelding.getArbeidsforhold().getReferanseType().equals(ReferanseType.ORG_NR)) {
            if (!ya.getArbeidsgiver().getErVirksomhet()) {
                return false;
            }
            return arbeidsforholdOgInntektsmelding.getArbeidsforhold().getOrgnr().equals(ya.getArbeidsgiver().getOrgnr());
        } else {
            if (!ya.getArbeidsgiver().erAktørId()) {
                return false;
            }
            return arbeidsforholdOgInntektsmelding.getArbeidsforhold().getAktørId().equals(ya.getArbeidsgiver().getAktørId().getId());
        }
    }

    private Arbeidsforhold lagArbeidsforhold(Collection<InntektsmeldingDto> inntektsmeldinger, YrkesaktivitetDto ya) {
        Optional<InntektsmeldingDto> matchendeInntektsmelding = inntektsmeldinger.stream()
                .filter(im -> ya.gjelderFor(im.getArbeidsgiver(), im.getArbeidsforholdRef()))
                .filter(im -> im.getArbeidsforholdRef().gjelderForSpesifiktArbeidsforhold())
                .findFirst();
        return MapArbeidsforholdFraVLTilRegel.mapArbeidsforhold(
                ya.getArbeidsgiver(),
                matchendeInntektsmelding.isPresent() ? matchendeInntektsmelding.get().getArbeidsforholdRef() : InternArbeidsforholdRefDto.nullRef());
    }

    private void lagArbeidsforholdOgInntektsmelding(BeregningsgrunnlagInput input, List<BeregningsgrunnlagPrStatusOgAndelDto> andeler,
                                                    List<ArbeidsforholdOgInntektsmelding> arbeidGraderingOgInntektsmeldinger,
                                                    YrkesaktivitetDto ya) {
        var referanse = input.getBehandlingReferanse();
        AktørId aktørId = referanse.getAktørId();
        Collection<InntektsmeldingDto> inntektsmeldinger = input.getInntektsmeldinger();
        var iayGrunnlag = input.getIayGrunnlag();
        var filter = new YrkesaktivitetFilterDto(iayGrunnlag.getArbeidsforholdInformasjon(), iayGrunnlag.getAktørArbeidFraRegister(aktørId));
        var førsteIMMap = InntektsmeldingMedRefusjonTjeneste.finnFørsteInntektsmeldingMedRefusjon(input);
        var andelGraderinger = input.getAktivitetGradering().getAndelGradering();
        var grunnlag = input.getBeregningsgrunnlagGrunnlag();

        LocalDate skjæringstidspunktBeregning = grunnlag.getBeregningsgrunnlag()
                .orElseThrow(() -> new IllegalStateException("Skal ha beregningsgrunnlag"))
                .getSkjæringstidspunkt();

        Periode ansettelsesPeriode = FinnAnsettelsesPeriode.getMinMaksPeriode(filter.getAnsettelsesPerioder(ya), skjæringstidspunktBeregning);
        Optional<LocalDate> startdatoPermisjonOpt = utledStartdatoPermisjon(input, skjæringstidspunktBeregning, inntektsmeldinger, ya, ansettelsesPeriode, iayGrunnlag);
        if (startdatoPermisjonOpt.isPresent()) {
            LocalDate startdatoPermisjon = startdatoPermisjonOpt.get();
            ArbeidsforholdOgInntektsmelding.Builder builder = ArbeidsforholdOgInntektsmelding.builder();
            Optional<BeregningsgrunnlagPrStatusOgAndelDto> matchendeAndel = finnMatchendeAndel(andeler, ya);
            logger.info("BehandlingsId={}, Inntektsmeldinger={}, matchende andel={}, ya={} ", input.getBehandlingReferanse().getBehandlingId(), input.getInntektsmeldinger(), matchendeAndel, ya);
            if (!harAndelMedInntektsmeldingUtenReferanseOgAktivitetTilkommerEtterStp(iayGrunnlag, skjæringstidspunktBeregning, matchendeAndel, ansettelsesPeriode, ya)) {
                if (!tilkommerPåEllerEtterStp(iayGrunnlag, skjæringstidspunktBeregning, ansettelsesPeriode, ya)) {
                    matchendeAndel.map(BeregningsgrunnlagPrStatusOgAndelDto::getAndelsnr).ifPresent(builder::medAndelsnr);
                }
                mapInntektsmelding(inntektsmeldinger, andelGraderinger, førsteIMMap, ya, startdatoPermisjon, builder, grunnlag.getRefusjonOverstyringer());
                List<Gradering> graderinger = mapGradering(andelGraderinger, ya);
                List<Gradering> graderingerSomSkalLeggesTil = hentGraderingerSomIkkeErLagtTilFraFør(arbeidGraderingOgInntektsmeldinger, ya, graderinger);
                builder.medGraderinger(graderingerSomSkalLeggesTil);
                Arbeidsforhold arbeidsforhold = lagArbeidsforhold(inntektsmeldinger, ya);
                ArbeidsforholdOgInntektsmelding arbeidsforholdOgInntektsmelding = builder
                        .medAnsettelsesperiode(ansettelsesPeriode)
                        .medArbeidsforhold(arbeidsforhold)
                        .medStartdatoPermisjon(startdatoPermisjon)
                        .build();
                arbeidGraderingOgInntektsmeldinger.add(arbeidsforholdOgInntektsmelding);
            }
        }
    }
}
