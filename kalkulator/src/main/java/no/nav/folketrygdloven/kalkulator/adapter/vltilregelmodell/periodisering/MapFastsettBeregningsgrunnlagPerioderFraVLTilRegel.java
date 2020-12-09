package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Gradering;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.ReferanseType;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.ArbeidsforholdOgInntektsmelding;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.PeriodeModell;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SplittetPeriode;
import no.nav.folketrygdloven.kalkulator.FinnYrkesaktiviteterForBeregningTjeneste;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.InntektsmeldingMedRefusjonTjeneste;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapArbeidsforholdFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringerDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.gradering.AndelGradering;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;

public abstract class MapFastsettBeregningsgrunnlagPerioderFraVLTilRegel {

    protected MapFastsettBeregningsgrunnlagPerioderFraVLTilRegel() {
    }

    public PeriodeModell map(BeregningsgrunnlagInput input,
                             BeregningsgrunnlagDto beregningsgrunnlag) {
        precondition(beregningsgrunnlag);
        var ref = input.getKoblingReferanse();
        var iayGrunnlag = input.getIayGrunnlag();
        var filter = new YrkesaktivitetFilterDto(iayGrunnlag.getArbeidsforholdInformasjon(), iayGrunnlag.getAktørArbeidFraRegister());
        var graderinger = input.getAktivitetGradering() == null ? new HashSet<AndelGradering>() : input.getAktivitetGradering().getAndelGradering();

        LocalDate skjæringstidspunkt = beregningsgrunnlag.getSkjæringstidspunkt();

        var beregningsgrunnlagPeriode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        var andeler = beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatusOgAndelList();
        var eksisterendePerioder = beregningsgrunnlag.getBeregningsgrunnlagPerioder().stream()
            .map(MapSplittetPeriodeFraVLTilRegel::map).collect(Collectors.toList());

        var regelInntektsmeldinger = mapInntektsmeldinger(new Input(input, andeler));
        var regelAndelGraderinger = graderinger.stream()
            .filter(ag -> !AktivitetStatus.ARBEIDSTAKER.equals(ag.getAktivitetStatus()))
            .map(andelGradering -> MapAndelGradering.mapTilRegelAndelGraderingForFLSN(beregningsgrunnlag, ref, andelGradering, filter))
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
                                                      List<no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.AndelGradering> regelAndelGraderinger);

    protected abstract void mapInntektsmelding(BeregningsgrunnlagInput input,
                                               Collection<InntektsmeldingDto> inntektsmeldinger,
                                               Map<Arbeidsgiver, LocalDate> førsteIMMap,
                                               YrkesaktivitetDto ya,
                                               LocalDate startdatoPermisjon,
                                               ArbeidsforholdOgInntektsmelding.Builder builder,
                                               Optional<BeregningRefusjonOverstyringerDto> beregningRefusjonOverstyringer);

    protected void precondition(@SuppressWarnings("unused") BeregningsgrunnlagDto beregningsgrunnlag) {
        // template method
    }

    private List<ArbeidsforholdOgInntektsmelding> mapInntektsmeldinger(Input inputAndeler) {
        var referanse = inputAndeler.getBeregningsgrunnlagInput().getKoblingReferanse();
        var iayGrunnlag = inputAndeler.getBeregningsgrunnlagInput().getIayGrunnlag();
        var filter = new YrkesaktivitetFilterDto(iayGrunnlag.getArbeidsforholdInformasjon(), iayGrunnlag.getAktørArbeidFraRegister());
        var grunnlag = inputAndeler.getBeregningsgrunnlagInput().getBeregningsgrunnlagGrunnlag();

        LocalDate skjæringstidspunktBeregning = grunnlag.getBeregningsgrunnlag()
            .orElseThrow(() -> new IllegalStateException("Skal ha beregningsgrunnlag"))
            .getSkjæringstidspunkt();

        Collection<InntektsmeldingDto> inntektsmeldinger = iayGrunnlag.getInntektsmeldinger()
            .map(InntektsmeldingAggregatDto::getInntektsmeldingerSomSkalBrukes)
            .orElse(Collections.emptyList());

        var resultat = new Resultat();
        FinnYrkesaktiviteterForBeregningTjeneste.finnYrkesaktiviteter(referanse, iayGrunnlag, grunnlag)
            .stream()
            .filter(ya -> slutterEtterSkjæringstidspunktet(filter, skjæringstidspunktBeregning, ya))
            .sorted(refusjonFørstComparator(inntektsmeldinger))
            .forEach(ya -> lagArbeidsforholdOgInntektsmelding(
                inputAndeler,
                resultat,
                ya));
        return resultat.getArbeidGraderingOgInntektsmeldinger();
    }

