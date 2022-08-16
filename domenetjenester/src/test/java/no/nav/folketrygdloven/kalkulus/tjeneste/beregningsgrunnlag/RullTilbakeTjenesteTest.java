package no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.KalkulatorInputEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.avklaringsbehov.AvklaringsbehovEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.avklaringsbehov.AvklaringsbehovKontrollTjeneste;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningAktivitetAggregatEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagBuilder;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.KoblingReferanse;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Saksnummer;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.sporing.RegelSporingGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.sporing.RegelSporingPeriodeEntitet;
import no.nav.folketrygdloven.kalkulus.felles.jpa.IntervallEntitet;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagPeriodeRegelType;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagRegelType;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseTyperKalkulusStøtterKontrakt;
import no.nav.folketrygdloven.kalkulus.tjeneste.avklaringsbehov.AvklaringsbehovRepository;
import no.nav.folketrygdloven.kalkulus.tjeneste.avklaringsbehov.AvklaringsbehovTjeneste;
import no.nav.folketrygdloven.kalkulus.tjeneste.extensions.JpaExtension;
import no.nav.folketrygdloven.kalkulus.tjeneste.forlengelse.ForlengelseRepository;
import no.nav.folketrygdloven.kalkulus.tjeneste.forlengelse.ForlengelseTjeneste;
import no.nav.folketrygdloven.kalkulus.tjeneste.kobling.KoblingRepository;
import no.nav.folketrygdloven.kalkulus.tjeneste.sporing.RegelsporingRepository;
import no.nav.folketrygdloven.kalkulus.typer.AktørId;
import no.nav.k9.felles.testutilities.db.EntityManagerAwareTest;

@ExtendWith({JpaExtension.class})
public class RullTilbakeTjenesteTest extends EntityManagerAwareTest {

    private BeregningsgrunnlagRepository repository;
    private RegelsporingRepository regelsporingRepository;
    private KoblingRepository koblingRepository;
    private RullTilbakeTjeneste rullTilbakeTjeneste;
    private AvklaringsbehovTjeneste avklaringsbehovTjeneste;
    private ForlengelseTjeneste forlengelseTjeneste;
    private Long koblingId;

    @BeforeEach
    public void setUp() {
        repository = new BeregningsgrunnlagRepository(getEntityManager());
        regelsporingRepository = new RegelsporingRepository(getEntityManager());
        koblingRepository = new KoblingRepository(getEntityManager());
        forlengelseTjeneste = new ForlengelseTjeneste(new ForlengelseRepository(getEntityManager()), true);
        AvklaringsbehovRepository avklaringsbehovRepository = new AvklaringsbehovRepository(getEntityManager());
        AvklaringsbehovKontrollTjeneste avklaringsbehovKontrollTjeneste = new AvklaringsbehovKontrollTjeneste();
        avklaringsbehovTjeneste = new AvklaringsbehovTjeneste(avklaringsbehovRepository, koblingRepository, avklaringsbehovKontrollTjeneste);
        rullTilbakeTjeneste = new RullTilbakeTjeneste(repository, regelsporingRepository, avklaringsbehovTjeneste, forlengelseTjeneste);
    }

    @Test
    public void skal_rulle_tilbake_til_obligatorisk_tilstand_når_ny_tilstand_er_før_aktiv() {
        prepareTestData();
        // Arrange
        repository.lagre(koblingId, BeregningsgrunnlagGrunnlagBuilder.kopiere(Optional.empty())
                .medRegisterAktiviteter(BeregningAktivitetAggregatEntitet.builder()
                        .medSkjæringstidspunktOpptjening(LocalDate.now())
                        .build()), BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);
        repository.lagre(koblingId, BeregningsgrunnlagGrunnlagBuilder.kopiere(Optional.empty())
                .medRegisterAktiviteter(BeregningAktivitetAggregatEntitet.builder()
                        .medSkjæringstidspunktOpptjening(LocalDate.now())
                        .build()), BeregningsgrunnlagTilstand.FORESLÅTT);
        regelsporingRepository.lagre(koblingId, Map.of(BeregningsgrunnlagPeriodeRegelType.FORESLÅ, List.of(RegelSporingPeriodeEntitet.ny().medRegelEvaluering(getTestJSON()).medRegelInput(getTestJSON()).medPeriode(IntervallEntitet.fraOgMed(LocalDate.now())))));
        rullTilbakeTjeneste.rullTilbakeTilObligatoriskTilstandFørVedBehov(Set.of(koblingId), BeregningsgrunnlagTilstand.KOFAKBER_UT);

        // Act
        Optional<BeregningsgrunnlagGrunnlagEntitet> aktivtGrunnlag = repository.hentBeregningsgrunnlagGrunnlagEntitet(koblingId);
        var aktiveRegelsporinger = regelsporingRepository.hentRegelSporingPeriodeMedGittType(koblingId, List.of(BeregningsgrunnlagPeriodeRegelType.FORESLÅ));

        // Assert
        assertThat(aktivtGrunnlag).isPresent();
        assertThat(aktivtGrunnlag.get().getBeregningsgrunnlagTilstand()).isEqualTo(BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);
        assertThat(aktiveRegelsporinger.isEmpty()).isTrue();

    }

