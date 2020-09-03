package no.nav.folketrygdloven.kalkulator.verdikjede;

import static no.nav.folketrygdloven.kalkulator.verdikjede.BeregningsgrunnlagGrunnlagTestUtil.nyttGrunnlag;
import static no.nav.folketrygdloven.kalkulator.verdikjede.VerdikjedeTestHjelper.SKJÆRINGSTIDSPUNKT_OPPTJENING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.kalkulator.BehandlingReferanseMock;
import no.nav.folketrygdloven.kalkulator.BeregningsgrunnlagInputTestUtil;
import no.nav.folketrygdloven.kalkulator.ForeslåBeregningsgrunnlag;
import no.nav.folketrygdloven.kalkulator.GrunnbeløpMock;
import no.nav.folketrygdloven.kalkulator.VurderBeregningsgrunnlagTjeneste;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.ForeldrepengerGrunnlagMapper;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapBeregningsgrunnlagFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapInntektsgrunnlagVLTilRegel;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapInntektsgrunnlagVLTilRegelFelles;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.YtelsesspesifikkRegelMapper;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.BehandlingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.behandling.Skjæringstidspunkt;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.VersjonTypeDto;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulator.modell.typer.AktørId;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.opptjening.OpptjeningAktiviteterDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.ArbeidType;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.Hjemmel;
import no.nav.folketrygdloven.utils.UnitTestLookupInstanceImpl;
import no.nav.vedtak.util.Tuple;

