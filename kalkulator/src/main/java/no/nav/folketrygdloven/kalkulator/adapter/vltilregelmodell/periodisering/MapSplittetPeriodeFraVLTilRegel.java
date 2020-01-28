package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering;

import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapArbeidsforholdFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.kodeverk.MapPeriodeÅrsakFraVlTilRegel;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SplittetPeriode;

class MapSplittetPeriodeFraVLTilRegel {
    private MapSplittetPeriodeFraVLTilRegel() {
        // skjul public constructor
    }

    static SplittetPeriode map(BeregningsgrunnlagPeriodeDto periode) {
        return SplittetPeriode.builder()
            .medFørstePeriodeAndeler(periode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(a -> a.getBgAndelArbeidsforhold().isPresent())
                .map(MapSplittetPeriodeFraVLTilRegel::mapToBeregningsgrunnlagPrArbeidsforhold).collect(Collectors.toList()))
            .medPeriode(Periode.of(periode.getBeregningsgrunnlagPeriodeFom(), periode.getBeregningsgrunnlagPeriodeTom()))
            .medPeriodeÅrsaker(periode.getPeriodeÅrsaker().stream().map(MapPeriodeÅrsakFraVlTilRegel::map).collect(Collectors.toList()))
            .build();
    }

    private static BeregningsgrunnlagPrArbeidsforhold mapToBeregningsgrunnlagPrArbeidsforhold(BeregningsgrunnlagPrStatusOgAndelDto bgAndel) {
        BGAndelArbeidsforholdDto bgAndelArbeidsforhold = bgAndel.getBgAndelArbeidsforhold()
            .orElseThrow(() -> new IllegalStateException("Må ha arbeidsforhold"));
        BeregningsgrunnlagPrArbeidsforhold.Builder builder = BeregningsgrunnlagPrArbeidsforhold.builder()
            .medAndelNr(bgAndel.getAndelsnr());
        builder.medArbeidsforhold(MapArbeidsforholdFraVLTilRegel.mapArbeidsforhold(bgAndelArbeidsforhold.getArbeidsgiver(), bgAndelArbeidsforhold.getArbeidsforholdRef()));
        return builder.build();
    }
}
