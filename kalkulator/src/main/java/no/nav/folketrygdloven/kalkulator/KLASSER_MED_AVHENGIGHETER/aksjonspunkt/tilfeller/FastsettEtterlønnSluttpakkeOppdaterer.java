package no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.tilfeller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.FaktaOmBeregningTilfelleRef;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FaktaBeregningLagreDto;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FastsettEtterlønnSluttpakkeDto;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;

@ApplicationScoped
@FaktaOmBeregningTilfelleRef("FASTSETT_ETTERLØNN_SLUTTPAKKE")
public class FastsettEtterlønnSluttpakkeOppdaterer implements FaktaOmBeregningTilfelleOppdaterer {

    private static final BigDecimal MÅNEDER_I_ET_ÅR = BigDecimal.valueOf(12);

    @Override
    public void oppdater(FaktaBeregningLagreDto dto, Optional<BeregningsgrunnlagDto> forrigeBg, BeregningsgrunnlagInput input, BeregningsgrunnlagGrunnlagDtoBuilder grunnlagBuilder) {
        FastsettEtterlønnSluttpakkeDto fastettDto = dto.getFastsettEtterlønnSluttpakke();
        List<BeregningsgrunnlagPrStatusOgAndelDto> etterlønnSluttpakkeAndeler = finnEtterlønnSluttpakkeAndel(grunnlagBuilder.getBeregningsgrunnlagBuilder().getBeregningsgrunnlag());
        Integer fastsattPrMnd = fastettDto.getFastsattPrMnd();
        if (etterlønnSluttpakkeAndeler.isEmpty() || fastsattPrMnd ==null) {
            throw new IllegalStateException("Finner ingen andeler på beregningsgrunnlaget med sluttpakke/etterlønn under fastsetting av inntekt");
        }
        BigDecimal nyVerdiEtterlønnSLuttpakke = BigDecimal.valueOf(fastsattPrMnd).multiply(MÅNEDER_I_ET_ÅR);
        etterlønnSluttpakkeAndeler.forEach(andel -> BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(andel)
            .medBeregnetPrÅr(nyVerdiEtterlønnSLuttpakke)
            .medFastsattAvSaksbehandler(true));
    }

    private List<BeregningsgrunnlagPrStatusOgAndelDto> finnEtterlønnSluttpakkeAndel(BeregningsgrunnlagDto nyttBeregningsgrunnlag) {
        return nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder()
            .get(0)
            .getBeregningsgrunnlagPrStatusOgAndelList()
            .stream()
            .filter(bpsa -> bpsa.getArbeidsforholdType().equals(OpptjeningAktivitetType.ETTERLØNN_SLUTTPAKKE))
            .collect(Collectors.toList());
    }
}
