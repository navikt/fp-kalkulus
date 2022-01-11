package no.nav.folketrygdloven.kalkulator.modell.iay;

import java.math.BigDecimal;
import java.math.RoundingMode;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.felles.v1.Aktør;
import no.nav.folketrygdloven.kalkulus.kodeverk.InntektPeriodeType;

public class YtelseFordelingDto {

    private BigDecimal beløp;
    private InntektPeriodeType inntektPeriodeType;
    private Arbeidsgiver arbeidsgiver;
    private Boolean erRefusjon;

    protected YtelseFordelingDto() {
    }

    public YtelseFordelingDto(Arbeidsgiver arbeidsgiver, InntektPeriodeType inntektPeriodeType, int beløp, Boolean erRefusjon) {
        this(arbeidsgiver, inntektPeriodeType, BigDecimal.valueOf(beløp), erRefusjon);
    }

    public YtelseFordelingDto(Arbeidsgiver arbeidsgiver, InntektPeriodeType inntektPeriodeType, BigDecimal beløp, Boolean erRefusjon) {
        this.arbeidsgiver = arbeidsgiver;
        this.inntektPeriodeType = inntektPeriodeType;
        this.beløp = beløp == null ? null : beløp.setScale(2, RoundingMode.HALF_UP);
    }

    public Arbeidsgiver getArbeidsgiver() {
        return arbeidsgiver;
    }

    public BigDecimal getBeløp() {
        return beløp;
    }

    public InntektPeriodeType getHyppighet() {
        return inntektPeriodeType;
    }

    public Boolean getErRefusjon() {
        return erRefusjon;
    }
}
