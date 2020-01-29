package no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.SammenligningsgrunnlagType;
import no.nav.folketrygdloven.kalkulus.felles.tid.DatoIntervallEntitet;


public class SammenligningsgrunnlagPrStatusDto {

    private DatoIntervallEntitet sammenligningsperiode;
    private SammenligningsgrunnlagType sammenligningsgrunnlagType;
    private BigDecimal rapportertPrÅr;
    private Long avvikPromille = 0L;
    private BigDecimal avvikPromilleNy = BigDecimal.ZERO;
    private BeregningsgrunnlagDto beregningsgrunnlag;

    public SammenligningsgrunnlagPrStatusDto() {
    }

    public SammenligningsgrunnlagPrStatusDto(SammenligningsgrunnlagPrStatusDto fraKopi) {
        this.sammenligningsperiode = fraKopi.sammenligningsperiode;
        this.sammenligningsgrunnlagType = fraKopi.sammenligningsgrunnlagType;
        this.rapportertPrÅr = fraKopi.rapportertPrÅr;
        this.avvikPromille = fraKopi.avvikPromille;
        this.avvikPromilleNy = fraKopi.getAvvikPromilleNy();
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

    public SammenligningsgrunnlagType getSammenligningsgrunnlagType() {
        return sammenligningsgrunnlagType;
    }

    public BeregningsgrunnlagDto getBeregningsgrunnlag() {
        return beregningsgrunnlag;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof SammenligningsgrunnlagPrStatusDto)) {
            return false;
        }
        SammenligningsgrunnlagPrStatusDto other = (SammenligningsgrunnlagPrStatusDto) obj;
        return Objects.equals(this.getBeregningsgrunnlag(), other.getBeregningsgrunnlag())
                && Objects.equals(this.getSammenligningsgrunnlagType(), other.getSammenligningsgrunnlagType())
                && Objects.equals(this.getSammenligningsperiodeFom(), other.getSammenligningsperiodeFom())
                && Objects.equals(this.getSammenligningsperiodeTom(), other.getSammenligningsperiodeTom())
                && Objects.equals(this.getRapportertPrÅr(), other.getRapportertPrÅr());
    }

    @Override
    public int hashCode() {
        return Objects.hash(beregningsgrunnlag, sammenligningsgrunnlagType, sammenligningsperiode, rapportertPrÅr, avvikPromille);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + //$NON-NLS-1$
                "sammenligningsgrunnlagType=" + sammenligningsgrunnlagType + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "sammenligningsperiodeFom=" + sammenligningsperiode.getFomDato() + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "sammenligningsperiodeTom=" + sammenligningsperiode.getTomDato() + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "rapportertPrÅr=" + rapportertPrÅr + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "avvikPromille=" + avvikPromille + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + ">"; //$NON-NLS-1$
    }

    public static Builder builder() {
        return new Builder();
    }

    void setBeregningsgrunnlagDto(BeregningsgrunnlagDto beregningsgrunnlagDto) {
        this.beregningsgrunnlag = beregningsgrunnlagDto;
    }

    public static class Builder {

        private SammenligningsgrunnlagPrStatusDto sammenligningsgrunnlagMal;

        public static Builder kopier(SammenligningsgrunnlagPrStatusDto s) {
            return new Builder(s);
        }

        public Builder(SammenligningsgrunnlagPrStatusDto s) {
            sammenligningsgrunnlagMal = new SammenligningsgrunnlagPrStatusDto(s);
        }

        public Builder() {
            sammenligningsgrunnlagMal = new SammenligningsgrunnlagPrStatusDto();
        }

        public Builder medSammenligningsgrunnlagType(SammenligningsgrunnlagType sammenligningsgrunnlagType) {
            sammenligningsgrunnlagMal.sammenligningsgrunnlagType = sammenligningsgrunnlagType;
            return this;
        }

        public Builder medSammenligningsperiode(LocalDate fom, LocalDate tom) {
            sammenligningsgrunnlagMal.sammenligningsperiode = DatoIntervallEntitet.fraOgMedTilOgMed(fom, tom);
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

        public Builder medAvvikPromilleNy(BigDecimal avvikPromilleUtenAvrunding) {
            if(avvikPromilleUtenAvrunding != null) {
                sammenligningsgrunnlagMal.avvikPromilleNy = avvikPromilleUtenAvrunding;
            }
            return this;
        }

        Builder medBeregningsgrunnlag(BeregningsgrunnlagDto beregningsgrunnlag) {
            sammenligningsgrunnlagMal.beregningsgrunnlag = beregningsgrunnlag;
            return this;
        }

        SammenligningsgrunnlagPrStatusDto build() {
            verifyStateForBuild();
            return sammenligningsgrunnlagMal;
        }

        public void verifyStateForBuild() {
            Objects.requireNonNull(sammenligningsgrunnlagMal.sammenligningsgrunnlagType, "sammenligningsgrunnlagType");
            Objects.requireNonNull(sammenligningsgrunnlagMal.sammenligningsperiode, "sammenligningsperiodePeriode");
            Objects.requireNonNull(sammenligningsgrunnlagMal.sammenligningsperiode.getFomDato(), "sammenligningsperiodeFom");
            Objects.requireNonNull(sammenligningsgrunnlagMal.sammenligningsperiode.getTomDato(), "sammenligningsperiodeTom");
            Objects.requireNonNull(sammenligningsgrunnlagMal.rapportertPrÅr, "rapportertPrÅr");
            Objects.requireNonNull(sammenligningsgrunnlagMal.avvikPromille, "avvikPromille");
            if (sammenligningsgrunnlagMal.beregningsgrunnlag.getSammenligningsgrunnlagPrStatusListe().stream().anyMatch(sg -> sg.sammenligningsgrunnlagType.equals(sammenligningsgrunnlagMal.sammenligningsgrunnlagType))) {
                throw new IllegalArgumentException("Kan ikke legge til sammenligningsgrunnlag for " + sammenligningsgrunnlagMal.sammenligningsgrunnlagType + " fordi det allerede er lagt til.");
            }
        }
    }

}
