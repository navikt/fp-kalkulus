package no.nav.folketrygdloven.kalkulus.mapTilKontrakt;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.konfig.KonfigTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittFrilansDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittFrilansInntektDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittOpptjeningDto;
import no.nav.folketrygdloven.kalkulator.ytelse.frisinn.FrisinnGrunnlag;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.folketrygdloven.kalkulus.felles.jpa.IntervallEntitet;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;

public class MapInntektstakFRISINN {
    private final static BigDecimal ANTALL_G_GRENSEVERDI;

    static {
        ANTALL_G_GRENSEVERDI = KonfigTjeneste.forYtelse(FagsakYtelseType.FRISINN).getAntallGØvreGrenseverdi();
    }

    private MapInntektstakFRISINN() {
        // SKjul konstruktør
    }

    static BigDecimal map(List<BeregningsgrunnlagPrStatusOgAndel> andeler,
                                                       no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus status,
                                                       FrisinnGrunnlag frisinnGrunnlag,
                                                       Optional<OppgittOpptjeningDto> oppgittOpptjeningDto,
                                                       BigDecimal gbeløp) {
        if (status.erFrilanser()) {
            return finnFrilanstakForPeriode(andeler, frisinnGrunnlag, oppgittOpptjeningDto, gbeløp);
        } else if (status.erSelvstendigNæringsdrivende()) {
            return finnNæringstakForPeriode(andeler, frisinnGrunnlag, oppgittOpptjeningDto, gbeløp);
        }
        return BigDecimal.ZERO;
    }

    private static BigDecimal finnNæringstakForPeriode(List<BeregningsgrunnlagPrStatusOgAndel> andeler,
                                                FrisinnGrunnlag frisinnGrunnlag,
                                                Optional<OppgittOpptjeningDto> oppgittOpptjeningDto,
                                                BigDecimal gbeløp) {
        Optional<BeregningsgrunnlagPrStatusOgAndel> andelOpt = andeler.stream().filter(andel -> andel.getAktivitetStatus().erSelvstendigNæringsdrivende()).findFirst();
        if (andelOpt.isEmpty() || oppgittOpptjeningDto.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BeregningsgrunnlagPrStatusOgAndel snAndel = andelOpt.get();
        IntervallEntitet bgPeriode = snAndel.getBeregningsgrunnlagPeriode().getPeriode();

        LocalDate fom = bgPeriode.getFomDato();
        if (frisinnGrunnlag.getSøkerYtelseForNæring(fom) && harOppgittNæringsinntektForPeriode(bgPeriode, oppgittOpptjeningDto.get())) {
            BigDecimal seksG = gbeløp.multiply(ANTALL_G_GRENSEVERDI);
            BigDecimal atBrutto = bruttoArbeidsforhold(andeler);
            BigDecimal inntektstak = seksG.subtract(atBrutto);
            if (!frisinnGrunnlag.getSøkerYtelseForFrilans(fom)) {
                BigDecimal flBrutto = andeler.stream().filter(a -> a.getAktivitetStatus().erFrilanser())
                        .filter(a -> a.getBruttoPrÅr() != null)
                        .map(BeregningsgrunnlagPrStatusOgAndel::getBruttoPrÅr)
                        .reduce(BigDecimal::add)
                        .orElse(BigDecimal.ZERO);
                inntektstak = inntektstak.subtract(flBrutto);
            } else {
                BigDecimal inntektstakFL = finnFrilanstakForPeriode(andeler, frisinnGrunnlag, oppgittOpptjeningDto, gbeløp);
                inntektstak = inntektstak.subtract(inntektstakFL);
            }
            inntektstak = inntektstak.max(BigDecimal.ZERO);
            return inntektstak.min(snAndel.getBruttoPrÅr());
        }
        return BigDecimal.ZERO;
    }

    private static boolean harOppgittNæringsinntektForPeriode(IntervallEntitet bgPeriode,
                                                              OppgittOpptjeningDto oppgittOpptjeningDto) {
        return oppgittOpptjeningDto.getEgenNæring()
                .stream()
                .anyMatch(en -> IntervallEntitet.fraOgMedTilOgMed(en.getPeriode().getFomDato(), en.getPeriode().getTomDato()).overlapper(bgPeriode));
    }

    private static BigDecimal finnFrilanstakForPeriode(List<BeregningsgrunnlagPrStatusOgAndel> andeler,
                                                FrisinnGrunnlag frisinnGrunnlag,
                                                Optional<OppgittOpptjeningDto> oppgittOpptjeningDto,
                                                BigDecimal gbeløp) {
        Optional<BeregningsgrunnlagPrStatusOgAndel> andelOpt = andeler.stream().filter(andel -> andel.getAktivitetStatus().erFrilanser()).findFirst();
        if (andelOpt.isEmpty() || oppgittOpptjeningDto.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BeregningsgrunnlagPrStatusOgAndel flAndel = andelOpt.get();
        IntervallEntitet bgPeriode = flAndel.getBeregningsgrunnlagPeriode().getPeriode();
        LocalDate fom = bgPeriode.getFomDato();
        if (frisinnGrunnlag.getSøkerYtelseForFrilans(fom) && erSøktYtelseForFLIPeriode(bgPeriode, oppgittOpptjeningDto.get())) {
            BigDecimal seksG = gbeløp.multiply(ANTALL_G_GRENSEVERDI);
            BigDecimal atBrutto = bruttoArbeidsforhold(andeler);
            BigDecimal inntektstak = seksG.subtract(atBrutto);
            if (!frisinnGrunnlag.getSøkerYtelseForNæring(fom)) {
                BigDecimal snBrutto = andeler.stream().filter(a -> a.getAktivitetStatus().erSelvstendigNæringsdrivende())
                        .filter(a -> a.getBruttoPrÅr() != null)
                        .map(BeregningsgrunnlagPrStatusOgAndel::getBruttoPrÅr)
                        .reduce(BigDecimal::add)
                        .orElse(BigDecimal.ZERO);
                inntektstak = inntektstak.subtract(snBrutto);
            }
            inntektstak = inntektstak.max(BigDecimal.ZERO);
            return inntektstak.min(flAndel.getBruttoPrÅr());
        }
        return BigDecimal.ZERO;
    }

    private static boolean erSøktYtelseForFLIPeriode(IntervallEntitet bgPeriode, OppgittOpptjeningDto oppgittOpptjeningDto) {
        List<OppgittFrilansInntektDto> flInntekter = oppgittOpptjeningDto.getFrilans()
                .map(OppgittFrilansDto::getOppgittFrilansInntekt)
                .orElse(Collections.emptyList());

        return flInntekter.stream().anyMatch(fli -> IntervallEntitet.fraOgMedTilOgMed(fli.getPeriode().getFomDato(), fli.getPeriode().getTomDato()).overlapper(bgPeriode));
    }

    private static BigDecimal bruttoArbeidsforhold(List<BeregningsgrunnlagPrStatusOgAndel> andeler) {
        return andeler.stream().filter(a -> a.getAktivitetStatus().erArbeidstaker())
                .filter(bga -> bga.getBruttoPrÅr() != null)
                .map(BeregningsgrunnlagPrStatusOgAndel::getBruttoPrÅr)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
    }

}
