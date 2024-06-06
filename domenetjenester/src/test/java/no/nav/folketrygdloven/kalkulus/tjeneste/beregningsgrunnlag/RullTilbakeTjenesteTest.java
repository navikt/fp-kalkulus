package no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.AktørId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import no.nav.folketrygdloven.kalkulator.output.RegelSporingPeriode;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.dbstoette.JpaExtension;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.avklaringsbehov.AvklaringsbehovEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.avklaringsbehov.AvklaringsbehovKontrollTjeneste;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningAktivitetAggregatEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagBuilder;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.KoblingReferanse;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Saksnummer;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.sporing.RegelSporingGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagPeriodeRegelType;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagRegelType;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.tjeneste.avklaringsbehov.AvklaringsbehovRepository;
import no.nav.folketrygdloven.kalkulus.tjeneste.avklaringsbehov.AvklaringsbehovTjeneste;
import no.nav.folketrygdloven.kalkulus.tjeneste.kobling.KoblingRepository;
import no.nav.folketrygdloven.kalkulus.tjeneste.sporing.RegelSporingTjeneste;
import no.nav.folketrygdloven.kalkulus.tjeneste.sporing.RegelsporingRepository;
import no.nav.vedtak.felles.testutilities.db.EntityManagerAwareTest;

@ExtendWith({JpaExtension.class})
public class RullTilbakeTjenesteTest extends EntityManagerAwareTest {

    private BeregningsgrunnlagRepository repository;
    private RegelsporingRepository regelsporingRepository;
    private KoblingRepository koblingRepository;
    private RullTilbakeTjeneste rullTilbakeTjeneste;
    private AvklaringsbehovTjeneste avklaringsbehovTjeneste;
    private RegelSporingTjeneste regelSporingTjeneste;
    private Long koblingId;

