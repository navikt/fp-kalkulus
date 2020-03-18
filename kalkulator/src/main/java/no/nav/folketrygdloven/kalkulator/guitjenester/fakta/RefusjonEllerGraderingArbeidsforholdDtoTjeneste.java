package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;

import static no.nav.vedtak.konfig.Tid.TIDENES_BEGYNNELSE;
import static no.nav.vedtak.konfig.Tid.TIDENES_ENDE;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.kalkulator.fordeling.FordelBeregningsgrunnlagTilfelleInput;
import no.nav.folketrygdloven.kalkulator.fordeling.FordelBeregningsgrunnlagTilfelleTjeneste;
import no.nav.folketrygdloven.kalkulator.fordeling.FordelingGraderingTjeneste;
import no.nav.folketrygdloven.kalkulator.fordeling.FordelingTilfelle;
import no.nav.folketrygdloven.kalkulator.gradering.AktivitetGradering;
import no.nav.folketrygdloven.kalkulator.gradering.AndelGradering.Gradering;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagRestInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.FordelBeregningsgrunnlagArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.GraderingEllerRefusjonDto;
import no.nav.folketrygdloven.kalkulator.guitjenester.BeregningsgrunnlagDtoUtil;

public class RefusjonEllerGraderingArbeidsforholdDtoTjeneste {

    private RefusjonEllerGraderingArbeidsforholdDtoTjeneste() {
        // Skjul
    }

    public static List<FordelBeregningsgrunnlagArbeidsforholdDto> lagListeMedDtoForArbeidsforholdSomSøkerRefusjonEllerGradering(BeregningsgrunnlagRestInput input, LocalDate skjæringstidspunktForBeregning) {
        List<BeregningsgrunnlagPeriodeDto> perioder = input.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder();
        FordelBeregningsgrunnlagTilfelleInput fordelingInput = FordelBeregningsgrunnlagTilfelleInput.fraBeregningsgrunnlagRestInput(input);

        List<BeregningsgrunnlagPrStatusOgAndelDto> beregningsgrunnlagPrStatusOgAndel = perioder.stream()
            .flatMap(periode -> periode.getBeregningsgrunnlagPrStatusOgAndelList().stream())
            .filter(andel -> andelHarTilfelleForFordeling(andel, fordelingInput))
            .filter(andel -> Boolean.FALSE.equals(andel.getLagtTilAvSaksbehandler()))
            .distinct()
            .collect(Collectors.toList());

        return beregningsgrunnlagPrStatusOgAndel.stream()
            .map(distinctAndel -> mapTilEndretArbeidsforholdDto(input, distinctAndel, skjæringstidspunktForBeregning, perioder))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .filter(af -> !af.getPerioderMedGraderingEllerRefusjon().isEmpty())
            .collect(Collectors.toList());
    }

