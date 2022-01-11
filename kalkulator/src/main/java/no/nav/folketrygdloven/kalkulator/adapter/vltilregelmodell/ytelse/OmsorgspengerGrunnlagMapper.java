package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.ytelse;

import static no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.MapRefusjonskravFraVLTilRegel.finnGradertRefusjonskravPåSkjæringstidspunktet;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.YtelsesSpesifiktGrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.omp.OmsorgspengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.YtelsesspesifikkRegelMapper;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.uttak.UttakArbeidType;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;


@ApplicationScoped
@FagsakYtelseTypeRef("OMP")
public class OmsorgspengerGrunnlagMapper implements YtelsesspesifikkRegelMapper {

    @Override
    public YtelsesSpesifiktGrunnlag map(BeregningsgrunnlagDto beregningsgrunnlagDto, BeregningsgrunnlagInput input) {
        YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag = input.getYtelsespesifiktGrunnlag();
        if (ytelsespesifiktGrunnlag instanceof no.nav.folketrygdloven.kalkulator.input.OmsorgspengerGrunnlag) {
            no.nav.folketrygdloven.kalkulator.input.OmsorgspengerGrunnlag omsorspengegrunnlag = (no.nav.folketrygdloven.kalkulator.input.OmsorgspengerGrunnlag) ytelsespesifiktGrunnlag;
            List<UtbetalingsgradPrAktivitetDto> utbetalingsgradPrAktivitet = omsorspengegrunnlag.getUtbetalingsgradPrAktivitet();
            boolean harSøktFLEllerSN = utbetalingsgradPrAktivitet.stream()
                    .filter(this::erFrilansEllerNæring)
                    .anyMatch(this::harUtbetaling);
            BigDecimal gradertRefusjonVedSkjæringstidspunkt = finnGradertRefusjonskravPåSkjæringstidspunktet(input.getInntektsmeldinger(), beregningsgrunnlagDto.getSkjæringstidspunkt(), input.getYtelsespesifiktGrunnlag());
            return new OmsorgspengerGrunnlag(gradertRefusjonVedSkjæringstidspunkt, harSøktFLEllerSN, finnesArbeidsandelIkkeSøktOm(utbetalingsgradPrAktivitet, beregningsgrunnlagDto), harRefusjonskrav(input.getInntektsmeldinger()));
        }

        throw new IllegalStateException("Forventer OmsorgspengerGrunnlag for OMP");
    }

    private boolean harRefusjonskrav(Collection<InntektsmeldingDto> inntektsmeldinger) {
        return inntektsmeldinger.stream().anyMatch(im -> im.getRefusjonBeløpPerMnd() != null && !im.getRefusjonBeløpPerMnd().erNullEllerNulltall());
    }

    private boolean harUtbetaling(UtbetalingsgradPrAktivitetDto aktivitet) {
        return aktivitet.getPeriodeMedUtbetalingsgrad().stream()
                .anyMatch(p -> p.getUtbetalingsgrad().compareTo(BigDecimal.ZERO) > 0);
    }

    private boolean erFrilansEllerNæring(UtbetalingsgradPrAktivitetDto aktivitet) {
        return aktivitet.getUtbetalingsgradArbeidsforhold().getUttakArbeidType().equals(UttakArbeidType.FRILANS)
                || aktivitet.getUtbetalingsgradArbeidsforhold().getUttakArbeidType().equals(UttakArbeidType.SELVSTENDIG_NÆRINGSDRIVENDE);
    }

    private boolean finnesArbeidsandelIkkeSøktOm(List<UtbetalingsgradPrAktivitetDto> utbetalingsgrader, BeregningsgrunnlagDto beregningsgrunnlagDto) {
        List<BeregningsgrunnlagPrStatusOgAndelDto> andeler = beregningsgrunnlagDto.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList();
        return andeler.stream()
                .filter(andel -> andel.getAktivitetStatus().equals(AktivitetStatus.ARBEIDSTAKER))
                .anyMatch(andel -> !erSøktOm(utbetalingsgrader, andel));
    }

    private boolean erSøktOm(List<UtbetalingsgradPrAktivitetDto> utbetalingsgradPrAktivitet, BeregningsgrunnlagPrStatusOgAndelDto andel) {
        return utbetalingsgradPrAktivitet.stream()
                .filter(utb -> utb.getUtbetalingsgradArbeidsforhold().getUttakArbeidType().equals(UttakArbeidType.ORDINÆRT_ARBEID))
                .filter(utb -> utb.getUtbetalingsgradArbeidsforhold().getArbeidsgiver().equals(andel.getArbeidsgiver()))
                .anyMatch(utb -> utb.getUtbetalingsgradArbeidsforhold().getInternArbeidsforholdRef().gjelderFor(andel.getArbeidsforholdRef().orElse(InternArbeidsforholdRefDto.nullRef())));
    }

}
