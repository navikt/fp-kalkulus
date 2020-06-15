package no.nav.folketrygdloven.kalkulator;

import java.time.LocalDate;
import java.util.List;

import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.FagsakYtelseType;

/**
 * Tjeneste for å finne siste aktivitetsdag.
 *
 */
public class SisteAktivitetsdagTjeneste {

    private SisteAktivitetsdagTjeneste() {
        // Skjul konstruktør
    }


    /**
     * Finne datogrensen for inkluderte aktiviteter. Aktiviteter som slutter på eller etter denne datoen blir med i beregningen.
     *
     * @param skjæringstidspunkt skjæringstidspunkt
     * @return Dato for inkluderte aktiviteter
     */
    public static LocalDate finnDatogrenseForInkluderteAktiviteter(LocalDate skjæringstidspunkt) {
        return skjæringstidspunkt.minusDays(1);
    }

}
