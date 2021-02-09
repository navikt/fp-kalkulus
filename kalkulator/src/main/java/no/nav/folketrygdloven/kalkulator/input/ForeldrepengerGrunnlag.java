package no.nav.folketrygdloven.kalkulator.input;

import no.nav.folketrygdloven.kalkulator.steg.besteberegning.BesteberegningVurderingGrunnlag;

public class ForeldrepengerGrunnlag implements YtelsespesifiktGrunnlag {

    private int dekningsgrad = 100;

    private boolean kvalifisererTilBesteberegning = false;

    private BesteberegningVurderingGrunnlag besteberegningVurderingGrunnlag;

    private Integer grunnbeløpMilitærHarKravPå;


    ForeldrepengerGrunnlag() {
    }

    public ForeldrepengerGrunnlag(int dekningsgrad, boolean kvalifisererTilBesteberegning) {
        this.dekningsgrad = dekningsgrad;
        this.kvalifisererTilBesteberegning = kvalifisererTilBesteberegning;
    }

    public ForeldrepengerGrunnlag(int dekningsgrad, BesteberegningVurderingGrunnlag besteberegningVurderingGrunnlag) {
        this.dekningsgrad = dekningsgrad;
        this.kvalifisererTilBesteberegning = besteberegningVurderingGrunnlag != null;
        this.besteberegningVurderingGrunnlag = besteberegningVurderingGrunnlag;
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

    public BesteberegningVurderingGrunnlag getBesteberegningVurderingGrunnlag() {
        return besteberegningVurderingGrunnlag;
    }

}
