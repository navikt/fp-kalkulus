package no.nav.folketrygdloven.kalkulus.domene.entiteter.sporing;

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
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.mapping.BeregningsgrunnlagPeriodeRegelTypeKodeverdiConverter;
import no.nav.folketrygdloven.kalkulus.felles.jpa.BaseEntitet;
import no.nav.folketrygdloven.kalkulus.felles.jpa.IntervallEntitet;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagPeriodeRegelType;

@Entity(name = "RegelSporingPeriodeEntitet")
@Table(name = "REGEL_SPORING_PERIODE")
public class RegelSporingPeriodeEntitet extends BaseEntitet {

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
    @Deprecated(forRemoval = true)
    private String regelInput;

    @Convert(converter = BeregningsgrunnlagPeriodeRegelTypeKodeverdiConverter.class)
    @Column(name = "regel_type", nullable = false)
    private BeregningsgrunnlagPeriodeRegelType regelType;

    @Column(name = "regel_versjon")
    private String regelVersjon;

    @Embedded
    @AttributeOverrides({@AttributeOverride(name = "fomDato", column = @Column(name = "fom")), @AttributeOverride(name = "tomDato", column = @Column(name = "tom"))})
    private IntervallEntitet periode;

    @Column(name = "aktiv", nullable = false)
    private boolean aktiv = true;

    public RegelSporingPeriodeEntitet() {
        // Hibernate
    }

    public Long getId() {
        return id;
    }

    public Long getKoblingId() {
        return koblingId;
    }

    public String getRegelEvaluering() {
        return regelEvaluering;
    }

    public String getRegelInput() {
        return regelInput;
    }

    public BeregningsgrunnlagPeriodeRegelType getRegelType() {
        return regelType;
    }

    public IntervallEntitet getPeriode() {
        return periode;
    }

    public boolean erAktiv() {
        return aktiv;
    }

    public String getRegelVersjon() {
        return regelVersjon;
    }

    public static Builder ny() {
        return new Builder();
    }

    public static class Builder {

        private RegelSporingPeriodeEntitet kladd;

        Builder() {
            kladd = new RegelSporingPeriodeEntitet();
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

        public Builder medRegelInput(String regelInput) {
            Objects.requireNonNull(regelInput, "regelInput");
            kladd.regelInput = regelInput;
            return this;
        }

        public Builder medPeriode(IntervallEntitet intervall) {
            Objects.requireNonNull(intervall, "intervall");
            kladd.periode = intervall;
            return this;
        }

        public RegelSporingPeriodeEntitet build(Long koblingId, BeregningsgrunnlagPeriodeRegelType regelType) {
            Objects.requireNonNull(koblingId, "koblingId");
            Objects.requireNonNull(regelType, "regelType");
            Objects.requireNonNull(kladd.regelEvaluering, "regelEvaluering");
            Objects.requireNonNull(kladd.periode, "periode");
            kladd.koblingId = koblingId;
            kladd.regelType = regelType;
            return kladd;
        }

    }


}
