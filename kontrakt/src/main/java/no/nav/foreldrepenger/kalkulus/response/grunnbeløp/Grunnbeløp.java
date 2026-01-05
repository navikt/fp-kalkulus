package no.nav.foreldrepenger.kalkulus.response.grunnbeløp;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import no.nav.foreldrepenger.kalkulus.typer.Beløp;
import no.nav.foreldrepenger.kalkulus.typer.Periode;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class Grunnbeløp {

    @Valid
    @JsonProperty(value = "verdi")
    private Beløp verdi;

    @JsonProperty(value = "periode")
    @NotNull
    @Valid
    private Periode periode;

    public Grunnbeløp() {
    }

    public Grunnbeløp(@Valid Beløp verdi,
                      @NotNull @Valid Periode periode) {
        this.verdi = verdi;
        this.periode = periode;
    }


    public Beløp getVerdi() {
        return verdi;
    }

    public Periode getPeriode() {
        return periode;
    }
}
