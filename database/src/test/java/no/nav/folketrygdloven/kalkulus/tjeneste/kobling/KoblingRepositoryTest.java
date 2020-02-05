package no.nav.folketrygdloven.kalkulus.tjeneste.kobling;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;

import no.nav.folketrygdloven.kalkulus.dbstoette.UnittestRepositoryRule;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.AktørId;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.KoblingReferanse;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Saksnummer;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.YtelseTyperKalkulusStøtter;
import no.nav.vedtak.felles.testutilities.db.RepositoryRule;

public class KoblingRepositoryTest {

    @Rule
    public RepositoryRule repositoryRule = new UnittestRepositoryRule();

    private final KoblingRepository koblingRepository = new KoblingRepository(repositoryRule.getEntityManager());

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
