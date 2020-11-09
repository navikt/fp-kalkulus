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
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaArbeidsforholdDto;
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
        FaktaAggregatDto.Builder faktaAggregatBuilder = grunnlagBuilder.getFaktaAggregatBuilder();
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

            // Setter Fakta-aggregat
            BGAndelArbeidsforholdDto arbeidsforholdDto = korrektAndel.getBgAndelArbeidsforhold()
                    .orElseThrow(() -> VurderTidsbegrensetArbeidsforholdOppdatererFeil.FACTORY.finnerIkkeArbeidsforholdFeil().toException());
            FaktaArbeidsforholdDto.Builder faktaArbBuilder = faktaAggregatBuilder.getFaktaArbeidsforholdBuilderFor(arbeidsforholdDto.getArbeidsgiver(), arbeidsforholdDto.getArbeidsforholdRef())
                    .medErTidsbegrenset(arbeidsforhold.isTidsbegrensetArbeidsforhold());
            faktaAggregatBuilder.erstattEksisterendeEllerLeggTil(faktaArbBuilder.build());
            grunnlagBuilder.medFaktaAggregat(faktaAggregatBuilder.build());
        }
    }

    private interface VurderTidsbegrensetArbeidsforholdOppdatererFeil extends DeklarerteFeil {

        VurderTidsbegrensetArbeidsforholdOppdatererFeil FACTORY = FeilFactory.create(VurderTidsbegrensetArbeidsforholdOppdatererFeil.class);

        @TekniskFeil(feilkode = "FT-238175", feilmelding = "Finner ikke andelen for eksisterende grunnlag", logLevel = LogLevel.WARN)
        Feil finnerIkkeAndelFeil();


        @TekniskFeil(feilkode = "FT-238176", feilmelding = "Finner ikke arbeidsforhold for eksisterende andel", logLevel = LogLevel.WARN)
        Feil finnerIkkeArbeidsforholdFeil();
    }


}
