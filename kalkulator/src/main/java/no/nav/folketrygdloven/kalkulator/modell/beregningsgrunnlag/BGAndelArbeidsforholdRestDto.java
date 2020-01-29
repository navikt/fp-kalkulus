package no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.ArbeidsgiverMedNavn;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;


public class BGAndelArbeidsforholdRestDto {

    private BeregningsgrunnlagPrStatusOgAndelRestDto beregningsgrunnlagPrStatusOgAndel;
    private ArbeidsgiverMedNavn arbeidsgiver;
    private InternArbeidsforholdRefDto arbeidsforholdRef;
    private BigDecimal refusjonskravPrÅr;
    private BigDecimal naturalytelseBortfaltPrÅr;
    private BigDecimal naturalytelseTilkommetPrÅr;
    private Boolean erTidsbegrensetArbeidsforhold;
    private Boolean lønnsendringIBeregningsperioden;
    private LocalDate arbeidsperiodeFom;
    private LocalDate arbeidsperiodeTom;

    private BGAndelArbeidsforholdRestDto() {
    }

    public BGAndelArbeidsforholdRestDto(BGAndelArbeidsforholdRestDto eksisterendeBGAndelArbeidsforhold) {
        this.arbeidsgiver = eksisterendeBGAndelArbeidsforhold.arbeidsgiver;
        this.arbeidsforholdRef = eksisterendeBGAndelArbeidsforhold.arbeidsforholdRef;
        this.refusjonskravPrÅr = eksisterendeBGAndelArbeidsforhold.refusjonskravPrÅr;
        this.naturalytelseBortfaltPrÅr = eksisterendeBGAndelArbeidsforhold.naturalytelseBortfaltPrÅr;
        this.naturalytelseTilkommetPrÅr = eksisterendeBGAndelArbeidsforhold.naturalytelseTilkommetPrÅr;
        this.erTidsbegrensetArbeidsforhold = eksisterendeBGAndelArbeidsforhold.erTidsbegrensetArbeidsforhold;
        this.lønnsendringIBeregningsperioden = eksisterendeBGAndelArbeidsforhold.lønnsendringIBeregningsperioden;
        this.arbeidsperiodeFom = eksisterendeBGAndelArbeidsforhold.arbeidsperiodeFom;
        this.arbeidsperiodeTom = eksisterendeBGAndelArbeidsforhold.arbeidsperiodeTom;
    }

    public InternArbeidsforholdRefDto getArbeidsforholdRef() {
        return arbeidsforholdRef != null ? arbeidsforholdRef : InternArbeidsforholdRefDto.nullRef();
    }

    public BigDecimal getRefusjonskravPrÅr() {
        return refusjonskravPrÅr;
    }

    public Optional<BigDecimal> getNaturalytelseBortfaltPrÅr() {
        return Optional.ofNullable(naturalytelseBortfaltPrÅr);
    }

    public Optional<BigDecimal> getNaturalytelseTilkommetPrÅr() {
        return Optional.ofNullable(naturalytelseTilkommetPrÅr);
    }

    public Boolean getErTidsbegrensetArbeidsforhold() {
        return erTidsbegrensetArbeidsforhold;
    }

    public Boolean erLønnsendringIBeregningsperioden() {
        return lønnsendringIBeregningsperioden;
    }

    public LocalDate getArbeidsperiodeFom() {
        return arbeidsperiodeFom;
    }

    public Optional<LocalDate> getArbeidsperiodeTom() {
        return Optional.ofNullable(arbeidsperiodeTom);
    }

    public Intervall getArbeidsperiode() {
        if (arbeidsperiodeTom == null) {
            return Intervall.fraOgMed(arbeidsperiodeFom);
        }
        return Intervall.fraOgMedTilOgMed(arbeidsperiodeFom, arbeidsperiodeTom);
    }

    public String getArbeidsforholdOrgnr() {
        return getArbeidsgiver().getOrgnr();
    }

