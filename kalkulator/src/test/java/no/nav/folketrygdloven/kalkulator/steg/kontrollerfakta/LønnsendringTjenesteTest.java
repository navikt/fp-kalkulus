package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.ArbeidsforholdInformasjonDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.ArbeidsforholdInformasjonDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.ArbeidsforholdOverstyringDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.VersjonTypeDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.AktørId;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.testutilities.BeregningIAYTestUtil;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.ArbeidType;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.ArbeidsforholdHandlingType;

public class LønnsendringTjenesteTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT_OPPTJENING = LocalDate.of(2018, 9, 30);
    private static final KoblingReferanse KOBLING_REFERANSE = new KoblingReferanseMock(SKJÆRINGSTIDSPUNKT_OPPTJENING);
    private static final AktørId AKTØR_ID = KOBLING_REFERANSE.getAktørId();

    @Test
    public void skalTesteAtAksjonspunktOpprettesNårBrukerHarLønnsendringUtenInntektsmelding() {
        // Arrange
        var arbId = InternArbeidsforholdRefDto.nyRef();
        String orgnr = "123456780";
        Intervall periode = Intervall.fraOgMed(SKJÆRINGSTIDSPUNKT_OPPTJENING);
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlagMedArbeid(arbId, orgnr, periode, SKJÆRINGSTIDSPUNKT_OPPTJENING);
        InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        BeregningIAYTestUtil.byggArbeidForBehandling(KOBLING_REFERANSE, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(1),
            SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(5).minusDays(2), arbId, Arbeidsgiver.virksomhet(orgnr),
            Optional.of(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(2L)),
            iayGrunnlagBuilder);

        // Act
        boolean brukerHarLønnsendring = LønnsendringTjeneste.brukerHarHattLønnsendringOgManglerInntektsmelding(AKTØR_ID, beregningsgrunnlag, iayGrunnlagBuilder.build());

        // Assert
        assertThat(brukerHarLønnsendring).isTrue();
    }

    private ArbeidsforholdInformasjonDto brukUtenInntektsmelding(InternArbeidsforholdRefDto arbId, String orgnr) {
        ArbeidsforholdInformasjonDtoBuilder informasjonBuilder = ArbeidsforholdInformasjonDtoBuilder
            .oppdatere(Optional.empty());
        ArbeidsforholdOverstyringDtoBuilder overstyringBuilder = informasjonBuilder.getOverstyringBuilderFor(Arbeidsgiver.virksomhet(orgnr), arbId);
        overstyringBuilder.medHandling(ArbeidsforholdHandlingType.BRUK_UTEN_INNTEKTSMELDING);
        informasjonBuilder.leggTil(overstyringBuilder);
        return informasjonBuilder.build();
    }

    @Test
    public void skalTesteAtAksjonspunktIkkeOpprettesNårBrukerHarLønnsendringUtenforBeregningsperioden() {
        // Arrange
        var arbId = InternArbeidsforholdRefDto.nyRef();
        String orgnr = "123456780";
        Intervall periode = Intervall.fraOgMed(SKJÆRINGSTIDSPUNKT_OPPTJENING);
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlagMedArbeid(arbId, orgnr, periode, SKJÆRINGSTIDSPUNKT_OPPTJENING);
        InntektArbeidYtelseAggregatBuilder oppdatere = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder = oppdatere.getAktørArbeidBuilder(AKTØR_ID);
        leggTilAktivitet(arbId, orgnr, periode, aktørArbeidBuilder, Optional.of(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(4L)));
        InntektArbeidYtelseGrunnlagDto iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.oppdatere(Optional.empty())
            .medData(oppdatere)
            .medInformasjon(brukUtenInntektsmelding(arbId, orgnr))
            .build();

        // Act
        boolean brukerHarLønnsendring = LønnsendringTjeneste.brukerHarHattLønnsendringOgManglerInntektsmelding(AKTØR_ID, beregningsgrunnlag, iayGrunnlag);

        // Assert
        assertThat(brukerHarLønnsendring).isFalse();
    }

    @Test
    public void skalFåAksjonspunktNårOverstyrtPeriodeInneholderSkjæringstidspunkt() {
        // Arrange
        ArbeidType arbeidType = ArbeidType.ORDINÆRT_ARBEIDSFORHOLD;
        var arbId = InternArbeidsforholdRefDto.nyRef();
        String orgnr = "123456780";
        Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(orgnr);
        Optional<LocalDate> lønnsendringsdato = Optional.of(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(1L));
        Intervall periode = Intervall.fraOgMed(SKJÆRINGSTIDSPUNKT_OPPTJENING);
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlagMedArbeid(arbId, orgnr, periode, SKJÆRINGSTIDSPUNKT_OPPTJENING);
        InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        BeregningIAYTestUtil.byggArbeidForBehandling(KOBLING_REFERANSE,
            SKJÆRINGSTIDSPUNKT_OPPTJENING,
            SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(1),
            null, arbId, arbeidsgiver, arbeidType,
            singletonList(BigDecimal.TEN), true, lønnsendringsdato,
            iayGrunnlagBuilder);

        InntektArbeidYtelseGrunnlagDto iayGrunnlag = iayGrunnlagBuilder.build();
        medOverstyrtPeriode(arbeidType, arbeidsgiver, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(10), SKJÆRINGSTIDSPUNKT_OPPTJENING, iayGrunnlag);

        // Act
        boolean brukerHarLønnsendring = LønnsendringTjeneste.brukerHarHattLønnsendringOgManglerInntektsmelding(AKTØR_ID, beregningsgrunnlag, iayGrunnlag);

        // Assert
        assertThat(brukerHarLønnsendring).isTrue();
    }

    @Test
    public void skalIkkeFåAksjonspunktNårOverstyrtPeriodeIkkeInneholderSkjæringstidspunkt() {
        // Arrange
        ArbeidType arbeidType = ArbeidType.ORDINÆRT_ARBEIDSFORHOLD;
        var arbId = InternArbeidsforholdRefDto.nyRef();
        String orgnr = "123456780";
        Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(orgnr);
        Optional<LocalDate> lønnsendringsdato = Optional.of(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(1L));
        LocalDate fraOgMed = SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(1);
        Intervall periode = Intervall.fraOgMed(fraOgMed);
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlagMedArbeid(arbId, orgnr, periode, SKJÆRINGSTIDSPUNKT_OPPTJENING);
        InntektArbeidYtelseAggregatBuilder oppdatere = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder = oppdatere.getAktørArbeidBuilder(AKTØR_ID);
        leggTilAktivitet(arbId, orgnr, periode, aktørArbeidBuilder, lønnsendringsdato);
        InntektArbeidYtelseGrunnlagDto iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.oppdatere(Optional.empty())
            .medData(oppdatere)
            .medInformasjon(brukUtenInntektsmelding(arbId, orgnr))
            .build();
        medOverstyrtPeriode(arbeidType, arbeidsgiver, fraOgMed, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusDays(1), iayGrunnlag);

        // Act
        boolean brukerHarLønnsendring = LønnsendringTjeneste.brukerHarHattLønnsendringOgManglerInntektsmelding(AKTØR_ID, beregningsgrunnlag, iayGrunnlag);

        // Assert
        assertThat(brukerHarLønnsendring).isFalse();
    }

    private void medOverstyrtPeriode(ArbeidType arbeidType, Arbeidsgiver arbeidsgiver, LocalDate fom, LocalDate tom, InntektArbeidYtelseGrunnlagDto grunnlag) {
        var filter = new YrkesaktivitetFilterDto(grunnlag.getArbeidsforholdInformasjon(), grunnlag.getAktørArbeidFraRegister(AKTØR_ID)).før(tom);

        if (!filter.getYrkesaktiviteter().isEmpty()) {
            YrkesaktivitetDto yrkesaktivitet = BeregningIAYTestUtil.finnKorresponderendeYrkesaktivitet(filter, arbeidType, arbeidsgiver);

            ArbeidsforholdInformasjonDtoBuilder informasjonBuilder = ArbeidsforholdInformasjonDtoBuilder
                .oppdatere(Optional.of(grunnlag));

            ArbeidsforholdOverstyringDtoBuilder overstyringBuilderFor = informasjonBuilder.getOverstyringBuilderFor(yrkesaktivitet.getArbeidsgiver(),
                yrkesaktivitet.getArbeidsforholdRef());
            overstyringBuilderFor.medHandling(ArbeidsforholdHandlingType.BRUK_MED_OVERSTYRT_PERIODE);
            overstyringBuilderFor.leggTilOverstyrtPeriode(fom, tom);
            informasjonBuilder.leggTil(overstyringBuilderFor);

            InntektArbeidYtelseGrunnlagDtoBuilder.oppdatere(Optional.of(grunnlag))
                .medInformasjon(informasjonBuilder.build()).build();
        }
    }

    private void leggTilAktivitet(InternArbeidsforholdRefDto arbId, String orgnr, Intervall periode, InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder, Optional<LocalDate> sisteLønnsendringsdato) {
        YrkesaktivitetDtoBuilder yrkesaktivitetBuilder = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
            .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
            .medArbeidsforholdId(arbId)
            .medArbeidsgiver(Arbeidsgiver.virksomhet(orgnr));
        yrkesaktivitetBuilder.leggTilAktivitetsAvtale(yrkesaktivitetBuilder.getAktivitetsAvtaleBuilder(periode, true));
        sisteLønnsendringsdato.ifPresent(d -> yrkesaktivitetBuilder.leggTilAktivitetsAvtale(yrkesaktivitetBuilder.getAktivitetsAvtaleBuilder(periode, false).medSisteLønnsendringsdato(d)));
        aktørArbeidBuilder.leggTilYrkesaktivitet(yrkesaktivitetBuilder);
    }

    private BeregningsgrunnlagDto lagBeregningsgrunnlagMedArbeid(InternArbeidsforholdRefDto arbId, String orgnr, Intervall periode, LocalDate skjæringstidspunktOpptjening) {
        BeregningsgrunnlagDto beregningsgrunnlag = BeregningsgrunnlagDto.Builder.oppdater(Optional.empty())
            .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER))
            .medSkjæringstidspunkt(skjæringstidspunktOpptjening)
            .build();
        BeregningsgrunnlagPeriodeDto bgPeriode = BeregningsgrunnlagPeriodeDto.builder()
            .medBeregningsgrunnlagPeriode(skjæringstidspunktOpptjening, null)
            .build(beregningsgrunnlag);
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medBeregningsperiode(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(3), SKJÆRINGSTIDSPUNKT_OPPTJENING)
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder()
                .medArbeidsforholdRef(arbId)
                .medArbeidsgiver(Arbeidsgiver.virksomhet(orgnr))
                .medArbeidsperiodeFom(periode.getFomDato())
                .medArbeidsperiodeTom(periode.getTomDato()))
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .build(bgPeriode);
        return beregningsgrunnlag;
    }



}
