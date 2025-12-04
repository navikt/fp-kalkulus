package no.nav.folketrygdloven.kalkulus.domene.forvaltning;

import static no.nav.folketrygdloven.kalkulus.domene.felles.jpa.AbstractIntervall.TIDENES_ENDE;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.AndelArbeidsforholdEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.AktivitetAggregatEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagBuilder;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagPeriodeEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagAndelEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Beløp;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Årsgrunnlag;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.kodeverk.GrunnbeløpReguleringStatus;

class GreguleringsstatusutlederTest {
    private static final LocalDate STP = LocalDate.of(2021, 1, 1);
    private static final BigDecimal GAMMEL_G = BigDecimal.valueOf(100000);
    private static final BigDecimal NY_G = BigDecimal.valueOf(105000);

    @Test
    void skal_ikke_regulere_grunnbeløp_når_ikke_foreslått_grunnlag() {
        var periode = BeregningsgrunnlagPeriodeEntitet.builder()
            .medBeregningsgrunnlagPeriode(STP, TIDENES_ENDE)
            .leggTilBeregningsgrunnlagAndel(BeregningsgrunnlagAndelEntitet.builder()
                .medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
                .medAndelsnr(1L)
                .medGrunnlagPrÅr(lagBeregnet(800000)))
            .build();
        BeregningsgrunnlagEntitet bg = BeregningsgrunnlagEntitet.builder()
            .medSkjæringstidspunkt(STP)
            .medGrunnbeløp(GAMMEL_G)
            .leggTilBeregningsgrunnlagPeriode(periode)
            .build();
        Optional<BeregningsgrunnlagGrunnlagEntitet> gr = Optional.of(BeregningsgrunnlagGrunnlagBuilder.kopiere(Optional.empty())
                .medRegisterAktiviteter(AktivitetAggregatEntitet.builder()
                        .medSkjæringstidspunktOpptjening(STP)
                        .build())
                .medBeregningsgrunnlag(bg)
                .build(1L, BeregningsgrunnlagTilstand.FASTSATT_BEREGNINGSAKTIVITETER));

        GrunnbeløpReguleringStatus resultat = Greguleringsstatusutleder.utledStatus(gr, NY_G, FagsakYtelseType.OMSORGSPENGER);

        assertThat(resultat).isEqualTo(GrunnbeløpReguleringStatus.IKKE_VURDERT);
    }

    private Årsgrunnlag lagBeregnet(int verdi) {
        return new Årsgrunnlag(new Beløp(verdi), null, null, null, null, new Beløp(verdi));
    }

    @Test
    void skal_ikke_regulere_grunnbeløp_når_arbeidstaker_med_inntekt_under_grenseverdi() {
        var periode = BeregningsgrunnlagPeriodeEntitet.builder()
            .medBeregningsgrunnlagPeriode(STP, TIDENES_ENDE)
            .leggTilBeregningsgrunnlagAndel(BeregningsgrunnlagAndelEntitet.builder()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medAndelsnr(1L)
                .medAndelArbeidsforhold(AndelArbeidsforholdEntitet.builder().medArbeidsgiver(Arbeidsgiver.virksomhet("999999999")).build())
                .medGrunnlagPrÅr(lagBeregnet(400000)))
            .build();
        BeregningsgrunnlagEntitet bg = BeregningsgrunnlagEntitet.builder()
            .medSkjæringstidspunkt(STP)
            .leggTilBeregningsgrunnlagPeriode(periode)
            .medGrunnbeløp(GAMMEL_G)
            .build();
        Optional<BeregningsgrunnlagGrunnlagEntitet> gr = Optional.of(BeregningsgrunnlagGrunnlagBuilder.kopiere(Optional.empty())
                .medRegisterAktiviteter(AktivitetAggregatEntitet.builder()
                        .medSkjæringstidspunktOpptjening(STP)
                        .build())
                .medBeregningsgrunnlag(bg)
                .build(1L, BeregningsgrunnlagTilstand.FASTSATT));

        GrunnbeløpReguleringStatus resultat = Greguleringsstatusutleder.utledStatus(gr, NY_G, FagsakYtelseType.OMSORGSPENGER);

        assertThat(resultat).isEqualTo(GrunnbeløpReguleringStatus.IKKE_NØDVENDIG);
    }

