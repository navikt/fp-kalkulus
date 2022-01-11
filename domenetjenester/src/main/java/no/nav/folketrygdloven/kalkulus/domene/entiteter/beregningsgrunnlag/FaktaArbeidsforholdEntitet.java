package no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag;

import java.util.Objects;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import com.fasterxml.jackson.annotation.JsonBackReference;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.FaktaVurdering;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.InternArbeidsforholdRef;
import no.nav.folketrygdloven.kalkulus.felles.diff.IndexKey;
import no.nav.folketrygdloven.kalkulus.felles.jpa.BaseEntitet;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaVurderingKilde;

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

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "vurdering", column = @Column(name = "er_tidsbegrenset")),
            @AttributeOverride(name = "kilde", column = @Column(name = "er_tidsbegrenset_kilde"))
    })
    private FaktaVurdering erTidsbegrenset;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "vurdering", column = @Column(name = "har_mottatt_ytelse")),
            @AttributeOverride(name = "kilde", column = @Column(name = "har_mottatt_ytelse_kilde"))
    })
    private FaktaVurdering harMottattYtelse;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "vurdering", column = @Column(name = "har_lonnsendring_i_beregningsperioden")),
            @AttributeOverride(name = "kilde", column = @Column(name = "har_lonnsendring_i_beregningsperioden_kilde"))
    })
    private FaktaVurdering harLønnsendringIBeregningsperioden;


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

    public Boolean getErTidsbegrensetVurdering() {
        return finnVurdering(erTidsbegrenset);
    }


    public Boolean getHarMottattYtelseVurdering() {
        return finnVurdering(harMottattYtelse);
    }

    public Boolean getHarLønnsendringIBeregningsperiodenVurdering() {
        return finnVurdering(harLønnsendringIBeregningsperioden);
    }

    private Boolean finnVurdering(FaktaVurdering faktaVurdering) {
        if (faktaVurdering == null) {
            return null;
        }
        return faktaVurdering.getVurdering();
    }

    public FaktaVurdering getErTidsbegrenset() {
        return erTidsbegrenset;
    }

    public FaktaVurdering getHarMottattYtelse() {
        return harMottattYtelse;
    }

    public FaktaVurdering getHarLønnsendringIBeregningsperioden() {
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
            mal.harMottattYtelse = new FaktaVurdering(harMottattYtelse, FaktaVurderingKilde.SAKSBEHANDLER);
            return this;
        }

        public Builder medErTidsbegrenset(Boolean erTidsbegrenset) {
            mal.erTidsbegrenset = new FaktaVurdering(erTidsbegrenset, FaktaVurderingKilde.SAKSBEHANDLER);
            return this;
        }

        public Builder medHarLønnsendringIBeregningsperioden(Boolean harLønnsendringIBeregningsperioden) {
            mal.harLønnsendringIBeregningsperioden = new FaktaVurdering(harLønnsendringIBeregningsperioden, FaktaVurderingKilde.SAKSBEHANDLER);
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
