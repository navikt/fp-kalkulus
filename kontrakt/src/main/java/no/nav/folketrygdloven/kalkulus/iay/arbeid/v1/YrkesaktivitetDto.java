package no.nav.folketrygdloven.kalkulus.iay.arbeid.v1;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

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
    @NotNull
    private Aktør arbeidsgiver;

    @JsonProperty("abakusReferanse")
    @Valid
    @NotNull
    private InternArbeidsforholdRefDto abakusReferanse;

    @JsonProperty("arbeidType")
    @Valid
    @NotNull
    private ArbeidType arbeidType;

    @JsonProperty("aktivitetsAvtaler")
    @Valid
    private List<AktivitetsAvtaleDto> aktivitetsAvtaler;

    @JsonProperty("navnArbeidsgiverUtland")
    @Pattern(regexp = "^[\\p{Graph}\\p{Space}\\p{Sc}\\p{L}\\p{M}\\p{N}]+$", message="Yrkesaktivitet#navnArbeidsgiverUtland '${validatedValue}' matcher ikke tillatt pattern '{regexp}'")
    @Valid
    private String navnArbeidsgiverUtland;

    protected YrkesaktivitetDto() {
        // default ctor
    }

    public YrkesaktivitetDto(@Valid @NotNull Aktør arbeidsgiver,
                             @Valid @NotNull InternArbeidsforholdRefDto abakusReferanse,
                             @Valid @NotNull ArbeidType arbeidType,
                             @Valid List<AktivitetsAvtaleDto> aktivitetsAvtaler,
                             @Pattern(regexp = "^[\\p{Graph}\\p{Space}\\p{Sc}\\p{L}\\p{M}\\p{N}]+$", message = "Yrkesaktivitet#navnArbeidsgiverUtland '${validatedValue}' matcher ikke tillatt pattern '{regexp}'") @Valid String navnArbeidsgiverUtland) {

        this.arbeidsgiver = arbeidsgiver;
        this.abakusReferanse = abakusReferanse;
        this.arbeidType = arbeidType;
        this.aktivitetsAvtaler = aktivitetsAvtaler;
        this.navnArbeidsgiverUtland = navnArbeidsgiverUtland;
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

    public String getNavnArbeidsgiverUtland() {
        return navnArbeidsgiverUtland;
    }
}
