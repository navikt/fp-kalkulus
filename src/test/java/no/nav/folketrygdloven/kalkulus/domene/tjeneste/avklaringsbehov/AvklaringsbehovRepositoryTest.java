package no.nav.folketrygdloven.kalkulus.domene.tjeneste.avklaringsbehov;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.UUID;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.AktørId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import no.nav.folketrygdloven.kalkulus.migreringer.dbstoette.JpaExtension;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.avklaringsbehov.AvklaringsbehovEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.avklaringsbehov.AvklaringsbehovKontrollTjeneste;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.KoblingReferanse;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Saksnummer;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.domene.tjeneste.kobling.KoblingRepository;
import no.nav.vedtak.felles.testutilities.db.EntityManagerAwareTest;

@ExtendWith(JpaExtension.class)
class AvklaringsbehovRepositoryTest extends EntityManagerAwareTest {

    private AvklaringsbehovRepository avklaringsbehovRepository;
    private KoblingRepository koblingRepository;
    private AvklaringsbehovKontrollTjeneste avklaringsbehovKontrollTjeneste = new AvklaringsbehovKontrollTjeneste();
    private KoblingEntitet kobling;

    @BeforeEach
    void setup() {
        AktørId aktørId = new AktørId("9999999999999");
        KoblingReferanse koblingReferanse = new KoblingReferanse(UUID.randomUUID());
        Saksnummer saksnummer = new Saksnummer("1234");

        kobling = new KoblingEntitet(koblingReferanse, FagsakYtelseType.PLEIEPENGER_NÆRSTÅENDE, saksnummer, aktørId);
        avklaringsbehovRepository = new AvklaringsbehovRepository(getEntityManager());
        koblingRepository = new KoblingRepository(getEntityManager());
        koblingRepository.lagre(kobling);
    }

    @Test
    void skal_lagre_og_hente_avklaringsbehov() {
        AvklaringsbehovEntitet ap = avklaringsbehovKontrollTjeneste.opprettForKobling(kobling, AvklaringsbehovDefinisjon.FASTSETT_BG_AT_FL);
        avklaringsbehovRepository.lagre(ap);

        Optional<AvklaringsbehovEntitet> hentetAP = avklaringsbehovRepository.hentAvklaringsbehovForKobling(kobling, AvklaringsbehovDefinisjon.FASTSETT_BG_AT_FL);

        assertThat(hentetAP).isNotEmpty();
        assertThat(hentetAP.get().getDefinisjon()).isEqualTo(AvklaringsbehovDefinisjon.FASTSETT_BG_AT_FL);
    }

    @Test
    void skal_hente_ap_som_ikke_finnes() {
        Optional<AvklaringsbehovEntitet> hentetAP = avklaringsbehovRepository.hentAvklaringsbehovForKobling(kobling, AvklaringsbehovDefinisjon.FASTSETT_BG_AT_FL);

        assertThat(hentetAP).isEmpty();
    }
}
