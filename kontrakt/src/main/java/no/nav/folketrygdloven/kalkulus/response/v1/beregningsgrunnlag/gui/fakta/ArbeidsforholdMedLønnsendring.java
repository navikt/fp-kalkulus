package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.fakta;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.time.LocalDate;

import javax.validation.Valid;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.FaktaOmBeregningAndelDto;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class ArbeidsforholdMedLønnsendring extends FaktaOmBeregningAndelDto {

    @Valid
    @JsonProperty(value = "sisteLønnsendring")
    @Size
    private LocalDate sisteLønnsendring;

    public ArbeidsforholdMedLønnsendring() {
    }

    public ArbeidsforholdMedLønnsendring(FaktaOmBeregningAndelDto faktaAndel, LocalDate sisteLønnsendring) {
        super(faktaAndel.getAndelsnr(), faktaAndel.getArbeidsforhold(), faktaAndel.getInntektskategori(), faktaAndel.getAktivitetStatus(), faktaAndel.getLagtTilAvSaksbehandler(), faktaAndel.getFastsattAvSaksbehandler(), faktaAndel.getAndelIArbeid(), faktaAndel.getKilde());
        this.sisteLønnsendring = sisteLønnsendring;
    }

    public LocalDate getSisteLønnsendring() {
        return sisteLønnsendring;
    }

    public void setSisteLønnsendring(LocalDate sisteLønnsendring) {
        this.sisteLønnsendring = sisteLønnsendring;
    }

}
