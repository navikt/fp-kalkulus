package no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = NON_ABSENT, content = NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class FastsatteVerdierDto {

    @JsonProperty("refusjonPrÅr")
    @Valid
    @NotNull
    private Integer refusjonPrÅr;

    @JsonProperty("fastsattÅrsbeløp")
    @Valid
    @NotNull
    private Integer fastsattÅrsbeløp;

    @JsonProperty("inntektskategori")
    @Valid
    @NotNull
    private Inntektskategori inntektskategori;

    @JsonProperty("skalHaBesteberegning")
    @Valid
    @NotNull
    private Boolean skalHaBesteberegning;

    public FastsatteVerdierDto(@Valid @NotNull Integer refusjonPrÅr, @Valid @NotNull Integer fastsattÅrsbeløp, @Valid @NotNull Inntektskategori inntektskategori, @Valid @NotNull Boolean skalHaBesteberegning) {
        this.refusjonPrÅr = refusjonPrÅr;
        this.fastsattÅrsbeløp = fastsattÅrsbeløp;
        this.inntektskategori = inntektskategori;
        this.skalHaBesteberegning = skalHaBesteberegning;
    }

    public Integer getRefusjonPrÅr() {
        return refusjonPrÅr;
    }

    public Integer getFastsattÅrsbeløp() {
        return fastsattÅrsbeløp;
    }

    public Inntektskategori getInntektskategori() {
        return inntektskategori;
    }

    public Boolean getSkalHaBesteberegning() {
        return skalHaBesteberegning;
    }
}
