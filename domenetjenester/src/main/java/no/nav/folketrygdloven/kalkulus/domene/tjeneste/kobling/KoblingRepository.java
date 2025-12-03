package no.nav.folketrygdloven.kalkulus.domene.tjeneste.kobling;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.KoblingReferanse;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Saksnummer;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.domene.felles.diff.DiffEntity;
import no.nav.folketrygdloven.kalkulus.domene.felles.diff.DiffResult;
import no.nav.folketrygdloven.kalkulus.domene.felles.diff.TraverseGraph;
import no.nav.folketrygdloven.kalkulus.domene.felles.diff.TraverseJpaEntityGraphConfig;
import no.nav.folketrygdloven.kalkulus.kodeverk.Kodeverdi;
import no.nav.vedtak.felles.jpa.HibernateVerktøy;

@ApplicationScoped
public class KoblingRepository {
    private static final Logger log = LoggerFactory.getLogger(KoblingRepository.class);
    private EntityManager entityManager;

    KoblingRepository() {
        // CDI
    }

    @Inject
    public KoblingRepository(EntityManager entityManager) {
        Objects.requireNonNull(entityManager, "entityManager");
        this.entityManager = entityManager;
    }

    public Optional<KoblingEntitet> hentForKoblingReferanse(KoblingReferanse referanse) {
        TypedQuery<KoblingEntitet> query = entityManager.createQuery("FROM Kobling k WHERE k.koblingReferanse = :referanse", KoblingEntitet.class);
        query.setParameter("referanse", referanse);
        return HibernateVerktøy.hentUniktResultat(query);
    }

    public Optional<KoblingEntitet> hentKoblingMedId(Long id) {
        TypedQuery<KoblingEntitet> query = entityManager.createQuery("FROM Kobling k WHERE id = :id", KoblingEntitet.class);
        query.setParameter("id", id);
        return HibernateVerktøy.hentUniktResultat(query);
    }

    public List<KoblingEntitet> hentAlleKoblingerForSaksnummer(Saksnummer saksnummer) {
        TypedQuery<KoblingEntitet> query = entityManager.createQuery(
                "SELECT k FROM Kobling k WHERE k.saksnummer = :saksnummer order by k.opprettetTidspunkt desc", KoblingEntitet.class);
        query.setParameter("saksnummer", saksnummer);
        return query.getResultList();
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

    public void markerKoblingSomAvsluttet(KoblingEntitet koblingEntitet) {
        koblingEntitet.setErAvsluttet(true);
        entityManager.persist(koblingEntitet);
        entityManager.flush();
    }

    private DiffResult getDiff(KoblingEntitet eksisterendeKobling, KoblingEntitet nyKobling) {
        var config = new TraverseJpaEntityGraphConfig();
        config.setIgnoreNulls(true);
        config.setOnlyCheckTrackedFields(false);
        config.addLeafClasses(Kodeverdi.class);
        var diffEntity = new DiffEntity(new TraverseGraph(config));

        return diffEntity.diff(eksisterendeKobling, nyKobling);
    }
}
