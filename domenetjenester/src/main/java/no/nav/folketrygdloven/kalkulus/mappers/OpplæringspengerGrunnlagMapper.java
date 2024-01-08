package no.nav.folketrygdloven.kalkulus.mappers;

import no.nav.folketrygdloven.kalkulator.input.OpplæringspengerGrunnlag;

class OpplæringspengerGrunnlagMapper {

    static OpplæringspengerGrunnlag mapGrunnlag(no.nav.folketrygdloven.kalkulus.beregning.v1.OpplæringspengerGrunnlag pleiepengerYtelsesGrunnlag) {
        var pleiepengerSyktBarnGrunnlag = new OpplæringspengerGrunnlag(
                UtbetalingsgradMapper.mapUtbetalingsgrad(pleiepengerYtelsesGrunnlag.getUtbetalingsgradPrAktivitet()));
        return pleiepengerSyktBarnGrunnlag;
    }


}
