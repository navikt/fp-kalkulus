package no.nav.folketrygdloven.kalkulator.modell.typer;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

import no.nav.folketrygdloven.kalkulator.modell.diff.SjekkVedKopiering;
import no.nav.folketrygdloven.kalkulus.kodeverk.Hjemmel;
import no.nav.folketrygdloven.kalkulus.kodeverk.Utfall;

/** Container for refusjon. */
public class Refusjon implements Serializable, Comparable<Refusjon> {


    @SjekkVedKopiering
    private BigDecimal refusjonskravPrÅr;
    @SjekkVedKopiering
    private BigDecimal saksbehandletRefusjonPrÅr;
    @SjekkVedKopiering
    private BigDecimal fordeltRefusjonPrÅr;
    @SjekkVedKopiering
    private BigDecimal manueltFordeltRefusjonPrÅr;
    @SjekkVedKopiering
    private Hjemmel hjemmelForRefusjonskravfrist;
    @SjekkVedKopiering
    private final Utfall refusjonskravFristUtfall;

    public Refusjon(BigDecimal refusjonskravPrÅr,
                    BigDecimal saksbehandletRefusjonPrÅr,
                    BigDecimal fordeltRefusjonPrÅr,
                    BigDecimal manueltFordeltRefusjonPrÅr,
                    Hjemmel hjemmelForRefusjonskravfrist, Utfall refusjonskravFristUtfall) {
        if (refusjonskravPrÅr == null && saksbehandletRefusjonPrÅr == null && fordeltRefusjonPrÅr == null && hjemmelForRefusjonskravfrist == null && manueltFordeltRefusjonPrÅr == null) {
            throw new IllegalStateException("refusjonskrav må Være satt");
        }
        this.refusjonskravPrÅr = refusjonskravPrÅr;
        this.saksbehandletRefusjonPrÅr = saksbehandletRefusjonPrÅr;
        this.fordeltRefusjonPrÅr = fordeltRefusjonPrÅr;
        this.manueltFordeltRefusjonPrÅr = manueltFordeltRefusjonPrÅr;
        this.hjemmelForRefusjonskravfrist = hjemmelForRefusjonskravfrist;
        this.refusjonskravFristUtfall = refusjonskravFristUtfall;
    }

    public Refusjon(Refusjon refusjon) {
        this.refusjonskravPrÅr = refusjon.getRefusjonskravPrÅr();
        this.saksbehandletRefusjonPrÅr = refusjon.getSaksbehandletRefusjonPrÅr();
        this.fordeltRefusjonPrÅr = refusjon.getFordeltRefusjonPrÅr();
        this.manueltFordeltRefusjonPrÅr = refusjon.getManueltFordeltRefusjonPrÅr();
        this.hjemmelForRefusjonskravfrist = refusjon.getHjemmelForRefusjonskravfrist();
        this.refusjonskravFristUtfall = refusjon.getRefusjonskravFristUtfall();
    }

    public static Refusjon medRefusjonskravPrÅr(BigDecimal refusjonskravPrÅr, Hjemmel hjemmel, Utfall refusjonskravFristUtfall) {
        return new Refusjon(refusjonskravPrÅr, null, null, null, hjemmel, refusjonskravFristUtfall);
    }

    public static Refusjon medSaksbehandletRefusjonPrÅr(BigDecimal saksbehandletRefusjonPrÅr) {
        return new Refusjon(null, saksbehandletRefusjonPrÅr, null, null, null, null);
    }

    public static Refusjon medFordeltRefusjonPrÅr(BigDecimal fordeltRefusjonPrÅr) {
        return new Refusjon(null, null, fordeltRefusjonPrÅr, null, null, null);
    }

    public static Refusjon medManueltFordeltRefusjonPrÅr(BigDecimal manueltFordeltRefusjonPrÅr) {
        return new Refusjon(null, null, null, manueltFordeltRefusjonPrÅr, null, null);
    }

    public BigDecimal getManueltFordeltRefusjonPrÅr() {
        return manueltFordeltRefusjonPrÅr;
    }

    public BigDecimal getRefusjonskravPrÅr() {
        return refusjonskravPrÅr;
    }

