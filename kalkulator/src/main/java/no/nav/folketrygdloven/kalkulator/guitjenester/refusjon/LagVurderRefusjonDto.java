package no.nav.folketrygdloven.kalkulator.guitjenester.refusjon;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringerDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.ArbeidsforholdInformasjonDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.ArbeidsgiverOpplysningerDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.EksternArbeidsforholdRef;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.modell.RefusjonAndel;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.response.v1.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.refusjon.RefusjonAndelTilVurderingDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.refusjon.RefusjonTilVurderingDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.refusjon.TidligereUtbetalingDto;

public final class LagVurderRefusjonDto {
    private static final BigDecimal MÅNEDER_I_ÅR = BigDecimal.valueOf(12);

    private LagVurderRefusjonDto() {
        // Skjuler default
    }

    public static Optional<RefusjonTilVurderingDto> lagDto(Map<Intervall, List<RefusjonAndel>> andelerMedØktRefusjon,
                                                           BeregningsgrunnlagGUIInput input) {
        Optional<BeregningsgrunnlagGrunnlagDto> orginaltGrunnlag = input.getBeregningsgrunnlagGrunnlagFraForrigeBehandling();
        if (orginaltGrunnlag.isEmpty() || orginaltGrunnlag.get().getBeregningsgrunnlag().isEmpty()) {
            return Optional.empty();
        }
        BeregningsgrunnlagDto orginaltBG = orginaltGrunnlag.get().getBeregningsgrunnlag().get();
        List<BeregningRefusjonOverstyringDto> gjeldendeOverstyringer = input.getBeregningsgrunnlagGrunnlag().getRefusjonOverstyringer()
                .map(BeregningRefusjonOverstyringerDto::getRefusjonOverstyringer)
                .orElse(Collections.emptyList());

        Optional<ArbeidsforholdInformasjonDto> arbeidsforholdInformasjon = input.getIayGrunnlag().getArbeidsforholdInformasjon();
        List<RefusjonAndelTilVurderingDto> dtoer = new ArrayList<>();
        NavigableMap<Intervall, List<RefusjonAndel>> navMap = new TreeMap<>(andelerMedØktRefusjon);
        BeregningsgrunnlagDto gjeldendeBeregningsgrunnlag = input.getBeregningsgrunnlag();
        for (Map.Entry<Intervall, List<RefusjonAndel>> e : navMap.entrySet()) {
            Map.Entry<Intervall, List<RefusjonAndel>> forrigeEntry = navMap.lowerEntry(e.getKey());
            if (forrigeEntry == null) {
                // Første periode, alle andeler skal legges til
                List<RefusjonAndelTilVurderingDto> andeler = e.getValue().stream()
                        .map(andel -> lagAndel(e.getKey(), andel, gjeldendeBeregningsgrunnlag, orginaltBG, gjeldendeOverstyringer, arbeidsforholdInformasjon))
                        .collect(Collectors.toList());
                dtoer.addAll(andeler);
            } else {
                // Senere perioden, kun legg til andeler som ikke var i forrige periode (vi vet periodene er sammenhengende)
                List<RefusjonAndelTilVurderingDto> andeler = e.getValue().stream().filter(a -> !forrigeEntry.getValue().contains(a))
                        .map(andel -> lagAndel(e.getKey(), andel, gjeldendeBeregningsgrunnlag, orginaltBG, gjeldendeOverstyringer, arbeidsforholdInformasjon))
                        .collect(Collectors.toList());
                dtoer.addAll(andeler);
            }
        }
        return dtoer.isEmpty() ? Optional.empty() : Optional.of(new RefusjonTilVurderingDto(dtoer));
    }

