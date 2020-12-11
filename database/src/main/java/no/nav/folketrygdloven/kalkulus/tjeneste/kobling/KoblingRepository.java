package no.nav.folketrygdloven.kalkulus.tjeneste.kobling;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Beløp;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.InternArbeidsforholdRef;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.KoblingReferanse;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Saksnummer;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.felles.diff.DiffEntity;
import no.nav.folketrygdloven.kalkulus.felles.diff.DiffResult;
import no.nav.folketrygdloven.kalkulus.felles.diff.TraverseGraph;
import no.nav.folketrygdloven.kalkulus.felles.diff.TraverseJpaEntityGraphConfig;
import no.nav.folketrygdloven.kalkulus.felles.verktøy.HibernateVerktøy;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseTyperKalkulusStøtterKontrakt;
import no.nav.folketrygdloven.kalkulus.typer.AktørId;
import no.nav.folketrygdloven.kalkulus.typer.OrgNummer;

@ApplicationScoped
public class KoblingRepository {
    private static final Logger log = LoggerFactory.getLogger(KoblingRepository.class);
    private EntityManager entityManager;

    KoblingRepository() {
        // CDI
    }

    // TODO(OJR) erstatt med egen KalkulusPersistenceUnit??
    @Inject
    public KoblingRepository(EntityManager entityManager) {
        Objects.requireNonNull(entityManager, "entityManager"); //$NON-NLS-1$
        this.entityManager = entityManager;
    }

    public Optional<KoblingEntitet> hentForKoblingReferanse(KoblingReferanse referanse) {
        TypedQuery<KoblingEntitet> query = entityManager.createQuery("FROM Kobling k WHERE koblingReferanse = :referanse", KoblingEntitet.class);
        query.setParameter("referanse", referanse);
        return HibernateVerktøy.hentUniktResultat(query);
    }

    public Long hentKoblingIdForKoblingReferanse(KoblingReferanse referanse) {
        TypedQuery<Long> query = entityManager.createQuery("SELECT k.id FROM Kobling k WHERE k.koblingReferanse = :referanse", Long.class);
        query.setParameter("referanse", referanse);
        return HibernateVerktøy.hentUniktResultat(query).orElse(null);
    }

    public List<KoblingEntitet> hentKoblingIdForKoblingReferanser(Collection<KoblingReferanse> referanser) {
        TypedQuery<KoblingEntitet> query = entityManager.createQuery(
                "SELECT k FROM Kobling k WHERE k.koblingReferanse IN(:referanser)", KoblingEntitet.class);
        query.setParameter("referanser", referanser);
        return query.getResultList();
    }

    public Optional<Long> hentFor(KoblingReferanse referanse, YtelseTyperKalkulusStøtterKontrakt ytelseType) {
        TypedQuery<Long> query = entityManager
            .createQuery("SELECT k.id FROM Kobling k WHERE k.koblingReferanse = :referanse AND k.ytelseTyperKalkulusStøtter = :ytelse", Long.class);
        query.setParameter("referanse", referanse);
        query.setParameter("ytelse", ytelseType);
        return HibernateVerktøy.hentUniktResultat(query);
    }

    public List<KoblingEntitet> hentKoblingerFor(Collection<KoblingReferanse> referanser, YtelseTyperKalkulusStøtterKontrakt ytelseType) {
        TypedQuery<KoblingEntitet> query = entityManager.createQuery(
            "SELECT k FROM Kobling k WHERE k.koblingReferanse IN(:referanser) AND k.ytelseTyperKalkulusStøtter = :ytelseType", KoblingEntitet.class);
        query.setParameter("referanser", referanser);
        query.setParameter("ytelseType", ytelseType);
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

    private DiffResult getDiff(KoblingEntitet eksisterendeKobling, KoblingEntitet nyKobling) {
        var config = new TraverseJpaEntityGraphConfig();
        config.setIgnoreNulls(true);
        config.setOnlyCheckTrackedFields(false);
        
        config.addLeafClasses(Beløp.class);
        config.addLeafClasses(AktørId.class);
        config.addLeafClasses(Saksnummer.class);
        config.addLeafClasses(OrgNummer.class);
        config.addLeafClasses(InternArbeidsforholdRef.class);
        config.addLeafClasses(Arbeidsgiver.class);
        
        var diffEntity = new DiffEntity(new TraverseGraph(config));

        return diffEntity.diff(eksisterendeKobling, nyKobling);
    }


    public List<KoblingEntitet> hentKoblingerFor(Collection<Long> koblingIder) {
        return entityManager.createQuery("SELECT k FROM Kobling k WHERE k.id IN (:koblingIder)", KoblingEntitet.class)
            .setParameter("koblingIder", koblingIder)
            .getResultList();
    }

}
