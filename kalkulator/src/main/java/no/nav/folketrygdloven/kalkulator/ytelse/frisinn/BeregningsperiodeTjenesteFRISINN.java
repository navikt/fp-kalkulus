package no.nav.folketrygdloven.kalkulator.ytelse.frisinn;

import java.time.LocalDate;

import jakarta.enterprise.context.ApplicationScoped;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.beregningsperiode.BeregningsperiodeTjeneste;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;

@ApplicationScoped
@FagsakYtelseTypeRef("FRISINN")
public class BeregningsperiodeTjenesteFRISINN extends BeregningsperiodeTjeneste {

    @Override
    public Intervall fastsettBeregningsperiodeForATFLAndeler(LocalDate skjæringstidspunkt) {
        return Intervall.fraOgMedTilOgMed(skjæringstidspunkt.minusYears(1).withDayOfMonth(1), skjæringstidspunkt.withDayOfMonth(1).minusDays(1));
    }
}
