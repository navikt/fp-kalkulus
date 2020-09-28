package no.nav.folketrygdloven.kalkulator.guitjenester.ytelsegrunnlag;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.ytelse.frisinn.FrisinnPeriode;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittEgenNæringDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittFrilansDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittFrilansInntektDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittOpptjeningDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.frisinn.FrisinnAndelDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.frisinn.FrisinnPeriodeDto;

public final class MapTilPerioderFRISINN {

    private MapTilPerioderFRISINN() {
        // Skjuler default
    }

    public static List<FrisinnPeriodeDto> map(List<FrisinnPeriode> frisinnPerioder, OppgittOpptjeningDto oppgittOpptjening) {
        List<FrisinnPeriodeDto> dtoer = new ArrayList<>();
        frisinnPerioder.forEach(periode -> {
            FrisinnPeriodeDto periodeDto = new FrisinnPeriodeDto();
            periodeDto.setFom(periode.getPeriode().getFomDato());
            periodeDto.setTom(periode.getPeriode().getTomDato());
            List<FrisinnAndelDto> andeler = new ArrayList<>();
            finnOppgittArbeidsinntekt(periode.getPeriode(), oppgittOpptjening).ifPresent(periodeDto::setOppgittArbeidsinntekt);
            if (periode.getSøkerFrilans()) {
                finnOppgittFrilansInntekt(periode.getPeriode(), oppgittOpptjening).ifPresent(andeler::add);
            }
            if (periode.getSøkerNæring()) {
                finnOppgittNæringsinntekt(periode.getPeriode(), oppgittOpptjening).ifPresent(andeler::add);
            }
            periodeDto.setFrisinnAndeler(andeler);
            dtoer.add(periodeDto);
        });
        return dtoer;
    }

    private static Optional<BigDecimal> finnOppgittArbeidsinntekt(Intervall periode, OppgittOpptjeningDto oppgittOpptjening) {
        return oppgittOpptjening.getOppgittArbeidsforhold().stream()
                .filter(arbfor -> arbfor.getPeriode().inkluderer(periode.getFomDato())
                        && arbfor.getPeriode().inkluderer(periode.getTomDato()))
                .filter(af -> af.getInntekt() != null)
                .map(OppgittArbeidsforholdDto::getInntekt)
                .reduce(BigDecimal::add);
    }

    private static Optional<FrisinnAndelDto> finnOppgittNæringsinntekt(Intervall periode, OppgittOpptjeningDto oppgittOpptjening) {
        List<OppgittEgenNæringDto> næringer  = oppgittOpptjening.getEgenNæring();
        if (næringer.isEmpty()) {
            return Optional.empty();
        }
        Optional<BigDecimal> oppgittNæringsinntektIPerioden = næringer.stream()
                .filter(næring -> næring.getPeriode().inkluderer(periode.getFomDato()) &&
                        næring.getPeriode().inkluderer(periode.getTomDato()))
                .filter(i -> i.getInntekt() != null)
                .map(OppgittEgenNæringDto::getInntekt)
                .reduce(BigDecimal::add);

        return oppgittNæringsinntektIPerioden.map(inntekt -> new FrisinnAndelDto(inntekt, AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE));
    }

    private static Optional<FrisinnAndelDto> finnOppgittFrilansInntekt(Intervall periode, OppgittOpptjeningDto oppgittOpptjening) {
        Optional<OppgittFrilansDto> oppgittFL = oppgittOpptjening.getFrilans();
        if (oppgittFL.isEmpty()) {
            return Optional.empty();
        }
        Optional<BigDecimal> oppgittFrilansinntektIPeriode = oppgittFL.get().getOppgittFrilansInntekt().stream()
                .filter(frilans -> frilans.getPeriode().inkluderer(periode.getFomDato())
                        && frilans.getPeriode().inkluderer(periode.getTomDato()))
                .filter(i -> i.getInntekt() != null)
                .map(OppgittFrilansInntektDto::getInntekt)
                .reduce(BigDecimal::add);

        return oppgittFrilansinntektIPeriode.map(inntekt -> new FrisinnAndelDto(inntekt, AktivitetStatus.FRILANSER));

    }
}
