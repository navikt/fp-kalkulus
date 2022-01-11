package no.nav.folketrygdloven.kalkulus.beregning.v1.besteberegning;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.kodeverk.RelatertYtelseType;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class Ytelsegrunnlag {

    @JsonProperty(value = "ytelse")
    @Valid
    @NotNull
    private RelatertYtelseType ytelse;

    @JsonProperty(value = "perioder")
    @Valid
    @NotNull
    private List<Ytelseperiode> perioder = new ArrayList<>();

    public Ytelsegrunnlag(@Valid @NotNull RelatertYtelseType ytelse,
                          @Valid @NotNull List<Ytelseperiode> perioder) {
        this.ytelse = ytelse;
        this.perioder = perioder;
    }

    public RelatertYtelseType getYtelse() {
        return ytelse;
    }

    public List<Ytelseperiode> getPerioder() {
        return perioder;
    }
}
