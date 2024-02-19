package no.nav.folketrygdloven.kalkulus.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittFrilansDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittFrilansInntektDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittOpptjeningDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittOpptjeningDtoBuilder;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulator.ytelse.frisinn.FrisinnPeriode;
import no.nav.folketrygdloven.kalkulus.beregning.v1.FrisinnGrunnlag;
import no.nav.folketrygdloven.kalkulus.typer.Beløp;

class MapGammeltFrisinngrunnlagTest {


    @Test
    public void sn_uten_fl() {
        // Arrange
        FrisinnGrunnlag frisinnGrunnlag = new FrisinnGrunnlag(false, true);
        Intervall april = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 4, 1), LocalDate.of(2020, 4, 30));
        var månedsinntekt = Beløp.fra(30_000);
        List<OppgittOpptjeningDtoBuilder.EgenNæringBuilder> oppgittNæring = List.of(lagOppgittInntekt(april, månedsinntekt));
        OppgittOpptjeningDto oo = OppgittOpptjeningDtoBuilder.ny().leggTilEgneNæringer(oppgittNæring).build();

        // Act
        List<FrisinnPeriode> resultat = MapGammeltFrisinngrunnlag.map(frisinnGrunnlag, Optional.of(oo));

        // Assert
        assertThat(resultat).hasSize(1);
        assertThat(resultat.get(0)).isEqualToComparingFieldByField(forventet(april, false, true));

    }

    @Test
    public void fl_uten_sn() {
        FrisinnGrunnlag frisinnGrunnlag = new FrisinnGrunnlag(true, false);
        Intervall april = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 4, 1), LocalDate.of(2020, 4, 30));
        var månedsinntekt = Beløp.fra(15_000);
        List<OppgittFrilansInntektDto> oppgittFrilansInntekt = List.of(lagOppgittFrilansInntekt(april, månedsinntekt));
        OppgittOpptjeningDto oo = OppgittOpptjeningDtoBuilder.ny()
                .leggTilFrilansOpplysninger(new OppgittFrilansDto(false, oppgittFrilansInntekt))
                .build();

        // Act
        List<FrisinnPeriode> resultat = MapGammeltFrisinngrunnlag.map(frisinnGrunnlag, Optional.of(oo));

        // Assert
        assertThat(resultat).hasSize(1);
        assertThat(resultat.get(0)).isEqualToComparingFieldByField(forventet(april, true, false));
    }

    @Test
    public void snfl_søker_kun_sn() {
        FrisinnGrunnlag frisinnGrunnlag = new FrisinnGrunnlag(false, true);
        Intervall april = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 4, 1), LocalDate.of(2020, 4, 30));
        var månedsinntekt = Beløp.fra(15_000);
        List<OppgittFrilansInntektDto> oppgittFrilansInntekt = List.of(lagOppgittFrilansInntekt(april, månedsinntekt));
        List<OppgittOpptjeningDtoBuilder.EgenNæringBuilder> oppgittNæring = List.of(lagOppgittInntekt(april, månedsinntekt));
        OppgittOpptjeningDto oo = OppgittOpptjeningDtoBuilder.ny()
                .leggTilEgneNæringer(oppgittNæring)
                .leggTilFrilansOpplysninger(new OppgittFrilansDto(false, oppgittFrilansInntekt))
                .build();

        // Act
        List<FrisinnPeriode> resultat = MapGammeltFrisinngrunnlag.map(frisinnGrunnlag, Optional.of(oo));

        // Assert
        assertThat(resultat).hasSize(1);
        assertThat(resultat.get(0)).isEqualToComparingFieldByField(forventet(april, false, true));
    }

    @Test
    public void snfl_søker_kun_fl() {
        FrisinnGrunnlag frisinnGrunnlag = new FrisinnGrunnlag(true, false);
        Intervall april = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 4, 1), LocalDate.of(2020, 4, 30));
        var månedsinntekt = Beløp.fra(15_000);
        List<OppgittFrilansInntektDto> oppgittFrilansInntekt = List.of(lagOppgittFrilansInntekt(april, månedsinntekt));
        List<OppgittOpptjeningDtoBuilder.EgenNæringBuilder> oppgittNæring = List.of(lagOppgittInntekt(april, månedsinntekt));
        OppgittOpptjeningDto oo = OppgittOpptjeningDtoBuilder.ny()
                .leggTilEgneNæringer(oppgittNæring)
                .leggTilFrilansOpplysninger(new OppgittFrilansDto(false, oppgittFrilansInntekt))
                .build();

        // Act
        List<FrisinnPeriode> resultat = MapGammeltFrisinngrunnlag.map(frisinnGrunnlag, Optional.of(oo));

        // Assert
        assertThat(resultat).hasSize(1);
        assertThat(resultat.get(0)).isEqualToComparingFieldByField(forventet(april, true, false));
    }

    @Test
    public void snfl_overlapper_eksakt() {
        FrisinnGrunnlag frisinnGrunnlag = new FrisinnGrunnlag(true, true);
        Intervall april = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 4, 1), LocalDate.of(2020, 4, 30));
        var månedsinntekt = Beløp.fra(15_000);
        List<OppgittFrilansInntektDto> oppgittFrilansInntekt = List.of(lagOppgittFrilansInntekt(april, månedsinntekt));
        List<OppgittOpptjeningDtoBuilder.EgenNæringBuilder> oppgittNæring = List.of(lagOppgittInntekt(april, månedsinntekt));
        OppgittOpptjeningDto oo = OppgittOpptjeningDtoBuilder.ny()
                .leggTilEgneNæringer(oppgittNæring)
                .leggTilFrilansOpplysninger(new OppgittFrilansDto(false, oppgittFrilansInntekt))
                .build();

        // Act
        List<FrisinnPeriode> resultat = MapGammeltFrisinngrunnlag.map(frisinnGrunnlag, Optional.of(oo));

        // Assert
        assertThat(resultat).hasSize(1);
        assertThat(resultat.get(0)).isEqualToComparingFieldByField(forventet(april, true, true));
    }

    @Test
    public void snfl_overlapper_ikke_fl_er_lengst() {
        FrisinnGrunnlag frisinnGrunnlag = new FrisinnGrunnlag(true, true);
        Intervall april = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 4, 1), LocalDate.of(2020, 4, 30));
        Intervall halveApril = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 4, 15), LocalDate.of(2020, 4, 30));

        var månedsinntekt = Beløp.fra(15_000);
        List<OppgittFrilansInntektDto> oppgittFrilansInntekt = List.of(lagOppgittFrilansInntekt(april, månedsinntekt));
        List<OppgittOpptjeningDtoBuilder.EgenNæringBuilder> oppgittNæring = List.of(lagOppgittInntekt(halveApril, månedsinntekt));
        OppgittOpptjeningDto oo = OppgittOpptjeningDtoBuilder.ny()
                .leggTilEgneNæringer(oppgittNæring)
                .leggTilFrilansOpplysninger(new OppgittFrilansDto(false, oppgittFrilansInntekt))
                .build();

        // Act
        List<FrisinnPeriode> resultat = MapGammeltFrisinngrunnlag.map(frisinnGrunnlag, Optional.of(oo));

        // Assert
        Intervall renFLPeriode = Intervall.fraOgMedTilOgMed(LocalDate.of(2020,4,1), LocalDate.of(2020, 4, 14));
        Intervall kombinertPeriode = Intervall.fraOgMedTilOgMed(LocalDate.of(2020,4,15), LocalDate.of(2020, 4, 30));
        assertThat(resultat).hasSize(2);
        assertThat(resultat.get(0)).isEqualToComparingFieldByField(forventet(renFLPeriode, true, false));
        assertThat(resultat.get(1)).isEqualToComparingFieldByField(forventet(kombinertPeriode, true, true));
    }

    @Test
    public void snfl_overlapper_ikke_sn_er_lengst() {
        FrisinnGrunnlag frisinnGrunnlag = new FrisinnGrunnlag(true, true);
        Intervall periode1 = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 3, 28), LocalDate.of(2020, 4, 30));
        Intervall periode2 = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 4, 3), LocalDate.of(2020, 4, 30));

        var månedsinntekt = Beløp.fra(15_000);
        List<OppgittFrilansInntektDto> oppgittFrilansInntekt = List.of(lagOppgittFrilansInntekt(periode2, månedsinntekt));
        List<OppgittOpptjeningDtoBuilder.EgenNæringBuilder> oppgittNæring = List.of(lagOppgittInntekt(periode1, månedsinntekt));
        OppgittOpptjeningDto oo = OppgittOpptjeningDtoBuilder.ny()
                .leggTilEgneNæringer(oppgittNæring)
                .leggTilFrilansOpplysninger(new OppgittFrilansDto(false, oppgittFrilansInntekt))
                .build();

        // Act
        List<FrisinnPeriode> resultat = MapGammeltFrisinngrunnlag.map(frisinnGrunnlag, Optional.of(oo));

        // Assert
        Intervall renSNPeriode = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 3, 28), LocalDate.of(2020, 4, 2));
        Intervall kombinertPeriode = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 4, 3), LocalDate.of(2020, 4, 30));
        assertThat(resultat).hasSize(2);
        assertThat(resultat.get(0)).isEqualToComparingFieldByField(forventet(renSNPeriode, false, true));
        assertThat(resultat.get(1)).isEqualToComparingFieldByField(forventet(kombinertPeriode, true, true));
    }

    private FrisinnPeriode forventet(Intervall april, boolean søkerFL, boolean søkerSN) {
        return new FrisinnPeriode(april, søkerFL, søkerSN);
    }

    private OppgittOpptjeningDtoBuilder.EgenNæringBuilder lagOppgittInntekt(Intervall april, Beløp periodeInntekt) {
        return OppgittOpptjeningDtoBuilder.EgenNæringBuilder.ny()
                .medPeriode(april)
                .medVirksomhet("999999999")
                .medBruttoInntekt(periodeInntekt);
    }

    private OppgittFrilansInntektDto lagOppgittFrilansInntekt(Intervall april, Beløp periodeInntekt) {
        return new OppgittFrilansInntektDto(april, periodeInntekt);
    }

}
