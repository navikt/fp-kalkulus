package no.nav.folketrygdloven.kalkulus.tjeneste.avklaringsbehov;

import static no.nav.folketrygdloven.kalkulus.felles.verktøy.HibernateVerktøy.hentUniktResultat;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.avklaringsbehov.AvklaringsbehovEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;

@ApplicationScoped
class AvklaringsbehovRepository {
    private static final Logger LOG = LoggerFactory.getLogger(AvklaringsbehovRepository.class);
    private EntityManager entityManager;

    protected AvklaringsbehovRepository() {
        // for CDI proxy
    }

    @Inject
    public AvklaringsbehovRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Optional<AvklaringsbehovEntitet> hentAvklaringsbehovForKobling(KoblingEntitet kobling, AvklaringsbehovDefinisjon definisjon) {
        TypedQuery<AvklaringsbehovEntitet> query = entityManager.createQuery("FROM AvklaringsbehovEntitet ab WHERE kobling = :kobling " +
                "and definisjon =:def", AvklaringsbehovEntitet.class);
        query.setParameter("kobling", kobling);
        query.setParameter("def", definisjon);
        return hentUniktResultat(query);
    }

    public List<AvklaringsbehovEntitet> hentAvklaringsbehovforKoblinger(Collection<Long> koblingIder) {
        TypedQuery<AvklaringsbehovEntitet> query = entityManager.createQuery("FROM AvklaringsbehovEntitet ab " +
                "WHERE ab.kobling.id in :koblinger", AvklaringsbehovEntitet.class);
        query.setParameter("koblinger", koblingIder);
        return query.getResultList();
    }

    public List<AvklaringsbehovEntitet> hentAvklaringsbehovForKobling(KoblingEntitet kobling) {
        TypedQuery<AvklaringsbehovEntitet> query = entityManager.createQuery("FROM AvklaringsbehovEntitet ab WHERE kobling = :kobling", AvklaringsbehovEntitet.class);
        query.setParameter("kobling", kobling);
        return query.getResultList();
    }

    public void lagre(AvklaringsbehovEntitet avklaringsbehov) {
        LOG.info("Lagrer avklaringsbehov med definisjon {} og status {} på kobling",
                avklaringsbehov.getDefinisjon(), avklaringsbehov.getStatus());
        entityManager.persist(avklaringsbehov);
        entityManager.flush();
    }
}
