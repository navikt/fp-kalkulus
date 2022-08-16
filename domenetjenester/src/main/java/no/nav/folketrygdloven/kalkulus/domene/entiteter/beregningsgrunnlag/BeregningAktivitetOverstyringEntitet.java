package no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.InternArbeidsforholdRef;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.mapping.BeregingAktivitetHandlingTypeKodeverdiConverter;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.mapping.OpptjeningAktivitetTypeKodeverdiConverter;
import no.nav.folketrygdloven.kalkulus.felles.jpa.BaseEntitet;
import no.nav.folketrygdloven.kalkulus.felles.jpa.IntervallEntitet;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningAktivitetHandlingType;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;

@Entity(name = "BeregningAktivitetOverstyring")
@Table(name = "BG_AKTIVITET_OVERSTYRING")
public class BeregningAktivitetOverstyringEntitet extends BaseEntitet {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_BG_AKTIVITET_OVERSTYRING")
    private Long id;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    @Embedded
    @AttributeOverride(name = "fomDato", column = @Column(name = "fom"))
    @AttributeOverride(name = "tomDato", column = @Column(name = "tom"))
    private IntervallEntitet periode;

    @Embedded
    private Arbeidsgiver arbeidsgiver;

    @Embedded
    private InternArbeidsforholdRef arbeidsforholdRef;

    @Convert(converter= BeregingAktivitetHandlingTypeKodeverdiConverter.class)
    @Column(name="handling_type", nullable = false)
    private BeregningAktivitetHandlingType handlingType;

    @Convert(converter = OpptjeningAktivitetTypeKodeverdiConverter.class)
    @Column(name="opptjening_aktivitet_type", nullable = false)
    private OpptjeningAktivitetType opptjeningAktivitetType;

    @JsonBackReference
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "ba_overstyringer_id", nullable = false, updatable = false)
    private BeregningAktivitetOverstyringerEntitet overstyringerEntitet;

    public BeregningAktivitetOverstyringEntitet(BeregningAktivitetOverstyringEntitet beregningAktivitetOverstyringEntitet) {
        this.arbeidsforholdRef = beregningAktivitetOverstyringEntitet.getArbeidsforholdRef();
        this.arbeidsgiver = beregningAktivitetOverstyringEntitet.getArbeidsgiver().orElse(null);
        this.handlingType = beregningAktivitetOverstyringEntitet.getHandling();
        this.opptjeningAktivitetType = beregningAktivitetOverstyringEntitet.getOpptjeningAktivitetType();
        this.periode = beregningAktivitetOverstyringEntitet.getPeriode();
    }


    public BeregningAktivitetOverstyringEntitet() {
    }

    public BeregningAktivitetHandlingType getHandling() {
        return handlingType;
    }

    public Optional<Arbeidsgiver> getArbeidsgiver() {
        return Optional.ofNullable(arbeidsgiver);
    }

    public InternArbeidsforholdRef getArbeidsforholdRef() {
        return arbeidsforholdRef;
    }

    public OpptjeningAktivitetType getOpptjeningAktivitetType() {
        return opptjeningAktivitetType;
    }

    public IntervallEntitet getPeriode() {
        return periode;
    }

    public BeregningAktivitetNøkkel getNøkkel() {
        return BeregningAktivitetNøkkel.builder()
                .medArbeidsgiverIdentifikator(getArbeidsgiver().map(Arbeidsgiver::getIdentifikator).orElse(null))
                .medArbeidsforholdRef(arbeidsforholdRef != null ? arbeidsforholdRef.getReferanse() : null)
                .medOpptjeningAktivitetType(opptjeningAktivitetType)
                .medFom(periode.getFomDato())
                .medTom(periode.getTomDato())
                .build();
    }

    void setBeregningAktivitetOverstyringer(BeregningAktivitetOverstyringerEntitet overstyringerEntitet) {
        this.overstyringerEntitet = overstyringerEntitet;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final BeregningAktivitetOverstyringEntitet kladd;

        private Builder() {
            kladd = new BeregningAktivitetOverstyringEntitet();
        }

        public Builder medPeriode(IntervallEntitet periode) {
            kladd.periode = periode;
            return this;
        }

        public Builder medArbeidsgiver(Arbeidsgiver arbeidsgiver) {
            kladd.arbeidsgiver = arbeidsgiver;
            return this;
        }

        public Builder medArbeidsforholdRef(InternArbeidsforholdRef arbeidsforholdRef) {
            kladd.arbeidsforholdRef = arbeidsforholdRef;
            return this;
        }

        public Builder medOpptjeningAktivitetType(OpptjeningAktivitetType opptjeningAktivitetType) {
            kladd.opptjeningAktivitetType = opptjeningAktivitetType;
            return this;
        }

        public Builder medHandling(BeregningAktivitetHandlingType beregningAktivitetHandlingType) {
            kladd.handlingType = beregningAktivitetHandlingType;
            return this;
        }

        public BeregningAktivitetOverstyringEntitet build() {
            return kladd;
        }
    }
}
