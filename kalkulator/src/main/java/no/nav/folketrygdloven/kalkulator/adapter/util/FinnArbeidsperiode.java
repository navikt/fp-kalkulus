package no.nav.folketrygdloven.kalkulator.adapter.util;

import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;

import java.time.LocalDate;
import java.util.Collection;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.typer.OrgNummer;


public class FinnArbeidsperiode {

    private final YrkesaktivitetFilterDto yrkesaktivitetFilter;

    public FinnArbeidsperiode(YrkesaktivitetFilterDto filter) {
        yrkesaktivitetFilter = filter;
    }

    public Intervall finnArbeidsperiode(Arbeidsgiver arbeidsgiver,
                                        InternArbeidsforholdRefDto iaRef,
                                        LocalDate skjæringstidspunkt) {
        var ansettelsesPerioder = yrkesaktivitetFilter.getYrkesaktiviteterForBeregning().stream()
                .filter(ya -> ya.gjelderFor(arbeidsgiver, iaRef))
                .map(yrkesaktivitetFilter::getAnsettelsesPerioder)
                .flatMap(Collection::stream)
                .filter(a -> !a.getPeriode().getFomDato().isAfter(skjæringstidspunkt))
                .collect(Collectors.toList());
        LocalDate arbeidsperiodeFom = ansettelsesPerioder.stream().map(a -> a.getPeriode().getFomDato()).min(LocalDate::compareTo).orElse(null);
        LocalDate arbeidsperiodeTom = ansettelsesPerioder.stream().map(a -> a.getPeriode().getTomDato()).max(LocalDate::compareTo).orElse(null);

        if (erKunstig(arbeidsgiver)) {
            if (arbeidsperiodeFom == null) {
                arbeidsperiodeFom = skjæringstidspunkt;
            }
            if (arbeidsperiodeTom == null) {
                arbeidsperiodeTom = TIDENES_ENDE;
            }
        }

        return Intervall.fraOgMedTilOgMed(arbeidsperiodeFom, arbeidsperiodeTom);
    }

    private boolean erKunstig(Arbeidsgiver arbeidsgiver) {
        return arbeidsgiver != null && arbeidsgiver.getErVirksomhet() && OrgNummer.KUNSTIG_ORG.equals(arbeidsgiver.getIdentifikator());
    }

}
