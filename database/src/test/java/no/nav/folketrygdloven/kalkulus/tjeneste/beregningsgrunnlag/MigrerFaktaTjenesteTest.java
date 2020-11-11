package no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag;

import static no.nav.folketrygdloven.kalkulus.felles.tid.AbstractIntervall.TIDENES_ENDE;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningAktivitetAggregatEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningAktivitetEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagBuilder;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.FaktaArbeidsforholdEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.AktørId;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.KoblingReferanse;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Saksnummer;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.felles.jpa.IntervallEntitet;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.YtelseTyperKalkulusStøtter;
import no.nav.folketrygdloven.kalkulus.tjeneste.extensions.EntityManagerFtKalkulusAwareExtension;
import no.nav.folketrygdloven.kalkulus.tjeneste.kobling.KoblingRepository;
import no.nav.vedtak.felles.testutilities.db.EntityManagerAwareTest;

@ExtendWith(EntityManagerFtKalkulusAwareExtension.class)
class MigrerFaktaTjenesteTest extends EntityManagerAwareTest {


    public static final LocalDate STP = LocalDate.now();
    public static final Arbeidsgiver ARBEIDSGIVER = Arbeidsgiver.virksomhet("129478323");
    private BeregningsgrunnlagRepository repository;
    private KoblingRepository koblingRepository;
    private MigrerFaktaTjeneste migrerFaktaTjeneste;

    @BeforeEach
    public void beforeEach() {
        repository = new BeregningsgrunnlagRepository(getEntityManager());
        koblingRepository = new KoblingRepository(getEntityManager());
        migrerFaktaTjeneste = new MigrerFaktaTjeneste(repository);
    }

    @Test
    public void skal_migrere_mottar_ytelse() {
        // Arrange
        KoblingEntitet koblingEntitet = lagKobling();
        BeregningsgrunnlagGrunnlagBuilder beregningsgrunnlagGrunnlagBuilder = lagGrunnlagMedMottarYtelseForFL();
        repository.lagre(koblingEntitet.getId(), beregningsgrunnlagGrunnlagBuilder, BeregningsgrunnlagTilstand.KOFAKBER_UT);

        // Act
        migrerFaktaTjeneste.migrerFakta();

        // Assert
        Optional<BeregningsgrunnlagGrunnlagEntitet> beregningsgrunnlagGrunnlagEntitet = repository.hentBeregningsgrunnlagGrunnlagEntitet(koblingEntitet.getId());
        assertThat(beregningsgrunnlagGrunnlagEntitet).isPresent();
        assertThat(beregningsgrunnlagGrunnlagEntitet.get().getFaktaAggregat()).isPresent();
        assertThat(beregningsgrunnlagGrunnlagEntitet.get().getFaktaAggregat().get().getFaktaAktør()).isPresent();
        assertThat(beregningsgrunnlagGrunnlagEntitet.get().getFaktaAggregat().get().getFaktaAktør().get().getHarFLMottattYtelse()).isTrue();

    }

    @Test
    public void skal_migrere_mottar_ytelse_i_gamle_grunnlag() {
        // Arrange
        KoblingEntitet koblingEntitet = lagKobling();
        BeregningsgrunnlagGrunnlagBuilder beregningsgrunnlagGrunnlagBuilder = lagGrunnlagMedMottarYtelseForFL();
        repository.lagre(koblingEntitet.getId(), beregningsgrunnlagGrunnlagBuilder, BeregningsgrunnlagTilstand.KOFAKBER_UT);
        BeregningsgrunnlagGrunnlagBuilder beregningsgrunnlagGrunnlagBuilder2 = lagGrunnlagMedMottarYtelseForFL();
        repository.lagre(koblingEntitet.getId(), beregningsgrunnlagGrunnlagBuilder2, BeregningsgrunnlagTilstand.FASTSATT);

        // Act
        migrerFaktaTjeneste.migrerFakta();

        // Assert
        Optional<BeregningsgrunnlagGrunnlagEntitet> kofakber_grunnlag = repository.hentSisteBeregningsgrunnlagGrunnlagEntitet(koblingEntitet.getId(), BeregningsgrunnlagTilstand.KOFAKBER_UT);
        assertThat(kofakber_grunnlag).isPresent();
        assertThat(kofakber_grunnlag.get().erAktivt()).isFalse();
        assertThat(kofakber_grunnlag.get().getFaktaAggregat()).isPresent();
        assertThat(kofakber_grunnlag.get().getFaktaAggregat().get().getFaktaAktør()).isPresent();
        assertThat(kofakber_grunnlag.get().getFaktaAggregat().get().getFaktaAktør().get().getHarFLMottattYtelse()).isTrue();

        Optional<BeregningsgrunnlagGrunnlagEntitet> fastsatt = repository.hentSisteBeregningsgrunnlagGrunnlagEntitet(koblingEntitet.getId(), BeregningsgrunnlagTilstand.FASTSATT);
        assertThat(fastsatt).isPresent();
        assertThat(fastsatt.get().erAktivt()).isTrue();
        assertThat(fastsatt.get().getFaktaAggregat()).isPresent();
        assertThat(fastsatt.get().getFaktaAggregat().get().getFaktaAktør()).isPresent();
        assertThat(fastsatt.get().getFaktaAggregat().get().getFaktaAktør().get().getHarFLMottattYtelse()).isTrue();
    }

