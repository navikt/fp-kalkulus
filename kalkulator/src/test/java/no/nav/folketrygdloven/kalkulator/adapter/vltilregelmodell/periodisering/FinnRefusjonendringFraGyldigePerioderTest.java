package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering;

import static no.nav.folketrygdloven.kalkulus.felles.tid.AbstractIntervall.TIDENES_ENDE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Refusjonskrav;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;

class FinnRefusjonendringFraGyldigePerioderTest {

    public static final LocalDate STP = LocalDate.now();

    @Test
    public void skal_gi_ingen_endring_i_refusjon_ved_ingen_opphør_av_ansattforhold() {
        // Arrange
        List<Refusjonskrav> refusjonerFraInntektsmelding = List.of(new Refusjonskrav(BigDecimal.TEN, STP, TIDENES_ENDE));

        // Act
        Map<LocalDate, Beløp> endringer = finnEndringer(refusjonerFraInntektsmelding, List.of(Intervall.fraOgMed(STP.minusMonths(12))));

        // Assert
        assertThat(endringer.size()).isEqualTo(0);

    }

    @Test
    public void skal_gi_ingen_endring_i_refusjon_ved_oppgitt_refusjon_i_inntektsmelding_på_startdato_av_arbeidsforhold() {
        // Arrange
        List<Refusjonskrav> refusjonerFraInntektsmelding = List.of(new Refusjonskrav(BigDecimal.TEN, STP, TIDENES_ENDE));

        // Act
        Map<LocalDate, Beløp> endringer = finnEndringer(refusjonerFraInntektsmelding, List.of(Intervall.fraOgMed(STP)));

        // Assert
        assertThat(endringer.size()).isEqualTo(0);

    }

    @Test
    public void skal_gi_endring_i_refusjon_ved_opphør_og_start() {
        // Arrange
        List<Refusjonskrav> refusjonerFraInntektsmelding = List.of(new Refusjonskrav(BigDecimal.TEN, STP, TIDENES_ENDE));
        List<Intervall> perioderMedUtbetaling = List.of(
                Intervall.fraOgMedTilOgMed(STP.minusMonths(12), STP.plusDays(2)),
                Intervall.fraOgMed(STP.plusDays(4)));

        // Act
        Map<LocalDate, Beløp> endringer = finnEndringer(refusjonerFraInntektsmelding, perioderMedUtbetaling);

        // Assert
        assertThat(endringer.size()).isEqualTo(2);
    }

    @Test
    public void skal_gi_ingen_endring_i_refusjon_ved_opphør_og_start_uten_dager_mellom() {
        // Arrange
        List<Refusjonskrav> refusjonerFraInntektsmelding = List.of(new Refusjonskrav(BigDecimal.TEN, STP, TIDENES_ENDE));
        List<Intervall> perioderMedUtbetaling = List.of(
                Intervall.fraOgMedTilOgMed(STP.minusMonths(12), STP.plusDays(2)),
                Intervall.fraOgMed(STP.plusDays(3)));

        // Act
        Map<LocalDate, Beløp> endringer = finnEndringer(refusjonerFraInntektsmelding, perioderMedUtbetaling);

        // Assert
        assertThat(endringer.size()).isEqualTo(0);
    }

    private Map<LocalDate, Beløp> finnEndringer(List<Refusjonskrav> refusjonerFraInntektsmelding, List<Intervall> perioderMedUtbetaling) {
        return new FinnRefusjonendringFraGyldigePerioder(perioderMedUtbetaling, refusjonerFraInntektsmelding, STP)
                .finnEndringerIRefusjon();
    }

}
