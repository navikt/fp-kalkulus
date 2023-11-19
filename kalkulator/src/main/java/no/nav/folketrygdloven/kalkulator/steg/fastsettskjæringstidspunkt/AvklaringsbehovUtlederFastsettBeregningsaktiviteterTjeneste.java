package no.nav.folketrygdloven.kalkulator.steg.fastsettskjæringstidspunkt;

import no.nav.folketrygdloven.kalkulator.steg.fastsettskjæringstidspunkt.ytelse.k14.fp.AvklaringsbehovUtlederFastsettBeregningsaktiviteterFP;
import no.nav.folketrygdloven.kalkulator.steg.fastsettskjæringstidspunkt.ytelse.k14.svp.AvklaringsbehovUtlederFastsettBeregningsaktiviteterSVP;
import no.nav.folketrygdloven.kalkulator.steg.fastsettskjæringstidspunkt.ytelse.k9.omp.AvklaringsbehovUtlederFastsettBeregningsaktiviteterOMP;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;

public class AvklaringsbehovUtlederFastsettBeregningsaktiviteterTjeneste {

    public static AvklaringsbehovUtlederFastsettBeregningsaktiviteter utledTjeneste(FagsakYtelseType ytelseType) {
        return switch (ytelseType) {
            case FORELDREPENGER -> new AvklaringsbehovUtlederFastsettBeregningsaktiviteterFP();
            case SVANGERSKAPSPENGER -> new AvklaringsbehovUtlederFastsettBeregningsaktiviteterSVP();
            case OMSORGSPENGER -> new AvklaringsbehovUtlederFastsettBeregningsaktiviteterOMP();
            case PLEIEPENGER_SYKT_BARN, PLEIEPENGER_NÆRSTÅENDE, OPPLÆRINGSPENGER -> new AvklaringsbehovUtlederFastsettBeregningsaktiviteterPleiepenger();
            default -> throw new IllegalStateException("Utviklerfeil: AvklaringsbehovUtlederFastsettBeregningsaktiviteter ikke implementert for ytelse " + ytelseType.getKode());
        };
    }
}
