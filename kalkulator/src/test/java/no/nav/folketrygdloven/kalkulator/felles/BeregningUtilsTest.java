package no.nav.folketrygdloven.kalkulator.felles;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseAnvistDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.ytelse.RelatertYtelseType;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.RelatertYtelseTilstand;

public class BeregningUtilsTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.of(2019,1,1);
    private static final RelatertYtelseType AAP = RelatertYtelseType.ARBEIDSAVKLARINGSPENGER;
    @Test
    public void skal_finne_ytelse_med_korrekt_ytelsetype() {
        YtelseDto aapYtelse = lagYtelse(AAP, SKJÆRINGSTIDSPUNKT.minusMonths(2), SKJÆRINGSTIDSPUNKT.minusMonths(1)).build();
        YtelseDto dpYtelse = lagYtelse(RelatertYtelseType.DAGPENGER, SKJÆRINGSTIDSPUNKT.minusMonths(1), SKJÆRINGSTIDSPUNKT.minusDays(1)).build();

        YtelseFilterDto filter = new YtelseFilterDto(Arrays.asList(aapYtelse, dpYtelse));

        Optional<YtelseDto> ytelse = BeregningUtils.sisteVedtakFørStpForType(filter, SKJÆRINGSTIDSPUNKT, Set.of(AAP));

        assertThat(ytelse).isPresent();
        assertThat(ytelse.get()).isEqualTo(aapYtelse);
    }

    @Test
    public void skal_finne_ytelse_med_vedtak_nærmest_skjæringstidspunkt() {
        YtelseDto aapYtelseGammel = lagYtelse(AAP, SKJÆRINGSTIDSPUNKT.minusMonths(2), SKJÆRINGSTIDSPUNKT.minusMonths(1)).build();
        YtelseDto aapYtelseNy = lagYtelse(AAP, SKJÆRINGSTIDSPUNKT.minusMonths(1), SKJÆRINGSTIDSPUNKT.minusDays(1)).build();

        YtelseFilterDto filter = new YtelseFilterDto(Arrays.asList(aapYtelseNy, aapYtelseGammel));

        Optional<YtelseDto> ytelse = BeregningUtils.sisteVedtakFørStpForType(filter, SKJÆRINGSTIDSPUNKT, Set.of(AAP));

        assertThat(ytelse).isPresent();
        assertThat(ytelse.get()).isEqualTo(aapYtelseNy);
    }

    @Test
    public void skal_ikke_ta_med_vedtak_med_fom_etter_skjæringstidspunkt() {
        YtelseDto aapYtelseGammel = lagYtelse(AAP, SKJÆRINGSTIDSPUNKT.minusMonths(2), SKJÆRINGSTIDSPUNKT.minusMonths(1)).build();
        YtelseDto aapYtelseNy = lagYtelse(AAP, SKJÆRINGSTIDSPUNKT.minusMonths(1), SKJÆRINGSTIDSPUNKT.minusDays(15)).build();
        YtelseDto aapYtelseEtterStp = lagYtelse(AAP, SKJÆRINGSTIDSPUNKT.plusDays(1), SKJÆRINGSTIDSPUNKT.plusMonths(1)).build();

        YtelseFilterDto filter = new YtelseFilterDto(Arrays.asList(aapYtelseNy, aapYtelseGammel, aapYtelseEtterStp));

        Optional<YtelseDto> ytelse = BeregningUtils.sisteVedtakFørStpForType(filter, SKJÆRINGSTIDSPUNKT, Set.of(AAP));

        assertThat(ytelse).isPresent();
        assertThat(ytelse.get()).isEqualTo(aapYtelseNy);
    }

    @Test
    public void skal_finne_korrekt_meldekort_når_det_tilhører_nyeste_vedtak() {
        YtelseDtoBuilder aapGammelBuilder = lagYtelse(AAP, SKJÆRINGSTIDSPUNKT.minusMonths(2), SKJÆRINGSTIDSPUNKT.minusMonths(1));
        YtelseDtoBuilder aapYtelseNyBuilder = lagYtelse(AAP, SKJÆRINGSTIDSPUNKT.minusMonths(1), SKJÆRINGSTIDSPUNKT.minusDays(15));

        YtelseAnvistDto nyttMeldekort = lagMeldekort(BigDecimal.valueOf(100), SKJÆRINGSTIDSPUNKT.minusDays(45), SKJÆRINGSTIDSPUNKT.minusDays(31));
        YtelseAnvistDto gammeltMeldekort = lagMeldekort(BigDecimal.valueOf(100), SKJÆRINGSTIDSPUNKT.minusDays(60), SKJÆRINGSTIDSPUNKT.minusDays(46));

        YtelseDto gammelYtelse = aapGammelBuilder.leggTilYtelseAnvist(gammeltMeldekort).build();
        YtelseDto nyYtelse = aapYtelseNyBuilder.leggTilYtelseAnvist(nyttMeldekort).build();

        YtelseFilterDto filter = new YtelseFilterDto(Arrays.asList(gammelYtelse, nyYtelse));
        Optional<YtelseAnvistDto> ytelseAnvist = BeregningUtils.sisteHeleMeldekortFørStp(filter, nyYtelse, SKJÆRINGSTIDSPUNKT, Set.of(AAP));

        assertThat(ytelseAnvist).isPresent();
        assertThat(ytelseAnvist.get()).isEqualTo(nyttMeldekort);
    }

    @Test
    public void skal_finne_korrekt_meldekort_når_det_tilhører_eldste_vedtak() {
        YtelseDtoBuilder aapGammelBuilder = lagYtelse(AAP, SKJÆRINGSTIDSPUNKT.minusMonths(2), SKJÆRINGSTIDSPUNKT.minusMonths(1));
        YtelseDtoBuilder aapYtelseNyBuilder = lagYtelse(AAP, SKJÆRINGSTIDSPUNKT.minusMonths(1), SKJÆRINGSTIDSPUNKT.minusDays(15));

        YtelseAnvistDto nyttMeldekort = lagMeldekort(BigDecimal.valueOf(100), SKJÆRINGSTIDSPUNKT.minusDays(45), SKJÆRINGSTIDSPUNKT.minusDays(31));
        YtelseAnvistDto gammeltMeldekort = lagMeldekort(BigDecimal.valueOf(100), SKJÆRINGSTIDSPUNKT.minusDays(60), SKJÆRINGSTIDSPUNKT.minusDays(46));

        YtelseDto gammelYtelse = aapGammelBuilder.leggTilYtelseAnvist(nyttMeldekort).build();
        YtelseDto nyYtelse = aapYtelseNyBuilder.leggTilYtelseAnvist(gammeltMeldekort).build();

        YtelseFilterDto filter = new YtelseFilterDto(Arrays.asList(gammelYtelse, nyYtelse));
        Optional<YtelseAnvistDto> ytelseAnvist = BeregningUtils.sisteHeleMeldekortFørStp(filter, nyYtelse, SKJÆRINGSTIDSPUNKT, Set.of(AAP));

        assertThat(ytelseAnvist).isPresent();
        assertThat(ytelseAnvist.get()).isEqualTo(nyttMeldekort);
    }

    @Test
    public void skal_finne_meldekort_fra_nyeste_vedtak_når_to_vedtak_har_meldekort_med_samme_periode() {
        YtelseDtoBuilder aapGammelBuilder = lagYtelse(AAP, SKJÆRINGSTIDSPUNKT.minusMonths(2), SKJÆRINGSTIDSPUNKT.minusMonths(1));
        YtelseDtoBuilder aapYtelseNyBuilder = lagYtelse(AAP, SKJÆRINGSTIDSPUNKT.minusMonths(1), SKJÆRINGSTIDSPUNKT.minusDays(15));

        YtelseAnvistDto meldekortHundre = lagMeldekort(BigDecimal.valueOf(100), SKJÆRINGSTIDSPUNKT.minusDays(45), SKJÆRINGSTIDSPUNKT.minusDays(31));
        YtelseAnvistDto meldekortFemti = lagMeldekort(BigDecimal.valueOf(50), SKJÆRINGSTIDSPUNKT.minusDays(45), SKJÆRINGSTIDSPUNKT.minusDays(31));

        YtelseDto gammelYtelse = aapGammelBuilder.leggTilYtelseAnvist(meldekortHundre).build();
        YtelseDto nyYtelse = aapYtelseNyBuilder.leggTilYtelseAnvist(meldekortFemti).build();

        YtelseFilterDto filter = new YtelseFilterDto(Arrays.asList(gammelYtelse, nyYtelse));
        Optional<YtelseAnvistDto> ytelseAnvist = BeregningUtils.sisteHeleMeldekortFørStp(filter, nyYtelse, SKJÆRINGSTIDSPUNKT, Set.of(AAP));

        assertThat(ytelseAnvist).isPresent();
        assertThat(ytelseAnvist.get()).isEqualTo(meldekortFemti);
    }

    @Test
    public void skal_ikke_ta_med_meldekort_fra_vedtak_etter_stp() {
        YtelseDtoBuilder aapGammelBuilder = lagYtelse(AAP, SKJÆRINGSTIDSPUNKT.minusMonths(2), SKJÆRINGSTIDSPUNKT.minusMonths(1));
        YtelseDtoBuilder aapYtelseNyBuilder = lagYtelse(AAP, SKJÆRINGSTIDSPUNKT.minusMonths(1), SKJÆRINGSTIDSPUNKT.minusDays(15));
        YtelseDtoBuilder aapYtelseEtterStpBuilder = lagYtelse(AAP, SKJÆRINGSTIDSPUNKT.minusMonths(1), SKJÆRINGSTIDSPUNKT.minusDays(15));

        YtelseAnvistDto meldekortGammel = lagMeldekort(BigDecimal.valueOf(50), SKJÆRINGSTIDSPUNKT.minusDays(45), SKJÆRINGSTIDSPUNKT.minusDays(31));
        YtelseAnvistDto meldekortNytt = lagMeldekort(BigDecimal.valueOf(50), SKJÆRINGSTIDSPUNKT.minusDays(30), SKJÆRINGSTIDSPUNKT.minusDays(16));
        YtelseAnvistDto meldekortNyest = lagMeldekort(BigDecimal.valueOf(50), SKJÆRINGSTIDSPUNKT.minusDays(2), SKJÆRINGSTIDSPUNKT.plusDays(12));

        YtelseDto gammelYtelse = aapGammelBuilder.leggTilYtelseAnvist(meldekortGammel).build();
        YtelseDto nyYtelse = aapYtelseNyBuilder.leggTilYtelseAnvist(meldekortNytt).build();
        YtelseDto ytelseEtterStp = aapYtelseEtterStpBuilder.leggTilYtelseAnvist(meldekortNyest).build();

        YtelseFilterDto filter = new YtelseFilterDto(Arrays.asList(gammelYtelse, nyYtelse, ytelseEtterStp));
        Optional<YtelseAnvistDto> ytelseAnvist = BeregningUtils.sisteHeleMeldekortFørStp(filter, nyYtelse, SKJÆRINGSTIDSPUNKT, Set.of(AAP));

        assertThat(ytelseAnvist).isPresent();
        assertThat(ytelseAnvist.get()).isEqualTo(meldekortNytt);
    }


    private YtelseAnvistDto lagMeldekort(BigDecimal utbetalingsgrad, LocalDate fom, LocalDate tom) {
        return YtelseDtoBuilder.oppdatere(Optional.empty()).getAnvistBuilder()
            .medAnvistPeriode(Intervall.fraOgMedTilOgMed(fom, tom))
            .medUtbetalingsgradProsent(utbetalingsgrad).build();
    }


    private YtelseDtoBuilder lagYtelse(RelatertYtelseType ytelsetype, LocalDate fom, LocalDate tom) {
        return YtelseDtoBuilder.oppdatere(Optional.empty())
                .medPeriode(Intervall.fraOgMedTilOgMed(fom, tom))
                .medYtelseType(ytelsetype)
                .medStatus(RelatertYtelseTilstand.AVSLUTTET);
    }


}
