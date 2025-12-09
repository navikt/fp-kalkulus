package no.nav.foreldrepenger.kalkulus.request.input.iay.oppgitt;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import no.nav.foreldrepenger.kalkulus.typer.Beløp;
import no.nav.foreldrepenger.kalkulus.typer.Periode;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class OppgittFrilansInntekt {

    @JsonProperty("periode")
    @NotNull
    @Valid
    private Periode periode;

    @JsonProperty("inntekt")
    @NotNull
    @Valid
    private Beløp inntekt;

    public OppgittFrilansInntekt() {
        // Json deserilaisering
    }

    public OppgittFrilansInntekt(@NotNull Periode periode, @NotNull Beløp inntekt) {
        this.periode = periode;
        this.inntekt = inntekt;
    }

    public Periode getPeriode() {
        return periode;
    }

    public Beløp getInntekt() {
        return inntekt;
    }
}
