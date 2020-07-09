package no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag;

import static no.nav.folketrygdloven.kalkulus.felles.verktøy.HibernateVerktøy.hentEksaktResultat;
import static no.nav.folketrygdloven.kalkulus.felles.verktøy.HibernateVerktøy.hentUniktResultat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.hibernate.jpa.QueryHints;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.BeregningSats;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.KalkulatorInputEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningAktivitetAggregatEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningAktivitetOverstyringerEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningRefusjonOverstyringEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningRefusjonOverstyringerEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagBuilder;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.SammenligningsgrunnlagPrStatus;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.GrunnlagReferanse;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningSatsType;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand;

/**
 * Henter siste {@link BeregningsgrunnlagGrunnlagEntitet} opprettet i et bestemt steg for revurdering. Ignorerer om grunnlaget er aktivt eller ikke.
 * Om revurderingen ikke har grunnlag opprettet i denne tilstanden returneres grunnlaget fra originalbehandlingen for samme tilstand.
 * @param koblingId en koblingId
 * @param beregningsgrunnlagTilstand steget {@link BeregningsgrunnlagGrunnlagEntitet} er opprettet i
 * @return Hvis det finnes et eller fler BeregningsgrunnlagGrunnlagEntitet som har blitt opprettet i {@code stegOpprettet} returneres den som ble opprettet sist
 */
@ApplicationScoped
public class BeregningsgrunnlagRepository {
    private static final String KOBLING_ID = "koblingId";
    private static final String BEREGNINGSGRUNNLAG_TILSTAND = "beregningsgrunnlagTilstand";
    private static final String BEREGNING_AKTIVITET_AGGREGAT = "beregningAktivitetAggregat";
    private static final String BEREGNING_AKTIVITET_OVERSTYRING = "beregningAktivitetOverstyring";
    private static final String BEREGNING_REFUSJON_OVERSTYRINGER = "beregningRefusjonOverstyringer";
    private static final String BUILDER = "beregningsgrunnlagGrunnlagBuilder";
    private EntityManager entityManager;

    protected BeregningsgrunnlagRepository() {
        // for CDI proxy
    }

    @Inject
    public BeregningsgrunnlagRepository(EntityManager entityManager) {
        Objects.requireNonNull(entityManager, "entityManager"); //$NON-NLS-1$
        this.entityManager = entityManager;
    }

    public Optional<BeregningsgrunnlagGrunnlagEntitet> hentSisteBeregningsgrunnlagGrunnlagEntitetForBehandlinger(Long koblingId, Optional<Long> originalKoblingId,
                                                                                                                 BeregningsgrunnlagTilstand beregningsgrunnlagTilstand) {
        Optional<BeregningsgrunnlagGrunnlagEntitet> sisteBg = hentSisteBeregningsgrunnlagGrunnlagEntitet(koblingId, beregningsgrunnlagTilstand);
        if (!sisteBg.isPresent() && originalKoblingId.isPresent()) {
            return hentSisteBeregningsgrunnlagGrunnlagEntitet(originalKoblingId.get(), beregningsgrunnlagTilstand);
        }
        return sisteBg;
    }

    public Optional<BeregningsgrunnlagGrunnlagEntitet> hentNestSisteBeregningsgrunnlagGrunnlagEntitetForBehandlinger(Long koblingId, Optional<Long> originalkoblingId,
                                                                                                                     BeregningsgrunnlagTilstand beregningsgrunnlagTilstand) {
        Optional<BeregningsgrunnlagGrunnlagEntitet> sisteBg = hentNestSisteBeregningsgrunnlagGrunnlagEntitet(koblingId, beregningsgrunnlagTilstand);
        if (!sisteBg.isPresent() && originalkoblingId.isPresent()) {
            return hentNestSisteBeregningsgrunnlagGrunnlagEntitet(originalkoblingId.get(), beregningsgrunnlagTilstand);
        }
        return sisteBg;
    }


    public BeregningsgrunnlagEntitet hentBeregningsgrunnlagAggregatForBehandling(Long koblingId) {
        return hentBeregningsgrunnlagForBehandling(koblingId)
            .orElseThrow(() -> new IllegalStateException("Mangler Beregningsgrunnlag for behandling " + koblingId));
    }

