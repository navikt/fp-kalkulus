package no.nav.folketrygdloven.kalkulus.domene.entiteter.sporing;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Clob;
import java.sql.SQLException;
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
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.PersistenceException;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.engine.jdbc.ClobProxy;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
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

    @JoinColumn(name = "kobling_id", nullable = false, updatable = false)
    private Long koblingId;

    @Lob
    @Column(name = "regel_evaluering")
    private Clob regelEvaluering;

    @Lob
    @Column(name = "regel_input", nullable = false)
    private Clob regelInput;

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
        return getStringFromLob(regelEvaluering);
    }

    public String getRegelInput() {
        return getStringFromLob(regelInput);
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

    private String getStringFromLob(Clob clob) {
        if (clob == null) {
            return null;
        }
        String string;
        try {
            BufferedReader in = new BufferedReader(clob.getCharacterStream());
            String line;
            StringBuilder sb = new StringBuilder(2048);
            while ((line = in.readLine()) != null) {
                sb.append(line);
            }
            string = sb.toString();
        } catch (SQLException | IOException e) {
            throw new PersistenceException("Kunne ikke konvertere clob til string: ", e);
        }
        return string;
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

        public Builder medKobling(Long koblingId) {
            kladd.koblingId = koblingId;
            return this;
        }

        public Builder medRegelInput(String regelInput) {
            Objects.requireNonNull(regelInput, "regelInput");
            kladd.regelInput = ClobProxy.generateProxy(regelInput);
            return this;
        }

        public Builder medRegelEvaluering(String regelEvaluering) {
            Objects.requireNonNull(regelEvaluering, "regelInput");
            kladd.regelEvaluering = ClobProxy.generateProxy(regelEvaluering);
            return this;
        }

        public Builder medRegelType(BeregningsgrunnlagPeriodeRegelType regelType) {
            Objects.requireNonNull(regelType, "regelType");
            kladd.regelType = regelType;
            return this;
        }

        public RegelSporingPeriodeEntitet build() {
            Objects.requireNonNull(kladd.koblingId, "koblingId");
            Objects.requireNonNull(kladd.regelEvaluering, "regelEvaluering");
            Objects.requireNonNull(kladd.regelInput, "regelInput");
            Objects.requireNonNull(kladd.regelType, "regelType");
            return kladd;
        }

    }


}
