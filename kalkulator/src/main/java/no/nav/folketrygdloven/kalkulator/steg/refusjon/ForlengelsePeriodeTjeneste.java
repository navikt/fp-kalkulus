package no.nav.folketrygdloven.kalkulator.steg.refusjon;

import no.nav.folketrygdloven.kalkulator.KonfigurasjonVerdi;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.ytelse.psb.SplittBGPerioder;
import no.nav.folketrygdloven.kalkulus.kodeverk.PeriodeÅrsak;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;

public class ForlengelsePeriodeTjeneste {

    public static BeregningsgrunnlagDto splittVedStartAvForlengelse(BeregningsgrunnlagInput input, BeregningsgrunnlagDto beregningsgrunnlag) {
        if (KonfigurasjonVerdi.get("KOPIERING_VED_FORLENGELSE", false)) {
            var forlengelseSegmenter = input.getForlengelseperioder().stream()
                    .map(p -> new LocalDateSegment<>(p.getFomDato(), p.getTomDato(), Boolean.TRUE))
                    .toList();
            var forlengelseTidslinje = new LocalDateTimeline<>(forlengelseSegmenter);
            return SplittBGPerioder.splittPerioderOgSettPeriodeårsak(beregningsgrunnlag, forlengelseTidslinje.compress(), PeriodeÅrsak.ENDRING_I_AKTIVITETER_SØKT_FOR);
        }
        return beregningsgrunnlag;
    }

}
