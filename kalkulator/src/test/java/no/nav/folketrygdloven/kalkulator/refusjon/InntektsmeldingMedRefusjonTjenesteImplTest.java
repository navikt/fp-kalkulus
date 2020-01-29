package no.nav.folketrygdloven.kalkulator.refusjon;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.BehandlingReferanseMock;
import no.nav.folketrygdloven.kalkulator.BeregningsgrunnlagInputTestUtil;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.InntektsmeldingMedRefusjonTjeneste;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.BehandlingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.VersjonTypeDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.testutilities.BeregningInntektsmeldingTestUtil;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;

import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.ArbeidType;
import no.nav.vedtak.konfig.Tid;

public class InntektsmeldingMedRefusjonTjenesteImplTest {
    private static final String ORGNR = "974760673";
    private static final String ORGNR2 = "915933149";

    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now();

    private BehandlingReferanse behandlingReferanse = new BehandlingReferanseMock(SKJÆRINGSTIDSPUNKT);

    @Test
    public void skal_finne_arbeidsgivere_som_har_søkt_for_sent() {
        // Arrange
        Map<Arbeidsgiver, LocalDate> førsteInnsendingMap = new HashMap<>();
        InntektArbeidYtelseAggregatBuilder registerBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        BeregningAktivitetAggregatDto aktivitetAggregat = leggTilAktivitet(registerBuilder, List.of(ORGNR, ORGNR2));
        Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(ORGNR);
        Arbeidsgiver arbeidsgiver2 = Arbeidsgiver.virksomhet(ORGNR2);
        InntektsmeldingDto im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORGNR, SKJÆRINGSTIDSPUNKT, BigDecimal.TEN, BigDecimal.TEN);
        førsteInnsendingMap.put(arbeidsgiver, SKJÆRINGSTIDSPUNKT.plusMonths(4));
        InntektsmeldingDto im2 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORGNR2, SKJÆRINGSTIDSPUNKT, BigDecimal.TEN, BigDecimal.TEN);
        førsteInnsendingMap.put(arbeidsgiver2, SKJÆRINGSTIDSPUNKT.plusMonths(2));
        BeregningsgrunnlagGrunnlagDtoBuilder grunnlag = byggGrunnlag(aktivitetAggregat, List.of(arbeidsgiver, arbeidsgiver2), behandlingReferanse);
        InntektArbeidYtelseGrunnlagDto iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.oppdatere(Optional.empty())
            .medData(registerBuilder)
            .medInntektsmeldinger(List.of(im1, im2)).build();
        BeregningsgrunnlagInput input = BeregningsgrunnlagInputTestUtil.lagInputMedBeregningsgrunnlagOgIAY(behandlingReferanse, grunnlag,
            BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER, iayGrunnlag, førsteInnsendingMap);

        // Act
        Set<Arbeidsgiver> arbeidsgivereSomHarSøktForSent = InntektsmeldingMedRefusjonTjeneste.finnArbeidsgiverSomHarSøktRefusjonForSent(
            behandlingReferanse,
            input.getIayGrunnlag(),
            input.getBeregningsgrunnlagGrunnlag(),
            input.getRefusjonskravDatoer());

        // Assert
        assertThat(arbeidsgivereSomHarSøktForSent.size()).isEqualTo(1);
        assertThat(arbeidsgivereSomHarSøktForSent.iterator().next()).isEqualTo(arbeidsgiver);
    }


    @Test
    public void skal_returnere_tomt_set_om_ingen_inntektsmeldinger_er_mottatt() {
        // Arrange
        InntektArbeidYtelseAggregatBuilder registerBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        BeregningAktivitetAggregatDto aktivitetAggregat = leggTilAktivitet(registerBuilder, List.of(ORGNR, ORGNR2));
        Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(ORGNR);
        Arbeidsgiver arbeidsgiver2 = Arbeidsgiver.virksomhet(ORGNR2);
        BeregningsgrunnlagGrunnlagDtoBuilder grunnlag = byggGrunnlag(aktivitetAggregat, List.of(arbeidsgiver, arbeidsgiver2), behandlingReferanse);
        InntektArbeidYtelseGrunnlagDto iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.oppdatere(Optional.empty())
            .medData(registerBuilder)
            .medInntektsmeldinger(List.of()).build();

        // Act
        BeregningsgrunnlagInput input = BeregningsgrunnlagInputTestUtil.lagInputMedBeregningsgrunnlagOgIAY(behandlingReferanse, grunnlag, BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER, iayGrunnlag);
        Set<Arbeidsgiver> arbeidsgivereSomHarSøktForSent = InntektsmeldingMedRefusjonTjeneste.finnArbeidsgiverSomHarSøktRefusjonForSent(
            behandlingReferanse,
            input.getIayGrunnlag(),
            input.getBeregningsgrunnlagGrunnlag(),
            input.getRefusjonskravDatoer()
            );

        // Assert
        assertThat(arbeidsgivereSomHarSøktForSent.size()).isEqualTo(0);
    }

    @Test
    public void skal_returnere_tomt_set_om_ingen_inntektsmeldinger_er_mottatt_for_sent() {
        // Arrange
        Map<Arbeidsgiver, LocalDate> førsteInnsendingMap = new HashMap<>();
        InntektArbeidYtelseAggregatBuilder registerBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        BeregningAktivitetAggregatDto aktivitetAggregat = leggTilAktivitet(registerBuilder, List.of(ORGNR, ORGNR2));
        Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(ORGNR);
        Arbeidsgiver arbeidsgiver2 = Arbeidsgiver.virksomhet(ORGNR2);
        InntektsmeldingDto im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORGNR, SKJÆRINGSTIDSPUNKT, BigDecimal.TEN, BigDecimal.TEN);
        førsteInnsendingMap.put(arbeidsgiver, SKJÆRINGSTIDSPUNKT.plusMonths(1));
        InntektsmeldingDto im2 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORGNR2, SKJÆRINGSTIDSPUNKT, BigDecimal.TEN, BigDecimal.TEN);
        førsteInnsendingMap.put(arbeidsgiver2, SKJÆRINGSTIDSPUNKT.plusMonths(2));
        BeregningsgrunnlagGrunnlagDtoBuilder grunnlag = byggGrunnlag(aktivitetAggregat, List.of(arbeidsgiver, arbeidsgiver2), behandlingReferanse);
        InntektArbeidYtelseGrunnlagDto iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.oppdatere(Optional.empty())
            .medData(registerBuilder)
            .medInntektsmeldinger(List.of(im1, im2)).build();

        // Act
        BeregningsgrunnlagInput input = BeregningsgrunnlagInputTestUtil.lagInputMedBeregningsgrunnlagOgIAY(behandlingReferanse, grunnlag,
            BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER, iayGrunnlag, førsteInnsendingMap);
        Set<Arbeidsgiver> arbeidsgivereSomHarSøktForSent = InntektsmeldingMedRefusjonTjeneste.finnArbeidsgiverSomHarSøktRefusjonForSent(
            behandlingReferanse,
            input.getIayGrunnlag(),
            input.getBeregningsgrunnlagGrunnlag(),
            input.getRefusjonskravDatoer()
        );

        // Assert
        assertThat(arbeidsgivereSomHarSøktForSent.size()).isEqualTo(0);
    }


    private BeregningsgrunnlagGrunnlagDtoBuilder byggGrunnlag(BeregningAktivitetAggregatDto aktivitetAggregat, List<Arbeidsgiver> arbeidsgivere, BehandlingReferanse behandlingReferanse) {
        return BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
            .medRegisterAktiviteter(aktivitetAggregat)
            .medBeregningsgrunnlag(lagBeregningsgrunnlag(arbeidsgivere));
    }

    private BeregningsgrunnlagDto lagBeregningsgrunnlag(List<Arbeidsgiver> ags) {

        BeregningsgrunnlagDto bg = BeregningsgrunnlagDto.builder()
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)).build();
        BeregningsgrunnlagPeriodeDto periode = BeregningsgrunnlagPeriodeDto.builder().medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null)
            .build(bg);
        ags.forEach(ag -> BeregningsgrunnlagPrStatusOgAndelDto.kopier()
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(ag))
            .build(periode)
        );
        return bg;
    }


    private BeregningAktivitetAggregatDto leggTilAktivitet(InntektArbeidYtelseAggregatBuilder iayAggregatBuilder, List<String> orgnr) {
        Intervall arbeidsperiode1 = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT.minusYears(2), Tid.TIDENES_ENDE);
        var aktørArbeidBuilder = InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder.oppdatere(Optional.empty())
            .medAktørId(behandlingReferanse.getAktørId());
        BeregningAktivitetAggregatDto.Builder aktivitetAggregatBuilder = BeregningAktivitetAggregatDto.builder()
            .medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT);
        for (String nr : orgnr) {
            Arbeidsgiver ag = leggTilYrkesaktivitet(arbeidsperiode1, aktørArbeidBuilder, nr);
            aktivitetAggregatBuilder.leggTilAktivitet(lagAktivitet(arbeidsperiode1, ag));
        }
        iayAggregatBuilder.leggTilAktørArbeid(aktørArbeidBuilder);
        return aktivitetAggregatBuilder.build();
    }

    private BeregningAktivitetDto lagAktivitet(Intervall arbeidsperiode1, Arbeidsgiver ag) {
        return BeregningAktivitetDto.builder().medArbeidsgiver(ag).medOpptjeningAktivitetType(OpptjeningAktivitetType.ARBEID).medPeriode(Intervall.fraOgMedTilOgMed(arbeidsperiode1.getFomDato(), arbeidsperiode1.getTomDato())).build();
    }

    private Arbeidsgiver leggTilYrkesaktivitet(Intervall arbeidsperiode, InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder, String orgnr) {
        Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(orgnr);
        AktivitetsAvtaleDtoBuilder aaBuilder1 = AktivitetsAvtaleDtoBuilder.ny()
            .medPeriode(arbeidsperiode);
        YrkesaktivitetDtoBuilder yaBuilder = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
            .medArbeidsgiver(arbeidsgiver)
            .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
            .leggTilAktivitetsAvtale(aaBuilder1);
        aktørArbeidBuilder.leggTilYrkesaktivitet(yaBuilder);
        return arbeidsgiver;
    }

}
