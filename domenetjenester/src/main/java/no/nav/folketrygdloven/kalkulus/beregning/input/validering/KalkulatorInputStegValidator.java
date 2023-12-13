package no.nav.folketrygdloven.kalkulus.beregning.input.validering;

import no.nav.folketrygdloven.kalkulus.felles.v1.KalkulatorInputDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningSteg;

@FunctionalInterface
public interface KalkulatorInputStegValidator {

    static KalkulatorInputStegValidator finnValidator(BeregningSteg steg) {
        return switch (steg) {
            case FASTSETT_STP_BER -> new FastsettSkjæringstidspunktInputValidator();
            default -> (input -> {
            }); // Ingen validering
        };
    }

    void valider(KalkulatorInputDto input);


}
