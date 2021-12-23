package no.nav.folketrygdloven.kalkulus.beregning.v1;

import java.util.List;
import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, creatorVisibility = Visibility.NONE)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
public class PleiepengerNærståendeGrunnlag extends YtelsespesifiktGrunnlagDto {

    public static final String YTELSE_TYPE = "PPN";

    @JsonProperty(value = "utbetalingsgradPrAktivitet", required = true)
    @Size()
    @Valid
    private List<UtbetalingsgradPrAktivitetDto> utbetalingsgradPrAktivitet;

    protected PleiepengerNærståendeGrunnlag() {
        // default ctor
    }

    public PleiepengerNærståendeGrunnlag(@NotNull @Valid List<UtbetalingsgradPrAktivitetDto> utbetalingsgradPrAktivitet) {
        this.utbetalingsgradPrAktivitet = utbetalingsgradPrAktivitet;
    }

    public List<UtbetalingsgradPrAktivitetDto> getUtbetalingsgradPrAktivitet() {
        return utbetalingsgradPrAktivitet;
    }

    public static String getYtelseType() {
        return YTELSE_TYPE;
    }

    @Override
    public String toString() {
        return "PleiepengerNærståendeGrunnlag{" +
                "utbetalingsgradPrAktivitet=" + utbetalingsgradPrAktivitet +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        PleiepengerNærståendeGrunnlag that = (PleiepengerNærståendeGrunnlag) o;
        return Objects.equals(utbetalingsgradPrAktivitet, that.utbetalingsgradPrAktivitet);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), utbetalingsgradPrAktivitet);
    }
}
