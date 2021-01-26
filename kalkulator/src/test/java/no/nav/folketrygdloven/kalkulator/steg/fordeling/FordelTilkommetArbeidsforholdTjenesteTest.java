package no.nav.folketrygdloven.kalkulator.steg.fordeling;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.aksjonpunkt.FordelTilkommetArbeidsforholdTjeneste;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;

class FordelTilkommetArbeidsforholdTjenesteTest {

    public static final LocalDate STP = LocalDate.now();

    @Test
    void skal_returnere_false_for_yrkesaktivitet_med_internId_og_beregningAktivitet_uten_id() {
        // Arrange
        Arbeidsgiver virksomhet = Arbeidsgiver.virksomhet("92839023");
        YrkesaktivitetDto yrkesaktivitetDto = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
                .medArbeidsgiver(virksomhet)
                .medArbeidsforholdId(InternArbeidsforholdRefDto.nyRef())
                .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
                .build();
        BeregningAktivitetAggregatDto beregningAktivitetAggregatDto = BeregningAktivitetAggregatDto.builder()
                .leggTilAktivitet(BeregningAktivitetDto.builder()
                        .medArbeidsforholdRef(InternArbeidsforholdRefDto.nullRef())
                        .medArbeidsgiver(virksomhet)
                        .medOpptjeningAktivitetType(OpptjeningAktivitetType.ARBEID)
                        .medPeriode(Intervall.fraOgMedTilOgMed(STP.minusMonths(10), STP.plusMonths(1)))
                        .build())
                .medSkjæringstidspunktOpptjening(STP)
                .build();

        // Act
        boolean erNyttArbeidsforhold = FordelTilkommetArbeidsforholdTjeneste.erNyttArbeidsforhold(yrkesaktivitetDto.getArbeidsgiver(), yrkesaktivitetDto.getArbeidsforholdRef(), beregningAktivitetAggregatDto, STP, FagsakYtelseType.FORELDREPENGER);

        // Assert
        assertThat(erNyttArbeidsforhold).isFalse();
    }

    @Test
    void skal_returnere_true_for_yrkesaktivitet_med_internId_og_beregningAktivitet_med_id() {
        // Arrange
        Arbeidsgiver virksomhet = Arbeidsgiver.virksomhet("92839023");
        YrkesaktivitetDto yrkesaktivitetDto = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
                .medArbeidsgiver(virksomhet)
                .medArbeidsforholdId(InternArbeidsforholdRefDto.nyRef())
                .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
                .build();
        BeregningAktivitetAggregatDto beregningAktivitetAggregatDto = BeregningAktivitetAggregatDto.builder()
                .leggTilAktivitet(BeregningAktivitetDto.builder()
                        .medArbeidsforholdRef(InternArbeidsforholdRefDto.nyRef())
                        .medArbeidsgiver(virksomhet)
                        .medOpptjeningAktivitetType(OpptjeningAktivitetType.ARBEID)
                        .medPeriode(Intervall.fraOgMedTilOgMed(STP.minusMonths(10), STP.plusMonths(1)))
                        .build())
                .medSkjæringstidspunktOpptjening(STP)
                .build();

        // Act
        boolean erNyttArbeidsforhold = FordelTilkommetArbeidsforholdTjeneste.erNyttArbeidsforhold(yrkesaktivitetDto.getArbeidsgiver(), yrkesaktivitetDto.getArbeidsforholdRef(), beregningAktivitetAggregatDto, STP, FagsakYtelseType.FORELDREPENGER);

        // Assert
        assertThat(erNyttArbeidsforhold).isTrue();
    }

    @Test
    void skal_returnere_true_for_yrkesaktivitet_med_internId_og_beregningAktivitet_uten_id_med_periode_etter_stp() {
        // Arrange
        Arbeidsgiver virksomhet = Arbeidsgiver.virksomhet("92839023");
        YrkesaktivitetDto yrkesaktivitetDto = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
                .medArbeidsgiver(virksomhet)
                .medArbeidsforholdId(InternArbeidsforholdRefDto.nyRef())
                .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
                .build();
        BeregningAktivitetAggregatDto beregningAktivitetAggregatDto = BeregningAktivitetAggregatDto.builder()
                .leggTilAktivitet(BeregningAktivitetDto.builder()
                        .medArbeidsforholdRef(InternArbeidsforholdRefDto.nyRef())
                        .medArbeidsgiver(virksomhet)
                        .medOpptjeningAktivitetType(OpptjeningAktivitetType.ARBEID)
                        .medPeriode(Intervall.fraOgMedTilOgMed(STP.plusDays(10), STP.plusMonths(1)))
                        .build())
                .medSkjæringstidspunktOpptjening(STP)
                .build();

        // Act
        boolean erNyttArbeidsforhold = FordelTilkommetArbeidsforholdTjeneste.erNyttArbeidsforhold(yrkesaktivitetDto.getArbeidsgiver(), yrkesaktivitetDto.getArbeidsforholdRef(), beregningAktivitetAggregatDto, STP, FagsakYtelseType.FORELDREPENGER);

        // Assert
        assertThat(erNyttArbeidsforhold).isTrue();
    }
}
