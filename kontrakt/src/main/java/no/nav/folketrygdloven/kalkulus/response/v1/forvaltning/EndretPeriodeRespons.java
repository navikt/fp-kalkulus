package no.nav.folketrygdloven.kalkulus.response.v1.forvaltning;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import no.nav.folketrygdloven.kalkulus.response.v1.KalkulusRespons;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.ANY, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public class EndretPeriodeRespons implements KalkulusRespons {

    @JsonProperty(value = "eksternReferanse")
    @Valid
    private UUID eksternReferanse;


    @JsonProperty(value = "periodedifferanser")
    @Valid
    @Size(max = 500)
    private List<PeriodeDifferanse> periodedifferanser;


    @JsonCreator
    public EndretPeriodeRespons(@JsonProperty(value = "eksternReferanse") UUID eksternReferanse,
                                @JsonProperty(value = "periodedifferanser") List<PeriodeDifferanse> periodedifferanser) {
        this.eksternReferanse = eksternReferanse;
        this.periodedifferanser = periodedifferanser;
    }

    public List<PeriodeDifferanse> getPeriodedifferanser() {
        return periodedifferanser;
    }

    @Override
    public UUID getEksternReferanse() {
        return eksternReferanse;
    }
}
