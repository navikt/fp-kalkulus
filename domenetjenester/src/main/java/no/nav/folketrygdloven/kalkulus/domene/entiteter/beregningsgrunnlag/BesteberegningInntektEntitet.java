package no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag;

import java.util.Objects;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Beløp;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.InternArbeidsforholdRef;
import no.nav.folketrygdloven.kalkulus.felles.jpa.BaseEntitet;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;


@Entity(name = "BesteberegningInntektEntitet")
@Table(name = "BESTEBEREGNING_INNTEKT")
public class BesteberegningInntektEntitet extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GLOBAL_PK_SEQ_GENERATOR")
    private Long id;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    @Column(name = "opptjening_aktivitet_type", nullable = false)
    private OpptjeningAktivitetType opptjeningAktivitetType;

    @Embedded
    private Arbeidsgiver arbeidsgiver;

    @Embedded
    private InternArbeidsforholdRef arbeidsforholdRef;

    @Embedded
    @AttributeOverride(name = "verdi", column = @Column(name = "inntekt"))
    private Beløp inntekt;

    @ManyToOne
    @JoinColumn(name = "besteberegning_maaned_id", updatable = false, unique = true)
    private BesteberegningMånedsgrunnlagEntitet besteberegningMåned;


    public BesteberegningInntektEntitet(BesteberegningInntektEntitet besteberegningInntektEntitet) {
        this.arbeidsforholdRef = besteberegningInntektEntitet.arbeidsforholdRef;
        this.arbeidsgiver = besteberegningInntektEntitet.arbeidsgiver;
        this.opptjeningAktivitetType = besteberegningInntektEntitet.opptjeningAktivitetType;
        this.inntekt = besteberegningInntektEntitet.inntekt;
    }

    public BesteberegningInntektEntitet() {
    }

    public Long getId() {
        return id;
    }

    public long getVersjon() {
        return versjon;
    }

    public OpptjeningAktivitetType getOpptjeningAktivitetType() {
        return opptjeningAktivitetType;
    }

    public Arbeidsgiver getArbeidsgiver() {
        return arbeidsgiver;
    }

    public InternArbeidsforholdRef getArbeidsforholdRef() {
        return arbeidsforholdRef == null ? InternArbeidsforholdRef.nullRef() : arbeidsforholdRef;
    }

    public Beløp getInntekt() {
        return inntekt;
    }

    void setBesteberegningMåned(BesteberegningMånedsgrunnlagEntitet besteberegningMåned) {
        this.besteberegningMåned = besteberegningMåned;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final BesteberegningInntektEntitet kladd;

        public Builder() {
            kladd = new BesteberegningInntektEntitet();
        }

        public Builder(BesteberegningInntektEntitet besteberegningInntektEntitet, boolean erOppdatering) {
            if (Objects.nonNull(besteberegningInntektEntitet.getId())) {
                throw new IllegalArgumentException("Kan ikke bygge på et lagret grunnlag");
            }
            if (erOppdatering) {
                kladd = besteberegningInntektEntitet;
            } else {
                kladd = new BesteberegningInntektEntitet(besteberegningInntektEntitet);
            }
        }

        public Builder medOpptjeningAktivitetType(OpptjeningAktivitetType opptjeningAktivitetType) {
            kladd.opptjeningAktivitetType = opptjeningAktivitetType;
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

        public Builder medInntekt(Beløp inntekt) {
            kladd.inntekt = inntekt;
            return this;
        }

        public BesteberegningInntektEntitet build() {
            Objects.requireNonNull(kladd.opptjeningAktivitetType);
            Objects.requireNonNull(kladd.inntekt);
            return kladd;
        }
    }

}
