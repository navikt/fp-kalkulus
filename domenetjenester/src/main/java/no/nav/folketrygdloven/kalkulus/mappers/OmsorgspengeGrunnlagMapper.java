package no.nav.folketrygdloven.kalkulus.mappers;

import static no.nav.folketrygdloven.kalkulus.mappers.MapFraKalkulator.validerForMidlertidigInaktivTypeA;
import static no.nav.folketrygdloven.kalkulus.mappers.UtbetalingsgradMapper.mapArbeidsforhold;

import java.util.List;

import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.konfig.KonfigTjeneste;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.beregning.v1.SøknadsperioderPrAktivitetDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;

/**
 * Mapper for Omsorgspengegrunnlag
 */
class OmsorgspengeGrunnlagMapper {

    /**
     * Mapper omsorgspengegrunnlag
     *
     * @param omsorgspengerGrunnlag           Omsorgspengegrunnlag fra input
     * @return Mappet omsorgspengegrunnlag til kalkulus
     */
    public static YtelsespesifiktGrunnlag mapOmsorgspengegrunnlag(no.nav.folketrygdloven.kalkulus.beregning.v1.OmsorgspengerGrunnlag omsorgspengerGrunnlag) {
        validerForMidlertidigInaktivTypeA(omsorgspengerGrunnlag.getUtbetalingsgradPrAktivitet());
        no.nav.folketrygdloven.kalkulator.input.OmsorgspengerGrunnlag kalkulatorGrunnlag = new no.nav.folketrygdloven.kalkulator.input.OmsorgspengerGrunnlag(
                UtbetalingsgradMapper.mapUtbetalingsgrad(omsorgspengerGrunnlag.getUtbetalingsgradPrAktivitet()), mapSøknadsperioder(omsorgspengerGrunnlag.getSøknadsperioderPrAktivitet()));
        kalkulatorGrunnlag.setGrunnbeløpMilitærHarKravPå(KonfigTjeneste.forYtelse(FagsakYtelseType.OMSORGSPENGER).getAntallGMilitærHarKravPå().intValue());
        return kalkulatorGrunnlag;
    }

    private static List<no.nav.folketrygdloven.kalkulator.modell.omp.SøknadsperioderPrAktivitetDto> mapSøknadsperioder(List<SøknadsperioderPrAktivitetDto> søknadsperioderPrAktivitet) {
        return søknadsperioderPrAktivitet == null ? null : søknadsperioderPrAktivitet.stream()
                .map(OmsorgspengeGrunnlagMapper::mapSøknadsperiode).toList();
    }

    private static no.nav.folketrygdloven.kalkulator.modell.omp.SøknadsperioderPrAktivitetDto mapSøknadsperiode(SøknadsperioderPrAktivitetDto s) {
        return s == null ? null : new no.nav.folketrygdloven.kalkulator.modell.omp.SøknadsperioderPrAktivitetDto(mapArbeidsforhold(s.getAktivitet()), mapPerioder(s));
    }

    private static List<Intervall> mapPerioder(SøknadsperioderPrAktivitetDto s) {
        return s.getPerioder().stream().map(p -> Intervall.fraOgMedTilOgMed(p.getFom(), p.getTom())).toList();
    }
}
