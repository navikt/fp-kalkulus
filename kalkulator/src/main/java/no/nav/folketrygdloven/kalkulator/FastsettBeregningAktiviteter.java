package no.nav.folketrygdloven.kalkulator;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapBeregningAktiviteterFraRegelTilVL;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapBeregningAktiviteterFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModell;

@ApplicationScoped
public class FastsettBeregningAktiviteter {

    private Instance<MapBeregningAktiviteterFraVLTilRegel> mappingInstanser;

    public FastsettBeregningAktiviteter() {
        // CDI
    }

    @Inject
    public FastsettBeregningAktiviteter(@Any Instance<MapBeregningAktiviteterFraVLTilRegel> mappingInstanser) {
        this.mappingInstanser = mappingInstanser;
    }

    public BeregningAktivitetAggregatDto fastsettAktiviteter(BeregningsgrunnlagInput input) {
        // Oversetter Opptjening -> regelmodell, hvor også skjæringstidspunkt for Opptjening er lagret
        AktivitetStatusModell regelmodell = FagsakYtelseTypeRef.Lookup.find(mappingInstanser, input.getFagsakYtelseType())
                .orElseThrow(() -> new IllegalStateException("Fant ikke mappingklasse for ytelse " + input.getFagsakYtelseType()))
                .mapForSkjæringstidspunkt(input);
        return MapBeregningAktiviteterFraRegelTilVL.map(regelmodell);
    }

}
