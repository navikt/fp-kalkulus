package no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag;


import static no.nav.vedtak.felles.jpa.HibernateVerktøy.hentEksaktResultat;
import static no.nav.vedtak.felles.jpa.HibernateVerktøy.hentUniktResultat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.BeregningSats;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningAktivitetAggregatEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningAktivitetOverstyringerEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningRefusjonOverstyringEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningRefusjonOverstyringerEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagBuilder;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BesteberegninggrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.FaktaAggregatEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.SammenligningsgrunnlagPrStatusEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.GrunnlagReferanse;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningSatsType;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;


@ApplicationScoped
public class BeregningsgrunnlagRepository {
    private static final String KOBLING_ID = "koblingId";
    private static final String BEREGNINGSGRUNNLAG_TILSTAND = "beregningsgrunnlagTilstand";
    private static final String BUILDER = "beregningsgrunnlagGrunnlagBuilder";
    private EntityManager entityManager;

    protected BeregningsgrunnlagRepository() {
        // for CDI proxy
    }

    @Inject
    public BeregningsgrunnlagRepository(EntityManager entityManager) {
        Objects.requireNonNull(entityManager, "entityManager");
        this.entityManager = entityManager;
    }

    /**
     * Henter siste {@link BeregningsgrunnlagGrunnlagEntitet} opprettet i et bestemt steg for revurdering. Ignorerer om grunnlaget er aktivt
     * eller ikke.
     * Om revurderingen ikke har grunnlag opprettet i denne tilstanden returneres grunnlaget fra originalbehandlingen for samme tilstand.
     *
     * @param kobling                    en kobling
     * @param beregningsgrunnlagTilstand steget {@link BeregningsgrunnlagGrunnlagEntitet} er opprettet i
     * @param originalKoblingEntitet
     * @return Hvis det finnes et eller fler BeregningsgrunnlagGrunnlagEntitet som har blitt opprettet i {@code stegOpprettet} returneres den
     * som ble opprettet sist
     */
    public Optional<BeregningsgrunnlagGrunnlagEntitet> hentSisteBeregningsgrunnlagGrunnlagEntitetForBehandlinger(KoblingEntitet kobling,
                                                                                                                 BeregningsgrunnlagTilstand beregningsgrunnlagTilstand,
                                                                                                                 Optional<KoblingEntitet> originalKoblingEntitet) {
        Optional<BeregningsgrunnlagGrunnlagEntitet> sisteBg = hentSisteBeregningsgrunnlagGrunnlagEntitet(kobling.getId(), beregningsgrunnlagTilstand);
        if (sisteBg.isEmpty() && originalKoblingEntitet.isPresent()) {
            // Henter siste grunnlaget som ble lagret med samme skjæringstidspunkt (tilsvarer original kobling)
            var originalGrunnlag = hentSisteBeregningsgrunnlagGrunnlagEntitet(originalKoblingEntitet.get().getId(), beregningsgrunnlagTilstand);
            return originalGrunnlag;
        }
        return sisteBg;
    }

    /**
     * Henter alle aktive BeregningsgrunnlagGrunnlagEntiteter
     *
     * @param koblingIds en liste med koblingId
     * @return Liste med alle aktive grunnlag som matcher koblingider {@link BeregningsgrunnlagGrunnlagEntitet}
     */
    public List<BeregningsgrunnlagGrunnlagEntitet> hentBeregningsgrunnlagGrunnlagEntiteter(Collection<Long> koblingIds) {
        TypedQuery<BeregningsgrunnlagGrunnlagEntitet> query = entityManager.createQuery(
                "select grunnlag from BeregningsgrunnlagGrunnlagEntitet grunnlag " +
                        "where grunnlag.koblingId in :koblingId " +
                        "and grunnlag.aktiv = :aktivt",
                BeregningsgrunnlagGrunnlagEntitet.class);
        query.setParameter(KOBLING_ID, koblingIds); // $NON-NLS-1$
        query.setParameter("aktivt", true);
        return query.getResultList();
    }

