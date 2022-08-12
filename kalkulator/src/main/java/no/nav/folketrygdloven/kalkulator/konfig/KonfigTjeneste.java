package no.nav.folketrygdloven.kalkulator.konfig;

import java.util.ArrayList;
import java.util.List;

import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;

public class KonfigTjeneste {
    private static final List<FagsakYtelseType> STØTTEDE_YTELSER = new ArrayList<>();

    private KonfigTjeneste() {
        // Skjuler default
    }

    static {
        STØTTEDE_YTELSER.add(FagsakYtelseType.FORELDREPENGER);
        STØTTEDE_YTELSER.add(FagsakYtelseType.SVANGERSKAPSPENGER);
        STØTTEDE_YTELSER.add(FagsakYtelseType.PLEIEPENGER_SYKT_BARN);
        STØTTEDE_YTELSER.add(FagsakYtelseType.PLEIEPENGER_NÆRSTÅENDE);
        STØTTEDE_YTELSER.add(FagsakYtelseType.OMSORGSPENGER);
        STØTTEDE_YTELSER.add(FagsakYtelseType.OPPLÆRINGSPENGER);
        STØTTEDE_YTELSER.add(FagsakYtelseType.FRISINN);
    }

    public static Konfigverdier forYtelse(FagsakYtelseType ytelse) {
        verfisierYtelsetype(ytelse);
        if (FagsakYtelseType.FORELDREPENGER.equals(ytelse)) {
            return new FPKonfig();
        } else if (FagsakYtelseType.FRISINN.equals(ytelse)) {
            return new FRISINNKonfig();
        } else {
            return new DefaultKonfig();
        }
    }

    private static void verfisierYtelsetype(FagsakYtelseType ytelse) {
        if (ytelse == null || !STØTTEDE_YTELSER.contains(ytelse)) {
            throw new IllegalStateException("Ytelsetype " + ytelse + " har ingen definerte konfigverdier");
        }
    }

}