    @Test
    public void skal_rulle_tilbake_til_obligatorisk_tilstand_når_ny_tilstand_er_før_aktiv_og_ny_tilstand_er_obligatorisk_tilstand() {
        prepareTestData();
        // Arrange
        repository.lagre(koblingId, BeregningsgrunnlagGrunnlagBuilder.kopiere(Optional.empty())
                .medRegisterAktiviteter(BeregningAktivitetAggregatEntitet.builder()
                        .medSkjæringstidspunktOpptjening(LocalDate.now())
                        .build()), BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);
        repository.lagre(koblingId, BeregningsgrunnlagGrunnlagBuilder.kopiere(Optional.empty())
                .medRegisterAktiviteter(BeregningAktivitetAggregatEntitet.builder()
                        .medSkjæringstidspunktOpptjening(LocalDate.now())
                        .build()), BeregningsgrunnlagTilstand.FORESLÅTT);
        regelsporingRepository.lagre(koblingId, Map.of(BeregningsgrunnlagPeriodeRegelType.FORESLÅ, List.of(RegelSporingPeriodeEntitet.ny().medRegelEvaluering(getTestJSON()).medRegelInput(getTestJSON()).medPeriode(IntervallEntitet.fraOgMed(LocalDate.now())))));
        rullTilbakeTjeneste.rullTilbakeTilObligatoriskTilstandFørVedBehov(Set.of(koblingId), BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);

        // Act
        Optional<BeregningsgrunnlagGrunnlagEntitet> aktivtGrunnlag = repository.hentBeregningsgrunnlagGrunnlagEntitet(koblingId);
        var aktiveRegelsporinger = regelsporingRepository.hentRegelSporingPeriodeMedGittType(koblingId, List.of(BeregningsgrunnlagPeriodeRegelType.FORESLÅ));

        // Assert
        assertThat(aktivtGrunnlag).isPresent();
        assertThat(aktivtGrunnlag.get().getBeregningsgrunnlagTilstand()).isEqualTo(BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);
        assertThat(aktiveRegelsporinger.isEmpty()).isTrue();

    }