    /**
     * Henter aktivt BeregningsgrunnlagGrunnlagEntitet
     *
     * @param koblingId en koblingId
     * @return Hvis det finnes en aktiv {@link BeregningsgrunnlagGrunnlagEntitet} returneres denne
     */
    public Optional<BeregningsgrunnlagGrunnlagEntitet> hentBeregningsgrunnlagGrunnlagEntitet(Long koblingId) {
        TypedQuery<BeregningsgrunnlagGrunnlagEntitet> query = entityManager.createQuery(
                "from BeregningsgrunnlagGrunnlagEntitet grunnlag " +
                        "where grunnlag.koblingId=:koblingId " +
                        "and grunnlag.aktiv = :aktivt",
                BeregningsgrunnlagGrunnlagEntitet.class);
        query.setParameter(KOBLING_ID, koblingId); // $NON-NLS-1$
        query.setParameter("aktivt", true);
        return hentUniktResultat(query);
    }

    /**
     * Henter aktivt BeregningsgrunnlagGrunnlagEntitet
     *
     * @param koblingId         en koblingId
     * @param grunnlagReferanse en referanse til et lagret grunnlag
     * @return Hvis det finnes en aktiv {@link BeregningsgrunnlagGrunnlagEntitet} returneres denne
     */
    public Optional<BeregningsgrunnlagGrunnlagEntitet> hentBeregningsgrunnlagGrunnlagEntitetForReferanse(Long koblingId, UUID grunnlagReferanse) {
        TypedQuery<BeregningsgrunnlagGrunnlagEntitet> query = entityManager.createQuery(
                "from BeregningsgrunnlagGrunnlagEntitet grunnlag " +
                        "where grunnlag.koblingId=:koblingId " +
                        "and grunnlag.grunnlagReferanse.referanse = :referanse",
                BeregningsgrunnlagGrunnlagEntitet.class);
        query.setParameter(KOBLING_ID, koblingId); // $NON-NLS-1$
        query.setParameter("referanse", grunnlagReferanse);
        return hentUniktResultat(query);
    }

    public Optional<BeregningsgrunnlagGrunnlagEntitet> hentFørsteFastsatteGrunnlagForSak(List<Long> alleKoblingIderForSaksnummer) {
        TypedQuery<BeregningsgrunnlagGrunnlagEntitet> query = entityManager.createQuery(
            "from BeregningsgrunnlagGrunnlagEntitet grunnlag " +
                "where grunnlag.koblingId in :koblingListe " +
                "and grunnlag.beregningsgrunnlagTilstand = :beregningsgrunnlagTilstand " +
                "order by grunnlag.opprettetTidspunkt asc",
            BeregningsgrunnlagGrunnlagEntitet.class);
        query.setParameter("koblingListe", alleKoblingIderForSaksnummer); // $NON-NLS-1$
        query.setParameter(BEREGNINGSGRUNNLAG_TILSTAND, BeregningsgrunnlagTilstand.FASTSATT);
        query.setMaxResults(1);
        List<BeregningsgrunnlagGrunnlagEntitet> resultatListe = query.getResultList();
        if (resultatListe.size() > 1) {
            throw new IllegalArgumentException("Flere enn en rader");
        }
        return resultatListe.size() == 1 ? Optional.of(resultatListe.getFirst()) : Optional.empty();
    }


