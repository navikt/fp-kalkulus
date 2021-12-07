package no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;

import no.nav.folketrygdloven.kalkulus.felles.diff.ChangeTracked;
import no.nav.folketrygdloven.kalkulus.felles.diff.IndexKey;
import no.nav.folketrygdloven.kalkulus.felles.diff.TraverseValue;

/**
 * Årsgrunnlag representerer inntektsgrunnlaget for en andel
 */
@Embeddable
public class Årsgrunnlag implements Serializable, IndexKey, TraverseValue, Comparable<Årsgrunnlag> {

    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "beregnet_pr_aar")))
    @ChangeTracked
    @Embedded
    private Beløp beregnetPrÅr;

    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "fordelt_pr_aar")))
    @ChangeTracked
    @Embedded
    private Beløp fordeltPrÅr;

    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "manuelt_fordelt_pr_aar")))
    @ChangeTracked
    @Embedded
    private Beløp manueltFordeltPrÅr;

    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "overstyrt_pr_aar")))
    @ChangeTracked
    @Embedded
    private Beløp overstyrtPrÅr;

    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "besteberegning_pr_aar")))
    @ChangeTracked
    @Embedded
    private Beløp besteberegningPrÅr;


    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "brutto_pr_aar")))
    @ChangeTracked
    @Embedded
    private Beløp bruttoPrÅr;


    public Årsgrunnlag() {
    }

    public Årsgrunnlag(Beløp beregnetPrÅr, Beløp fordeltPrÅr, Beløp manueltFordeltPrÅr, Beløp overstyrtPrÅr, Beløp besteberegningPrÅr, Beløp bruttoPrÅr) {
        this.beregnetPrÅr = beregnetPrÅr;
        this.fordeltPrÅr = fordeltPrÅr;
        this.manueltFordeltPrÅr = manueltFordeltPrÅr;
        this.overstyrtPrÅr = overstyrtPrÅr;
        this.besteberegningPrÅr = besteberegningPrÅr;
        this.bruttoPrÅr = bruttoPrÅr;
    }

    public Årsgrunnlag(Årsgrunnlag årsgrunnlag) {
        this.bruttoPrÅr = årsgrunnlag.bruttoPrÅr;
        this.fordeltPrÅr = årsgrunnlag.fordeltPrÅr;
        this.manueltFordeltPrÅr = årsgrunnlag.manueltFordeltPrÅr;
        this.overstyrtPrÅr = årsgrunnlag.overstyrtPrÅr;
        this.besteberegningPrÅr = årsgrunnlag.besteberegningPrÅr;
        this.beregnetPrÅr = årsgrunnlag.beregnetPrÅr;
    }

    @Override
    public String getIndexKey() {
        return IndexKey.createKey(getBeregnetPrÅr(), getOverstyrtPrÅr(), getFordeltPrÅr(), getBruttoPrÅr());
    }

    public Beløp getBeregnetPrÅr() {
        return beregnetPrÅr;
    }

    public Beløp getFordeltPrÅr() {
        return fordeltPrÅr;
    }

    public Beløp getManueltFordeltPrÅr() {
        return manueltFordeltPrÅr;
    }

    public Beløp getOverstyrtPrÅr() {
        return overstyrtPrÅr;
    }

    public Beløp getBruttoPrÅr() {
        return bruttoPrÅr;
    }

    public Beløp getBesteberegningPrÅr() {
        return besteberegningPrÅr;
    }

    public boolean erSatt() {
        return beregnetPrÅr != null || overstyrtPrÅr != null || fordeltPrÅr != null || manueltFordeltPrÅr != null || bruttoPrÅr != null;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Årsgrunnlag that = (Årsgrunnlag) o;
        return Objects.equals(beregnetPrÅr, that.beregnetPrÅr) && Objects.equals(fordeltPrÅr, that.fordeltPrÅr) && Objects.equals(manueltFordeltPrÅr, that.manueltFordeltPrÅr) && Objects.equals(overstyrtPrÅr, that.overstyrtPrÅr) && Objects.equals(bruttoPrÅr, that.bruttoPrÅr);
    }

    @Override
    public int hashCode() {
        return Objects.hash(beregnetPrÅr, fordeltPrÅr, manueltFordeltPrÅr, overstyrtPrÅr, bruttoPrÅr);
    }

    @Override
    public String toString() {
        return "Årsgrunnlag{" +
                "beregnetPrÅr=" + beregnetPrÅr +
                ", fordeltPrÅr=" + fordeltPrÅr +
                ", manueltFordeltPrÅr=" + manueltFordeltPrÅr +
                ", overstyrtPrÅr=" + overstyrtPrÅr +
                ", bruttoPrÅr=" + bruttoPrÅr +
                '}';
    }

    @Override
    public int compareTo(Årsgrunnlag o) {
        if (!erSatt() || !o.erSatt()) {
            if (erSatt() == o.erSatt()) {
                return 0;
            }
            return erSatt() ? 1 : -1;
        }
        return getBruttoPrÅr().compareTo(o.getBruttoPrÅr());
    }
}
