package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektspostDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittOpptjeningDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.VersjonTypeDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.kodeverk.InntektskildeType;
import no.nav.folketrygdloven.kalkulus.kodeverk.InntektspostType;
import no.nav.folketrygdloven.kalkulus.kodeverk.SkatteOgAvgiftsregelType;
import no.nav.folketrygdloven.kalkulus.kodeverk.VirksomhetType;

public class FastsettInntektskategoriTjenesteTest {


    private static final LocalDate SKJÆRINGSTIDSPUNKT_OPPTJENING = LocalDate.of(2018, Month.MAY, 10);
    private static final BigDecimal GRUNNBELØP = BigDecimal.valueOf(90000);
    private static final Intervall BEREGNINGSPERIODE_ATFL = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(3).withDayOfMonth(1),
            SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth()));

    private static final String ARBEIDSFORHOLD_ORGNR = "973152351";

    private BeregningsgrunnlagDto lagBeregningsgrunnlag(AktivitetStatus aktivitetStatus) {
        BeregningsgrunnlagDto beregningsgrunnlag = BeregningsgrunnlagDto.builder()
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_OPPTJENING)
            .medGrunnbeløp(GRUNNBELØP)
            .build();
        BeregningsgrunnlagAktivitetStatusDto.builder()
            .medAktivitetStatus(aktivitetStatus)
            .build(beregningsgrunnlag);
        BeregningsgrunnlagPeriodeDto periode = BeregningsgrunnlagPeriodeDto.builder()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_OPPTJENING, null)
            .build(beregningsgrunnlag);
        BGAndelArbeidsforholdDto.Builder bga = BGAndelArbeidsforholdDto
            .builder()
            .medArbeidsperiodeFom(LocalDate.now().minusYears(1))
            .medArbeidsperiodeTom(LocalDate.now().plusYears(2))
            .medArbeidsgiver(Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR));
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medBGAndelArbeidsforhold(bga)
            .medAktivitetStatus(aktivitetStatus)
            .build(periode);
        return beregningsgrunnlag;
    }


    private InntektArbeidYtelseGrunnlagDto opprettOppgittOpptjening(List<VirksomhetType> næringtyper) {
        OppgittOpptjeningDtoBuilder oob = OppgittOpptjeningDtoBuilder.ny();
        ArrayList<OppgittOpptjeningDtoBuilder.EgenNæringBuilder> egneNæringBuilders = new ArrayList<>();
        LocalDate fraOgMed = LocalDate.now().minusMonths(1);
        LocalDate tilOgMed = LocalDate.now().plusMonths(1);
        Intervall periode = Intervall.fraOgMedTilOgMed(fraOgMed, tilOgMed);
        for (VirksomhetType type : næringtyper) {
            egneNæringBuilders.add(OppgittOpptjeningDtoBuilder.EgenNæringBuilder.ny().medVirksomhetType(type).medPeriode(periode));
        }
        oob.leggTilEgneNæringer(egneNæringBuilders);
        return InntektArbeidYtelseGrunnlagDtoBuilder.nytt().medOppgittOpptjening(oob).build();
    }

    private InntektspostDtoBuilder lagInntektspost(int månederFørStp, SkatteOgAvgiftsregelType skatteOgAvgiftsregelType) {
        LocalDate fom = SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(månederFørStp).withDayOfMonth(1);
        LocalDate tom = SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(månederFørStp).with(TemporalAdjusters.lastDayOfMonth());
        return InntektspostDtoBuilder.ny()
                .medInntektspostType(InntektspostType.LØNN)
                .medPeriode(fom, tom).medBeløp(BigDecimal.ONE)
                .medSkatteOgAvgiftsregelType(skatteOgAvgiftsregelType);
    }

    private InntektArbeidYtelseGrunnlagDto lagIAY(InntektDtoBuilder inntektBuilder) {
        InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder aktørInntektBuilder = InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder.oppdatere(Optional.empty()).leggTilInntekt(inntektBuilder);
        InntektArbeidYtelseAggregatBuilder data = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER).leggTilAktørInntekt(aktørInntektBuilder);
        InntektArbeidYtelseGrunnlagDto iay = InntektArbeidYtelseGrunnlagDtoBuilder.nytt().medData(data).build();
        return iay;
    }

    @Test
    public void arbeidstakerSkalTilRiktigInntektskategori() {
        // Arrange
        final var grunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.nytt().build();
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlag(AktivitetStatus.ARBEIDSTAKER);

        // Act
        FastsettInntektskategoriTjeneste.fastsettInntektskategori(beregningsgrunnlag, grunnlag);

        // Assert
        BeregningsgrunnlagPeriodeDto periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        List<BeregningsgrunnlagPrStatusOgAndelDto> andeler = periode.getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler).hasSize(1);
        assertThat(andeler.get(0).getInntektskategori()).isEqualTo(Inntektskategori.ARBEIDSTAKER);
    }

    @Test
    public void frilanserSkalTilRiktigInntektskategori() {
        // Arrange
        final var grunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.nytt().build();
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlag(AktivitetStatus.FRILANSER);

        // Act
        FastsettInntektskategoriTjeneste.fastsettInntektskategori(beregningsgrunnlag, grunnlag);

        // Assert
        BeregningsgrunnlagPeriodeDto periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        List<BeregningsgrunnlagPrStatusOgAndelDto> andeler = periode.getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler).hasSize(1);
        assertThat(andeler.get(0).getInntektskategori()).isEqualTo(Inntektskategori.FRILANSER);
    }


    @Test
    public void dagpengerSkalTilRiktigInntektskategori() {
        // Arrange
        final var grunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.nytt().build();
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlag(AktivitetStatus.DAGPENGER);

        // Act
        FastsettInntektskategoriTjeneste.fastsettInntektskategori(beregningsgrunnlag, grunnlag);

        // Assert
        BeregningsgrunnlagPeriodeDto periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        List<BeregningsgrunnlagPrStatusOgAndelDto> andeler = periode.getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler).hasSize(1);
        assertThat(andeler.get(0).getInntektskategori()).isEqualTo(Inntektskategori.DAGPENGER);
    }

    @Test
    public void arbeidsavklaringspengerSkalTilRiktigInntektskategori() {
        // Arrange
        final var grunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.nytt().build();
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlag(AktivitetStatus.ARBEIDSAVKLARINGSPENGER);

        // Act
        FastsettInntektskategoriTjeneste.fastsettInntektskategori(beregningsgrunnlag, grunnlag);

        // Assert
        BeregningsgrunnlagPeriodeDto periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        List<BeregningsgrunnlagPrStatusOgAndelDto> andeler = periode.getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler).hasSize(1);
        assertThat(andeler.get(0).getInntektskategori()).isEqualTo(Inntektskategori.ARBEIDSAVKLARINGSPENGER);
    }


    @Test
    public void SNUtenFiskeJordbrukEllerDagmammaSkalTilRiktigInntektskategori() {
        // Arrange
        final var grunnlag = opprettOppgittOpptjening(Collections.singletonList(VirksomhetType.ANNEN));
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlag(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);

        // Act
        FastsettInntektskategoriTjeneste.fastsettInntektskategori(beregningsgrunnlag, grunnlag);

        // Assert
        BeregningsgrunnlagPeriodeDto periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        List<BeregningsgrunnlagPrStatusOgAndelDto> andeler = periode.getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler).hasSize(1);
        assertThat(andeler.get(0).getInntektskategori()).isEqualTo(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE);
    }

    @Test
    public void SNMedFiskeSkalTilRiktigInntektskategori() {
        // Arrange
        final var grunnlag = opprettOppgittOpptjening(Collections.singletonList(VirksomhetType.FISKE));
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlag(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);

        // Act
        FastsettInntektskategoriTjeneste.fastsettInntektskategori(beregningsgrunnlag, grunnlag);

        // Assert
        BeregningsgrunnlagPeriodeDto periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        List<BeregningsgrunnlagPrStatusOgAndelDto> andeler = periode.getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler).hasSize(1);
        assertThat(andeler.get(0).getInntektskategori()).isEqualTo(Inntektskategori.FISKER);
    }

    @Test
    public void SNMedJorbrukSkalTilRiktigInntektskategori() {
        // Arrange
        final var grunnlag = opprettOppgittOpptjening(Collections.singletonList(VirksomhetType.JORDBRUK_SKOGBRUK));
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlag(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);

        // Act
        FastsettInntektskategoriTjeneste.fastsettInntektskategori(beregningsgrunnlag, grunnlag);

        // Assert
        BeregningsgrunnlagPeriodeDto periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        List<BeregningsgrunnlagPrStatusOgAndelDto> andeler = periode.getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler).hasSize(1);
        assertThat(andeler.get(0).getInntektskategori()).isEqualTo(Inntektskategori.JORDBRUKER);
    }

    @Test
    public void SNMedDagmammaSkalTilRiktigInntektskategori() {
        // Arrange
        final var grunnlag = opprettOppgittOpptjening(Collections.singletonList(VirksomhetType.DAGMAMMA));
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlag(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);

        // Act
        FastsettInntektskategoriTjeneste.fastsettInntektskategori(beregningsgrunnlag, grunnlag);

        // Assert
        BeregningsgrunnlagPeriodeDto periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        List<BeregningsgrunnlagPrStatusOgAndelDto> andeler = periode.getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler).hasSize(1);
        assertThat(andeler.get(0).getInntektskategori()).isEqualTo(Inntektskategori.DAGMAMMA);
    }

    @Test
    public void SNMedFiskeOgJordbrukSkalMappeTilInntektskategoriFisker() {
        // Arrange
        final var grunnlag = opprettOppgittOpptjening(List.of(VirksomhetType.FISKE, VirksomhetType.JORDBRUK_SKOGBRUK));
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlag(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);

        // Act
        FastsettInntektskategoriTjeneste.fastsettInntektskategori(beregningsgrunnlag, grunnlag);

        // Assert
        BeregningsgrunnlagPeriodeDto periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        List<BeregningsgrunnlagPrStatusOgAndelDto> andeler = periode.getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler).hasSize(1);
        assertThat(andeler.get(0).getInntektskategori()).isEqualTo(Inntektskategori.FISKER);
    }

    @Test
    public void SNMedFiskeOgDagmammaSkalMappeTilInntektskategoriFisker() {
        // Arrange
        final var grunnlag = opprettOppgittOpptjening(List.of(VirksomhetType.DAGMAMMA, VirksomhetType.FISKE));
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlag(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);

        // Act
        FastsettInntektskategoriTjeneste.fastsettInntektskategori(beregningsgrunnlag, grunnlag);

        // Assert
        BeregningsgrunnlagPeriodeDto periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        List<BeregningsgrunnlagPrStatusOgAndelDto> andeler = periode.getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler).hasSize(1);
        assertThat(andeler.get(0).getInntektskategori()).isEqualTo(Inntektskategori.FISKER);
    }

    @Test
    public void SNMedJordbrukOgDagmammaSkalMappeTilInntektskategoriJordbruker() {
        // Arrange
        final var grunnlag = opprettOppgittOpptjening(List.of(VirksomhetType.DAGMAMMA, VirksomhetType.JORDBRUK_SKOGBRUK));
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlag(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);

        // Act
        FastsettInntektskategoriTjeneste.fastsettInntektskategori(beregningsgrunnlag, grunnlag);

        // Assert
        BeregningsgrunnlagPeriodeDto periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        List<BeregningsgrunnlagPrStatusOgAndelDto> andeler = periode.getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler).hasSize(1);
        assertThat(andeler.get(0).getInntektskategori()).isEqualTo(Inntektskategori.JORDBRUKER);
    }

    @Test
    public void SNMedJordbrukOgOrdinærNæringSkalMappeTilInntektskategoriJordbruker() {
        // Arrange
        final var grunnlag = opprettOppgittOpptjening(List.of(VirksomhetType.ANNEN, VirksomhetType.JORDBRUK_SKOGBRUK));
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlag(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);

        // Act
        FastsettInntektskategoriTjeneste.fastsettInntektskategori(beregningsgrunnlag, grunnlag);

        // Assert
        BeregningsgrunnlagPeriodeDto periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        List<BeregningsgrunnlagPrStatusOgAndelDto> andeler = periode.getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler).hasSize(1);
        assertThat(andeler.get(0).getInntektskategori()).isEqualTo(Inntektskategori.JORDBRUKER);
    }

    @Test
    public void SNMedDagmammaOgOrdinærNæringSkalMappeTilInntektskategoriJordbruker() {
        // Arrange
        final var grunnlag = opprettOppgittOpptjening(List.of(VirksomhetType.ANNEN, VirksomhetType.DAGMAMMA));
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlag(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);

        // Act
        FastsettInntektskategoriTjeneste.fastsettInntektskategori(beregningsgrunnlag, grunnlag);

        // Assert
        BeregningsgrunnlagPeriodeDto periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        List<BeregningsgrunnlagPrStatusOgAndelDto> andeler = periode.getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler).hasSize(1);
        assertThat(andeler.get(0).getInntektskategori()).isEqualTo(Inntektskategori.DAGMAMMA);
    }

    @Test
    public void SNMedFiskeOgOrdinærNæringSkalMappeTilInntektskategoriFisker() {
        // Arrange
        final var grunnlag = opprettOppgittOpptjening(List.of(VirksomhetType.ANNEN, VirksomhetType.FISKE));
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlag(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);

        // Act
        FastsettInntektskategoriTjeneste.fastsettInntektskategori(beregningsgrunnlag, grunnlag);

        // Assert
        BeregningsgrunnlagPeriodeDto periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        List<BeregningsgrunnlagPrStatusOgAndelDto> andeler = periode.getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler).hasSize(1);
        assertThat(andeler.get(0).getInntektskategori()).isEqualTo(Inntektskategori.FISKER);
    }

    @Test
    public void skal_gi_arbeidstaker_inntektskategori_når_ingen_fisker_inntekt_hos_det_arbeidsforholdet() {
        // Arrange
        InntektDtoBuilder inntektBuilder = InntektDtoBuilder.oppdatere(Optional.empty()).medInntektsKilde(InntektskildeType.INNTEKT_BEREGNING).medArbeidsgiver(Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR));
        inntektBuilder.leggTilInntektspost(lagInntektspost(1,SkatteOgAvgiftsregelType.NETTOLØNN));
        inntektBuilder.leggTilInntektspost(lagInntektspost(2,SkatteOgAvgiftsregelType.NETTOLØNN));
        inntektBuilder.leggTilInntektspost(lagInntektspost(3,SkatteOgAvgiftsregelType.NETTOLØNN));
        inntektBuilder.leggTilInntektspost(lagInntektspost(4,SkatteOgAvgiftsregelType.NETTOLØNN));
        InntektArbeidYtelseGrunnlagDto iay = lagIAY(inntektBuilder);
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlag(AktivitetStatus.ARBEIDSTAKER);

        // Act
        FastsettInntektskategoriTjeneste.fastsettInntektskategori(beregningsgrunnlag, iay);

        // Assert
        BeregningsgrunnlagPeriodeDto periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        List<BeregningsgrunnlagPrStatusOgAndelDto> andeler = periode.getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler).hasSize(1);
        assertThat(andeler.get(0).getInntektskategori()).isEqualTo(Inntektskategori.ARBEIDSTAKER);
    }

    @Test
    public void skal_gi_arbeidstaker_inntektskategori_når_ingen_fisker_inntekt_siste_3_mnd() {
        // Arrange
        InntektDtoBuilder inntektBuilder = InntektDtoBuilder.oppdatere(Optional.empty()).medInntektsKilde(InntektskildeType.INNTEKT_BEREGNING).medArbeidsgiver(Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR));
        inntektBuilder.leggTilInntektspost(lagInntektspost(1,SkatteOgAvgiftsregelType.NETTOLØNN));
        inntektBuilder.leggTilInntektspost(lagInntektspost(2,SkatteOgAvgiftsregelType.NETTOLØNN));
        inntektBuilder.leggTilInntektspost(lagInntektspost(3,SkatteOgAvgiftsregelType.NETTOLØNN));
        inntektBuilder.leggTilInntektspost(lagInntektspost(4,SkatteOgAvgiftsregelType.SÆRSKILT_FRADRAG_FOR_SJØFOLK));
        InntektArbeidYtelseGrunnlagDto iay = lagIAY(inntektBuilder);
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlag(AktivitetStatus.ARBEIDSTAKER);

        // Act
        FastsettInntektskategoriTjeneste.fastsettInntektskategori(beregningsgrunnlag, iay);

        // Assert
        BeregningsgrunnlagPeriodeDto periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        List<BeregningsgrunnlagPrStatusOgAndelDto> andeler = periode.getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler).hasSize(1);
        assertThat(andeler.get(0).getInntektskategori()).isEqualTo(Inntektskategori.ARBEIDSTAKER);
    }

    @Test
    public void skal_gi_inntektskategori_sjømann_når_fisker_inntekt_siste_3_mnd() {
        // Arrange
        InntektDtoBuilder inntektBuilder = InntektDtoBuilder.oppdatere(Optional.empty()).medInntektsKilde(InntektskildeType.INNTEKT_BEREGNING).medArbeidsgiver(Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR));
        inntektBuilder.leggTilInntektspost(lagInntektspost(1,SkatteOgAvgiftsregelType.NETTOLØNN));
        inntektBuilder.leggTilInntektspost(lagInntektspost(2,SkatteOgAvgiftsregelType.NETTOLØNN_FOR_SJØFOLK));
        inntektBuilder.leggTilInntektspost(lagInntektspost(3,SkatteOgAvgiftsregelType.NETTOLØNN));
        inntektBuilder.leggTilInntektspost(lagInntektspost(4,SkatteOgAvgiftsregelType.NETTOLØNN));
        InntektArbeidYtelseGrunnlagDto iay = lagIAY(inntektBuilder);
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlag(AktivitetStatus.ARBEIDSTAKER);

        // Act
        FastsettInntektskategoriTjeneste.fastsettInntektskategori(beregningsgrunnlag, iay);

        // Assert
        BeregningsgrunnlagPeriodeDto periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        List<BeregningsgrunnlagPrStatusOgAndelDto> andeler = periode.getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler).hasSize(1);
        assertThat(andeler.get(0).getInntektskategori()).isEqualTo(Inntektskategori.SJØMANN);
    }

    @Test
    public void skal_gi_arbeidstaker_inntektskategori_når_fisker_inntekt_siste_3_mnd_i_annet_orgnr() {
        // Arrange
        InntektDtoBuilder inntektBuilder = InntektDtoBuilder.oppdatere(Optional.empty()).medInntektsKilde(InntektskildeType.INNTEKT_BEREGNING).medArbeidsgiver(Arbeidsgiver.virksomhet("2333"));
        inntektBuilder.leggTilInntektspost(lagInntektspost(1,SkatteOgAvgiftsregelType.NETTOLØNN_FOR_SJØFOLK));
        inntektBuilder.leggTilInntektspost(lagInntektspost(2,SkatteOgAvgiftsregelType.NETTOLØNN_FOR_SJØFOLK));
        inntektBuilder.leggTilInntektspost(lagInntektspost(3,SkatteOgAvgiftsregelType.NETTOLØNN_FOR_SJØFOLK));
        inntektBuilder.leggTilInntektspost(lagInntektspost(4,SkatteOgAvgiftsregelType.NETTOLØNN_FOR_SJØFOLK));
        InntektArbeidYtelseGrunnlagDto iay = lagIAY(inntektBuilder);
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlag(AktivitetStatus.ARBEIDSTAKER);

        // Act
        FastsettInntektskategoriTjeneste.fastsettInntektskategori(beregningsgrunnlag, iay);

        // Assert
        BeregningsgrunnlagPeriodeDto periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        List<BeregningsgrunnlagPrStatusOgAndelDto> andeler = periode.getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler).hasSize(1);
        assertThat(andeler.get(0).getInntektskategori()).isEqualTo(Inntektskategori.ARBEIDSTAKER);
    }

    @Test
    public void skalReturnereFiskerSomHøgastPrioriterteInntektskategori() {
        List<Inntektskategori> inntektskategoriList = List.of(Inntektskategori.FISKER, Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE, Inntektskategori.DAGMAMMA, Inntektskategori.JORDBRUKER);
        Optional<Inntektskategori> prioritert = FastsettInntektskategoriTjeneste.finnHøyestPrioriterteInntektskategoriForSN(inntektskategoriList);
        assertThat(prioritert.get()).isEqualTo(Inntektskategori.FISKER);
    }

    @Test
    public void skalReturnereJordbrukerSomHøgastPrioriterteInntektskategori() {
        List<Inntektskategori> inntektskategoriList = List.of(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE, Inntektskategori.DAGMAMMA, Inntektskategori.JORDBRUKER);
        Optional<Inntektskategori> prioritert = FastsettInntektskategoriTjeneste.finnHøyestPrioriterteInntektskategoriForSN(inntektskategoriList);
        assertThat(prioritert.get()).isEqualTo(Inntektskategori.JORDBRUKER);
    }

    @Test
    public void skalReturnereDagmammaSomHøgastPrioriterteInntektskategori() {
        List<Inntektskategori> inntektskategoriList = List.of(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE, Inntektskategori.DAGMAMMA);
        Optional<Inntektskategori> prioritert = FastsettInntektskategoriTjeneste.finnHøyestPrioriterteInntektskategoriForSN(inntektskategoriList);
        assertThat(prioritert.get()).isEqualTo(Inntektskategori.DAGMAMMA);
    }

    @Test
    public void skalReturnereSelvstendigNæringsdrivendeSomHøgastPrioriterteInntektskategori() {
        List<Inntektskategori> inntektskategoriList = List.of(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE);
        Optional<Inntektskategori> prioritert = FastsettInntektskategoriTjeneste.finnHøyestPrioriterteInntektskategoriForSN(inntektskategoriList);
        assertThat(prioritert.get()).isEqualTo(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE);
    }
}
