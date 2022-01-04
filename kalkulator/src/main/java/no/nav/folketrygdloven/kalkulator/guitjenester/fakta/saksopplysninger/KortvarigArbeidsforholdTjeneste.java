package no.nav.folketrygdloven.kalkulator.guitjenester.fakta.saksopplysninger;

import java.util.List;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.fakta.ArbeidsforholdDto;

class KortvarigArbeidsforholdTjeneste {

    static List<ArbeidsforholdDto> lagKortvarigeArbeidsforhold(BeregningsgrunnlagGUIInput input) {
        var tidsbegrensedeArbeidsforhold = input.getFaktaAggregat().stream()
                .flatMap(f -> f.getFaktaArbeidsforhold().stream())
                .filter(FaktaArbeidsforholdDto::getErTidsbegrensetVurdering);
        return tidsbegrensedeArbeidsforhold.map(a -> new ArbeidsforholdDto(a.getArbeidsgiver().getIdentifikator(), a.getArbeidsforholdRef().getReferanse()))
                .collect(Collectors.toList());
    }

}
