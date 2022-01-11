package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.frisinn;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import java.math.BigDecimal;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = NON_ABSENT, content = NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class FrisinnAndelDto {

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

    public FrisinnAndelDto() {
        // Jackson
    }

    public FrisinnAndelDto(@Valid @Digits(integer = 8, fraction = 2) @DecimalMin("0.00") @DecimalMax("10000000.00") BigDecimal oppgittInntekt,
                           @Valid @NotNull AktivitetStatus statusSøktFor) {
        this.oppgittInntekt = oppgittInntekt;
        this.statusSøktFor = statusSøktFor;
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
