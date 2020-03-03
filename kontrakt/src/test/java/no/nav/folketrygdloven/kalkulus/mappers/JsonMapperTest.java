package no.nav.folketrygdloven.kalkulus.mappers;


import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.validation.Validation;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

import no.nav.folketrygdloven.kalkulus.UuidDto;
import no.nav.folketrygdloven.kalkulus.beregning.v1.AktivitetGraderingDto;
import no.nav.folketrygdloven.kalkulus.beregning.v1.AndelGraderingDto;
import no.nav.folketrygdloven.kalkulus.beregning.v1.GraderingDto;
import no.nav.folketrygdloven.kalkulus.beregning.v1.GrunnbeløpDto;
import no.nav.folketrygdloven.kalkulus.beregning.v1.RefusjonskravDatoDto;
import no.nav.folketrygdloven.kalkulus.felles.v1.AktørIdPersonident;
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
import no.nav.folketrygdloven.kalkulus.iay.ytelse.v1.YtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.iay.ytelse.v1.YtelseStørrelseDto;
import no.nav.folketrygdloven.kalkulus.iay.ytelse.v1.YtelserDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;
import no.nav.folketrygdloven.kalkulus.kodeverk.Arbeidskategori;
import no.nav.folketrygdloven.kalkulus.kodeverk.Fagsystem;
import no.nav.folketrygdloven.kalkulus.kodeverk.InntektPeriodeType;
import no.nav.folketrygdloven.kalkulus.kodeverk.InntektskildeType;
import no.nav.folketrygdloven.kalkulus.kodeverk.InntektspostType;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.kodeverk.RelatertYtelseTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.RelatertYtelseType;
import no.nav.folketrygdloven.kalkulus.kodeverk.TemaUnderkategori;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseTyperKalkulusStøtterKontrakt;
import no.nav.folketrygdloven.kalkulus.opptjening.v1.OpptjeningAktiviteterDto;
import no.nav.folketrygdloven.kalkulus.opptjening.v1.OpptjeningPeriodeDto;
import no.nav.folketrygdloven.kalkulus.request.v1.StartBeregningRequest;

public class JsonMapperTest {


    private static final ObjectWriter WRITER = JsonMapper.getMapper().writerWithDefaultPrettyPrinter();
    private static final ObjectReader READER = JsonMapper.getMapper().reader();

    private final InternArbeidsforholdRefDto ref = new InternArbeidsforholdRefDto(UUID.randomUUID().toString());
    private final Periode periode = new Periode(LocalDate.now(), LocalDate.now().plusMonths(2));
    private final Organisasjon organisasjon = new Organisasjon("945748931");
    private final BeløpDto beløpDto = new BeløpDto(BigDecimal.TEN);


    @Test
    void skal_generere_og_validere_roundtrip_kalkulator_input_json() throws Exception {

        KalkulatorInputDto grunnlag = byggKalkulatorInput();

        String json = WRITER.writeValueAsString(grunnlag);
        System.out.println(json);

        KalkulatorInputDto roundTripped = READER.forType(KalkulatorInputDto.class).readValue(json);

        assertThat(roundTripped).isNotNull();
        assertThat(roundTripped.getIayGrunnlag()).isNotNull();
        validateResult(roundTripped);
    }

    @Test
    public void skal_generere_og_validere_roundtrip_av_start_beregning_request() throws Exception {
        //arrange
        String saksnummer = "1234";
        AktørIdPersonident dummy = AktørIdPersonident.dummy();
        KalkulatorInputDto kalkulatorInputDto = byggKalkulatorInput();

        UuidDto koblingReferanse = new UuidDto(UUID.randomUUID());
        StartBeregningRequest spesifikasjon = new StartBeregningRequest(koblingReferanse, saksnummer, dummy, YtelseTyperKalkulusStøtterKontrakt.PLEIEPENGER_SYKT_BARN, kalkulatorInputDto);

        String json = WRITER.writeValueAsString(spesifikasjon);
        System.out.println(json);

        StartBeregningRequest roundTripped = READER.forType(StartBeregningRequest.class).readValue(json);

        assertThat(roundTripped).isNotNull();
        assertThat(roundTripped.getAktør()).isEqualTo(dummy);
        assertThat(roundTripped.getSaksnummer()).isEqualTo(saksnummer);
        assertThat(roundTripped.getEksternReferanse().getReferanse()).isEqualTo(koblingReferanse.getReferanse());
        validateResult(roundTripped);
    }

