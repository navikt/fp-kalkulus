package no.nav.folketrygdloven.kalkulus.domene.entiteter;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.mapping.BeregningSatsTypeKodeverdiConverter;
import no.nav.folketrygdloven.kalkulus.felles.jpa.BaseEntitet;
import no.nav.folketrygdloven.kalkulus.felles.jpa.IntervallEntitet;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningSatsType;

@Entity(name = "BeregningSats")
@Table(name = "BR_SATS")
public class BeregningSats extends BaseEntitet {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GLOBAL_PK_SEQ_GENERATOR")
    private Long id;

    @Version
    @Column(name = "versjon", nullable = false)
    private int versjon;

    @Column(name = "verdi", nullable = false)
    private long verdi;

    @Embedded
    IntervallEntitet periode;

    @Convert(converter = BeregningSatsTypeKodeverdiConverter.class)
    @Column(name = "sats_type", nullable = false)
    private BeregningSatsType satsType = BeregningSatsType.UDEFINERT;

    @SuppressWarnings("unused")
    private BeregningSats() {
        // For hibernate
    }

    public BeregningSats(BeregningSatsType satsType, IntervallEntitet periode, Long verdi) {
        Objects.requireNonNull(satsType, "satsType må være satt");
        Objects.requireNonNull(periode, "periode må være satt");
        Objects.requireNonNull(verdi, "verdi  må være satt");
        this.setSatsType(satsType);
        this.periode = periode;
        this.verdi = verdi;
    }

    public long getVerdi() {
        return verdi;
    }

    public IntervallEntitet getPeriode() {
        return periode;
    }

    public BeregningSatsType getSatsType() {
        return Objects.equals(BeregningSatsType.UDEFINERT, satsType) ? null : satsType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BeregningSats)) {
            return false;
        }
        BeregningSats annen = (BeregningSats) o;

        return Objects.equals(this.getSatsType(), annen.getSatsType()) && Objects.equals(this.periode, annen.periode) && Objects.equals(this.verdi,
            annen.verdi);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSatsType(), periode, verdi);
    }

    private void setSatsType(BeregningSatsType satsType) {
        this.satsType = satsType == null ? BeregningSatsType.UDEFINERT : satsType;
    }
}
