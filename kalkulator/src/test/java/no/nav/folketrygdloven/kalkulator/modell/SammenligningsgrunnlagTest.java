package no.nav.folketrygdloven.kalkulator.modell;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.Before;
import org.junit.Test;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.SammenligningsgrunnlagDto;

public class SammenligningsgrunnlagTest {
    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now();
    private static final BigDecimal RAPPORTERT_PR_ÅR = BigDecimal.valueOf(400000d);
    private static final BigDecimal AVVIK_PROMILLE = BigDecimal.valueOf(240L);
    private final LocalDate PERIODE_FOM = LocalDate.now();
    private final LocalDate PERIODE_TOM = LocalDate.now().plusWeeks(6);

    private BeregningsgrunnlagDto beregningsgrunnlag;
    private SammenligningsgrunnlagDto sammenligningsgrunnlag;

    @Before
    public void setup() {
        beregningsgrunnlag = lagBeregningsgrunnlag();
        sammenligningsgrunnlag = lagSammenligningsgrunnlag();
    }

    @Test
    public void skal_bygge_instans_med_påkrevde_felter() {
        assertThat(sammenligningsgrunnlag.getBeregningsgrunnlag()).isEqualTo(beregningsgrunnlag);
        assertThat(sammenligningsgrunnlag.getSammenligningsperiodeFom()).isEqualTo(PERIODE_FOM);
        assertThat(sammenligningsgrunnlag.getSammenligningsperiodeTom()).isEqualTo(PERIODE_TOM);
        assertThat(sammenligningsgrunnlag.getRapportertPrÅr()).isEqualTo(RAPPORTERT_PR_ÅR);
    }

    @Test
    public void skal_ikke_bygge_instans_hvis_mangler_påkrevde_felter() {
        SammenligningsgrunnlagDto.Builder sammenligningsgrunnlagBuilder = SammenligningsgrunnlagDto.builder();
        try {
            sammenligningsgrunnlagBuilder.build(null);
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).contains("beregningsgrunnlag");
        }

