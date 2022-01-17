package no.nav.folketrygdloven.kalkulus.tjeneste.forlengelse;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.forlengelse.ForlengelseperioderEntitet;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningSteg;

@ApplicationScoped
public class ForlengelseRepository {

    private EntityManager entityManager;

    ForlengelseRepository() {
        // CDI
    }

    @Inject
    public ForlengelseRepository(EntityManager entityManager) {
        Objects.requireNonNull(entityManager, "entityManager"); //$NON-NLS-1$
        this.entityManager = entityManager;
    }

    public List<ForlengelseperioderEntitet> hentAktivePerioderForKoblingId(Set<Long> koblingIder) {
        TypedQuery<ForlengelseperioderEntitet> query = entityManager.createQuery("FROM Forlengelseperioder fp WHERE koblingId in (:koblingIder) and aktiv = true", ForlengelseperioderEntitet.class);
        query.setParameter("koblingIder", koblingIder);
        return query.getResultList();
    }

    public void lagre(List<ForlengelseperioderEntitet> forlengelser) {
        var eksisterende = hentAktivePerioderForKoblingId(forlengelser.stream().map(ForlengelseperioderEntitet::getKoblingId).collect(Collectors.toSet()));
        eksisterende.forEach(gammel -> {
            gammel.setAktiv(false);
            entityManager.persist(gammel);
            entityManager.flush();
        });
        forlengelser.forEach(entityManager::persist);
        entityManager.flush();
    }


    public void deaktiverAlle(Set<Long> koblingIder) {
        var eksisterende = hentAktivePerioderForKoblingId(koblingIder);
        eksisterende.forEach(gammel -> {
            gammel.setAktiv(false);
            entityManager.persist(gammel);
            entityManager.flush();
        });
    }


}
