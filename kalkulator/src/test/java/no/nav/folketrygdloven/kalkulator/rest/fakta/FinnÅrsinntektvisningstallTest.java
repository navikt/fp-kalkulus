package no.nav.folketrygdloven.kalkulator.rest.fakta;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.FaktaOmBeregningTilfelle;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.ArbeidsgiverMedNavn;

public class FinnÅrsinntektvisningstallTest {
    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.of(2019,1,1);
    private static final String ORGNR = "987123987";

    @Test
    public void skal_ikke_sette_visningstall_hvis_ingen_perioder_på_grunnlaget() {
        BeregningsgrunnlagRestDto grunnlag = BeregningsgrunnlagRestDto.builder()
            .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusRestDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER))
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .build();

        Optional<BigDecimal> visningstall = FinnÅrsinntektvisningstall.finn(grunnlag);

        assertThat(visningstall).isEmpty();
    }

    @Test
    public void skal_sette_visningstall_lik_brutto_første_periode() {
        BigDecimal bruttoFørstePeriode = BigDecimal.valueOf(500000);
        BeregningsgrunnlagRestDto grunnlag = lagBeregningsgrunnlagVanlig(bruttoFørstePeriode);

        Optional<BigDecimal> visningstall = FinnÅrsinntektvisningstall.finn(grunnlag);

        assertThat(visningstall).isPresent();
        assertThat(visningstall).hasValue(bruttoFørstePeriode);
    }

    @Test
    public void skal_sette_visningstall_lik_pgisnitt_hvis_selvstendig_næringsdrivende_og_ikke_ny_i_arblivet() {
        BigDecimal pgi= BigDecimal.valueOf(987595);
        BigDecimal brutto = BigDecimal.valueOf(766663);
        BeregningsgrunnlagRestDto grunnlag = lagBeregningsgrunnlagSN(pgi, brutto, false, false);

        Optional<BigDecimal> visningstall = FinnÅrsinntektvisningstall.finn(grunnlag);

        assertThat(visningstall).isPresent();
        assertThat(visningstall).hasValue(pgi);
    }

    @Test
    public void skal_ikke_returnere_visningstall_hvis_sn_mned_ny_i_arbliv() {
        BigDecimal pgi = BigDecimal.valueOf(987595);
        BigDecimal brutto = BigDecimal.valueOf(766663);
        BeregningsgrunnlagRestDto grunnlag = lagBeregningsgrunnlagSN(pgi, brutto, true, false);

        Optional<BigDecimal> visningstall = FinnÅrsinntektvisningstall.finn(grunnlag);

        assertThat(visningstall).isEmpty();
    }

    @Test
    public void skal_sette_visningstall_lik_brutto_hvis_sn_med_besteberegning() {
        BigDecimal pgi = BigDecimal.valueOf(987595);
        BigDecimal brutto = BigDecimal.valueOf(766663);

        BeregningsgrunnlagRestDto grunnlag = lagBeregningsgrunnlagSN(pgi, brutto, false, true);

        Optional<BigDecimal> visningstall = FinnÅrsinntektvisningstall.finn(grunnlag);

        assertThat(visningstall).isPresent();
        assertThat(visningstall).hasValue(brutto);
    }

    @Test
    public void skal_sette_visningstall_lik_brutto_hvis_sn_med_besteberegning_og_ny_i_arb() {
        BigDecimal pgi = BigDecimal.valueOf(987595);
        BigDecimal brutto = BigDecimal.valueOf(766663);

        BeregningsgrunnlagRestDto grunnlag = lagBeregningsgrunnlagSN(pgi, brutto, true, true);

        Optional<BigDecimal> visningstall = FinnÅrsinntektvisningstall.finn(grunnlag);

        assertThat(visningstall).isPresent();
        assertThat(visningstall).hasValue(brutto);
    }

    @Test
    public void skal_håndtere_nullverdier() {
        BeregningsgrunnlagRestDto grunnlag = lagBeregningsgrunnlagSN(null, null, false, false);

        Optional<BigDecimal> visningstall = FinnÅrsinntektvisningstall.finn(grunnlag);

        assertThat(visningstall).isEmpty();
    }

    private BeregningsgrunnlagRestDto lagBeregningsgrunnlagVanlig(BigDecimal bruttoFørstePeriode) {

        BeregningsgrunnlagRestDto grunnlag = BeregningsgrunnlagRestDto.builder()
            .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusRestDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER))
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .build();

        BeregningsgrunnlagPeriodeRestDto aktivPeriode = BeregningsgrunnlagPeriodeRestDto.builder()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null)
            .build(grunnlag);

        BeregningsgrunnlagPrStatusOgAndelRestDto.kopier()
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdRestDto.builder().medArbeidsgiver(ArbeidsgiverMedNavn.virksomhet(ORGNR)))
            .medBeregnetPrÅr(bruttoFørstePeriode)
            .build(aktivPeriode);

        return grunnlag;

    }

    private BeregningsgrunnlagRestDto lagBeregningsgrunnlagSN(BigDecimal pgiSnitt, BigDecimal bruttoPrÅrAndel, boolean nyIArbliv, boolean medBesteberegning) {

        BeregningsgrunnlagRestDto grunnlag = BeregningsgrunnlagRestDto.builder()
            .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusRestDto.builder().medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE))
            .leggTilFaktaOmBeregningTilfeller(medBesteberegning ? Collections.singletonList(FaktaOmBeregningTilfelle.FASTSETT_BESTEBEREGNING_FØDENDE_KVINNE) : Collections.emptyList())
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .build();

        BeregningsgrunnlagPeriodeRestDto aktivPeriode = BeregningsgrunnlagPeriodeRestDto.builder()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null)
            .build(grunnlag);

        BeregningsgrunnlagPrStatusOgAndelRestDto.kopier()
            .medInntektskategori(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE)
            .medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
            .medBeregnetPrÅr(bruttoPrÅrAndel)
            .medPgi(pgiSnitt, Collections.emptyList())
            .medNyIArbeidslivet(nyIArbliv)
            .build(aktivPeriode);


        return grunnlag;

    }


}
