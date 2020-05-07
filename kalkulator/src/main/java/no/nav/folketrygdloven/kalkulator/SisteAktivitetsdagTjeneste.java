package no.nav.folketrygdloven.kalkulator;

import java.time.LocalDate;
import java.util.List;

import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.FagsakYtelseType;

/**
 * Tjeneste for å finne siste aktivitetsdag.
 *
 * Tjenesten løser problemet med at livsoppholdsytelser er ulik andre ytelser i hvilke aktiviteter som skal inkluderes i beregning.
 *
 *
 */
public class SisteAktivitetsdagTjeneste {

    private SisteAktivitetsdagTjeneste() {
        // Skjul konstruktør
    }


    // Om det er fleire omfattende endringer i korleis man behandler livsoppholdytelser bør man vurdere å gjere dette på ein annan måte,
    // og heller dele løsningen i eit spor for livsoppholdsytelser og eit spor for andre ytelser
    // Per 06.05.2020 er det såpass lite skilnad mellom ytelsene at det kan gjerast på denne måten

    public static final List<FagsakYtelseType> YTELSER_LIVSOPPHOLD = List.of(FagsakYtelseType.FORELDREPENGER);

    /**
     * Finne datogrensen for inkluderte aktiviteter. Aktiviteter som slutter på eller etter denne datoen blir med i beregningen.
     *
     * @param fagsakYtelseType BeregningsgrunnlagInput
     * @param skjæringstidspunkt skjæringstidspunkt
     * @return Dato for inkluderte aktiviteter
     */
    public static LocalDate finnDatogrenseForInkluderteAktiviteter(FagsakYtelseType fagsakYtelseType, LocalDate skjæringstidspunkt) {
        return erLivsoppholdsytelse(fagsakYtelseType) ? skjæringstidspunkt.minusDays(1) : skjæringstidspunkt;
    }

    private static boolean erLivsoppholdsytelse(FagsakYtelseType fagsakYtelseType) {
        return YTELSER_LIVSOPPHOLD.contains(fagsakYtelseType);
    }

}
