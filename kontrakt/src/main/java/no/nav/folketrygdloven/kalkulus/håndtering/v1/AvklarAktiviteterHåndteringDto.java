package no.nav.folketrygdloven.kalkulus.håndtering.v1;

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
public class AvklarAktiviteterHåndteringDto extends HåndterBeregningDto {
    public static final String IDENT_TYPE = "AVKLAR";

    @JsonProperty("avklarteAktiviteterDto")
    @Valid
    @NotNull
    private AvklarteAktiviteterDto avklarteAktiviteterDto;

    public AvklarAktiviteterHåndteringDto() {
        // default ctor
    }

    public AvklarAktiviteterHåndteringDto(AvklarteAktiviteterDto avklarteAktiviteterDto) {
        this.avklarteAktiviteterDto = avklarteAktiviteterDto;
    }

    @Override
    public String getIdentType() {
        return IDENT_TYPE;
    }

    public AvklarteAktiviteterDto getAvklarteAktiviteterDto() {
        return avklarteAktiviteterDto;
    }

}
