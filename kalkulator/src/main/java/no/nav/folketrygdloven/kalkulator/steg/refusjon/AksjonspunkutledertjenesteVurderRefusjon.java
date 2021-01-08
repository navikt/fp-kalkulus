package no.nav.folketrygdloven.kalkulator.steg.refusjon;

import java.util.List;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningAksjonspunktResultat;

/**
 * Tjeneste for å utlede aksjonspunkter i steg for å vurdere refusjonskrav
 * Hvis det har kommet nye inntektsmeldinger siden forrige beregningsgrunnlag og disse leder til mindre
 * utbetaling til bruker skal det opprettes aksjonspunkt for å vurdere når disse refusjonskravene skal tas med.
 */
public interface AksjonspunkutledertjenesteVurderRefusjon {

    List<BeregningAksjonspunktResultat> utledAksjonspunkter(BeregningsgrunnlagInput input,
                                                                          BeregningsgrunnlagDto periodisertMedRefusjonOgGradering);
}
