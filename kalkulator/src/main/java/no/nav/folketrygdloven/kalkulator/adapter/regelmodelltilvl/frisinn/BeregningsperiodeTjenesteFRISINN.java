package no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.frisinn;

import java.time.LocalDate;
import javax.enterprise.context.ApplicationScoped;
import no.nav.folketrygdloven.kalkulator.BeregningsperiodeTjeneste;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;

@ApplicationScoped
@FagsakYtelseTypeRef("FRISINN")
public class BeregningsperiodeTjenesteFRISINN extends BeregningsperiodeTjeneste {

    public static final LocalDate BEREGNINGSPERIODE_FOM_FRISINN = LocalDate.of(2019, 3, 1);
    public static final LocalDate BEREGNINGSPERIODE_TOM_FRISINN = LocalDate.of(2020, 2, 29);
    public static final LocalDate BEREGNINGSPERIODE_FOM_SN_FRISINN = LocalDate.of(2017, 1, 1);
    public static final LocalDate BEREGNINSPERIODE_TOM_SN_FRISINN = LocalDate.of(2019, 12, 31);

    @Override
    public Intervall fastsettBeregningsperiodeForATFLAndeler(LocalDate skjæringstidspunkt) {
        return Intervall.fraOgMedTilOgMed(BEREGNINGSPERIODE_FOM_FRISINN, BEREGNINGSPERIODE_TOM_FRISINN);
    }

    @Override
    public Intervall fastsettBeregningsperiodeForSNAndeler(LocalDate skjæringstidspunkt) {
        return Intervall.fraOgMedTilOgMed(BEREGNINGSPERIODE_FOM_SN_FRISINN, BEREGNINSPERIODE_TOM_SN_FRISINN);
    }

}
