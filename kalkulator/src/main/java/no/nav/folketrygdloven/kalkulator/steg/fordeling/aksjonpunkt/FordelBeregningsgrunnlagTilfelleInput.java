package no.nav.folketrygdloven.kalkulator.steg.fordeling.aksjonpunkt;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.input.ForeldrepengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.gradering.AktivitetGradering;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;

/**
 * Input for å utlede tilfelle for fordel beregningsgrunnlag
 */
public class FordelBeregningsgrunnlagTilfelleInput {

    private BeregningsgrunnlagDto beregningsgrunnlag;
    private AktivitetGradering aktivitetGradering;
    private Collection<InntektsmeldingDto> inntektsmeldinger;


    public FordelBeregningsgrunnlagTilfelleInput(BeregningsgrunnlagDto beregningsgrunnlag,
                                                 AktivitetGradering aktivitetGradering,
                                                 Collection<InntektsmeldingDto> inntektsmeldinger) {
        this.beregningsgrunnlag = beregningsgrunnlag;
        this.aktivitetGradering = aktivitetGradering;
        this.inntektsmeldinger = inntektsmeldinger;
        verifyStateForBuild();
    }

    public BeregningsgrunnlagDto getBeregningsgrunnlag() {
        return beregningsgrunnlag;
    }

    public AktivitetGradering getAktivitetGradering() {
        return aktivitetGradering;
    }

    public Collection<InntektsmeldingDto> getInntektsmeldinger() {
        return Collections.unmodifiableCollection(inntektsmeldinger);
    }

    @Override
    public String toString() {
        return "FordelBeregningsgrunnlagTilfelleInput{" +
                "beregningsgrunnlag=" + beregningsgrunnlag +
                ", aktivitetGradering=" + aktivitetGradering +
                ", inntektsmeldinger=" + inntektsmeldinger +
                '}';
    }

    public static FordelBeregningsgrunnlagTilfelleInput fraBeregningsgrunnlagRestInput(BeregningsgrunnlagGUIInput input) {
        BeregningsgrunnlagDto bg = input.getFordelBeregningsgrunnlag().orElseThrow(() -> new IllegalStateException("Skal ikke kalle fordel-logikk uten å ha utført steg"));
        AktivitetGradering aktivitetGradering = input.getYtelsespesifiktGrunnlag() instanceof ForeldrepengerGrunnlag ?
                ((ForeldrepengerGrunnlag) input.getYtelsespesifiktGrunnlag()).getAktivitetGradering() : AktivitetGradering.INGEN_GRADERING;
        Collection<InntektsmeldingDto> inntektsmeldinger = input.getInntektsmeldinger();
        return new FordelBeregningsgrunnlagTilfelleInput(bg, aktivitetGradering, inntektsmeldinger);
    }

    private void verifyStateForBuild() {
        Objects.requireNonNull(beregningsgrunnlag, "Beregningsgrunnlag");
    }

}