    private boolean slutterEtterSkjæringstidspunktet(YrkesaktivitetFilterDto filter, LocalDate skjæringstidspunktBeregning, YrkesaktivitetDto ya) {
        Optional<Periode> periode = FinnAnsettelsesPeriode.finnMinMaksPeriode(filter.getAnsettelsesPerioder(ya), skjæringstidspunktBeregning);
        return periode.map(p -> !p.getTom().isBefore(skjæringstidspunktBeregning)).orElse(false);
    }

    private Boolean harAggregertAndelOgAktivitetTilkommerEtterStp(BeregningsgrunnlagInput input,
                                                                  InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                                                  LocalDate skjæringstidspunktBeregning,
                                                                  Optional<BeregningsgrunnlagPrStatusOgAndelDto> matchendeAndel,
                                                                  Periode ansettelsesPeriode, YrkesaktivitetDto ya) {
        if (matchendeAndel.isEmpty()) {
            return false;
        }
        if (tilkommerPåEllerEtterStp(iayGrunnlag, skjæringstidspunktBeregning, ansettelsesPeriode, ya)) {
            return erAggregertAndel(matchendeAndel) && harMottattInntektsmeldingSomSkalMappesTilAndel(matchendeAndel, input);
        }
        return false;
    }

    private Boolean harMottattInntektsmeldingSomSkalMappesTilAndel(Optional<BeregningsgrunnlagPrStatusOgAndelDto> matchendeAndel,
                                                                   BeregningsgrunnlagInput input) {
        Collection<InntektsmeldingDto> inntektsmeldinger = input.getInntektsmeldinger();
        Collection<YrkesaktivitetDto> yrkesaktiviteter = new YrkesaktivitetFilterDto(input.getIayGrunnlag().getArbeidsforholdInformasjon(),
            input.getIayGrunnlag().getAktørArbeidFraRegister()).getYrkesaktiviteterForBeregning();
        Optional<Boolean> harInntektsmelding = matchendeAndel.flatMap(BeregningsgrunnlagPrStatusOgAndelDto::getBgAndelArbeidsforhold)
            .map(
                arb -> InntektsmeldingForAndel.harInntektsmeldingForAndel(arb, inntektsmeldinger, yrkesaktiviteter, input.getSkjæringstidspunktForBeregning()));
        return harInntektsmelding.orElse(false);
    }

    private boolean erAggregertAndel(Optional<BeregningsgrunnlagPrStatusOgAndelDto> matchendeAndel) {
        Optional<Boolean> gjelderSpesifikt = matchendeAndel.flatMap(BeregningsgrunnlagPrStatusOgAndelDto::getBgAndelArbeidsforhold)
            .map(BGAndelArbeidsforholdDto::getArbeidsforholdRef)
            .map(InternArbeidsforholdRefDto::gjelderForSpesifiktArbeidsforhold);
        return !gjelderSpesifikt.orElse(false);
    }

    private boolean tilkommerPåEllerEtterStp(InntektArbeidYtelseGrunnlagDto iayGrunnlag, LocalDate skjæringstidspunktBeregning, Periode ansettelsesPeriode,
                                             YrkesaktivitetDto ya) {
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
        return yrkesaktivitet.gjelderFor(im);
    }

