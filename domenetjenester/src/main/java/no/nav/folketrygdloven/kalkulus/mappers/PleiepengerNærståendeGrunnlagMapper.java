package no.nav.folketrygdloven.kalkulus.mappers;

import no.nav.folketrygdloven.kalkulator.input.PleiepengerNærståendeGrunnlag;


class PleiepengerNærståendeGrunnlagMapper {


    static PleiepengerNærståendeGrunnlag mapPleiepengerNærståendeGrunnlag(no.nav.folketrygdloven.kalkulus.beregning.v1.PleiepengerNærståendeGrunnlag ytelsespesifiktGrunnlag) {
        no.nav.folketrygdloven.kalkulus.beregning.v1.PleiepengerNærståendeGrunnlag ppnYtelsesGrunnlag = ytelsespesifiktGrunnlag;
        var tilretteleggingMedUtbelingsgrad = UtbetalingsgradMapper.mapUtbetalingsgrad(ppnYtelsesGrunnlag.getUtbetalingsgradPrAktivitet());
        var pleiepengerNærståendeGrunnlag = ppnYtelsesGrunnlag.getTilkommetInntektHensyntasFom() == null
                ? new PleiepengerNærståendeGrunnlag(tilretteleggingMedUtbelingsgrad)
                : new PleiepengerNærståendeGrunnlag(tilretteleggingMedUtbelingsgrad, ppnYtelsesGrunnlag.getTilkommetInntektHensyntasFom());
        return pleiepengerNærståendeGrunnlag;
    }

}
