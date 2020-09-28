package no.nav.folketrygdloven.kalkulus.mappers;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.ytelse.frisinn.FrisinnPeriode;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittEgenNæringDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittFrilansInntektDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittOpptjeningDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.fpsak.tidsserie.LocalDateInterval;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;

public final class MapGammeltFrisinngrunnlag {
    private static final LocalDate ORIGINAL_STP_FOR_FRISINN = LocalDate.of(2020, 3, 1);

    private MapGammeltFrisinngrunnlag() {
        // SKjuler default
    }

    public static List<FrisinnPeriode> map(no.nav.folketrygdloven.kalkulus.beregning.v1.FrisinnGrunnlag frisinnGrunnlag, Optional<OppgittOpptjeningDto> oppgittOpptjening) {
        if (oppgittOpptjening.isEmpty()) {
            return Collections.emptyList();
        }
        List<OppgittFrilansInntektDto> oppgittFLEtterSTP = finnOppgittFLEtterSTP(oppgittOpptjening.get());
        List<OppgittEgenNæringDto> oppgittSNEtterSTP = finnOppgittNæringEtterSTP(oppgittOpptjening.get());
        List<LocalDateSegment<FrisinnPeriode>> flTidslinje = frisinnGrunnlag.getSøkerYtelseForFrilans() ? lagSegmenterFL(oppgittFLEtterSTP) : Collections.emptyList();
        List<LocalDateSegment<FrisinnPeriode>> snTidslinje = frisinnGrunnlag.getSøkerYtelseForNæring() ? lagSegmenterSN(oppgittSNEtterSTP) : Collections.emptyList();

        LocalDateTimeline<FrisinnPeriode> frilansTidsserie = new LocalDateTimeline<>(flTidslinje);
        LocalDateTimeline<FrisinnPeriode> næringsdrivendeTidsserie = new LocalDateTimeline<>(snTidslinje);
        LocalDateTimeline<FrisinnPeriode> kombinert = frilansTidsserie.combine(næringsdrivendeTidsserie, MapGammeltFrisinngrunnlag::combine, LocalDateTimeline.JoinStyle.CROSS_JOIN);

        return kombinert.getDatoIntervaller().stream().map(intervall -> {
            LocalDateSegment<FrisinnPeriode> segment = kombinert.getSegment(intervall);
            return segment.getValue();
        }).collect(Collectors.toList());
    }

    private static List<LocalDateSegment<FrisinnPeriode>> lagSegmenterFL(List<OppgittFrilansInntektDto> frilansperioder) {
        return frilansperioder.stream()
                .map(flPeriode -> {
                    FrisinnPeriode dto = new FrisinnPeriode(Intervall.fraOgMedTilOgMed(flPeriode.getPeriode().getFomDato(), flPeriode.getPeriode().getTomDato()), true, false);
                    return new LocalDateSegment<>(flPeriode.getPeriode().getFomDato(), flPeriode.getPeriode().getTomDato(), dto);
                }).collect(Collectors.toList());
    }

    private static List<LocalDateSegment<FrisinnPeriode>> lagSegmenterSN(List<OppgittEgenNæringDto> næringsperioder) {
        return næringsperioder.stream()
                .map(snPeriode -> {
                    FrisinnPeriode dto = new FrisinnPeriode(Intervall.fraOgMedTilOgMed(snPeriode.getPeriode().getFomDato(), snPeriode.getPeriode().getTomDato()), false, true);
                    return new LocalDateSegment<>(snPeriode.getPeriode().getFomDato(), snPeriode.getPeriode().getTomDato(), dto);
                }).collect(Collectors.toList());
    }

    private static List<OppgittEgenNæringDto> finnOppgittNæringEtterSTP(OppgittOpptjeningDto oppgittOpptjening) {
        return oppgittOpptjening.getEgenNæring().stream()
                .filter(en -> en.getPeriode().getFomDato().isAfter(ORIGINAL_STP_FOR_FRISINN))
                .collect(Collectors.toList());
    }

    private static List<OppgittFrilansInntektDto> finnOppgittFLEtterSTP(OppgittOpptjeningDto oppgittOpptjening) {
        return oppgittOpptjening.getFrilans().map(ol -> ol.getOppgittFrilansInntekt().stream()
                .filter(o -> o.getPeriode().getFomDato().isAfter(ORIGINAL_STP_FOR_FRISINN))
                .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    private static LocalDateSegment<FrisinnPeriode> combine(LocalDateInterval interval,
                                                                    LocalDateSegment<FrisinnPeriode> fl,
                                                                    LocalDateSegment<FrisinnPeriode> sn) {
        if (fl != null && sn != null) {
            return new LocalDateSegment<>(interval, new FrisinnPeriode(Intervall.fraOgMedTilOgMed(interval.getFomDato(), interval.getTomDato()), true, true));
        } else if (fl != null) {
            return new LocalDateSegment<>(interval, new FrisinnPeriode(Intervall.fraOgMedTilOgMed(interval.getFomDato(), interval.getTomDato()), true, false));
        }
        return new LocalDateSegment<>(interval, new FrisinnPeriode(Intervall.fraOgMedTilOgMed(interval.getFomDato(), interval.getTomDato()), false, true));
    }
}
