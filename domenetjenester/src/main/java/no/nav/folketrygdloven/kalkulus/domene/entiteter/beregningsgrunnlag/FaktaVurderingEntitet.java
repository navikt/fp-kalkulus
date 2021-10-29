package no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import com.fasterxml.jackson.annotation.JsonBackReference;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.InternArbeidsforholdRef;
import no.nav.folketrygdloven.kalkulus.felles.diff.ChangeTracked;
import no.nav.folketrygdloven.kalkulus.felles.diff.IndexKey;
import no.nav.folketrygdloven.kalkulus.felles.jpa.BaseEntitet;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaVurderingKilde;

@Entity(name = "FaktaVurderingEntitet")
@Table(name = "FAKTA_VURDERING")
public class FaktaVurderingEntitet extends BaseEntitet implements IndexKey {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_FAKTA_VURDERING")
    private Long id;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    @Column(name = "VURDERING")
    @ChangeTracked
    private Boolean vurdering;

    @Column(name = "KILDE")
    @ChangeTracked
    private FaktaVurderingKilde kilde;


    public FaktaVurderingEntitet() {
        // hibernate
    }

    public FaktaVurderingEntitet(FaktaVurderingEntitet original) {
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

    public static class Builder {
        private FaktaVurderingEntitet mal;

        private Builder() {
            mal = new FaktaVurderingEntitet();
        }

        public Builder(FaktaVurderingEntitet kopi) {
            mal = new FaktaVurderingEntitet(kopi);
        }

        public Builder medVurdering(boolean vurdering, FaktaVurderingKilde kilde) {
            mal.vurdering = vurdering;
            mal.kilde = kilde;
            return this;
        }

        public FaktaVurderingEntitet build() {
            verifyStateForBuild();
            return mal;
        }

        private void verifyStateForBuild() {
            Objects.requireNonNull(mal.vurdering, "vurdering");
            Objects.requireNonNull(mal.kilde, "kilde");
        }

    }
}
