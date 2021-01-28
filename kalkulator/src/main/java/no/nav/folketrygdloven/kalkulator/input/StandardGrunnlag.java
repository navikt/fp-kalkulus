package no.nav.folketrygdloven.kalkulator.input;

import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;

public class StandardGrunnlag implements YtelsespesifiktGrunnlag {

    private final int dekningsgrad = 100;

    private Integer grunnbeløpMilitærHarKravPå = 2;

    public StandardGrunnlag() {
    }

    @Override
    public int getDekningsgrad() {
        return dekningsgrad;
    }

    @Override
    public int getGrunnbeløpMilitærHarKravPå() {
        return grunnbeløpMilitærHarKravPå;
    }

    @Override
    public void setGrunnbeløpMilitærHarKravPå(int grunnbeløpMilitærHarKravPå) {
        this.grunnbeløpMilitærHarKravPå = grunnbeløpMilitærHarKravPå;
    }
}
