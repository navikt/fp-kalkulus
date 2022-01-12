package no.nav.folketrygdloven.kalkulator.ytelse.k9;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.felles.frist.ArbeidsgiverRefusjonskravTjeneste;

@FagsakYtelseTypeRef("PSB")
@FagsakYtelseTypeRef("PPN")
@ApplicationScoped
public class MapRefusjonPerioderFraVLTilRegelPleiepenger extends MapRefusjonPerioderFraVLTilRegelK9 {

    @Inject
    public MapRefusjonPerioderFraVLTilRegelPleiepenger(ArbeidsgiverRefusjonskravTjeneste arbeidsgiverRefusjonskravTjeneste) {
        super(arbeidsgiverRefusjonskravTjeneste);
    }
}
