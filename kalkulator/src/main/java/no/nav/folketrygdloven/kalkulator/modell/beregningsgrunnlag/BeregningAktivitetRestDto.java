package no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.diff.IndexKey;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.ArbeidsgiverMedNavn;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningAktivitetHandlingType;


public class BeregningAktivitetRestDto {

    private Intervall periode;
    private BeregningAktivitetAggregatRestDto beregningAktiviteter;
    private ArbeidsgiverMedNavn arbeidsgiver;
    private InternArbeidsforholdRefDto arbeidsforholdRef;
    private OpptjeningAktivitetType opptjeningAktivitetType = OpptjeningAktivitetType.UDEFINERT;

    public BeregningAktivitetRestDto() {
        // hibernate
    }

    public BeregningAktivitetRestDto(BeregningAktivitetRestDto original) {
        this.opptjeningAktivitetType = original.getOpptjeningAktivitetType();
        this.periode = original.getPeriode();
        this.arbeidsgiver = ArbeidsgiverMedNavn.fra(original.getArbeidsgiver());
        this.arbeidsforholdRef = original.getArbeidsforholdRef();
    }

    public Intervall getPeriode() {
        return periode;
    }

    public ArbeidsgiverMedNavn getArbeidsgiver() {
        return arbeidsgiver;
    }

    public InternArbeidsforholdRefDto getArbeidsforholdRef() {
        return arbeidsforholdRef == null ? InternArbeidsforholdRefDto.nullRef() : arbeidsforholdRef;
    }

    public OpptjeningAktivitetType getOpptjeningAktivitetType() {
        return opptjeningAktivitetType != null ? opptjeningAktivitetType : OpptjeningAktivitetType.UDEFINERT;
    }

    public BeregningAktivitetAggregatRestDto getBeregningAktiviteter() {
        return beregningAktiviteter;
    }

    void setBeregningAktiviteter(BeregningAktivitetAggregatRestDto beregningAktiviteter) {
        this.beregningAktiviteter = beregningAktiviteter;
    }

    public BeregningAktivitetNøkkel getNøkkel() {
        BeregningAktivitetNøkkel.Builder builder = BeregningAktivitetNøkkel.builder()
                .medOpptjeningAktivitetType(opptjeningAktivitetType)
                .medFom(periode.getFomDato())
                .medTom(periode.getTomDato())
                .medArbeidsforholdRef(getArbeidsforholdRef().getReferanse());
        if (arbeidsgiver != null) {
            builder.medArbeidsgiverIdentifikator(arbeidsgiver.getIdentifikator());
        }
        return builder.build();
    }

    public boolean gjelderFor(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto arbeidsforholdRef) {
        return Objects.equals(this.getArbeidsgiver(), arbeidsgiver) &&
                this.getArbeidsforholdRef().gjelderFor(arbeidsforholdRef);
    }

    public boolean skalBrukes(BeregningAktivitetOverstyringerDto overstyringer) {
        List<BeregningAktivitetOverstyringDto> overstyringerForAktivitet = overstyringer.getOverstyringer().stream()
                .filter(overstyring -> overstyring.getNøkkel().equals(this.getNøkkel())).collect(Collectors.toList());
        if (overstyringerForAktivitet.isEmpty()) {
            return true;
        }
        if (overstyringerForAktivitet.size() == 1) {
            return !BeregningAktivitetHandlingType.IKKE_BENYTT.equals(overstyringerForAktivitet.get(0).getHandling());
        }
        throw new IllegalStateException("Kan ikke ha flere overstyringer for aktivitet " + this.toString());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BeregningAktivitetRestDto that = (BeregningAktivitetRestDto) o;
        return Objects.equals(periode, that.periode) &&
                Objects.equals(arbeidsgiver, that.arbeidsgiver) &&
                Objects.equals(arbeidsforholdRef, that.arbeidsforholdRef) &&
                Objects.equals(opptjeningAktivitetType, that.opptjeningAktivitetType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(periode, arbeidsgiver, arbeidsforholdRef, opptjeningAktivitetType);
    }

    @Override
    public String toString() {
        return "BeregningAktivitet{" +
                "periode=" + periode +
                ", arbeidsgiver=" + arbeidsgiver +
                ", arbeidsforholdRef=" + arbeidsforholdRef +
                ", opptjeningAktivitetType=" + getOpptjeningAktivitetType().getKode() +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }


    public static Builder kopier(BeregningAktivitetRestDto beregningAktivitetDto) {
        return new Builder(beregningAktivitetDto);
    }

    public String getIndexKey() {
        return IndexKey.createKey(arbeidsgiver, arbeidsforholdRef);
    }

    public static class Builder {
        private BeregningAktivitetRestDto mal;

        private Builder() {
            mal = new BeregningAktivitetRestDto();
        }

        private Builder(BeregningAktivitetRestDto beregningAktivitetDto) {
            mal = new BeregningAktivitetRestDto(beregningAktivitetDto);
        }

        public Builder medPeriode(Intervall periode) {
            mal.periode = periode;
            return this;
        }

        public Builder medArbeidsgiver(ArbeidsgiverMedNavn arbeidsgiver) {
            mal.arbeidsgiver = arbeidsgiver;
            return this;
        }

        public Builder medArbeidsforholdRef(InternArbeidsforholdRefDto arbeidsforholdRef) {
            mal.arbeidsforholdRef = arbeidsforholdRef;
            return this;
        }

        public Builder medOpptjeningAktivitetType(OpptjeningAktivitetType opptjeningAktivitetType) {
            mal.opptjeningAktivitetType = opptjeningAktivitetType;
            return this;
        }

        public BeregningAktivitetRestDto build() {
            verifyStateForBuild();
            return mal;
        }

        private void verifyStateForBuild() {
            Objects.requireNonNull(mal.opptjeningAktivitetType, "opptjeningAktivitetType");
            Objects.requireNonNull(mal.periode, "periode");
        }
    }
}
