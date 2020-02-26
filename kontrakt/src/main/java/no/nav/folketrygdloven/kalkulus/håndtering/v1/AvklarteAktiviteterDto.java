package no.nav.folketrygdloven.kalkulus.håndtering.v1;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = NON_ABSENT, content = NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class AvklarteAktiviteterDto {

    @JsonProperty("beregningsaktivitetLagreDtoList")
    @Valid
    @NotNull
    private List<BeregningsaktivitetLagreDto> beregningsaktivitetLagreDtoList;

    public AvklarteAktiviteterDto(List<BeregningsaktivitetLagreDto> beregningsaktivitetLagreDtoList) { // NOSONAR
        this.beregningsaktivitetLagreDtoList = beregningsaktivitetLagreDtoList;
    }

    public List<BeregningsaktivitetLagreDto> getBeregningsaktivitetLagreDtoList() {
        return beregningsaktivitetLagreDtoList;
    }
}
