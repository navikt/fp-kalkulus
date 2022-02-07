package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell;

import jakarta.enterprise.context.ApplicationScoped;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;

@ApplicationScoped
@FagsakYtelseTypeRef("SVP")
@FagsakYtelseTypeRef("PSB")
@FagsakYtelseTypeRef("FP")
@FagsakYtelseTypeRef("PPN")
@FagsakYtelseTypeRef("FRISINN")
public class BrukerSøktForAllePerioderMapper implements SøknadsperiodeMapper {

    @Override
    public boolean harBrukerSøktFor(Arbeidsgiver arbeidsgiver, Intervall periode, YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {
        return true;
    }

}
