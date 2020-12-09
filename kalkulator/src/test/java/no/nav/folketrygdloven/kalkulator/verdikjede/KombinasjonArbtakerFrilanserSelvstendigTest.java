package no.nav.folketrygdloven.kalkulator.verdikjede;

import static no.nav.folketrygdloven.kalkulator.verdikjede.BeregningsgrunnlagGrunnlagTestUtil.nyttGrunnlag;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.BeregningsgrunnlagInputTestUtil;
import no.nav.folketrygdloven.kalkulator.GrunnbeløpMock;
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
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktiviteterDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.vilkår.VurderBeregningsgrunnlagTjeneste;
import no.nav.folketrygdloven.kalkulator.steg.foreslå.ForeslåBeregningsgrunnlag;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.FaktaOmBeregningTilfelle;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.Hjemmel;
import no.nav.folketrygdloven.utils.UnitTestLookupInstanceImpl;

public class KombinasjonArbtakerFrilanserSelvstendigTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT_BEREGNING = VerdikjedeTestHjelper.SKJÆRINGSTIDSPUNKT_OPPTJENING;

    private static final String ORGNR1 = "915933149";
    private static final String ORGNR2 = "974760673";
    private static final String ORGNR3 = "974761076";

    private VerdikjedeTestHjelper verdikjedeTestHjelper = new VerdikjedeTestHjelper();
    private long seksG;
    private Long gverdi;

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

        gverdi = GrunnbeløpMock.finnGrunnbeløp(SKJÆRINGSTIDSPUNKT_BEREGNING);
        seksG = gverdi * 6;
    }

    @Test
    public void toArbeidsforholdOgFrilansMedBgOver6gOgRefusjonUnder6G() {

        final List<Double> ÅRSINNTEKT = List.of(12 * 28000d, 12 * 14_000d);
        final List<Double> refusjonsKrav = List.of(12 * 20000d, 12 * 14_000d);
        final double frilansÅrsinntekt = 12 * 23000d;

        final double årsinntekt1 = 4.0 * GrunnbeløpMock.finnGrunnbeløpsnitt(LocalDate.of(2014, Month.JANUARY, 1));
        final double årsinntekt2 = 4.0 * GrunnbeløpMock.finnGrunnbeløpsnitt(LocalDate.of(2015, Month.JANUARY, 1));
        final double årsinntekt3 = 4.0 * GrunnbeløpMock.finnGrunnbeløpsnitt(LocalDate.of(2016, Month.JANUARY, 1));
        final List<Double> ÅRSINNTEKT_SN = List.of(årsinntekt1, årsinntekt2, årsinntekt3);
        final List<Integer> ÅR = List.of(2014, 2015, 2016);

        double forventetFlyttetTilArbeidsforhold = Math.max(refusjonsKrav.get(0) - ÅRSINNTEKT.get(0), 0L) + Math.max(refusjonsKrav.get(1) - ÅRSINNTEKT.get(1), 0L);
        List<Double> forventetFordelt = List.of(Math.max(refusjonsKrav.get(0), ÅRSINNTEKT.get(0)), Math.max(refusjonsKrav.get(1), ÅRSINNTEKT.get(1)));

        final double forventetBeregnetSN = BigDecimal.ZERO.max(BigDecimal.valueOf(4.0 * gverdi - (ÅRSINNTEKT.get(0) + ÅRSINNTEKT.get(1)))).doubleValue();

        boolean kanFlytteAltTilSN = (forventetBeregnetSN - forventetFlyttetTilArbeidsforhold) >= 0;
        double forventetFordeltSN = kanFlytteAltTilSN ? forventetBeregnetSN - forventetFlyttetTilArbeidsforhold : 0;
        double flyttesFraFL = forventetFlyttetTilArbeidsforhold - forventetBeregnetSN;
        double forventetFordeltFL = frilansÅrsinntekt - flyttesFraFL;

        double forventetRedusert1 = refusjonsKrav.get(0);
        double forventetRedusert2 = refusjonsKrav.get(1);

        double forventetRedusertFLogSN = Math.max(0, seksG - (forventetFordelt.stream().mapToDouble(Double::doubleValue).sum()));
        double forventetBrukersAndelFL = forventetRedusertFLogSN * forventetFordeltFL / (forventetFordeltFL + forventetFordeltSN);

        final double forventetAvkortetSN = forventetRedusertFLogSN * forventetFordeltSN / (forventetFordeltFL + forventetFordeltSN);
        final double forventetRedusertSN = forventetAvkortetSN;

        double forventetBrukersAndel1 = forventetFordelt.get(0) - forventetRedusert1;
        double forventetBrukersAndel2 = forventetFordelt.get(1) - forventetRedusert2;

        final List<Double> forventetRedusert = List.of(forventetRedusert1, forventetRedusert2);
        final List<Double> forventetRedusertBrukersAndel = List.of(forventetBrukersAndel1, forventetBrukersAndel2);

        List<BigDecimal> månedsinntekterAT = ÅRSINNTEKT.stream().map((v) -> BigDecimal.valueOf(v / 12)).collect(Collectors.toList());
        List<BigDecimal> årsinntekterSN = ÅRSINNTEKT_SN.stream().map(BigDecimal::valueOf).collect(Collectors.toList());

        List<String> virksomhetene = List.of(ORGNR1, ORGNR2);

        // Arrange
        InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder = verdikjedeTestHjelper.lagBehandlingATogFLogSN(månedsinntekterAT,
            virksomhetene,
            BigDecimal.valueOf(frilansÅrsinntekt / 12),
            årsinntekterSN,
            ÅR.get(0),
            null);

        var im1 = verdikjedeTestHjelper.opprettInntektsmeldingMedRefusjonskrav(Arbeidsgiver.virksomhet(ORGNR1), månedsinntekterAT.get(0),
            BigDecimal.valueOf(refusjonsKrav.get(0) / 12));
        var im2 = verdikjedeTestHjelper.opprettInntektsmeldingMedRefusjonskrav(Arbeidsgiver.virksomhet(ORGNR2), månedsinntekterAT.get(1),
            BigDecimal.valueOf(refusjonsKrav.get(1) / 12));
        var iayGrunnlag = iayGrunnlagBuilder.medInntektsmeldinger(im1, im2).build();

        var opptjeningPeriode = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING.minusYears(1), SKJÆRINGSTIDSPUNKT_BEREGNING.minusDays(1));
        var opptjeningAktiviteter = new OpptjeningAktiviteterDto(
            OpptjeningAktiviteterDto.nyPeriodeOrgnr(OpptjeningAktivitetType.FRILANS, opptjeningPeriode, ORGNR3),
            OpptjeningAktiviteterDto.nyPeriodeOrgnr(OpptjeningAktivitetType.ARBEID, opptjeningPeriode, ORGNR1),
            OpptjeningAktiviteterDto.nyPeriodeOrgnr(OpptjeningAktivitetType.ARBEID, opptjeningPeriode, ORGNR2),
            OpptjeningAktiviteterDto.nyPeriodeOrgnr(OpptjeningAktivitetType.NÆRING, opptjeningPeriode, ORGNR3)
            );

        var input = lagInput(koblingReferanse, opptjeningAktiviteter, iayGrunnlag, 100);

        // Act 1: kontroller fakta om beregning
        BeregningsgrunnlagGrunnlagDto grunnlag = kjørStegOgLagreGrunnlag(lagFastsettBeregningsaktiviteteterInput(input));
        input = input.medBeregningsgrunnlagGrunnlag(grunnlag);
        grunnlag = input.getBeregningsgrunnlagGrunnlag();
        var aksjonspunktResultat = beregningTjenesteWrapper.getAksjonspunktUtlederFaktaOmBeregning().utledAksjonspunkterFor(lagFaktaOmBeregningInput(input), grunnlag,
            false);

        // Assert 1
        assertThat(aksjonspunktResultat.getBeregningAksjonspunktResultatList()).hasSize(1);
        List<FaktaOmBeregningTilfelle> faktaOmBeregningTilfeller = aksjonspunktResultat.getFaktaOmBeregningTilfeller();
        assertThat(faktaOmBeregningTilfeller).hasSize(1);
        assertThat(faktaOmBeregningTilfeller.get(0)).isEqualTo(FaktaOmBeregningTilfelle.VURDER_MOTTAR_YTELSE);

        // Act 2: foreslå beregningsgrunnlag
        BeregningsgrunnlagRegelResultat resultat = foreslåBeregningsgrunnlag.foreslåBeregningsgrunnlag(lagForeslåBeregningsgrunnlagInput(input));

        // Assert 2
        verdikjedeTestHjelper.verifiserBeregningsgrunnlagBasis(resultat, Hjemmel.F_14_7_8_43);

        BeregningsgrunnlagDto foreslåttBeregningsgrunnlag = resultat.getBeregningsgrunnlag();
        BeregningsgrunnlagPeriodeDto periode = foreslåttBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        verdikjedeTestHjelper.verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, Intervall.TIDENES_ENDE, 4);
        verdikjedeTestHjelper.verifiserBGSNførAvkorting(periode, forventetFordeltSN, forventetBeregnetSN, 2016);
        assertThat(foreslåttBeregningsgrunnlag.getSammenligningsgrunnlag()).isNull();

        verdikjedeTestHjelper.verifiserBGATførAvkorting(periode, ÅRSINNTEKT, virksomhetene);
        verdikjedeTestHjelper.verifiserFLførAvkorting(periode, frilansÅrsinntekt);

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
        verdikjedeTestHjelper.verifiserBeregningsgrunnlagBasis(fastsattBeregningsgrunnlag, Hjemmel.F_14_7_8_43);

        periode = fastsattBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        verdikjedeTestHjelper.verifiserBGSNetterAvkorting(periode, forventetBeregnetSN, forventetFordeltSN, forventetAvkortetSN, forventetRedusertSN, 2016);

        verdikjedeTestHjelper.verifiserBGATetterAvkorting(periode,
            ÅRSINNTEKT, forventetFordelt, virksomhetene, forventetFordelt, forventetRedusert, forventetRedusert, forventetRedusertBrukersAndel, false);
        verdikjedeTestHjelper.verifiserFLetterAvkorting(periode, frilansÅrsinntekt, forventetFordeltFL, forventetBrukersAndelFL, forventetBrukersAndelFL);
    }

    @Test
    public void frilansOgSNMedBgFraArbeidsforholdUnder6G() {

        final List<Double> ÅRSINNTEKT = new ArrayList<>();
        final Double frilansÅrsinntekt = 12 * 23000d;

        final double årsinntekt1 = 4.0 * GrunnbeløpMock.finnGrunnbeløpsnitt(LocalDate.of(2014, Month.JANUARY, 1));
        final double årsinntekt2 = 4.0 * GrunnbeløpMock.finnGrunnbeløpsnitt(LocalDate.of(2015, Month.JANUARY, 1));
        final double årsinntekt3 = 4.0 * GrunnbeløpMock.finnGrunnbeløpsnitt(LocalDate.of(2016, Month.JANUARY, 1));
        final List<Double> ÅRSINNTEKT_SN = List.of(årsinntekt1, årsinntekt2, årsinntekt3);
        final List<Integer> ÅR = List.of(2014, 2015, 2016);

        final double forventetBruttoSN = 4 * gverdi - frilansÅrsinntekt;

        double forventetBrukersAndelFL = frilansÅrsinntekt;

        final double forventetAvkortetSN = forventetBruttoSN;
        final double forventetRedusertSN = forventetAvkortetSN;

        final List<Double> forventetRedusert = new ArrayList<>();
        final List<Double> forventetRedusertBrukersAndel = new ArrayList<>();

        List<BigDecimal> årsinntekterSN = ÅRSINNTEKT_SN.stream().map(BigDecimal::valueOf).collect(Collectors.toList());

        List<String> virksomhetene = new ArrayList<>();

        // Arrange 1
        InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder = verdikjedeTestHjelper.lagBehandlingATogFLogSN(List.of(),
            virksomhetene,
            BigDecimal.valueOf(frilansÅrsinntekt / 12),
            årsinntekterSN,
            ÅR.get(0),
            null);


        var opptjeningPeriode = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING.minusYears(1), SKJÆRINGSTIDSPUNKT_BEREGNING.minusDays(1));
        var opptjeningAktiviteter = new OpptjeningAktiviteterDto(
            OpptjeningAktiviteterDto.nyPeriodeOrgnr(OpptjeningAktivitetType.FRILANS, opptjeningPeriode, ORGNR3),
            OpptjeningAktiviteterDto.nyPeriodeOrgnr(OpptjeningAktivitetType.NÆRING, opptjeningPeriode, ORGNR3)
            );
        var iayGrunnlag = iayGrunnlagBuilder.build();
        var input = lagInput(koblingReferanse, opptjeningAktiviteter, iayGrunnlag, 100);

        // Act 1: kontroller fakta om beregning
        BeregningsgrunnlagGrunnlagDto grunnlag = kjørStegOgLagreGrunnlag(lagFastsettBeregningsaktiviteteterInput(input));
        input = input.medBeregningsgrunnlagGrunnlag(grunnlag);
        grunnlag = input.getBeregningsgrunnlagGrunnlag();
        var aksjonspunktResultat = beregningTjenesteWrapper.getAksjonspunktUtlederFaktaOmBeregning().utledAksjonspunkterFor(lagFaktaOmBeregningInput(input), grunnlag,
            false);

        // Assert 1
        assertThat(aksjonspunktResultat.getBeregningAksjonspunktResultatList()).hasSize(1);
        BeregningsgrunnlagDto beregningsgrunnlag = grunnlag.getBeregningsgrunnlag().orElse(null);
        List<FaktaOmBeregningTilfelle> faktaOmBeregningTilfeller = aksjonspunktResultat.getFaktaOmBeregningTilfeller();
        assertThat(faktaOmBeregningTilfeller).hasSize(1);
        assertThat(faktaOmBeregningTilfeller.get(0)).isEqualTo(FaktaOmBeregningTilfelle.VURDER_MOTTAR_YTELSE);

        // Act 2: foreslå beregningsgrunnlag
        BeregningsgrunnlagRegelResultat resultat = foreslåBeregningsgrunnlag.foreslåBeregningsgrunnlag(lagForeslåBeregningsgrunnlagInput(input));

        // Assert 2
        verdikjedeTestHjelper.verifiserBeregningsgrunnlagBasis(resultat, Hjemmel.F_14_7_8_42);

        BeregningsgrunnlagDto foreslåttBeregningsgrunnlag = resultat.getBeregningsgrunnlag();
        BeregningsgrunnlagPeriodeDto periode = foreslåttBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        verdikjedeTestHjelper.verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, Intervall.TIDENES_ENDE, 2);
        verdikjedeTestHjelper.verifiserBGSNførAvkorting(periode, forventetBruttoSN, forventetBruttoSN, 2016);
        assertThat(beregningsgrunnlag.getSammenligningsgrunnlag()).isNull();

        verdikjedeTestHjelper.verifiserBGATførAvkorting(periode, ÅRSINNTEKT, virksomhetene);
        verdikjedeTestHjelper.verifiserFLførAvkorting(periode, frilansÅrsinntekt);

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
        verdikjedeTestHjelper.verifiserBeregningsgrunnlagBasis(fastsattBeregningsgrunnlag, Hjemmel.F_14_7_8_42);

        periode = fastsattBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        verdikjedeTestHjelper.verifiserBGSNetterAvkorting(periode, forventetBruttoSN, forventetBruttoSN, forventetAvkortetSN, forventetRedusertSN, 2016);

        verdikjedeTestHjelper.verifiserBGATetterAvkorting(periode,
            ÅRSINNTEKT, ÅRSINNTEKT, virksomhetene, ÅRSINNTEKT, forventetRedusert, forventetRedusert, forventetRedusertBrukersAndel, false);
        verdikjedeTestHjelper.verifiserFLetterAvkorting(periode, frilansÅrsinntekt, frilansÅrsinntekt, forventetBrukersAndelFL, forventetBrukersAndelFL);
    }

    private BeregningsgrunnlagGrunnlagDto kjørStegOgLagreGrunnlag(FastsettBeregningsaktiviteterInput input) {
        return verdikjedeTestHjelper.kjørStegOgLagreGrunnlag(input, beregningTjenesteWrapper);
    }

    private BeregningsgrunnlagDto fordelBeregningsgrunnlag(BeregningsgrunnlagInput input, BeregningsgrunnlagRegelResultat resultat) {
        return beregningTjenesteWrapper.getFordelBeregningsgrunnlagTjeneste().fordelBeregningsgrunnlag(input,
            resultat.getBeregningsgrunnlag()).getBeregningsgrunnlag();
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
