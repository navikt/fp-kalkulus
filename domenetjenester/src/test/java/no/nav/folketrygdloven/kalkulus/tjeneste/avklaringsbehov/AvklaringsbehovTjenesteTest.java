package no.nav.folketrygdloven.kalkulus.tjeneste.avklaringsbehov;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import no.nav.folketrygdloven.kalkulus.dbstoette.JpaExtension;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.avklaringsbehov.AvklaringsbehovEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.avklaringsbehov.AvklaringsbehovKontrollTjeneste;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.KoblingReferanse;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Saksnummer;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningSteg;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.tjeneste.kobling.KoblingRepository;
import no.nav.folketrygdloven.kalkulus.typer.AktørId;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.testutilities.db.EntityManagerAwareTest;

@ExtendWith(JpaExtension.class)
class AvklaringsbehovTjenesteTest extends EntityManagerAwareTest {
    private AvklaringsbehovRepository avklaringsbehovRepository;
    private KoblingRepository koblingRepository;
    private AvklaringsbehovKontrollTjeneste avklaringsbehovKontrollTjeneste = new AvklaringsbehovKontrollTjeneste();
    private KoblingEntitet kobling;
    private AvklaringsbehovTjeneste avklaringsbehovTjeneste;

    @BeforeEach
    public void setup() {
        AktørId aktørId = new AktørId("9999999999999");
        KoblingReferanse koblingReferanse = new KoblingReferanse(UUID.randomUUID());
        Saksnummer saksnummer = new Saksnummer("1234");

        kobling = new KoblingEntitet(koblingReferanse, FagsakYtelseType.PLEIEPENGER_NÆRSTÅENDE, saksnummer, aktørId);
        avklaringsbehovRepository = new AvklaringsbehovRepository(getEntityManager());
        koblingRepository = new KoblingRepository(getEntityManager());
        koblingRepository.lagre(kobling);
        avklaringsbehovTjeneste = new AvklaringsbehovTjeneste(avklaringsbehovRepository, koblingRepository, avklaringsbehovKontrollTjeneste);
    }

    @Test
    public void skal_opprette_lagre_og_hente_avklaringsbehov() {
        List<AvklaringsbehovDefinisjon> avklaringsbehov = Collections.singletonList(AvklaringsbehovDefinisjon.FASTSETT_BG_AT_FL);

        avklaringsbehovTjeneste.lagreAvklaringsresultater(kobling.getId(), avklaringsbehov);

        AvklaringsbehovEntitet lagretAvklaringsbehov = avklaringsbehovTjeneste.hentAvklaringsbehov(kobling.getId(), AvklaringsbehovDefinisjon.FASTSETT_BG_AT_FL)
                .orElse(null);

        assertThat(lagretAvklaringsbehov).isNotNull();
        assertThat(lagretAvklaringsbehov.getDefinisjon()).isEqualTo(AvklaringsbehovDefinisjon.FASTSETT_BG_AT_FL);
        assertThat(lagretAvklaringsbehov.getStatus()).isEqualTo(AvklaringsbehovStatus.OPPRETTET);
        assertThat(lagretAvklaringsbehov.getStegFunnet()).isEqualTo(BeregningSteg.FORS_BERGRUNN);
    }

    @Test
    public void skal_opprette_og_løse_avklaringsbehov() {
        List<AvklaringsbehovDefinisjon> avklaringsbehov = Collections.singletonList(AvklaringsbehovDefinisjon.FASTSETT_BG_AT_FL);

        // Opprett
        avklaringsbehovTjeneste.lagreAvklaringsresultater(kobling.getId(), avklaringsbehov);
        // Løs
        avklaringsbehovTjeneste.løsAvklaringsbehov(kobling.getId(), AvklaringsbehovDefinisjon.FASTSETT_BG_AT_FL, "Dette er en begrunnelse");

        AvklaringsbehovEntitet løstAvklaringsbehov = avklaringsbehovTjeneste.hentAvklaringsbehov(kobling.getId(), AvklaringsbehovDefinisjon.FASTSETT_BG_AT_FL)
                .orElse(null);
        assertThat(løstAvklaringsbehov).isNotNull();
        assertThat(løstAvklaringsbehov.getDefinisjon()).isEqualTo(AvklaringsbehovDefinisjon.FASTSETT_BG_AT_FL);
        assertThat(løstAvklaringsbehov.getStatus()).isEqualTo(AvklaringsbehovStatus.UTFØRT);
        assertThat(løstAvklaringsbehov.getStegFunnet()).isEqualTo(BeregningSteg.FORS_BERGRUNN);
        assertThat(løstAvklaringsbehov.getBegrunnelse()).isEqualTo("Dette er en begrunnelse");
    }