    public Optional<BeregningsgrunnlagEntitet> hentBeregningsgrunnlagForBehandling(Long koblingId) {
        return hentBeregningsgrunnlagGrunnlagEntitet(koblingId).flatMap(BeregningsgrunnlagGrunnlagEntitet::getBeregningsgrunnlag);
    }

    /**
     * Hent angitt beregningsgrunnlag basert på beregningsgrunnlag id (ikke behandling Id som ellers i interfacet her).
     */
    public Optional<BeregningsgrunnlagEntitet> hentBeregningsgrunnlagForId(Long beregningsgrunnlagId) {
        TypedQuery<BeregningsgrunnlagEntitet> query = entityManager.createQuery(
            "from Beregningsgrunnlag grunnlag " +
                "where grunnlag.id = :beregningsgrunnlagId", BeregningsgrunnlagEntitet.class); //$NON-NLS-1$
        query.setParameter("beregningsgrunnlagId", beregningsgrunnlagId); //$NON-NLS-1$
        return hentUniktResultat(query);
    }

    /**
     * Henter aktivt BeregningsgrunnlagGrunnlagEntitet
     * @param koblingId en koblingId
     * @return Hvis det finnes en aktiv {@link BeregningsgrunnlagGrunnlagEntitet} returneres denne
     */
    public Optional<BeregningsgrunnlagGrunnlagEntitet> hentBeregningsgrunnlagGrunnlagEntitet(Long koblingId) {
        TypedQuery<BeregningsgrunnlagGrunnlagEntitet> query = entityManager.createQuery(
            "from BeregningsgrunnlagGrunnlagEntitet grunnlag " +
                "where grunnlag.koblingId=:koblingId " +
                "and grunnlag.aktiv = :aktivt", BeregningsgrunnlagGrunnlagEntitet.class); //$NON-NLS-1$
        query.setParameter(KOBLING_ID, koblingId); //$NON-NLS-1$
        query.setParameter("aktivt", true); //$NON-NLS-1$
        return hentUniktResultat(query);
    }


    /**
     * Henter aktivt BeregningsgrunnlagGrunnlagEntitet
     * @param koblingId en koblingId
     * @param grunnlagReferanse en referanse til et lagret grunnlag
     * @return Hvis det finnes en aktiv {@link BeregningsgrunnlagGrunnlagEntitet} returneres denne
     */
    public Optional<BeregningsgrunnlagGrunnlagEntitet> hentBeregningsgrunnlagGrunnlagEntitetForReferanse(Long koblingId, UUID grunnlagReferanse) {
        TypedQuery<BeregningsgrunnlagGrunnlagEntitet> query = entityManager.createQuery(
                "from BeregningsgrunnlagGrunnlagEntitet grunnlag " +
                        "where grunnlag.koblingId=:koblingId " +
                        "and grunnlag.grunnlagReferanse.referanse = :referanse", BeregningsgrunnlagGrunnlagEntitet.class); //$NON-NLS-1$
        query.setParameter(KOBLING_ID, koblingId); //$NON-NLS-1$
        query.setParameter("referanse", grunnlagReferanse); //$NON-NLS-1$
        return hentUniktResultat(query);
    }

    /**
     * Henter siste {@link BeregningsgrunnlagGrunnlagEntitet} opprettet i et bestemt steg. Ignorerer om grunnlaget er aktivt eller ikke.
     * @param koblingId en koblingId
     * @param beregningsgrunnlagTilstand steget {@link BeregningsgrunnlagGrunnlagEntitet} er opprettet i
     * @return Hvis det finnes et eller fler BeregningsgrunnlagGrunnlagEntitet som har blitt opprettet i {@code stegOpprettet} returneres den som ble opprettet sist
     */
    public Optional<BeregningsgrunnlagGrunnlagEntitet> hentSisteBeregningsgrunnlagGrunnlagEntitet(Long koblingId, BeregningsgrunnlagTilstand beregningsgrunnlagTilstand) {
        TypedQuery<BeregningsgrunnlagGrunnlagEntitet> query = entityManager.createQuery(
            "from BeregningsgrunnlagGrunnlagEntitet " +
                "where koblingId=:koblingId " +
                "and beregningsgrunnlagTilstand = :beregningsgrunnlagTilstand " +
                "order by opprettetTidspunkt desc, id desc", BeregningsgrunnlagGrunnlagEntitet.class); //$NON-NLS-1$
        query.setParameter(KOBLING_ID, koblingId); //$NON-NLS-1$
        query.setParameter(BEREGNINGSGRUNNLAG_TILSTAND, beregningsgrunnlagTilstand); //$NON-NLS-1$
        query.setMaxResults(1);
        return query.getResultStream().findFirst();
    }

