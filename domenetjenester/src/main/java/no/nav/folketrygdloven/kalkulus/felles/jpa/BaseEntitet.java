package no.nav.folketrygdloven.kalkulus.felles.jpa;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Optional;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import no.nav.folketrygdloven.kalkulus.felles.diff.DiffIgnore;
import no.nav.vedtak.sikkerhet.kontekst.Kontekst;
import no.nav.vedtak.sikkerhet.kontekst.KontekstHolder;

/**
 * En basis entitetklasse som håndtere felles standarder for utformign av tabeller (eks. sporing av hvem som har
 * opprettet eller oppdatert en rad, og når).
 */
@MappedSuperclass
public class BaseEntitet implements Serializable {

    private static final String BRUKERNAVN_NÅR_SIKKERHETSKONTEKST_IKKE_FINNES = "VL";

    @DiffIgnore
    @Column(name = "opprettet_av", nullable = false, insertable = true, updatable = false)
    private String opprettetAv;

    @DiffIgnore
    @Column(name = "opprettet_tid", nullable = false, insertable = true, updatable = false)
    private LocalDateTime opprettetTidspunkt; // NOSONAR

    @DiffIgnore
    @Column(name = "endret_av", insertable = false, updatable = true)
    private String endretAv;

    @DiffIgnore
    @Column(name = "endret_tid", insertable = false, updatable = true)
    private LocalDateTime endretTidspunkt; // NOSONAR

    private static String finnBrukernavn() {
        return Optional.ofNullable(KontekstHolder.getKontekst()).map(Kontekst::getKompaktUid)
            .orElse(BRUKERNAVN_NÅR_SIKKERHETSKONTEKST_IKKE_FINNES);
    }

    @PrePersist
    protected void onCreate() {
        this.opprettetAv = opprettetAv != null ? opprettetAv : finnBrukernavn();
        this.opprettetTidspunkt = opprettetTidspunkt != null ? opprettetTidspunkt : LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        endretAv = finnBrukernavn();
        endretTidspunkt = LocalDateTime.now();
    }

    public String getOpprettetAv() {
        return opprettetAv;
    }

    public LocalDateTime getOpprettetTidspunkt() {
        return opprettetTidspunkt;
    }

    /**
     * Kan brukes til å eksplisitt sette opprettet tidspunkt, f.eks. ved migrering av data fra et annet system. Ivaretar da opprinnelig
     * tidspunkt istdf å sette likt now().
     */
    public void setOpprettetTidspunkt(LocalDateTime opprettetTidspunkt) {
        this.opprettetTidspunkt = opprettetTidspunkt;
    }

    public void setOpprettetAv(String opprettetAv) {
        this.opprettetAv = opprettetAv;
    }

    public void setEndretTidspunkt(LocalDateTime endretTidspunkt) {
        this.endretTidspunkt = endretTidspunkt;
    }

    public void setEndretAv(String endretAv) {
        this.endretAv = endretAv;
    }

    public String getEndretAv() {
        return endretAv;
    }

    public LocalDateTime getEndretTidspunkt() {
        return endretTidspunkt;
    }
}
