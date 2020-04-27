package no.nav.folketrygdloven.kalkulator.verdikjede;

import no.nav.folketrygdloven.kalkulator.AksjonspunktUtlederFaktaOmBeregning;
import no.nav.folketrygdloven.kalkulator.FastsettBeregningsgrunnlagPerioderTjeneste;
import no.nav.folketrygdloven.kalkulator.FordelBeregningsgrunnlagTjeneste;
import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLNaturalytelse;
import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLRefusjonOgGradering;
import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLUtenAndelendring;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.ForeldrepengerGrunnlagMapper;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapBeregningsgrunnlagFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapInntektsgrunnlagVLTilRegel;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapInntektsgrunnlagVLTilRegelFelles;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.YtelsesspesifikkRegelMapper;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelNaturalYtelse;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelRefusjonOgGradering;
import no.nav.folketrygdloven.kalkulator.kontrollerfakta.FaktaOmBeregningTilfelleTjeneste;
import no.nav.folketrygdloven.kalkulator.kontrollerfakta.TilfelleUtlederMockTjeneste;
import no.nav.folketrygdloven.kalkulator.ytelse.fp.FullføreBeregningsgrunnlagFPImpl;
import no.nav.folketrygdloven.utils.UnitTestLookupInstanceImpl;

public class BeregningTjenesteProvider {

    public static BeregningTjenesteWrapper provide() {
        FaktaOmBeregningTilfelleTjeneste faktaOmBeregningTilfelleTjeneste = new FaktaOmBeregningTilfelleTjeneste(
            TilfelleUtlederMockTjeneste.getUtlederInstances());
        AksjonspunktUtlederFaktaOmBeregning aksjonspunktUtlederFaktaOmBeregning = new AksjonspunktUtlederFaktaOmBeregning(faktaOmBeregningTilfelleTjeneste);

        MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelNaturalYtelse oversetterTilRegelNaturalytelse = new MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelNaturalYtelse();
        MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelRefusjonOgGradering oversetterTilRegelRefusjonOgGradering = new MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelRefusjonOgGradering();
        MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLNaturalytelse oversetterFraRegelNaturalytelse = new MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLNaturalytelse();
        MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLRefusjonOgGradering oversetterFraRegelRefusjonOgGradering = new MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLRefusjonOgGradering();
        var oversetterFraRegelTilVLUtenAndelendring = new MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLUtenAndelendring();
        var fastsettBeregningsgrunnlagPerioderTjeneste = new FastsettBeregningsgrunnlagPerioderTjeneste(oversetterTilRegelNaturalytelse, new UnitTestLookupInstanceImpl<>(oversetterTilRegelRefusjonOgGradering), oversetterFraRegelNaturalytelse, oversetterFraRegelRefusjonOgGradering, oversetterFraRegelTilVLUtenAndelendring);

        MapInntektsgrunnlagVLTilRegel mapInntektsgrunnlagVLTilRegel = new MapInntektsgrunnlagVLTilRegelFelles();
        UnitTestLookupInstanceImpl<YtelsesspesifikkRegelMapper> ytelsesSpesifikkMapper = new UnitTestLookupInstanceImpl<>(new ForeldrepengerGrunnlagMapper());
        MapBeregningsgrunnlagFraVLTilRegel mapBeregningsgrunnlagFraVLTilRegel = new MapBeregningsgrunnlagFraVLTilRegel(new UnitTestLookupInstanceImpl<>(mapInntektsgrunnlagVLTilRegel), ytelsesSpesifikkMapper);
        var fordelBeregningsgrunnlagTjeneste = new FordelBeregningsgrunnlagTjeneste(fastsettBeregningsgrunnlagPerioderTjeneste, mapBeregningsgrunnlagFraVLTilRegel);
        var fullføreBeregningsgrunnlagTjeneste = new FullføreBeregningsgrunnlagFPImpl(mapBeregningsgrunnlagFraVLTilRegel);

        return new BeregningTjenesteWrapper(fullføreBeregningsgrunnlagTjeneste, fordelBeregningsgrunnlagTjeneste, aksjonspunktUtlederFaktaOmBeregning, fastsettBeregningsgrunnlagPerioderTjeneste);
    }

}
