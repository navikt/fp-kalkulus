package no.nav.folketrygdloven.kalkulus.håndtering.refusjon;

import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringerDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.Endringer;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.RefusjonoverstyringEndring;

public final class UtledEndringForRefusjonOverstyring {

    private UtledEndringForRefusjonOverstyring() {
        // skjul
    }

    public static Endringer utled(BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlagDto, Optional<BeregningsgrunnlagGrunnlagDto> forrigeGrunnlag) {
        BeregningRefusjonOverstyringerDto refusjonOverstyringer = beregningsgrunnlagGrunnlagDto.getRefusjonOverstyringer()
                .orElseThrow(() -> new IllegalArgumentException("Skal ha refusjonoverstyringer her"));
        Optional<BeregningRefusjonOverstyringerDto> forrigeRefusjonOverstyringer = forrigeGrunnlag.flatMap(BeregningsgrunnlagGrunnlagDto::getRefusjonOverstyringer);
        RefusjonoverstyringEndring refusjonoverstyringEndring = UtledEndringIRefusjonsperiode.utledRefusjonoverstyringEndring(refusjonOverstyringer,
                beregningsgrunnlagGrunnlagDto.getBeregningsgrunnlagHvisFinnes().orElseThrow(),
                forrigeRefusjonOverstyringer,
                forrigeGrunnlag.stream().flatMap(gr -> gr.getBeregningsgrunnlagHvisFinnes().stream()).findFirst());
        return new Endringer(refusjonoverstyringEndring);
    }
}
