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
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import com.fasterxml.jackson.annotation.JsonBackReference;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.InternArbeidsforholdRef;
import no.nav.folketrygdloven.kalkulus.felles.diff.IndexKey;
import no.nav.folketrygdloven.kalkulus.felles.jpa.BaseEntitet;

@Entity(name = "FaktaArbeidsforholdEntitet")
@Table(name = "FAKTA_ARBEIDSFORHOLD")
public class FaktaArbeidsforholdEntitet extends BaseEntitet implements IndexKey {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_FAKTA_ARBEIDSFORHOLD")
    private Long id;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    @JsonBackReference
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "FAKTA_AGGREGAT_ID", nullable = false, updatable = false)
    private FaktaAggregatEntitet faktaAggregat;

    @Embedded
    private Arbeidsgiver arbeidsgiver;

    @Embedded
    private InternArbeidsforholdRef arbeidsforholdRef;

    @Column(name = "ER_TIDSBEGRENSET")
    private Boolean erTidsbegrenset;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_ER_TIDSBEGRENSET", updatable = false)
    private FaktaVurderingEntitet erTidsbegrensetVurdering;

    @Column(name = "HAR_MOTTATT_YTELSE")
    private Boolean harMottattYtelse;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_HAR_MOTTATT_YTELSE", updatable = false)
    private FaktaVurderingEntitet harMottattYtelseVurdering;

    @Column(name = "HAR_LONNSENDRING_I_BEREGNINGSPERIODEN")
    private Boolean harLønnsendringIBeregningsperioden;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_HAR_LONNSENDRING_I_BEREGNINGSPERIODEN", updatable = false)
    private FaktaVurderingEntitet harLønnsendringIBeregningsperiodenVurdering;

    public FaktaArbeidsforholdEntitet() {
        // hibernate
    }

    public FaktaArbeidsforholdEntitet(FaktaArbeidsforholdEntitet original) {
        this.arbeidsgiver = Arbeidsgiver.fra(original.getArbeidsgiver());
        this.arbeidsforholdRef = original.getArbeidsforholdRef();
        this.erTidsbegrenset = original.getErTidsbegrenset();
        this.harMottattYtelse = original.getHarMottattYtelse();
        this.harLønnsendringIBeregningsperioden = original.getHarLønnsendringIBeregningsperioden();
    }


    public Arbeidsgiver getArbeidsgiver() {
        return arbeidsgiver;
    }

    public InternArbeidsforholdRef getArbeidsforholdRef() {
        return arbeidsforholdRef == null ? InternArbeidsforholdRef.nullRef() : arbeidsforholdRef;
    }

    void setFaktaAggregat(FaktaAggregatEntitet faktaAggregat) {
        this.faktaAggregat = faktaAggregat;
    }


    public boolean gjelderFor(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRef arbeidsforholdRef) {
        return Objects.equals(this.getArbeidsgiver(), arbeidsgiver) &&
                this.getArbeidsforholdRef().gjelderFor(arbeidsforholdRef);
    }

    public Boolean getErTidsbegrenset() {
        return erTidsbegrenset;
    }

    public Boolean getHarMottattYtelse() {
        return harMottattYtelse;
    }

    public Boolean getHarLønnsendringIBeregningsperioden() {
        return harLønnsendringIBeregningsperioden;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(FaktaArbeidsforholdEntitet kopi) {
        return new Builder(kopi);
    }

    @Override
    public String getIndexKey() {
        return IndexKey.createKey(arbeidsgiver, arbeidsforholdRef);
    }

    public static class Builder {
        private FaktaArbeidsforholdEntitet mal;

        private Builder() {
            mal = new FaktaArbeidsforholdEntitet();
        }

        public Builder(FaktaArbeidsforholdEntitet kopi) {
            mal = new FaktaArbeidsforholdEntitet(kopi);
        }

        public Builder medArbeidsgiver(Arbeidsgiver arbeidsgiver) {
            mal.arbeidsgiver = arbeidsgiver;
            return this;
        }

        public Builder medArbeidsforholdRef(InternArbeidsforholdRef arbeidsforholdRef) {
            mal.arbeidsforholdRef = arbeidsforholdRef;
            return this;
        }

        public Builder medHarMottattYtelse(Boolean harMottattYtelse) {
            mal.harMottattYtelse = harMottattYtelse;
            return this;
        }

        public Builder medErTidsbegrenset(Boolean erTidsbegrenset) {
            mal.erTidsbegrenset = erTidsbegrenset;
            return this;
        }

        public Builder medHarLønnsendringIBeregningsperioden(Boolean harLønnsendringIBeregningsperioden) {
            mal.harLønnsendringIBeregningsperioden = harLønnsendringIBeregningsperioden;
            return this;
        }

        public FaktaArbeidsforholdEntitet build() {
            verifyStateForBuild();
            return mal;
        }

        private void verifyStateForBuild() {
            Objects.requireNonNull(mal.arbeidsgiver, "arbeidsgiver");
            if (erUgyldig()) {
                throw new IllegalStateException("Må ha satt minst et faktafelt.");
            }
        }

        public boolean erUgyldig() {
            return mal.erTidsbegrenset == null
                    && mal.harLønnsendringIBeregningsperioden == null
                    && mal.harMottattYtelse == null;
        }
    }
}
