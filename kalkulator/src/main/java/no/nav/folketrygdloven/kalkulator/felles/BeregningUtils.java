package no.nav.folketrygdloven.kalkulator.felles;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseAnvistDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseFilterDto;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.FagsakYtelseType;

public class BeregningUtils {
    private static final Logger LOG = LoggerFactory.getLogger(BeregningUtils.class);

    private static final Period MELDEKORT_PERIODE_UTV = Period.parse("P30D");

    public static final BigDecimal MAX_UTBETALING_PROSENT_AAP_DAG = BigDecimal.valueOf(200);

    private BeregningUtils() {}

    public static Optional<YtelseDto> sisteVedtakFørStpForType(YtelseFilterDto ytelseFilter, LocalDate skjæringstidspunkt, Set<FagsakYtelseType> ytelseTyper) {
        return ytelseFilter.getFiltrertYtelser().stream()
            .filter(ytelse -> ytelseTyper.contains(ytelse.getRelatertYtelseType()))
            .filter(ytelse -> !skjæringstidspunkt.isBefore(ytelse.getPeriode().getFomDato()))
            .max(Comparator.comparing(YtelseDto::getPeriode).thenComparing(ytelse -> ytelse.getPeriode().getTomDato()));
    }

    public static Optional<YtelseAnvistDto> sisteHeleMeldekortFørStp(YtelseFilterDto ytelseFilter, YtelseDto sisteVedtak, LocalDate skjæringstidspunkt, Set<FagsakYtelseType> ytelseTyper) {
        LOG.info("Finner siste meldekort for vedtak " + sisteVedtak + " på skjæringstidspunkt " + skjæringstidspunkt + " for ytelser " + ytelseTyper);
        final LocalDate sisteVedtakFom = sisteVedtak.getPeriode().getFomDato();

        List<YtelseAnvistDto> alleMeldekort = ytelseFilter.getFiltrertYtelser().stream()
            .filter(ytelse -> ytelseTyper.contains(ytelse.getRelatertYtelseType()))
            .flatMap(ytelse -> ytelse.getYtelseAnvist().stream()).collect(Collectors.toList());

        LOG.info("Antall meldekort funnet: " + alleMeldekort.size());

        Optional<YtelseAnvistDto> sisteMeldekort = alleMeldekort.stream()
            .filter(ytelseAnvist -> sisteVedtakFom.minus(MELDEKORT_PERIODE_UTV).isBefore(ytelseAnvist.getAnvistTOM()))
            .filter(ytelseAnvist -> skjæringstidspunkt.isAfter(ytelseAnvist.getAnvistTOM()))
            .max(Comparator.comparing(YtelseAnvistDto::getAnvistFOM));

        if (sisteMeldekort.isEmpty()) {
            return Optional.empty();
        }

        LOG.info("Fant meldekort " + sisteMeldekort.get());
        // Vi er nødt til å sjekke om vi har flere meldekort med samme periode
        List<YtelseAnvistDto> alleMeldekortMedPeriode = alleMeldekortMedPeriode(sisteMeldekort.get().getAnvistFOM(), sisteMeldekort.get().getAnvistTOM(), alleMeldekort);

        if (alleMeldekortMedPeriode.size() > 1) {
            LOG.info("Fant " +alleMeldekortMedPeriode.size() + " meldekort med samme periode som " + sisteMeldekort.get());
            return finnMeldekortSomGjelderForVedtak(alleMeldekortMedPeriode, sisteVedtak);
        }

        return sisteMeldekort;

    }

    private static List<YtelseAnvistDto> alleMeldekortMedPeriode(LocalDate anvistFOM, LocalDate anvistTOM, List<YtelseAnvistDto> alleMeldekort) {
        return alleMeldekort.stream()
            .filter(meldekort -> Objects.equals(meldekort.getAnvistFOM(), anvistFOM))
            .filter(meldekort -> Objects.equals(meldekort.getAnvistTOM(), anvistTOM))
            .collect(Collectors.toList());
    }

    private static Optional<YtelseAnvistDto> finnMeldekortSomGjelderForVedtak(List<YtelseAnvistDto> meldekort, YtelseDto sisteVedtak) {
        return meldekort.stream().filter(m -> matcherMeldekortFraSisteVedtak(m, sisteVedtak)).findFirst();
    }

    private static boolean matcherMeldekortFraSisteVedtak(YtelseAnvistDto meldekort, YtelseDto sisteVedtak) {
        return sisteVedtak.getYtelseAnvist().stream().anyMatch(ya -> Objects.equals(ya, meldekort));
    }
}
