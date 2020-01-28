package no.nav.folketrygdloven.kalkulator;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import org.junit.After;
import org.junit.Test;

import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.BeregningsgrunnlagTestUtil;
import no.nav.folketrygdloven.kalkulator.modell.behandling.BehandlingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktørYtelseDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.kodeverk.Arbeidskategori;
import no.nav.folketrygdloven.kalkulator.modell.iay.kodeverk.RelatertYtelseTilstand;
import no.nav.folketrygdloven.kalkulator.modell.ytelse.RelatertYtelseType;
import no.nav.folketrygdloven.kalkulator.regelmodell.Periode;
import no.nav.folketrygdloven.kalkulator.testutilities.BeregningIAYTestUtil;
import no.nav.vedtak.util.FPDateUtil;

public class AutopunktUtlederFastsettBeregningsaktiviteterTjenesteTest {

    private BehandlingReferanse ref = new BehandlingReferanseMock();

    @After
    public void after() {
        System.clearProperty(FPDateUtil.SystemConfiguredClockProvider.PROPERTY_KEY_OFFSET_PERIODE);
        FPDateUtil.init();
    }

    @Test
    public void skal_vente_på_meldekort_når_har_AAP_og_meldekort_uten_AAP_status() {
        // Arrange
        LocalDate dagensdato = LocalDate.of(2019, 1, 4);
        LocalDate skjæringstidspunktOpptjening = LocalDate.of(2019, 2, 1);
        BeregningsgrunnlagDto bg = BeregningsgrunnlagTestUtil.lagGjeldendeBeregningsgrunnlag(ref, skjæringstidspunktOpptjening, Optional.empty(), AktivitetStatus.ARBEIDSTAKER);
        LocalDate fom = LocalDate.of(2018, 9, 1);
        LocalDate tom = LocalDate.of(2019, 5, 1);
        LocalDate meldekortFom = LocalDate.of(2018, 12, 1);
        AktørYtelseDto aktørYtelse = BeregningIAYTestUtil.leggTilAktørytelse(InntektArbeidYtelseGrunnlagDtoBuilder.nytt(), ref, fom, tom, RelatertYtelseTilstand.LØPENDE, "123", RelatertYtelseType.ARBEIDSAVKLARINGSPENGER,
            Collections.emptyList(), Arbeidskategori.ARBEIDSTAKER, lagMeldekortPeriode(meldekortFom));

        // Act
        Optional<LocalDate> resultat = AutopunktUtlederFastsettBeregningsaktiviteterTjeneste.skalVenteTilDatoPåMeldekortAAPellerDP(Optional.of(aktørYtelse), bg, dagensdato);

        //Assert
        assertThat(resultat).isPresent();
        assertThat(resultat.get()).isEqualTo(skjæringstidspunktOpptjening.plusDays(1));
    }

    @Test
    public void skal_vente_på_meldekort_når_har_AAP_og_meldekort_uten_AAP_status_grenseverdi() {
        // Arrange
        LocalDate dagensdato = LocalDate.of(2019, 1, 4);
        LocalDate skjæringstidspunktOpptjening = LocalDate.of(2019, 2, 1);
        BeregningsgrunnlagDto bg = BeregningsgrunnlagTestUtil.lagGjeldendeBeregningsgrunnlag(ref, skjæringstidspunktOpptjening, Optional.empty(), AktivitetStatus.ARBEIDSTAKER);
        LocalDate fom = LocalDate.of(2018, 9, 1);
        LocalDate tom = skjæringstidspunktOpptjening.minusDays(1);
        LocalDate meldekortFom = LocalDate.of(2018, 10, 1);
        AktørYtelseDto aktørYtelse = BeregningIAYTestUtil.leggTilAktørytelse(InntektArbeidYtelseGrunnlagDtoBuilder.nytt(), ref, fom, tom, RelatertYtelseTilstand.LØPENDE, "123", RelatertYtelseType.ARBEIDSAVKLARINGSPENGER,
            Collections.emptyList(), Arbeidskategori.ARBEIDSTAKER, lagMeldekortPeriode(meldekortFom));

        // Act
        Optional<LocalDate> resultat = AutopunktUtlederFastsettBeregningsaktiviteterTjeneste.skalVenteTilDatoPåMeldekortAAPellerDP(Optional.of(aktørYtelse), bg, dagensdato);

        //Assert
        assertThat(resultat).isPresent();
        assertThat(resultat.get()).isEqualTo(skjæringstidspunktOpptjening.plusDays(1));
    }

