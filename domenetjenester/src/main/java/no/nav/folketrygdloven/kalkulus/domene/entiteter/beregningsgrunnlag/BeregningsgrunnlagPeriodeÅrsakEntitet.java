package no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag;


import java.util.Objects;

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

import no.nav.folketrygdloven.kalkulus.domene.entiteter.mapping.PeriodeÅrsakKodeverdiConverter;
import no.nav.folketrygdloven.kalkulus.felles.jpa.BaseEntitet;
import no.nav.folketrygdloven.kalkulus.kodeverk.PeriodeÅrsak;

@Entity(name = "BeregningsgrunnlagPeriodeÅrsakEntitet")
@Table(name = "BG_PERIODE_AARSAK")
public class BeregningsgrunnlagPeriodeÅrsakEntitet extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_BG_PERIODE_AARSAK")
    private Long id;

    @Version
    @Column(name = "versjon", nullable = false)
    private int versjon;

    @JsonBackReference
    @ManyToOne(optional = false)
    @JoinColumn(name = "bg_periode_id", nullable = false, updatable = false)
    private BeregningsgrunnlagPeriodeEntitet beregningsgrunnlagPeriode;

    @Convert(converter = PeriodeÅrsakKodeverdiConverter.class)
    @Column(name="periode_aarsak", nullable = false)
    private PeriodeÅrsak periodeÅrsak = PeriodeÅrsak.UDEFINERT;

    public BeregningsgrunnlagPeriodeÅrsakEntitet(BeregningsgrunnlagPeriodeÅrsakEntitet beregningsgrunnlagPeriodeÅrsak) {
        this.periodeÅrsak = beregningsgrunnlagPeriodeÅrsak.periodeÅrsak;
    }

    public BeregningsgrunnlagPeriodeÅrsakEntitet() {
    }

    public Long getId() {
        return id;
    }

    public BeregningsgrunnlagPeriodeEntitet getBeregningsgrunnlagPeriode() {
        return beregningsgrunnlagPeriode;
    }

    public PeriodeÅrsak getPeriodeÅrsak() {
        return periodeÅrsak;
    }


    void setBeregningsgrunnlagPeriode(BeregningsgrunnlagPeriodeEntitet beregningsgrunnlagPeriode) {
        this.beregningsgrunnlagPeriode = beregningsgrunnlagPeriode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(beregningsgrunnlagPeriode, periodeÅrsak);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof BeregningsgrunnlagPeriodeÅrsakEntitet)) {
            return false;
        }
        BeregningsgrunnlagPeriodeÅrsakEntitet other = (BeregningsgrunnlagPeriodeÅrsakEntitet) obj;
        return Objects.equals(this.getBeregningsgrunnlagPeriode(), other.getBeregningsgrunnlagPeriode())
                && Objects.equals(this.getPeriodeÅrsak(), other.getPeriodeÅrsak());
    }

    public static class Builder {
        private BeregningsgrunnlagPeriodeÅrsakEntitet beregningsgrunnlagPeriodeÅrsakMal;

        public Builder() {
            beregningsgrunnlagPeriodeÅrsakMal = new BeregningsgrunnlagPeriodeÅrsakEntitet();
        }

        public Builder medPeriodeÅrsak(PeriodeÅrsak periodeÅrsak) {
            beregningsgrunnlagPeriodeÅrsakMal.periodeÅrsak = periodeÅrsak;
            return this;
        }

        public BeregningsgrunnlagPeriodeÅrsakEntitet build() {
            return beregningsgrunnlagPeriodeÅrsakMal;
        }
    }
}
