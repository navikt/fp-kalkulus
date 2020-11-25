package no.nav.folketrygdloven.kalkulus.mappers;

import static no.nav.folketrygdloven.kalkulus.mappers.MapFraKalkulator.mapFraKalkulatorInputTilBeregningsgrunnlagInput;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulus.beregning.v1.AktivitetGraderingDto;
import no.nav.folketrygdloven.kalkulus.beregning.v1.AndelGraderingDto;
import no.nav.folketrygdloven.kalkulus.beregning.v1.GraderingDto;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.BeregningSats;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.AktørId;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.KoblingReferanse;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Saksnummer;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.felles.jpa.IntervallEntitet;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningSatsType;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.YtelseTyperKalkulusStøtter;
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
import no.nav.folketrygdloven.kalkulus.iay.ytelse.v1.YtelseDto;
import no.nav.folketrygdloven.kalkulus.iay.ytelse.v1.YtelserDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;
import no.nav.folketrygdloven.kalkulus.kodeverk.InntektskildeType;
import no.nav.folketrygdloven.kalkulus.kodeverk.InntektspostType;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.kodeverk.RelatertYtelseType;
import no.nav.folketrygdloven.kalkulus.kodeverk.TemaUnderkategori;
import no.nav.folketrygdloven.kalkulus.opptjening.v1.OpptjeningAktiviteterDto;
import no.nav.folketrygdloven.kalkulus.opptjening.v1.OpptjeningPeriodeDto;

class MapFraKalkulatorTest {

    private final Periode periode = new Periode(LocalDate.now(), LocalDate.now().plusMonths(2));
    private final Organisasjon organisasjon = new Organisasjon("945748931");
    private final InternArbeidsforholdRefDto ref = new InternArbeidsforholdRefDto(UUID.randomUUID().toString());


    @Test
    void skal_mappe_fra_kalkulator_til_beregningsgrunnlag_input() {
        String saksnummer = "1234";
        UUID randomUUID = UUID.randomUUID();
        YtelseTyperKalkulusStøtter ytelseTyperKalkulusStøtter = YtelseTyperKalkulusStøtter.DAGPENGER;
        Saksnummer saksnummer1 = new Saksnummer(saksnummer);
        KoblingEntitet koblingEntitet = new KoblingEntitet(new KoblingReferanse(randomUUID), ytelseTyperKalkulusStøtter, saksnummer1, AktørId.dummy());
        KalkulatorInputDto kalkulatorInputDto = byggKalkulatorInput();

        BeregningsgrunnlagInput input = mapFraKalkulatorInputTilBeregningsgrunnlagInput(koblingEntitet, kalkulatorInputDto, Optional.empty());

        assertThat(input.getKoblingReferanse().getKoblingId()).isEqualTo(koblingEntitet.getId());
    }


    private KalkulatorInputDto byggKalkulatorInput() {
        GraderingDto graderingDto = new GraderingDto(periode, BigDecimal.valueOf(100));
        AndelGraderingDto andelGraderingDto = new AndelGraderingDto(AktivitetStatus.ARBEIDSTAKER, organisasjon, null, List.of(graderingDto));
        AktivitetGraderingDto aktivitetGraderingDto = new AktivitetGraderingDto(List.of(andelGraderingDto));

        InntektArbeidYtelseGrunnlagDto iayGrunnlag = byggIAY();
        OpptjeningAktiviteterDto opptjeningAktiviteter = new OpptjeningAktiviteterDto(List.of(new OpptjeningPeriodeDto(OpptjeningAktivitetType.ARBEID, periode, organisasjon, null)));
        LocalDate skjæringstidspunkt = periode.getFom();

        KalkulatorInputDto kalkulatorInputDto = new KalkulatorInputDto(iayGrunnlag, opptjeningAktiviteter, skjæringstidspunkt);
        kalkulatorInputDto.medAktivitetGradering(aktivitetGraderingDto);

        return kalkulatorInputDto;
    }

    private InntektArbeidYtelseGrunnlagDto byggIAY() {
        InntektArbeidYtelseGrunnlagDto iayGrunnlag = new InntektArbeidYtelseGrunnlagDto();
        iayGrunnlag.medArbeidDto(new ArbeidDto(List.of(new YrkesaktivitetDto(organisasjon, ref, ArbeidType.ORDINÆRT_ARBEIDSFORHOLD, List.of(new AktivitetsAvtaleDto(periode, null, BigDecimal.valueOf(100)), new AktivitetsAvtaleDto(periode, null, null))))));
        iayGrunnlag.medYtelserDto(new YtelserDto(List.of(new YtelseDto(new BeløpDto(BigDecimal.TEN), Set.of(), RelatertYtelseType.FORELDREPENGER, periode, TemaUnderkategori.FORELDREPENGER))));
        iayGrunnlag.medInntekterDto(new InntekterDto(List.of(new UtbetalingDto(InntektskildeType.INNTEKT_BEREGNING, List.of(new UtbetalingsPostDto(periode, InntektspostType.LØNN, BigDecimal.valueOf(1000L)))))));
        iayGrunnlag.medInntektsmeldingerDto(new InntektsmeldingerDto(List.of(new InntektsmeldingDto(organisasjon, new BeløpDto(BigDecimal.valueOf(100)), List.of(), List.of(), null, null, null, null))));
        return iayGrunnlag;
    }
}
