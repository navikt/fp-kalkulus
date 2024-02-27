package no.nav.folketrygdloven.kalkulus.rest.forvaltning;

import static no.nav.folketrygdloven.kalkulus.felles.tid.AbstractIntervall.TIDENES_ENDE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import no.nav.folketrygdloven.kalkulus.beregning.input.KalkulatorInputTjeneste;
import no.nav.folketrygdloven.kalkulus.beregning.v1.AktivitetGraderingDto;
import no.nav.folketrygdloven.kalkulus.beregning.v1.AndelGraderingDto;
import no.nav.folketrygdloven.kalkulus.beregning.v1.ForeldrepengerGrunnlag;
import no.nav.folketrygdloven.kalkulus.beregning.v1.GraderingDto;
import no.nav.folketrygdloven.kalkulus.beregning.v1.RefusjonskravDatoDto;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.avklaringsbehov.AvklaringsbehovKontrollTjeneste;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningAktivitetAggregatEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagBuilder;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Beløp;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.KoblingReferanse;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Saksnummer;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Årsgrunnlag;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingRelasjon;
import no.nav.folketrygdloven.kalkulus.felles.v1.BeløpDto;
import no.nav.folketrygdloven.kalkulus.felles.v1.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.felles.v1.KalkulatorInputDto;
import no.nav.folketrygdloven.kalkulus.felles.v1.Organisasjon;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.forvaltning.ResetGrunnlagTjeneste;
import no.nav.folketrygdloven.kalkulus.iay.arbeid.v1.AktivitetsAvtaleDto;
import no.nav.folketrygdloven.kalkulus.iay.arbeid.v1.ArbeidDto;
import no.nav.folketrygdloven.kalkulus.iay.arbeid.v1.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulus.iay.inntekt.v1.InntekterDto;
import no.nav.folketrygdloven.kalkulus.iay.inntekt.v1.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulus.iay.inntekt.v1.InntektsmeldingerDto;
import no.nav.folketrygdloven.kalkulus.iay.inntekt.v1.UtbetalingDto;
import no.nav.folketrygdloven.kalkulus.iay.inntekt.v1.UtbetalingsPostDto;
import no.nav.folketrygdloven.kalkulus.iay.v1.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.iay.ytelse.v1.AnvistAndel;
import no.nav.folketrygdloven.kalkulus.iay.ytelse.v1.YtelseAnvistDto;
import no.nav.folketrygdloven.kalkulus.iay.ytelse.v1.YtelseDto;
import no.nav.folketrygdloven.kalkulus.iay.ytelse.v1.YtelserDto;
import no.nav.folketrygdloven.kalkulus.kobling.KoblingTjeneste;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningSteg;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.kodeverk.InntektskildeType;
import no.nav.folketrygdloven.kalkulus.kodeverk.InntektspostType;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseType;
import no.nav.folketrygdloven.kalkulus.kopiering.KopierBeregningsgrunnlagTjeneste;
import no.nav.folketrygdloven.kalkulus.opptjening.v1.OpptjeningAktiviteterDto;
import no.nav.folketrygdloven.kalkulus.opptjening.v1.OpptjeningPeriodeDto;
import no.nav.folketrygdloven.kalkulus.request.v1.KopierBeregningRequest;
import no.nav.folketrygdloven.kalkulus.tjeneste.avklaringsbehov.AvklaringsbehovRepository;
import no.nav.folketrygdloven.kalkulus.tjeneste.avklaringsbehov.AvklaringsbehovTjeneste;
import no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag.BeregningsgrunnlagRepository;
import no.nav.folketrygdloven.kalkulus.tjeneste.extensions.JpaExtension;
import no.nav.folketrygdloven.kalkulus.tjeneste.kobling.KoblingRepository;
import no.nav.folketrygdloven.kalkulus.tjeneste.kobling.LåsRepository;
import no.nav.folketrygdloven.kalkulus.typer.AktørId;
import no.nav.k9.felles.testutilities.db.EntityManagerAwareTest;

