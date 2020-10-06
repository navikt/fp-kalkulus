package no.nav.folketrygdloven.kalkulator.steg.refusjon;


import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.periodisering.FordelPerioderTjeneste;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.modell.RefusjonAndel;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand;

/**
 * Tjeneste for å finne andeler i nytt beregningsgrunnlag som har økt refusjon siden orginalbehandlingen.
 */
@ApplicationScoped
public class AndelerMedØktRefusjonTjeneste {

    private FordelPerioderTjeneste fordelPerioderTjeneste;

    @Inject
    public AndelerMedØktRefusjonTjeneste(FordelPerioderTjeneste fordelPerioderTjeneste) {
        this.fordelPerioderTjeneste = fordelPerioderTjeneste;
    }

    public AndelerMedØktRefusjonTjeneste() {
        // CDI
    }

    public Map<Intervall, List<RefusjonAndel>> finnAndelerMedØktRefusjon(BeregningsgrunnlagInput input) {
        if (input.getBeregningsgrunnlagGrunnlag() == null || input.getBeregningsgrunnlagGrunnlag().getBeregningsgrunnlagTilstand().erFør(BeregningsgrunnlagTilstand.FORESLÅTT)) {
            // Tjenesten er kalt før vi er klar for å vurdere refusjon, returnere tomt map
            return Collections.emptyMap();
        }
        BeregningsgrunnlagDto bgMedRef = hentRefusjonForBG(input);
        Optional<BeregningsgrunnlagDto> originaltGrunnlag = input.getBeregningsgrunnlagGrunnlagFraForrigeBehandling().flatMap(BeregningsgrunnlagGrunnlagDto::getBeregningsgrunnlag);
        LocalDate alleredeUtbetaltTOM = FinnAlleredeUtbetaltTom.finn();
        return originaltGrunnlag.map(og -> BeregningRefusjonTjeneste.finnUtbetaltePerioderMedAndelerMedØktRefusjon(bgMedRef, og, alleredeUtbetaltTOM)).orElse(Collections.emptyMap());
    }

    private BeregningsgrunnlagDto hentRefusjonForBG(BeregningsgrunnlagInput input) {
        // TODO TFP-3792 Vi bør flytte splitting av perioder slik at vi ikke trenger å gjøre dette to ganger, en gang her for å "sniktitte" og en gang i neste steg permanent
        BeregningsgrunnlagRegelResultat resultat = fordelPerioderTjeneste.fastsettPerioderForRefusjonOgGradering(input, input.getBeregningsgrunnlag());
        return resultat.getBeregningsgrunnlag();
    }

}
