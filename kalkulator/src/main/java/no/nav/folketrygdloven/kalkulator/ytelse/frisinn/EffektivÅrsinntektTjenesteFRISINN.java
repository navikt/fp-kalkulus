package no.nav.folketrygdloven.kalkulator.ytelse.frisinn;

import java.math.BigDecimal;
import java.math.RoundingMode;

import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittPeriodeInntekt;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;

public final class EffektivÅrsinntektTjenesteFRISINN {

    private EffektivÅrsinntektTjenesteFRISINN() {
        // SKjuler default
    }

    public static final int VIRKEDAGER_I_ET_ÅR = 260;

    /**
     * Finner effektiv årsinntekt fra oppgitt inntekt
     *
     * @param oppgittInntekt oppgitt inntektsinformasjon
     * @return effektiv årsinntekt fra inntekt
     */
    public static BigDecimal finnEffektivÅrsinntektForLøpenedeInntekt(OppgittPeriodeInntekt oppgittInntekt) {
        BigDecimal dagsats = finnEffektivDagsatsIPeriode(oppgittInntekt);
        return dagsats.multiply(BigDecimal.valueOf(VIRKEDAGER_I_ET_ÅR));
    }

    /**
     * Finner opptjent inntekt pr dag i periode
     *
     * @param oppgittInntekt Informasjon om oppgitt inntekt
     * @return dagsats i periode
     */
    private static BigDecimal finnEffektivDagsatsIPeriode(OppgittPeriodeInntekt oppgittInntekt) {
        Intervall periode = oppgittInntekt.getPeriode();
        long dagerIRapportertPeriode = Virkedager.beregnAntallVirkedagerEllerKunHelg(periode.getFomDato(), periode.getTomDato());
        if (oppgittInntekt.getInntekt() == null) {
            return BigDecimal.ZERO;
        }
        return oppgittInntekt.getInntekt().divide(BigDecimal.valueOf(dagerIRapportertPeriode), 10, RoundingMode.HALF_EVEN);
    }
}
