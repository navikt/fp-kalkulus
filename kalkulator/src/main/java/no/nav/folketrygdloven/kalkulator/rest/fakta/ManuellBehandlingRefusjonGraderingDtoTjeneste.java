package no.nav.folketrygdloven.kalkulator.rest.fakta;

import static no.nav.folketrygdloven.kalkulator.rest.MapBeregningsgrunnlagFraRestTilDomene.mapAktivitetAggregat;
import static no.nav.folketrygdloven.kalkulator.rest.MapBeregningsgrunnlagFraRestTilDomene.mapPeriode;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import no.nav.folketrygdloven.kalkulator.fordeling.FordelBeregningsgrunnlagTilfelleInput;
import no.nav.folketrygdloven.kalkulator.fordeling.FordelBeregningsgrunnlagTilfelleTjeneste;
import no.nav.folketrygdloven.kalkulator.fordeling.FordelingTilfelle;
import no.nav.folketrygdloven.kalkulator.gradering.AktivitetGradering;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelRestDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.rest.MapBeregningsgrunnlagFraRestTilDomene;

public class ManuellBehandlingRefusjonGraderingDtoTjeneste {

    private ManuellBehandlingRefusjonGraderingDtoTjeneste() {
        // Skjul
    }

    public static boolean skalSaksbehandlerRedigereInntekt(BeregningAktivitetAggregatRestDto beregningAktivitetAggregat,
                                                           AktivitetGradering aktivitetGradering,
                                                           BeregningsgrunnlagPeriodeRestDto periode,
                                                           Collection<InntektsmeldingDto> inntektsmeldinger) {
        Map<BeregningsgrunnlagPrStatusOgAndelDto, FordelingTilfelle> periodeTilfelleMap = utledTilfellerForAndelerIPeriode(beregningAktivitetAggregat, aktivitetGradering, periode, inntektsmeldinger);
        return periode.getBeregningsgrunnlagPrStatusOgAndelList().stream().anyMatch(andelFraSteg -> andelLiggerITilfelleMap(andelFraSteg, periodeTilfelleMap));
    }

    public static boolean skalSaksbehandlerRedigereRefusjon(BeregningAktivitetAggregatRestDto beregningAktivitetAggregat,
                                                            AktivitetGradering aktivitetGradering,
                                                            BeregningsgrunnlagPeriodeRestDto periode,
                                                            Collection<InntektsmeldingDto> inntektsmeldinger,
                                                            Beløp grunnbeløp) {
        Map<BeregningsgrunnlagPrStatusOgAndelDto, FordelingTilfelle> periodeTilfelleMap = utledTilfellerForAndelerIPeriode(beregningAktivitetAggregat, aktivitetGradering, periode, inntektsmeldinger);
        return periode.getBeregningsgrunnlagPrStatusOgAndelList().stream().anyMatch(andelFraSteg -> andelLiggerITilfelleMap(andelFraSteg, periodeTilfelleMap)
                && RefusjonDtoTjeneste.skalKunneEndreRefusjon(andelFraSteg, periode, aktivitetGradering, inntektsmeldinger, grunnbeløp));
    }

    private static Map<BeregningsgrunnlagPrStatusOgAndelDto, FordelingTilfelle> utledTilfellerForAndelerIPeriode(BeregningAktivitetAggregatRestDto beregningAktivitetAggregat,
                                                                                                                 AktivitetGradering aktivitetGradering,
                                                                                                                 BeregningsgrunnlagPeriodeRestDto periode,
                                                                                                                 Collection<InntektsmeldingDto> inntektsmeldinger) {
        BeregningsgrunnlagDto domeneBG = MapBeregningsgrunnlagFraRestTilDomene.mapBeregningsgrunnlag(periode.getBeregningsgrunnlag());
        BeregningAktivitetAggregatDto domeneAktiviteter = mapAktivitetAggregat(beregningAktivitetAggregat);
        BeregningsgrunnlagPeriodeDto domenePeriode = mapPeriode(periode);
        FordelBeregningsgrunnlagTilfelleInput fordelingInput = new FordelBeregningsgrunnlagTilfelleInput(domeneBG, domeneAktiviteter, aktivitetGradering, inntektsmeldinger);
        return FordelBeregningsgrunnlagTilfelleTjeneste.vurderManuellBehandlingForPeriode(domenePeriode, fordelingInput);
    }

    private static boolean andelLiggerITilfelleMap(BeregningsgrunnlagPrStatusOgAndelRestDto andelFraSteg, Map<BeregningsgrunnlagPrStatusOgAndelDto, FordelingTilfelle> periodeTilfelleMap) {
        return periodeTilfelleMap.keySet().stream().anyMatch(key -> Objects.equals(key.getAndelsnr(), andelFraSteg.getAndelsnr()));
    }

}
