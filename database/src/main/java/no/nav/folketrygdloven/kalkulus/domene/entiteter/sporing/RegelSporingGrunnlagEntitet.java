package no.nav.folketrygdloven.kalkulus.domene.entiteter.sporing;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.PersistenceException;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.engine.jdbc.ClobProxy;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.felles.jpa.BaseEntitet;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagPeriodeRegelType;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagRegelType;

@Entity(name = "RegelSporingGrunnlagEntitet")
@Table(name = "REGEL_SPORING_GRUNNLAG")
public class RegelSporingGrunnlagEntitet extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_REGEL_SPORING_GRUNNLAG")
    private Long id;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    @JoinColumn(name = "kobling_id", nullable = false, updatable = false)
    private Long koblingId;

    @Lob
    @Column(name = "regel_evaluering", nullable = false)
    private Clob regelEvaluering;

    @Lob
    @Column(name = "regel_input", nullable = false)
    private Clob regelInput;

    @Convert(converter= BeregningsgrunnlagRegelType.KodeverdiConverter.class)
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
        return getStringFromLob(this.regelEvaluering);
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

    public String getRegelInput() {
        return getStringFromLob(regelInput);
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

    public static RegelSporingPeriodeEntitet.Builder ny() {
        return new RegelSporingPeriodeEntitet.Builder();
    }

    public static class Builder {

        private RegelSporingGrunnlagEntitet kladd;

        Builder() {
            kladd = new RegelSporingGrunnlagEntitet();
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

        public Builder medRegelType(BeregningsgrunnlagRegelType regelType) {
            Objects.requireNonNull(regelType, "regelType");
            kladd.regelType = regelType;
            return this;
        }

        public RegelSporingGrunnlagEntitet build() {
            Objects.requireNonNull(kladd.koblingId, "koblingId");
            Objects.requireNonNull(kladd.regelEvaluering, "regelEvaluering");
            Objects.requireNonNull(kladd.regelInput, "regelInput");
            Objects.requireNonNull(kladd.regelType, "regelType");
            kladd.aktiv = true;
            return kladd;
        }

    }


}
