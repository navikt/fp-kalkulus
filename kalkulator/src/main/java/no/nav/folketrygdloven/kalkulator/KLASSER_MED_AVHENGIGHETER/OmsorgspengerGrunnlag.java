package no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.svp.PeriodeMedUtbetalingsgradDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;

public class OmsorgspengerGrunnlag extends UtbetalingsgradGrunnlag implements YtelsespesifiktGrunnlag {

    private int dekningsgrad = 100;
    private Integer grunnbeløpMilitærHarKravPå;
    private Map<Arbeidsgiver, Intervall> periodeUtenGyldigRefusjonPrArbeidsgiver = new HashMap<>();

    public OmsorgspengerGrunnlag(List<UtbetalingsgradPrAktivitetDto> tilretteleggingMedUtbelingsgrad) {
        super(tilretteleggingMedUtbelingsgrad);
    }

    @Override
    public int getDekningsgrad() {
        return dekningsgrad;
    }

    @Override
    public int getGrunnbeløpMilitærHarKravPå() {
        return grunnbeløpMilitærHarKravPå;
    }

    @Override
    public void setGrunnbeløpMilitærHarKravPå(int grunnbeløpMilitærHarKravPå) {
        this.grunnbeløpMilitærHarKravPå = grunnbeløpMilitærHarKravPå;
    }

    @Override
    public List<UtbetalingsgradPrAktivitetDto> getUtbetalingsgradPrAktivitet() {
        List<UtbetalingsgradPrAktivitetDto> utbetalingsgradPrAktivitet = super.getUtbetalingsgradPrAktivitet();
        return utbetalingsgradPrAktivitet.stream()
                .map(utbGradAktivitet -> {
                    var arbeidsforhold = utbGradAktivitet.getUtbetalingsgradArbeidsforhold();
                    if (arbeidsforhold.getArbeidsgiver().map(periodeUtenGyldigRefusjonPrArbeidsgiver::containsKey).orElse(false)) {
                        return mapTilAktivitetMedGyldigePerioder(utbGradAktivitet, arbeidsforhold);
                    }
                    return utbGradAktivitet;
                }).collect(Collectors.toList());
    }

    private UtbetalingsgradPrAktivitetDto mapTilAktivitetMedGyldigePerioder(UtbetalingsgradPrAktivitetDto utbGradAktivitet, UtbetalingsgradArbeidsforholdDto arbeidsforhold) {
        var ugyldigPeriode = arbeidsforhold.getArbeidsgiver().map(periodeUtenGyldigRefusjonPrArbeidsgiver::get).orElseThrow();
        var perioder = utbGradAktivitet.getPeriodeMedUtbetalingsgrad();
        List<PeriodeMedUtbetalingsgradDto> gyldigePerioder = utbGradAktivitet.getPeriodeMedUtbetalingsgrad()
                .stream()
                .filter(periodeMedUtbetalingsgradDto -> !periodeMedUtbetalingsgradDto.getPeriode().overlapper(ugyldigPeriode))
                .collect(Collectors.toList());
        splittGyldigPeriodeSomOverlapperUgyldig(ugyldigPeriode, perioder).ifPresent(gyldigePerioder::add);
        gyldigePerioder.add(new PeriodeMedUtbetalingsgradDto(ugyldigPeriode, BigDecimal.ZERO));
        return new UtbetalingsgradPrAktivitetDto(arbeidsforhold, gyldigePerioder.stream().sorted().collect(Collectors.toList()));
    }

    private Optional<PeriodeMedUtbetalingsgradDto> splittGyldigPeriodeSomOverlapperUgyldig(Intervall ugyldigPeriode, List<PeriodeMedUtbetalingsgradDto> perioder) {
        var perioderSomOverlapperUgyldig = perioder.stream()
                .filter(p -> p.getPeriode().overlapper(ugyldigPeriode))
                .collect(Collectors.toList());
        var sisteUgyldigePeriode = perioderSomOverlapperUgyldig.stream()
                .max(Comparator.comparing(PeriodeMedUtbetalingsgradDto::getPeriode));
        Boolean sisteSlutterEtterUgyldig = sisteUgyldigePeriode
                .map(PeriodeMedUtbetalingsgradDto::getPeriode)
                .map(Intervall::getTomDato).map(tom -> tom.isAfter(ugyldigPeriode.getTomDato())).orElse(false);
        if (sisteSlutterEtterUgyldig) {
            LocalDate fom = ugyldigPeriode.getTomDato().plusDays(1);
            LocalDate tom = sisteUgyldigePeriode.get().getPeriode().getTomDato();
            return Optional.of(new PeriodeMedUtbetalingsgradDto(Intervall.fraOgMedTilOgMed(fom, tom), sisteUgyldigePeriode.get().getUtbetalingsgrad()));
        }
        return Optional.empty();
    }

    public void setPeriodeUtenGyldigRefusjonPrArbeidsgiver(Map<Arbeidsgiver, Intervall> periodeUtenGyldigRefusjonPrArbeidsgiver) {
        this.periodeUtenGyldigRefusjonPrArbeidsgiver = periodeUtenGyldigRefusjonPrArbeidsgiver;
    }
}
