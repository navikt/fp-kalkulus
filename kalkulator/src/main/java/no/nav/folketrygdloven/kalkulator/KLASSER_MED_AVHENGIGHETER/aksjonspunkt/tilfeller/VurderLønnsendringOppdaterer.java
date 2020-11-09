package no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.tilfeller;

import static no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.LønnsendringTjeneste.finnAlleAktiviteterMedLønnsendringUtenInntektsmelding;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.FaktaOmBeregningTilfelleRef;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FaktaBeregningLagreDto;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.VurderLønnsendringDto;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;

@ApplicationScoped
@FaktaOmBeregningTilfelleRef("VURDER_LØNNSENDRING")
public class VurderLønnsendringOppdaterer implements FaktaOmBeregningTilfelleOppdaterer {

    @Override
    public void oppdater(FaktaBeregningLagreDto dto, Optional<BeregningsgrunnlagDto> forrigeBg, BeregningsgrunnlagInput input, BeregningsgrunnlagGrunnlagDtoBuilder grunnlagBuilder) {
        VurderLønnsendringDto lønnsendringDto = dto.getVurdertLonnsendring();
        List<BeregningsgrunnlagPrStatusOgAndelDto> arbeidstakerAndeler = grunnlagBuilder.getBeregningsgrunnlagBuilder().getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().stream()
                .map(BeregningsgrunnlagPeriodeDto::getBeregningsgrunnlagPrStatusOgAndelList).flatMap(Collection::stream)
                .filter(bpsa -> bpsa.getAktivitetStatus().erArbeidstaker())
                .collect(Collectors.toList());

        if (lønnsendringDto.erLønnsendringIBeregningsperioden()) {
            arbeidstakerAndeler.forEach(andel -> {
                BGAndelArbeidsforholdDto.Builder bgAndelArbeidsforholdDtoBuilder = BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(andel).getBgAndelArbeidsforholdDtoBuilder();
                BGAndelArbeidsforholdDto.Builder bgAndelArbeidsforhold = bgAndelArbeidsforholdDtoBuilder
                        .medLønnsendringIBeregningsperioden(true);
                BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(andel)
                        .medBGAndelArbeidsforhold(bgAndelArbeidsforhold);
            });
        } else {
            arbeidstakerAndeler.forEach(bgAndel -> {
                BGAndelArbeidsforholdDto.Builder bgAndelArbeidsforholdDtoBuilder = BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(bgAndel).getBgAndelArbeidsforholdDtoBuilder();
                BGAndelArbeidsforholdDto.Builder bgAndelArbeidsforhold = bgAndelArbeidsforholdDtoBuilder
                        .medLønnsendringIBeregningsperioden(false);
                BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(bgAndel)
                        .medBGAndelArbeidsforhold(bgAndelArbeidsforhold);
            });
        }

        List<YrkesaktivitetDto> aktiviteterMedLønnsendring = finnAlleAktiviteterMedLønnsendringUtenInntektsmelding(input.getAktørId(), input.getBeregningsgrunnlag(), input.getIayGrunnlag());

        FaktaAggregatDto.Builder faktaAggregatBuilder = grunnlagBuilder.getFaktaAggregatBuilder();
        arbeidstakerAndeler.stream().filter(a -> a.getBgAndelArbeidsforhold().isPresent())
                .map(BeregningsgrunnlagPrStatusOgAndelDto::getBgAndelArbeidsforhold)
                .map(Optional::get)
                .filter(arb -> aktiviteterMedLønnsendring.stream().anyMatch(ya -> ya.getArbeidsgiver().equals(arb.getArbeidsgiver()) &&
                        ya.getArbeidsforholdRef().gjelderFor(arb.getArbeidsforholdRef())))
                .forEach(arb -> {
                    FaktaArbeidsforholdDto.Builder faktaArbBuilder = faktaAggregatBuilder.getFaktaArbeidsforholdBuilderFor(arb.getArbeidsgiver(), arb.getArbeidsforholdRef())
                            .medHarLønnsendringIBeregningsperioden(lønnsendringDto.erLønnsendringIBeregningsperioden());
                    faktaAggregatBuilder.erstattEksisterendeEllerLeggTil(faktaArbBuilder.build());
                });
        grunnlagBuilder.medFaktaAggregat(faktaAggregatBuilder.build());
    }

}
