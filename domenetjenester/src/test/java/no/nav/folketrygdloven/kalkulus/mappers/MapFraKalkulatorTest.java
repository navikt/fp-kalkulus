package no.nav.folketrygdloven.kalkulus.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulus.beregning.v1.AktivitetGraderingDto;
import no.nav.folketrygdloven.kalkulus.beregning.v1.AndelGraderingDto;
import no.nav.folketrygdloven.kalkulus.beregning.v1.GraderingDto;
import no.nav.folketrygdloven.kalkulus.beregning.v1.GrunnbeløpDto;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.AktørId;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.KoblingReferanse;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Saksnummer;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.YtelseTyperKalkulusStøtter;
import no.nav.folketrygdloven.kalkulus.felles.v1.AktørIdPersonident;
import no.nav.folketrygdloven.kalkulus.felles.v1.KalkulatorInputDto;
import no.nav.folketrygdloven.kalkulus.felles.v1.Organisasjon;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.iay.v1.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.opptjening.v1.OpptjeningAktiviteterDto;
import no.nav.folketrygdloven.kalkulus.opptjening.v1.OpptjeningPeriodeDto;

class MapFraKalkulatorTest {

    private final Periode periode = new Periode(LocalDate.now(), LocalDate.now().plusMonths(2));
    private final Organisasjon organisasjon = new Organisasjon("945748931");


    @Test
    void skal_mappe_fra_kalkulator_til_beregningsgrunnlag_input() {
        MapFraKalkulator mapFraKalkulator = new MapFraKalkulator();

        String saksnummer = "1234";
        UUID randomUUID = UUID.randomUUID();
        AktørIdPersonident dummy = AktørIdPersonident.dummy();

        YtelseTyperKalkulusStøtter ytelseTyperKalkulusStøtter = YtelseTyperKalkulusStøtter.DAGPENGER;
        AktørId aktørId = new AktørId(dummy.getIdent());
        Saksnummer saksnummer1 = new Saksnummer(saksnummer);
        KoblingEntitet koblingEntitet = new KoblingEntitet(new KoblingReferanse(randomUUID), ytelseTyperKalkulusStøtter, saksnummer1, aktørId);
        KalkulatorInputDto kalkulatorInputDto = byggKalkulatorInput();


        BeregningsgrunnlagInput input = mapFraKalkulator.mapFraKalkulatorInputTilBeregningsgrunnlagInput(koblingEntitet, kalkulatorInputDto);


        assertThat(input.getAktørId().getId()).isEqualTo(aktørId.getId());
//        assertThat(input.getAktivitetGradering().getAndelGradering()).hasSize(1);
    }


    private KalkulatorInputDto byggKalkulatorInput() {
        GraderingDto graderingDto = new GraderingDto(periode, BigDecimal.valueOf(100));
        AndelGraderingDto andelGraderingDto = new AndelGraderingDto(AktivitetStatus.ARBEIDSTAKER, organisasjon, null, List.of(graderingDto));
        AktivitetGraderingDto aktivitetGraderingDto = new AktivitetGraderingDto(List.of(andelGraderingDto));

        List<GrunnbeløpDto> grunnbeløp = List.of(new GrunnbeløpDto(periode, BigDecimal.valueOf(99000), BigDecimal.valueOf(99000)));
        InntektArbeidYtelseGrunnlagDto iayGrunnlag = new InntektArbeidYtelseGrunnlagDto();
        OpptjeningAktiviteterDto opptjeningAktiviteter = new OpptjeningAktiviteterDto(List.of(new OpptjeningPeriodeDto(OpptjeningAktivitetType.ARBEID, periode, organisasjon, null)));
        LocalDate skjæringstidspunkt = periode.getFom();

        KalkulatorInputDto kalkulatorInputDto = new KalkulatorInputDto(grunnbeløp, iayGrunnlag, opptjeningAktiviteter, skjæringstidspunkt);
        kalkulatorInputDto.medAktivitetGradering(aktivitetGraderingDto);

        return kalkulatorInputDto;
    }
}
