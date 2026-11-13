package no.nav.folketrygdloven.kalkulus.domene.tjeneste.fordeling;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import no.nav.folketrygdloven.kalkulator.modell.refusjon.FordelingGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.refusjon.RefusjonsandelDto;
import no.nav.folketrygdloven.kalkulator.modell.refusjon.RefusjonsperiodeDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.AktørId;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.KoblingReferanse;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Saksnummer;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.fordeling.FordelingGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.domene.mapTilEntitet.FordelingsgrunnlagMapper;
import no.nav.folketrygdloven.kalkulus.felles.JournalpostId;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.kodeverk.RefusjonKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.RefusjonUtfall;
import no.nav.folketrygdloven.kalkulus.migreringer.dbstoette.JpaExtension;
import no.nav.vedtak.felles.testutilities.db.EntityManagerAwareTest;

@ExtendWith(JpaExtension.class)
class FordelingsgrunnlagRepositoryTest extends EntityManagerAwareTest {

    private FordelingsgrunnlagRepository repository;

    @BeforeEach
    void setUp() {
        repository = new FordelingsgrunnlagRepository(getEntityManager());
    }

    @Test
    void skal_lagre_og_hente_mappet_grunnlag_med_andeler_og_perioder() {
        var koblingId = lagKobling();
        var lagret = repository.lagre(koblingId, FordelingsgrunnlagMapper.map(lagGrunnlag(20000)));
        getEntityManager().clear();

        var hentet = repository.hentAktivtGrunnlag(koblingId).orElseThrow();
        assertThat(hentet.getId()).isEqualTo(lagret.getId());
        assertThat(hentet.getKoblingId()).isEqualTo(koblingId);
        var andel = hentet.getRefusjonsandeler().getFirst();
        assertThat(andel.getArbeidsgiver().getOrgnr()).isEqualTo("999999999");
        assertThat(andel.getJournalpostId()).contains("123456789");
        assertThat(andel.getRefusjonsperioder()).hasSize(1);
        assertThat(andel.getRefusjonsperioder().getFirst().getRefusjonPrMnd().getVerdi()).isEqualByComparingTo("20000");
    }

    @Test
    void skal_deaktivere_tidligere_grunnlag_og_beholde_historiske_krav() {
        var koblingId = lagKobling();
        var gammelt = repository.lagre(koblingId, FordelingsgrunnlagMapper.map(lagGrunnlag(20000)));
        var nytt = repository.lagre(koblingId, FordelingsgrunnlagMapper.map(lagGrunnlag(15000)));
        getEntityManager().clear();

        var hentetGammelt = getEntityManager().find(FordelingGrunnlagEntitet.class, gammelt.getId());
        assertThat(hentetGammelt.erAktivt()).isFalse();
        assertThat(hentetGammelt.getRefusjonsandeler().getFirst().getRefusjonsperioder().getFirst().getRefusjonPrMnd().getVerdi())
            .isEqualByComparingTo("20000");
        var hentetNytt = repository.hentAktivtGrunnlag(koblingId).orElseThrow();
        assertThat(hentetNytt.getId()).isEqualTo(nytt.getId()).isNotEqualTo(gammelt.getId());
        assertThat(hentetNytt.getRefusjonsandeler().getFirst().getRefusjonsperioder().getFirst().getRefusjonPrMnd().getVerdi())
            .isEqualByComparingTo("15000");
    }

    @Test
    void skal_kunne_erstatte_med_tomt_grunnlag_uten_aa_endre_annen_kobling() {
        var koblingId = lagKobling();
        var annenKoblingId = lagKobling();
        assertThat(repository.hentAktivtGrunnlag(koblingId)).isEmpty();
        repository.lagre(koblingId, FordelingsgrunnlagMapper.map(lagGrunnlag(20000)));
        var annetGrunnlag = repository.lagre(annenKoblingId, FordelingsgrunnlagMapper.map(lagGrunnlag(10000)));

        repository.lagre(koblingId, FordelingGrunnlagEntitet.builder());
        getEntityManager().clear();

        assertThat(repository.hentAktivtGrunnlag(koblingId).orElseThrow().getRefusjonsandeler()).isEmpty();
        var hentetAnnetGrunnlag = repository.hentAktivtGrunnlag(annenKoblingId).orElseThrow();
        assertThat(hentetAnnetGrunnlag.getId()).isEqualTo(annetGrunnlag.getId());
        assertThat(hentetAnnetGrunnlag.getRefusjonsandeler()).hasSize(1);
    }

    private FordelingGrunnlagDto lagGrunnlag(int beløp) {
        var periode = new RefusjonsperiodeDto(Intervall.fraOgMed(LocalDate.of(2026, 1, 1)),
            Beløp.fra(BigDecimal.valueOf(beløp)), RefusjonUtfall.INNVILGET, null, null);
        var andel = new RefusjonsandelDto(Arbeidsgiver.virksomhet("999999999"), null, RefusjonKilde.INNTEKTSMELDING,
            new JournalpostId("123456789"), List.of(periode));
        return new FordelingGrunnlagDto(List.of(andel));
    }

    private Long lagKobling() {
        var kobling = new KoblingEntitet(new KoblingReferanse(UUID.randomUUID()), FagsakYtelseType.FORELDREPENGER,
            new Saksnummer("1234"), new AktørId("9999999999999"));
        getEntityManager().persist(kobling);
        getEntityManager().flush();
        return kobling.getId();
    }
}
