package no.nav.folketrygdloven.kalkulator.steg.fordeling.periodisering;

import static no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapRegelSporingFraRegelTilVL.mapRegelSporingGrunnlag;

import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.gradering.PeriodeModellGradering;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.refusjon.PeriodeModellRefusjon;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.utbetalingsgrad.PeriodeModellUtbetalingsgrad;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SplittetPeriode;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.KonfigurasjonVerdi;
import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLGraderingOgUtbetalingsgrad;
import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLRefusjon;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.gradering.MapPerioderForGraderingFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.refusjon.MapRefusjonPerioderFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.output.RegelSporingAggregat;
import no.nav.folketrygdloven.kalkulator.ytelse.utbgradytelse.MapPerioderForAktivitetsgradFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.ytelse.utbgradytelse.MapPerioderForUtbetalingsgradFraVLTilRegel;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagRegelType;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.regelmodelloversetter.KalkulusRegler;


/**
 * Splitter periode ved endring i refusjon, gradering og utbetalingsgrad
 * <p>
 * Sette refusjon på andeler med gyldig refusjon
 */
@ApplicationScoped
public class FordelPerioderTjeneste {
    private Instance<MapRefusjonPerioderFraVLTilRegel> oversetterTilRegelRefusjon;
    private final MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLGraderingOgUtbetalingsgrad oversetterFraRegelGraderingOgUtbetalingsgrad = new MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLGraderingOgUtbetalingsgrad();
    private final MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLRefusjon oversetterFraRegelRefusjon = new MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLRefusjon();

    FordelPerioderTjeneste() {
        // For CDI
    }

    @Inject
    public FordelPerioderTjeneste(@Any Instance<MapRefusjonPerioderFraVLTilRegel> oversetterTilRegelRefusjon) {
        this.oversetterTilRegelRefusjon = oversetterTilRegelRefusjon;
    }


    public BeregningsgrunnlagRegelResultat fastsettPerioderForRefusjon(BeregningsgrunnlagInput input) {
        var beregningsgrunnlag = input.getBeregningsgrunnlag();
        var ref = input.getKoblingReferanse();
        var mapperForYtelse = FagsakYtelseTypeRef.Lookup.find(oversetterTilRegelRefusjon, ref.getFagsakYtelseType());

        return mapperForYtelse.map(mapper -> {
                    PeriodeModellRefusjon periodeModell = mapper.map(input, beregningsgrunnlag);
                    return kjørRegelOgMapTilVLRefusjon(beregningsgrunnlag, periodeModell);
                }
        ).orElse(new BeregningsgrunnlagRegelResultat(beregningsgrunnlag, List.of()));
    }

    public BeregningsgrunnlagRegelResultat fastsettPerioderForUtbetalingsgradEllerGradering(BeregningsgrunnlagInput input,
                                                                                            BeregningsgrunnlagDto beregningsgrunnlag) {
        if (input.getFagsakYtelseType().equals(FagsakYtelseType.FORELDREPENGER)) {
            return fastsettPerioderForGradering(input, beregningsgrunnlag);
        } else {
            var regelResultatUtbetalingsgrad = fastsettPerioderForUtbetalingsgrad(input, beregningsgrunnlag);
            if (KonfigurasjonVerdi.get("GRADERING_MOT_INNTEKT", false)) {
                var regelResultatAktivitetsgrad = fastsettPerioderForAktivitetsgrad(input, regelResultatUtbetalingsgrad.getBeregningsgrunnlag());
                return new BeregningsgrunnlagRegelResultat(
                        regelResultatAktivitetsgrad.getBeregningsgrunnlag(),
                        RegelSporingAggregat.konkatiner(regelResultatUtbetalingsgrad.getRegelsporinger().orElse(null), regelResultatAktivitetsgrad.getRegelsporinger().orElse(null))
                );
            }
            return regelResultatUtbetalingsgrad;
        }
    }

