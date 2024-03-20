package no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag;

import java.util.Objects;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import com.fasterxml.jackson.annotation.JsonBackReference;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.mapping.AktivitetStatusKodeverdiConverter;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.mapping.HjemmelKodeverdiConverter;
import no.nav.folketrygdloven.kalkulus.felles.jpa.BaseEntitet;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.Hjemmel;

@Entity(name = "BeregningsgrunnlagAktivitetStatusEntitet")
@Table(name = "BG_AKTIVITET_STATUS")
public class BeregningsgrunnlagAktivitetStatusEntitet extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_BG_AKTIVITET_STATUS")
    private Long id;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    @JsonBackReference
    @ManyToOne(cascade = {CascadeType.PERSIST}, optional = false)
    @JoinColumn(name = "beregningsgrunnlag_id", nullable = false, updatable = false)
    private BeregningsgrunnlagEntitet beregningsgrunnlag;

    @Convert(converter= AktivitetStatusKodeverdiConverter.class)
    @Column(name="aktivitet_status", nullable = false)
    private AktivitetStatus aktivitetStatus;

    @Convert(converter= HjemmelKodeverdiConverter.class)
    @Column(name="hjemmel", nullable = false)
    private Hjemmel hjemmel;


    public BeregningsgrunnlagAktivitetStatusEntitet(BeregningsgrunnlagAktivitetStatusEntitet beregningsgrunnlagAktivitetStatus) {
        this.aktivitetStatus = beregningsgrunnlagAktivitetStatus.getAktivitetStatus();
        this.hjemmel = beregningsgrunnlagAktivitetStatus.getHjemmel();
    }

    protected BeregningsgrunnlagAktivitetStatusEntitet() {
    }


    public Long getId() {
        return id;
    }

    public BeregningsgrunnlagEntitet getBeregningsgrunnlag() {
        return beregningsgrunnlag;
    }

    public AktivitetStatus getAktivitetStatus() {
        return aktivitetStatus;
    }

    public Hjemmel getHjemmel() {
        return hjemmel;
    }

    void setBeregningsgrunnlag(BeregningsgrunnlagEntitet beregningsgrunnlag) {
        this.beregningsgrunnlag = beregningsgrunnlag;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof BeregningsgrunnlagAktivitetStatusEntitet)) {
            return false;
        }
        BeregningsgrunnlagAktivitetStatusEntitet other = (BeregningsgrunnlagAktivitetStatusEntitet) obj;
        return Objects.equals(this.getAktivitetStatus(), other.getAktivitetStatus())
                && Objects.equals(this.getBeregningsgrunnlag(), other.getBeregningsgrunnlag());
    }

    @Override
    public int hashCode() {
        return Objects.hash(beregningsgrunnlag, aktivitetStatus);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + //$NON-NLS-1$
                "id=" + id + ", " //$NON-NLS-2$
                + "beregningsgrunnlag=" + beregningsgrunnlag + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "aktivitetStatus=" + aktivitetStatus + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "hjemmel=" + hjemmel + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + ">"; //$NON-NLS-1$
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private BeregningsgrunnlagAktivitetStatusEntitet beregningsgrunnlagAktivitetStatusMal;

        public Builder() {
            beregningsgrunnlagAktivitetStatusMal = new BeregningsgrunnlagAktivitetStatusEntitet();
            beregningsgrunnlagAktivitetStatusMal.hjemmel = Hjemmel.UDEFINERT;
        }

        public Builder medAktivitetStatus(AktivitetStatus aktivitetStatus) {
            beregningsgrunnlagAktivitetStatusMal.aktivitetStatus = aktivitetStatus;
            return this;
        }

        public Builder medHjemmel(Hjemmel hjemmel) {
            beregningsgrunnlagAktivitetStatusMal.hjemmel = hjemmel;
            return this;
        }

        public BeregningsgrunnlagAktivitetStatusEntitet build(BeregningsgrunnlagEntitet beregningsgrunnlag) {
            verifyStateForBuild();
            beregningsgrunnlag.leggTilBeregningsgrunnlagAktivitetStatus(beregningsgrunnlagAktivitetStatusMal);
            return beregningsgrunnlagAktivitetStatusMal;
        }

        public void verifyStateForBuild() {
            Objects.requireNonNull(beregningsgrunnlagAktivitetStatusMal.aktivitetStatus, "aktivitetStatus");
            Objects.requireNonNull(beregningsgrunnlagAktivitetStatusMal.getHjemmel(), "hjemmel");
        }
    }
}
