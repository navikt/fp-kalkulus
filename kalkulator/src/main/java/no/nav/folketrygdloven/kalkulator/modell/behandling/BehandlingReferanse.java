package no.nav.folketrygdloven.kalkulator.modell.behandling;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import no.nav.folketrygdloven.kalkulator.modell.typer.AktørId;


/**
 * Minimal metadata for en behandling.
 */
public class BehandlingReferanse {

    private Long behandlingId;

    private FagsakYtelseType fagsakYtelseType;

    /**
     * Søkers aktørid.
     */
    private AktørId aktørId;

    /**
     * Original behandling id (i tilfelle dette f.eks er en revurdering av en annen behandling.
     */
    private Optional<Long> originalBehandlingId;

    /**
     * Inneholder relevante tidspunkter for en behandling
     */
    private Skjæringstidspunkt skjæringstidspunkt;

    private BehandlingStatus behandlingStatus;

    /** Eksternt refererbar UUID for behandling. */
    private UUID behandlingUuid;

    public BehandlingReferanse() {
    }

    private BehandlingReferanse(FagsakYtelseType fagsakYtelseType, AktørId aktørId, // NOSONAR
                                Long behandlingId, UUID behandlingUuid, Optional<Long> originalBehandlingId,
                                BehandlingStatus behandlingStatus, Skjæringstidspunkt skjæringstidspunkt) {
        this.fagsakYtelseType = fagsakYtelseType;
        this.aktørId = aktørId;
        this.behandlingId = behandlingId;
        this.behandlingUuid = behandlingUuid;
        this.originalBehandlingId = originalBehandlingId;
        this.behandlingStatus = behandlingStatus;
        this.skjæringstidspunkt = skjæringstidspunkt;
    }


    public static BehandlingReferanse fra(FagsakYtelseType fagsakYtelseType, AktørId aktørId, // NOSONAR
                                          Long behandlingId, UUID behandlingUuid, Optional<Long> originalBehandlingId,
                                          BehandlingStatus behandlingStatus, Skjæringstidspunkt skjæringstidspunkt) {
        return new BehandlingReferanse(fagsakYtelseType,
                aktørId,
                behandlingId,
            behandlingUuid,
            originalBehandlingId,
            behandlingStatus,
            skjæringstidspunkt);
    }

    public Long getBehandlingId() {
        return behandlingId;
    }

    public UUID getBehandlingUuid() {
        return behandlingUuid;
    }

    public Long getId() {
        return getBehandlingId();
    }

    public Optional<Long> getOriginalBehandlingId() {
        return originalBehandlingId;
    }

    public FagsakYtelseType getFagsakYtelseType() {
        return fagsakYtelseType;
    }

    public AktørId getAktørId() {
        return aktørId;
    }

    public Skjæringstidspunkt getSkjæringstidspunkt() {
        return skjæringstidspunkt;
    }

    public LocalDate getSkjæringstidspunktBeregning() {
        // precondition
        return skjæringstidspunkt.getSkjæringstidspunktBeregning();
    }

    public LocalDate getSkjæringstidspunktOpptjening() {
        // precondition
        return skjæringstidspunkt.getSkjæringstidspunktOpptjening();
    }

    public LocalDate getFørsteUttaksdato() {
        // precondition
        return skjæringstidspunkt.getFørsteUttaksdato();
    }


    @Override
    public int hashCode() {
        return Objects.hash(behandlingId, originalBehandlingId, fagsakYtelseType, aktørId);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || !getClass().equals(obj.getClass())) {
            return false;
        }
        BehandlingReferanse other = (BehandlingReferanse) obj;
        return Objects.equals(behandlingId, other.behandlingId)
            && Objects.equals(aktørId, other.aktørId)
            && Objects.equals(fagsakYtelseType, other.fagsakYtelseType)
            && Objects.equals(originalBehandlingId, other.originalBehandlingId)
        // tar ikke med status eller skjæringstidspunkt i equals siden de kan endre seg
        ;
    }


    @Override
    public String toString() {
        return getClass().getSimpleName() + String.format(
            "<behandlingId=%s, fagsakType=%s, aktørId=%s, status=%s, skjæringstidspunjkt=%s, originalBehandlingId=%s>",
            behandlingId, fagsakYtelseType, aktørId, behandlingStatus, skjæringstidspunkt, originalBehandlingId);
    }

    /**
     * Lag immutable copy av referanse med satt utledet skjæringstidspunkt.
     */
    public BehandlingReferanse medSkjæringstidspunkt(LocalDate utledetSkjæringstidspunkt) {
        return new BehandlingReferanse(getFagsakYtelseType(),
            getAktørId(),
                getId(),
            getBehandlingUuid(),
            getOriginalBehandlingId(),
            getBehandlingStatus(),
            Skjæringstidspunkt.builder()
                .medSkjæringstidspunktBeregning(utledetSkjæringstidspunkt)
                .build());
    }

    /**
     * Lag immutable copy av referanse med mulighet til å legge til skjæringstidspunkt av flere typer
     */
    public BehandlingReferanse medSkjæringstidspunkt(Skjæringstidspunkt skjæringstidspunkt) {
        return new BehandlingReferanse(getFagsakYtelseType(),
            getAktørId(),
                getId(),
            getBehandlingUuid(),
            getOriginalBehandlingId(),
            getBehandlingStatus(),
            skjæringstidspunkt);
    }

    public BehandlingStatus getBehandlingStatus() {
        return behandlingStatus;
    }

}
