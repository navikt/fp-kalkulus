package no.nav.folketrygdloven.kalkulator.guitjenester;

import static no.nav.folketrygdloven.kalkulator.FastsettBeregningsgrunnlagPerioderTjeneste.MÅNEDER_I_1_ÅR;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.BeregningInntektsmeldingTjeneste;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.MatchBeregningsgrunnlagTjeneste;
import no.nav.folketrygdloven.kalkulator.fordeling.FordelTilkommetArbeidsforholdTjeneste;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagRestInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.FordelBeregningsgrunnlagAndelDto;
import no.nav.folketrygdloven.kalkulator.guitjenester.fakta.RefusjonDtoTjeneste;

class FordelBeregningsgrunnlagAndelDtoTjeneste {

    private FordelBeregningsgrunnlagAndelDtoTjeneste() {
        // Skjul
    }

    static List<FordelBeregningsgrunnlagAndelDto> lagEndretBgAndelListe(BeregningsgrunnlagRestInput input,
                                                                        BeregningsgrunnlagPeriodeDto periode) {
        List<FordelBeregningsgrunnlagAndelDto> endringAndeler = new ArrayList<>();
        for (BeregningsgrunnlagPrStatusOgAndelDto andel : periode.getBeregningsgrunnlagPrStatusOgAndelList()) {
            FordelBeregningsgrunnlagAndelDto endringAndel = lagEndretBGAndel(input, andel, periode);
            RefusjonDtoTjeneste.settRefusjonskrav(andel, periode.getPeriode(), endringAndel, input.getInntektsmeldinger());
            var beregningAktivitetAggregat = input.getBeregningsgrunnlagGrunnlag().getGjeldendeAktiviteter();
            endringAndel.setNyttArbeidsforhold(FordelTilkommetArbeidsforholdTjeneste.erNyAktivitet(andel, beregningAktivitetAggregat, input.getSkjæringstidspunktForBeregning()));
            endringAndel.setArbeidsforholdType(new OpptjeningAktivitetType(andel.getArbeidsforholdType().getKode()));
            endringAndeler.add(endringAndel);
        }
        return endringAndeler;
    }

    private static FordelBeregningsgrunnlagAndelDto lagEndretBGAndel(BeregningsgrunnlagRestInput input,
                                                                     BeregningsgrunnlagPrStatusOgAndelDto andel, BeregningsgrunnlagPeriodeDto periode) {
        FordelBeregningsgrunnlagAndelDto endringAndel = new FordelBeregningsgrunnlagAndelDto(BeregningsgrunnlagDtoUtil.lagFaktaOmBeregningAndel(
            andel,
            input.getAktivitetGradering(),
            input.getIayGrunnlag(),
            periode
        ));
        settFordelingForrigeBehandling(input, andel, endringAndel);
        endringAndel.setFordeltPrAar(andel.getFordeltPrÅr());
        settBeløpFraInntektsmelding(andel, input.getInntektsmeldinger(), endringAndel);
        return endringAndel;
    }

    private static void settBeløpFraInntektsmelding(BeregningsgrunnlagPrStatusOgAndelDto andel,
                                                    Collection<InntektsmeldingDto> inntektsmeldinger,
                                                    FordelBeregningsgrunnlagAndelDto endringAndel) {
        Optional<InntektsmeldingDto> inntektsmeldingOpt = BeregningInntektsmeldingTjeneste.finnInntektsmeldingForAndel(andel, inntektsmeldinger);
        inntektsmeldingOpt.ifPresent(im -> endringAndel.setBelopFraInntektsmelding(im.getInntektBeløp().getVerdi()));
    }

    private static void settFordelingForrigeBehandling(BeregningsgrunnlagRestInput input,
                                                       BeregningsgrunnlagPrStatusOgAndelDto andel,
                                                       FordelBeregningsgrunnlagAndelDto endringAndel) {
        if (andel.getLagtTilAvSaksbehandler()) {
            endringAndel.setFordelingForrigeBehandling(null);
            return;
        }
        Optional<BeregningsgrunnlagDto> bgForrigeBehandling = input.getBeregningsgrunnlagGrunnlagFraForrigeBehandling().flatMap(BeregningsgrunnlagGrunnlagDto::getBeregningsgrunnlag);
        if (bgForrigeBehandling.isEmpty()) {
            return;
        }
        BeregningsgrunnlagPeriodeDto periodeIOriginaltGrunnlag = MatchBeregningsgrunnlagTjeneste.finnPeriodeIBeregningsgrunnlag(andel.getBeregningsgrunnlagPeriode(), bgForrigeBehandling.get());
        BigDecimal fastsattForrigeBehandling = periodeIOriginaltGrunnlag.getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .filter(a -> a.matchUtenInntektskategori(andel.getAktivitetStatus(),
                andel.getArbeidsgiver().orElse(null),
                andel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getArbeidsforholdRef).orElse(InternArbeidsforholdRefDto.nullRef()),
                andel.getArbeidsforholdType()))
            .map(BeregningsgrunnlagPrStatusOgAndelDto::getFordeltPrÅr)
            .filter(Objects::nonNull)
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO);
        if (fastsattForrigeBehandling != null) {
            endringAndel.setFordelingForrigeBehandling(fastsattForrigeBehandling.divide(BigDecimal.valueOf(MÅNEDER_I_1_ÅR), 0, RoundingMode.HALF_UP));
        }
    }
}
