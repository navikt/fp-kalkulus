package no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.fasterxml.jackson.annotation.JsonBackReference;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Beløp;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.InternArbeidsforholdRef;
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
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "refusjonskrav_pr_aar")))
    private Beløp refusjonskravPrÅr;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "saksbehandlet_refusjon_pr_aar")))
    private Beløp saksbehandletRefusjonPrÅr;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "fordelt_refusjon_pr_aar")))
    private Beløp fordeltRefusjonPrÅr;

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

    @Column(name = "hjemmel_for_refusjonskravfrist")
    private Hjemmel hjemmelForRefusjonskravfrist;

    public BGAndelArbeidsforhold(BGAndelArbeidsforhold bgAndelArbeidsforhold) {
        this.arbeidsforholdRef = bgAndelArbeidsforhold.arbeidsforholdRef;
        this.arbeidsgiver = bgAndelArbeidsforhold.arbeidsgiver;
        this.arbeidsperiodeFom = bgAndelArbeidsforhold.arbeidsperiodeFom;
        this.arbeidsperiodeTom = bgAndelArbeidsforhold.arbeidsperiodeTom;
        this.fordeltRefusjonPrÅr = bgAndelArbeidsforhold.fordeltRefusjonPrÅr;
        this.naturalytelseBortfaltPrÅr = bgAndelArbeidsforhold.naturalytelseBortfaltPrÅr;
        this.refusjonskravPrÅr = bgAndelArbeidsforhold.refusjonskravPrÅr;
        this.saksbehandletRefusjonPrÅr = bgAndelArbeidsforhold.saksbehandletRefusjonPrÅr;
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

    public Beløp getRefusjonskravPrÅr() {
        return refusjonskravPrÅr;
    }

    public Beløp getSaksbehandletRefusjonPrÅr() {
        return saksbehandletRefusjonPrÅr;
    }

    public Beløp getFordeltRefusjonPrÅr() {
        return fordeltRefusjonPrÅr;
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
        return hjemmelForRefusjonskravfrist;
    }

    /**
     * Refusjonskrav settes på forskjellige steder i beregning dersom avklaringsbehov oppstår.
     * Først settes refusjonskravPrÅr, deretter saksbehandletRefusjonPrÅr og til slutt fordeltRefusjonPrÅr.
     * Det er det sist avklarte beløpet som til en hver tid skal være gjeldende.
     * @return returnerer det refusjonskravet som skal være gjeldende
     */
    public Beløp getGjeldendeRefusjonPrÅr() {
        if (fordeltRefusjonPrÅr != null) {
            return fordeltRefusjonPrÅr;
        } else if (saksbehandletRefusjonPrÅr != null) {
            return saksbehandletRefusjonPrÅr;
        }
        return refusjonskravPrÅr;
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
                + "refusjonskravPrÅr=" + refusjonskravPrÅr + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "saksbehandletRefusjonPrÅr=" + saksbehandletRefusjonPrÅr + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "fordeltRefusjonPrÅr=" + fordeltRefusjonPrÅr + ", " //$NON-NLS-1$ //$NON-NLS-2$
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
            return medArbeidsforholdRef(arbeidsforholdRef==null?null:InternArbeidsforholdRef.ref(arbeidsforholdRef));
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

        public Builder medRefusjonskravPrÅr(Beløp refusjonskravPrÅr) {
            bgAndelArbeidsforhold.refusjonskravPrÅr = refusjonskravPrÅr;
            return this;
        }

        public Builder medSaksbehandletRefusjonPrÅr(Beløp saksbehandletRefusjonPrÅr) {
            bgAndelArbeidsforhold.saksbehandletRefusjonPrÅr = saksbehandletRefusjonPrÅr;
            return this;
        }

        public Builder medFordeltRefusjonPrÅr(Beløp fordeltRefusjonPrÅr) {
            bgAndelArbeidsforhold.fordeltRefusjonPrÅr = fordeltRefusjonPrÅr;
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

        public Builder medHjemmel(Hjemmel hjemmel) {
            bgAndelArbeidsforhold.hjemmelForRefusjonskravfrist = hjemmel;
            return this;
        }

        BGAndelArbeidsforhold build(BeregningsgrunnlagPrStatusOgAndel andel) {
            Objects.requireNonNull(bgAndelArbeidsforhold.arbeidsgiver, "arbeidsgiver");
            andel.setBgAndelArbeidsforhold(bgAndelArbeidsforhold);
            return bgAndelArbeidsforhold;
        }
    }
}
