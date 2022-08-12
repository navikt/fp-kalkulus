package no.nav.folketrygdloven.kalkulator.ytelse.k9;

import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_BEGYNNELSE;
import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.felles.frist.FristVurderer;
import no.nav.folketrygdloven.kalkulator.modell.iay.PerioderForKravDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.kodeverk.Utfall;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;

@FagsakYtelseTypeRef(FagsakYtelseType.PLEIEPENGER_SYKT_BARN)
@FagsakYtelseTypeRef(FagsakYtelseType.OPPLÆRINGSPENGER)
@FagsakYtelseTypeRef(FagsakYtelseType.OMSORGSPENGER)
@FagsakYtelseTypeRef(FagsakYtelseType.PLEIEPENGER_NÆRSTÅENDE)
@ApplicationScoped
public class AllePerioderGodkjentFristVurderer implements FristVurderer {

    public LocalDateTimeline<Utfall> finnTidslinje(PerioderForKravDto krav, Optional<LocalDate> overstyrtRefusjonFom) {
        return new LocalDateTimeline<>(List.of(new LocalDateSegment<>(
                TIDENES_BEGYNNELSE,
                TIDENES_ENDE, Utfall.GODKJENT)));
    }

}