    @Test
    public void skal_migrere_tidsbegrenset_arbeid() {
        // Arrange
        KoblingEntitet koblingEntitet = lagKobling();
        BeregningsgrunnlagGrunnlagBuilder beregningsgrunnlagGrunnlagBuilder = lagGrunnlagMedTidsbegrensetArbeid();
        repository.lagre(koblingEntitet.getId(), beregningsgrunnlagGrunnlagBuilder, BeregningsgrunnlagTilstand.KOFAKBER_UT);

        // Act
        migrerFaktaTjeneste.migrerFakta();

        // Assert
        Optional<BeregningsgrunnlagGrunnlagEntitet> beregningsgrunnlagGrunnlagEntitet = repository.hentBeregningsgrunnlagGrunnlagEntitet(koblingEntitet.getId());
        assertThat(beregningsgrunnlagGrunnlagEntitet).isPresent();
        assertThat(beregningsgrunnlagGrunnlagEntitet.get().getFaktaAggregat()).isPresent();
        assertThat(beregningsgrunnlagGrunnlagEntitet.get().getFaktaAggregat().get().getFaktaArbeidsforhold().size()).isEqualTo(1);
        FaktaArbeidsforholdEntitet faktaArbeidsforholdEntitet = beregningsgrunnlagGrunnlagEntitet.get().getFaktaAggregat().get().getFaktaArbeidsforhold().get(0);
        assertThat(faktaArbeidsforholdEntitet.getArbeidsgiver().getOrgnr()).isEqualTo(ARBEIDSGIVER.getOrgnr());
        assertThat(faktaArbeidsforholdEntitet.getErTidsbegrenset()).isTrue();
    }

    private KoblingEntitet lagKobling() {
        AktørId aktørId = new AktørId("1234123412341");
        KoblingReferanse koblingReferanse = new KoblingReferanse(UUID.randomUUID());
        Saksnummer saksnummer = new Saksnummer("1234");
        KoblingEntitet koblingEntitet = new KoblingEntitet(koblingReferanse, YtelseTyperKalkulusStøtter.OMSORGSPENGER, saksnummer, aktørId);
        koblingRepository.lagre(koblingEntitet);
        return koblingEntitet;
    }

    private BeregningsgrunnlagGrunnlagBuilder lagGrunnlagMedMottarYtelseForFL() {
        BeregningsgrunnlagEntitet bg = BeregningsgrunnlagEntitet.builder()
                .medSkjæringstidspunkt(STP)
                .build();
        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder().medBeregningsgrunnlagPeriode(STP, TIDENES_ENDE)
                .build(bg);
        BeregningsgrunnlagPrStatusOgAndel.builder().medAktivitetStatus(AktivitetStatus.FRILANSER)
                .medMottarYtelse(true, AktivitetStatus.FRILANSER)
                .build(periode);
        return BeregningsgrunnlagGrunnlagBuilder.oppdatere(Optional.empty())
                .medRegisterAktiviteter(BeregningAktivitetAggregatEntitet.builder()
                        .leggTilAktivitet(BeregningAktivitetEntitet.builder()
                                .medOpptjeningAktivitetType(OpptjeningAktivitetType.FRILANS)
                                .medPeriode(IntervallEntitet.fraOgMed(STP.minusMonths(10))).build())
                        .medSkjæringstidspunktOpptjening(STP).build())
                .medBeregningsgrunnlag(bg);
    }

    private BeregningsgrunnlagGrunnlagBuilder lagGrunnlagMedTidsbegrensetArbeid() {
        BeregningsgrunnlagEntitet bg = BeregningsgrunnlagEntitet.builder()
                .medSkjæringstidspunkt(STP)
                .build();
        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder().medBeregningsgrunnlagPeriode(STP, TIDENES_ENDE)
                .build(bg);
        BeregningsgrunnlagPrStatusOgAndel.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medBGAndelArbeidsforhold(BGAndelArbeidsforhold.builder().medArbeidsgiver(ARBEIDSGIVER)
                .medTidsbegrensetArbeidsforhold(true))
                .build(periode);
        return BeregningsgrunnlagGrunnlagBuilder.oppdatere(Optional.empty())
                .medRegisterAktiviteter(BeregningAktivitetAggregatEntitet.builder()
                        .leggTilAktivitet(BeregningAktivitetEntitet.builder()
                                .medOpptjeningAktivitetType(OpptjeningAktivitetType.ARBEID)
                                .medArbeidsgiver(ARBEIDSGIVER)
                                .medPeriode(IntervallEntitet.fraOgMed(STP.minusMonths(10))).build())
                        .medSkjæringstidspunktOpptjening(STP).build())
                .medBeregningsgrunnlag(bg);
    }

}