    public BigDecimal getSaksbehandletRefusjonPrÅr() {
        return saksbehandletRefusjonPrÅr;
    }

    public BigDecimal getFordeltRefusjonPrÅr() {
        return fordeltRefusjonPrÅr;
    }

    public Hjemmel getHjemmelForRefusjonskravfrist() {
        return hjemmelForRefusjonskravfrist;
    }

    public Utfall getRefusjonskravFristUtfall() {
        return refusjonskravFristUtfall;
    }

    public void setRefusjonskravPrÅr(BigDecimal refusjonskravPrÅr) {
        this.refusjonskravPrÅr = refusjonskravPrÅr;
    }

    public void setSaksbehandletRefusjonPrÅr(BigDecimal saksbehandletRefusjonPrÅr) {
        this.saksbehandletRefusjonPrÅr = saksbehandletRefusjonPrÅr;
    }

    public void setFordeltRefusjonPrÅr(BigDecimal fordeltRefusjonPrÅr) {
        this.fordeltRefusjonPrÅr = fordeltRefusjonPrÅr;
    }

    public void setManueltFordeltRefusjonPrÅr(BigDecimal manueltFordeltRefusjonPrÅr) {
        this.manueltFordeltRefusjonPrÅr = manueltFordeltRefusjonPrÅr;
    }

    public void setHjemmelForRefusjonskravfrist(Hjemmel hjemmelForRefusjonskravfrist) {
        this.hjemmelForRefusjonskravfrist = hjemmelForRefusjonskravfrist;
    }

    /**
     * Refusjonskrav settes på forskjellige steder i beregning dersom avklaringsbehov oppstår.
     * Først settes refusjonskravPrÅr, deretter saksbehandletRefusjonPrÅr og til slutt fordeltRefusjonPrÅr.
     * Det er det sist avklarte beløpet som til en hver tid skal være gjeldende.
     * @return returnerer det refusjonskravet som skal være gjeldende
     */
    public BigDecimal getGjeldendeRefusjonPrÅr() {
        if (manueltFordeltRefusjonPrÅr != null) {
            return manueltFordeltRefusjonPrÅr;
        } if (fordeltRefusjonPrÅr != null) {
            return fordeltRefusjonPrÅr;
        } else if (saksbehandletRefusjonPrÅr != null) {
            return saksbehandletRefusjonPrÅr;
        }
        if (refusjonskravFristUtfall != null && refusjonskravFristUtfall.equals(Utfall.UNDERKJENT)) {
            return BigDecimal.ZERO;
        }
        return refusjonskravPrÅr;
    }


    /** Returnerer refusjonskrav fra inntektsmelding om fristvilkåret er godkjent
     *
     * @return Innvilget refusjonskrav
     */
    public BigDecimal getInnvilgetRefusjonskravPrÅr() {
        if (refusjonskravFristUtfall != null && refusjonskravFristUtfall.equals(Utfall.UNDERKJENT)) {
            return BigDecimal.ZERO;
        }
        return refusjonskravPrÅr;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Refusjon refusjon = (Refusjon) o;
        return Objects.equals(refusjonskravPrÅr, refusjon.refusjonskravPrÅr) &&
                Objects.equals(saksbehandletRefusjonPrÅr, refusjon.saksbehandletRefusjonPrÅr) &&
                Objects.equals(fordeltRefusjonPrÅr, refusjon.fordeltRefusjonPrÅr) &&
                hjemmelForRefusjonskravfrist == refusjon.hjemmelForRefusjonskravfrist &&
                Objects.equals(refusjonskravFristUtfall, refusjon.refusjonskravFristUtfall);
    }

    @Override
    public int hashCode() {
        return Objects.hash(refusjonskravPrÅr, saksbehandletRefusjonPrÅr, fordeltRefusjonPrÅr, hjemmelForRefusjonskravfrist, refusjonskravFristUtfall);
    }

    @Override
    public int compareTo(Refusjon o) {
        return this.getGjeldendeRefusjonPrÅr().compareTo(o.getGjeldendeRefusjonPrÅr());
    }
}
