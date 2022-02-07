package no.nav.folketrygdloven.kalkulus.beregning.v1;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, creatorVisibility = Visibility.NONE)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
public class OmsorgspengerGrunnlag extends YtelsespesifiktGrunnlagDto {

    public static final String YTELSE_TYPE = "OMP";

    @JsonProperty(value = "utbetalingsgradPrAktivitet", required = true)
    @Size(min = 1)
    @Valid
    private List<UtbetalingsgradPrAktivitetDto> utbetalingsgradPrAktivitet;

    @JsonProperty(value = "søknadsperioderPrAktivitet")
    @Size()
    @Valid
    private List<SøknadsperioderPrAktivitetDto> søknadsperioderPrAktivitet;

    protected OmsorgspengerGrunnlag() {
        // default ctor
    }


    public OmsorgspengerGrunnlag(@NotNull @Valid List<UtbetalingsgradPrAktivitetDto> utbetalingsgradPrAktivitet) {
        this.utbetalingsgradPrAktivitet = utbetalingsgradPrAktivitet;
    }

    public List<UtbetalingsgradPrAktivitetDto> getUtbetalingsgradPrAktivitet() {
        return utbetalingsgradPrAktivitet;
    }

    public List<SøknadsperioderPrAktivitetDto> getSøknadsperioderPrAktivitet() {
        return søknadsperioderPrAktivitet;
    }

    public static String getYtelseType() {
        return YTELSE_TYPE;
    }


    @Override
    public String toString() {
        return "OmsorgspengerGrunnlag{" +
                "utbetalingsgradPrAktivitet=" + utbetalingsgradPrAktivitet +
                ", søknadsperioderPrAktivitet=" + søknadsperioderPrAktivitet +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        OmsorgspengerGrunnlag that = (OmsorgspengerGrunnlag) o;
        return Objects.equals(utbetalingsgradPrAktivitet, that.utbetalingsgradPrAktivitet) && Objects.equals(søknadsperioderPrAktivitet, that.søknadsperioderPrAktivitet);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), utbetalingsgradPrAktivitet, søknadsperioderPrAktivitet);
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

    @AssertTrue(message = "Liste med søknadsperioder skal ikke ha duplikate arbeidsforhold")
    public boolean isIngenDuplikateArbeidsforholdForSøknad() {
        if (søknadsperioderPrAktivitet == null) {
            return true;
        }
        long antallUnike = søknadsperioderPrAktivitet.stream().map(SøknadsperioderPrAktivitetDto::getAktivitet)
                .distinct()
                .count();
        long antall = søknadsperioderPrAktivitet.stream().map(SøknadsperioderPrAktivitetDto::getAktivitet)
                .count();
        return antall == antallUnike;
    }

}
