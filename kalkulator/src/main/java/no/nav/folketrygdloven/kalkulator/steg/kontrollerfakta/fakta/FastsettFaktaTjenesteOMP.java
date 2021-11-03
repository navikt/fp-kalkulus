package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.fakta;

import static no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.fakta.FastsettFaktaKortvarigArbeidsforhold.fastsettFaktaForKortvarigeArbeidsforhold;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.input.FaktaOmBeregningInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;

@ApplicationScoped
@FagsakYtelseTypeRef("OMP")
public class FastsettFaktaTjenesteOMP implements FastsettFakta {

    public Optional<FaktaAggregatDto> fastsettFakta(BeregningsgrunnlagDto beregningsgrunnlag, InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        if (!harRefusjonPåSkjæringstidspunktet(beregningsgrunnlag.getSkjæringstidspunkt(), iayGrunnlag)) {
            List<FaktaArbeidsforholdDto> faktaArbeidsforholdDtos = fastsettFaktaForKortvarigeArbeidsforhold(beregningsgrunnlag, iayGrunnlag);
            if (!faktaArbeidsforholdDtos.isEmpty()) {
                FaktaAggregatDto.Builder faktaBuilder = FaktaAggregatDto.builder();
                faktaArbeidsforholdDtos.forEach(faktaBuilder::erstattEksisterendeEllerLeggTil);
                return Optional.of(faktaBuilder.build());
            }
        }
        return Optional.empty();
    }

    private boolean harRefusjonPåSkjæringstidspunktet(LocalDate skjæringstidspunktForBeregning, InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        List<InntektsmeldingDto> inntektsmeldinger = finnInntektsmeldinger(iayGrunnlag);
        return harMinstEnInntektsmeldingMedRefusjonFraSkjæringstidspunktet(inntektsmeldinger, skjæringstidspunktForBeregning);
    }

    private boolean harMinstEnInntektsmeldingMedRefusjonFraSkjæringstidspunktet(List<InntektsmeldingDto> inntektsmeldinger, LocalDate skjæringstidspunktForBeregning) {
        return inntektsmeldinger.stream().anyMatch(im -> harInntektsmeldingRefusjonFraStart(im) || harEndringIRefusjonFraSkjæringstidspunktet(im, skjæringstidspunktForBeregning));
    }

    private List<InntektsmeldingDto> finnInntektsmeldinger(InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        return iayGrunnlag.getInntektsmeldinger()
                .map(InntektsmeldingAggregatDto::getInntektsmeldingerSomSkalBrukes)
                .orElse(List.of());
    }

    private boolean harEndringIRefusjonFraSkjæringstidspunktet(InntektsmeldingDto im, LocalDate skjæringstidspunktForBeregning) {
        return im.getEndringerRefusjon().stream().anyMatch(er -> er.getFom().equals(skjæringstidspunktForBeregning) && !er.getRefusjonsbeløp().erNullEllerNulltall());
    }

    private boolean harInntektsmeldingRefusjonFraStart(InntektsmeldingDto im) {
        return im.getRefusjonBeløpPerMnd() != null
                && !im.getRefusjonBeløpPerMnd().erNullEllerNulltall();
    }

}
