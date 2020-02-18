package no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta;


import java.time.LocalDate;
import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;

public class RedigerbarAndelDto {

    @JsonProperty("forrigeArbeidsinntektPrÅr")
    @Valid
    @NotNull
    private Long andelsnr;

    @JsonProperty("forrigeArbeidsinntektPrÅr")
    @Valid
    @NotNull
    private String arbeidsgiverId;

    @JsonProperty("forrigeArbeidsinntektPrÅr")
    @Valid
    @NotNull
    private String arbeidsforholdId;

    @JsonProperty("forrigeArbeidsinntektPrÅr")
    @Valid
    @NotNull
    private Boolean nyAndel;

    @JsonProperty("forrigeArbeidsinntektPrÅr")
    @Valid
    @NotNull
    private AktivitetStatus aktivitetStatus;

    @JsonProperty("forrigeArbeidsinntektPrÅr")
    @Valid
    @NotNull
    private OpptjeningAktivitetType arbeidsforholdType;

    @JsonProperty("forrigeArbeidsinntektPrÅr")
    @Valid
    @NotNull
    private Boolean lagtTilAvSaksbehandler;

    @JsonProperty("forrigeArbeidsinntektPrÅr")
    @Valid
    @NotNull
    private LocalDate beregningsperiodeFom;

    @JsonProperty("forrigeArbeidsinntektPrÅr")
    @Valid
    @NotNull
    private LocalDate beregningsperiodeTom;


    protected RedigerbarAndelDto() { // NOSONAR
        // Jackson
    }

    public RedigerbarAndelDto(Long andelsnr,
                              String arbeidsgiverId,
                              String arbeidsforholdId,
                              Boolean nyAndel,
                              AktivitetStatus aktivitetStatus,
                              OpptjeningAktivitetType arbeidsforholdType,
                              Boolean lagtTilAvSaksbehandler,
                              LocalDate beregningsperiodeFom,
                              LocalDate beregningsperiodeTom) {
        this.andelsnr = andelsnr;
        this.arbeidsgiverId = arbeidsgiverId;
        this.arbeidsforholdId = arbeidsforholdId;
        this.nyAndel = nyAndel;
        this.aktivitetStatus = aktivitetStatus;
        this.arbeidsforholdType = arbeidsforholdType;
        this.lagtTilAvSaksbehandler = lagtTilAvSaksbehandler;
        this.beregningsperiodeFom = beregningsperiodeFom;
        this.beregningsperiodeTom = beregningsperiodeTom;
    }

    public RedigerbarAndelDto(Boolean nyAndel,
                              String arbeidsgiverId, String internArbeidsforholdId,
                              Long andelsnr,
                              Boolean lagtTilAvSaksbehandler,
                              AktivitetStatus aktivitetStatus, OpptjeningAktivitetType arbeidsforholdType) {
        Objects.requireNonNull(aktivitetStatus, "aktivitetStatus");
        Objects.requireNonNull(arbeidsforholdType, "arbeidsforholdType");
        this.nyAndel = nyAndel;
        this.arbeidsgiverId = arbeidsgiverId;
        this.arbeidsforholdId = internArbeidsforholdId;
        this.andelsnr = andelsnr;
        this.lagtTilAvSaksbehandler = lagtTilAvSaksbehandler;
        this.aktivitetStatus = aktivitetStatus;
        this.arbeidsforholdType = arbeidsforholdType;
    }

    public RedigerbarAndelDto(Boolean nyAndel,
                              String arbeidsgiverId, InternArbeidsforholdRefDto arbeidsforholdId,
                              Long andelsnr,
                              Boolean lagtTilAvSaksbehandler,
                              AktivitetStatus aktivitetStatus, OpptjeningAktivitetType arbeidsforholdType) {
        Objects.requireNonNull(aktivitetStatus, "aktivitetStatus");
        Objects.requireNonNull(arbeidsforholdType, "arbeidsforholdType");
        this.nyAndel = nyAndel;
        this.arbeidsgiverId = arbeidsgiverId;
        this.arbeidsforholdId = arbeidsforholdId == null ? null : arbeidsforholdId.getReferanse();
        this.andelsnr = andelsnr;
        this.lagtTilAvSaksbehandler = lagtTilAvSaksbehandler;
        this.aktivitetStatus = aktivitetStatus;
        this.arbeidsforholdType = arbeidsforholdType;
    }


    public RedigerbarAndelDto(Boolean nyAndel,
                              Long andelsnr,
                              Boolean lagtTilAvSaksbehandler,
                              AktivitetStatus aktivitetStatus, OpptjeningAktivitetType arbeidsforholdType) {
        this(nyAndel, null, (InternArbeidsforholdRefDto) null, andelsnr, lagtTilAvSaksbehandler, aktivitetStatus, arbeidsforholdType);
    }

    public AktivitetStatus getAktivitetStatus() {
        return aktivitetStatus;
    }

    public Long getAndelsnr() {
        return andelsnr;
    }

    public InternArbeidsforholdRefDto getArbeidsforholdId() {
        return InternArbeidsforholdRefDto.ref(arbeidsforholdId);
    }

    public String getArbeidsgiverId() {
        return arbeidsgiverId;
    }

    public OpptjeningAktivitetType getArbeidsforholdType() {
        return arbeidsforholdType;
    }

    public Boolean getNyAndel() {
        return nyAndel;
    }

    public Boolean getLagtTilAvSaksbehandler() {
        return lagtTilAvSaksbehandler;
    }

    public LocalDate getBeregningsperiodeFom() {
        return beregningsperiodeFom;
    }

    public LocalDate getBeregningsperiodeTom() {
        return beregningsperiodeTom;
    }
}
