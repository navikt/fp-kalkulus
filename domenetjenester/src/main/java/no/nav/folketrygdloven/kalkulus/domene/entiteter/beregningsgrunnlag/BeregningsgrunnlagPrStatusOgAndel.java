package no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.fasterxml.jackson.annotation.JsonBackReference;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Beløp;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.InternArbeidsforholdRef;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.mapping.AktivitetStatusKodeverdiConverter;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.mapping.AndelKildeKodeverdiConverter;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.mapping.InntektskategoriKodeverdiConverter;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.mapping.OpptjeningAktivitetTypeKodeverdiConverter;
import no.nav.folketrygdloven.kalkulus.felles.diff.ChangeTracked;
import no.nav.folketrygdloven.kalkulus.felles.jpa.BaseEntitet;
import no.nav.folketrygdloven.kalkulus.felles.jpa.IntervallEntitet;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.AndelKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;


@Entity(name = "BeregningsgrunnlagPrStatusOgAndel")
@Table(name = "BG_PR_STATUS_OG_ANDEL")
@DynamicInsert
@DynamicUpdate
public class BeregningsgrunnlagPrStatusOgAndel extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_BG_PR_STATUS_OG_ANDEL")
    private Long id;

    @Column(name = "andelsnr", nullable = false)
    private Long andelsnr;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    @JsonBackReference
    @ManyToOne(optional = false)
    @JoinColumn(name = "bg_periode_id", nullable = false, updatable = false)
    private BeregningsgrunnlagPeriode beregningsgrunnlagPeriode;

    @Convert(converter= AktivitetStatusKodeverdiConverter.class)
    @Column(name="aktivitet_status", nullable = false)
    private AktivitetStatus aktivitetStatus;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "fomDato", column = @Column(name = "beregningsperiode_fom")),
        @AttributeOverride(name = "tomDato", column = @Column(name = "beregningsperiode_tom"))
    })
    private IntervallEntitet beregningsperiode;

    @Convert(converter = OpptjeningAktivitetTypeKodeverdiConverter.class)
    @Column(name="arbeidsforhold_type", nullable = false)
    private OpptjeningAktivitetType arbeidsforholdType;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "brutto_pr_aar")))
    private Beløp bruttoPrÅr;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "overstyrt_pr_aar")))
    private Beløp overstyrtPrÅr;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "avkortet_pr_aar")))
    private Beløp avkortetPrÅr;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "redusert_pr_aar")))
    private Beløp redusertPrÅr;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "beregnet_pr_aar")))
    private Beløp beregnetPrÅr;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "fordelt_pr_aar")))
    private Beløp fordeltPrÅr;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "maksimal_refusjon_pr_aar")))
    private Beløp maksimalRefusjonPrÅr;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "avkortet_refusjon_pr_aar")))
    private Beløp avkortetRefusjonPrÅr;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "redusert_refusjon_pr_aar")))
    private Beløp redusertRefusjonPrÅr;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "avkortet_brukers_andel_pr_aar")))
    private Beløp avkortetBrukersAndelPrÅr;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "redusert_brukers_andel_pr_aar")))
    private Beløp redusertBrukersAndelPrÅr;

    @Column(name = "dagsats_bruker")
    private Long dagsatsBruker;

    @Column(name = "dagsats_arbeidsgiver")
    private Long dagsatsArbeidsgiver;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "pgi_snitt")))
    private Beløp pgiSnitt;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "pgi1")))
    private Beløp pgi1;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "pgi2")))
    private Beløp pgi2;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "pgi3")))
    private Beløp pgi3;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "avkortet_foer_gradering_pr_aar")))
    private Beløp avkortetFørGraderingPrÅr;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "aarsbeloep_tilstoetende_ytelse")))
    private Beløp årsbeløpFraTilstøtendeYtelse;

    @Column(name = "fastsatt_av_saksbehandler", nullable = false)
    private Boolean fastsattAvSaksbehandler = false;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "besteberegning_pr_aar")))
    private Beløp besteberegningPrÅr;

    @Convert(converter= InntektskategoriKodeverdiConverter.class)
    @Column(name="inntektskategori", nullable = false)
    private Inntektskategori inntektskategori = Inntektskategori.UDEFINERT;

    @Convert(converter= AndelKildeKodeverdiConverter.class)
    @Column(name = "kilde", nullable = false)
    private AndelKilde kilde = AndelKilde.PROSESS_START;

    @OneToOne(mappedBy = "beregningsgrunnlagPrStatusOgAndel", cascade = CascadeType.PERSIST)
    private BGAndelArbeidsforhold bgAndelArbeidsforhold;

    @Column(name = "dagsats_tilstoetende_ytelse")
    private Long orginalDagsatsFraTilstøtendeYtelse;

    public BeregningsgrunnlagPrStatusOgAndel(BeregningsgrunnlagPrStatusOgAndel beregningsgrunnlagPrStatusOgAndel) {
        this.beregnetPrÅr = beregningsgrunnlagPrStatusOgAndel.getBeregnetPrÅr();
        this.aktivitetStatus = beregningsgrunnlagPrStatusOgAndel.getAktivitetStatus();
        this.andelsnr = beregningsgrunnlagPrStatusOgAndel.getAndelsnr();
        this.arbeidsforholdType = beregningsgrunnlagPrStatusOgAndel.getArbeidsforholdType();
        this.avkortetBrukersAndelPrÅr = beregningsgrunnlagPrStatusOgAndel.getAvkortetBrukersAndelPrÅr();
        this.avkortetPrÅr = beregningsgrunnlagPrStatusOgAndel.getAvkortetPrÅr();
        this.avkortetRefusjonPrÅr = beregningsgrunnlagPrStatusOgAndel.getAvkortetRefusjonPrÅr();
        this.avkortetFørGraderingPrÅr = beregningsgrunnlagPrStatusOgAndel.getAvkortetFørGraderingPrÅr();
        this.beregningsperiode = beregningsgrunnlagPrStatusOgAndel.beregningsperiode;
        this.besteberegningPrÅr = beregningsgrunnlagPrStatusOgAndel.getBesteberegningPrÅr();
        this.bruttoPrÅr = beregningsgrunnlagPrStatusOgAndel.getBruttoPrÅr();
        this.dagsatsArbeidsgiver = beregningsgrunnlagPrStatusOgAndel.getDagsatsArbeidsgiver();
        this.dagsatsBruker = beregningsgrunnlagPrStatusOgAndel.getDagsatsBruker();
        this.fastsattAvSaksbehandler = beregningsgrunnlagPrStatusOgAndel.getFastsattAvSaksbehandler();
        this.fordeltPrÅr = beregningsgrunnlagPrStatusOgAndel.getFordeltPrÅr();
        this.inntektskategori = beregningsgrunnlagPrStatusOgAndel.getInntektskategori();
        this.kilde = beregningsgrunnlagPrStatusOgAndel.getKilde();
        this.maksimalRefusjonPrÅr = beregningsgrunnlagPrStatusOgAndel.getMaksimalRefusjonPrÅr();
        this.orginalDagsatsFraTilstøtendeYtelse = beregningsgrunnlagPrStatusOgAndel.getOrginalDagsatsFraTilstøtendeYtelse();
        this.overstyrtPrÅr = beregningsgrunnlagPrStatusOgAndel.getOverstyrtPrÅr();
        this.pgi1 = beregningsgrunnlagPrStatusOgAndel.getPgi1();
        this.pgi2 = beregningsgrunnlagPrStatusOgAndel.getPgi2();
        this.pgi3 = beregningsgrunnlagPrStatusOgAndel.getPgi3();
        this.pgiSnitt = beregningsgrunnlagPrStatusOgAndel.getPgiSnitt();
        this.redusertBrukersAndelPrÅr = beregningsgrunnlagPrStatusOgAndel.getRedusertBrukersAndelPrÅr();
        this.redusertPrÅr = beregningsgrunnlagPrStatusOgAndel.getRedusertPrÅr();
        this.redusertRefusjonPrÅr = beregningsgrunnlagPrStatusOgAndel.getRedusertRefusjonPrÅr();
        this.årsbeløpFraTilstøtendeYtelse = beregningsgrunnlagPrStatusOgAndel.getÅrsbeløpFraTilstøtendeYtelse();
        beregningsgrunnlagPrStatusOgAndel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforhold::new)
                .ifPresent(this::setBgAndelArbeidsforhold);
    }

    public BeregningsgrunnlagPrStatusOgAndel() {
    }


    public Long getId() {
        return id;
    }

    public BeregningsgrunnlagPeriode getBeregningsgrunnlagPeriode() {
        return beregningsgrunnlagPeriode;
    }

    public AktivitetStatus getAktivitetStatus() {
        return aktivitetStatus;
    }

    public LocalDate getBeregningsperiodeFom() {
        return beregningsperiode != null ? beregningsperiode.getFomDato() : null;
    }

    public LocalDate getBeregningsperiodeTom() {
        return beregningsperiode != null ? beregningsperiode.getTomDato() : null;
    }

    public OpptjeningAktivitetType getArbeidsforholdType() {
        return arbeidsforholdType;
    }

    public Beløp getBruttoPrÅr() {
        return bruttoPrÅr;
    }

    public Beløp getOverstyrtPrÅr() {
        return overstyrtPrÅr;
    }

    public Beløp getAvkortetPrÅr() {
        return avkortetPrÅr;
    }

    public Beløp getRedusertPrÅr() {
        return redusertPrÅr;
    }

    public Beløp getBeregnetPrÅr() {
        return beregnetPrÅr;
    }

    public Beløp getFordeltPrÅr() {
        return fordeltPrÅr;
    }

    public Beløp getMaksimalRefusjonPrÅr() {
        return maksimalRefusjonPrÅr;
    }

    public Beløp getAvkortetRefusjonPrÅr() {
        return avkortetRefusjonPrÅr;
    }

    public Beløp getRedusertRefusjonPrÅr() {
        return redusertRefusjonPrÅr;
    }

    public Beløp getAvkortetFørGraderingPrÅr() {
        return avkortetFørGraderingPrÅr;
    }

    public Beløp getAvkortetBrukersAndelPrÅr() {
        return avkortetBrukersAndelPrÅr;
    }

    public Beløp getRedusertBrukersAndelPrÅr() {
        return redusertBrukersAndelPrÅr;
    }

    public Boolean getFastsattAvSaksbehandler() {
        return fastsattAvSaksbehandler;
    }

    public Inntektskategori getInntektskategori() {
        return inntektskategori;
    }

    public Long getDagsatsBruker() {
        return dagsatsBruker;
    }

    public Long getDagsatsArbeidsgiver() {
        return dagsatsArbeidsgiver;
    }

    public Long getDagsats() {
        if (dagsatsBruker == null) {
            return dagsatsArbeidsgiver;
        }
        if (dagsatsArbeidsgiver == null) {
            return dagsatsBruker;
        }
        return dagsatsBruker + dagsatsArbeidsgiver;
    }

    public Beløp getPgiSnitt() {
        return pgiSnitt;
    }

    public Beløp getPgi1() {
        return pgi1;
    }

    public Beløp getPgi2() {
        return pgi2;
    }

    public Beløp getPgi3() {
        return pgi3;
    }

    public Beløp getÅrsbeløpFraTilstøtendeYtelse() {
        return årsbeløpFraTilstøtendeYtelse;
    }

    public Long getAndelsnr() {
        return andelsnr;
    }

    public Beløp getBesteberegningPrÅr() {
        return besteberegningPrÅr;
    }

    public AndelKilde getKilde() {
        return kilde;
    }

    public Optional<BGAndelArbeidsforhold> getBgAndelArbeidsforhold() {
        return Optional.ofNullable(bgAndelArbeidsforhold);
    }

    public Long getOrginalDagsatsFraTilstøtendeYtelse() {
        return orginalDagsatsFraTilstøtendeYtelse;
    }

    public Optional<Arbeidsgiver> getArbeidsgiver() {
        Optional<BGAndelArbeidsforhold> beregningArbeidsforhold = getBgAndelArbeidsforhold();
        return beregningArbeidsforhold.map(BGAndelArbeidsforhold::getArbeidsgiver);
    }

    public Optional<InternArbeidsforholdRef> getArbeidsforholdRef() {
        Optional<BGAndelArbeidsforhold> beregningArbeidsforhold = getBgAndelArbeidsforhold();
        return beregningArbeidsforhold.map(BGAndelArbeidsforhold::getArbeidsforholdRef);
    }

    void setBgAndelArbeidsforhold(BGAndelArbeidsforhold bgAndelArbeidsforhold) {
        bgAndelArbeidsforhold.setBeregningsgrunnlagPrStatusOgAndel(this);
        this.bgAndelArbeidsforhold = bgAndelArbeidsforhold;
    }

    void setBeregningsgrunnlagPeriode(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode) {
        this.beregningsgrunnlagPeriode = beregningsgrunnlagPeriode;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof BeregningsgrunnlagPrStatusOgAndel)) {
            return false;
        }
        // Endring av denne har store konsekvenser for matching av andeler
        // Resultat av endringer må testes manuelt
        BeregningsgrunnlagPrStatusOgAndel other = (BeregningsgrunnlagPrStatusOgAndel) obj;
        return Objects.equals(this.getAktivitetStatus(), other.getAktivitetStatus())
                && Objects.equals(this.getInntektskategori(), other.getInntektskategori())
                && Objects.equals(this.getBgAndelArbeidsforhold().map(BGAndelArbeidsforhold::getArbeidsgiver),
                    other.getBgAndelArbeidsforhold().map(BGAndelArbeidsforhold::getArbeidsgiver))
                && Objects.equals(this.getBgAndelArbeidsforhold().map(BGAndelArbeidsforhold::getArbeidsforholdRef),
                    other.getBgAndelArbeidsforhold().map(BGAndelArbeidsforhold::getArbeidsforholdRef))
                && Objects.equals(this.getArbeidsforholdType(), other.getArbeidsforholdType());
    }


    @Override
    public int hashCode() {
        return Objects.hash(aktivitetStatus,
            inntektskategori,
            getBgAndelArbeidsforhold().map(BGAndelArbeidsforhold::getArbeidsgiver),
            getBgAndelArbeidsforhold().map(BGAndelArbeidsforhold::getArbeidsforholdRef),
            arbeidsforholdType);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + //$NON-NLS-1$
                "id=" + id + ", " //$NON-NLS-2$
                + "beregningsgrunnlagPeriode=" + beregningsgrunnlagPeriode + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "aktivitetStatus=" + aktivitetStatus + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "beregningsperiode=" + beregningsperiode + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "arbeidsforholdType=" + arbeidsforholdType + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "maksimalRefusjonPrÅr=" + maksimalRefusjonPrÅr + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "avkortetRefusjonPrÅr=" + avkortetRefusjonPrÅr + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "redusertRefusjonPrÅr=" + redusertRefusjonPrÅr + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "avkortetBrukersAndelPrÅr=" + avkortetBrukersAndelPrÅr + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "redusertBrukersAndelPrÅr=" + redusertBrukersAndelPrÅr + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "beregnetPrÅr=" + beregnetPrÅr + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "fordeltPrÅr=" + fordeltPrÅr + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "overstyrtPrÅr=" + overstyrtPrÅr + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "bruttoPrÅr=" + bruttoPrÅr + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "avkortetPrÅr=" + avkortetPrÅr + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "redusertPrÅr=" + redusertPrÅr + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "dagsatsBruker=" + dagsatsBruker + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "dagsatsArbeidsgiver=" + dagsatsArbeidsgiver + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "pgiSnitt=" + pgiSnitt + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "pgi1=" + pgi1 + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "pgi2=" + pgi2 + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "pgi3=" + pgi3 + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "årsbeløpFraTilstøtendeYtelse=" + årsbeløpFraTilstøtendeYtelse //$NON-NLS-1$
                + "besteberegningPrÅr=" + besteberegningPrÅr //$NON-NLS-1$
                + ">"; //$NON-NLS-1$
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(BeregningsgrunnlagPrStatusOgAndel eksisterendeBGPrStatusOgAndel) {
        if (eksisterendeBGPrStatusOgAndel.getId() != null) {
            throw new IllegalArgumentException("Utviklerfeil: Kan ikke bygge på en allerede lagret andel.");
        }
        return new Builder(eksisterendeBGPrStatusOgAndel);
    }

    public static class Builder {
        /** Når det er built kan ikke denne builderen brukes til annet enn å returnere samme objekt. */
        private boolean built;

        private BeregningsgrunnlagPrStatusOgAndel kladd;

        private Builder() {
            kladd = new BeregningsgrunnlagPrStatusOgAndel();
            kladd.arbeidsforholdType = OpptjeningAktivitetType.UDEFINERT;
        }

        private Builder(BeregningsgrunnlagPrStatusOgAndel eksisterendeBGPrStatusOgAndelMal) {
            kladd = eksisterendeBGPrStatusOgAndelMal;
        }

        public Builder medAktivitetStatus(AktivitetStatus aktivitetStatus) {
            verifiserKanModifisere();
            kladd.aktivitetStatus = Objects.requireNonNull(aktivitetStatus, "aktivitetStatus");
            if (OpptjeningAktivitetType.UDEFINERT.equals(kladd.arbeidsforholdType)) {
                if (AktivitetStatus.ARBEIDSTAKER.equals(aktivitetStatus)) {
                    kladd.arbeidsforholdType = OpptjeningAktivitetType.ARBEID;
                } else if (AktivitetStatus.FRILANSER.equals(aktivitetStatus)) {
                    kladd.arbeidsforholdType = OpptjeningAktivitetType.FRILANS;
                }
            }
            return this;
        }

        public Builder medBeregningsperiode(LocalDate beregningsperiodeFom, LocalDate beregningsperiodeTom) {
            verifiserKanModifisere();
            kladd.beregningsperiode = IntervallEntitet.fraOgMedTilOgMed(beregningsperiodeFom, beregningsperiodeTom);
            return this;
        }

        public Builder medArbforholdType(OpptjeningAktivitetType arbforholdType) {
            verifiserKanModifisere();
            kladd.arbeidsforholdType = arbforholdType;
            return this;
        }

        public Builder medOverstyrtPrÅr(Beløp overstyrtPrÅr) {
            verifiserKanModifisere();
            kladd.overstyrtPrÅr = overstyrtPrÅr;
            if (overstyrtPrÅr != null && kladd.fordeltPrÅr == null && kladd.besteberegningPrÅr == null) {
                kladd.bruttoPrÅr = overstyrtPrÅr;
                if (kladd.getBeregningsgrunnlagPeriode() != null) {
                    kladd.beregningsgrunnlagPeriode.updateBruttoPrÅr();
                }
            }
            return this;
        }

        public Builder medFordeltPrÅr(Beløp fordeltPrÅr) {
            verifiserKanModifisere();
            kladd.fordeltPrÅr = fordeltPrÅr;
            if (fordeltPrÅr != null) {
                kladd.bruttoPrÅr = fordeltPrÅr;
                if (kladd.getBeregningsgrunnlagPeriode() != null) {
                    kladd.beregningsgrunnlagPeriode.updateBruttoPrÅr();
                }
            }
            return this;
        }


        public Builder medAvkortetPrÅr(Beløp avkortetPrÅr) {
            verifiserKanModifisere();
            kladd.avkortetPrÅr = avkortetPrÅr;
            return this;
        }

        public Builder medRedusertPrÅr(Beløp redusertPrÅr) {
            verifiserKanModifisere();
            kladd.redusertPrÅr = redusertPrÅr;
            return this;
        }

        public Builder medMaksimalRefusjonPrÅr(Beløp maksimalRefusjonPrÅr) {
            verifiserKanModifisere();
            kladd.maksimalRefusjonPrÅr = maksimalRefusjonPrÅr;
            return this;
        }

        public Builder medAvkortetRefusjonPrÅr(Beløp avkortetRefusjonPrÅr) {
            verifiserKanModifisere();
            kladd.avkortetRefusjonPrÅr = avkortetRefusjonPrÅr;
            return this;
        }

        public Builder medRedusertRefusjonPrÅr(Beløp redusertRefusjonPrÅr) {
            verifiserKanModifisere();
            kladd.redusertRefusjonPrÅr = redusertRefusjonPrÅr;
            kladd.dagsatsArbeidsgiver = redusertRefusjonPrÅr == null ?
                null : redusertRefusjonPrÅr.getVerdi().divide(BigDecimal.valueOf(260), 0, RoundingMode.HALF_UP).longValue();
            return this;
        }

        public Builder medAvkortetBrukersAndelPrÅr(Beløp avkortetBrukersAndelPrÅr) {
            verifiserKanModifisere();
            kladd.avkortetBrukersAndelPrÅr = avkortetBrukersAndelPrÅr;
            return this;
        }

        public Builder medRedusertBrukersAndelPrÅr(Beløp redusertBrukersAndelPrÅr) {
            verifiserKanModifisere();
            kladd.redusertBrukersAndelPrÅr = redusertBrukersAndelPrÅr;
            kladd.dagsatsBruker = redusertBrukersAndelPrÅr == null ?
                null : redusertBrukersAndelPrÅr.getVerdi().divide(BigDecimal.valueOf(260), 0, RoundingMode.HALF_UP).longValue();
            return this;
        }

        public Builder medBeregnetPrÅr(Beløp beregnetPrÅr) {
            verifiserKanModifisere();
            kladd.beregnetPrÅr = beregnetPrÅr;
            if (kladd.fordeltPrÅr == null && kladd.overstyrtPrÅr == null && kladd.besteberegningPrÅr == null) {
                kladd.bruttoPrÅr = beregnetPrÅr;
                if (kladd.getBeregningsgrunnlagPeriode() != null) {
                    kladd.beregningsgrunnlagPeriode.updateBruttoPrÅr();
                }
            }
            return this;
        }

        public Builder medPgi(Beløp pgiSnitt, List<Beløp> pgiListe) {
            verifiserKanModifisere();
            kladd.pgiSnitt = pgiSnitt;
            kladd.pgi1 = pgiListe.isEmpty() ? null : pgiListe.get(0);
            kladd.pgi2 = pgiListe.isEmpty() ? null : pgiListe.get(1);
            kladd.pgi3 = pgiListe.isEmpty() ? null : pgiListe.get(2);
            return this;
        }

        public Builder medÅrsbeløpFraTilstøtendeYtelse(Beløp årsbeløpFraTilstøtendeYtelse) {
            verifiserKanModifisere();
            kladd.årsbeløpFraTilstøtendeYtelse = årsbeløpFraTilstøtendeYtelse;
            return this;
        }

        public Builder medInntektskategori(Inntektskategori inntektskategori) {
            verifiserKanModifisere();
            kladd.inntektskategori = inntektskategori;
            return this;
        }

        public Builder medFastsattAvSaksbehandler(Boolean fastsattAvSaksbehandler) {
            verifiserKanModifisere();
            kladd.fastsattAvSaksbehandler = fastsattAvSaksbehandler;
            return this;
        }

        public Builder medKilde(AndelKilde kilde) {
            verifiserKanModifisere();
            kladd.kilde = kilde;
            return this;
        }

        public Builder medBesteberegningPrÅr(Beløp besteberegningPrÅr) {
            verifiserKanModifisere();
            kladd.besteberegningPrÅr = besteberegningPrÅr;
            if (kladd.besteberegningPrÅr != null && kladd.fordeltPrÅr == null) {
                kladd.bruttoPrÅr = besteberegningPrÅr;
                if (kladd.getBeregningsgrunnlagPeriode() != null) {
                    kladd.beregningsgrunnlagPeriode.updateBruttoPrÅr();
                }
            }
            return this;
        }

        public Builder medAndelsnr(Long andelsnr) {
            verifiserKanModifisere();
            kladd.andelsnr = andelsnr;
            return this;
        }

        public Builder medBGAndelArbeidsforhold(BGAndelArbeidsforhold.Builder bgAndelArbeidsforholdBuilder) {
            verifiserKanModifisere();
            kladd.bgAndelArbeidsforhold = bgAndelArbeidsforholdBuilder.build(kladd);
            return this;
        }

        public Builder medOrginalDagsatsFraTilstøtendeYtelse(Long orginalDagsatsFraTilstøtendeYtelse) {
            verifiserKanModifisere();
            kladd.orginalDagsatsFraTilstøtendeYtelse = orginalDagsatsFraTilstøtendeYtelse;
            return this;
        }


        public Builder medAvkortetFørGraderingPrÅr(Beløp avkortetFørGraderingPrÅr) {
            verifiserKanModifisere();
            kladd.avkortetFørGraderingPrÅr = avkortetFørGraderingPrÅr;
            return this;
        }

        public BeregningsgrunnlagPrStatusOgAndel build(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode) {
            if(built) {
                return kladd;
            }
            verifyStateForBuild();
            if (kladd.andelsnr == null) {
                finnOgSettAndelsnr(beregningsgrunnlagPeriode);
            }
            beregningsgrunnlagPeriode.addBeregningsgrunnlagPrStatusOgAndel(kladd);
            beregningsgrunnlagPeriode.updateBruttoPrÅr();
            verifiserAndelsnr();
            built = true;
            return kladd;
        }

        private void finnOgSettAndelsnr(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode) {
            verifiserKanModifisere();
            Long forrigeAndelsnr = beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                    .mapToLong(BeregningsgrunnlagPrStatusOgAndel::getAndelsnr)
                    .max()
                    .orElse(0L);
            kladd.andelsnr = forrigeAndelsnr + 1L;
        }

        private void verifiserAndelsnr() {
            Set<Long> andelsnrIBruk = new HashSet<>();
            kladd.beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .map(BeregningsgrunnlagPrStatusOgAndel::getAndelsnr)
            .forEach(andelsnr -> {
                if (andelsnrIBruk.contains(andelsnr)) {
                    throw new IllegalStateException("Utviklerfeil: Kan ikke bygge andel. Andelsnr eksisterer allerede på en annen andel i samme bgPeriode.");
                }
                andelsnrIBruk.add(andelsnr);
            });
        }

        private void verifiserKanModifisere() {
            if(built) {
                throw new IllegalStateException("Er allerede bygd, kan ikke oppdatere videre: " + this.kladd);
            }
        }

        public void verifyStateForBuild() {
            Objects.requireNonNull(kladd.aktivitetStatus, "aktivitetStatus");
            if (kladd.getAktivitetStatus().equals(AktivitetStatus.ARBEIDSTAKER)
                && kladd.getArbeidsforholdType().equals(OpptjeningAktivitetType.ARBEID)) {
                Objects.requireNonNull(kladd.bgAndelArbeidsforhold, "bgAndelArbeidsforhold");
            }
        }
    }
}
