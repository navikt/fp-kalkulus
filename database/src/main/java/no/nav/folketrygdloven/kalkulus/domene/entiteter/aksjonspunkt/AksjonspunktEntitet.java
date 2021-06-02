package no.nav.folketrygdloven.kalkulus.domene.entiteter.aksjonspunkt;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import com.fasterxml.jackson.annotation.JsonBackReference;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.mapping.AksjonspunktDefinisjonKodeverdiConverter;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.mapping.AksjonspunktStatusDefinisjonKodeverdiConverter;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.mapping.BeregningStegKodeverdiConverter;
import no.nav.folketrygdloven.kalkulus.felles.jpa.BaseEntitet;
import no.nav.folketrygdloven.kalkulus.kodeverk.AksjonspunktDefinisjon;
import no.nav.folketrygdloven.kalkulus.kodeverk.AksjonspunktStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningSteg;

@Entity(name = "Aksjonspunkt")
@Table(name = "AKSJONSPUNKT")
public class AksjonspunktEntitet extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_AKSJONSPUNKT")
    private Long id;

    @JsonBackReference
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "kobling_id", nullable = false, updatable = false)
    private KoblingEntitet kobling;

    @Convert(converter = AksjonspunktDefinisjonKodeverdiConverter.class)
    @Column(name = "aksjonspunkt_def", nullable = false)
    private AksjonspunktDefinisjon definisjon;

    @Convert(converter = AksjonspunktStatusDefinisjonKodeverdiConverter.class)
    @Column(name = "aksjonspunkt_status", nullable = false)
    private AksjonspunktStatus status;

    @Convert(converter = BeregningStegKodeverdiConverter.class)
    @Column(name = "steg_funnet", nullable = false)
    private BeregningSteg stegFunnet;

    @Column(name = "begrunnelse")
    private String begrunnelse;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    private AksjonspunktEntitet(AksjonspunktDefinisjon aksjonspunktDefinisjon) {
        Objects.requireNonNull(aksjonspunktDefinisjon, "aksjonspunktDefinisjon");
        this.definisjon = aksjonspunktDefinisjon;
    }

    protected AksjonspunktEntitet() {
        // Hibernate
    }

    public KoblingEntitet getKobling() {
        return kobling;
    }

    public AksjonspunktDefinisjon getDefinisjon() {
        return definisjon;
    }

    public AksjonspunktStatus getStatus() {
        return status;
    }

    public BeregningSteg getStegFunnet() {
        return stegFunnet;
    }

    public String getBegrunnelse() {
        return begrunnelse;
    }

    void setKobling(KoblingEntitet kobling) {
        Objects.requireNonNull(kobling, "koblingId");
        this.kobling = kobling;
    }

    void setStegFunnet(BeregningSteg steg) {
        Objects.requireNonNull(steg, "steg");
        this.stegFunnet = steg;
    }

    void setBegrunnelse(String begrunnelse) {
        Objects.requireNonNull(begrunnelse, "begrunnelse");
        this.begrunnelse = begrunnelse;
    }

    void setStatus(AksjonspunktStatus status) {
        Objects.requireNonNull(status, "aksjonspunktstatus");
        this.status = status;
    }

    void oppdaterStatus(AksjonspunktStatus nyStatus) {
        if (getStatus().equals(nyStatus)) {
            // Kaster feil hvis dette skjer, tyder på at noe er gått galt
            throw new IllegalStateException("Prøver å oppdatere aksjonspunkt " + this +
                    " med en status det allerede har, status var " + nyStatus);
        }
        if (AksjonspunktStatus.UTFØRT.equals(nyStatus)) {
            kastFeilHvisAvbrutt();
        }
        setStatus(nyStatus);
    }

    void oppdaterBegrunnelse(String begrunnelse) {
        setBegrunnelse(begrunnelse);
    }

    private void kastFeilHvisAvbrutt() {
        if (AksjonspunktStatus.AVBRUTT.equals(status)) {
           throw new IllegalStateException("Prøver å løse aksjonspunkt som er avbrutt: " + this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AksjonspunktEntitet that = (AksjonspunktEntitet) o;
        return definisjon == that.definisjon && status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(definisjon, status);
    }

    @Override
    public String toString() {
        return "AksjonspunktEntitet{" +
                "koblingId=" + kobling +
                ", definisjon=" + definisjon +
                ", status=" + status +
                ", stegFunnet=" + stegFunnet +
                ", begrunnelse='" + begrunnelse + '\'' +
                '}';
    }

    static class Builder {
        private AksjonspunktEntitet mal;

        Builder(AksjonspunktDefinisjon aksjonspunktDefinisjon) {
            this.mal = new AksjonspunktEntitet(aksjonspunktDefinisjon);
        }

        AksjonspunktEntitet.Builder medStatus(AksjonspunktStatus status) {
            mal.setStatus(status);
            return this;
        }

        AksjonspunktEntitet.Builder medStegFunnet(BeregningSteg stegFunnet) {
            mal.setStegFunnet(stegFunnet);
            return this;
        }

        AksjonspunktEntitet.Builder medBegrunnelse(String begrunnelse) {
            mal.setBegrunnelse(begrunnelse);
            return this;
        }

        AksjonspunktEntitet buildFor(KoblingEntitet kobling) {
            mal.setKobling(kobling);
            verifiserAksjonspunkt();
            return mal;
        }

        private void verifiserAksjonspunkt() {
            Objects.requireNonNull(mal, "aksjonspunktentitet");
            Objects.requireNonNull(mal.kobling, "koblingId");
            Objects.requireNonNull(mal.definisjon, "aksjonspunktDefinisjon");
            Objects.requireNonNull(mal.status, "aksjonspunktStatus");
        }
    }
}
