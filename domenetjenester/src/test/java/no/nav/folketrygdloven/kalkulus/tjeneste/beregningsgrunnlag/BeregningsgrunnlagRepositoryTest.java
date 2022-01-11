package no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import jakarta.validation.Validation;

import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import no.nav.folketrygdloven.kalkulus.beregning.v1.AktivitetGraderingDto;
import no.nav.folketrygdloven.kalkulus.beregning.v1.AndelGraderingDto;
import no.nav.folketrygdloven.kalkulus.beregning.v1.ForeldrepengerGrunnlag;
import no.nav.folketrygdloven.kalkulus.beregning.v1.GraderingDto;
import no.nav.folketrygdloven.kalkulus.beregning.v1.RefusjonskravDatoDto;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.KalkulatorInputEntitet;
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
import no.nav.folketrygdloven.kalkulus.felles.v1.BeløpDto;
import no.nav.folketrygdloven.kalkulus.felles.v1.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.felles.v1.KalkulatorInputDto;
import no.nav.folketrygdloven.kalkulus.felles.v1.Organisasjon;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.iay.arbeid.v1.AktivitetsAvtaleDto;
import no.nav.folketrygdloven.kalkulus.iay.arbeid.v1.ArbeidDto;
import no.nav.folketrygdloven.kalkulus.iay.arbeid.v1.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulus.iay.inntekt.v1.InntekterDto;
import no.nav.folketrygdloven.kalkulus.iay.inntekt.v1.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulus.iay.inntekt.v1.InntektsmeldingerDto;
import no.nav.folketrygdloven.kalkulus.iay.inntekt.v1.UtbetalingDto;
import no.nav.folketrygdloven.kalkulus.iay.inntekt.v1.UtbetalingsPostDto;
import no.nav.folketrygdloven.kalkulus.iay.v1.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.iay.ytelse.v1.YtelseAnvistDto;
import no.nav.folketrygdloven.kalkulus.iay.ytelse.v1.YtelseDto;
import no.nav.folketrygdloven.kalkulus.iay.ytelse.v1.YtelserDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.InntektskildeType;
import no.nav.folketrygdloven.kalkulus.kodeverk.InntektspostType;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.kodeverk.RelatertYtelseType;
import no.nav.folketrygdloven.kalkulus.kodeverk.TemaUnderkategori;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseTyperKalkulusStøtterKontrakt;
import no.nav.folketrygdloven.kalkulus.mappers.JsonMapper;
import no.nav.folketrygdloven.kalkulus.opptjening.v1.OpptjeningAktiviteterDto;
import no.nav.folketrygdloven.kalkulus.opptjening.v1.OpptjeningPeriodeDto;
import no.nav.folketrygdloven.kalkulus.tjeneste.extensions.JpaExtension;
import no.nav.folketrygdloven.kalkulus.tjeneste.kobling.KoblingRepository;
import no.nav.folketrygdloven.kalkulus.typer.AktørId;
import no.nav.k9.felles.testutilities.db.EntityManagerAwareTest;

@ExtendWith(JpaExtension.class)
public class BeregningsgrunnlagRepositoryTest extends EntityManagerAwareTest {

        private static final ObjectWriter WRITER_JSON = JsonMapper.getMapper().writerWithDefaultPrettyPrinter();
        private static final ObjectReader READER_JSON = JsonMapper.getMapper().reader();

        private static void validateResult(Object roundTripped) {
                Assertions.assertThat(roundTripped).isNotNull();
                try (var factory = Validation.buildDefaultValidatorFactory()) {
                        var validator = factory.getValidator();
                        var violations = validator.validate(roundTripped);
                        assertThat(violations).isEmpty();
                }
        }

        private BeregningsgrunnlagRepository repository;
        private KoblingRepository koblingRepository;

        private final InternArbeidsforholdRefDto ref = new InternArbeidsforholdRefDto(UUID.randomUUID().toString());
        private final Periode periode = new Periode(LocalDate.now(), LocalDate.now().plusMonths(2));
        private final Organisasjon organisasjon = new Organisasjon("945748931");
        private final BeløpDto beløpDto = new BeløpDto(BigDecimal.TEN);

        @BeforeEach
        public void beforeEach() {
                repository = new BeregningsgrunnlagRepository(getEntityManager());
                koblingRepository = new KoblingRepository(getEntityManager());
        }

