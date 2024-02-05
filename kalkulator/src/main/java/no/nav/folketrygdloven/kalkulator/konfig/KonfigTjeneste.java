package no.nav.folketrygdloven.kalkulator.konfig;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;

public class KonfigTjeneste {

    private static final Map<FagsakYtelseType, BigDecimal> MINSTE_G_MILITÆR_SIVIL = Map.of(
            FagsakYtelseType.FORELDREPENGER, BigDecimal.valueOf(3),
            FagsakYtelseType.FRISINN, BigDecimal.ZERO
    );

    private KonfigTjeneste() {
        // Skjuler default
    }


    public static Konfigverdier forYtelse(FagsakYtelseType ytelse) {
        verfisierYtelsetype(ytelse);
        return Optional.ofNullable(MINSTE_G_MILITÆR_SIVIL.get(ytelse))
                .map(Konfigverdier::new).orElseGet(Konfigverdier::new);
    }

    public static Konfigverdier forUtbetalingsgradYtelse() {
        return new Konfigverdier();
    }

    private static void verfisierYtelsetype(FagsakYtelseType ytelse) {
        if (ytelse == null || FagsakYtelseType.UDEFINERT.equals(ytelse)) {
            throw new IllegalStateException("Ytelsetype " + ytelse + " har ingen definerte konfigverdier");
        }
    }

}
