package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.fakta;

import static no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.fakta.FastsettFaktaKortvarigArbeidsforhold.fastsettFaktaForKortvarigeArbeidsforhold;

import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;

@ApplicationScoped
@FagsakYtelseTypeRef("PSB")
public class FastsettFaktaTjenestePSB implements FastsettFakta {

    public Optional<FaktaAggregatDto> fastsettFakta(BeregningsgrunnlagDto beregningsgrunnlag, InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        List<FaktaArbeidsforholdDto> faktaArbeidsforholdDtos = fastsettFaktaForKortvarigeArbeidsforhold(beregningsgrunnlag, iayGrunnlag);
        if (!faktaArbeidsforholdDtos.isEmpty()) {
            FaktaAggregatDto.Builder faktaBuilder = FaktaAggregatDto.builder();
            faktaArbeidsforholdDtos.forEach(faktaBuilder::erstattEksisterendeEllerLeggTil);
            return Optional.of(faktaBuilder.build());
        }
        return Optional.empty();
    }

}
