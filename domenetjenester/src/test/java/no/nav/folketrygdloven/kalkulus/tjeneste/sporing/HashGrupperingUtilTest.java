package no.nav.folketrygdloven.kalkulus.tjeneste.sporing;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.output.RegelSporingPeriode;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagPeriodeRegelType;

class HashGrupperingUtilTest {
    private Function<String, String> superenkelHashfunksjon = streng -> streng.substring(0, 1);

    @Test
    void skal_gruppere_pr_hash_når_alle_er_like() {
        List<String> input = List.of("A", "A", "A");
        Map<String, List<String>> resultat = HashGrupperingUtil.grupperPrHash(input, Function.identity(), superenkelHashfunksjon);
        Assertions.assertThat(resultat).isEqualTo(Map.of(
                "A", List.of("A", "A", "A")
        ));

    }

    @Test
    void skal_gruppere_pr_hash_når_over_5_ulike_hash_verdier() {
        List<String> input = List.of(
                "A", "Abba", "Adfa",
                "B", "B",
                "D", "C",
                "C", "D",
                "F", "E",
                "E");
        Map<String, List<String>> resultat = HashGrupperingUtil.grupperPrHash(input, Function.identity(), superenkelHashfunksjon);
        Assertions.assertThat(resultat).isEqualTo(Map.of(
                "A", List.of("A", "Abba", "Adfa"),
                "B", List.of("B", "B"),
                "C", List.of("C", "C"),
                "D", List.of("D", "D"),
                "E", List.of("E", "E"),
                "F", List.of("F")
        ));

    }

    @Test
    void skal_gruppere_like_regelsporinger() {
        LocalDate dag1 = LocalDate.now();
        LocalDate dag2 = dag1.plusDays(1);
        LocalDate dag3 = dag1.plusDays(2);
        Intervall intervall1 = Intervall.fraOgMedTilOgMed(dag1, dag1);
        Intervall intervall2 = Intervall.fraOgMedTilOgMed(dag2, dag2);
        Intervall intervall3 = Intervall.fraOgMedTilOgMed(dag3, dag3);
        RegelSporingPeriode rsp1 = new RegelSporingPeriode("evaluering1", "input1", intervall1, BeregningsgrunnlagPeriodeRegelType.FORESLÅ);
        RegelSporingPeriode rsp2 = new RegelSporingPeriode("evaluering2", "input1", intervall2, BeregningsgrunnlagPeriodeRegelType.FORESLÅ);
        RegelSporingPeriode rsp3 = new RegelSporingPeriode("evaluering3", "input1", intervall3, BeregningsgrunnlagPeriodeRegelType.FORESLÅ);
        Map<String, List<RegelSporingPeriode>> gruppert = HashGrupperingUtil.grupperRegelsporinger(List.of(rsp1, rsp2, rsp3), superenkelHashfunksjon);
        Assertions.assertThat(gruppert).containsOnlyKeys("i");
        Assertions.assertThat(gruppert.get("i")).containsOnly(rsp1, rsp2, rsp3); //garanterer ikke rekkefølge
    }

    @Test
    void skal_gruppere_ulike_regelsporinger_som_havner_på_samme_hash_selv_om_de_har_ulik_type() {
        LocalDate dag1 = LocalDate.now();
        LocalDate dag2 = dag1.plusDays(1);
        LocalDate dag3 = dag1.plusDays(2);
        Intervall intervall1 = Intervall.fraOgMedTilOgMed(dag1, dag1);
        Intervall intervall2 = Intervall.fraOgMedTilOgMed(dag2, dag2);
        Intervall intervall3 = Intervall.fraOgMedTilOgMed(dag3, dag3);
        RegelSporingPeriode rsp1 = new RegelSporingPeriode("evaluering1", "input1", intervall1, BeregningsgrunnlagPeriodeRegelType.FORESLÅ);
        RegelSporingPeriode rsp2 = new RegelSporingPeriode("evaluering2", "input2", intervall2, BeregningsgrunnlagPeriodeRegelType.FASTSETT);
        RegelSporingPeriode rsp3 = new RegelSporingPeriode("evaluering3", "input3", intervall3, BeregningsgrunnlagPeriodeRegelType.FINN_GRENSEVERDI);
        Map<String, List<RegelSporingPeriode>> gruppert = HashGrupperingUtil.grupperRegelsporinger(List.of(rsp1, rsp2, rsp3), superenkelHashfunksjon);
        Assertions.assertThat(gruppert).containsOnlyKeys("i");
        Assertions.assertThat(gruppert.get("i")).containsOnly(rsp1, rsp2, rsp3); //garanterer ikke rekkefølge
    }

    @Test
    void skal_gruppere_ulike_regelsporinger() {
        LocalDate dag1 = LocalDate.now();
        LocalDate dag2 = dag1.plusDays(1);
        LocalDate dag3 = dag1.plusDays(2);
        Intervall intervall1 = Intervall.fraOgMedTilOgMed(dag1, dag1);
        Intervall intervall2 = Intervall.fraOgMedTilOgMed(dag2, dag2);
        Intervall intervall3 = Intervall.fraOgMedTilOgMed(dag3, dag3);
        RegelSporingPeriode rspA1 = new RegelSporingPeriode("evalueringA1", "Ainput1", intervall1, BeregningsgrunnlagPeriodeRegelType.FORESLÅ);
        RegelSporingPeriode rspA2 = new RegelSporingPeriode("evalueringA2", "Ainput1", intervall2, BeregningsgrunnlagPeriodeRegelType.FORESLÅ);
        RegelSporingPeriode rspA3 = new RegelSporingPeriode("evalueringA3", "Ainput2", intervall3, BeregningsgrunnlagPeriodeRegelType.FORESLÅ);
        RegelSporingPeriode rspB1 = new RegelSporingPeriode("evalueringB1", "Binput1", intervall2, BeregningsgrunnlagPeriodeRegelType.FORESLÅ);
        RegelSporingPeriode rspC1 = new RegelSporingPeriode("evalueringC1", "Cinput1", intervall3, BeregningsgrunnlagPeriodeRegelType.FINN_GRENSEVERDI);
        RegelSporingPeriode rspC2 = new RegelSporingPeriode("evalueringC2", "Cinput1", intervall3, BeregningsgrunnlagPeriodeRegelType.FINN_GRENSEVERDI);
        Map<String, List<RegelSporingPeriode>> gruppert = HashGrupperingUtil.grupperRegelsporinger(List.of(rspA1, rspA2, rspA3, rspB1, rspC1, rspC2), superenkelHashfunksjon);
        Assertions.assertThat(gruppert).containsOnlyKeys("A", "B", "C");
        Assertions.assertThat(gruppert.get("A")).containsOnly(rspA1, rspA2, rspA3); //garanterer ikke rekkefølge
        Assertions.assertThat(gruppert.get("B")).containsOnly(rspB1);
        Assertions.assertThat(gruppert.get("C")).containsOnly(rspC1, rspC2); //garanterer ikke rekkefølge
    }
}
