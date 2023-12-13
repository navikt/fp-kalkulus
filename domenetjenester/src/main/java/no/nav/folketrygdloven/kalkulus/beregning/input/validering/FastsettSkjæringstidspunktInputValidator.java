package no.nav.folketrygdloven.kalkulus.beregning.input.validering;

import org.jboss.weld.exceptions.IllegalArgumentException;

import no.nav.folketrygdloven.kalkulus.felles.v1.KalkulatorInputDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningSteg;

public class FastsettSkjæringstidspunktInputValidator implements KalkulatorInputStegValidator {

    private static final BeregningSteg STEG = BeregningSteg.FASTSETT_STP_BER;

    @Override
    public void valider(KalkulatorInputDto input) {

        if (input.getOpptjeningAktiviteter() == null) {
            throw new IllegalArgumentException("Må ha satt opptjenignsaktiviteter ved beregning av steg " + STEG.getKode());
        }

        if (input.getOpptjeningAktiviteter().getPerioder().isEmpty() && input.getOpptjeningAktiviteter().getMidlertidigInaktivType() == null) {
            throw new IllegalArgumentException("Må ha minst en opptjeningsaktivitet dersom bruker ikke er midlertidig inaktiv");
        }

    }
}
