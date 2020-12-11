package no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.refusjon;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;

public class RefusjonSplittAndel {
    private Arbeidsgiver arbeidsgiver;
    private InternArbeidsforholdRefDto internArbeidsforholdRefDto;
    private LocalDate startdatoRefusjon;
    private BigDecimal delvisRefusjonBeløpPrÅr;

    public RefusjonSplittAndel(Arbeidsgiver arbeidsgiver,
                               InternArbeidsforholdRefDto internArbeidsforholdRefDto,
                               LocalDate startdatoRefusjon,
                               BigDecimal delvisRefusjonBeløpPrÅr) {
        this.arbeidsgiver = arbeidsgiver;
        this.internArbeidsforholdRefDto = internArbeidsforholdRefDto;
        this.startdatoRefusjon = startdatoRefusjon;
        this.delvisRefusjonBeløpPrÅr = delvisRefusjonBeløpPrÅr;
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

    public BigDecimal getDelvisRefusjonBeløpPrÅr() {
        return delvisRefusjonBeløpPrÅr;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RefusjonSplittAndel that = (RefusjonSplittAndel) o;
        return Objects.equals(arbeidsgiver, that.arbeidsgiver) &&
                Objects.equals(internArbeidsforholdRefDto, that.internArbeidsforholdRefDto) &&
                Objects.equals(delvisRefusjonBeløpPrÅr, that.delvisRefusjonBeløpPrÅr) &&
                Objects.equals(startdatoRefusjon, that.startdatoRefusjon);
    }

    @Override
    public int hashCode() {
        return Objects.hash(arbeidsgiver, internArbeidsforholdRefDto, delvisRefusjonBeløpPrÅr, startdatoRefusjon);
    }
}
