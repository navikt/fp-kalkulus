package no.nav.folketrygdloven.kalkulator.kontrollerfakta.utledere;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.FaktaOmBeregningTilfelle;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Organisasjonstype;

@ApplicationScoped
public class FastsettMånedsinntektUtenInntektsmeldingTilfelleUtleder implements TilfelleUtleder {

    @Override
    public Optional<FaktaOmBeregningTilfelle> utled(BeregningsgrunnlagInput input,
                                                    BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag) {
        return utled(beregningsgrunnlagGrunnlag);
    }

    private Optional<FaktaOmBeregningTilfelle> utled(BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag) {
        boolean harKunstigVirksomhet = beregningsgrunnlagGrunnlag.getBeregningsgrunnlag()
            .stream()
            .flatMap(bg -> bg.getBeregningsgrunnlagPerioder().stream())
            .flatMap(p -> p.getBeregningsgrunnlagPrStatusOgAndelList().stream())
            .anyMatch(this::harKunstigArbeidsforhold);
        return harKunstigVirksomhet ? Optional.of(FaktaOmBeregningTilfelle.FASTSETT_MÅNEDSLØNN_ARBEIDSTAKER_UTEN_INNTEKTSMELDING) : Optional.empty();
    }

    private boolean harKunstigArbeidsforhold(BeregningsgrunnlagPrStatusOgAndelDto a) {
        if (!a.getBgAndelArbeidsforhold().isPresent()) {
            return false;
        }
        BGAndelArbeidsforholdDto bgAndelArbeidsforhold = a.getBgAndelArbeidsforhold().get();
        Arbeidsgiver arbeidsgiver = bgAndelArbeidsforhold.getArbeidsgiver();
        return arbeidsgiver.getErVirksomhet() && Organisasjonstype.erKunstig(arbeidsgiver.getOrgnr());
    }
}
