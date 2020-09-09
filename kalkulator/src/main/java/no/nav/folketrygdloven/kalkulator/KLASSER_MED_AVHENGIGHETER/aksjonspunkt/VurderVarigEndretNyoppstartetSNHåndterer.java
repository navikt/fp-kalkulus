package no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt;

import java.math.BigDecimal;
import java.util.List;

import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.VurderVarigEndringEllerNyoppstartetSNDto;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand;


public class VurderVarigEndretNyoppstartetSNHåndterer {


    private VurderVarigEndretNyoppstartetSNHåndterer() {
        // Skjul
    }

    public static BeregningsgrunnlagGrunnlagDto håndter(BeregningsgrunnlagInput input, VurderVarigEndringEllerNyoppstartetSNDto dto) {
        Integer bruttoBeregningsgrunnlag = dto.getBruttoBeregningsgrunnlag();
        if (bruttoBeregningsgrunnlag != null) {
            BeregningsgrunnlagGrunnlagDtoBuilder grunnlagBuilder = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag());
            List<BeregningsgrunnlagPeriodeDto> bgPerioder = grunnlagBuilder.getBeregningsgrunnlagBuilder().getBeregningsgrunnlag().getBeregningsgrunnlagPerioder();
            for (BeregningsgrunnlagPeriodeDto bgPeriode : bgPerioder) {
                BeregningsgrunnlagPrStatusOgAndelDto bgAndel = bgPeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                    .filter(bpsa -> AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE.equals(bpsa.getAktivitetStatus()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Mangler BeregningsgrunnlagPrStatusOgAndel[SELVSTENDIG_NÆRINGSDRIVENDE] for behandling " + input.getKoblingReferanse().getKoblingId()));

                BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(bgAndel)
                    .medOverstyrtPrÅr(BigDecimal.valueOf(bruttoBeregningsgrunnlag));
            }
            return grunnlagBuilder.build(BeregningsgrunnlagTilstand.FORESLÅTT_UT);
        }
        return null;
    }

}