    /**
     * Henter nest siste {@link BeregningsgrunnlagGrunnlagEntitet} opprettet i et bestemt steg. Ignorerer om grunnlaget er aktivt eller ikke.
     * @param koblingId en koblingId
     * @param beregningsgrunnlagTilstand steget {@link BeregningsgrunnlagGrunnlagEntitet} er opprettet i
     * @return Hvis det finnes kun ett BeregningsgrunnlagGrunnlagEntitet som har blitt opprettet i {@code stegOpprettet} returneres dette grunnlaget
     */
    private Optional<BeregningsgrunnlagGrunnlagEntitet> hentNestSisteBeregningsgrunnlagGrunnlagEntitet(Long koblingId, BeregningsgrunnlagTilstand beregningsgrunnlagTilstand) {
        TypedQuery<BeregningsgrunnlagGrunnlagEntitet> query = entityManager.createQuery(
            "from BeregningsgrunnlagGrunnlagEntitet " +
                "where koblingId=:koblingId " +
                "and beregningsgrunnlagTilstand = :beregningsgrunnlagTilstand " +
                "order by opprettetTidspunkt desc, id desc", BeregningsgrunnlagGrunnlagEntitet.class); //$NON-NLS-1$
        query.setParameter(KOBLING_ID, koblingId); //$NON-NLS-1$
        query.setParameter(BEREGNINGSGRUNNLAG_TILSTAND, beregningsgrunnlagTilstand); //$NON-NLS-1$
        query.setMaxResults(2);
        return query.getResultStream().min(Comparator.comparing(BeregningsgrunnlagGrunnlagEntitet::getOpprettetTidspunkt));
    }

    public BeregningSats finnEksaktSats(BeregningSatsType satsType, LocalDate dato) {
        TypedQuery<BeregningSats> query = entityManager.createQuery("from BeregningSats where satsType=:satsType" + //$NON-NLS-1$
                " and periode.fomDato<=:dato" + //$NON-NLS-1$
                " and periode.tomDato>=:dato", BeregningSats.class); //$NON-NLS-1$

        query.setParameter("satsType", satsType); //$NON-NLS-1$
        query.setParameter("dato", dato); //$NON-NLS-1$
        query.setHint(QueryHints.HINT_READONLY, "true");//$NON-NLS-1$
        query.getResultList();
        return hentEksaktResultat(query);
    }

    public BeregningsgrunnlagGrunnlagEntitet lagre(Long koblingId, BeregningsgrunnlagEntitet beregningsgrunnlag, BeregningsgrunnlagTilstand beregningsgrunnlagTilstand) {
        Objects.requireNonNull(koblingId, KOBLING_ID);
        Objects.requireNonNull(beregningsgrunnlag, BEREGNING_AKTIVITET_AGGREGAT);
        Objects.requireNonNull(beregningsgrunnlagTilstand, BEREGNINGSGRUNNLAG_TILSTAND);

        BeregningsgrunnlagGrunnlagBuilder builder = opprettGrunnlagBuilderFor(koblingId);
        builder.medBeregningsgrunnlag(beregningsgrunnlag);
        BeregningsgrunnlagGrunnlagEntitet grunnlagEntitet = builder.build(koblingId, beregningsgrunnlagTilstand);
        lagreOgFlush(koblingId, grunnlagEntitet);
        return grunnlagEntitet;
    }

    public BeregningsgrunnlagGrunnlagEntitet lagre(Long koblingId, BeregningsgrunnlagGrunnlagBuilder builder, BeregningsgrunnlagTilstand beregningsgrunnlagTilstand) {
        Objects.requireNonNull(koblingId, KOBLING_ID);
        Objects.requireNonNull(builder, BUILDER);
        Objects.requireNonNull(beregningsgrunnlagTilstand, BEREGNINGSGRUNNLAG_TILSTAND);
        BeregningsgrunnlagGrunnlagEntitet grunnlagEntitet = builder.build(koblingId, beregningsgrunnlagTilstand);
        lagreOgFlush(koblingId, grunnlagEntitet);
        return grunnlagEntitet;
    }