@ExtendWith({JpaExtension.class})
class KopierOgResetRestTjenesteTest extends EntityManagerAwareTest {


    public static final Saksnummer SAK = new Saksnummer("SAK");
    public static final AktørId AKTØR_ID = new AktørId("123456789");
    private final InternArbeidsforholdRefDto ref = new InternArbeidsforholdRefDto(UUID.randomUUID().toString());
    private final Periode periode = new Periode(LocalDate.now(), LocalDate.now().plusMonths(2));
    private final Organisasjon organisasjon = new Organisasjon("974652269");
    private final BeløpDto beløpDto = new BeløpDto(BigDecimal.TEN);


    public static final LocalDate STP = LocalDate.now();
    private BeregningsgrunnlagRepository repository;
    private KoblingRepository koblingRepository;
    private KalkulatorInputTjeneste kalkulatorInputTjeneste;

    private KopierOgResetRestTjeneste tjeneste;


    @BeforeEach
    public void setUp() {
        repository = new BeregningsgrunnlagRepository(getEntityManager());
        koblingRepository = new KoblingRepository(getEntityManager());
        koblingRepository = new KoblingRepository(getEntityManager());
        AvklaringsbehovRepository avklaringsbehovRepository = new AvklaringsbehovRepository(getEntityManager());
        AvklaringsbehovKontrollTjeneste avklaringsbehovKontrollTjeneste = new AvklaringsbehovKontrollTjeneste();
        AvklaringsbehovTjeneste avklaringsbehovTjeneste = new AvklaringsbehovTjeneste(avklaringsbehovRepository, koblingRepository, avklaringsbehovKontrollTjeneste);
        var koblingTjeneste = new KoblingTjeneste(koblingRepository, new LåsRepository(getEntityManager()));
        kalkulatorInputTjeneste = new KalkulatorInputTjeneste(repository, koblingTjeneste);
        var kopierBeregningsgrunnlagTjeneste = new KopierBeregningsgrunnlagTjeneste(koblingTjeneste, repository, avklaringsbehovTjeneste, kalkulatorInputTjeneste);
        tjeneste = new KopierOgResetRestTjeneste(kopierBeregningsgrunnlagTjeneste, new ResetGrunnlagTjeneste(repository, koblingTjeneste, koblingRepository));
    }


    @Test
    void kopierOgResettForFireGrunnlag() {

        var originalKoblingReferanse = UUID.randomUUID();
        var originalKobling = new KoblingEntitet(new KoblingReferanse(originalKoblingReferanse), FagsakYtelseType.PLEIEPENGER_SYKT_BARN, SAK, AKTØR_ID);
        koblingRepository.lagre(originalKobling);
        koblingRepository.lagre(new KoblingRelasjon(originalKobling.getId(), originalKobling.getId()));
        kalkulatorInputTjeneste.lagreKalkulatorInput(originalKobling.getId(), byggKalkulatorInput());

        var gr1 = repository.lagre(originalKobling.getId(), byggGrunnlag(200000), BeregningsgrunnlagTilstand.FASTSATT);
        var behandling1AvsluttetTid = LocalDateTime.now();
        var gr2 = repository.lagre(originalKobling.getId(), byggGrunnlag(300000), BeregningsgrunnlagTilstand.FASTSATT);
        var behandling2AvsluttetTid = LocalDateTime.now();
        var gr3 = repository.lagre(originalKobling.getId(), byggGrunnlag(400000), BeregningsgrunnlagTilstand.FASTSATT);
        var behandling3AvsluttetTid = LocalDateTime.now();
        var gr4 = repository.lagre(originalKobling.getId(), byggGrunnlag(500000), BeregningsgrunnlagTilstand.FASTSATT);
        var behandling4AvsluttetTid = LocalDateTime.now();


        var grOriginalKobling = repository.hentBeregningsgrunnlagGrunnlagEntitet(originalKobling.getId()).get();
        assertThat(grOriginalKobling).isEqualTo(gr4);

        // 1

        kjørOgVerifiser(originalKoblingReferanse, originalKobling, gr3, behandling3AvsluttetTid, gr4, behandling4AvsluttetTid, 2);

        var relasjoner = koblingRepository.hentRelasjonerFor(List.of(originalKobling.getId()));
        assertThat(relasjoner.stream().noneMatch(r -> r.getKoblingId().equals(r.getOriginalKoblingId()))).isTrue();

        // 2

        kjørOgVerifiser(originalKoblingReferanse, originalKobling, gr2, behandling2AvsluttetTid, gr3, behandling3AvsluttetTid, 3);

        // 3

        kjørOgVerifiser(originalKoblingReferanse, originalKobling, gr1, behandling1AvsluttetTid, gr2, behandling2AvsluttetTid, 4);


    }