        try {
            sammenligningsgrunnlagBuilder.build(beregningsgrunnlag);
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).contains("sammenligningsperiodePeriode");
        }

        try {
            sammenligningsgrunnlagBuilder.medSammenligningsperiode(PERIODE_FOM, null);
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).contains("Til og med dato må være satt");
        }

        try {
            sammenligningsgrunnlagBuilder.medSammenligningsperiode(PERIODE_FOM, PERIODE_TOM);
        } catch (IllegalArgumentException e) {
            fail(e.getMessage());
        }

        try {
            sammenligningsgrunnlagBuilder.build(beregningsgrunnlag);
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).contains("rapportertPrÅr");
        }
    }

    @Test
    public void skal_håndtere_null_this_feilKlasse_i_equals() {
        assertThat(sammenligningsgrunnlag).isNotEqualTo(null);
        assertThat(sammenligningsgrunnlag).isNotEqualTo("blabla");
        assertThat(sammenligningsgrunnlag).isEqualTo(sammenligningsgrunnlag);
    }

    @Test
    public void skal_ha_refleksiv_equalsOgHashCode() {
        SammenligningsgrunnlagDto sammenligningsgrunnlag2 = lagSammenligningsgrunnlag();

        assertThat(sammenligningsgrunnlag).isEqualTo(sammenligningsgrunnlag2);
        assertThat(sammenligningsgrunnlag2).isEqualTo(sammenligningsgrunnlag);
        assertThat(sammenligningsgrunnlag.hashCode()).isEqualTo(sammenligningsgrunnlag2.hashCode());
        assertThat(sammenligningsgrunnlag2.hashCode()).isEqualTo(sammenligningsgrunnlag.hashCode());

        SammenligningsgrunnlagDto.Builder sammenligningsgrunnlagBuilder = lagSammenligningsgrunnlagBuilder();
        sammenligningsgrunnlagBuilder.medSammenligningsperiode(LocalDate.now().minusDays(1), PERIODE_TOM);
        sammenligningsgrunnlag2 = sammenligningsgrunnlagBuilder.build(beregningsgrunnlag);
        assertThat(sammenligningsgrunnlag).isNotEqualTo(sammenligningsgrunnlag2);
        assertThat(sammenligningsgrunnlag2).isNotEqualTo(sammenligningsgrunnlag);
        assertThat(sammenligningsgrunnlag.hashCode()).isNotEqualTo(sammenligningsgrunnlag2.hashCode());
        assertThat(sammenligningsgrunnlag2.hashCode()).isNotEqualTo(sammenligningsgrunnlag.hashCode());
    }

    @Test
    public void skal_bruke_beregningsgrunnlag_i_equalsOgHashCode() {
        SammenligningsgrunnlagDto.Builder builder = lagSammenligningsgrunnlagBuilder();
        SammenligningsgrunnlagDto sammenligningsgrunnlag2 = builder.build(beregningsgrunnlag);

        assertThat(sammenligningsgrunnlag).isEqualTo(sammenligningsgrunnlag2);
        assertThat(sammenligningsgrunnlag.hashCode()).isEqualTo(sammenligningsgrunnlag2.hashCode());

        BeregningsgrunnlagDto beregningsgrunnlag2 = lagBeregningsgrunnlagMedSkjæringstidspunkt(LocalDate.now().plusDays(4));
        sammenligningsgrunnlag2 = builder.build(beregningsgrunnlag2);

        assertThat(sammenligningsgrunnlag).isNotEqualTo(sammenligningsgrunnlag2);
        assertThat(sammenligningsgrunnlag.hashCode()).isNotEqualTo(sammenligningsgrunnlag2.hashCode());
    }

    @Test
    public void skal_bruke_sammenligningsgrunnlagFom_i_equalsOgHashCode() {
        SammenligningsgrunnlagDto sammenligningsgrunnlag2 = lagSammenligningsgrunnlag();

        assertThat(sammenligningsgrunnlag).isEqualTo(sammenligningsgrunnlag2);
        assertThat(sammenligningsgrunnlag.hashCode()).isEqualTo(sammenligningsgrunnlag2.hashCode());

        SammenligningsgrunnlagDto.Builder sammenligningsgrunnlagBuilder = lagSammenligningsgrunnlagBuilder();
        sammenligningsgrunnlagBuilder.medSammenligningsperiode(LocalDate.now().minusDays(1), PERIODE_TOM);
        sammenligningsgrunnlag2 = sammenligningsgrunnlagBuilder.build(beregningsgrunnlag);

        assertThat(sammenligningsgrunnlag).isNotEqualTo(sammenligningsgrunnlag2);
        assertThat(sammenligningsgrunnlag.hashCode()).isNotEqualTo(sammenligningsgrunnlag2.hashCode());
    }

    @Test
    public void skal_bruke_sammenligningsgrunnlagTom_i_equalsOgHashCode() {
        SammenligningsgrunnlagDto sammenligningsgrunnlag2 = lagSammenligningsgrunnlag();

        assertThat(sammenligningsgrunnlag).isEqualTo(sammenligningsgrunnlag2);
        assertThat(sammenligningsgrunnlag.hashCode()).isEqualTo(sammenligningsgrunnlag2.hashCode());

        SammenligningsgrunnlagDto.Builder sammenligningsgrunnlagBuilder = lagSammenligningsgrunnlagBuilder();
        sammenligningsgrunnlagBuilder.medSammenligningsperiode(PERIODE_FOM, LocalDate.now().plusWeeks(5));
        sammenligningsgrunnlag2 = sammenligningsgrunnlagBuilder.build(beregningsgrunnlag);

        assertThat(sammenligningsgrunnlag).isNotEqualTo(sammenligningsgrunnlag2);
        assertThat(sammenligningsgrunnlag.hashCode()).isNotEqualTo(sammenligningsgrunnlag2.hashCode());
    }

    @Test
    public void skal_bruke_rapportertPrÅr_i_equalsOgHashCode() {
        SammenligningsgrunnlagDto sammenligningsgrunnlag2 = lagSammenligningsgrunnlag();

        assertThat(sammenligningsgrunnlag).isEqualTo(sammenligningsgrunnlag2);
        assertThat(sammenligningsgrunnlag.hashCode()).isEqualTo(sammenligningsgrunnlag2.hashCode());

        SammenligningsgrunnlagDto.Builder sammenligningsgrunnlagBuilder = lagSammenligningsgrunnlagBuilder();
        sammenligningsgrunnlagBuilder.medRapportertPrÅr(RAPPORTERT_PR_ÅR.add(BigDecimal.valueOf(1)));
        sammenligningsgrunnlag2 = sammenligningsgrunnlagBuilder.build(beregningsgrunnlag);

        assertThat(sammenligningsgrunnlag).isNotEqualTo(sammenligningsgrunnlag2);
        assertThat(sammenligningsgrunnlag.hashCode()).isNotEqualTo(sammenligningsgrunnlag2.hashCode());
    }

    private SammenligningsgrunnlagDto lagSammenligningsgrunnlag() {
        return lagSammenligningsgrunnlagBuilder().build(beregningsgrunnlag);
    }

    private SammenligningsgrunnlagDto.Builder lagSammenligningsgrunnlagBuilder() {
        return SammenligningsgrunnlagDto.builder()
            .medSammenligningsperiode(PERIODE_FOM, PERIODE_TOM)
            .medRapportertPrÅr(RAPPORTERT_PR_ÅR)
            .medAvvikPromilleNy(AVVIK_PROMILLE);
    }

    private static BeregningsgrunnlagDto lagBeregningsgrunnlag() {
        return lagBeregningsgrunnlagMedSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT);
    }

    private static BeregningsgrunnlagDto lagBeregningsgrunnlagMedSkjæringstidspunkt(LocalDate skjæringstidspunkt) {
        return BeregningsgrunnlagDto.builder().medSkjæringstidspunkt(skjæringstidspunkt).build();
    }
}