    public void lagreRegisterAktiviteter(Long koblingId,
                                         BeregningAktivitetAggregatEntitet beregningAktivitetAggregat,
                                         BeregningsgrunnlagTilstand beregningsgrunnlagTilstand) {
        Objects.requireNonNull(koblingId, KOBLING_ID);
        Objects.requireNonNull(beregningAktivitetAggregat, BEREGNING_AKTIVITET_AGGREGAT);
        Objects.requireNonNull(beregningsgrunnlagTilstand, BEREGNINGSGRUNNLAG_TILSTAND);

        BeregningsgrunnlagGrunnlagBuilder builder = opprettGrunnlagBuilderFor(koblingId);
        builder.medRegisterAktiviteter(beregningAktivitetAggregat);
        lagreOgFlush(koblingId, builder.build(koblingId, beregningsgrunnlagTilstand));
    }

    public void lagreSaksbehandledeAktiviteter(Long koblingId,
                                               BeregningAktivitetAggregatEntitet beregningAktivitetAggregat,
                                               BeregningsgrunnlagTilstand beregningsgrunnlagTilstand) {
        Objects.requireNonNull(koblingId, KOBLING_ID);
        Objects.requireNonNull(beregningAktivitetAggregat, BEREGNING_AKTIVITET_AGGREGAT);
        Objects.requireNonNull(beregningsgrunnlagTilstand, BEREGNINGSGRUNNLAG_TILSTAND);

        BeregningsgrunnlagGrunnlagBuilder builder = opprettGrunnlagBuilderFor(koblingId);
        builder.medSaksbehandletAktiviteter(beregningAktivitetAggregat);
        lagreOgFlush(koblingId, builder.build(koblingId, beregningsgrunnlagTilstand));
    }

    public void lagre(Long koblingId, BeregningAktivitetOverstyringerEntitet beregningAktivitetOverstyringer) {
        Objects.requireNonNull(koblingId, KOBLING_ID);
        Objects.requireNonNull(beregningAktivitetOverstyringer, BEREGNING_AKTIVITET_OVERSTYRING);
        BeregningsgrunnlagTilstand beregningsgrunnlagTilstand = BeregningsgrunnlagTilstand.FASTSATT_BEREGNINGSAKTIVITETER;

        BeregningsgrunnlagGrunnlagBuilder builder = opprettGrunnlagBuilderFor(koblingId);
        builder.medOverstyring(beregningAktivitetOverstyringer);
        lagreOgFlush(koblingId, builder.build(koblingId, beregningsgrunnlagTilstand));
    }

    public void lagre(Long koblingId, BeregningRefusjonOverstyringerEntitet beregningRefusjonOverstyringer) {
        Objects.requireNonNull(koblingId, KOBLING_ID);
        Objects.requireNonNull(beregningRefusjonOverstyringer, BEREGNING_REFUSJON_OVERSTYRINGER);
        BeregningsgrunnlagTilstand beregningsgrunnlagTilstand = BeregningsgrunnlagTilstand.KOFAKBER_UT;
        BeregningsgrunnlagGrunnlagBuilder builder = opprettGrunnlagBuilderFor(koblingId);
        builder.medRefusjonOverstyring(beregningRefusjonOverstyringer);
        lagreOgFlush(koblingId, builder.build(koblingId, beregningsgrunnlagTilstand));
    }

    public void lagreOgFlush(Long koblingId, BeregningsgrunnlagGrunnlagEntitet nyttGrunnlag) {
        Objects.requireNonNull(koblingId, KOBLING_ID);
        Objects.requireNonNull(nyttGrunnlag.getBeregningsgrunnlagTilstand(), BEREGNINGSGRUNNLAG_TILSTAND);
        Optional<BeregningsgrunnlagGrunnlagEntitet> tidligereAggregat = hentBeregningsgrunnlagGrunnlagEntitet(koblingId);
        if (tidligereAggregat.isPresent()) {
            tidligereAggregat.get().setAktiv(false);
            entityManager.persist(tidligereAggregat.get());
        }
        if (nyttGrunnlag.getGrunnlagReferanse() == null) {
            // lag ny referanse
            nyttGrunnlag.setGrunnlagReferanse(new GrunnlagReferanse(UUID.randomUUID()));
        }
        lagreGrunnlag(nyttGrunnlag);
        entityManager.flush();
    }

