package no.nav.folketrygdloven.kalkulator.steg.refusjon;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.folketrygdloven.beregningsgrunnlag.perioder.FastsettPeriodeRegel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.PeriodeModell;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SplittetPeriode;
import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLRefusjonOgGradering;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelRefusjon;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.modell.RefusjonAndel;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand;

/**
 * Tjeneste for å finne andeler i nytt beregningsgrunnlag som har økt refusjon siden orginalbehandlingen.
 */
@ApplicationScoped
public class AndelerMedØktRefusjonTjeneste {

    public static final RegelResultat DUMMY_REGEL_RESULTAT = new RegelResultat(null, "", "");
    private MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelRefusjon mapFastsettBeregningsgrunnlagPerioderFraVLTilRegelRefusjon;
    private MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLRefusjonOgGradering oversetterFraRegelRefusjonsOgGradering;

    @Inject
    public AndelerMedØktRefusjonTjeneste(MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelRefusjon mapFastsettBeregningsgrunnlagPerioderFraVLTilRegelRefusjon,
                                         MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLRefusjonOgGradering oversetterFraRegelRefusjonsOgGradering) {
        this.mapFastsettBeregningsgrunnlagPerioderFraVLTilRegelRefusjon = mapFastsettBeregningsgrunnlagPerioderFraVLTilRegelRefusjon;
        this.oversetterFraRegelRefusjonsOgGradering = oversetterFraRegelRefusjonsOgGradering;
    }

    public AndelerMedØktRefusjonTjeneste() {
        // CDI
    }

    public Map<Intervall, List<RefusjonAndel>> finnAndelerMedØktRefusjon(BeregningsgrunnlagInput input) {
        if (input.getBeregningsgrunnlagGrunnlag() == null || input.getBeregningsgrunnlagGrunnlag().getBeregningsgrunnlagTilstand().erFør(BeregningsgrunnlagTilstand.FORESLÅTT)) {
            // Tjenesten er kalt før vi er klar for å vurdere refusjon, returnere tomt map
            return Collections.emptyMap();
        }
        BeregningsgrunnlagDto bgMedRef = hentRefusjonForBG(input);
        Optional<BeregningsgrunnlagDto> originaltGrunnlag = input.getBeregningsgrunnlagGrunnlagFraForrigeBehandling().flatMap(BeregningsgrunnlagGrunnlagDto::getBeregningsgrunnlag);
        LocalDate alleredeUtbetaltTOM = FinnAlleredeUtbetaltTom.finn();
        return originaltGrunnlag.map(og -> BeregningRefusjonTjeneste.finnUtbetaltePerioderMedAndelerMedØktRefusjon(bgMedRef, og, alleredeUtbetaltTOM)).orElse(Collections.emptyMap());
    }

    private BeregningsgrunnlagDto hentRefusjonForBG(BeregningsgrunnlagInput input) {
        PeriodeModell modell = mapFastsettBeregningsgrunnlagPerioderFraVLTilRegelRefusjon.map(input, input.getBeregningsgrunnlag());
        List<SplittetPeriode> splittedePerioder = kjørRegel(modell);
        return oversetterFraRegelRefusjonsOgGradering.mapFraRegel(splittedePerioder, DUMMY_REGEL_RESULTAT, input.getBeregningsgrunnlag());
    }

    private List<SplittetPeriode> kjørRegel(PeriodeModell modell) {
        List<SplittetPeriode> splittedePerioder = new ArrayList<>();
        new FastsettPeriodeRegel().evaluer(modell, splittedePerioder);
        return splittedePerioder;
    }

}
