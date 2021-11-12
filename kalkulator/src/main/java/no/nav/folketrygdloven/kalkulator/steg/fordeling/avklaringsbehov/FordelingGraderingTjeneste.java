package no.nav.folketrygdloven.kalkulator.steg.fordeling.avklaringsbehov;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.gradering.AktivitetGradering;
import no.nav.folketrygdloven.kalkulator.modell.gradering.AndelGradering;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;

public final class FordelingGraderingTjeneste {
    private static final List<AktivitetStatus> STATUSER_PRIORITERT_OVER_SN = Arrays.asList(AktivitetStatus.ARBEIDSTAKER,
            AktivitetStatus.FRILANSER,
            AktivitetStatus.DAGPENGER,
            AktivitetStatus.ARBEIDSAVKLARINGSPENGER);
    private FordelingGraderingTjeneste() {
        // SKjuler default
    }

    public static List<AndelGradering.Gradering> hentGraderingerForAndel(BeregningsgrunnlagPrStatusOgAndelDto andel, AktivitetGradering aktivitetGradering) {
        Optional<AndelGradering> graderingOpt = finnGraderingForAndel(andel, aktivitetGradering);
        if (graderingOpt.isPresent()) {
            AndelGradering gradering = graderingOpt.get();
            return gradering.getGraderinger();
        }
        return Collections.emptyList();
    }

    public static boolean harGraderingForAndelIPeriode(BeregningsgrunnlagPrStatusOgAndelDto andel, AktivitetGradering aktivitetGradering, Intervall periode) {
        return !hentGraderingerForAndelIPeriode(andel, aktivitetGradering, periode).isEmpty();
    }

    public static List<AndelGradering.Gradering> hentGraderingerForAndelIPeriode(BeregningsgrunnlagPrStatusOgAndelDto andel, AktivitetGradering aktivitetGradering, Intervall periode) {
        Optional<AndelGradering> graderingOpt = finnGraderingForAndel(andel, aktivitetGradering);
        if (graderingOpt.isPresent()) {
            AndelGradering andelGradering = graderingOpt.get();
            return andelGradering.getGraderinger().stream()
                    .filter(gradering -> gradering.getPeriode().overlapper(periode))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private static Optional<AndelGradering> finnGraderingForAndel(BeregningsgrunnlagPrStatusOgAndelDto andel, AktivitetGradering aktivitetGradering) {
        if (aktivitetGradering == null) {
            return Optional.empty();
        }
        return aktivitetGradering.getAndelGradering().stream()
                .filter(andelGradering -> andelGradering.matcher(andel))
                .findFirst();
    }

    public static boolean skalGraderePåAndelUtenBeregningsgrunnlag(BeregningsgrunnlagPrStatusOgAndelDto andel, boolean harGraderingIBGPeriode) {
        boolean harIkkjeBeregningsgrunnlag = andel.getBruttoPrÅr() == null || andel.getBruttoPrÅr().compareTo(BigDecimal.ZERO) == 0;
        return harGraderingIBGPeriode && harIkkjeBeregningsgrunnlag;
    }

    public static boolean gradertAndelVilleBlittAvkortet(BeregningsgrunnlagPrStatusOgAndelDto andel, Beløp grunnbeløp, BeregningsgrunnlagPeriodeDto periode) {
        if (andel.getAktivitetStatus().erSelvstendigNæringsdrivende()) {
            BigDecimal totaltBgFraStatuserPrioritertOverSN = inntektFraAndelerMedStatus(periode, STATUSER_PRIORITERT_OVER_SN);
            BigDecimal seksG = grunnbeløp.getVerdi().multiply(BigDecimal.valueOf(6));
            return totaltBgFraStatuserPrioritertOverSN.compareTo(seksG) >= 0;
        }
        if (andel.getAktivitetStatus().erFrilanser()) {
            BigDecimal totaltBgFraArbeidstaker = inntektFraAndelerMedStatus(periode, Collections.singletonList(AktivitetStatus.ARBEIDSTAKER));
            BigDecimal seksG = grunnbeløp.getVerdi().multiply(BigDecimal.valueOf(6));
            return totaltBgFraArbeidstaker.compareTo(seksG) >= 0;
        }
        return false;
    }

    private static BigDecimal inntektFraAndelerMedStatus(BeregningsgrunnlagPeriodeDto periode, List<AktivitetStatus> statuserSomSkalTelles) {
        return periode.getBeregningsgrunnlagPrStatusOgAndelList()
                .stream()
                .filter(a -> statuserSomSkalTelles.contains(a.getAktivitetStatus()))
                .map(BeregningsgrunnlagPrStatusOgAndelDto::getBruttoPrÅr)
                .filter(Objects::nonNull)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
    }

}