    /**
     * Henter siste {@link BeregningsgrunnlagGrunnlagEntitet} opprettet i et bestemt steg. Ignorerer om grunnlaget er aktivt eller ikke.
     *
     * @param koblingId                  en koblingId
     * @param beregningsgrunnlagTilstand steget {@link BeregningsgrunnlagGrunnlagEntitet} er opprettet i
     * @return Hvis det finnes et eller fler BeregningsgrunnlagGrunnlagEntitet som har blitt opprettet i {@code stegOpprettet} returneres den
     * som ble opprettet sist
     */
    public Optional<BeregningsgrunnlagGrunnlagEntitet> hentSisteBeregningsgrunnlagGrunnlagEntitet(Long koblingId,
                                                                                                  BeregningsgrunnlagTilstand beregningsgrunnlagTilstand) {
        TypedQuery<BeregningsgrunnlagGrunnlagEntitet> query = entityManager.createQuery(
                "select b from BeregningsgrunnlagGrunnlagEntitet b " +
                        "where b.koblingId=:koblingId " +
                        "and b.beregningsgrunnlagTilstand = :beregningsgrunnlagTilstand " +
                        "order by b.opprettetTidspunkt desc, b.id desc",
                BeregningsgrunnlagGrunnlagEntitet.class);
        query.setParameter(KOBLING_ID, koblingId); // $NON-NLS-1$
        query.setParameter(BEREGNINGSGRUNNLAG_TILSTAND, beregningsgrunnlagTilstand); // $NON-NLS-1$
        var resultList = query.getResultList();
        if (resultList.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(resultList.iterator().next());
    }


    /**
     * Henter siste {@link BeregningsgrunnlagGrunnlagEntitet} opprettet i et bestemt steg. Ignorerer om grunnlaget er aktivt eller ikke.
     *
     * @param koblingId                  en koblingId
     * @param opprettetTidspunktMin      minste opprettet tidspunkt
     * @param beregningsgrunnlagTilstand steget {@link BeregningsgrunnlagGrunnlagEntitet} er opprettet i
     * @return Hvis det finnes et eller fler BeregningsgrunnlagGrunnlagEntitet som har blitt opprettet i {@code stegOpprettet} returneres den
     * som ble opprettet sist
     */
    public Optional<BeregningsgrunnlagGrunnlagEntitet> hentSisteBeregningsgrunnlagGrunnlagEntitetOpprettetEtter(Long koblingId,
                                                                                                                LocalDateTime opprettetTidspunktMin,
                                                                                                                BeregningsgrunnlagTilstand beregningsgrunnlagTilstand) {
        TypedQuery<BeregningsgrunnlagGrunnlagEntitet> query = entityManager.createQuery(
                "from BeregningsgrunnlagGrunnlagEntitet " +
                        "where koblingId=:koblingId " +
                        "and beregningsgrunnlagTilstand = :beregningsgrunnlagTilstand " +
                        "and opprettetTidspunkt > :opprettetTidspunktMin " +
                        "order by opprettetTidspunkt desc, id desc",
                BeregningsgrunnlagGrunnlagEntitet.class);
        query.setParameter(KOBLING_ID, koblingId); // $NON-NLS-1$
        query.setParameter(BEREGNINGSGRUNNLAG_TILSTAND, beregningsgrunnlagTilstand); // $NON-NLS-1$
        query.setParameter("opprettetTidspunktMin", opprettetTidspunktMin); // $NON-NLS-1$
        query.setMaxResults(1);
        return hentUniktResultat(query);
    }

    /**
     * Henter siste {@link BeregningsgrunnlagGrunnlagEntitet} opprettet i et bestemt steg for alle koblinger. Ignorerer om grunnlaget er aktivt
     * eller ikke.
     *
     * @param koblingId                 en liste med koblingId
     * @param beregningsgrunnlagTilstand steget {@link BeregningsgrunnlagGrunnlagEntitet} er opprettet i
     * @param opprettetTidMax
     * @return Liste med grunnlag fra gitt {@link BeregningsgrunnlagTilstand} som ble opprettet sist pr kobling
     */
    @SuppressWarnings("unchecked")
    public Optional<BeregningsgrunnlagGrunnlagEntitet> hentSisteBeregningsgrunnlagGrunnlagEntitetForKobling(Long koblingId,
                                                                                                            BeregningsgrunnlagTilstand beregningsgrunnlagTilstand,
                                                                                                            LocalDateTime opprettetTidMax) {
        List<BeregningsgrunnlagGrunnlagEntitet> resultatListe;
        if (opprettetTidMax != null) {
            Query query = entityManager.createNativeQuery(
                    "SELECT GR.* FROM GR_BEREGNINGSGRUNNLAG GR " +
                            "WHERE GR.OPPRETTET_TID = (SELECT max(GR2.OPPRETTET_TID) FROM GR_BEREGNINGSGRUNNLAG GR2 " +
                            "WHERE GR2.KOBLING_ID = GR.KOBLING_ID AND " +
                            "GR2.STEG_OPPRETTET = :beregningsgrunnlagTilstand AND " +
                            "GR2.OPPRETTET_TID <= :opprettetTidMax) AND " +
                            "GR.KOBLING_ID = :koblingId AND " +
                            "GR.STEG_OPPRETTET = :beregningsgrunnlagTilstand",
                    BeregningsgrunnlagGrunnlagEntitet.class);
            query.setParameter(KOBLING_ID, koblingId); // $NON-NLS-1$
            query.setParameter("opprettetTidMax", opprettetTidMax);
            query.setParameter(BEREGNINGSGRUNNLAG_TILSTAND, beregningsgrunnlagTilstand.getKode()); // $NON-NLS-1$
            resultatListe = query.getResultList();
        } else {
            Query query = entityManager.createNativeQuery(
                    "SELECT GR.* FROM GR_BEREGNINGSGRUNNLAG GR " +
                            "WHERE GR.OPPRETTET_TID = (SELECT max(GR2.OPPRETTET_TID) FROM GR_BEREGNINGSGRUNNLAG GR2 " +
                            "WHERE GR2.KOBLING_ID = GR.KOBLING_ID AND " +
                            "GR2.STEG_OPPRETTET = :beregningsgrunnlagTilstand) AND " +
                            "GR.KOBLING_ID = :koblingId AND " +
                            "GR.STEG_OPPRETTET = :beregningsgrunnlagTilstand",
                    BeregningsgrunnlagGrunnlagEntitet.class);
            query.setParameter(KOBLING_ID, koblingId); // $NON-NLS-1$
            query.setParameter(BEREGNINGSGRUNNLAG_TILSTAND, beregningsgrunnlagTilstand.getKode()); // $NON-NLS-1$
            resultatListe = query.getResultList();
        }
        if (resultatListe.size() > 1) {
            throw new IllegalArgumentException("Fant mer enn en rad");
        }
        return resultatListe.size() == 1 ? Optional.of(resultatListe.get(0)) : Optional.empty();
    }


    /**
     * Henter siste {@link BeregningsgrunnlagGrunnlagEntitet} opprettet i et bestemt steg. Ignorerer om grunnlaget er aktivt eller ikke.
     *
     * @param koblingId                  en koblingId
     * @param opprettetTidspunktMax      største opprettet tidspunkt
     * @param beregningsgrunnlagTilstand steget {@link BeregningsgrunnlagGrunnlagEntitet} er opprettet i
     * @return Hvis det finnes et eller fler BeregningsgrunnlagGrunnlagEntitet som har blitt opprettet i {@code stegOpprettet} returneres den
     * som ble opprettet sist
     */
    public Optional<BeregningsgrunnlagGrunnlagEntitet> hentSisteBeregningsgrunnlagGrunnlagEntitetOpprettetFør(Long koblingId,
                                                                                                              LocalDateTime opprettetTidspunktMax,
                                                                                                              BeregningsgrunnlagTilstand beregningsgrunnlagTilstand) {
        TypedQuery<BeregningsgrunnlagGrunnlagEntitet> query = entityManager.createQuery(
                "from BeregningsgrunnlagGrunnlagEntitet " +
                        "where koblingId=:koblingId " +
                        "and beregningsgrunnlagTilstand = :beregningsgrunnlagTilstand " +
                        "and opprettetTidspunkt < :opprettetTidspunktMax " +
                        "order by opprettetTidspunkt desc, id desc",
                BeregningsgrunnlagGrunnlagEntitet.class);
        query.setParameter(KOBLING_ID, koblingId); // $NON-NLS-1$
        query.setParameter(BEREGNINGSGRUNNLAG_TILSTAND, beregningsgrunnlagTilstand); // $NON-NLS-1$
        query.setParameter("opprettetTidspunktMax", opprettetTidspunktMax); // $NON-NLS-1$
        query.setMaxResults(1);
        return hentUniktResultat(query);
    }


    public BeregningSats finnGrunnbeløp(LocalDate dato) {
        TypedQuery<BeregningSats> query = entityManager.createQuery("from BeregningSats where satsType=:satsType" +
                " and periode.fomDato<=:dato" +
                " and periode.tomDato>=:dato", BeregningSats.class);

        query.setParameter("satsType", BeregningSatsType.GRUNNBELØP);
        query.setParameter("dato", dato);
        query.getResultList();
        return hentEksaktResultat(query);
    }

    public List<BeregningSats> finnAlleSatser() {
        TypedQuery<BeregningSats> query = entityManager.createQuery("from BeregningSats", BeregningSats.class);
        query.getResultList();
        return query.getResultList();
    }

    public BeregningsgrunnlagGrunnlagEntitet lagre(Long koblingId, BeregningsgrunnlagGrunnlagBuilder builder,
                                                   BeregningsgrunnlagTilstand beregningsgrunnlagTilstand) {
        Objects.requireNonNull(koblingId, KOBLING_ID);
        Objects.requireNonNull(builder, BUILDER);
        Objects.requireNonNull(beregningsgrunnlagTilstand, BEREGNINGSGRUNNLAG_TILSTAND);
        BeregningsgrunnlagGrunnlagEntitet grunnlagEntitet = builder.build(koblingId, beregningsgrunnlagTilstand);
        return lagreOgFlush(koblingId, grunnlagEntitet);
    }

    private BeregningsgrunnlagGrunnlagEntitet lagreOgFlush(Long koblingId, BeregningsgrunnlagGrunnlagEntitet nyttGrunnlag) {
        Objects.requireNonNull(koblingId, KOBLING_ID);
        Objects.requireNonNull(nyttGrunnlag.getBeregningsgrunnlagTilstand(), BEREGNINGSGRUNNLAG_TILSTAND);
        Optional<BeregningsgrunnlagGrunnlagEntitet> tidligereAggregat = hentBeregningsgrunnlagGrunnlagEntitet(koblingId);
        if (tidligereAggregat.isPresent()) {
            nyttGrunnlag = settFaktaFraTidligere(koblingId, nyttGrunnlag, tidligereAggregat);
            nyttGrunnlag = settBesteberegningFraTidligere(koblingId, nyttGrunnlag, tidligereAggregat);
            tidligereAggregat.get().setAktiv(false);
            entityManager.persist(tidligereAggregat.get());
        }
        if (nyttGrunnlag.getGrunnlagReferanse() == null) {
            // lag ny referanse
            nyttGrunnlag.setGrunnlagReferanse(new GrunnlagReferanse(UUID.randomUUID()));
        }
        lagreGrunnlag(nyttGrunnlag);
        entityManager.flush();
        return nyttGrunnlag;
    }

    private BeregningsgrunnlagGrunnlagEntitet settBesteberegningFraTidligere(Long koblingId,
                                                                             BeregningsgrunnlagGrunnlagEntitet nyttGrunnlag,
                                                                             Optional<BeregningsgrunnlagGrunnlagEntitet> tidligereAggregat) {
        var nyttBg = nyttGrunnlag.getBeregningsgrunnlag();
        var nyBesteberegning = nyttBg.flatMap(BeregningsgrunnlagEntitet::getBesteberegninggrunnlag);
        var tidligereBesteberegning = tidligereAggregat.flatMap(BeregningsgrunnlagGrunnlagEntitet::getBeregningsgrunnlag)
            .flatMap(BeregningsgrunnlagEntitet::getBesteberegninggrunnlag);
        var kanKopiereBesteberegning = nyBesteberegning.isEmpty() && tidligereBesteberegning.isPresent();
        if (kanKopiereBesteberegning && nyttBg.isPresent()) {
            var nyttBGMedBesteberegning = BeregningsgrunnlagEntitet.kopiere(nyttBg.get())
                .medBesteberegninggrunnlag(BesteberegninggrunnlagEntitet.kopier(tidligereBesteberegning.get()).build())
                .build();
            nyttGrunnlag = BeregningsgrunnlagGrunnlagBuilder.kopiere(nyttGrunnlag)
                .medBeregningsgrunnlag(nyttBGMedBesteberegning)
                .build(koblingId, nyttGrunnlag.getBeregningsgrunnlagTilstand());
        }
        return nyttGrunnlag;
    }

    private BeregningsgrunnlagGrunnlagEntitet settFaktaFraTidligere(Long koblingId, BeregningsgrunnlagGrunnlagEntitet nyttGrunnlag, Optional<BeregningsgrunnlagGrunnlagEntitet> tidligereAggregat) {
        if (nyttGrunnlag.getFaktaAggregat().isEmpty()) {
            nyttGrunnlag = BeregningsgrunnlagGrunnlagBuilder.kopiere(nyttGrunnlag)
                    .medFaktaAggregat(tidligereAggregat.get().getFaktaAggregat().orElse(null))
                    .build(koblingId, nyttGrunnlag.getBeregningsgrunnlagTilstand());
        }
        return nyttGrunnlag;
    }

    private void lagreGrunnlag(BeregningsgrunnlagGrunnlagEntitet nyttGrunnlag) {
        if (nyttGrunnlag.getId() != null) {
            throw new IllegalArgumentException("Kan ikke lagre et allerede lagret grunnlag");
        }

        BeregningAktivitetAggregatEntitet registerAktiviteter = nyttGrunnlag.getRegisterAktiviteter();
        if (registerAktiviteter != null) {
            lagreBeregningAktivitetAggregat(registerAktiviteter);
        }
        nyttGrunnlag.getFaktaAggregat().ifPresent(this::lagreFaktaAggregat);
        nyttGrunnlag.getSaksbehandletAktiviteter().ifPresent(this::lagreBeregningAktivitetAggregat);
        nyttGrunnlag.getOverstyring()
                .ifPresent(this::lagreOverstyring);
        nyttGrunnlag.getBeregningsgrunnlag().ifPresent(entityManager::persist);
        nyttGrunnlag.getRefusjonOverstyringer()
                .ifPresent(this::lagreRefusjonOverstyring);

        entityManager.persist(nyttGrunnlag);

        nyttGrunnlag.getBeregningsgrunnlag().stream()
                .flatMap(beregningsgrunnlagEntitet -> beregningsgrunnlagEntitet.getSammenligningsgrunnlagPrStatusListe().stream())
                .forEach(this::lagreSammenligningsgrunnlagPrStatus);
    }

    private void lagreFaktaAggregat(FaktaAggregatEntitet faktaAggregat) {
        if (faktaAggregat.getId() == null) {
            entityManager.persist(faktaAggregat);
            faktaAggregat.getFaktaArbeidsforhold().forEach(entityManager::persist);
            faktaAggregat.getFaktaAktør().ifPresent(entityManager::persist);
        }
    }

    private void lagreOverstyring(BeregningAktivitetOverstyringerEntitet beregningAktivitetOverstyringer) {
        if (beregningAktivitetOverstyringer.getId() != null) {
            throw new IllegalArgumentException("Kan ikke lagre et allerede lagret grunnlag");
        }
        entityManager.persist(beregningAktivitetOverstyringer);
        beregningAktivitetOverstyringer.getOverstyringer().forEach(entityManager::persist);
    }

    private void lagreRefusjonOverstyring(BeregningRefusjonOverstyringerEntitet beregningRefusjonOverstyringerEntitet) {
        entityManager.persist(beregningRefusjonOverstyringerEntitet);
        beregningRefusjonOverstyringerEntitet.getRefusjonOverstyringer().forEach(this::lagreRefusjonOverstyring);

    }

    private void lagreRefusjonOverstyring(BeregningRefusjonOverstyringEntitet beregningRefusjonOverstyringEntitet) {
        entityManager.persist(beregningRefusjonOverstyringEntitet);
        beregningRefusjonOverstyringEntitet.getRefusjonPerioder().forEach(entityManager::persist);
    }

    private void lagreBeregningAktivitetAggregat(BeregningAktivitetAggregatEntitet aggregat) {
        BeregningAktivitetAggregatEntitet entitet = aggregat;
        if (entitet.getId() != null) {
            throw new IllegalArgumentException("Kan ikke lagre et allerede lagret grunnlag");
        }
        entityManager.persist(entitet);
        entitet.getBeregningAktiviteter().forEach(entityManager::persist);
    }

    private void lagreSammenligningsgrunnlagPrStatus(SammenligningsgrunnlagPrStatusEntitet sammenligningsgrunnlagPrStatus) {
        if (sammenligningsgrunnlagPrStatus.getId() != null) {
            throw new IllegalArgumentException("Kan ikke lagre et allerede lagret grunnlag");
        }
        entityManager.persist(sammenligningsgrunnlagPrStatus);
    }

    public void deaktiverBeregningsgrunnlagGrunnlagEntitet(Long koblingId) {
        Optional<BeregningsgrunnlagGrunnlagEntitet> entitetOpt = hentBeregningsgrunnlagGrunnlagEntitet(koblingId);
        entitetOpt.ifPresent(this::deaktiverBeregningsgrunnlagGrunnlagEntitet);
    }

    public void deaktiverBeregningsgrunnlagGrunnlagEntiteter(List<BeregningsgrunnlagGrunnlagEntitet> entiteter) {
        endreAktivOgLagre(entiteter, false);
    }

    private void deaktiverBeregningsgrunnlagGrunnlagEntitet(BeregningsgrunnlagGrunnlagEntitet entitet) {
        endreAktivOgLagre(entitet, false);
    }

    public void endreAktivOgLagre(BeregningsgrunnlagGrunnlagEntitet entitet, boolean aktiv) {
        entitet.setAktiv(aktiv);
        entityManager.persist(entitet);
        entityManager.flush();
    }

    private void endreAktivOgLagre(List<BeregningsgrunnlagGrunnlagEntitet> entiteter, boolean aktiv) {
        entiteter.forEach(e -> {
            e.setAktiv(aktiv);
            entityManager.persist(e);
        });
        entityManager.flush();
    }

    public void reaktiverSisteMedTilstand(BeregningsgrunnlagTilstand tilstand, Long koblingId) {
        var grunnlagForKobling = hentSisteBeregningsgrunnlagGrunnlagEntitetForKobling(koblingId, tilstand, null);
        grunnlagForKobling.ifPresent(g -> {
            endreAktivOgLagre(g, true);
            entityManager.flush();
        });
    }

    /**
     * Reaktiverer grunnlag til forrige tilstand før gjeldende tilstand.
     * <p>
     * Grunnlaget som reaktiveres må oppfylle følgende:
     * - tilstand må vere før gjeldende
     * - opprettet tid må være før opprettet tid i forrige grunnlag med gjeldende tilstand
     * - er sist opprettet av grunnlag med samme tilstand
     * <p>
     * Dersom en kobling ikke har et tidligere grunnlag med gjeldende tilstand
     * benyttes opprettet tid fra grunnlag med tilstand lik neste tilstand i samsvar med tilstandrekkefølgen
     *
     * @param kobling         Koblinger med grunnlag som skal reaktiveres
     * @param gjeldendeTilstand Gjeldende tilstand
     */
    public void reaktiverForrigeGrunnlagForKoblinger(Long kobling, BeregningsgrunnlagTilstand gjeldendeTilstand) {
        var maksOppretteTidPrKobling = finnMaksOpprettetTidForGrunnlagPrKobling(kobling, gjeldendeTilstand);
        reaktiverSisteLagredeMedTilstandFør(gjeldendeTilstand, kobling, maksOppretteTidPrKobling);
    }

    private void reaktiverSisteLagredeMedTilstandFør(BeregningsgrunnlagTilstand tilstand, Long koblingId, LocalDateTime maksOpprettetTid) {
        Optional<BeregningsgrunnlagGrunnlagEntitet> grunnlagForKobling = Optional.empty();
        var forrigeTilstand = tilstand;
        while (grunnlagForKobling.isEmpty()) {
            forrigeTilstand = BeregningsgrunnlagTilstand.finnForrigeTilstand(forrigeTilstand).orElseThrow();
            grunnlagForKobling = hentSisteBeregningsgrunnlagGrunnlagEntitetOpprettetFør(koblingId, maksOpprettetTid, forrigeTilstand);
        }
        endreAktivOgLagre(grunnlagForKobling.get(), true);
        entityManager.flush();
    }

    private LocalDateTime finnMaksOpprettetTidForGrunnlagPrKobling(Long kobling, BeregningsgrunnlagTilstand tilstand) {
        var sisteGrunnlagFraTilstand = hentSisteBeregningsgrunnlagGrunnlagEntitetForKobling(
                kobling,
                tilstand,
                null);

        // Koblinger kan mangle grunnlag med gitt tilstand dersom steget er opprettet etter at koblingen ble behandlet forrige gang
        // Dersom dette skjer ser vi på neste tilstand i loop til alle koblingene er dekket
        var nesteTilstand = tilstand;
        while (sisteGrunnlagFraTilstand.isEmpty()) {
            nesteTilstand = BeregningsgrunnlagTilstand.finnNesteTilstand(nesteTilstand).orElseThrow(() -> new IllegalStateException("Kunne ikke finne neste tilstand fordi siste tilstand er nådd"));
            sisteGrunnlagFraTilstand = hentSisteBeregningsgrunnlagGrunnlagEntitetForKobling(kobling,
                    nesteTilstand,
                    null);
        }
        return sisteGrunnlagFraTilstand.get().getOpprettetTidspunkt();
    }

}
