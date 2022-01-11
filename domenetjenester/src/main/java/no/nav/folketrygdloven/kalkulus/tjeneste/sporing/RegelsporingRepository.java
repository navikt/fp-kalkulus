package no.nav.folketrygdloven.kalkulus.tjeneste.sporing;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
                    entityManager.persist(sporing);
                }));
        entityManager.flush();
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
     * @param tilstand  Beregningsgrunnlagtilstand
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
