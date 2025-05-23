package no.nav.folketrygdloven.kalkulus.tjeneste.avklaringsbehov;


import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.avklaringsbehov.AvklaringsbehovEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;

import static no.nav.vedtak.felles.jpa.HibernateVerktøy.hentUniktResultat;

@ApplicationScoped
public class AvklaringsbehovRepository {
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
        return query.getResultList().stream()
                .sorted(Comparator.naturalOrder())
                .toList();
    }
    public List<AvklaringsbehovEntitet> hentAvklaringsbehovforKobling(Long koblingId) {
        TypedQuery<AvklaringsbehovEntitet> query = entityManager.createQuery("FROM AvklaringsbehovEntitet ab " +
            "WHERE ab.kobling.id = :kobling", AvklaringsbehovEntitet.class);
        query.setParameter("kobling", koblingId);
        return query.getResultList().stream()
            .sorted(Comparator.naturalOrder())
            .toList();
    }

    public List<AvklaringsbehovEntitet> hentAvklaringsbehovForKobling(KoblingEntitet kobling) {
        TypedQuery<AvklaringsbehovEntitet> query = entityManager.createQuery("FROM AvklaringsbehovEntitet ab WHERE kobling = :kobling", AvklaringsbehovEntitet.class);
        query.setParameter("kobling", kobling);
        return query.getResultList().stream()
                .sorted(Comparator.naturalOrder())
                .toList();
    }

    public void lagre(AvklaringsbehovEntitet avklaringsbehov) {
        LOG.info("Lagrer avklaringsbehov med definisjon {} og status {} på kobling",
                avklaringsbehov.getDefinisjon(), avklaringsbehov.getStatus());
        entityManager.persist(avklaringsbehov);
        entityManager.flush();
    }

    public void slettAlleAvklaringsbehovForKobling(Long koblingId) {
        var grunnlagQuery = entityManager.createNativeQuery("delete from AVKLARINGSBEHOV " +
                "where kobling_id = :koblingId ")
            .setParameter("koblingId", koblingId);

        var slettedeRader = grunnlagQuery.executeUpdate();
        LOG.info("Slettet {} avklaringsbehov for koblingId={}", slettedeRader, koblingId);
    }
}
