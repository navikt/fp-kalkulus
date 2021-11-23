package no.nav.folketrygdloven.kalkulator.modell.typer;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Årsgrunnlag representerer inntektsgrunnlaget for en andel
 */
public class Årsgrunnlag implements Serializable, Comparable<Årsgrunnlag> {

    private BigDecimal beregnetPrÅr;
    private BigDecimal fordeltPrÅr;
    private BigDecimal overstyrtPrÅr;
    private BigDecimal besteberegningPrÅr;
    private BigDecimal bruttoPrÅr;

    public Årsgrunnlag() {
    }

    public Årsgrunnlag(Årsgrunnlag årsgrunnlag) {
        this.bruttoPrÅr = årsgrunnlag.bruttoPrÅr;
        this.fordeltPrÅr = årsgrunnlag.fordeltPrÅr;
        this.overstyrtPrÅr = årsgrunnlag.overstyrtPrÅr;
        this.besteberegningPrÅr = årsgrunnlag.besteberegningPrÅr;
        this.beregnetPrÅr = årsgrunnlag.beregnetPrÅr;
    }

    public BigDecimal getBeregnetPrÅr() {
        return beregnetPrÅr;
    }

    public BigDecimal getFordeltPrÅr() {
        return fordeltPrÅr;
    }

    public BigDecimal getOverstyrtPrÅr() {
        return overstyrtPrÅr;
    }

    public BigDecimal getBruttoPrÅr() {
        return bruttoPrÅr;
    }

    public BigDecimal getBesteberegningPrÅr() {
        return besteberegningPrÅr;
    }

    public void setBeregnetPrÅr(BigDecimal beregnetPrÅr) {
        if (beregnetPrÅr != null && fordeltPrÅr == null && overstyrtPrÅr == null && besteberegningPrÅr == null) {
            bruttoPrÅr = beregnetPrÅr;
        }
        this.beregnetPrÅr = beregnetPrÅr;
    }

    public void setFordeltPrÅr(BigDecimal fordeltPrÅr) {
        this.fordeltPrÅr = fordeltPrÅr;
        if (fordeltPrÅr != null) {
            this.bruttoPrÅr = fordeltPrÅr;
        }
    }

    public void setOverstyrtPrÅr(BigDecimal overstyrtPrÅr) {
        this.overstyrtPrÅr = overstyrtPrÅr;
        if (overstyrtPrÅr != null && fordeltPrÅr == null && besteberegningPrÅr == null) {
            bruttoPrÅr = overstyrtPrÅr;
        }
    }

    public void setBesteberegningPrÅr(BigDecimal besteberegningPrÅr) {
        this.besteberegningPrÅr = besteberegningPrÅr;
        if (besteberegningPrÅr != null && fordeltPrÅr == null) {
            bruttoPrÅr = besteberegningPrÅr;
        }
    }

    public boolean erSatt() {
        return beregnetPrÅr != null || overstyrtPrÅr != null || fordeltPrÅr != null || bruttoPrÅr != null;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Årsgrunnlag that = (Årsgrunnlag) o;
        return Objects.equals(beregnetPrÅr, that.beregnetPrÅr) && Objects.equals(fordeltPrÅr, that.fordeltPrÅr) && Objects.equals(overstyrtPrÅr, that.overstyrtPrÅr) && Objects.equals(bruttoPrÅr, that.bruttoPrÅr);
    }

    @Override
    public int hashCode() {
        return Objects.hash(beregnetPrÅr, fordeltPrÅr, overstyrtPrÅr, bruttoPrÅr);
    }

    @Override
    public String toString() {
        return "Årsgrunnlag{" +
                "beregnetPrÅr=" + beregnetPrÅr +
                ", fordeltPrÅr=" + fordeltPrÅr +
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
