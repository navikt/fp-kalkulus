package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell;

import jakarta.enterprise.inject.Instance;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;

public interface SøknadsperiodeMapper {

    static SøknadsperiodeMapper finnTjeneste(Instance<SøknadsperiodeMapper> instances, FagsakYtelseType ytelseType) {
        return FagsakYtelseTypeRef.Lookup.find(SøknadsperiodeMapper.class, instances, ytelseType)
                .orElseThrow(() -> new IllegalStateException("Har ikke tjeneste for ytelseType=" + ytelseType));
    }

    boolean harBrukerSøktFor(Arbeidsgiver arbeidsgiver, Intervall periode, YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag);

}