    @Test
    void skal_regulere_grunnbeløp_når_beregnet_som_næringsdrivende() {
        var periode = BeregningsgrunnlagPeriodeEntitet.builder()
            .medBeregningsgrunnlagPeriode(STP, TIDENES_ENDE)
            .leggTilBeregningsgrunnlagAndel(BeregningsgrunnlagAndelEntitet.builder()
                .medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
                .medAndelsnr(1L)
                .medGrunnlagPrÅr(lagBeregnet(200000)))
            .build();
        BeregningsgrunnlagEntitet bg = BeregningsgrunnlagEntitet.builder()
            .medSkjæringstidspunkt(STP)
            .medGrunnbeløp(GAMMEL_G)
            .leggTilBeregningsgrunnlagPeriode(periode)
            .leggTilAktivitetstatus(BeregningsgrunnlagAktivitetStatusEntitet.builder().medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE).build())
            .build();
        Optional<BeregningsgrunnlagGrunnlagEntitet> gr = Optional.of(BeregningsgrunnlagGrunnlagBuilder.kopiere(Optional.empty())
                .medRegisterAktiviteter(AktivitetAggregatEntitet.builder()
                        .medSkjæringstidspunktOpptjening(STP)
                        .build())
                .medBeregningsgrunnlag(bg)
                .build(1L, BeregningsgrunnlagTilstand.FASTSATT));

        GrunnbeløpReguleringStatus resultat = Greguleringsstatusutleder.utledStatus(gr, NY_G, FagsakYtelseType.OMSORGSPENGER);

        assertThat(resultat).isEqualTo(GrunnbeløpReguleringStatus.NØDVENDIG);
    }

    @Test
    void skal_regulere_grunnbeløp_når_beregnet_som_militær_med_inntekt_under_minstegrense() {
        var periode = BeregningsgrunnlagPeriodeEntitet.builder()
            .medBeregningsgrunnlagPeriode(STP, TIDENES_ENDE)
            .leggTilBeregningsgrunnlagAndel(BeregningsgrunnlagAndelEntitet.builder()
                .medAktivitetStatus(AktivitetStatus.MILITÆR_ELLER_SIVIL)
                .medAndelsnr(1L)
                .medGrunnlagPrÅr(lagBeregnet(200000)))
            .build();
        BeregningsgrunnlagEntitet bg = BeregningsgrunnlagEntitet.builder()
            .medSkjæringstidspunkt(STP)
            .medGrunnbeløp(GAMMEL_G)
            .leggTilBeregningsgrunnlagPeriode(periode)
            .leggTilAktivitetstatus(BeregningsgrunnlagAktivitetStatusEntitet.builder().medAktivitetStatus(AktivitetStatus.MILITÆR_ELLER_SIVIL).build())
            .build();

        Optional<BeregningsgrunnlagGrunnlagEntitet> gr = Optional.of(BeregningsgrunnlagGrunnlagBuilder.kopiere(Optional.empty())
                .medRegisterAktiviteter(AktivitetAggregatEntitet.builder()
                        .medSkjæringstidspunktOpptjening(STP)
                        .build())
                .medBeregningsgrunnlag(bg)
                .build(1L, BeregningsgrunnlagTilstand.FASTSATT));

        GrunnbeløpReguleringStatus resultat = Greguleringsstatusutleder.utledStatus(gr, NY_G, FagsakYtelseType.OMSORGSPENGER);

        assertThat(resultat).isEqualTo(GrunnbeløpReguleringStatus.NØDVENDIG);
    }

    @Test
    void skal_regulere_grunnbeløp_når_beregnet_som_arbeid_som_blir_avkortet() {
        var periode = BeregningsgrunnlagPeriodeEntitet.builder()
            .medBeregningsgrunnlagPeriode(STP, TIDENES_ENDE)
            .leggTilBeregningsgrunnlagAndel(BeregningsgrunnlagAndelEntitet.builder()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medAndelsnr(1L)
                .medAndelArbeidsforhold(AndelArbeidsforholdEntitet.builder().medArbeidsgiver(Arbeidsgiver.virksomhet("999999999")).build())
                .medGrunnlagPrÅr(lagBeregnet(610000)))
            .build();
        BeregningsgrunnlagEntitet bg = BeregningsgrunnlagEntitet.builder()
            .medSkjæringstidspunkt(STP)
            .leggTilBeregningsgrunnlagPeriode(periode)
            .medGrunnbeløp(GAMMEL_G)
            .build();
        Optional<BeregningsgrunnlagGrunnlagEntitet> gr = Optional.of(BeregningsgrunnlagGrunnlagBuilder.kopiere(Optional.empty())
                .medRegisterAktiviteter(AktivitetAggregatEntitet.builder()
                        .medSkjæringstidspunktOpptjening(STP)
                        .build())
                .medBeregningsgrunnlag(bg)
                .build(1L, BeregningsgrunnlagTilstand.FASTSATT));

        GrunnbeløpReguleringStatus resultat = Greguleringsstatusutleder.utledStatus(gr, NY_G, FagsakYtelseType.OMSORGSPENGER);
        assertThat(resultat).isEqualTo(GrunnbeløpReguleringStatus.NØDVENDIG);
    }

}
