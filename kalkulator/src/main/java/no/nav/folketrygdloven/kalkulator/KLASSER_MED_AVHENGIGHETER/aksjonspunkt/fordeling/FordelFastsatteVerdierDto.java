package no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.fordeling;

import java.math.BigDecimal;

import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;


public class FordelFastsatteVerdierDto {

    private static final int MÅNEDER_I_1_ÅR = 12;

    private Integer refusjonPrÅr;
    private Integer fastsattBeløp;
    private Integer fastsattÅrsbeløp;
    private Integer fastsattÅrsbeløpInklNaturalytelse;
    private Inntektskategori inntektskategori;

    private FordelFastsatteVerdierDto() {}

    private FordelFastsatteVerdierDto(Integer refusjonPrÅr,
                                      Integer fastsattBeløp,
                                      Integer fastsattÅrsbeløp,
                                      Inntektskategori inntektskategori) {
        this.refusjonPrÅr = refusjonPrÅr;
        this.fastsattBeløp = fastsattBeløp;
        this.fastsattÅrsbeløp = fastsattÅrsbeløp;
        this.inntektskategori = inntektskategori;
    }

    public Integer getRefusjonPrÅr() {
        return refusjonPrÅr;
    }

    public Integer getFastsattBeløp() {
        return fastsattBeløp;
    }

    public Integer getFastsattÅrsbeløpInklNaturalytelse() {
        return fastsattÅrsbeløpInklNaturalytelse;
    }

    public BigDecimal finnEllerUtregnFastsattBeløpPrÅr() {
        if (fastsattÅrsbeløpInklNaturalytelse != null) {
            return BigDecimal.valueOf(fastsattÅrsbeløpInklNaturalytelse);
        }
        if (fastsattÅrsbeløp != null) {
            return BigDecimal.valueOf(fastsattÅrsbeløp);
        }
        if (fastsattBeløp == null) {
            throw new IllegalStateException("Feil under oppdatering: Hverken årslønn eller månedslønn er satt.");
        }
        return BigDecimal.valueOf((long) fastsattBeløp * MÅNEDER_I_1_ÅR);
    }

    public Inntektskategori getInntektskategori() {
        return inntektskategori;
    }


    public static class Builder {

        public Builder(FordelFastsatteVerdierDto fordelFastsatteVerdierDto) {
            kladd = new FordelFastsatteVerdierDto(
                    fordelFastsatteVerdierDto.refusjonPrÅr,
                    fordelFastsatteVerdierDto.fastsattBeløp,
                    fordelFastsatteVerdierDto.fastsattÅrsbeløp,
                    fordelFastsatteVerdierDto.inntektskategori
            );
        }

        public Builder() {
            kladd = new FordelFastsatteVerdierDto();
        }

        private FordelFastsatteVerdierDto kladd;

        public static Builder ny() {
            return new Builder();
        }

        public static Builder oppdater(FordelFastsatteVerdierDto fordelFastsatteVerdierDto) {
            return new Builder(fordelFastsatteVerdierDto);
        }

        public Builder medRefusjonPrÅr(Integer refusjonPrÅr) {
            kladd.refusjonPrÅr = refusjonPrÅr;
            return this;
        }

        public Builder medInntektskategori(Inntektskategori inntektskategori) {
            kladd.inntektskategori = inntektskategori;
            return this;
        }

        public Builder medFastsattBeløpPrÅr(Integer fastsattBeløpPrÅr) {
            kladd.fastsattÅrsbeløp = fastsattBeløpPrÅr;
            return this;
        }

        public Builder medFastsattBeløpPrÅrInklNaturalytelse(Integer fastsattBeløpPrÅrInklNaturalytelse) {
            kladd.fastsattÅrsbeløpInklNaturalytelse = fastsattBeløpPrÅrInklNaturalytelse;
            return this;
        }

        public Builder medFastsattBeløpPrMnd(Integer fastsattBeløpPrMnd) {
            kladd.fastsattBeløp = fastsattBeløpPrMnd;
            return this;
        }

        public FordelFastsatteVerdierDto build() {
            return kladd;
        }

    }
}
