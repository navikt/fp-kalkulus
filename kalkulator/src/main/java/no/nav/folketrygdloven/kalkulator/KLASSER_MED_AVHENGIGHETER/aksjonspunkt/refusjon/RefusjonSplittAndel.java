package no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.refusjon;

import java.time.LocalDate;
import java.util.Objects;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;

public class RefusjonSplittAndel {
    private Arbeidsgiver arbeidsgiver;
    private InternArbeidsforholdRefDto internArbeidsforholdRefDto;
    private LocalDate startdatoRefusjon;

    public RefusjonSplittAndel(Arbeidsgiver arbeidsgiver,
                               InternArbeidsforholdRefDto internArbeidsforholdRefDto,
                               LocalDate startdatoRefusjon) {
        this.arbeidsgiver = arbeidsgiver;
        this.internArbeidsforholdRefDto = internArbeidsforholdRefDto;
        this.startdatoRefusjon = startdatoRefusjon;
    }

    public Arbeidsgiver getArbeidsgiver() {
        return arbeidsgiver;
    }

    public InternArbeidsforholdRefDto getInternArbeidsforholdRefDto() {
        return internArbeidsforholdRefDto == null ? InternArbeidsforholdRefDto.nullRef() : internArbeidsforholdRefDto;
    }

    public LocalDate getStartdatoRefusjon() {
        return startdatoRefusjon;
    }

    public boolean gjelderFor(BeregningsgrunnlagPrStatusOgAndelDto andel) {
        Arbeidsgiver andelAG = andel.getArbeidsgiver().orElse(null);
        InternArbeidsforholdRefDto andelRef = andel.getArbeidsforholdRef().orElse(InternArbeidsforholdRefDto.nullRef());
        return Objects.equals(andelAG, arbeidsgiver) && getInternArbeidsforholdRefDto().gjelderFor(andelRef);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RefusjonSplittAndel that = (RefusjonSplittAndel) o;
        return Objects.equals(arbeidsgiver, that.arbeidsgiver) &&
                Objects.equals(internArbeidsforholdRefDto, that.internArbeidsforholdRefDto) &&
                Objects.equals(startdatoRefusjon, that.startdatoRefusjon);
    }

    @Override
    public int hashCode() {
        return Objects.hash(arbeidsgiver, internArbeidsforholdRefDto, startdatoRefusjon);
    }
}
