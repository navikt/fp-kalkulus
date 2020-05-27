package no.nav.folketrygdloven.kalkulator.fordeling;

import no.nav.folketrygdloven.kalkulator.BeregningInntektsmeldingTjeneste;
import no.nav.folketrygdloven.kalkulator.gradering.AktivitetGradering;
import no.nav.folketrygdloven.kalkulator.gradering.AndelGradering;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public final class FordelingGraderingTjeneste {

    private FordelingGraderingTjeneste() {
        // SKjuler default
    }

    public static List<AndelGradering.Gradering> hentGraderingerForAndel(BeregningsgrunnlagPrStatusOgAndelDto andel, AktivitetGradering aktivitetGradering) {
        Optional<AndelGradering> graderingOpt = BeregningInntektsmeldingTjeneste.finnGraderingForAndel(andel, aktivitetGradering);
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
        Optional<AndelGradering> graderingOpt = BeregningInntektsmeldingTjeneste.finnGraderingForAndel(andel, aktivitetGradering);
        if (graderingOpt.isPresent()) {
            AndelGradering andelGradering = graderingOpt.get();
            return andelGradering.getGraderinger().stream()
                    .filter(gradering -> gradering.getPeriode().overlapper(periode))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    public static boolean skalGraderePåAndelUtenBeregningsgrunnlag(BeregningsgrunnlagPrStatusOgAndelDto andel, boolean harGraderingIBGPeriode) {
        boolean harIkkjeBeregningsgrunnlag = andel.getBruttoPrÅr() == null || andel.getBruttoPrÅr().compareTo(BigDecimal.ZERO) == 0;
        return harGraderingIBGPeriode && harIkkjeBeregningsgrunnlag;
    }

    public static boolean gradertAndelVilleBlittAvkortet(BeregningsgrunnlagPrStatusOgAndelDto andel, Beløp grunnbeløp, BeregningsgrunnlagPeriodeDto periode) {
        if (erStatusSomAvkortesVedATOver6G(andel)) {
            BigDecimal totaltBgFraArbeidstaker = periode.getBeregningsgrunnlagPrStatusOgAndelList()
                    .stream()
                    .filter(a -> a.getAktivitetStatus().erArbeidstaker())
                    .map(BeregningsgrunnlagPrStatusOgAndelDto::getBruttoPrÅr)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal::add)
                    .orElse(BigDecimal.ZERO);
            BigDecimal seksG = grunnbeløp.getVerdi().multiply(BigDecimal.valueOf(6));
            return totaltBgFraArbeidstaker.compareTo(seksG) >= 0;
        }
        return false;
    }

    private static boolean erStatusSomAvkortesVedATOver6G(BeregningsgrunnlagPrStatusOgAndelDto andel) {
        AktivitetStatus aktivitetStatus = andel.getAktivitetStatus();
        return !aktivitetStatus.erArbeidstaker();
    }

}