    @BeforeEach
    public void setUp() {
        repository = new BeregningsgrunnlagRepository(getEntityManager());
        regelsporingRepository = new RegelsporingRepository(getEntityManager());
        regelSporingTjeneste = new RegelSporingTjeneste(regelsporingRepository);
        koblingRepository = new KoblingRepository(getEntityManager());
        AvklaringsbehovRepository avklaringsbehovRepository = new AvklaringsbehovRepository(getEntityManager());
        AvklaringsbehovKontrollTjeneste avklaringsbehovKontrollTjeneste = new AvklaringsbehovKontrollTjeneste();
        avklaringsbehovTjeneste = new AvklaringsbehovTjeneste(avklaringsbehovRepository, koblingRepository, avklaringsbehovKontrollTjeneste);
        rullTilbakeTjeneste = new RullTilbakeTjeneste(repository, regelsporingRepository, avklaringsbehovTjeneste);
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
        regelSporingTjeneste.lagre(koblingId, List.of(new RegelSporingPeriode(getTestJSON(), getTestJSON(), Intervall.fraOgMed(LocalDate.now()), BeregningsgrunnlagPeriodeRegelType.FORESLÅ, "1.2.3")));
        rullTilbakeTjeneste.rullTilbakeTilForrigeTilstandVedBehov(koblingId, BeregningsgrunnlagTilstand.KOFAKBER_UT, true);

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
                        .build()), BeregningsgrunnlagTilstand.OPPRETTET);
        repository.lagre(koblingId, BeregningsgrunnlagGrunnlagBuilder.kopiere(Optional.empty())
                .medRegisterAktiviteter(BeregningAktivitetAggregatEntitet.builder()
                        .medSkjæringstidspunktOpptjening(LocalDate.now())
                        .build()), BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);
        repository.lagre(koblingId, BeregningsgrunnlagGrunnlagBuilder.kopiere(Optional.empty())
                .medRegisterAktiviteter(BeregningAktivitetAggregatEntitet.builder()
                        .medSkjæringstidspunktOpptjening(LocalDate.now())
                        .build()), BeregningsgrunnlagTilstand.FORESLÅTT);
        regelSporingTjeneste.lagre(koblingId, List.of(new RegelSporingPeriode(getTestJSON(), getTestJSON(), Intervall.fraOgMed(LocalDate.now()), BeregningsgrunnlagPeriodeRegelType.FORESLÅ, "1.2.3")));
        rullTilbakeTjeneste.rullTilbakeTilForrigeTilstandVedBehov(koblingId, BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER, true);

        // Act
        Optional<BeregningsgrunnlagGrunnlagEntitet> aktivtGrunnlag = repository.hentBeregningsgrunnlagGrunnlagEntitet(koblingId);
        var aktiveRegelsporinger = regelsporingRepository.hentRegelSporingPeriodeMedGittType(koblingId, List.of(BeregningsgrunnlagPeriodeRegelType.FORESLÅ));

        // Assert
        assertThat(aktivtGrunnlag).isPresent();
        assertThat(aktivtGrunnlag.get().getBeregningsgrunnlagTilstand()).isEqualTo(BeregningsgrunnlagTilstand.OPPRETTET);
        assertThat(aktiveRegelsporinger.isEmpty()).isTrue();

    }

    @Test
    public void skal_rulle_tilbake_til_obligatorisk_tilstand_når_ny_tilstand_er_før_aktiv_og_kobling_ikke_har_grunnlag_fra_tilstand_tidligere() {
        prepareTestData();
        // Arrange
        repository.lagre(koblingId, BeregningsgrunnlagGrunnlagBuilder.kopiere(Optional.empty())
                .medRegisterAktiviteter(BeregningAktivitetAggregatEntitet.builder()
                        .medSkjæringstidspunktOpptjening(LocalDate.now())
                        .build()), BeregningsgrunnlagTilstand.OPPRETTET);

        repository.lagre(koblingId, BeregningsgrunnlagGrunnlagBuilder.kopiere(Optional.empty())
                .medRegisterAktiviteter(BeregningAktivitetAggregatEntitet.builder()
                        .medSkjæringstidspunktOpptjening(LocalDate.now())
                        .build()), BeregningsgrunnlagTilstand.FORESLÅTT);
        regelSporingTjeneste.lagre(koblingId, List.of(new RegelSporingPeriode(getTestJSON(), getTestJSON(), Intervall.fraOgMed(LocalDate.now()), BeregningsgrunnlagPeriodeRegelType.FORESLÅ, "1.2.3")));
        rullTilbakeTjeneste.rullTilbakeTilForrigeTilstandVedBehov(koblingId, BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER, true);

        // Act
        Optional<BeregningsgrunnlagGrunnlagEntitet> aktivtGrunnlag = repository.hentBeregningsgrunnlagGrunnlagEntitet(koblingId);
        var aktiveRegelsporinger = regelsporingRepository.hentRegelSporingPeriodeMedGittType(koblingId, List.of(BeregningsgrunnlagPeriodeRegelType.FORESLÅ));

        // Assert
        assertThat(aktivtGrunnlag).isPresent();
        assertThat(aktivtGrunnlag.get().getBeregningsgrunnlagTilstand()).isEqualTo(BeregningsgrunnlagTilstand.OPPRETTET);
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
        rullTilbakeTjeneste.rullTilbakeTilForrigeTilstandVedBehov(koblingId, BeregningsgrunnlagTilstand.FORESLÅTT_UT, true);

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
        AvklaringsbehovDefinisjon avklaringsbehovDefinisjon = AvklaringsbehovDefinisjon.VURDER_FAKTA_ATFL_SN;
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
        var aktiveRegelsporinger = regelsporingRepository.hentRegelSporingGrunnlagMedGittType(koblingId, List.of(BeregningsgrunnlagRegelType.SKJÆRINGSTIDSPUNKT));

        // Assert
        assertThat(aktivtGrunnlag).isEmpty();
        assertThat(aktiveRegelsporinger.isEmpty()).isTrue();

        Optional<AvklaringsbehovEntitet> avklaringsbehovEntitet = avklaringsbehovTjeneste.hentAvklaringsbehov(koblingId, avklaringsbehovDefinisjon);
        assertThat(avklaringsbehovEntitet.isPresent());
        assertThat(avklaringsbehovEntitet.get().getStatus()).isEqualTo(AvklaringsbehovStatus.AVBRUTT);
    }

    @Test
    public void skal_avbryte_avklaringsbehov() {
        prepareTestData();
        // Arrange
        AvklaringsbehovDefinisjon avklaringsbehovDefinisjon = AvklaringsbehovDefinisjon.VURDER_FAKTA_ATFL_SN;
        avklaringsbehovTjeneste.opprettEllerGjennopprettAvklaringsbehov(koblingId, avklaringsbehovDefinisjon);
        avklaringsbehovTjeneste.løsAvklaringsbehov(koblingId, avklaringsbehovDefinisjon, "Begrunnelse");
        repository.lagre(koblingId, BeregningsgrunnlagGrunnlagBuilder.kopiere(Optional.empty())
                .medRegisterAktiviteter(BeregningAktivitetAggregatEntitet.builder()
                        .medSkjæringstidspunktOpptjening(LocalDate.now())
                        .build()), BeregningsgrunnlagTilstand.FASTSATT_BEREGNINGSAKTIVITETER);

        // Act
        rullTilbakeTjeneste.rullTilbakeTilForrigeTilstandVedBehov(koblingId, BeregningsgrunnlagTilstand.OPPRETTET, true);

        // Assert
        Optional<AvklaringsbehovEntitet> avklaringsbehovEntitet = avklaringsbehovTjeneste.hentAvklaringsbehov(koblingId, avklaringsbehovDefinisjon);
        assertThat(avklaringsbehovEntitet).isPresent();
        assertThat(avklaringsbehovEntitet.get().getStatus()).isEqualTo(AvklaringsbehovStatus.AVBRUTT);
    }

    private String getTestJSON() {
        return """
                {
                  "jeg" : {
                    "er" : "en",
                    "test" : "json",
                    "fordi" : "jeg",
                    "tester" : "jsonb",
                    "lagring" : {
                      "i" : "postgres",
                      "databasen" : "til"
                    }
                  },
                  "kalkulus" : "okey?"
                }""";
    }


    private void prepareTestData() {
        AktørId aktørId = new AktørId("9999999999999");
        KoblingReferanse koblingReferanse = new KoblingReferanse(UUID.randomUUID());
        Saksnummer saksnummer = new Saksnummer("1234");
        KoblingEntitet koblingEntitet = new KoblingEntitet(koblingReferanse, FagsakYtelseType.FORELDREPENGER, saksnummer, aktørId);
        koblingRepository.lagre(koblingEntitet);
        koblingId = koblingEntitet.getId();
    }


}
