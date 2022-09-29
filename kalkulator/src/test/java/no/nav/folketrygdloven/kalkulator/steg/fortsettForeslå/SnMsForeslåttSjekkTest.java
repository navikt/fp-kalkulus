package no.nav.folketrygdloven.kalkulator.steg.fortsettForeslå;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;

class SnMsForeslåttSjekkTest {
    private static final LocalDate FOM = LocalDate.of(2022,1,1);
    private static final LocalDate TOM = LocalDate.of(2023,1,1);

    @Test
    public void tester_sn_og_ms_foreslått() {
        var snAndel = BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
                .medBeregnetPrÅr(BigDecimal.valueOf(500))
                .medAndelsnr(1L);
        var msAndel = BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(AktivitetStatus.MILITÆR_ELLER_SIVIL)
                .medBeregnetPrÅr(BigDecimal.valueOf(500))
                .medAndelsnr(2L);
        var periode = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(FOM, TOM)
                .leggTilBeregningsgrunnlagPrStatusOgAndel(snAndel)
                .leggTilBeregningsgrunnlagPrStatusOgAndel(msAndel);
        var bg = BeregningsgrunnlagDto.builder().medSkjæringstidspunkt(FOM)
                .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE))
                .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.MILITÆR_ELLER_SIVIL))
                .leggTilBeregningsgrunnlagPeriode(periode).build();
        var erForeslått = SnMsForeslåttSjekk.snOgMsErAlleredeForeslått(bg);

        assertThat(erForeslått).isTrue();
    }

    @Test
    public void tester_sn_foreslått_ingen_ms() {
        var snAndel = BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
                .medBeregnetPrÅr(BigDecimal.valueOf(500))
                .medAndelsnr(1L);
        var periode = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(FOM, TOM)
                .leggTilBeregningsgrunnlagPrStatusOgAndel(snAndel);
        var bg = BeregningsgrunnlagDto.builder().medSkjæringstidspunkt(FOM)
                .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE))
                .leggTilBeregningsgrunnlagPeriode(periode).build();
        var erForeslått = SnMsForeslåttSjekk.snOgMsErAlleredeForeslått(bg);

        assertThat(erForeslått).isTrue();
    }

    @Test
    public void tester_ms_foreslått_ingen_sn() {
        var snAndel = BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(AktivitetStatus.MILITÆR_ELLER_SIVIL)
                .medBeregnetPrÅr(BigDecimal.valueOf(500))
                .medAndelsnr(1L);
        var periode = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(FOM, TOM)
                .leggTilBeregningsgrunnlagPrStatusOgAndel(snAndel);
        var bg = BeregningsgrunnlagDto.builder().medSkjæringstidspunkt(FOM)
                .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.MILITÆR_ELLER_SIVIL))
                .leggTilBeregningsgrunnlagPeriode(periode).build();
        var erForeslått = SnMsForeslåttSjekk.snOgMsErAlleredeForeslått(bg);

        assertThat(erForeslått).isTrue();
    }

    @Test
    public void tester_sn_ikke_foreslått_ingen_ms() {
        var snAndel = BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
                .medAndelsnr(1L);
        var periode = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(FOM, TOM)
                .leggTilBeregningsgrunnlagPrStatusOgAndel(snAndel);
        var bg = BeregningsgrunnlagDto.builder().medSkjæringstidspunkt(FOM)
                .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE))
                .leggTilBeregningsgrunnlagPeriode(periode).build();
        var erForeslått = SnMsForeslåttSjekk.snOgMsErAlleredeForeslått(bg);

        assertThat(erForeslått).isFalse();
    }

    @Test
    public void tester_ms_ikke_foreslått_ingen_sn() {
        var snAndel = BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(AktivitetStatus.MILITÆR_ELLER_SIVIL)
                .medAndelsnr(1L);
        var periode = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(FOM, TOM)
                .leggTilBeregningsgrunnlagPrStatusOgAndel(snAndel);
        var bg = BeregningsgrunnlagDto.builder().medSkjæringstidspunkt(FOM)
                .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.MILITÆR_ELLER_SIVIL))
                .leggTilBeregningsgrunnlagPeriode(periode).build();
        var erForeslått = SnMsForeslåttSjekk.snOgMsErAlleredeForeslått(bg);

        assertThat(erForeslått).isFalse();
    }

    @Test
    public void tester_verken_ms_eller_sn_foreslått() {
        var snAndel = BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
                .medAndelsnr(1L);
        var msAndel = BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(AktivitetStatus.MILITÆR_ELLER_SIVIL)
                .medAndelsnr(2L);
        var periode = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(FOM, TOM)
                .leggTilBeregningsgrunnlagPrStatusOgAndel(snAndel)
                .leggTilBeregningsgrunnlagPrStatusOgAndel(msAndel);
        var bg = BeregningsgrunnlagDto.builder().medSkjæringstidspunkt(FOM)
                .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE))
                .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.MILITÆR_ELLER_SIVIL))
                .leggTilBeregningsgrunnlagPeriode(periode).build();
        var erForeslått = SnMsForeslåttSjekk.snOgMsErAlleredeForeslått(bg);

        assertThat(erForeslått).isFalse();
    }

    @Test
    public void tester_verken_sn_eller_ms() {
        var snAndel = BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(AktivitetStatus.DAGPENGER)
                .medAndelsnr(1L);
        var periode = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(FOM, TOM)
                .leggTilBeregningsgrunnlagPrStatusOgAndel(snAndel);
        var bg = BeregningsgrunnlagDto.builder().medSkjæringstidspunkt(FOM)
                .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.DAGPENGER))
                .leggTilBeregningsgrunnlagPeriode(periode).build();
        var erForeslått = SnMsForeslåttSjekk.snOgMsErAlleredeForeslått(bg);

        assertThat(erForeslått).isFalse();
    }

}
