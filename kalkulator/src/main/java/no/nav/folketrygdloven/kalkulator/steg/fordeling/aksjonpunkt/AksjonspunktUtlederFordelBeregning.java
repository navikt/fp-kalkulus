package no.nav.folketrygdloven.kalkulator.steg.fordeling.aksjonpunkt;

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
import no.nav.folketrygdloven.kalkulator.output.BeregningAksjonspunktResultat;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningAksjonspunkt;

public class AksjonspunktUtlederFordelBeregning {

    private AksjonspunktUtlederFordelBeregning() {
        // Skjul
    }

    public static List<BeregningAksjonspunktResultat> utledAksjonspunkterFor(KoblingReferanse ref,
                                                                                BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag,
                                                                                YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag,
                                                                                Collection<InntektsmeldingDto> inntektsmeldinger) {
        List<BeregningAksjonspunktResultat> aksjonspunktResultater = new ArrayList<>();
        if (harTilfellerForFordeling(ref, beregningsgrunnlagGrunnlag, ytelsespesifiktGrunnlag, inntektsmeldinger)) {
            BeregningAksjonspunktResultat aksjonspunktResultat = BeregningAksjonspunktResultat.opprettFor(BeregningAksjonspunkt.FORDEL_BEREGNINGSGRUNNLAG);
            aksjonspunktResultater.add(aksjonspunktResultat);
        }
        return aksjonspunktResultater;
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