    private void lagreGrunnlag(BeregningsgrunnlagGrunnlagEntitet nyttGrunnlag) {
        BeregningAktivitetAggregatEntitet registerAktiviteter = nyttGrunnlag.getRegisterAktiviteter();
        if (registerAktiviteter != null) {
            lagreBeregningAktivitetAggregat(registerAktiviteter);
        }
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

    private void lagreOverstyring(BeregningAktivitetOverstyringerEntitet beregningAktivitetOverstyringer) {
        if (beregningAktivitetOverstyringer.getId() == null) {
            entityManager.persist(beregningAktivitetOverstyringer);
            beregningAktivitetOverstyringer.getOverstyringer().forEach(entityManager::persist);
        }
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
        if (entitet.getId() == null) {
            entityManager.persist(entitet);
            entitet.getBeregningAktiviteter().forEach(entityManager::persist);
        }
    }

    private void lagreSammenligningsgrunnlagPrStatus(SammenligningsgrunnlagPrStatus sammenligningsgrunnlagPrStatus) {
        if (sammenligningsgrunnlagPrStatus.getId() == null) {
            entityManager.persist(sammenligningsgrunnlagPrStatus);
        }
    }

    private BeregningsgrunnlagGrunnlagBuilder opprettGrunnlagBuilderFor(Long koblingId) {
        Optional<BeregningsgrunnlagGrunnlagEntitet> entitetOpt = hentBeregningsgrunnlagGrunnlagEntitet(koblingId);
        Optional<BeregningsgrunnlagGrunnlagEntitet> grunnlag = entitetOpt.isPresent() ? Optional.of(entitetOpt.get()) : Optional.empty();
        return BeregningsgrunnlagGrunnlagBuilder.oppdatere(grunnlag);
    }

    public void deaktiverKalkulatorInput(Long koblingId) {
        Optional<KalkulatorInputEntitet> kalkulatorInputEntitet = hentHvisEksitererKalkulatorInput(koblingId);
        kalkulatorInputEntitet.ifPresent(this::deaktiverKalkulatorInput);
    }


    public void deaktiverBeregningsgrunnlagGrunnlagEntitet(Long koblingId) {
        Optional<BeregningsgrunnlagGrunnlagEntitet> entitetOpt = hentBeregningsgrunnlagGrunnlagEntitet(koblingId);
        entitetOpt.ifPresent(this::deaktiverBeregningsgrunnlagGrunnlagEntitet);
    }

    private void deaktiverBeregningsgrunnlagGrunnlagEntitet(BeregningsgrunnlagGrunnlagEntitet entitet) {
        setAktivOgLagre(entitet, false);
    }

    private void deaktiverKalkulatorInput(KalkulatorInputEntitet entitet){
        entitet.setAktiv(false);
        entityManager.persist(entitet);
        entityManager.flush();
    }

    private void setAktivOgLagre(BeregningsgrunnlagGrunnlagEntitet entitet, boolean aktiv) {
        entitet.setAktiv(aktiv);
        entityManager.persist(entitet);
        entityManager.flush();
    }

    public boolean reaktiverBeregningsgrunnlagGrunnlagEntitet(Long koblingId, BeregningsgrunnlagTilstand beregningsgrunnlagTilstand) {
        Optional<BeregningsgrunnlagGrunnlagEntitet> aktivEntitetOpt = hentBeregningsgrunnlagGrunnlagEntitet(koblingId);
        aktivEntitetOpt.ifPresent(this::deaktiverBeregningsgrunnlagGrunnlagEntitet);
        Optional<BeregningsgrunnlagGrunnlagEntitet> kontrollerFaktaEntitetOpt = hentSisteBeregningsgrunnlagGrunnlagEntitet(koblingId, beregningsgrunnlagTilstand);
        boolean reaktiverer = kontrollerFaktaEntitetOpt.isPresent();
        kontrollerFaktaEntitetOpt.ifPresent(entitet -> setAktivOgLagre(entitet, true));
        entityManager.flush();
        return reaktiverer;
    }

    public Optional<BeregningsgrunnlagGrunnlagEntitet> hentBeregningsgrunnlagForPreutfylling(Long behandlingId, Optional<Long> originalBehandlingId,
                                                                                             BeregningsgrunnlagTilstand forrigeTilstand, BeregningsgrunnlagTilstand nesteTilstand) {
        Optional<BeregningsgrunnlagGrunnlagEntitet> sisteBeregningsgrunnlag = hentBeregningsgrunnlagGrunnlagEntitet(behandlingId);

        if (sisteBeregningsgrunnlag.isEmpty()) {
            return Optional.empty();
        }

        BeregningsgrunnlagTilstand gjeldendeTilstand = sisteBeregningsgrunnlag.get().getBeregningsgrunnlagTilstand();

        if (gjeldendeTilstand.erFør(nesteTilstand) || gjeldendeTilstand.equals(nesteTilstand)) {
            return sisteBeregningsgrunnlag;
        }

        Optional<BeregningsgrunnlagGrunnlagEntitet> grunnlagMedNesteTilstandOpt = hentSisteBeregningsgrunnlagGrunnlagEntitet(behandlingId, nesteTilstand);
        Optional<BeregningsgrunnlagGrunnlagEntitet> grunnlagMedForrigeTiltandOpt = hentSisteBeregningsgrunnlagGrunnlagEntitet(behandlingId, forrigeTilstand);

        if (grunnlagMedForrigeTiltandOpt.isEmpty() && originalBehandlingId.isPresent()) {
            grunnlagMedForrigeTiltandOpt = hentSisteBeregningsgrunnlagGrunnlagEntitet(originalBehandlingId.get(), forrigeTilstand);
        }

        if (grunnlagMedNesteTilstandOpt.isEmpty()) {
            return grunnlagMedForrigeTiltandOpt;
        }

        BeregningsgrunnlagGrunnlagEntitet forrigeTilstandGrunnlag = grunnlagMedForrigeTiltandOpt
                .orElseThrow(() -> new IllegalArgumentException("Mangler beregningsgrunnlag med tilstand: " + forrigeTilstand + " for behandling " + behandlingId));

        LocalDateTime nesteTilstandOpprettetTidspunkt = grunnlagMedNesteTilstandOpt.get().getOpprettetTidspunkt();
        LocalDateTime forrigeTilstandOpprettetTitdspunkt = forrigeTilstandGrunnlag.getOpprettetTidspunkt();

        if (nesteTilstandOpprettetTidspunkt.isAfter(forrigeTilstandOpprettetTitdspunkt)) {
            return grunnlagMedNesteTilstandOpt;
        }

        return grunnlagMedForrigeTiltandOpt;
    }

    public boolean lagreOgSjekkStatus(KalkulatorInputEntitet input) {
        Optional<KalkulatorInputEntitet> inputEntitetOptional = hentHvisEksitererKalkulatorInput(input.getKoblingId());

        if (inputEntitetOptional.isPresent()) {
            KalkulatorInputEntitet utdaterInput = inputEntitetOptional.get();
            // ingen endring i input trenger ikke lagre
            if (input.getInput().equals(utdaterInput.getInput())) {
                return false;
            } else {
                utdaterInput.setAktiv(false);
                entityManager.persist(utdaterInput);
                entityManager.flush();
            }
        }
        entityManager.persist(input);
        entityManager.flush();
        return true;
    }

    public KalkulatorInputEntitet hentKalkulatorInput(Long koblingId) {
        TypedQuery<KalkulatorInputEntitet> query = entityManager.createQuery("from KalkulatorInput where koblingId =:koblingId and aktiv =:aktiv", KalkulatorInputEntitet.class);
        query.setParameter("koblingId", koblingId); //$NON-NLS-1$
        query.setParameter("aktiv", true); //$NON-NLS-1$
        return hentEksaktResultat(query);
    }

    public Optional<KalkulatorInputEntitet> hentHvisEksitererKalkulatorInput(Long koblingId) {
        TypedQuery<KalkulatorInputEntitet> query = entityManager.createQuery("from KalkulatorInput where koblingId =:koblingId and aktiv =:aktiv", KalkulatorInputEntitet.class);
        query.setParameter("koblingId", koblingId); //$NON-NLS-1$
        query.setParameter("aktiv", true); //$NON-NLS-1$
        return hentUniktResultat(query);
    }
}
