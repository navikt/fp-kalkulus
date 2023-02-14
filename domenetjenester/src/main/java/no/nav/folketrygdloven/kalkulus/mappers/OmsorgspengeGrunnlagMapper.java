package no.nav.folketrygdloven.kalkulus.mappers;

import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.konfig.KonfigTjeneste;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;

/**
 * Mapper for Omsorgspengegrunnlag
 */
class OmsorgspengeGrunnlagMapper {

    /**
     * Mapper omsorgspengegrunnlag
     *
     * @param omsorgspengerGrunnlag Omsorgspengegrunnlag fra input
     * @return Mappet omsorgspengegrunnlag til kalkulus
     */
    public static YtelsespesifiktGrunnlag mapOmsorgspengegrunnlag(no.nav.folketrygdloven.kalkulus.beregning.v1.OmsorgspengerGrunnlag omsorgspengerGrunnlag) {
        no.nav.folketrygdloven.kalkulator.input.OmsorgspengerGrunnlag kalkulatorGrunnlag = new no.nav.folketrygdloven.kalkulator.input.OmsorgspengerGrunnlag(
                UtbetalingsgradMapper.mapUtbetalingsgrad(omsorgspengerGrunnlag.getUtbetalingsgradPrAktivitet()));
        kalkulatorGrunnlag.setGrunnbeløpMilitærHarKravPå(KonfigTjeneste.forYtelse(FagsakYtelseType.OMSORGSPENGER).getAntallGMilitærHarKravPå().intValue());
        return kalkulatorGrunnlag;
    }

}
