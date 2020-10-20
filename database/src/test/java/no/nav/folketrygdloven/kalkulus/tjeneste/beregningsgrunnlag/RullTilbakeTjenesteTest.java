package no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.KalkulatorInputEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningAktivitetAggregatEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagBuilder;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.AktørId;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.KoblingReferanse;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Saksnummer;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.YtelseTyperKalkulusStøtter;
import no.nav.folketrygdloven.kalkulus.tjeneste.extensions.EntityManagerFtKalkulusAwareExtension;
import no.nav.folketrygdloven.kalkulus.tjeneste.kobling.KoblingRepository;
import no.nav.vedtak.felles.testutilities.db.EntityManagerAwareTest;

@ExtendWith({EntityManagerFtKalkulusAwareExtension.class})
public class RullTilbakeTjenesteTest extends EntityManagerAwareTest {

    private BeregningsgrunnlagRepository repository;
    private KoblingRepository koblingRepository;
    private RullTilbakeTjeneste rullTilbakeTjeneste;
    private Long koblingId;

    @BeforeEach
    public void setUp() {
        repository = new BeregningsgrunnlagRepository(getEntityManager());
        koblingRepository = new KoblingRepository(getEntityManager());
        rullTilbakeTjeneste = new RullTilbakeTjeneste(repository);
    }

    @Test
    public void skal_rulle_tilbake_til_obligatorisk_tilstand_når_ny_tilstand_er_før_aktiv() {
        prepareTestData();
        // Arrange
        repository.lagre(koblingId, BeregningsgrunnlagGrunnlagBuilder.oppdatere(Optional.empty())
                .medRegisterAktiviteter(BeregningAktivitetAggregatEntitet.builder()
                        .medSkjæringstidspunktOpptjening(LocalDate.now())
                        .build()), BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);
        repository.lagre(koblingId, BeregningsgrunnlagGrunnlagBuilder.oppdatere(Optional.empty())
                .medRegisterAktiviteter(BeregningAktivitetAggregatEntitet.builder()
                        .medSkjæringstidspunktOpptjening(LocalDate.now())
                        .build()), BeregningsgrunnlagTilstand.FORESLÅTT);
        rullTilbakeTjeneste.rullTilbakeTilObligatoriskTilstandFørVedBehov(koblingId, BeregningsgrunnlagTilstand.KOFAKBER_UT);

        // Act
        Optional<BeregningsgrunnlagGrunnlagEntitet> aktivtGrunnlag = repository.hentBeregningsgrunnlagGrunnlagEntitet(koblingId);

        // Assert
        assertThat(aktivtGrunnlag).isPresent();
        assertThat(aktivtGrunnlag.get().getBeregningsgrunnlagTilstand()).isEqualTo(BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);
    }

    @Test
    public void skal_rulle_tilbake_til_obligatorisk_tilstand_når_ny_tilstand_er_før_aktiv_og_ny_tilstand_er_obligatorisk_tilstand() {
        prepareTestData();
        // Arrange
        repository.lagre(koblingId, BeregningsgrunnlagGrunnlagBuilder.oppdatere(Optional.empty())
                .medRegisterAktiviteter(BeregningAktivitetAggregatEntitet.builder()
                        .medSkjæringstidspunktOpptjening(LocalDate.now())
                        .build()), BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);
        repository.lagre(koblingId, BeregningsgrunnlagGrunnlagBuilder.oppdatere(Optional.empty())
                .medRegisterAktiviteter(BeregningAktivitetAggregatEntitet.builder()
                        .medSkjæringstidspunktOpptjening(LocalDate.now())
                        .build()), BeregningsgrunnlagTilstand.FORESLÅTT);
        rullTilbakeTjeneste.rullTilbakeTilObligatoriskTilstandFørVedBehov(koblingId, BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);

        // Act
        Optional<BeregningsgrunnlagGrunnlagEntitet> aktivtGrunnlag = repository.hentBeregningsgrunnlagGrunnlagEntitet(koblingId);

        // Assert
        assertThat(aktivtGrunnlag).isPresent();
        assertThat(aktivtGrunnlag.get().getBeregningsgrunnlagTilstand()).isEqualTo(BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);
    }


    @Test
    public void skal_ikkje_rulle_tilbake_til_obligatorisk_tilstand_når_ny_tilstand_er_etter_aktiv() {
        prepareTestData();
        // Arrange
        repository.lagre(koblingId, BeregningsgrunnlagGrunnlagBuilder.oppdatere(Optional.empty())
                .medRegisterAktiviteter(BeregningAktivitetAggregatEntitet.builder()
                        .medSkjæringstidspunktOpptjening(LocalDate.now())
                        .build()), BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);
        repository.lagre(koblingId, BeregningsgrunnlagGrunnlagBuilder.oppdatere(Optional.empty())
                .medRegisterAktiviteter(BeregningAktivitetAggregatEntitet.builder()
                        .medSkjæringstidspunktOpptjening(LocalDate.now())
                        .build()), BeregningsgrunnlagTilstand.FORESLÅTT);
        rullTilbakeTjeneste.rullTilbakeTilObligatoriskTilstandFørVedBehov(koblingId, BeregningsgrunnlagTilstand.FORESLÅTT_UT);

        // Act
        Optional<BeregningsgrunnlagGrunnlagEntitet> aktivtGrunnlag = repository.hentBeregningsgrunnlagGrunnlagEntitet(koblingId);

        // Assert
        assertThat(aktivtGrunnlag).isPresent();
        assertThat(aktivtGrunnlag.get().getBeregningsgrunnlagTilstand()).isEqualTo(BeregningsgrunnlagTilstand.FORESLÅTT);
    }

    @Test
    public void skal_deaktivere_beregningsgrunnlag_og_input() {
        prepareTestData();
        // Arrange
        String json = getTestJSON();
        KalkulatorInputEntitet input = new KalkulatorInputEntitet(koblingId, json);
        repository.lagreOgSjekkStatus(input);
        repository.lagre(koblingId, BeregningsgrunnlagGrunnlagBuilder.oppdatere(Optional.empty())
                .medRegisterAktiviteter(BeregningAktivitetAggregatEntitet.builder()
                        .medSkjæringstidspunktOpptjening(LocalDate.now())
                        .build()), BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);
        rullTilbakeTjeneste.deaktiverAktivtBeregningsgrunnlagOgInput(koblingId);

        // Act
        Optional<BeregningsgrunnlagGrunnlagEntitet> aktivtGrunnlag = repository.hentBeregningsgrunnlagGrunnlagEntitet(koblingId);
        Optional<KalkulatorInputEntitet> kalkulatorInputEntitet = repository.hentHvisEksitererKalkulatorInput(koblingId);

        // Assert
        assertThat(aktivtGrunnlag).isEmpty();
        assertThat(kalkulatorInputEntitet).isEmpty();
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


    private void prepareTestData() {
        AktørId aktørId = new AktørId("1234123412341");
        KoblingReferanse koblingReferanse = new KoblingReferanse(UUID.randomUUID());
        Saksnummer saksnummer = new Saksnummer("1234");
        KoblingEntitet koblingEntitet = new KoblingEntitet(koblingReferanse, YtelseTyperKalkulusStøtter.FORELDREPENGER, saksnummer, aktørId);
        koblingRepository.lagre(koblingEntitet);
        koblingId = koblingEntitet.getId();
    }



}
