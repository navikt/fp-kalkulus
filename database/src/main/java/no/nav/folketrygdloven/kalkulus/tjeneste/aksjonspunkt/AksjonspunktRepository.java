package no.nav.folketrygdloven.kalkulus.tjeneste.aksjonspunkt;

import static no.nav.folketrygdloven.kalkulus.felles.verktøy.HibernateVerktøy.hentUniktResultat;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.aksjonspunkt.AksjonspunktEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.kodeverk.AksjonspunktDefinisjon;

@ApplicationScoped
class AksjonspunktRepository {
    private static final Logger LOG = LoggerFactory.getLogger(AksjonspunktRepository.class);
    private EntityManager entityManager;

    protected AksjonspunktRepository() {
        // for CDI proxy
    }

    @Inject
    public AksjonspunktRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Optional<AksjonspunktEntitet> hentAksjonspunktforKobling(KoblingEntitet kobling, AksjonspunktDefinisjon definisjon) {
        TypedQuery<AksjonspunktEntitet> query = entityManager.createQuery("FROM Aksjonspunkt ap WHERE kobling = :kobling " +
                "and definisjon =:def", AksjonspunktEntitet.class);
        query.setParameter("kobling", kobling);
        query.setParameter("def", definisjon);
        return hentUniktResultat(query);
    }

    public void lagre(AksjonspunktEntitet aksjonspunkt) {
        LOG.info("Lagrer aksjonspunkt med definisjon {} og status {} på kobling",
                aksjonspunkt.getDefinisjon(), aksjonspunkt.getStatus());
        entityManager.persist(aksjonspunkt);
        entityManager.flush();
    }
}
