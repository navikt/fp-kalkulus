package no.nav.folketrygdloven.kalkulus.domene.entiteter.sporing;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.AktørId;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.KoblingReferanse;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Saksnummer;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.tjeneste.kobling.KoblingRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import no.nav.folketrygdloven.kalkulus.dbstoette.JpaExtension;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagRegelType;
import no.nav.folketrygdloven.kalkulus.migrering.RegelSporingGrunnlagMigreringDto;
import no.nav.vedtak.felles.testutilities.db.EntityManagerAwareTest;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

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

        kobling = new KoblingEntitet(koblingReferanse, FagsakYtelseType.PLEIEPENGER_NÆRSTÅENDE, saksnummer, aktørId);
        regelsporingRepository = new RegelsporingRepository(getEntityManager());
        koblingRepository = new KoblingRepository(getEntityManager());
        koblingRepository.lagre(kobling);
    }

    @Test
    void skal_kunne_lagre_migrert_sporing_med_null_evaluering() {
        var s1 = new RegelSporingGrunnlagMigreringDto("evaluering", "input", BeregningsgrunnlagRegelType.BRUKERS_STATUS, "1.0.0");
        var s2 = new RegelSporingGrunnlagMigreringDto(null, "input", BeregningsgrunnlagRegelType.PERIODISERING, null);

        regelsporingRepository.migrerSporinger(List.of(s1, s2), List.of(), kobling.getId());

        var resultat = regelsporingRepository.hentAlleRegelSporingGrunnlag(kobling.getId());

        assertThat(resultat).hasSize(2);
        var s1Res = resultat.stream()
            .filter(s -> s.getRegelType().equals(BeregningsgrunnlagRegelType.BRUKERS_STATUS))
            .findFirst()
            .orElseThrow();
        assertThat(s1Res.getRegelInput()).isEqualTo("input");
        assertThat(s1Res.getRegelEvaluering()).isEqualTo("evaluering");
        assertThat(s1Res.getRegelVersjon()).isEqualTo("1.0.0");

        var s2Res = resultat.stream()
            .filter(s -> s.getRegelType().equals(BeregningsgrunnlagRegelType.PERIODISERING))
            .findFirst()
            .orElseThrow();
        assertThat(s2Res.getRegelInput()).isEqualTo("input");
        assertThat(s2Res.getRegelEvaluering()).isNull();
        assertThat(s2Res.getRegelVersjon()).isNull();
    }
}
