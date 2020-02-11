package no.nav.folketrygdloven.kalkulator.rest.fakta;

import static no.nav.folketrygdloven.kalkulator.rest.MapBeregningsgrunnlagFraRestTilDomene.mapAndel;
import static no.nav.folketrygdloven.kalkulator.rest.MapBeregningsgrunnlagFraRestTilDomene.mapPeriode;
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
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelRestDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.rest.BeregningsgrunnlagDtoUtil;
import no.nav.folketrygdloven.kalkulator.rest.dto.FordelBeregningsgrunnlagArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.rest.dto.GraderingEllerRefusjonDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;

public class RefusjonEllerGraderingArbeidsforholdDtoTjeneste {

    private RefusjonEllerGraderingArbeidsforholdDtoTjeneste() {
        // Skjul
    }

    public static List<FordelBeregningsgrunnlagArbeidsforholdDto> lagListeMedDtoForArbeidsforholdSomSøkerRefusjonEllerGradering(BeregningsgrunnlagRestInput input, Beløp grunnbeløp, LocalDate skjæringstidspunktForBeregning) {
        List<BeregningsgrunnlagPeriodeRestDto> perioder = input.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder();
        FordelBeregningsgrunnlagTilfelleInput fordelingInput = FordelBeregningsgrunnlagTilfelleInput.fraBeregningsgrunnlagRestInput(input);

        List<BeregningsgrunnlagPrStatusOgAndelRestDto> beregningsgrunnlagPrStatusOgAndel = perioder.stream()
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

    private static boolean andelHarTilfelleForFordeling(BeregningsgrunnlagPrStatusOgAndelRestDto andel, FordelBeregningsgrunnlagTilfelleInput fordelingInput) {
        BeregningsgrunnlagPeriodeRestDto restPeriode = andel.getBeregningsgrunnlagPeriode();
        BeregningsgrunnlagPeriodeDto domenePeriode = mapPeriode(restPeriode);
        Map<BeregningsgrunnlagPrStatusOgAndelDto, FordelingTilfelle> periodeTilfelleMap = FordelBeregningsgrunnlagTilfelleTjeneste.vurderManuellBehandlingForPeriode(domenePeriode, fordelingInput);
        return periodeTilfelleMap.keySet().stream().anyMatch(key -> Objects.equals(key.getAndelsnr(), andel.getAndelsnr()));
    }

    private static Optional<FordelBeregningsgrunnlagArbeidsforholdDto> mapTilEndretArbeidsforholdDto(BeregningsgrunnlagRestInput input,
                                                                                                     BeregningsgrunnlagPrStatusOgAndelRestDto distinctAndel,
                                                                                                     LocalDate stp, List<BeregningsgrunnlagPeriodeRestDto> perioder) {
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

    private static void settEndretArbeidsforholdForNyttRefusjonskrav(BeregningsgrunnlagPrStatusOgAndelRestDto distinctAndel,
                                                                     FordelBeregningsgrunnlagArbeidsforholdDto endretArbeidsforhold, List<BeregningsgrunnlagPeriodeRestDto> perioder) {
        List<Periode> refusjonsperioder = finnRefusjonsperioderForAndel(distinctAndel, perioder);
        refusjonsperioder.forEach(refusjonsperiode -> {
            GraderingEllerRefusjonDto refusjonDto = new GraderingEllerRefusjonDto(true, false);
            refusjonDto.setFom(refusjonsperiode.getFomOrNull());
            refusjonDto.setTom(TIDENES_ENDE.minusDays(2).isBefore(refusjonsperiode.getTom()) ? null : refusjonsperiode.getTom());
            endretArbeidsforhold.leggTilPeriodeMedGraderingEllerRefusjon(refusjonDto);
        });
    }

    private static List<Periode> finnRefusjonsperioderForAndel(BeregningsgrunnlagPrStatusOgAndelRestDto distinctAndel, List<BeregningsgrunnlagPeriodeRestDto> perioder) {
        List<Periode> refusjonsperioder = new ArrayList<>();
        LocalDate sluttDatoRefusjon = TIDENES_BEGYNNELSE;
        for (int i = 0; i < perioder.size(); i++) {
            BeregningsgrunnlagPeriodeRestDto periode = perioder.get(i);
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

    private static LocalDate finnSluttdato(BeregningsgrunnlagPrStatusOgAndelRestDto distinctAndel, List<BeregningsgrunnlagPeriodeRestDto> perioder, int i, BigDecimal refusjonBeløp) {
        LocalDate sluttDatoRefusjon = TIDENES_ENDE;
        if (i == perioder.size() - 1) {
            return sluttDatoRefusjon;
        }
        for (int k = i + 1; k < perioder.size(); k++) {
            BeregningsgrunnlagPeriodeRestDto nestePeriode = perioder.get(k);
            BigDecimal refusjonINestePeriode = finnRefusjonsbeløpForAndelIPeriode(distinctAndel, nestePeriode).orElse(BigDecimal.ZERO);
            if (refusjonINestePeriode.compareTo(refusjonBeløp) != 0) {
                return perioder.get(k - 1).getBeregningsgrunnlagPeriodeTom();
            }
        }
        return sluttDatoRefusjon;
    }

    private static Optional<BigDecimal> finnRefusjonsbeløpForAndelIPeriode(BeregningsgrunnlagPrStatusOgAndelRestDto distinctAndel,
                                                                           BeregningsgrunnlagPeriodeRestDto periode) {
        Optional<BeregningsgrunnlagPrStatusOgAndelRestDto> matchendeAndel = periode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .filter(andel -> !andel.getLagtTilAvSaksbehandler())
            .filter(andel -> andel.gjelderSammeArbeidsforhold(distinctAndel))
            .filter(andel -> andel.getBgAndelArbeidsforhold()
                .map(BGAndelArbeidsforholdRestDto::getRefusjonskravPrÅr).orElse(BigDecimal.ZERO).compareTo(BigDecimal.ZERO) != 0)
            .findFirst();
        return matchendeAndel
            .flatMap(andel -> andel.getBgAndelArbeidsforhold()
                .map(BGAndelArbeidsforholdRestDto::getRefusjonskravPrÅr)
            );
    }

    private static void settEndretArbeidsforholdForSøktGradering(BeregningsgrunnlagPrStatusOgAndelRestDto distinctAndel,
                                                                 FordelBeregningsgrunnlagArbeidsforholdDto endretArbeidsforhold,
                                                                 AktivitetGradering aktivitetGradering) {
        List<Gradering> graderingerForArbeidsforhold = FordelingGraderingTjeneste.hentGraderingerForAndel(mapAndel(distinctAndel), aktivitetGradering);
        graderingerForArbeidsforhold.forEach(gradering -> {
            GraderingEllerRefusjonDto graderingDto = new GraderingEllerRefusjonDto(false, true);
            Intervall periode = gradering.getPeriode();
            graderingDto.setFom(periode.getFomDato());
            graderingDto.setTom(periode.getTomDato().isBefore(TIDENES_ENDE) ? periode.getTomDato() : null);
            endretArbeidsforhold.leggTilPeriodeMedGraderingEllerRefusjon(graderingDto);
        });
    }

}