    @Test
    public void skal_vente_på_meldekort_når_har_AAP_og_meldekort_uten_AAP_status_etter_første_utta() {
        // Arrange
        LocalDate dagensdato = LocalDate.of(2019, 2, 2);
        LocalDate skjæringstidspunktOpptjening = LocalDate.of(2019, 2, 1);
        LocalDate fom = LocalDate.of(2018, 9, 1);
        LocalDate tom = LocalDate.of(2019, 5, 1);
        LocalDate meldekortFom = LocalDate.of(2018, 12, 1);
        LocalDate meldekortFom2 = LocalDate.of(2019, 1, 12);
        BeregningsgrunnlagDto bg = BeregningsgrunnlagTestUtil.lagGjeldendeBeregningsgrunnlag(ref, skjæringstidspunktOpptjening, Optional.empty(), AktivitetStatus.ARBEIDSTAKER);
        AktørYtelseDto aktørYtelse = BeregningIAYTestUtil.leggTilAktørytelse(InntektArbeidYtelseGrunnlagDtoBuilder.nytt(), ref, fom, tom, RelatertYtelseTilstand.LØPENDE, "123", RelatertYtelseType.ARBEIDSAVKLARINGSPENGER,
            Collections.emptyList(), Arbeidskategori.ARBEIDSTAKER, lagMeldekortPeriode(meldekortFom), lagMeldekortPeriode(meldekortFom2));

        // Act
        Optional<LocalDate> resultat = AutopunktUtlederFastsettBeregningsaktiviteterTjeneste.skalVenteTilDatoPåMeldekortAAPellerDP(Optional.of(aktørYtelse), bg, dagensdato);

        //Assert
        assertThat(resultat).isPresent();
        assertThat(resultat.get()).isEqualTo(skjæringstidspunktOpptjening.plusDays(2));
    }

    @Test
    public void skal_vente_på_meldekort_også_når_har_AAP_status() {
        // Arrange
        LocalDate dagensdato = LocalDate.of(2019, 2, 9);
        LocalDate skjæringstidspunktOpptjening = LocalDate.of(2019, 2, 1);
        LocalDate fom = LocalDate.of(2018, 9, 1);
        LocalDate tom = LocalDate.of(2019, 5, 1);
        LocalDate meldekortFom = LocalDate.of(2018, 12, 1);
        LocalDate meldekortFom2 = LocalDate.of(2019, 1, 12);
        InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        AktørYtelseDto aktørYtelse = BeregningIAYTestUtil.leggTilAktørytelse(iayGrunnlagBuilder, ref, fom, tom, RelatertYtelseTilstand.LØPENDE, "123", RelatertYtelseType.ARBEIDSAVKLARINGSPENGER,
            Collections.emptyList(), Arbeidskategori.ARBEIDSTAKER, lagMeldekortPeriode(meldekortFom), lagMeldekortPeriode(meldekortFom2));
        BeregningsgrunnlagDto bg = BeregningsgrunnlagTestUtil.lagGjeldendeBeregningsgrunnlag(ref, skjæringstidspunktOpptjening, Optional.of(iayGrunnlagBuilder.build()), AktivitetStatus.ARBEIDSAVKLARINGSPENGER);

        // Act
        Optional<LocalDate> resultat = AutopunktUtlederFastsettBeregningsaktiviteterTjeneste.skalVenteTilDatoPåMeldekortAAPellerDP(Optional.of(aktørYtelse), bg, dagensdato);

        //Assert
        assertThat(resultat).isPresent();
        assertThat(resultat.get()).isEqualTo(dagensdato.plusDays(1));
    }

    @Test
    public void skal_ikke_vente_på_meldekort_når_14_dager_etter_første_uttaksdag() {
        // Arrange
        LocalDate dagensdato = LocalDate.of(2019, 2, 16);
        LocalDate skjæringstidspunktOpptjening = LocalDate.of(2019, 2, 1);
        LocalDate fom = LocalDate.of(2018, 9, 1);
        LocalDate tom = LocalDate.of(2019, 5, 1);
        LocalDate meldekortFom = LocalDate.of(2018, 12, 1);
        LocalDate meldekortFom2 = LocalDate.of(2019, 1, 12);
        BeregningsgrunnlagDto bg = BeregningsgrunnlagTestUtil.lagGjeldendeBeregningsgrunnlag(ref, skjæringstidspunktOpptjening, Optional.empty(), AktivitetStatus.ARBEIDSTAKER);
        AktørYtelseDto aktørYtelse = BeregningIAYTestUtil.leggTilAktørytelse(InntektArbeidYtelseGrunnlagDtoBuilder.nytt(), ref, fom, tom, RelatertYtelseTilstand.LØPENDE, "123", RelatertYtelseType.ARBEIDSAVKLARINGSPENGER,
            Collections.emptyList(), Arbeidskategori.ARBEIDSTAKER, lagMeldekortPeriode(meldekortFom), lagMeldekortPeriode(meldekortFom2));

        // Act
        Optional<LocalDate> resultat = AutopunktUtlederFastsettBeregningsaktiviteterTjeneste.skalVenteTilDatoPåMeldekortAAPellerDP(Optional.of(aktørYtelse), bg, dagensdato);

        //Assert
        assertThat(resultat).isNotPresent();
    }

