package no.nav.folketrygdloven.kalkulus.domene.entiteter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;

import no.nav.folketrygdloven.kalkulus.felles.jpa.BaseEntitet;


@Entity(name = "KalkulatorInput")
@Table(name = "KALKULATOR_INPUT")
@TypeDef(
        name = "jsonb",
        typeClass = JsonBinaryType.class
)
public class KalkulatorInputEntitet extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_KALKULATOR_INPUT")
    private Long id;

    @Column(name = "kobling_id", nullable = false, updatable = false, unique = true)
    private Long koblingId;

    @Type(type = "jsonb")
    @Column(name = "input", nullable = false, updatable = false)
    private String input;

    public KalkulatorInputEntitet() {
        // for hibernate
    }

    public KalkulatorInputEntitet(Long koblingId, String input) {
        this.koblingId = koblingId;
        this.input = input;
    }

    public String getInput() {
        return input;
    }

    public Long getKoblingId() {
        return koblingId;
    }
}
