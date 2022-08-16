package no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag;

import java.time.LocalDate;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Beløp;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Promille;
import no.nav.folketrygdloven.kalkulus.felles.jpa.BaseEntitet;
import no.nav.folketrygdloven.kalkulus.felles.jpa.IntervallEntitet;

@Entity(name = "Sammenligningsgrunnlag")
@Table(name = "SAMMENLIGNINGSGRUNNLAG")
public class Sammenligningsgrunnlag extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_SAMMENLIGNINGSGRUNNLAG")
    private Long id;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "fomDato", column = @Column(name = "sammenligningsperiode_fom")),
        @AttributeOverride(name = "tomDato", column = @Column(name = "sammenligningsperiode_tom"))
    })
    private IntervallEntitet sammenligningsperiode;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "rapportert_pr_aar", nullable = false)))
    private Beløp rapportertPrÅr;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "avvik_promille_ny", nullable = false)))
    private Promille avvikPromilleNy = Promille.ZERO;

    @JsonBackReference
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "beregningsgrunnlag_id", nullable = false, updatable = false, unique = true)
    private BeregningsgrunnlagEntitet beregningsgrunnlag;

    public Sammenligningsgrunnlag() {
    }

    public Sammenligningsgrunnlag(Sammenligningsgrunnlag sammenligningsgrunnlag) {
        this.avvikPromilleNy = sammenligningsgrunnlag.getAvvikPromilleNy();
        this.rapportertPrÅr = sammenligningsgrunnlag.getRapportertPrÅr();
        this.sammenligningsperiode = sammenligningsgrunnlag.getSammenligningsperiode();
    }

    public Long getId() {
        return id;
    }

    public LocalDate getSammenligningsperiodeFom() {
        return sammenligningsperiode.getFomDato();
    }

    public LocalDate getSammenligningsperiodeTom() {
        return sammenligningsperiode.getTomDato();
    }

    public Beløp getRapportertPrÅr() {
        return rapportertPrÅr;
    }

    public Promille getAvvikPromilleNy() {
        return avvikPromilleNy;
    }

    public BeregningsgrunnlagEntitet getBeregningsgrunnlag() {
        return beregningsgrunnlag;
    }

    public IntervallEntitet getSammenligningsperiode() {
        return sammenligningsperiode;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof Sammenligningsgrunnlag)) {
            return false;
        }
        Sammenligningsgrunnlag other = (Sammenligningsgrunnlag) obj;
        return Objects.equals(this.getBeregningsgrunnlag(), other.getBeregningsgrunnlag())
                && Objects.equals(this.getSammenligningsperiodeFom(), other.getSammenligningsperiodeFom())
                && Objects.equals(this.getSammenligningsperiodeTom(), other.getSammenligningsperiodeTom())
                && Objects.equals(this.getRapportertPrÅr(), other.getRapportertPrÅr());
    }

    @Override
    public int hashCode() {
        return Objects.hash(beregningsgrunnlag, sammenligningsperiode, rapportertPrÅr, avvikPromilleNy);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + //$NON-NLS-1$
                "id=" + id + ", " //$NON-NLS-2$
                + "beregningsgrunnlag=" + beregningsgrunnlag + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "sammenligningsperiodeFom=" + sammenligningsperiode.getFomDato() + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "sammenligningsperiodeTom=" + sammenligningsperiode.getTomDato() + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "rapportertPrÅr=" + rapportertPrÅr + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "avvikPromille=" + avvikPromilleNy + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + ">"; //$NON-NLS-1$
    }

    void setBeregningsgrunnlag(BeregningsgrunnlagEntitet beregningsgrunnlag) {
        this.beregningsgrunnlag = beregningsgrunnlag;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Sammenligningsgrunnlag sammenligningsgrunnlagMal;

        public Builder() {
            sammenligningsgrunnlagMal = new Sammenligningsgrunnlag();
        }

        public Builder medSammenligningsperiode(LocalDate fom, LocalDate tom) {
            sammenligningsgrunnlagMal.sammenligningsperiode = IntervallEntitet.fraOgMedTilOgMed(fom, tom);
            return this;
        }

        public Builder medRapportertPrÅr(Beløp rapportertPrÅr) {
            sammenligningsgrunnlagMal.rapportertPrÅr = rapportertPrÅr;
            return this;
        }

        public Builder medAvvikPromilleNy(Promille avvikPromille) {
            if(avvikPromille != null) {
                sammenligningsgrunnlagMal.avvikPromilleNy = avvikPromille;
            }
            return this;
        }

        public Sammenligningsgrunnlag build(BeregningsgrunnlagEntitet beregningsgrunnlag) {
            verifyStateForBuild();
            beregningsgrunnlag.setSammenligningsgrunnlag(sammenligningsgrunnlagMal);
            return sammenligningsgrunnlagMal;
        }

        public Sammenligningsgrunnlag build() {
            verifyStateForBuild();
            return sammenligningsgrunnlagMal;
        }

        public void verifyStateForBuild() {
            Objects.requireNonNull(sammenligningsgrunnlagMal.sammenligningsperiode, "sammenligningsperiodePeriode");
            Objects.requireNonNull(sammenligningsgrunnlagMal.sammenligningsperiode.getFomDato(), "sammenligningsperiodeFom");
            Objects.requireNonNull(sammenligningsgrunnlagMal.sammenligningsperiode.getTomDato(), "sammenligningsperiodeTom");
            Objects.requireNonNull(sammenligningsgrunnlagMal.rapportertPrÅr, "rapportertPrÅr");
            Objects.requireNonNull(sammenligningsgrunnlagMal.avvikPromilleNy, "avvikPromille");
        }
    }

}
