package no.nav.folketrygdloven.kalkulus.beregning.v1;

import java.util.List;
import java.util.Objects;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, creatorVisibility = Visibility.NONE)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
public class OmsorgspengerGrunnlag extends YtelsespesifiktGrunnlagDto {

    public static final String YTELSE_TYPE = "OMP";

    @JsonProperty(value = "utbetalingsgradPrAktivitet", required = true)
    @Size(min = 1)
    @Valid
    private List<UtbetalingsgradPrAktivitetDto> utbetalingsgradPrAktivitet;


    protected OmsorgspengerGrunnlag() {
        // default ctor
    }


    public OmsorgspengerGrunnlag(@NotNull @Valid List<UtbetalingsgradPrAktivitetDto> utbetalingsgradPrAktivitet) {
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
        return "OmsorgspengerGrunnlag{" +
                "utbetalingsgradPrAktivitet=" + utbetalingsgradPrAktivitet +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        OmsorgspengerGrunnlag that = (OmsorgspengerGrunnlag) o;
        return Objects.equals(utbetalingsgradPrAktivitet, that.utbetalingsgradPrAktivitet);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), utbetalingsgradPrAktivitet);
    }


    @AssertTrue(message = "Liste med utbetalingsgrader skal ikke ha duplikate arbeidsforhold")
    public boolean isIngenDuplikateArbeidsforhold() {
        long antallUnike = utbetalingsgradPrAktivitet.stream().map(UtbetalingsgradPrAktivitetDto::getUtbetalingsgradArbeidsforholdDto)
                .distinct()
                .count();
        long antall = utbetalingsgradPrAktivitet.stream().map(UtbetalingsgradPrAktivitetDto::getUtbetalingsgradArbeidsforholdDto)
                .count();
        return antall == antallUnike;
    }

}
