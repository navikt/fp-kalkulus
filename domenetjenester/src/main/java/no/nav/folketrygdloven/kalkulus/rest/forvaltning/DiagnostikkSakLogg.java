package no.nav.folketrygdloven.kalkulus.rest.forvaltning;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PreRemove;
import jakarta.persistence.Table;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Saksnummer;
import no.nav.folketrygdloven.kalkulus.felles.jpa.BaseEntitet;

@Entity(name = "DiagnostikkSakLogg")
@Table(name = "DIAGNOSTIKK_SAK_LOGG")
public class DiagnostikkSakLogg extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_DIAGNOSTIKK_SAK_LOGG")
    @Column(name = "id")
    private Long id;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "saksnummer",
            column = @Column(name = "saksnummer", nullable = false, updatable = false, insertable = true)))
    private Saksnummer saksnummer;

    @Column(name = "begrunnelse", updatable = false, length = 4000)
    private String begrunnelse;

    @Column(name = "tjeneste", updatable = false, length = 200)
    private String tjeneste;

    DiagnostikkSakLogg() {
        // Hibernate
    }

    public DiagnostikkSakLogg(Saksnummer saksnummer, String tjeneste, String begrunnelse) {
        this.saksnummer = saksnummer;
        this.tjeneste = tjeneste;
        this.begrunnelse = begrunnelse;
    }

    public Long getId() {
        return id;
    }

    public Saksnummer getSaksnummer() {
        return saksnummer;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<saksnummer=" + saksnummer + ">";
    }

    @PreRemove
    protected void onDelete() {
        throw new IllegalStateException("Skal aldri kunne slette saksnummer. [id=" + id + "]");
    }
}
