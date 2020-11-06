package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.ForeldrepengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.FaktaOmBeregningTilfelle;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.AndelKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.AndelMedBeløpDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.FaktaOmBeregningDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.KunYtelseDto;
import no.nav.folketrygdloven.kalkulator.guitjenester.BeregningsgrunnlagDtoUtil;

@ApplicationScoped
public class KunYtelseDtoTjeneste implements FaktaOmBeregningTilfelleDtoTjeneste {

    KunYtelseDtoTjeneste() {
        // For CDI
    }


    @Override
    public void lagDto(BeregningsgrunnlagGUIInput input,
                       FaktaOmBeregningDto faktaOmBeregningDto) {
        BeregningsgrunnlagDto beregningsgrunnlag = input.getBeregningsgrunnlag();
        if (beregningsgrunnlag.getFaktaOmBeregningTilfeller().contains(FaktaOmBeregningTilfelle.FASTSETT_BG_KUN_YTELSE)) {
            faktaOmBeregningDto.setKunYtelse(lagKunYtelseDto(input));
        }
    }

    KunYtelseDto lagKunYtelseDto(BeregningsgrunnlagGUIInput input) {
        KunYtelseDto dto = new KunYtelseDto();

        dto.setErBesteberegning(harBesteberegning(input.getBeregningsgrunnlag(), input.getBeregningsgrunnlagGrunnlag().getBeregningsgrunnlagTilstand()));
        settVerdier(dto, input.getBeregningsgrunnlag(), input.getIayGrunnlag());
        if (input.getYtelsespesifiktGrunnlag() instanceof ForeldrepengerGrunnlag) {
            dto.setFodendeKvinneMedDP(((ForeldrepengerGrunnlag) input.getYtelsespesifiktGrunnlag()).isKvalifisererTilBesteberegning());
        }
        return dto;
    }

    private Boolean harBesteberegning(BeregningsgrunnlagDto beregningsgrunnlag, BeregningsgrunnlagTilstand aktivTilstand) {
        if (aktivTilstand.erFør(BeregningsgrunnlagTilstand.KOFAKBER_UT)) {
            return null;
        }
        return beregningsgrunnlag.getBeregningsgrunnlagPerioder().stream()
            .flatMap(periode -> periode.getBeregningsgrunnlagPrStatusOgAndelList().stream()).anyMatch(andel -> andel.getBesteberegningPrÅr() != null);
    }

    private void settVerdier(KunYtelseDto dto, BeregningsgrunnlagDto beregningsgrunnlag, InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag) {
        BeregningsgrunnlagPeriodeDto periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        periode.getBeregningsgrunnlagPrStatusOgAndelList().forEach(andel -> {
            AndelMedBeløpDto brukersAndel = initialiserStandardAndelProperties(andel, inntektArbeidYtelseGrunnlag);
            brukersAndel.setFastsattBelopPrMnd(finnFastsattMånedsbeløp(andel));
            dto.leggTilAndel(brukersAndel);
        });
    }

    private BigDecimal finnFastsattMånedsbeløp(BeregningsgrunnlagPrStatusOgAndelDto andel) {
        return andel.getBeregnetPrÅr() != null ?
            andel.getBeregnetPrÅr().divide(BigDecimal.valueOf(12), RoundingMode.HALF_UP) : null;
    }

    private AndelMedBeløpDto initialiserStandardAndelProperties(BeregningsgrunnlagPrStatusOgAndelDto andel, InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag) {
        AndelMedBeløpDto andelDto = new AndelMedBeløpDto();
        andelDto.setAndelsnr(andel.getAndelsnr());
        BeregningsgrunnlagDtoUtil.lagArbeidsforholdDto(andel, Optional.empty(), inntektArbeidYtelseGrunnlag)
                .ifPresent(andelDto::setArbeidsforhold);
        andelDto.setLagtTilAvSaksbehandler(andel.erLagtTilAvSaksbehandler());
        andelDto.setKilde(new AndelKilde(andel.getKilde().getKode()));
        andelDto.setFastsattAvSaksbehandler(Boolean.TRUE.equals(andel.getFastsattAvSaksbehandler()));
        andelDto.setAktivitetStatus(new AktivitetStatus(andel.getAktivitetStatus().getKode()));
        andelDto.setInntektskategori(new Inntektskategori(andel.getInntektskategori().getKode()));
        return andelDto;
    }

}
