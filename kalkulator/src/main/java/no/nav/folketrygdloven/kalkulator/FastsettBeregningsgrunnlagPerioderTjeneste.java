package no.nav.folketrygdloven.kalkulator;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLNaturalytelse;
import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLRefusjonOgGradering;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelNaturalYtelse;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelRefusjonOgGradering;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.beregningsgrunnlag.perioder.FastsettPeriodeRegel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeModell;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SplittetPeriode;


@ApplicationScoped
public class FastsettBeregningsgrunnlagPerioderTjeneste {
    public static final int MÅNEDER_I_1_ÅR = 12;
    private static final Logger logger = LoggerFactory.getLogger(FastsettBeregningsgrunnlagPerioderTjeneste.class);

    private MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelNaturalYtelse oversetterTilRegelNaturalytelse;
    private Instance<MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelRefusjonOgGradering> oversetterTilRegelRefusjonOgGradering;
    private MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLNaturalytelse oversetterFraRegelNaturalytelse;
    private MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLRefusjonOgGradering oversetterFraRegelRefusjonsOgGradering;

    FastsettBeregningsgrunnlagPerioderTjeneste() {
        // For CDI
    }

    @Inject
    public FastsettBeregningsgrunnlagPerioderTjeneste(MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelNaturalYtelse oversetterTilRegelNaturalytelse,
                                                          @Any Instance<MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelRefusjonOgGradering> oversetterTilRegelRefusjonOgGradering,
                                                          MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLNaturalytelse oversetterFraRegelNaturalytelse,
                                                          MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLRefusjonOgGradering oversetterFraRegelRefusjonsOgGradering) {
        this.oversetterTilRegelNaturalytelse = oversetterTilRegelNaturalytelse;
        this.oversetterTilRegelRefusjonOgGradering = oversetterTilRegelRefusjonOgGradering;
        this.oversetterFraRegelNaturalytelse = oversetterFraRegelNaturalytelse;
        this.oversetterFraRegelRefusjonsOgGradering = oversetterFraRegelRefusjonsOgGradering;
    }


    public BeregningsgrunnlagDto fastsettPerioderForNaturalytelse(BeregningsgrunnlagInput input,
                                                                  BeregningsgrunnlagDto beregningsgrunnlag) {
        PeriodeModell periodeModell = oversetterTilRegelNaturalytelse.map(input, beregningsgrunnlag);
        return kjørRegelOgMapTilVLNaturalytelse(beregningsgrunnlag, periodeModell);
    }


    public BeregningsgrunnlagDto fastsettPerioderForRefusjonOgGradering(BeregningsgrunnlagInput input,
                                                                        BeregningsgrunnlagDto beregningsgrunnlag) {
        var ref = input.getBehandlingReferanse();
        var mapper = FagsakYtelseTypeRef.Lookup.find(oversetterTilRegelRefusjonOgGradering, ref.getFagsakYtelseType())
            .orElseThrow(() -> new IllegalStateException("Finner ikke implementasjon for håndtering av refusjon/gradering for BehandlingReferanse " + ref));

        PeriodeModell periodeModell = mapper.map(input, beregningsgrunnlag);
        return kjørRegelOgMapTilVLRefusjonOgGradering(beregningsgrunnlag, periodeModell);
    }

    private BeregningsgrunnlagDto kjørRegelOgMapTilVLNaturalytelse(BeregningsgrunnlagDto beregningsgrunnlag, PeriodeModell input) {
        String regelInput = toJson(input);
        logger.info("Regelinput for splitt perioder naturalytelse: " + regelInput);
        List<SplittetPeriode> splittedePerioder = FastsettPeriodeRegel.fastsett(input);
        return oversetterFraRegelNaturalytelse.mapFraRegel(splittedePerioder, regelInput, beregningsgrunnlag);
    }

    private BeregningsgrunnlagDto kjørRegelOgMapTilVLRefusjonOgGradering(BeregningsgrunnlagDto beregningsgrunnlag, PeriodeModell input) {
        String regelInput = toJson(input);
        logger.info("Regelinput for splitt perioder refusjon og gradering: " + regelInput);
        List<SplittetPeriode> splittedePerioder = FastsettPeriodeRegel.fastsett(input);
        return oversetterFraRegelRefusjonsOgGradering.mapFraRegel(splittedePerioder, regelInput, beregningsgrunnlag);
    }

    private String toJson(Object o) {
        return JacksonJsonConfig.toJson(o, BeregningsgrunnlagFeil.FEILFACTORY::kanIkkeSerialisereRegelinput);
    }
}
