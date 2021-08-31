package no.nav.folketrygdloven.kalkulus.håndtering;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.Endringer;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.OppdateringRespons;

public class HåndteringResultat {

    private final BeregningsgrunnlagGrunnlagDto nyttGrunnlag;
    private final Endringer endring;

    public HåndteringResultat(BeregningsgrunnlagGrunnlagDto nyttGrunnlag, Endringer endring) {
        this.nyttGrunnlag = nyttGrunnlag;
        this.endring = endring;
    }


    public BeregningsgrunnlagGrunnlagDto getNyttGrunnlag() {
        return nyttGrunnlag;
    }

    public Endringer getEndring() {
        return endring;
    }
}
