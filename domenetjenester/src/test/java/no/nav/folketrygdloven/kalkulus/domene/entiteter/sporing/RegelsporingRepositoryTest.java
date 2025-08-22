package no.nav.folketrygdloven.kalkulus.domene.entiteter.sporing;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import no.nav.folketrygdloven.kalkulator.output.RegelSporingPeriode;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.dbstoette.JpaExtension;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.AktørId;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.KoblingReferanse;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Saksnummer;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagPeriodeRegelType;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagRegelType;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.tjeneste.kobling.KoblingRepository;
import no.nav.vedtak.felles.testutilities.db.EntityManagerAwareTest;

@ExtendWith(JpaExtension.class)
class RegelsporingRepositoryTest extends EntityManagerAwareTest {
    private RegelsporingRepository regelsporingRepository;
    private KoblingRepository koblingRepository;
    private KoblingEntitet kobling;

    @BeforeEach
    public void setup() {
        AktørId aktørId = new AktørId("9999999999999");
        KoblingReferanse koblingReferanse = new KoblingReferanse(UUID.randomUUID());
        Saksnummer saksnummer = new Saksnummer("1234");

        kobling = new KoblingEntitet(koblingReferanse, FagsakYtelseType.FORELDREPENGER, saksnummer, aktørId);
        regelsporingRepository = new RegelsporingRepository(getEntityManager());
        koblingRepository = new KoblingRepository(getEntityManager());
        koblingRepository.lagre(kobling);
    }

    @Test
    void skal_kunne_lagre_regelsporing_grunnlag() {
        var builder1 = new RegelSporingGrunnlagEntitet.Builder();
        builder1.medRegelVersjon("1.0.0").medRegelEvaluering("Evaluering1").medRegelInput("Input1");
        var builder2 = new RegelSporingGrunnlagEntitet.Builder();
        builder2.medRegelVersjon("1.0.0").medRegelEvaluering("Evaluering2").medRegelInput("Input2");
        regelsporingRepository.lagre(kobling.getId(), builder1, BeregningsgrunnlagRegelType.SKJÆRINGSTIDSPUNKT);
        regelsporingRepository.lagre(kobling.getId(), builder2, BeregningsgrunnlagRegelType.BRUKERS_STATUS);

        var resultat = regelsporingRepository.hentRegelSporingGrunnlagMedGittType(kobling.getId(), List.of(BeregningsgrunnlagRegelType.values()));

        assertThat(resultat).hasSize(2);
        var s1Res = resultat.stream()
            .filter(s -> s.getRegelType().equals(BeregningsgrunnlagRegelType.SKJÆRINGSTIDSPUNKT))
            .findFirst()
            .orElseThrow();
        assertThat(s1Res.getRegelInput()).isEqualTo("Input1");
        assertThat(s1Res.getRegelEvaluering()).isEqualTo("Evaluering1");
        assertThat(s1Res.getRegelVersjon()).isEqualTo("1.0.0");

        var s2Res = resultat.stream()
            .filter(s -> s.getRegelType().equals(BeregningsgrunnlagRegelType.BRUKERS_STATUS))
            .findFirst()
            .orElseThrow();
        assertThat(s2Res.getRegelInput()).isEqualTo("Input2");
        assertThat(s2Res.getRegelEvaluering()).isEqualTo("Evaluering2");
        assertThat(s2Res.getRegelVersjon()).isEqualTo("1.0.0");
    }

    @Test
    void skal_kunne_lagre_regelsporing_periode() {
        var regel1 = new RegelSporingPeriode("Evaluering1", "Input1",
            Intervall.fraOgMedTilOgMed(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 2, 1)), BeregningsgrunnlagPeriodeRegelType.FORESLÅ, "1.0.0");
        var regel2 = new RegelSporingPeriode("Evaluering2", "Input2",
            Intervall.fraOgMedTilOgMed(LocalDate.of(2025, 3, 1), LocalDate.of(2025, 4, 1)), BeregningsgrunnlagPeriodeRegelType.FORESLÅ_2, "2.0.0");
        regelsporingRepository.lagreSporinger(Arrays.asList(regel1, regel2), kobling.getId());

        var resultat = regelsporingRepository.hentRegelSporingPeriodeMedGittType(kobling.getId(), List.of(BeregningsgrunnlagPeriodeRegelType.values()));

        assertThat(resultat).hasSize(2);
        var s1Res = resultat.stream()
            .filter(s -> s.getRegelType().equals(BeregningsgrunnlagPeriodeRegelType.FORESLÅ))
            .findFirst()
            .orElseThrow();
        assertThat(s1Res.getRegelInput()).isEqualTo("Input1");
        assertThat(s1Res.getRegelEvaluering()).isEqualTo("Evaluering1");
        assertThat(s1Res.getRegelVersjon()).isEqualTo("1.0.0");
        assertThat(s1Res.getPeriode().getFomDato()).isEqualTo(LocalDate.of(2025, 1, 1));
        assertThat(s1Res.getPeriode().getTomDato()).isEqualTo(LocalDate.of(2025, 2, 1));

        var s2Res = resultat.stream()
            .filter(s -> s.getRegelType().equals(BeregningsgrunnlagPeriodeRegelType.FORESLÅ_2))
            .findFirst()
            .orElseThrow();
        assertThat(s2Res.getRegelInput()).isEqualTo("Input2");
        assertThat(s2Res.getRegelEvaluering()).isEqualTo("Evaluering2");
        assertThat(s2Res.getRegelVersjon()).isEqualTo("2.0.0");
        assertThat(s2Res.getPeriode().getFomDato()).isEqualTo(LocalDate.of(2025, 3, 1));
        assertThat(s2Res.getPeriode().getTomDato()).isEqualTo(LocalDate.of(2025, 4, 1));

    }

}
