package no.nav.folketrygdloven.kalkulator.kontrollerfakta.utledere;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Organisasjonstype;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.FaktaOmBeregningTilfelle;

@ApplicationScoped
public class FastsettMånedsinntektUtenInntektsmeldingTilfelleUtleder implements TilfelleUtleder {

    @Override
    public Optional<FaktaOmBeregningTilfelle> utled(BeregningsgrunnlagInput input,
                                                    BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag) {
        return utled(beregningsgrunnlagGrunnlag);
    }

    protected Optional<FaktaOmBeregningTilfelle> utled(BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag) {
        boolean harKunstigVirksomhet = harBeregningsgrunnlagKunstigVirksomhet(beregningsgrunnlagGrunnlag);
        boolean harAndelerForSammeVirksomhetMedOgUtenInntektsmelding = harArbeidstakerandelerForSammeVirksomhetMedOgUtenReferanse(beregningsgrunnlagGrunnlag);
        return harKunstigVirksomhet || harAndelerForSammeVirksomhetMedOgUtenInntektsmelding ? Optional.of(FaktaOmBeregningTilfelle.FASTSETT_MÅNEDSLØNN_ARBEIDSTAKER_UTEN_INNTEKTSMELDING) : Optional.empty();
    }

    private boolean harArbeidstakerandelerForSammeVirksomhetMedOgUtenReferanse(BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag) {
        Map<Arbeidsgiver, List<BeregningsgrunnlagPrStatusOgAndelDto>> arbeidsgiverTilAndelerMap = beregningsgrunnlagGrunnlag.getBeregningsgrunnlag()
                .map(bg -> bg.getBeregningsgrunnlagPerioder().get(0))
                .stream()
                .flatMap(p -> p.getBeregningsgrunnlagPrStatusOgAndelList().stream())
                .filter(a -> a.getAktivitetStatus().erArbeidstaker() && a.getArbeidsgiver().isPresent())
                .collect(Collectors.groupingBy(a -> a.getArbeidsgiver().orElseThrow(() -> new IllegalStateException("Forventer å ha arbeidsgiver her"))));
        return arbeidsgiverTilAndelerMap.entrySet().stream().anyMatch(entry -> {
            List<BeregningsgrunnlagPrStatusOgAndelDto> andeler = entry.getValue();
            long antallAndelerForSpesifikkeArbeidsforhold = andeler.stream().filter(a -> a.getArbeidsforholdRef().isPresent() && a.getArbeidsforholdRef().get().gjelderForSpesifiktArbeidsforhold()).count();
            long antallAndelerUtenReferanse = andeler.stream().filter(a -> a.getArbeidsforholdRef().isEmpty() || !a.getArbeidsforholdRef().get().gjelderForSpesifiktArbeidsforhold()).count();
            return antallAndelerUtenReferanse > 0 && antallAndelerForSpesifikkeArbeidsforhold > 0;
        });
    }

    private boolean harBeregningsgrunnlagKunstigVirksomhet(BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag) {
        return beregningsgrunnlagGrunnlag.getBeregningsgrunnlag()
                .stream()
                .flatMap(bg -> bg.getBeregningsgrunnlagPerioder().stream())
                .flatMap(p -> p.getBeregningsgrunnlagPrStatusOgAndelList().stream())
                .anyMatch(this::harKunstigArbeidsforhold);
    }


    private boolean harKunstigArbeidsforhold(BeregningsgrunnlagPrStatusOgAndelDto a) {
        if (a.getBgAndelArbeidsforhold().isEmpty()) {
            return false;
        }
        BGAndelArbeidsforholdDto bgAndelArbeidsforhold = a.getBgAndelArbeidsforhold().get();
        Arbeidsgiver arbeidsgiver = bgAndelArbeidsforhold.getArbeidsgiver();
        return arbeidsgiver.getErVirksomhet() && Organisasjonstype.erKunstig(arbeidsgiver.getOrgnr());
    }
}
