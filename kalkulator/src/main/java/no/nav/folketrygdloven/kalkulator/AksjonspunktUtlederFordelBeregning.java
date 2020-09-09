package no.nav.folketrygdloven.kalkulator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import no.nav.folketrygdloven.kalkulator.fordeling.FordelBeregningsgrunnlagTilfelleInput;
import no.nav.folketrygdloven.kalkulator.fordeling.FordelBeregningsgrunnlagTilfelleTjeneste;
import no.nav.folketrygdloven.kalkulator.gradering.AktivitetGradering;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningAksjonspunktDefinisjon;
import no.nav.folketrygdloven.kalkulator.output.BeregningAksjonspunktResultat;

public class AksjonspunktUtlederFordelBeregning {

    private AksjonspunktUtlederFordelBeregning() {
        // Skjul
    }

    protected static List<BeregningAksjonspunktResultat> utledAksjonspunkterFor(KoblingReferanse ref,
                                                                                BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag,
                                                                                AktivitetGradering aktivitetGradering,
                                                                                Collection<InntektsmeldingDto> inntektsmeldinger) {
        List<BeregningAksjonspunktResultat> aksjonspunktResultater = new ArrayList<>();
        if (harTilfellerForFordeling(ref, beregningsgrunnlagGrunnlag, aktivitetGradering, inntektsmeldinger)) {
            BeregningAksjonspunktResultat aksjonspunktResultat = BeregningAksjonspunktResultat.opprettFor(BeregningAksjonspunktDefinisjon.FORDEL_BEREGNINGSGRUNNLAG);
            aksjonspunktResultater.add(aksjonspunktResultat);
        }
        return aksjonspunktResultater;
    }

    private static boolean harTilfellerForFordeling(@SuppressWarnings("unused") KoblingReferanse ref,
                                                    BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag,
                                                    AktivitetGradering aktivitetGradering,
                                                    Collection<InntektsmeldingDto> inntektsmeldinger) {
        BeregningsgrunnlagDto beregningsgrunnlag = beregningsgrunnlagGrunnlag.getBeregningsgrunnlag()
            .orElseThrow(() -> new IllegalStateException("Utviklerfeil: Mangler beregningsgrunnlagGrunnlag"));
        FordelBeregningsgrunnlagTilfelleInput fordelingInput = new FordelBeregningsgrunnlagTilfelleInput(beregningsgrunnlag, beregningsgrunnlagGrunnlag.getGjeldendeAktiviteter(), aktivitetGradering, inntektsmeldinger);
        return FordelBeregningsgrunnlagTilfelleTjeneste.harTilfelleForFordeling(fordelingInput);
    }
}
