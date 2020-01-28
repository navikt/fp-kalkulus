package no.nav.folketrygdloven.kalkulator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.gradering.AktivitetGradering;
import no.nav.folketrygdloven.kalkulator.kontrollerfakta.FordelBeregningsgrunnlagTjeneste;
import no.nav.folketrygdloven.kalkulator.kontrollerfakta.FordelBeregningsgrunnlagTjeneste.VurderManuellBehandling;
import no.nav.folketrygdloven.kalkulator.modell.behandling.BehandlingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningAksjonspunktDefinisjon;
import no.nav.folketrygdloven.kalkulator.output.BeregningAksjonspunktResultat;

public class AksjonspunktUtlederFordelBeregning {

    private AksjonspunktUtlederFordelBeregning() {
        // Skjul
    }

    protected static List<BeregningAksjonspunktResultat> utledAksjonspunkterFor(BehandlingReferanse ref,
                                                                         BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag,
                                                                         AktivitetGradering aktivitetGradering,
                                                                         Collection<InntektsmeldingDto> inntektsmeldinger) {
        List<BeregningAksjonspunktResultat> aksjonspunktResultater = new ArrayList<>();
        if (utledAksjonspunktFordelBG(ref, beregningsgrunnlagGrunnlag, aktivitetGradering, inntektsmeldinger).isPresent()) {
            BeregningAksjonspunktResultat aksjonspunktResultat = BeregningAksjonspunktResultat.opprettFor(BeregningAksjonspunktDefinisjon.FORDEL_BEREGNINGSGRUNNLAG);
            aksjonspunktResultater.add(aksjonspunktResultat);
        }
        return aksjonspunktResultater;
    }

    private static Optional<VurderManuellBehandling> utledAksjonspunktFordelBG(@SuppressWarnings("unused") BehandlingReferanse ref,
                                                                        BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag,
                                                                        AktivitetGradering aktivitetGradering,
                                                                        Collection<InntektsmeldingDto> inntektsmeldinger) {
        BeregningsgrunnlagDto beregningsgrunnlag = beregningsgrunnlagGrunnlag.getBeregningsgrunnlag()
            .orElseThrow(() -> new IllegalStateException("Utviklerfeil: Mangler beregningsgrunnlagGrunnlag"));
        return FordelBeregningsgrunnlagTjeneste.vurderManuellBehandling(beregningsgrunnlag, beregningsgrunnlagGrunnlag.getGjeldendeAktiviteter(), aktivitetGradering, inntektsmeldinger);
    }
}
