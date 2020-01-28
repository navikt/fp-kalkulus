package no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.tilfeller;

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
            arbeidstakerAndeler.forEach(andel ->{
                BGAndelArbeidsforholdDto.Builder bgAndelArbeidsforholdDtoBuilder = BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(andel).getBgAndelArbeidsforholdDtoBuilder();
                BGAndelArbeidsforholdDto.Builder bgAndelArbeidsforhold = bgAndelArbeidsforholdDtoBuilder
                    .medLønnsendringIBeregningsperioden(true);
                BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(andel)
                    .medBGAndelArbeidsforhold(bgAndelArbeidsforhold);
            });
        } else {
            arbeidstakerAndeler.forEach(bgAndel ->{
                BGAndelArbeidsforholdDto.Builder bgAndelArbeidsforholdDtoBuilder = BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(bgAndel).getBgAndelArbeidsforholdDtoBuilder();
                BGAndelArbeidsforholdDto.Builder bgAndelArbeidsforhold = bgAndelArbeidsforholdDtoBuilder
                    .medLønnsendringIBeregningsperioden(false);
                BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(bgAndel)
                    .medBGAndelArbeidsforhold(bgAndelArbeidsforhold);
            });
        }
    }

}