    @Test
    public void skal_ikke_vente_på_meldekort_når_ikke_har_meldekort_siste_4_måneder() {
        // Arrange
        LocalDate dagensdato = LocalDate.of(2019, 1, 4);
        LocalDate skjæringstidspunktOpptjening = LocalDate.of(2019, 2, 1);
        BeregningsgrunnlagDto bg = BeregningsgrunnlagTestUtil.lagGjeldendeBeregningsgrunnlag(ref, skjæringstidspunktOpptjening, Optional.empty(), AktivitetStatus.ARBEIDSTAKER);
        LocalDate fom = LocalDate.of(2018, 9, 1);
        LocalDate tom = LocalDate.of(2019, 5, 1);
        LocalDate meldekortFom = LocalDate.of(2018, 9, 17);
        AktørYtelseDto aktørYtelse = BeregningIAYTestUtil.leggTilAktørytelse(InntektArbeidYtelseGrunnlagDtoBuilder.nytt(), ref, fom, tom, RelatertYtelseTilstand.LØPENDE, "123",
            RelatertYtelseType.ARBEIDSAVKLARINGSPENGER, Collections.emptyList(), Arbeidskategori.ARBEIDSTAKER, lagMeldekortPeriode(meldekortFom));

        // Act
        Optional<LocalDate> resultat = AutopunktUtlederFastsettBeregningsaktiviteterTjeneste.skalVenteTilDatoPåMeldekortAAPellerDP(Optional.of(aktørYtelse), bg, dagensdato);

        //Assert
        assertThat(resultat).isNotPresent();
    }

    @Test
    public void skal_ikke_vente_på_meldekort_når_ikke_har_løpende_vedtak() {
        // Arrange
        LocalDate dagensdato = LocalDate.of(2019, 1, 4);
        LocalDate skjæringstidspunktOpptjening = LocalDate.of(2019, 2, 1);
        BeregningsgrunnlagDto bg = BeregningsgrunnlagTestUtil.lagGjeldendeBeregningsgrunnlag(ref, skjæringstidspunktOpptjening, Optional.empty(), AktivitetStatus.ARBEIDSTAKER);
        LocalDate fom = LocalDate.of(2018, 9, 1);
        LocalDate tom = LocalDate.of(2018, 12, 31);
        LocalDate meldekortFom = LocalDate.of(2018, 12, 1);
        AktørYtelseDto aktørYtelse = BeregningIAYTestUtil.leggTilAktørytelse(InntektArbeidYtelseGrunnlagDtoBuilder.nytt(), ref, fom, tom, RelatertYtelseTilstand.AVSLUTTET, "123",
            RelatertYtelseType.ARBEIDSAVKLARINGSPENGER, Collections.emptyList(), Arbeidskategori.ARBEIDSTAKER, lagMeldekortPeriode(meldekortFom));

        // Act
        Optional<LocalDate> resultat = AutopunktUtlederFastsettBeregningsaktiviteterTjeneste.skalVenteTilDatoPåMeldekortAAPellerDP(Optional.of(aktørYtelse), bg, dagensdato);

        //Assert
        assertThat(resultat).isNotPresent();
    }

    @Test
    public void skal_ikke_vente_på_meldekort_når_er_vanlig_arbeidstaker() {
        // Arrange
        LocalDate dagensdato = LocalDate.of(2019, 1, 4);
        LocalDate skjæringstidspunktOpptjening = LocalDate.of(2019, 2, 1);
        BeregningsgrunnlagDto bg = BeregningsgrunnlagTestUtil.lagGjeldendeBeregningsgrunnlag(ref, skjæringstidspunktOpptjening, Optional.empty(), AktivitetStatus.ARBEIDSTAKER);

        // Act
        Optional<LocalDate> resultat = AutopunktUtlederFastsettBeregningsaktiviteterTjeneste.skalVenteTilDatoPåMeldekortAAPellerDP(Optional.empty(), bg, dagensdato);

        //Assert
        assertThat(resultat).isNotPresent();
    }

