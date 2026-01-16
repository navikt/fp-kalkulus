package no.nav.folketrygdloven.kalkulus.domene.mappers;

import no.nav.folketrygdloven.kalkulator.input.SvangerskapspengerGrunnlag;

class SvangerskapspengerGrunnlagMapper {

    private SvangerskapspengerGrunnlagMapper() {
        // skjul konstruktor
    }

    static SvangerskapspengerGrunnlag mapSvangerskapspengerGrunnlag(no.nav.foreldrepenger.kalkulus.kontrakt.request.input.svangerskapspenger.SvangerskapspengerGrunnlag svangerskapspengerGrunnlag) {
        return new SvangerskapspengerGrunnlag(
            UtbetalingsgradMapper.mapUtbetalingsgrad(svangerskapspengerGrunnlag.getUtbetalingsgradPrAktivitet()),
            svangerskapspengerGrunnlag.getTilkommetInntektHensyntasFom());
    }

}
