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

@Entity(name = "PeriodeÅrsakEntitet")
@Table(name = "PERIODE_AARSAK")
public class PeriodeÅrsakEntitet extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GLOBAL_PK_SEQ_GENERATOR")
    private Long id;

    @Version
    @Column(name = "versjon", nullable = false)
    private int versjon;

    @JsonBackReference
    @ManyToOne(optional = false)
    @JoinColumn(name = "beregningsgrunnlag_periode_id", nullable = false, updatable = false)
    private BeregningsgrunnlagPeriodeEntitet beregningsgrunnlagPeriode;

    @Convert(converter = PeriodeÅrsakKodeverdiConverter.class)
    @Column(name = "periode_aarsak", nullable = false)
    private PeriodeÅrsak periodeÅrsak = PeriodeÅrsak.UDEFINERT;

    public PeriodeÅrsakEntitet(PeriodeÅrsakEntitet beregningsgrunnlagPeriodeÅrsak) {
        this.periodeÅrsak = beregningsgrunnlagPeriodeÅrsak.periodeÅrsak;
    }

    public PeriodeÅrsakEntitet() {
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
        } else if (!(obj instanceof PeriodeÅrsakEntitet)) {
            return false;
        }
        PeriodeÅrsakEntitet other = (PeriodeÅrsakEntitet) obj;
        return Objects.equals(this.getBeregningsgrunnlagPeriode(), other.getBeregningsgrunnlagPeriode()) && Objects.equals(this.getPeriodeÅrsak(),
            other.getPeriodeÅrsak());
    }

    public static class Builder {
        private PeriodeÅrsakEntitet beregningsgrunnlagPeriodeÅrsakMal;

        public Builder() {
            beregningsgrunnlagPeriodeÅrsakMal = new PeriodeÅrsakEntitet();
        }

        public Builder medPeriodeÅrsak(PeriodeÅrsak periodeÅrsak) {
            beregningsgrunnlagPeriodeÅrsakMal.periodeÅrsak = periodeÅrsak;
            return this;
        }

        public PeriodeÅrsakEntitet build() {
            return beregningsgrunnlagPeriodeÅrsakMal;
        }
    }
}
