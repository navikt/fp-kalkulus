package no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.mapping.InntektskategoriKodeverdiConverter;
import no.nav.folketrygdloven.kalkulus.felles.diff.IndexKey;
import no.nav.folketrygdloven.kalkulus.felles.diff.TraverseValue;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;

/**
 * FastsattInntektskategori representerer inntektskategorien for en andel
 */
@Embeddable
public class FastsattInntektskategori implements Serializable, IndexKey, TraverseValue, Comparable<FastsattInntektskategori> {

    @Convert(converter= InntektskategoriKodeverdiConverter.class)
    @Column(name="inntektskategori", nullable = false)
    private Inntektskategori inntektskategori = Inntektskategori.UDEFINERT;

    @Convert(converter= InntektskategoriKodeverdiConverter.class)
    @Column(name="inntektskategori_fordeling")
    private Inntektskategori inntektskategoriAutomatiskFordeling;

    @Convert(converter= InntektskategoriKodeverdiConverter.class)
    @Column(name="inntektskategori_manuell_fordeling")
    private Inntektskategori inntektskategoriManuellFordeling;

    public FastsattInntektskategori() {
    }


    public FastsattInntektskategori(Inntektskategori inntektskategori, Inntektskategori inntektskategoriAutomatiskFordeling, Inntektskategori inntektskategoriManuellFordeling) {
        this.inntektskategori = inntektskategori;
        this.inntektskategoriAutomatiskFordeling = inntektskategoriAutomatiskFordeling;
        this.inntektskategoriManuellFordeling = inntektskategoriManuellFordeling;
    }

    public FastsattInntektskategori(FastsattInntektskategori fastsattInntektskategori) {
        this.inntektskategori = fastsattInntektskategori.getInntektskategori();
        this.inntektskategoriAutomatiskFordeling = fastsattInntektskategori.getInntektskategoriAutomatiskFordeling();
        this.inntektskategoriManuellFordeling = fastsattInntektskategori.getInntektskategoriManuellFordeling();
    }


    @Override
    public String getIndexKey() {
        return IndexKey.createKey(getInntektskategori(), getInntektskategoriAutomatiskFordeling(), getInntektskategoriManuellFordeling());
    }

    public Inntektskategori getInntektskategori() {
        return inntektskategori;
    }

    public Inntektskategori getInntektskategoriAutomatiskFordeling() {
        return inntektskategoriAutomatiskFordeling;
    }

    public Inntektskategori getInntektskategoriManuellFordeling() {
        return inntektskategoriManuellFordeling;
    }

    // Rekkefølge er viktig
    public Inntektskategori getGjeldendeInntektskategori() {
        if (inntektskategoriManuellFordeling != null) {
            return inntektskategoriManuellFordeling;
        } else if (inntektskategoriAutomatiskFordeling != null) {
            return inntektskategoriAutomatiskFordeling;
        }
        return inntektskategori;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FastsattInntektskategori that = (FastsattInntektskategori) o;
        return inntektskategori == that.inntektskategori &&
                inntektskategoriAutomatiskFordeling == that.inntektskategoriAutomatiskFordeling &&
                inntektskategoriManuellFordeling == that.inntektskategoriManuellFordeling;
    }

    @Override
    public int hashCode() {
        return Objects.hash(inntektskategori, inntektskategoriAutomatiskFordeling, inntektskategoriManuellFordeling);
    }

    @Override
    public String toString() {
        return "FastsattInntektskategori{" +
                "inntektskategori=" + inntektskategori +
                ", inntektskategoriFordeling=" + inntektskategoriAutomatiskFordeling +
                ", inntektskategoriManuellFordeling=" + inntektskategoriManuellFordeling +
                '}';
    }

    @Override
    public int compareTo(FastsattInntektskategori o) {
        return getGjeldendeInntektskategori().compareTo(o.getGjeldendeInntektskategori());
    }
}
