package no.nav.foreldrepenger.kalkulus.request.input.iay.oppgitt;

import java.time.LocalDate;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.annoteringer.Fritekst;
import no.nav.folketrygdloven.kalkulus.kodeverk.VirksomhetType;
import no.nav.foreldrepenger.kalkulus.typer.Aktør;
import no.nav.foreldrepenger.kalkulus.typer.Beløp;
import no.nav.foreldrepenger.kalkulus.typer.Periode;

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
    @Fritekst
    private String begrunnelse;

    @JsonProperty(value = "bruttoInntekt")
    @Valid
    private Beløp bruttoInntekt;

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
                                @Valid Beløp bruttoInntekt) {

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

    public Beløp getBruttoInntekt() {
        return bruttoInntekt;
    }

    public LocalDate getEndringDato() {
        return endringDato;
    }

    public String getBegrunnelse() {
        return begrunnelse;
    }
}
