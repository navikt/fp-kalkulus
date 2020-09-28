package no.nav.folketrygdloven.kalkulator.steg.fastsettskjæringstidspunkt;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapBeregningAktiviteterFraRegelTilVL;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapBeregningAktiviteterFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModell;

@ApplicationScoped
public class FastsettBeregningAktiviteter {

    private Instance<MapBeregningAktiviteterFraVLTilRegel> mapperTilRegel;

    public FastsettBeregningAktiviteter() {
        // CDI
    }

    @Inject
    public FastsettBeregningAktiviteter(@Any Instance<MapBeregningAktiviteterFraVLTilRegel> mapperTilRegel) {
        this.mapperTilRegel = mapperTilRegel;
    }

    public BeregningAktivitetAggregatDto fastsettAktiviteter(BeregningsgrunnlagInput input) {
        // Oversetter Opptjening -> regelmodell, hvor også skjæringstidspunkt for Opptjening er lagret
        AktivitetStatusModell regelmodell = FagsakYtelseTypeRef.Lookup.find(mapperTilRegel, input.getFagsakYtelseType())
                .orElseThrow(() -> new IllegalStateException("Forventer å finne implementasjon for ytelse " + input.getFagsakYtelseType().getKode()))
                .mapForSkjæringstidspunkt(input);
        return MapBeregningAktiviteterFraRegelTilVL.map(regelmodell);
    }

}
