package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class AvklarAktiviteterDto {

    @JsonProperty(value = "aktiviteterTomDatoMapping")
    private List<AktivitetTomDatoMappingDto> aktiviteterTomDatoMapping;

    public List<AktivitetTomDatoMappingDto> getAktiviteterTomDatoMapping() {
        return aktiviteterTomDatoMapping;
    }

    public void setAktiviteterTomDatoMapping(List<AktivitetTomDatoMappingDto> aktiviteterTomDatoMapping) {
        this.aktiviteterTomDatoMapping = aktiviteterTomDatoMapping;
    }
}
