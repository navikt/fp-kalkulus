package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.detaljert;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.time.LocalDate;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.response.v1.Arbeidsgiver;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class BeregningRefusjonOverstyringDto {

    @JsonProperty(value = "arbeidsgiver")
    @Valid
    @NotNull
    private Arbeidsgiver arbeidsgiver;

    @JsonProperty(value = "førsteMuligeRefusjonFom")
    @Valid
    private LocalDate førsteMuligeRefusjonFom;

    @JsonProperty(value = "refusjonPerioder")
    @Valid
    @Size(min = 1)
    private List<BeregningRefusjonPeriodeDto> refusjonPerioder;

    public BeregningRefusjonOverstyringDto() {
    }

    public BeregningRefusjonOverstyringDto(@Valid @NotNull Arbeidsgiver arbeidsgiver, @Valid LocalDate førsteMuligeRefusjonFom) {
        this.arbeidsgiver = arbeidsgiver;
        this.førsteMuligeRefusjonFom = førsteMuligeRefusjonFom;
    }

    public BeregningRefusjonOverstyringDto(@Valid @NotNull Arbeidsgiver arbeidsgiver,
                                           @Valid LocalDate førsteMuligeRefusjonFom,
                                           @Valid List<BeregningRefusjonPeriodeDto> refusjonPerioder) {
        this.arbeidsgiver = arbeidsgiver;
        this.førsteMuligeRefusjonFom = førsteMuligeRefusjonFom;
        this.refusjonPerioder = refusjonPerioder;
    }


    public Arbeidsgiver getArbeidsgiver() {
        return arbeidsgiver;
    }

    public LocalDate getFørsteMuligeRefusjonFom() {
        return førsteMuligeRefusjonFom;
    }
}
