package no.nav.folketrygdloven.kalkulator.avklaringsbehov;

import java.util.List;

import no.nav.folketrygdloven.kalkulator.input.HåndterBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.ytelse.psb.SplittBGPerioder;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fordeling.VurderRepresentererStortingetHåndteringDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.PeriodeÅrsak;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;

public class VurderRepresentererStortingetTjeneste {

    public static BeregningsgrunnlagGrunnlagDto løsAvklaringsbehov(VurderRepresentererStortingetHåndteringDto vurderDto, HåndterBeregningsgrunnlagInput input) {
        BeregningsgrunnlagGrunnlagDtoBuilder grunnlagBuilder = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag());
        if (vurderDto.getRepresentererStortinget()) {
            var nyttBg = SplittBGPerioder.splittPerioderOgSettPeriodeårsak(input.getBeregningsgrunnlag(),
                    input.getForlengelseperioder(),
                    new LocalDateTimeline<>(List.of(new LocalDateSegment<>(vurderDto.getFom(), vurderDto.getTom(), true))),
                    PeriodeÅrsak.REPRESENTERER_STORTINGET);
            grunnlagBuilder.medBeregningsgrunnlag(nyttBg);
        }
        return grunnlagBuilder.build(BeregningsgrunnlagTilstand.FASTSATT_INN);
    }

}
