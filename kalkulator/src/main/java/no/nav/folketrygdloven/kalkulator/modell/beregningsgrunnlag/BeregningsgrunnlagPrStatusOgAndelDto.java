package no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag;

import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.Inntektskategori;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class BeregningsgrunnlagPrStatusOgAndelDto {

    private Long andelsnr;
    private BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode;
    private AktivitetStatus aktivitetStatus;
    private Intervall beregningsperiode;
    private OpptjeningAktivitetType arbeidsforholdType;
    private BigDecimal bruttoPrÅr;
    private BigDecimal overstyrtPrÅr;
    private BigDecimal avkortetPrÅr;
    private BigDecimal redusertPrÅr;
    private BigDecimal beregnetPrÅr;
    private BigDecimal fordeltPrÅr;
    private BigDecimal maksimalRefusjonPrÅr;
    private BigDecimal avkortetRefusjonPrÅr;
    private BigDecimal redusertRefusjonPrÅr;
    private BigDecimal avkortetBrukersAndelPrÅr;
    private BigDecimal redusertBrukersAndelPrÅr;
    private Long dagsatsBruker;
    private Long dagsatsArbeidsgiver;
    private BigDecimal pgiSnitt;
    private BigDecimal pgi1;
    private BigDecimal pgi2;
    private BigDecimal pgi3;
    private Beløp årsbeløpFraTilstøtendeYtelse;
    private Boolean nyIArbeidslivet;
    private Boolean fastsattAvSaksbehandler = false;
    private BigDecimal besteberegningPrÅr;
    private Inntektskategori inntektskategori = Inntektskategori.UDEFINERT;
    private Boolean lagtTilAvSaksbehandler = false;
    private BGAndelArbeidsforholdDto bgAndelArbeidsforhold;
    private Long orginalDagsatsFraTilstøtendeYtelse;
    private BeregningsgrunnlagFrilansAndelDto beregningsgrunnlagFrilansAndel;
    private BeregningsgrunnlagArbeidstakerAndelDto beregningsgrunnlagArbeidstakerAndel;

    public BeregningsgrunnlagPrStatusOgAndelDto() { }

    public BeregningsgrunnlagPrStatusOgAndelDto(BeregningsgrunnlagPrStatusOgAndelDto kopiereFra) {
        this.andelsnr = kopiereFra.andelsnr;
        this.aktivitetStatus = kopiereFra.aktivitetStatus;
        this.beregningsperiode = kopiereFra.beregningsperiode;
        this.arbeidsforholdType = kopiereFra.arbeidsforholdType;
        this.bruttoPrÅr = kopiereFra.bruttoPrÅr;
        this.overstyrtPrÅr = kopiereFra.overstyrtPrÅr;
        this.avkortetPrÅr = kopiereFra.avkortetPrÅr;
        this.redusertPrÅr = kopiereFra.redusertPrÅr;
        this.beregnetPrÅr = kopiereFra.beregnetPrÅr;
        this.fordeltPrÅr = kopiereFra.fordeltPrÅr;
        this.maksimalRefusjonPrÅr = kopiereFra.maksimalRefusjonPrÅr;
        this.avkortetRefusjonPrÅr = kopiereFra.avkortetRefusjonPrÅr;
        this.redusertRefusjonPrÅr = kopiereFra.redusertRefusjonPrÅr;
        this.avkortetBrukersAndelPrÅr = kopiereFra.avkortetBrukersAndelPrÅr;
        this.redusertBrukersAndelPrÅr = kopiereFra.redusertBrukersAndelPrÅr;
        this.dagsatsBruker = kopiereFra.dagsatsBruker;
        this.dagsatsArbeidsgiver = kopiereFra.dagsatsArbeidsgiver;
        this.pgiSnitt = kopiereFra.pgiSnitt;
        this.pgi1 = kopiereFra.pgi1;
        this.pgi2 = kopiereFra.pgi2;
        this.pgi3 = kopiereFra.pgi3;
        this.årsbeløpFraTilstøtendeYtelse = kopiereFra.årsbeløpFraTilstøtendeYtelse;
        this.nyIArbeidslivet = kopiereFra.nyIArbeidslivet;
        this.fastsattAvSaksbehandler = kopiereFra.fastsattAvSaksbehandler;
        this.besteberegningPrÅr = kopiereFra.besteberegningPrÅr;
        this.inntektskategori = kopiereFra.inntektskategori;
        this.lagtTilAvSaksbehandler = kopiereFra.lagtTilAvSaksbehandler;
        this.orginalDagsatsFraTilstøtendeYtelse = kopiereFra.orginalDagsatsFraTilstøtendeYtelse;
        if (kopiereFra.bgAndelArbeidsforhold != null) {
            this.bgAndelArbeidsforhold = BGAndelArbeidsforholdDto.Builder.kopier(kopiereFra.bgAndelArbeidsforhold).build(this);
        }
        if (kopiereFra.beregningsgrunnlagFrilansAndel != null) {
            this.beregningsgrunnlagFrilansAndel = BeregningsgrunnlagFrilansAndelDto.Builder.kopier(kopiereFra.beregningsgrunnlagFrilansAndel).build(this);
        }
        if (kopiereFra.beregningsgrunnlagArbeidstakerAndel != null) {
            this.setBeregningsgrunnlagArbeidstakerAndel(BeregningsgrunnlagArbeidstakerAndelDto.Builder.kopier(kopiereFra.beregningsgrunnlagArbeidstakerAndel).build());
        }
    }


    private BeregningsgrunnlagPeriodeDto getBeregningsgrunnlagPeriode() {
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

    public Optional<Boolean> mottarYtelse() {
        if (beregningsgrunnlagFrilansAndel != null) {
            return Optional.ofNullable(beregningsgrunnlagFrilansAndel.getMottarYtelse());
        } else if (beregningsgrunnlagArbeidstakerAndel != null) {
            return Optional.ofNullable(beregningsgrunnlagArbeidstakerAndel.getMottarYtelse());
        }
        return Optional.empty();
    }

    public Optional<Boolean> erNyoppstartet() {
        if (beregningsgrunnlagFrilansAndel != null) {
            return Optional.ofNullable(beregningsgrunnlagFrilansAndel.getNyoppstartet());
        }
        return Optional.empty();
    }

    public boolean gjelderSammeArbeidsforhold(BeregningsgrunnlagPrStatusOgAndelDto that) {
        if (!Objects.equals(this.getAktivitetStatus(), AktivitetStatus.ARBEIDSTAKER) || !Objects.equals(that.getAktivitetStatus(), AktivitetStatus.ARBEIDSTAKER)) {
            return false;
        }
        return gjelderSammeArbeidsforhold(that.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getArbeidsgiver),
            that.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getArbeidsforholdRef).orElse(InternArbeidsforholdRefDto.nullRef()));
    }

    public boolean gjelderSammeArbeidsforhold(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto arbeidsforholdRef) {
        return gjelderSammeArbeidsforhold(Optional.ofNullable(arbeidsgiver), arbeidsforholdRef);
    }

    public boolean gjelderInntektsmeldingFor(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto arbeidsforholdRef) {
        Optional<BGAndelArbeidsforholdDto> bgAndelArbeidsforholdOpt = getBgAndelArbeidsforhold();
        if (!Objects.equals(getAktivitetStatus(), AktivitetStatus.ARBEIDSTAKER) || !bgAndelArbeidsforholdOpt.isPresent()) {
            return false;
        }
        if(beregningsgrunnlagArbeidstakerAndel != null && !beregningsgrunnlagArbeidstakerAndel.getHarInntektsmelding()) {
            return false;
        }
        if (!Objects.equals(this.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getArbeidsgiver), Optional.of(arbeidsgiver))) {
            return false;
        }
        return  bgAndelArbeidsforholdOpt.get().getArbeidsforholdRef().gjelderFor(arbeidsforholdRef);
    }

    private boolean gjelderSammeArbeidsforhold(Optional<Arbeidsgiver> arbeidsgiver, InternArbeidsforholdRefDto arbeidsforholdRef) {
        Optional<BGAndelArbeidsforholdDto> bgAndelArbeidsforholdOpt = getBgAndelArbeidsforhold();
        if (!Objects.equals(getAktivitetStatus(), AktivitetStatus.ARBEIDSTAKER) || !bgAndelArbeidsforholdOpt.isPresent()) {
            return false;
        }
        return Objects.equals(this.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getArbeidsgiver), arbeidsgiver)
                && bgAndelArbeidsforholdOpt.get().getArbeidsforholdRef().gjelderFor(arbeidsforholdRef);
    }

    public boolean matchUtenInntektskategori(BeregningsgrunnlagPrStatusOgAndelDto other) {
        return Objects.equals(this.getAktivitetStatus(), other.getAktivitetStatus())
                && Objects.equals(this.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getArbeidsgiver), other.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getArbeidsgiver))
                && Objects.equals(this.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getArbeidsforholdRef), other.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getArbeidsforholdRef))
                && Objects.equals(this.getArbeidsforholdType(), other.getArbeidsforholdType());
    }

    public boolean matchUtenInntektskategori(AktivitetStatus aktivitetStatus, Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto internArbeidsforholdRefDto, OpptjeningAktivitetType arbeidsforholdType) {
        return Objects.equals(this.getAktivitetStatus(), aktivitetStatus)
            && Objects.equals(this.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getArbeidsgiver), arbeidsgiver)
            && Objects.equals(this.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getArbeidsforholdRef), internArbeidsforholdRefDto)
            && Objects.equals(this.getArbeidsforholdType(), arbeidsforholdType);
    }

    public OpptjeningAktivitetType getArbeidsforholdType() {
        return arbeidsforholdType;
    }

    public BigDecimal getBruttoPrÅr() {
        return bruttoPrÅr;
    }

    public BigDecimal getOverstyrtPrÅr() {
        return overstyrtPrÅr;
    }

    public BigDecimal getAvkortetPrÅr() {
        return avkortetPrÅr;
    }

    public BigDecimal getRedusertPrÅr() {
        return redusertPrÅr;
    }

    public BigDecimal getBeregnetPrÅr() {
        return beregnetPrÅr;
    }

    public BigDecimal getFordeltPrÅr() {
        return fordeltPrÅr;
    }

    public BigDecimal getMaksimalRefusjonPrÅr() {
        return maksimalRefusjonPrÅr;
    }

    public BigDecimal getAvkortetRefusjonPrÅr() {
        return avkortetRefusjonPrÅr;
    }

    public BigDecimal getRedusertRefusjonPrÅr() {
        return redusertRefusjonPrÅr;
    }

    public BigDecimal getAvkortetBrukersAndelPrÅr() {
        return avkortetBrukersAndelPrÅr;
    }

    public BigDecimal getRedusertBrukersAndelPrÅr() {
        return redusertBrukersAndelPrÅr;
    }

    public Boolean getNyIArbeidslivet() {
        return nyIArbeidslivet;
    }

    public Boolean getFastsattAvSaksbehandler() {
        return fastsattAvSaksbehandler;
    }

    public Inntektskategori getInntektskategori() {
        return inntektskategori;
    }

    public BigDecimal getBruttoInkludertNaturalYtelser() {
        BigDecimal naturalytelseBortfalt = getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforholdDto::getNaturalytelseBortfaltPrÅr).orElse(BigDecimal.ZERO);
        BigDecimal naturalYtelseTilkommet = getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforholdDto::getNaturalytelseTilkommetPrÅr).orElse(BigDecimal.ZERO);
        BigDecimal brutto = bruttoPrÅr != null ? bruttoPrÅr : BigDecimal.ZERO;
        return brutto.add(naturalytelseBortfalt).subtract(naturalYtelseTilkommet);
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

    public BigDecimal getPgiSnitt() {
        return pgiSnitt;
    }

    public BigDecimal getPgi1() {
        return pgi1;
    }

    public BigDecimal getPgi2() {
        return pgi2;
    }

    public BigDecimal getPgi3() {
        return pgi3;
    }

    public Beløp getÅrsbeløpFraTilstøtendeYtelse() {
        return årsbeløpFraTilstøtendeYtelse;
    }

    public BigDecimal getÅrsbeløpFraTilstøtendeYtelseVerdi() {
        return Optional.ofNullable(getÅrsbeløpFraTilstøtendeYtelse())
                .map(Beløp::getVerdi).orElse(BigDecimal.ZERO);
    }

    public Long getAndelsnr() {
        return andelsnr;
    }

    public BigDecimal getBesteberegningPrÅr() {
        return besteberegningPrÅr;
    }

    public Boolean getLagtTilAvSaksbehandler() {
        return lagtTilAvSaksbehandler;
    }

    public Optional<BGAndelArbeidsforholdDto> getBgAndelArbeidsforhold() {
        return Optional.ofNullable(bgAndelArbeidsforhold);
    }

    public Long getOrginalDagsatsFraTilstøtendeYtelse() {
        return orginalDagsatsFraTilstøtendeYtelse;
    }

    public Optional<Arbeidsgiver> getArbeidsgiver() {
        Optional<BGAndelArbeidsforholdDto> beregningArbeidsforhold = getBgAndelArbeidsforhold();
        return beregningArbeidsforhold.map(BGAndelArbeidsforholdDto::getArbeidsgiver);
    }

    public Optional<InternArbeidsforholdRefDto> getArbeidsforholdRef() {
        Optional<BGAndelArbeidsforholdDto> beregningArbeidsforhold = getBgAndelArbeidsforhold();
        return beregningArbeidsforhold.map(BGAndelArbeidsforholdDto::getArbeidsforholdRef);
    }

    private void setBeregningsgrunnlagArbeidstakerAndel(BeregningsgrunnlagArbeidstakerAndelDto beregningsgrunnlagArbeidstakerAndel) {
        beregningsgrunnlagArbeidstakerAndel.setBeregningsgrunnlagPrStatusOgAndel(this);
        this.beregningsgrunnlagArbeidstakerAndel = beregningsgrunnlagArbeidstakerAndel;
    }

    private void setBeregningsgrunnlagFrilansAndel(BeregningsgrunnlagFrilansAndelDto beregningsgrunnlagFrilansAndel) {
        beregningsgrunnlagFrilansAndel.setBeregningsgrunnlagPrStatusOgAndel(this);
        this.beregningsgrunnlagFrilansAndel = beregningsgrunnlagFrilansAndel;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof BeregningsgrunnlagPrStatusOgAndelDto)) {
            return false;
        }
        // Endring av denne har store konsekvenser for matching av andeler
        // Resultat av endringer må testes manuelt
        BeregningsgrunnlagPrStatusOgAndelDto other = (BeregningsgrunnlagPrStatusOgAndelDto) obj;
        return Objects.equals(this.getAktivitetStatus(), other.getAktivitetStatus())
                && Objects.equals(this.getInntektskategori(), other.getInntektskategori())
                && Objects.equals(this.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getArbeidsgiver),
                    other.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getArbeidsgiver))
                && Objects.equals(this.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getArbeidsforholdRef),
                    other.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getArbeidsforholdRef))
                && Objects.equals(this.getArbeidsforholdType(), other.getArbeidsforholdType());
    }


    @Override
    public int hashCode() {
        return Objects.hash(aktivitetStatus,
            inntektskategori,
            getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getArbeidsgiver),
            getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getArbeidsforholdRef),
            arbeidsforholdType);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + //$NON-NLS-1$
            "beregningsgrunnlagPeriode=" + beregningsgrunnlagPeriode + ", " //$NON-NLS-1$ //$NON-NLS-2$
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
                + "bgAndelArbeidsforhold=" + bgAndelArbeidsforhold //$NON-NLS-1$
                + ">"; //$NON-NLS-1$
    }

    public static Builder kopier() {
        return new Builder();
    }

    public static Builder kopier(BeregningsgrunnlagPrStatusOgAndelDto eksisterendeBGPrStatusOgAndel) {
        return new Builder(eksisterendeBGPrStatusOgAndel);
    }

    void setBeregningsgrunnlagPeriode(BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriodeDto) {
        this.beregningsgrunnlagPeriode = beregningsgrunnlagPeriodeDto;
    }

    public static class Builder {
        /** Når det er built kan ikke denne builderen brukes til annet enn å returnere samme objekt. */
        private boolean built;

        private BeregningsgrunnlagPrStatusOgAndelDto kladd;

        private boolean erOppdatering;

        private Builder() {
            kladd = new BeregningsgrunnlagPrStatusOgAndelDto();
            kladd.arbeidsforholdType = OpptjeningAktivitetType.UDEFINERT;
        }

        private Builder(BeregningsgrunnlagPrStatusOgAndelDto eksisterendeBGPrStatusOgAndelMal) {
            kladd = new BeregningsgrunnlagPrStatusOgAndelDto(eksisterendeBGPrStatusOgAndelMal);
            this.erOppdatering = false;
        }

        private Builder(BeregningsgrunnlagPrStatusOgAndelDto eksisterendeBGPrStatusOgAndelMal, boolean erOppdatering) {
            kladd = eksisterendeBGPrStatusOgAndelMal;
            this.erOppdatering = erOppdatering;
        }

        public static Builder oppdatere(BeregningsgrunnlagPrStatusOgAndelDto oppdatere) {
            return new Builder(oppdatere, true);
        }

        public static Builder ny() {
            return new Builder(new BeregningsgrunnlagPrStatusOgAndelDto(), false);
        }

        public static Builder oppdatere(Optional<BeregningsgrunnlagPrStatusOgAndelDto> oppdatere) {
            return oppdatere.map(Builder::oppdatere).orElseGet(Builder::ny);
        }

        static Builder kopier(BeregningsgrunnlagPrStatusOgAndelDto a) {
            return new Builder(a, false);
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
            kladd.beregningsperiode = Intervall.fraOgMedTilOgMed(beregningsperiodeFom, beregningsperiodeTom);
            return this;
        }

        public Builder medArbforholdType(OpptjeningAktivitetType arbforholdType) {
            verifiserKanModifisere();
            kladd.arbeidsforholdType = arbforholdType;
            return this;
        }

        public Builder medOverstyrtPrÅr(BigDecimal overstyrtPrÅr) {
            verifiserKanModifisere();
            kladd.overstyrtPrÅr = overstyrtPrÅr;
            if (overstyrtPrÅr != null && kladd.fordeltPrÅr == null) {
                kladd.bruttoPrÅr = overstyrtPrÅr;
                if (kladd.getBeregningsgrunnlagPeriode() != null) {
                    kladd.beregningsgrunnlagPeriode.updateBruttoPrÅr();
                }
            }
            return this;
        }

        public Builder medFordeltPrÅr(BigDecimal fordeltPrÅr) {
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


        public Builder medAvkortetPrÅr(BigDecimal avkortetPrÅr) {
            verifiserKanModifisere();
            kladd.avkortetPrÅr = avkortetPrÅr;
            return this;
        }

        public Builder medRedusertPrÅr(BigDecimal redusertPrÅr) {
            verifiserKanModifisere();
            kladd.redusertPrÅr = redusertPrÅr;
            return this;
        }

        public Builder medMaksimalRefusjonPrÅr(BigDecimal maksimalRefusjonPrÅr) {
            verifiserKanModifisere();
            kladd.maksimalRefusjonPrÅr = maksimalRefusjonPrÅr;
            return this;
        }

        public Builder medAvkortetRefusjonPrÅr(BigDecimal avkortetRefusjonPrÅr) {
            verifiserKanModifisere();
            kladd.avkortetRefusjonPrÅr = avkortetRefusjonPrÅr;
            return this;
        }

        public Builder medRedusertRefusjonPrÅr(BigDecimal redusertRefusjonPrÅr) {
            verifiserKanModifisere();
            kladd.redusertRefusjonPrÅr = redusertRefusjonPrÅr;
            kladd.dagsatsArbeidsgiver = redusertRefusjonPrÅr == null ?
                null : redusertRefusjonPrÅr.divide(BigDecimal.valueOf(260), 0, RoundingMode.HALF_UP).longValue();
            return this;
        }

        public Builder medAvkortetBrukersAndelPrÅr(BigDecimal avkortetBrukersAndelPrÅr) {
            verifiserKanModifisere();
            kladd.avkortetBrukersAndelPrÅr = avkortetBrukersAndelPrÅr;
            return this;
        }

        public Builder medRedusertBrukersAndelPrÅr(BigDecimal redusertBrukersAndelPrÅr) {
            verifiserKanModifisere();
            kladd.redusertBrukersAndelPrÅr = redusertBrukersAndelPrÅr;
            kladd.dagsatsBruker = redusertBrukersAndelPrÅr == null ?
                null : redusertBrukersAndelPrÅr.divide(BigDecimal.valueOf(260), 0, RoundingMode.HALF_UP).longValue();
            return this;
        }

        public Builder medBeregnetPrÅr(BigDecimal beregnetPrÅr) {
            verifiserKanModifisere();
            kladd.beregnetPrÅr = beregnetPrÅr;
            if (kladd.fordeltPrÅr == null && kladd.overstyrtPrÅr == null) {
                kladd.bruttoPrÅr = beregnetPrÅr;
                if (kladd.getBeregningsgrunnlagPeriode() != null) {
                    kladd.beregningsgrunnlagPeriode.updateBruttoPrÅr();
                }
            }
            return this;
        }

        public Builder medPgi(BigDecimal pgiSnitt, List<BigDecimal> pgiListe) {
            verifiserKanModifisere();
            kladd.pgiSnitt = pgiSnitt;
            kladd.pgi1 = pgiListe.isEmpty() ? null : pgiListe.get(0);
            kladd.pgi2 = pgiListe.isEmpty() ? null : pgiListe.get(1);
            kladd.pgi3 = pgiListe.isEmpty() ? null : pgiListe.get(2);
            return this;
        }

        public Builder medÅrsbeløpFraTilstøtendeYtelse(BigDecimal årsbeløpFraTilstøtendeYtelse) {
            verifiserKanModifisere();
            kladd.årsbeløpFraTilstøtendeYtelse = new Beløp(årsbeløpFraTilstøtendeYtelse);
            return this;
        }

        public Builder medNyIArbeidslivet(Boolean nyIArbeidslivet) {
            verifiserKanModifisere();
            kladd.nyIArbeidslivet = nyIArbeidslivet;
            return this;
        }

        public Builder medBeregningsgrunnlagArbeidstakerAndel(BeregningsgrunnlagArbeidstakerAndelDto beregningsgrunnlagArbeidstakerAndelDto) {
            verifiserKanModifisere();
            kladd.setBeregningsgrunnlagArbeidstakerAndel(beregningsgrunnlagArbeidstakerAndelDto);
            return this;
        }

        public Builder medBeregningsgrunnlagFrilansAndel(BeregningsgrunnlagFrilansAndelDto beregningsgrunnlagFrilansAndelDto) {
            verifiserKanModifisere();
            kladd.setBeregningsgrunnlagFrilansAndel(beregningsgrunnlagFrilansAndelDto);
            return this;
        }



        public Builder medMottarYtelse(Boolean mottarYtelse, AktivitetStatus aktivitetStatus) {
            verifiserKanModifisere();
            kladd.aktivitetStatus = aktivitetStatus;
            if (kladd.aktivitetStatus.erFrilanser()) {
                if (kladd.beregningsgrunnlagFrilansAndel == null) {
                    kladd.beregningsgrunnlagFrilansAndel = BeregningsgrunnlagFrilansAndelDto.builder()
                            .medMottarYtelse(mottarYtelse)
                            .build(kladd);
                } else {
                    BeregningsgrunnlagFrilansAndelDto.Builder.oppdatere(kladd.beregningsgrunnlagFrilansAndel)
                    .medMottarYtelse(mottarYtelse);
                }
            } else if (kladd.aktivitetStatus.erArbeidstaker()) {
                if (kladd.beregningsgrunnlagArbeidstakerAndel == null) {
                    kladd.beregningsgrunnlagArbeidstakerAndel = BeregningsgrunnlagArbeidstakerAndelDto.builder()
                            .medMottarYtelse(mottarYtelse)
                            .build(kladd);
                } else {
                    BeregningsgrunnlagArbeidstakerAndelDto.Builder.oppdatere(kladd.beregningsgrunnlagArbeidstakerAndel)
                    .medMottarYtelse(mottarYtelse);
                }
            }
            return this;
        }

        public Builder medNyoppstartet(Boolean nyoppstartet, AktivitetStatus aktivitetStatus) {
            verifiserKanModifisere();
            kladd.aktivitetStatus = aktivitetStatus;
            if (kladd.aktivitetStatus.erFrilanser()) {
                if (kladd.beregningsgrunnlagFrilansAndel == null) {
                    kladd.beregningsgrunnlagFrilansAndel = BeregningsgrunnlagFrilansAndelDto.builder()
                            .medNyoppstartet(nyoppstartet)
                            .build(kladd);
                } else {
                    BeregningsgrunnlagFrilansAndelDto.builder(kladd.beregningsgrunnlagFrilansAndel)
                    .medNyoppstartet(nyoppstartet);
                }
            } else {
                throw new IllegalArgumentException("Andel må vere frilans for å sette nyoppstartet");
            }
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

        public Builder medBesteberegningPrÅr(BigDecimal besteberegningPrÅr) {
            verifiserKanModifisere();
            kladd.besteberegningPrÅr = besteberegningPrÅr;
            return this;
        }

        public Builder medAndelsnr(Long andelsnr) {
            if (!erOppdatering) {
                verifiserKanModifisere();
                kladd.andelsnr = andelsnr;
            }
            return this;
        }

        public Builder nyttAndelsnr(BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode) {
            if (!erOppdatering) {
                verifiserKanModifisere();
                finnOgSettAndelsnr(beregningsgrunnlagPeriode);
            }
            return this;
        }

        public Builder medLagtTilAvSaksbehandler(Boolean lagtTilAvSaksbehandler) {
            if (!erOppdatering) {
                verifiserKanModifisere();
                kladd.lagtTilAvSaksbehandler = lagtTilAvSaksbehandler;
            }
            return this;
        }

        public BeregningsgrunnlagArbeidstakerAndelDto.Builder getBeregningsgrunnlagArbeidstakerAndelBuilder() {
            return BeregningsgrunnlagArbeidstakerAndelDto.builder(kladd.beregningsgrunnlagArbeidstakerAndel);
        }

        public BeregningsgrunnlagFrilansAndelDto.Builder getBeregningsgrunnlagFrilansAndelBuilder() {
            return BeregningsgrunnlagFrilansAndelDto.builder(kladd.beregningsgrunnlagFrilansAndel);
        }

        public BGAndelArbeidsforholdDto.Builder getBgAndelArbeidsforholdDtoBuilder() {
            return BGAndelArbeidsforholdDto.builder(kladd.bgAndelArbeidsforhold);
        }

        public Builder medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.Builder bgAndelArbeidsforholdBuilder) {
            verifiserKanModifisere();
            kladd.bgAndelArbeidsforhold = bgAndelArbeidsforholdBuilder.build(kladd);
            return this;
        }

        public Builder medOrginalDagsatsFraTilstøtendeYtelse(Long orginalDagsatsFraTilstøtendeYtelse) {
            verifiserKanModifisere();
            kladd.orginalDagsatsFraTilstøtendeYtelse = orginalDagsatsFraTilstøtendeYtelse;
            return this;
        }

        public BeregningsgrunnlagPrStatusOgAndelDto build() {
            if (this.kladd.andelsnr == null) {
                throw new IllegalStateException("Må sette andelsnr for å bygge uten referanse til periode");
            }
            if(built) {
                return kladd;
            }
            built = true;
            return kladd;
        }


        public BeregningsgrunnlagPrStatusOgAndelDto build(BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode) {
            if(built) {
                return kladd;
            }
            kladd.beregningsgrunnlagPeriode = beregningsgrunnlagPeriode;
            verifyStateForBuild();
            if (kladd.andelsnr == null) {
                // TODO (OleSandbu): Ikke mod input!
                finnOgSettAndelsnr(beregningsgrunnlagPeriode);
            }
            if (kladd.lagtTilAvSaksbehandler == null) {
                kladd.lagtTilAvSaksbehandler = false;
            }
            // TODO (OleSandbu): Ikke mod input!
            beregningsgrunnlagPeriode.addBeregningsgrunnlagPrStatusOgAndel(kladd);
            beregningsgrunnlagPeriode.updateBruttoPrÅr();
            verifiserAndelsnr();
            built = true;
            return kladd;
        }

        private void finnOgSettAndelsnr(BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode) {
            verifiserKanModifisere();
            Long forrigeAndelsnr = beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                    .mapToLong(BeregningsgrunnlagPrStatusOgAndelDto::getAndelsnr)
                    .max()
                    .orElse(0L);
            kladd.andelsnr = forrigeAndelsnr + 1L;
        }

        private void verifiserAndelsnr() {
            Set<Long> andelsnrIBruk = new HashSet<>();
            kladd.beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .map(BeregningsgrunnlagPrStatusOgAndelDto::getAndelsnr)
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
            Objects.requireNonNull(kladd.beregningsgrunnlagPeriode, "beregningsgrunnlagPeriode");
            Objects.requireNonNull(kladd.aktivitetStatus, "aktivitetStatus");
            if (AktivitetStatus.ARBEIDSTAKER.equals(kladd.getAktivitetStatus())
                && OpptjeningAktivitetType.ARBEID.equals(kladd.getArbeidsforholdType())) {
                Objects.requireNonNull(kladd.bgAndelArbeidsforhold, "bgAndelArbeidsforhold");
            }
        }

    }
}
