package no.nav.folketrygdloven.kalkulator.steg.besteberegning;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Periodeinntekt;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseAnvistDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseAnvistDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseFilterDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;

class MapArenaVedtakTilBesteberegningRegelmodellTest {
    private static final LocalDate STP = LocalDate.of(2021,5,1);

    @Test
    public void skal_mappe_dagpenger_meldekort_innenfor_vedtak() {
        List<YtelseDto> vedtak = Arrays.asList(lagVedtak(STP, stpPluss(30), FagsakYtelseType.DAGPENGER, lagMeldekort(STP, stpPluss(14), 500)));

        List<Periodeinntekt> inntekter = kjørMapping(vedtak);

        assertThat(inntekter).hasSize(1);
        assertInntekt(inntekter, STP, stpPluss(14), 500);
    }

    @Test
    public void skal_mappe_dagpenger_meldekort_avgrenses_av_vedtak() {
        List<YtelseDto> vedtak = Arrays.asList(lagVedtak(STP, stpPluss(30), FagsakYtelseType.DAGPENGER, lagMeldekort(stpPluss(20), stpPluss(34), 500)));

        List<Periodeinntekt> inntekter = kjørMapping(vedtak);

        assertThat(inntekter).hasSize(1);
        assertInntekt(inntekter, stpPluss(20), stpPluss(30), 500);
    }

    @Test
    public void skal_mappe_dagpenger_flere_ikke_overlappende_vedtak() {
        List<YtelseDto> vedtak = Arrays.asList(lagVedtak(STP, stpPluss(30), FagsakYtelseType.DAGPENGER,
                lagMeldekort(STP, stpPluss(14), 500)),
        lagVedtak(stpPluss(31), stpPluss(60), FagsakYtelseType.DAGPENGER,
                lagMeldekort(stpPluss(31), stpPluss(45), 600)));

        List<Periodeinntekt> inntekter = kjørMapping(vedtak);

        assertThat(inntekter).hasSize(2);
        assertInntekt(inntekter, STP, stpPluss(14), 500);
        assertInntekt(inntekter, stpPluss(31), stpPluss(45), 600);
    }

    @Test
    public void skal_ikke_slå_sammen_vedtak_med_opphold() {
        List<YtelseDto> vedtak = Arrays.asList(lagVedtak(STP, stpPluss(30), FagsakYtelseType.DAGPENGER,
                lagMeldekort(STP, stpPluss(14), 500),
                lagMeldekort(stpPluss(15), stpPluss(35), 600)),
                lagVedtak(stpPluss(32), stpPluss(60), FagsakYtelseType.DAGPENGER,
                        lagMeldekort(stpPluss(30), stpPluss(45), 700),
                        lagMeldekort(stpPluss(46), stpPluss(65), 800)));

        List<Periodeinntekt> inntekter = kjørMapping(vedtak);

        assertThat(inntekter).hasSize(4);
        assertInntekt(inntekter, STP, stpPluss(14), 500);
        assertInntekt(inntekter, stpPluss(15), stpPluss(30), 600);
        assertInntekt(inntekter, stpPluss(32), stpPluss(45), 700); // Fom avgrenses av vedtaksperiode
        assertInntekt(inntekter, stpPluss(46), stpPluss(60), 800); // tom avgrenses av vedtaksperiode
    }

    @Test
    public void skal_mappe_aap_flere_overlappende_vedtak() {
        List<YtelseDto> vedtak = Arrays.asList(lagVedtak(STP, STP, FagsakYtelseType.ARBEIDSAVKLARINGSPENGER,
                lagMeldekort(STP, stpPluss(14), 500), lagMeldekort(stpPluss(15), stpPluss(29), 700)),
        lagVedtak(STP, stpPluss(20), FagsakYtelseType.ARBEIDSAVKLARINGSPENGER));

        List<Periodeinntekt> inntekter = kjørMapping(vedtak);

        assertThat(inntekter).hasSize(2);
        assertInntekt(inntekter, STP, stpPluss(14), 500);
        assertInntekt(inntekter, stpPluss(15), stpPluss(20), 700);
    }

    @Test
    public void vedtak_med_meldekort_utenfor_periode_skal_slås_sammen_med_neste_vegg_i_vegg_vedtak() {
        List<YtelseDto> vedtak = Arrays.asList(lagVedtak(STP, stpPluss(30), FagsakYtelseType.ARBEIDSAVKLARINGSPENGER,
                lagMeldekort(STP, stpPluss(14), 500), lagMeldekort(stpPluss(15), stpPluss(30), 700),
                lagMeldekort(stpPluss(31), stpPluss(45), 800), lagMeldekort(stpPluss(46), stpPluss(60), 900)),
        lagVedtak(stpPluss(31), stpPluss(50), FagsakYtelseType.ARBEIDSAVKLARINGSPENGER));

        List<Periodeinntekt> inntekter = kjørMapping(vedtak);

        assertThat(inntekter).hasSize(4);
        assertInntekt(inntekter, STP, stpPluss(14), 500);
        assertInntekt(inntekter, stpPluss(15), stpPluss(30), 700);
        assertInntekt(inntekter, stpPluss(31), stpPluss(45), 800);
        assertInntekt(inntekter, stpPluss(46), stpPluss(50), 900); // Avkuttes av vedtaksperiode
    }

    private void assertInntekt(List<Periodeinntekt> inntekter, LocalDate fom, LocalDate tom, int inntekt) {
        Periodeinntekt matchendeInntekt = inntekter.stream()
                .filter(it -> it.getPeriode().getFom().equals(fom))
                .filter(it -> it.getPeriode().getTom().equals(tom))
                .filter(it -> it.getInntekt().compareTo(BigDecimal.valueOf(inntekt)) == 0)
                .findFirst()
                .orElse(null);
        assertThat(matchendeInntekt).isNotNull();
    }

    private List<Periodeinntekt> kjørMapping(List<YtelseDto> vedtak) {
        return MapArenaVedtakTilBesteberegningRegelmodell.lagInntektFraArenaYtelser(new YtelseFilterDto(vedtak));
    }

    private LocalDate stpPluss(int dager) {
        return STP.plusDays(dager);
    }

    private YtelseAnvistDto lagMeldekort(LocalDate fom, LocalDate tom, int beløp) {
        return YtelseAnvistDtoBuilder.ny()
                .medAnvistPeriode(Intervall.fraOgMedTilOgMed(fom, tom))
                .medBeløp(BigDecimal.valueOf(beløp))
                .build();
    }

    private YtelseDto lagVedtak(LocalDate fom, LocalDate tom, FagsakYtelseType ytelse, YtelseAnvistDto... meldekort) {
        YtelseDtoBuilder builder = YtelseDtoBuilder.oppdatere(Optional.empty())
                .medPeriode(Intervall.fraOgMedTilOgMed(fom, tom))
                .medYtelseType(ytelse);
        Arrays.asList(meldekort).forEach(builder::leggTilYtelseAnvist);
        return builder.build();
    }

}
