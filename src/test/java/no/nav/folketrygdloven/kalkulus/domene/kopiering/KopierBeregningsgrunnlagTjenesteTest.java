package no.nav.folketrygdloven.kalkulus.domene.kopiering;

import static no.nav.folketrygdloven.kalkulus.domene.felles.jpa.AbstractIntervall.TIDENES_ENDE;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningSteg;

import no.nav.foreldrepenger.kalkulus.kontrakt.request.input.foreldrepenger.ForeldrepengerGrunnlag;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import no.nav.folketrygdloven.kalkulus.migreringer.dbstoette.JpaExtension;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.AktivitetAggregatEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.AndelArbeidsforholdEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagAndelEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagBuilder;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagPeriodeEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.AktørId;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Beløp;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.InternArbeidsforholdRef;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.KoblingReferanse;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Saksnummer;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Årsgrunnlag;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.fordeling.FordelingGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.fordeling.RefusjonsandelEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.fordeling.RefusjonsperiodeEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.domene.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.input.KalkulatorInputDto;
import no.nav.folketrygdloven.kalkulus.domene.kobling.KoblingTjeneste;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.kodeverk.RefusjonKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.RefusjonUtfall;
import no.nav.folketrygdloven.kalkulus.kodeverk.RefusjonUtfallÅrsak;
import no.nav.folketrygdloven.kalkulus.domene.tjeneste.avklaringsbehov.AvklaringsbehovRepository;
import no.nav.folketrygdloven.kalkulus.domene.tjeneste.avklaringsbehov.AvklaringsbehovTjeneste;
import no.nav.folketrygdloven.kalkulus.domene.tjeneste.beregningsgrunnlag.BeregningsgrunnlagRepository;
import no.nav.folketrygdloven.kalkulus.domene.tjeneste.fordeling.FordelingsgrunnlagRepository;
import no.nav.folketrygdloven.kalkulus.domene.tjeneste.kobling.KoblingRepository;
import no.nav.vedtak.felles.testutilities.db.EntityManagerAwareTest;
import no.nav.vedtak.sikkerhet.kontekst.BasisKontekst;
import no.nav.vedtak.sikkerhet.kontekst.KontekstHolder;

@ExtendWith({JpaExtension.class})
class KopierBeregningsgrunnlagTjenesteTest extends EntityManagerAwareTest {

    public static final Saksnummer SAK = new Saksnummer("SAK");
    public static final AktørId AKTØR_ID = new AktørId("9999999999999");

    public static final LocalDate STP = LocalDate.now();
    private BeregningsgrunnlagRepository repository;
    private KoblingRepository koblingRepository;
    private AvklaringsbehovTjeneste avklaringsbehovTjeneste;
    private AvklaringsbehovRepository avklaringsbehovRepository;
    private FordelingsgrunnlagRepository fordelingsgrunnlagRepository;

    private KopierBeregningsgrunnlagTjeneste tjeneste;


    @BeforeEach
    void setUp() {
        repository = new BeregningsgrunnlagRepository(getEntityManager());
        koblingRepository = new KoblingRepository(getEntityManager());
        koblingRepository = new KoblingRepository(getEntityManager());
        avklaringsbehovRepository = new AvklaringsbehovRepository(getEntityManager());
        avklaringsbehovTjeneste = new AvklaringsbehovTjeneste(avklaringsbehovRepository, koblingRepository);
        fordelingsgrunnlagRepository = new FordelingsgrunnlagRepository(getEntityManager());
        var koblingTjeneste = new KoblingTjeneste(koblingRepository);
        tjeneste = new KopierBeregningsgrunnlagTjeneste(koblingTjeneste, repository, avklaringsbehovTjeneste, fordelingsgrunnlagRepository);
    }


