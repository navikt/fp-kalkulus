package no.nav.folketrygdloven.kalkulus.opptjening.v1;

import java.math.BigDecimal;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class OppgittFrilansInntekt implements OppgittPeriodeInntekt {

    @JsonProperty("periode")
    @NotNull
    private Periode periode;

    @JsonProperty("inntekt")
    @NotNull
    private BigDecimal inntekt;

    public OppgittFrilansInntekt() {
        // Json deserilaisering
    }

    public OppgittFrilansInntekt(@NotNull Periode periode, @NotNull BigDecimal inntekt) {
        this.periode = periode;
        this.inntekt = inntekt;
    }

    @Override
    public Periode getPeriode() {
        return periode;
    }

    @Override
    public BigDecimal getInntekt() {
        return inntekt;
    }
}
