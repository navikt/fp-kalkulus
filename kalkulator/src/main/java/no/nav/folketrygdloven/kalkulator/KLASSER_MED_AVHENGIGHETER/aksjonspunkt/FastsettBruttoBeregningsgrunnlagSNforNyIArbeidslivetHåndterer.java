package no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt;

import java.math.BigDecimal;

import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FastsettBruttoBeregningsgrunnlagSNforNyIArbeidslivetDto;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand;


public class FastsettBruttoBeregningsgrunnlagSNforNyIArbeidslivetHåndterer {

    public static BeregningsgrunnlagGrunnlagDto oppdater(BeregningsgrunnlagInput input, FastsettBruttoBeregningsgrunnlagSNforNyIArbeidslivetDto dto) {
        Integer bruttoBeregningsgrunnlag = dto.getBruttoBeregningsgrunnlag();
        BeregningsgrunnlagGrunnlagDtoBuilder grunnlagBuilder = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag());
        if (bruttoBeregningsgrunnlag != null) {
            BeregningsgrunnlagDto grunnlag = grunnlagBuilder.getBeregningsgrunnlagBuilder().getBeregningsgrunnlag();
            grunnlag.getBeregningsgrunnlagPerioder().forEach(bgPeriode -> {
                BeregningsgrunnlagPrStatusOgAndelDto bgAndel = bgPeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                    .filter(bpsa -> bpsa.getAktivitetStatus().erSelvstendigNæringsdrivende())
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Mangler andel for selvstendig næringsdrivende (eller kombinasjon med SN) for behandling "  + input.getKoblingReferanse().getKoblingId()));

                BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(bgAndel)
                    .medOverstyrtPrÅr(BigDecimal.valueOf(bruttoBeregningsgrunnlag))
                    .build(bgPeriode);
            });
        }
        return grunnlagBuilder.build(BeregningsgrunnlagTilstand.FORESLÅTT_UT);
    }
}