    @Test
    public void skal_avbryte_avklaringsbehov_fra_og_med_oppgitt_avklaringsbehov_ved_tilbakerulling() {
        // Opprett
        avklaringsbehovTjeneste.lagreAvklaringsresultater(kobling.getId(), Collections.singletonList(AvklaringsbehovDefinisjon.VURDER_FAKTA_ATFL_SN));
        avklaringsbehovTjeneste.lagreAvklaringsresultater(kobling.getId(), Collections.singletonList(AvklaringsbehovDefinisjon.FASTSETT_BG_AT_FL));
        avklaringsbehovTjeneste.lagreAvklaringsresultater(kobling.getId(), Collections.singletonList(AvklaringsbehovDefinisjon.FORDEL_BG));

        // Løs
        avklaringsbehovTjeneste.løsAvklaringsbehov(kobling.getId(), AvklaringsbehovDefinisjon.VURDER_FAKTA_ATFL_SN, "Dette er en begrunnelse");
        avklaringsbehovTjeneste.løsAvklaringsbehov(kobling.getId(), AvklaringsbehovDefinisjon.FASTSETT_BG_AT_FL, "Dette er en begrunnelse");
        avklaringsbehovTjeneste.løsAvklaringsbehov(kobling.getId(), AvklaringsbehovDefinisjon.FORDEL_BG, "Dette er en begrunnelse");

        // Rull tilbake fra og med avvikavklaringsbehov
        avklaringsbehovTjeneste.avbrytAlleAvklaringsbehovEtter(kobling.getId(), AvklaringsbehovDefinisjon.FASTSETT_BG_AT_FL);

        List<AvklaringsbehovEntitet> avklaringsbehov = avklaringsbehovRepository.hentAvklaringsbehovForKobling(kobling);

        assertThat(avklaringsbehov).hasSize(3);
        AvklaringsbehovEntitet faktaAP = avklaringsbehov.stream().filter(ap -> ap.getDefinisjon().equals(AvklaringsbehovDefinisjon.VURDER_FAKTA_ATFL_SN)).findFirst().orElse(null);
        assertThat(faktaAP).isNotNull();
        assertThat(faktaAP.getDefinisjon()).isEqualTo(AvklaringsbehovDefinisjon.VURDER_FAKTA_ATFL_SN);
        assertThat(faktaAP.getStatus()).isEqualTo(AvklaringsbehovStatus.UTFØRT);
        assertThat(faktaAP.getStegFunnet()).isEqualTo(BeregningSteg.KOFAKBER);

        AvklaringsbehovEntitet avvikAP = avklaringsbehov.stream().filter(ap -> ap.getDefinisjon().equals(AvklaringsbehovDefinisjon.FASTSETT_BG_AT_FL)).findFirst().orElse(null);
        assertThat(avvikAP).isNotNull();
        assertThat(avvikAP.getDefinisjon()).isEqualTo(AvklaringsbehovDefinisjon.FASTSETT_BG_AT_FL);
        assertThat(avvikAP.getStatus()).isEqualTo(AvklaringsbehovStatus.UTFØRT);
        assertThat(avvikAP.getStegFunnet()).isEqualTo(BeregningSteg.FORS_BERGRUNN);

        AvklaringsbehovEntitet fordelAP = avklaringsbehov.stream().filter(ap -> ap.getDefinisjon().equals(AvklaringsbehovDefinisjon.FORDEL_BG)).findFirst().orElse(null);
        assertThat(fordelAP).isNotNull();
        assertThat(fordelAP.getDefinisjon()).isEqualTo(AvklaringsbehovDefinisjon.FORDEL_BG);
        assertThat(fordelAP.getStatus()).isEqualTo(AvklaringsbehovStatus.AVBRUTT);
        assertThat(fordelAP.getStegFunnet()).isEqualTo(BeregningSteg.FORDEL_BERGRUNN);
    }

    @Test
    public void skal_kaste_feil_om_vi_prøver_å_løse_avklaringsbehov_som_ikke_er_utledet() {
        assertThatThrownBy(() ->
                avklaringsbehovTjeneste.løsAvklaringsbehov(kobling.getId(), AvklaringsbehovDefinisjon.FASTSETT_BG_AT_FL, "Dette er en begrunnelse"))
                .isInstanceOf(TekniskException.class);
    }

    @Test
    public void skal_kaste_feil_hvis_vi_prøver_å_løse_allerede_løst_avklaringsbehov() {
        List<AvklaringsbehovDefinisjon> avklaringsbehov = Collections.singletonList(AvklaringsbehovDefinisjon.FASTSETT_BG_AT_FL);

        // Opprett
        avklaringsbehovTjeneste.lagreAvklaringsresultater(kobling.getId(), avklaringsbehov);
        // Løs
        avklaringsbehovTjeneste.løsAvklaringsbehov(kobling.getId(), AvklaringsbehovDefinisjon.FASTSETT_BG_AT_FL, "Dette er en begrunnelse");

        AvklaringsbehovEntitet løstAvklaringsbehov = avklaringsbehovTjeneste.hentAvklaringsbehov(kobling.getId(), AvklaringsbehovDefinisjon.FASTSETT_BG_AT_FL)
                .orElse(null);
        assertThat(løstAvklaringsbehov).isNotNull();
        assertThat(løstAvklaringsbehov.getDefinisjon()).isEqualTo(AvklaringsbehovDefinisjon.FASTSETT_BG_AT_FL);
        assertThat(løstAvklaringsbehov.getStatus()).isEqualTo(AvklaringsbehovStatus.UTFØRT);
        assertThat(løstAvklaringsbehov.getStegFunnet()).isEqualTo(BeregningSteg.FORS_BERGRUNN);
        assertThat(løstAvklaringsbehov.getBegrunnelse()).isEqualTo("Dette er en begrunnelse");

        // Prøv å løse igjen
        assertThatThrownBy(() ->
                avklaringsbehovTjeneste.løsAvklaringsbehov(kobling.getId(), AvklaringsbehovDefinisjon.FASTSETT_BG_AT_FL, "Dette er en begrunnelse"))
                .isInstanceOf(TekniskException.class);

    }


}
