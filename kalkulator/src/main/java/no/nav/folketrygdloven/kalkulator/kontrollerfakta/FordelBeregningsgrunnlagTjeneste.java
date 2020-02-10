package no.nav.folketrygdloven.kalkulator.kontrollerfakta;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.BeregningInntektsmeldingTjeneste;
import no.nav.folketrygdloven.kalkulator.gradering.AktivitetGradering;
import no.nav.folketrygdloven.kalkulator.gradering.AndelGradering;
import no.nav.folketrygdloven.kalkulator.gradering.AndelGradering.Gradering;
import no.nav.folketrygdloven.kalkulator.modell.behandling.BehandlingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;

public class FordelBeregningsgrunnlagTjeneste {

    public enum VurderManuellBehandling {
        NYTT_ARBEIDSFORHOLD,
        FL_ELLER_SN_TILKOMMER,
        TOTALT_REFUSJONSKRAV_STØRRE_ENN_6G,
        REFUSJON_STØRRE_ENN_OPPGITT_INNTEKT_OG_HAR_AAP,
        GRADERT_ANDEL_SOM_VILLE_HA_BLITT_AVKORTET_TIL_0,
        FORESLÅTT_BG_PÅ_GRADERT_ANDEL_ER_0
    }

    private FordelBeregningsgrunnlagTjeneste() {
        // Skjul
    }

