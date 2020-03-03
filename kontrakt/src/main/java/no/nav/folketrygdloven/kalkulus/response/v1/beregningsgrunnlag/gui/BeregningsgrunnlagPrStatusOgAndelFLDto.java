package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui;

public class BeregningsgrunnlagPrStatusOgAndelFLDto extends BeregningsgrunnlagPrStatusOgAndelDto {
    private Boolean erNyoppstartet;

    public BeregningsgrunnlagPrStatusOgAndelFLDto() {
        super();
        // trengs for deserialisering av JSON
    }

    public Boolean getErNyoppstartet() {
        return erNyoppstartet;
    }

    public void setErNyoppstartet(Boolean erNyoppstartet) {
        this.erNyoppstartet = erNyoppstartet;
    }

}
