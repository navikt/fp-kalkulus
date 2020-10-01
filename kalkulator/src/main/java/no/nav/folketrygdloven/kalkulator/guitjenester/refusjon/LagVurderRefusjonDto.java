package no.nav.folketrygdloven.kalkulator.guitjenester.refusjon;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.kontrakt.v1.ArbeidsgiverOpplysningerDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringerDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.modell.RefusjonAndel;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.response.v1.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.refusjon.RefusjonAndelTilVurderingDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.refusjon.RefusjonTilVurderingDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.refusjon.TidligereUtbetalingDto;

public final class LagVurderRefusjonDto {

    private LagVurderRefusjonDto() {
        // Skjuler default
    }

    public static Optional<RefusjonTilVurderingDto> lagDto(Map<Intervall, List<RefusjonAndel>> andelerMedØktRefusjon,
                                                           BeregningsgrunnlagGUIInput input) {
        Optional<BeregningsgrunnlagGrunnlagDto> orginaltGrunnlag = input.getBeregningsgrunnlagGrunnlagFraForrigeBehandling();
        if (orginaltGrunnlag.isEmpty() || orginaltGrunnlag.get().getBeregningsgrunnlag().isEmpty()) {
            return Optional.empty();
        }
        List<ArbeidsgiverOpplysningerDto> agOpplysninger = input.getIayGrunnlag().getArbeidsgiverOpplysninger();
        BeregningsgrunnlagDto orginaltBG = orginaltGrunnlag.get().getBeregningsgrunnlag().get();
        List<BeregningRefusjonOverstyringDto> gjeldendeOverstyringer = input.getBeregningsgrunnlagGrunnlag().getRefusjonOverstyringer()
                .map(BeregningRefusjonOverstyringerDto::getRefusjonOverstyringer)
                .orElse(Collections.emptyList());

        List<RefusjonAndelTilVurderingDto> dtoer = new ArrayList<>();
        NavigableMap<Intervall, List<RefusjonAndel>> navMap = new TreeMap<>(andelerMedØktRefusjon);

        for (Map.Entry<Intervall, List<RefusjonAndel>> e : navMap.entrySet()) {
            Map.Entry<Intervall, List<RefusjonAndel>> forrigeEntry = navMap.lowerEntry(e.getKey());
            if (forrigeEntry == null) {
                // Første periode, alle andeler skal legges til
                List<RefusjonAndelTilVurderingDto> andeler = e.getValue().stream()
                        .map(andel -> lagAndel(e.getKey(), andel, orginaltBG, agOpplysninger, gjeldendeOverstyringer, input.getSkjæringstidspunktForBeregning()))
                        .collect(Collectors.toList());
                dtoer.addAll(andeler);
            } else {
                // Senere perioden, kun legg til andeler som ikke var i forrige periode (vi vet periodene er sammenhengende)
                List<RefusjonAndelTilVurderingDto> andeler = e.getValue().stream().filter(a -> !forrigeEntry.getValue().contains(a))
                        .map(andel -> lagAndel(e.getKey(), andel, orginaltBG, agOpplysninger, gjeldendeOverstyringer, input.getSkjæringstidspunktForBeregning()))
                        .collect(Collectors.toList());
                dtoer.addAll(andeler);
            }
        }
        return dtoer.isEmpty() ? Optional.empty() : Optional.of(new RefusjonTilVurderingDto(dtoer));
    }

    private static RefusjonAndelTilVurderingDto lagAndel(Intervall periode,
                                                         RefusjonAndel andel,
                                                         BeregningsgrunnlagDto orginalBG,
                                                         List<ArbeidsgiverOpplysningerDto> agOpplysninger,
                                                         List<BeregningRefusjonOverstyringDto> gjeldendeOvertyringer,
                                                         LocalDate skjæringstidspunktForBeregning) {
        RefusjonAndelTilVurderingDto dto = new RefusjonAndelTilVurderingDto();
        Arbeidsgiver ag = mapArbeidsgiver(andel);
        dto.setArbeidsgiverId(ag);
        dto.setInternArbeidsforholdRef(andel.getArbeidsforholdRef().getReferanse());
        dto.setAktivitetStatus(AktivitetStatus.ARBEIDSTAKER); // Hardkoder denne til vi ser en grunn til å bruke andre statuser, er uansett kun AT som har inntektsmeldinger.
        dto.setNyttRefusjonskravFom(periode.getFomDato());
        mapArbeidsgivernavn(andel, agOpplysninger).ifPresent(ago -> dto.setArbeidsgiverNavn(ago.getNavn()));
        List<TidligereUtbetalingDto> tidligereUtbetalinger = finnTidligereUtbetalinger(andel.getArbeidsgiver(), orginalBG);
        dto.setTidligereUtbetalinger(tidligereUtbetalinger);

        Optional<LocalDate> tidligsteMuligeRefusjonsdato = getTidligsteMuligeRefusjonsdato(andel, gjeldendeOvertyringer);
        dto.setTidligsteMuligeRefusjonsdato(tidligsteMuligeRefusjonsdato.orElse(skjæringstidspunktForBeregning));
        Optional<LocalDate> fastsattDato = getFastsattRefusjonsdato(andel, gjeldendeOvertyringer);
        fastsattDato.ifPresent(dto::setFastsattNyttRefusjonskravFom);
        return dto;
    }

