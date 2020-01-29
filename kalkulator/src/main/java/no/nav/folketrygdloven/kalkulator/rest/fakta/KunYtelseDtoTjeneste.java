package no.nav.folketrygdloven.kalkulator.rest.fakta;

import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.ForeldrepengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagRestInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagRestDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.rest.dto.AndelMedBeløpDto;
import no.nav.folketrygdloven.kalkulator.rest.dto.FaktaOmBeregningDto;
import no.nav.folketrygdloven.kalkulator.rest.dto.KunYtelseDto;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.FaktaOmBeregningTilfelle;

@ApplicationScoped
public class KunYtelseDtoTjeneste implements FaktaOmBeregningTilfelleDtoTjeneste {

    KunYtelseDtoTjeneste() {
        // For CDI
    }


    @Override
    public void lagDto(BeregningsgrunnlagRestInput input,
                       FaktaOmBeregningDto faktaOmBeregningDto) {
        BeregningsgrunnlagRestDto beregningsgrunnlag = input.getBeregningsgrunnlag();
        if (beregningsgrunnlag.getFaktaOmBeregningTilfeller().contains(FaktaOmBeregningTilfelle.FASTSETT_BG_KUN_YTELSE)) {
            faktaOmBeregningDto.setKunYtelse(lagKunYtelseDto(input));
        }
    }

    KunYtelseDto lagKunYtelseDto(BeregningsgrunnlagRestInput input) {
        var ref = input.getBehandlingReferanse();
        KunYtelseDto dto = new KunYtelseDto();

        dto.setErBesteberegning(harBesteberegning(input.getBeregningsgrunnlag(), input.getBeregningsgrunnlagGrunnlag().getBeregningsgrunnlagTilstand()));
        settVerdier(dto, input.getBeregningsgrunnlag(), input.getIayGrunnlag());
        dto.setFodendeKvinneMedDP(((ForeldrepengerGrunnlag) input.getYtelsespesifiktGrunnlag()).isKvalifisererTilBesteberegning());
        return dto;
    }

    private Boolean harBesteberegning(BeregningsgrunnlagRestDto beregningsgrunnlag, BeregningsgrunnlagTilstand aktivTilstand) {
        if (aktivTilstand.erFør(BeregningsgrunnlagTilstand.KOFAKBER_UT)) {
            return null;
        }
        return beregningsgrunnlag.getBeregningsgrunnlagPerioder().stream()
            .flatMap(periode -> periode.getBeregningsgrunnlagPrStatusOgAndelList().stream()).anyMatch(andel -> andel.getBesteberegningPrÅr() != null);
    }

    private void settVerdier(KunYtelseDto dto, BeregningsgrunnlagRestDto beregningsgrunnlag, InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag) {
        BeregningsgrunnlagPeriodeRestDto periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        periode.getBeregningsgrunnlagPrStatusOgAndelList().forEach(andel -> {
            AndelMedBeløpDto brukersAndel = new AndelMedBeløpDto();
            brukersAndel.initialiserStandardAndelProperties(andel, inntektArbeidYtelseGrunnlag);
            brukersAndel.setFastsattBelopPrMnd(finnFastsattMånedsbeløp(andel));
            dto.leggTilAndel(brukersAndel);
        });
    }

    private BigDecimal finnFastsattMånedsbeløp(BeregningsgrunnlagPrStatusOgAndelRestDto andel) {
        return andel.getBeregnetPrÅr() != null ?
            andel.getBeregnetPrÅr().divide(BigDecimal.valueOf(12), RoundingMode.HALF_UP) : null;
    }
}