    @Test
    public void skal_ikkje_rulle_tilbake_til_obligatorisk_tilstand_når_ny_tilstand_er_etter_aktiv() {
        prepareTestData();
        // Arrange
        repository.lagre(koblingId, BeregningsgrunnlagGrunnlagBuilder.kopiere(Optional.empty())
                .medRegisterAktiviteter(BeregningAktivitetAggregatEntitet.builder()
                        .medSkjæringstidspunktOpptjening(LocalDate.now())
                        .build()), BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);
        repository.lagre(koblingId, BeregningsgrunnlagGrunnlagBuilder.kopiere(Optional.empty())
                .medRegisterAktiviteter(BeregningAktivitetAggregatEntitet.builder()
                        .medSkjæringstidspunktOpptjening(LocalDate.now())
                        .build()), BeregningsgrunnlagTilstand.FORESLÅTT);
        rullTilbakeTjeneste.rullTilbakeTilObligatoriskTilstandFørVedBehov(Set.of(koblingId), BeregningsgrunnlagTilstand.FORESLÅTT_UT);

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
        AvklaringsbehovDefinisjon avklaringsbehovDefinisjon = AvklaringsbehovDefinisjon.VURDER_FAKTA_FOR_ATFL_SN;
        avklaringsbehovTjeneste.opprettEllerGjennopprettAvklaringsbehov(koblingId, avklaringsbehovDefinisjon);
        repository.lagre(koblingId, BeregningsgrunnlagGrunnlagBuilder.kopiere(Optional.empty())
                .medRegisterAktiviteter(BeregningAktivitetAggregatEntitet.builder()
                        .medSkjæringstidspunktOpptjening(LocalDate.now())
                        .build()), BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);
        regelsporingRepository.lagre(koblingId, RegelSporingGrunnlagEntitet.ny().medRegelEvaluering(getTestJSON()).medRegelInput(getTestJSON()), BeregningsgrunnlagRegelType.SKJÆRINGSTIDSPUNKT);
        var aktiveRegelsporingerFørDeaktivering = regelsporingRepository.hentRegelSporingGrunnlagMedGittType(koblingId, List.of(BeregningsgrunnlagRegelType.SKJÆRINGSTIDSPUNKT));
        assertThat(aktiveRegelsporingerFørDeaktivering.isEmpty()).isFalse();
        rullTilbakeTjeneste.deaktiverAllKoblingdata(koblingId);

        // Act
        Optional<BeregningsgrunnlagGrunnlagEntitet> aktivtGrunnlag = repository.hentBeregningsgrunnlagGrunnlagEntitet(koblingId);
        Optional<KalkulatorInputEntitet> kalkulatorInputEntitet = repository.hentHvisEksitererKalkulatorInput(koblingId);
        var aktiveRegelsporinger = regelsporingRepository.hentRegelSporingGrunnlagMedGittType(koblingId, List.of(BeregningsgrunnlagRegelType.SKJÆRINGSTIDSPUNKT));

        // Assert
        assertThat(aktivtGrunnlag).isEmpty();
        assertThat(kalkulatorInputEntitet).isEmpty();
        assertThat(aktiveRegelsporinger.isEmpty()).isTrue();

        Optional<AvklaringsbehovEntitet> avklaringsbehovEntitet = avklaringsbehovTjeneste.hentAvklaringsbehov(koblingId, avklaringsbehovDefinisjon);
        assertThat(avklaringsbehovEntitet.isPresent());
        assertThat(avklaringsbehovEntitet.get().getStatus()).isEqualTo(AvklaringsbehovStatus.AVBRUTT);
    }

    @Test
    public void skal_avbryte_avklaringsbehov() {
        prepareTestData();
        // Arrange
        AvklaringsbehovDefinisjon avklaringsbehovDefinisjon = AvklaringsbehovDefinisjon.VURDER_FAKTA_FOR_ATFL_SN;
        avklaringsbehovTjeneste.opprettEllerGjennopprettAvklaringsbehov(koblingId, avklaringsbehovDefinisjon);
        avklaringsbehovTjeneste.løsAvklaringsbehov(koblingId, avklaringsbehovDefinisjon, "Begrunnelse");
        repository.lagre(koblingId, BeregningsgrunnlagGrunnlagBuilder.kopiere(Optional.empty())
                .medRegisterAktiviteter(BeregningAktivitetAggregatEntitet.builder()
                        .medSkjæringstidspunktOpptjening(LocalDate.now())
                        .build()), BeregningsgrunnlagTilstand.FASTSATT_BEREGNINGSAKTIVITETER);

        // Act
        rullTilbakeTjeneste.rullTilbakeTilObligatoriskTilstandFørVedBehov(Set.of(koblingId), BeregningsgrunnlagTilstand.OPPRETTET);

        // Assert
        Optional<AvklaringsbehovEntitet> avklaringsbehovEntitet = avklaringsbehovTjeneste.hentAvklaringsbehov(koblingId, avklaringsbehovDefinisjon);
        assertThat(avklaringsbehovEntitet).isPresent();
        assertThat(avklaringsbehovEntitet.get().getStatus()).isEqualTo(AvklaringsbehovStatus.AVBRUTT);
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
        KoblingEntitet koblingEntitet = new KoblingEntitet(koblingReferanse, YtelseTyperKalkulusStøtterKontrakt.FORELDREPENGER, saksnummer, aktørId, false);
        koblingRepository.lagre(koblingEntitet);
        koblingId = koblingEntitet.getId();
    }


}
