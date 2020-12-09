package no.nav.folketrygdloven.kalkulator.verdikjede;

import no.nav.folketrygdloven.kalkulator.steg.fordeling.omfordeling.OmfordelBeregningsgrunnlagTjeneste;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.periodisering.FordelPerioderTjeneste;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.AksjonspunktUtlederFaktaOmBeregning;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.periodisering.FastsettBeregningsgrunnlagPerioderTjeneste;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.FordelBeregningsgrunnlagTjeneste;
import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLRefusjonOgGradering;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.ForeldrepengerGrunnlagMapper;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapBeregningsgrunnlagFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapInntektsgrunnlagVLTilRegel;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapInntektsgrunnlagVLTilRegelFelles;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.YtelsesspesifikkRegelMapper;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelRefusjonOgGradering;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.FaktaOmBeregningTilfelleTjeneste;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.TilfelleUtlederMockTjeneste;
import no.nav.folketrygdloven.kalkulator.steg.fullføre.ytelse.fp.FullføreBeregningsgrunnlagFPImpl;
import no.nav.folketrygdloven.utils.UnitTestLookupInstanceImpl;

class BeregningTjenesteProvider {

    static BeregningTjenesteWrapper provide() {
        FaktaOmBeregningTilfelleTjeneste faktaOmBeregningTilfelleTjeneste = new FaktaOmBeregningTilfelleTjeneste(
            TilfelleUtlederMockTjeneste.getUtlederInstances());
        AksjonspunktUtlederFaktaOmBeregning aksjonspunktUtlederFaktaOmBeregning = new AksjonspunktUtlederFaktaOmBeregning(faktaOmBeregningTilfelleTjeneste);

        MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelRefusjonOgGradering oversetterTilRegelRefusjonOgGradering = new MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelRefusjonOgGradering();
        MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLRefusjonOgGradering oversetterFraRegelRefusjonOgGradering = new MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLRefusjonOgGradering();
        var fordelPerioderTjeneste = new FordelPerioderTjeneste(new UnitTestLookupInstanceImpl<>(oversetterTilRegelRefusjonOgGradering), oversetterFraRegelRefusjonOgGradering);
        var fastsettBeregningsgrunnlagPerioderTjeneste = new FastsettBeregningsgrunnlagPerioderTjeneste();

        MapInntektsgrunnlagVLTilRegel mapInntektsgrunnlagVLTilRegel = new MapInntektsgrunnlagVLTilRegelFelles();
        UnitTestLookupInstanceImpl<YtelsesspesifikkRegelMapper> ytelsesSpesifikkMapper = new UnitTestLookupInstanceImpl<>(new ForeldrepengerGrunnlagMapper());
        MapBeregningsgrunnlagFraVLTilRegel mapBeregningsgrunnlagFraVLTilRegel = new MapBeregningsgrunnlagFraVLTilRegel(new UnitTestLookupInstanceImpl<>(mapInntektsgrunnlagVLTilRegel), ytelsesSpesifikkMapper);
        var fordelBeregningsgrunnlagTjeneste = new FordelBeregningsgrunnlagTjeneste(fordelPerioderTjeneste, new OmfordelBeregningsgrunnlagTjeneste(mapBeregningsgrunnlagFraVLTilRegel));
        var fullføreBeregningsgrunnlagTjeneste = new FullføreBeregningsgrunnlagFPImpl(mapBeregningsgrunnlagFraVLTilRegel);

        return new BeregningTjenesteWrapper(fullføreBeregningsgrunnlagTjeneste, fordelBeregningsgrunnlagTjeneste, aksjonspunktUtlederFaktaOmBeregning, fastsettBeregningsgrunnlagPerioderTjeneste);
    }

}
