package no.nav.folketrygdloven.kalkulus.iay.ytelse.v1;

import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.felles.v1.Aktør;
import no.nav.folketrygdloven.kalkulus.kodeverk.InntektPeriodeType;

/** Angir hyppighet og størrelse for ytelse. */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
public class YtelseFordelingDto {

    /** Tillater kun positive verdier. Max verdi håndteres av mottager. */
    @JsonProperty(value = "beløp", required = true)
    @Valid
    @NotNull
    @Digits(integer = 8, fraction = 2)
    @DecimalMin("0.00")
    @DecimalMax("10000000.00")
    private BigDecimal beløp;

    /** Angir hvilken periode beløp gjelder for. */
    @JsonProperty(value = "inntektPeriodeType", required = true)
    @NotNull
    private InntektPeriodeType inntektPeriodeType;

    /** Kan være null. */
    @JsonProperty(value = "arbeidsgiver")
    @Valid
    private Aktør arbeidsgiver;

    protected YtelseFordelingDto() {
    }

    public YtelseFordelingDto(Aktør arbeidsgiver, InntektPeriodeType inntektPeriodeType, int beløp) {
        this(arbeidsgiver, inntektPeriodeType, BigDecimal.valueOf(beløp));
    }

    public YtelseFordelingDto(Aktør arbeidsgiver, InntektPeriodeType inntektPeriodeType, BigDecimal beløp) {
        this.arbeidsgiver = arbeidsgiver;
        this.inntektPeriodeType = inntektPeriodeType;
        this.beløp = beløp == null ? null : beløp.setScale(2, RoundingMode.HALF_UP);
    }

    public Aktør getArbeidsgiver() {
        return arbeidsgiver;
    }

    public BigDecimal getBeløp() {
        return beløp;
    }

    public InntektPeriodeType getHyppighet() {
        return inntektPeriodeType;
    }

}
