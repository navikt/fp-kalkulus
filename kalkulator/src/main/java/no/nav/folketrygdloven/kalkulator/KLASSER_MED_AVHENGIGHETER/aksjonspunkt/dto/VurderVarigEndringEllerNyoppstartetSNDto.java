package no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto;

public class VurderVarigEndringEllerNyoppstartetSNDto {

    private boolean erVarigEndretNaering;

    private Integer bruttoBeregningsgrunnlag;

    public VurderVarigEndringEllerNyoppstartetSNDto(boolean erVarigEndretNaering, Integer bruttoBeregningsgrunnlag) {
        this.erVarigEndretNaering = erVarigEndretNaering;
        this.bruttoBeregningsgrunnlag = bruttoBeregningsgrunnlag;
    }

    public boolean getErVarigEndretNaering() {
        return erVarigEndretNaering;
    }

    public Integer getBruttoBeregningsgrunnlag() {
        return bruttoBeregningsgrunnlag;
    }
}
