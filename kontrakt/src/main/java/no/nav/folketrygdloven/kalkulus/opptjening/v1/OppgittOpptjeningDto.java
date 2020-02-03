package no.nav.folketrygdloven.kalkulus.opptjening.v1;


import java.util.List;

import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class OppgittOpptjeningDto {

    @JsonProperty(value = "frilans")
    @Valid
    private OppgittFrilansDto frilans;

    @JsonProperty(value = "egenNæring")
    @Valid
    private List<OppgittEgenNæringDto> egenNæring;


    public OppgittOpptjeningDto(@Valid OppgittFrilansDto frilans, @Valid List<OppgittEgenNæringDto> egenNæring) {
        this.frilans = frilans;
        this.egenNæring = egenNæring;
    }

    public OppgittFrilansDto getFrilans() {
        return frilans;
    }

    public List<OppgittEgenNæringDto> getEgenNæring() {
        return egenNæring;
    }
}
