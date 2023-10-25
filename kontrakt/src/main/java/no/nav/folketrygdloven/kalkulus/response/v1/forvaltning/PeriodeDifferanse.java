package no.nav.folketrygdloven.kalkulus.response.v1.forvaltning;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.ANY, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public class PeriodeDifferanse {

    @JsonProperty(value = "periode")
    @NotNull
    @Valid
    private Periode periode;

    @JsonProperty(value = "andeldifferanser")
    @Valid
    @Size(max = 20)
    private List<AndelDifferanse> andeldifferanser;

    @JsonCreator
    public PeriodeDifferanse(@JsonProperty(value = "periode") Periode periode, @JsonProperty(value = "andeldifferanser") List<AndelDifferanse> andeldifferanser) {
        this.periode = periode;
        this.andeldifferanser = andeldifferanser;
    }

    public Periode getPeriode() {
        return periode;
    }

    public List<AndelDifferanse> getAndeldifferanser() {
        return andeldifferanser;
    }
}
