package no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag;

import java.util.Objects;

import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningSatsType;

public class BeregningSats {

    private long verdi;
    private Intervall periode;
    private BeregningSatsType satsType = BeregningSatsType.UDEFINERT;

    @SuppressWarnings("unused")
    private BeregningSats() {
        // For hibernate
    }

    public BeregningSats(BeregningSatsType satsType, Intervall periode, Long verdi) {
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

    public Intervall getPeriode() {
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

        return Objects.equals(this.getSatsType(), annen.getSatsType())
            && Objects.equals(this.periode, annen.periode)
            && Objects.equals(this.verdi, annen.verdi);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSatsType(), periode, verdi);
    }

    private void setSatsType(BeregningSatsType satsType) {
        this.satsType = satsType == null ? BeregningSatsType.UDEFINERT : satsType;
    }
}
