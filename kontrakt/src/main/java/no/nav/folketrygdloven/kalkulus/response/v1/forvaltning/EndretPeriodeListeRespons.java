package no.nav.folketrygdloven.kalkulus.response.v1.forvaltning;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.ANY, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public class EndretPeriodeListeRespons {

    @JsonProperty(value = "perioderPrReferanse")
    @Valid
    @Size(max = 500)
    private List<EndretPeriodeRespons> perioderPrReferanse;


    @JsonCreator
    public EndretPeriodeListeRespons(@JsonProperty(value = "perioderPrReferanse") List<EndretPeriodeRespons> perioderPrReferanse) {
        this.perioderPrReferanse = perioderPrReferanse;
    }

    public List<EndretPeriodeRespons> getPerioderPrReferanse() {
        return perioderPrReferanse;
    }
}
