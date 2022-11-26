package no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag;

import java.math.BigDecimal;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;

public class TilkommetInntektDto {

    private final AktivitetStatus aktivitetStatus;
    private final Arbeidsgiver arbeidsgiver;
    private final BigDecimal bruttoInntektPrÅr;
    private final BigDecimal tilkommetInntektPrÅr;


    public TilkommetInntektDto(TilkommetInntektDto tilkommetInntektDto) {
        this.aktivitetStatus = tilkommetInntektDto.aktivitetStatus;
        this.arbeidsgiver = tilkommetInntektDto.arbeidsgiver;
        this.bruttoInntektPrÅr = tilkommetInntektDto.bruttoInntektPrÅr;
        this.tilkommetInntektPrÅr = tilkommetInntektDto.tilkommetInntektPrÅr;
    }


    public TilkommetInntektDto(AktivitetStatus aktivitetStatus,
                                 Arbeidsgiver arbeidsgiver,
                                 BigDecimal bruttoInntektPrÅr,
                                 BigDecimal tilkommetInntektPrÅr) {
        this.aktivitetStatus = aktivitetStatus;
        this.arbeidsgiver = arbeidsgiver;
        this.bruttoInntektPrÅr = bruttoInntektPrÅr;
        this.tilkommetInntektPrÅr = tilkommetInntektPrÅr;
    }

    public AktivitetStatus getAktivitetStatus() {
        return aktivitetStatus;
    }

    public Optional<Arbeidsgiver> getArbeidsgiver() {
        return Optional.ofNullable(arbeidsgiver);
    }

    public BigDecimal getBruttoInntektPrÅr() {
        return bruttoInntektPrÅr;
    }

    public BigDecimal getTilkommetInntektPrÅr() {
        return tilkommetInntektPrÅr;
    }
}
