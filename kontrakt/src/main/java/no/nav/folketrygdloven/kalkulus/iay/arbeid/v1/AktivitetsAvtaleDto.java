package no.nav.folketrygdloven.kalkulus.iay.arbeid.v1;

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
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class AktivitetsAvtaleDto {

    @JsonProperty("periode")
    @Valid
    @NotNull
    private Periode periode;

    @JsonProperty("sisteLønnsendringsdato")
    @Valid
    private LocalDate sisteLønnsendringsdato;


    /**
     * Det går an å ha stillingprosent mer enn 100%, men innsendingsfeil hos LPS leverandører og manglende Altinn validering
     * gjør at i noen historiske tilfeller har man akseptert innsending opp til 500% (typisk skjedd når man har tastet inn ett ukesverks antall
     * timer i dag-felt i de systemene).
     */
    @JsonProperty("stillingsprosent")
    @Valid
    @DecimalMin(value = "0.00", message = "verdien ${validatedValue} må være >= {value}")
    @DecimalMax(value = "1000.00", message = "verdien ${validatedValue} må være <= {value}")
    @Digits(integer = 3, fraction = 2)
    private BigDecimal stillingsprosent;

    protected AktivitetsAvtaleDto() {
        // default ctor
    }

    public AktivitetsAvtaleDto(@Valid @NotNull Periode periode,
                               @Valid LocalDate sisteLønnsendringsdato,
                               @Valid @DecimalMin(value = "0.00", message = "verdien ${validatedValue} må være >= {value}") @DecimalMax(value = "1000.00", message = "verdien ${validatedValue} må være <= {value}") BigDecimal stillingsprosent) {
        this.periode = periode;
        this.sisteLønnsendringsdato = sisteLønnsendringsdato;
        this.stillingsprosent = stillingsprosent;
    }

    public Periode getPeriode() {
        return periode;
    }

    public BigDecimal getStillingsprosent() {
        return stillingsprosent;
    }

    public LocalDate getSisteLønnsendringsdato() {
        return sisteLønnsendringsdato;
    }
}
