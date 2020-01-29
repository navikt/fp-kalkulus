package no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.tid.Intervall;


public class SammenligningsgrunnlagRestDto {

    private Intervall sammenligningsperiode;
    private BigDecimal rapportertPrÅr;
    private Long avvikPromille = 0L;
    private BigDecimal avvikPromilleNy = BigDecimal.ZERO;
    private BeregningsgrunnlagRestDto beregningsgrunnlag;

    public SammenligningsgrunnlagRestDto() {
    }

    public SammenligningsgrunnlagRestDto(SammenligningsgrunnlagRestDto sammenligningsgrunnlag) {
        this.avvikPromille = sammenligningsgrunnlag.getAvvikPromille();
        this.rapportertPrÅr = sammenligningsgrunnlag.getRapportertPrÅr();
        this.sammenligningsperiode = sammenligningsgrunnlag.sammenligningsperiode;
    }

    public LocalDate getSammenligningsperiodeFom() {
        return sammenligningsperiode.getFomDato();
    }

    public LocalDate getSammenligningsperiodeTom() {
        return sammenligningsperiode.getTomDato();
    }

    public BigDecimal getRapportertPrÅr() {
        return rapportertPrÅr;
    }

    public Long getAvvikPromille() {
        return avvikPromille;
    }

    public BigDecimal getAvvikPromilleNy() {
        return avvikPromilleNy;
    }

    public BeregningsgrunnlagRestDto getBeregningsgrunnlag() {
        return beregningsgrunnlag;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof SammenligningsgrunnlagRestDto)) {
            return false;
        }
        SammenligningsgrunnlagRestDto other = (SammenligningsgrunnlagRestDto) obj;
        return Objects.equals(this.getBeregningsgrunnlag(), other.getBeregningsgrunnlag())
                && Objects.equals(this.getSammenligningsperiodeFom(), other.getSammenligningsperiodeFom())
                && Objects.equals(this.getSammenligningsperiodeTom(), other.getSammenligningsperiodeTom())
                && Objects.equals(this.getRapportertPrÅr(), other.getRapportertPrÅr());
    }

    @Override
    public int hashCode() {
        return Objects.hash(beregningsgrunnlag, sammenligningsperiode, rapportertPrÅr, avvikPromille);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + //$NON-NLS-1$
                "beregningsgrunnlag=" + beregningsgrunnlag + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "sammenligningsperiodeFom=" + sammenligningsperiode.getFomDato() + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "sammenligningsperiodeTom=" + sammenligningsperiode.getTomDato() + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "rapportertPrÅr=" + rapportertPrÅr + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "avvikPromille=" + avvikPromille + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + ">"; //$NON-NLS-1$
    }

    public static Builder builder() {
        return new Builder();
    }

    void setBeregningsgrunnlag(BeregningsgrunnlagRestDto beregningsgrunnlagDto) {
        this.beregningsgrunnlag = beregningsgrunnlagDto;
    }

    public static class Builder {
        private SammenligningsgrunnlagRestDto sammenligningsgrunnlagMal;

        public Builder() {
            sammenligningsgrunnlagMal = new SammenligningsgrunnlagRestDto();
        }

        public Builder(SammenligningsgrunnlagRestDto sammenligningsgrunnlag) {
            sammenligningsgrunnlagMal = new SammenligningsgrunnlagRestDto(sammenligningsgrunnlag);
        }

        public static Builder kopi(SammenligningsgrunnlagRestDto sammenligningsgrunnlag) {
            return new Builder(sammenligningsgrunnlag);
        }

        public Builder medSammenligningsperiode(LocalDate fom, LocalDate tom) {
            sammenligningsgrunnlagMal.sammenligningsperiode = Intervall.fraOgMedTilOgMed(fom, tom);
            return this;
        }

        public Builder medRapportertPrÅr(BigDecimal rapportertPrÅr) {
            sammenligningsgrunnlagMal.rapportertPrÅr = rapportertPrÅr;
            return this;
        }

        public Builder medAvvikPromille(Long avvikPromille) {
            if(avvikPromille != null) {
                sammenligningsgrunnlagMal.avvikPromille = avvikPromille;
            }
            return this;
        }

        public Builder medAvvikPromilleNy(BigDecimal avvikPromille) {
            if(avvikPromille != null) {
                sammenligningsgrunnlagMal.avvikPromilleNy = avvikPromille;
            }
            return this;
        }

        public SammenligningsgrunnlagRestDto build(BeregningsgrunnlagRestDto beregningsgrunnlag) {
            sammenligningsgrunnlagMal.beregningsgrunnlag = beregningsgrunnlag;
            verifyStateForBuild();
            BeregningsgrunnlagRestDto.Builder.oppdater(Optional.of(beregningsgrunnlag))
                .medSammenligningsgrunnlag(sammenligningsgrunnlagMal);
            return sammenligningsgrunnlagMal;
        }

        public SammenligningsgrunnlagRestDto build() {
            verifyStateForBuild();
            return sammenligningsgrunnlagMal;
        }

        public void verifyStateForBuild() {
            Objects.requireNonNull(sammenligningsgrunnlagMal.beregningsgrunnlag, "beregningsgrunnlag");
            Objects.requireNonNull(sammenligningsgrunnlagMal.sammenligningsperiode, "sammenligningsperiodePeriode");
            Objects.requireNonNull(sammenligningsgrunnlagMal.sammenligningsperiode.getFomDato(), "sammenligningsperiodeFom");
            Objects.requireNonNull(sammenligningsgrunnlagMal.sammenligningsperiode.getTomDato(), "sammenligningsperiodeTom");
            Objects.requireNonNull(sammenligningsgrunnlagMal.rapportertPrÅr, "rapportertPrÅr");
            Objects.requireNonNull(sammenligningsgrunnlagMal.avvikPromille, "avvikPromille");
        }
    }

}
