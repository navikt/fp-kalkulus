package no.nav.folketrygdloven.kalkulus.opptjening.v1;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = NON_ABSENT, content = NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class OpptjeningAktiviteterDto {


    @JsonProperty(value = "perioder", required = true)
    @Valid
    @NotNull
    List<OpptjeningPeriodeDto> perioder;

    public OpptjeningAktiviteterDto(@JsonProperty(value = "perioder",required = true) @Valid @NotNull List<OpptjeningPeriodeDto> perioder) {
        this.perioder = perioder;
    }

    public List<OpptjeningPeriodeDto> getPerioder() {
        return perioder;
    }
}
