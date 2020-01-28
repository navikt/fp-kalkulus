package no.nav.folketrygdloven.kalkulator.rest;

import static java.util.stream.Collectors.toList;

import java.util.Comparator;
import java.util.List;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagRestInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagRestDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.rest.dto.FordelBeregningsgrunnlagAndelDto;
import no.nav.folketrygdloven.kalkulator.rest.dto.FordelBeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.rest.dto.FordelBeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.rest.dto.FordelingDto;
import no.nav.folketrygdloven.kalkulator.rest.fakta.ManuellBehandlingRefusjonGraderingDtoTjeneste;
import no.nav.folketrygdloven.kalkulator.rest.fakta.RefusjonDtoTjeneste;
import no.nav.folketrygdloven.kalkulator.rest.fakta.RefusjonEllerGraderingArbeidsforholdDtoTjeneste;

public class FordelBeregningsgrunnlagDtoTjeneste {

    private FordelBeregningsgrunnlagDtoTjeneste() {
        // Skjul
    }

    public static void lagDto(BeregningsgrunnlagRestInput input,
                       FordelingDto dto) {
        FordelBeregningsgrunnlagDto bgDto = new FordelBeregningsgrunnlagDto();
        settEndretArbeidsforholdDto(input, bgDto, input.getBeregningsgrunnlag().getGrunnbeløp());
        bgDto.setFordelBeregningsgrunnlagPerioder(lagPerioder(input));
        dto.setFordelBeregningsgrunnlag(bgDto);
    }

    private static List<FordelBeregningsgrunnlagPeriodeDto> lagPerioder(BeregningsgrunnlagRestInput input) {
        BeregningsgrunnlagRestDto beregningsgrunnlag = input.getBeregningsgrunnlag();
        List<BeregningsgrunnlagPeriodeRestDto> bgPerioder = beregningsgrunnlag.getBeregningsgrunnlagPerioder();
        List<FordelBeregningsgrunnlagPeriodeDto> fordelPerioder = bgPerioder.stream()
            .map(periode -> mapTilPeriodeDto(input, periode, beregningsgrunnlag.getGrunnbeløp()))
            .sorted(Comparator.comparing(FordelBeregningsgrunnlagPeriodeDto::getFom)).collect(toList());
        return fordelPerioder;
    }

    private static FordelBeregningsgrunnlagPeriodeDto mapTilPeriodeDto(BeregningsgrunnlagRestInput input,
                                                                       BeregningsgrunnlagPeriodeRestDto periode,
                                                                       Beløp grunnbeløp) {
        FordelBeregningsgrunnlagPeriodeDto fordelBGPeriode = new FordelBeregningsgrunnlagPeriodeDto();
        fordelBGPeriode.setFom(periode.getBeregningsgrunnlagPeriodeFom());
        fordelBGPeriode.setTom(periode.getBeregningsgrunnlagPeriodeTom());
        List<FordelBeregningsgrunnlagAndelDto> fordelAndeler = lagFordelBGAndeler(fordelBGPeriode, input, periode, grunnbeløp);
        fordelBGPeriode.setFordelBeregningsgrunnlagAndeler(fordelAndeler);
        return fordelBGPeriode;
    }

    private static void settEndretArbeidsforholdDto(BeregningsgrunnlagRestInput input, FordelBeregningsgrunnlagDto bgDto, Beløp grunnbeløp) {
        RefusjonEllerGraderingArbeidsforholdDtoTjeneste
            .lagListeMedDtoForArbeidsforholdSomSøkerRefusjonEllerGradering(input, grunnbeløp, input.getSkjæringstidspunktForBeregning())
            .forEach(bgDto::leggTilArbeidsforholdTilFordeling);
    }

    private static List<FordelBeregningsgrunnlagAndelDto> lagFordelBGAndeler(FordelBeregningsgrunnlagPeriodeDto fordelBGPeriode,
                                                                             BeregningsgrunnlagRestInput input,
                                                                             BeregningsgrunnlagPeriodeRestDto periode,
                                                                             Beløp grunnbeløp) {
        var beregningAktivitetAggregat = input.getBeregningsgrunnlagGrunnlag().getGjeldendeAktiviteter();
        var aktivitetGradering = input.getAktivitetGradering();
        List<FordelBeregningsgrunnlagAndelDto> fordelAndeler = FordelBeregningsgrunnlagAndelDtoTjeneste.lagEndretBgAndelListe(input, periode);
        fordelBGPeriode.setHarPeriodeAarsakGraderingEllerRefusjon(ManuellBehandlingRefusjonGraderingDtoTjeneste
            .skalSaksbehandlerRedigereInntekt(beregningAktivitetAggregat,
                aktivitetGradering,
                periode,
                input.getInntektsmeldinger(),
                grunnbeløp,
                input.getSkjæringstidspunktForBeregning()));
        fordelBGPeriode.setSkalKunneEndreRefusjon(ManuellBehandlingRefusjonGraderingDtoTjeneste
            .skalSaksbehandlerRedigereRefusjon(beregningAktivitetAggregat,
                aktivitetGradering,
                periode,
                input.getInntektsmeldinger(),
                grunnbeløp,
                input.getSkjæringstidspunktForBeregning()));
        RefusjonDtoTjeneste.slåSammenRefusjonForAndelerISammeArbeidsforhold(fordelAndeler);
        return fordelAndeler;
    }

}
