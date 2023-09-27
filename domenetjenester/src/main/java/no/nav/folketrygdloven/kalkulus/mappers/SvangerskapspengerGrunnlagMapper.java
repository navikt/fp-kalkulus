package no.nav.folketrygdloven.kalkulus.mappers;

import no.nav.folketrygdloven.kalkulator.input.SvangerskapspengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.konfig.KonfigTjeneste;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;

class SvangerskapspengerGrunnlagMapper {

    static SvangerskapspengerGrunnlag mapSvangerskapspengerGrunnlag(no.nav.folketrygdloven.kalkulus.beregning.v1.SvangerskapspengerGrunnlag svangerskapspengerGrunnlag) {
        SvangerskapspengerGrunnlag svpGrunnlag = new SvangerskapspengerGrunnlag(
                UtbetalingsgradMapper.mapUtbetalingsgrad(svangerskapspengerGrunnlag.getUtbetalingsgradPrAktivitet()), svangerskapspengerGrunnlag.getTilkommetInntektHensyntasFom());
        svpGrunnlag
                .setGrunnbeløpMilitærHarKravPå(KonfigTjeneste.forYtelse(FagsakYtelseType.SVANGERSKAPSPENGER).getAntallGMilitærHarKravPå().intValue());
        return svpGrunnlag;
    }

}
