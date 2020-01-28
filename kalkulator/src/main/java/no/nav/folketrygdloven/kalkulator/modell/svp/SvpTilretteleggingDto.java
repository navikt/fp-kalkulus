package no.nav.folketrygdloven.kalkulator.modell.svp;

import java.util.Objects;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;

public class SvpTilretteleggingDto {

    private Arbeidsgiver arbeidsgiver;
    private boolean skalBrukes = true;
    private InternArbeidsforholdRefDto internArbeidsforholdRef;
    private boolean harSøktDelvisTilrettelegging = false;

    public SvpTilretteleggingDto() {
        //jaja
    }

    public SvpTilretteleggingDto(SvpTilretteleggingDto svpTilrettelegging) {
        this.arbeidsgiver = svpTilrettelegging.getArbeidsgiver().orElse(null);
        this.skalBrukes = svpTilrettelegging.getSkalBrukes();
        this.harSøktDelvisTilrettelegging = svpTilrettelegging.harSøktDelvisTilrettelegging;
        this.internArbeidsforholdRef = svpTilrettelegging.internArbeidsforholdRef;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SvpTilretteleggingDto that = (SvpTilretteleggingDto) o;
        return Objects.equals(arbeidsgiver, that.arbeidsgiver) &&
            Objects.equals(internArbeidsforholdRef, that.internArbeidsforholdRef) &&
            Objects.equals(harSøktDelvisTilrettelegging, that.harSøktDelvisTilrettelegging);
    }

    @Override
    public int hashCode() {
        return Objects.hash(internArbeidsforholdRef, harSøktDelvisTilrettelegging, arbeidsgiver);
    }

    public boolean getHarSøktDelvisTilrettelegging() {
        return harSøktDelvisTilrettelegging;
    }

    public Optional<Arbeidsgiver> getArbeidsgiver() {
        return Optional.ofNullable(arbeidsgiver);
    }

    public boolean getSkalBrukes() {
        return skalBrukes;
    }

    public static class Builder {

        private SvpTilretteleggingDto mal;

        public Builder() {
            this(new SvpTilretteleggingDto());
        }

        public Builder(SvpTilretteleggingDto tilretteleggingEntitet) {
            mal = new SvpTilretteleggingDto(tilretteleggingEntitet);
        }

        public Builder medInternArbeidsforholdRef(InternArbeidsforholdRefDto internArbeidsforholdRef) {
            this.mal.internArbeidsforholdRef = internArbeidsforholdRef;
            return this;
        }

        public Builder medArbeidsgiver(Arbeidsgiver arbeidsgiver) {
            this.mal.arbeidsgiver = arbeidsgiver;
            return this;
        }

        public Builder medSkalBrukes(boolean skalBrukes) {
            this.mal.skalBrukes = skalBrukes;
            return this;
        }

        public Builder medHarSøktDelvisTilrettelegging(boolean harSøktDelvisTilrettelegging) {
            this.mal.harSøktDelvisTilrettelegging = harSøktDelvisTilrettelegging;
            return this;
        }

        public Builder medDelvisTilrettelegging() {
            this.mal.harSøktDelvisTilrettelegging = true;
            return this;
        }

        public SvpTilretteleggingDto build() {
            return this.mal;
        }
    }
}
