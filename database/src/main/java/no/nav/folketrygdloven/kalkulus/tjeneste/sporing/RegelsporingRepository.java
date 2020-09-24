package no.nav.folketrygdloven.kalkulus.tjeneste.sporing;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.sporing.RegelSporingGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.sporing.RegelSporingPeriodeEntitet;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagPeriodeRegelType;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagRegelType;
import no.nav.folketrygdloven.kalkulus.felles.verktøy.HibernateVerktøy;

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
     * Henter alle aktive RegelsporingGrunnlag
     *
     * @param koblingId en koblingId
     * @return Alle aktive {@link RegelSporingGrunnlagEntitet}
     */
    public List<RegelSporingGrunnlagEntitet> hentAlleRegelSporingGrunnlag(Long koblingId) {
        TypedQuery<RegelSporingGrunnlagEntitet> query = entityManager.createQuery(
                "from RegelSporingGrunnlag sporing " +
                        "where sporing.koblingId=:koblingId " +
                        "and sporing.aktiv = :aktivt", RegelSporingGrunnlagEntitet.class); //$NON-NLS-1$
        query.setParameter(KOBLING_ID, koblingId); //$NON-NLS-1$
        query.setParameter("aktiv", true); //$NON-NLS-1$
        return query.getResultList();
    }

    /**
     * Henter alle aktive RegelsporingPeriode
     *
     * @param koblingId en koblingId
     * @return Alle aktive {@link no.nav.folketrygdloven.kalkulus.domene.entiteter.sporing.RegelSporingPeriodeEntitet}
     */
    public List<RegelSporingPeriodeEntitet> hentAlleRegelSporingPerioder(Long koblingId) {
        TypedQuery<RegelSporingPeriodeEntitet> query = entityManager.createQuery(
                "from RegelSporingPeriodeEntitet sporing " +
                        "where sporing.koblingId=:koblingId " +
                        "and sporing.aktiv = :aktivt", RegelSporingPeriodeEntitet.class); //$NON-NLS-1$
        query.setParameter(KOBLING_ID, koblingId); //$NON-NLS-1$
        query.setParameter("aktiv", true); //$NON-NLS-1$
        return query.getResultList();
    }

    /**
     * Lagrer regelsporing for periode
     *
     * @param regelSporingPeriode reglsporingperiode som skal lagres
     */
    public void lagre(RegelSporingPeriodeEntitet regelSporingPeriode) {
        if (!regelSporingPeriode.erAktiv()) {
            throw new IllegalArgumentException("Kan ikke lagre en inaktivt reglsporing");
        }
        var eksisterendeRegelsporing = hentRegelSporingPeriodeMedGittType(regelSporingPeriode.getKoblingId(), regelSporingPeriode.getRegelType());
        eksisterendeRegelsporing.ifPresent(rs -> {
            rs.setAktiv(false);
            entityManager.persist(rs);
        });
        entityManager.persist(regelSporingPeriode);
    }

    /**
     * Lagrer regelsporing
     *
     * @param regelSporingGrunnlag Regelsporing
     */
    public void lagre(RegelSporingGrunnlagEntitet regelSporingGrunnlag) {
        if (!regelSporingGrunnlag.erAktiv()) {
            throw new IllegalArgumentException("Kan ikke lagre en inaktivt reglsporing");
        }
        var eksisterendeRegelsporing = hentRegelSporingGrunnlagMedGittType(regelSporingGrunnlag.getKoblingId(), regelSporingGrunnlag.getRegelType());
        eksisterendeRegelsporing.ifPresent(rs -> {
            rs.setAktiv(false);
            entityManager.persist(rs);
        });
        entityManager.persist(regelSporingGrunnlag);
    }

    /**
     * Henter aktiv RegelsporingPeriode med gitt type
     *
     * @param koblingId en koblingId
     * @return Alle aktive {@link no.nav.folketrygdloven.kalkulus.domene.entiteter.sporing.RegelSporingPeriodeEntitet}
     */
    private Optional<RegelSporingPeriodeEntitet> hentRegelSporingPeriodeMedGittType(Long koblingId, BeregningsgrunnlagPeriodeRegelType regelType) {
        TypedQuery<RegelSporingPeriodeEntitet> query = entityManager.createQuery(
                "from RegelSporingPeriodeEntitet sporing " +
                        "where sporing.koblingId=:koblingId " +
                        "and sporing.aktiv = :aktivt " +
                "and sporing.regelType = :regeltype", RegelSporingPeriodeEntitet.class); //$NON-NLS-1$
        query.setParameter(KOBLING_ID, koblingId); //$NON-NLS-1$
        query.setParameter("aktiv", true); //$NON-NLS-1$
        query.setParameter("regeltype", regelType);
        return HibernateVerktøy.hentUniktResultat(query);
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
                        "and sporing.aktiv = :aktivt " +
                        "and sporing.regelType = :regeltype", RegelSporingGrunnlagEntitet.class); //$NON-NLS-1$
        query.setParameter(KOBLING_ID, koblingId); //$NON-NLS-1$
        query.setParameter("aktiv", true); //$NON-NLS-1$
        query.setParameter("regeltype", regelType);
        return HibernateVerktøy.hentUniktResultat(query);
    }


}
