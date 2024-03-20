package no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag;

import static no.nav.folketrygdloven.kalkulus.felles.verktøy.HibernateVerktøy.hentEksaktResultat;
import static no.nav.folketrygdloven.kalkulus.felles.verktøy.HibernateVerktøy.hentUniktResultat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.hibernate.jpa.QueryHints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.FaktaAggregatEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.SammenligningsgrunnlagPrStatusEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.GrunnlagReferanse;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningSatsType;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.k9.felles.jpa.HibernateVerktøy;

@ApplicationScoped
public class BeregningsgrunnlagRepository {
    private static final String KOBLING_ID = "koblingId";
    private static final String BEREGNINGSGRUNNLAG_TILSTAND = "beregningsgrunnlagTilstand";
    private static final String BEREGNINGSGRUNNLAG = "beregningsgrunnlag";
    private static final String BUILDER = "beregningsgrunnlagGrunnlagBuilder";
    private static final Logger LOG = LoggerFactory.getLogger(BeregningsgrunnlagRepository.class);
    private EntityManager entityManager;

    protected BeregningsgrunnlagRepository() {
        // for CDI proxy
    }

    @Inject
    public BeregningsgrunnlagRepository(EntityManager entityManager) {
        Objects.requireNonNull(entityManager, "entityManager"); //$NON-NLS-1$
        this.entityManager = entityManager;
    }