public class FlereArbeidsforholdTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT_BEREGNING = SKJÆRINGSTIDSPUNKT_OPPTJENING;
    private static final LocalDate MINUS_YEARS_1 = SKJÆRINGSTIDSPUNKT_OPPTJENING.minusYears(1);
    private static final String ARBEIDSFORHOLD_ORGNR1 = "890412882";
    private static final String ARBEIDSFORHOLD_ORGNR2 = "915933149";
    private static final String ARBEIDSFORHOLD_ORGNR3 = "923609016";
    private static final String ARBEIDSFORHOLD_ORGNR4 = "973152351";
    public static final BigDecimal GRUNNBELØP = BigDecimal.valueOf(GrunnbeløpMock.finnGrunnbeløp(SKJÆRINGSTIDSPUNKT_BEREGNING));
    private static double SEKS_G = GRUNNBELØP.multiply(BigDecimal.valueOf(6)).doubleValue();
    private BehandlingReferanse behandlingReferanse = new BehandlingReferanseMock(SKJÆRINGSTIDSPUNKT_BEREGNING);

    private String beregningVirksomhet1 = ARBEIDSFORHOLD_ORGNR1;
    private String beregningVirksomhet2 = ARBEIDSFORHOLD_ORGNR2;
    private String beregningVirksomhet3 = ARBEIDSFORHOLD_ORGNR3;
    private String beregningVirksomhet4 = ARBEIDSFORHOLD_ORGNR4;

    private VerdikjedeTestHjelper verdikjedeTestHjelper = new VerdikjedeTestHjelper();
    private BeregningTjenesteWrapper beregningTjenesteWrapper;

    private MapInntektsgrunnlagVLTilRegel mapInntektsgrunnlagVLTilRegel = new MapInntektsgrunnlagVLTilRegelFelles();
    private final UnitTestLookupInstanceImpl<YtelsesspesifikkRegelMapper> ytelsesSpesifikkMapper = new UnitTestLookupInstanceImpl<>(new ForeldrepengerGrunnlagMapper());
    private MapBeregningsgrunnlagFraVLTilRegel mapBeregningsgrunnlagFraVLTilRegel = new MapBeregningsgrunnlagFraVLTilRegel(new UnitTestLookupInstanceImpl<>(mapInntektsgrunnlagVLTilRegel), ytelsesSpesifikkMapper);
    private ForeslåBeregningsgrunnlag foreslåBeregningsgrunnlag = new ForeslåBeregningsgrunnlag(mapBeregningsgrunnlagFraVLTilRegel);
    private VurderBeregningsgrunnlagTjeneste vurderBeregningsgrunnlagTjeneste = new VurderBeregningsgrunnlagTjeneste(mapBeregningsgrunnlagFraVLTilRegel);

    @BeforeEach
    public void setup() {
        beregningTjenesteWrapper = BeregningTjenesteProvider.provide();
    }

    private Tuple<BehandlingReferanse, InntektArbeidYtelseAggregatBuilder> lagBehandlingAT(BigDecimal inntektSammenligningsgrunnlag,
                                                                                           List<String> beregningVirksomhet) {
        LocalDate fraOgMed = MINUS_YEARS_1;
        LocalDate tilOgMed = fraOgMed.plusYears(4);

        AktørId aktørId = behandlingReferanse.getAktørId();
        InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        beregningVirksomhet
            .forEach(virksomhetOrgnr -> {
                verdikjedeTestHjelper.lagAktørArbeid(inntektArbeidYtelseBuilder, aktørId, Arbeidsgiver.virksomhet(virksomhetOrgnr),
                    fraOgMed, tilOgMed, ArbeidType.ORDINÆRT_ARBEIDSFORHOLD);
            });

        for (LocalDate dt = fraOgMed; dt.isBefore(tilOgMed); dt = dt.plusMonths(1)) {
            verdikjedeTestHjelper.lagInntektForSammenligning(inntektArbeidYtelseBuilder, aktørId, dt, dt.plusMonths(1), inntektSammenligningsgrunnlag,
                Arbeidsgiver.virksomhet(beregningVirksomhet.get(0)));
        }
        return new Tuple<>(behandlingReferanse, inntektArbeidYtelseBuilder);
    }

    @Test
    public void ettArbeidsforholdMedAvrundetDagsats() {
        String orgnr1 = ARBEIDSFORHOLD_ORGNR1;

        final double DAGSATS = 1959.76;
        final List<Double> ÅRSINNTEKT = List.of(DAGSATS * 260);
        final Double bg = ÅRSINNTEKT.get(0);

        final double forventetAvkortet = ÅRSINNTEKT.get(0);
        final double forventetRedusert = forventetAvkortet;

        final long forventetDagsats = 1960;
        List<BigDecimal> månedsinntekter = ÅRSINNTEKT.stream().map((v) -> BigDecimal.valueOf(v / 12)).collect(Collectors.toList());
        List<String> virksomhetene = List.of(orgnr1);

        // Arrange 1
        Tuple<BehandlingReferanse, InntektArbeidYtelseAggregatBuilder> tuple = lagBehandlingAT(
            BigDecimal.valueOf(ÅRSINNTEKT.get(0) / 12),
            virksomhetene);
        BehandlingReferanse ref = lagReferanse(tuple.getElement1());

        var im1 = verdikjedeTestHjelper.opprettInntektsmeldingMedRefusjonskrav(Arbeidsgiver.virksomhet(beregningVirksomhet1),
            månedsinntekter.get(0), månedsinntekter.get(0));
        List<InntektsmeldingDto> inntektsmeldinger = List.of(im1);

        var opptjeningAktiviteter = OpptjeningAktiviteterDto.fraOrgnr(OpptjeningAktivitetType.ARBEID,
            Periode.of(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusYears(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.minusDays(1)), orgnr1);

        InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlagDto = verdikjedeTestHjelper.opprettIAYforOrg(orgnr1, SKJÆRINGSTIDSPUNKT_OPPTJENING);
        InntektArbeidYtelseGrunnlagDto iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.oppdatere(inntektArbeidYtelseGrunnlagDto)
            .medData(tuple.getElement2())
            .medInntektsmeldinger(inntektsmeldinger).build();

        var input = lagInput(ref, opptjeningAktiviteter, iayGrunnlag, 100);

        // Act 1: kontroller fakta for beregning
        BeregningsgrunnlagGrunnlagDto grunnlag = verdikjedeTestHjelper.kjørStegOgLagreGrunnlag(input, beregningTjenesteWrapper);
        input = input.medBeregningsgrunnlagGrunnlag(grunnlag);

        var aksjonspunktResultat = beregningTjenesteWrapper.getAksjonspunktUtlederFaktaOmBeregning().utledAksjonspunkterFor(input,
            grunnlag, false);
        assertThat(aksjonspunktResultat.getBeregningAksjonspunktResultatList()).isEmpty();

        // Act 2: foreslå beregningsgrunnlag
        BeregningsgrunnlagRegelResultat resultat = foreslåBeregningsgrunnlag.foreslåBeregningsgrunnlag(input);

        // Assert 1
        verdikjedeTestHjelper.verifiserBeregningsgrunnlagBasis(resultat, Hjemmel.F_14_7_8_30);

        BeregningsgrunnlagDto foreslåttBeregningsgrunnlag = resultat.getBeregningsgrunnlag();
        BeregningsgrunnlagPeriodeDto periode = foreslåttBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        verdikjedeTestHjelper.verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, Intervall.TIDENES_ENDE, 1);
        verdikjedeTestHjelper.verifiserBGATførAvkorting(periode, ÅRSINNTEKT, virksomhetene);
        verdikjedeTestHjelper.verifiserSammenligningsgrunnlag(foreslåttBeregningsgrunnlag.getSammenligningsgrunnlag(),
            bg, SKJÆRINGSTIDSPUNKT_BEREGNING.minusYears(1).withDayOfMonth(1),
            SKJÆRINGSTIDSPUNKT_BEREGNING.withDayOfMonth(1).minusDays(1), BigDecimal.ZERO);

        // Act 3: fordel beregningsgrunnlag
        resultat = vurderBeregningsgrunnlagTjeneste.vurderBeregningsgrunnlag(input,
            nyttGrunnlag(grunnlag, foreslåttBeregningsgrunnlag, BeregningsgrunnlagTilstand.FORESLÅTT));
        assertThat(resultat.getVilkårOppfylt()).isTrue();
        BeregningsgrunnlagDto fordeltBeregningsgrunnlag = fordelBeregningsgrunnlag(input, grunnlag, resultat);
        input = input.medBeregningsgrunnlagGrunnlag(BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(grunnlag).medBeregningsgrunnlag(fordeltBeregningsgrunnlag).build(BeregningsgrunnlagTilstand.OPPDATERT_MED_REFUSJON_OG_GRADERING));

        // Act 4: fastsette beregningsgrunnlag
        var fastsattBeregningsgrunnlag = beregningTjenesteWrapper.getFullføreBeregningsgrunnlagTjeneste().fullføreBeregningsgrunnlag(input
        );

        // Assert 3
        verdikjedeTestHjelper.verifiserBeregningsgrunnlagBasis(fastsattBeregningsgrunnlag, Hjemmel.F_14_7_8_30);

        periode = fastsattBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        verdikjedeTestHjelper.verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, Intervall.TIDENES_ENDE, 1, forventetDagsats);
        verdikjedeTestHjelper.verifiserBGATetterAvkorting(periode,
            ÅRSINNTEKT, ÅRSINNTEKT, virksomhetene, List.of(forventetRedusert), ÅRSINNTEKT, List.of(forventetRedusert), List.of(0.0d), false);
    }

    @Test
    public void ettArbeidsforholdMedOverstyringUnder6G() {

        String orgnr1 = ARBEIDSFORHOLD_ORGNR1;
        final List<Double> ÅRSINNTEKT = List.of(180000d);
        final Double bg = ÅRSINNTEKT.get(0);
        final Double overstyrt = 200000d;

        final double forventetAvkortet1 = ÅRSINNTEKT.get(0);
        final double forventetRedusert1 = forventetAvkortet1;

        final long forventetDagsats = Math.round(overstyrt / 260);

        List<BigDecimal> månedsinntekter = ÅRSINNTEKT.stream().map((v) -> BigDecimal.valueOf(v / 12)).collect(Collectors.toList());
        List<String> virksomhetene = List.of(orgnr1);

        // Arrange 1
        Tuple<BehandlingReferanse, InntektArbeidYtelseAggregatBuilder> tuple = lagBehandlingAT(
            BigDecimal.valueOf(ÅRSINNTEKT.get(0) / 12 / 2),
            virksomhetene);
        BehandlingReferanse ref = lagReferanse(tuple.getElement1());

        var im1 = verdikjedeTestHjelper.opprettInntektsmeldingMedRefusjonskrav(Arbeidsgiver.virksomhet(beregningVirksomhet1),
            månedsinntekter.get(0), månedsinntekter.get(0));
        List<InntektsmeldingDto> inntektsmeldinger = List.of(im1);

        var opptjeningAktiviteter = OpptjeningAktiviteterDto.fraOrgnr(OpptjeningAktivitetType.ARBEID,
            Periode.of(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusYears(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.minusDays(1)), orgnr1);

        var iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.oppdatere(Optional.empty())
            .medData(tuple.getElement2())
            .medInntektsmeldinger(inntektsmeldinger).build();
        var input = lagInput(ref, opptjeningAktiviteter, iayGrunnlag, 100);

        // Act 1: kontroller fakta for beregning
        BeregningsgrunnlagGrunnlagDto grunnlag = verdikjedeTestHjelper.kjørStegOgLagreGrunnlag(input, beregningTjenesteWrapper);
        input = input.medBeregningsgrunnlagGrunnlag(grunnlag);
        var aksjonspunktResultat = beregningTjenesteWrapper.getAksjonspunktUtlederFaktaOmBeregning().utledAksjonspunkterFor(input,
            grunnlag, false);

        // Assert 1
        assertThat(aksjonspunktResultat.getBeregningAksjonspunktResultatList()).isEmpty();

        // Act 2: foreslå beregningsgrunnlag
        BeregningsgrunnlagRegelResultat resultat = foreslåBeregningsgrunnlag.foreslåBeregningsgrunnlag(input);

        // Assert 2
        verifiserBeregningsgrunnlagMedAksjonspunkt(resultat);

        BeregningsgrunnlagDto foreslåttBeregningsgrunnlag = resultat.getBeregningsgrunnlag();
        BeregningsgrunnlagPeriodeDto periode = foreslåttBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        verdikjedeTestHjelper.verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, Intervall.TIDENES_ENDE, 1);
        verdikjedeTestHjelper.verifiserBGATførAvkorting(periode, ÅRSINNTEKT, virksomhetene);
        verdikjedeTestHjelper.verifiserSammenligningsgrunnlag(foreslåttBeregningsgrunnlag.getSammenligningsgrunnlag(),
            bg / 2, SKJÆRINGSTIDSPUNKT_BEREGNING.minusYears(1).withDayOfMonth(1),
            SKJÆRINGSTIDSPUNKT_BEREGNING.withDayOfMonth(1).minusDays(1), BigDecimal.valueOf(1000));

        // Arrange 2: Overstyring
        periode = foreslåttBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0))
            .medOverstyrtPrÅr(BigDecimal.valueOf(overstyrt))
            .build(periode);

        // Act 3: fordel beregningsgrunnlag
        resultat = vurderBeregningsgrunnlagTjeneste.vurderBeregningsgrunnlag(input,
            nyttGrunnlag(grunnlag, foreslåttBeregningsgrunnlag, BeregningsgrunnlagTilstand.FORESLÅTT));
        assertThat(resultat.getVilkårOppfylt()).isTrue();
        BeregningsgrunnlagDto fordeltBeregningsgrunnlag = fordelBeregningsgrunnlag(input, grunnlag, resultat);
        input = input.medBeregningsgrunnlagGrunnlag(BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(grunnlag).medBeregningsgrunnlag(fordeltBeregningsgrunnlag).build(BeregningsgrunnlagTilstand.OPPDATERT_MED_REFUSJON_OG_GRADERING));

        // Act 4: fastsette beregningsgrunnlag
        var fastsattBeregningsgrunnlag = beregningTjenesteWrapper.getFullføreBeregningsgrunnlagTjeneste().fullføreBeregningsgrunnlag(input
        );

        // Assert 3
        verdikjedeTestHjelper.verifiserBeregningsgrunnlagBasis(fastsattBeregningsgrunnlag, Hjemmel.F_14_7_8_30);

        periode = fastsattBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        verdikjedeTestHjelper.verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, Intervall.TIDENES_ENDE, 1, forventetDagsats);
        verifiserBGATetterOverstyring(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0),
            bg, beregningVirksomhet1, overstyrt, overstyrt, overstyrt, bg, forventetAvkortet1, overstyrt - forventetAvkortet1, forventetRedusert1,
            overstyrt - forventetRedusert1);
    }

    private BeregningsgrunnlagInput lagInput(BehandlingReferanse ref, OpptjeningAktiviteterDto opptjeningAktiviteter, InntektArbeidYtelseGrunnlagDto iayGrunnlag, int dekningsgrad) {
        return BeregningsgrunnlagInputTestUtil.lagInputMedIAYOgOpptjeningsaktiviteter(ref, opptjeningAktiviteter, iayGrunnlag, dekningsgrad, 2);
    }

    @Test
    public void ettArbeidsforholdMedOverstyringOver6G() {
        String orgnr1 = ARBEIDSFORHOLD_ORGNR1;

        final List<Double> ÅRSINNTEKT = List.of(480000d);
        final Double bg = ÅRSINNTEKT.get(0);
        final Double overstyrt = 700000d;

        final double forventetAvkortet1 = ÅRSINNTEKT.get(0);
        final double forventetRedusert1 = forventetAvkortet1;

        List<BigDecimal> månedsinntekter = ÅRSINNTEKT.stream().map((v) -> BigDecimal.valueOf(v / 12)).collect(Collectors.toList());
        List<String> virksomhetene = List.of(orgnr1);

        final long forventetDagsats = Math.round(SEKS_G / 260);

        // Arrange 1
        Tuple<BehandlingReferanse, InntektArbeidYtelseAggregatBuilder> tuple = lagBehandlingAT(
            BigDecimal.valueOf(ÅRSINNTEKT.get(0) / 12 / 2),
            virksomhetene);

        var im1 = verdikjedeTestHjelper.opprettInntektsmeldingMedRefusjonskrav(Arbeidsgiver.virksomhet(beregningVirksomhet1),
            månedsinntekter.get(0), månedsinntekter.get(0));
        List<InntektsmeldingDto> inntektsmeldinger = List.of(im1);

        var opptjeningAktiviteter = OpptjeningAktiviteterDto.fraOrgnr(OpptjeningAktivitetType.ARBEID,
            Periode.of(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusYears(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.minusDays(1)), orgnr1);

        var iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.oppdatere(Optional.empty())
            .medData(tuple.getElement2())
            .medInntektsmeldinger(inntektsmeldinger).build();
        BeregningsgrunnlagInput input = lagInput(behandlingReferanse, opptjeningAktiviteter, iayGrunnlag, 100);

        // Act 1: kontroller fakta for beregning
        BeregningsgrunnlagGrunnlagDto grunnlag = verdikjedeTestHjelper.kjørStegOgLagreGrunnlag(input, beregningTjenesteWrapper);
        input = input.medBeregningsgrunnlagGrunnlag(grunnlag);
        var aksjonspunktResultat = beregningTjenesteWrapper.getAksjonspunktUtlederFaktaOmBeregning().utledAksjonspunkterFor(input,
            grunnlag,
            false);

        // Assert 1
        assertThat(aksjonspunktResultat.getBeregningAksjonspunktResultatList()).isEmpty();

        // Act 2: foreslå beregningsgrunnlag
        BeregningsgrunnlagRegelResultat resultat = foreslåBeregningsgrunnlag.foreslåBeregningsgrunnlag(input);

        // Assert 2
        verifiserBeregningsgrunnlagMedAksjonspunkt(resultat);

        BeregningsgrunnlagDto foreslåttBeregningsgrunnlag = resultat.getBeregningsgrunnlag();
        BeregningsgrunnlagPeriodeDto periode = foreslåttBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        verdikjedeTestHjelper.verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, Intervall.TIDENES_ENDE, 1);
        verdikjedeTestHjelper.verifiserBGATførAvkorting(periode, ÅRSINNTEKT, virksomhetene);
        verdikjedeTestHjelper.verifiserSammenligningsgrunnlag(foreslåttBeregningsgrunnlag.getSammenligningsgrunnlag(),
            bg / 2, SKJÆRINGSTIDSPUNKT_BEREGNING.minusYears(1).withDayOfMonth(1),
            SKJÆRINGSTIDSPUNKT_BEREGNING.withDayOfMonth(1).minusDays(1), BigDecimal.valueOf(1000));

        // Arrange 2: Overstyring
        periode = foreslåttBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0))
            .medOverstyrtPrÅr(BigDecimal.valueOf(overstyrt))
            .build(periode);

        // Act 3: fordel beregningsgrunnlag
        resultat = vurderBeregningsgrunnlagTjeneste.vurderBeregningsgrunnlag(input,
            nyttGrunnlag(grunnlag, foreslåttBeregningsgrunnlag, BeregningsgrunnlagTilstand.FORESLÅTT));
        assertThat(resultat.getVilkårOppfylt()).isTrue();
        BeregningsgrunnlagDto fordeltBeregningsgrunnlag = fordelBeregningsgrunnlag(input, grunnlag, resultat);
        input = input.medBeregningsgrunnlagGrunnlag(BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(grunnlag).medBeregningsgrunnlag(fordeltBeregningsgrunnlag).build(BeregningsgrunnlagTilstand.OPPDATERT_MED_REFUSJON_OG_GRADERING));

        // Act 4: fastsette beregningsgrunnlag
        var fastsattBeregningsgrunnlag = beregningTjenesteWrapper.getFullføreBeregningsgrunnlagTjeneste().fullføreBeregningsgrunnlag(input
        );

        // Assert 3
        verdikjedeTestHjelper.verifiserBeregningsgrunnlagBasis(fastsattBeregningsgrunnlag, Hjemmel.F_14_7_8_30);

        periode = fastsattBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        verdikjedeTestHjelper.verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, Intervall.TIDENES_ENDE, 1, forventetDagsats);
        verifiserBGATetterOverstyring(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0),
            bg, beregningVirksomhet1, overstyrt, SEKS_G, SEKS_G, bg, forventetAvkortet1, SEKS_G - forventetAvkortet1, forventetRedusert1,
            SEKS_G - forventetRedusert1);
    }

    @Test
    public void ettArbeidsforholdMedOverstyringOver6GOgReduksjon() {
        String orgnr1 = ARBEIDSFORHOLD_ORGNR1;

        final List<Double> ÅRSINNTEKT = List.of(480000d);
        final Double bg = ÅRSINNTEKT.get(0);
        final double overstyrt = 700000d;

        final double forventetAvkortet = SEKS_G;
        final double forventetRedusert = forventetAvkortet * 0.8;
        final double forventetAvkortet1 = ÅRSINNTEKT.get(0);
        final double forventetRedusert1 = forventetAvkortet1 * 0.8;

        final long forventetDagsats = Math.round(forventetRedusert / 260);
        List<BigDecimal> månedsinntekter = ÅRSINNTEKT.stream().map((v) -> BigDecimal.valueOf(v / 12)).collect(Collectors.toList());
        List<String> virksomhetene = List.of(orgnr1);

        // Arrange 1
        Tuple<BehandlingReferanse, InntektArbeidYtelseAggregatBuilder> tuple = lagBehandlingAT(
            BigDecimal.valueOf(ÅRSINNTEKT.get(0) / 12 / 2),
            virksomhetene);
        BehandlingReferanse ref = lagReferanse(tuple.getElement1());

        var im1 = verdikjedeTestHjelper.opprettInntektsmeldingMedRefusjonskrav(Arbeidsgiver.virksomhet(beregningVirksomhet1),
            månedsinntekter.get(0), månedsinntekter.get(0));
        List<InntektsmeldingDto> inntektsmeldinger = List.of(im1);

        var opptjeningAktiviteter = OpptjeningAktiviteterDto.fraOrgnr(OpptjeningAktivitetType.ARBEID,
            Periode.of(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusYears(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.minusDays(1)), orgnr1);

        var iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.oppdatere(Optional.empty())
            .medData(tuple.getElement2())
            .medInntektsmeldinger(inntektsmeldinger).build();
        var input = lagInput(ref, opptjeningAktiviteter, iayGrunnlag, 80);

        // Act 1: kontroller fakta for beregning
        var grunnlag = verdikjedeTestHjelper.kjørStegOgLagreGrunnlag(input, beregningTjenesteWrapper);
        input = input.medBeregningsgrunnlagGrunnlag(grunnlag);
        var aksjonspunktResultat = beregningTjenesteWrapper.getAksjonspunktUtlederFaktaOmBeregning().utledAksjonspunkterFor(input, grunnlag, false);

        // Assert 1
        assertThat(aksjonspunktResultat.getBeregningAksjonspunktResultatList()).isEmpty();

        // Act 2: foreslå beregningsgrunnlag
        BeregningsgrunnlagRegelResultat resultat = foreslåBeregningsgrunnlag.foreslåBeregningsgrunnlag(input);

        // Assert 2
        verifiserBeregningsgrunnlagMedAksjonspunkt(resultat);

        BeregningsgrunnlagDto foreslåttBeregningsgrunnlag = resultat.getBeregningsgrunnlag();
        BeregningsgrunnlagPeriodeDto periode = foreslåttBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        verdikjedeTestHjelper.verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, Intervall.TIDENES_ENDE, 1);
        verdikjedeTestHjelper.verifiserBGATførAvkorting(periode, ÅRSINNTEKT, virksomhetene);
        verdikjedeTestHjelper.verifiserSammenligningsgrunnlag(foreslåttBeregningsgrunnlag.getSammenligningsgrunnlag(),
            bg / 2, SKJÆRINGSTIDSPUNKT_BEREGNING.minusYears(1).withDayOfMonth(1),
            SKJÆRINGSTIDSPUNKT_BEREGNING.withDayOfMonth(1).minusDays(1), BigDecimal.valueOf(1000));

        // Arrange 2: Overstyring
        periode = foreslåttBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0))
            .medOverstyrtPrÅr(BigDecimal.valueOf(overstyrt))
            .build(periode);

        // Act 3: fordel beregningsgrunnlag
        resultat = vurderBeregningsgrunnlagTjeneste.vurderBeregningsgrunnlag(input,
                nyttGrunnlag(grunnlag, foreslåttBeregningsgrunnlag, BeregningsgrunnlagTilstand.FORESLÅTT));
        assertThat(resultat.getVilkårOppfylt()).isTrue();
        BeregningsgrunnlagDto fordeltBeregningsgrunnlag = fordelBeregningsgrunnlag(input, grunnlag, resultat);
        input = input.medBeregningsgrunnlagGrunnlag(BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(grunnlag).medBeregningsgrunnlag(fordeltBeregningsgrunnlag).build(BeregningsgrunnlagTilstand.OPPDATERT_MED_REFUSJON_OG_GRADERING));

        // Act 4: fastsette beregningsgrunnlag
        var fastsattBeregningsgrunnlag = beregningTjenesteWrapper.getFullføreBeregningsgrunnlagTjeneste().fullføreBeregningsgrunnlag(input
        );

        // Assert 3
        verdikjedeTestHjelper.verifiserBeregningsgrunnlagBasis(fastsattBeregningsgrunnlag, Hjemmel.F_14_7_8_30);

        periode = fastsattBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        verdikjedeTestHjelper.verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, Intervall.TIDENES_ENDE, 1, forventetDagsats);
        verifiserBGATetterOverstyring(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0),
            bg, beregningVirksomhet1, overstyrt, forventetAvkortet, forventetRedusert, bg,
            forventetAvkortet1, SEKS_G - forventetAvkortet1,
            forventetRedusert1, SEKS_G * 0.8 - forventetRedusert1);
    }

    @Test
    public void toArbeidsforholdMedBgUnder6gOgFullRefusjon() {
        String orgnr1 = ARBEIDSFORHOLD_ORGNR1;
        String orgnr2 = ARBEIDSFORHOLD_ORGNR2;

        final List<Double> ÅRSINNTEKT = List.of(180000d, 72000d);
        final Double totalÅrsinntekt = ÅRSINNTEKT.stream().reduce((v1, v2) -> v1 + v2).orElse(null);

        final double forventetRedusert1 = ÅRSINNTEKT.get(0);
        final double forventetRedusert2 = ÅRSINNTEKT.get(1);

        final List<Double> forventetRedusert = List.of(forventetRedusert1, forventetRedusert2);
        final List<Double> forventetRedusertBrukersAndel = List.of(0d, 0d);

        final long forventetDagsats = Math.round(forventetRedusert1 / 260) + Math.round(forventetRedusert2 / 260);

        List<BigDecimal> månedsinntekter = ÅRSINNTEKT.stream().map((v) -> BigDecimal.valueOf(v / 12)).collect(Collectors.toList());
        List<String> virksomhetene = List.of(orgnr1, orgnr2);

        // Arrange 1
        Tuple<BehandlingReferanse, InntektArbeidYtelseAggregatBuilder> tuple = lagBehandlingAT(
            BigDecimal.valueOf(totalÅrsinntekt / 12),
            virksomhetene);
        BehandlingReferanse ref = lagReferanse(tuple.getElement1());

        var im1 = verdikjedeTestHjelper.opprettInntektsmeldingMedRefusjonskrav(Arbeidsgiver.virksomhet(beregningVirksomhet1),
            månedsinntekter.get(0), månedsinntekter.get(0));
        var im2 = verdikjedeTestHjelper.opprettInntektsmeldingMedRefusjonskrav(Arbeidsgiver.virksomhet(beregningVirksomhet2),
            månedsinntekter.get(1), månedsinntekter.get(1));
        List<InntektsmeldingDto> inntektsmeldinger = List.of(im1, im2);

        Periode opptjeningPeriode = Periode.of(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusYears(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.minusDays(1));
        var opptjeningAktiviteter = new OpptjeningAktiviteterDto(
            OpptjeningAktiviteterDto.nyPeriodeOrgnr(OpptjeningAktivitetType.ARBEID, opptjeningPeriode, orgnr1),
            OpptjeningAktiviteterDto.nyPeriodeOrgnr(OpptjeningAktivitetType.ARBEID, opptjeningPeriode, orgnr2));

        var iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.oppdatere(Optional.empty())
            .medData(tuple.getElement2())
            .medInntektsmeldinger(inntektsmeldinger).build();
        var input = lagInput(ref, opptjeningAktiviteter, iayGrunnlag, 100);

        // Act 1: kontroller fakta for beregning
        BeregningsgrunnlagGrunnlagDto grunnlag = verdikjedeTestHjelper.kjørStegOgLagreGrunnlag(input, beregningTjenesteWrapper);
        input = input.medBeregningsgrunnlagGrunnlag(grunnlag);
        var aksjonspunktResultat = beregningTjenesteWrapper.getAksjonspunktUtlederFaktaOmBeregning().utledAksjonspunkterFor(input, grunnlag, false);

        // Assert 1
        assertThat(aksjonspunktResultat.getBeregningAksjonspunktResultatList()).isEmpty();

        // Act 2: foreslå beregningsgrunnlag
        BeregningsgrunnlagRegelResultat resultat = foreslåBeregningsgrunnlag.foreslåBeregningsgrunnlag(input);

        // Assert 2
        verdikjedeTestHjelper.verifiserBeregningsgrunnlagBasis(resultat, Hjemmel.F_14_7_8_30);

        BeregningsgrunnlagDto foreslåttBeregningsgrunnlag = resultat.getBeregningsgrunnlag();
        BeregningsgrunnlagPeriodeDto periode = foreslåttBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        verdikjedeTestHjelper.verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, Intervall.TIDENES_ENDE, 2);
        verdikjedeTestHjelper.verifiserBGATførAvkorting(periode, ÅRSINNTEKT, virksomhetene);
        verdikjedeTestHjelper.verifiserSammenligningsgrunnlag(foreslåttBeregningsgrunnlag.getSammenligningsgrunnlag(),
            totalÅrsinntekt, SKJÆRINGSTIDSPUNKT_BEREGNING.minusYears(1).withDayOfMonth(1),
            SKJÆRINGSTIDSPUNKT_BEREGNING.withDayOfMonth(1).minusDays(1), BigDecimal.ZERO);

        // Act 3: fordel beregningsgrunnlag
        resultat = vurderBeregningsgrunnlagTjeneste.vurderBeregningsgrunnlag(input,
            nyttGrunnlag(grunnlag, foreslåttBeregningsgrunnlag, BeregningsgrunnlagTilstand.FORESLÅTT));
        assertThat(resultat.getVilkårOppfylt()).isTrue();
        BeregningsgrunnlagDto fordeltBeregningsgrunnlag = fordelBeregningsgrunnlag(input, grunnlag, resultat);
        input = input.medBeregningsgrunnlagGrunnlag(BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(grunnlag).medBeregningsgrunnlag(fordeltBeregningsgrunnlag).build(BeregningsgrunnlagTilstand.OPPDATERT_MED_REFUSJON_OG_GRADERING));

        // Act 4: fastsette beregningsgrunnlag
        var fastsattBeregningsgrunnlag = beregningTjenesteWrapper.getFullføreBeregningsgrunnlagTjeneste().fullføreBeregningsgrunnlag(input
        );

        // Assert 3
        verdikjedeTestHjelper.verifiserBeregningsgrunnlagBasis(fastsattBeregningsgrunnlag, Hjemmel.F_14_7_8_30);

        periode = fastsattBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        verdikjedeTestHjelper.verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, Intervall.TIDENES_ENDE, 2, forventetDagsats);
        verdikjedeTestHjelper.verifiserBGATetterAvkorting(periode,
            ÅRSINNTEKT, ÅRSINNTEKT, virksomhetene, forventetRedusert, ÅRSINNTEKT, forventetRedusert, forventetRedusertBrukersAndel, false);
    }

    @Test
    public void toArbeidsforholdMedBgOver6gOgFullRefusjon() {
        String orgnr1 = ARBEIDSFORHOLD_ORGNR1;
        String orgnr2 = ARBEIDSFORHOLD_ORGNR2;

        final List<Double> ÅRSINNTEKT = List.of(448000d, 336000d);
        final Double totalÅrsinntekt = ÅRSINNTEKT.stream().reduce((v1, v2) -> v1 + v2).orElse(null);

        final double forventetRedusert1 = SEKS_G * ÅRSINNTEKT.get(0) / (ÅRSINNTEKT.get(0) + ÅRSINNTEKT.get(1));
        final double forventetRedusert2 = SEKS_G * ÅRSINNTEKT.get(1) / (ÅRSINNTEKT.get(0) + ÅRSINNTEKT.get(1));

        final List<Double> forventetRedusert = List.of(forventetRedusert1, forventetRedusert2);
        final List<Double> forventetRedusertBrukersAndel = List.of(0d, 0d);

        List<BigDecimal> månedsinntekter = ÅRSINNTEKT.stream().map((v) -> BigDecimal.valueOf(v / 12)).collect(Collectors.toList());
        List<String> virksomhetene = List.of(orgnr1, orgnr2);

        final long forventetDagsats = forventetRedusert.stream().mapToLong(dv -> Math.round(dv / 260)).sum() +
            forventetRedusertBrukersAndel.stream().mapToLong(dv -> Math.round(dv / 260)).sum();

        // Arrange 1
        Tuple<BehandlingReferanse, InntektArbeidYtelseAggregatBuilder> tuple = lagBehandlingAT(
            BigDecimal.valueOf(totalÅrsinntekt / 12),
            virksomhetene);
        BehandlingReferanse ref = lagReferanse(tuple.getElement1());

        var im1 = verdikjedeTestHjelper.opprettInntektsmeldingMedRefusjonskrav(Arbeidsgiver.virksomhet(beregningVirksomhet1),
            månedsinntekter.get(0), månedsinntekter.get(0));
        var im2 = verdikjedeTestHjelper.opprettInntektsmeldingMedRefusjonskrav(Arbeidsgiver.virksomhet(beregningVirksomhet2),
            månedsinntekter.get(1), månedsinntekter.get(1));
        List<InntektsmeldingDto> inntektsmeldinger = List.of(im1, im2);

        Periode opptjeningPeriode = Periode.of(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusYears(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.minusDays(1));
        var opptjeningAktiviteter = new OpptjeningAktiviteterDto(
            OpptjeningAktiviteterDto.nyPeriodeOrgnr(OpptjeningAktivitetType.ARBEID, opptjeningPeriode, orgnr1),
            OpptjeningAktiviteterDto.nyPeriodeOrgnr(OpptjeningAktivitetType.ARBEID, opptjeningPeriode, orgnr2));

        var iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.oppdatere(Optional.empty())
            .medData(tuple.getElement2())
            .medInntektsmeldinger(inntektsmeldinger).build();
        var input = lagInput(ref, opptjeningAktiviteter, iayGrunnlag, 100);

        // Act 1: kontroller fakta for beregning
        BeregningsgrunnlagGrunnlagDto grunnlag = verdikjedeTestHjelper.kjørStegOgLagreGrunnlag(input, beregningTjenesteWrapper);
        input = input.medBeregningsgrunnlagGrunnlag(grunnlag);
        var aksjonspunktResultat = beregningTjenesteWrapper.getAksjonspunktUtlederFaktaOmBeregning().utledAksjonspunkterFor(input, grunnlag, false);

        // Assert 1
        assertThat(aksjonspunktResultat.getBeregningAksjonspunktResultatList()).isEmpty();

        // Act 2: foreslå beregningsgrunnlag
        BeregningsgrunnlagRegelResultat resultat = foreslåBeregningsgrunnlag.foreslåBeregningsgrunnlag(input);

        // Assert 2
        verdikjedeTestHjelper.verifiserBeregningsgrunnlagBasis(resultat, Hjemmel.F_14_7_8_30);

        BeregningsgrunnlagDto foreslåttBeregningsgrunnlag = resultat.getBeregningsgrunnlag();
        BeregningsgrunnlagPeriodeDto periode = foreslåttBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        verdikjedeTestHjelper.verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, Intervall.TIDENES_ENDE, 2);
        verdikjedeTestHjelper.verifiserBGATførAvkorting(periode, ÅRSINNTEKT, virksomhetene);
        verdikjedeTestHjelper.verifiserSammenligningsgrunnlag(foreslåttBeregningsgrunnlag.getSammenligningsgrunnlag(),
            totalÅrsinntekt, SKJÆRINGSTIDSPUNKT_BEREGNING.minusYears(1).withDayOfMonth(1),
            SKJÆRINGSTIDSPUNKT_BEREGNING.withDayOfMonth(1).minusDays(1), BigDecimal.ZERO);

        // Act 3: fordel beregningsgrunnlag
        resultat = vurderBeregningsgrunnlagTjeneste.vurderBeregningsgrunnlag(input,
            nyttGrunnlag(grunnlag, foreslåttBeregningsgrunnlag, BeregningsgrunnlagTilstand.FORESLÅTT));
        assertThat(resultat.getVilkårOppfylt()).isTrue();
        BeregningsgrunnlagDto fordeltBeregningsgrunnlag = fordelBeregningsgrunnlag(input, grunnlag, resultat);
        input = input.medBeregningsgrunnlagGrunnlag(BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(grunnlag).medBeregningsgrunnlag(fordeltBeregningsgrunnlag).build(BeregningsgrunnlagTilstand.OPPDATERT_MED_REFUSJON_OG_GRADERING));

        // Act 4: fastsette beregningsgrunnlag
        var fastsattBeregningsgrunnlag = beregningTjenesteWrapper.getFullføreBeregningsgrunnlagTjeneste().fullføreBeregningsgrunnlag(input
        );

        // Assert 3
        verdikjedeTestHjelper.verifiserBeregningsgrunnlagBasis(fastsattBeregningsgrunnlag, Hjemmel.F_14_7_8_30);

        periode = fastsattBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        verdikjedeTestHjelper.verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, Intervall.TIDENES_ENDE, 2, forventetDagsats);
        verdikjedeTestHjelper.verifiserBGATetterAvkorting(periode,
            ÅRSINNTEKT, ÅRSINNTEKT, virksomhetene, forventetRedusert, ÅRSINNTEKT, forventetRedusert, forventetRedusertBrukersAndel, false);
    }

    @Test
    public void fireArbeidsforholdMedBgOver6gOgDelvisRefusjonUnder6G() {
        String orgnr1 = ARBEIDSFORHOLD_ORGNR1;
        String orgnr2 = ARBEIDSFORHOLD_ORGNR2;
        String orgnr3 = ARBEIDSFORHOLD_ORGNR3;
        String orgnr4 = ARBEIDSFORHOLD_ORGNR4;

        final List<Double> ÅRSINNTEKT = List.of(400000d, 500000d, 300000d, 100000d);
        final List<Double> refusjonsKrav = List.of(200000d, 150000d, 300000d, 100000d);
        final Double totalÅrsinntekt = ÅRSINNTEKT.stream().reduce((v1, v2) -> v1 + v2).orElse(null);

        double fordelingRunde2 = SEKS_G - (refusjonsKrav.get(0) + refusjonsKrav.get(1));
        double forventetRedusert1 = refusjonsKrav.get(0);
        double forventetRedusert2 = refusjonsKrav.get(1);
        double forventetRedusert3 = fordelingRunde2 * ÅRSINNTEKT.get(2) / (ÅRSINNTEKT.get(2) + ÅRSINNTEKT.get(3));
        double forventetRedusert4 = fordelingRunde2 * ÅRSINNTEKT.get(3) / (ÅRSINNTEKT.get(2) + ÅRSINNTEKT.get(3));

        final List<Double> forventetRedusert = List.of(forventetRedusert1, forventetRedusert2, forventetRedusert3, forventetRedusert4);
        final List<Double> forventetRedusertBrukersAndel = List.of(0d, 0d, 0d, 0d);

        List<BigDecimal> månedsinntekter = ÅRSINNTEKT.stream().map((v) -> BigDecimal.valueOf(v / 12)).collect(Collectors.toList());
        List<String> virksomhetene = List.of(orgnr1, orgnr2, orgnr3, orgnr4);

        final long forventetDagsats = forventetRedusert.stream().mapToLong(dv -> Math.round(dv / 260)).sum() +
            forventetRedusertBrukersAndel.stream().mapToLong(dv -> Math.round(dv / 260)).sum();

        // Arrange 1
        Tuple<BehandlingReferanse, InntektArbeidYtelseAggregatBuilder> tuple = lagBehandlingAT(
            BigDecimal.valueOf(totalÅrsinntekt / 12),
            virksomhetene);
        BehandlingReferanse ref = lagReferanse(tuple.getElement1());

        var im1 = verdikjedeTestHjelper.opprettInntektsmeldingMedRefusjonskrav(Arbeidsgiver.virksomhet(beregningVirksomhet1),
            månedsinntekter.get(0), BigDecimal.valueOf(refusjonsKrav.get(0) / 12));
        var im2 = verdikjedeTestHjelper.opprettInntektsmeldingMedRefusjonskrav(Arbeidsgiver.virksomhet(beregningVirksomhet2),
            månedsinntekter.get(1), BigDecimal.valueOf(refusjonsKrav.get(1) / 12));
        var im3 = verdikjedeTestHjelper.opprettInntektsmeldingMedRefusjonskrav(Arbeidsgiver.virksomhet(beregningVirksomhet3),
            månedsinntekter.get(2), BigDecimal.valueOf(refusjonsKrav.get(2) / 12));
        var im4 = verdikjedeTestHjelper.opprettInntektsmeldingMedRefusjonskrav(Arbeidsgiver.virksomhet(beregningVirksomhet4),
            månedsinntekter.get(3), BigDecimal.valueOf(refusjonsKrav.get(3) / 12));
        List<InntektsmeldingDto> inntektsmeldinger = List.of(im1, im2, im3, im4);

        Periode opptjeningPeriode = Periode.of(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusYears(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.minusDays(1));
        var opptjeningAktiviteter = new OpptjeningAktiviteterDto(
            OpptjeningAktiviteterDto.nyPeriodeOrgnr(OpptjeningAktivitetType.ARBEID, opptjeningPeriode, orgnr1),
            OpptjeningAktiviteterDto.nyPeriodeOrgnr(OpptjeningAktivitetType.ARBEID, opptjeningPeriode, orgnr2),
            OpptjeningAktiviteterDto.nyPeriodeOrgnr(OpptjeningAktivitetType.ARBEID, opptjeningPeriode, orgnr3),
            OpptjeningAktiviteterDto.nyPeriodeOrgnr(OpptjeningAktivitetType.ARBEID, opptjeningPeriode, orgnr4));

        var iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.oppdatere(Optional.empty())
            .medData(tuple.getElement2())
            .medInntektsmeldinger(inntektsmeldinger).build();
        var input = lagInput(ref, opptjeningAktiviteter, iayGrunnlag, 100);

        // Act 1: kontroller fakta for beregning
        BeregningsgrunnlagGrunnlagDto grunnlag = verdikjedeTestHjelper.kjørStegOgLagreGrunnlag(input, beregningTjenesteWrapper);
        input = input.medBeregningsgrunnlagGrunnlag(grunnlag);
        var aksjonspunktResultat = beregningTjenesteWrapper.getAksjonspunktUtlederFaktaOmBeregning().utledAksjonspunkterFor(input, grunnlag, false);

        // Assert 1
        assertThat(aksjonspunktResultat.getBeregningAksjonspunktResultatList()).isEmpty();

        // Act 2: foreslå beregningsgrunnlag
        BeregningsgrunnlagRegelResultat resultat = foreslåBeregningsgrunnlag.foreslåBeregningsgrunnlag(input);

        // Assert 2
        verdikjedeTestHjelper.verifiserBeregningsgrunnlagBasis(resultat, Hjemmel.F_14_7_8_30);

        BeregningsgrunnlagDto foreslåttBeregningsgrunnlag = resultat.getBeregningsgrunnlag();
        BeregningsgrunnlagPeriodeDto periode = foreslåttBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        verdikjedeTestHjelper.verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, Intervall.TIDENES_ENDE, 4);
        verdikjedeTestHjelper.verifiserBGATførAvkorting(periode, ÅRSINNTEKT, virksomhetene);
        verdikjedeTestHjelper.verifiserSammenligningsgrunnlag(foreslåttBeregningsgrunnlag.getSammenligningsgrunnlag(),
            totalÅrsinntekt, SKJÆRINGSTIDSPUNKT_BEREGNING.minusYears(1).withDayOfMonth(1),
            SKJÆRINGSTIDSPUNKT_BEREGNING.withDayOfMonth(1).minusDays(1), BigDecimal.ZERO);

        // Act 3: fordel beregningsgrunnlag
        resultat = vurderBeregningsgrunnlagTjeneste.vurderBeregningsgrunnlag(input,
            nyttGrunnlag(grunnlag, foreslåttBeregningsgrunnlag, BeregningsgrunnlagTilstand.FORESLÅTT));
        assertThat(resultat.getVilkårOppfylt()).isTrue();
        BeregningsgrunnlagDto fordeltBeregningsgrunnlag = fordelBeregningsgrunnlag(input, grunnlag, resultat);
        input = input.medBeregningsgrunnlagGrunnlag(BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(grunnlag).medBeregningsgrunnlag(fordeltBeregningsgrunnlag).build(BeregningsgrunnlagTilstand.OPPDATERT_MED_REFUSJON_OG_GRADERING));

        // Act 4: fastsette beregningsgrunnlag
        var fastsattBeregningsgrunnlag = beregningTjenesteWrapper.getFullføreBeregningsgrunnlagTjeneste().fullføreBeregningsgrunnlag(input
        );

        // Assert 3
        verdikjedeTestHjelper.verifiserBeregningsgrunnlagBasis(fastsattBeregningsgrunnlag, Hjemmel.F_14_7_8_30);

        periode = fastsattBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        verdikjedeTestHjelper.verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, Intervall.TIDENES_ENDE, 4, forventetDagsats);
        verdikjedeTestHjelper.verifiserBGATetterAvkorting(periode,
            ÅRSINNTEKT, ÅRSINNTEKT, virksomhetene, forventetRedusert, refusjonsKrav, forventetRedusert, forventetRedusertBrukersAndel, false);
    }

    @Test
    public void fireArbeidsforholdMedBgOver6gOgDelvisRefusjonOver6G() {
        String orgnr1 = ARBEIDSFORHOLD_ORGNR1;
        String orgnr2 = ARBEIDSFORHOLD_ORGNR2;
        String orgnr3 = ARBEIDSFORHOLD_ORGNR3;
        String orgnr4 = ARBEIDSFORHOLD_ORGNR4;

        final List<Double> ÅRSINNTEKT = List.of(400000d, 500000d, 300000d, 100000d);
        final List<Double> refusjonsKrav = List.of(200000d, 150000d, 100000d, 42000d);

        final Double totalÅrsinntekt = ÅRSINNTEKT.stream().reduce((v1, v2) -> v1 + v2).orElse(null);

        double arb1 = refusjonsKrav.get(0);
        double arb2 = refusjonsKrav.get(1);
        double arb3 = refusjonsKrav.get(2);
        double arb4 = refusjonsKrav.get(3);

        double rest = SEKS_G - (arb1 + arb4);
        double bruker1 = 0.0d;
        double bruker2 = rest * ÅRSINNTEKT.get(1) / (ÅRSINNTEKT.get(1) + ÅRSINNTEKT.get(2)) - arb2;
        double bruker3 = rest * ÅRSINNTEKT.get(2) / (ÅRSINNTEKT.get(1) + ÅRSINNTEKT.get(2)) - arb3;
        double bruker4 = 0.0d;

        final List<Double> forventetRedusert = List.of(arb1, arb2, arb3, arb4);

        final List<Double> forventetRedusertBrukersAndel = List.of(bruker1, bruker2, bruker3, bruker4);

        final long forventetDagsats = forventetRedusert.stream().mapToLong(dv -> Math.round(dv / 260)).sum() +
            forventetRedusertBrukersAndel.stream().mapToLong(dv -> Math.round(dv / 260)).sum();

        final List<Double> forventetAvkortet = List.of(arb1 + bruker1, arb2 + bruker2, arb3 + bruker3, arb4 + bruker4);

        List<BigDecimal> månedsinntekter = ÅRSINNTEKT.stream().map((v) -> BigDecimal.valueOf(v / 12)).collect(Collectors.toList());
        List<String> virksomhetene = List.of(orgnr1, orgnr2, orgnr3, orgnr4);

        // Arrange 1
        Tuple<BehandlingReferanse, InntektArbeidYtelseAggregatBuilder> tuple = lagBehandlingAT(
            BigDecimal.valueOf(totalÅrsinntekt / 12),
            virksomhetene);
        BehandlingReferanse ref = lagReferanse(tuple.getElement1());

        var im1 = verdikjedeTestHjelper.opprettInntektsmeldingMedRefusjonskrav(Arbeidsgiver.virksomhet(beregningVirksomhet1),
            månedsinntekter.get(0), BigDecimal.valueOf(refusjonsKrav.get(0) / 12));
        var im2 = verdikjedeTestHjelper.opprettInntektsmeldingMedRefusjonskrav(Arbeidsgiver.virksomhet(beregningVirksomhet2),
            månedsinntekter.get(1), BigDecimal.valueOf(refusjonsKrav.get(1) / 12));
        var im3 = verdikjedeTestHjelper.opprettInntektsmeldingMedRefusjonskrav(Arbeidsgiver.virksomhet(beregningVirksomhet3),
            månedsinntekter.get(2), BigDecimal.valueOf(refusjonsKrav.get(2) / 12));
        var im4 = verdikjedeTestHjelper.opprettInntektsmeldingMedRefusjonskrav(Arbeidsgiver.virksomhet(beregningVirksomhet4),
            månedsinntekter.get(3), BigDecimal.valueOf(refusjonsKrav.get(3) / 12));
        List<InntektsmeldingDto> inntektsmeldinger = List.of(im1, im2, im3, im4);

        Periode opptjeningPeriode = Periode.of(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusYears(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.minusDays(1));
        var opptjeningAktiviteter = new OpptjeningAktiviteterDto(
            OpptjeningAktiviteterDto.nyPeriodeOrgnr(OpptjeningAktivitetType.ARBEID, opptjeningPeriode, orgnr1),
            OpptjeningAktiviteterDto.nyPeriodeOrgnr(OpptjeningAktivitetType.ARBEID, opptjeningPeriode, orgnr2),
            OpptjeningAktiviteterDto.nyPeriodeOrgnr(OpptjeningAktivitetType.ARBEID, opptjeningPeriode, orgnr3),
            OpptjeningAktiviteterDto.nyPeriodeOrgnr(OpptjeningAktivitetType.ARBEID, opptjeningPeriode, orgnr4));

        var iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.oppdatere(Optional.empty())
            .medData(tuple.getElement2())
            .medInntektsmeldinger(inntektsmeldinger).build();
        var input = lagInput(ref, opptjeningAktiviteter, iayGrunnlag, 100);

        // Act 1: kontroller fakta for beregning
        BeregningsgrunnlagGrunnlagDto grunnlag = verdikjedeTestHjelper.kjørStegOgLagreGrunnlag(input, beregningTjenesteWrapper);
        input = input.medBeregningsgrunnlagGrunnlag(grunnlag);
        var aksjonspunktResultat = beregningTjenesteWrapper.getAksjonspunktUtlederFaktaOmBeregning().utledAksjonspunkterFor(input, grunnlag, false);

        // Assert 1
        assertThat(aksjonspunktResultat.getBeregningAksjonspunktResultatList()).isEmpty();

        // Act 2: foreslå beregningsgrunnlag
        BeregningsgrunnlagRegelResultat resultat = foreslåBeregningsgrunnlag.foreslåBeregningsgrunnlag(input);

        // Assert 2
        verdikjedeTestHjelper.verifiserBeregningsgrunnlagBasis(resultat, Hjemmel.F_14_7_8_30);

        BeregningsgrunnlagDto foreslåttBeregningsgrunnlag = resultat.getBeregningsgrunnlag();
        BeregningsgrunnlagPeriodeDto periode = foreslåttBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        verdikjedeTestHjelper.verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, Intervall.TIDENES_ENDE, 4);
        verdikjedeTestHjelper.verifiserBGATførAvkorting(periode, ÅRSINNTEKT, virksomhetene);
        verdikjedeTestHjelper.verifiserSammenligningsgrunnlag(foreslåttBeregningsgrunnlag.getSammenligningsgrunnlag(),
            totalÅrsinntekt, SKJÆRINGSTIDSPUNKT_BEREGNING.minusYears(1).withDayOfMonth(1),
            SKJÆRINGSTIDSPUNKT_BEREGNING.withDayOfMonth(1).minusDays(1), BigDecimal.ZERO);

        // Act 3: fordel beregningsgrunnlag
        resultat = vurderBeregningsgrunnlagTjeneste.vurderBeregningsgrunnlag(input,
            nyttGrunnlag(grunnlag, foreslåttBeregningsgrunnlag, BeregningsgrunnlagTilstand.FORESLÅTT));
        assertThat(resultat.getVilkårOppfylt()).isTrue();
        BeregningsgrunnlagDto fordeltBeregningsgrunnlag = fordelBeregningsgrunnlag(input, grunnlag, resultat);
        input = input.medBeregningsgrunnlagGrunnlag(BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(grunnlag).medBeregningsgrunnlag(fordeltBeregningsgrunnlag).build(BeregningsgrunnlagTilstand.OPPDATERT_MED_REFUSJON_OG_GRADERING));

        // Act 4: fastsette beregningsgrunnlag
        var fastsattBeregningsgrunnlag = beregningTjenesteWrapper.getFullføreBeregningsgrunnlagTjeneste().fullføreBeregningsgrunnlag(input
        );

        // Assert 3
        verdikjedeTestHjelper.verifiserBeregningsgrunnlagBasis(fastsattBeregningsgrunnlag, Hjemmel.F_14_7_8_30);

        periode = fastsattBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        verdikjedeTestHjelper.verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, Intervall.TIDENES_ENDE, 4, forventetDagsats);
        verdikjedeTestHjelper.verifiserBGATetterAvkorting(periode,
            ÅRSINNTEKT, ÅRSINNTEKT, virksomhetene, forventetAvkortet, refusjonsKrav, forventetRedusert, forventetRedusertBrukersAndel, false);
    }

    @Test
    public void toArbeidsforholdMedOverstyringEtterTilbakeføringOver6GMedRefusjonOver6G() {
        String orgnr1 = ARBEIDSFORHOLD_ORGNR1;
        String orgnr2 = ARBEIDSFORHOLD_ORGNR2;

        final List<Double> ÅRSINNTEKT = List.of(720000d, 720000d);
        final Double totalÅrsinntekt = ÅRSINNTEKT.stream().reduce((v1, v2) -> v1 + v2).orElse(null);
        final List<Double> refusjonsKrav = List.of(SEKS_G, SEKS_G);

        final double forventetRedusert1 = SEKS_G * ÅRSINNTEKT.get(0) / (ÅRSINNTEKT.get(0) + ÅRSINNTEKT.get(1));
        final double forventetRedusert2 = SEKS_G * ÅRSINNTEKT.get(1) / (ÅRSINNTEKT.get(0) + ÅRSINNTEKT.get(1));

        final List<Double> forventetRedusert = List.of(forventetRedusert1, forventetRedusert2);
        final List<Double> forventetRedusertBrukersAndel = List.of(0d, 0d);

        List<BigDecimal> månedsinntekter = ÅRSINNTEKT.stream().map((v) -> BigDecimal.valueOf(v / 12)).collect(Collectors.toList());

        // Arrange 1
        List<String> virksomhetene = List.of(orgnr1, orgnr2);
        Tuple<BehandlingReferanse, InntektArbeidYtelseAggregatBuilder> behandlingReferanse = lagBehandlingAT(
            BigDecimal.valueOf(totalÅrsinntekt / 12),
            virksomhetene);
        BehandlingReferanse ref = lagReferanse(behandlingReferanse.getElement1());

        var im1 = verdikjedeTestHjelper.opprettInntektsmeldingMedRefusjonskrav(Arbeidsgiver.virksomhet(beregningVirksomhet1),
            månedsinntekter.get(0), BigDecimal.valueOf(refusjonsKrav.get(0) / 12));
        var im2 = verdikjedeTestHjelper.opprettInntektsmeldingMedRefusjonskrav(Arbeidsgiver.virksomhet(beregningVirksomhet2),
            månedsinntekter.get(1), BigDecimal.valueOf(refusjonsKrav.get(1) / 12));
        List<InntektsmeldingDto> inntektsmeldinger = List.of(im1, im2);

        Periode opptjeningPeriode = Periode.of(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusYears(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.minusDays(1));
        var opptjeningAktiviteter = new OpptjeningAktiviteterDto(
            OpptjeningAktiviteterDto.nyPeriodeOrgnr(OpptjeningAktivitetType.ARBEID, opptjeningPeriode, orgnr1),
            OpptjeningAktiviteterDto.nyPeriodeOrgnr(OpptjeningAktivitetType.ARBEID, opptjeningPeriode, orgnr2));


        var iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.oppdatere(Optional.empty())
            .medData(behandlingReferanse.getElement2())
            .medInntektsmeldinger(inntektsmeldinger).build();
        var input = lagInput(ref, opptjeningAktiviteter, iayGrunnlag, 100);

        // Act 1: kontroller fakta for beregning
        BeregningsgrunnlagGrunnlagDto grunnlag = verdikjedeTestHjelper.kjørStegOgLagreGrunnlag(input, beregningTjenesteWrapper);
        input = input.medBeregningsgrunnlagGrunnlag(grunnlag);
        var aksjonspunktResultat = beregningTjenesteWrapper.getAksjonspunktUtlederFaktaOmBeregning().utledAksjonspunkterFor(input, grunnlag, false);

        // Assert 1
        assertThat(aksjonspunktResultat.getBeregningAksjonspunktResultatList()).isEmpty();

        // Act 2: foreslå beregningsgrunnlag
        BeregningsgrunnlagRegelResultat resultat = foreslåBeregningsgrunnlag.foreslåBeregningsgrunnlag(input);

        // Assert 2
        verdikjedeTestHjelper.verifiserBeregningsgrunnlagBasis(resultat, Hjemmel.F_14_7_8_30);

        BeregningsgrunnlagDto beregningsgrunnlagEtter1 = resultat.getBeregningsgrunnlag();
        BeregningsgrunnlagPeriodeDto periodeEtter1 = beregningsgrunnlagEtter1.getBeregningsgrunnlagPerioder().get(0);
        verdikjedeTestHjelper.verifiserPeriode(periodeEtter1, SKJÆRINGSTIDSPUNKT_BEREGNING, Intervall.TIDENES_ENDE, 2);
        verdikjedeTestHjelper.verifiserBGATførAvkorting(periodeEtter1, ÅRSINNTEKT, virksomhetene);
        verdikjedeTestHjelper.verifiserSammenligningsgrunnlag(beregningsgrunnlagEtter1.getSammenligningsgrunnlag(),
            totalÅrsinntekt, SKJÆRINGSTIDSPUNKT_BEREGNING.minusYears(1).withDayOfMonth(1),
            SKJÆRINGSTIDSPUNKT_BEREGNING.withDayOfMonth(1).minusDays(1), BigDecimal.ZERO);

        // Arrange 2: Overstyring
        double overstyrt = 700000.0;
        final BeregningsgrunnlagPeriodeDto periode1 = periodeEtter1;
        periodeEtter1.getBeregningsgrunnlagPrStatusOgAndelList().forEach(af -> BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(af)
            .medOverstyrtPrÅr(BigDecimal.valueOf(overstyrt))
            .build(periode1));

        // Act 3: fordel beregningsgrunnlag
        resultat = vurderBeregningsgrunnlagTjeneste.vurderBeregningsgrunnlag(input,
            nyttGrunnlag(grunnlag, beregningsgrunnlagEtter1, BeregningsgrunnlagTilstand.FORESLÅTT));
        assertThat(resultat.getVilkårOppfylt()).isTrue();
        beregningsgrunnlagEtter1 = fordelBeregningsgrunnlag(input, grunnlag, resultat);
        input = input.medBeregningsgrunnlagGrunnlag(BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(grunnlag).medBeregningsgrunnlag(beregningsgrunnlagEtter1).build(BeregningsgrunnlagTilstand.OPPDATERT_MED_REFUSJON_OG_GRADERING));

        // Act 4: fastsette beregningsgrunnlag
        var fastsattBeregningsgrunnlag = beregningTjenesteWrapper.getFullføreBeregningsgrunnlagTjeneste().fullføreBeregningsgrunnlag(input
        );

        // Arrange 3: Tilbakehopp med Overstyring
        double overstyrt2 = 720000.0;
        BeregningsgrunnlagDto beregningsgrunnlagEtter2 = fastsattBeregningsgrunnlag;
        final BeregningsgrunnlagPeriodeDto periodeEtter2 = beregningsgrunnlagEtter2.getBeregningsgrunnlagPerioder().get(0);
        periodeEtter2.getBeregningsgrunnlagPrStatusOgAndelList().forEach(af -> BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(af)
            .medOverstyrtPrÅr(BigDecimal.valueOf(overstyrt2))
            .build(periodeEtter2));

        // Act 3: fordel beregningsgrunnlag
        resultat = vurderBeregningsgrunnlagTjeneste.vurderBeregningsgrunnlag(input,
            nyttGrunnlag(grunnlag, beregningsgrunnlagEtter2, BeregningsgrunnlagTilstand.FORESLÅTT));
        assertThat(resultat.getVilkårOppfylt()).isTrue();
        beregningsgrunnlagEtter2 = fordelBeregningsgrunnlag(input, grunnlag, resultat);
        input = input.medBeregningsgrunnlagGrunnlag(BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(grunnlag).medBeregningsgrunnlag(beregningsgrunnlagEtter2).build(BeregningsgrunnlagTilstand.OPPDATERT_MED_REFUSJON_OG_GRADERING));

        // Act 4: fastsette beregningsgrunnlag
        fastsattBeregningsgrunnlag = beregningTjenesteWrapper.getFullføreBeregningsgrunnlagTjeneste().fullføreBeregningsgrunnlag(input
        );

        // Assert 3-4
        verdikjedeTestHjelper.verifiserBeregningsgrunnlagBasis(fastsattBeregningsgrunnlag, Hjemmel.F_14_7_8_30);

        BeregningsgrunnlagPeriodeDto periodeEtter3 = fastsattBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        Long forvetetAndelSum = Math.round((SEKS_G / 2) / 260) * 2;
        verdikjedeTestHjelper.verifiserPeriode(periodeEtter3, SKJÆRINGSTIDSPUNKT_BEREGNING, Intervall.TIDENES_ENDE, 2, forvetetAndelSum);
        verdikjedeTestHjelper.verifiserBGATetterAvkorting(periodeEtter3,
            ÅRSINNTEKT, ÅRSINNTEKT, virksomhetene, forventetRedusert, refusjonsKrav, forventetRedusert, forventetRedusertBrukersAndel, true);
    }

    private BeregningsgrunnlagDto fordelBeregningsgrunnlag(BeregningsgrunnlagInput input, BeregningsgrunnlagGrunnlagDto grunnlag,
                                                           BeregningsgrunnlagRegelResultat resultat) {
        return beregningTjenesteWrapper.getFordelBeregningsgrunnlagTjeneste().fordelBeregningsgrunnlag(input.medBeregningsgrunnlagGrunnlag(grunnlag),
            resultat.getBeregningsgrunnlag());
    }

    private BehandlingReferanse lagReferanse(BehandlingReferanse behandlingReferanse) {
        return behandlingReferanse.medSkjæringstidspunkt(
            Skjæringstidspunkt.builder()
                .medSkjæringstidspunktBeregning(SKJÆRINGSTIDSPUNKT_BEREGNING)
                .medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT_OPPTJENING)
                .medFørsteUttaksdato(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusDays(1))
                .build());
    }

    private void verifiserBeregningsgrunnlagMedAksjonspunkt(BeregningsgrunnlagRegelResultat resultat) {
        assertThat(resultat.getBeregningsgrunnlag()).isNotNull();
        assertThat(resultat.getAksjonspunkter()).hasSize(1);
        assertThat(resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder()).hasSize(1);
    }

    private void verifiserBGATetterOverstyring(BeregningsgrunnlagPrStatusOgAndelDto bgpsa,
                                               Double bg,
                                               String virksomhetOrgnr,
                                               Double overstyrt,
                                               Double avkortet,
                                               Double redusert,
                                               Double maksimalRefusjon,
                                               Double avkortetRefusjon,
                                               Double avkortetBrukersAndel,
                                               Double redusertRefusjon,
                                               Double redusertBrukersAndel) {
        assertThat(bgpsa.getAktivitetStatus()).isEqualTo(AktivitetStatus.ARBEIDSTAKER);
        assertThat(bgpsa.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getArbeidsgiver))
            .hasValueSatisfying(arbeidsgiver -> assertThat(arbeidsgiver.getOrgnr()).isEqualTo(virksomhetOrgnr));
        assertThat(bgpsa.getBgAndelArbeidsforhold()
            .map(BGAndelArbeidsforholdDto::getArbeidsforholdRef)
            .map(InternArbeidsforholdRefDto::gjelderForSpesifiktArbeidsforhold).orElse(false))
            .as("gjelderSpesifiktArbeidsforhold").isFalse();
        assertThat(bgpsa.getArbeidsforholdType()).isEqualTo(OpptjeningAktivitetType.ARBEID);
        assertThat(bgpsa.getBeregnetPrÅr().doubleValue()).isEqualTo(bg);
        assertThat(bgpsa.getBruttoPrÅr().doubleValue()).isEqualTo(overstyrt);

        assertThat(bgpsa.getOverstyrtPrÅr().doubleValue()).as("OverstyrtPrÅr")
            .isEqualTo(overstyrt);
        assertThat(bgpsa.getAvkortetPrÅr().doubleValue()).as("AvkortetPrÅr")
            .isCloseTo(avkortet, within(0.01));
        assertThat(bgpsa.getRedusertPrÅr().doubleValue()).as("RedusertPrÅr")
            .isCloseTo(redusert, within(0.01));

        assertThat(bgpsa.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforholdDto::getNaturalytelseBortfaltPrÅr))
            .as("NaturalytelseBortfaltPrÅr")
            .isEmpty();

        assertThat(bgpsa.getMaksimalRefusjonPrÅr().doubleValue()).as("MaksimalRefusjonPrÅr")
            .isCloseTo(maksimalRefusjon, within(0.01));
        assertThat(bgpsa.getAvkortetRefusjonPrÅr().doubleValue()).as("AvkortetRefusjonPrÅr")
            .isCloseTo(avkortetRefusjon, within(0.01));
        assertThat(bgpsa.getRedusertRefusjonPrÅr().doubleValue()).as("RedusertRefusjonPrÅr")
            .isCloseTo(redusertRefusjon, within(0.01));

        assertThat(bgpsa.getAvkortetBrukersAndelPrÅr().doubleValue()).as("AvkortetBrukersAndelPrÅr")
            .isCloseTo(avkortetBrukersAndel, within(0.01));
        assertThat(bgpsa.getRedusertBrukersAndelPrÅr().doubleValue()).as("RedusertBrukersAndelPrÅr")
            .isCloseTo(redusertBrukersAndel, within(0.01));
    }

}