    @Test
    void skal_kopiere_grunnlag_til_ny_kobling() {
        KontekstHolder.setKontekst(BasisKontekst.forProsesstaskUtenSystembruker());
        var originalKoblingReferanse = new KoblingReferanse(UUID.randomUUID());
        var originalKobling = new KoblingEntitet(originalKoblingReferanse, FagsakYtelseType.FORELDREPENGER, SAK, AKTØR_ID);
        koblingRepository.lagre(originalKobling);
        koblingRepository.markerKoblingSomAvsluttet(originalKobling);
        assertThat(fordelingsgrunnlagRepository.hentAktivtGrunnlag(originalKobling.getId())).isEmpty();

        avklaringsbehovTjeneste.opprettEllerGjennopprettAvklaringsbehov(originalKobling.getId(), AvklaringsbehovDefinisjon.AVKLAR_AKTIVITETER);
        avklaringsbehovTjeneste.løsAvklaringsbehov(originalKobling.getId(), AvklaringsbehovDefinisjon.AVKLAR_AKTIVITETER, "ok");

        var gr1 = repository.lagre(originalKobling.getId(), byggGrunnlag(200000, 100000), BeregningsgrunnlagTilstand.FASTSATT);

        LocalDateTime førKopiering = LocalDateTime.now();
        var nyReferanse = new KoblingReferanse(UUID.randomUUID());
        tjeneste.kopierFastsattBeregningsgrunnlag(nyReferanse, originalKoblingReferanse, SAK);

        var alleKoblinger = koblingRepository.hentAlleKoblingerForSaksnummer(SAK);
        assertThat(alleKoblinger.size()).isEqualTo(2);
        var nyKobling = alleKoblinger.stream().filter(k -> k.getKoblingReferanse().equals(nyReferanse)).findFirst();
        assertThat(nyKobling).isPresent();

        var grNyKobling = repository.hentBeregningsgrunnlagGrunnlagEntitet(nyKobling.get().getId());
        assertThat(finnBrutto(grNyKobling.get())).isEqualTo(finnBrutto(gr1));

        var nyeAvklaringsbehov = avklaringsbehovRepository.hentAvklaringsbehovForKobling(nyKobling.get().getId());
        assertThat(nyeAvklaringsbehov.size()).isEqualTo(1);
        var nyAvklaringsbehov = nyeAvklaringsbehov.get(0);
        assertThat(nyAvklaringsbehov.getBegrunnelse()).isEqualTo("ok");
        assertThat(nyAvklaringsbehov.getDefinisjon()).isEqualTo(AvklaringsbehovDefinisjon.AVKLAR_AKTIVITETER);
        assertThat(nyAvklaringsbehov.getStatus()).isEqualTo(AvklaringsbehovStatus.UTFØRT);
        assertThat(nyAvklaringsbehov.getVurdertAv()).isEqualTo("srvvtp");
        assertThat(nyAvklaringsbehov.getVurdertTidspunkt()).isBefore(førKopiering);
    }

    @Test
    void skal_kopiere_aktivt_fordelingsgrunnlag_til_ny_kobling() {
        KontekstHolder.setKontekst(BasisKontekst.forProsesstaskUtenSystembruker());
        var originalKoblingReferanse = new KoblingReferanse(UUID.randomUUID());
        var originalKobling = new KoblingEntitet(originalKoblingReferanse, FagsakYtelseType.FORELDREPENGER, SAK, AKTØR_ID);
        koblingRepository.lagre(originalKobling);
        koblingRepository.markerKoblingSomAvsluttet(originalKobling);
        repository.lagre(originalKobling.getId(), byggGrunnlag(200000, 100000), BeregningsgrunnlagTilstand.FASTSATT);

        var nyReferanse = new KoblingReferanse(UUID.randomUUID());
        var eksisterendeNyKobling = new KoblingEntitet(nyReferanse, FagsakYtelseType.FORELDREPENGER, SAK, AKTØR_ID);
        koblingRepository.lagre(eksisterendeNyKobling);
        var gammeltFordelingsgrunnlag = fordelingsgrunnlagRepository.lagre(eksisterendeNyKobling.getId(), FordelingGrunnlagEntitet.builder());

        var arbeidsforholdRef = InternArbeidsforholdRef.nyRef();
        var kildeAndel = new RefusjonsandelEntitet(Arbeidsgiver.virksomhet("999999999"), arbeidsforholdRef,
            RefusjonKilde.INNTEKTSMELDING, "123456789");
        kildeAndel.leggTilRefusjonsperiode(new RefusjonsperiodeEntitet(
            IntervallEntitet.fraOgMedTilOgMed(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31)),
            new Beløp("12345.67"), RefusjonUtfall.INNVILGET, null));
        kildeAndel.leggTilRefusjonsperiode(new RefusjonsperiodeEntitet(
            IntervallEntitet.fraOgMed(LocalDate.of(2026, 2, 1)), new Beløp("9876.54"), RefusjonUtfall.AVSLÅTT,
            RefusjonUtfallÅrsak.IKKE_INNENFOR_FRIST));
        var kildeGrunnlag = FordelingGrunnlagEntitet.builder()
            .leggTilRefusjonsandel(kildeAndel)
            .leggTilRefusjonsandel(new RefusjonsandelEntitet(Arbeidsgiver.person(new AktørId("9999999999998")),
                InternArbeidsforholdRef.nullRef(), RefusjonKilde.SAKSBEHANDLER, null));
        var lagretKildeGrunnlag = fordelingsgrunnlagRepository.lagre(originalKobling.getId(), kildeGrunnlag);

