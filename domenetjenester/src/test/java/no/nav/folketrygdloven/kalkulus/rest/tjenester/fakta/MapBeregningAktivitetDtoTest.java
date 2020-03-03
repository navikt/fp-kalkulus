package no.nav.folketrygdloven.kalkulus.rest.tjenester.fakta;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulator.modell.typer.AktørId;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;

public class MapBeregningAktivitetDtoTest {
    private static final AktørId AKTØRID_1 = AktørId.dummy();
    private static final AktørId AKTØRID_2 = AktørId.dummy();

    @Test
    public void nyAktivitetIDetteGrunnlaget() {
        // Arrange
        BeregningAktivitetDto beregningAktivitet = lagAktivitet(AKTØRID_1);
        List<BeregningAktivitetDto> saksbehandledeAktiviteter = List.of();

        // Act
        no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.BeregningAktivitetDto dto = MapBeregningAktivitetDto.mapBeregningAktivitet(beregningAktivitet, saksbehandledeAktiviteter,
            Optional.empty(), List.of());

        // Assert
        assertThat(dto.getSkalBrukes()).isNull();
    }

    @Test
    public void aldriSaksbehandletEllerIngenAktiviteterIForrigeSaksbehandlet() {
        // Arrange
        BeregningAktivitetDto beregningAktivitet = lagAktivitet(AKTØRID_1);
        List<BeregningAktivitetDto> saksbehandledeAktiviteter = List.of();

        // Act
        no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.BeregningAktivitetDto dto = MapBeregningAktivitetDto.mapBeregningAktivitet(beregningAktivitet, saksbehandledeAktiviteter,
            Optional.empty(), List.of());

        // Assert
        assertThat(dto.getSkalBrukes()).isNull();
    }

    @Test
    public void saksbehandletIDetteGrunnlagetSattTilBenytt() {
        // Arrange
        BeregningAktivitetDto beregningAktivitet = lagAktivitet(AKTØRID_1);
        List<BeregningAktivitetDto> saksbehandledeAktiviteter = List.of(lagAktivitet(AKTØRID_1));

        // Act
        no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.BeregningAktivitetDto dto = MapBeregningAktivitetDto.mapBeregningAktivitet(beregningAktivitet, saksbehandledeAktiviteter,
            Optional.empty(), List.of());

        // Assert
        assertThat(dto.getSkalBrukes()).isTrue();
    }

    @Test
    public void saksbehandletIDetteGrunnlagetSattTilIkkeBenytt() {
        // Arrange
        BeregningAktivitetDto beregningAktivitet = lagAktivitet(AKTØRID_1);
        List<BeregningAktivitetDto> saksbehandledeAktiviteter = List.of(lagAktivitet(AKTØRID_2));

        // Act
        no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.BeregningAktivitetDto dto = MapBeregningAktivitetDto.mapBeregningAktivitet(beregningAktivitet, saksbehandledeAktiviteter,
            Optional.empty(), List.of());

        // Assert
        assertThat(dto.getSkalBrukes()).isFalse();
    }

    private BeregningAktivitetDto lagAktivitet(AktørId aktørId) {
        return BeregningAktivitetDto.builder()
            .medPeriode(Intervall.fraOgMed(LocalDate.now()))
            .medOpptjeningAktivitetType(OpptjeningAktivitetType.ARBEID)
            .medArbeidsgiver(Arbeidsgiver.fra(aktørId))
            .build();
    }

}
