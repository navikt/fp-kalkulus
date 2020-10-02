package no.nav.folketrygdloven.kalkulus.domene.entiteter.sporing;

import java.util.Objects;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.Type;

import no.nav.folketrygdloven.kalkulus.felles.jpa.BaseEntitet;
import no.nav.folketrygdloven.kalkulus.felles.jpa.IntervallEntitet;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagPeriodeRegelType;

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

    @Type(type = "jsonb")
    @Column(name = "regel_evaluering_json")
    private String regelEvaluering;

    @Type(type = "jsonb")
    @Column(name = "regel_input_json")
    private String regelInput;

    @Convert(converter= BeregningsgrunnlagPeriodeRegelType.KodeverdiConverter.class)
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
            kladd.regelInput = regelInput;
            return this;
        }

        public Builder medRegelEvaluering(String regelEvaluering) {
            Objects.requireNonNull(regelEvaluering, "regelInput");
            kladd.regelEvaluering = regelEvaluering;
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
