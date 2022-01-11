package no.nav.folketrygdloven.kalkulus.håndtering.v1.overstyring;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.håndtering.v1.HåndterBeregningDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.avklaraktiviteter.BeregningsaktivitetLagreDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.HåndteringKode;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = NON_ABSENT, content = NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class OverstyrBeregningsaktiviteterDto extends HåndterBeregningDto {

    public static final String IDENT_TYPE = "6014";

    @JsonProperty("beregningsaktivitetLagreDtoList")
    @Valid
    @NotNull
    @Size(max = 1000)
    private List<BeregningsaktivitetLagreDto> beregningsaktivitetLagreDtoList;


    public OverstyrBeregningsaktiviteterDto() {
        super(HåndteringKode.fraKode(IDENT_TYPE));
        // Json deserialisering
    }

    public OverstyrBeregningsaktiviteterDto(@Valid @NotNull @Size(max = 1000) List<BeregningsaktivitetLagreDto> beregningsaktivitetLagreDtoList) {
        super(HåndteringKode.fraKode(IDENT_TYPE));
        this.beregningsaktivitetLagreDtoList = beregningsaktivitetLagreDtoList;
    }

    public List<BeregningsaktivitetLagreDto> getBeregningsaktivitetLagreDtoList() {
        return beregningsaktivitetLagreDtoList;
    }

    @Override
    public String getIdentType() {
        return IDENT_TYPE;
    }
}
