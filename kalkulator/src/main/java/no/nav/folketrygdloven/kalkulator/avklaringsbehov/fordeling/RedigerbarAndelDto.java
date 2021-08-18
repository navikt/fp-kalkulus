package no.nav.folketrygdloven.kalkulator.avklaringsbehov.fordeling;


import java.time.LocalDate;
import java.util.Objects;

import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.AndelKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;

public class RedigerbarAndelDto {

    private Long andelsnr;
    private String arbeidsgiverId;
    private String arbeidsforholdId;
    private Boolean nyAndel;
    private AndelKilde kilde;
    private AktivitetStatus aktivitetStatus;
    private OpptjeningAktivitetType arbeidsforholdType;
    private LocalDate beregningsperiodeFom;
    private LocalDate beregningsperiodeTom;


    protected RedigerbarAndelDto() { // NOSONAR
        // Jackson
    }

    public RedigerbarAndelDto(Long andelsnr,
                              String arbeidsgiverId,
                              String arbeidsforholdId,
                              Boolean nyAndel,
                              AndelKilde kilde,
                              AktivitetStatus aktivitetStatus,
                              OpptjeningAktivitetType arbeidsforholdType,
                              LocalDate beregningsperiodeFom,
                              LocalDate beregningsperiodeTom) {
        this.andelsnr = andelsnr;
        this.arbeidsgiverId = arbeidsgiverId;
        this.arbeidsforholdId = arbeidsforholdId;
        this.nyAndel = nyAndel;
        this.kilde = kilde;
        this.aktivitetStatus = aktivitetStatus;
        this.arbeidsforholdType = arbeidsforholdType;
        this.beregningsperiodeFom = beregningsperiodeFom;
        this.beregningsperiodeTom = beregningsperiodeTom;
    }

    public RedigerbarAndelDto(Boolean nyAndel,
                              String arbeidsgiverId, String internArbeidsforholdId,
                              Long andelsnr,
                              AktivitetStatus aktivitetStatus, OpptjeningAktivitetType arbeidsforholdType, AndelKilde kilde) {
        Objects.requireNonNull(aktivitetStatus, "aktivitetStatus");
        Objects.requireNonNull(arbeidsforholdType, "arbeidsforholdType");
        this.nyAndel = nyAndel;
        this.arbeidsgiverId = arbeidsgiverId;
        this.arbeidsforholdId = internArbeidsforholdId;
        this.andelsnr = andelsnr;
        this.kilde = kilde;
        this.aktivitetStatus = aktivitetStatus;
        this.arbeidsforholdType = arbeidsforholdType;
    }

    public RedigerbarAndelDto(Boolean nyAndel,
                              String arbeidsgiverId, InternArbeidsforholdRefDto arbeidsforholdId,
                              Long andelsnr,
                              AktivitetStatus aktivitetStatus, OpptjeningAktivitetType arbeidsforholdType, AndelKilde kilde) {
        Objects.requireNonNull(aktivitetStatus, "aktivitetStatus");
        Objects.requireNonNull(arbeidsforholdType, "arbeidsforholdType");
        this.nyAndel = nyAndel;
        this.arbeidsgiverId = arbeidsgiverId;
        this.arbeidsforholdId = arbeidsforholdId == null ? null : arbeidsforholdId.getReferanse();
        this.andelsnr = andelsnr;
        this.kilde = kilde;
        this.aktivitetStatus = aktivitetStatus;
        this.arbeidsforholdType = arbeidsforholdType;
    }


    public RedigerbarAndelDto(Boolean nyAndel,
                              Long andelsnr,
                              AktivitetStatus aktivitetStatus, OpptjeningAktivitetType arbeidsforholdType, AndelKilde kilde) {
        this(nyAndel, null, (InternArbeidsforholdRefDto) null, andelsnr, aktivitetStatus, arbeidsforholdType, kilde);
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

    public Boolean erLagtTilAvSaksbehandler() {
        return kilde.equals(AndelKilde.SAKSBEHANDLER_FORDELING) || kilde.equals(AndelKilde.SAKSBEHANDLER_KOFAKBER);
    }

    public LocalDate getBeregningsperiodeFom() {
        return beregningsperiodeFom;
    }

    public LocalDate getBeregningsperiodeTom() {
        return beregningsperiodeTom;
    }

    public AndelKilde getKilde() {
        return kilde;
    }
}
