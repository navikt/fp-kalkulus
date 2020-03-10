package no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = NON_ABSENT, content = NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class RefusjonskravPrArbeidsgiverVurderingDto {

    @JsonProperty("arbeidsgiverId")
    @Valid
    @NotNull
    private String arbeidsgiverId;

    @JsonProperty("skalUtvideGyldighet")
    @Valid
    @NotNull
    private boolean skalUtvideGyldighet;

    public RefusjonskravPrArbeidsgiverVurderingDto() {
    }

    public RefusjonskravPrArbeidsgiverVurderingDto(@Valid @NotNull String arbeidsgiverId, @Valid @NotNull boolean skalUtvideGyldighet) {
        this.arbeidsgiverId = arbeidsgiverId;
        this.skalUtvideGyldighet = skalUtvideGyldighet;
    }

    public String getArbeidsgiverId() {
        return arbeidsgiverId;
    }

    public boolean isSkalUtvideGyldighet() {
        return skalUtvideGyldighet;
    }
}
