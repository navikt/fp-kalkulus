package no.nav.folketrygdloven.kalkulus.tjeneste.kobling;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Beløp;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.InternArbeidsforholdRef;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.KoblingReferanse;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Saksnummer;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingGrunnlagskopiSporing;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingRelasjon;
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

    public Optional<KoblingEntitet> hentKoblingMedId(Long id) {
        TypedQuery<KoblingEntitet> query = entityManager.createQuery("FROM Kobling k WHERE id = :id", KoblingEntitet.class);
        query.setParameter("id", id);
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

    public List<KoblingEntitet> hentAlleKoblingerForSaksnummer(Saksnummer saksnummer) {
        TypedQuery<KoblingEntitet> query = entityManager.createQuery(
                "SELECT k FROM Kobling k WHERE k.saksnummer = :saksnummer order by k.opprettetTidspunkt desc", KoblingEntitet.class);
        query.setParameter("saksnummer", saksnummer);
        return query.getResultList();
    }

    public Optional<KoblingEntitet> hentSisteKoblingForSaksnummer(Saksnummer saksnummer) {
        TypedQuery<KoblingEntitet> query = entityManager.createQuery(
                "SELECT k FROM Kobling k WHERE k.saksnummer = :saksnummer order by k.opprettetTidspunkt desc", KoblingEntitet.class);
        query.setParameter("saksnummer", saksnummer);
        query.setMaxResults(1);
        return HibernateVerktøy.hentUniktResultat(query);
    }

    public void fjernUgyldigKoblingrelasjonForId(Long koblingId) {
        var query = entityManager.createNativeQuery(
                "DELETE FROM KOBLING_RELASJON k " +
                        "WHERE k.kobling_id = :koblingId " +
                        "AND k.original_kobling_id = :koblingId"
        );
        query.setParameter("koblingId", koblingId);
        var slettedeRader = query.executeUpdate();
        if (slettedeRader > 1) {
            throw new IllegalStateException("Skal kun slette en ugyldig koblingrelasjon");
        }
    }

    public List<KoblingRelasjon> hentRelasjonerForId(Long koblingId) {
        TypedQuery<KoblingRelasjon> query = entityManager.createQuery(
                "SELECT k FROM KoblingRelasjon k WHERE k.koblingId = :koblingId", KoblingRelasjon.class);
        query.setParameter("koblingId", koblingId);
        return query.getResultList();
    }

    public Optional<KoblingGrunnlagskopiSporing> hentGrunnlagskopiForKobling(Long koblingId) {
        TypedQuery<KoblingGrunnlagskopiSporing> query = entityManager.createQuery(
                "SELECT k FROM KoblingGrunnlagskopiSporing k WHERE k.kopiertTilKoblingId = :koblingId and aktiv = true", KoblingGrunnlagskopiSporing.class);
        query.setParameter("koblingId", koblingId);
        return HibernateVerktøy.hentUniktResultat(query);
    }


    public List<KoblingRelasjon> hentRelasjonerFor(Collection<Long> ider) {
        TypedQuery<KoblingRelasjon> query = entityManager.createQuery(
                "SELECT k FROM KoblingRelasjon k WHERE k.koblingId IN (:ider)", KoblingRelasjon.class);
        query.setParameter("ider", ider);
        return query.getResultList();
    }

    public Optional<KoblingRelasjon> hentRelasjon(Long koblingId, Long originalKoblingId) {
        TypedQuery<KoblingRelasjon> query = entityManager.createQuery(
                "SELECT k FROM KoblingRelasjon k WHERE k.koblingId = :koblingId and k.originalKoblingId = :originalKoblingId", KoblingRelasjon.class);
        query.setParameter("koblingId", koblingId);
        query.setParameter("originalKoblingId", originalKoblingId);
        return HibernateVerktøy.hentUniktResultat(query);
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

    public void lagre(KoblingGrunnlagskopiSporing grunnlagskopi) {
        var eksisterendeRelasjon = hentGrunnlagskopiForKobling(grunnlagskopi.getKopiertTilKoblingId());

        if (eksisterendeRelasjon.isEmpty()) {
            entityManager.persist(grunnlagskopi);
            entityManager.flush();
        } else {
            var gammel = eksisterendeRelasjon.get();
            if (!gammel.getKopiertGrunnlagId().equals(grunnlagskopi.getKopiertGrunnlagId())) {
                gammel.setAktiv(false);
                entityManager.persist(gammel);
                entityManager.persist(grunnlagskopi);
                entityManager.flush();
            }
        }
    }


    public void lagre(KoblingRelasjon koblingRelasjon) {
        var eksisterendeRelasjon = hentRelasjon(koblingRelasjon.getKoblingId(), koblingRelasjon.getOriginalKoblingId());
        if (eksisterendeRelasjon.isEmpty()) {
            entityManager.persist(koblingRelasjon);
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
