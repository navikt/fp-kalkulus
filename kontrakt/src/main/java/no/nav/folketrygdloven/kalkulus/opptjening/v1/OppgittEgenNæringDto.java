package no.nav.folketrygdloven.kalkulus.opptjening.v1;


import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.felles.v1.Aktør;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.kodeverk.VirksomhetType;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class OppgittEgenNæringDto {

    @JsonProperty(value = "periode")
    @Valid
    @NotNull
    private Periode periode;

    @JsonProperty(value = "aktør")
    @Valid
    private Aktør aktør;

    @JsonProperty(value = "virksomhetType")
    @Valid
    private VirksomhetType virksomhetType;

    @JsonProperty(value = "nyoppstartet")
    @Valid
    @NotNull
    private Boolean nyoppstartet;

    @JsonProperty(value = "varigEndring")
    @Valid
    @NotNull
    private Boolean varigEndring;

    @JsonProperty(value = "endringDato")
    @Valid
    private LocalDate endringDato;

    @JsonProperty(value = "nyIArbeidslivet")
    @Valid
    @NotNull
    private Boolean nyIArbeidslivet;

    @JsonProperty(value = "begrunnelse")
    @Valid
    @Size(max = 4000)
    @Pattern(regexp = "^[\\p{Graph}\\p{Space}\\p{Sc}\\p{L}\\p{M}\\p{N}§]+$", message = "[${validatedValue}] matcher ikke tillatt pattern [{regexp}]")
    private String begrunnelse;

    @JsonProperty(value = "bruttoInntekt")
    @Valid
    @DecimalMin(value = "0.00", message = "verdien ${validatedValue} må være >= {value}")
    @DecimalMax(value = "1000000000.00", message = "verdien ${validatedValue} må være <= {value}")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal bruttoInntekt;

    public OppgittEgenNæringDto() {
        // default ctor
    }

    public OppgittEgenNæringDto(@Valid @NotNull Periode periode,
                                @Valid Aktør aktør,
                                @Valid VirksomhetType virksomhetType,
                                @Valid @NotNull Boolean nyoppstartet,
                                @Valid @NotNull Boolean varigEndring,
                                @Valid LocalDate endringDato,
                                @Valid @NotNull Boolean nyIArbeidslivet,
                                @Valid String begrunnelse,
                                @Valid @NotNull BigDecimal bruttoInntekt) {

        this.periode = periode;
        this.aktør = aktør;
        this.virksomhetType = virksomhetType;
        this.nyoppstartet = nyoppstartet;
        this.varigEndring = varigEndring;
        this.endringDato = endringDato;
        this.nyIArbeidslivet = nyIArbeidslivet;
        this.begrunnelse = begrunnelse;
        this.bruttoInntekt = bruttoInntekt;
    }

    public Periode getPeriode() {
        return periode;
    }

    public Aktør getAktør() {
        return aktør;
    }

    public VirksomhetType getVirksomhetType() {
        return virksomhetType;
    }

    public Boolean getNyoppstartet() {
        return nyoppstartet;
    }

    public Boolean getVarigEndring() {
        return varigEndring;
    }

    public Boolean getNyIArbeidslivet() {
        return nyIArbeidslivet;
    }

    public BigDecimal getBruttoInntekt() {
        return bruttoInntekt;
    }

    public LocalDate getEndringDato() {
        return endringDato;
    }

    public String getBegrunnelse() {
        return begrunnelse;
    }
}
