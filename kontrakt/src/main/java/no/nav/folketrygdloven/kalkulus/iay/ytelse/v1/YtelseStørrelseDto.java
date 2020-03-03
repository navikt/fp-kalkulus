package no.nav.folketrygdloven.kalkulus.iay.ytelse.v1;

import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.felles.v1.Aktør;
import no.nav.folketrygdloven.kalkulus.felles.v1.BeløpDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.InntektPeriodeType;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.ALWAYS, content = Include.ALWAYS)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class YtelseStørrelseDto {

    @JsonProperty(value = "hyppighet")
    @Valid
    private InntektPeriodeType hyppighet;

    @JsonProperty(value = "aktør")
    @Valid
    private Aktør aktør;

    @JsonProperty(value = "beløp")
    @Valid
    private BeløpDto beløp;

    protected YtelseStørrelseDto() {
        // default ctor
    }

    public YtelseStørrelseDto(InntektPeriodeType hyppighet, Aktør aktør, BeløpDto beløp) {
        this.hyppighet = hyppighet;
        this.aktør = aktør;
        this.beløp = beløp;
    }

    public InntektPeriodeType getHyppighet() {
        return hyppighet;
    }

    public Aktør getAktør() {
        return aktør;
    }

    public BeløpDto getBeløp() {
        return beløp;
    }
}