    private static Optional<LocalDate> getTidligsteMuligeRefusjonsdato(RefusjonAndel andel, List<BeregningRefusjonOverstyringDto> gjeldendeOvertyringer) {
        return gjeldendeOvertyringer.stream()
                .filter(os -> os.getArbeidsgiver().getIdentifikator().equals(andel.getArbeidsgiver().getIdentifikator()))
                .findFirst()
                .flatMap(BeregningRefusjonOverstyringDto::getFørsteMuligeRefusjonFom);
    }

    private static Optional<ArbeidsgiverOpplysningerDto> mapArbeidsgivernavn(RefusjonAndel andel, List<ArbeidsgiverOpplysningerDto> agOpplysninger) {
        return agOpplysninger.stream()
                .filter(ago -> ago.getIdentifikator().equals(andel.getArbeidsgiver().getIdentifikator()))
                .findFirst();
    }

    private static Arbeidsgiver mapArbeidsgiver(RefusjonAndel andel) {
        return andel.getArbeidsgiver().erAktørId()
                ? new Arbeidsgiver(null, andel.getArbeidsgiver().getAktørId().getId())
                : new Arbeidsgiver(andel.getArbeidsgiver().getOrgnr(), null);
    }

    private static List<TidligereUtbetalingDto> finnTidligereUtbetalinger(no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver ag, BeregningsgrunnlagDto orginaltBG) {
        List<TidligereUtbetalingDto> tidligereUtbetalinger = new ArrayList<>();
        List<BeregningsgrunnlagPeriodeDto> alleOrginalePerioder = orginaltBG.getBeregningsgrunnlagPerioder();
        alleOrginalePerioder.forEach(p -> {
            List<BeregningsgrunnlagPrStatusOgAndelDto> andelerMedSammeAG = p.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                    .filter(andel -> andel.getArbeidsgiver().isPresent())
                    .filter(andel -> andel.getArbeidsgiver().get().equals(ag))
                    .collect(Collectors.toList());
            andelerMedSammeAG.forEach(a -> lagTidligereUtbetaling(a).ifPresent(tidligereUtbetalinger::add));
        });
        return tidligereUtbetalinger;
    }

    private static Optional<LocalDate> getFastsattRefusjonsdato(RefusjonAndel andel, List<BeregningRefusjonOverstyringDto> gjeldendeOvertyringer) {
        List<BeregningRefusjonPeriodeDto> refusjonperioderForAG = gjeldendeOvertyringer.stream()
                .filter(os -> os.getArbeidsgiver().getIdentifikator().equals(andel.getArbeidsgiver().getIdentifikator()))
                .findFirst()
                .map(BeregningRefusjonOverstyringDto::getRefusjonPerioder)
                .orElse(Collections.emptyList());

        return refusjonperioderForAG.stream()
                .filter(refusjon -> refusjon.getArbeidsforholdRef().gjelderFor(andel.getArbeidsforholdRef()))
                .findFirst()
                .map(BeregningRefusjonPeriodeDto::getStartdatoRefusjon);
    }


    private static Optional<TidligereUtbetalingDto> lagTidligereUtbetaling(BeregningsgrunnlagPrStatusOgAndelDto andelIOrginaltGrunnlag) {
        if (andelIOrginaltGrunnlag.getDagsats() == null || BigDecimal.valueOf(andelIOrginaltGrunnlag.getDagsats()).compareTo(BigDecimal.ZERO) == 0) {
            // Ingen utbetaling for andelen på orginalt grunnlag
            return Optional.empty();
        }
        Intervall periode = andelIOrginaltGrunnlag.getBeregningsgrunnlagPeriode().getPeriode();
        if (andelIOrginaltGrunnlag.getDagsatsArbeidsgiver() != null && BigDecimal.valueOf(andelIOrginaltGrunnlag.getDagsatsArbeidsgiver()).compareTo(BigDecimal.ZERO) > 0) {
            return Optional.of(new TidligereUtbetalingDto(periode.getFomDato(), periode.getTomDato(), true));
        }
        return Optional.of(new TidligereUtbetalingDto(periode.getFomDato(), periode.getTomDato(), false));
    }

}
