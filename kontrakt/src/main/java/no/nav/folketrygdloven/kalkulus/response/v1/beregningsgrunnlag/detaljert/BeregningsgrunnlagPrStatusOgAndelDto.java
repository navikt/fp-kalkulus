package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.detaljert;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.response.v1.Arbeidsgiver;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class BeregningsgrunnlagPrStatusOgAndelDto {

    @JsonProperty(value = "andelsnr")
    @NotNull
    @Valid
    private Long andelsnr;

    @JsonProperty(value = "aktivitetStatus")
    @NotNull
    @Valid
    private AktivitetStatus aktivitetStatus;

    @JsonProperty(value = "beregningsperiode")
    @NotNull
    @Valid
    private Periode beregningsperiode;

    @JsonProperty(value = "arbeidsforholdType")
    @NotNull
    @Valid
    private OpptjeningAktivitetType arbeidsforholdType;

    @JsonProperty(value = "bruttoPrÅr")
    @NotNull
    @Valid
    private BigDecimal bruttoPrÅr;

    @JsonProperty(value = "redusertRefusjonPrÅr")
    @NotNull
    @Valid
    private BigDecimal redusertRefusjonPrÅr;

    @JsonProperty(value = "redusertBrukersAndelPrÅr")
    @NotNull
    @Valid
    private BigDecimal redusertBrukersAndelPrÅr;

    @JsonProperty(value = "dagsatsBruker")
    @NotNull
    @Valid
    private Long dagsatsBruker;

    @JsonProperty(value = "dagsatsArbeidsgiver")
    @Valid
    private Long dagsatsArbeidsgiver;

    @JsonProperty(value = "inntektskategori")
    @NotNull
    @Valid
    private Inntektskategori inntektskategori;

    @JsonProperty(value = "bgAndelArbeidsforhold")
    @Valid
    private BGAndelArbeidsforhold bgAndelArbeidsforhold;

    @JsonProperty(value = "overstyrtPrÅr")
    private BigDecimal overstyrtPrÅr;

    @JsonProperty(value = "avkortetPrÅr")
    private BigDecimal avkortetPrÅr;

    @JsonProperty(value = "redusertPrÅr")
    private BigDecimal redusertPrÅr;

    @JsonProperty(value = "beregnetPrÅr")
    private BigDecimal beregnetPrÅr;

    @JsonProperty(value = "fordeltPrÅr")
    private BigDecimal fordeltPrÅr;

    @JsonProperty(value = "maksimalRefusjonPrÅr")
    private BigDecimal maksimalRefusjonPrÅr;

    @JsonProperty(value = "avkortetRefusjonPrÅr")
    private BigDecimal avkortetRefusjonPrÅr;

    @JsonProperty(value = "avkortetBrukersAndelPrÅr")
    private BigDecimal avkortetBrukersAndelPrÅr;

    @JsonProperty(value = "pgiSnitt")
    private BigDecimal pgiSnitt;

    @JsonProperty(value = "pgi1")
    private BigDecimal pgi1;

    @JsonProperty(value = "pgi2")
    private BigDecimal pgi2;

    @JsonProperty(value = "pgi3")
    private BigDecimal pgi3;

    @JsonProperty(value = "årsbeløpFraTilstøtendeYtelse")
    private BigDecimal årsbeløpFraTilstøtendeYtelse;

    @JsonProperty(value = "nyIArbeidslivet")
    private Boolean nyIArbeidslivet;

    @JsonProperty(value = "fastsattAvSaksbehandler")
    private Boolean fastsattAvSaksbehandler = false;

    @JsonProperty(value = "besteberegningPrÅr")
    private BigDecimal besteberegningPrÅr;

    @JsonProperty(value = "lagtTilAvSaksbehandler")
    private Boolean lagtTilAvSaksbehandler = false;

    @JsonProperty(value = "orginalDagsatsFraTilstøtendeYtelse")
    private Long orginalDagsatsFraTilstøtendeYtelse;

    @JsonProperty(value = "mottarYtelse")
    private Boolean mottarYtelse;

    @JsonProperty(value = "nyoppstartet")
    private Boolean nyoppstartet;

    public BeregningsgrunnlagPrStatusOgAndelDto(Long andelsnr, @NotNull @Valid AktivitetStatus aktivitetStatus,
                                                @NotNull @Valid Periode beregningsperiode,
                                                @NotNull @Valid OpptjeningAktivitetType arbeidsforholdType,
                                                @NotNull @Valid BigDecimal bruttoPrÅr,
                                                @NotNull @Valid BigDecimal redusertRefusjonPrÅr,
                                                @NotNull @Valid BigDecimal redusertBrukersAndelPrÅr,
                                                @NotNull @Valid Long dagsatsBruker,
                                                @Valid Long dagsatsArbeidsgiver,
                                                @NotNull @Valid Inntektskategori inntektskategori,
                                                @Valid BGAndelArbeidsforhold bgAndelArbeidsforhold,
                                                BigDecimal overstyrtPrÅr,
                                                BigDecimal avkortetPrÅr,
                                                BigDecimal redusertPrÅr,
                                                BigDecimal beregnetPrÅr,
                                                BigDecimal fordeltPrÅr,
                                                BigDecimal maksimalRefusjonPrÅr,
                                                BigDecimal avkortetRefusjonPrÅr,
                                                BigDecimal avkortetBrukersAndelPrÅr,
                                                BigDecimal pgiSnitt,
                                                BigDecimal pgi1,
                                                BigDecimal pgi2,
                                                BigDecimal pgi3,
                                                BigDecimal årsbeløpFraTilstøtendeYtelse,
                                                Boolean nyIArbeidslivet,
                                                Boolean fastsattAvSaksbehandler,
                                                BigDecimal besteberegningPrÅr,
                                                Boolean lagtTilAvSaksbehandler,
                                                Long orginalDagsatsFraTilstøtendeYtelse,
                                                Boolean mottarYtelse,
                                                Boolean nyoppstartet) {
        this.andelsnr = andelsnr;
        this.aktivitetStatus = aktivitetStatus;
        this.beregningsperiode = beregningsperiode;
        this.arbeidsforholdType = arbeidsforholdType;
        this.bruttoPrÅr = bruttoPrÅr;
        this.redusertRefusjonPrÅr = redusertRefusjonPrÅr;
        this.redusertBrukersAndelPrÅr = redusertBrukersAndelPrÅr;
        this.dagsatsBruker = dagsatsBruker;
        this.dagsatsArbeidsgiver = dagsatsArbeidsgiver;
        this.inntektskategori = inntektskategori;
        this.bgAndelArbeidsforhold = bgAndelArbeidsforhold;
        this.overstyrtPrÅr = overstyrtPrÅr;
        this.avkortetPrÅr = avkortetPrÅr;
        this.redusertPrÅr = redusertPrÅr;
        this.beregnetPrÅr = beregnetPrÅr;
        this.fordeltPrÅr = fordeltPrÅr;
        this.maksimalRefusjonPrÅr = maksimalRefusjonPrÅr;
        this.avkortetRefusjonPrÅr = avkortetRefusjonPrÅr;
        this.avkortetBrukersAndelPrÅr = avkortetBrukersAndelPrÅr;
        this.pgiSnitt = pgiSnitt;
        this.pgi1 = pgi1;
        this.pgi2 = pgi2;
        this.pgi3 = pgi3;
        this.årsbeløpFraTilstøtendeYtelse = årsbeløpFraTilstøtendeYtelse;
        this.nyIArbeidslivet = nyIArbeidslivet;
        this.fastsattAvSaksbehandler = fastsattAvSaksbehandler;
        this.besteberegningPrÅr = besteberegningPrÅr;
        this.lagtTilAvSaksbehandler = lagtTilAvSaksbehandler;
        this.orginalDagsatsFraTilstøtendeYtelse = orginalDagsatsFraTilstøtendeYtelse;
        this.mottarYtelse = mottarYtelse;
        this.nyoppstartet = nyoppstartet;
    }

    public AktivitetStatus getAktivitetStatus() {
        return aktivitetStatus;
    }

    public LocalDate getBeregningsperiodeFom() {
        return beregningsperiode != null ? beregningsperiode.getFom() : null;
    }

    public LocalDate getBeregningsperiodeTom() {
        return beregningsperiode != null ? beregningsperiode.getTom() : null;
    }

    public OpptjeningAktivitetType getArbeidsforholdType() {
        return arbeidsforholdType;
    }

    public BigDecimal getBruttoPrÅr() {
        return bruttoPrÅr;
    }

    public Inntektskategori getInntektskategori() {
        return inntektskategori;
    }

    public BigDecimal getBruttoInkludertNaturalYtelser() {
        BigDecimal naturalytelseBortfalt = getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getNaturalytelseBortfaltPrÅr).orElse(BigDecimal.ZERO);
        BigDecimal naturalYtelseTilkommet = getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getNaturalytelseTilkommetPrÅr).orElse(BigDecimal.ZERO);
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

    public Optional<BGAndelArbeidsforhold> getBgAndelArbeidsforhold() {
        return Optional.ofNullable(bgAndelArbeidsforhold);
    }

    public Optional<Arbeidsgiver> getArbeidsgiver() {
        Optional<BGAndelArbeidsforhold> beregningArbeidsforhold = getBgAndelArbeidsforhold();
        return beregningArbeidsforhold.map(BGAndelArbeidsforhold::getArbeidsgiver);
    }

    public BigDecimal getRedusertBrukersAndelPrÅr() {
        return redusertBrukersAndelPrÅr;
    }

    public BigDecimal getRedusertRefusjonPrÅr() {
        return redusertRefusjonPrÅr;
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

    public BigDecimal getAvkortetBrukersAndelPrÅr() {
        return avkortetBrukersAndelPrÅr;
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

    public BigDecimal getÅrsbeløpFraTilstøtendeYtelse() {
        return årsbeløpFraTilstøtendeYtelse;
    }

    public Boolean getNyIArbeidslivet() {
        return nyIArbeidslivet;
    }

    public Boolean getFastsattAvSaksbehandler() {
        return fastsattAvSaksbehandler;
    }

    public BigDecimal getBesteberegningPrÅr() {
        return besteberegningPrÅr;
    }

    public Boolean getLagtTilAvSaksbehandler() {
        return lagtTilAvSaksbehandler;
    }

    public Long getOrginalDagsatsFraTilstøtendeYtelse() {
        return orginalDagsatsFraTilstøtendeYtelse;
    }

    public Boolean getMottarYtelse() {
        return mottarYtelse;
    }

    public Boolean getNyoppstartet() {
        return nyoppstartet;
    }

    public Long getAndelsnr() {
        return andelsnr;
    }
}
