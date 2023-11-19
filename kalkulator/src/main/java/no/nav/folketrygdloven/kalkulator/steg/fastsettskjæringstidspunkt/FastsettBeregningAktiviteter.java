package no.nav.folketrygdloven.kalkulator.steg.fastsettskjæringstidspunkt;

import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapBeregningAktiviteterFraRegelTilVL;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapBeregningAktiviteterFraVLTilRegelFelles;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.frisinn.MapBeregningAktiviteterFraVLTilRegelFRISINN;
import no.nav.folketrygdloven.kalkulator.input.FastsettBeregningsaktiviteterInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModell;

public class FastsettBeregningAktiviteter {

    public BeregningAktivitetAggregatDto fastsettAktiviteter(FastsettBeregningsaktiviteterInput input) {
        // Oversetter Opptjening -> regelmodell, hvor også skjæringstidspunkt for Opptjening er lagret
        AktivitetStatusModell regelmodell = FagsakYtelseType.FRISINN.equals(input.getFagsakYtelseType()) ?
                new MapBeregningAktiviteterFraVLTilRegelFRISINN().mapForSkjæringstidspunkt(input) :
                new MapBeregningAktiviteterFraVLTilRegelFelles().mapForSkjæringstidspunkt(input);
        return MapBeregningAktiviteterFraRegelTilVL.map(regelmodell);
    }

}
