package no.nav.folketrygdloven.kalkulus.domene.entiteter.sporing;

import static no.nav.folketrygdloven.kalkulus.domene.entiteter.sporing.PayloadUtil.getPayload;

import java.sql.Clob;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.hibernate.annotations.Type;
import org.hibernate.engine.jdbc.ClobProxy;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.mapping.BeregningsgrunnlagPeriodeRegelTypeKodeverdiConverter;
import no.nav.folketrygdloven.kalkulus.felles.diff.DiffIgnore;
import no.nav.folketrygdloven.kalkulus.felles.jpa.BaseEntitet;
import no.nav.folketrygdloven.kalkulus.felles.jpa.IntervallEntitet;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagPeriodeRegelType;

@Entity(name = "RegelSporingPeriodeEntitet")
@Table(name = "REGEL_SPORING_PERIODE")
public class RegelSporingPeriodeEntitet extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_REGEL_SPORING_PERIODE")
    private Long id;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    @Column(name = "kobling_id", nullable = false, updatable = false)
    private Long koblingId;

    @Lob
    @Column(name = "regel_evaluering_json")
    @DiffIgnore
    private Clob regelEvaluering;

    @Transient
    private transient AtomicReference<String> regelEvalueringCached = new AtomicReference<>();

    @Lob
    @Column(name = "regel_input_json")
    @DiffIgnore
    private Clob regelInput;

    @Transient
    private transient AtomicReference<String> regelInputCached = new AtomicReference<>();

    @Convert(converter= BeregningsgrunnlagPeriodeRegelTypeKodeverdiConverter.class)
    @Column(name="regel_type", nullable = false)
    private BeregningsgrunnlagPeriodeRegelType regelType;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "fomDato", column = @Column(name = "fom")),
            @AttributeOverride(name = "tomDato", column = @Column(name = "tom"))
    })
    private IntervallEntitet periode;

    @Column(name = "aktiv", nullable = false)
    private boolean aktiv = true;

    public RegelSporingPeriodeEntitet() {
    }

    public Long getId() {
        return id;
    }

    public Long getKoblingId() {
        return koblingId;
    }

    public String getRegelEvaluering() {
        return getPayload(regelEvaluering, regelEvalueringCached);
    }

    void setRegelEvaluering(String regelEvaluering) {
        if (this.id != null && this.regelEvaluering != null) {
            throw new IllegalStateException("Kan ikke overskrive regelEvaluering for RegelSporingPeriodeEntitet: " + this.id);
        }
        this.regelEvaluering = regelEvaluering == null || regelEvaluering.isEmpty() ? null : ClobProxy.generateProxy(regelEvaluering);
    }

    public String getRegelInput() {
        return getPayload(regelInput, regelInputCached);
    }

    void setRegelInput(String regelInput) {
        if (this.id != null && this.regelInput != null) {
            throw new IllegalStateException("Kan ikke overskrive regelInput for RegelSporingPeriodeEntitet: " + this.id);
        }
        this.regelInput = regelInput == null || regelInput.isEmpty() ? null : ClobProxy.generateProxy(regelInput);
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

    public void setAktiv(boolean aktiv) {
        this.aktiv = aktiv;
    }

    public static Builder ny() {
        return new Builder();
    }

    public static class Builder {

        private RegelSporingPeriodeEntitet kladd;

        Builder() {
            kladd = new RegelSporingPeriodeEntitet();
        }

        public Builder medRegelInput(String regelInput) {
            Objects.requireNonNull(regelInput, "regelInput");
            kladd.setRegelInput(regelInput);
            return this;
        }

        public Builder medRegelEvaluering(String regelEvaluering) {
            Objects.requireNonNull(regelEvaluering, "regelInput");
            kladd.setRegelEvaluering(regelEvaluering);
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
            Objects.requireNonNull(kladd.regelInput, "regelInput");
            Objects.requireNonNull(kladd.periode, "periode");
            kladd.koblingId = koblingId;
            kladd.regelType = regelType;
            return kladd;
        }

    }


}
