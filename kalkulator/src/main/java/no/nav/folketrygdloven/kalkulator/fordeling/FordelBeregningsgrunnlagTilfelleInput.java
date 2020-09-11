package no.nav.folketrygdloven.kalkulator.fordeling;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import no.nav.folketrygdloven.kalkulator.gradering.AktivitetGradering;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;

/**
 * Input for å utlede tilfelle for fordel beregningsgrunnlag
 */
public class FordelBeregningsgrunnlagTilfelleInput {

    private BeregningsgrunnlagDto beregningsgrunnlag;
    private BeregningAktivitetAggregatDto aktivitetAggregat;
    private AktivitetGradering aktivitetGradering;
    private Collection<InntektsmeldingDto> inntektsmeldinger;


    public FordelBeregningsgrunnlagTilfelleInput(BeregningsgrunnlagDto beregningsgrunnlag,
                                                 BeregningAktivitetAggregatDto aktivitetAggregat,
                                                 AktivitetGradering aktivitetGradering,
                                                 Collection<InntektsmeldingDto> inntektsmeldinger) {
        this.beregningsgrunnlag = beregningsgrunnlag;
        this.aktivitetAggregat = aktivitetAggregat;
        this.aktivitetGradering = aktivitetGradering;
        this.inntektsmeldinger = inntektsmeldinger;
        verifyStateForBuild();
    }

    public BeregningsgrunnlagDto getBeregningsgrunnlag() {
        return beregningsgrunnlag;
    }

    public BeregningAktivitetAggregatDto getAktivitetAggregat() {
        return aktivitetAggregat;
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
                ", aktivitetAggregat=" + aktivitetAggregat +
                ", aktivitetGradering=" + aktivitetGradering +
                ", inntektsmeldinger=" + inntektsmeldinger +
                '}';
    }

    public static FordelBeregningsgrunnlagTilfelleInput fraBeregningsgrunnlagRestInput(BeregningsgrunnlagGUIInput input) {
        BeregningsgrunnlagGrunnlagDto grunnlag = input.getBeregningsgrunnlagGrunnlag();
        BeregningsgrunnlagDto bg = input.getFordelBeregningsgrunnlag().orElseThrow(() -> new IllegalStateException("Skal ikke kalle fordel-logikk uten å ha utført steg"));
        AktivitetGradering aktivitetGradering = input.getAktivitetGradering();
        Collection<InntektsmeldingDto> inntektsmeldinger = input.getInntektsmeldinger();
        return new FordelBeregningsgrunnlagTilfelleInput(bg, grunnlag.getGjeldendeAktiviteter(), aktivitetGradering, inntektsmeldinger);
    }

    private void verifyStateForBuild() {
        Objects.requireNonNull(beregningsgrunnlag, "Beregningsgrunnlag");
        Objects.requireNonNull(aktivitetAggregat, "Aktivitetaggregat");
    }

}
