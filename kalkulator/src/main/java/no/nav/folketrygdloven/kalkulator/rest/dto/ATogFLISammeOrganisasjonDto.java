package no.nav.folketrygdloven.kalkulator.rest.dto;

import java.math.BigDecimal;

public class ATogFLISammeOrganisasjonDto extends FaktaOmBeregningAndelDto {

    private BigDecimal inntektPrMnd;

    public BigDecimal getInntektPrMnd() {
        return inntektPrMnd;
    }

    public void setInntektPrMnd(BigDecimal inntektPrMnd) {
        this.inntektPrMnd = inntektPrMnd;
    }
}
