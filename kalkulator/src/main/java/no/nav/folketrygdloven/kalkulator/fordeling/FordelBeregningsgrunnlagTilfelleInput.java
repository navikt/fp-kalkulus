package no.nav.folketrygdloven.kalkulator.fordeling;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagRestInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.gradering.AktivitetGradering;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagRestDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.rest.MapBeregningsgrunnlagFraRestTilDomene;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

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

    public static FordelBeregningsgrunnlagTilfelleInput fraBeregningsgrunnlagRestInput(BeregningsgrunnlagRestInput input) {
        BeregningsgrunnlagGrunnlagRestDto restGr = input.getBeregningsgrunnlagGrunnlag();
        BeregningsgrunnlagRestDto restBG = input.getBeregningsgrunnlag();
        BeregningsgrunnlagDto domeneBG = MapBeregningsgrunnlagFraRestTilDomene.mapBeregningsgrunnlag(restBG);
        BeregningAktivitetAggregatDto domeneAktiviteter = MapBeregningsgrunnlagFraRestTilDomene.mapAktivitetAggregat(restGr.getGjeldendeAktiviteter());
        AktivitetGradering aktivitetGradering = input.getAktivitetGradering();
        Collection<InntektsmeldingDto> inntektsmeldinger = input.getInntektsmeldinger();
        return new FordelBeregningsgrunnlagTilfelleInput(domeneBG, domeneAktiviteter, aktivitetGradering, inntektsmeldinger);
    }

    private void verifyStateForBuild() {
        Objects.requireNonNull(beregningsgrunnlag, "Beregningsgrunnlag");
        Objects.requireNonNull(aktivitetAggregat, "Aktivitetaggregat");
    }

}
