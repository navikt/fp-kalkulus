package no.nav.folketrygdloven.kalkulus.mappers;

import no.nav.folketrygdloven.kalkulator.input.OpplæringspengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.konfig.KonfigTjeneste;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;

class OpplæringspengerGrunnlagMapper {

    static OpplæringspengerGrunnlag mapGrunnlag(no.nav.folketrygdloven.kalkulus.beregning.v1.OpplæringspengerGrunnlag pleiepengerYtelsesGrunnlag) {
        var pleiepengerSyktBarnGrunnlag = new OpplæringspengerGrunnlag(
                UtbetalingsgradMapper.mapUtbetalingsgrad(pleiepengerYtelsesGrunnlag.getUtbetalingsgradPrAktivitet()));
        pleiepengerSyktBarnGrunnlag
                .setGrunnbeløpMilitærHarKravPå(KonfigTjeneste.forYtelse(FagsakYtelseType.OPPLÆRINGSPENGER).getAntallGMilitærHarKravPå().intValue());
        return pleiepengerSyktBarnGrunnlag;
    }


}