    private static RefusjonAndelTilVurderingDto lagAndel(Intervall periode,
                                                         RefusjonAndel andel,
                                                         BeregningsgrunnlagDto gjeldendeBeregningsgrunnlag,
                                                         BeregningsgrunnlagDto orginalBG,
                                                         List<BeregningRefusjonOverstyringDto> gjeldendeOvertyringer,
                                                         Optional<ArbeidsforholdInformasjonDto> arbeidsforholdInformasjon) {
        RefusjonAndelTilVurderingDto dto = new RefusjonAndelTilVurderingDto();
        // Visningsfelter
        Arbeidsgiver ag = mapArbeidsgiver(andel);
        dto.setArbeidsgiver(ag);
        dto.setInternArbeidsforholdRef(andel.getArbeidsforholdRef().getReferanse());
        dto.setAktivitetStatus(AktivitetStatus.ARBEIDSTAKER); // Hardkoder denne til vi ser en grunn til å bruke andre statuser, er uansett kun AT som har inntektsmeldinger.
        dto.setNyttRefusjonskravFom(periode.getFomDato());
        dto.setTidligereUtbetalinger(finnTidligereUtbetalinger(andel.getArbeidsgiver(), orginalBG));
        mapEksternReferanse(andel, arbeidsforholdInformasjon).ifPresent(ref -> dto.setEksternArbeidsforholdRef(ref.getReferanse()));

        // Sjekk om delvis refusjon skal settes og avklar evt valideringer
        BigDecimal tidligereRefusjonForAndelIPeriode = finnTidligereUtbetaltRefusjonForAndelIPeriode(periode, andel, orginalBG);
        boolean skalFastsetteDelvisRefusjon = erRefusjonTidligereInnvilgetMedLavereBeløp(tidligereRefusjonForAndelIPeriode, andel);
        dto.setSkalKunneFastsetteDelvisRefusjon(skalFastsetteDelvisRefusjon);

        // Valideringsfelter
        getTidligsteMuligeRefusjonsdato(andel, gjeldendeOvertyringer)
                .ifPresentOrElse(dto::setTidligsteMuligeRefusjonsdato, () -> dto.setTidligsteMuligeRefusjonsdato(gjeldendeBeregningsgrunnlag.getSkjæringstidspunkt()));
        dto.setMaksTillattDelvisRefusjonPrMnd(månedsbeløp(tidligereRefusjonForAndelIPeriode));

        // Tidligere fastsatte verdier som brukes til preutfylling av gui
        finnFastsattDelvisRefusjon(gjeldendeBeregningsgrunnlag, andel, periode).ifPresent(dto::setFastsattDelvisRefusjonPrMnd);
        getFastsattRefusjonStartdato(gjeldendeOvertyringer, andel).ifPresent(dto::setFastsattNyttRefusjonskravFom);

        return dto;
    }

    private static Optional<EksternArbeidsforholdRef> mapEksternReferanse(RefusjonAndel andel, Optional<ArbeidsforholdInformasjonDto> arbeidsforholdInformasjon) {
        return arbeidsforholdInformasjon.map(d -> d.finnEkstern(andel.getArbeidsgiver(), andel.getArbeidsforholdRef()));
    }

