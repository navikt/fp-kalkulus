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
import no.nav.folketrygdloven.kalkulator.modell.behandling.BehandlingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringerDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
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


    protected MapFastsettBeregningsgrunnlagPerioderFraVLTilRegel() {
    }

    public PeriodeModell map(BeregningsgrunnlagInput input,
                             BeregningsgrunnlagDto beregningsgrunnlag) {
        precondition(beregningsgrunnlag);
        var ref = input.getBehandlingReferanse();
        AktørId aktørId = ref.getAktørId();
        var iayGrunnlag = input.getIayGrunnlag();
        var filter = new YrkesaktivitetFilterDto(iayGrunnlag.getArbeidsforholdInformasjon(), iayGrunnlag.getAktørArbeidFraRegister(aktørId));
        var førsteIMMap = InntektsmeldingMedRefusjonTjeneste.finnFørsteInntektsmeldingMedRefusjon(input);
        var graderinger = input.getAktivitetGradering().getAndelGradering();

        LocalDate skjæringstidspunkt = beregningsgrunnlag.getSkjæringstidspunkt();

        var beregningsgrunnlagPeriode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        var andeler = beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatusOgAndelList();
        var eksisterendePerioder = beregningsgrunnlag.getBeregningsgrunnlagPerioder().stream()
            .map(MapSplittetPeriodeFraVLTilRegel::map).collect(Collectors.toList());

        var grunnlag = input.getBeregningsgrunnlagGrunnlag();
        var regelInntektsmeldinger = mapInntektsmeldinger(ref,
            iayGrunnlag,
            graderinger,
            filter,
            andeler,
            skjæringstidspunkt,
            førsteIMMap,
            grunnlag);
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

    private List<ArbeidsforholdOgInntektsmelding> mapInntektsmeldinger(BehandlingReferanse referanse,
                                                                       InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                                                       Collection<AndelGradering> andelGraderinger,
                                                                       YrkesaktivitetFilterDto filter,
                                                                       List<BeregningsgrunnlagPrStatusOgAndelDto> andeler,
                                                                       LocalDate skjæringstidspunktBeregning,
                                                                       Map<Arbeidsgiver, LocalDate> førsteIMMap,
                                                                       BeregningsgrunnlagGrunnlagDto grunnlag) {
        Collection<InntektsmeldingDto> inntektsmeldinger = iayGrunnlag.getInntektsmeldinger()
            .map(InntektsmeldingAggregatDto::getInntektsmeldingerSomSkalBrukes)
            .orElse(Collections.emptyList());
        List<ArbeidsforholdOgInntektsmelding> arbeidGraderingOgInntektsmeldinger = new ArrayList<>();
        FinnYrkesaktiviteterForBeregningTjeneste.finnYrkesaktiviteter(referanse, iayGrunnlag, grunnlag)
            .stream()
            .sorted(refusjonFørstComparator(inntektsmeldinger))
            .forEach(ya -> lagArbeidsforholdOgInntektsmelding(iayGrunnlag, andelGraderinger, filter, andeler, skjæringstidspunktBeregning, førsteIMMap, grunnlag, inntektsmeldinger, arbeidGraderingOgInntektsmeldinger, ya));
        return arbeidGraderingOgInntektsmeldinger;
    }

    private Boolean harAlleredeOpprettetAndelOgAktivitetTilkommerEtterStp(LocalDate skjæringstidspunktBeregning, LocalDate startdatoPermisjon, Optional<BeregningsgrunnlagPrStatusOgAndelDto> matchendeAndel) {
        return matchendeAndel.flatMap(BeregningsgrunnlagPrStatusOgAndelDto::getBgAndelArbeidsforhold)
            .map(BGAndelArbeidsforholdDto::getArbeidsforholdRef).map(InternArbeidsforholdRefDto::gjelderForSpesifiktArbeidsforhold)
            .map(gjelderSpesifikt -> !gjelderSpesifikt && startdatoPermisjon.isAfter(skjæringstidspunktBeregning)).orElse(false);
    }

    private Optional<BeregningsgrunnlagPrStatusOgAndelDto> finnMatchendeAndel(List<BeregningsgrunnlagPrStatusOgAndelDto> andeler, YrkesaktivitetDto ya) {
        return andeler.stream()
            .filter(andel -> andel.gjelderSammeArbeidsforhold(ya.getArbeidsgiver(), ya.getArbeidsforholdRef()))
            .findFirst();
    }

    private Comparator<YrkesaktivitetDto> refusjonFørstComparator(Collection<InntektsmeldingDto> inntektsmeldinger) {
        return (y1, y2) -> {
            boolean y1HarRefusjon = inntektsmeldinger.stream()
                .filter(im -> y1.gjelderFor(im.getArbeidsgiver(), im.getArbeidsforholdRef()))
                .anyMatch(im -> (im.getRefusjonBeløpPerMnd() != null && !im.getRefusjonBeløpPerMnd().erNullEllerNulltall()) || im.getEndringerRefusjon().size() > 0);
            boolean y2HarRefusjon = inntektsmeldinger.stream()
                .filter(im -> y2.gjelderFor(im.getArbeidsgiver(), im.getArbeidsforholdRef()))
                .anyMatch(im -> (im.getRefusjonBeløpPerMnd() != null && !im.getRefusjonBeløpPerMnd().erNullEllerNulltall()) || im.getEndringerRefusjon().size() > 0);
            if (y1HarRefusjon && !y2HarRefusjon) {
                return 1;
            } else if (y2HarRefusjon && !y1HarRefusjon) {
                return -1;
            }
            return 0;
        };
    }

    private Optional<LocalDate> utledStartdatoPermisjon(LocalDate skjæringstidspunktBeregning, Collection<InntektsmeldingDto> inntektsmeldinger, YrkesaktivitetDto ya, Periode ansettelsesPeriode, InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        Optional<LocalDate> førstedagEtterBekreftetPermisjonOpt = FinnFørsteDagEtterBekreftetPermisjon.finn(
            iayGrunnlag, ya, ansettelsesPeriode, skjæringstidspunktBeregning);

        if (førstedagEtterBekreftetPermisjonOpt.isEmpty()) {
            return Optional.empty();
        }

        LocalDate førstedagEtterBekreftetPermisjon = førstedagEtterBekreftetPermisjonOpt.get();
        return Optional.of(FinnStartdatoPermisjon.finnStartdatoPermisjon(ya, skjæringstidspunktBeregning,
            førstedagEtterBekreftetPermisjon, inntektsmeldinger));
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

    private void lagArbeidsforholdOgInntektsmelding(InntektArbeidYtelseGrunnlagDto iayGrunnlag, Collection<AndelGradering> andelGraderinger, YrkesaktivitetFilterDto filter, List<BeregningsgrunnlagPrStatusOgAndelDto> andeler,
                                                    LocalDate skjæringstidspunktBeregning, Map<Arbeidsgiver, LocalDate> førsteIMMap, BeregningsgrunnlagGrunnlagDto grunnlag, Collection<InntektsmeldingDto> inntektsmeldinger,
                                                    List<ArbeidsforholdOgInntektsmelding> arbeidGraderingOgInntektsmeldinger, YrkesaktivitetDto ya) {

        Periode ansettelsesPeriode = FinnAnsettelsesPeriode.getMinMaksPeriode(filter.getAnsettelsesPerioder(ya), skjæringstidspunktBeregning);
        Optional<LocalDate> startdatoPermisjonOpt = utledStartdatoPermisjon(skjæringstidspunktBeregning, inntektsmeldinger, ya, ansettelsesPeriode, iayGrunnlag);
        if (startdatoPermisjonOpt.isPresent()) {
            LocalDate startdatoPermisjon = startdatoPermisjonOpt.get();
            ArbeidsforholdOgInntektsmelding.Builder builder = ArbeidsforholdOgInntektsmelding.builder();
            Optional<BeregningsgrunnlagPrStatusOgAndelDto> matchendeAndel = finnMatchendeAndel(andeler, ya);
            if (!harAlleredeOpprettetAndelOgAktivitetTilkommerEtterStp(skjæringstidspunktBeregning, startdatoPermisjon, matchendeAndel)) {
                matchendeAndel.map(BeregningsgrunnlagPrStatusOgAndelDto::getAndelsnr).ifPresent(builder::medAndelsnr);
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
