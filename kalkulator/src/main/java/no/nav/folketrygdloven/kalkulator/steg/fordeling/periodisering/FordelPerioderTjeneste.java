package no.nav.folketrygdloven.kalkulator.steg.fordeling.periodisering;

import static no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapRegelSporingFraRegelTilVL.mapRegelSporingGrunnlag;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import no.nav.folketrygdloven.beregningsgrunnlag.RegelmodellOversetter;
import no.nav.folketrygdloven.beregningsgrunnlag.perioder.FastsettPeriodeRegel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.PeriodeModell;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SplittetPeriode;
import no.nav.folketrygdloven.kalkulator.BeregningsgrunnlagFeil;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.JsonMapper;
import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLRefusjonOgGradering;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelRefusjonOgGradering;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.output.RegelSporingAggregat;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagRegelType;
import no.nav.fpsak.nare.evaluation.Evaluation;


/**
 * Splitter periode ved endring i refusjon, gradering og utbetalingsgrad
 *
 * Sette refusjon på andeler med gyldig refusjon
 */
@ApplicationScoped
public class FordelPerioderTjeneste {
    private Instance<MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelRefusjonOgGradering> oversetterTilRegelRefusjonOgGradering;
    private MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLRefusjonOgGradering oversetterFraRegelRefusjonsOgGradering;

    FordelPerioderTjeneste() {
        // For CDI
    }

    @Inject
    public FordelPerioderTjeneste(@Any Instance<MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelRefusjonOgGradering> oversetterTilRegelRefusjonOgGradering,
                                  MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLRefusjonOgGradering oversetterFraRegelRefusjonsOgGradering) {
        this.oversetterTilRegelRefusjonOgGradering = oversetterTilRegelRefusjonOgGradering;
        this.oversetterFraRegelRefusjonsOgGradering = oversetterFraRegelRefusjonsOgGradering;
    }

    public BeregningsgrunnlagRegelResultat fastsettPerioderForRefusjonOgGradering(BeregningsgrunnlagInput input,
                                                                                  BeregningsgrunnlagDto beregningsgrunnlag) {
        var ref = input.getKoblingReferanse();
        var mapper = FagsakYtelseTypeRef.Lookup.find(oversetterTilRegelRefusjonOgGradering, ref.getFagsakYtelseType())
                .orElseThrow(() -> new IllegalStateException("Finner ikke implementasjon for håndtering av refusjon/gradering for BehandlingReferanse " + ref));

        PeriodeModell periodeModell = mapper.map(input, beregningsgrunnlag);
        return kjørRegelOgMapTilVLRefusjonOgGradering(beregningsgrunnlag, periodeModell);
    }


    private BeregningsgrunnlagRegelResultat kjørRegelOgMapTilVLRefusjonOgGradering(BeregningsgrunnlagDto beregningsgrunnlag, PeriodeModell input) {
        String regelInput = toJson(input);
        List<SplittetPeriode> splittedePerioder = new ArrayList<>();
        Evaluation evaluation = new FastsettPeriodeRegel().evaluer(input, splittedePerioder);
        RegelResultat regelResultat = RegelmodellOversetter.getRegelResultat(evaluation, regelInput);
        var nyttBeregningsgrunnlag = oversetterFraRegelRefusjonsOgGradering.mapFraRegel(splittedePerioder, beregningsgrunnlag);
        return new BeregningsgrunnlagRegelResultat(
                nyttBeregningsgrunnlag,
                new RegelSporingAggregat(mapRegelSporingGrunnlag(regelResultat, BeregningsgrunnlagRegelType.PERIODISERING_REFUSJON)));
    }

    private String toJson(Object o) {
        return JsonMapper.toJson(o, BeregningsgrunnlagFeil.FEILFACTORY::kanIkkeSerialisereRegelinput);
    }
}
