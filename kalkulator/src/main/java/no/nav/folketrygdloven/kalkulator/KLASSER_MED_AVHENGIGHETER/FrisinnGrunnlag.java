package no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER;

import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;

public class FrisinnGrunnlag implements YtelsespesifiktGrunnlag {

    private final int dekningsgrad = 100;

    private Integer grunnbeløpMilitærHarKravPå = 2;

    public FrisinnGrunnlag() {
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
