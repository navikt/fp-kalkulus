package no.nav.folketrygdloven.kalkulator.ytelse.psb;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapBGSkjæringstidspunktOgStatuserFraRegelTilVL;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapBGStatuserFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.steg.fastsettskjæringstidspunkt.FastsettSkjæringstidspunktOgStatuser;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModell;

@ApplicationScoped
@FagsakYtelseTypeRef("PSB")
public class FastsettSkjæringstidspunktOgStatuserPSB extends FastsettSkjæringstidspunktOgStatuser {


    public FastsettSkjæringstidspunktOgStatuserPSB() {
        super();
    }

    @Inject
    public FastsettSkjæringstidspunktOgStatuserPSB(MapBGSkjæringstidspunktOgStatuserFraRegelTilVL mapFraRegel) {
        super(mapFraRegel);
    }

    @Override
    protected AktivitetStatusModell mapTilRegel(BeregningAktivitetAggregatDto beregningAktivitetAggregat) {
        AktivitetStatusModell modell = MapBGStatuserFraVLTilRegel.map(beregningAktivitetAggregat);
        modell.setFinnBeregningstidspunkt(stp -> stp);
        return modell;
    }

}
