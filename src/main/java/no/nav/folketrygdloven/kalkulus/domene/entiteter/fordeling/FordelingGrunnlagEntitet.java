package no.nav.folketrygdloven.kalkulus.domene.entiteter.fordeling;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import no.nav.folketrygdloven.kalkulus.domene.felles.jpa.BaseEntitet;

@Entity(name = "FordelingGrunnlagEntitet")
@Table(name = "GR_FORDELING")
public class FordelingGrunnlagEntitet extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GLOBAL_PK_SEQ_GENERATOR")
    private Long id;

    @Version
    @Column(name = "versjon", nullable = false)
    private int versjon;

    @Column(name = "kobling_id", nullable = false, updatable = false)
    private Long koblingId;

    @Column(name = "aktiv", nullable = false)
    private boolean aktiv = true;

    @OneToMany(mappedBy = "fordelingGrunnlag", cascade = CascadeType.PERSIST)
    private List<RefusjonsandelEntitet> refusjonsandeler = new ArrayList<>();

    protected FordelingGrunnlagEntitet() {
        // Hibernate
    }

    public FordelingGrunnlagEntitet(Long koblingId) {
        this.koblingId = Objects.requireNonNull(koblingId, "koblingId");
    }

    public Long getId() {
        return id;
    }

    public Long getKoblingId() {
        return koblingId;
    }

    public boolean erAktivt() {
        return aktiv;
    }

    public void setAktiv(boolean aktiv) {
        this.aktiv = aktiv;
    }

    public List<RefusjonsandelEntitet> getRefusjonsandeler() {
        return List.copyOf(refusjonsandeler);
    }

    public void leggTilRefusjonsandel(RefusjonsandelEntitet refusjonsandel) {
        refusjonsandel.setFordelingGrunnlag(this);
        refusjonsandeler.add(refusjonsandel);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final FordelingGrunnlagEntitet kladd = new FordelingGrunnlagEntitet();

        private Builder() {
        }

        public Builder leggTilRefusjonsandel(RefusjonsandelEntitet refusjonsandel) {
            kladd.leggTilRefusjonsandel(refusjonsandel);
            return this;
        }

        public FordelingGrunnlagEntitet build(Long koblingId) {
            kladd.koblingId = Objects.requireNonNull(koblingId, "koblingId");
            return kladd;
        }
    }
}
