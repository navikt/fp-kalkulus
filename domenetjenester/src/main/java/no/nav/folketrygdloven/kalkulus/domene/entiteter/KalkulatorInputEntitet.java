package no.nav.folketrygdloven.kalkulus.domene.entiteter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.ColumnTransformer;

import no.nav.folketrygdloven.kalkulus.felles.jpa.BaseEntitet;

@Entity(name = "KalkulatorInput")
@Table(name = "KALKULATOR_INPUT")
public class KalkulatorInputEntitet extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_KALKULATOR_INPUT")
    private Long id;

    @Column(name = "kobling_id", nullable = false, updatable = false, unique = true)
    private Long koblingId;

    @ColumnTransformer(read = "cast(input as text)", write = "to_jsonb(?::text)")
    @Column(name = "input", nullable = false, updatable = false)
    private String input;

    @Column(name = "aktiv", nullable = false)
    private boolean aktiv = true;

    public KalkulatorInputEntitet() {
        // for hibernate
    }

    public KalkulatorInputEntitet(Long koblingId, String input) {
        this.koblingId = koblingId;
        this.input = input;
    }

    public void setAktiv(boolean aktiv) {
        this.aktiv = aktiv;
    }

    public String getInput() {
        return input;
    }

    public Long getKoblingId() {
        return koblingId;
    }
}
