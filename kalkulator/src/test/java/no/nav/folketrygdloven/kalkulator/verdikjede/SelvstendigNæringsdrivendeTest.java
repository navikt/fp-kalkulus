package no.nav.folketrygdloven.kalkulator.verdikjede;

import static no.nav.folketrygdloven.kalkulator.verdikjede.BeregningsgrunnlagGrunnlagTestUtil.nyttGrunnlag;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.BeregningsgrunnlagInputTestUtil;
import no.nav.folketrygdloven.kalkulator.GrunnbeløpMock;
import no.nav.folketrygdloven.kalkulator.GrunnbeløpTestKonstanter;
import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.ForeldrepengerGrunnlagMapper;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapBeregningsgrunnlagFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapInntektsgrunnlagVLTilRegel;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapInntektsgrunnlagVLTilRegelFelles;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.YtelsesspesifikkRegelMapper;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.FaktaOmBeregningInput;
import no.nav.folketrygdloven.kalkulator.input.FastsettBeregningsaktiviteterInput;
import no.nav.folketrygdloven.kalkulator.input.FordelBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.ForeslåBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.StegProsesseringInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.SammenligningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktiviteterDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.vilkår.VurderBeregningsgrunnlagTjeneste;
import no.nav.folketrygdloven.kalkulator.steg.foreslå.ForeslåBeregningsgrunnlag;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.Hjemmel;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.utils.UnitTestLookupInstanceImpl;

public class SelvstendigNæringsdrivendeTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT_OPPTJENING = VerdikjedeTestHjelper.SKJÆRINGSTIDSPUNKT_OPPTJENING;
    private static final LocalDate SKJÆRINGSTIDSPUNKT_BEREGNING = SKJÆRINGSTIDSPUNKT_OPPTJENING;
    private static final String ORGNR1 = "974761076";
    private VerdikjedeTestHjelper verdikjedeTestHjelper = new VerdikjedeTestHjelper();
    private BeregningTjenesteWrapper beregningTjenesteWrapper;
    private KoblingReferanse koblingReferanse = new KoblingReferanseMock(SKJÆRINGSTIDSPUNKT_BEREGNING);
    private MapInntektsgrunnlagVLTilRegel mapInntektsgrunnlagVLTilRegel = new MapInntektsgrunnlagVLTilRegelFelles();
    private final UnitTestLookupInstanceImpl<YtelsesspesifikkRegelMapper> ytelsesSpesifikkMapper = new UnitTestLookupInstanceImpl<>(new ForeldrepengerGrunnlagMapper());
    private MapBeregningsgrunnlagFraVLTilRegel mapBeregningsgrunnlagFraVLTilRegel = new MapBeregningsgrunnlagFraVLTilRegel(new UnitTestLookupInstanceImpl<>(mapInntektsgrunnlagVLTilRegel), ytelsesSpesifikkMapper);
    private ForeslåBeregningsgrunnlag foreslåBeregningsgrunnlag = new ForeslåBeregningsgrunnlag(mapBeregningsgrunnlagFraVLTilRegel);
    private VurderBeregningsgrunnlagTjeneste vurderBeregningsgrunnlagTjeneste = new VurderBeregningsgrunnlagTjeneste(mapBeregningsgrunnlagFraVLTilRegel);

    @BeforeEach
    public void setup() {
        beregningTjenesteWrapper = BeregningTjenesteProvider.provide();
    }

    @Test
    public void skalBeregneAvvikVedVarigEndring() {
        // Arrange
        //6Gsnitt<PGI<12Gsnitt: Bidrag til beregningsgrunnlaget = 6 + (PGI-6*Gsnitt)/3*Gsnitt
        final List<Double> ÅRSINNTEKT = List.of(7.0 * GrunnbeløpTestKonstanter.GSNITT_2014, 8.0 * GrunnbeløpTestKonstanter.GSNITT_2015, 9.0 * GrunnbeløpTestKonstanter.GSNITT_2016);

        final double varigEndringMånedsinntekt = 8.0 * GrunnbeløpTestKonstanter.GRUNNBELØP_2017 / 12;
        //Gjennomsnittlig PGI = SUM(Bidrag til beregningsgrunnlaget)/3 * G
        final double forventetBrutto = 624226.66666;
        final BigDecimal forventetAvvikPromille = BigDecimal.valueOf(200);
        final double forventetAvkortet = 6.0 * GrunnbeløpTestKonstanter.GRUNNBELØP_2017;
        final double forventetRedusert = forventetAvkortet;

        List<BigDecimal> årsinntekterSN = ÅRSINNTEKT.stream().map(BigDecimal::valueOf).collect(Collectors.toList());

        // Arrange
        InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder = verdikjedeTestHjelper.lagBehandlingATogFLogSN(
            List.of(), List.of(), null, årsinntekterSN, 2014, BigDecimal.valueOf(12 * varigEndringMånedsinntekt));

        var opptjeningPeriode = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusYears(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.minusDays(1));
        var opptjeningAktiviteter = OpptjeningAktiviteterDto.fraOrgnr(OpptjeningAktivitetType.NÆRING, opptjeningPeriode, ORGNR1);

        var iayGrunnlag = iayGrunnlagBuilder.build();
        var input = lagInput(koblingReferanse, opptjeningAktiviteter, iayGrunnlag, 100);

        // Act 1: kontroller fakta om beregning
        BeregningsgrunnlagGrunnlagDto grunnlag = kjørStegOgLagreGrunnlag(lagFastsettBeregningsaktiviteteterInput(input));
        input = input.medBeregningsgrunnlagGrunnlag(grunnlag);

        var aksjonspunktResultat = beregningTjenesteWrapper.getAksjonspunktUtlederFaktaOmBeregning().utledAksjonspunkterFor(lagFaktaOmBeregningInput(input), grunnlag, false);

        // Assert 1
        assertThat(aksjonspunktResultat.getBeregningAksjonspunktResultatList()).isEmpty();

        // Act 2: foreslå beregningsgrunnlag
        BeregningsgrunnlagRegelResultat resultat = foreslåBeregningsgrunnlag.foreslåBeregningsgrunnlag(lagForeslåBeregningsgrunnlagInput(input));

        // Assert 2
        verdikjedeTestHjelper.verifiserBeregningsgrunnlagBasis(resultat, Hjemmel.F_14_7_8_35);

        BeregningsgrunnlagDto foreslåttBeregningsgrunnlag = resultat.getBeregningsgrunnlag();
        BeregningsgrunnlagPeriodeDto periode = foreslåttBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        verdikjedeTestHjelper.verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, Intervall.TIDENES_ENDE, 1);
        verdikjedeTestHjelper.verifiserBGSNførAvkorting(periode, forventetBrutto, forventetBrutto, 2016);
        SammenligningsgrunnlagDto sg = foreslåttBeregningsgrunnlag.getSammenligningsgrunnlag();
        assertThat(sg).isNotNull();
        assertThat(sg.getAvvikPromilleNy().compareTo(forventetAvvikPromille)).isEqualTo(0);

        // Act 3: fordel beregningsgrunnlag
        resultat = vurderBeregningsgrunnlagTjeneste.vurderBeregningsgrunnlag(lagFordelBeregningsgrunnlagInput(input),
            nyttGrunnlag(grunnlag, foreslåttBeregningsgrunnlag, BeregningsgrunnlagTilstand.FORESLÅTT));
        assertThat(resultat.getVilkårOppfylt()).isTrue();
        BeregningsgrunnlagDto fordeltBeregningsgrunnlag = fordelBeregningsgrunnlag(input, resultat);
        input = input.medBeregningsgrunnlagGrunnlag(BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(grunnlag).medBeregningsgrunnlag(fordeltBeregningsgrunnlag).build(BeregningsgrunnlagTilstand.OPPDATERT_MED_REFUSJON_OG_GRADERING));

        // Act 4: fastsette beregningsgrunnlag
        var fastsattBeregningsgrunnlag = beregningTjenesteWrapper.getFullføreBeregningsgrunnlagTjeneste().fullføreBeregningsgrunnlag(input
        ).getBeregningsgrunnlag();

        // Assert 3
        verdikjedeTestHjelper.verifiserBeregningsgrunnlagBasis(fastsattBeregningsgrunnlag, Hjemmel.F_14_7_8_35);

        periode = fastsattBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        verdikjedeTestHjelper.verifiserBGSNetterAvkorting(periode, forventetBrutto, forventetBrutto, forventetAvkortet, forventetRedusert, 2016);
    }

    @Test
    public void sammeLønnHvertÅrUnder6GIkkeVarigEndring() {

        //PGI <= 6xGsnitt: Bidrag til beregningsgrunnlaget = PGI/Gsnitt
        final double årsinntekt1 = 4.0 * GrunnbeløpTestKonstanter.GSNITT_2014;
        final double årsinntekt2 = 4.0 * GrunnbeløpTestKonstanter.GSNITT_2015;
        final double årsinntekt3 = 4.0 * GrunnbeløpTestKonstanter.GSNITT_2016;
        final List<Double> ÅRSINNTEKT = List.of(årsinntekt1, årsinntekt2, årsinntekt3);

        //Gjennomsnittlig PGI = SUM(Bidrag til beregningsgrunnlaget)/3 * G
        final double forventetBrutto = 4.0 * GrunnbeløpTestKonstanter.GRUNNBELØP_2017;
        final double forventetAvkortet = forventetBrutto;
        final double forventetRedusert = forventetAvkortet;
        List<BigDecimal> årsinntekterSN = ÅRSINNTEKT.stream().map(BigDecimal::valueOf).collect(Collectors.toList());

        // Arrange
        InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder = verdikjedeTestHjelper.lagBehandlingATogFLogSN(List.of(), List.of(), null, årsinntekterSN, 2014, null);

        var opptjeningPeriode = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusYears(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.minusDays(1));
        var opptjeningAktiviteter = OpptjeningAktiviteterDto.fraOrgnr(OpptjeningAktivitetType.NÆRING, opptjeningPeriode, ORGNR1);

        var iayGrunnlag = iayGrunnlagBuilder.build();
        var input = lagInput(koblingReferanse, opptjeningAktiviteter, iayGrunnlag, 100);

        // Act 1: kontroller fakta om beregning
        BeregningsgrunnlagGrunnlagDto grunnlag = kjørStegOgLagreGrunnlag(lagFastsettBeregningsaktiviteteterInput(input));
        input = input.medBeregningsgrunnlagGrunnlag(grunnlag);

        var aksjonspunktResultat = beregningTjenesteWrapper.getAksjonspunktUtlederFaktaOmBeregning().utledAksjonspunkterFor(lagFaktaOmBeregningInput(input), grunnlag, false);

        // Assert 1
        assertThat(aksjonspunktResultat.getBeregningAksjonspunktResultatList()).isEmpty();

        // Act 2: foreslå beregningsgrunnlag
        BeregningsgrunnlagRegelResultat resultat = foreslåBeregningsgrunnlag.foreslåBeregningsgrunnlag(lagForeslåBeregningsgrunnlagInput(input));

        // Assert 2
        verdikjedeTestHjelper.verifiserBeregningsgrunnlagBasis(resultat, Hjemmel.F_14_7_8_35);

        BeregningsgrunnlagDto foreslåttBeregningsgrunnlag = resultat.getBeregningsgrunnlag();
        BeregningsgrunnlagPeriodeDto periode = foreslåttBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        verdikjedeTestHjelper.verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, Intervall.TIDENES_ENDE, 1);
        verdikjedeTestHjelper.verifiserBGSNførAvkorting(periode, forventetBrutto, forventetBrutto, 2016);
        assertThat(foreslåttBeregningsgrunnlag.getSammenligningsgrunnlag()).isNull();

        // Act 3: fordel beregningsgrunnlag
        resultat = vurderBeregningsgrunnlagTjeneste.vurderBeregningsgrunnlag(lagFordelBeregningsgrunnlagInput(input),
            nyttGrunnlag(grunnlag, foreslåttBeregningsgrunnlag, BeregningsgrunnlagTilstand.FORESLÅTT));
        assertThat(resultat.getVilkårOppfylt()).isTrue();
        BeregningsgrunnlagDto fordeltBeregningsgrunnlag = fordelBeregningsgrunnlag(input, resultat);
        input = input.medBeregningsgrunnlagGrunnlag(BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(grunnlag).medBeregningsgrunnlag(fordeltBeregningsgrunnlag).build(BeregningsgrunnlagTilstand.OPPDATERT_MED_REFUSJON_OG_GRADERING));

        // Act 4: fastsette beregningsgrunnlag
        var fastsattBeregningsgrunnlag = beregningTjenesteWrapper.getFullføreBeregningsgrunnlagTjeneste().fullføreBeregningsgrunnlag(input
        ).getBeregningsgrunnlag();

        // Assert 3
        verdikjedeTestHjelper.verifiserBeregningsgrunnlagBasis(fastsattBeregningsgrunnlag, Hjemmel.F_14_7_8_35);

        periode = fastsattBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        verdikjedeTestHjelper.verifiserBGSNetterAvkorting(periode, forventetBrutto, forventetBrutto, forventetAvkortet, forventetRedusert, 2016);
    }

    @Test
    public void sammeLønnHvertÅrOver6GIkkeVarigEndring() {

        //6Gsnitt<PGI<12Gsnitt: Bidrag til beregningsgrunnlaget = 6 + (PGI-6*Gsnitt)/3*Gsnitt
        final double årsinntekt1 = 7.0 * GrunnbeløpTestKonstanter.GSNITT_2014;
        final double årsinntekt2 = 7.0 * GrunnbeløpTestKonstanter.GSNITT_2015;
        final double årsinntekt3 = 7.0 * GrunnbeløpTestKonstanter.GSNITT_2016;
        final List<Double> ÅRSINNTEKT = List.of(årsinntekt1, årsinntekt2, årsinntekt3);

        //Gjennomsnittlig PGI = SUM(Bidrag til beregningsgrunnlaget)/3 * G
        final double forventetBrutto = 6.333333 * GrunnbeløpTestKonstanter.GRUNNBELØP_2017;
        final double forventetAvkortet = 6.0 * GrunnbeløpTestKonstanter.GRUNNBELØP_2017;
        final double forventetRedusert = forventetAvkortet;

        List<BigDecimal> årsinntekterSN = ÅRSINNTEKT.stream().map(BigDecimal::valueOf).collect(Collectors.toList());

        // Arrange
        InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder = verdikjedeTestHjelper.lagBehandlingATogFLogSN(List.of(), List.of(), null, årsinntekterSN, 2014, null);

        var opptjeningPeriode = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusYears(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.minusDays(1));
        var opptjeningAktiviteter = OpptjeningAktiviteterDto.fraOrgnr(OpptjeningAktivitetType.NÆRING, opptjeningPeriode, ORGNR1);

        var iayGrunnlag = iayGrunnlagBuilder.build();
        var input = lagInput(koblingReferanse, opptjeningAktiviteter, iayGrunnlag, 100);

        // Act 1: kontroller fakta om beregning
        BeregningsgrunnlagGrunnlagDto grunnlag = kjørStegOgLagreGrunnlag(lagFastsettBeregningsaktiviteteterInput(input));
        input = input.medBeregningsgrunnlagGrunnlag(grunnlag);

        var aksjonspunktResultat = beregningTjenesteWrapper.getAksjonspunktUtlederFaktaOmBeregning().utledAksjonspunkterFor(lagFaktaOmBeregningInput(input), grunnlag, false);

        // Assert 1
        assertThat(aksjonspunktResultat.getBeregningAksjonspunktResultatList()).isEmpty();

        // Act 2: foreslå beregningsgrunnlag
        BeregningsgrunnlagRegelResultat resultat = foreslåBeregningsgrunnlag.foreslåBeregningsgrunnlag(lagForeslåBeregningsgrunnlagInput(input));

        // Assert 2
        verdikjedeTestHjelper.verifiserBeregningsgrunnlagBasis(resultat, Hjemmel.F_14_7_8_35);

        BeregningsgrunnlagDto foreslåttBeregningsgrunnlag = resultat.getBeregningsgrunnlag();
        BeregningsgrunnlagPeriodeDto periode = foreslåttBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        verdikjedeTestHjelper.verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, Intervall.TIDENES_ENDE, 1);
        verdikjedeTestHjelper.verifiserBGSNførAvkorting(periode, forventetBrutto, forventetBrutto, 2016);
        assertThat(foreslåttBeregningsgrunnlag.getSammenligningsgrunnlag()).isNull();

        // Act 3: fordel beregningsgrunnlag
        resultat = vurderBeregningsgrunnlagTjeneste.vurderBeregningsgrunnlag(lagFordelBeregningsgrunnlagInput(input),
            nyttGrunnlag(grunnlag, foreslåttBeregningsgrunnlag, BeregningsgrunnlagTilstand.FORESLÅTT));
        assertThat(resultat.getVilkårOppfylt()).isTrue();
        BeregningsgrunnlagDto fordeltBeregningsgrunnlag = fordelBeregningsgrunnlag(input, resultat);
        input = input.medBeregningsgrunnlagGrunnlag(BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(grunnlag).medBeregningsgrunnlag(fordeltBeregningsgrunnlag).build(BeregningsgrunnlagTilstand.OPPDATERT_MED_REFUSJON_OG_GRADERING));

        // Act 4: fastsette beregningsgrunnlag
        var fastsattBeregningsgrunnlag = beregningTjenesteWrapper.getFullføreBeregningsgrunnlagTjeneste().fullføreBeregningsgrunnlag(input
        ).getBeregningsgrunnlag();

        // Assert 3
        verdikjedeTestHjelper.verifiserBeregningsgrunnlagBasis(fastsattBeregningsgrunnlag, Hjemmel.F_14_7_8_35);

        periode = fastsattBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        verdikjedeTestHjelper.verifiserBGSNetterAvkorting(periode, forventetBrutto, forventetBrutto, forventetAvkortet, forventetRedusert, 2016);
    }

    @Test
    public void sammeLønnHvertÅrOver6GIkkeVarigEndringReduksjon() {

        //6Gsnitt<PGI<12Gsnitt: Bidrag til beregningsgrunnlaget = 6 + (PGI-6*Gsnitt)/3*Gsnitt
        final double årsinntekt1 = 7.0 * GrunnbeløpTestKonstanter.GSNITT_2014;
        final double årsinntekt2 = 7.0 * GrunnbeløpTestKonstanter.GSNITT_2015;
        final double årsinntekt3 = 7.0 * GrunnbeløpTestKonstanter.GSNITT_2016;
        final List<Double> ÅRSINNTEKT = List.of(årsinntekt1, årsinntekt2, årsinntekt3);

        //Gjennomsnittlig PGI = SUM(Bidrag til beregningsgrunnlaget)/3 * G
        final double forventetBrutto = 6.333333 * GrunnbeløpTestKonstanter.GRUNNBELØP_2017;
        final double forventetAvkortet = 6.0 * GrunnbeløpTestKonstanter.GRUNNBELØP_2017;
        final double forventetRedusert = forventetAvkortet * 0.8;

        List<BigDecimal> årsinntekterSN = ÅRSINNTEKT.stream().map(BigDecimal::valueOf).collect(Collectors.toList());

        // Arrange
        InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder = verdikjedeTestHjelper.lagBehandlingATogFLogSN(List.of(), List.of(), null, årsinntekterSN, 2014, null);

        var opptjeningPeriode = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusYears(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.minusDays(1));
        var opptjeningAktiviteter = OpptjeningAktiviteterDto.fraOrgnr(OpptjeningAktivitetType.NÆRING, opptjeningPeriode, ORGNR1);

        var iayGrunnlag = iayGrunnlagBuilder.build();
        var input = lagInput(koblingReferanse, opptjeningAktiviteter, iayGrunnlag, 80);

        // Act 1: kontroller fakta om beregning
        BeregningsgrunnlagGrunnlagDto grunnlag = kjørStegOgLagreGrunnlag(lagFastsettBeregningsaktiviteteterInput(input));
        input = input.medBeregningsgrunnlagGrunnlag(grunnlag);

        var aksjonspunktResultat = beregningTjenesteWrapper.getAksjonspunktUtlederFaktaOmBeregning().utledAksjonspunkterFor(lagFaktaOmBeregningInput(input), grunnlag, false);

        // Assert 1
        assertThat(aksjonspunktResultat.getBeregningAksjonspunktResultatList()).isEmpty();

        // Act 2: foreslå beregningsgrunnlag
        BeregningsgrunnlagRegelResultat resultat = foreslåBeregningsgrunnlag.foreslåBeregningsgrunnlag(lagForeslåBeregningsgrunnlagInput(input));

        // Assert 2
        verdikjedeTestHjelper.verifiserBeregningsgrunnlagBasis(resultat, Hjemmel.F_14_7_8_35);

        BeregningsgrunnlagDto foreslåttBeregningsgrunnlag = resultat.getBeregningsgrunnlag();
        BeregningsgrunnlagPeriodeDto periode = foreslåttBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        verdikjedeTestHjelper.verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, Intervall.TIDENES_ENDE, 1);
        verdikjedeTestHjelper.verifiserBGSNførAvkorting(periode, forventetBrutto, forventetBrutto, 2016);
        assertThat(foreslåttBeregningsgrunnlag.getSammenligningsgrunnlag()).isNull();

        // Act 3: fordel beregningsgrunnlag
        resultat = vurderBeregningsgrunnlagTjeneste.vurderBeregningsgrunnlag(lagFordelBeregningsgrunnlagInput(input),
            nyttGrunnlag(grunnlag, foreslåttBeregningsgrunnlag, BeregningsgrunnlagTilstand.FORESLÅTT));
        assertThat(resultat.getVilkårOppfylt()).isTrue();
        BeregningsgrunnlagDto fordeltBeregningsgrunnlag = fordelBeregningsgrunnlag(input, resultat);
        input = input.medBeregningsgrunnlagGrunnlag(BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(grunnlag).medBeregningsgrunnlag(fordeltBeregningsgrunnlag).build(BeregningsgrunnlagTilstand.OPPDATERT_MED_REFUSJON_OG_GRADERING));

        // Act 4: fastsette beregningsgrunnlag
        var fastsattBeregningsgrunnlag = beregningTjenesteWrapper.getFullføreBeregningsgrunnlagTjeneste().fullføreBeregningsgrunnlag(input
        ).getBeregningsgrunnlag();

        // Assert 3
        verdikjedeTestHjelper.verifiserBeregningsgrunnlagBasis(fastsattBeregningsgrunnlag, Hjemmel.F_14_7_8_35);

        periode = fastsattBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        verdikjedeTestHjelper.verifiserBGSNetterAvkorting(periode, forventetBrutto, forventetBrutto, forventetAvkortet, forventetRedusert, 2016);
    }

    private BeregningsgrunnlagGrunnlagDto kjørStegOgLagreGrunnlag(FastsettBeregningsaktiviteterInput input) {
        return verdikjedeTestHjelper.kjørStegOgLagreGrunnlag(input, beregningTjenesteWrapper);
    }

    private BeregningsgrunnlagDto fordelBeregningsgrunnlag(BeregningsgrunnlagInput input, BeregningsgrunnlagRegelResultat resultat) {
        return beregningTjenesteWrapper.getFordelBeregningsgrunnlagTjeneste().fordelBeregningsgrunnlag(input, resultat.getBeregningsgrunnlag()).getBeregningsgrunnlag();
    }

    private BeregningsgrunnlagInput lagInput(KoblingReferanse ref, OpptjeningAktiviteterDto opptjeningAktiviteter, InntektArbeidYtelseGrunnlagDto iayGrunnlag, int dekningsgrad) {
        return BeregningsgrunnlagInputTestUtil.lagInputMedIAYOgOpptjeningsaktiviteter(ref, opptjeningAktiviteter, iayGrunnlag, dekningsgrad, 2);
    }

    private StegProsesseringInput lagStegInput(BeregningsgrunnlagTilstand tilstand, BeregningsgrunnlagInput beregningsgrunnlagInput) {
        return new StegProsesseringInput(beregningsgrunnlagInput, tilstand);
    }

    private FaktaOmBeregningInput lagFaktaOmBeregningInput(BeregningsgrunnlagInput input) {
        return new FaktaOmBeregningInput(lagStegInput(BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER, input))
                .medGrunnbeløpsatser(GrunnbeløpMock.GRUNNBELØPSATSER);
    }

    private FastsettBeregningsaktiviteterInput lagFastsettBeregningsaktiviteteterInput(BeregningsgrunnlagInput input) {
        return new FastsettBeregningsaktiviteterInput(lagStegInput(BeregningsgrunnlagTilstand.OPPRETTET, input))
                .medGrunnbeløpsatser(GrunnbeløpMock.GRUNNBELØPSATSER);
    }


    private ForeslåBeregningsgrunnlagInput lagForeslåBeregningsgrunnlagInput(BeregningsgrunnlagInput input) {
        return new ForeslåBeregningsgrunnlagInput(lagStegInput(BeregningsgrunnlagTilstand.OPPDATERT_MED_REFUSJON_OG_GRADERING, input))
                .medGrunnbeløpsatser(GrunnbeløpMock.GRUNNBELØPSATSER);
    }

    private FordelBeregningsgrunnlagInput lagFordelBeregningsgrunnlagInput(BeregningsgrunnlagInput input) {
        return new FordelBeregningsgrunnlagInput(lagStegInput(BeregningsgrunnlagTilstand.OPPDATERT_MED_REFUSJON_OG_GRADERING, input))
                .medUregulertGrunnbeløp(BigDecimal.valueOf(GrunnbeløpMock.finnGrunnbeløp(SKJÆRINGSTIDSPUNKT_BEREGNING)));
    }


}
