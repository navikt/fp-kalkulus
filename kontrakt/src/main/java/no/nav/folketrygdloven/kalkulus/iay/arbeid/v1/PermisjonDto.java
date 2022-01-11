package no.nav.folketrygdloven.kalkulus.iay.arbeid.v1;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

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

import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.kodeverk.PermisjonsbeskrivelseType;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class PermisjonDto {

    @JsonProperty("periode")
    @Valid
    @NotNull
    private Periode periode;

    /**
     * Prosent sats med to desimaler - min 0.00 - 100.00.
     * Pga inntastingfeil og manglende validering i LPS systemer og Altinn har man historisk akseptert mottatt permisjonsprosenter langt over
     * 100%. C'est la vie.
     */
    @JsonProperty("prosentsats")
    @Valid
    @DecimalMin(value = "0.00", message = "permisjon prosentsats [${validatedValue}] må være >= {value}")
    @DecimalMax(value = "500.00", message = "permisjon prosentsats [${validatedValue}] må være <= {value}") // insane maks verdi, men Aa-reg sier så
    @Digits(integer = 3, fraction = 2)
    private BigDecimal prosentsats;

    @Valid
    @JsonProperty(value = "permisjonsbeskrivelseType")
    private PermisjonsbeskrivelseType permisjonsbeskrivelseType;

    PermisjonDto(){
        // Skjul default constructor
    }

    public PermisjonDto(@Valid @NotNull Periode periode,
                        @Valid BigDecimal prosentsats,
                        @Valid PermisjonsbeskrivelseType permisjonsbeskrivelseType) {
        this.periode = periode;
        this.prosentsats = prosentsats;
        this.permisjonsbeskrivelseType = permisjonsbeskrivelseType;
    }

    public Periode getPeriode() {
        return periode;
    }

    public BigDecimal getProsentsats() {
        return prosentsats;
    }

    public PermisjonsbeskrivelseType getPermisjonsbeskrivelseType() {
        return permisjonsbeskrivelseType;
    }

}
