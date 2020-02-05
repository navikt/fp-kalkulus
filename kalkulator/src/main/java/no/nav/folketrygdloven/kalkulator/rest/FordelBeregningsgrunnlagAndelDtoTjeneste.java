package no.nav.folketrygdloven.kalkulator.rest;

import static no.nav.folketrygdloven.kalkulator.FastsettBeregningsgrunnlagPerioderTjeneste.MÅNEDER_I_1_ÅR;
import static no.nav.folketrygdloven.kalkulator.rest.MapBeregningsgrunnlagFraRestTilDomene.mapAktivitetAggregat;
import static no.nav.folketrygdloven.kalkulator.rest.MapBeregningsgrunnlagFraRestTilDomene.mapAndel;
import static no.nav.folketrygdloven.kalkulator.rest.MapBeregningsgrunnlagFraRestTilDomene.mapBeregningsgrunnlag;
import static no.nav.folketrygdloven.kalkulator.rest.MapBeregningsgrunnlagFraRestTilDomene.mapPeriode;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.BeregningInntektsmeldingTjeneste;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.MatchBeregningsgrunnlagTjeneste;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagRestInput;
import no.nav.folketrygdloven.kalkulator.kontrollerfakta.FordelBeregningsgrunnlagTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagRestDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.rest.dto.FordelBeregningsgrunnlagAndelDto;
import no.nav.folketrygdloven.kalkulator.rest.fakta.RefusjonDtoTjeneste;

class FordelBeregningsgrunnlagAndelDtoTjeneste {

    private FordelBeregningsgrunnlagAndelDtoTjeneste() {
        // Skjul
    }

    static List<FordelBeregningsgrunnlagAndelDto> lagEndretBgAndelListe(BeregningsgrunnlagRestInput input,
                                                                        BeregningsgrunnlagPeriodeRestDto periode) {
        List<FordelBeregningsgrunnlagAndelDto> endringAndeler = new ArrayList<>();
        for (BeregningsgrunnlagPrStatusOgAndelRestDto andel : periode.getBeregningsgrunnlagPrStatusOgAndelList()) {
            FordelBeregningsgrunnlagAndelDto endringAndel = lagEndretBGAndel(input, andel, periode);
            RefusjonDtoTjeneste.settRefusjonskrav(andel, periode.getPeriode(), endringAndel, input.getInntektsmeldinger());
            var beregningAktivitetAggregat = input.getBeregningsgrunnlagGrunnlag().getGjeldendeAktiviteter();
            endringAndel.setNyttArbeidsforhold(FordelBeregningsgrunnlagTjeneste.erNyttArbeidsforhold(mapAndel(andel), mapAktivitetAggregat(beregningAktivitetAggregat), input.getSkjæringstidspunktForBeregning()));
            endringAndel.setArbeidsforholdType(andel.getArbeidsforholdType());
            endringAndeler.add(endringAndel);
        }
        return endringAndeler;
    }

    private static FordelBeregningsgrunnlagAndelDto lagEndretBGAndel(BeregningsgrunnlagRestInput input,
                                                                     BeregningsgrunnlagPrStatusOgAndelRestDto andel, BeregningsgrunnlagPeriodeRestDto periode) {
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

    private static void settBeløpFraInntektsmelding(BeregningsgrunnlagPrStatusOgAndelRestDto andel,
                                                    Collection<InntektsmeldingDto> inntektsmeldinger,
                                                    FordelBeregningsgrunnlagAndelDto endringAndel) {
        Optional<InntektsmeldingDto> inntektsmeldingOpt = BeregningInntektsmeldingTjeneste.finnInntektsmeldingForAndel(mapAndel(andel), inntektsmeldinger);
        inntektsmeldingOpt.ifPresent(im -> endringAndel.setBelopFraInntektsmelding(im.getInntektBeløp().getVerdi()));
    }

    private static void settFordelingForrigeBehandling(BeregningsgrunnlagRestInput input,
                                                       BeregningsgrunnlagPrStatusOgAndelRestDto andel,
                                                       FordelBeregningsgrunnlagAndelDto endringAndel) {
        if (andel.getLagtTilAvSaksbehandler()) {
            endringAndel.setFordelingForrigeBehandling(null);
            return;
        }
        Optional<BeregningsgrunnlagRestDto> bgForrigeBehandling = input.getBeregningsgrunnlagGrunnlagFraForrigeBehandling().flatMap(BeregningsgrunnlagGrunnlagRestDto::getBeregningsgrunnlag);
        if (bgForrigeBehandling.isEmpty()) {
            return;
        }
        BeregningsgrunnlagPeriodeDto periodeIOriginaltGrunnlag = MatchBeregningsgrunnlagTjeneste.finnPeriodeIBeregningsgrunnlag(mapPeriode(andel.getBeregningsgrunnlagPeriode()),
            mapBeregningsgrunnlag(bgForrigeBehandling.get()));
        BigDecimal fastsattForrigeBehandling = periodeIOriginaltGrunnlag.getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .filter(a -> a.matchUtenInntektskategori(andel.getAktivitetStatus(),
                andel.getArbeidsgiver().map(MapBeregningsgrunnlagFraRestTilDomene::mapArbeidsgiver).orElse(null),
                andel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdRestDto::getArbeidsforholdRef).orElse(InternArbeidsforholdRefDto.nullRef()),
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
