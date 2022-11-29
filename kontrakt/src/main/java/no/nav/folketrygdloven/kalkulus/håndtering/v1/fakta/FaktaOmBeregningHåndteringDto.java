package no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.håndtering.v1.HåndterBeregningDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = NON_ABSENT, content = NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class FaktaOmBeregningHåndteringDto extends HåndterBeregningDto {

    public static final String IDENT_TYPE = "5058";
    public static final String AVKLARINGSBEHOV_KODE = "VURDER_FAKTA_ATFL_SN";

    @JsonProperty("fakta")
    @Valid
    @NotNull
    private FaktaBeregningLagreDto fakta;

    public FaktaOmBeregningHåndteringDto() {
    }

    public FaktaOmBeregningHåndteringDto(@Valid @NotNull FaktaBeregningLagreDto fakta) {
        super(AvklaringsbehovDefinisjon.fraKodeNy(AVKLARINGSBEHOV_KODE));
        this.fakta = fakta;
    }

    @Override
    public String getIdentType() {
        return IDENT_TYPE;
    }

    @Override
    public String getAvklaringsbehovKode() {
        return AVKLARINGSBEHOV_KODE;
    }


    public FaktaBeregningLagreDto getFakta() {
        return fakta;
    }


}
