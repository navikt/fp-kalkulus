package no.nav.folketrygdloven.kalkulator.steg.foreslå;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelMerknad;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.output.BeregningAvklaringsbehovResultat;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;

public class AvklaringsbehovUtlederForeslåBeregning {

    private AvklaringsbehovUtlederForeslåBeregning() {
        // Skjul
    }

    public static List<BeregningAvklaringsbehovResultat> utledAvklaringsbehov(@SuppressWarnings("unused") BeregningsgrunnlagInput input,
                                                                             List<RegelResultat> regelResultater) {
        return mapRegelResultater(regelResultater);
    }

    private static List<BeregningAvklaringsbehovResultat> mapRegelResultater(List<RegelResultat> regelResultater) {
        return regelResultater.stream()
            .map(RegelResultat::getMerknader)
            .flatMap(Collection::stream)
            .distinct()
            .map(AvklaringsbehovUtlederForeslåBeregning::mapRegelMerknad)
            .map(BeregningAvklaringsbehovResultat::opprettFor)
            .collect(Collectors.toList());
    }

    private static AvklaringsbehovDefinisjon mapRegelMerknad(RegelMerknad regelMerknad) {
        return AvklaringsbehovDefinisjon.fraKode(regelMerknad.getMerknadKode());
    }
}
