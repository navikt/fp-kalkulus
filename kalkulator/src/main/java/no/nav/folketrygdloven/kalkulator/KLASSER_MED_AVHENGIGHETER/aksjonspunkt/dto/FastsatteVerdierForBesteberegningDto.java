package no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto;

import java.math.BigDecimal;

import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;


public class FastsatteVerdierForBesteberegningDto {

    private static final int MÅNEDER_I_1_ÅR = 12;

    private Integer fastsattBeløp;

    private Inntektskategori inntektskategori;

    public FastsatteVerdierForBesteberegningDto(Integer fastsattBeløp,
                                                Inntektskategori inntektskategori) {
        this.fastsattBeløp = fastsattBeløp;
        this.inntektskategori = inntektskategori;
    }

    public Integer getFastsattBeløp() {
        return fastsattBeløp;
    }

    public BigDecimal finnFastsattBeløpPrÅr() {
        if (fastsattBeløp == null) {
            throw new IllegalStateException("Feil under oppdatering: Månedslønn er ikke satt");
        }
        return BigDecimal.valueOf((long) fastsattBeløp * MÅNEDER_I_1_ÅR);
    }

    public Inntektskategori getInntektskategori() {
        return inntektskategori;
    }

    public Boolean getSkalHaBesteberegning() {
        return true;
    }

}