    private void kjørOgVerifiser(UUID originalKoblingReferanse, KoblingEntitet originalKobling, BeregningsgrunnlagGrunnlagEntitet gr1, LocalDateTime behandling1AvsluttetTid, BeregningsgrunnlagGrunnlagEntitet gr2, LocalDateTime behandling2AvsluttetTid, int i) {
        var nyReferanse2 = UUID.randomUUID();
        tjeneste.kopierOgResett(new KopierOgResetRestTjeneste.KopierOgResettBeregningListeRequestAbacDto(
                new no.nav.folketrygdloven.kalkulus.felles.v1.Saksnummer(SAK.getVerdi()),
                UUID.randomUUID(),
                FagsakYtelseType.PLEIEPENGER_SYKT_BARN,
                List.of(new KopierBeregningRequest(nyReferanse2, originalKoblingReferanse)),
                BeregningSteg.FAST_BERGRUNN,
                behandling1AvsluttetTid,
                behandling2AvsluttetTid));

        var alleKoblinger2 = koblingRepository.hentAlleKoblingerForSaksnummer(SAK);
        assertThat(alleKoblinger2.size()).isEqualTo(i);
        var nyKobling2 = alleKoblinger2.stream().filter(k -> k.getKoblingReferanse().getReferanse().equals(nyReferanse2)).findFirst();
        assertThat(nyKobling2).isPresent();

        var grNyKobling2 = repository.hentBeregningsgrunnlagGrunnlagEntitet(nyKobling2.get().getId());
        assertThat(finnBrutto(grNyKobling2.get())).isEqualTo(finnBrutto(gr2));

        var grOriginalKobling2 = repository.hentBeregningsgrunnlagGrunnlagEntitet(originalKobling.getId());
        assertThat(grOriginalKobling2.get()).isEqualTo(gr1);
    }

    private BigDecimal finnBrutto(BeregningsgrunnlagGrunnlagEntitet grNyKobling1) {
        return grNyKobling1.getBeregningsgrunnlag().get().getBeregningsgrunnlagPerioder().get(0).getBruttoPrÅr().getVerdi();
    }

    private KalkulatorInputDto byggKalkulatorInput() {
        GraderingDto graderingDto = new GraderingDto(periode, BigDecimal.valueOf(100));
        AndelGraderingDto andelGraderingDto = new AndelGraderingDto(AktivitetStatus.ARBEIDSTAKER, organisasjon,
                null,
                List.of(graderingDto));
        AktivitetGraderingDto aktivitetGraderingDto = new AktivitetGraderingDto(List.of(andelGraderingDto));

        InntektArbeidYtelseGrunnlagDto iayGrunnlag = byggIAY();
        OpptjeningAktiviteterDto opptjeningAktiviteter = new OpptjeningAktiviteterDto(
                List.of(new OpptjeningPeriodeDto(OpptjeningAktivitetType.ARBEID, periode, organisasjon,
                        null)));
        LocalDate skjæringstidspunkt = periode.getFom();

        KalkulatorInputDto kalkulatorInputDto = new KalkulatorInputDto(iayGrunnlag, opptjeningAktiviteter,
                skjæringstidspunkt);
        kalkulatorInputDto.medYtelsespesifiktGrunnlag(
                new ForeldrepengerGrunnlag(BigDecimal.valueOf(100), false, aktivitetGraderingDto,
                        Collections.emptyList()));
        kalkulatorInputDto.medRefusjonskravDatoer(List
                .of(new RefusjonskravDatoDto(organisasjon, periode.getFom(),
                        periode.getFom().minusMonths(1), true)));
        kalkulatorInputDto.medRefusjonskravDatoer(List
                .of(new RefusjonskravDatoDto(organisasjon, periode.getFom(),
                        periode.getFom().minusMonths(1), true)));

        return kalkulatorInputDto;
    }


