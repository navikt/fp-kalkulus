package no.nav.folketrygdloven.kalkulator.avklaringsbehov;

import java.util.List;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.typer.Beløp;


public class FastsettBruttoBeregningsgrunnlagSNHåndterer {

    private FastsettBruttoBeregningsgrunnlagSNHåndterer() {
        // Skjul
    }

    public static BeregningsgrunnlagGrunnlagDto håndter(BeregningsgrunnlagInput input, Integer bruttoBeregningsgrunnlag) {
        BeregningsgrunnlagGrunnlagDtoBuilder grunnlagBuilder = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag());
        if (bruttoBeregningsgrunnlag != null) {
            BeregningsgrunnlagDto grunnlag = grunnlagBuilder.getBeregningsgrunnlagBuilder().getBeregningsgrunnlag();
            List<BeregningsgrunnlagPeriodeDto> bgPerioder = grunnlag.getBeregningsgrunnlagPerioder();
            for (BeregningsgrunnlagPeriodeDto bgPeriode : bgPerioder) {
                BeregningsgrunnlagPrStatusOgAndelDto bgAndel = bgPeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                    .filter(bpsa -> AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE.equals(bpsa.getAktivitetStatus()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Mangler BeregningsgrunnlagPrStatusOgAndel[SELVSTENDIG_NÆRINGSDRIVENDE] for behandling " + input.getKoblingReferanse().getKoblingId()));

                BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(bgAndel)
                    .medOverstyrtPrÅr(Beløp.fra(bruttoBeregningsgrunnlag))
                    .build(bgPeriode);
            }
        }
        return grunnlagBuilder.build(BeregningsgrunnlagTilstand.FORESLÅTT_UT);
    }

}
