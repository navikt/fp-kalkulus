package no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.refusjonskravgyldighet;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.felles.HarYrkesaktivitetInnsendtRefusjonForSent;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.RefusjonskravDatoDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;

class HarYrkesaktivitetInnsendtRefusjonForSentTest {

    public static final Arbeidsgiver VIRKSOMHET = Arbeidsgiver.virksomhet("786284722");
    public static final InternArbeidsforholdRefDto ARBEIDSFORHOLD_ID = InternArbeidsforholdRefDto.nyRef();
    public static final YrkesaktivitetDto YRKESAKTIVITET = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
            .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
            .medArbeidsgiver(VIRKSOMHET)
            .medArbeidsforholdId(ARBEIDSFORHOLD_ID)
            .build();

    @Test
    void skal_returnere_true_for_refusjonskrav_som_har_kommet_inn_en_dag_for_sent() {
        // Arrange
        RefusjonskravDatoDto refusjonskravdato = new RefusjonskravDatoDto(VIRKSOMHET, LocalDate.of(2019, 9, 30), LocalDate.of(2020, 1, 15), true);
        LocalDate stp = LocalDate.of(2019, 9, 30);
        BeregningAktivitetAggregatDto gjeldendeAktiviteter = lagAktivitetPåStp(stp);

        // Act
        boolean vurder = HarYrkesaktivitetInnsendtRefusjonForSent.vurder(refusjonskravdato, YRKESAKTIVITET, gjeldendeAktiviteter, stp);

        // Assert
        assertThat(vurder).isTrue();

    }

    @Test
    void skal_returnere_false_for_refusjonskrav_som_har_kommet_inn_på_fristen() {
        // Arrange
        RefusjonskravDatoDto refusjonskravdato = new RefusjonskravDatoDto(VIRKSOMHET, LocalDate.of(2019, 10, 1), LocalDate.of(2020, 1, 15), false);
        LocalDate stp = LocalDate.of(2019, 9, 30);
        BeregningAktivitetAggregatDto gjeldendeAktiviteter = lagAktivitetPåStp(stp);

        // Act
        boolean vurder = HarYrkesaktivitetInnsendtRefusjonForSent.vurder(refusjonskravdato, YRKESAKTIVITET, gjeldendeAktiviteter, stp);

        // Assert
        assertThat(vurder).isFalse();
    }


    @Test
    void skal_returnere_false_for_refusjonskrav_som_har_kommet_inn_siste_dag_i_måneden() {
        // Arrange
        RefusjonskravDatoDto refusjonskravdato = new RefusjonskravDatoDto(VIRKSOMHET, LocalDate.of(2019, 9, 1), LocalDate.of(2019, 12, 31), true);
        LocalDate stp = LocalDate.of(2019, 9, 1);
        BeregningAktivitetAggregatDto gjeldendeAktiviteter = lagAktivitetPåStp(stp);

        // Act
        boolean vurder = HarYrkesaktivitetInnsendtRefusjonForSent.vurder(refusjonskravdato, YRKESAKTIVITET, gjeldendeAktiviteter, stp);

        // Assert
        assertThat(vurder).isFalse();
    }


    @Test
    void skal_returnere_false_for_refusjonskrav_som_har_kommet_inn_første_dag_i_måneden() {
        // Arrange
        RefusjonskravDatoDto refusjonskravdato = new RefusjonskravDatoDto(VIRKSOMHET, LocalDate.of(2019, 9, 1), LocalDate.of(2019, 12, 1), true);
        LocalDate stp = LocalDate.of(2019, 9, 1);
        BeregningAktivitetAggregatDto gjeldendeAktiviteter = lagAktivitetPåStp(stp);

        // Act
        boolean vurder = HarYrkesaktivitetInnsendtRefusjonForSent.vurder(refusjonskravdato, YRKESAKTIVITET, gjeldendeAktiviteter, stp);

        // Assert
        assertThat(vurder).isFalse();
    }


    @Test
    void skal_returnere_true_for_refusjonskrav_som_har_kommet_inn_med_refusjons_fra_start_og_oppgitt_startdato_ulik_skjæringstidspunkt() {
        // Arrange
        RefusjonskravDatoDto refusjonskravdato = new RefusjonskravDatoDto(VIRKSOMHET, LocalDate.of(2019, 10, 1), LocalDate.of(2020, 1, 1), true);
        LocalDate stp = LocalDate.of(2019, 9, 1);
        BeregningAktivitetAggregatDto gjeldendeAktiviteter = lagAktivitetPåStp(stp);

        // Act
        boolean vurder = HarYrkesaktivitetInnsendtRefusjonForSent.vurder(refusjonskravdato, YRKESAKTIVITET, gjeldendeAktiviteter, stp);

        // Assert
        assertThat(vurder).isTrue();
    }

    @Test
    void skal_returnere_false_for_refusjonskrav_for_nytt_arbeidsforhold_som_har_kommet_inn_med_refusjons_fra_start_og_oppgitt_startdato_ulik_skjæringstidspunkt() {
        // Arrange
        RefusjonskravDatoDto refusjonskravdato = new RefusjonskravDatoDto(VIRKSOMHET, LocalDate.of(2019, 10, 1), LocalDate.of(2020, 1, 1), true);
        LocalDate stp = LocalDate.of(2019, 9, 1);
        BeregningAktivitetAggregatDto gjeldendeAktiviteter = lagAktivitetEtterStp(stp);

        // Act
        boolean vurder = HarYrkesaktivitetInnsendtRefusjonForSent.vurder(refusjonskravdato, YRKESAKTIVITET, gjeldendeAktiviteter, stp);

        // Assert
        assertThat(vurder).isFalse();
    }


    private BeregningAktivitetAggregatDto lagAktivitetEtterStp(LocalDate stp) {
        return BeregningAktivitetAggregatDto.builder()
                .leggTilAktivitet(BeregningAktivitetDto.builder()
                        .medArbeidsforholdRef(InternArbeidsforholdRefDto.nullRef())
                        .medArbeidsgiver(VIRKSOMHET)
                        .medOpptjeningAktivitetType(OpptjeningAktivitetType.ARBEID)
                        .medPeriode(Intervall.fraOgMedTilOgMed(stp.plusMonths(1), stp.plusMonths(3)))
                        .build())
                .medSkjæringstidspunktOpptjening(stp)
                .build();
    }

    private BeregningAktivitetAggregatDto lagAktivitetPåStp(LocalDate stp) {
        return BeregningAktivitetAggregatDto.builder()
                .leggTilAktivitet(BeregningAktivitetDto.builder()
                        .medArbeidsforholdRef(InternArbeidsforholdRefDto.nullRef())
                        .medArbeidsgiver(VIRKSOMHET)
                        .medOpptjeningAktivitetType(OpptjeningAktivitetType.ARBEID)
                        .medPeriode(Intervall.fraOgMedTilOgMed(stp.minusMonths(10), stp.plusMonths(3)))
                        .build())
                .medSkjæringstidspunktOpptjening(stp)
                .build();
    }

}