    protected Optional<LocalDate> utledStartdatoPermisjon(Input input,
                                                          LocalDate skjæringstidspunktBeregning,
                                                          YrkesaktivitetDto ya, Periode ansettelsesPeriode) {
        Optional<LocalDate> førstedagEtterBekreftetPermisjonOpt = finnFørsteSøktePermisjonsdag(input.getBeregningsgrunnlagInput(), ya, ansettelsesPeriode);
        if (førstedagEtterBekreftetPermisjonOpt.isEmpty()) {
            return Optional.empty();
        }
        return Optional
            .of(FinnStartdatoPermisjon.finnStartdatoPermisjon(ya, skjæringstidspunktBeregning, førstedagEtterBekreftetPermisjonOpt.get(),
                input.getInntektsmeldinger()));
    }

    protected Optional<LocalDate> finnFørsteSøktePermisjonsdag(BeregningsgrunnlagInput input, YrkesaktivitetDto ya, Periode ansettelsesPeriode) {
        var grunnlag = input.getBeregningsgrunnlagGrunnlag();
        LocalDate skjæringstidspunktBeregning = grunnlag.getBeregningsgrunnlag()
            .orElseThrow(() -> new IllegalStateException("Skal ha beregningsgrunnlag"))
            .getSkjæringstidspunkt();
        Optional<LocalDate> førstedagEtterBekreftetPermisjonOpt = FinnFørsteDagEtterBekreftetPermisjon.finn(input.getIayGrunnlag(), ya, ansettelsesPeriode,
            skjæringstidspunktBeregning);
        if (førstedagEtterBekreftetPermisjonOpt.isEmpty()) {
            return Optional.empty();
        }
        return førstedagEtterBekreftetPermisjonOpt;
    }

    private List<Gradering> hentGraderingerSomIkkeErLagtTilFraFør(Resultat resultat,
                                                                  YrkesaktivitetDto ya, List<Gradering> graderinger) {
        List<Gradering> graderingerSomErLagtTil = resultat.getArbeidGraderingOgInntektsmeldinger().stream()
            .filter(arbeidsforholdOgInntektsmelding -> matchArbeidsforhold(ya, arbeidsforholdOgInntektsmelding))
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
            .filter(im -> ya.gjelderFor(im))
            .filter(im -> im.getArbeidsforholdRef().gjelderForSpesifiktArbeidsforhold())
            .findFirst();
        return MapArbeidsforholdFraVLTilRegel.mapArbeidsforhold(
            ya.getArbeidsgiver(),
            matchendeInntektsmelding.isPresent() ? matchendeInntektsmelding.get().getArbeidsforholdRef() : InternArbeidsforholdRefDto.nullRef());
    }

