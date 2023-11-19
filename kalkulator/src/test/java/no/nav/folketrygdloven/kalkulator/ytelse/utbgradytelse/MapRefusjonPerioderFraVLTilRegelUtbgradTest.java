package no.nav.folketrygdloven.kalkulator.ytelse.utbgradytelse;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.input.PleiepengerSyktBarnGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.permisjon.PermisjonDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.permisjon.PermisjonFilter;
import no.nav.folketrygdloven.kalkulator.modell.svp.AktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.PeriodeMedUtbetalingsgradDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.uttak.UttakArbeidType;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulator.ytelse.k9.MapRefusjonPerioderFraVLTilRegelPleiepenger;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;
import no.nav.folketrygdloven.kalkulus.kodeverk.PermisjonsbeskrivelseType;

class MapRefusjonPerioderFraVLTilRegelUtbgradTest {


    private final MapRefusjonPerioderFraVLTilRegelUtbgrad mapper = new MapRefusjonPerioderFraVLTilRegelPleiepenger();

    @Test
    void skal_finne_første_dag_etter_permisjon() {
        var stp = LocalDate.now();
        var arbeidsgiver = Arbeidsgiver.virksomhet("123456789");
        var utbetalingsperiode = Intervall.fraOgMedTilOgMed(stp, stp.plusDays(15));
        var ytelsespesifiktGrunnlag = lagUtbetalingsgrunnlag(arbeidsgiver, utbetalingsperiode);
        var inntektsmelding = lagInntektsmelding(stp, arbeidsgiver);
        var aktivitetsAvtaleDtoBuilder = AktivitetsAvtaleDtoBuilder.ny().medPeriode(Intervall.fraOgMedTilOgMed(stp.minusDays(1), stp.plusDays(15))).medErAnsettelsesPeriode(true);
        var permisjonsperiode = Intervall.fraOgMedTilOgMed(stp.minusDays(1), stp.plusDays(15));
        var relaterteYrkesaktiviteter = Set.of(lagYrkesaktivitet(arbeidsgiver, aktivitetsAvtaleDtoBuilder, permisjonsperiode));
        var permisjonFilter = new PermisjonFilter(Collections.emptyList(), relaterteYrkesaktiviteter, stp);

        var startdato = mapper.utledStartdatoEtterPermisjon(stp, inntektsmelding, relaterteYrkesaktiviteter, permisjonFilter, ytelsespesifiktGrunnlag);

        assertThat(startdato.isPresent()).isTrue();
        assertThat(startdato.get()).isEqualTo(permisjonsperiode.getTomDato().plusDays(1));
    }

    @Test
    void skal_ikke_bruke_permisjon_som_starter_etter_stp() {
        var stp = LocalDate.now();
        var arbeidsgiver = Arbeidsgiver.virksomhet("123456789");
        var utbetalingsperiode = Intervall.fraOgMedTilOgMed(stp, stp.plusDays(15));
        var ytelsespesifiktGrunnlag = lagUtbetalingsgrunnlag(arbeidsgiver, utbetalingsperiode);
        var inntektsmelding = lagInntektsmelding(stp, arbeidsgiver);
        var aktivitetsAvtaleDtoBuilder = AktivitetsAvtaleDtoBuilder.ny().medPeriode(utbetalingsperiode).medErAnsettelsesPeriode(true);
        var permisjonsperiode = Intervall.fraOgMedTilOgMed(stp.plusDays(1), stp.plusDays(15));
        var relaterteYrkesaktiviteter = Set.of(lagYrkesaktivitet(arbeidsgiver, aktivitetsAvtaleDtoBuilder, permisjonsperiode));
        var permisjonFilter = new PermisjonFilter(Collections.emptyList(), relaterteYrkesaktiviteter, stp);

        var startdato = mapper.utledStartdatoEtterPermisjon(stp, inntektsmelding, relaterteYrkesaktiviteter, permisjonFilter, ytelsespesifiktGrunnlag);

        assertThat(startdato.isPresent()).isTrue();
        assertThat(startdato.get()).isEqualTo(stp);
    }

