package no.nav.folketrygdloven.kalkulus.iay.ytelse.v1;

import java.math.BigDecimal;

import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.felles.v1.BeløpDto;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.ALWAYS, content = Include.ALWAYS)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class YtelseAnvistDto {

    @JsonProperty("anvistPeriode")
    @Valid
    private Periode anvistPeriode;

    @JsonProperty("beløp")
    @Valid
    private BeløpDto beløp;

    @JsonProperty("dagsats")
    @Valid
    private BeløpDto dagsats;

    @JsonProperty("utbetalingsgradProsent")
    @Valid
    private BigDecimal utbetalingsgradProsent;

    public Periode getAnvistPeriode() {
        return anvistPeriode;
    }

    public BeløpDto getBeløp() {
        return beløp;
    }

    public BeløpDto getDagsats() {
        return dagsats;
    }

    public BigDecimal getUtbetalingsgradProsent() {
        return utbetalingsgradProsent;
    }
}
