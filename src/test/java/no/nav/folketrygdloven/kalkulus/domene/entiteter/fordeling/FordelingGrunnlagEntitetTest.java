package no.nav.folketrygdloven.kalkulus.domene.entiteter.fordeling;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.PersistenceException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.AktørId;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Beløp;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.InternArbeidsforholdRef;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.KoblingReferanse;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Saksnummer;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.domene.felles.jpa.IntervallEntitet;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.kodeverk.RefusjonKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.RefusjonUtfall;
import no.nav.folketrygdloven.kalkulus.kodeverk.RefusjonUtfallÅrsak;
import no.nav.folketrygdloven.kalkulus.migreringer.dbstoette.JpaExtension;
import no.nav.vedtak.felles.testutilities.db.EntityManagerAwareTest;

@ExtendWith(JpaExtension.class)
class FordelingGrunnlagEntitetTest extends EntityManagerAwareTest {

    @Test
    void skal_lagre_og_hente_grunnlag_med_andeler_og_perioder() {
        var koblingId = lagKobling();
        var grunnlag = new FordelingGrunnlagEntitet(koblingId);
        var arbeidsgiver = Arbeidsgiver.virksomhet("999999999");
        var ref = InternArbeidsforholdRef.nyRef();
        var andel = new RefusjonsandelEntitet(arbeidsgiver, ref, RefusjonKilde.INNTEKTSMELDING, "123456789");
        var januar = IntervallEntitet.fraOgMedTilOgMed(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));
        andel.leggTilRefusjonsperiode(new RefusjonsperiodeEntitet(januar, new Beløp("12345.67"), RefusjonUtfall.INNVILGET, null, null));
        var fraFebruar = IntervallEntitet.fraOgMed(LocalDate.of(2026, 2, 1));
        andel.leggTilRefusjonsperiode(new RefusjonsperiodeEntitet(fraFebruar, Beløp.ZERO, RefusjonUtfall.AVSLÅTT,
            RefusjonUtfallÅrsak.IKKE_INNENFOR_FRIST, "Kravet er fremsatt etter fristen."));
        grunnlag.leggTilRefusjonsandel(andel);
        grunnlag.leggTilRefusjonsandel(new RefusjonsandelEntitet(arbeidsgiver, ref, RefusjonKilde.SAKSBEHANDLER, null));
        var personligArbeidsgiver = Arbeidsgiver.person(new AktørId("9999999999998"));
        grunnlag.leggTilRefusjonsandel(new RefusjonsandelEntitet(personligArbeidsgiver, null, RefusjonKilde.SAKSBEHANDLER, null));

        getEntityManager().persist(grunnlag);
        getEntityManager().flush();
        getEntityManager().clear();

