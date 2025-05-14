package no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.InternArbeidsforholdRef;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.mapping.BeregingAktivitetHandlingTypeKodeverdiConverter;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.mapping.OpptjeningAktivitetTypeKodeverdiConverter;
import no.nav.folketrygdloven.kalkulus.felles.diff.IndexKey;
import no.nav.folketrygdloven.kalkulus.felles.jpa.BaseEntitet;
import no.nav.folketrygdloven.kalkulus.felles.jpa.IntervallEntitet;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningAktivitetHandlingType;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;

@Entity(name = "AktivitetEntitet")
@Table(name = "AKTIVITET")
public class AktivitetEntitet extends BaseEntitet implements IndexKey, Comparable<AktivitetEntitet> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GLOBAL_PK_SEQ_GENERATOR")
    private Long id;

    @Version
    @Column(name = "versjon", nullable = false)
    private int versjon;

    @Embedded
    @AttributeOverride(name = "fomDato", column = @Column(name = "fom"))
    @AttributeOverride(name = "tomDato", column = @Column(name = "tom"))
    private IntervallEntitet periode;

    @Embedded
    private Arbeidsgiver arbeidsgiver;

    @Embedded
    private InternArbeidsforholdRef arbeidsforholdRef;

    @Convert(converter = OpptjeningAktivitetTypeKodeverdiConverter.class)
    @Column(name = "opptjening_aktivitet_type", nullable = false)
    private OpptjeningAktivitetType opptjeningAktivitetType = OpptjeningAktivitetType.UDEFINERT;

    @Convert(converter = BeregingAktivitetHandlingTypeKodeverdiConverter.class)
    @Column(name = "overstyr_handling_type")
    private BeregningAktivitetHandlingType overstyrHandlingType;

    public AktivitetEntitet() {
        // hibernate
    }

    public AktivitetEntitet(AktivitetEntitet original) {
        this.opptjeningAktivitetType = original.getOpptjeningAktivitetType();
        this.periode = original.getPeriode();
        this.arbeidsgiver = Arbeidsgiver.fra(original.getArbeidsgiver());
        this.arbeidsforholdRef = original.getArbeidsforholdRef();
        this.overstyrHandlingType = original.getOverstyrHandlingType().orElse(null);
    }

    public IntervallEntitet getPeriode() {
        return periode;
    }

    public Arbeidsgiver getArbeidsgiver() {
        return arbeidsgiver;
    }

    public InternArbeidsforholdRef getArbeidsforholdRef() {
        return arbeidsforholdRef == null ? InternArbeidsforholdRef.nullRef() : arbeidsforholdRef;
    }

    public OpptjeningAktivitetType getOpptjeningAktivitetType() {
        return opptjeningAktivitetType != null ? opptjeningAktivitetType : OpptjeningAktivitetType.UDEFINERT;
    }

    public Optional<BeregningAktivitetHandlingType> getOverstyrHandlingType() {
        return Optional.ofNullable(overstyrHandlingType);
    }

    public AktivitetNøkkel getNøkkel() {
        AktivitetNøkkel.Builder builder = AktivitetNøkkel.builder()
            .medOpptjeningAktivitetType(opptjeningAktivitetType)
            .medFom(periode.getFomDato())
            .medTom(periode.getTomDato())
            .medArbeidsforholdRef(getArbeidsforholdRef().getReferanse());
        if (arbeidsgiver != null) {
            builder.medArbeidsgiverIdentifikator(arbeidsgiver.getIdentifikator());
        }
        return builder.build();
    }

    public boolean gjelderFor(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRef arbeidsforholdRef) {
        return Objects.equals(this.getArbeidsgiver(), arbeidsgiver) && this.getArbeidsforholdRef().gjelderFor(arbeidsforholdRef);
    }

    boolean skalBrukes(AktivitetAggregatEntitet overstyringer) {
        List<AktivitetEntitet> overstyringerForAktivitet = overstyringer.getAktiviteter()
            .stream()
            .filter(overstyring -> overstyring.getNøkkel().equals(this.getNøkkel()))
            .toList();
        if (overstyringerForAktivitet.isEmpty()) {
            return true;
        }
        if (overstyringerForAktivitet.size() == 1) {
            return !BeregningAktivitetHandlingType.IKKE_BENYTT.equals(overstyringerForAktivitet.getFirst().getOverstyrHandlingType().orElseThrow());
        }
        throw new IllegalStateException("Kan ikke ha flere overstyringer for aktivitet " + this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AktivitetEntitet that = (AktivitetEntitet) o;
        return Objects.equals(periode, that.periode) && Objects.equals(arbeidsgiver, that.arbeidsgiver) && Objects.equals(this.getArbeidsforholdRef(),
            that.getArbeidsforholdRef()) && Objects.equals(opptjeningAktivitetType, that.opptjeningAktivitetType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(periode, arbeidsgiver, arbeidsforholdRef, opptjeningAktivitetType);
    }

    @Override
    public String toString() {
        return "BeregningAktivitetEntitet{" + "id=" + id + ", periode=" + periode + ", arbeidsgiver=" + arbeidsgiver + ", arbeidsforholdRef="
            + arbeidsforholdRef + ", opptjeningAktivitetType=" + getOpptjeningAktivitetType().getKode() + '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(AktivitetEntitet kopi) {
        return new Builder(kopi);
    }

    @Override
    public String getIndexKey() {
        return IndexKey.createKey(arbeidsgiver, arbeidsforholdRef);
    }

    @Override
    public int compareTo(AktivitetEntitet o) {
        int sammenlignFom = this.periode.getFomDato().compareTo(o.getPeriode().getFomDato());
        boolean erFomUlik = sammenlignFom != 0;
        if (erFomUlik) {
            return sammenlignFom;
        }

        if (this.arbeidsgiver == null) {
            if (o.arbeidsgiver == null) {
                return this.opptjeningAktivitetType.compareTo(o.opptjeningAktivitetType);
            }
            return -1;
        }

        if (o.arbeidsgiver == null) {
            return 1;
        }

        int sammenlignArbeidsgiverId = this.arbeidsgiver.getIdentifikator().compareTo(o.arbeidsgiver.getIdentifikator());
        boolean erArbeidsgiverIdUlike = sammenlignArbeidsgiverId != 0;
        if (erArbeidsgiverIdUlike) {
            return sammenlignArbeidsgiverId;
        }

        if (this.arbeidsforholdRef == null || this.arbeidsforholdRef.getReferanse() == null) {
            if (o.arbeidsforholdRef == null || o.arbeidsforholdRef.getReferanse() == null) {
                return this.opptjeningAktivitetType.compareTo(o.opptjeningAktivitetType);
            }
            return -1;
        }

        if (o.arbeidsforholdRef == null || o.arbeidsforholdRef.getReferanse() == null) {
            return 1;
        }

        return this.arbeidsforholdRef.getReferanse().compareTo(o.arbeidsforholdRef.getReferanse());
    }

    public static class Builder {
        private AktivitetEntitet mal;

        private Builder() {
            mal = new AktivitetEntitet();
        }

        public Builder(AktivitetEntitet kopi) {
            mal = new AktivitetEntitet(kopi);
        }

        public Builder medPeriode(IntervallEntitet periode) {
            mal.periode = periode;
            return this;
        }

        public Builder medArbeidsgiver(Arbeidsgiver arbeidsgiver) {
            mal.arbeidsgiver = arbeidsgiver;
            return this;
        }

        public Builder medArbeidsforholdRef(InternArbeidsforholdRef arbeidsforholdRef) {
            mal.arbeidsforholdRef = arbeidsforholdRef;
            return this;
        }

        public Builder medOpptjeningAktivitetType(OpptjeningAktivitetType opptjeningAktivitetType) {
            mal.opptjeningAktivitetType = opptjeningAktivitetType;
            return this;
        }

        public Builder medOverstyrHandlingType(BeregningAktivitetHandlingType overstyrHandlingType) {
            mal.overstyrHandlingType = overstyrHandlingType;
            return this;
        }

        public AktivitetEntitet build() {
            verifyStateForBuild();
            return mal;
        }

        private void verifyStateForBuild() {
            Objects.requireNonNull(mal.opptjeningAktivitetType, "opptjeningAktivitetType");
            Objects.requireNonNull(mal.periode, "periode");
        }
    }
}
