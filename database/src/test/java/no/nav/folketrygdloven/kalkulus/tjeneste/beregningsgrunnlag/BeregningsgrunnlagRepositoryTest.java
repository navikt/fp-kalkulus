package no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;

import no.nav.folketrygdloven.kalkulus.dbstoette.UnittestRepositoryRule;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.KalkulatorInputEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.AktørId;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.KoblingReferanse;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Saksnummer;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.YtelseTyperKalkulusStøtter;
import no.nav.folketrygdloven.kalkulus.tjeneste.kobling.KoblingRepository;
import no.nav.vedtak.felles.testutilities.db.RepositoryRule;

public class BeregningsgrunnlagRepositoryTest {

    @Rule
    public RepositoryRule repositoryRule = new UnittestRepositoryRule();


    private BeregningsgrunnlagRepository repository = new BeregningsgrunnlagRepository(repositoryRule.getEntityManager());
    private KoblingRepository koblingRepository = new KoblingRepository(repositoryRule.getEntityManager());


    @Test
    public void skal_lagre_ned_input() {
        AktørId aktørId = new AktørId("1234123412341");
        KoblingReferanse koblingReferanse = new KoblingReferanse(UUID.randomUUID());
        Saksnummer saksnummer = new Saksnummer("1234");

        KoblingEntitet koblingEntitet = new KoblingEntitet(koblingReferanse, YtelseTyperKalkulusStøtter.PLEIEPENGER_NÆRSTÅENDE, saksnummer, aktørId);
        koblingRepository.lagre(koblingEntitet);

        Long koblingId = koblingEntitet.getId();

        String json = getTestJSON();

        KalkulatorInputEntitet input = new KalkulatorInputEntitet(koblingId, json);

        repository.lagre(input);

        KalkulatorInputEntitet resultat = repository.hentKalkulatorInput(koblingId);

        assertThat(resultat.getInput()).isEqualTo(json);
    }

    private String getTestJSON() {
        return "{\n" +
                "  \"jeg\" : {\n" +
                "    \"er\" : \"en\",\n" +
                "    \"test\" : \"json\",\n" +
                "    \"fordi\" : \"jeg\",\n" +
                "    \"tester\" : \"jsonb\",\n" +
                "    \"lagring\" : {\n" +
                "      \"i\" : \"postgres\",\n" +
                "      \"databasen\" : \"til\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"kalkulus\" : \"okey?\"\n" +
                "}";
    }
}
