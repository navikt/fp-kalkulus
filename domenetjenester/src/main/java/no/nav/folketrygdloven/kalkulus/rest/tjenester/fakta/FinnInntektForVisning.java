package no.nav.folketrygdloven.kalkulus.rest.tjenester.fakta;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.behandling.BehandlingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.FaktaOmBeregningTilfelle;

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

    public static Optional<BigDecimal> finnInntektForKunLese(BehandlingReferanse ref,
                                                             BeregningsgrunnlagPrStatusOgAndelDto andel,
                                                             Optional<InntektsmeldingDto> inntektsmeldingForAndel,
                                                             InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag,
                                                             List<FaktaOmBeregningTilfelle> faktaOmBeregningTilfeller) {
        if (faktaOmBeregningTilfeller.contains(FaktaOmBeregningTilfelle.VURDER_AT_OG_FL_I_SAMME_ORGANISASJON)) {
            if (andel.getAktivitetStatus().erFrilanser()) {
                return Optional.empty();
            }
            if (andel.getAktivitetStatus().erArbeidstaker()) {
                if (!inntektsmeldingForAndel.isPresent()) {
                    return Optional.empty();
                }
            }
        }
        if (andel.getAktivitetStatus().erArbeidstaker()) {
            return finnInntektsbeløpForArbeidstaker(ref, andel, inntektsmeldingForAndel, inntektArbeidYtelseGrunnlag);
        }
        if (andel.getAktivitetStatus().erFrilanser()) {
            return finnMånedsbeløpIBeregningsperiodenForFrilanser(ref, andel, inntektArbeidYtelseGrunnlag);
        }
        if (andel.getAktivitetStatus().equals(AktivitetStatus.DAGPENGER) || andel.getAktivitetStatus().equals(AktivitetStatus.ARBEIDSAVKLARINGSPENGER)) {
            return FinnInntektFraYtelse.finnÅrbeløpFraMeldekort(ref, andel.getAktivitetStatus(), inntektArbeidYtelseGrunnlag)
                .map(årsbeløp -> årsbeløp.divide(MND_I_1_ÅR, 10, RoundingMode.HALF_EVEN));
        }
        return Optional.empty();
    }

    private static Optional<BigDecimal> finnInntektsbeløpForArbeidstaker(BehandlingReferanse ref, BeregningsgrunnlagPrStatusOgAndelDto andel,
                                                                         Optional<InntektsmeldingDto> inntektsmeldingForAndel,
                                                                         InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag) {
        Optional<BigDecimal> inntektsmeldingBeløp = inntektsmeldingForAndel
            .map(InntektsmeldingDto::getInntektBeløp)
            .map(Beløp::getVerdi);
        if (inntektsmeldingBeløp.isPresent()) {
            return inntektsmeldingBeløp;
        }
        return finnMånedsbeløpIBeregningsperiodenForArbeidstaker(ref, andel, inntektArbeidYtelseGrunnlag);
    }

    private static Optional<BigDecimal> finnMånedsbeløpIBeregningsperiodenForFrilanser(BehandlingReferanse ref, BeregningsgrunnlagPrStatusOgAndelDto andel,
                                                                                       InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag) {
        return InntektForAndelTjeneste.finnSnittAvFrilansinntektIBeregningsperioden(ref.getAktørId(),
            inntektArbeidYtelseGrunnlag, andel, ref.getSkjæringstidspunktBeregning());
    }

    private static Optional<BigDecimal> finnMånedsbeløpIBeregningsperiodenForArbeidstaker(BehandlingReferanse ref, BeregningsgrunnlagPrStatusOgAndelDto andel,
                                                                                          InntektArbeidYtelseGrunnlagDto grunnlag) {
        return grunnlag.getAktørInntektFraRegister(ref.getAktørId())
            .map(aktørInntekt -> {
                var filter = new InntektFilterDto(aktørInntekt).før(ref.getSkjæringstidspunktBeregning());
                BigDecimal årsbeløp = InntektForAndelTjeneste.finnSnittinntektPrÅrForArbeidstakerIBeregningsperioden(filter, andel);
                return årsbeløp.divide(MND_I_1_ÅR, 10, RoundingMode.HALF_EVEN);
            });
    }

}
