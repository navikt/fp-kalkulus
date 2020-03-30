package no.nav.folketrygdloven.kalkulator.konfig;

import java.util.ArrayList;
import java.util.List;

import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.FagsakYtelseType;

public class KonfigTjeneste {
    private static final List<FagsakYtelseType> STØTTDEDE_YTELSER = new ArrayList<>();

    private KonfigTjeneste() {
        // Skjuler default
    }

    static {
        STØTTDEDE_YTELSER.add(FagsakYtelseType.FORELDREPENGER);
        STØTTDEDE_YTELSER.add(FagsakYtelseType.SVANGERSKAPSPENGER);
        STØTTDEDE_YTELSER.add(FagsakYtelseType.PLEIEPENGER_SYKT_BARN);
        STØTTDEDE_YTELSER.add(FagsakYtelseType.OMSORGSPENGER);
    }

    public static Konfigverdier forYtelse(FagsakYtelseType ytelse) {
        verfisierYtelsetype(ytelse);
        if (FagsakYtelseType.FORELDREPENGER.equals(ytelse)) {
            return new FPKonfig();
        } else if (FagsakYtelseType.SVANGERSKAPSPENGER.equals(ytelse)) {
            return new SVPKonfig();
        } else if (FagsakYtelseType.PLEIEPENGER_SYKT_BARN.equals(ytelse)) {
            return new PSBKonfig();
        } else {
            return new OMPKonfig();
        }
    }

    private static void verfisierYtelsetype(FagsakYtelseType ytelse) {
        if (ytelse == null || !STØTTDEDE_YTELSER.contains(ytelse)) {
            throw new IllegalStateException("Ytelsetype " + ytelse + " har ingen definerte konfigverdier");
        }
    }

}
