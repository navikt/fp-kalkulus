package no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto;


import java.time.LocalDate;
import java.util.Objects;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;

public class RedigerbarAndelDto {

    private Long andelsnr;
    private String arbeidsgiverId;
    private String arbeidsforholdId;
    private Boolean nyAndel;
    private AktivitetStatus aktivitetStatus;
    private OpptjeningAktivitetType arbeidsforholdType;
    private Boolean lagtTilAvSaksbehandler;
    private LocalDate beregningsperiodeFom;
    private LocalDate beregningsperiodeTom;


    protected RedigerbarAndelDto() { // NOSONAR
        // Jackson
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
