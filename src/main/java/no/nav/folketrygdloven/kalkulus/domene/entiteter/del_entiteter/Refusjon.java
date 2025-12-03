package no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.mapping.HjemmelKodeverdiConverter;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.mapping.RefusjonskravFristUtfallKodeverdiConverter;
import no.nav.folketrygdloven.kalkulus.domene.felles.diff.IndexKey;
import no.nav.folketrygdloven.kalkulus.domene.felles.diff.TraverseValue;
import no.nav.folketrygdloven.kalkulus.kodeverk.Hjemmel;
import no.nav.folketrygdloven.kalkulus.kodeverk.Utfall;

/** En container for refusjon */
@Embeddable
public class Refusjon implements Serializable, IndexKey, TraverseValue, Comparable<Refusjon> {


    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "refusjonskrav_pr_aar")))
    private Beløp refusjonskravPrÅr;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "saksbehandlet_refusjon_pr_aar")))
    private Beløp saksbehandletRefusjonPrÅr;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "fordelt_refusjon_pr_aar")))
    private Beløp fordeltRefusjonPrÅr;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "manuelt_fordelt_refusjon_pr_aar")))
    private Beløp manueltFordeltRefusjonPrÅr;

    @Convert(converter = HjemmelKodeverdiConverter.class)
    @Column(name = "hjemmel_for_refusjonskravfrist")
    private Hjemmel hjemmelForRefusjonskravfrist;

    @Convert(converter= RefusjonskravFristUtfallKodeverdiConverter.class)
    @Column(name = "refusjonskrav_frist_utfall")
    private Utfall refusjonskravFristUtfall;

    @SuppressWarnings("unused")
    protected Refusjon() {
        // for JPA
    }

    public Refusjon(Beløp refusjonskravPrÅr,
                    Beløp saksbehandletRefusjonPrÅr,
                    Beløp fordeltRefusjonPrÅr,
                    Beløp manueltFordeltRefusjonPrÅr, Hjemmel hjemmelForRefusjonskravfrist,
                    Utfall refusjonskravFristUtfall) {
        if (refusjonskravPrÅr == null && saksbehandletRefusjonPrÅr == null && fordeltRefusjonPrÅr == null && refusjonskravFristUtfall == null && manueltFordeltRefusjonPrÅr == null) {
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


    public Beløp getRefusjonskravPrÅr() {
        return refusjonskravPrÅr;
    }

    public Beløp getSaksbehandletRefusjonPrÅr() {
        return saksbehandletRefusjonPrÅr;
    }

    public Beløp getFordeltRefusjonPrÅr() {
        return fordeltRefusjonPrÅr;
    }

    public Beløp getManueltFordeltRefusjonPrÅr() {
        return manueltFordeltRefusjonPrÅr;
    }

    public Hjemmel getHjemmelForRefusjonskravfrist() {
        return hjemmelForRefusjonskravfrist;
    }

    public Utfall getRefusjonskravFristUtfall() {
        return refusjonskravFristUtfall;
    }

    public void setRefusjonskravPrÅr(Beløp refusjonskravPrÅr) {
        this.refusjonskravPrÅr = refusjonskravPrÅr;
    }

    public void setSaksbehandletRefusjonPrÅr(Beløp saksbehandletRefusjonPrÅr) {
        this.saksbehandletRefusjonPrÅr = saksbehandletRefusjonPrÅr;
    }

    public void setFordeltRefusjonPrÅr(Beløp fordeltRefusjonPrÅr) {
        this.fordeltRefusjonPrÅr = fordeltRefusjonPrÅr;
    }

    public void setHjemmelForRefusjonskravfrist(Hjemmel hjemmelForRefusjonskravfrist) {
        this.hjemmelForRefusjonskravfrist = hjemmelForRefusjonskravfrist;
    }

    public void setRefusjonskravFristUtfall(Utfall refusjonskravFristUtfall) {
        this.refusjonskravFristUtfall = refusjonskravFristUtfall;
    }

    /**
     * Refusjonskrav settes på forskjellige steder i beregning dersom avklaringsbehov oppstår.
     * Først settes refusjonskravPrÅr, deretter saksbehandletRefusjonPrÅr og til slutt fordeltRefusjonPrÅr.
     * Det er det sist avklarte beløpet som til en hver tid skal være gjeldende.
     * @return returnerer det refusjonskravet som skal være gjeldende
     */
    public Beløp getGjeldendeRefusjonPrÅr() {
        if (manueltFordeltRefusjonPrÅr != null) {
            return manueltFordeltRefusjonPrÅr;
        } else if (fordeltRefusjonPrÅr != null) {
            return fordeltRefusjonPrÅr;
        } else if (saksbehandletRefusjonPrÅr != null) {
            return saksbehandletRefusjonPrÅr;
        }
        if (refusjonskravFristUtfall != null && refusjonskravFristUtfall.equals(Utfall.UNDERKJENT)) {
            return Beløp.ZERO;
        }
        return refusjonskravPrÅr;
    }

    @Override
    public String getIndexKey() {
        return IndexKey.createKey(refusjonskravPrÅr, saksbehandletRefusjonPrÅr, fordeltRefusjonPrÅr, manueltFordeltRefusjonPrÅr, hjemmelForRefusjonskravfrist);
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Refusjon refusjon = (Refusjon) o;
        return Objects.equals(refusjonskravPrÅr, refusjon.refusjonskravPrÅr) &&
                Objects.equals(saksbehandletRefusjonPrÅr, refusjon.saksbehandletRefusjonPrÅr) &&
                Objects.equals(fordeltRefusjonPrÅr, refusjon.fordeltRefusjonPrÅr) &&
                Objects.equals(manueltFordeltRefusjonPrÅr, refusjon.manueltFordeltRefusjonPrÅr) &&
                hjemmelForRefusjonskravfrist == refusjon.hjemmelForRefusjonskravfrist;
    }

    @Override
    public int hashCode() {
        return Objects.hash(refusjonskravPrÅr, saksbehandletRefusjonPrÅr, fordeltRefusjonPrÅr, manueltFordeltRefusjonPrÅr, hjemmelForRefusjonskravfrist);
    }

    @Override
    public int compareTo(Refusjon o) {
        return this.getGjeldendeRefusjonPrÅr().compareTo(o.getGjeldendeRefusjonPrÅr());
    }
}