    @Test
    public void skal_vente_på_meldekort_når_har_DP_og_meldekort_uten_DP_status() {
        // Arrange
        LocalDate dagensdato = LocalDate.of(2019, 1, 4);
        LocalDate skjæringstidspunktOpptjening = LocalDate.of(2019, 2, 1);
        BeregningsgrunnlagDto bg = BeregningsgrunnlagTestUtil.lagGjeldendeBeregningsgrunnlag(ref, skjæringstidspunktOpptjening, Optional.empty(), AktivitetStatus.ARBEIDSTAKER);
        LocalDate fom = LocalDate.of(2018, 9, 1);
        LocalDate tom = LocalDate.of(2019, 5, 1);
        LocalDate meldekortFom = LocalDate.of(2018, 5, 1);
        AktørYtelseDto aktørYtelse = BeregningIAYTestUtil.leggTilAktørytelse(InntektArbeidYtelseGrunnlagDtoBuilder.nytt(), ref, fom, tom, RelatertYtelseTilstand.LØPENDE, "123", RelatertYtelseType.DAGPENGER,
            Collections.emptyList(), Arbeidskategori.ARBEIDSTAKER, lagMeldekortPeriode(meldekortFom));

        // Act
        Optional<LocalDate> resultat = AutopunktUtlederFastsettBeregningsaktiviteterTjeneste.skalVenteTilDatoPåMeldekortAAPellerDP(Optional.of(aktørYtelse), bg, dagensdato);

        //Assert
        assertThat(resultat).isPresent();
        assertThat(resultat.get()).isEqualTo(skjæringstidspunktOpptjening.plusDays(1));
    }

    @Test
    public void skal_vente_på_meldekort_når_har_DP_status() {
        // Arrange
        LocalDate dagensdato = LocalDate.of(2019, 2, 9);
        LocalDate skjæringstidspunktOpptjening = LocalDate.of(2019, 2, 1);
        LocalDate fom = LocalDate.of(2018, 9, 1);
        LocalDate tom = LocalDate.of(2019, 5, 1);
        LocalDate meldekortFom = LocalDate.of(2018, 12, 1);
        LocalDate meldekortFom2 = LocalDate.of(2019, 1, 12);
        BeregningsgrunnlagDto bg = BeregningsgrunnlagTestUtil.lagGjeldendeBeregningsgrunnlag(ref, skjæringstidspunktOpptjening, Optional.empty(), AktivitetStatus.DAGPENGER);
        AktørYtelseDto aktørYtelse = BeregningIAYTestUtil.leggTilAktørytelse(InntektArbeidYtelseGrunnlagDtoBuilder.nytt(), ref, fom, tom, RelatertYtelseTilstand.LØPENDE, "123", RelatertYtelseType.DAGPENGER,
            Collections.emptyList(), Arbeidskategori.ARBEIDSTAKER, lagMeldekortPeriode(meldekortFom), lagMeldekortPeriode(meldekortFom2));

        // Act
        Optional<LocalDate> resultat = AutopunktUtlederFastsettBeregningsaktiviteterTjeneste.skalVenteTilDatoPåMeldekortAAPellerDP(Optional.of(aktørYtelse), bg, dagensdato);

        //Assert
        assertThat(resultat).isPresent();
        assertThat(resultat.get()).isEqualTo(dagensdato.plusDays(1));
    }

    @Test
    public void skal_vente_på_meldekort_når_ikke_har_løpende_vedtak_men_var_løpende_til_skjæringstidspunkt() {
        // Arrange
        LocalDate dagensdato = LocalDate.of(2019, 2, 12);
        LocalDate skjæringstidspunktOpptjening = LocalDate.of(2019, 2, 11);
        BeregningsgrunnlagDto bg = BeregningsgrunnlagTestUtil.lagGjeldendeBeregningsgrunnlag(ref, skjæringstidspunktOpptjening, Optional.empty(), AktivitetStatus.ARBEIDSTAKER);
        LocalDate fom = LocalDate.of(2018, 9, 3);
        LocalDate tom = LocalDate.of(2019, 2, 11);
        LocalDate meldekortFom = LocalDate.of(2019, 1, 21);
        AktørYtelseDto aktørYtelse = BeregningIAYTestUtil.leggTilAktørytelse(InntektArbeidYtelseGrunnlagDtoBuilder.nytt(), ref, fom, tom, RelatertYtelseTilstand.AVSLUTTET, "123", RelatertYtelseType.ARBEIDSAVKLARINGSPENGER,
            Collections.emptyList(), Arbeidskategori.ARBEIDSTAKER, lagMeldekortPeriode(meldekortFom));

        // Act
        Optional<LocalDate> resultat = AutopunktUtlederFastsettBeregningsaktiviteterTjeneste.skalVenteTilDatoPåMeldekortAAPellerDP(Optional.of(aktørYtelse), bg, dagensdato);

        //Assert
        assertThat(resultat).isPresent();
        assertThat(resultat.get()).isEqualTo(dagensdato.plusDays(1));
    }

    private Periode lagMeldekortPeriode(LocalDate fom) {
        return Periode.of(fom, fom.plusDays(13));
    }

}
