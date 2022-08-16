package no.nav.folketrygdloven.kalkulus.domene.entiteter.avklaringsbehov;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import com.fasterxml.jackson.annotation.JsonBackReference;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.mapping.AvklaringsbehovDefinisjonKodeverdiConverter;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.mapping.AvklaringsbehovStatusDefinisjonKodeverdiConverter;
import no.nav.folketrygdloven.kalkulus.felles.jpa.BaseEntitet;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningSteg;

@Entity(name = "AvklaringsbehovEntitet")
@Table(name = "AVKLARINGSBEHOV")
public class AvklaringsbehovEntitet extends BaseEntitet implements Comparable<AvklaringsbehovEntitet> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_AVKLARINGSBEHOV")
    private Long id;

    @JsonBackReference
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "kobling_id", nullable = false, updatable = false)
    private KoblingEntitet kobling;

    @Convert(converter = AvklaringsbehovDefinisjonKodeverdiConverter.class)
    @Column(name = "avklaringsbehov_def", nullable = false)
    private AvklaringsbehovDefinisjon definisjon;

    @Convert(converter = AvklaringsbehovStatusDefinisjonKodeverdiConverter.class)
    @Column(name = "avklaringsbehov_status", nullable = false)
    private AvklaringsbehovStatus status;

    @Column(name = "begrunnelse")
    private String begrunnelse;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    private AvklaringsbehovEntitet(AvklaringsbehovDefinisjon avklaringsbehovDefinisjon) {
        Objects.requireNonNull(avklaringsbehovDefinisjon, "avklaringsbehovDefinisjon");
        this.definisjon = avklaringsbehovDefinisjon;
    }

    protected AvklaringsbehovEntitet() {
        // Hibernate
    }

    public KoblingEntitet getKobling() {
        return kobling;
    }

    public Long getKoblingId() {
        return kobling.getId();
    }

    public AvklaringsbehovDefinisjon getDefinisjon() {
        return definisjon;
    }

    public AvklaringsbehovStatus getStatus() {
        return status;
    }

    public String getBegrunnelse() {
        return begrunnelse;
    }

    void setKobling(KoblingEntitet kobling) {
        Objects.requireNonNull(kobling, "koblingId");
        this.kobling = kobling;
    }

    void setBegrunnelse(String begrunnelse) {
        Objects.requireNonNull(begrunnelse, "begrunnelse");
        this.begrunnelse = begrunnelse;
    }

    void setStatus(AvklaringsbehovStatus status) {
        Objects.requireNonNull(status, "avklaringsbehovstatus");
        this.status = status;
    }

    void oppdaterStatus(AvklaringsbehovStatus nyStatus) {
        if (getStatus().equals(nyStatus)) {
            // Kaster feil hvis dette skjer, tyder på at noe er gått galt
            throw new IllegalStateException("Prøver å oppdatere avklaringsbehov " + this +
                    " med en status det allerede har, status var " + nyStatus);
        }
        if (AvklaringsbehovStatus.UTFØRT.equals(nyStatus)) {
            kastFeilHvisAvbrutt();
        }
        setStatus(nyStatus);
    }

    void oppdaterBegrunnelse(String begrunnelse) {
        setBegrunnelse(begrunnelse);
    }

    private void kastFeilHvisAvbrutt() {
        if (AvklaringsbehovStatus.AVBRUTT.equals(status)) {
           throw new IllegalStateException("Prøver å løse avklaringsbehov som er avbrutt: " + this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AvklaringsbehovEntitet that = (AvklaringsbehovEntitet) o;
        return definisjon == that.definisjon && status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(definisjon, status);
    }

    @Override
    public String toString() {
        return "AvklaringsbehovEntitet{" +
                "koblingId=" + kobling +
                ", definisjon=" + definisjon +
                ", status=" + status +
                ", begrunnelse='" + begrunnelse + '\'' +
                '}';
    }

    public BeregningSteg getStegFunnet() {
        return definisjon.getStegFunnet();
    }

    @Override
    public int compareTo(AvklaringsbehovEntitet o) {
        if (this.getStegFunnet().erFør(o.getStegFunnet())) {
            return -1;
        }
        if (this.getStegFunnet().erEtter(o.getStegFunnet())) {
            return 1;
        }
        return 0;
    }

    static class Builder {
        private AvklaringsbehovEntitet mal;

        Builder(AvklaringsbehovDefinisjon avklaringsbehovDefinisjon) {
            this.mal = new AvklaringsbehovEntitet(avklaringsbehovDefinisjon);
        }

        AvklaringsbehovEntitet.Builder medStatus(AvklaringsbehovStatus status) {
            mal.setStatus(status);
            return this;
        }

        AvklaringsbehovEntitet.Builder medBegrunnelse(String begrunnelse) {
            mal.setBegrunnelse(begrunnelse);
            return this;
        }

        AvklaringsbehovEntitet buildFor(KoblingEntitet kobling) {
            mal.setKobling(kobling);
            verifiserAvklaringsbehov();
            return mal;
        }

        private void verifiserAvklaringsbehov() {
            Objects.requireNonNull(mal, "avklaringsbehoventitet");
            Objects.requireNonNull(mal.kobling, "koblingId");
            Objects.requireNonNull(mal.definisjon, "avklaringsbehovDefinisjon");
            Objects.requireNonNull(mal.status, "avklaringsbehovStatus");
        }
    }
}
