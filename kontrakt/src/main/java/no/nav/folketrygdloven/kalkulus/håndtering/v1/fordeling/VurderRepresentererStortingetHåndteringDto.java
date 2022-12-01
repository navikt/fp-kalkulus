package no.nav.folketrygdloven.kalkulus.håndtering.v1.fordeling;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.HåndterBeregningDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = NON_ABSENT, content = NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class VurderRepresentererStortingetHåndteringDto extends HåndterBeregningDto {

    public static final String IDENT_TYPE = "5087";
    public static final String AVKLARINGSBEHOV_KODE = "VURDER_REPRSNTR_STORTNGT";


    @JsonProperty("avklaringsbehovKode")
    @Valid
    @Pattern(regexp = "^[\\p{Graph}\\p{Space}\\p{Sc}\\p{L}\\p{M}\\p{N}§]+$", message = "[${validatedValue}] matcher ikke tillatt pattern [{regexp}]")
    private String avklaringsbehovKode = AVKLARINGSBEHOV_KODE;


    @JsonProperty("fom")
    @Valid
    private LocalDate fom;

    @JsonProperty("tom")
    @Valid
    private LocalDate tom;

    @Valid
    @JsonProperty(value = "representererStortinget")
    private boolean representererStortinget;

    public VurderRepresentererStortingetHåndteringDto() {
        super(AvklaringsbehovDefinisjon.fraKodeNy(AVKLARINGSBEHOV_KODE));
    }

    public VurderRepresentererStortingetHåndteringDto(LocalDate fom, LocalDate tom, boolean representererStortinget) {
        super(AvklaringsbehovDefinisjon.fraKodeNy(AVKLARINGSBEHOV_KODE));
        this.fom = fom;
        this.tom = tom;
        this.representererStortinget = representererStortinget;
    }

    @Override
    public String getIdentType() {
        return IDENT_TYPE;
    }

    @Override
    public String getAvklaringsbehovKode() {
        return AVKLARINGSBEHOV_KODE;
    }


    public LocalDate getFom() {
        return fom;
    }

    public void setFom(LocalDate fom) {
        this.fom = fom;
    }

    public LocalDate getTom() {
        return tom;
    }

    public void setTom(LocalDate tom) {
        this.tom = tom;
    }

    public boolean getRepresentererStortinget() {
        return representererStortinget;
    }

    public void setRepresentererStortinget(boolean representererStortinget) {
        this.representererStortinget = representererStortinget;
    }
}