    public ArbeidsgiverMedNavn getArbeidsgiver() {
        return arbeidsgiver;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof BGAndelArbeidsforholdRestDto)) {
            return false;
        }
        BGAndelArbeidsforholdRestDto other = (BGAndelArbeidsforholdRestDto) obj;
        return Objects.equals(this.getArbeidsgiver(), other.getArbeidsgiver())
                && Objects.equals(this.arbeidsforholdRef, other.arbeidsforholdRef);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getArbeidsgiver(), arbeidsforholdRef);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + //$NON-NLS-1$
                "orgnr=" + getArbeidsforholdOrgnr() + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "arbeidsgiver=" + arbeidsgiver + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "arbeidsforholdRef=" + arbeidsforholdRef + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "naturalytelseBortfaltPrÅr=" + naturalytelseBortfaltPrÅr + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "naturalytelseTilkommetPrÅr=" + naturalytelseTilkommetPrÅr + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "refusjonskravPrÅr=" + refusjonskravPrÅr + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "arbeidsperiodeFom=" + arbeidsperiodeFom //$NON-NLS-1$
                + "arbeidsperiodeTom=" + arbeidsperiodeTom //$NON-NLS-1$
                + ">"; //$NON-NLS-1$
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(BGAndelArbeidsforholdRestDto bgAndelArbeidsforhold) {
        return bgAndelArbeidsforhold == null ? new Builder() : new Builder(bgAndelArbeidsforhold);
    }

    public static class Builder {
        private BGAndelArbeidsforholdRestDto bgAndelArbeidsforhold;
        private boolean erOppdatering;

        private Builder() {
            bgAndelArbeidsforhold = new BGAndelArbeidsforholdRestDto();
        }

        private Builder(BGAndelArbeidsforholdRestDto eksisterendeBGAndelArbeidsforhold) {
            bgAndelArbeidsforhold = new BGAndelArbeidsforholdRestDto(eksisterendeBGAndelArbeidsforhold);
        }

        private Builder(BGAndelArbeidsforholdRestDto eksisterendeBGAndelArbeidsforhold, boolean erOppdatering) {
            bgAndelArbeidsforhold = eksisterendeBGAndelArbeidsforhold;
            this.erOppdatering = erOppdatering;
        }

        private static Builder oppdater(BGAndelArbeidsforholdRestDto oppdatere) {
            return new Builder(oppdatere, true);
        }

        static Builder ny() {
            return new Builder(new BGAndelArbeidsforholdRestDto(), false);
        }

        public static Builder oppdater(Optional<BGAndelArbeidsforholdRestDto> bgAndelArbeidsforhold) {
            return bgAndelArbeidsforhold.map(Builder::oppdater).orElseGet(Builder::ny);
        }

        public static Builder kopier(BGAndelArbeidsforholdRestDto bgAndelArbeidsforhold) {
            return new Builder(bgAndelArbeidsforhold);
        }

        public Builder medArbeidsgiver(ArbeidsgiverMedNavn arbeidsgiver) {
            bgAndelArbeidsforhold.arbeidsgiver = arbeidsgiver;
            return this;
        }

        public Builder medArbeidsforholdRef(String arbeidsforholdRef) {
            return medArbeidsforholdRef(arbeidsforholdRef==null?null: InternArbeidsforholdRefDto.ref(arbeidsforholdRef));
        }

        public Builder medArbeidsforholdRef(InternArbeidsforholdRefDto arbeidsforholdRef) {
            bgAndelArbeidsforhold.arbeidsforholdRef = arbeidsforholdRef;
            return this;
        }

        public Builder medNaturalytelseBortfaltPrÅr(BigDecimal naturalytelseBortfaltPrÅr) {
            bgAndelArbeidsforhold.naturalytelseBortfaltPrÅr = naturalytelseBortfaltPrÅr;
            return this;
        }

        public Builder medNaturalytelseTilkommetPrÅr(BigDecimal naturalytelseTilkommetPrÅr) {
            bgAndelArbeidsforhold.naturalytelseTilkommetPrÅr = naturalytelseTilkommetPrÅr;
            return this;
        }

        public Builder medRefusjonskravPrÅr(BigDecimal refusjonskravPrÅr) {
            bgAndelArbeidsforhold.refusjonskravPrÅr = refusjonskravPrÅr;
            return this;
        }

        public BGAndelArbeidsforholdRestDto.Builder medTidsbegrensetArbeidsforhold(Boolean erTidsbegrensetArbeidsforhold) {
            bgAndelArbeidsforhold.erTidsbegrensetArbeidsforhold = erTidsbegrensetArbeidsforhold;
            return this;
        }

        public Builder medLønnsendringIBeregningsperioden(Boolean lønnsendringIBeregningsperioden) {
            bgAndelArbeidsforhold.lønnsendringIBeregningsperioden = lønnsendringIBeregningsperioden;
            return this;
        }

        public Builder medArbeidsperiodeFom(LocalDate arbeidsperiodeFom) {
            bgAndelArbeidsforhold.arbeidsperiodeFom = arbeidsperiodeFom;
            return this;
        }

        public Builder medArbeidsperiodeTom(LocalDate arbeidsperiodeTom) {
            bgAndelArbeidsforhold.arbeidsperiodeTom = arbeidsperiodeTom;
            return this;
        }

        BGAndelArbeidsforholdRestDto build(BeregningsgrunnlagPrStatusOgAndelRestDto andel) {
            Objects.requireNonNull(bgAndelArbeidsforhold.arbeidsgiver, "arbeidsgiver");
            bgAndelArbeidsforhold.beregningsgrunnlagPrStatusOgAndel = andel;
            return bgAndelArbeidsforhold;
        }
    }
}
