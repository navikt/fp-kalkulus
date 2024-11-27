package no.nav.folketrygdloven.kalkulus.domene.entiteter.sporing;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import no.nav.folketrygdloven.fpkalkulus.kontrakt.migrering.RegelSporingGrunnlagMigreringDto;

import no.nav.folketrygdloven.fpkalkulus.kontrakt.migrering.RegelSporingPeriodeMigreringDto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.folketrygdloven.kalkulator.output.RegelSporingPeriode;
import no.nav.folketrygdloven.kalkulus.felles.jpa.IntervallEntitet;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagPeriodeRegelType;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagRegelType;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;

@ApplicationScoped
public class RegelsporingRepository {

    private static final Logger log = LoggerFactory.getLogger(RegelsporingRepository.class);

    private static final String KOBLING_ID = "koblingId";
    private EntityManager entityManager;

    RegelsporingRepository() {
        // for CDI proxy
    }

    @Inject
    public RegelsporingRepository(EntityManager entityManager) {
        Objects.requireNonNull(entityManager, "entityManager");
        entityManager.setProperty("hibernate.jdbc.batch_size", 50);
        this.entityManager = entityManager;
    }

    public void migrerSporinger(List<RegelSporingGrunnlagMigreringDto> grunnlagsporinger, List<RegelSporingPeriodeMigreringDto> periodesporinger, Long koblingId) {
        // Sletter alle eksisterende regelsporinger på koblingen før vi migrerer over de nye, i tilfelle migrering blir kjørt flere ganger
        slettAlleSporingerPåKobling(koblingId);
        for (RegelSporingPeriodeMigreringDto sporing : periodesporinger) {
            RegelSporingPeriodeEntitet entitet = RegelSporingPeriodeEntitet.ny()
                .medRegelEvaluering(sporing.getRegelEvaluering())
                .medRegelVersjon(sporing.getRegelVersjon())
                .medRegelInput(sporing.getRegelInput())
                .medPeriode(IntervallEntitet.fraOgMedTilOgMed(sporing.getPeriode().getFom(), sporing.getPeriode().getTom()))
                .build(koblingId, sporing.getRegelType());
            entityManager.persist(entitet);
        }
        for (RegelSporingGrunnlagMigreringDto sporing : grunnlagsporinger) {
            RegelSporingGrunnlagEntitet entitet = RegelSporingGrunnlagEntitet.ny()
                .medRegelEvaluering(sporing.getRegelEvaluering())
                .medRegelVersjon(sporing.getRegelVersjon())
                .medRegelInput(sporing.getRegelInput())
                .build(koblingId, sporing.getRegelType());
            entityManager.persist(entitet);
        }
        entityManager.flush();
    }

    private void slettAlleSporingerPåKobling(Long koblingId) {
        // Sletter grunnlag-sproring
        var grunnlagQuery = entityManager.createNativeQuery("delete from REGEL_SPORING_GRUNNLAG " +
                "where kobling_id = :koblingId")
            .setParameter("koblingId", koblingId);
        var oppdaterteRader = grunnlagQuery.executeUpdate();
        log.info("Slettet {} regelsporringsgrunnlag for koblingId={}", oppdaterteRader, koblingId);

        // Sletter periode-sporing
        var perioderQuery = entityManager.createNativeQuery("delete from REGEL_SPORING_PERIODE " +
                "where kobling_id = :koblingId")
            .setParameter("koblingId", koblingId);

        var perioderOppdaterteRader = perioderQuery.executeUpdate();
        log.info("Slettet {} regelsporringsperioder for koblingId={}", perioderOppdaterteRader, koblingId);

        entityManager.flush();
    }

    public void lagreSporinger(Map<String, List<RegelSporingPeriode>> perioderPrRegelInputHash, Long koblingId) {
        int antall = 0;
        int størrelseRegelEvaluering = 0;

        for (var entry : perioderPrRegelInputHash.entrySet()) {
            for (RegelSporingPeriode sporing : entry.getValue()) {
                RegelSporingPeriodeEntitet entitet = RegelSporingPeriodeEntitet.ny()
                    .medRegelEvaluering(sporing.regelEvaluering())
                    .medRegelVersjon(sporing.regelVersjon())
                    .medRegelInput(sporing.regelInput())
                    .medPeriode(IntervallEntitet.fraOgMedTilOgMed(sporing.periode().getFomDato(), sporing.periode().getTomDato()))
                    .build(koblingId, sporing.regelType());
                entityManager.persist(entitet);

                antall++;
                størrelseRegelEvaluering += sporing.regelEvaluering().length();
            }
        }
        entityManager.flush();

        log.info("Lagret {} regelsporingperioder med {} kb regelevaluering", antall, (størrelseRegelEvaluering + 512) / 1024);
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
                "and sporing.regelType in :regeltype", RegelSporingPeriodeEntitet.class);
        query.setParameter(KOBLING_ID, koblingId);
        query.setParameter("aktiv", true);
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
                "and sporing.regelType in :regeltype", RegelSporingGrunnlagEntitet.class);
        query.setParameter(KOBLING_ID, koblingId);
        query.setParameter("aktiv", true);
        query.setParameter("regeltype", regelTyper);
        return query.getResultList();
    }

}
