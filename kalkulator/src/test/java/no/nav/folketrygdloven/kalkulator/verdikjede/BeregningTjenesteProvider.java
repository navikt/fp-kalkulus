package no.nav.folketrygdloven.kalkulator.verdikjede;

import no.nav.folketrygdloven.kalkulator.AksjonspunktUtlederFaktaOmBeregning;
import no.nav.folketrygdloven.kalkulator.FastsettBeregningsgrunnlagPerioderTjeneste;
import no.nav.folketrygdloven.kalkulator.FordelBeregningsgrunnlagTjeneste;
import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLNaturalytelse;
import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLRefusjonOgGradering;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelNaturalYtelse;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelRefusjonOgGradering;
import no.nav.folketrygdloven.kalkulator.kontrollerfakta.FaktaOmBeregningTilfelleTjeneste;
import no.nav.folketrygdloven.kalkulator.kontrollerfakta.TilfelleUtlederMockTjeneste;
import no.nav.folketrygdloven.kalkulator.ytelse.fp.FullføreBeregningsgrunnlagFPImpl;
import no.nav.vedtak.felles.testutilities.cdi.UnitTestLookupInstanceImpl;

class BeregningTjenesteProvider {

    static BeregningTjenesteWrapper provide() {
        FaktaOmBeregningTilfelleTjeneste faktaOmBeregningTilfelleTjeneste = new FaktaOmBeregningTilfelleTjeneste(
            TilfelleUtlederMockTjeneste.getUtlederInstances());
        AksjonspunktUtlederFaktaOmBeregning aksjonspunktUtlederFaktaOmBeregning = new AksjonspunktUtlederFaktaOmBeregning(faktaOmBeregningTilfelleTjeneste);
        var fullføreBeregningsgrunnlagTjeneste = new FullføreBeregningsgrunnlagFPImpl();

        MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelNaturalYtelse oversetterTilRegelNaturalytelse = new MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelNaturalYtelse();
        MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelRefusjonOgGradering oversetterTilRegelRefusjonOgGradering = new MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelRefusjonOgGradering();
        MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLNaturalytelse oversetterFraRegelNaturalytelse = new MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLNaturalytelse();
        MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLRefusjonOgGradering oversetterFraRegelRefusjonOgGradering = new MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLRefusjonOgGradering();
        var fastsettBeregningsgrunnlagPerioderTjeneste = new FastsettBeregningsgrunnlagPerioderTjeneste(oversetterTilRegelNaturalytelse, new UnitTestLookupInstanceImpl<>(oversetterTilRegelRefusjonOgGradering), oversetterFraRegelNaturalytelse, oversetterFraRegelRefusjonOgGradering);
        var fordelBeregningsgrunnlagTjeneste = new FordelBeregningsgrunnlagTjeneste(fastsettBeregningsgrunnlagPerioderTjeneste);

        return new BeregningTjenesteWrapper(fullføreBeregningsgrunnlagTjeneste, fordelBeregningsgrunnlagTjeneste, aksjonspunktUtlederFaktaOmBeregning, fastsettBeregningsgrunnlagPerioderTjeneste);
    }

}
