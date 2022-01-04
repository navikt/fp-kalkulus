package no.nav.folketrygdloven.kalkulus.domene.entiteter.sporing;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.Type;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.mapping.BeregningsgrunnlagRegelTypeKodeverdiConverter;
import no.nav.folketrygdloven.kalkulus.felles.jpa.BaseEntitet;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagRegelType;

@Entity(name = "RegelSporingGrunnlagEntitet")
@Table(name = "REGEL_SPORING_GRUNNLAG")
public class RegelSporingGrunnlagEntitet extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_REGEL_SPORING_GRUNNLAG")
    private Long id;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    @Column(name = "kobling_id", nullable = false, updatable = false)
    private Long koblingId;

    @Type(type = "jsonb")
    @Column(name = "regel_evaluering_json")
    private String regelEvaluering;

    @Type(type = "jsonb")
    @Column(name = "regel_input_json")
    private String regelInput;

    @Convert(converter= BeregningsgrunnlagRegelTypeKodeverdiConverter.class)
    @Column(name="regel_type", nullable = false)
    private BeregningsgrunnlagRegelType regelType;

    @Column(name = "aktiv", nullable = false)
    private boolean aktiv = true;

    public RegelSporingGrunnlagEntitet() {
    }

    public Long getId() {
        return id;
    }

    public Long getKoblingId() {
        return koblingId;
    }

    public String getRegelEvaluering() {
        return this.regelEvaluering;
    }

    public String getRegelInput() {
        return regelInput;
    }

    public BeregningsgrunnlagRegelType getRegelType() {
        return regelType;
    }

    public boolean erAktiv() {
        return aktiv;
    }

    public void setAktiv(boolean aktiv) {
        this.aktiv = aktiv;
    }

    public static Builder ny() {
        return new Builder();
    }

    public static class Builder {

        private RegelSporingGrunnlagEntitet kladd;

        Builder() {
            kladd = new RegelSporingGrunnlagEntitet();
        }

        public Builder medRegelInput(String regelInput) {
            Objects.requireNonNull(regelInput, "regelInput");
            kladd.regelInput = regelInput;
            return this;
        }

        public Builder medRegelEvaluering(String regelEvaluering) {
            Objects.requireNonNull(regelEvaluering, "regelInput");
            kladd.regelEvaluering = regelEvaluering;
            return this;
        }

        public RegelSporingGrunnlagEntitet build(Long koblingId, BeregningsgrunnlagRegelType regelType) {
            Objects.requireNonNull(koblingId, "koblingId");
            Objects.requireNonNull(regelType, "regelType");
            Objects.requireNonNull(kladd.regelEvaluering, "regelEvaluering");
            Objects.requireNonNull(kladd.regelInput, "regelInput");
            kladd.koblingId = koblingId;
            kladd.regelType = regelType;
            kladd.aktiv = true;
            return kladd;
        }

    }


}
