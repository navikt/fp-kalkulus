package no.nav.folketrygdloven.kalkulus.mappers;

import java.util.List;

import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.beregning.v1.SøktPeriode;

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
                UtbetalingsgradMapper.mapUtbetalingsgrad(omsorgspengerGrunnlag.getUtbetalingsgradPrAktivitet()), mapBrukerSøkerPerioder(omsorgspengerGrunnlag.getSøktePerioder()));
        return kalkulatorGrunnlag;
    }

    private static List<Intervall> mapBrukerSøkerPerioder(List<SøktPeriode> søktePerioder) {
        if (søktePerioder != null) {
            return søktePerioder.stream().filter(SøktPeriode::getHarBrukerSøkt).map(SøktPeriode::getPeriode)
                    .map(it -> Intervall.fraOgMedTilOgMed(it.getFom(), it.getTom())).toList();
        }
        return null;
    }

}
