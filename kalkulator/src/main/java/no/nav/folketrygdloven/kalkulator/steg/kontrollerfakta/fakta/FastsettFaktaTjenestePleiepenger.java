package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.fakta;

import static no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.fakta.FastsettFaktaKortvarigArbeidsforhold.fastsettFaktaForKortvarigeArbeidsforhold;
import static no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.fakta.FastsettFaktaLønnsendring.fastsettFaktaForLønnsendring;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;

@ApplicationScoped
@FagsakYtelseTypeRef("PSB")
@FagsakYtelseTypeRef("PPN")
public class FastsettFaktaTjenestePleiepenger implements FastsettFakta {

    public Optional<FaktaAggregatDto> fastsettFakta(BeregningsgrunnlagDto beregningsgrunnlag,
                                                    InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                                    Collection<InntektsmeldingDto> inntektsmeldinger) {
        List<FaktaArbeidsforholdDto> faktaKortvarig = fastsettFaktaForKortvarigeArbeidsforhold(beregningsgrunnlag, iayGrunnlag);
        FaktaAggregatDto.Builder faktaBuilder = FaktaAggregatDto.builder();
        faktaKortvarig.forEach(faktaBuilder::kopierTilEksisterendeEllerLeggTil);
        List<FaktaArbeidsforholdDto> faktaLønnsendring = fastsettFaktaForLønnsendring(beregningsgrunnlag, iayGrunnlag, inntektsmeldinger);
        faktaLønnsendring.forEach(faktaBuilder::kopierTilEksisterendeEllerLeggTil);
        if (!faktaBuilder.manglerFakta()) {
            return Optional.of(faktaBuilder.build());
        }
        return Optional.empty();
    }

}
