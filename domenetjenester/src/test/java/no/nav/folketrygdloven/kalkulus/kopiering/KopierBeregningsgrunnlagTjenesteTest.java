package no.nav.folketrygdloven.kalkulus.kopiering;

import static no.nav.folketrygdloven.kalkulus.felles.jpa.AbstractIntervall.TIDENES_ENDE;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import no.nav.folketrygdloven.kalkulus.beregning.v1.ForeldrepengerGrunnlag;
import no.nav.folketrygdloven.kalkulus.dbstoette.JpaExtension;
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
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.KoblingReferanse;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Saksnummer;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Årsgrunnlag;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.felles.v1.KalkulatorInputDto;
import no.nav.folketrygdloven.kalkulus.kobling.KoblingTjeneste;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningSteg;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.tjeneste.avklaringsbehov.AvklaringsbehovRepository;
import no.nav.folketrygdloven.kalkulus.tjeneste.avklaringsbehov.AvklaringsbehovTjeneste;
import no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag.BeregningsgrunnlagRepository;
import no.nav.folketrygdloven.kalkulus.tjeneste.kobling.KoblingRepository;
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

    private KopierBeregningsgrunnlagTjeneste tjeneste;


    @BeforeEach
    public void setUp() {
        repository = new BeregningsgrunnlagRepository(getEntityManager());
        koblingRepository = new KoblingRepository(getEntityManager());
        koblingRepository = new KoblingRepository(getEntityManager());
        avklaringsbehovRepository = new AvklaringsbehovRepository(getEntityManager());
        avklaringsbehovTjeneste = new AvklaringsbehovTjeneste(avklaringsbehovRepository, koblingRepository);
        var koblingTjeneste = new KoblingTjeneste(koblingRepository);
        tjeneste = new KopierBeregningsgrunnlagTjeneste(koblingTjeneste, repository, avklaringsbehovTjeneste);
    }


    @Test
    void skal_kopiere_grunnlag_til_ny_kobling() {
        KontekstHolder.setKontekst(BasisKontekst.forProsesstaskUtenSystembruker());
        var originalKoblingReferanse = new KoblingReferanse(UUID.randomUUID());
        var originalKobling = new KoblingEntitet(originalKoblingReferanse, FagsakYtelseType.FORELDREPENGER, SAK, AKTØR_ID);
        koblingRepository.lagre(originalKobling);

        avklaringsbehovTjeneste.opprettEllerGjennopprettAvklaringsbehov(originalKobling.getId(), AvklaringsbehovDefinisjon.AVKLAR_AKTIVITETER);
        avklaringsbehovTjeneste.løsAvklaringsbehov(originalKobling.getId(), AvklaringsbehovDefinisjon.AVKLAR_AKTIVITETER, "ok");

        var gr1 = repository.lagre(originalKobling.getId(), byggGrunnlag(200000, 100000), BeregningsgrunnlagTilstand.FASTSATT);

        LocalDateTime førKopiering = LocalDateTime.now();
        var nyReferanse = new KoblingReferanse(UUID.randomUUID());
        tjeneste.kopierGrunnlagOgOpprettKoblinger(nyReferanse, originalKoblingReferanse, SAK, BeregningSteg.FAST_BERGRUNN);

        var alleKoblinger = koblingRepository.hentAlleKoblingerForSaksnummer(SAK);
        assertThat(alleKoblinger.size()).isEqualTo(2);
        var nyKobling = alleKoblinger.stream().filter(k -> k.getKoblingReferanse().equals(nyReferanse)).findFirst();
        assertThat(nyKobling).isPresent();

        var grNyKobling = repository.hentBeregningsgrunnlagGrunnlagEntitet(nyKobling.get().getId());
        assertThat(finnBrutto(grNyKobling.get())).isEqualTo(finnBrutto(gr1));

        var nyeAvklaringsbehov = avklaringsbehovRepository.hentAvklaringsbehovForKobling(nyKobling.get());
        assertThat(nyeAvklaringsbehov.size()).isEqualTo(1);
        var nyAvklaringsbehov = nyeAvklaringsbehov.get(0);
        assertThat(nyAvklaringsbehov.getBegrunnelse()).isEqualTo("ok");
        assertThat(nyAvklaringsbehov.getDefinisjon()).isEqualTo(AvklaringsbehovDefinisjon.AVKLAR_AKTIVITETER);
        assertThat(nyAvklaringsbehov.getStatus()).isEqualTo(AvklaringsbehovStatus.UTFØRT);
        assertThat(nyAvklaringsbehov.getVurdertAv()).isEqualTo("srvvtp");
        assertThat(nyAvklaringsbehov.getVurdertTidspunkt()).isBefore(førKopiering);
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

        var nyReferanse = new KoblingReferanse(UUID.randomUUID());
        var input = new KalkulatorInputDto(null, null, LocalDate.of(2025, 6, 1));
        input.medYtelsespesifiktGrunnlag(new ForeldrepengerGrunnlag(BigDecimal.valueOf(100), false, null, List.of(), LocalDate.of(2025, 6, 1)));
        tjeneste.kopierGrunnlagForGregulering(nyReferanse, originalKoblingReferanse, SAK, input);

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
        var originalKoblingReferanse = UUID.randomUUID();
        var originalKobling = new KoblingEntitet(new KoblingReferanse(originalKoblingReferanse), FagsakYtelseType.FORELDREPENGER, SAK,
            AKTØR_ID);
        koblingRepository.lagre(originalKobling);

        var gr1 = repository.lagre(forrigeOriginalKobling.getId(), byggGrunnlag(200000, 100000), BeregningsgrunnlagTilstand.FASTSATT);

        var nyReferanse = new KoblingReferanse(UUID.randomUUID());
        tjeneste.kopierGrunnlagOgOpprettKoblinger(nyReferanse, forrigeOriginalKoblingReferanse, SAK, BeregningSteg.FAST_BERGRUNN);

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