        var hentet = getEntityManager().find(FordelingGrunnlagEntitet.class, grunnlag.getId());
        assertThat(hentet.getKoblingId()).isEqualTo(koblingId);
        assertThat(hentet.erAktivt()).isTrue();
        assertThat(hentet.getOpprettetTidspunkt()).isNotNull();
        assertThat(hentet.getRefusjonsandeler()).hasSize(3);
        var hentetAndel = hentet.getRefusjonsandeler().stream().filter(a -> a.getKilde() == RefusjonKilde.INNTEKTSMELDING).findFirst().orElseThrow();
        assertThat(hentetAndel.getArbeidsgiver()).isEqualTo(arbeidsgiver);
        assertThat(hentetAndel.getArbeidsforholdRef()).isEqualTo(ref);
        assertThat(hentetAndel.getJournalpostId()).contains("123456789");
        assertThat(hentetAndel.getRefusjonsperioder()).hasSize(2);
        var innvilget = hentetAndel.getRefusjonsperioder().stream().filter(p -> p.getUtfall() == RefusjonUtfall.INNVILGET).findFirst().orElseThrow();
        assertThat(innvilget.getPeriode()).isEqualTo(januar);
        assertThat(innvilget.getRefusjonPrMnd()).isEqualTo(new Beløp("12345.67"));
        assertThat(innvilget.getUtfallÅrsak()).isNull();
        assertThat(innvilget.getUtfallBegrunnelse()).isNull();
        var avslått = hentetAndel.getRefusjonsperioder().stream().filter(p -> p.getUtfall() == RefusjonUtfall.AVSLÅTT).findFirst().orElseThrow();
        assertThat(avslått.getPeriode()).isEqualTo(fraFebruar);
        assertThat(avslått.getRefusjonPrMnd()).isEqualTo(Beløp.ZERO);
        assertThat(avslått.getUtfallÅrsak()).isEqualTo(RefusjonUtfallÅrsak.IKKE_INNENFOR_FRIST);
        assertThat(avslått.getUtfallBegrunnelse()).isEqualTo("Kravet er fremsatt etter fristen.");
        var personligAndel = hentet.getRefusjonsandeler().stream().filter(a -> a.getArbeidsgiver().erAktørId()).findFirst().orElseThrow();
        assertThat(personligAndel.getArbeidsgiver()).isEqualTo(personligArbeidsgiver);
        assertThat(personligAndel.getArbeidsforholdRef().gjelderForSpesifiktArbeidsforhold()).isFalse();
        assertThat(personligAndel.getJournalpostId()).isEmpty();
    }

    @Test
    void skal_beholde_historikk_ved_nytt_grunnlag() {
        var koblingId = lagKobling();
        var gammelt = new FordelingGrunnlagEntitet(koblingId);
        getEntityManager().persist(gammelt);
        getEntityManager().flush();
        gammelt.setAktiv(false);
        getEntityManager().flush();
        var nytt = new FordelingGrunnlagEntitet(koblingId);
        getEntityManager().persist(nytt);
        getEntityManager().flush();
        getEntityManager().clear();

        assertThat(getEntityManager().find(FordelingGrunnlagEntitet.class, gammelt.getId()).erAktivt()).isFalse();
        assertThat(getEntityManager().find(FordelingGrunnlagEntitet.class, nytt.getId()).erAktivt()).isTrue();
    }

    @Test
    void skal_avvise_flere_aktive_grunnlag_for_samme_kobling() {
        var koblingId = lagKobling();
        getEntityManager().persist(new FordelingGrunnlagEntitet(koblingId));
        getEntityManager().flush();
        getEntityManager().persist(new FordelingGrunnlagEntitet(koblingId));

        assertThatThrownBy(() -> getEntityManager().flush()).isInstanceOf(PersistenceException.class);
    }

    @Test
    void skal_avvise_duplikate_andeler_med_ulike_journalpostider() {
        var grunnlag = new FordelingGrunnlagEntitet(lagKobling());
        var arbeidsgiver = Arbeidsgiver.virksomhet("999999999");
        grunnlag.leggTilRefusjonsandel(new RefusjonsandelEntitet(arbeidsgiver, null, RefusjonKilde.INNTEKTSMELDING, "123"));
        assertThatThrownBy(() -> grunnlag.leggTilRefusjonsandel(
            new RefusjonsandelEntitet(arbeidsgiver, null, RefusjonKilde.INNTEKTSMELDING, "456")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Flere refusjonsandeler med samme arbeidsgiver, arbeidsforholdsreferanse og kilde.");
        assertThat(grunnlag.getRefusjonsandeler()).hasSize(1);
    }

    @ParameterizedTest
    @EnumSource(RefusjonKilde.class)
    void skal_kunne_lagre_journalpost_uavhengig_av_kilde(RefusjonKilde kilde) {
        var grunnlag = new FordelingGrunnlagEntitet(lagKobling());
        var journalpost = kilde == RefusjonKilde.INNTEKTSMELDING ? null : "123";
        grunnlag.leggTilRefusjonsandel(new RefusjonsandelEntitet(Arbeidsgiver.virksomhet("999999999"), null, kilde, journalpost));
        getEntityManager().persist(grunnlag);

        getEntityManager().flush();
        getEntityManager().clear();

        var andel = getEntityManager().find(FordelingGrunnlagEntitet.class, grunnlag.getId()).getRefusjonsandeler().getFirst();
        assertThat(andel.getJournalpostId().orElse(null)).isEqualTo(journalpost);
    }

    private Long lagKobling() {
        var kobling = new KoblingEntitet(new KoblingReferanse(UUID.randomUUID()), FagsakYtelseType.FORELDREPENGER,
            new Saksnummer("1234"), new AktørId("9999999999999"));
        getEntityManager().persist(kobling);
        getEntityManager().flush();
        return kobling.getId();
    }
}
