package no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.refusjon;

import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.VurderRefusjonBeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringerDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand;

public class VurderRefusjonBeregningsgrunnlagHåndterer {

    private VurderRefusjonBeregningsgrunnlagHåndterer() {
        // skjul
    }

    public static BeregningsgrunnlagGrunnlagDto håndter(VurderRefusjonBeregningsgrunnlagDto dto, BeregningsgrunnlagInput input) {

        // Lag overstyringsobjekter
        BeregningRefusjonOverstyringerDto refusjonOverstyringer = MapTilRefusjonOverstyring.map(dto, input);

        // Periodiser og fastsett refusjon på eksisterende beregningsgrunnlag basert på data fra overstyringsobjekter
        BeregningsgrunnlagDto beregningsgrunnlag = input.getBeregningsgrunnlagGrunnlag().getBeregningsgrunnlag().orElseThrow();
        BeregningsgrunnlagDto periodisertPåFastsattRefusjon = PeriodiserOgFastsettRefusjonTjeneste.periodiserOgFastsett(beregningsgrunnlag, dto.getAndeler());

        // Lag nytt aggregat og sett korrekt tilstand
        BeregningsgrunnlagGrunnlagDtoBuilder grunnlagBuilder = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag());
        grunnlagBuilder.medRefusjonOverstyring(refusjonOverstyringer);
        grunnlagBuilder.medBeregningsgrunnlag(periodisertPåFastsattRefusjon);
        return grunnlagBuilder.build(BeregningsgrunnlagTilstand.VURDERT_REFUSJON_UT);
    }

}
