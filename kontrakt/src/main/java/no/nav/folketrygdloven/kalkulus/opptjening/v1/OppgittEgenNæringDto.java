package no.nav.folketrygdloven.kalkulus.opptjening.v1;


import java.math.BigDecimal;
import java.time.LocalDate;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.felles.v1.Aktør;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.kodeverk.VirksomhetType;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class OppgittEgenNæringDto implements OppgittPeriodeInntekt {

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

    @JsonProperty(value = "nærRelasjon")
    @Valid
    @NotNull
    private Boolean nærRelasjon;

    @JsonProperty(value = "nyIArbeidslivet")
    @Valid
    @NotNull
    private Boolean nyIArbeidslivet;

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
                                @Valid @NotNull Boolean nærRelasjon,
                                @Valid @NotNull Boolean nyIArbeidslivet,
                                @Valid @NotNull BigDecimal bruttoInntekt) {

        this.periode = periode;
        this.aktør = aktør;
        this.virksomhetType = virksomhetType;
        this.nyoppstartet = nyoppstartet;
        this.varigEndring = varigEndring;
        this.endringDato = endringDato;
        this.nærRelasjon = nærRelasjon;
        this.nyIArbeidslivet = nyIArbeidslivet;
        this.bruttoInntekt = bruttoInntekt;
    }

    public Periode getPeriode() {
        return periode;
    }

    @Override
    public BigDecimal getInntekt() {
        return bruttoInntekt;
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

    public Boolean getNærRelasjon() {
        return nærRelasjon;
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
}
