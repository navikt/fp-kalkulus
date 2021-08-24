package no.nav.folketrygdloven.kalkulus.request.v1.migrerAksjonspunkt;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovStatus;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
public class MigrerAksjonspunktListeRequest {

    @JsonProperty(value = "aksjonspunktdata", required = true)
    @Valid
    @NotNull
    private List<MigrerAksjonspunktRequest> aksjonspunktdata;

    @JsonProperty(value = "avklaringsbehovKode", required = true)
    @Valid
    @NotNull
    private String avklaringsbehovKode;

    protected MigrerAksjonspunktListeRequest() {
    }

    @JsonCreator
    public MigrerAksjonspunktListeRequest(@JsonProperty(value = "aksjonspunktdata", required = true) List<MigrerAksjonspunktRequest> aksjonspunktdata,
                                          @JsonProperty(value = "avklaringsbehovKode", required = true) String avklaringsbehovKode) {
        this.aksjonspunktdata = aksjonspunktdata;
        this.avklaringsbehovKode = avklaringsbehovKode;
    }

    public List<MigrerAksjonspunktRequest> getAksjonspunktdata() {
        return aksjonspunktdata;
    }

    public String getAvklaringsbehovKode() {
        return avklaringsbehovKode;
    }
}