    private static boolean andelHarTilfelleForFordeling(BeregningsgrunnlagPrStatusOgAndelDto andel, FordelBeregningsgrunnlagTilfelleInput fordelingInput) {
        BeregningsgrunnlagPeriodeDto periode = andel.getBeregningsgrunnlagPeriode();
        BeregningsgrunnlagPeriodeDto periodeFraSteg = fordelingInput.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().stream()
                .filter(p -> p.getPeriode().getFomDato().equals(periode.getBeregningsgrunnlagPeriodeFom()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Forventet å finne matchende periode"));
        Map<BeregningsgrunnlagPrStatusOgAndelDto, FordelingTilfelle> periodeTilfelleMap = FordelBeregningsgrunnlagTilfelleTjeneste.vurderManuellBehandlingForPeriode(periodeFraSteg, fordelingInput);
        return periodeTilfelleMap.keySet().stream().anyMatch(key -> Objects.equals(key.getAndelsnr(), andel.getAndelsnr()));
    }

    private static Optional<FordelBeregningsgrunnlagArbeidsforholdDto> mapTilEndretArbeidsforholdDto(BeregningsgrunnlagRestInput input,
                                                                                                     BeregningsgrunnlagPrStatusOgAndelDto distinctAndel,
                                                                                                     LocalDate stp, List<BeregningsgrunnlagPeriodeDto> perioder) {
        return BeregningsgrunnlagDtoUtil.lagArbeidsforholdEndringDto(distinctAndel, input.getIayGrunnlag())
            .map(af -> {
                FordelBeregningsgrunnlagArbeidsforholdDto endringAf = (FordelBeregningsgrunnlagArbeidsforholdDto) af;
                settEndretArbeidsforholdForNyttRefusjonskrav(distinctAndel, endringAf, perioder);
                settEndretArbeidsforholdForSøktGradering(distinctAndel, endringAf, input.getAktivitetGradering());
                distinctAndel.getBgAndelArbeidsforhold().flatMap(bga ->
                    UtledBekreftetPermisjonerTilDto.utled(input.getIayGrunnlag(), stp, bga)
                ).ifPresent(endringAf::setPermisjon);

                return endringAf;
            });
    }

    private static void settEndretArbeidsforholdForNyttRefusjonskrav(BeregningsgrunnlagPrStatusOgAndelDto distinctAndel,
                                                                     FordelBeregningsgrunnlagArbeidsforholdDto endretArbeidsforhold, List<BeregningsgrunnlagPeriodeDto> perioder) {
        List<Periode> refusjonsperioder = finnRefusjonsperioderForAndel(distinctAndel, perioder);
        refusjonsperioder.forEach(refusjonsperiode -> {
            GraderingEllerRefusjonDto refusjonDto = new GraderingEllerRefusjonDto(true, false);
            refusjonDto.setFom(refusjonsperiode.getFomOrNull());
            refusjonDto.setTom(TIDENES_ENDE.minusDays(2).isBefore(refusjonsperiode.getTom()) ? null : refusjonsperiode.getTom());
            endretArbeidsforhold.leggTilPeriodeMedGraderingEllerRefusjon(refusjonDto);
        });
    }

    private static List<Periode> finnRefusjonsperioderForAndel(BeregningsgrunnlagPrStatusOgAndelDto distinctAndel, List<BeregningsgrunnlagPeriodeDto> perioder) {
        List<Periode> refusjonsperioder = new ArrayList<>();
        LocalDate sluttDatoRefusjon = TIDENES_BEGYNNELSE;
        for (int i = 0; i < perioder.size(); i++) {
            BeregningsgrunnlagPeriodeDto periode = perioder.get(i);
            LocalDate tomDatoPeriode = periode.getBeregningsgrunnlagPeriodeTom() == null ?
                TIDENES_ENDE : periode.getBeregningsgrunnlagPeriodeTom();
            if (sluttDatoRefusjon.isBefore(tomDatoPeriode)) {
                Optional<BigDecimal> refusjonBeløpOpt = finnRefusjonsbeløpForAndelIPeriode(distinctAndel, periode);
                if (refusjonBeløpOpt.isPresent()) {
                    LocalDate startdatoRefusjon = periode.getBeregningsgrunnlagPeriodeFom();
                    sluttDatoRefusjon = finnSluttdato(distinctAndel, perioder, i, refusjonBeløpOpt.get());
                    refusjonsperioder.add(new Periode(startdatoRefusjon, sluttDatoRefusjon));
                }
            }
        }
        return refusjonsperioder;
    }

    private static LocalDate finnSluttdato(BeregningsgrunnlagPrStatusOgAndelDto distinctAndel, List<BeregningsgrunnlagPeriodeDto> perioder, int i, BigDecimal refusjonBeløp) {
        LocalDate sluttDatoRefusjon = TIDENES_ENDE;
        if (i == perioder.size() - 1) {
            return sluttDatoRefusjon;
        }
        for (int k = i + 1; k < perioder.size(); k++) {
            BeregningsgrunnlagPeriodeDto nestePeriode = perioder.get(k);
            BigDecimal refusjonINestePeriode = finnRefusjonsbeløpForAndelIPeriode(distinctAndel, nestePeriode).orElse(BigDecimal.ZERO);
            if (refusjonINestePeriode.compareTo(refusjonBeløp) != 0) {
                return perioder.get(k - 1).getBeregningsgrunnlagPeriodeTom();
            }
        }
        return sluttDatoRefusjon;
    }

    private static Optional<BigDecimal> finnRefusjonsbeløpForAndelIPeriode(BeregningsgrunnlagPrStatusOgAndelDto distinctAndel,
                                                                           BeregningsgrunnlagPeriodeDto periode) {
        Optional<BeregningsgrunnlagPrStatusOgAndelDto> matchendeAndel = periode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .filter(andel -> !andel.getLagtTilAvSaksbehandler())
            .filter(andel -> andel.gjelderSammeArbeidsforhold(distinctAndel))
            .filter(andel -> andel.getBgAndelArbeidsforhold()
                .map(BGAndelArbeidsforholdDto::getRefusjonskravPrÅr).orElse(BigDecimal.ZERO).compareTo(BigDecimal.ZERO) != 0)
            .findFirst();
        return matchendeAndel
            .flatMap(andel -> andel.getBgAndelArbeidsforhold()
                .map(BGAndelArbeidsforholdDto::getRefusjonskravPrÅr)
            );
    }

    private static void settEndretArbeidsforholdForSøktGradering(BeregningsgrunnlagPrStatusOgAndelDto distinctAndel,
                                                                 FordelBeregningsgrunnlagArbeidsforholdDto endretArbeidsforhold,
                                                                 AktivitetGradering aktivitetGradering) {
        List<Gradering> graderingerForArbeidsforhold = FordelingGraderingTjeneste.hentGraderingerForAndel(distinctAndel, aktivitetGradering);
        graderingerForArbeidsforhold.forEach(gradering -> {
            GraderingEllerRefusjonDto graderingDto = new GraderingEllerRefusjonDto(false, true);
            Intervall periode = gradering.getPeriode();
            graderingDto.setFom(periode.getFomDato());
            graderingDto.setTom(periode.getTomDato().isBefore(TIDENES_ENDE) ? periode.getTomDato() : null);
            endretArbeidsforhold.leggTilPeriodeMedGraderingEllerRefusjon(graderingDto);
        });
    }

}
