package no.nav.folketrygdloven.kalkulator.refusjon;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.folketrygdloven.beregningsgrunnlag.perioder.FastsettPeriodeRegel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeModell;
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
        boolean harTilkommetRefkravSomMåVurderes = BeregningRefusjonTjeneste.måVurdereRefusjonskravForBeregning(bgMedRef, originaltGrunnlag);
        List<BeregningAksjonspunktResultat> aksjonspunkter = new ArrayList<>();
        if (harTilkommetRefkravSomMåVurderes) {
            aksjonspunkter.add(BeregningAksjonspunktResultat.opprettFor(BeregningAksjonspunktDefinisjon.VURDER_REFUSJONSKRAV));
        }
        return aksjonspunkter;
    }

    private BeregningsgrunnlagDto hentRefusjonForBG(BeregningsgrunnlagInput input) {
        PeriodeModell modell = mapFastsettBeregningsgrunnlagPerioderFraVLTilRegelRefusjon.map(input, input.getBeregningsgrunnlag());
        List<SplittetPeriode> splittedePerioder = FastsettPeriodeRegel.fastsett(modell);
        return oversetterFraRegelRefusjonsOgGradering.mapFraRegel(splittedePerioder, null, input.getBeregningsgrunnlag());
    }

}
