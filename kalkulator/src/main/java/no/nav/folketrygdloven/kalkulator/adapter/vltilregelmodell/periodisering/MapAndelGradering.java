package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering;

import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.AndelGraderingImpl;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapArbeidsforholdFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.kodeverk.MapAktivitetStatusV2FraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.felles.FinnYrkesaktiviteterForBeregningTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.gradering.AndelGradering;
import no.nav.folketrygdloven.kalkulator.modell.gradering.AndelGradering.Gradering;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetFilterDto;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;
import no.nav.fpsak.tidsserie.StandardCombinators;


public final class MapAndelGradering {
    private MapAndelGradering() {
        // private constructor
    }

    public static AndelGraderingImpl mapTilRegelAndelGraderingForFLSN(BeregningsgrunnlagDto beregningsgrunnlag,
                                                                      KoblingReferanse ref,
                                                                      AndelGradering andelGradering,
                                                                      YrkesaktivitetFilterDto filter) {
        if (andelGradering.getAktivitetStatus().erArbeidstaker()) {
            throw new IllegalArgumentException("Gradering for arbeidstaker skal ikke mappes her");
        }
        var regelAktivitetStatus = MapAktivitetStatusV2FraVLTilRegel.map(andelGradering.getAktivitetStatus(), null);
        List<no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Gradering> graderinger = mapGraderingPerioder(andelGradering.getGraderinger());
        AndelGraderingImpl.Builder builder = AndelGraderingImpl.builder()
            .medAktivitetStatus(regelAktivitetStatus)
            .medGraderinger(graderinger);


        if (andelGradering.getAktivitetStatus().erFrilanser() || andelGradering.getAktivitetStatus().erSelvstendigNæringsdrivende()){
            settTidslinjeForNyAktivitetForStatus(beregningsgrunnlag, builder, andelGradering.getAktivitetStatus());
        }

        // Finner yrkesaktiviteter inkludert fjernet i overstyring siden vi kun er interessert i å lage nye arbeidsforhold for nye aktiviteter (Disse kan ikke fjernes)
        Optional<YrkesaktivitetDto> yrkesaktivitet = FinnYrkesaktiviteterForBeregningTjeneste.finnAlleYrkesaktiviteterInkludertFjernetIOverstyring(ref, filter)
            .stream()
            .filter(ya -> ya.gjelderFor(andelGradering.getArbeidsgiver(), andelGradering.getArbeidsforholdRef()))
            .findFirst();

        if (andelGradering.getArbeidsgiver() != null) {
            Arbeidsforhold arbeidsforhold = MapArbeidsforholdFraVLTilRegel.mapArbeidsforhold(
                andelGradering.getArbeidsgiver(),
                andelGradering.getArbeidsforholdRef());
            yrkesaktivitet.ifPresent(ya -> Arbeidsforhold.builder(arbeidsforhold)
                .medAnsettelsesPeriode(FinnAnsettelsesPeriode.getMinMaksPeriode(filter.getAnsettelsesPerioder(ya),
                    ref.getSkjæringstidspunktBeregning())));
            builder.medArbeidsforhold(arbeidsforhold);
        }
        return builder.build();
    }

    private static List<no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Gradering> mapGraderingPerioder(List<Gradering> graderingList) {
        return graderingList.stream()
            .map(gradering -> new no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Gradering(
                Periode.of(gradering.getPeriode().getFomDato(), gradering.getPeriode().getTomDato()),
                gradering.getArbeidstidProsent()))
            .collect(Collectors.toList());
    }


    private static void settTidslinjeForNyAktivitetForStatus(BeregningsgrunnlagDto beregningsgrunnlag,
                                                             AndelGraderingImpl.Builder builder,
                                                             no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus status) {
        builder.medNyAktivitetTidslinje(finnNyAndelTidslinje(status, beregningsgrunnlag));
    }

    private static LocalDateTimeline<Boolean> finnNyAndelTidslinje(no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus aktivitetstatus, BeregningsgrunnlagDto beregningsgrunnlag) {
        var eksisterendeAndelSegmenter = beregningsgrunnlag.getBeregningsgrunnlagPerioder().stream()
                .filter(p -> p.getBeregningsgrunnlagPrStatusOgAndelList()
                        .stream().anyMatch(andel -> andel.getAktivitetStatus().equals(aktivitetstatus)))
                .map(p -> new LocalDateSegment<>(p.getBeregningsgrunnlagPeriodeFom(), p.getBeregningsgrunnlagPeriodeTom(), false))
                .collect(Collectors.toList());
        LocalDateTimeline<Boolean> eksisterendeAndelTidslinje = new LocalDateTimeline<>(eksisterendeAndelSegmenter);
        return new LocalDateTimeline<>(beregningsgrunnlag.getSkjæringstidspunkt(), TIDENES_ENDE, true)
                .crossJoin(eksisterendeAndelTidslinje, StandardCombinators::coalesceRightHandSide);
    }



}
