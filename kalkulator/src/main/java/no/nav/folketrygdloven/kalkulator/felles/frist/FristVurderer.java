package no.nav.folketrygdloven.kalkulator.felles.frist;

import java.time.LocalDate;
import java.util.Optional;

import jakarta.enterprise.inject.Instance;

import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.modell.iay.PerioderForKravDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.kodeverk.Utfall;
import no.nav.fpsak.tidsserie.LocalDateTimeline;

public interface FristVurderer {

    static FristVurderer finnTjeneste(Instance<FristVurderer> vurderere, FagsakYtelseType ytelseType) {
        return FagsakYtelseTypeRef.Lookup.find(vurderere, ytelseType)
                .orElseThrow(() -> new IllegalStateException("Fant ingen fristvurderer for ytelsetype=" + ytelseType));
    }

    LocalDateTimeline<Utfall> finnTidslinje(PerioderForKravDto krav, Optional<LocalDate> overstyrtRefusjonFom);


}
