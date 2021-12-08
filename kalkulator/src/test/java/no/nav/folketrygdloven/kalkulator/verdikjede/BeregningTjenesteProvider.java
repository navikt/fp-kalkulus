package no.nav.folketrygdloven.kalkulator.verdikjede;

import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLGraderingOgUtbetalingsgrad;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapBeregningsgrunnlagFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapInntektsgrunnlagVLTilRegel;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapInntektsgrunnlagVLTilRegelFelles;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.YtelsesspesifikkRegelMapper;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.MapPerioderForGraderingOgUtbetalingsgrad;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.ytelse.ForeldrepengerGrunnlagMapper;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.FordelBeregningsgrunnlagTjenesteImpl;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.omfordeling.OmfordelBeregningsgrunnlagTjeneste;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.periodisering.FordelPerioderTjeneste;
import no.nav.folketrygdloven.kalkulator.steg.fullføre.ytelse.fp.FullføreBeregningsgrunnlagFPImpl;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.AvklaringsbehovUtlederFaktaOmBeregning;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.FaktaOmBeregningTilfelleTjeneste;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.TilfelleUtlederMockTjeneste;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.periodisering.FastsettNaturalytelsePerioderTjeneste;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.VurderRefusjonBeregningsgrunnlag;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.ytelse.AvklaringsbehovutledertjenesteVurderRefusjonFP;
import no.nav.folketrygdloven.kalkulator.ytelse.fp.MapRefusjonPerioderFraVLTilRegelFP;
import no.nav.folketrygdloven.utils.UnitTestLookupInstanceImpl;

class BeregningTjenesteProvider {

    static BeregningTjenesteWrapper provide() {
        FaktaOmBeregningTilfelleTjeneste faktaOmBeregningTilfelleTjeneste = new FaktaOmBeregningTilfelleTjeneste(
                TilfelleUtlederMockTjeneste.getUtlederInstances());
        AvklaringsbehovUtlederFaktaOmBeregning avklaringsbehovUtlederFaktaOmBeregning = new AvklaringsbehovUtlederFaktaOmBeregning(faktaOmBeregningTilfelleTjeneste);

        var oversetterTilRegelRefusjonOgGradering = new MapPerioderForGraderingOgUtbetalingsgrad();
        var oversetterTilRegelRefusjon = new MapRefusjonPerioderFraVLTilRegelFP();
        var oversetterFraRegelRefusjonOgGradering = new MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLGraderingOgUtbetalingsgrad();
        var fordelPerioderTjeneste = new FordelPerioderTjeneste(
                new UnitTestLookupInstanceImpl<>(oversetterTilRegelRefusjon)
        );
        var fastsettBeregningsgrunnlagPerioderTjeneste = new FastsettNaturalytelsePerioderTjeneste();

        MapInntektsgrunnlagVLTilRegel mapInntektsgrunnlagVLTilRegel = new MapInntektsgrunnlagVLTilRegelFelles();
        UnitTestLookupInstanceImpl<YtelsesspesifikkRegelMapper> ytelsesSpesifikkMapper = new UnitTestLookupInstanceImpl<>(new ForeldrepengerGrunnlagMapper());
        MapBeregningsgrunnlagFraVLTilRegel mapBeregningsgrunnlagFraVLTilRegel = new MapBeregningsgrunnlagFraVLTilRegel(new UnitTestLookupInstanceImpl<>(mapInntektsgrunnlagVLTilRegel), ytelsesSpesifikkMapper);
        var fordelBeregningsgrunnlagTjeneste = new FordelBeregningsgrunnlagTjenesteImpl(new OmfordelBeregningsgrunnlagTjeneste(mapBeregningsgrunnlagFraVLTilRegel));
        var fullføreBeregningsgrunnlagTjeneste = new FullføreBeregningsgrunnlagFPImpl(mapBeregningsgrunnlagFraVLTilRegel);
        var vurderRefusjonBeregningsgrunnlag = new VurderRefusjonBeregningsgrunnlag(fordelPerioderTjeneste, new UnitTestLookupInstanceImpl<>(new AvklaringsbehovutledertjenesteVurderRefusjonFP()));
        return new BeregningTjenesteWrapper(fullføreBeregningsgrunnlagTjeneste, fordelBeregningsgrunnlagTjeneste, avklaringsbehovUtlederFaktaOmBeregning, fastsettBeregningsgrunnlagPerioderTjeneste, vurderRefusjonBeregningsgrunnlag);
    }

}
