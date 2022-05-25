package no.nav.folketrygdloven.kalkulator.ytelse.k9;

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
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;
import no.nav.folketrygdloven.kalkulus.kodeverk.PermisjonsbeskrivelseType;

class MapRefusjonPerioderFraVLTilRegelK9Test {

    private final MapRefusjonPerioderFraVLTilRegelPleiepenger mapper = new MapRefusjonPerioderFraVLTilRegelPleiepenger(null);

    @Test
    void skal_finne_gyldige_refusjonsperioder_uten_permisjon() {
        var stp = LocalDate.now();
        var arbeidsgiver = Arbeidsgiver.virksomhet("123456789");
        var utbetalingsperiode = Intervall.fraOgMedTilOgMed(stp, stp.plusDays(15));
        var ytelsespesifiktGrunnlag = lagUtbetalingsgrunnlag(arbeidsgiver, utbetalingsperiode);
        var inntektsmelding = lagInntektsmelding(stp, arbeidsgiver);
        var aktivitetsAvtaleDtoBuilder = AktivitetsAvtaleDtoBuilder.ny().medPeriode(utbetalingsperiode).medErAnsettelsesPeriode(true);
        var aktivitetsavtaler = List.of(aktivitetsAvtaleDtoBuilder.build());
        var relaterteYrkesaktiviteter = Set.of(lagYrkesaktivitet(arbeidsgiver, aktivitetsAvtaleDtoBuilder, null));
        var permisjonFilter = new PermisjonFilter(Collections.emptyList(), relaterteYrkesaktiviteter, stp);

        var gyldigeRefusjonPerioder = mapper.finnGyldigeRefusjonPerioder(stp,
                ytelsespesifiktGrunnlag,
                inntektsmelding,
                aktivitetsavtaler,
                relaterteYrkesaktiviteter, permisjonFilter);

        assertThat(gyldigeRefusjonPerioder.size()).isEqualTo(1);
        assertThat(gyldigeRefusjonPerioder.get(0)).isEqualTo(utbetalingsperiode);
    }

    private PleiepengerSyktBarnGrunnlag lagUtbetalingsgrunnlag(Arbeidsgiver arbeidsgiver, Intervall utbetalingsperiode) {
        return new PleiepengerSyktBarnGrunnlag(List.of(new UtbetalingsgradPrAktivitetDto(
                new AktivitetDto(arbeidsgiver, InternArbeidsforholdRefDto.nullRef(), UttakArbeidType.ORDINÆRT_ARBEID),
                List.of(new PeriodeMedUtbetalingsgradDto(utbetalingsperiode, BigDecimal.ONE)))));
    }

    @Test
    void skal_finne_gyldige_refusjonsperioder_med_permisjon() {
        var stp = LocalDate.now();
        var arbeidsgiver = Arbeidsgiver.virksomhet("123456789");
        var utbetalingsperiode = Intervall.fraOgMedTilOgMed(stp, stp.plusDays(15));
        var ytelsespesifiktGrunnlag = lagUtbetalingsgrunnlag(arbeidsgiver, utbetalingsperiode);
        var inntektsmelding = lagInntektsmelding(stp, arbeidsgiver);
        var aktivitetsAvtaleDtoBuilder = AktivitetsAvtaleDtoBuilder.ny().medPeriode(utbetalingsperiode).medErAnsettelsesPeriode(true);
        var aktivitetsavtaler = List.of(aktivitetsAvtaleDtoBuilder.build());
        var permisjonsperiode = Intervall.fraOgMedTilOgMed(stp.plusDays(1), stp.plusDays(15));
        var relaterteYrkesaktiviteter = Set.of(lagYrkesaktivitet(arbeidsgiver, aktivitetsAvtaleDtoBuilder, permisjonsperiode));
        var permisjonFilter = new PermisjonFilter(Collections.emptyList(), relaterteYrkesaktiviteter, stp);

        var gyldigeRefusjonPerioder = mapper.finnGyldigeRefusjonPerioder(stp,
                ytelsespesifiktGrunnlag,
                inntektsmelding,
                aktivitetsavtaler,
                relaterteYrkesaktiviteter, permisjonFilter);

        assertThat(gyldigeRefusjonPerioder.size()).isEqualTo(1);
        assertThat(gyldigeRefusjonPerioder.get(0)).isEqualTo(Intervall.fraOgMedTilOgMed(stp, stp));
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