    private KalkulatorInputDto byggKalkulatorInput() {
        GraderingDto graderingDto = new GraderingDto(periode, BigDecimal.valueOf(100));
        AndelGraderingDto andelGraderingDto = new AndelGraderingDto(AktivitetStatus.ARBEIDSTAKER, organisasjon, null, List.of(graderingDto));
        AktivitetGraderingDto aktivitetGraderingDto = new AktivitetGraderingDto(List.of(andelGraderingDto));

        List<GrunnbeløpDto> grunnbeløp = List.of(new GrunnbeløpDto(periode, BigDecimal.valueOf(99000), BigDecimal.valueOf(99000)));
        InntektArbeidYtelseGrunnlagDto iayGrunnlag = byggIAY();
        OpptjeningAktiviteterDto opptjeningAktiviteter = new OpptjeningAktiviteterDto(List.of(new OpptjeningPeriodeDto(OpptjeningAktivitetType.ARBEID, periode, organisasjon, null)));
        LocalDate skjæringstidspunkt = periode.getFom();

        KalkulatorInputDto kalkulatorInputDto = new KalkulatorInputDto(grunnbeløp, iayGrunnlag, opptjeningAktiviteter, skjæringstidspunkt);
        kalkulatorInputDto.medAktivitetGradering(aktivitetGraderingDto);
        kalkulatorInputDto.medRefusjonskravDatoer(List.of(new RefusjonskravDatoDto(organisasjon, periode.getFom(), periode.getFom().minusMonths(1), true)));

        return kalkulatorInputDto;
    }

    private InntektArbeidYtelseGrunnlagDto byggIAY() {
        InntektArbeidYtelseGrunnlagDto iayGrunnlag = new InntektArbeidYtelseGrunnlagDto();
        iayGrunnlag.medArbeidDto(new ArbeidDto(List.of(new YrkesaktivitetDto(organisasjon, ref, ArbeidType.ORDINÆRT_ARBEIDSFORHOLD, List.of(new AktivitetsAvtaleDto(periode, BigDecimal.valueOf(100)), new AktivitetsAvtaleDto(periode, null)), null))));
        iayGrunnlag.medYtelserDto(new YtelserDto(byggYtelseDto()));
        iayGrunnlag.medInntekterDto(new InntekterDto(List.of(new UtbetalingDto(InntektskildeType.INNTEKT_BEREGNING, List.of(new UtbetalingsPostDto(periode, InntektspostType.LØNN, BigDecimal.valueOf(1000L)))))));
        iayGrunnlag.medInntektsmeldingerDto(new InntektsmeldingerDto(List.of(new InntektsmeldingDto(organisasjon, new BeløpDto(BigDecimal.valueOf(100)), List.of(), List.of(), null, null, null, null))));
        return iayGrunnlag;
    }

    private List<YtelseDto> byggYtelseDto() {
        YtelseStørrelseDto ytelseStørrelse = new YtelseStørrelseDto(InntektPeriodeType.DAGLIG, organisasjon, beløpDto);
        YtelseGrunnlagDto ytelseGrunnlag = new YtelseGrunnlagDto(List.of(ytelseStørrelse), Arbeidskategori.FRILANSER, BigDecimal.TEN, BigDecimal.TEN, BigDecimal.TEN, LocalDate.now(), beløpDto);
        YtelseAnvistDto ytelseAnvistDto = new YtelseAnvistDto(periode, beløpDto, beløpDto, BigDecimal.TEN);
        return List.of(new YtelseDto(ytelseGrunnlag, Set.of(ytelseAnvistDto), RelatertYtelseType.FORELDREPENGER, periode, RelatertYtelseTilstand.ÅPEN, Fagsystem.ARENA, TemaUnderkategori.FORELDREPENGER));
    }

    private void validateResult(Object roundTripped) {
        Assertions.assertThat(roundTripped).isNotNull();
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            var violations = validator.validate(roundTripped);
            assertThat(violations).isEmpty();
        }
    }
}
