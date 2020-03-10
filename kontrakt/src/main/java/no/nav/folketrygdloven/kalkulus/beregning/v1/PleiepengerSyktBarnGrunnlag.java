package no.nav.folketrygdloven.kalkulus.beregning.v1;

import java.math.BigDecimal;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseTyperKalkulusStøtterKontrakt;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, creatorVisibility = Visibility.NONE)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
public class PleiepengerSyktBarnGrunnlag extends YtelsespesifiktGrunnlagDto {

    public static final String YTELSE_TYPE = "PSB";

    @JsonProperty(value = "utbetalingsgradPrAktivitet", required = true)
    @NotNull
    @Valid
    private List<UtbetalingsgradPrAktivitetDto> utbetalingsgradPrAktivitet;

    protected PleiepengerSyktBarnGrunnlag() {
        // default ctor
    }

    public PleiepengerSyktBarnGrunnlag(@Valid @DecimalMin(value = "0.00", message = "dekningsgrad ${validatedValue} må være >= {value}") @DecimalMax(value = "100.00", message = "dekningsgrad ${validatedValue} må være <= {value}") BigDecimal dekningsgrad,
                                       @Valid @NotNull Integer grunnbeløpMilitærHarKravPå,
                                       @NotNull @Valid List<UtbetalingsgradPrAktivitetDto> utbetalingsgradPrAktivitet) {
        super(dekningsgrad, grunnbeløpMilitærHarKravPå);
        this.utbetalingsgradPrAktivitet = utbetalingsgradPrAktivitet;
    }

    public List<UtbetalingsgradPrAktivitetDto> getUtbetalingsgradPrAktivitet() {
        return utbetalingsgradPrAktivitet;
    }

    public static String getYtelseType() {
        return YTELSE_TYPE;
    }
}
