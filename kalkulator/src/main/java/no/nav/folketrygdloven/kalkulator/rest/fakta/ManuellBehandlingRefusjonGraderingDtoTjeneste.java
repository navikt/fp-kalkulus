package no.nav.folketrygdloven.kalkulator.rest.fakta;

import java.util.Collection;
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
                                                           Collection<InntektsmeldingDto> inntektsmeldinger) {
        Map<BeregningsgrunnlagPrStatusOgAndelDto, FordelingTilfelle> periodeTilfelleMap = utledTilfellerForAndelerIPeriode(grunnlag, aktivitetGradering, periode, inntektsmeldinger);
        return periode.getBeregningsgrunnlagPrStatusOgAndelList().stream().anyMatch(andelFraSteg -> andelLiggerITilfelleMap(andelFraSteg, periodeTilfelleMap));
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
