package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import no.nav.folketrygdloven.kalkulator.fordeling.FordelBeregningsgrunnlagTilfelleInput;
import no.nav.folketrygdloven.kalkulator.fordeling.FordelBeregningsgrunnlagTilfelleTjeneste;
import no.nav.folketrygdloven.kalkulator.fordeling.FordelingTilfelle;
import no.nav.folketrygdloven.kalkulator.gradering.AktivitetGradering;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;

public class ManuellBehandlingRefusjonGraderingDtoTjeneste {

    private ManuellBehandlingRefusjonGraderingDtoTjeneste() {
        // Skjul
    }

    public static boolean skalSaksbehandlerRedigereInntekt(BeregningsgrunnlagGrunnlagDto grunnlag,
                                                           AktivitetGradering aktivitetGradering,
                                                           BeregningsgrunnlagPeriodeDto periode,
                                                           List<BeregningsgrunnlagPeriodeDto> perioder,
                                                           Collection<InntektsmeldingDto> inntektsmeldinger) {
        boolean grunnetTidligerePerioder = skalRedigereGrunnetTidligerePerioder(grunnlag, aktivitetGradering, periode, perioder, inntektsmeldinger);
        if (grunnetTidligerePerioder) {
            return true;
        }
        Map<BeregningsgrunnlagPrStatusOgAndelDto, FordelingTilfelle> periodeTilfelleMap = utledTilfellerForAndelerIPeriode(grunnlag, aktivitetGradering, periode, inntektsmeldinger);
        return periode.getBeregningsgrunnlagPrStatusOgAndelList().stream().anyMatch(andelFraSteg -> andelLiggerITilfelleMap(andelFraSteg, periodeTilfelleMap));
    }

    private static boolean skalRedigereGrunnetTidligerePerioder(BeregningsgrunnlagGrunnlagDto grunnlag, AktivitetGradering aktivitetGradering, BeregningsgrunnlagPeriodeDto periode, List<BeregningsgrunnlagPeriodeDto> perioder, Collection<InntektsmeldingDto> inntektsmeldinger) {
        return perioder.stream()
                .filter(p -> p.getBeregningsgrunnlagPeriodeFom().isBefore(periode.getBeregningsgrunnlagPeriodeFom()))
                .flatMap(p -> utledTilfellerForAndelerIPeriode(grunnlag, aktivitetGradering, p, inntektsmeldinger).values().stream())
                .anyMatch(tilfelle -> tilfelle.equals(FordelingTilfelle.GRADERT_ANDEL_SOM_VILLE_HA_BLITT_AVKORTET_TIL_0) || tilfelle.equals(FordelingTilfelle.FORESLÅTT_BG_PÅ_GRADERT_ANDEL_ER_0));
    }

    public static boolean skalSaksbehandlerRedigereRefusjon(BeregningsgrunnlagGrunnlagDto grunnlag,
                                                            AktivitetGradering aktivitetGradering,
                                                            BeregningsgrunnlagPeriodeDto periode,
                                                            Collection<InntektsmeldingDto> inntektsmeldinger,
                                                            Beløp grunnbeløp) {
        Map<BeregningsgrunnlagPrStatusOgAndelDto, FordelingTilfelle> periodeTilfelleMap = utledTilfellerForAndelerIPeriode(grunnlag, aktivitetGradering, periode, inntektsmeldinger);
        return periode.getBeregningsgrunnlagPrStatusOgAndelList().stream().anyMatch(andelFraSteg -> andelLiggerITilfelleMap(andelFraSteg, periodeTilfelleMap)
                && RefusjonDtoTjeneste.skalKunneEndreRefusjon(andelFraSteg, periode, aktivitetGradering, inntektsmeldinger, grunnbeløp));
    }

    private static Map<BeregningsgrunnlagPrStatusOgAndelDto, FordelingTilfelle> utledTilfellerForAndelerIPeriode(BeregningsgrunnlagGrunnlagDto grunnlag,
                                                                                                                 AktivitetGradering aktivitetGradering,
                                                                                                                 BeregningsgrunnlagPeriodeDto periode,
                                                                                                                 Collection<InntektsmeldingDto> inntektsmeldinger) {
        var beregningAktivitetAggregat = grunnlag.getGjeldendeAktiviteter();
        FordelBeregningsgrunnlagTilfelleInput fordelingInput = new FordelBeregningsgrunnlagTilfelleInput(grunnlag.getBeregningsgrunnlag().orElse(null),
                beregningAktivitetAggregat, aktivitetGradering, inntektsmeldinger);
        return FordelBeregningsgrunnlagTilfelleTjeneste.vurderManuellBehandlingForPeriode(periode, fordelingInput);
    }

    private static boolean andelLiggerITilfelleMap(BeregningsgrunnlagPrStatusOgAndelDto andelFraSteg, Map<BeregningsgrunnlagPrStatusOgAndelDto, FordelingTilfelle> periodeTilfelleMap) {
        return periodeTilfelleMap.keySet().stream().anyMatch(key -> Objects.equals(key.getAndelsnr(), andelFraSteg.getAndelsnr()));
    }

}
