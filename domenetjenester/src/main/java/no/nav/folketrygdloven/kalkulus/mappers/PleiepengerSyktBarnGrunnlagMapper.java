package no.nav.folketrygdloven.kalkulus.mappers;

import no.nav.folketrygdloven.kalkulator.input.PleiepengerSyktBarnGrunnlag;
import no.nav.folketrygdloven.kalkulator.konfig.KonfigTjeneste;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;

class PleiepengerSyktBarnGrunnlagMapper {

    static PleiepengerSyktBarnGrunnlag mapPleiepengerSyktBarnGrunnlag(no.nav.folketrygdloven.kalkulus.beregning.v1.PleiepengerSyktBarnGrunnlag pleiepengerYtelsesGrunnlag) {
        PleiepengerSyktBarnGrunnlag pleiepengerSyktBarnGrunnlag = new PleiepengerSyktBarnGrunnlag(
                UtbetalingsgradMapper.mapUtbetalingsgrad(pleiepengerYtelsesGrunnlag.getUtbetalingsgradPrAktivitet()));
        pleiepengerSyktBarnGrunnlag
                .setGrunnbeløpMilitærHarKravPå(KonfigTjeneste.forYtelse(FagsakYtelseType.PLEIEPENGER_SYKT_BARN).getAntallGMilitærHarKravPå().intValue());
        return pleiepengerSyktBarnGrunnlag;
    }


}
