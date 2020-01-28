package no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.tilfeller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.FaktaOmBeregningTilfelleRef;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FaktaBeregningLagreDto;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.VurderEtterlønnSluttpakkeDto;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktivitetType;

@ApplicationScoped
@FaktaOmBeregningTilfelleRef("VURDER_ETTERLØNN_SLUTTPAKKE")
public class VurderEtterlønnSluttpakkeOppdaterer implements FaktaOmBeregningTilfelleOppdaterer {

    @Override
    public void oppdater(FaktaBeregningLagreDto dto, Optional<BeregningsgrunnlagDto> forrigeBg, BeregningsgrunnlagInput input, BeregningsgrunnlagGrunnlagDtoBuilder grunnlagBuilder) {
        VurderEtterlønnSluttpakkeDto vurderDto = dto.getVurderEtterlønnSluttpakke();
        List<BeregningsgrunnlagPrStatusOgAndelDto> etterlønnSluttpakkeAndel = finnEtterlønnSluttpakkeAndeler(grunnlagBuilder.getBeregningsgrunnlagBuilder().getBeregningsgrunnlag());
        if (!vurderDto.erEtterlønnSluttpakke()) {
            etterlønnSluttpakkeAndel.forEach(andel -> BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(andel)
                .medFastsattAvSaksbehandler(true)
                .medBeregnetPrÅr(BigDecimal.ZERO));
        }
    }

    private List<BeregningsgrunnlagPrStatusOgAndelDto> finnEtterlønnSluttpakkeAndeler(BeregningsgrunnlagDto beregningsgrunnlag) {
        return beregningsgrunnlag.getBeregningsgrunnlagPerioder()
                .get(0)
                .getBeregningsgrunnlagPrStatusOgAndelList()
                .stream()
                .filter(bpsa -> bpsa.getArbeidsforholdType().equals(OpptjeningAktivitetType.ETTERLØNN_SLUTTPAKKE))
                .collect(Collectors.toList());
    }
}