        @Test
        public void skal_lagre_ned_json_input() throws Exception {
                AktørId aktørId = new AktørId("1234123412341");
                KoblingReferanse koblingReferanse = new KoblingReferanse(UUID.randomUUID());
                Saksnummer saksnummer = new Saksnummer("1234");

                KoblingEntitet koblingEntitet = new KoblingEntitet(koblingReferanse,
                                YtelseTyperKalkulusStøtterKontrakt.PLEIEPENGER_NÆRSTÅENDE, saksnummer, aktørId);
                koblingRepository.lagre(koblingEntitet);

                Long koblingId = koblingEntitet.getId();

                String json = getTestJSON();

                KalkulatorInputEntitet input = new KalkulatorInputEntitet(koblingId, json);

                var inputJsonTree = READER_JSON.readTree(json);

                repository.lagreOgSjekkStatus(input);

                getEntityManager().clear(); // må cleare for å ikke å få fra 1st-level cache ved spørring

                KalkulatorInputEntitet resultat = repository.hentKalkulatorInput(koblingId);

                var readJsonTree = READER_JSON.readTree(resultat.getInput());
                assertThat(readJsonTree).isEqualTo(inputJsonTree);

                assertThat(readJsonTree.has("jeg"));
        }

        @Test
        public void skal_lagre_ned_generert_kalkulator_input() throws Exception {
                AktørId aktørId = new AktørId("1234123412341");
                KoblingReferanse koblingReferanse = new KoblingReferanse(UUID.randomUUID());
                Saksnummer saksnummer = new Saksnummer("1234");

                KoblingEntitet koblingEntitet = new KoblingEntitet(koblingReferanse,
                                YtelseTyperKalkulusStøtterKontrakt.PLEIEPENGER_NÆRSTÅENDE, saksnummer, aktørId);
                koblingRepository.lagre(koblingEntitet);

                Long koblingId = koblingEntitet.getId();

                var grunnlag = byggKalkulatorInput();

                String json = WRITER_JSON.writeValueAsString(grunnlag);

                KalkulatorInputEntitet input = new KalkulatorInputEntitet(koblingId, json);

                var inputJsonTree = READER_JSON.readTree(json);

                repository.lagreOgSjekkStatus(input);

                getEntityManager().clear(); // må cleare for å ikke å få fra 1st-level cache ved spørring

                KalkulatorInputEntitet resultat = repository.hentKalkulatorInput(koblingId);
                System.out.println(resultat.getInput());

                KalkulatorInputDto roundTripped = READER_JSON.forType(KalkulatorInputDto.class).readValue(json);
                validateResult(roundTripped);

                var readJsonTree = READER_JSON.readTree(resultat.getInput());
                assertThat(readJsonTree).isEqualTo(inputJsonTree);
                assertThat(readJsonTree.has("iayGrunnlag"));
        }

        @Test
        public void skal_hente_beregningsgrunnlag_for_referanse() {
                AktørId aktørId = new AktørId("1234123412341");
                KoblingReferanse koblingReferanse = new KoblingReferanse(UUID.randomUUID());
                Saksnummer saksnummer = new Saksnummer("1234");

                KoblingEntitet koblingEntitet = new KoblingEntitet(koblingReferanse,
                                YtelseTyperKalkulusStøtterKontrakt.PLEIEPENGER_NÆRSTÅENDE, saksnummer, aktørId);
                koblingRepository.lagre(koblingEntitet);

                Long koblingId = koblingEntitet.getId();

                BeregningsgrunnlagEntitet build = BeregningsgrunnlagEntitet.builder()
                                .medSkjæringstidspunkt(LocalDate.now())
                                .build();
                BeregningsgrunnlagGrunnlagBuilder builder = BeregningsgrunnlagGrunnlagBuilder
                                .oppdatere(Optional.empty())
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
                                                LocalDate.now()));
                kalkulatorInputDto.medRefusjonskravDatoer(List
                                .of(new RefusjonskravDatoDto(organisasjon, periode.getFom(),
                                                periode.getFom().minusMonths(1), true)));
                kalkulatorInputDto.medRefusjonskravDatoer(List
                                .of(new RefusjonskravDatoDto(organisasjon, periode.getFom(),
                                                periode.getFom().minusMonths(1), true)));

                return kalkulatorInputDto;
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
                                                                BigDecimal.valueOf(1000L)))))));
                iayGrunnlag.medInntektsmeldingerDto(
                                new InntektsmeldingerDto(List.of(new InntektsmeldingDto(organisasjon,
                                                new BeløpDto(BigDecimal.valueOf(100)), List.of(), List.of(), null, null,
                                                null, null, null, null))));
                return iayGrunnlag;
        }

        private List<YtelseDto> byggYtelseDto() {
                YtelseAnvistDto ytelseAnvistDto = new YtelseAnvistDto(periode, beløpDto, beløpDto, BigDecimal.TEN);
                return List.of(new YtelseDto(beløpDto, Set.of(ytelseAnvistDto), RelatertYtelseType.FORELDREPENGER,
                                periode,
                                TemaUnderkategori.FORELDREPENGER, null));
        }
}
