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

import no.nav.folketrygdloven.kalkulus.håndtering.v1.avklaraktiviteter.AvklarAktiviteterHåndteringDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta.FaktaOmBeregningHåndteringDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fordeling.FaktaOmFordelingHåndteringDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.foreslå.FastsettBGTidsbegrensetArbeidsforholdHåndteringDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.foreslå.FastsettBeregningsgrunnlagATFLHåndteringDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.foreslå.FastsettBeregningsgrunnlagSNNyIArbeidslivetHåndteringDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.foreslå.FastsettBruttoBeregningsgrunnlagSNHåndteringDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.foreslå.VurderVarigEndringEllerNyoppstartetSNHåndteringDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.overstyring.OverstyrBeregningsaktiviteterDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.overstyring.OverstyrBeregningsgrunnlagHåndteringDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.HåndteringKode;


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "identType", defaultImpl = Void.class)
@JsonSubTypes({
        @JsonSubTypes.Type(value = AvklarAktiviteterHåndteringDto.class, name = AvklarAktiviteterHåndteringDto.IDENT_TYPE),
        @JsonSubTypes.Type(value = FaktaOmBeregningHåndteringDto.class, name = FaktaOmBeregningHåndteringDto.IDENT_TYPE),
        @JsonSubTypes.Type(value = OverstyrBeregningsaktiviteterDto.class, name = OverstyrBeregningsaktiviteterDto.IDENT_TYPE),
        @JsonSubTypes.Type(value = OverstyrBeregningsgrunnlagHåndteringDto.class, name = OverstyrBeregningsgrunnlagHåndteringDto.IDENT_TYPE),
        @JsonSubTypes.Type(value = FaktaOmFordelingHåndteringDto.class, name = FaktaOmFordelingHåndteringDto.IDENT_TYPE),
        @JsonSubTypes.Type(value = FastsettBeregningsgrunnlagATFLHåndteringDto.class, name = FastsettBeregningsgrunnlagATFLHåndteringDto.IDENT_TYPE),
        @JsonSubTypes.Type(value = FastsettBeregningsgrunnlagSNNyIArbeidslivetHåndteringDto.class, name = FastsettBeregningsgrunnlagSNNyIArbeidslivetHåndteringDto.IDENT_TYPE),
        @JsonSubTypes.Type(value = FastsettBGTidsbegrensetArbeidsforholdHåndteringDto.class, name = FastsettBGTidsbegrensetArbeidsforholdHåndteringDto.IDENT_TYPE),
        @JsonSubTypes.Type(value = FastsettBruttoBeregningsgrunnlagSNHåndteringDto.class, name = FastsettBruttoBeregningsgrunnlagSNHåndteringDto.IDENT_TYPE),
        @JsonSubTypes.Type(value = VurderVarigEndringEllerNyoppstartetSNHåndteringDto.class, name = VurderVarigEndringEllerNyoppstartetSNHåndteringDto.IDENT_TYPE),
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
