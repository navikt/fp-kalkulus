package no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.tilfeller;

import java.math.BigDecimal;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.FaktaOmBeregningTilfelleRef;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FaktaBeregningLagreDto;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.VurderATogFLiSammeOrganisasjonAndelDto;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.VurderATogFLiSammeOrganisasjonDto;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;

@ApplicationScoped
@FaktaOmBeregningTilfelleRef("VURDER_AT_OG_FL_I_SAMME_ORGANISASJON")
class FastsettMånedsinntektATogFLiSammeOrganisasjonOppdaterer implements FaktaOmBeregningTilfelleOppdaterer {

    @Override
    public void oppdater(FaktaBeregningLagreDto dto, Optional<BeregningsgrunnlagDto> forrigeBg, BeregningsgrunnlagInput input, BeregningsgrunnlagGrunnlagDtoBuilder grunnlagBuilder) {
        VurderATogFLiSammeOrganisasjonDto vurderATFLISammeOrgDto = dto.getVurderATogFLiSammeOrganisasjon();
        vurderATFLISammeOrgDto.getVurderATogFLiSammeOrganisasjonAndelListe().forEach(dtoAndel ->
        {
            BeregningsgrunnlagDto beregningsgrunnlag = grunnlagBuilder.getBeregningsgrunnlagBuilder().getBeregningsgrunnlag();
            BeregningsgrunnlagPrStatusOgAndelDto andelIFørstePeriode = finnAndelIFørstePeriode(beregningsgrunnlag, dtoAndel);
            int årsinntekt = dtoAndel.getArbeidsinntekt() * 12;
            beregningsgrunnlag.getBeregningsgrunnlagPerioder().forEach(periode -> {
                BeregningsgrunnlagPrStatusOgAndelDto matchendeAndel = periode.getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(a -> a.equals(andelIFørstePeriode)).findFirst()
                    .orElseThrow(() -> new IllegalStateException("Fant ingen mactchende andel i periode med fom " + periode.getBeregningsgrunnlagPeriodeFom()));
                BeregningsgrunnlagPrStatusOgAndelDto.kopier(matchendeAndel)
                    .medBeregnetPrÅr(BigDecimal.valueOf(årsinntekt))
                    .medFastsattAvSaksbehandler(true);
            });
        });
    }

    private BeregningsgrunnlagPrStatusOgAndelDto finnAndelIFørstePeriode(BeregningsgrunnlagDto nyttBeregningsgrunnlag, VurderATogFLiSammeOrganisasjonAndelDto dtoAndel) {
        return nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0)
                    .getBeregningsgrunnlagPrStatusOgAndelList().stream()
                    .filter(bpsa -> bpsa.getAndelsnr().equals(dtoAndel.getAndelsnr()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Fant ikke andel i første periode med andelsnr " + dtoAndel.getAndelsnr()));
    }

}
