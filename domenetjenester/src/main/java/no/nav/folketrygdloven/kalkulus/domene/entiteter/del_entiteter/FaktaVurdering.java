package no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.mapping.FaktaVurderingKildeKodeverdiConverter;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.mapping.InntektskategoriKodeverdiConverter;
import no.nav.folketrygdloven.kalkulus.felles.diff.ChangeTracked;
import no.nav.folketrygdloven.kalkulus.felles.diff.IndexKey;
import no.nav.folketrygdloven.kalkulus.felles.diff.TraverseValue;
import no.nav.folketrygdloven.kalkulus.felles.jpa.BaseEntitet;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaVurderingKilde;

@Embeddable
public class FaktaVurdering implements Serializable, IndexKey, TraverseValue {

    @Column(name = "vurdering")
    @ChangeTracked
    private Boolean vurdering;

    @Convert(converter= FaktaVurderingKildeKodeverdiConverter.class)
    @Column(name = "kilde")
    @ChangeTracked
    private FaktaVurderingKilde kilde;


    public FaktaVurdering() {
        // hibernate
    }

    public FaktaVurdering(Boolean vurdering, FaktaVurderingKilde kilde) {
        this.vurdering = vurdering;
        this.kilde = kilde;
    }

    public FaktaVurdering(FaktaVurdering original) {
        this.vurdering = original.getVurdering();
        this.kilde = original.getKilde();
    }

    public Boolean getVurdering() {
        return vurdering;
    }

    public FaktaVurderingKilde getKilde() {
        return kilde;
    }

    @Override
    public String getIndexKey() {
        return IndexKey.createKey(vurdering, kilde);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FaktaVurdering that = (FaktaVurdering) o;
        return vurdering.equals(that.vurdering) && kilde == that.kilde;
    }

    @Override
    public int hashCode() {
        return Objects.hash(vurdering, kilde);
    }
}
