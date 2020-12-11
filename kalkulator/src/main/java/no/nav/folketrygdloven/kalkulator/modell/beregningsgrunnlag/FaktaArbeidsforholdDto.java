package no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag;

import java.util.Objects;

import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;


public class FaktaArbeidsforholdDto {

    private Arbeidsgiver arbeidsgiver;
    private InternArbeidsforholdRefDto arbeidsforholdRef;
    private Boolean erTidsbegrenset;
    private Boolean harMottattYtelse;
    private Boolean harLønnsendringIBeregningsperioden;

    public FaktaArbeidsforholdDto(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto arbeidsforholdRefDto) {
        this.arbeidsgiver = arbeidsgiver;
        this.arbeidsforholdRef = arbeidsforholdRefDto;
    }

    public FaktaArbeidsforholdDto(FaktaArbeidsforholdDto original) {
        this.arbeidsgiver = Arbeidsgiver.fra(original.getArbeidsgiver());
        this.arbeidsforholdRef = original.getArbeidsforholdRef();
        this.erTidsbegrenset = original.getErTidsbegrenset();
        this.harMottattYtelse = original.getHarMottattYtelse();
        this.harLønnsendringIBeregningsperioden = original.getHarLønnsendringIBeregningsperioden();
    }

    public Arbeidsgiver getArbeidsgiver() {
        return arbeidsgiver;
    }

    public InternArbeidsforholdRefDto getArbeidsforholdRef() {
        return arbeidsforholdRef == null ? InternArbeidsforholdRefDto.nullRef() : arbeidsforholdRef;
    }

    public boolean gjelderFor(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto arbeidsforholdRef) {
        return Objects.equals(this.getArbeidsgiver(), arbeidsgiver) &&
                this.getArbeidsforholdRef().gjelderFor(arbeidsforholdRef);
    }

    public Boolean getErTidsbegrenset() {
        return erTidsbegrenset;
    }

    public Boolean getHarMottattYtelse() {
        return harMottattYtelse;
    }

    public Boolean getHarLønnsendringIBeregningsperioden() {
        return harLønnsendringIBeregningsperioden;
    }

    @Override
    public String toString() {
        return "FaktaArbeidsforholdDto{" +
                "arbeidsgiver=" + arbeidsgiver +
                ", arbeidsforholdRef=" + arbeidsforholdRef +
                ", erTidsbegrenset=" + erTidsbegrenset +
                ", harMottattYtelse=" + harMottattYtelse +
                '}';
    }

    public static Builder builder(FaktaArbeidsforholdDto kopi) {
        return new Builder(kopi);
    }

    public static Builder builder(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto arbeidsforholdRefDto) {
        return new Builder(arbeidsgiver, arbeidsforholdRefDto);
    }

    public static class Builder {
        private FaktaArbeidsforholdDto mal;

        public Builder(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto arbeidsforholdRef) {
            mal = new FaktaArbeidsforholdDto(arbeidsgiver, arbeidsforholdRef);
        }

        private Builder(FaktaArbeidsforholdDto faktaArbeidsforholdDto) {
            mal = new FaktaArbeidsforholdDto(faktaArbeidsforholdDto);
        }

        static Builder oppdater(FaktaArbeidsforholdDto faktaArbeidsforholdDto) {
            return new Builder(faktaArbeidsforholdDto);
        }

        public Builder medArbeidsgiver(Arbeidsgiver arbeidsgiver) {
            mal.arbeidsgiver = arbeidsgiver;
            return this;
        }

        public Builder medArbeidsforholdRef(InternArbeidsforholdRefDto arbeidsforholdRef) {
            mal.arbeidsforholdRef = arbeidsforholdRef;
            return this;
        }

        public Builder medHarMottattYtelse(Boolean harMottattYtelse) {
            mal.harMottattYtelse = harMottattYtelse;
            return this;
        }

        public Builder medErTidsbegrenset(Boolean erTidsbegrenset) {
            mal.erTidsbegrenset = erTidsbegrenset;
            return this;
        }

        public Builder medHarLønnsendringIBeregningsperioden(Boolean harLønnsendringIBeregningsperioden) {
            mal.harLønnsendringIBeregningsperioden = harLønnsendringIBeregningsperioden;
            return this;
        }

        public FaktaArbeidsforholdDto build() {
            verifyStateForBuild();
            return mal;
        }

        private void verifyStateForBuild() {
            Objects.requireNonNull(mal.arbeidsgiver, "arbeidsgiver");
            if (manglerFakta()) {
                throw new IllegalStateException("Må ha satt minst et faktafelt.");
            }
        }

        // Brukes av fp-sak og må vere public
        public boolean manglerFakta() {
            return mal.erTidsbegrenset == null &&
                    mal.harLønnsendringIBeregningsperioden == null &&
                    mal.harMottattYtelse == null;
        }
    }
}
