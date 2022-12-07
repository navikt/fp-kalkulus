package no.nav.folketrygdloven.kalkulator.steg.fordeling.ytelse.psb;

import java.util.Collections;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktørArbeidDto;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.tilkommetInntekt.TilkommetInntektsforholdTjeneste;
import no.nav.folketrygdloven.kalkulus.kodeverk.PeriodeÅrsak;

public class TilkommetInntektPeriodeTjeneste {


    public BeregningsgrunnlagDto splittPerioderVedTilkommetInntekt(BeregningsgrunnlagInput input, BeregningsgrunnlagDto beregningsgrunnlag) {

        var tilkommetAktivitetTidslinje = TilkommetInntektsforholdTjeneste.finnTilkommetInntektsforholdTidslinje(
                beregningsgrunnlag.getSkjæringstidspunkt(),
                input.getIayGrunnlag().getAktørArbeidFraRegister().map(AktørArbeidDto::hentAlleYrkesaktiviteter).orElse(Collections.emptyList()),
                beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList(),
                input.getYtelsespesifiktGrunnlag());
        return SplittBGPerioder.splittPerioderOgSettPeriodeårsak(beregningsgrunnlag, input.getForlengelseperioder(), tilkommetAktivitetTidslinje.compress(), PeriodeÅrsak.TILKOMMET_INNTEKT);
    }

}
