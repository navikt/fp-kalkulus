package no.nav.folketrygdloven.kalkulus.iay.ytelse.v1;

import java.util.Set;

import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.kodeverk.Fagsystem;
import no.nav.folketrygdloven.kalkulus.kodeverk.RelatertYtelseTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.RelatertYtelseType;
import no.nav.folketrygdloven.kalkulus.kodeverk.TemaUnderkategori;


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.ALWAYS, content = Include.ALWAYS)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class YtelseDto {

    @JsonProperty("ytelseGrunnlag")
    @Valid
    private YtelseGrunnlagDto ytelseGrunnlag;

    @JsonProperty("ytelseAnvist")
    @Valid
    private Set<YtelseAnvistDto> ytelseAnvist;

    @JsonProperty("relatertYtelseType")
    @Valid
    private RelatertYtelseType relatertYtelseType;

    @JsonProperty("periode")
    @Valid
    private Periode periode;

    @JsonProperty("status")
    @Valid
    private RelatertYtelseTilstand status;

    @JsonProperty("kilde")
    @Valid
    private Fagsystem kilde;

    @JsonProperty("temaUnderkategori")
    @Valid
    private TemaUnderkategori temaUnderkategori;

    protected YtelseDto() {
        // default ctor
    }

    public YtelseDto(@Valid YtelseGrunnlagDto ytelseGrunnlag,
                     @Valid Set<YtelseAnvistDto> ytelseAnvist,
                     @Valid RelatertYtelseType relatertYtelseType,
                     @Valid Periode periode,
                     @Valid RelatertYtelseTilstand status,
                     @Valid Fagsystem kilde,
                     @Valid TemaUnderkategori temaUnderkategori) {
        this.ytelseGrunnlag = ytelseGrunnlag;
        this.ytelseAnvist = ytelseAnvist;
        this.relatertYtelseType = relatertYtelseType;
        this.periode = periode;
        this.status = status;
        this.kilde = kilde;
        this.temaUnderkategori = temaUnderkategori;
    }

    public YtelseGrunnlagDto getYtelseGrunnlag() {
        return ytelseGrunnlag;
    }

    public Set<YtelseAnvistDto> getYtelseAnvist() {
        return ytelseAnvist;
    }

    public RelatertYtelseType getRelatertYtelseType() {
        return relatertYtelseType;
    }

    public Periode getPeriode() {
        return periode;
    }

    public RelatertYtelseTilstand getStatus() {
        return status;
    }

    public Fagsystem getKilde() {
        return kilde;
    }

    public TemaUnderkategori getTemaUnderkategori() {
        return temaUnderkategori;
    }
}
