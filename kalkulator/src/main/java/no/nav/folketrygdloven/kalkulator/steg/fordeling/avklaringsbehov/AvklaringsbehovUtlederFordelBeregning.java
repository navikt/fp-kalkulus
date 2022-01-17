package no.nav.folketrygdloven.kalkulator.steg.fordeling.avklaringsbehov;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.input.ForeldrepengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.gradering.AktivitetGradering;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningAvklaringsbehovResultat;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;

public class AvklaringsbehovUtlederFordelBeregning {

    private AvklaringsbehovUtlederFordelBeregning() {
        // Skjul
    }

    public static List<BeregningAvklaringsbehovResultat> utledAvklaringsbehovFor(KoblingReferanse ref,
                                                                                 BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag,
                                                                                 YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag,
                                                                                 Collection<InntektsmeldingDto> inntektsmeldinger,
                                                                                 List<Intervall> forlengelseperioder) {
        var perioderTilManuellVurdering = finnPerioderMedTilfellerForFordeling(ref, beregningsgrunnlagGrunnlag, ytelsespesifiktGrunnlag, inntektsmeldinger, forlengelseperioder);
        return perioderTilManuellVurdering.isEmpty() ? Collections.emptyList():
                List.of(BeregningAvklaringsbehovResultat.opprettFor(AvklaringsbehovDefinisjon.FORDEL_BEREGNINGSGRUNNLAG));
    }

    private static List<Intervall> finnPerioderMedTilfellerForFordeling(@SuppressWarnings("unused") KoblingReferanse ref,
                                                                        BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag,
                                                                        YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag,
                                                                        Collection<InntektsmeldingDto> inntektsmeldinger,
                                                                        List<Intervall> forlengelseperioder) {
        BeregningsgrunnlagDto beregningsgrunnlag = beregningsgrunnlagGrunnlag.getBeregningsgrunnlag()
                .orElseThrow(() -> new IllegalStateException("Utviklerfeil: Mangler beregningsgrunnlagGrunnlag"));
        FordelBeregningsgrunnlagTilfelleInput fordelingInput = new FordelBeregningsgrunnlagTilfelleInput(beregningsgrunnlag, finnGraderinger(ytelsespesifiktGrunnlag), inntektsmeldinger, forlengelseperioder);
        return FordelBeregningsgrunnlagTilfelleTjeneste.finnPerioderMedBehovForManuellVurdering(fordelingInput);
    }

    private static AktivitetGradering finnGraderinger(YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {
        return ytelsespesifiktGrunnlag instanceof ForeldrepengerGrunnlag ? ((ForeldrepengerGrunnlag) ytelsespesifiktGrunnlag).getAktivitetGradering() : AktivitetGradering.INGEN_GRADERING;
    }
}
