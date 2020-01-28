package no.nav.folketrygdloven.kalkulator.rest.fakta;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetRestDto;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulator.modell.typer.AktørId;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.ArbeidsgiverMedNavn;
import no.nav.folketrygdloven.kalkulator.rest.dto.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;

public class MapBeregningAktivitetDtoTest {
    private static final AktørId AKTØRID_1 = AktørId.dummy();
    private static final AktørId AKTØRID_2 = AktørId.dummy();

    @Test
    public void nyAktivitetIDetteGrunnlaget() {
        // Arrange
        BeregningAktivitetRestDto beregningAktivitet = lagAktivitet(AKTØRID_1);
        List<BeregningAktivitetRestDto> saksbehandledeAktiviteter = List.of();

        // Act
        BeregningAktivitetDto dto = MapBeregningAktivitetDto.mapBeregningAktivitet(beregningAktivitet, saksbehandledeAktiviteter,
            Optional.empty());

        // Assert
        assertThat(dto.getSkalBrukes()).isNull();
    }

    @Test
    public void aldriSaksbehandletEllerIngenAktiviteterIForrigeSaksbehandlet() {
        // Arrange
        BeregningAktivitetRestDto beregningAktivitet = lagAktivitet(AKTØRID_1);
        List<BeregningAktivitetRestDto> saksbehandledeAktiviteter = List.of();

        // Act
        BeregningAktivitetDto dto = MapBeregningAktivitetDto.mapBeregningAktivitet(beregningAktivitet, saksbehandledeAktiviteter,
            Optional.empty());

        // Assert
        assertThat(dto.getSkalBrukes()).isNull();
    }

    @Test
    public void saksbehandletIDetteGrunnlagetSattTilBenytt() {
        // Arrange
        BeregningAktivitetRestDto beregningAktivitet = lagAktivitet(AKTØRID_1);
        List<BeregningAktivitetRestDto> saksbehandledeAktiviteter = List.of(lagAktivitet(AKTØRID_1));

        // Act
        BeregningAktivitetDto dto = MapBeregningAktivitetDto.mapBeregningAktivitet(beregningAktivitet, saksbehandledeAktiviteter,
            Optional.empty());

        // Assert
        assertThat(dto.getSkalBrukes()).isTrue();
    }

    @Test
    public void saksbehandletIDetteGrunnlagetSattTilIkkeBenytt() {
        // Arrange
        BeregningAktivitetRestDto beregningAktivitet = lagAktivitet(AKTØRID_1);
        List<BeregningAktivitetRestDto> saksbehandledeAktiviteter = List.of(lagAktivitet(AKTØRID_2));

        // Act
        BeregningAktivitetDto dto = MapBeregningAktivitetDto.mapBeregningAktivitet(beregningAktivitet, saksbehandledeAktiviteter,
            Optional.empty());

        // Assert
        assertThat(dto.getSkalBrukes()).isFalse();
    }

    private BeregningAktivitetRestDto lagAktivitet(AktørId aktørId) {
        return BeregningAktivitetRestDto.builder()
            .medPeriode(Intervall.fraOgMed(LocalDate.now()))
            .medOpptjeningAktivitetType(OpptjeningAktivitetType.ARBEID)
            .medArbeidsgiver(ArbeidsgiverMedNavn.fra(aktørId))
            .build();
    }

}
