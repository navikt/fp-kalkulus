package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.AndelKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;

public class FinnInntektForVisning {

    private static final BigDecimal MND_I_1_ÅR = BigDecimal.valueOf(12);

    private FinnInntektForVisning() {
        // Hide constructor
    }

    public static BigDecimal finnInntektForPreutfylling(BeregningsgrunnlagPrStatusOgAndelDto andel) {
        if (andel.getBesteberegningPrÅr() != null) {
            return andel.getBesteberegningPrÅr().divide(MND_I_1_ÅR, 10, RoundingMode.HALF_EVEN);
        }
        return andel.getBeregnetPrÅr() == null ? null : andel.getBeregnetPrÅr().divide(MND_I_1_ÅR, 10, RoundingMode.HALF_EVEN);
    }

    public static Optional<BigDecimal> finnInntektForKunLese(KoblingReferanse ref,
                                                             BeregningsgrunnlagPrStatusOgAndelDto andel,
                                                             Optional<InntektsmeldingDto> inntektsmeldingForAndel,
                                                             InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag,
                                                             List<FaktaOmBeregningTilfelle> faktaOmBeregningTilfeller,
                                                             List<BeregningsgrunnlagPrStatusOgAndelDto> alleAndeler) {
        if (faktaOmBeregningTilfeller.contains(FaktaOmBeregningTilfelle.VURDER_AT_OG_FL_I_SAMME_ORGANISASJON)) {
            if (andel.getAktivitetStatus().erFrilanser()) {
                return Optional.empty();
            }
            if (andel.getAktivitetStatus().erArbeidstaker()) {
                if (inntektsmeldingForAndel.isEmpty()) {
                    return Optional.empty();
                }
            }
        }
        if (andel.getAktivitetStatus().erArbeidstaker()) {
            return finnInntektsbeløpForArbeidstaker(ref, andel, inntektsmeldingForAndel, inntektArbeidYtelseGrunnlag, alleAndeler);
        }
        if (andel.getAktivitetStatus().erFrilanser()) {
            return finnMånedsbeløpIBeregningsperiodenForFrilanser(ref, andel, inntektArbeidYtelseGrunnlag);
        }
        if (andel.getAktivitetStatus().erDagpenger()) {
            YtelseFilterDto ytelseFilter = new YtelseFilterDto(inntektArbeidYtelseGrunnlag.getAktørYtelseFraRegister()).før(ref.getSkjæringstidspunktBeregning());
            return FinnInntektFraYtelse.finnÅrbeløpForDagpenger(ref, andel, ytelseFilter, ref.getSkjæringstidspunktBeregning())
                    .map(årsbeløp -> årsbeløp.divide(MND_I_1_ÅR, 10, RoundingMode.HALF_EVEN));
        }
        if (andel.getAktivitetStatus().equals(AktivitetStatus.ARBEIDSAVKLARINGSPENGER)) {
            YtelseFilterDto ytelseFilter = new YtelseFilterDto(inntektArbeidYtelseGrunnlag.getAktørYtelseFraRegister()).før(ref.getSkjæringstidspunktBeregning());
            return FinnInntektFraYtelse.finnÅrbeløpFraMeldekortForAndel(ref, andel, ytelseFilter)
                    .map(årsbeløp -> årsbeløp.divide(MND_I_1_ÅR, 10, RoundingMode.HALF_EVEN));
        }
        return Optional.empty();
    }

    private static Optional<BigDecimal> finnInntektsbeløpForArbeidstaker(KoblingReferanse ref, BeregningsgrunnlagPrStatusOgAndelDto andel,
                                                                         Optional<InntektsmeldingDto> inntektsmeldingForAndel,
                                                                         InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag,
                                                                         List<BeregningsgrunnlagPrStatusOgAndelDto> alleAndeler) {
        Optional<BigDecimal> inntektsmeldingBeløp = inntektsmeldingForAndel
            .map(InntektsmeldingDto::getInntektBeløp)
            .map(Beløp::getVerdi);
        if (inntektsmeldingBeløp.isPresent()) {
            return inntektsmeldingBeløp;
        }
        return finnMånedsbeløpIBeregningsperiodenForArbeidstaker(ref, andel, inntektArbeidYtelseGrunnlag, alleAndeler);
    }

    private static Optional<BigDecimal> finnMånedsbeløpIBeregningsperiodenForFrilanser(KoblingReferanse ref, BeregningsgrunnlagPrStatusOgAndelDto andel,
                                                                                       InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag) {
        return InntektForAndelTjeneste.finnSnittAvFrilansinntektIBeregningsperioden(
                inntektArbeidYtelseGrunnlag, andel, ref.getSkjæringstidspunktBeregning());
    }

