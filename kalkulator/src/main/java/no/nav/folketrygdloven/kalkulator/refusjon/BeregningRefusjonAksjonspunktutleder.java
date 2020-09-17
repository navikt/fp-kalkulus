package no.nav.folketrygdloven.kalkulator.refusjon;


import java.util.ArrayList;
import java.util.List;
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
import no.nav.folketrygdloven.kalkulator.output.BeregningAksjonspunktResultat;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningAksjonspunktDefinisjon;

/**
 * Tjeneste for å utlede aksjonspunkter i steg for å vurdere refusjonskrav
 */
@ApplicationScoped
public class BeregningRefusjonAksjonspunktutleder {

    public static final RegelResultat DUMMY_REGEL_RESULTAT = new RegelResultat(null, "", "");
    private MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelRefusjon mapFastsettBeregningsgrunnlagPerioderFraVLTilRegelRefusjon;
    private MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLRefusjonOgGradering oversetterFraRegelRefusjonsOgGradering;

    @Inject
    public BeregningRefusjonAksjonspunktutleder(MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelRefusjon mapFastsettBeregningsgrunnlagPerioderFraVLTilRegelRefusjon,
                                                MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLRefusjonOgGradering oversetterFraRegelRefusjonsOgGradering) {
        this.mapFastsettBeregningsgrunnlagPerioderFraVLTilRegelRefusjon = mapFastsettBeregningsgrunnlagPerioderFraVLTilRegelRefusjon;
        this.oversetterFraRegelRefusjonsOgGradering = oversetterFraRegelRefusjonsOgGradering;
    }

    public BeregningRefusjonAksjonspunktutleder() {
        // CDI
    }

    public List<BeregningAksjonspunktResultat> utledAksjonspunkter(BeregningsgrunnlagInput input) {
        BeregningsgrunnlagDto bgMedRef = hentRefusjonForBG(input);
        Optional<BeregningsgrunnlagDto> originaltGrunnlag = input.getBeregningsgrunnlagGrunnlagFraForrigeBehandling().flatMap(BeregningsgrunnlagGrunnlagDto::getBeregningsgrunnlag);
        boolean harTilkommetRefkravSomMåVurderes = originaltGrunnlag.map(og -> BeregningRefusjonTjeneste.måVurdereRefusjonskravForBeregning(bgMedRef, og)).orElse(false);
        List<BeregningAksjonspunktResultat> aksjonspunkter = new ArrayList<>();
        if (harTilkommetRefkravSomMåVurderes) {
            aksjonspunkter.add(BeregningAksjonspunktResultat.opprettFor(BeregningAksjonspunktDefinisjon.VURDER_REFUSJONSKRAV));
        }
        return aksjonspunkter;
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
