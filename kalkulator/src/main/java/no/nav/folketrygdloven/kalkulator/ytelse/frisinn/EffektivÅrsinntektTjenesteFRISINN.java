package no.nav.folketrygdloven.kalkulator.ytelse.frisinn;

import java.math.BigDecimal;
import java.math.RoundingMode;

import no.nav.folketrygdloven.kalkulator.konfig.KonfigTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittPeriodeInntekt;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulator.tid.Virkedager;
import no.nav.folketrygdloven.kalkulus.typer.Beløp;

public final class EffektivÅrsinntektTjenesteFRISINN {

    private EffektivÅrsinntektTjenesteFRISINN() {
        // SKjuler default
    }

    /**
     * Finner effektiv årsinntekt fra oppgitt inntekt
     *
     * @param oppgittInntekt oppgitt inntektsinformasjon
     * @return effektiv årsinntekt fra inntekt
     */
    public static BigDecimal finnEffektivÅrsinntektForLøpenedeInntekt(OppgittPeriodeInntekt oppgittInntekt) {
        BigDecimal dagsats = finnEffektivDagsatsIPeriode(oppgittInntekt);
        return dagsats.multiply(KonfigTjeneste.getYtelsesdagerIÅr());
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
        if (Beløp.safeVerdi(oppgittInntekt.getInntekt()) == null) {
            return BigDecimal.ZERO;
        }
        return oppgittInntekt.getInntekt().verdi().divide(BigDecimal.valueOf(dagerIRapportertPeriode), 10, RoundingMode.HALF_EVEN);
    }
}
