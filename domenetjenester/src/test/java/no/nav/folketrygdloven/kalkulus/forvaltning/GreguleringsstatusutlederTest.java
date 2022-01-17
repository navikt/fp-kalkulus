package no.nav.folketrygdloven.kalkulus.forvaltning;

import static no.nav.folketrygdloven.kalkulus.felles.tid.AbstractIntervall.TIDENES_ENDE;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningAktivitetAggregatEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatus;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagBuilder;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Beløp;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Årsgrunnlag;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.GrunnbeløpReguleringStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseTyperKalkulusStøtterKontrakt;

class GreguleringsstatusutlederTest {
    private static final LocalDate STP = LocalDate.of(2021, 1, 1);
    private static final BigDecimal GAMMEL_G = BigDecimal.valueOf(100000);
    private static final BigDecimal NY_G = BigDecimal.valueOf(105000);

    @Test
    public void skal_ikke_regulere_grunnbeløp_når_ikke_foreslått_grunnlag() {
        BeregningsgrunnlagEntitet bg = BeregningsgrunnlagEntitet.builder()
                .medSkjæringstidspunkt(STP)
                .medGrunnbeløp(GAMMEL_G)
                .build();
        BeregningsgrunnlagPeriode.builder()
                .medBeregningsgrunnlagPeriode(STP, TIDENES_ENDE)
                .leggTilBeregningsgrunnlagPrStatusOgAndel(BeregningsgrunnlagPrStatusOgAndel.builder()
                        .medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
                        .medAndelsnr(1L)
                        .medGrunnlagPrÅr(lagBeregnet(800000)))
                .build(bg);
        Optional<BeregningsgrunnlagGrunnlagEntitet> gr = Optional.of(BeregningsgrunnlagGrunnlagBuilder.kopiere(Optional.empty())
                .medRegisterAktiviteter(BeregningAktivitetAggregatEntitet.builder()
                        .medSkjæringstidspunktOpptjening(STP)
                        .build())
                .medBeregningsgrunnlag(bg)
                .build(1L, BeregningsgrunnlagTilstand.FASTSATT_BEREGNINGSAKTIVITETER));

        GrunnbeløpReguleringStatus resultat = Greguleringsstatusutleder.utledStatus(gr, NY_G, YtelseTyperKalkulusStøtterKontrakt.OMSORGSPENGER);

        assertThat(resultat).isEqualTo(GrunnbeløpReguleringStatus.IKKE_VURDERT);
    }

    private Årsgrunnlag lagBeregnet(int verdi) {
        return new Årsgrunnlag(new Beløp(verdi), null, null, null, null, new Beløp(verdi));
    }

    @Test
    public void skal_ikke_regulere_grunnbeløp_når_arbeidstaker_med_inntekt_under_grenseverdi() {
        BeregningsgrunnlagEntitet bg = BeregningsgrunnlagEntitet.builder()
                .medSkjæringstidspunkt(STP)
                .medGrunnbeløp(GAMMEL_G)
                .build();
        BeregningsgrunnlagPeriode.builder()
                .medBeregningsgrunnlagPeriode(STP, TIDENES_ENDE)
                .leggTilBeregningsgrunnlagPrStatusOgAndel(BeregningsgrunnlagPrStatusOgAndel.builder()
                        .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                        .medAndelsnr(1L)
                        .medBGAndelArbeidsforhold(BGAndelArbeidsforhold.builder().medArbeidsgiver(Arbeidsgiver.virksomhet("999999999")))
                        .medGrunnlagPrÅr(lagBeregnet(400000)))
                .build(bg);
        Optional<BeregningsgrunnlagGrunnlagEntitet> gr = Optional.of(BeregningsgrunnlagGrunnlagBuilder.kopiere(Optional.empty())
                .medRegisterAktiviteter(BeregningAktivitetAggregatEntitet.builder()
                        .medSkjæringstidspunktOpptjening(STP)
                        .build())
                .medBeregningsgrunnlag(bg)
                .build(1L, BeregningsgrunnlagTilstand.FASTSATT));

        GrunnbeløpReguleringStatus resultat = Greguleringsstatusutleder.utledStatus(gr, NY_G, YtelseTyperKalkulusStøtterKontrakt.OMSORGSPENGER);

        assertThat(resultat).isEqualTo(GrunnbeløpReguleringStatus.IKKE_NØDVENDIG);
    }

