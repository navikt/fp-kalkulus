package no.nav.folketrygdloven.kalkulus.tjeneste.sporing;

import static ch.qos.logback.core.encoder.ByteArrayUtil.toHexString;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import no.nav.folketrygdloven.kalkulator.KonfigurasjonVerdi;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.sporing.RegelSporingGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.sporing.RegelSporingPeriodeEntitet;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagPeriodeRegelType;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagRegelType;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;

@ApplicationScoped
public class RegelsporingRepository {

    private static final Logger log = LoggerFactory.getLogger(RegelsporingRepository.class);

    private static final String KOBLING_ID = "koblingId";
    private EntityManager entityManager;

    protected RegelsporingRepository() {
        // for CDI proxy
    }

    @Inject
    public RegelsporingRepository(EntityManager entityManager) {
        Objects.requireNonNull(entityManager, "entityManager"); //$NON-NLS-1$
        this.entityManager = entityManager;
    }

    /**
     * Lagrer regelsporing for periode
     *
     * @param regelSporingPerioder reglsporingperioder som skal lagres
     */
    public void lagre(Long koblingId, Map<BeregningsgrunnlagPeriodeRegelType, List<RegelSporingPeriodeEntitet.Builder>> regelSporingPerioder) {
        regelSporingPerioder.forEach((key, value) -> value.stream()
                .map(builder -> builder.build(koblingId, key)).forEach(sporing -> {
                    if (!sporing.erAktiv()) {
                        throw new IllegalArgumentException("Kan ikke lagre en inaktivt reglsporing");
                    }
                    if (KonfigurasjonVerdi.get("REGEL_INPUT_HASHING_ENABLED", true)) {
                        var hash = kjørRegelInputHashing(sporing.getRegelInput());
                        sporing.setRegelInputHash(hash);
                        if (KonfigurasjonVerdi.get("REGEL_INPUT_KOMPRIMERING_ENABLED", true)) {
                            sporing.setRegelInput(null);
                        }
                    }
                    entityManager.persist(sporing);
                }));
        entityManager.flush();
    }

    /**
     * Lager hash og komprimererer regelsporinger
     *
     * @return
     */
    public int hashVilkårlige(int antall) {
        TypedQuery<RegelSporingPeriodeEntitet> query = entityManager.createQuery(
                "from RegelSporingPeriodeEntitet sporing " +
                        "where regelInput is not null", RegelSporingPeriodeEntitet.class); //$NON-NLS-1$
        query.setMaxResults(antall);
        var resultList = query.getResultList();

        resultList.forEach(r -> {
            var hash = kjørRegelInputHashing(r.getRegelInput());
            r.setRegelInputHash(hash);
            r.setRegelInput(null);
            entityManager.persist(r);
        });
        entityManager.flush();
        return resultList.size();
    }

    /**
     * Lager hash og komprimererer alle regelsporinger på kobling
     *
     * @param koblingId koblingID
     */
    public void hashAlleForKobling(Long koblingId) {
        TypedQuery<RegelSporingPeriodeEntitet> query = entityManager.createQuery(
                "from RegelSporingPeriodeEntitet sporing " +
                        "where sporing.koblingId = :koblingId and regelInput is not null", RegelSporingPeriodeEntitet.class); //$NON-NLS-1$
        query.setParameter(KOBLING_ID, koblingId); //$NON-NLS-1$
        var resultList = query.getResultList();

        resultList.forEach(r -> {
            var hash = kjørRegelInputHashing(r.getRegelInput());
            r.setRegelInputHash(hash);
            r.setRegelInput(null);
            entityManager.persist(r);
        });
        entityManager.flush();
    }