    private BeregningsgrunnlagRegelResultat fastsettPerioderForUtbetalingsgrad(BeregningsgrunnlagInput input,
                                                                               BeregningsgrunnlagDto beregningsgrunnlag) {
        var periodeModell = new MapPerioderForUtbetalingsgradFraVLTilRegel().map(input, beregningsgrunnlag);
        return kjørRegelOgMapTilVLUtbetalingsgrad(beregningsgrunnlag, periodeModell, BeregningsgrunnlagRegelType.PERIODISERING_UTBETALINGSGRAD);
    }

    private BeregningsgrunnlagRegelResultat fastsettPerioderForAktivitetsgrad(BeregningsgrunnlagInput input,
                                                                              BeregningsgrunnlagDto beregningsgrunnlag) {
        var periodeModell = new MapPerioderForAktivitetsgradFraVLTilRegel().map(input, beregningsgrunnlag);
        return kjørRegelOgMapTilVLUtbetalingsgrad(beregningsgrunnlag, periodeModell, BeregningsgrunnlagRegelType.PERIODISERING_AKTIVITETSGRAD);
    }

    private BeregningsgrunnlagRegelResultat fastsettPerioderForGradering(BeregningsgrunnlagInput input,
                                                                         BeregningsgrunnlagDto beregningsgrunnlag) {
        var periodeModell = MapPerioderForGraderingFraVLTilRegel.map(input, beregningsgrunnlag);
        return kjørRegelOgMapTilVLGradering(beregningsgrunnlag, periodeModell);
    }

    private BeregningsgrunnlagRegelResultat kjørRegelOgMapTilVLRefusjon(BeregningsgrunnlagDto beregningsgrunnlag,
                                                                        PeriodeModellRefusjon input) {
        List<SplittetPeriode> splittedePerioder = new ArrayList<>();
        RegelResultat regelResultat = KalkulusRegler.fastsettPerioderRefusjon(input, splittedePerioder);
        var nyttBeregningsgrunnlag = oversetterFraRegelRefusjon.mapFraRegel(splittedePerioder, beregningsgrunnlag);
        return new BeregningsgrunnlagRegelResultat(
                nyttBeregningsgrunnlag,
                new RegelSporingAggregat(mapRegelSporingGrunnlag(regelResultat, BeregningsgrunnlagRegelType.PERIODISERING_REFUSJON)));
    }


    private BeregningsgrunnlagRegelResultat kjørRegelOgMapTilVLGradering(BeregningsgrunnlagDto beregningsgrunnlag,
                                                                         PeriodeModellGradering input) {
        List<SplittetPeriode> splittedePerioder = new ArrayList<>();
        RegelResultat regelResultat = KalkulusRegler.fastsettPerioderGradering(input, splittedePerioder);
        var nyttBeregningsgrunnlag = oversetterFraRegelGraderingOgUtbetalingsgrad.mapFraRegel(splittedePerioder, beregningsgrunnlag);
        return new BeregningsgrunnlagRegelResultat(
                nyttBeregningsgrunnlag,
                new RegelSporingAggregat(mapRegelSporingGrunnlag(regelResultat, BeregningsgrunnlagRegelType.PERIODISERING_GRADERING)));
    }

    private BeregningsgrunnlagRegelResultat kjørRegelOgMapTilVLUtbetalingsgrad(BeregningsgrunnlagDto beregningsgrunnlag,
                                                                               PeriodeModellUtbetalingsgrad input, BeregningsgrunnlagRegelType regeltype) {
        List<SplittetPeriode> splittedePerioder = new ArrayList<>();
        RegelResultat regelResultat = KalkulusRegler.fastsettPerioderForUtbetalingsgrad(input, splittedePerioder);
        var nyttBeregningsgrunnlag = oversetterFraRegelGraderingOgUtbetalingsgrad.mapFraRegel(splittedePerioder, beregningsgrunnlag);
        return new BeregningsgrunnlagRegelResultat(
                nyttBeregningsgrunnlag,
                new RegelSporingAggregat(mapRegelSporingGrunnlag(regelResultat, regeltype)));
    }
}
