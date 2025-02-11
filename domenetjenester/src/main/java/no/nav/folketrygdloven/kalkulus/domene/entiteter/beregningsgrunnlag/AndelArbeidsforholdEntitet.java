package no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.fasterxml.jackson.annotation.JsonBackReference;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Beløp;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.InternArbeidsforholdRef;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Refusjon;
import no.nav.folketrygdloven.kalkulus.felles.jpa.BaseEntitet;
import no.nav.folketrygdloven.kalkulus.kodeverk.Hjemmel;

@Entity(name = "AndelArbeidsforholdEntitet")
@Table(name = "ANDEL_ARBEIDSFORHOLD")
@DynamicInsert
@DynamicUpdate
public class AndelArbeidsforholdEntitet extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GLOBAL_PK_SEQ_GENERATOR")
    private Long id;

    @Version
    @Column(name = "versjon", nullable = false)
    private int versjon;

    @JsonBackReference
    @OneToOne(optional = false)
    @JoinColumn(name = "beregningsgrunnlag_andel_id", nullable = false, updatable = false)
    private BeregningsgrunnlagAndelEntitet beregningsgrunnlagAndel;

    @Embedded
    private Arbeidsgiver arbeidsgiver;

    @Embedded
    private InternArbeidsforholdRef arbeidsforholdRef;

    @Embedded
    private Refusjon refusjon;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "naturalytelse_bortfalt_pr_aar")))
    private Beløp naturalytelseBortfaltPrÅr;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "naturalytelse_tilkommet_pr_aar")))
    private Beløp naturalytelseTilkommetPrÅr;

    @Column(name = "arbeidsperiode_fom")
    private LocalDate arbeidsperiodeFom;

    @Column(name = "arbeidsperiode_tom")
    private LocalDate arbeidsperiodeTom;

    public AndelArbeidsforholdEntitet(AndelArbeidsforholdEntitet andelArbeidsforhold) {
        this.arbeidsforholdRef = andelArbeidsforhold.arbeidsforholdRef;
        this.arbeidsgiver = andelArbeidsforhold.arbeidsgiver;
        this.arbeidsperiodeFom = andelArbeidsforhold.arbeidsperiodeFom;
        this.arbeidsperiodeTom = andelArbeidsforhold.arbeidsperiodeTom;
        this.refusjon = andelArbeidsforhold.refusjon == null ? null : new Refusjon(andelArbeidsforhold.refusjon);
        this.naturalytelseBortfaltPrÅr = andelArbeidsforhold.naturalytelseBortfaltPrÅr;
        this.naturalytelseTilkommetPrÅr = andelArbeidsforhold.naturalytelseTilkommetPrÅr;
    }

    public AndelArbeidsforholdEntitet() {
    }

    public Long getId() {
        return id;
    }

    public InternArbeidsforholdRef getArbeidsforholdRef() {
        return arbeidsforholdRef != null ? arbeidsforholdRef : InternArbeidsforholdRef.nullRef();
    }

    public Refusjon getRefusjon() {
        return refusjon;
    }

    public Beløp getRefusjonskravPrÅr() {
        return refusjon != null ? refusjon.getRefusjonskravPrÅr() : null;
    }

    public Beløp getSaksbehandletRefusjonPrÅr() {
        return refusjon != null ? refusjon.getSaksbehandletRefusjonPrÅr() : null;
    }

    public Beløp getFordeltRefusjonPrÅr() {
        return refusjon != null ? refusjon.getFordeltRefusjonPrÅr() : null;
    }

    public Optional<Beløp> getNaturalytelseBortfaltPrÅr() {
        return Optional.ofNullable(naturalytelseBortfaltPrÅr);
    }

    public Optional<Beløp> getNaturalytelseTilkommetPrÅr() {
        return Optional.ofNullable(naturalytelseTilkommetPrÅr);
    }

    public LocalDate getArbeidsperiodeFom() {
        return arbeidsperiodeFom;
    }

    public Optional<LocalDate> getArbeidsperiodeTom() {
        return Optional.ofNullable(arbeidsperiodeTom);
    }

    public String getArbeidsforholdOrgnr() {
        return getArbeidsgiver().getOrgnr();
    }

    public Arbeidsgiver getArbeidsgiver() {
        return arbeidsgiver;
    }

    public Hjemmel getHjemmelForRefusjonskravfrist() {
        return refusjon != null ? refusjon.getHjemmelForRefusjonskravfrist() : null;
    }

    public Beløp getGjeldendeRefusjonPrÅr() {
        return refusjon == null ? null : refusjon.getGjeldendeRefusjonPrÅr();
    }

    void setBeregningsgrunnlagAndel(BeregningsgrunnlagAndelEntitet beregningsgrunnlagPrStatusOgAndel) {
        this.beregningsgrunnlagAndel = beregningsgrunnlagPrStatusOgAndel;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof AndelArbeidsforholdEntitet)) {
            return false;
        }
        AndelArbeidsforholdEntitet other = (AndelArbeidsforholdEntitet) obj;
        return Objects.equals(this.getArbeidsgiver(), other.getArbeidsgiver()) && Objects.equals(this.arbeidsforholdRef, other.arbeidsforholdRef);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getArbeidsgiver(), arbeidsforholdRef);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + "id=" + id + ", " //$NON-NLS-2$
            + "orgnr=" + getArbeidsforholdOrgnr() + ", " //$NON-NLS-2$
            + "arbeidsgiver=" + arbeidsgiver + ", " //$NON-NLS-2$
            + "arbeidsforholdRef=" + arbeidsforholdRef + ", " //$NON-NLS-2$
            + "naturalytelseBortfaltPrÅr=" + naturalytelseBortfaltPrÅr + ", " //$NON-NLS-2$
            + "naturalytelseTilkommetPrÅr=" + naturalytelseTilkommetPrÅr + ", " //$NON-NLS-2$
            + "refusjon=" + refusjon + ", " //$NON-NLS-2$
            + "arbeidsperiodeFom=" + arbeidsperiodeFom + "arbeidsperiodeTom=" + arbeidsperiodeTom + ">";
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(AndelArbeidsforholdEntitet andelArbeidsforhold) {
        return andelArbeidsforhold == null ? new Builder() : new Builder(andelArbeidsforhold);
    }

    public static class Builder {
        private AndelArbeidsforholdEntitet andelArbeidsforhold;

        private Builder() {
            andelArbeidsforhold = new AndelArbeidsforholdEntitet();
        }

        private Builder(AndelArbeidsforholdEntitet eksisterendeAndelArbeidsforhold) {
            andelArbeidsforhold = eksisterendeAndelArbeidsforhold;
        }

        public Builder medArbeidsgiver(Arbeidsgiver arbeidsgiver) {
            andelArbeidsforhold.arbeidsgiver = arbeidsgiver;
            return this;
        }

        public Builder medArbeidsforholdRef(String arbeidsforholdRef) {
            return medArbeidsforholdRef(arbeidsforholdRef == null ? null : InternArbeidsforholdRef.ref(arbeidsforholdRef));
        }

        public Builder medArbeidsforholdRef(InternArbeidsforholdRef arbeidsforholdRef) {
            andelArbeidsforhold.arbeidsforholdRef = arbeidsforholdRef;
            return this;
        }

        public Builder medNaturalytelseBortfaltPrÅr(Beløp naturalytelseBortfaltPrÅr) {
            andelArbeidsforhold.naturalytelseBortfaltPrÅr = naturalytelseBortfaltPrÅr;
            return this;
        }

        public Builder medNaturalytelseTilkommetPrÅr(Beløp naturalytelseTilkommetPrÅr) {
            andelArbeidsforhold.naturalytelseTilkommetPrÅr = naturalytelseTilkommetPrÅr;
            return this;
        }

        public Builder medRefusjon(Refusjon refusjon) {
            andelArbeidsforhold.refusjon = refusjon;
            return this;
        }

        public Builder medArbeidsperiodeFom(LocalDate arbeidsperiodeFom) {
            andelArbeidsforhold.arbeidsperiodeFom = arbeidsperiodeFom;
            return this;
        }

        public Builder medArbeidsperiodeTom(LocalDate arbeidsperiodeTom) {
            andelArbeidsforhold.arbeidsperiodeTom = arbeidsperiodeTom;
            return this;
        }

        public AndelArbeidsforholdEntitet build() {
            Objects.requireNonNull(andelArbeidsforhold.arbeidsgiver, "arbeidsgiver");
            return andelArbeidsforhold;
        }
    }
}