        tjeneste.kopierFastsattBeregningsgrunnlag(nyReferanse, originalKoblingReferanse, SAK);
        getEntityManager().flush();
        getEntityManager().clear();

        var aktivKildeGrunnlag = fordelingsgrunnlagRepository.hentAktivtGrunnlag(originalKobling.getId()).orElseThrow();
        var kopiertGrunnlag = fordelingsgrunnlagRepository.hentAktivtGrunnlag(eksisterendeNyKobling.getId()).orElseThrow();
        assertThat(aktivKildeGrunnlag.getId()).isEqualTo(lagretKildeGrunnlag.getId());
        assertThat(aktivKildeGrunnlag.erAktivt()).isTrue();
        assertThat(kopiertGrunnlag.getId()).isNotEqualTo(aktivKildeGrunnlag.getId());
        assertThat(kopiertGrunnlag.getKoblingId()).isEqualTo(eksisterendeNyKobling.getId());
        assertThat(getEntityManager().find(FordelingGrunnlagEntitet.class, gammeltFordelingsgrunnlag.getId()).erAktivt()).isFalse();

        var kildeAndeler = aktivKildeGrunnlag.getRefusjonsandeler();
        var kopierteAndeler = kopiertGrunnlag.getRefusjonsandeler();
        assertThat(kopierteAndeler).hasSize(2);
        var kildeAndelEtterKopiering = kildeAndeler.stream()
            .filter(andel -> andel.getKilde() == RefusjonKilde.INNTEKTSMELDING).findFirst().orElseThrow();
        var kopiertAndel = kopierteAndeler.stream()
            .filter(andel -> andel.getKilde() == RefusjonKilde.INNTEKTSMELDING).findFirst().orElseThrow();
        assertThat(kopiertAndel.getArbeidsgiver()).isEqualTo(kildeAndelEtterKopiering.getArbeidsgiver());
        assertThat(kopiertAndel.getArbeidsforholdRef()).isEqualTo(arbeidsforholdRef);
        assertThat(kopiertAndel.getKilde()).isEqualTo(RefusjonKilde.INNTEKTSMELDING);
        assertThat(kopiertAndel.getJournalpostId()).contains("123456789");
        assertThat(kopiertAndel.getRefusjonsperioder()).hasSize(2);
        var innvilgetPeriode = kopiertAndel.getRefusjonsperioder().stream()
            .filter(periode -> periode.getUtfall() == RefusjonUtfall.INNVILGET).findFirst().orElseThrow();
        assertThat(innvilgetPeriode.getPeriode()).isEqualTo(IntervallEntitet.fraOgMedTilOgMed(
            LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31)));
        assertThat(innvilgetPeriode.getRefusjonPrMnd()).isEqualTo(new Beløp("12345.67"));
        assertThat(innvilgetPeriode.getUtfallÅrsak()).isNull();
        var avslåttPeriode = kopiertAndel.getRefusjonsperioder().stream()
            .filter(periode -> periode.getUtfall() == RefusjonUtfall.AVSLÅTT).findFirst().orElseThrow();
        assertThat(avslåttPeriode.getPeriode()).isEqualTo(IntervallEntitet.fraOgMed(LocalDate.of(2026, 2, 1)));
        assertThat(avslåttPeriode.getRefusjonPrMnd()).isEqualTo(new Beløp("9876.54"));
        assertThat(avslåttPeriode.getUtfallÅrsak()).isEqualTo(RefusjonUtfallÅrsak.IKKE_INNENFOR_FRIST);
        var saksbehandlerandel = kopierteAndeler.stream()
            .filter(andel -> andel.getKilde() == RefusjonKilde.SAKSBEHANDLER).findFirst().orElseThrow();
        assertThat(saksbehandlerandel.getArbeidsgiver()).isEqualTo(Arbeidsgiver.person(new AktørId("9999999999998")));
        assertThat(saksbehandlerandel.getArbeidsforholdRef().gjelderForSpesifiktArbeidsforhold()).isFalse();
        assertThat(saksbehandlerandel.getJournalpostId()).isEmpty();

        var kildeAndelIder = getEntityManager().createQuery(
            "select andel.id from RefusjonsandelEntitet andel where andel.fordelingGrunnlag.id = :grunnlagId", Long.class)
            .setParameter("grunnlagId", aktivKildeGrunnlag.getId()).getResultList();
        var kopiertAndelIder = getEntityManager().createQuery(
            "select andel.id from RefusjonsandelEntitet andel where andel.fordelingGrunnlag.id = :grunnlagId", Long.class)
            .setParameter("grunnlagId", kopiertGrunnlag.getId()).getResultList();
        assertThat(kopiertAndelIder).doesNotContainAnyElementsOf(kildeAndelIder);
        var kildePeriodeIder = getEntityManager().createQuery(
            "select periode.id from RefusjonsperiodeEntitet periode where periode.refusjonsandel.fordelingGrunnlag.id = :grunnlagId", Long.class)
            .setParameter("grunnlagId", aktivKildeGrunnlag.getId()).getResultList();
        var kopiertPeriodeIder = getEntityManager().createQuery(
            "select periode.id from RefusjonsperiodeEntitet periode where periode.refusjonsandel.fordelingGrunnlag.id = :grunnlagId", Long.class)
            .setParameter("grunnlagId", kopiertGrunnlag.getId()).getResultList();
        assertThat(kopiertPeriodeIder).hasSize(2).doesNotContainAnyElementsOf(kildePeriodeIder);
    }

    @Test
    void skal_kopiere_med_nytt_grunnbeløp() {
        KontekstHolder.setKontekst(BasisKontekst.forProsesstaskUtenSystembruker());
        var originalKoblingReferanse = new KoblingReferanse(UUID.randomUUID());
        var originalKobling = new KoblingEntitet(originalKoblingReferanse, FagsakYtelseType.FORELDREPENGER, SAK, AKTØR_ID);
        koblingRepository.lagre(originalKobling);

        repository.lagre(originalKobling.getId(), byggGrunnlag(200000, 124_028), BeregningsgrunnlagTilstand.OPPRETTET);
        repository.lagre(originalKobling.getId(), byggGrunnlag(200000, 124_028), BeregningsgrunnlagTilstand.FASTSATT_BEREGNINGSAKTIVITETER);
        repository.lagre(originalKobling.getId(), byggGrunnlag(200000, 124_028), BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);
        koblingRepository.markerKoblingSomAvsluttet(originalKobling);

        var nyReferanse = new KoblingReferanse(UUID.randomUUID());
        var input = new KalkulatorInputDto(null, null, LocalDate.of(2025, 6, 1),
            new ForeldrepengerGrunnlag(BigDecimal.valueOf(100), false, null, List.of(), LocalDate.of(2025, 6, 1)));
        tjeneste.kopierBeregningsgrunlagForStartISteg(nyReferanse, originalKoblingReferanse, SAK, BeregningSteg.FORS_BERGRUNN, input);

        var alleKoblinger = koblingRepository.hentAlleKoblingerForSaksnummer(SAK);
        assertThat(alleKoblinger).hasSize(2);
        var nyKobling = alleKoblinger.stream().filter(k -> k.getKoblingReferanse().equals(nyReferanse)).findFirst();
        assertThat(nyKobling).isPresent();

        var grGammelKobling = repository.hentBeregningsgrunnlagGrunnlagEntitet(originalKobling.getId());
        assertThat(grGammelKobling).isPresent();
        assertThat(grGammelKobling.get().getBeregningsgrunnlag().get().getGrunnbeløp().getVerdi()).isEqualTo(BigDecimal.valueOf(124_028));

        var grNyKobling = repository.hentBeregningsgrunnlagGrunnlagEntitet(nyKobling.get().getId());
        assertThat(grNyKobling).isPresent();
        assertThat(grNyKobling.get().getBeregningsgrunnlag().get().getGrunnbeløp().getVerdi()).isEqualTo(BigDecimal.valueOf(130_160));
    }

    @Test
    void skal_kopiere_grunnlag_fra_forrige_original_kobling_til_ny_kobling() {
        var forrigeOriginalKoblingReferanse = new KoblingReferanse(UUID.randomUUID());
        var forrigeOriginalKobling = new KoblingEntitet(forrigeOriginalKoblingReferanse, FagsakYtelseType.FORELDREPENGER, SAK, AKTØR_ID);
        koblingRepository.lagre(forrigeOriginalKobling);
        koblingRepository.markerKoblingSomAvsluttet(forrigeOriginalKobling);
        var originalKoblingReferanse = UUID.randomUUID();
        var originalKobling = new KoblingEntitet(new KoblingReferanse(originalKoblingReferanse), FagsakYtelseType.FORELDREPENGER, SAK,
            AKTØR_ID);
        koblingRepository.lagre(originalKobling);
        koblingRepository.markerKoblingSomAvsluttet(originalKobling);

        var gr1 = repository.lagre(forrigeOriginalKobling.getId(), byggGrunnlag(200000, 100000), BeregningsgrunnlagTilstand.FASTSATT);

        var nyReferanse = new KoblingReferanse(UUID.randomUUID());
        tjeneste.kopierFastsattBeregningsgrunnlag(nyReferanse, forrigeOriginalKoblingReferanse, SAK);

        var alleKoblinger = koblingRepository.hentAlleKoblingerForSaksnummer(SAK);
        assertThat(alleKoblinger).hasSize(3);
        var nyKobling = alleKoblinger.stream().filter(k -> k.getKoblingReferanse().equals(nyReferanse)).findFirst();
        assertThat(nyKobling).isPresent();

        var grNyKobling = repository.hentBeregningsgrunnlagGrunnlagEntitet(nyKobling.get().getId());
        assertThat(finnBrutto(grNyKobling.get())).isEqualTo(finnBrutto(gr1));
    }

    private BigDecimal finnBrutto(BeregningsgrunnlagGrunnlagEntitet grNyKobling1) {
        return grNyKobling1.getBeregningsgrunnlag().get().getBeregningsgrunnlagPerioder().get(0).getBruttoPrÅr().getVerdi();
    }

    private BeregningsgrunnlagGrunnlagBuilder byggGrunnlag(int verdi, int grunnbeløp) {
        var bgp = BeregningsgrunnlagPeriodeEntitet.builder()
            .medBeregningsgrunnlagPeriode(STP, TIDENES_ENDE)
            .leggTilBeregningsgrunnlagAndel(BeregningsgrunnlagAndelEntitet.builder()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medAndelsnr(1L)
                .medAndelArbeidsforhold(AndelArbeidsforholdEntitet.builder().medArbeidsgiver(Arbeidsgiver.virksomhet("999999999")).build())
                .medGrunnlagPrÅr(lagBeregnet(verdi)))
            .build();
        BeregningsgrunnlagEntitet bg = BeregningsgrunnlagEntitet.builder().medSkjæringstidspunkt(STP)
            .medGrunnbeløp(new Beløp(BigDecimal.valueOf(grunnbeløp)))
            .leggTilBeregningsgrunnlagPeriode(bgp)
            .build();
        var gr = BeregningsgrunnlagGrunnlagBuilder.kopiere(Optional.empty())
            .medRegisterAktiviteter(AktivitetAggregatEntitet.builder().medSkjæringstidspunktOpptjening(STP).build())
            .medBeregningsgrunnlag(bg);
        return gr;
    }


    private Årsgrunnlag lagBeregnet(int verdi) {
        return new Årsgrunnlag(new Beløp(verdi), null, null, null, null, new Beløp(verdi));
    }
}
