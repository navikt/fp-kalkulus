package no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag;

import java.time.LocalDate;
import java.util.Objects;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import com.fasterxml.jackson.annotation.JsonBackReference;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Beløp;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Promille;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.mapping.SammenligningsgrunnlagTypeKodeverdiConverter;
import no.nav.folketrygdloven.kalkulus.domene.felles.jpa.BaseEntitet;
import no.nav.folketrygdloven.kalkulus.domene.felles.jpa.IntervallEntitet;
import no.nav.folketrygdloven.kalkulus.kodeverk.SammenligningsgrunnlagType;

@Entity(name = "SammenligningsgrunnlagPrStatusEntitet")
@Table(name = "SAMMENLIGNINGSGRUNNLAG_PR_STATUS")
public class SammenligningsgrunnlagPrStatusEntitet extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GLOBAL_PK_SEQ_GENERATOR")
    private Long id;

    @Version
    @Column(name = "versjon", nullable = false)
    private int versjon;

    @Embedded
    @AttributeOverrides({@AttributeOverride(name = "fomDato", column = @Column(name = "sammenligningsperiode_fom")), @AttributeOverride(name = "tomDato", column = @Column(name = "sammenligningsperiode_tom"))})
    private IntervallEntitet sammenligningsperiode;

    @Convert(converter = SammenligningsgrunnlagTypeKodeverdiConverter.class)
    @Column(name = "sammenligningsgrunnlag_type", nullable = false)
    private SammenligningsgrunnlagType sammenligningsgrunnlagType;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "rapportert_pr_aar", nullable = false)))
    private Beløp rapportertPrÅr;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "avvik_promille", nullable = false)))
    private Promille avvikPromille = Promille.ZERO;

    @JsonBackReference
    @ManyToOne(optional = false)
    @JoinColumn(name = "beregningsgrunnlag_id", nullable = false, updatable = false, unique = true)
    private BeregningsgrunnlagEntitet beregningsgrunnlag;

    public SammenligningsgrunnlagPrStatusEntitet() {
        // Hibernate
    }

    public SammenligningsgrunnlagPrStatusEntitet(SammenligningsgrunnlagPrStatusEntitet sammenligningsgrunnlagPrStatus) {
        this.avvikPromille = sammenligningsgrunnlagPrStatus.getAvvikPromille();
        this.rapportertPrÅr = sammenligningsgrunnlagPrStatus.getRapportertPrÅr();
        this.sammenligningsgrunnlagType = sammenligningsgrunnlagPrStatus.getSammenligningsgrunnlagType();
        this.sammenligningsperiode = sammenligningsgrunnlagPrStatus.sammenligningsperiode;
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

    public Promille getAvvikPromille() {
        return avvikPromille;
    }

    public SammenligningsgrunnlagType getSammenligningsgrunnlagType() {
        return sammenligningsgrunnlagType;
    }

    public BeregningsgrunnlagEntitet getBeregningsgrunnlag() {
        return beregningsgrunnlag;
    }

    void setBeregningsgrunnlag(BeregningsgrunnlagEntitet beregningsgrunnlag) {
        this.beregningsgrunnlag = beregningsgrunnlag;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof SammenligningsgrunnlagPrStatusEntitet)) {
            return false;
        }
        SammenligningsgrunnlagPrStatusEntitet other = (SammenligningsgrunnlagPrStatusEntitet) obj;
        return Objects.equals(this.getBeregningsgrunnlag(), other.getBeregningsgrunnlag()) && Objects.equals(this.getSammenligningsgrunnlagType(),
            other.getSammenligningsgrunnlagType()) && Objects.equals(this.getSammenligningsperiodeFom(), other.getSammenligningsperiodeFom())
            && Objects.equals(this.getSammenligningsperiodeTom(), other.getSammenligningsperiodeTom()) && Objects.equals(this.getAvvikPromille(),
            other.getAvvikPromille()) && Objects.equals(this.getRapportertPrÅr(), other.getRapportertPrÅr());
    }

    @Override
    public int hashCode() {
        return Objects.hash(beregningsgrunnlag, sammenligningsgrunnlagType, sammenligningsperiode, rapportertPrÅr);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + "id=" + id + ", " //$NON-NLS-2$
            + "sammenligningsgrunnlagType=" + sammenligningsgrunnlagType + ", " //$NON-NLS-2$
            + "sammenligningsperiodeFom=" + sammenligningsperiode.getFomDato() + ", " //$NON-NLS-2$
            + "sammenligningsperiodeTom=" + sammenligningsperiode.getTomDato() + ", " //$NON-NLS-2$
            + "rapportertPrÅr=" + rapportertPrÅr + ", " //$NON-NLS-2$
            + "avvikPromille=" + avvikPromille + ", " //$NON-NLS-2$
            + ">";
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private SammenligningsgrunnlagPrStatusEntitet sammenligningsgrunnlagMal;

        public Builder() {
            sammenligningsgrunnlagMal = new SammenligningsgrunnlagPrStatusEntitet();
        }

        public Builder medSammenligningsgrunnlagType(SammenligningsgrunnlagType sammenligningsgrunnlagType) {
            sammenligningsgrunnlagMal.sammenligningsgrunnlagType = sammenligningsgrunnlagType;
            return this;
        }

        public Builder medSammenligningsperiode(LocalDate fom, LocalDate tom) {
            sammenligningsgrunnlagMal.sammenligningsperiode = IntervallEntitet.fraOgMedTilOgMed(fom, tom);
            return this;
        }

        public Builder medRapportertPrÅr(Beløp rapportertPrÅr) {
            sammenligningsgrunnlagMal.rapportertPrÅr = rapportertPrÅr;
            return this;
        }

        public Builder medAvvikPromille(Promille avvikPromille) {
            Objects.requireNonNull(avvikPromille, "avvik");
            sammenligningsgrunnlagMal.avvikPromille = avvikPromille;
            return this;
        }

        public SammenligningsgrunnlagPrStatusEntitet build() {
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
        }
    }

}