    private void lagArbeidsforholdOgInntektsmelding(Input inputAndeler,
                                                    Resultat resultat,
                                                    YrkesaktivitetDto ya) {
        BeregningsgrunnlagInput beregningsgrunnlagInput = inputAndeler.getBeregningsgrunnlagInput();
        Collection<InntektsmeldingDto> inntektsmeldinger = inputAndeler.getInntektsmeldinger();
        var iayGrunnlag = beregningsgrunnlagInput.getIayGrunnlag();
        var filter = new YrkesaktivitetFilterDto(iayGrunnlag.getArbeidsforholdInformasjon(), iayGrunnlag.getAktørArbeidFraRegister());
        var førsteIMMap = InntektsmeldingMedRefusjonTjeneste.finnFørsteInntektsmeldingMedRefusjon(beregningsgrunnlagInput);
        var andelGraderinger = beregningsgrunnlagInput.getAktivitetGradering() == null
            ? new ArrayList<AndelGradering>()
            : beregningsgrunnlagInput.getAktivitetGradering().getAndelGradering();
        var grunnlag = beregningsgrunnlagInput.getBeregningsgrunnlagGrunnlag();

        LocalDate skjæringstidspunktBeregning = grunnlag.getBeregningsgrunnlag()
            .orElseThrow(() -> new IllegalStateException("Skal ha beregningsgrunnlag"))
            .getSkjæringstidspunkt();

        Periode ansettelsesPeriode = FinnAnsettelsesPeriode.getMinMaksPeriode(filter.getAnsettelsesPerioder(ya), skjæringstidspunktBeregning);
        Optional<LocalDate> startdatoPermisjonOpt = utledStartdatoPermisjon(inputAndeler, skjæringstidspunktBeregning, ya, ansettelsesPeriode);

        if (startdatoPermisjonOpt.isPresent()) {
            LocalDate startdatoPermisjon = startdatoPermisjonOpt.get();
            ArbeidsforholdOgInntektsmelding.Builder builder = ArbeidsforholdOgInntektsmelding.builder();
            Optional<BeregningsgrunnlagPrStatusOgAndelDto> matchendeAndel = finnMatchendeAndel(inputAndeler.getAndeler(), ya);

            if (!harAggregertAndelOgAktivitetTilkommerEtterStp(beregningsgrunnlagInput, iayGrunnlag, skjæringstidspunktBeregning, matchendeAndel,
                ansettelsesPeriode, ya)) {
                Optional<InntektsmeldingDto> inntektsmelding = finnMatchendeInntektsmelding(ya, inntektsmeldinger);
                if (!tilkommerPåEllerEtterStp(iayGrunnlag, skjæringstidspunktBeregning, ansettelsesPeriode, ya)
                    || harAggregertAndelOgMottattInntektsmeldingUtenId(matchendeAndel, inntektsmelding)) {
                    matchendeAndel.map(BeregningsgrunnlagPrStatusOgAndelDto::getAndelsnr).ifPresent(builder::medAndelsnr);
                }
                mapInntektsmelding(beregningsgrunnlagInput, inntektsmeldinger, førsteIMMap, ya, startdatoPermisjon, builder,
                    grunnlag.getRefusjonOverstyringer());
                List<Gradering> graderinger = mapGradering(andelGraderinger, ya);
                List<Gradering> graderingerSomSkalLeggesTil = hentGraderingerSomIkkeErLagtTilFraFør(resultat, ya, graderinger);
                builder.medGraderinger(graderingerSomSkalLeggesTil);

                Arbeidsforhold arbeidsforhold = lagArbeidsforhold(inntektsmeldinger, ya);
                ArbeidsforholdOgInntektsmelding arbeidsforholdOgInntektsmelding = builder
                    .medAnsettelsesperiode(ansettelsesPeriode)
                    .medArbeidsforhold(arbeidsforhold)
                    .medStartdatoPermisjon(startdatoPermisjon)
                    .build();
                resultat.getArbeidGraderingOgInntektsmeldinger().add(arbeidsforholdOgInntektsmelding);
            }
        }
    }

    private Optional<InntektsmeldingDto> finnMatchendeInntektsmelding(YrkesaktivitetDto ya, Collection<InntektsmeldingDto> inntektsmeldinger) {
        return inntektsmeldinger.stream()
            .filter(im -> ya.gjelderFor(im))
            .findFirst();
    }

    private boolean harAggregertAndelOgMottattInntektsmeldingUtenId(Optional<BeregningsgrunnlagPrStatusOgAndelDto> matchendeAndel,
                                                                    Optional<InntektsmeldingDto> inntektsmelding) {
        return erAggregertAndel(matchendeAndel) && inntektsmelding.stream().noneMatch(InntektsmeldingDto::gjelderForEtSpesifiktArbeidsforhold);
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

    static class Resultat {
        final List<ArbeidsforholdOgInntektsmelding> arbeidGraderingOgInntektsmeldinger = new ArrayList<>();

        List<ArbeidsforholdOgInntektsmelding> getArbeidGraderingOgInntektsmeldinger() {
            return arbeidGraderingOgInntektsmeldinger;
        }

    }
}
