package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAktørDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;

public class FinnÅrsinntektvisningstallTest {
    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.of(2019,1,1);
    private static final String ORGNR = "987123987";

    @Test
    public void skal_ikke_sette_visningstall_hvis_ingen_perioder_på_grunnlaget() {
        BeregningsgrunnlagDto grunnlag = BeregningsgrunnlagDto.builder()
            .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER))
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .build();

        Optional<BigDecimal> visningstall = FinnÅrsinntektvisningstall.finn(grunnlag, Optional.empty());

        assertThat(visningstall).isEmpty();
    }

    @Test
    public void skal_sette_visningstall_lik_brutto_første_periode() {
        BigDecimal bruttoFørstePeriode = BigDecimal.valueOf(500000);
        BeregningsgrunnlagDto grunnlag = lagBeregningsgrunnlagVanlig(bruttoFørstePeriode);

        Optional<BigDecimal> visningstall = FinnÅrsinntektvisningstall.finn(grunnlag, Optional.empty());

        assertThat(visningstall).isPresent();
        assertThat(visningstall).hasValue(bruttoFørstePeriode);
    }

    @Test
    public void skal_sette_visningstall_lik_pgisnitt_hvis_selvstendig_næringsdrivende_og_ikke_ny_i_arblivet() {
        BigDecimal pgi= BigDecimal.valueOf(987595);
        BigDecimal brutto = BigDecimal.valueOf(766663);
        BeregningsgrunnlagDto grunnlag = lagBeregningsgrunnlagSN(pgi, brutto, false);
        FaktaAktørDto faktaAktør = FaktaAktørDto.builder().medErNyIArbeidslivetSNFastsattAvSaksbehandler(false).medSkalBesteberegnesFastsattAvSaksbehandler(false).build();

        Optional<BigDecimal> visningstall = FinnÅrsinntektvisningstall.finn(grunnlag, Optional.of(faktaAktør));

        assertThat(visningstall).isPresent();
        assertThat(visningstall).hasValue(pgi);
    }

    @Test
    public void skal_ikke_returnere_visningstall_hvis_sn_mned_ny_i_arbliv() {
        BigDecimal pgi = BigDecimal.valueOf(987595);
        BigDecimal brutto = BigDecimal.valueOf(766663);
        BeregningsgrunnlagDto grunnlag = lagBeregningsgrunnlagSN(pgi, brutto, false);
        FaktaAktørDto faktaAktør = FaktaAktørDto.builder().medErNyIArbeidslivetSNFastsattAvSaksbehandler(true).medSkalBesteberegnesFastsattAvSaksbehandler(false).build();

        Optional<BigDecimal> visningstall = FinnÅrsinntektvisningstall.finn(grunnlag, Optional.of(faktaAktør));

        assertThat(visningstall).isEmpty();
    }

    @Test
    public void skal_sette_visningstall_lik_brutto_hvis_sn_med_besteberegning() {
        BigDecimal pgi = BigDecimal.valueOf(987595);
        BigDecimal brutto = BigDecimal.valueOf(766663);

        BeregningsgrunnlagDto grunnlag = lagBeregningsgrunnlagSN(pgi, brutto, true);
        FaktaAktørDto faktaAktør = FaktaAktørDto.builder().medErNyIArbeidslivetSNFastsattAvSaksbehandler(false).medSkalBesteberegnesFastsattAvSaksbehandler(true).build();

        Optional<BigDecimal> visningstall = FinnÅrsinntektvisningstall.finn(grunnlag, Optional.of(faktaAktør));

        assertThat(visningstall).isPresent();
        assertThat(visningstall).hasValue(brutto);
    }

    @Test
    public void skal_sette_visningstall_lik_brutto_hvis_sn_med_besteberegning_og_ny_i_arb() {
        BigDecimal pgi = BigDecimal.valueOf(987595);
        BigDecimal brutto = BigDecimal.valueOf(766663);

        BeregningsgrunnlagDto grunnlag = lagBeregningsgrunnlagSN(pgi, brutto, true);
        FaktaAktørDto faktaAktør = FaktaAktørDto.builder().medErNyIArbeidslivetSNFastsattAvSaksbehandler(true).medSkalBesteberegnesFastsattAvSaksbehandler(true).build();

        Optional<BigDecimal> visningstall = FinnÅrsinntektvisningstall.finn(grunnlag, Optional.of(faktaAktør));

        assertThat(visningstall).isPresent();
        assertThat(visningstall).hasValue(brutto);
    }

    @Test
    public void skal_håndtere_nullverdier() {
        BeregningsgrunnlagDto grunnlag = lagBeregningsgrunnlagSN(null, null, false);
        FaktaAktørDto faktaAktør = FaktaAktørDto.builder().medErNyIArbeidslivetSNFastsattAvSaksbehandler(false).medSkalBesteberegnesFastsattAvSaksbehandler(false).build();

        Optional<BigDecimal> visningstall = FinnÅrsinntektvisningstall.finn(grunnlag, Optional.of(faktaAktør));

        assertThat(visningstall).isEmpty();
    }

    private BeregningsgrunnlagDto lagBeregningsgrunnlagVanlig(BigDecimal bruttoFørstePeriode) {

        BeregningsgrunnlagDto grunnlag = BeregningsgrunnlagDto.builder()
            .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER))
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .build();

        BeregningsgrunnlagPeriodeDto aktivPeriode = BeregningsgrunnlagPeriodeDto.builder()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null)
            .build(grunnlag);

        BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(Arbeidsgiver.virksomhet(ORGNR)))
            .medBeregnetPrÅr(bruttoFørstePeriode)
            .build(aktivPeriode);

        return grunnlag;

    }

    private BeregningsgrunnlagDto lagBeregningsgrunnlagSN(BigDecimal pgiSnitt, BigDecimal bruttoPrÅrAndel, boolean medBesteberegning) {

        BeregningsgrunnlagDto grunnlag = BeregningsgrunnlagDto.builder()
            .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE))
            .leggTilFaktaOmBeregningTilfeller(medBesteberegning ? Collections.singletonList(FaktaOmBeregningTilfelle.FASTSETT_BESTEBEREGNING_FØDENDE_KVINNE) : Collections.emptyList())
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .build();

        BeregningsgrunnlagPeriodeDto aktivPeriode = BeregningsgrunnlagPeriodeDto.builder()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null)
            .build(grunnlag);

        BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medInntektskategori(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE)
            .medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
            .medBeregnetPrÅr(bruttoPrÅrAndel)
            .medPgi(pgiSnitt, Collections.emptyList())
            .build(aktivPeriode);


        return grunnlag;

    }


}