    @Test
    public void skal_regulere_grunnbeløp_når_beregnet_som_næringsdrivende() {
        BeregningsgrunnlagEntitet bg = BeregningsgrunnlagEntitet.builder()
                .medSkjæringstidspunkt(STP)
                .medGrunnbeløp(GAMMEL_G)
                .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatus.builder().medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE))
                .build();
        BeregningsgrunnlagPeriode.builder()
                .medBeregningsgrunnlagPeriode(STP, TIDENES_ENDE)
                .leggTilBeregningsgrunnlagPrStatusOgAndel(BeregningsgrunnlagPrStatusOgAndel.builder()
                        .medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
                        .medAndelsnr(1L)
                        .medGrunnlagPrÅr(lagBeregnet(200000)))
                .build(bg);
        Optional<BeregningsgrunnlagGrunnlagEntitet> gr = Optional.of(BeregningsgrunnlagGrunnlagBuilder.kopiere(Optional.empty())
                .medRegisterAktiviteter(BeregningAktivitetAggregatEntitet.builder()
                        .medSkjæringstidspunktOpptjening(STP)
                        .build())
                .medBeregningsgrunnlag(bg)
                .build(1L, BeregningsgrunnlagTilstand.FASTSATT));

        GrunnbeløpReguleringStatus resultat = Greguleringsstatusutleder.utledStatus(gr, NY_G, YtelseTyperKalkulusStøtterKontrakt.OMSORGSPENGER);

        assertThat(resultat).isEqualTo(GrunnbeløpReguleringStatus.NØDVENDIG);
    }

    @Test
    public void skal_regulere_grunnbeløp_når_beregnet_som_militær_med_inntekt_under_minstegrense() {
        BeregningsgrunnlagEntitet bg = BeregningsgrunnlagEntitet.builder()
                .medSkjæringstidspunkt(STP)
                .medGrunnbeløp(GAMMEL_G)
                .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatus.builder().medAktivitetStatus(AktivitetStatus.MILITÆR_ELLER_SIVIL))
                .build();
        BeregningsgrunnlagPeriode.builder()
                .medBeregningsgrunnlagPeriode(STP, TIDENES_ENDE)
                .leggTilBeregningsgrunnlagPrStatusOgAndel(BeregningsgrunnlagPrStatusOgAndel.builder()
                        .medAktivitetStatus(AktivitetStatus.MILITÆR_ELLER_SIVIL)
                        .medAndelsnr(1L)
                        .medGrunnlagPrÅr(lagBeregnet(200000)))
                .build(bg);
        Optional<BeregningsgrunnlagGrunnlagEntitet> gr = Optional.of(BeregningsgrunnlagGrunnlagBuilder.kopiere(Optional.empty())
                .medRegisterAktiviteter(BeregningAktivitetAggregatEntitet.builder()
                        .medSkjæringstidspunktOpptjening(STP)
                        .build())
                .medBeregningsgrunnlag(bg)
                .build(1L, BeregningsgrunnlagTilstand.FASTSATT));

        GrunnbeløpReguleringStatus resultat = Greguleringsstatusutleder.utledStatus(gr, NY_G, YtelseTyperKalkulusStøtterKontrakt.OMSORGSPENGER);

        assertThat(resultat).isEqualTo(GrunnbeløpReguleringStatus.NØDVENDIG);
    }

    @Test
    public void skal_regulere_grunnbeløp_når_beregnet_som_arbeid_som_blir_avkortet() {
        BeregningsgrunnlagEntitet bg = BeregningsgrunnlagEntitet.builder()
                .medSkjæringstidspunkt(STP)
                .medGrunnbeløp(GAMMEL_G)
                .build();
        BeregningsgrunnlagPeriode.builder()
                .medBeregningsgrunnlagPeriode(STP, TIDENES_ENDE)
                .leggTilBeregningsgrunnlagPrStatusOgAndel(BeregningsgrunnlagPrStatusOgAndel.builder()
                        .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                        .medAndelsnr(1L)
                        .medBGAndelArbeidsforhold(BGAndelArbeidsforhold.builder().medArbeidsgiver(Arbeidsgiver.virksomhet("999999999")))
                        .medGrunnlagPrÅr(lagBeregnet(610000)))
                .build(bg);
        Optional<BeregningsgrunnlagGrunnlagEntitet> gr = Optional.of(BeregningsgrunnlagGrunnlagBuilder.kopiere(Optional.empty())
                .medRegisterAktiviteter(BeregningAktivitetAggregatEntitet.builder()
                        .medSkjæringstidspunktOpptjening(STP)
                        .build())
                .medBeregningsgrunnlag(bg)
                .build(1L, BeregningsgrunnlagTilstand.FASTSATT));

        GrunnbeløpReguleringStatus resultat = Greguleringsstatusutleder.utledStatus(gr, NY_G, YtelseTyperKalkulusStøtterKontrakt.OMSORGSPENGER);

        assertThat(resultat).isEqualTo(GrunnbeløpReguleringStatus.NØDVENDIG);
    }

}
