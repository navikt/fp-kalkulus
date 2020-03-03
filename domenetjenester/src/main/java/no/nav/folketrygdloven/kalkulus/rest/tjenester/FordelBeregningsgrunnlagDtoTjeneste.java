package no.nav.folketrygdloven.kalkulus.rest.tjenester;

import static java.util.stream.Collectors.toList;
import static no.nav.folketrygdloven.kalkulus.felles.tid.AbstractIntervall.TIDENES_ENDE;

import java.util.Comparator;
import java.util.List;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagRestInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.FordelBeregningsgrunnlagAndelDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.FordelBeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.FordelBeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.FordelingDto;
import no.nav.folketrygdloven.kalkulus.rest.tjenester.fakta.ManuellBehandlingRefusjonGraderingDtoTjeneste;
import no.nav.folketrygdloven.kalkulus.rest.tjenester.fakta.RefusjonDtoTjeneste;
import no.nav.folketrygdloven.kalkulus.rest.tjenester.fakta.RefusjonEllerGraderingArbeidsforholdDtoTjeneste;

public class FordelBeregningsgrunnlagDtoTjeneste {

    private FordelBeregningsgrunnlagDtoTjeneste() {
        // Skjul
    }

    public static void lagDto(BeregningsgrunnlagRestInput input,
                              FordelingDto dto) {
        FordelBeregningsgrunnlagDto bgDto = new FordelBeregningsgrunnlagDto();
        settEndretArbeidsforholdDto(input, bgDto);
        bgDto.setFordelBeregningsgrunnlagPerioder(lagPerioder(input));
        dto.setFordelBeregningsgrunnlag(bgDto);
    }

    private static List<FordelBeregningsgrunnlagPeriodeDto> lagPerioder(BeregningsgrunnlagRestInput input) {
        BeregningsgrunnlagDto beregningsgrunnlag = input.getBeregningsgrunnlag();
        List<BeregningsgrunnlagPeriodeDto> bgPerioder = beregningsgrunnlag.getBeregningsgrunnlagPerioder();
        List<FordelBeregningsgrunnlagPeriodeDto> fordelPerioder = bgPerioder.stream()
                .map(periode -> mapTilPeriodeDto(input, periode, beregningsgrunnlag.getGrunnbeløp()))
                .sorted(Comparator.comparing(FordelBeregningsgrunnlagPeriodeDto::getFom)).collect(toList());
        return fordelPerioder;
    }

    private static FordelBeregningsgrunnlagPeriodeDto mapTilPeriodeDto(BeregningsgrunnlagRestInput input,
                                                                       BeregningsgrunnlagPeriodeDto periode,
                                                                       Beløp grunnbeløp) {
        FordelBeregningsgrunnlagPeriodeDto fordelBGPeriode = new FordelBeregningsgrunnlagPeriodeDto();
        fordelBGPeriode.setFom(periode.getBeregningsgrunnlagPeriodeFom());
        fordelBGPeriode.setTom(periode.getBeregningsgrunnlagPeriodeTom() == TIDENES_ENDE ? null : periode.getBeregningsgrunnlagPeriodeTom());
        List<FordelBeregningsgrunnlagAndelDto> fordelAndeler = lagFordelBGAndeler(fordelBGPeriode, input, periode, grunnbeløp);
        fordelBGPeriode.setFordelBeregningsgrunnlagAndeler(fordelAndeler);
        return fordelBGPeriode;
    }

    private static void settEndretArbeidsforholdDto(BeregningsgrunnlagRestInput input, FordelBeregningsgrunnlagDto bgDto) {
        RefusjonEllerGraderingArbeidsforholdDtoTjeneste
                .lagListeMedDtoForArbeidsforholdSomSøkerRefusjonEllerGradering(input, input.getSkjæringstidspunktForBeregning())
                .forEach(bgDto::leggTilArbeidsforholdTilFordeling);
    }

    private static List<FordelBeregningsgrunnlagAndelDto> lagFordelBGAndeler(FordelBeregningsgrunnlagPeriodeDto fordelBGPeriode,
                                                                             BeregningsgrunnlagRestInput input,
                                                                             BeregningsgrunnlagPeriodeDto periode,
                                                                             Beløp grunnbeløp) {
        var aktivitetGradering = input.getAktivitetGradering();
        List<FordelBeregningsgrunnlagAndelDto> fordelAndeler = FordelBeregningsgrunnlagAndelDtoTjeneste.lagEndretBgAndelListe(input, periode);
        fordelBGPeriode.setHarPeriodeAarsakGraderingEllerRefusjon(ManuellBehandlingRefusjonGraderingDtoTjeneste
                .skalSaksbehandlerRedigereInntekt(
                        input.getBeregningsgrunnlagGrunnlag(),
                        aktivitetGradering,
                        periode,
                        input.getInntektsmeldinger()));
        fordelBGPeriode.setSkalKunneEndreRefusjon(ManuellBehandlingRefusjonGraderingDtoTjeneste
                .skalSaksbehandlerRedigereRefusjon(
                        input.getBeregningsgrunnlagGrunnlag(),
                        aktivitetGradering,
                        periode,
                        input.getInntektsmeldinger(),
                        grunnbeløp));
        RefusjonDtoTjeneste.slåSammenRefusjonForAndelerISammeArbeidsforhold(fordelAndeler);
        return fordelAndeler;
    }

}
