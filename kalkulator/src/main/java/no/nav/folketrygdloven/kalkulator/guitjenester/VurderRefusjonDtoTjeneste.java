package no.nav.folketrygdloven.kalkulator.guitjenester;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagRestInput;
import no.nav.folketrygdloven.kalkulator.kontrakt.v1.ArbeidsgiverOpplysningerDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringerDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.response.v1.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.refusjon.RefusjonAndelTilVurderingDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.refusjon.RefusjonTilVurderingDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.refusjon.TidligereUtbetalingDto;

public class VurderRefusjonDtoTjeneste {

    private VurderRefusjonDtoTjeneste() {
        // Skjul
    }

    public static Optional<RefusjonTilVurderingDto> lagDto(BeregningsgrunnlagRestInput input) {
        List<InntektsmeldingDto> tilkomneInntektsmeldinger = input.getIayGrunnlag().getInntektsmeldinger()
                .map(InntektsmeldingAggregatDto::getInntektsmeldingdiffFraOriginalbehandling)
                .orElse(Collections.emptyList());
        Optional<BeregningsgrunnlagGrunnlagDto> orginaltGrunnlag = input.getBeregningsgrunnlagGrunnlagFraForrigeBehandling();
        if (tilkomneInntektsmeldinger.isEmpty() || orginaltGrunnlag.isEmpty() || orginaltGrunnlag.get().getBeregningsgrunnlag().isEmpty()) {
            return Optional.empty();
        }
        List<ArbeidsgiverOpplysningerDto> agOpplysninger = input.getIayGrunnlag().getArbeidsgiverOpplysninger();
        BeregningsgrunnlagDto orginaltBG = orginaltGrunnlag.get().getBeregningsgrunnlag().get();
        List<BeregningRefusjonOverstyringDto> gjeldendeOverstyringer = input.getBeregningsgrunnlagGrunnlag().getRefusjonOverstyringer()
                .map(BeregningRefusjonOverstyringerDto::getRefusjonOverstyringer)
                .orElse(Collections.emptyList());
        List<RefusjonAndelTilVurderingDto> dtoer = tilkomneInntektsmeldinger.stream()
                .filter(im -> im.getStartDatoPermisjon().isPresent())
                .map(im -> lagDto(orginaltBG, im, agOpplysninger, gjeldendeOverstyringer, input.getSkjæringstidspunktForBeregning()))
                .collect(Collectors.toList());
        return dtoer.isEmpty() ? Optional.empty() : Optional.of(new RefusjonTilVurderingDto(dtoer));

    }

    private static RefusjonAndelTilVurderingDto lagDto(BeregningsgrunnlagDto orginaltBG,
                                                       InntektsmeldingDto im,
                                                       List<ArbeidsgiverOpplysningerDto> agOpplysninger,
                                                       List<BeregningRefusjonOverstyringDto> gjeldendeOvertyringer,
                                                       LocalDate skjæringstidspunktForBeregning) {
        RefusjonAndelTilVurderingDto dto = new RefusjonAndelTilVurderingDto();
        Arbeidsgiver ag = mapArbeidsgiver(im);
        dto.setArbeidsgiverId(ag);
        dto.setInternArbeidsforholdRef(im.getArbeidsforholdRef().getReferanse());
        dto.setAktivitetStatus(AktivitetStatus.ARBEIDSTAKER); // Hardkoder denne til vi ser en grunn til å bruke andre statuser, er uansett kun AT som har inntektsmeldinger.
        dto.setNyttRefusjonskravFom(im.getStartDatoPermisjon().orElseThrow());
        mapArbeidsgivernavn(im, agOpplysninger).ifPresent(ago -> dto.setArbeidsgiverNavn(ago.getNavn()));
        List<TidligereUtbetalingDto> tidligereUtbetalinger = finnTidligereUtbetalinger(im, orginaltBG);
        dto.setTidligereUtbetalinger(tidligereUtbetalinger);

        Optional<LocalDate> tidligsteMuligeRefusjonsdato = getTidligsteMuligeRefusjonsdato(im, gjeldendeOvertyringer);
        dto.setTidligsteMuligeRefusjonsdato(tidligsteMuligeRefusjonsdato.orElse(skjæringstidspunktForBeregning));
        Optional<LocalDate> fastsattDato = getFastsattRefusjonsdato(im, gjeldendeOvertyringer);
        fastsattDato.ifPresent(dto::setFastsattNyttRefusjonskravFom);

        return dto;
    }

    private static Optional<LocalDate> getTidligsteMuligeRefusjonsdato(InntektsmeldingDto im, List<BeregningRefusjonOverstyringDto> gjeldendeOvertyringer) {
        return gjeldendeOvertyringer.stream()
                .filter(os -> os.getArbeidsgiver().getIdentifikator().equals(im.getArbeidsgiver().getIdentifikator()))
                .findFirst()
                .flatMap(BeregningRefusjonOverstyringDto::getFørsteMuligeRefusjonFom);
    }

    private static Optional<ArbeidsgiverOpplysningerDto> mapArbeidsgivernavn(InntektsmeldingDto im, List<ArbeidsgiverOpplysningerDto> agOpplysninger) {
        return agOpplysninger.stream()
                .filter(ago -> ago.getIdentifikator().equals(im.getArbeidsgiver().getIdentifikator()))
                .findFirst();
    }

    private static Arbeidsgiver mapArbeidsgiver(InntektsmeldingDto im) {
        return im.getArbeidsgiver().erAktørId()
                ? new Arbeidsgiver(null, im.getArbeidsgiver().getAktørId().getId())
                : new Arbeidsgiver(im.getArbeidsgiver().getOrgnr(), null);
    }

    private static List<TidligereUtbetalingDto> finnTidligereUtbetalinger(InntektsmeldingDto im, BeregningsgrunnlagDto orginaltBG) {
        List<TidligereUtbetalingDto> tidligereUtbetalinger = new ArrayList<>();
        List<BeregningsgrunnlagPeriodeDto> alleOrginalePerioder = orginaltBG.getBeregningsgrunnlagPerioder();
        alleOrginalePerioder.forEach(p -> {
            List<BeregningsgrunnlagPrStatusOgAndelDto> andelerMedSammeAG = p.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                    .filter(andel -> andel.getArbeidsgiver().isPresent())
                    .filter(andel -> andel.getArbeidsgiver().get().equals(im.getArbeidsgiver()))
                    .collect(Collectors.toList());
            andelerMedSammeAG.forEach(a -> lagTidligereUtbetaling(a).ifPresent(tidligereUtbetalinger::add));
        });
        return tidligereUtbetalinger;
    }

    private static Optional<LocalDate> getFastsattRefusjonsdato(InntektsmeldingDto im, List<BeregningRefusjonOverstyringDto> gjeldendeOvertyringer) {
        List<BeregningRefusjonPeriodeDto> refusjonperioderForAG = gjeldendeOvertyringer.stream()
                .filter(os -> os.getArbeidsgiver().getIdentifikator().equals(im.getArbeidsgiver().getIdentifikator()))
                .findFirst()
                .map(BeregningRefusjonOverstyringDto::getRefusjonPerioder)
                .orElse(Collections.emptyList());

        return refusjonperioderForAG.stream()
                .filter(refusjon -> refusjon.getArbeidsforholdRef().gjelderFor(im.getArbeidsforholdRef()))
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
