package no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;

import no.nav.folketrygdloven.kalkulus.felles.jpa.BaseEntitet;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagPeriodeRegelType;

@Entity(name = "BeregningsgrunnlagPeriodeRegelSporing")
@TypeDef(
        name = "jsonb",
        typeClass = JsonBinaryType.class
)
@Table(name = "BG_PERIODE_REGEL_SPORING")
public class BeregningsgrunnlagPeriodeRegelSporing extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_BG_PERIODE_REGEL_SPORING")
    private Long id;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    @JsonBackReference
    @ManyToOne(optional = false)
    @JoinColumn(name = "bg_periode_id", nullable = false, updatable = false)
    private BeregningsgrunnlagPeriode beregningsgrunnlagPeriode;

    @Type(type = "jsonb")
    @Column(name = "regel_evaluering")
    private String regelEvaluering;

    @Type(type = "jsonb")
    @Column(name = "regel_input")
    private String regelInput;

    @Convert(converter= BeregningsgrunnlagPeriodeRegelType.KodeverdiConverter.class)
    @Column(name="regel_type", nullable = false)
    private BeregningsgrunnlagPeriodeRegelType regelType;

    public Long getId() {
        return id;
    }

    public BeregningsgrunnlagPeriodeRegelType getRegelType() {
        return regelType;
    }

    public String getRegelEvaluering() {
        return regelEvaluering;
    }

    public String getRegelInput() {
        return regelInput;
    }

    static Builder ny() {
        return new Builder();
    }

    static class Builder {
        private BeregningsgrunnlagPeriodeRegelSporing beregningsgrunnlagPeriodeRegelSporingMal;

        public Builder() {
            beregningsgrunnlagPeriodeRegelSporingMal = new BeregningsgrunnlagPeriodeRegelSporing();
        }

        Builder medRegelInput(String regelInput) {
            beregningsgrunnlagPeriodeRegelSporingMal.regelInput = regelInput;
            return this;
        }

        Builder medRegelEvaluering(String regelEvaluering) {
            beregningsgrunnlagPeriodeRegelSporingMal.regelEvaluering = regelEvaluering;
            return this;
        }

        Builder medRegelType(BeregningsgrunnlagPeriodeRegelType regelType) {
            beregningsgrunnlagPeriodeRegelSporingMal.regelType = regelType;
            return this;
        }

        BeregningsgrunnlagPeriodeRegelSporing build(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode) {
            beregningsgrunnlagPeriodeRegelSporingMal.beregningsgrunnlagPeriode = beregningsgrunnlagPeriode;
            beregningsgrunnlagPeriode.leggTilBeregningsgrunnlagPeriodeRegel(beregningsgrunnlagPeriodeRegelSporingMal);
            return beregningsgrunnlagPeriodeRegelSporingMal;
        }
    }

}
