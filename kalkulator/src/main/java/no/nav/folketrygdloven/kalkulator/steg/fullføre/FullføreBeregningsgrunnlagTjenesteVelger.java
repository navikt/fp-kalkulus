package no.nav.folketrygdloven.kalkulator.steg.fullføre;

import no.nav.folketrygdloven.kalkulator.steg.fullføre.ytelse.fp.FullføreBeregningsgrunnlagFPImpl;
import no.nav.folketrygdloven.kalkulator.steg.fullføre.ytelse.omp.FullføreBeregningsgrunnlagOMP;
import no.nav.folketrygdloven.kalkulator.steg.fullføre.ytelse.psb.FullføreBeregningsgrunnlagPleiepenger;
import no.nav.folketrygdloven.kalkulator.steg.fullføre.ytelse.svp.FullføreBeregningsgrunnlagSVP;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;

public class FullføreBeregningsgrunnlagTjenesteVelger {

    public static FullføreBeregningsgrunnlag utledTjeneste(FagsakYtelseType ytelseType) {
        return switch (ytelseType) {
            case FORELDREPENGER -> new FullføreBeregningsgrunnlagFPImpl();
            case SVANGERSKAPSPENGER -> new FullføreBeregningsgrunnlagSVP();
            case OMSORGSPENGER -> new FullføreBeregningsgrunnlagOMP();
            case PLEIEPENGER_SYKT_BARN, PLEIEPENGER_NÆRSTÅENDE, OPPLÆRINGSPENGER -> new FullføreBeregningsgrunnlagPleiepenger();
            default -> throw new IllegalStateException("Utviklerfeil: AvklaringsbehovUtlederFastsettBeregningsaktiviteter ikke implementert for ytelse " + ytelseType.getKode());
        };
    }
}
