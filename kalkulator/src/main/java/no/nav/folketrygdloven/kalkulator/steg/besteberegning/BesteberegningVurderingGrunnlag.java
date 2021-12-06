package no.nav.folketrygdloven.kalkulator.steg.besteberegning;

import java.math.BigDecimal;
import java.util.List;

public class BesteberegningVurderingGrunnlag {

    private final List<BesteberegningMånedGrunnlag> seksBesteMåneder;
    private final BigDecimal avvikFraFørsteLedd;

    public BesteberegningVurderingGrunnlag(List<BesteberegningMånedGrunnlag> seksBesteMåneder,
                                           BigDecimal avvikFraFørsteLedd) {
        this.seksBesteMåneder = seksBesteMåneder;
        this.avvikFraFørsteLedd = avvikFraFørsteLedd;
    }

    public List<BesteberegningMånedGrunnlag> getSeksBesteMåneder() {
        return seksBesteMåneder;
    }

    public BigDecimal getAvvikFraFørsteLedd() {
        return avvikFraFørsteLedd;
    }
}
