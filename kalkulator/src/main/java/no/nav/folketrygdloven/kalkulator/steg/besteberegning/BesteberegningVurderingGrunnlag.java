package no.nav.folketrygdloven.kalkulator.steg.besteberegning;

import java.util.List;

public class BesteberegningVurderingGrunnlag {

    private final List<BesteberegningMånedGrunnlag> seksBesteMåneder;

    public BesteberegningVurderingGrunnlag(List<BesteberegningMånedGrunnlag> seksBesteMåneder) {
        this.seksBesteMåneder = seksBesteMåneder;
    }

    public List<BesteberegningMånedGrunnlag> getSeksBesteMåneder() {
        return seksBesteMåneder;
    }
}
