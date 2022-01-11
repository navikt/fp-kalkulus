package no.nav.folketrygdloven.kalkulator.steg.fastsettskjæringstidspunkt;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapBeregningAktiviteterFraRegelTilVL;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapBeregningAktiviteterFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.input.FastsettBeregningsaktiviteterInput;
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

    public BeregningAktivitetAggregatDto fastsettAktiviteter(FastsettBeregningsaktiviteterInput input) {
        // Oversetter Opptjening -> regelmodell, hvor også skjæringstidspunkt for Opptjening er lagret
        MapBeregningAktiviteterFraVLTilRegel mapper = FagsakYtelseTypeRef.Lookup.find(mapperTilRegel, input.getFagsakYtelseType())
                .orElseThrow(() -> new IllegalStateException("Forventer å finne implementasjon for ytelse " + input.getFagsakYtelseType().getKode()));
        AktivitetStatusModell regelmodell = mapper.mapForSkjæringstidspunkt(input);
        return MapBeregningAktiviteterFraRegelTilVL.map(regelmodell);
    }

}
