package no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

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
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
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
    public void skal_hente_beregningsgrunnlag_for_referanse() {
        AktørId aktørId = new AktørId("9999999999999");
        KoblingReferanse koblingReferanse = new KoblingReferanse(UUID.randomUUID());
        Saksnummer saksnummer = new Saksnummer("1234");

        KoblingEntitet koblingEntitet = new KoblingEntitet(koblingReferanse,
                FagsakYtelseType.PLEIEPENGER_NÆRSTÅENDE, saksnummer, aktørId);
        koblingRepository.lagre(koblingEntitet);

        Long koblingId = koblingEntitet.getId();

        BeregningsgrunnlagEntitet build = BeregningsgrunnlagEntitet.builder()
                .medSkjæringstidspunkt(LocalDate.now())
                .build();
        BeregningsgrunnlagGrunnlagBuilder builder = BeregningsgrunnlagGrunnlagBuilder
                .kopiere(Optional.empty())
                .medBeregningsgrunnlag(build)
                .medRegisterAktiviteter(
                        BeregningAktivitetAggregatEntitet.builder()
                                .medSkjæringstidspunktOpptjening(LocalDate.now())
                                .leggTilAktivitet(BeregningAktivitetEntitet.builder()
                                        .medOpptjeningAktivitetType(
                                                OpptjeningAktivitetType.FRILANS)
                                        .medPeriode(IntervallEntitet
                                                .fraOgMedTilOgMed(
                                                        LocalDate.now(),
                                                        LocalDate.now().plusMonths(
                                                                1)))
                                        .build())
                                .build());

        BeregningsgrunnlagGrunnlagEntitet lagretGrunnlag = repository.lagre(koblingId, builder,
                BeregningsgrunnlagTilstand.OPPRETTET);

        GrunnlagReferanse grunnlagReferanse = lagretGrunnlag.getGrunnlagReferanse();

        Optional<BeregningsgrunnlagGrunnlagEntitet> grunnlagForReferanse = repository
                .hentBeregningsgrunnlagGrunnlagEntitetForReferanse(koblingId,
                        grunnlagReferanse.getReferanse());

        assertThat(grunnlagForReferanse).isPresent();
        assertThat(grunnlagForReferanse.get()).isEqualTo(lagretGrunnlag);
    }
}
