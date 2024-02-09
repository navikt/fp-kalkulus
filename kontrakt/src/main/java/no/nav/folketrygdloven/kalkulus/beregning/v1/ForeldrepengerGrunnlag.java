package no.nav.folketrygdloven.kalkulus.beregning.v1;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import no.nav.folketrygdloven.kalkulus.beregning.v1.besteberegning.Ytelsegrunnlag;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, creatorVisibility = Visibility.NONE)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
public class ForeldrepengerGrunnlag extends YtelsespesifiktGrunnlagDto {

    @JsonProperty(value = "aktivitetGradering")
    @Valid
    private AktivitetGraderingDto aktivitetGradering;

    @JsonProperty(value = "sisteSøkteUttaksdag")
    @Valid
    private LocalDate sisteSøkteUttaksdag;

    @JsonProperty(value = "ytelsegrunnlagForBesteberegning")
    @Valid
    private List<Ytelsegrunnlag> ytelsegrunnlagForBesteberegning;

    protected ForeldrepengerGrunnlag() {
        // default ctor
    }

    public ForeldrepengerGrunnlag(@Valid @DecimalMin(value = "0.00", message = "verdien ${validatedValue} må være >= {value}") @DecimalMax(value = "100.00", message = "verdien ${validatedValue} må være <= {value}") @Digits(integer = 3, fraction = 2) BigDecimal dekningsgrad,
                                  @Valid @NotNull Boolean kvalifisererTilBesteberegning,
                                  @Valid AktivitetGraderingDto aktivitetGradering) {
        super(dekningsgrad, kvalifisererTilBesteberegning);
        this.aktivitetGradering = aktivitetGradering;
    }

    public ForeldrepengerGrunnlag(@Valid @DecimalMin(value = "0.00", message = "verdien ${validatedValue} må være >= {value}") @DecimalMax(value = "100.00", message = "verdien ${validatedValue} må være <= {value}") @Digits(integer = 3, fraction = 2) BigDecimal dekningsgrad,
                                  @Valid @NotNull Boolean kvalifisererTilBesteberegning,
                                  @Valid AktivitetGraderingDto aktivitetGradering,
                                  @Valid LocalDate sisteSøkteUttaksdag) {
        super(dekningsgrad, kvalifisererTilBesteberegning);
        this.aktivitetGradering = aktivitetGradering;
        this.sisteSøkteUttaksdag = sisteSøkteUttaksdag;
    }

    public ForeldrepengerGrunnlag(@Valid @DecimalMin(value = "0.00", message = "verdien ${validatedValue} må være >= {value}") @DecimalMax(value = "100.00", message = "verdien ${validatedValue} må være <= {value}") @Digits(integer = 3, fraction = 2) BigDecimal dekningsgrad,
                                  @Valid @NotNull Boolean kvalifisererTilBesteberegning,
                                  @Valid AktivitetGraderingDto aktivitetGradering,
                                  @Valid LocalDate sisteSøkteUttaksdag,
                                  @Valid List<Ytelsegrunnlag> ytelsegrunnlagForBesteberegning) {
        super(dekningsgrad, kvalifisererTilBesteberegning);
        this.aktivitetGradering = aktivitetGradering;
        this.sisteSøkteUttaksdag = sisteSøkteUttaksdag;
        this.ytelsegrunnlagForBesteberegning = ytelsegrunnlagForBesteberegning;
    }

    public AktivitetGraderingDto getAktivitetGradering() {
        return aktivitetGradering;
    }

    public LocalDate getSisteSøkteUttaksdag() {
        return sisteSøkteUttaksdag;
    }

    public List<Ytelsegrunnlag> getYtelsegrunnlagForBesteberegning() {
        return ytelsegrunnlagForBesteberegning;
    }
}
