package no.nav.folketrygdloven.kalkulator.steg.fastsettskjæringstidspunkt;

import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapBeregningAktiviteterFraRegelTilVL;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapBeregningAktiviteterFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapBeregningAktiviteterFraVLTilRegelFelles;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapInntektsgrunnlagVLTilRegel;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapInntektsgrunnlagVLTilRegelFRISINN;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapInntektsgrunnlagVLTilRegelFelles;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.frisinn.MapBeregningAktiviteterFraVLTilRegelFRISINN;
import no.nav.folketrygdloven.kalkulator.input.FastsettBeregningsaktiviteterInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModell;

public class FastsettBeregningAktiviteter {

    public BeregningAktivitetAggregatDto fastsettAktiviteter(FastsettBeregningsaktiviteterInput input) {
        // Oversetter Opptjening -> regelmodell, hvor også skjæringstidspunkt for Opptjening er lagret
        AktivitetStatusModell regelmodell = finnMapper(input.getFagsakYtelseType()).mapForSkjæringstidspunkt(input);
        return MapBeregningAktiviteterFraRegelTilVL.map(regelmodell);
    }

    private MapBeregningAktiviteterFraVLTilRegel finnMapper(FagsakYtelseType ytelseType) {
        return switch (ytelseType) {
            case PLEIEPENGER_SYKT_BARN, PLEIEPENGER_NÆRSTÅENDE, OMSORGSPENGER, OPPLÆRINGSPENGER, FORELDREPENGER, SVANGERSKAPSPENGER -> new MapBeregningAktiviteterFraVLTilRegelFelles();
            case FRISINN -> new MapBeregningAktiviteterFraVLTilRegelFRISINN();
            default -> throw new IllegalArgumentException("Finner ikke MapInntektsgrunnlagVLTilRegel implementasjon for ytelse " + ytelseType);
        };
    }


}
