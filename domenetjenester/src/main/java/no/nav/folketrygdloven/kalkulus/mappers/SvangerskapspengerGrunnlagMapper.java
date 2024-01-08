package no.nav.folketrygdloven.kalkulus.mappers;

import no.nav.folketrygdloven.kalkulator.input.SvangerskapspengerGrunnlag;

class SvangerskapspengerGrunnlagMapper {

    static SvangerskapspengerGrunnlag mapSvangerskapspengerGrunnlag(no.nav.folketrygdloven.kalkulus.beregning.v1.SvangerskapspengerGrunnlag svangerskapspengerGrunnlag) {
        SvangerskapspengerGrunnlag svpGrunnlag = new SvangerskapspengerGrunnlag(
                UtbetalingsgradMapper.mapUtbetalingsgrad(svangerskapspengerGrunnlag.getUtbetalingsgradPrAktivitet()), svangerskapspengerGrunnlag.getTilkommetInntektHensyntasFom());
        return svpGrunnlag;
    }

}
