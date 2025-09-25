package no.nav.folketrygdloven.kalkulus.domene.entiteter.sporing;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.mapping.BeregningsgrunnlagRegelTypeKodeverdiConverter;
import no.nav.folketrygdloven.kalkulus.felles.jpa.BaseEntitet;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagRegelType;

@Entity(name = "RegelSporingGrunnlagEntitet")
@Table(name = "REGEL_SPORING_GRUNNLAG")
public class RegelSporingGrunnlagEntitet extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GLOBAL_PK_SEQ_GENERATOR")
    private Long id;

    @Version
    @Column(name = "versjon", nullable = false)
    private int versjon;

    @Column(name = "kobling_id", nullable = false, updatable = false)
    private Long koblingId;

    @Column(name = "regel_evaluering_json", columnDefinition = "TEXT")
    private String regelEvaluering;

    @Column(name = "regel_input_json", columnDefinition = "TEXT")
    private String regelInput;

    @Convert(converter = BeregningsgrunnlagRegelTypeKodeverdiConverter.class)
    @Column(name = "regel_type", nullable = false)
    private BeregningsgrunnlagRegelType regelType;

    @Column(name = "regel_versjon")
    private String regelVersjon;

    @Column(name = "aktiv", nullable = false)
    private boolean aktiv = true;

    public RegelSporingGrunnlagEntitet() {
        // Hibernate
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

    public String getRegelVersjon() {
        return regelVersjon;
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
            Objects.requireNonNull(regelEvaluering, "regelEvaluering");
            kladd.regelEvaluering = regelEvaluering;
            return this;
        }

        public Builder medRegelVersjon(String regelVersjon) {
            kladd.regelVersjon = regelVersjon;
            return this;
        }

        public RegelSporingGrunnlagEntitet build(Long koblingId, BeregningsgrunnlagRegelType regelType) {
            Objects.requireNonNull(koblingId, "koblingId");
            Objects.requireNonNull(kladd.regelEvaluering, "regelEvaluering");
            Objects.requireNonNull(regelType, "regelType");
            Objects.requireNonNull(kladd.regelInput, "regelInput");
            kladd.koblingId = koblingId;
            kladd.regelType = regelType;
            kladd.aktiv = true;
            return kladd;
        }

    }


}
