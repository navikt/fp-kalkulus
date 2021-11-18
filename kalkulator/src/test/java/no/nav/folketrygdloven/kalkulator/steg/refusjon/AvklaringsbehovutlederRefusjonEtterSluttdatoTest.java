package no.nav.folketrygdloven.kalkulator.steg.refusjon;

import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;
import no.nav.folketrygdloven.kalkulus.kodeverk.Utfall;

class AvklaringsbehovutlederRefusjonEtterSluttdatoTest {
    private static final UUID REFERANSE = UUID.randomUUID();
    private static final LocalDate STP = LocalDate.of(2020,1,1);
    private BeregningsgrunnlagDto.Builder grunnlagBuilder = BeregningsgrunnlagDto.builder().medSkjæringstidspunkt(STP);
    private List<YrkesaktivitetDto> yrkesaktiviteter = new ArrayList<>();

    @Test
    public void skal_ikke_slå_ut_på_løpende_arbeidsforhold_som_er_aktivt() {
        // Arrange
        Arbeidsgiver ag = Arbeidsgiver.virksomhet("333");
        lagJobb(ag, InternArbeidsforholdRefDto.nullRef(), lagAnsettelse(STP.minusDays(100), null));
        lagBGPeriode(STP, null, lagBGAndel(ag, InternArbeidsforholdRefDto.nullRef(), 300000));

        // Act
        boolean slårUt = kjørUtleder(TIDENES_ENDE, STP.minusDays(100));

        // Assert
        assertThat(slårUt).isFalse();
    }

    @Test
    public void skal_slå_ut_på_avsluttet_arbeidsforhold_som_overlapper_refusjon_før_behandlingstidspunkt() {
        // Arrange
        Arbeidsgiver ag = Arbeidsgiver.virksomhet("333");
        lagJobb(ag, InternArbeidsforholdRefDto.nullRef(), lagAnsettelse(STP.minusDays(100), STP.plusDays(30)));
        lagBGPeriode(STP, null, lagBGAndel(ag, InternArbeidsforholdRefDto.nullRef(), 300000));

        // Act
        boolean slårUt = kjørUtleder(TIDENES_ENDE, STP.plusDays(31));

        // Assert
        assertThat(slårUt).isTrue();
    }

    @Test
    public void skal_ikke_slå_ut_når_arbeidsforhold_som_er_avsluttet_ikke_har_ref_etter_sluttdato() {
        // Arrange
        Arbeidsgiver ag = Arbeidsgiver.virksomhet("333");
        Arbeidsgiver ag2 = Arbeidsgiver.virksomhet("444");
        lagJobb(ag, InternArbeidsforholdRefDto.nullRef(), lagAnsettelse(STP.minusDays(100), null));
        lagJobb(ag2, InternArbeidsforholdRefDto.nullRef(), lagAnsettelse(STP.minusDays(100), STP.plusDays(30)));
        lagBGPeriode(STP, STP.plusDays(30), lagBGAndel(ag, InternArbeidsforholdRefDto.nullRef(), 300000), lagBGAndel(ag2, InternArbeidsforholdRefDto.nullRef(), 300000));
        lagBGPeriode(STP.plusDays(31), null, lagBGAndel(ag, InternArbeidsforholdRefDto.nullRef(), 300000), lagBGAndel(ag2, InternArbeidsforholdRefDto.nullRef(), 0));

        // Act
        boolean slårUt = kjørUtleder(TIDENES_ENDE, STP.plusDays(100));

        // Assert
        assertThat(slårUt).isFalse();
    }

    @Test
    public void skal_slå_ut_når_arbeidsforhold_som_er_avsluttet_har_ref_etter_men_ikke_før_sluttdato() {
        // Arrange
        Arbeidsgiver ag = Arbeidsgiver.virksomhet("333");
        Arbeidsgiver ag2 = Arbeidsgiver.virksomhet("444");
        lagJobb(ag, InternArbeidsforholdRefDto.nullRef(), lagAnsettelse(STP.minusDays(100), null));
        lagJobb(ag2, InternArbeidsforholdRefDto.nullRef(), lagAnsettelse(STP.minusDays(100), STP.plusDays(30)));
        lagBGPeriode(STP, STP.plusDays(30), lagBGAndel(ag, InternArbeidsforholdRefDto.nullRef(), 300000), lagBGAndel(ag2, InternArbeidsforholdRefDto.nullRef(), 0));
        lagBGPeriode(STP.plusDays(31), null, lagBGAndel(ag, InternArbeidsforholdRefDto.nullRef(), 300000), lagBGAndel(ag2, InternArbeidsforholdRefDto.nullRef(), 300000));

        // Act
        boolean slårUt = kjørUtleder(TIDENES_ENDE, STP.plusDays(100));

        // Assert
        assertThat(slårUt).isTrue();
    }

