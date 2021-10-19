package no.nav.folketrygdloven.kalkulator.guitjenester.fakta.saksopplysninger;


import static no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.beregningsperiode.FastsettBeregningsperiodeForLønnsendring.finnAndelAktivitetMap;
import static no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.beregningsperiode.FastsettBeregningsperiodeForLønnsendring.finnSisteLønnsendringIBeregningsperioden;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.guitjenester.fakta.FaktaOmBeregningAndelDtoTjeneste;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.LønnsendringTjeneste;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.beregningsperiode.BeregningsperiodeTjeneste;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.fakta.ArbeidsforholdMedLønnsendring;

class ArbeidMedLønnsendringTjeneste {

    private ArbeidMedLønnsendringTjeneste() {
    }

    static List<ArbeidsforholdMedLønnsendring> lagArbeidsforholdMedLønnsendring(BeregningsgrunnlagGUIInput input) {
        Intervall beregningsperiode = new BeregningsperiodeTjeneste().fastsettBeregningsperiodeForATFLAndeler(input.getSkjæringstidspunktForBeregning());
        List<YrkesaktivitetDto> aktiviteterMedLønnsendring = LønnsendringTjeneste.finnAktiviteterMedLønnsendringUtenInntektsmelding(input.getBeregningsgrunnlag(), input.getIayGrunnlag(), beregningsperiode);
        Map<BeregningsgrunnlagPrStatusOgAndelDto, List<YrkesaktivitetDto>> andelAktivitetMap = finnAndelAktivitetMap(aktiviteterMedLønnsendring, input.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(0));
        List<ArbeidsforholdMedLønnsendring> arbeidsforholdMedLønnsendringList = andelAktivitetMap.entrySet().stream().map(e -> {
            var faktaAndel = FaktaOmBeregningAndelDtoTjeneste.lagArbeidsforholdUtenInntektsmeldingDto(e.getKey(), input.getIayGrunnlag());
            return new ArbeidsforholdMedLønnsendring(faktaAndel, finnSisteLønnsendringIBeregningsperioden(e.getValue(), input.getSkjæringstidspunktForBeregning()));
        }).collect(Collectors.toList());
        return arbeidsforholdMedLønnsendringList;
    }

}
