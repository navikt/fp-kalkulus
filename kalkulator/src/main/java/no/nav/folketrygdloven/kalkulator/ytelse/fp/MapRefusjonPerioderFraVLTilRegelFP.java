package no.nav.folketrygdloven.kalkulator.ytelse.fp;

import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.refusjon.MapRefusjonPerioderFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.felles.frist.ArbeidsgiverRefusjonskravTjeneste;
import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.permisjon.PermisjonFilter;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;
import no.nav.fpsak.tidsserie.StandardCombinators;

@FagsakYtelseTypeRef("FP")
@ApplicationScoped
public class MapRefusjonPerioderFraVLTilRegelFP extends MapRefusjonPerioderFraVLTilRegel {

    @Inject
    public MapRefusjonPerioderFraVLTilRegelFP(ArbeidsgiverRefusjonskravTjeneste arbeidsgiverRefusjonskravTjeneste) {
        super(arbeidsgiverRefusjonskravTjeneste);
    }

    @Override
    protected List<Intervall> finnGyldigeRefusjonPerioder(LocalDate startdatoPermisjon, YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag, InntektsmeldingDto inntektsmelding, List<AktivitetsAvtaleDto> alleAnsattperioderForInntektsmeldingEtterStartAvBeregning, Set<YrkesaktivitetDto> yrkesaktiviteter, PermisjonFilter permisjonFilter) {
        if (inntektsmelding.getRefusjonOpphører() != null && inntektsmelding.getRefusjonOpphører().isBefore(startdatoPermisjon)) {
            // Refusjon opphører før det utledede startpunktet, blir aldri refusjon
            return Collections.emptyList();
        }
        var utbetalingTidslinje = new LocalDateTimeline<>(List.of(new LocalDateSegment<>(startdatoPermisjon, TIDENES_ENDE, Boolean.TRUE)));
        var permisjontidslinje = finnPermisjontidslinje(yrkesaktiviteter, permisjonFilter);
        return utbetalingTidslinje.disjoint(permisjontidslinje).getLocalDateIntervals()
                .stream()
                .map(it -> Intervall.fraOgMedTilOgMed(it.getFomDato(), it.getTomDato()))
                .toList();
    }

    private LocalDateTimeline<Boolean> finnPermisjontidslinje(Set<YrkesaktivitetDto> yrkesaktiviteterRelatertTilInntektsmelding, PermisjonFilter permisjonFilter) {
        return yrkesaktiviteterRelatertTilInntektsmelding.stream()
                .map(permisjonFilter::finnTidslinjeForPermisjonOver14Dager)
                .reduce((t1, t2) -> t1.combine(t2, StandardCombinators::alwaysTrueForMatch, LocalDateTimeline.JoinStyle.INNER_JOIN))
                .orElse(LocalDateTimeline.empty());
    }


    // For CDI (for håndtere at annotation propageres til subklasser)

}
