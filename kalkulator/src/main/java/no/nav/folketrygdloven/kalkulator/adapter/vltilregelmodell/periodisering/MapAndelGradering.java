package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AndelGraderingImpl;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.kalkulator.FinnYrkesaktiviteterForBeregningTjeneste;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapArbeidsforholdFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.kodeverk.MapAktivitetStatusV2FraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.gradering.AndelGradering;
import no.nav.folketrygdloven.kalkulator.gradering.AndelGradering.Gradering;
import no.nav.folketrygdloven.kalkulator.modell.behandling.BehandlingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetFilterDto;


public final class MapAndelGradering {
    private MapAndelGradering() {
        // private constructor
    }

    public static AndelGraderingImpl mapTilRegelAndelGradering(BeregningsgrunnlagDto beregningsgrunnlag,
                                                               BehandlingReferanse ref,
                                                               AndelGradering andelGradering,
                                                               YrkesaktivitetFilterDto filter) {
        var regelAktivitetStatus = MapAktivitetStatusV2FraVLTilRegel.map(andelGradering.getAktivitetStatus(), null);
        List<no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Gradering> graderinger = mapGraderingPerioder(andelGradering.getGraderinger());
        AndelGraderingImpl.Builder builder = AndelGraderingImpl.builder()
            .medAktivitetStatus(regelAktivitetStatus)
            .medGraderinger(graderinger);

        // TODO (Andelsnr skal fjernes. Matching av perioder med utebetalingsgrad må fikses før denne fjernes)
        if ( andelGradering.getAndelsnr() != null) {
            builder.medAndelsnr(andelGradering.getAndelsnr());
        } else if (andelGradering.getAktivitetStatus().erFrilanser() || andelGradering.getAktivitetStatus().erSelvstendigNæringsdrivende()){
            settAndelsnrForStatus(beregningsgrunnlag, builder, andelGradering.getAktivitetStatus());
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


    private static void settAndelsnrForStatus(BeregningsgrunnlagDto beregningsgrunnlag,
                                              AndelGraderingImpl.Builder builder,
                                              no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus status) {
        Optional<BeregningsgrunnlagPrStatusOgAndelDto> matchetAndel = finnAndelIGrunnlag(status, beregningsgrunnlag);
        matchetAndel.ifPresent(beregningsgrunnlagPrStatusOgAndel -> builder.medAndelsnr(beregningsgrunnlagPrStatusOgAndel.getAndelsnr()));
    }

    private static Optional<BeregningsgrunnlagPrStatusOgAndelDto> finnAndelIGrunnlag(no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus aktivitetstatus, BeregningsgrunnlagDto beregningsgrunnlag) {
        BeregningsgrunnlagPeriodeDto periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        List<BeregningsgrunnlagPrStatusOgAndelDto> andeler = periode.getBeregningsgrunnlagPrStatusOgAndelList();
        return andeler.stream().filter(andel -> andel.getAktivitetStatus().equals(aktivitetstatus)).findFirst();
    }


}