    @Test
    void skal_returnerer_første_dag_med_søkt_utbetaling() {
        var stp = LocalDate.now();
        var arbeidsgiver = Arbeidsgiver.virksomhet("123456789");
        var utbetalingsperiode = Intervall.fraOgMedTilOgMed(stp.plusDays(20), stp.plusDays(25));
        var ytelsespesifiktGrunnlag = lagUtbetalingsgrunnlag(arbeidsgiver, utbetalingsperiode);
        var inntektsmelding = lagInntektsmelding(stp, arbeidsgiver);
        var ansettelsesperiode = Intervall.fraOgMedTilOgMed(stp.minusYears(10), stp.plusYears(15));
        var aktivitetsAvtaleDtoBuilder = AktivitetsAvtaleDtoBuilder.ny().medPeriode(ansettelsesperiode).medErAnsettelsesPeriode(true);
        var permisjonsperiode = Intervall.fraOgMedTilOgMed(stp.minusDays(1), stp.plusDays(15));
        var relaterteYrkesaktiviteter = Set.of(lagYrkesaktivitet(arbeidsgiver, aktivitetsAvtaleDtoBuilder, permisjonsperiode));
        var permisjonFilter = new PermisjonFilter(Collections.emptyList(), relaterteYrkesaktiviteter, stp);

        var startdato = mapper.utledStartdatoEtterPermisjon(stp, inntektsmelding, relaterteYrkesaktiviteter, permisjonFilter, ytelsespesifiktGrunnlag);

        assertThat(startdato.isPresent()).isTrue();
        assertThat(startdato.get()).isEqualTo(utbetalingsperiode.getFomDato());
    }

    @Test
    void skal_ikke_returnerer_første_dag_med_søkt_utbetaling_ved_inaktiv_og_ingen_arbeid() {
        var stp = LocalDate.now();
        var arbeidsgiver = Arbeidsgiver.virksomhet("123456789");
        var utbetalingsperiode = Intervall.fraOgMedTilOgMed(stp.plusDays(20), stp.plusDays(25));
        var ytelsespesifiktGrunnlag = lagUtbetalingsgrunnlagInaktiv(utbetalingsperiode);
        var inntektsmelding = lagInntektsmelding(stp, arbeidsgiver);
        var ansettelsesperiode = Intervall.fraOgMedTilOgMed(stp.minusYears(10), stp.plusYears(15));
        var aktivitetsAvtaleDtoBuilder = AktivitetsAvtaleDtoBuilder.ny().medPeriode(ansettelsesperiode).medErAnsettelsesPeriode(true);
        var permisjonsperiode = Intervall.fraOgMedTilOgMed(stp.minusDays(1), stp.plusDays(15));
        var relaterteYrkesaktiviteter = Set.of(lagYrkesaktivitet(arbeidsgiver, aktivitetsAvtaleDtoBuilder, permisjonsperiode));
        var permisjonFilter = new PermisjonFilter(Collections.emptyList(), relaterteYrkesaktiviteter, stp);

        var startdato = mapper.utledStartdatoEtterPermisjon(stp, inntektsmelding, relaterteYrkesaktiviteter, permisjonFilter, ytelsespesifiktGrunnlag);

        assertThat(startdato.isPresent()).isFalse();
    }

    private PleiepengerSyktBarnGrunnlag lagUtbetalingsgrunnlag(Arbeidsgiver arbeidsgiver, Intervall utbetalingsperiode) {
        return new PleiepengerSyktBarnGrunnlag(List.of(new UtbetalingsgradPrAktivitetDto(
                new AktivitetDto(arbeidsgiver, InternArbeidsforholdRefDto.nullRef(), UttakArbeidType.ORDINÆRT_ARBEID),
                List.of(new PeriodeMedUtbetalingsgradDto(utbetalingsperiode, BigDecimal.ONE)))));
    }

    private PleiepengerSyktBarnGrunnlag lagUtbetalingsgrunnlagInaktiv(Intervall utbetalingsperiode) {
        return new PleiepengerSyktBarnGrunnlag(List.of(new UtbetalingsgradPrAktivitetDto(
                new AktivitetDto(null, InternArbeidsforholdRefDto.nullRef(), UttakArbeidType.MIDL_INAKTIV),
                List.of(new PeriodeMedUtbetalingsgradDto(utbetalingsperiode, BigDecimal.ONE)))));
    }


    private InntektsmeldingDto lagInntektsmelding(LocalDate stp, Arbeidsgiver arbeidsgiver) {
        return InntektsmeldingDtoBuilder.builder()
                .medArbeidsgiver(arbeidsgiver)
                .medArbeidsforholdId(InternArbeidsforholdRefDto.nullRef())
                .medRefusjon(BigDecimal.TEN, stp.plusDays(16))
                .medBeløp(BigDecimal.TEN)
                .build();
    }

    private YrkesaktivitetDto lagYrkesaktivitet(Arbeidsgiver arbeidsgiver, AktivitetsAvtaleDtoBuilder aktivitetsavtale, Intervall permisjonsperiode) {
        var builder = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
                .medArbeidsgiver(arbeidsgiver)
                .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
                .medArbeidsforholdId(InternArbeidsforholdRefDto.nullRef());
        if (permisjonsperiode != null) {
            builder.leggTilPermisjon(PermisjonDtoBuilder.ny().medPeriode(permisjonsperiode)
                    .medProsentsats(BigDecimal.valueOf(100))
                    .medPermisjonsbeskrivelseType(PermisjonsbeskrivelseType.VELFERDSPERMISJON));
        }
        builder.leggTilAktivitetsAvtale(aktivitetsavtale);
        return builder
                .build();
    }
}
