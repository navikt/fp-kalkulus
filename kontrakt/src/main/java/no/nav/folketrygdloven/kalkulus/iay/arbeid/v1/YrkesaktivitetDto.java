package no.nav.folketrygdloven.kalkulus.iay.arbeid.v1;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.felles.v1.Aktør;
import no.nav.folketrygdloven.kalkulus.felles.v1.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class YrkesaktivitetDto {

    @JsonProperty("arbeidsgiver")
    @Valid
    private Aktør arbeidsgiver;

    @JsonProperty("abakusReferanse")
    @Valid
    private InternArbeidsforholdRefDto abakusReferanse;

    @JsonProperty("arbeidType")
    @Valid
    @NotNull
    private ArbeidType arbeidType;

    @JsonProperty("aktivitetsAvtaler")
    @Valid
    @Size
    private List<AktivitetsAvtaleDto> aktivitetsAvtaler;

    protected YrkesaktivitetDto() {
        // default ctor
    }

    public YrkesaktivitetDto(@Valid Aktør arbeidsgiver,
                             @Valid InternArbeidsforholdRefDto abakusReferanse,
                             @Valid @NotNull ArbeidType arbeidType,
                             @Valid List<AktivitetsAvtaleDto> aktivitetsAvtaler) {

        this.arbeidsgiver = arbeidsgiver;
        this.abakusReferanse = abakusReferanse;
        this.arbeidType = arbeidType;
        this.aktivitetsAvtaler = aktivitetsAvtaler;
    }

    public Aktør getArbeidsgiver() {
        return arbeidsgiver;
    }

    public InternArbeidsforholdRefDto getAbakusReferanse() {
        return abakusReferanse;
    }

    public ArbeidType getArbeidType() {
        return arbeidType;
    }

    public List<AktivitetsAvtaleDto> getAktivitetsAvtaler() {
        return aktivitetsAvtaler;
    }


    @AssertTrue(message = "Må ha arbeidsgiver for arbeidtype FRILANSER_OPPDRAGSTAKER eller ORDINÆRT_ARBEIDSFORHOLD.")
    private boolean okArbeidsgiver() {
        return arbeidsgiver != null || !(arbeidType.equals(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD) || arbeidType.equals(ArbeidType.FRILANSER_OPPDRAGSTAKER_MED_MER));
    }


}
