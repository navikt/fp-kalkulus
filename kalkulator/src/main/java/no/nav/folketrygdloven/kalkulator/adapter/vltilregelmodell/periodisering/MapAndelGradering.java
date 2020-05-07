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
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetFilterDto;


public final class MapAndelGradering {
    private MapAndelGradering() {
        // private constructor
    }

    public static AndelGraderingImpl mapTilRegelAndelGradering(BehandlingReferanse ref, AndelGradering andelGradering,
                                                               YrkesaktivitetFilterDto filter) {
        var regelAktivitetStatus = MapAktivitetStatusV2FraVLTilRegel.map(andelGradering.getAktivitetStatus(), null);
        List<no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Gradering> graderinger = mapGraderingPerioder(andelGradering.getGraderinger());
        AndelGraderingImpl.Builder builder = AndelGraderingImpl.builder()
            .medAktivitetStatus(regelAktivitetStatus)
            .medGraderinger(graderinger);
        builder.medAndelsnr(andelGradering.getAndelsnr());

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
                .medAnsettelsesPeriode(FinnAnsettelsesPeriode.getMinMaksPeriode(ref.getFagsakYtelseType(), filter.getAnsettelsesPerioder(ya),
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
}
