package no.nav.folketrygdloven.kalkulus.felles.verktøy;

import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.KonfigurasjonVerdiProvider;

public final class KalkulusKonfigurasjonVerdiProvider implements KonfigurasjonVerdiProvider {

    @Override
    public String get(String key) {
        return Optional.ofNullable(System.getProperty(key))
                .or(() -> Optional.ofNullable(System.getenv(key)))
                .orElse(null);
    }
}
