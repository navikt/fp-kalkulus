package no.nav.folketrygdloven.kalkulus.domene.entiteter.sporing;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity(name = "RegelInputKomprimering")
@Table(name = "REGEL_INPUT_KOMPRIMERING")
public class RegelInputKomprimering {

    @Id
    @Column(name = "regel_input_hash", columnDefinition="TEXT")
    private String regelInputHash;

    @Column(name = "regel_input_json", columnDefinition="TEXT")
    private String regelInput;


    public String getRegelInputHash() {
        return regelInputHash;
    }

    public String getRegelInput() {
        return regelInput;
    }
}
