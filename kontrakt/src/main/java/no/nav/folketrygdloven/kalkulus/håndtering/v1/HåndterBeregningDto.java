package no.nav.folketrygdloven.kalkulus.håndtering.v1;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta.FaktaOmBeregningHåndteringDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.HåndteringKode;


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "identType", defaultImpl = Void.class)
@JsonSubTypes({
        @JsonSubTypes.Type(value = AvklarAktiviteterHåndteringDto.class, name = AvklarAktiviteterHåndteringDto.IDENT_TYPE),
        @JsonSubTypes.Type(value = FaktaOmBeregningHåndteringDto.class, name = FaktaOmBeregningHåndteringDto.IDENT_TYPE),
        @JsonSubTypes.Type(value = OverstyrBeregningsaktiviteterDto.class, name = OverstyrBeregningsaktiviteterDto.IDENT_TYPE),
        @JsonSubTypes.Type(value = OverstyrBeregningsgrunnlagHåndteringDto.class, name = OverstyrBeregningsgrunnlagHåndteringDto.IDENT_TYPE),
})
@JsonInclude(value = NON_ABSENT, content = NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public abstract class HåndterBeregningDto {

    @JsonProperty(value = "kode")
    @NotNull
    @Valid
    private HåndteringKode kode;

    public HåndterBeregningDto(@NotNull @Valid HåndteringKode kode) {
        this.kode = kode;
    }

    public HåndterBeregningDto() {
        // default ctor
    }

    public HåndteringKode getKode() {
        return kode;
    }

    /** Type ident. (per ident fra subklasse). */
    public abstract String getIdentType();
}
