package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.frisinn;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import java.math.BigDecimal;
import java.time.LocalDate;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;

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
    @JsonProperty("oppgittInntekt")
    @Digits(integer = 8, fraction = 2)
    @DecimalMin("0.00")
    @DecimalMax("10000000.00")
    private BigDecimal oppgittInntekt;

    @Valid
    @JsonProperty("statusSøktFor")
    @NotNull
    private AktivitetStatus statusSøktFor;

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

    public BigDecimal getOppgittInntekt() {
        return oppgittInntekt;
    }

    public void setOppgittInntekt(BigDecimal oppgittInntekt) {
        this.oppgittInntekt = oppgittInntekt;
    }

    public AktivitetStatus getStatusSøktFor() {
        return statusSøktFor;
    }

    public void setStatusSøktFor(AktivitetStatus statusSøktFor) {
        this.statusSøktFor = statusSøktFor;
    }
}