    /**
     * Finner vilkårlig sporing uten komprimering
     */
    public Optional<Long> finnKoblingUtenKomprimering() {
        TypedQuery<RegelSporingPeriodeEntitet> query = entityManager.createQuery(
                "from RegelSporingPeriodeEntitet sporing " +
                        "where regelInput is not null", RegelSporingPeriodeEntitet.class); //$NON-NLS-1$
        query.setMaxResults(1); //$NON-NLS-1$
        var resultList = query.getResultList();
        if (resultList.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(resultList.get(0).getKoblingId());
    }

    private String kjørRegelInputHashing(String regelInput) {
        String regelInputHash = lagMD5Hash(regelInput);
        var insertQuery = entityManager.createNativeQuery(
                        "INSERT INTO REGEL_INPUT_KOMPRIMERING (REGEL_INPUT_HASH, REGEL_INPUT_JSON) " +
                                "VALUES (:md5_hash, :sporing_json) " +
                                "ON CONFLICT DO NOTHING")
                .setParameter("sporing_json", regelInput)
                .setParameter("md5_hash", regelInputHash);
        insertQuery.executeUpdate();
        return regelInputHash;
    }

    private String lagMD5Hash(String regelInput) {
        byte[] md5Hash;
        try {
            md5Hash = MessageDigest.getInstance("MD5").digest(regelInput.getBytes());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e.getMessage());
        }
        return toHexString(md5Hash);
    }

    /**
     * Lagrer regelsporing
     *
     * @param regelSporingGrunnlag Regelsporing
     */
    public void lagre(Long koblingId,
                      RegelSporingGrunnlagEntitet.Builder regelSporingGrunnlag,
                      BeregningsgrunnlagRegelType regelType) {
        entityManager.persist(regelSporingGrunnlag.build(koblingId, regelType));
        entityManager.flush();
    }

    /**
     * Rydder alle regelsporinger lagret lik eller før gitt tilstand
     *
     * @param koblingId KoblingId
     * @param tilstand  Beregningsgrunnlagtilstand
     */
    public void ryddRegelsporingForTilstand(Long koblingId, BeregningsgrunnlagTilstand tilstand) {
        ryddRegelsporingForTilstand(Set.of(koblingId), tilstand);
    }

    /**
     * Rydder alle regelsporinger lagret lik eller før gitt tilstand
     *
     * @param koblingIder koblingIder
     * @param tilstand    Beregningsgrunnlagtilstand
     */
    public void ryddRegelsporingForTilstand(Set<Long> koblingIder, BeregningsgrunnlagTilstand tilstand) {

        // Rydd grunnlag-sporing
        var typerSomSkalRyddes = EnumSet.allOf(BeregningsgrunnlagRegelType.class)
                .stream()
                .filter(t -> !t.getLagretTilstand().erFør(tilstand))
                .collect(Collectors.toList());

        var grunnlagQuery = entityManager.createQuery("Update RegelSporingGrunnlagEntitet " +
                        "set aktiv = false " +
                        "where koblingId in :koblingId " +
                        "and aktiv = true " +
                        "and regelType in (:tilstander)")
                .setParameter(KOBLING_ID, koblingIder)
                .setParameter("tilstander", typerSomSkalRyddes);

        var oppdaterteRader = grunnlagQuery.executeUpdate();
        log.info("Deaktivert {} regelsporringsgrunnlag for koblingId={}", oppdaterteRader, koblingIder);

        // Rydd periode-sporing
        var periodetyperSomSkalRyddes = EnumSet.allOf(BeregningsgrunnlagPeriodeRegelType.class)
                .stream()
                .filter(t -> !t.getLagretTilstand().erFør(tilstand))
                .collect(Collectors.toList());

        var perioderQuery = entityManager.createQuery("Update RegelSporingPeriodeEntitet " +
                        "set aktiv = false " +
                        "where koblingId in :koblingId " +
                        "and aktiv = true " +
                        "and regelType in (:tilstander)")
                .setParameter(KOBLING_ID, koblingIder)
                .setParameter("tilstander", periodetyperSomSkalRyddes);

        var perioderOppdaterteRader = perioderQuery.executeUpdate();
        log.info("Deaktivert {} regelsporringsperioder for koblingId={}", perioderOppdaterteRader, koblingIder);

        entityManager.flush();
    }