    private static Optional<BigDecimal> finnFastsattDelvisRefusjon(BeregningsgrunnlagDto gjeldendeBeregningsgrunnlag, RefusjonAndel andel, Intervall periode) {
        Optional<BeregningsgrunnlagPeriodeDto> bgPeriode = gjeldendeBeregningsgrunnlag.getBeregningsgrunnlagPerioder().stream()
                .filter(bgp -> bgp.getPeriode().inkluderer(periode.getFomDato()))
                .findFirst();
        List<BeregningsgrunnlagPrStatusOgAndelDto> bgAndelerIPeriode = bgPeriode
                .map(BeregningsgrunnlagPeriodeDto::getBeregningsgrunnlagPrStatusOgAndelList)
                .orElse(Collections.emptyList());
        Optional<BeregningsgrunnlagPrStatusOgAndelDto> matchetAndel = bgAndelerIPeriode.stream()
                .filter(bgAndel -> bgAndel.getArbeidsgiver().isPresent() && bgAndel.getArbeidsgiver().get().equals(andel.getArbeidsgiver())
                && bgAndel.getArbeidsforholdRef().orElse(InternArbeidsforholdRefDto.nullRef()).gjelderFor(andel.getArbeidsforholdRef()))
                .findFirst();
        Optional<BigDecimal> tidligereFastsattRefusjonPrÅr = matchetAndel.flatMap(bga -> bga.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getSaksbehandletRefusjonPrÅr));
        return tidligereFastsattRefusjonPrÅr.map(LagVurderRefusjonDto::månedsbeløp);
    }

    private static Optional<BeregningRefusjonPeriodeDto> finnTidligereOverstyringForAndel(List<BeregningRefusjonOverstyringDto> gjeldendeOvertyringer, RefusjonAndel andel) {
        List<BeregningRefusjonPeriodeDto> refusjonperioderForAG = gjeldendeOvertyringer.stream()
                .filter(os -> os.getArbeidsgiver().getIdentifikator().equals(andel.getArbeidsgiver().getIdentifikator()))
                .findFirst()
                .map(BeregningRefusjonOverstyringDto::getRefusjonPerioder)
                .orElse(Collections.emptyList());
        return refusjonperioderForAG.stream()
                .filter(refusjon -> refusjon.getArbeidsforholdRef().gjelderFor(andel.getArbeidsforholdRef()))
                .findFirst();
    }

    private static BigDecimal månedsbeløp(BigDecimal årsbeløp) {
        if (årsbeløp == null) {
            return BigDecimal.ZERO;
        }
        return årsbeløp.divide(MÅNEDER_I_ÅR, RoundingMode.HALF_UP);
    }

    private static boolean erRefusjonTidligereInnvilgetMedLavereBeløp(BigDecimal tidligereRefusjonForAndelIPeriode, RefusjonAndel andel) {
        return tidligereRefusjonForAndelIPeriode.compareTo(BigDecimal.ZERO) > 0 && andel.getRefusjon().compareTo(tidligereRefusjonForAndelIPeriode) > 0;
    }

    private static BigDecimal finnTidligereUtbetaltRefusjonForAndelIPeriode(Intervall periode, RefusjonAndel refusjonAndel, BeregningsgrunnlagDto orginalBG) {
        List<BeregningsgrunnlagPrStatusOgAndelDto> andelerIOrginalPeriode = finnOrginalBGPeriode(periode.getFomDato(), orginalBG).getBeregningsgrunnlagPrStatusOgAndelList();
        List<BeregningsgrunnlagPrStatusOgAndelDto> matchedeAndeler = andelerIOrginalPeriode.stream()
                .filter(bga -> {
                    InternArbeidsforholdRefDto bgAndelReferanse = bga.getArbeidsforholdRef().orElse(InternArbeidsforholdRefDto.nullRef());
                    no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver bgAndelAG = bga.getArbeidsgiver().orElse(null);
                    return Objects.equals(bgAndelAG, refusjonAndel.getArbeidsgiver()) && bgAndelReferanse.gjelderFor(refusjonAndel.getArbeidsforholdRef());
                })
                .collect(Collectors.toList());
        return matchedeAndeler.stream()
                .filter(andel -> andel.getBgAndelArbeidsforhold().isPresent() && andel.getBgAndelArbeidsforhold().get().getGjeldendeRefusjonPrÅr() != null)
                .map(a -> a.getBgAndelArbeidsforhold().get().getGjeldendeRefusjonPrÅr())
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
    }

    private static BeregningsgrunnlagPeriodeDto finnOrginalBGPeriode(LocalDate fomDato, BeregningsgrunnlagDto orginalBG) {
        if (fomDato.isBefore(orginalBG.getSkjæringstidspunkt())) {
            return orginalBG.getBeregningsgrunnlagPerioder().get(0);
        }
        return orginalBG.getBeregningsgrunnlagPerioder().stream()
                .filter(bgp -> bgp.getPeriode().inkluderer(fomDato))
                .findFirst()
                .orElseThrow();
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

    private static List<TidligereUtbetalingDto> finnTidligereUtbetalinger(no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver ag, BeregningsgrunnlagDto orginaltBG) {
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

    private static Optional<LocalDate> getFastsattRefusjonStartdato(List<BeregningRefusjonOverstyringDto> gjeldendeOvertyringer, RefusjonAndel andel) {
        return finnTidligereOverstyringForAndel(gjeldendeOvertyringer, andel).map(BeregningRefusjonPeriodeDto::getStartdatoRefusjon);
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
