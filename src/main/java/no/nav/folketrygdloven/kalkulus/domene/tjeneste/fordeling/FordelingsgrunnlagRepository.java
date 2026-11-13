package no.nav.folketrygdloven.kalkulus.domene.tjeneste.fordeling;

import static no.nav.vedtak.felles.jpa.HibernateVerktøy.hentUniktResultat;

import java.util.Objects;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.fordeling.FordelingGrunnlagEntitet;

@ApplicationScoped
public class FordelingsgrunnlagRepository {

    private EntityManager entityManager;

    protected FordelingsgrunnlagRepository() {
        // CDI
    }

    @Inject
    public FordelingsgrunnlagRepository(EntityManager entityManager) {
        this.entityManager = Objects.requireNonNull(entityManager, "entityManager");
    }

    public Optional<FordelingGrunnlagEntitet> hentAktivtGrunnlag(Long koblingId) {
        var query = entityManager.createQuery(
            "from FordelingGrunnlagEntitet grunnlag where grunnlag.koblingId = :koblingId and grunnlag.aktiv = true",
            FordelingGrunnlagEntitet.class);
        query.setParameter("koblingId", Objects.requireNonNull(koblingId, "koblingId"));
        return hentUniktResultat(query);
    }

    public FordelingGrunnlagEntitet lagre(Long koblingId, FordelingGrunnlagEntitet.Builder builder) {
        var nyttGrunnlag = Objects.requireNonNull(builder, "builder").build(koblingId);
        hentAktivtGrunnlag(koblingId).ifPresent(grunnlag -> {
            grunnlag.setAktiv(false);
            // Frigjør den unike aktiv-indeksen før Hibernate setter inn det nye grunnlaget.
            entityManager.flush();
        });
        entityManager.persist(nyttGrunnlag);
        entityManager.flush();
        return nyttGrunnlag;
    }
}