    /**
     * Sletter alle inaktive regelsporinger
     *
     * @param koblingId koblingId
     */
    public void slettAlleInaktiveRegelsporinger(Long koblingId) {

        // Sletter grunnlag-sproring
        var grunnlagQuery = entityManager.createNativeQuery("delete from REGEL_SPORING_GRUNNLAG " +
                        "where kobling_id = :koblingId " +
                        "and aktiv = false")
                .setParameter("koblingId", koblingId);

        var oppdaterteRader = grunnlagQuery.executeUpdate();
        log.info("Slettet {} regelsporringsgrunnlag for koblingId={}", oppdaterteRader, koblingId);


        // Sletter periode-sporing
        var perioderQuery = entityManager.createNativeQuery("delete from REGEL_SPORING_PERIODE " +
                        "where kobling_id = :koblingId " +
                        "and aktiv = false")
                .setParameter("koblingId", koblingId);

        var perioderOppdaterteRader = perioderQuery.executeUpdate();
        log.info("Slettet {} regelsporringsperioder for koblingId={}", perioderOppdaterteRader, koblingId);

        entityManager.flush();
    }

    /**
     * Henter aktiv RegelsporingPeriode med gitt type
     *
     * @param koblingId en koblingId
     * @return Alle aktive {@link no.nav.folketrygdloven.kalkulus.domene.entiteter.sporing.RegelSporingPeriodeEntitet}
     */
    public List<RegelSporingPeriodeEntitet> hentRegelSporingPeriodeMedGittType(Long koblingId, List<BeregningsgrunnlagPeriodeRegelType> regelTyper) {
        TypedQuery<RegelSporingPeriodeEntitet> query = entityManager.createQuery(
                "from RegelSporingPeriodeEntitet sporing " +
                        "where sporing.koblingId=:koblingId " +
                        "and sporing.aktiv = :aktiv " +
                        "and sporing.regelType in :regeltype", RegelSporingPeriodeEntitet.class); //$NON-NLS-1$
        query.setParameter(KOBLING_ID, koblingId); //$NON-NLS-1$
        query.setParameter("aktiv", true); //$NON-NLS-1$
        query.setParameter("regeltype", regelTyper);
        return query.getResultList();
    }

    /**
     * Henter aktiv RegelsporingPeriode med gitt type
     *
     * @param koblingIder et sett med koblingIder
     * @return Alle aktive {@link no.nav.folketrygdloven.kalkulus.domene.entiteter.sporing.RegelSporingPeriodeEntitet}
     */
    public List<RegelSporingPeriodeEntitet> hentRegelSporingerPeriodeMedGittType(Set<Long> koblingIder, List<BeregningsgrunnlagPeriodeRegelType> regelTyper) {
        TypedQuery<RegelSporingPeriodeEntitet> query = entityManager.createQuery(
                "from RegelSporingPeriodeEntitet sporing " +
                        "where sporing.koblingId in :koblingId " +
                        "and sporing.aktiv = :aktiv " +
                        "and sporing.regelType in :regeltype", RegelSporingPeriodeEntitet.class); //$NON-NLS-1$
        query.setParameter(KOBLING_ID, koblingIder); //$NON-NLS-1$
        query.setParameter("aktiv", true); //$NON-NLS-1$
        query.setParameter("regeltype", regelTyper);
        return query.getResultList();
    }

    /**
     * Henter aktiv RegelsporingPeriode med gitt type
     *
     * @param koblingId en koblingId
     * @return Alle aktive {@link no.nav.folketrygdloven.kalkulus.domene.entiteter.sporing.RegelSporingPeriodeEntitet}
     */
    public List<RegelSporingGrunnlagEntitet> hentRegelSporingGrunnlagMedGittType(Long koblingId, List<BeregningsgrunnlagRegelType> regelTyper) {
        TypedQuery<RegelSporingGrunnlagEntitet> query = entityManager.createQuery(
                "from RegelSporingGrunnlagEntitet sporing " +
                        "where sporing.koblingId=:koblingId " +
                        "and sporing.aktiv = :aktiv " +
                        "and sporing.regelType in :regeltype", RegelSporingGrunnlagEntitet.class); //$NON-NLS-1$
        query.setParameter(KOBLING_ID, koblingId); //$NON-NLS-1$
        query.setParameter("aktiv", true); //$NON-NLS-1$
        query.setParameter("regeltype", regelTyper);
        return query.getResultList();
    }

}
