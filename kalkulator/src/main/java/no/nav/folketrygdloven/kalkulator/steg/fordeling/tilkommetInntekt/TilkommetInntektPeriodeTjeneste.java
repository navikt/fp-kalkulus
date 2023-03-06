package no.nav.folketrygdloven.kalkulator.steg.fordeling.tilkommetInntekt;

import static no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.beregningsperiode.BeregningsperiodeTjeneste.INNTEKT_RAPPORTERING_FRIST_DATO;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.TilkommetInntektDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktørArbeidDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktørYtelseDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.StatusOgArbeidsgiver;
import no.nav.folketrygdloven.kalkulus.kodeverk.InntektskildeType;
import no.nav.folketrygdloven.kalkulus.kodeverk.PeriodeÅrsak;
import no.nav.fpsak.tidsserie.LocalDateInterval;
import no.nav.fpsak.tidsserie.LocalDateSegment;

/**
 * Ved nye inntektsforhold skal beregningsgrunnlaget graderes mot inntekt.
 * <p>
 * Utleder her om det er potensielle nye inntektsforhold og oppretter perioder.
 * <p>
 * Se <a href="https://navno.sharepoint.com/sites/fag-og-ytelser-arbeid-sykefravarsoppfolging-og-sykepenger/SitePages/%C2%A7-8-13-Graderte-sykepenger.aspx">...</a>
 */
public class TilkommetInntektPeriodeTjeneste {


    public BeregningsgrunnlagDto splittPerioderVedTilkommetInntekt(BeregningsgrunnlagInput input, BeregningsgrunnlagDto beregningsgrunnlag) {

        var tilkommetAktivitetTidslinje = TilkommetInntektsforholdTjeneste.finnTilkommetInntektsforholdTidslinje(
                beregningsgrunnlag.getSkjæringstidspunkt(),
                (int) input.getKonfigVerdi(INNTEKT_RAPPORTERING_FRIST_DATO),
                beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList(),
                input.getYtelsespesifiktGrunnlag(),
                input.getIayGrunnlag(),
                input.getFagsakYtelseType()
                );
        var tidlinjeMedTilkommetAktivitet = tilkommetAktivitetTidslinje.filterValue(v -> !v.isEmpty()).compress();
        return SplittBGPerioder.splittPerioder(beregningsgrunnlag,
                tidlinjeMedTilkommetAktivitet,
                TilkommetInntektPeriodeTjeneste::opprettTilkommetInntekt,
                SplittBGPerioder.getSettAvsluttetPeriodeårsakMapper(tidlinjeMedTilkommetAktivitet, input.getForlengelseperioder(), PeriodeÅrsak.TILKOMMET_INNTEKT_AVSLUTTET));
    }


    public static LocalDateSegment<BeregningsgrunnlagPeriodeDto> opprettTilkommetInntekt(LocalDateInterval di,
                                                                                         LocalDateSegment<BeregningsgrunnlagPeriodeDto> lhs,
                                                                                         LocalDateSegment<Set<StatusOgArbeidsgiver>> rhs) {
        if (lhs != null && rhs != null) {
            var nyPeriode = BeregningsgrunnlagPeriodeDto.kopier(lhs.getValue())
                    .leggTilPeriodeÅrsak(PeriodeÅrsak.TILKOMMET_INNTEKT)
                    .medBeregningsgrunnlagPeriode(di.getFomDato(), di.getTomDato());
            mapTilkomneInntekter(lhs.getValue(), rhs.getValue()).forEach(nyPeriode::leggTilTilkommetInntekt);
            return new LocalDateSegment<>(di, nyPeriode.build());
        } else if (lhs != null) {
            var nyPeriode = BeregningsgrunnlagPeriodeDto.kopier(lhs.getValue())
                    .medBeregningsgrunnlagPeriode(di.getFomDato(), di.getTomDato())
                    .build();
            return new LocalDateSegment<>(di, nyPeriode);
        }
        return null;
    }


    private static List<TilkommetInntektDto> mapTilkomneInntekter(BeregningsgrunnlagPeriodeDto periode, Set<StatusOgArbeidsgiver> nyeInntektsforhold) {
        var innteksforholdListe = new ArrayList<TilkommetInntektDto>();
        innteksforholdListe.addAll(mapAndelerTilInntektsforholdDto(periode, nyeInntektsforhold));
        innteksforholdListe.addAll(mapResterendeUtenAndel(nyeInntektsforhold, innteksforholdListe));
        return innteksforholdListe;
    }


    private static Set<TilkommetInntektDto> mapResterendeUtenAndel(Set<StatusOgArbeidsgiver> nyeInntektsforholdListe,
                                                                   Collection<TilkommetInntektDto> tilkomneInntektsforhold) {
        var tilkomneInntekterUtenAndel = finnTilkomneUtenAndel(nyeInntektsforholdListe, tilkomneInntektsforhold);
        return tilkomneInntekterUtenAndel.stream()
                .map(it -> new TilkommetInntektDto(it.aktivitetStatus(), it.arbeidsgiver(), InternArbeidsforholdRefDto.nullRef()))
                .collect(Collectors.toSet());
    }

    private static List<StatusOgArbeidsgiver> finnTilkomneUtenAndel(Set<StatusOgArbeidsgiver> nyeInntektsforholdListe,
                                                                    Collection<TilkommetInntektDto> tilkomneInntektsforhold) {
        return nyeInntektsforholdListe.stream()
                .filter(it -> tilkomneInntektsforhold.stream().noneMatch(ti -> ti.getAktivitetStatus().equals(it.aktivitetStatus()) &&
                        Objects.equals(ti.getArbeidsgiver().orElse(null), it.arbeidsgiver())))
                .toList();
    }

    private static Set<TilkommetInntektDto> mapAndelerTilInntektsforholdDto(BeregningsgrunnlagPeriodeDto periode,
                                                                            Set<StatusOgArbeidsgiver> nyeInntektsforholdListe) {
        return periode.getBeregningsgrunnlagPrStatusOgAndelList()
                .stream()
                .filter(a -> erNyttArbeidsforhold(nyeInntektsforholdListe, a))
                .map(TilkommetInntektPeriodeTjeneste::mapTilInntektsforhold)
                .collect(Collectors.toCollection(HashSet::new));
    }

    private static boolean erNyttArbeidsforhold(Set<StatusOgArbeidsgiver> nyeInntektsforholdListe, BeregningsgrunnlagPrStatusOgAndelDto a) {
        return finnMatchendeNyttInntektsforhold(nyeInntektsforholdListe, a).isPresent();
    }

    private static Optional<StatusOgArbeidsgiver> finnMatchendeNyttInntektsforhold(Set<StatusOgArbeidsgiver> nyeInntektsforholdListe, BeregningsgrunnlagPrStatusOgAndelDto a) {
        return nyeInntektsforholdListe.stream().filter(sa -> sa.aktivitetStatus().equals(a.getAktivitetStatus()) &&
                Objects.equals(sa.arbeidsgiver(), a.getArbeidsgiver().orElse(null))).findFirst();
    }

    private static TilkommetInntektDto mapTilInntektsforhold(BeregningsgrunnlagPrStatusOgAndelDto a) {
        return new TilkommetInntektDto(
                a.getAktivitetStatus(),
                a.getArbeidsgiver().orElse(null),
                a.getArbeidsforholdRef().orElse(InternArbeidsforholdRefDto.nullRef()));
    }


}
