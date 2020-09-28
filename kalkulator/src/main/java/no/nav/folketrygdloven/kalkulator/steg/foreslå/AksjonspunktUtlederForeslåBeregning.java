package no.nav.folketrygdloven.kalkulator.steg.foreslå;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelMerknad;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningAksjonspunktDefinisjon;
import no.nav.folketrygdloven.kalkulator.output.BeregningAksjonspunktResultat;

public class AksjonspunktUtlederForeslåBeregning {

    private AksjonspunktUtlederForeslåBeregning() {
        // Skjul
    }

    public static List<BeregningAksjonspunktResultat> utledAksjonspunkter(@SuppressWarnings("unused") BeregningsgrunnlagInput input,
                                                                             List<RegelResultat> regelResultater) {
        return mapRegelResultater(regelResultater);
    }

    private static List<BeregningAksjonspunktResultat> mapRegelResultater(List<RegelResultat> regelResultater) {
        return regelResultater.stream()
            .map(RegelResultat::getMerknader)
            .flatMap(Collection::stream)
            .distinct()
            .map(AksjonspunktUtlederForeslåBeregning::mapRegelMerknad)
            .map(BeregningAksjonspunktResultat::opprettFor)
            .collect(Collectors.toList());
    }

    private static BeregningAksjonspunktDefinisjon mapRegelMerknad(RegelMerknad regelMerknad) {
        return BeregningAksjonspunktDefinisjon.fraKode(regelMerknad.getMerknadKode());
    }
}
