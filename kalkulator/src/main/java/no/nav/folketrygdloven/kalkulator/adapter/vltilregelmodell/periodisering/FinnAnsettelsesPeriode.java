package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDto;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;

public final class FinnAnsettelsesPeriode {

    private FinnAnsettelsesPeriode() {
        // skjul public constructor
    }

    public static Optional<Periode> finnMinMaksPeriode(Collection<AktivitetsAvtaleDto> ansettelsesPerioder, LocalDate skjæringstidspunkt) {
        return Optional.ofNullable(getMinMaksPeriode(ansettelsesPerioder, skjæringstidspunkt));
    }

    /** Forventer at skjæringstidspunktet ligger i en av ansettelses periodene
     *
     * @param ansettelsesPerioder
     * @param skjæringstidspunkt
     * @return Periode {@link Periode}
     */
    public static Periode getMinMaksPeriode(Collection< AktivitetsAvtaleDto > ansettelsesPerioder, LocalDate skjæringstidspunkt) {
        List<AktivitetsAvtaleDto> perioderSomSlutterEtterStp = ansettelsesPerioder
            .stream()
            .filter(ap -> !ap.getPeriode().getTomDato().isBefore(skjæringstidspunkt.minusDays(1)))
            .collect(Collectors.toList());
        if (perioderSomSlutterEtterStp.isEmpty()) {
            return null;
        }
        LocalDate arbeidsperiodeFom = perioderSomSlutterEtterStp
            .stream()
            .map(a -> a.getPeriode().getFomDato())
            .min(Comparator.naturalOrder()).orElseThrow();

        LocalDate arbeidsperiodeTom = perioderSomSlutterEtterStp
            .stream()
            .map(a -> a.getPeriode().getTomDato())
            .max(Comparator.naturalOrder()).orElseThrow();
        return Periode.of(arbeidsperiodeFom, arbeidsperiodeTom);
    }
}
