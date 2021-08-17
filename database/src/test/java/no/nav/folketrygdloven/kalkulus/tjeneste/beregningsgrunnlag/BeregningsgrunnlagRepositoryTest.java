package no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag;


import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.KalkulatorInputEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningAktivitetAggregatEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningAktivitetEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagBuilder;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.GrunnlagReferanse;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.KoblingReferanse;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Saksnummer;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.felles.jpa.IntervallEntitet;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseTyperKalkulusStøtterKontrakt;
import no.nav.folketrygdloven.kalkulus.tjeneste.extensions.JpaExtension;
import no.nav.folketrygdloven.kalkulus.tjeneste.kobling.KoblingRepository;
import no.nav.folketrygdloven.kalkulus.typer.AktørId;
import no.nav.k9.felles.testutilities.db.EntityManagerAwareTest;

@ExtendWith(JpaExtension.class)
public class BeregningsgrunnlagRepositoryTest extends EntityManagerAwareTest {

    private BeregningsgrunnlagRepository repository;
    private KoblingRepository koblingRepository;

    @BeforeEach
    public void beforeEach() {
        repository = new BeregningsgrunnlagRepository(getEntityManager());
        koblingRepository = new KoblingRepository(getEntityManager());
    }

    @Test
    public void skal_lagre_ned_input() {
        AktørId aktørId = new AktørId("1234123412341");
        KoblingReferanse koblingReferanse = new KoblingReferanse(UUID.randomUUID());
        Saksnummer saksnummer = new Saksnummer("1234");

        KoblingEntitet koblingEntitet = new KoblingEntitet(koblingReferanse, YtelseTyperKalkulusStøtterKontrakt.PLEIEPENGER_NÆRSTÅENDE, saksnummer, aktørId);
        koblingRepository.lagre(koblingEntitet);

        Long koblingId = koblingEntitet.getId();

        String json = getTestJSON();

        KalkulatorInputEntitet input = new KalkulatorInputEntitet(koblingId, json);

        repository.lagreOgSjekkStatus(input);

        KalkulatorInputEntitet resultat = repository.hentKalkulatorInput(koblingId);

        assertThat(resultat.getInput()).isEqualTo(json);
    }

    @Test
    public void skal_hente_beregningsgrunnlag_for_referanse() {
        AktørId aktørId = new AktørId("1234123412341");
        KoblingReferanse koblingReferanse = new KoblingReferanse(UUID.randomUUID());
        Saksnummer saksnummer = new Saksnummer("1234");

        KoblingEntitet koblingEntitet = new KoblingEntitet(koblingReferanse, YtelseTyperKalkulusStøtterKontrakt.PLEIEPENGER_NÆRSTÅENDE, saksnummer, aktørId);
        koblingRepository.lagre(koblingEntitet);

        Long koblingId = koblingEntitet.getId();

        BeregningsgrunnlagEntitet build = BeregningsgrunnlagEntitet.builder()
                .medSkjæringstidspunkt(LocalDate.now())
                .build();
        BeregningsgrunnlagGrunnlagBuilder builder = BeregningsgrunnlagGrunnlagBuilder.oppdatere(Optional.empty())
                .medBeregningsgrunnlag(build)
                .medRegisterAktiviteter(BeregningAktivitetAggregatEntitet.builder().medSkjæringstidspunktOpptjening(LocalDate.now())
                        .leggTilAktivitet(BeregningAktivitetEntitet.builder().medOpptjeningAktivitetType(OpptjeningAktivitetType.FRILANS)
                                .medPeriode(IntervallEntitet.fraOgMedTilOgMed(LocalDate.now(), LocalDate.now().plusMonths(1))).build()).build());

        BeregningsgrunnlagGrunnlagEntitet lagretGrunnlag = repository.lagre(koblingId, builder, BeregningsgrunnlagTilstand.OPPRETTET);

        GrunnlagReferanse grunnlagReferanse = lagretGrunnlag.getGrunnlagReferanse();

        Optional<BeregningsgrunnlagGrunnlagEntitet> grunnlagForReferanse = repository.hentBeregningsgrunnlagGrunnlagEntitetForReferanse(koblingId, grunnlagReferanse.getReferanse());

        assertThat(grunnlagForReferanse).isPresent();
        assertThat(grunnlagForReferanse.get()).isEqualTo(lagretGrunnlag);
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
