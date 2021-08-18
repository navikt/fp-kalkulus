package no.nav.folketrygdloven.kalkulator.steg.fordeling.avklaringsbehov;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import no.nav.folketrygdloven.kalkulator.input.ForeldrepengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.gradering.AktivitetGradering;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningAvklaringsbehovResultat;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;

public class AvklaringsbehovUtlederFordelBeregning {

    private AvklaringsbehovUtlederFordelBeregning() {
        // Skjul
    }

    public static List<BeregningAvklaringsbehovResultat> utledAvklaringsbehovFor(KoblingReferanse ref,
                                                                                BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag,
                                                                                YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag,
                                                                                Collection<InntektsmeldingDto> inntektsmeldinger) {
        List<BeregningAvklaringsbehovResultat> avklaringsbehovResultater = new ArrayList<>();
        if (harTilfellerForFordeling(ref, beregningsgrunnlagGrunnlag, ytelsespesifiktGrunnlag, inntektsmeldinger)) {
            BeregningAvklaringsbehovResultat avklaringsbehovResultat = BeregningAvklaringsbehovResultat.opprettFor(AvklaringsbehovDefinisjon.FORDEL_BEREGNINGSGRUNNLAG);
            avklaringsbehovResultater.add(avklaringsbehovResultat);
        }
        return avklaringsbehovResultater;
    }

    private static boolean harTilfellerForFordeling(@SuppressWarnings("unused") KoblingReferanse ref,
                                                    BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag,
                                                    YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag,
                                                    Collection<InntektsmeldingDto> inntektsmeldinger) {
        BeregningsgrunnlagDto beregningsgrunnlag = beregningsgrunnlagGrunnlag.getBeregningsgrunnlag()
            .orElseThrow(() -> new IllegalStateException("Utviklerfeil: Mangler beregningsgrunnlagGrunnlag"));
        FordelBeregningsgrunnlagTilfelleInput fordelingInput = new FordelBeregningsgrunnlagTilfelleInput(beregningsgrunnlag, finnGraderinger(ytelsespesifiktGrunnlag), inntektsmeldinger);
        return FordelBeregningsgrunnlagTilfelleTjeneste.harTilfelleForFordeling(fordelingInput);
    }

    private static AktivitetGradering finnGraderinger(YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {
        return ytelsespesifiktGrunnlag instanceof ForeldrepengerGrunnlag ? ((ForeldrepengerGrunnlag) ytelsespesifiktGrunnlag).getAktivitetGradering() : AktivitetGradering.INGEN_GRADERING;
    }
}
