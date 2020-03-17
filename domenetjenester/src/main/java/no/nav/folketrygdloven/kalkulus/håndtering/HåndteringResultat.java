package no.nav.folketrygdloven.kalkulus.håndtering;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.OppdateringRespons;

public class HåndteringResultat {

    private BeregningsgrunnlagGrunnlagDto nyttGrunnlag;

    private OppdateringRespons endring;

    public HåndteringResultat(BeregningsgrunnlagGrunnlagDto nyttGrunnlag, OppdateringRespons endring) {
        this.nyttGrunnlag = nyttGrunnlag;
        this.endring = endring;
    }


    public BeregningsgrunnlagGrunnlagDto getNyttGrunnlag() {
        return nyttGrunnlag;
    }

    public OppdateringRespons getEndring() {
        return endring;
    }
}
