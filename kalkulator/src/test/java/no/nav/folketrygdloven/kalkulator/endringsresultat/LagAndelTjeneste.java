package no.nav.folketrygdloven.kalkulator.endringsresultat;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;

public interface LagAndelTjeneste {

    public void lagAndeler(BeregningsgrunnlagPeriodeDto periode, boolean medOppjustertDagsat, boolean skalDeleAndelMellomArbeidsgiverOgBruker);

}
