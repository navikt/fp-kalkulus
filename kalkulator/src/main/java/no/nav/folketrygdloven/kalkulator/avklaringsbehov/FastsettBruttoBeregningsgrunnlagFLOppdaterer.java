package no.nav.folketrygdloven.kalkulator.avklaringsbehov;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.FaktaOmBeregningTilfelleRef;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FaktaBeregningLagreDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FastsettMånedsinntektFLDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.tilfeller.FaktaOmBeregningTilfelleOppdaterer;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;


@ApplicationScoped
@FaktaOmBeregningTilfelleRef("FASTSETT_MAANEDSINNTEKT_FL")
class FastsettBruttoBeregningsgrunnlagFLOppdaterer implements FaktaOmBeregningTilfelleOppdaterer {

    @Override
    public void oppdater(FaktaBeregningLagreDto dto, Optional<BeregningsgrunnlagDto> forrigeBg, BeregningsgrunnlagInput input, BeregningsgrunnlagGrunnlagDtoBuilder grunnlagBuilder) {
        FastsettMånedsinntektFLDto fastsettMånedsinntektFLDto = dto.getFastsettMaanedsinntektFL();
        Integer frilansinntekt = fastsettMånedsinntektFLDto.getMaanedsinntekt();
        BigDecimal årsinntektFL = BigDecimal.valueOf(frilansinntekt).multiply(BigDecimal.valueOf(12));
        List<BeregningsgrunnlagPeriodeDto> bgPerioder = grunnlagBuilder.getBeregningsgrunnlagBuilder().getBeregningsgrunnlag().getBeregningsgrunnlagPerioder();
        for (BeregningsgrunnlagPeriodeDto bgPeriode : bgPerioder) {
            BeregningsgrunnlagPrStatusOgAndelDto bgAndel = bgPeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(bpsa -> AktivitetStatus.FRILANSER.equals(bpsa.getAktivitetStatus()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Mangler BeregningsgrunnlagPrStatusOgAndel[FRILANS] for behandling " + input.getKoblingReferanse().getId()));
            BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(bgAndel)
                .medBeregnetPrÅr(årsinntektFL)
                .medFastsattAvSaksbehandler(true);
        }
    }

}