    @Test
    public void skal_slå_ut_når_det_finnes_periode_med_refusjon_etter_arbfor_før_behandlingstidspunkt() {
        // Arrange
        Arbeidsgiver ag = Arbeidsgiver.virksomhet("333");
        lagJobb(ag, InternArbeidsforholdRefDto.nullRef(), lagAnsettelse(STP.minusDays(100), STP));
        lagBGPeriode(STP, null, lagBGAndel(ag, InternArbeidsforholdRefDto.nullRef(), 300000));

        // Act
        boolean slårUt = kjørUtleder(TIDENES_ENDE, STP.plusDays(1));

        // Assert
        assertThat(slårUt).isTrue();
    }

    @Test
    public void skal_ikk_slå_ut_når_det_finnes_periode_med_refusjon_etter_arbfor_etter_behandlingstidspunkt() {
        // Arrange
        Arbeidsgiver ag = Arbeidsgiver.virksomhet("333");
        lagJobb(ag, InternArbeidsforholdRefDto.nullRef(), lagAnsettelse(STP.minusDays(100), STP));
        lagBGPeriode(STP, null, lagBGAndel(ag, InternArbeidsforholdRefDto.nullRef(), 300000));

        // Act
        boolean slårUt = kjørUtleder(TIDENES_ENDE, STP.minusDays(1));

        // Assert
        assertThat(slårUt).isFalse();
    }


    private boolean kjørUtleder(LocalDate sisteUttak, LocalDate behandlingstidspunkt) {
        return AvklaringsbehovutlederRefusjonEtterSluttdato.harRefusjonEtterSisteDatoIArbeidsforhold(yrkesaktiviteter,
                REFERANSE,
                Optional.of(sisteUttak),
                Optional.of(behandlingstidspunkt),
                grunnlagBuilder.build());
    }

    private BeregningsgrunnlagPrStatusOgAndelDto.Builder lagBGAndel(Arbeidsgiver ag, InternArbeidsforholdRefDto ref, int refusjonPrÅr) {
        BeregningsgrunnlagPrStatusOgAndelDto.Builder andelBuilder = BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(ag == null ? AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE : AktivitetStatus.ARBEIDSTAKER);
        if (ag != null) {
            BGAndelArbeidsforholdDto.Builder arbforBuilder = BGAndelArbeidsforholdDto.builder()
                    .medRefusjonskravPrÅr(BigDecimal.valueOf(refusjonPrÅr), Utfall.GODKJENT)
                    .medArbeidsforholdRef(ref)
                    .medArbeidsgiver(ag);
            andelBuilder.medBGAndelArbeidsforhold(arbforBuilder);
        }
        return andelBuilder;
    }

    private void lagBGPeriode(LocalDate fom, LocalDate tom, BeregningsgrunnlagPrStatusOgAndelDto.Builder... andeler) {
        BeregningsgrunnlagPeriodeDto.Builder bgPeriodeBuilder = BeregningsgrunnlagPeriodeDto.builder().medBeregningsgrunnlagPeriode(fom, tom);
        List<BeregningsgrunnlagPrStatusOgAndelDto.Builder> bgAndeler = Arrays.asList(andeler);
        bgAndeler.forEach(bgPeriodeBuilder::leggTilBeregningsgrunnlagPrStatusOgAndel);
        grunnlagBuilder.leggTilBeregningsgrunnlagPeriode(bgPeriodeBuilder);
    }

    private void lagJobb(Arbeidsgiver ag, InternArbeidsforholdRefDto ref, AktivitetsAvtaleDtoBuilder... avtaler) {
        YrkesaktivitetDtoBuilder builder = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
                .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
                .medArbeidsforholdId(ref)
                .medArbeidsgiver(ag);
        Arrays.asList(avtaler).forEach(builder::leggTilAktivitetsAvtale);
        yrkesaktiviteter.add(builder.build());
    }

    private AktivitetsAvtaleDtoBuilder lagAnsettelse(LocalDate fom, LocalDate tom) {
        return tom == null
                ? AktivitetsAvtaleDtoBuilder.ny().medPeriode(Intervall.fraOgMed(fom)).medErAnsettelsesPeriode(true)
                : AktivitetsAvtaleDtoBuilder.ny().medPeriode(Intervall.fraOgMedTilOgMed(fom, tom)).medErAnsettelsesPeriode(true);
    }


}
