package no.nav.folketrygdloven.kalkulus.domene.mappers;

import static no.nav.folketrygdloven.kalkulus.domene.mappers.MapFraKalkulator.mapFraKalkulatorInputTilBeregningsgrunnlagInput;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.AktørId;

import no.nav.foreldrepenger.kalkulus.kontrakt.request.input.foreldrepenger.ForeldrepengerGrunnlag;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.input.foreldrepenger.gradering.AktivitetGraderingDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.input.foreldrepenger.gradering.AndelGraderingDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.input.foreldrepenger.gradering.GraderingDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.input.iay.arbeid.AktivitetsAvtaleDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.input.opptjening.OpptjeningAktiviteterDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.input.opptjening.OpptjeningPeriodeDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.typer.Periode;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.KoblingReferanse;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Saksnummer;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.foreldrepenger.kalkulus.kontrakt.typer.Aktivitetsgrad;
import no.nav.foreldrepenger.kalkulus.kontrakt.typer.Beløp;
import no.nav.foreldrepenger.kalkulus.kontrakt.typer.InternArbeidsforholdRefDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.input.KalkulatorInputDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.typer.Organisasjon;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.input.iay.IayProsent;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.input.iay.arbeid.ArbeidDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.input.iay.arbeid.YrkesaktivitetDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.input.iay.inntekt.InntekterDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.input.iay.inntekt.InntektsmeldingDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.input.iay.inntekt.InntektsmeldingerDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.input.iay.inntekt.UtbetalingDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.input.iay.inntekt.UtbetalingsPostDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.input.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.input.iay.ytelse.YtelseDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.input.iay.ytelse.YtelserDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.kodeverk.InntektskildeType;
import no.nav.folketrygdloven.kalkulus.kodeverk.InntektspostType;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseType;

class MapFraKalkulatorTest {

    private final Periode periode = new Periode(LocalDate.now(), LocalDate.now().plusMonths(2));
    private final Organisasjon organisasjon = new Organisasjon("974652269");
    private final InternArbeidsforholdRefDto ref = new InternArbeidsforholdRefDto(UUID.randomUUID().toString());


    @Test
    void skal_mappe_fra_kalkulator_til_beregningsgrunnlag_input() {
        String saksnummer = "1234";
        UUID randomUUID = UUID.randomUUID();
        Saksnummer saksnummer1 = new Saksnummer(saksnummer);
        KoblingEntitet koblingEntitet = new KoblingEntitet(new KoblingReferanse(randomUUID), FagsakYtelseType.FORELDREPENGER, saksnummer1, new AktørId("9999999999999"));
        KalkulatorInputDto kalkulatorInputDto = byggKalkulatorInput();

        BeregningsgrunnlagInput input = mapFraKalkulatorInputTilBeregningsgrunnlagInput(koblingEntitet, kalkulatorInputDto, Optional.empty());

        assertThat(input.getKoblingReferanse().getKoblingId()).isEqualTo(koblingEntitet.getId());
    }


    private KalkulatorInputDto byggKalkulatorInput() {
        GraderingDto graderingDto = new GraderingDto(periode, Aktivitetsgrad.fra(100));
        AndelGraderingDto andelGraderingDto = new AndelGraderingDto(AktivitetStatus.ARBEIDSTAKER, organisasjon, null, List.of(graderingDto));
        AktivitetGraderingDto aktivitetGraderingDto = new AktivitetGraderingDto(List.of(andelGraderingDto));

        InntektArbeidYtelseGrunnlagDto iayGrunnlag = byggIAY();
        OpptjeningAktiviteterDto opptjeningAktiviteter = new OpptjeningAktiviteterDto(List.of(new OpptjeningPeriodeDto(OpptjeningAktivitetType.ARBEID, periode, organisasjon, null)));
        LocalDate skjæringstidspunkt = periode.getFom();

        KalkulatorInputDto kalkulatorInputDto = new KalkulatorInputDto(iayGrunnlag, opptjeningAktiviteter, skjæringstidspunkt, new ForeldrepengerGrunnlag(BigDecimal.valueOf(100), false, aktivitetGraderingDto, Collections.emptyList(), skjæringstidspunkt));

        return kalkulatorInputDto;
    }

    private InntektArbeidYtelseGrunnlagDto byggIAY() {
        InntektArbeidYtelseGrunnlagDto iayGrunnlag = new InntektArbeidYtelseGrunnlagDto();
        iayGrunnlag.medArbeidDto(new ArbeidDto(List.of(new YrkesaktivitetDto(organisasjon, ref, ArbeidType.ORDINÆRT_ARBEIDSFORHOLD,
            List.of(new AktivitetsAvtaleDto(periode, null, IayProsent.fra(100)), new AktivitetsAvtaleDto(periode, null, null))))));
        iayGrunnlag.medYtelserDto(new YtelserDto(List.of(new YtelseDto(Beløp.fra(BigDecimal.TEN), Set.of(), YtelseType.FORELDREPENGER, periode, null))));
        iayGrunnlag.medInntekterDto(new InntekterDto(List.of(new UtbetalingDto(InntektskildeType.INNTEKT_BEREGNING, List.of(new UtbetalingsPostDto(periode, InntektspostType.LØNN, Beløp.fra(1000)))))));
        iayGrunnlag.medInntektsmeldingerDto(new InntektsmeldingerDto(List.of(new InntektsmeldingDto(organisasjon, Beløp.fra(100), List.of(), List.of(), null, null, null, null, null))));
        iayGrunnlag.medAlleInntektsmeldingerPåSak(List.of(new InntektsmeldingDto(organisasjon, Beløp.fra(100), List.of(), List.of(), null, null, null, null, null)));
        return iayGrunnlag;
    }
}
