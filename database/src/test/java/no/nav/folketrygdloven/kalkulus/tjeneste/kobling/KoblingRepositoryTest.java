package no.nav.folketrygdloven.kalkulus.tjeneste.kobling;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.TimeZone;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.AktørId;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.KoblingReferanse;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Saksnummer;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.YtelseTyperKalkulusStøtter;
import no.nav.folketrygdloven.kalkulus.tjeneste.extensions.EntityManagerFtKalkulusAwareExtension;
import no.nav.vedtak.felles.testutilities.db.EntityManagerAwareTest;

@ExtendWith(EntityManagerFtKalkulusAwareExtension.class)
public class KoblingRepositoryTest extends EntityManagerAwareTest {

    private KoblingRepository koblingRepository;

    static {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Oslo"));
    }

    @BeforeEach
    public void beforeEach() {
        koblingRepository = new KoblingRepository(getEntityManager());
    }

    @Test
    public void skal_lagre_ned_beregningsgrunnlag() {
        AktørId aktørId = new AktørId("1234123412341");
        KoblingReferanse koblingReferanse = new KoblingReferanse(UUID.randomUUID());
        Saksnummer saksnummer = new Saksnummer("1234");

        KoblingEntitet koblingEntitet = new KoblingEntitet(koblingReferanse, YtelseTyperKalkulusStøtter.PLEIEPENGER_NÆRSTÅENDE, saksnummer, aktørId);

        koblingRepository.lagre(koblingEntitet);

        Optional<KoblingEntitet> koblingOpt = koblingRepository.hentForKoblingReferanse(koblingReferanse);
        assertThat(koblingOpt).isNotEmpty();
    }
}
