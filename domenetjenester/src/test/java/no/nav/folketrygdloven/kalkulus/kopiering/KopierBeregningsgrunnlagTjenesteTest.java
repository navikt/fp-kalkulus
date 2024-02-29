package no.nav.folketrygdloven.kalkulus.kopiering;

import static no.nav.folketrygdloven.kalkulus.felles.tid.AbstractIntervall.TIDENES_ENDE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import no.nav.folketrygdloven.kalkulus.typer.Aktivitetsgrad;

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
import no.nav.folketrygdloven.kalkulus.iay.IayProsent;
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
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningSteg;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.kodeverk.InntektskildeType;
import no.nav.folketrygdloven.kalkulus.kodeverk.InntektspostType;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseType;
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
import no.nav.k9.felles.testutilities.sikkerhet.StaticSubjectHandler;
import no.nav.k9.felles.testutilities.sikkerhet.SubjectHandlerUtils;

@ExtendWith({JpaExtension.class})
class KopierBeregningsgrunnlagTjenesteTest extends EntityManagerAwareTest {

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
    private AvklaringsbehovTjeneste avklaringsbehovTjeneste;
    private AvklaringsbehovRepository avklaringsbehovRepository;

    private KopierBeregningsgrunnlagTjeneste tjeneste;


    @BeforeEach
    public void setUp() {
        repository = new BeregningsgrunnlagRepository(getEntityManager());
        koblingRepository = new KoblingRepository(getEntityManager());
        koblingRepository = new KoblingRepository(getEntityManager());
        avklaringsbehovRepository = new AvklaringsbehovRepository(getEntityManager());
        AvklaringsbehovKontrollTjeneste avklaringsbehovKontrollTjeneste = new AvklaringsbehovKontrollTjeneste();
        avklaringsbehovTjeneste = new AvklaringsbehovTjeneste(avklaringsbehovRepository, koblingRepository, avklaringsbehovKontrollTjeneste);
        var koblingTjeneste = new KoblingTjeneste(koblingRepository, new LåsRepository(getEntityManager()));
        kalkulatorInputTjeneste = new KalkulatorInputTjeneste(repository, koblingTjeneste);
        tjeneste = new KopierBeregningsgrunnlagTjeneste(koblingTjeneste, repository, avklaringsbehovTjeneste, kalkulatorInputTjeneste);
        SubjectHandlerUtils.useSubjectHandler(StaticSubjectHandler.class);
    }


    @Test
    void skal_kopiere_grunnlag_til_ny_kobling() {
        SubjectHandlerUtils.setInternBruker("noen");

        var originalKoblingReferanse = UUID.randomUUID();
        var originalKobling = new KoblingEntitet(new KoblingReferanse(originalKoblingReferanse), FagsakYtelseType.PLEIEPENGER_SYKT_BARN, SAK, AKTØR_ID);
        koblingRepository.lagre(originalKobling);
        kalkulatorInputTjeneste.lagreKalkulatorInput(originalKobling.getId(), byggKalkulatorInput());

        avklaringsbehovTjeneste.opprettEllerGjennopprettAvklaringsbehov(originalKobling.getId(), AvklaringsbehovDefinisjon.AVKLAR_AKTIVITETER);
        avklaringsbehovTjeneste.løsAvklaringsbehov(originalKobling.getId(), AvklaringsbehovDefinisjon.AVKLAR_AKTIVITETER, "ok");

        var gr1 = repository.lagre(originalKobling.getId(), byggGrunnlag(200000), BeregningsgrunnlagTilstand.FASTSATT);

        SubjectHandlerUtils.setInternBruker("noen andre");
        LocalDateTime førKopiering = LocalDateTime.now();
        var nyReferanse2 = UUID.randomUUID();
        tjeneste.kopierGrunnlagOgOpprettKoblinger(
                List.of(new KopierBeregningRequest(nyReferanse2, originalKoblingReferanse)),
                FagsakYtelseType.PLEIEPENGER_SYKT_BARN,
                SAK,
                BeregningSteg.FAST_BERGRUNN,
                null
        );

        var alleKoblinger = koblingRepository.hentAlleKoblingerForSaksnummer(SAK);
        assertThat(alleKoblinger.size()).isEqualTo(2);
        var nyKobling = alleKoblinger.stream().filter(k -> k.getKoblingReferanse().getReferanse().equals(nyReferanse2)).findFirst();
        assertThat(nyKobling).isPresent();

        var grNyKobling = repository.hentBeregningsgrunnlagGrunnlagEntitet(nyKobling.get().getId());
        assertThat(finnBrutto(grNyKobling.get())).isEqualTo(finnBrutto(gr1));

        var nyeAvklaringsbehov = avklaringsbehovRepository.hentAvklaringsbehovForKobling(nyKobling.get());
        assertThat(nyeAvklaringsbehov.size()).isEqualTo(1);
        var nyAvklaringsbehov = nyeAvklaringsbehov.get(0);
        assertThat(nyAvklaringsbehov.getBegrunnelse()).isEqualTo("ok");
        assertThat(nyAvklaringsbehov.getDefinisjon()).isEqualTo(AvklaringsbehovDefinisjon.AVKLAR_AKTIVITETER);
        assertThat(nyAvklaringsbehov.getStatus()).isEqualTo(AvklaringsbehovStatus.UTFØRT);
        assertThat(nyAvklaringsbehov.getVurdertAv()).isEqualTo("noen");
        assertThat(nyAvklaringsbehov.getVurdertTidspunkt()).isBefore(førKopiering);
    }

