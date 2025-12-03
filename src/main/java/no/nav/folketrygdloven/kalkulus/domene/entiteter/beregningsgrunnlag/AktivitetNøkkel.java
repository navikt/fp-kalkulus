package no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag;

import java.time.LocalDate;
import java.util.Objects;

import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;


public class AktivitetNøkkel implements Comparable<AktivitetNøkkel> {
    private OpptjeningAktivitetType opptjeningAktivitetType;
    private LocalDate fom;
    private LocalDate tom;
    private String arbeidsgiverIdentifikator;
    private String arbeidsforholdRef;

    private AktivitetNøkkel() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AktivitetNøkkel)) {
            return false;
        }
        AktivitetNøkkel that = (AktivitetNøkkel) o;
        return Objects.equals(opptjeningAktivitetType, that.opptjeningAktivitetType)
                && Objects.equals(fom, that.fom)
                && Objects.equals(tom, that.tom)
                && Objects.equals(arbeidsgiverIdentifikator, that.arbeidsgiverIdentifikator)
                && Objects.equals(arbeidsforholdRef, that.arbeidsforholdRef);
    }

    @Override
    public int hashCode() {
        return Objects.hash(opptjeningAktivitetType, fom, tom, arbeidsgiverIdentifikator, arbeidsforholdRef);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public int compareTo(AktivitetNøkkel nøkkel) {

        int sammenlignFom = this.fom.compareTo(nøkkel.fom);
        boolean fomErUlike = sammenlignFom != 0;
        if (fomErUlike) {
            return sammenlignFom;
        }

        int sammenlignOpptjeningType = this.opptjeningAktivitetType.compareTo(nøkkel.opptjeningAktivitetType);

        boolean opptjengtypeErUlike = sammenlignOpptjeningType != 0;
        if (opptjengtypeErUlike) {
            return sammenlignOpptjeningType;
        }

        if (this.tom != null && nøkkel.tom != null) {
            int sammenlignTom = this.tom.compareTo(nøkkel.tom);
            boolean tomErUlike = sammenlignTom != 0;
            if (tomErUlike) {
                return sammenlignTom;
            }
        }

        if (this.arbeidsgiverIdentifikator == null) {
            return 1;
        }

        if (nøkkel.arbeidsgiverIdentifikator == null) {
            return -1;
        }

        int sammenlignArbeidsgiverId = this.arbeidsgiverIdentifikator.compareTo(nøkkel.arbeidsgiverIdentifikator);

        boolean arbeidsgiverIdErUlike = sammenlignArbeidsgiverId != 0;
        if (arbeidsgiverIdErUlike) {
            return sammenlignArbeidsgiverId;
        }

        if (nøkkel.arbeidsforholdRef != null) {
            return 1;
        }

        return -1;

    }

    public static class Builder {
        private AktivitetNøkkel kladd;

        private Builder() {
            kladd = new AktivitetNøkkel();
        }

        public Builder medOpptjeningAktivitetType(OpptjeningAktivitetType opptjeningAktivitetType) {
            kladd.opptjeningAktivitetType = opptjeningAktivitetType;
            return this;
        }

        public Builder medFom(LocalDate fom) {
            kladd.fom = fom;
            return this;
        }

        public Builder medTom(LocalDate tom) {
            kladd.tom = tom;
            return this;
        }

        public Builder medArbeidsgiverIdentifikator(String arbeidsgiverIdentifikator) {
            kladd.arbeidsgiverIdentifikator = arbeidsgiverIdentifikator;
            return this;
        }

        public Builder medArbeidsforholdRef(String arbeidsforholdRef) {
            kladd.arbeidsforholdRef = arbeidsforholdRef;
            return this;
        }

        public AktivitetNøkkel build() {
            verifyStateForBuild();
            return kladd;
        }

        private void verifyStateForBuild() {
            Objects.requireNonNull(kladd.opptjeningAktivitetType);
            Objects.requireNonNull(kladd.fom);
        }
    }
}
