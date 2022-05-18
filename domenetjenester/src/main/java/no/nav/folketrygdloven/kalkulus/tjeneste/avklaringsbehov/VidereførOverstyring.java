package no.nav.folketrygdloven.kalkulus.tjeneste.avklaringsbehov;

import java.util.Optional;

import jakarta.enterprise.inject.Instance;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.avklaringsbehov.AvklaringsbehovEntitet;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningSteg;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;

/**
 * Bestemmer om overstyringsaksjonspunkt for steg skal opprettes ved kjøring framover
 */
public interface VidereførOverstyring {

    static VidereførOverstyring finnTjeneste(Instance<VidereførOverstyring> instances, FagsakYtelseType ytelseType) {
        return FagsakYtelseTypeRef.Lookup.find(instances, ytelseType)
                .orElseThrow(() -> new UnsupportedOperationException("Har ikke " + VidereførOverstyring.class.getSimpleName() + " for ytelseType=" + ytelseType));
    }

    Optional<AvklaringsbehovEntitet> videreførOverstyringForSteg(Long koblingid, BeregningSteg beregningSteg);


}
