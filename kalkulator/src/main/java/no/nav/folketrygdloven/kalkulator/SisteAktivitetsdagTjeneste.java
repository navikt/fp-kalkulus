package no.nav.folketrygdloven.kalkulator;

import java.time.LocalDate;

import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;

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
     * @param fagsakYtelseType Fagsakytelsetype
     * @return Dato for inkluderte aktiviteter
     */
    // Vi må vurdere om dette skal fortsette å vere ein static klasse eller om vi burde lage eit interface med implementasjoner pr ytelse
    public static LocalDate finnDatogrenseForInkluderteAktiviteter(LocalDate skjæringstidspunkt, FagsakYtelseType fagsakYtelseType) {
        if (FagsakYtelseType.PLEIEPENGER_SYKT_BARN.equals(fagsakYtelseType)) {
            return skjæringstidspunkt;
        }
        return skjæringstidspunkt.minusDays(1);
    }

}
