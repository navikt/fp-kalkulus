package no.nav.folketrygdloven.kalkulator.aksjonspunkt.dto;

public class VurderLønnsendringDto {

    private Boolean erLønnsendringIBeregningsperioden;

    public VurderLønnsendringDto(Boolean erLønnsendringIBeregningsperioden) { // NOSONAR
        this.erLønnsendringIBeregningsperioden = erLønnsendringIBeregningsperioden;
    }

    public Boolean erLønnsendringIBeregningsperioden() {
        return erLønnsendringIBeregningsperioden;
    }

    public void setErLønnsendringIBeregningsperioden(Boolean erLønnsendringIBeregningsperioden) {
        this.erLønnsendringIBeregningsperioden = erLønnsendringIBeregningsperioden;
    }
}
