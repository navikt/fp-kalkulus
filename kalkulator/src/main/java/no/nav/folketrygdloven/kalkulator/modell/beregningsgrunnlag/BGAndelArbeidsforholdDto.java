package no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.Hjemmel;


public class BGAndelArbeidsforholdDto {

    private BeregningsgrunnlagPrStatusOgAndelDto beregningsgrunnlagPrStatusOgAndel;
    private Arbeidsgiver arbeidsgiver;
    private InternArbeidsforholdRefDto arbeidsforholdRef;
    private BigDecimal refusjonskravPrÅr;
    private BigDecimal saksbehandletRefusjonPrÅr;
    private BigDecimal fordeltRefusjonPrÅr;
    private BigDecimal naturalytelseBortfaltPrÅr;
    private BigDecimal naturalytelseTilkommetPrÅr;
    private Boolean erTidsbegrensetArbeidsforhold;
    private LocalDate arbeidsperiodeFom;
    private LocalDate arbeidsperiodeTom;
    private Hjemmel hjemmelForRefusjonskravfrist;

    private BGAndelArbeidsforholdDto() {
    }

    public BGAndelArbeidsforholdDto(BGAndelArbeidsforholdDto eksisterendeBGAndelArbeidsforhold) {
        this.arbeidsgiver = eksisterendeBGAndelArbeidsforhold.arbeidsgiver;
        this.arbeidsforholdRef = eksisterendeBGAndelArbeidsforhold.arbeidsforholdRef;
        this.refusjonskravPrÅr = eksisterendeBGAndelArbeidsforhold.refusjonskravPrÅr;
        this.naturalytelseBortfaltPrÅr = eksisterendeBGAndelArbeidsforhold.naturalytelseBortfaltPrÅr;
        this.naturalytelseTilkommetPrÅr = eksisterendeBGAndelArbeidsforhold.naturalytelseTilkommetPrÅr;
        this.erTidsbegrensetArbeidsforhold = eksisterendeBGAndelArbeidsforhold.erTidsbegrensetArbeidsforhold;
        this.arbeidsperiodeFom = eksisterendeBGAndelArbeidsforhold.arbeidsperiodeFom;
        this.arbeidsperiodeTom = eksisterendeBGAndelArbeidsforhold.arbeidsperiodeTom;
        this.saksbehandletRefusjonPrÅr = eksisterendeBGAndelArbeidsforhold.saksbehandletRefusjonPrÅr;
        this.fordeltRefusjonPrÅr = eksisterendeBGAndelArbeidsforhold.fordeltRefusjonPrÅr;
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
        return null;
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

    public Arbeidsgiver getArbeidsgiver() {
        return arbeidsgiver;
    }

    public Hjemmel getHjemmelForRefusjonskravfrist() {
        return hjemmelForRefusjonskravfrist;
    }

    public BigDecimal getSaksbehandletRefusjonPrÅr() {
        return saksbehandletRefusjonPrÅr;
    }

    public BigDecimal getFordeltRefusjonPrÅr() {
        return fordeltRefusjonPrÅr;
    }

    /**
     * Refusjonskrav settes på forskjellige steder i beregning dersom avklaringsbehov oppstår.
     * Først settes refusjonskravPrÅr, deretter saksbehandletRefusjonPrÅr og til slutt fordeltRefusjonPrÅr.
     * Det er det sist avklarte beløpet som til en hver tid skal være gjeldende.
     * @return returnerer det refusjonskravet som skal være gjeldende
     */
    public BigDecimal getGjeldendeRefusjonPrÅr() {
        if (fordeltRefusjonPrÅr != null) {
            return fordeltRefusjonPrÅr;
        } else if (saksbehandletRefusjonPrÅr != null) {
            return saksbehandletRefusjonPrÅr;
        }
        return refusjonskravPrÅr;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof BGAndelArbeidsforholdDto)) {
            return false;
        }
        BGAndelArbeidsforholdDto other = (BGAndelArbeidsforholdDto) obj;
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

    public static Builder builder(BGAndelArbeidsforholdDto bgAndelArbeidsforhold) {
        return bgAndelArbeidsforhold == null ? new Builder() : new Builder(bgAndelArbeidsforhold);
    }

    public static class Builder {
        private BGAndelArbeidsforholdDto bgAndelArbeidsforhold;
        private boolean erOppdatering;

        private Builder() {
            bgAndelArbeidsforhold = new BGAndelArbeidsforholdDto();
        }

        private Builder(BGAndelArbeidsforholdDto eksisterendeBGAndelArbeidsforhold) {
            bgAndelArbeidsforhold = new BGAndelArbeidsforholdDto(eksisterendeBGAndelArbeidsforhold);
        }

        private Builder(BGAndelArbeidsforholdDto eksisterendeBGAndelArbeidsforhold, boolean erOppdatering) {
            bgAndelArbeidsforhold = eksisterendeBGAndelArbeidsforhold;
            this.erOppdatering = erOppdatering;
        }

        private static Builder oppdater(BGAndelArbeidsforholdDto oppdatere) {
            return new Builder(oppdatere, true);
        }

        static Builder ny() {
            return new Builder(new BGAndelArbeidsforholdDto(), false);
        }

        public static Builder oppdater(Optional<BGAndelArbeidsforholdDto> bgAndelArbeidsforhold) {
            return bgAndelArbeidsforhold.map(Builder::oppdater).orElseGet(Builder::ny);
        }

        public static Builder kopier(BGAndelArbeidsforholdDto bgAndelArbeidsforhold) {
            return new Builder(bgAndelArbeidsforhold);
        }

        public Builder medArbeidsgiver(Arbeidsgiver arbeidsgiver) {
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

        public Builder medSaksbehandletRefusjonPrÅr(BigDecimal saksbehandletRefusjonPrÅr) {
            bgAndelArbeidsforhold.saksbehandletRefusjonPrÅr = saksbehandletRefusjonPrÅr;
            return this;
        }

        public Builder medFordeltRefusjonPrÅr(BigDecimal fordeltRefusjonPrÅr) {
            bgAndelArbeidsforhold.fordeltRefusjonPrÅr = fordeltRefusjonPrÅr;
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

        public Builder medHjemmel(Hjemmel hjemmel) {
            bgAndelArbeidsforhold.hjemmelForRefusjonskravfrist = hjemmel;
            return this;
        }

        BGAndelArbeidsforholdDto build(BeregningsgrunnlagPrStatusOgAndelDto andel) {
            Objects.requireNonNull(bgAndelArbeidsforhold.arbeidsgiver, "arbeidsgiver");
            bgAndelArbeidsforhold.beregningsgrunnlagPrStatusOgAndel = andel;
            return bgAndelArbeidsforhold;
        }

        public BGAndelArbeidsforholdDto build() {
            Objects.requireNonNull(bgAndelArbeidsforhold.arbeidsgiver, "arbeidsgiver");
            return bgAndelArbeidsforhold;
        }
    }
}
