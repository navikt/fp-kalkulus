package no.nav.folketrygdloven.kalkulus.domene.mappers;

import no.nav.folketrygdloven.kalkulator.input.SvangerskapspengerGrunnlag;

class SvangerskapspengerGrunnlagMapper {

    private SvangerskapspengerGrunnlagMapper() {
        // skjul konstruktor
    }

    static SvangerskapspengerGrunnlag mapSvangerskapspengerGrunnlag(no.nav.folketrygdloven.kalkulus.beregning.v1.SvangerskapspengerGrunnlag svangerskapspengerGrunnlag) {
        return new SvangerskapspengerGrunnlag(
            UtbetalingsgradMapper.mapUtbetalingsgrad(svangerskapspengerGrunnlag.getUtbetalingsgradPrAktivitet()),
            svangerskapspengerGrunnlag.getTilkommetInntektHensyntasFom());
    }

}
