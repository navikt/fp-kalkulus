package no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RefusjonskravPrArbeidsgiverVurderingDto {

    @JsonProperty("arbeidsgiverId")
    @Valid
    @NotNull
    private String arbeidsgiverId;

    @JsonProperty("skalUtvideGyldighet")
    @Valid
    @NotNull
    private boolean skalUtvideGyldighet;

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