    private static Optional<BigDecimal> finnMånedsbeløpIBeregningsperiodenForArbeidstaker(KoblingReferanse ref, BeregningsgrunnlagPrStatusOgAndelDto andel,
                                                                                          InntektArbeidYtelseGrunnlagDto grunnlag, List<BeregningsgrunnlagPrStatusOgAndelDto> alleAndeler) {
        if (andel.getArbeidsgiver().isEmpty()) {
            // For arbeidstakerandeler uten arbeidsgiver, som etterlønn / sluttpakke.
            return Optional.empty();
        }
        Arbeidsgiver arbeidsgiver = andel.getArbeidsgiver().get();
        List<InntektsmeldingDto> imFraArbeidsgiver = grunnlag.getInntektsmeldinger().stream()
                .flatMap(i -> i.getInntektsmeldingerSomSkalBrukes().stream())
                .filter(im -> im.getArbeidsgiver().getIdentifikator().equals(arbeidsgiver.getIdentifikator()))
                .filter(im -> im.getArbeidsforholdRef().gjelderForSpesifiktArbeidsforhold())
                .collect(Collectors.toList());
        BigDecimal inntektFraInntektsmedlingForAndreArbeidsforholdISammeOrg = imFraArbeidsgiver.stream()
                .map(InntektsmeldingDto::getInntektBeløp)
                .map(Beløp::getVerdi)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
        long antallArbeidsforholdUtenIM = finnAntallArbeidsforholdUtenIM(alleAndeler, arbeidsgiver, imFraArbeidsgiver);
        Optional<BigDecimal> snittInntektFraAOrdningen = finnSnittinntektForArbeidsgiverPrMåned(ref, andel, grunnlag);
        return snittInntektFraAOrdningen.map(inntekt -> finnAndelAvInntekt(inntektFraInntektsmedlingForAndreArbeidsforholdISammeOrg, antallArbeidsforholdUtenIM, inntekt));
    }

    private static BigDecimal finnAndelAvInntekt(BigDecimal inntektFraInntektsmedlingForAndreArbeidsforholdISammeOrg, long antallArbeidsforholdUtenIM, BigDecimal inntekt) {
        BigDecimal restInntektForArbeidsforholdUtenIM = inntekt.subtract(inntektFraInntektsmedlingForAndreArbeidsforholdISammeOrg);
        if (restInntektForArbeidsforholdUtenIM.compareTo(BigDecimal.ZERO) < 0 || antallArbeidsforholdUtenIM == 0) {
            return BigDecimal.ZERO;
        } else {
            return restInntektForArbeidsforholdUtenIM.divide(BigDecimal.valueOf(antallArbeidsforholdUtenIM), 10, RoundingMode.HALF_UP);
        }
    }

    private static Optional<BigDecimal> finnSnittinntektForArbeidsgiverPrMåned(KoblingReferanse ref, BeregningsgrunnlagPrStatusOgAndelDto andel, InntektArbeidYtelseGrunnlagDto grunnlag) {
        return grunnlag.getAktørInntektFraRegister()
            .flatMap(aktørInntekt -> {
                var filter = new InntektFilterDto(aktørInntekt).før(ref.getSkjæringstidspunktBeregning());
                var årsbeløp = InntektForAndelTjeneste.finnSnittinntektPrÅrForArbeidstakerIBeregningsperioden(filter, andel);
                return årsbeløp.map(b -> b.divide(MND_I_1_ÅR, 10, RoundingMode.HALF_EVEN));
            });
    }

    private static long finnAntallArbeidsforholdUtenIM(List<BeregningsgrunnlagPrStatusOgAndelDto> alleAndeler, Arbeidsgiver arbeidsgiver, List<InntektsmeldingDto> imFraArbeidsgiver) {
        return alleAndeler.stream()
                .filter(a -> a.getKilde().equals(AndelKilde.PROSESS_START))
                .filter(a -> a.getArbeidsgiver().isPresent() &&
                a.getArbeidsgiver().get().getIdentifikator().equals(arbeidsgiver.getIdentifikator()) &&
                imFraArbeidsgiver.stream().noneMatch(im -> im.getArbeidsforholdRef().gjelderFor(a.getArbeidsforholdRef().orElse(InternArbeidsforholdRefDto.nullRef()))
                )).count();
    }

}
