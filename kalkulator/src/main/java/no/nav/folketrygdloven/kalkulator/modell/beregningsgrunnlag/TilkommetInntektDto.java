package no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag;

import java.math.BigDecimal;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;

public class TilkommetInntektDto {

    private final AktivitetStatus aktivitetStatus;
    private final Arbeidsgiver arbeidsgiver;
    private final InternArbeidsforholdRefDto arbeidsforholdRef;
    private final BigDecimal bruttoInntektPrÅr;
    private final BigDecimal tilkommetInntektPrÅr;
    private final boolean erTilkommet;


    public TilkommetInntektDto(TilkommetInntektDto tilkommetInntektDto) {
        this.aktivitetStatus = tilkommetInntektDto.aktivitetStatus;
        this.arbeidsgiver = tilkommetInntektDto.arbeidsgiver;
        this.arbeidsforholdRef = tilkommetInntektDto.arbeidsforholdRef;
        this.bruttoInntektPrÅr = tilkommetInntektDto.bruttoInntektPrÅr;
        this.tilkommetInntektPrÅr = tilkommetInntektDto.tilkommetInntektPrÅr;
        this.erTilkommet = tilkommetInntektDto.erTilkommet;
    }


    public TilkommetInntektDto(AktivitetStatus aktivitetStatus,
                               Arbeidsgiver arbeidsgiver,
                               InternArbeidsforholdRefDto arbeidsforholdRef,
                               BigDecimal bruttoInntektPrÅr,
                               BigDecimal tilkommetInntektPrÅr,
                               boolean erTilkommet) {
        if (!erTilkommet && tilkommetInntektPrÅr != null) {
            throw new IllegalStateException("Skal ikke sette tilkommet inntekt når ikke tilkommet");
        }
        this.aktivitetStatus = aktivitetStatus;
        this.arbeidsgiver = arbeidsgiver;
        this.arbeidsforholdRef = arbeidsforholdRef;
        this.bruttoInntektPrÅr = bruttoInntektPrÅr;
        this.tilkommetInntektPrÅr = tilkommetInntektPrÅr;
        this.erTilkommet = erTilkommet;
    }

    public AktivitetStatus getAktivitetStatus() {
        return aktivitetStatus;
    }

    public Optional<Arbeidsgiver> getArbeidsgiver() {
        return Optional.ofNullable(arbeidsgiver);
    }

    public InternArbeidsforholdRefDto getArbeidsforholdRef() {
        return arbeidsforholdRef;
    }

    public BigDecimal getBruttoInntektPrÅr() {
        return bruttoInntektPrÅr;
    }

    public BigDecimal getTilkommetInntektPrÅr() {
        return tilkommetInntektPrÅr;
    }

    public boolean erTilkommet() {
        return erTilkommet;
    }
}
