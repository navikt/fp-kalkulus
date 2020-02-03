package no.nav.folketrygdloven.kalkulus.tjeneste.kobling;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.AktørId;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.KoblingReferanse;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Saksnummer;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.felles.diff.DiffEntity;
import no.nav.folketrygdloven.kalkulus.felles.diff.DiffResult;
import no.nav.folketrygdloven.kalkulus.felles.diff.TraverseGraph;
import no.nav.folketrygdloven.kalkulus.felles.diff.TraverseJpaEntityGraphConfig;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.Kodeliste;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.KodeverkTabell;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.YtelseTyperKalkulusStøtter;
import no.nav.folketrygdloven.kalkulus.felles.verktøy.HibernateVerktøy;
import no.nav.folketrygdloven.kalkulus.felles.verktøy.KalkulusPersistenceUnit;

@ApplicationScoped
public class KoblingRepository {
    private static final Logger log = LoggerFactory.getLogger(KoblingRepository.class);
    private EntityManager entityManager;

    KoblingRepository() {
        // CDI
    }

    @Inject
    public KoblingRepository(@KalkulusPersistenceUnit EntityManager entityManager) {
        Objects.requireNonNull(entityManager, "entityManager"); //$NON-NLS-1$
        this.entityManager = entityManager;
    }

    public Optional<KoblingEntitet> hentForKoblingReferanse(KoblingReferanse referanse) {
        TypedQuery<KoblingEntitet> query = entityManager.createQuery("FROM Kobling k WHERE koblingReferanse = :referanse", KoblingEntitet.class);
        query.setParameter("referanse", referanse);
        return HibernateVerktøy.hentUniktResultat(query);
    }

    public Optional<KoblingEntitet> hentSisteKoblingReferanseFor(AktørId aktørId, Saksnummer saksnummer, YtelseTyperKalkulusStøtter ytelseType) {
        TypedQuery<KoblingEntitet> query = entityManager.createQuery("FROM Kobling k " +
                        " WHERE k.saksnummer = :ref AND k.ytelseType = :ytelse and k.aktørId = :aktørId " + // NOSONAR
                        "order by k.opprettetTidspunkt desc, k.id desc"
                , KoblingEntitet.class);
        query.setParameter("ref", saksnummer);
        query.setParameter("ytelse", ytelseType);
        query.setParameter("aktørId", aktørId);
        query.setMaxResults(1);
        return query.getResultList().stream().findFirst();
    }


    public Long hentKoblingIdForKoblingReferanse(KoblingReferanse referanse) {
        TypedQuery<Long> query = entityManager.createQuery("SELECT k.id FROM Kobling k WHERE k.koblingReferanse = :referanse", Long.class);
        query.setParameter("referanse", referanse);
        return HibernateVerktøy.hentUniktResultat(query).orElse(null);
    }

    public void lagre(KoblingEntitet nyKobling) {
        Optional<KoblingEntitet> eksisterendeKobling = hentForKoblingReferanse(nyKobling.getKoblingReferanse());

        DiffResult diff = getDiff(eksisterendeKobling.orElse(null), nyKobling);

        if (!diff.isEmpty()) {
            log.info("Detekterte endringer på kobling med referanse={}, endringer={}", nyKobling.getId(), diff.getLeafDifferences());
            entityManager.persist(nyKobling);
            entityManager.flush();
        }
    }

    private DiffResult getDiff(KoblingEntitet eksisterendeKobling, KoblingEntitet nyKobling) {
        var config = new TraverseJpaEntityGraphConfig(); // NOSONAR
        config.setIgnoreNulls(true);
        config.setOnlyCheckTrackedFields(false);
        config.addLeafClasses(KodeverkTabell.class);
        config.addLeafClasses(Kodeliste.class);
        var diffEntity = new DiffEntity(new TraverseGraph(config));

        return diffEntity.diff(eksisterendeKobling, nyKobling);
    }

    public KoblingEntitet hentForKoblingId(Long koblingId) {
        return entityManager.find(KoblingEntitet.class, koblingId);
    }

    public List<Saksnummer> hentAlleSaksnummer() {
        TypedQuery<Saksnummer> query = entityManager.createQuery("SELECT k.saksnummer FROM Kobling k", Saksnummer.class);
        return query.getResultList();
    }
}
