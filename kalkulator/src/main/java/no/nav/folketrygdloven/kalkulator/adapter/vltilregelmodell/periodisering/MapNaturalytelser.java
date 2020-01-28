package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering;

import java.util.List;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.regelmodell.grunnlag.inntekt.NaturalYtelse;

class MapNaturalytelser {
    private MapNaturalytelser() {
        // skjul public constructor
    }

    static List<NaturalYtelse> mapNaturalytelser(InntektsmeldingDto im) {
        return im.getNaturalYtelser().stream()
            .map(ny -> new NaturalYtelse(ny.getBeloepPerMnd().getVerdi(), ny.getPeriode().getFomDato(), ny.getPeriode().getTomDato()))
            .collect(Collectors.toList());
    }
}
