package no.nav.folketrygdloven.kalkulator.rest.fakta;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;


public class SkalKunneEndreAktivitet {

    private SkalKunneEndreAktivitet() {
        // Hide constructor
    }

    /**
     * Vurderer om ein gitt andel skal kunne endres i gui.
     *
     * Endring vil seie å kunne slette eller endre arbeidsforhold i nedtrekksmeny for andelen.
     *
     * @param andel Ein gitt beregningsgrunnlagsandel
     * @return boolean som seier om andel/aktivitet skal kunne endres i gui
     */
    public static Boolean skalKunneEndreAktivitet(BeregningsgrunnlagPrStatusOgAndelDto andel) {
        return andel.getLagtTilAvSaksbehandler() && !andel.getAktivitetStatus().equals(AktivitetStatus.DAGPENGER);
    }

}
