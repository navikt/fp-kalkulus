package no.nav.folketrygdloven.kalkulus.mappers;

import static no.nav.folketrygdloven.kalkulus.mappers.MapFraKalkulator.validerForMidlertidigInaktivTypeA;

import no.nav.folketrygdloven.kalkulator.input.PleiepengerNærståendeGrunnlag;
import no.nav.folketrygdloven.kalkulator.konfig.KonfigTjeneste;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;

class PleiepengerNærståendeGrunnlagMapper {


    static PleiepengerNærståendeGrunnlag mapPleiepengerNærståendeGrunnlag(no.nav.folketrygdloven.kalkulus.beregning.v1.PleiepengerNærståendeGrunnlag ytelsespesifiktGrunnlag) {
        no.nav.folketrygdloven.kalkulus.beregning.v1.PleiepengerNærståendeGrunnlag ppnYtelsesGrunnlag = ytelsespesifiktGrunnlag;
        validerForMidlertidigInaktivTypeA(ppnYtelsesGrunnlag.getUtbetalingsgradPrAktivitet());
        var pleiepengerNærståendeGrunnlag = new PleiepengerNærståendeGrunnlag(
                UtbetalingsgradMapper.mapUtbetalingsgrad(ppnYtelsesGrunnlag.getUtbetalingsgradPrAktivitet()));
        pleiepengerNærståendeGrunnlag
                .setGrunnbeløpMilitærHarKravPå(KonfigTjeneste.forYtelse(FagsakYtelseType.PLEIEPENGER_NÆRSTÅENDE).getAntallGMilitærHarKravPå().intValue());
        return pleiepengerNærståendeGrunnlag;
    }

}
