package no.nav.folketrygdloven.kalkulator.steg.refusjon;

import static no.nav.folketrygdloven.kalkulus.kodeverk.PeriodeÅrsak.ENDRING_I_AKTIVITETER_SØKT_FOR;

import no.nav.folketrygdloven.kalkulator.KonfigurasjonVerdi;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.felles.periodesplitting.PeriodeSplitter;
import no.nav.folketrygdloven.kalkulator.felles.periodesplitting.SplittPeriodeConfig;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;

public class ForlengelsePeriodeTjeneste {

    public static BeregningsgrunnlagDto splittVedStartAvForlengelse(BeregningsgrunnlagInput input, BeregningsgrunnlagDto beregningsgrunnlag) {
        if (KonfigurasjonVerdi.get("KOPIERING_VED_FORLENGELSE", false)) {
            var forlengelseSegmenter = input.getForlengelseperioder().stream()
                    .map(p -> new LocalDateSegment<>(p.getFomDato(), p.getTomDato(), Boolean.TRUE))
                    .toList();
            var forlengelseTidslinje = new LocalDateTimeline<>(forlengelseSegmenter);
            getPeriodeSplitter(input).splittPerioder(beregningsgrunnlag, forlengelseTidslinje);
        }
        return beregningsgrunnlag;
    }

    private static PeriodeSplitter<Boolean> getPeriodeSplitter(BeregningsgrunnlagInput input) {
        SplittPeriodeConfig<Boolean> splittPeriodeConfig = SplittPeriodeConfig.medAvsluttetPeriodeårsakConfig(ENDRING_I_AKTIVITETER_SØKT_FOR, ENDRING_I_AKTIVITETER_SØKT_FOR, input.getForlengelseperioder());
        return new PeriodeSplitter<>(splittPeriodeConfig);
    }


}
