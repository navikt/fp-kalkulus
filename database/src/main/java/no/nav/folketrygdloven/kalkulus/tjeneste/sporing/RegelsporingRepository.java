package no.nav.folketrygdloven.kalkulus.tjeneste.sporing;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.sporing.RegelSporingGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.sporing.RegelSporingPeriodeEntitet;
import no.nav.folketrygdloven.kalkulus.felles.verktøy.HibernateVerktøy;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagPeriodeRegelType;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagRegelType;

@ApplicationScoped
public class RegelsporingRepository {

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
        regelSporingPerioder.forEach((key, value) -> {
            var eksisterendeRegelsporinger = hentRegelSporingPeriodeMedGittType(koblingId, key);
            eksisterendeRegelsporinger.forEach(rs -> {
                rs.setAktiv(false);
                entityManager.persist(rs);
            });
            value.stream().map(builder -> builder.build(koblingId, key)).forEach(sporing -> {
                if (!sporing.erAktiv()) {
                    throw new IllegalArgumentException("Kan ikke lagre en inaktivt reglsporing");
                }
                entityManager.persist(sporing);
            });
        });
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
        var eksisterendeRegelsporing = hentRegelSporingGrunnlagMedGittType(koblingId, regelType);
        eksisterendeRegelsporing.ifPresent(rs -> {
            rs.setAktiv(false);
            entityManager.persist(rs);
        });
        entityManager.persist(regelSporingGrunnlag.build(koblingId, regelType));
        entityManager.flush();
    }

    /**
     * Henter aktiv RegelsporingPeriode med gitt type
     *
     * @param koblingId en koblingId
     * @return Alle aktive {@link no.nav.folketrygdloven.kalkulus.domene.entiteter.sporing.RegelSporingPeriodeEntitet}
     */
    private List<RegelSporingPeriodeEntitet> hentRegelSporingPeriodeMedGittType(Long koblingId, BeregningsgrunnlagPeriodeRegelType regelType) {
        TypedQuery<RegelSporingPeriodeEntitet> query = entityManager.createQuery(
                "from RegelSporingPeriodeEntitet sporing " +
                        "where sporing.koblingId=:koblingId " +
                        "and sporing.aktiv = :aktiv " +
                "and sporing.regelType = :regeltype", RegelSporingPeriodeEntitet.class); //$NON-NLS-1$
        query.setParameter(KOBLING_ID, koblingId); //$NON-NLS-1$
        query.setParameter("aktiv", true); //$NON-NLS-1$
        query.setParameter("regeltype", regelType);
        return query.getResultList();
    }

    /**
     * Henter aktiv RegelsporingPeriode med gitt type
     *
     * @param koblingId en koblingId
     * @return Alle aktive {@link no.nav.folketrygdloven.kalkulus.domene.entiteter.sporing.RegelSporingPeriodeEntitet}
     */
    private Optional<RegelSporingGrunnlagEntitet> hentRegelSporingGrunnlagMedGittType(Long koblingId, BeregningsgrunnlagRegelType regelType) {
        TypedQuery<RegelSporingGrunnlagEntitet> query = entityManager.createQuery(
                "from RegelSporingGrunnlagEntitet sporing " +
                        "where sporing.koblingId=:koblingId " +
                        "and sporing.aktiv = :aktiv " +
                        "and sporing.regelType = :regeltype", RegelSporingGrunnlagEntitet.class); //$NON-NLS-1$
        query.setParameter(KOBLING_ID, koblingId); //$NON-NLS-1$
        query.setParameter("aktiv", true); //$NON-NLS-1$
        query.setParameter("regeltype", regelType);
        return HibernateVerktøy.hentUniktResultat(query);
    }


}
