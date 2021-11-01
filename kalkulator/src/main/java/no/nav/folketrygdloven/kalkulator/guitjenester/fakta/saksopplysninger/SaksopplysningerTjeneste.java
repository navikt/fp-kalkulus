package no.nav.folketrygdloven.kalkulator.guitjenester.fakta.saksopplysninger;

import static no.nav.folketrygdloven.kalkulator.guitjenester.fakta.saksopplysninger.ArbeidMedLønnsendringTjeneste.lagArbeidsforholdMedLønnsendring;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.fakta.Saksopplysninger;

public class SaksopplysningerTjeneste {

    private SaksopplysningerTjeneste() {}

    public static Saksopplysninger lagSaksopplysninger(BeregningsgrunnlagGUIInput input) {
        Saksopplysninger saksopplysninger = new Saksopplysninger();
        // Kommenterer ut til vi får prodsatt k9-sak 01.11.2021 (brukes ikkje frontend enda)
//        saksopplysninger.setArbeidsforholdMedLønnsendring(lagArbeidsforholdMedLønnsendring(input));
        return saksopplysninger;
    }

}
