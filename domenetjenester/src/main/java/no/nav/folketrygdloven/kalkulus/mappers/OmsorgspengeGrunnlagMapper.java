package no.nav.folketrygdloven.kalkulus.mappers;

import static no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.refusjonskravgyldighet.HarYrkesaktivitetInnsendtRefusjonForSent.finnFørsteGyldigeDatoMedRefusjon;
import static no.nav.folketrygdloven.kalkulus.mappers.MapFraKalkulator.mapFraDto;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.refusjonskravgyldighet.HarYrkesaktivitetInnsendtRefusjonForSent;
import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.konfig.KonfigTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.RefusjonskravDatoDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.beregning.v1.OmsorgspengerGrunnlag;
import no.nav.folketrygdloven.kalkulus.beregning.v1.UtbetalingsgradArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulus.beregning.v1.UtbetalingsgradPrAktivitetDto;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.felles.v1.KalkulatorInputDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.mapFraEntitet.BehandlingslagerTilKalkulusMapper;

/**
 * Mapper for Omsorgspengegrunnlag
 */
class OmsorgspengeGrunnlagMapper {

    /**
     * Mapper omsorgspengegrunnlag
     *
     * @param input                             Kalkulatorinput
     * @param beregningsgrunnlagGrunnlagEntitet Aktiv beregningsgrunnlagGrunnlagEntitet
     * @param omsorgspengerGrunnlag           Omsorgspengegrunnlag fra input
     * @return Mappet omsorgspengegrunnlag til kalkulus
     */
    public static YtelsespesifiktGrunnlag mapOmsorgspengegrunnlag(KalkulatorInputDto input, Optional<BeregningsgrunnlagGrunnlagEntitet> beregningsgrunnlagGrunnlagEntitet, OmsorgspengerGrunnlag omsorgspengerGrunnlag) {
        no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.OmsorgspengerGrunnlag kalkulatorGrunnlag = new no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.OmsorgspengerGrunnlag(
                UtbetalingsgradMapper.mapUtbetalingsgrad(omsorgspengerGrunnlag.getUtbetalingsgradPrAktivitet()));
        kalkulatorGrunnlag.setGrunnbeløpMilitærHarKravPå(KonfigTjeneste.forYtelse(FagsakYtelseType.OMSORGSPENGER).getAntallGMilitærHarKravPå().intValue());
        kalkulatorGrunnlag.setPeriodeUtenGyldigRefusjonPrArbeidsgiver(finnPeriodeUtenGyldigRefusjonPrArbeidsgiver(input, beregningsgrunnlagGrunnlagEntitet, omsorgspengerGrunnlag));
        return kalkulatorGrunnlag;
    }

    private static Map<Arbeidsgiver, Intervall> finnPeriodeUtenGyldigRefusjonPrArbeidsgiver(KalkulatorInputDto input, Optional<BeregningsgrunnlagGrunnlagEntitet> beregningsgrunnlagGrunnlagEntitet, OmsorgspengerGrunnlag omsorgspengerGrunnlag) {
        if (beregningsgrunnlagGrunnlagEntitet.isPresent() && beregningsgrunnlagGrunnlagEntitet.get().getBeregningsgrunnlag().isPresent()) {
            var gjeldendeAktiviteter = BehandlingslagerTilKalkulusMapper.mapAktiviteter(beregningsgrunnlagGrunnlagEntitet
                    .get().getGjeldendeAktiviteter());
            var arbeidsforholdMedUtbetalingsgrad = omsorgspengerGrunnlag.getUtbetalingsgradPrAktivitet().stream()
                    .map(UtbetalingsgradPrAktivitetDto::getUtbetalingsgradArbeidsforholdDto)
                    .collect(Collectors.toList());
            var datoer = mapFraDto(input.getRefusjonskravDatoer(),
                    input.getIayGrunnlag().getInntektsmeldingDto() == null ? Collections.emptyList() : input.getIayGrunnlag().getInntektsmeldingDto().getInntektsmeldinger(),
                    input.getSkjæringstidspunkt());
            return arbeidsforholdMedUtbetalingsgrad.stream()
                    .filter(a -> a.getArbeidsgiver() != null)
                    .filter(a -> mapUgyldigPeriodeHvisFinnes(beregningsgrunnlagGrunnlagEntitet, gjeldendeAktiviteter, datoer, a).isPresent())
                    .collect(Collectors.toMap(
                            a -> MapFraKalkulator.mapArbeidsgiver(a.getArbeidsgiver()),
                            a -> mapUgyldigPeriodeHvisFinnes(beregningsgrunnlagGrunnlagEntitet, gjeldendeAktiviteter, datoer, a).get()));
        }
        return new HashMap<>();
    }

    private static Optional<Intervall> mapUgyldigPeriodeHvisFinnes(Optional<BeregningsgrunnlagGrunnlagEntitet> beregningsgrunnlagGrunnlagEntitet, BeregningAktivitetAggregatDto gjeldendeAktiviteter, List<RefusjonskravDatoDto> datoer, UtbetalingsgradArbeidsforholdDto a) {
        var dtoForArbeidsgiver = datoer.stream().filter(d -> d.getArbeidsgiver().getIdentifikator().equals(a.getArbeidsgiver().getIdent())).findFirst();
        return dtoForArbeidsgiver.flatMap(dto -> finnIntervallMedUgyligRefusjon(beregningsgrunnlagGrunnlagEntitet, gjeldendeAktiviteter, a, dto));
    }

    private static Optional<? extends Intervall> finnIntervallMedUgyligRefusjon(Optional<BeregningsgrunnlagGrunnlagEntitet> beregningsgrunnlagGrunnlagEntitet,
                                                                                BeregningAktivitetAggregatDto gjeldendeAktiviteter,
                                                                                UtbetalingsgradArbeidsforholdDto arbeidsforhold, RefusjonskravDatoDto dto) {
        LocalDate førsteDatoMedGyldigRefusjon = finnFørsteGyldigeDatoMedRefusjon(dto, FagsakYtelseType.OMSORGSPENGER);
        LocalDate førsteDatoMedRefusjon = HarYrkesaktivitetInnsendtRefusjonForSent.finnFørsteDagMedSøktRefusjon(
                dto,
                gjeldendeAktiviteter,
                beregningsgrunnlagGrunnlagEntitet.get().getBeregningsgrunnlag().get().getSkjæringstidspunkt(),
                arbeidsforhold.getInternArbeidsforholdRef() == null ? InternArbeidsforholdRefDto.nullRef() : InternArbeidsforholdRefDto.ref(arbeidsforhold.getInternArbeidsforholdRef().getAbakusReferanse())
        );
        if (førsteDatoMedGyldigRefusjon.isAfter(førsteDatoMedRefusjon)) {
            return Optional.of(Intervall.fraOgMedTilOgMed(førsteDatoMedRefusjon, førsteDatoMedGyldigRefusjon.minusDays(1)));
        }
        return Optional.empty();
    }

}
