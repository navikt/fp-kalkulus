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

@Entity(name = "BeregningsgrunnlagPeriodeÅrsak")
@Table(name = "BG_PERIODE_AARSAK")
public class BeregningsgrunnlagPeriodeÅrsak extends BaseEntitet {

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

    public BeregningsgrunnlagPeriodeÅrsak(BeregningsgrunnlagPeriodeÅrsak beregningsgrunnlagPeriodeÅrsak) {
        this.periodeÅrsak = beregningsgrunnlagPeriodeÅrsak.periodeÅrsak;
    }

    public BeregningsgrunnlagPeriodeÅrsak() {
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
        } else if (!(obj instanceof BeregningsgrunnlagPeriodeÅrsak)) {
            return false;
        }
        BeregningsgrunnlagPeriodeÅrsak other = (BeregningsgrunnlagPeriodeÅrsak) obj;
        return Objects.equals(this.getBeregningsgrunnlagPeriode(), other.getBeregningsgrunnlagPeriode())
                && Objects.equals(this.getPeriodeÅrsak(), other.getPeriodeÅrsak());
    }

    public static class Builder {
        private BeregningsgrunnlagPeriodeÅrsak beregningsgrunnlagPeriodeÅrsakMal;

        public Builder() {
            beregningsgrunnlagPeriodeÅrsakMal = new BeregningsgrunnlagPeriodeÅrsak();
        }

        public Builder medPeriodeÅrsak(PeriodeÅrsak periodeÅrsak) {
            beregningsgrunnlagPeriodeÅrsakMal.periodeÅrsak = periodeÅrsak;
            return this;
        }

        public BeregningsgrunnlagPeriodeÅrsak build() {
            return beregningsgrunnlagPeriodeÅrsakMal;
        }
    }
}
