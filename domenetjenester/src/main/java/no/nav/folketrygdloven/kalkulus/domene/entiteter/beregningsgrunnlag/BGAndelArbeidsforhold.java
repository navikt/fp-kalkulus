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

@Entity(name = "BGAndelArbeidsforhold")
@Table(name = "BG_ANDEL_ARBEIDSFORHOLD")
@DynamicInsert
@DynamicUpdate
public class BGAndelArbeidsforhold extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_BG_ANDEL_ARBEIDSFORHOLD")
    private Long id;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    @JsonBackReference
    @OneToOne(optional = false)
    @JoinColumn(name = "bg_andel_id", nullable = false, updatable = false)
    private BeregningsgrunnlagPrStatusOgAndel beregningsgrunnlagPrStatusOgAndel;

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

    public BGAndelArbeidsforhold(BGAndelArbeidsforhold bgAndelArbeidsforhold) {
        this.arbeidsforholdRef = bgAndelArbeidsforhold.arbeidsforholdRef;
        this.arbeidsgiver = bgAndelArbeidsforhold.arbeidsgiver;
        this.arbeidsperiodeFom = bgAndelArbeidsforhold.arbeidsperiodeFom;
        this.arbeidsperiodeTom = bgAndelArbeidsforhold.arbeidsperiodeTom;
        this.refusjon = bgAndelArbeidsforhold.refusjon == null ? null : new Refusjon(bgAndelArbeidsforhold.refusjon);
        this.naturalytelseBortfaltPrÅr = bgAndelArbeidsforhold.naturalytelseBortfaltPrÅr;
        this.naturalytelseTilkommetPrÅr = bgAndelArbeidsforhold.naturalytelseTilkommetPrÅr;
    }

    public BGAndelArbeidsforhold() {
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

    void setBeregningsgrunnlagPrStatusOgAndel(BeregningsgrunnlagPrStatusOgAndel beregningsgrunnlagPrStatusOgAndel) {
        this.beregningsgrunnlagPrStatusOgAndel = beregningsgrunnlagPrStatusOgAndel;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof BGAndelArbeidsforhold)) {
            return false;
        }
        BGAndelArbeidsforhold other = (BGAndelArbeidsforhold) obj;
        return Objects.equals(this.getArbeidsgiver(), other.getArbeidsgiver())
                && Objects.equals(this.arbeidsforholdRef, other.arbeidsforholdRef);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getArbeidsgiver(), arbeidsforholdRef);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + //$NON-NLS-1$
                "id=" + id + ", " //$NON-NLS-2$
                + "orgnr=" + getArbeidsforholdOrgnr() + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "arbeidsgiver=" + arbeidsgiver + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "arbeidsforholdRef=" + arbeidsforholdRef + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "naturalytelseBortfaltPrÅr=" + naturalytelseBortfaltPrÅr + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "naturalytelseTilkommetPrÅr=" + naturalytelseTilkommetPrÅr + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "refusjon=" + refusjon + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "arbeidsperiodeFom=" + arbeidsperiodeFom //$NON-NLS-1$
                + "arbeidsperiodeTom=" + arbeidsperiodeTom //$NON-NLS-1$
                + ">"; //$NON-NLS-1$
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(BGAndelArbeidsforhold bgAndelArbeidsforhold) {
        return bgAndelArbeidsforhold == null ? new Builder() : new Builder(bgAndelArbeidsforhold);
    }

    public static class Builder {
        private BGAndelArbeidsforhold bgAndelArbeidsforhold;

        private Builder() {
            bgAndelArbeidsforhold = new BGAndelArbeidsforhold();
        }

        private Builder(BGAndelArbeidsforhold eksisterendeBGAndelArbeidsforhold) {
            bgAndelArbeidsforhold = eksisterendeBGAndelArbeidsforhold;
        }

        public Builder medArbeidsgiver(Arbeidsgiver arbeidsgiver) {
            bgAndelArbeidsforhold.arbeidsgiver = arbeidsgiver;
            return this;
        }

        public Builder medArbeidsforholdRef(String arbeidsforholdRef) {
            return medArbeidsforholdRef(arbeidsforholdRef == null ? null : InternArbeidsforholdRef.ref(arbeidsforholdRef));
        }

        public Builder medArbeidsforholdRef(InternArbeidsforholdRef arbeidsforholdRef) {
            bgAndelArbeidsforhold.arbeidsforholdRef = arbeidsforholdRef;
            return this;
        }

        public Builder medNaturalytelseBortfaltPrÅr(Beløp naturalytelseBortfaltPrÅr) {
            bgAndelArbeidsforhold.naturalytelseBortfaltPrÅr = naturalytelseBortfaltPrÅr;
            return this;
        }

        public Builder medNaturalytelseTilkommetPrÅr(Beløp naturalytelseTilkommetPrÅr) {
            bgAndelArbeidsforhold.naturalytelseTilkommetPrÅr = naturalytelseTilkommetPrÅr;
            return this;
        }

        public Builder medRefusjon(Refusjon refusjon) {
            bgAndelArbeidsforhold.refusjon = refusjon;
            return this;
        }

        public Builder medArbeidsperiodeFom(LocalDate arbeidsperiodeFom) {
            bgAndelArbeidsforhold.arbeidsperiodeFom = arbeidsperiodeFom;
            return this;
        }

        public Builder medArbeidsperiodeTom(LocalDate arbeidsperiodeTom) {
            bgAndelArbeidsforhold.arbeidsperiodeTom = arbeidsperiodeTom;
            return this;
        }

        BGAndelArbeidsforhold build(BeregningsgrunnlagPrStatusOgAndel andel) {
            Objects.requireNonNull(bgAndelArbeidsforhold.arbeidsgiver, "arbeidsgiver");
            andel.setBgAndelArbeidsforhold(bgAndelArbeidsforhold);
            return bgAndelArbeidsforhold;
        }
    }
}