    /**
     * Henter siste {@link BeregningsgrunnlagGrunnlagEntitet} opprettet i et bestemt steg for revurdering. Ignorerer om grunnlaget er aktivt
     * eller ikke.
     * Om revurderingen ikke har grunnlag opprettet i denne tilstanden returneres grunnlaget fra originalbehandlingen for samme tilstand.
     *
     * @param kobling                    en kobling
     * @param beregningsgrunnlagTilstand steget {@link BeregningsgrunnlagGrunnlagEntitet} er opprettet i
     * @param skjæringstidspunkt
     * @return Hvis det finnes et eller fler BeregningsgrunnlagGrunnlagEntitet som har blitt opprettet i {@code stegOpprettet} returneres den
     * som ble opprettet sist
     */
    public Optional<BeregningsgrunnlagGrunnlagEntitet> hentSisteBeregningsgrunnlagGrunnlagEntitetForBehandlinger(KoblingEntitet kobling,
                                                                                                                 BeregningsgrunnlagTilstand beregningsgrunnlagTilstand,
                                                                                                                 LocalDate skjæringstidspunkt) {
        Optional<BeregningsgrunnlagGrunnlagEntitet> sisteBg = hentSisteBeregningsgrunnlagGrunnlagEntitet(kobling.getId(), beregningsgrunnlagTilstand);
        if (!sisteBg.isPresent()) {
            // Henter siste grunnlaget som ble lagret med samme skjæringstidspunkt (tilsvarer original kobling)
            var originalGrunnlag = hentOriginalGrunnlagForTilstand(kobling.getId(), beregningsgrunnlagTilstand, skjæringstidspunkt);
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
                        "and grunnlag.aktiv = :aktivt", //$NON-NLS-1$
                BeregningsgrunnlagGrunnlagEntitet.class);
        query.setParameter(KOBLING_ID, koblingIds); // $NON-NLS-1$
        query.setParameter("aktivt", true); //$NON-NLS-1$
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
                        "and grunnlag.aktiv = :aktivt", //$NON-NLS-1$
                BeregningsgrunnlagGrunnlagEntitet.class);
        query.setParameter(KOBLING_ID, koblingId); // $NON-NLS-1$
        query.setParameter("aktivt", true); //$NON-NLS-1$
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
                        "and grunnlag.grunnlagReferanse.referanse = :referanse", //$NON-NLS-1$
                BeregningsgrunnlagGrunnlagEntitet.class);
        query.setParameter(KOBLING_ID, koblingId); // $NON-NLS-1$
        query.setParameter("referanse", grunnlagReferanse); //$NON-NLS-1$
        return hentUniktResultat(query);
    }


    /**
     * Henter originalt grunnlag for kobling med gitt tilstand og skjæringstidspunkt
     *
     * @param koblingId koblingId
     * @param tilstand  Tilstand for grunnlag
     * @return Originalt grunnlag med gitt tilstand
     */
    @SuppressWarnings("unchecked")
    public Optional<BeregningsgrunnlagGrunnlagEntitet> hentOriginalGrunnlagForTilstand(Long koblingId,
                                                                                       BeregningsgrunnlagTilstand tilstand,
                                                                                       LocalDate skjæringstidspunktOpptjening) {
        var query = entityManager.createNativeQuery(
                "SELECT GR.* FROM  GR_BEREGNINGSGRUNNLAG GR " +
                        "INNER JOIN BG_AKTIVITETER AKT ON GR.register_aktiviteter_id = AKT.ID " +
                        "WHERE (GR.KOBLING_ID IN (" +
                        "with recursive originalkoblinger as (" +
                        "    select original_kobling_id" +
                        "    from kobling_relasjon" +
                        "    where kobling_id = :koblingId" +
                        "  union" +
                        "    select kr.original_kobling_id" +
                        "    from kobling_relasjon kr" +
                        "         inner join originalkoblinger o on o.original_kobling_id = kr.kobling_id" +
                        ") select * from originalkoblinger" +
                        ")) " +
                        "AND STEG_OPPRETTET = :beregningsgrunnlagTilstand " +
                        "AND AKT.skjaringstidspunkt_opptjening = :stp " +
                        "order by GR.OPPRETTET_TID desc, GR.ID desc", //$NON-NLS-1$
                BeregningsgrunnlagGrunnlagEntitet.class);
        query.setParameter("koblingId", koblingId); // $NON-NLS-1$
        query.setParameter("beregningsgrunnlagTilstand", tilstand.getKode()); // $NON-NLS-1$
        query.setParameter("stp", skjæringstidspunktOpptjening); // $NON-NLS-1$
        query.setMaxResults(1);
        List<BeregningsgrunnlagGrunnlagEntitet> resultatListe = query.getResultList();
        if (resultatListe.size() > 1) {
            throw new IllegalArgumentException("Flere enn en rader");
        }
        return resultatListe.size() == 1 ? Optional.of(resultatListe.get(0)) : Optional.empty();
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
                        "order by b.opprettetTidspunkt desc, b.id desc", //$NON-NLS-1$
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
                        "order by opprettetTidspunkt desc, id desc", //$NON-NLS-1$
                BeregningsgrunnlagGrunnlagEntitet.class);
        query.setParameter(KOBLING_ID, koblingId); // $NON-NLS-1$
        query.setParameter(BEREGNINGSGRUNNLAG_TILSTAND, beregningsgrunnlagTilstand); // $NON-NLS-1$
        query.setParameter("opprettetTidspunktMin", opprettetTidspunktMin); // $NON-NLS-1$
        query.setMaxResults(1);
        return HibernateVerktøy.hentUniktResultat(query);
    }

    /**
     * Henter siste {@link BeregningsgrunnlagGrunnlagEntitet} opprettet i et bestemt steg for alle koblinger. Ignorerer om grunnlaget er aktivt
     * eller ikke.
     *
     * @param koblingIds                 en liste med koblingId
     * @param beregningsgrunnlagTilstand steget {@link BeregningsgrunnlagGrunnlagEntitet} er opprettet i
     * @param opprettetTidMax
     * @return Liste med grunnlag fra gitt {@link BeregningsgrunnlagTilstand} som ble opprettet sist pr kobling
     */
    @SuppressWarnings("unchecked")
    public List<BeregningsgrunnlagGrunnlagEntitet> hentSisteBeregningsgrunnlagGrunnlagEntitetForKoblinger(Collection<Long> koblingIds,
                                                                                                          BeregningsgrunnlagTilstand beregningsgrunnlagTilstand,
                                                                                                          LocalDateTime opprettetTidMax) {
        if (opprettetTidMax != null) {
            Query query = entityManager.createNativeQuery(
                    "SELECT GR.* FROM GR_BEREGNINGSGRUNNLAG GR " +
                            "WHERE GR.OPPRETTET_TID = (SELECT max(GR2.OPPRETTET_TID) FROM GR_BEREGNINGSGRUNNLAG GR2 " +
                            "WHERE GR2.KOBLING_ID = GR.KOBLING_ID AND " +
                            "GR2.STEG_OPPRETTET = :beregningsgrunnlagTilstand AND " +
                            "GR2.OPPRETTET_TID <= :opprettetTidMax) AND " +
                            "GR.KOBLING_ID IN :koblingId AND " +
                            "GR.STEG_OPPRETTET = :beregningsgrunnlagTilstand", //$NON-NLS-1$
                    BeregningsgrunnlagGrunnlagEntitet.class);
            query.setParameter(KOBLING_ID, koblingIds); // $NON-NLS-1$
            query.setParameter("opprettetTidMax", opprettetTidMax);
            query.setParameter(BEREGNINGSGRUNNLAG_TILSTAND, beregningsgrunnlagTilstand.getKode()); // $NON-NLS-1$
            return query.getResultList();
        } else {
            Query query = entityManager.createNativeQuery(
                    "SELECT GR.* FROM GR_BEREGNINGSGRUNNLAG GR " +
                            "WHERE GR.OPPRETTET_TID = (SELECT max(GR2.OPPRETTET_TID) FROM GR_BEREGNINGSGRUNNLAG GR2 " +
                            "WHERE GR2.KOBLING_ID = GR.KOBLING_ID AND " +
                            "GR2.STEG_OPPRETTET = :beregningsgrunnlagTilstand) AND " +
                            "GR.KOBLING_ID IN :koblingId AND " +
                            "GR.STEG_OPPRETTET = :beregningsgrunnlagTilstand", //$NON-NLS-1$
                    BeregningsgrunnlagGrunnlagEntitet.class);
            query.setParameter(KOBLING_ID, koblingIds); // $NON-NLS-1$
            query.setParameter(BEREGNINGSGRUNNLAG_TILSTAND, beregningsgrunnlagTilstand.getKode()); // $NON-NLS-1$
            return query.getResultList();
        }
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
                        "order by opprettetTidspunkt desc, id desc", //$NON-NLS-1$
                BeregningsgrunnlagGrunnlagEntitet.class);
        query.setParameter(KOBLING_ID, koblingId); // $NON-NLS-1$
        query.setParameter(BEREGNINGSGRUNNLAG_TILSTAND, beregningsgrunnlagTilstand); // $NON-NLS-1$
        query.setParameter("opprettetTidspunktMax", opprettetTidspunktMax); // $NON-NLS-1$
        query.setMaxResults(1);
        return HibernateVerktøy.hentUniktResultat(query);
    }


    public BeregningSats finnGrunnbeløp(LocalDate dato) {
        TypedQuery<BeregningSats> query = entityManager.createQuery("from BeregningSats where satsType=:satsType" + //$NON-NLS-1$
                " and periode.fomDato<=:dato" + //$NON-NLS-1$
                " and periode.tomDato>=:dato", BeregningSats.class); //$NON-NLS-1$

        query.setParameter("satsType", BeregningSatsType.GRUNNBELØP); //$NON-NLS-1$
        query.setParameter("dato", dato); //$NON-NLS-1$
        query.setHint(QueryHints.HINT_READONLY, "true");//$NON-NLS-1$
        query.getResultList();
        return hentEksaktResultat(query);
    }

    public List<BeregningSats> finnAlleSatser() {
        TypedQuery<BeregningSats> query = entityManager.createQuery("from BeregningSats", BeregningSats.class); //$NON-NLS-1$
        query.setHint(QueryHints.HINT_READONLY, "true");//$NON-NLS-1$
        query.getResultList();
        return query.getResultList();
    }

    public BeregningsgrunnlagGrunnlagEntitet lagre(Long koblingId, BeregningsgrunnlagEntitet beregningsgrunnlag,
                                                   BeregningsgrunnlagTilstand beregningsgrunnlagTilstand) {
        Objects.requireNonNull(koblingId, KOBLING_ID);
        Objects.requireNonNull(beregningsgrunnlag, BEREGNINGSGRUNNLAG);
        Objects.requireNonNull(beregningsgrunnlagTilstand, BEREGNINGSGRUNNLAG_TILSTAND);

        BeregningsgrunnlagGrunnlagBuilder builder = opprettGrunnlagBuilderFor(koblingId);
        builder.medBeregningsgrunnlag(beregningsgrunnlag);
        BeregningsgrunnlagGrunnlagEntitet grunnlagEntitet = builder.build(koblingId, beregningsgrunnlagTilstand);
        return lagreOgFlush(koblingId, grunnlagEntitet);
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

    private BeregningsgrunnlagGrunnlagBuilder opprettGrunnlagBuilderFor(Long koblingId) {
        Optional<BeregningsgrunnlagGrunnlagEntitet> entitetOpt = hentBeregningsgrunnlagGrunnlagEntitet(koblingId);
        if (entitetOpt.isEmpty()) {
            LOG.info("Fant ingen aktiv grunnlag for kobling " + koblingId + ". Oppretter ny grunnlagbuilder.");
        }
        Optional<BeregningsgrunnlagGrunnlagEntitet> grunnlag = entitetOpt.isPresent() ? Optional.of(entitetOpt.get()) : Optional.empty();
        return BeregningsgrunnlagGrunnlagBuilder.kopiere(grunnlag);
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

    public void reaktiverSisteMedTilstand(BeregningsgrunnlagTilstand tilstand, Collection<Long> koblingId) {
        var grunnlagForKobling = hentSisteBeregningsgrunnlagGrunnlagEntitetForKoblinger(koblingId, tilstand, null);
        endreAktivOgLagre(grunnlagForKobling, true);
        entityManager.flush();
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
     * @param koblinger         Koblinger med grunnlag som skal reaktiveres
     * @param gjeldendeTilstand Gjeldende tilstand
     */
    public void reaktiverForrigeGrunnlagForKoblinger(Set<Long> koblinger, BeregningsgrunnlagTilstand gjeldendeTilstand) {
        var maksOppretteTidPrKobling = finnMaksOpprettetTidForGrunnlagPrKobling(koblinger, gjeldendeTilstand);
        maksOppretteTidPrKobling.forEach((koblingId, maksOpprettetTid) -> reaktiverSisteLagredeMedTilstandFør(gjeldendeTilstand, koblingId, maksOpprettetTid));
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

    private Map<Long, LocalDateTime> finnMaksOpprettetTidForGrunnlagPrKobling(Set<Long> koblinger, BeregningsgrunnlagTilstand tilstand) {
        var sisteGrunnlagFraTilstand = hentSisteBeregningsgrunnlagGrunnlagEntitetForKoblinger(
                koblinger,
                tilstand,
                null);

        Map<Long, LocalDateTime> maksOppretteTidPrKobling = new HashMap<>();

        sisteGrunnlagFraTilstand.forEach(gr -> maksOppretteTidPrKobling.put(gr.getKoblingId(), gr.getOpprettetTidspunkt()));

        var manglendeKoblinger = koblinger.stream()
                .filter(k -> !maksOppretteTidPrKobling.containsKey(k))
                .collect(Collectors.toSet());

        // Koblinger kan mangle grunnlag med gitt tilstand dersom steget er opprettet etter at koblingen ble behandlet forrige gang
        // Dersom dette skjer ser vi på neste tilstand i loop til alle koblingene er dekket
        var nesteTilstand = tilstand;
        while (!manglendeKoblinger.isEmpty()) {
            nesteTilstand = BeregningsgrunnlagTilstand.finnNesteTilstand(nesteTilstand).orElseThrow(() -> new IllegalStateException("Kunne ikke finne neste tilstand fordi siste tilstand er nådd"));
            sisteGrunnlagFraTilstand = hentSisteBeregningsgrunnlagGrunnlagEntitetForKoblinger(manglendeKoblinger,
                    nesteTilstand,
                    null);

            sisteGrunnlagFraTilstand.forEach(gr -> maksOppretteTidPrKobling.put(gr.getKoblingId(), gr.getOpprettetTidspunkt()));
            manglendeKoblinger = koblinger.stream()
                    .filter(k -> !maksOppretteTidPrKobling.containsKey(k))
                    .collect(Collectors.toSet());
        }
        return maksOppretteTidPrKobling;
    }
}
