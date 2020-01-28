package no.nav.folketrygdloven.kalkulator.ytelse.svp;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.svp.PeriodeMedUtbetalingsgradDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.TilretteleggingMedUtbelingsgradDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.uttak.UttakArbeidType;
import no.nav.folketrygdloven.kalkulator.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.kalkulator.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.folketrygdloven.kalkulator.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.kalkulator.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.kalkulator.regelmodell.resultat.BeregningsgrunnlagPrStatus;

/**
 * Klasse for å modifisere regelmodellen før gjennomkjøring av regel for fastsetting av beregningsgrunnlag for svangerskapspenger
 */
final class RegelmodellModifiserer {
    private RegelmodellModifiserer() {
    }

    static void tilpassRegelModellForSVP(Beregningsgrunnlag beregningsgrunnlagRegel, List<TilretteleggingMedUtbelingsgradDto> tilretteleggingMedUtbelingsgrad) {
        settAlleUtbetalingsgraderTil0(beregningsgrunnlagRegel);
        mapUtbetalingsgrad(beregningsgrunnlagRegel, tilretteleggingMedUtbelingsgrad);
        mapErSøktYtelseFor(beregningsgrunnlagRegel);
    }


    private static void mapUtbetalingsgrad(Beregningsgrunnlag beregningsgrunnlagRegel, List<TilretteleggingMedUtbelingsgradDto> tilretteleggingMedUtbelingsgrad) {
        tilretteleggingMedUtbelingsgrad.forEach(tilrettelegging -> {
            beregningsgrunnlagRegel.getBeregningsgrunnlagPerioder().forEach(bgPeriode -> {
                BigDecimal utbetalingsgrad = getUtbetalingsgrad(tilrettelegging, bgPeriode);
                var tilretteleggingArbeidsforhold = tilrettelegging.getTilretteleggingArbeidsforhold();
                if (UttakArbeidType.ORDINÆRT_ARBEID.equals(tilretteleggingArbeidsforhold.getUttakArbeidType())) {
                    getBGArbeidsforhold(bgPeriode).forEach(bgArbeidsforhold -> {
                        if (tilretteleggingArbeidsforhold.getArbeidsgiver().isPresent() &&
                            tilretteleggingArbeidsforhold.getArbeidsgiver().get().getIdentifikator().equals(bgArbeidsforhold.getArbeidsgiverId()) &&
                            tilretteleggingArbeidsforhold.getInternArbeidsforholdRef()
                                .gjelderFor(InternArbeidsforholdRefDto.ref(bgArbeidsforhold.getArbeidsforhold().getArbeidsforholdId()))) {
                            mapUtbetalingsgrad(utbetalingsgrad, bgArbeidsforhold);
                        }
                    });
                } else if (UttakArbeidType.FRILANS.equals(tilretteleggingArbeidsforhold.getUttakArbeidType())) {
                    getBGArbeidsforhold(bgPeriode).stream()
                        .filter(BeregningsgrunnlagPrArbeidsforhold::erFrilanser)
                        .findFirst()
                        .ifPresent(bgFrilans -> mapUtbetalingsgrad(utbetalingsgrad, bgFrilans));

                } else if (UttakArbeidType.SELVSTENDIG_NÆRINGSDRIVENDE.equals(tilretteleggingArbeidsforhold.getUttakArbeidType())) {
                    Optional.ofNullable(bgPeriode.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN))
                        .ifPresent(bgps -> BeregningsgrunnlagPrStatus.builder(bgps)
                            .medUtbetalingsprosentSVP(utbetalingsgrad)
                            .build());
                }
            });
        });
    }

    private static void mapErSøktYtelseFor(Beregningsgrunnlag beregningsgrunnlagRegel) {
        beregningsgrunnlagRegel.getBeregningsgrunnlagPerioder().forEach(periode -> {
            periode.getBeregningsgrunnlagPrStatus().forEach(status -> {
                if (status.getAktivitetStatus().equals(AktivitetStatus.ATFL)) {
                    status.getArbeidsforhold().forEach(arbeidsforhold -> {
                        boolean erSøktYtelseFor = erSøktYtelseFor(arbeidsforhold);
                        arbeidsforhold.setErSøktYtelseFor(erSøktYtelseFor);
                    });
                } else {
                    status.setErSøktYtelseFor(erSøktYtelseForStatusUtenArbeidsforhold(status));
                }
            });
        });
    }

    private static void settAlleUtbetalingsgraderTil0(Beregningsgrunnlag beregningsgrunnlagRegel) {
        beregningsgrunnlagRegel.getBeregningsgrunnlagPerioder().stream()
            .flatMap(p -> p.getBeregningsgrunnlagPrStatus().stream())
        .forEach(status -> {
            if (status.getAktivitetStatus().equals(AktivitetStatus.ATFL)) {
                status.getArbeidsforhold()
                    .forEach(arb -> BeregningsgrunnlagPrArbeidsforhold.builder(arb).medUtbetalingsprosentSVP(BigDecimal.ZERO));
                BeregningsgrunnlagPrStatus.builder(status).medUtbetalingsprosentSVP(BigDecimal.ZERO);
            } else {
                BeregningsgrunnlagPrStatus.builder(status).medUtbetalingsprosentSVP(BigDecimal.ZERO);
            }
        });
    }

    private static void mapUtbetalingsgrad(BigDecimal utbetalingsgrad, BeregningsgrunnlagPrArbeidsforhold bgArbeidsforhold) {
        BeregningsgrunnlagPrArbeidsforhold.builder(bgArbeidsforhold)
            .medUtbetalingsprosentSVP(utbetalingsgrad)
            .build();
    }

    private static List<BeregningsgrunnlagPrArbeidsforhold> getBGArbeidsforhold(BeregningsgrunnlagPeriode bgPeriode) {
        return Optional.ofNullable(bgPeriode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL))
            .map(BeregningsgrunnlagPrStatus::getArbeidsforhold)
            .orElse(Collections.emptyList());
    }

    private static BigDecimal getUtbetalingsgrad(TilretteleggingMedUtbelingsgradDto tilretteleggingMedUtbelingsgrad, BeregningsgrunnlagPeriode bgPeriode) {
        return tilretteleggingMedUtbelingsgrad.getPeriodeMedUtbetalingsgrad().stream()
            .filter(p -> p.getPeriode().inkluderer(bgPeriode.getBeregningsgrunnlagPeriode().getFom()))
            .map(PeriodeMedUtbetalingsgradDto::getUtbetalingsgrad)
            .findFirst().orElse(BigDecimal.ZERO);
    }

    private static boolean erSøktYtelseFor(BeregningsgrunnlagPrArbeidsforhold bgArbeidsforhold) {
        return bgArbeidsforhold.getUtbetalingsprosentSVP().compareTo(BigDecimal.ZERO) > 0;
    }

    private static boolean erSøktYtelseForStatusUtenArbeidsforhold(BeregningsgrunnlagPrStatus status) {
        if (status.getArbeidsforhold() != null && status.getArbeidsforhold().size() > 0) {
            throw new IllegalStateException("Utviklerfeil: Statusobjekt har arbeidsforhold.");
        }
        return status.getUtbetalingsprosent().compareTo(BigDecimal.ZERO) > 0;
    }

}
