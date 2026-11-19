package no.nav.foreldrepenger.kalkulus.kontrakt.request.håndtering.overstyring;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.håndtering.HåndterBeregningDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.håndtering.refusjon.RefusjonOverstyringAndel;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = NON_ABSENT, content = NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class OverstyrRefusjonHåndteringDto extends HåndterBeregningDto {

    @JsonProperty("overstyrteAndeler")
    @Size(max = 100)
    private List<@Valid @NotNull RefusjonOverstyringAndel> overstyrteAndeler;

    private OverstyrRefusjonHåndteringDto(boolean avbrutt) {
        super(AvklaringsbehovDefinisjon.OVST_REFUSJON, avbrutt);
    }

    public static OverstyrRefusjonHåndteringDto avbryt() {
        return new OverstyrRefusjonHåndteringDto(true);
    }

    @JsonCreator
    public OverstyrRefusjonHåndteringDto(@JsonProperty("overstyrteAndeler") @NotNull List<@Valid RefusjonOverstyringAndel> overstyrteAndeler) {
        super(AvklaringsbehovDefinisjon.OVST_REFUSJON, false);
        this.overstyrteAndeler = overstyrteAndeler;
    }

    public List<RefusjonOverstyringAndel> getOverstyrteAndeler() {
        return overstyrteAndeler;
    }

}
