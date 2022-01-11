package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.frisinn;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = NON_ABSENT, content = NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class FrisinnPeriodeDto {

    @Valid
    @NotNull
    @JsonProperty("fom")
    private LocalDate fom;

    @Valid
    @JsonProperty("tom")
    @NotNull
    private LocalDate tom;

    @Valid
    @JsonProperty("oppgittArbeidsinntekt")
    @Digits(integer = 8, fraction = 2)
    @DecimalMin("0.00")
    @DecimalMax("10000000.00")
    private BigDecimal oppgittArbeidsinntekt;

    @Valid
    @JsonProperty("frisinnAndeler")
    @NotNull
    @Size(min = 1)
    private List<FrisinnAndelDto> frisinnAndeler;

    public LocalDate getFom() {
        return fom;
    }

    public void setFom(LocalDate fom) {
        this.fom = fom;
    }

    public LocalDate getTom() {
        return tom;
    }

    public void setTom(LocalDate tom) {
        this.tom = tom;
    }

    public BigDecimal getOppgittArbeidsinntekt() {
        return oppgittArbeidsinntekt;
    }

    public void setOppgittArbeidsinntekt(BigDecimal oppgittArbeidsinntekt) {
        this.oppgittArbeidsinntekt = oppgittArbeidsinntekt;
    }

    public List<FrisinnAndelDto> getFrisinnAndeler() {
        return frisinnAndeler;
    }

    public void setFrisinnAndeler(List<FrisinnAndelDto> frisinnAndeler) {
        this.frisinnAndeler = frisinnAndeler;
    }
}
