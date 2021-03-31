package no.nav.folketrygdloven.kalkulator.steg.refusjon;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.modell.RefusjonAndel;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;

/**
 * Tjeneste for å finne andeler i nytt beregningsgrunnlag som har økt refusjon siden orginalbehandlingen.
 */
public final class AndelerMedØktRefusjonTjeneste {

    private AndelerMedØktRefusjonTjeneste() {
        // Skjuler default
    }

    public static Map<Intervall, List<RefusjonAndel>> finnAndelerMedØktRefusjon(BeregningsgrunnlagDto beregningsgrunnlag,
                                                                         BeregningsgrunnlagDto originaltGrunnlag,
                                                                         BigDecimal grenseverdi) {
        if (beregningsgrunnlag == null || originaltGrunnlag == null) {
            return Collections.emptyMap();
        }
        LocalDate alleredeUtbetaltTOM = FinnAlleredeUtbetaltTom.finn();
        return BeregningRefusjonTjeneste.finnUtbetaltePerioderMedAndelerMedØktRefusjon(beregningsgrunnlag, originaltGrunnlag, alleredeUtbetaltTOM, grenseverdi);
    }
}