    private BeregningsgrunnlagGrunnlagBuilder byggGrunnlag(int verdi) {
        BeregningsgrunnlagEntitet bg = BeregningsgrunnlagEntitet.builder()
                .medSkjæringstidspunkt(STP)
                .medGrunnbeløp(BigDecimal.valueOf(100_000))
                .build();
        BeregningsgrunnlagPeriode.builder()
                .medBeregningsgrunnlagPeriode(STP, TIDENES_ENDE)
                .leggTilBeregningsgrunnlagPrStatusOgAndel(BeregningsgrunnlagPrStatusOgAndel.builder()
                        .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                        .medAndelsnr(1L)
                        .medBGAndelArbeidsforhold(BGAndelArbeidsforhold.builder().medArbeidsgiver(Arbeidsgiver.virksomhet("999999999")))
                        .medGrunnlagPrÅr(lagBeregnet(verdi)))
                .build(bg);
        var gr = BeregningsgrunnlagGrunnlagBuilder.kopiere(Optional.empty())
                .medRegisterAktiviteter(BeregningAktivitetAggregatEntitet.builder()
                        .medSkjæringstidspunktOpptjening(STP)
                        .build())
                .medBeregningsgrunnlag(bg);
        return gr;
    }

    private Årsgrunnlag lagBeregnet(int verdi) {
        return new Årsgrunnlag(new Beløp(verdi), null, null, null, null, new Beløp(verdi));
    }


    private InntektArbeidYtelseGrunnlagDto byggIAY() {
        InntektArbeidYtelseGrunnlagDto iayGrunnlag = new InntektArbeidYtelseGrunnlagDto();
        iayGrunnlag.medArbeidDto(
                new ArbeidDto(List.of(new YrkesaktivitetDto(organisasjon, ref,
                        ArbeidType.ORDINÆRT_ARBEIDSFORHOLD,
                        List.of(new AktivitetsAvtaleDto(periode, null, BigDecimal.valueOf(100)),
                                new AktivitetsAvtaleDto(periode, null, null))))));
        iayGrunnlag.medYtelserDto(new YtelserDto(byggYtelseDto()));
        iayGrunnlag.medInntekterDto(
                new InntekterDto(List.of(new UtbetalingDto(InntektskildeType.INNTEKT_BEREGNING,
                        List.of(new UtbetalingsPostDto(periode, InntektspostType.LØNN,
                                no.nav.folketrygdloven.kalkulus.typer.Beløp.fra(1000L)))))));
        iayGrunnlag.medInntektsmeldingerDto(
                new InntektsmeldingerDto(List.of(new InntektsmeldingDto(organisasjon,
                        new BeløpDto(BigDecimal.valueOf(100)), List.of(), List.of(), null, null,
                        null, null, null, null))));
        return iayGrunnlag;
    }

    private List<YtelseDto> byggYtelseDto() {
        YtelseAnvistDto ytelseAnvistDto = new YtelseAnvistDto(periode, beløpDto, beløpDto, BigDecimal.TEN,
                List.of(new AnvistAndel(new Organisasjon("974652269"),
                        new InternArbeidsforholdRefDto("r8j3wr8w3"),
                        beløpDto,
                        BigDecimal.valueOf(100),
                        BigDecimal.valueOf(100),
                        Inntektskategori.ARBEIDSTAKER)));
        return List.of(new YtelseDto(beløpDto, Set.of(ytelseAnvistDto), YtelseType.FORELDREPENGER,
                periode,
                null));
    }

}
