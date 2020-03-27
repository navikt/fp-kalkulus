package no.nav.folketrygdloven.kalkulus.iay.ytelse.v1;

import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.felles.v1.BeløpDto;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.kodeverk.RelatertYtelseType;
import no.nav.folketrygdloven.kalkulus.kodeverk.TemaUnderkategori;


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.ALWAYS, content = Include.ALWAYS)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class YtelseDto {


    @JsonProperty(value = "vedtaksDagsats")
    @Valid
    private BeløpDto vedtaksDagsats;

    @JsonProperty("ytelseAnvist")
    @Valid
    @Size
    private Set<YtelseAnvistDto> ytelseAnvist;

    @JsonProperty(value = "relatertYtelseType", required = true)
    @Valid
    @NotNull
    private RelatertYtelseType relatertYtelseType;

    @JsonProperty(value = "periode", required = true)
    @Valid
    @NotNull
    private Periode periode;

    @JsonProperty("temaUnderkategori")
    @Valid
    private TemaUnderkategori temaUnderkategori;

    protected YtelseDto() {
        // default ctor
    }

    public YtelseDto(@Valid BeløpDto vedtaksDagsats,
                     @Valid @Size Set<YtelseAnvistDto> ytelseAnvist,
                     @Valid @NotNull RelatertYtelseType relatertYtelseType,
                     @Valid @NotNull Periode periode,
                     @Valid TemaUnderkategori temaUnderkategori) {
        this.vedtaksDagsats = vedtaksDagsats;
        this.ytelseAnvist = ytelseAnvist;
        this.relatertYtelseType = relatertYtelseType;
        this.periode = periode;
        this.temaUnderkategori = temaUnderkategori;
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

    public TemaUnderkategori getTemaUnderkategori() {
        return temaUnderkategori;
    }

    public BeløpDto getVedtaksDagsats() {
        return vedtaksDagsats;
    }
}
