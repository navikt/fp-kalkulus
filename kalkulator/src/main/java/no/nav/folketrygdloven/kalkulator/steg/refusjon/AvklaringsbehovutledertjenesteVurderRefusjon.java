package no.nav.folketrygdloven.kalkulator.steg.refusjon;

import java.util.List;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningAvklaringsbehovResultat;

/**
 * Tjeneste for å utlede avklaringsbehov i steg for å vurdere refusjonskrav
 * Hvis det har kommet nye inntektsmeldinger siden forrige beregningsgrunnlag og disse leder til mindre
 * utbetaling til bruker skal det opprettes avklaringsbehov for å vurdere når disse refusjonskravene skal tas med.
 */
public interface AvklaringsbehovutledertjenesteVurderRefusjon {

    List<BeregningAvklaringsbehovResultat> utledAvklaringsbehov(BeregningsgrunnlagInput input,
                                                                          BeregningsgrunnlagDto periodisertMedRefusjonOgGradering);
}
