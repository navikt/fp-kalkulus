package no.nav.folketrygdloven.kalkulus.mappers;


import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import javax.validation.Validation;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

import no.nav.folketrygdloven.kalkulus.beregning.v1.AktivitetGraderingDto;
import no.nav.folketrygdloven.kalkulus.beregning.v1.AndelGraderingDto;
import no.nav.folketrygdloven.kalkulus.beregning.v1.GraderingDto;
import no.nav.folketrygdloven.kalkulus.beregning.v1.GrunnbeløpDto;
import no.nav.folketrygdloven.kalkulus.felles.v1.KalkulatorInputDto;
import no.nav.folketrygdloven.kalkulus.felles.v1.Organisasjon;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.iay.v1.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.opptjening.v1.OpptjeningAktiviteterDto;
import no.nav.folketrygdloven.kalkulus.opptjening.v1.OpptjeningPeriodeDto;

public class KalkulatorInputDtoMapperTest {


    private static final ObjectWriter WRITER = KalkulatorInputMapper.getMapper().writerWithDefaultPrettyPrinter();
    private static final ObjectReader READER = KalkulatorInputMapper.getMapper().reader();

    private final Periode periode = new Periode(LocalDate.now(), LocalDate.now().plusMonths(2));
    private final Organisasjon organisasjon = new Organisasjon("945748931");


    @Test
    void skal_generere_og_validere_roundtrip_mega_kalkulator_input_json() throws Exception {

        KalkulatorInputDto grunnlag = byggKalkulatorInput();

        String json = WRITER.writeValueAsString(grunnlag);
        System.out.println(json);

        KalkulatorInputDto roundTripped = READER.forType(KalkulatorInputDto.class).readValue(json);

        assertThat(roundTripped).isNotNull();
        assertThat(roundTripped.getIayGrunnlag()).isNotNull();
        validateResult(roundTripped);
    }

    private KalkulatorInputDto byggKalkulatorInput() {
        GraderingDto graderingDto = new GraderingDto(periode, BigDecimal.valueOf(100));
        AndelGraderingDto andelGraderingDto = new AndelGraderingDto(AktivitetStatus.ARBEIDSTAKER, organisasjon, null, List.of(graderingDto));
        AktivitetGraderingDto aktivitetGraderingDto = new AktivitetGraderingDto(List.of(andelGraderingDto));

        List<GrunnbeløpDto> grunnbeløp = List.of(new GrunnbeløpDto(periode, BigDecimal.valueOf(99000), BigDecimal.valueOf(99000)));
        InntektArbeidYtelseGrunnlagDto iayGrunnlag = new InntektArbeidYtelseGrunnlagDto();
        OpptjeningAktiviteterDto opptjeningAktiviteter = new OpptjeningAktiviteterDto(List.of(new OpptjeningPeriodeDto(OpptjeningAktivitetType.ARBEID, periode, organisasjon, null)));

        KalkulatorInputDto kalkulatorInputDto = new KalkulatorInputDto(grunnbeløp, iayGrunnlag, opptjeningAktiviteter);
        kalkulatorInputDto.medAktivitetGradering(aktivitetGraderingDto);

        return kalkulatorInputDto;
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
