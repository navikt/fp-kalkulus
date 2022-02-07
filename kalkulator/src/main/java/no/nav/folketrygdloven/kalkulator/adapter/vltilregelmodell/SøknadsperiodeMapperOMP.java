package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell;

import jakarta.enterprise.context.ApplicationScoped;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.input.OmsorgspengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;

@ApplicationScoped
@FagsakYtelseTypeRef("OMP")
public class SøknadsperiodeMapperOMP implements SøknadsperiodeMapper {

    @Override
    public boolean harBrukerSøktFor(Arbeidsgiver arbeidsgiver, Intervall periode, YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {
        var omsorgspengerGrunnlag = (OmsorgspengerGrunnlag) ytelsespesifiktGrunnlag;
        return omsorgspengerGrunnlag.harBrukerSøktForArbeidsgiverIPeriode(periode, arbeidsgiver);
    }
}