    @Test
    void skal_kopiere_grunnlag_fra_forrige_original_kobling_til_ny_kobling() {
        var forrigeOriginalKoblingReferanse = UUID.randomUUID();
        var forrigeOriginalKobling = new KoblingEntitet(new KoblingReferanse(forrigeOriginalKoblingReferanse), FagsakYtelseType.PLEIEPENGER_SYKT_BARN, SAK, AKTØR_ID);
        koblingRepository.lagre(forrigeOriginalKobling);
        var originalKoblingReferanse = UUID.randomUUID();
        var originalKobling = new KoblingEntitet(new KoblingReferanse(originalKoblingReferanse), FagsakYtelseType.PLEIEPENGER_SYKT_BARN, SAK, AKTØR_ID);
        koblingRepository.lagre(originalKobling);

        koblingRepository.lagre(new KoblingRelasjon(originalKobling.getId(), forrigeOriginalKobling.getId()));

        kalkulatorInputTjeneste.lagreKalkulatorInput(originalKobling.getId(), byggKalkulatorInput());

        var gr1 = repository.lagre(forrigeOriginalKobling.getId(), byggGrunnlag(200000), BeregningsgrunnlagTilstand.FASTSATT);

        var nyReferanse2 = UUID.randomUUID();
        tjeneste.kopierGrunnlagOgOpprettKoblinger(
                List.of(new KopierBeregningRequest(nyReferanse2, originalKoblingReferanse)),
                FagsakYtelseType.PLEIEPENGER_SYKT_BARN,
                SAK,
                BeregningSteg.FAST_BERGRUNN,
                null
        );

        var alleKoblinger = koblingRepository.hentAlleKoblingerForSaksnummer(SAK);
        assertThat(alleKoblinger.size()).isEqualTo(3);
        var nyKobling = alleKoblinger.stream().filter(k -> k.getKoblingReferanse().getReferanse().equals(nyReferanse2)).findFirst();
        assertThat(nyKobling).isPresent();

        var grNyKobling = repository.hentBeregningsgrunnlagGrunnlagEntitet(nyKobling.get().getId());
        assertThat(finnBrutto(grNyKobling.get())).isEqualTo(finnBrutto(gr1));

    }


    private BigDecimal finnBrutto(BeregningsgrunnlagGrunnlagEntitet grNyKobling1) {
        return grNyKobling1.getBeregningsgrunnlag().get().getBeregningsgrunnlagPerioder().get(0).getBruttoPrÅr().getVerdi();
    }


    private KalkulatorInputDto byggKalkulatorInput() {
        GraderingDto graderingDto = new GraderingDto(periode, Aktivitetsgrad.valueOf(100));
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
                .medGrunnbeløp(new Beløp(100000))
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
                        List.of(new AktivitetsAvtaleDto(periode, null, IayProsent.fra(100)),
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
        YtelseAnvistDto ytelseAnvistDto = new YtelseAnvistDto(periode, beløpDto, beløpDto, IayProsent.fra(10),
                List.of(new AnvistAndel(new Organisasjon("974652269"),
                        new InternArbeidsforholdRefDto("r8j3wr8w3"),
                        beløpDto,
                        IayProsent.fra(100),
                        IayProsent.fra(100),
                        Inntektskategori.ARBEIDSTAKER)));
        return List.of(new YtelseDto(beløpDto, Set.of(ytelseAnvistDto), YtelseType.FORELDREPENGER,
                periode,
                null));
    }


}
