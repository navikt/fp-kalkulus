package no.nav.folketrygdloven.kalkulator.steg.foreslå;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Periodeinntekt;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.folketrygdloven.kalkulator.input.ForeslåBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AnvistAndel;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseAnvistDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulator.ytelse.frisinn.Virkedager;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;

class BeregneFraYtelse {

    private static final Logger log = LoggerFactory.getLogger(BeregneFraYtelse.class);


    static void sjekkBeregningFraYtelse(ForeslåBeregningsgrunnlagInput input, BeregningsgrunnlagDto foreslåttBeregningsgrunnlag, Beregningsgrunnlag regelmodell) {
        // Legges inn kun for å unngå spam i loggen. Vil håndteres når endelig løsning implementeres
        if (input.getFagsakYtelseType().equals(FagsakYtelseType.FRISINN)) {
            return;
        }
        boolean harVurderMottarYtelse = foreslåttBeregningsgrunnlag.getFaktaOmBeregningTilfeller()
                .stream().anyMatch(faktaOmBeregningTilfelle -> faktaOmBeregningTilfelle.equals(FaktaOmBeregningTilfelle.VURDER_MOTTAR_YTELSE));
        if (harVurderMottarYtelse) {

            var atflStatus = regelmodell.getBeregningsgrunnlagPerioder().get(0)
                    .getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);
            List<BeregningsgrunnlagPrArbeidsforhold> arbeidsforholdUtenInntektsmeldingMedBeregningsperiode = atflStatus == null ? Collections.emptyList() : finnArbeidsforholdUtenInntektsmeldingMedBeregningsperiode(regelmodell, atflStatus);

            for (var beregnetAndel : arbeidsforholdUtenInntektsmeldingMedBeregningsperiode) {
                Intervall beregningsperiode = Intervall.fraOgMedTilOgMed(beregnetAndel.getBeregningsperiode().getFom(), beregnetAndel.getBeregningsperiode().getTom());
                List<YtelseAnvistDto> anvisninger = hentAlleRelevanteAnvisninger(input, beregningsperiode);
                boolean harAnvisteAndeler = anvisninger.stream().anyMatch(a -> a.getAnvisteAndeler() != null && !a.getAnvisteAndeler().isEmpty());
                if (!harAnvisteAndeler) {
                    continue;
                }

                BigDecimal direkteUtbetalingForAndel = finnSumDirekteUtbetaltYtelseIPeriodeForAndel(beregningsperiode, anvisninger, beregnetAndel);
                BigDecimal andelLønnOgYtelse = finnLønnOgDirekteUtbetaltYtelseForAndel(regelmodell, beregningsperiode, atflStatus, arbeidsforholdUtenInntektsmeldingMedBeregningsperiode, beregnetAndel, direkteUtbetalingForAndel);
                if (beregnetAndel.getBeregnetPrÅr().compareTo(andelLønnOgYtelse) != 0) {
                    loggBeregning(input, beregnetAndel, direkteUtbetalingForAndel, andelLønnOgYtelse, "Fant differanse i beregning");
                } else {
                    loggBeregning(input, beregnetAndel, direkteUtbetalingForAndel, andelLønnOgYtelse, "Fant ingen differanse i beregning");
                }
            }
        }

    }

    private static List<YtelseAnvistDto> hentAlleRelevanteAnvisninger(ForeslåBeregningsgrunnlagInput input, Intervall beregningsperiode) {
        return input.getIayGrunnlag().getAktørYtelseFraRegister()
                .stream()
                .flatMap(aktørYtelseDto -> aktørYtelseDto.getAlleYtelser().stream())
                .filter(y -> y.getYtelseType().equals(FagsakYtelseType.SYKEPENGER))
                .filter(y -> y.getPeriode().overlapper(beregningsperiode))
                .flatMap(y -> y.getYtelseAnvist().stream())
                .filter(a -> a.getAnvistPeriode().overlapper(beregningsperiode))
                .collect(Collectors.toList());
    }

    private static List<BeregningsgrunnlagPrArbeidsforhold> finnArbeidsforholdUtenInntektsmeldingMedBeregningsperiode(Beregningsgrunnlag regelmodell, BeregningsgrunnlagPrStatus atflStatus) {
        return atflStatus.getArbeidsforholdIkkeFrilans()
                .stream()
                .filter(a -> a.getBeregningsperiode() != null)
                .filter(a -> !regelmodell.getInntektsgrunnlag().finnesInntektsdata(Inntektskilde.INNTEKTSMELDING, a))
                .collect(Collectors.toList());
    }

    private static BigDecimal finnSumDirekteUtbetaltYtelseIPeriodeForAndel(Intervall beregningsperiode, List<YtelseAnvistDto> anvisninger, BeregningsgrunnlagPrArbeidsforhold beregnetAndel) {
        return anvisninger.stream()
                .map(anvisning -> finnDirekteUtbetalingForAndelIPeriode(beregningsperiode, beregnetAndel, anvisning)).reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
    }

    private static BigDecimal finnLønnOgDirekteUtbetaltYtelseForAndel(Beregningsgrunnlag regelmodell, Intervall beregningsperiode, BeregningsgrunnlagPrStatus atflStatus, List<BeregningsgrunnlagPrArbeidsforhold> arbeidsforholdUtenInntektsmelding, BeregningsgrunnlagPrArbeidsforhold beregnetAndel, BigDecimal direkteUtbetalingForAndel) {
        BigDecimal snittFraBeregningsperiodenPrÅr = finnGjennomsnittligLønnOgYtelseIBeregningsperioden(regelmodell, beregningsperiode, beregnetAndel, direkteUtbetalingForAndel);
        var antallArbeidsforhold = finnAntallArbeidsforholdUtenInntektsmeldingForArbeidsgiver(arbeidsforholdUtenInntektsmelding, beregnetAndel);
        BigDecimal inntektFraInntektsmelding = finnInntektFraInntektsmelding(regelmodell, atflStatus, beregnetAndel);
        BigDecimal inntektForArbeidUtenIM = snittFraBeregningsperiodenPrÅr.subtract(inntektFraInntektsmelding);
        BigDecimal andelLønnOgYtelse = inntektForArbeidUtenIM.divide(BigDecimal.valueOf(antallArbeidsforhold), 10, RoundingMode.HALF_UP);
        return andelLønnOgYtelse;
    }

    private static long finnAntallArbeidsforholdUtenInntektsmeldingForArbeidsgiver(List<BeregningsgrunnlagPrArbeidsforhold> arbeidsforholdUtenInntektsmelding, BeregningsgrunnlagPrArbeidsforhold beregnetAndel) {
        return arbeidsforholdUtenInntektsmelding.stream()
                .filter(a -> a.getArbeidsgiverId().equals(beregnetAndel.getArbeidsgiverId()))
                .count();
    }

    private static BigDecimal finnGjennomsnittligLønnOgYtelseIBeregningsperioden(Beregningsgrunnlag regelmodell, Intervall beregningsperiode, BeregningsgrunnlagPrArbeidsforhold beregnetAndel, BigDecimal direkteUtbetalingForAndel) {
        var inntekter = regelmodell.getInntektsgrunnlag().getPeriodeinntekter(
                Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING,
                beregnetAndel, beregningsperiode.getTomDato(),
                3);
        BigDecimal sum = inntekter.stream().map(Periodeinntekt::getInntekt)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO).add(direkteUtbetalingForAndel);
        BigDecimal snittFraBeregningsperiodenPrÅr = sum.multiply(BigDecimal.valueOf(12)).divide(BigDecimal.valueOf(3), 10, RoundingMode.HALF_EVEN);
        return snittFraBeregningsperiodenPrÅr;
    }

    private static void loggBeregning(ForeslåBeregningsgrunnlagInput input, BeregningsgrunnlagPrArbeidsforhold beregnetAndel, BigDecimal direkteUtbetalingForAndel, BigDecimal andelLønnOgYtelse, String startMelding) {
        List<FaktaArbeidsforholdDto> fakta = input.getBeregningsgrunnlagGrunnlag().getFaktaAggregat().map(FaktaAggregatDto::getFaktaArbeidsforhold).orElse(Collections.emptyList());
        Boolean harMottattYtelse = fakta.stream().filter(f -> f.getArbeidsgiver().getIdentifikator().equals(beregnetAndel.getArbeidsgiverId()))
                .findFirst()
                .map(FaktaArbeidsforholdDto::getHarMottattYtelseVurdering)
                .orElse(null);
        boolean harMottattYtelseRegister = direkteUtbetalingForAndel.compareTo(BigDecimal.ZERO) > 0;
        Long koblingId = input.getKoblingReferanse().getKoblingId();
        lagLoggmelding(beregnetAndel, andelLønnOgYtelse, harMottattYtelse, harMottattYtelseRegister,
                koblingId, startMelding);
    }


    private static void lagLoggmelding(BeregningsgrunnlagPrArbeidsforhold beregnetAndel,
                                       BigDecimal andelLønnOgYtelse, Boolean harMottattYtelse,
                                       boolean harMottattYtelseRegister, Long koblingId, final String fantMelding) {
        log.info(fantMelding + " for kobling {}: " +
                        "Beregnet: {}, " +
                        "BeregnetMedDirekteUtbetaling: {}, " +
                        "HarMottattYtelseSaksbehandler: {}, " +
                        "HarMottattYtelseRegister: {}",
                koblingId,
                beregnetAndel.getBeregnetPrÅr(),
                andelLønnOgYtelse,
                harMottattYtelse,
                harMottattYtelseRegister);
    }

    private static BigDecimal finnInntektFraInntektsmelding(Beregningsgrunnlag regelmodell, BeregningsgrunnlagPrStatus atflStatus, BeregningsgrunnlagPrArbeidsforhold beregnetAndel) {
        var inntektFraInntektsmelding = atflStatus.getArbeidsforholdIkkeFrilans()
                .stream()
                .filter(a -> a.getArbeidsgiverId().equals(beregnetAndel.getArbeidsgiverId()))
                .filter(a -> regelmodell.getInntektsgrunnlag().finnesInntektsdata(Inntektskilde.INNTEKTSMELDING, a))
                .map(BeregningsgrunnlagPrArbeidsforhold::getBeregnetPrÅr)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
        return inntektFraInntektsmelding;
    }

    private static BigDecimal finnDirekteUtbetalingForAndelIPeriode(Intervall beregningsperiode, BeregningsgrunnlagPrArbeidsforhold beregnetAndel, YtelseAnvistDto anvisning) {
        if (anvisning.getAnvisteAndeler() != null) {
            int antallVirkedagerOverlapperMedBP = finnAntallVirkedagerIBergningsperiode(beregningsperiode, anvisning);
            int virkedagerIAnvistPeriode = Virkedager.beregnAntallVirkedagerEllerKunHelg(anvisning.getAnvistFOM(), anvisning.getAnvistTOM());
            var anvistAndel = finnAnvistAndel(beregnetAndel, anvisning);
            return anvistAndel.map(anvist -> finnDirekteUtbetaltBeløp(antallVirkedagerOverlapperMedBP, virkedagerIAnvistPeriode, anvist))
                    .orElse(BigDecimal.ZERO);
        }
        return BigDecimal.ZERO;
    }

    private static BigDecimal finnDirekteUtbetaltBeløp(int antallVirkedagerOverlapperMedBP, int virkedagerIAnvistPeriode, AnvistAndel anvist) {
        BigDecimal direkteUtbetalingProsent = BigDecimal.valueOf(100).subtract(anvist.getRefusjonsgrad().getVerdi());
        BigDecimal direkteUtbetalingGrad = direkteUtbetalingProsent.divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP);
        return anvist.getBeløp()
                .multiply(direkteUtbetalingGrad)
                .multiply(BigDecimal.valueOf(antallVirkedagerOverlapperMedBP))
                .divide(BigDecimal.valueOf(virkedagerIAnvistPeriode), RoundingMode.HALF_UP);
    }

    private static Optional<AnvistAndel> finnAnvistAndel(BeregningsgrunnlagPrArbeidsforhold beregnetAndel, YtelseAnvistDto anvisning) {
        return anvisning.getAnvisteAndeler().stream()
                .filter(aa -> aa.getArbeidsgiver().map(a -> a.getIdentifikator().equals(beregnetAndel.getArbeidsgiverId())).orElse(false))
                .findFirst();
    }

    private static int finnAntallVirkedagerIBergningsperiode(Intervall beregningsperiode, YtelseAnvistDto anvisning) {
        var fom = anvisning.getAnvistFOM().isBefore(beregningsperiode.getFomDato()) ?
                beregningsperiode.getFomDato() : anvisning.getAnvistFOM();
        var tom = anvisning.getAnvistTOM().isBefore(beregningsperiode.getTomDato()) ?
                anvisning.getAnvistTOM() : beregningsperiode.getTomDato();
        int antallVirkedager = Virkedager.beregnAntallVirkedagerEllerKunHelg(fom, tom);
        return antallVirkedager;
    }

}
