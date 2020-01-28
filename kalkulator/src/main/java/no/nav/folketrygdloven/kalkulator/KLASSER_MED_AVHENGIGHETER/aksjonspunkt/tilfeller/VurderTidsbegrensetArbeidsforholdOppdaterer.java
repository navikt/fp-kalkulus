package no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.tilfeller;

import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.FaktaOmBeregningTilfelleRef;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FaktaBeregningLagreDto;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.VurderTidsbegrensetArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.VurderteArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

@ApplicationScoped
@FaktaOmBeregningTilfelleRef("VURDER_TIDSBEGRENSET_ARBEIDSFORHOLD")
public class VurderTidsbegrensetArbeidsforholdOppdaterer implements FaktaOmBeregningTilfelleOppdaterer {

    @Override
    public void oppdater(FaktaBeregningLagreDto dto, Optional<BeregningsgrunnlagDto> forrigeBg, BeregningsgrunnlagInput input, BeregningsgrunnlagGrunnlagDtoBuilder grunnlagBuilder) {
        VurderTidsbegrensetArbeidsforholdDto tidsbegrensetDto = dto.getVurderTidsbegrensetArbeidsforhold();
        BeregningsgrunnlagPeriodeDto periode = grunnlagBuilder.getBeregningsgrunnlagBuilder().getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(0);
        List<VurderteArbeidsforholdDto> fastsatteArbeidsforhold = tidsbegrensetDto.getFastsatteArbeidsforhold();
        for (VurderteArbeidsforholdDto arbeidsforhold : fastsatteArbeidsforhold) {
            BeregningsgrunnlagPrStatusOgAndelDto korrektAndel = periode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(a -> a.getAndelsnr().equals(arbeidsforhold.getAndelsnr()))
                .findFirst()
                .orElseThrow(() -> VurderTidsbegrensetArbeidsforholdOppdatererFeil.FACTORY.finnerIkkeAndelFeil().toException());
            BGAndelArbeidsforholdDto.Builder bgAndelArbeidsforholdDtoBuilder = BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(korrektAndel).getBgAndelArbeidsforholdDtoBuilder();
            BGAndelArbeidsforholdDto.Builder bgAndelArbeidsforhold = bgAndelArbeidsforholdDtoBuilder
                .medTidsbegrensetArbeidsforhold(arbeidsforhold.isTidsbegrensetArbeidsforhold());
            BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(korrektAndel)
                .medBGAndelArbeidsforhold(bgAndelArbeidsforhold);
        }
    }

    private interface VurderTidsbegrensetArbeidsforholdOppdatererFeil extends DeklarerteFeil {

        VurderTidsbegrensetArbeidsforholdOppdatererFeil FACTORY = FeilFactory.create(VurderTidsbegrensetArbeidsforholdOppdatererFeil.class);

        @TekniskFeil(feilkode = "FP-238175", feilmelding = "Finner ikke andelen for eksisterende grunnlag", logLevel = LogLevel.WARN)
        Feil finnerIkkeAndelFeil();
    }


}
