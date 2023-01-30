package no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import no.nav.folketrygdloven.kalkulus.felles.diff.IndexKey;
import no.nav.folketrygdloven.kalkulus.felles.jpa.BaseEntitet;

/**
 * Angir kopi av grunnlag fra en kobling til en annen
 */
@Entity(name = "KoblingGrunnlagskopiSporing")
@Table(name = "KOBLING_GRUNNLAGSKOPI_SPORING")
public class KoblingGrunnlagskopiSporing extends BaseEntitet implements IndexKey {


    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_KOBLING_GRUNNLAGSKOPI_SPORING")
    private Long id;

    /**
     * Refererer til en kobling det kopieres til
     */
    @Column(name = "KOPIERT_TIL_KOBLING_ID", nullable = false)
    private Long kopiertTilKoblingId;

    /**
     * Refererer til kobling som er kopiert fra
     */
    @Column(name = "KOPIERT_FRA_KOBLING_ID", nullable = false)
    private Long kopiertFraKoblingId;


    /**
     * Refererer til grunnlaget som kopieres
     */
    @Column(name = "KOPIERT_GRUNNLAG_ID", nullable = false)
    private Long kopiertGrunnlagId;


    @Column(name = "aktiv", nullable = false)
    private boolean aktiv = true;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;


    public KoblingGrunnlagskopiSporing() {
    }

    public KoblingGrunnlagskopiSporing(Long kopiertTilKoblingId, Long kopiertFraKoblingId, Long kopiertGrunnlagId) {
        this.kopiertTilKoblingId = kopiertTilKoblingId;
        this.kopiertFraKoblingId = kopiertFraKoblingId;
        this.kopiertGrunnlagId = kopiertGrunnlagId;
    }

    public Long getKopiertTilKoblingId() {
        return kopiertTilKoblingId;
    }

    public Long getKopiertFraKoblingId() {
        return kopiertFraKoblingId;
    }

    public Long getKopiertGrunnlagId() {
        return kopiertGrunnlagId;
    }

    public void setAktiv(boolean aktiv) {
        this.aktiv = aktiv;
    }

    @Override
    public String getIndexKey() {
        return IndexKey.createKey(kopiertTilKoblingId, kopiertFraKoblingId);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KoblingGrunnlagskopiSporing that = (KoblingGrunnlagskopiSporing) o;
        return kopiertTilKoblingId.equals(that.kopiertTilKoblingId) && kopiertFraKoblingId.equals(that.kopiertFraKoblingId) && kopiertGrunnlagId.equals(that.kopiertGrunnlagId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kopiertTilKoblingId, kopiertFraKoblingId, kopiertGrunnlagId);
    }
}
