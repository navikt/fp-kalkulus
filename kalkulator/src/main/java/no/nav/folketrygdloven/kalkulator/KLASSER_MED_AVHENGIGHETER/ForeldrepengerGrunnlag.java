package no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER;

import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;

public class ForeldrepengerGrunnlag implements YtelsespesifiktGrunnlag {

    private int dekningsgrad = 100;

    private boolean kvalifisererTilBesteberegning = false;

    private Integer grunnbeløpMilitærHarKravPå;


    ForeldrepengerGrunnlag() {
    }

    public ForeldrepengerGrunnlag(int dekningsgrad, boolean kvalifisererTilBesteberegning) {
        this.dekningsgrad = dekningsgrad;
        this.kvalifisererTilBesteberegning = kvalifisererTilBesteberegning;
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

    public boolean isKvalifisererTilBesteberegning() {
        return kvalifisererTilBesteberegning;
    }
}
