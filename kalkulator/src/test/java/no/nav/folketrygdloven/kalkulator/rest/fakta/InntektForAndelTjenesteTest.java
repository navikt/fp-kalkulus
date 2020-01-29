package no.nav.folketrygdloven.kalkulator.rest.fakta;

import static java.util.Collections.singletonList;
import static no.nav.folketrygdloven.kalkulator.rest.MapBeregningsgrunnlagFraRestTilDomene.mapArbeidsgiver;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.BehandlingReferanseMock;
import no.nav.folketrygdloven.kalkulator.modell.behandling.BehandlingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektspostDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.VersjonTypeDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.kodeverk.InntektsKilde;
import no.nav.folketrygdloven.kalkulator.modell.iay.kodeverk.InntektspostType;
import no.nav.folketrygdloven.kalkulator.modell.iay.kodeverk.SkatteOgAvgiftsregelType;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.ArbeidType;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.ArbeidsgiverMedNavn;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;


public class InntektForAndelTjenesteTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT_OPPTJENING = LocalDate.of(2018, 9, 30);
    private static final BigDecimal INNTEKT1 = BigDecimal.valueOf(25000);
    private static final BigDecimal INNTEKT2 = BigDecimal.valueOf(30000);
    private static final BigDecimal INNTEKT3 = BigDecimal.valueOf(35000);
    private static final BigDecimal SNITT_AV_ULIKE_INNTEKTER = INNTEKT1.add(INNTEKT2).add(INNTEKT3).divide(BigDecimal.valueOf(3), RoundingMode.HALF_UP);
    public static final String ORGNR = "379472397427";
    private static final InternArbeidsforholdRefDto ARB_ID = InternArbeidsforholdRefDto.namedRef("TEST-REF");
    private static final String FRILANS_OPPDRAG_ORGNR = "784385345";
    private static final String FRILANS_OPPDRAG_ORGNR2 = "748935793457";

    private ArbeidsgiverMedNavn arbeidsgiver;
    private ArbeidsgiverMedNavn frilansArbeidsgiver;
    private ArbeidsgiverMedNavn frilansArbeidsgiver2;
    private YrkesaktivitetDto arbeidstakerYrkesaktivitet;
    private YrkesaktivitetDto frilansOppdrag;
    private YrkesaktivitetDto frilansOppdrag2;
    private YrkesaktivitetDto frilans;
    private BeregningsgrunnlagPrStatusOgAndelRestDto arbeidstakerAndel;
    private BeregningsgrunnlagPrStatusOgAndelRestDto frilansAndel;
    private BeregningsgrunnlagPeriodeRestDto periode;
    private BehandlingReferanse behandlingReferanse = new BehandlingReferanseMock();


    @BeforeEach
    public void setUp() {
        byggArbeidsgiver();
        byggArbeidstakerYrkesaktivitet();
        byggFrilansOppdragAktivitet();
        byggFrilansAktivitet();
        lagBGPeriode();
        lagArbeidstakerAndel();
        lagFrilansAndel();

    }

    @Test
    public void skal_finne_snitt_inntekt_for_arbeidstaker_med_lik_inntekt_pr_mnd() {
        var aktørInntekt = lagAktørInntekt(singletonList(lagLikInntektSiste3Mnd(mapArbeidsgiver(arbeidsgiver))));
        var filter = new InntektFilterDto(aktørInntekt.build());
        BigDecimal snittIBeregningsperioden = InntektForAndelTjeneste.finnSnittinntektForArbeidstakerIBeregningsperioden(filter, arbeidstakerAndel);
        assertThat(snittIBeregningsperioden).isEqualByComparingTo(INNTEKT1);
    }

    @Test
    public void skal_finne_snitt_inntekt_for_arbeidstaker_med_ulik_inntekt_pr_mnd() {
        var aktørInntekt = lagAktørInntekt(singletonList(lagUlikInntektSiste3Mnd(mapArbeidsgiver(arbeidsgiver))));
        var filter = new InntektFilterDto(aktørInntekt.build());
        BigDecimal snittIBeregningsperioden = InntektForAndelTjeneste.finnSnittinntektForArbeidstakerIBeregningsperioden(filter, arbeidstakerAndel);
        assertThat(snittIBeregningsperioden).isEqualByComparingTo(SNITT_AV_ULIKE_INNTEKTER);
    }

    @Test
    public void skal_finne_snitt_inntekt_for_frilans_med_lik_inntekt_pr_mnd() {
        List<InntektDtoBuilder> inntekter = List.of(lagLikInntektSiste3Mnd(mapArbeidsgiver(arbeidsgiver)), lagLikInntektSiste3Mnd(mapArbeidsgiver(frilansArbeidsgiver)));
        List<YrkesaktivitetDto> aktiviteter = List.of(arbeidstakerYrkesaktivitet, frilans, frilansOppdrag);
        var grunnlagEntitet = lagIAYGrunnlagEntitet(inntekter, aktiviteter);
        Optional<BigDecimal> snittIBeregningsperioden = InntektForAndelTjeneste.finnSnittAvFrilansinntektIBeregningsperioden(behandlingReferanse.getAktørId(), grunnlagEntitet, frilansAndel, SKJÆRINGSTIDSPUNKT_OPPTJENING);
        assertThat(snittIBeregningsperioden).hasValueSatisfying(a -> assertThat(a).isEqualByComparingTo(INNTEKT1));
    }

    @Test
    public void skal_finne_snitt_inntekt_for_frilans_med_ulik_inntekt_pr_mnd() {
        List<InntektDtoBuilder> inntekter = List.of(lagLikInntektSiste3Mnd(mapArbeidsgiver(arbeidsgiver)), lagUlikInntektSiste3Mnd(mapArbeidsgiver(frilansArbeidsgiver)));
        List<YrkesaktivitetDto> aktiviteter = List.of(arbeidstakerYrkesaktivitet, frilans, frilansOppdrag);
        var grunnlagEntitet = lagIAYGrunnlagEntitet(inntekter, aktiviteter);
        Optional<BigDecimal> snittIBeregningsperioden = InntektForAndelTjeneste.finnSnittAvFrilansinntektIBeregningsperioden(behandlingReferanse.getAktørId(), grunnlagEntitet, frilansAndel, SKJÆRINGSTIDSPUNKT_OPPTJENING);
        assertThat(snittIBeregningsperioden).hasValueSatisfying(a -> assertThat(a).isEqualByComparingTo(SNITT_AV_ULIKE_INNTEKTER));
    }

    @Test
    public void skal_finne_snitt_inntekt_for_frilans_med_fleire_oppdragsgivere() {
        List<InntektDtoBuilder> inntekter = List.of(lagLikInntektSiste3Mnd(mapArbeidsgiver(arbeidsgiver)), lagUlikInntektSiste3Mnd(mapArbeidsgiver(frilansArbeidsgiver)), lagUlikInntektSiste3Mnd(mapArbeidsgiver(frilansArbeidsgiver2)));
        List<YrkesaktivitetDto> aktiviteter = List.of(arbeidstakerYrkesaktivitet, frilans, frilansOppdrag, frilansOppdrag2);
        var grunnlagEntitet = lagIAYGrunnlagEntitet(inntekter, aktiviteter);
        Optional<BigDecimal> snittIBeregningsperioden = InntektForAndelTjeneste.finnSnittAvFrilansinntektIBeregningsperioden(behandlingReferanse.getAktørId(), grunnlagEntitet, frilansAndel, SKJÆRINGSTIDSPUNKT_OPPTJENING);
        assertThat(snittIBeregningsperioden).hasValueSatisfying(a -> assertThat(a).isEqualByComparingTo(SNITT_AV_ULIKE_INNTEKTER.multiply(BigDecimal.valueOf(2))));
    }

    private InntektArbeidYtelseGrunnlagDto lagIAYGrunnlagEntitet(List<InntektDtoBuilder> inntekter, List<YrkesaktivitetDto> aktiviteter) {
        var aggregatBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        var aktørInntektBuilder = aggregatBuilder.getAktørInntektBuilder(behandlingReferanse.getAktørId());
        var iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.oppdatere(Optional.empty())
            .medData(InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER)
                .leggTilAktørArbeid(lagAktørArbeid(aktiviteter))
                .leggTilAktørInntekt(lagAktørInntekt(aktørInntektBuilder, inntekter)));
        return iayGrunnlag.build();
    }

    private InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder lagAktørArbeid(List<YrkesaktivitetDto> aktiviteter) {
        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder builder = InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder.oppdatere(Optional.empty())
            .medAktørId(behandlingReferanse.getAktørId());
        aktiviteter.forEach(builder::leggTilYrkesaktivitet);
        return builder;
    }

    private AktivitetsAvtaleDtoBuilder lagAktivitetsavtale() {
        return AktivitetsAvtaleDtoBuilder.ny()
            .medPeriode(DatoIntervallEntitet.fraOgMed(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusYears(2)));
    }

    private InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder lagAktørInntekt(List<InntektDtoBuilder> inntektList) {
        InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder builder = InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder.oppdatere(Optional.empty());
        inntektList.forEach(builder::leggTilInntekt);
        return builder;
    }

    private InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder lagAktørInntekt(InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder builder, List<InntektDtoBuilder> inntektList) {
        inntektList.forEach(builder::leggTilInntekt);
        return builder;
    }

    private InntektDtoBuilder lagLikInntektSiste3Mnd(Arbeidsgiver arbeidsgiver) {
        return InntektDtoBuilder.oppdatere(Optional.empty())
        .leggTilInntektspost(lagInntektspost(INNTEKT1, 1))
        .leggTilInntektspost(lagInntektspost(INNTEKT1, 2))
        .leggTilInntektspost(lagInntektspost(INNTEKT1, 3))
        .medArbeidsgiver(arbeidsgiver)
        .medInntektsKilde(InntektsKilde.INNTEKT_BEREGNING);
    }

    private InntektDtoBuilder lagUlikInntektSiste3Mnd(Arbeidsgiver arbeidsgiver) {
        return InntektDtoBuilder.oppdatere(Optional.empty())
            .leggTilInntektspost(lagInntektspost(INNTEKT1, 1))
            .leggTilInntektspost(lagInntektspost(INNTEKT2, 2))
            .leggTilInntektspost(lagInntektspost(INNTEKT3, 3))
            .medArbeidsgiver(arbeidsgiver)
            .medInntektsKilde(InntektsKilde.INNTEKT_BEREGNING);
    }


    private InntektspostDtoBuilder lagInntektspost(BigDecimal inntekt, int mndFørSkjæringstidspunkt) {
        return InntektspostDtoBuilder.ny().medBeløp(inntekt)
            .medInntektspostType(InntektspostType.LØNN)
            .medPeriode(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(mndFørSkjæringstidspunkt).withDayOfMonth(1),
                SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(mndFørSkjæringstidspunkt-1).withDayOfMonth(1).minusDays(1))
            .medSkatteOgAvgiftsregelType(SkatteOgAvgiftsregelType.UDEFINERT);
    }

    private void lagArbeidstakerAndel() {
        arbeidstakerAndel = BeregningsgrunnlagPrStatusOgAndelRestDto.kopier()
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medBeregningsperiode(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(3).withDayOfMonth(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.withDayOfMonth(1).minusDays(1))
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdRestDto.builder().medArbeidsgiver(arbeidsgiver))
            .medAndelsnr(1L)
            .build(periode);
    }

    private void lagBGPeriode() {
        BeregningsgrunnlagRestDto beregningsgrunnlag = BeregningsgrunnlagRestDto.builder()
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_OPPTJENING)
            .medGrunnbeløp(BigDecimal.valueOf(91425L))
            .build();
        periode = BeregningsgrunnlagPeriodeRestDto.builder()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_OPPTJENING, null)
            .build(beregningsgrunnlag);
    }

    private void byggArbeidstakerYrkesaktivitet() {
        arbeidstakerYrkesaktivitet = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
            .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
            .medArbeidsgiver(mapArbeidsgiver(arbeidsgiver))
            .medArbeidsforholdId(ARB_ID)
            .leggTilAktivitetsAvtale(lagAktivitetsavtale())
            .build();
    }

    private void byggArbeidsgiver() {
        arbeidsgiver = ArbeidsgiverMedNavn.virksomhet(ORGNR);
        frilansArbeidsgiver = ArbeidsgiverMedNavn.virksomhet(FRILANS_OPPDRAG_ORGNR);
        frilansArbeidsgiver2 = ArbeidsgiverMedNavn.virksomhet(FRILANS_OPPDRAG_ORGNR2);
    }

    private void lagFrilansAndel() {
        frilansAndel = BeregningsgrunnlagPrStatusOgAndelRestDto.kopier()
            .medAktivitetStatus(AktivitetStatus.FRILANSER)
            .medBeregningsperiode(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(3).withDayOfMonth(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.withDayOfMonth(1).minusDays(1))
            .medAndelsnr(2L)
            .build(periode);
    }

    private void byggFrilansAktivitet() {
        frilans = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
            .medArbeidType(ArbeidType.FRILANSER)
            .build();
    }

    private void byggFrilansOppdragAktivitet() {
        frilansOppdrag = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
            .medArbeidType(ArbeidType.FRILANSER_OPPDRAGSTAKER_MED_MER)
            .medArbeidsgiver(mapArbeidsgiver(frilansArbeidsgiver))
            .leggTilAktivitetsAvtale(lagAktivitetsavtale())
            .build();
        frilansOppdrag2 = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
            .medArbeidType(ArbeidType.FRILANSER_OPPDRAGSTAKER_MED_MER)
            .medArbeidsgiver(mapArbeidsgiver(frilansArbeidsgiver2))
            .leggTilAktivitetsAvtale(lagAktivitetsavtale())
            .build();
    }

}
