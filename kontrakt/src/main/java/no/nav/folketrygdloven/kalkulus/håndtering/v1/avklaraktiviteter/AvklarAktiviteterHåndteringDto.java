package no.nav.folketrygdloven.kalkulus.håndtering.v1.avklaraktiviteter;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Pattern;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.HåndterBeregningDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;
import no.nav.folketrygdloven.kalkulus.kodeverk.HåndteringKode;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = NON_ABSENT, content = NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class AvklarAktiviteterHåndteringDto extends HåndterBeregningDto {

    public static final String IDENT_TYPE = "5052";
    public static final String AVKLARINGSBEHOV_KODE = "AVKLAR_AKTIVITETER";

    @JsonProperty("avklarteAktiviteterDto")
    @Valid
    @NotNull
    private AvklarteAktiviteterDto avklarteAktiviteterDto;

    @JsonProperty("avklaringsbehovKode")
    @Valid
    @Pattern(regexp = "^[\\p{Graph}\\p{Space}\\p{Sc}\\p{L}\\p{M}\\p{N}§]+$", message = "[${validatedValue}] matcher ikke tillatt pattern [{regexp}]")
    private String avklaringsbehovKode = AVKLARINGSBEHOV_KODE;

    @JsonCreator
    public AvklarAktiviteterHåndteringDto(@JsonProperty("avklarteAktiviteterDto") AvklarteAktiviteterDto avklarteAktiviteterDto) {
        super(AvklaringsbehovDefinisjon.fraKodeNy(AVKLARINGSBEHOV_KODE));
        this.avklarteAktiviteterDto = avklarteAktiviteterDto;
    }

    @Override
    public String getIdentType() {
        return IDENT_TYPE;
    }

    @Override
    public String getAvklaringsbehovKode() {
        return AVKLARINGSBEHOV_KODE;
    }


    public AvklarteAktiviteterDto getAvklarteAktiviteterDto() {
        return avklarteAktiviteterDto;
    }

}
