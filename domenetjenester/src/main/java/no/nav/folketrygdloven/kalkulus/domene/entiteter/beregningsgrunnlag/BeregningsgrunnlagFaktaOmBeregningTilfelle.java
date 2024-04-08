package no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import com.fasterxml.jackson.annotation.JsonBackReference;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.mapping.FaktaOmBeregningTilfelleKodeverdiConverter;
import no.nav.folketrygdloven.kalkulus.felles.jpa.BaseEntitet;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;

@Entity(name = "BeregningsgrunnlagFaktaOmBeregningTilfelle")
@Table(name = "BG_FAKTA_BER_TILFELLE")
public class BeregningsgrunnlagFaktaOmBeregningTilfelle extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_BG_FAKTA_BER_TILFELLE")
    private Long id;

    @Version
    @Column(name = "versjon", nullable = false)
    private int versjon;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "BEREGNINGSGRUNNLAG_ID", nullable = false, updatable = false, unique = true)
    private BeregningsgrunnlagEntitet beregningsgrunnlag;

    @Convert(converter= FaktaOmBeregningTilfelleKodeverdiConverter.class)
    @Column(name="fakta_beregning_tilfelle", nullable = false)
    private FaktaOmBeregningTilfelle faktaOmBeregningTilfelle = FaktaOmBeregningTilfelle.UDEFINERT;


    // kun til bruk for kopiering (bruk Builder ved opprettelse)
    BeregningsgrunnlagFaktaOmBeregningTilfelle(FaktaOmBeregningTilfelle faktaOmBeregningTilfelle) {
        this.faktaOmBeregningTilfelle = faktaOmBeregningTilfelle;
    }

    protected BeregningsgrunnlagFaktaOmBeregningTilfelle() {
    }

    public FaktaOmBeregningTilfelle getFaktaOmBeregningTilfelle() {
        return faktaOmBeregningTilfelle;
    }

    void setBeregningsgrunnlag(BeregningsgrunnlagEntitet beregningsgrunnlag) {
        this.beregningsgrunnlag = beregningsgrunnlag;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BeregningsgrunnlagFaktaOmBeregningTilfelle)) {
            return false;
        }
        BeregningsgrunnlagFaktaOmBeregningTilfelle that = (BeregningsgrunnlagFaktaOmBeregningTilfelle) o;
        return Objects.equals(beregningsgrunnlag, that.beregningsgrunnlag) &&
                Objects.equals(faktaOmBeregningTilfelle, that.faktaOmBeregningTilfelle);
    }

    @Override
    public int hashCode() {
        return Objects.hash(beregningsgrunnlag, faktaOmBeregningTilfelle);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private BeregningsgrunnlagFaktaOmBeregningTilfelle beregningsgrunnlagFaktaOmBeregningTilfelle;

        public Builder() {
            beregningsgrunnlagFaktaOmBeregningTilfelle = new BeregningsgrunnlagFaktaOmBeregningTilfelle();
        }

        BeregningsgrunnlagFaktaOmBeregningTilfelle.Builder medFaktaOmBeregningTilfelle(FaktaOmBeregningTilfelle tilfelle) {
            beregningsgrunnlagFaktaOmBeregningTilfelle.faktaOmBeregningTilfelle = tilfelle;
            return this;
        }

        public BeregningsgrunnlagFaktaOmBeregningTilfelle build(BeregningsgrunnlagEntitet beregningsgrunnlag) {
            beregningsgrunnlag.leggTilFaktaOmBeregningTilfelle(beregningsgrunnlagFaktaOmBeregningTilfelle);
            return beregningsgrunnlagFaktaOmBeregningTilfelle;
        }
    }
}
