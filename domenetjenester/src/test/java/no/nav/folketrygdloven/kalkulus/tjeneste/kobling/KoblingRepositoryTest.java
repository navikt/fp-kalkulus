package no.nav.folketrygdloven.kalkulus.tjeneste.kobling;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.TimeZone;
import java.util.UUID;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.AktørId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.KoblingReferanse;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Saksnummer;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.dbstoette.JpaExtension;
import no.nav.vedtak.felles.testutilities.db.EntityManagerAwareTest;

@ExtendWith(JpaExtension.class)
public class KoblingRepositoryTest extends EntityManagerAwareTest {

    static {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Oslo"));
    }

    private KoblingRepository koblingRepository;

    @BeforeEach
    public void beforeEach() {
        koblingRepository = new KoblingRepository(getEntityManager());
    }

    @Test
    public void skal_lagre_ned_beregningsgrunnlag() {
        AktørId aktørId = new AktørId("9999999999999");
        KoblingReferanse koblingReferanse = new KoblingReferanse(UUID.randomUUID());
        Saksnummer saksnummer = new Saksnummer("1234");

        KoblingEntitet koblingEntitet = new KoblingEntitet(koblingReferanse, FagsakYtelseType.PLEIEPENGER_NÆRSTÅENDE, saksnummer, aktørId);

        koblingRepository.lagre(koblingEntitet);

        Optional<KoblingEntitet> koblingOpt = koblingRepository.hentForKoblingReferanse(koblingReferanse);
        assertThat(koblingOpt).isNotEmpty();
    }
}
