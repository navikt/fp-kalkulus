package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.ytelse;

import static no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.MapRefusjonskravFraVLTilRegel.finnGradertRefusjonskravPåSkjæringstidspunktet;

import java.math.BigDecimal;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.YtelsesSpesifiktGrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.omp.OmsorgspengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.YtelsesspesifikkRegelMapper;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.uttak.UttakArbeidType;


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
            return new OmsorgspengerGrunnlag(gradertRefusjonVedSkjæringstidspunkt, harSøktFLEllerSN);
        }

        throw new IllegalStateException("Forventer OmsorgspengerGrunnlag for OMP");
    }

    private boolean harUtbetaling(UtbetalingsgradPrAktivitetDto aktivitet) {
        return aktivitet.getPeriodeMedUtbetalingsgrad().stream()
                .anyMatch(p -> p.getUtbetalingsgrad().compareTo(BigDecimal.ZERO) > 0);
    }

    private boolean erFrilansEllerNæring(UtbetalingsgradPrAktivitetDto aktivitet) {
        return aktivitet.getUtbetalingsgradArbeidsforhold().getUttakArbeidType().equals(UttakArbeidType.FRILANS)
                || aktivitet.getUtbetalingsgradArbeidsforhold().getUttakArbeidType().equals(UttakArbeidType.SELVSTENDIG_NÆRINGSDRIVENDE);
    }

}