    public static List<Gradering> hentGraderingerForAndelIPeriode(BeregningsgrunnlagPrStatusOgAndelDto andel, AktivitetGradering aktivitetGradering, Intervall periode) {
        Optional<AndelGradering> graderingOpt = BeregningInntektsmeldingTjeneste.finnGraderingForAndel(andel, aktivitetGradering);
        if (graderingOpt.isPresent()) {
            AndelGradering andelGradering = graderingOpt.get();
            return andelGradering.getGraderinger().stream()
                .filter(gradering -> gradering.getPeriode().overlapper(periode))
                .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    public static List<Gradering> hentGraderingerForAndel(BeregningsgrunnlagPrStatusOgAndelDto andel, AktivitetGradering aktivitetGradering) {
        Optional<AndelGradering> graderingOpt = BeregningInntektsmeldingTjeneste.finnGraderingForAndel(andel, aktivitetGradering);
        if (graderingOpt.isPresent()) {
            AndelGradering gradering = graderingOpt.get();
            return gradering.getGraderinger();
        }
        return Collections.emptyList();
    }

    /** Dersom returnerer {@link #vurderManuellBehandling(BehandlingReferanse, BeregningsgrunnlagDto, BeregningAktivitetAggregatDto)} så bør manuell behandling .... */
    public static Optional<VurderManuellBehandling> vurderManuellBehandling(BeregningsgrunnlagDto beregningsgrunnlag,
                                                                     BeregningAktivitetAggregatDto beregningAktivitetAggregat,
                                                                     AktivitetGradering aktivitetGradering,
                                                                     Collection<InntektsmeldingDto> inntektsmeldinger) {
        return vurderManuellBehandlingForGraderingEllerEndretRefusjon(beregningsgrunnlag, beregningAktivitetAggregat, aktivitetGradering, inntektsmeldinger);
    }

    private static Optional<VurderManuellBehandling> vurderManuellBehandlingForGraderingEllerEndretRefusjon(BeregningsgrunnlagDto beregningsgrunnlag,
                                                                                                            BeregningAktivitetAggregatDto beregningAktivitetAggregat,
                                                                                                            AktivitetGradering aktivitetGradering,
                                                                                                            Collection<InntektsmeldingDto> inntektsmeldinger) {
        for (BeregningsgrunnlagPeriodeDto periode : beregningsgrunnlag.getBeregningsgrunnlagPerioder()) {
            Optional<VurderManuellBehandling> vurderManuell = vurderManuellBehandlingForPeriode(periode, beregningAktivitetAggregat,
                aktivitetGradering, inntektsmeldinger, beregningsgrunnlag.getGrunnbeløp(),
                beregningsgrunnlag.getSkjæringstidspunkt());
            if (vurderManuell.isPresent())
                return vurderManuell;
        }
        return Optional.empty();
    }

    private static Optional<VurderManuellBehandling> vurderManuellBehandlingForPeriode(BeregningsgrunnlagPeriodeDto periode,
                                                                                       BeregningAktivitetAggregatDto beregningAktivitetAggregat,
                                                                                       AktivitetGradering aktivitetGradering,
                                                                                       Collection<InntektsmeldingDto> inntektsmeldinger, Beløp grunnbeløp, LocalDate skjæringstidspunkt) {
        boolean erTotaltRefusjonskravStørreEnnSeksG = BeregningInntektsmeldingTjeneste.erTotaltRefusjonskravStørreEnnEllerLikSeksG(periode, inntektsmeldinger, grunnbeløp);
        boolean harNoenAndelerMedAap = harNoenAndelerMedAAP(periode);

        for (BeregningsgrunnlagPrStatusOgAndelDto andel : periode.getBeregningsgrunnlagPrStatusOgAndelList()) {
            Optional<VurderManuellBehandling> vurderManuell = vurderManuellBehandlingForAndel(andel,
                aktivitetGradering,
                beregningAktivitetAggregat,
                erTotaltRefusjonskravStørreEnnSeksG,
                harNoenAndelerMedAap,
                inntektsmeldinger,
                periode,
                grunnbeløp,
                skjæringstidspunkt);
            if (vurderManuell.isPresent()) {
                return vurderManuell;
            }
        }
        return Optional.empty();
    }

    /**
     * @deprecated TODO : refactor kode som kaller på denne metoden slik at det blir tydeligere hva som brukes her.
     */
    @Deprecated(forRemoval=true)
    public static Optional<VurderManuellBehandling> vurderManuellBehandlingForAndel(BeregningsgrunnlagPeriodeDto periode,
                                                                             BeregningsgrunnlagPrStatusOgAndelDto andel,
                                                                             AktivitetGradering aktivitetGradering,
                                                                             BeregningAktivitetAggregatDto beregningAktivitetAggregat,
                                                                             Collection<InntektsmeldingDto> inntektsmeldinger,
                                                                             Beløp grunnbeløp, LocalDate skjæringstidspunkt) {
        boolean erTotaltRefusjonskravStørreEnnSeksG = BeregningInntektsmeldingTjeneste.erTotaltRefusjonskravStørreEnnEllerLikSeksG(periode, inntektsmeldinger, grunnbeløp);
        boolean harNoenAndelerMedAap = harNoenAndelerMedAAP(periode);
        return vurderManuellBehandlingForAndel(andel, aktivitetGradering, beregningAktivitetAggregat,
            erTotaltRefusjonskravStørreEnnSeksG,
            harNoenAndelerMedAap,
            inntektsmeldinger,
            periode,
            grunnbeløp,
            skjæringstidspunkt);
    }

    // TODO: refaktorer
    private static Optional<VurderManuellBehandling> vurderManuellBehandlingForAndel(BeregningsgrunnlagPrStatusOgAndelDto andel,
                                                                                     AktivitetGradering aktivitetGradering,
                                                                                     BeregningAktivitetAggregatDto beregningAktivitetAggregat,
                                                                                     boolean harTotaltRefusjonskravStørreEnn6G,
                                                                                     boolean harNoenAndelerMedAap,
                                                                                     Collection<InntektsmeldingDto> inntektsmeldinger,
                                                                                     BeregningsgrunnlagPeriodeDto periode,
                                                                                     Beløp grunnbeløp, LocalDate skjæringstidspunkt) {
        boolean harGraderingIBGPeriode = !hentGraderingerForAndelIPeriode(andel, aktivitetGradering, periode.getPeriode()).isEmpty();
        BigDecimal refusjonskravPrÅr = BeregningInntektsmeldingTjeneste.finnRefusjonskravPrÅrIPeriodeForAndel(andel, periode.getPeriode(), inntektsmeldinger).orElse(BigDecimal.ZERO);

        boolean harRefusjonIPerioden = refusjonskravPrÅr.compareTo(BigDecimal.ZERO) != 0;

        boolean erNytt = erNyttArbeidsforhold(andel, beregningAktivitetAggregat, skjæringstidspunkt);
        if (erNytt) {
            return Optional.of(VurderManuellBehandling.NYTT_ARBEIDSFORHOLD);
        }

        if (!harGraderingIBGPeriode && !harRefusjonIPerioden) {
            return Optional.empty();
        }

        if (erNyFLSNAndel(andel, beregningAktivitetAggregat, skjæringstidspunkt)) {
            return Optional.of(VurderManuellBehandling.FL_ELLER_SN_TILKOMMER);
        }

        if (skalGraderePåAndelUtenBeregningsgrunnlag(andel, harGraderingIBGPeriode)) {
            return Optional.of(VurderManuellBehandling.FORESLÅTT_BG_PÅ_GRADERT_ANDEL_ER_0);
        }

        if (harGraderingUtenRefusjon(harGraderingIBGPeriode, harRefusjonIPerioden)) {
            if (harTotaltRefusjonskravStørreEnn6G) {
                return Optional.of(VurderManuellBehandling.TOTALT_REFUSJONSKRAV_STØRRE_ENN_6G);
            }
            if (gradertAndelVilleBlittAvkortet(andel, grunnbeløp, periode)) {
                return Optional.of(VurderManuellBehandling.GRADERT_ANDEL_SOM_VILLE_HA_BLITT_AVKORTET_TIL_0);
            }
        }

        if (harAndelerMedAAPOgRefusjonOverstigerInntekt(andel, harNoenAndelerMedAap, periode.getPeriode(), inntektsmeldinger)) {
            return Optional.of(VurderManuellBehandling.REFUSJON_STØRRE_ENN_OPPGITT_INNTEKT_OG_HAR_AAP);
        }
        return Optional.empty();
    }

    private static boolean harAndelerMedAAPOgRefusjonOverstigerInntekt(BeregningsgrunnlagPrStatusOgAndelDto andel,
                                                                       boolean harNoenAndelerMedAap,
                                                                       Intervall periode,
                                                                       Collection<InntektsmeldingDto> inntektsmeldinger) {
        BigDecimal refusjonskravForAndelIPeriode = BeregningInntektsmeldingTjeneste.finnRefusjonskravPrÅrIPeriodeForAndel(andel, periode, inntektsmeldinger).orElse(BigDecimal.ZERO);
        return harNoenAndelerMedAap && harHøyereRefusjonEnnInntekt(refusjonskravForAndelIPeriode, andel);
    }

    private static boolean harGraderingUtenRefusjon(boolean harGraderingIBGPeriode, boolean harRefusjonIPerioden) {
        return harGraderingIBGPeriode && !harRefusjonIPerioden;
    }

    private static boolean skalGraderePåAndelUtenBeregningsgrunnlag(BeregningsgrunnlagPrStatusOgAndelDto andel, boolean harGraderingIBGPeriode) {
        boolean harIkkjeBeregningsgrunnlag = hentBeløpForAndelSomErGjeldendeForFordeling(andel).compareTo(BigDecimal.ZERO) == 0;
        return harGraderingIBGPeriode && harIkkjeBeregningsgrunnlag;
    }

    private static boolean gradertAndelVilleBlittAvkortet(BeregningsgrunnlagPrStatusOgAndelDto andel, Beløp grunnbeløp, BeregningsgrunnlagPeriodeDto periode) {
        if (erStatusSomAvkortesVedATOver6G(andel)) {
            BigDecimal totaltBgFraArbeidstaker = periode.getBeregningsgrunnlagPrStatusOgAndelList()
                .stream()
                .filter(a -> a.getAktivitetStatus().erArbeidstaker())
                .map(FordelBeregningsgrunnlagTjeneste::hentBeløpForAndelSomErGjeldendeForFordeling)
                .filter(Objects::nonNull)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
            BigDecimal seksG = grunnbeløp.getVerdi().multiply(BigDecimal.valueOf(6));
            return totaltBgFraArbeidstaker.compareTo(seksG) > 0;
        }
        return false;
    }

    private static boolean erStatusSomAvkortesVedATOver6G(BeregningsgrunnlagPrStatusOgAndelDto andel) {
        AktivitetStatus aktivitetStatus = andel.getAktivitetStatus();
        return !aktivitetStatus.erArbeidstaker();
    }

    private static boolean erNyFLSNAndel(BeregningsgrunnlagPrStatusOgAndelDto andel, BeregningAktivitetAggregatDto beregningAktivitetAggregat, LocalDate skjæringstidspunkt) {
        if (andel.getAktivitetStatus().erFrilanser()) {
            return erNyAndelMedType(beregningAktivitetAggregat, skjæringstidspunkt, OpptjeningAktivitetType.FRILANS);
        }
        if (andel.getAktivitetStatus().erSelvstendigNæringsdrivende()) {
            return erNyAndelMedType(beregningAktivitetAggregat, skjæringstidspunkt, OpptjeningAktivitetType.NÆRING);
        }
        return false;
    }

    private static boolean erNyAndelMedType(BeregningAktivitetAggregatDto beregningAktivitetAggregat, LocalDate skjæringstidspunkt, OpptjeningAktivitetType opptjeningAktivitetType) {
        return beregningAktivitetAggregat.getBeregningAktiviteter().stream()
            .filter(beregningAktivitet -> !beregningAktivitet.getPeriode().getTomDato().isBefore(skjæringstidspunkt.minusDays(1)))
            .noneMatch(
                beregningAktivitet -> opptjeningAktivitetType.equals(beregningAktivitet.getOpptjeningAktivitetType()));
    }

    private static boolean harHøyereRefusjonEnnInntekt(BigDecimal refusjonskravPrÅr, BeregningsgrunnlagPrStatusOgAndelDto andel) {
            return harHøyereRefusjonEnnBeregningsgrunnlag(refusjonskravPrÅr, hentBeløpForAndelSomErGjeldendeForFordeling(andel));
    }

    private static boolean harHøyereRefusjonEnnBeregningsgrunnlag(BigDecimal refusjonskravPrÅr, BigDecimal bruttoPrÅr) {
            return refusjonskravPrÅr.compareTo(bruttoPrÅr) > 0;
    }

    private static boolean harNoenAndelerMedAAP(BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode) {
        return beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .anyMatch(a -> AktivitetStatus.ARBEIDSAVKLARINGSPENGER.equals(a.getAktivitetStatus()));
    }

    public static boolean erNyttArbeidsforhold(BeregningsgrunnlagPrStatusOgAndelDto andel, BeregningAktivitetAggregatDto beregningAktivitetAggregat, LocalDate skjæringstidspunkt) {
        if (!andel.getBgAndelArbeidsforhold().isPresent()) {
            return false;
        }
        BGAndelArbeidsforholdDto arbeidsforhold = andel.getBgAndelArbeidsforhold().get();
        var beregningAktiviteter = beregningAktivitetAggregat.getBeregningAktiviteter();
        return beregningAktiviteter.stream()
            .filter(beregningAktivitet -> !beregningAktivitet.getPeriode().getTomDato().isBefore(skjæringstidspunkt.minusDays(1)))
            .filter(beregningAktivitet -> !beregningAktivitet.getPeriode().getFomDato().isAfter(skjæringstidspunkt.minusDays(1)))
            .noneMatch(
                beregningAktivitet -> matcherArbeidsgiver(arbeidsforhold, beregningAktivitet) && matcherReferanse(arbeidsforhold, beregningAktivitet));
    }

    private static boolean matcherReferanse(BGAndelArbeidsforholdDto arbeidsforhold, BeregningAktivitetDto beregningAktivitet) {
        String andelRef = arbeidsforhold.getArbeidsforholdRef().getReferanse();
        String aktivitetRef = beregningAktivitet.getArbeidsforholdRef().getReferanse();
        return Objects.equals(andelRef, aktivitetRef);
    }

    private static boolean matcherArbeidsgiver(BGAndelArbeidsforholdDto arbeidsforhold, BeregningAktivitetDto beregningAktivitet) {
        return Objects.equals(arbeidsforhold.getArbeidsgiver(), beregningAktivitet.getArbeidsgiver());
    }

    public static BigDecimal hentBeløpForAndelSomErGjeldendeForFordeling(BeregningsgrunnlagPrStatusOgAndelDto andel) {
        BigDecimal overstyrtPrÅr = andel.getOverstyrtPrÅr();
        BigDecimal beregnetPrÅr = andel.getBeregnetPrÅr();
        return overstyrtPrÅr == null ? beregnetPrÅr : overstyrtPrÅr;
    }
}
