package no.nav.folketrygdloven.kalkulator.verdikjede;

import static no.nav.folketrygdloven.kalkulator.OpprettRefusjondatoerFraInntektsmeldinger.opprett;
import static no.nav.folketrygdloven.kalkulator.verdikjede.BeregningsgrunnlagGrunnlagTestUtil.nyttGrunnlag;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.in;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.BeregningsgrunnlagInputTestUtil;
import no.nav.folketrygdloven.kalkulator.input.FaktaOmBeregningInput;
import no.nav.folketrygdloven.kalkulator.input.FastsettBeregningsaktiviteterInput;
import no.nav.folketrygdloven.kalkulator.input.FordelBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.ForeslåBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.StegProsesseringInput;
import no.nav.folketrygdloven.kalkulator.steg.foreslå.ForeslåBeregningsgrunnlag;
import no.nav.folketrygdloven.kalkulator.GrunnbeløpMock;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.vilkår.VurderBeregningsgrunnlagTjeneste;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.ForeldrepengerGrunnlagMapper;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapBeregningsgrunnlagFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapInntektsgrunnlagVLTilRegel;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapInntektsgrunnlagVLTilRegelFelles;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.YtelsesspesifikkRegelMapper;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.VersjonTypeDto;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulator.modell.typer.AktørId;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktiviteterDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.ArbeidType;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.FaktaOmBeregningTilfelle;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.Hjemmel;
import no.nav.folketrygdloven.utils.UnitTestLookupInstanceImpl;

public class FrilanserTest {

    private static final String DUMMY_ORGNR = "974760673";
    private static final LocalDate SKJÆRINGSTIDSPUNKT_BEREGNING = VerdikjedeTestHjelper.SKJÆRINGSTIDSPUNKT_OPPTJENING;
    private static final LocalDate MINUS_YEARS_1 = SKJÆRINGSTIDSPUNKT_BEREGNING.minusYears(1);
    private static final String ARBEIDSFORHOLD_ORGNR1 = "915933149";
    private static final String ARBEIDSFORHOLD_ORGNR2 = "923609016";
    private static final String ARBEIDSFORHOLD_ORGNR3 = "973152351";
    private KoblingReferanse koblingReferanse = new KoblingReferanseMock(SKJÆRINGSTIDSPUNKT_BEREGNING);

    private double seksG;

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

        seksG = GrunnbeløpMock.finnGrunnbeløp(SKJÆRINGSTIDSPUNKT_BEREGNING) * 6;
    }

    @Test
    public void toArbeidsforholdOgFrilansMedBgOver6gOgRefusjonUnder6G() {

        final List<Double> ÅRSINNTEKT = List.of(12 * 28_000d, 12 * 14_000d);
        final List<Double> refusjonsKrav = List.of(12 * 20_000d, 12 * 14_000d);
        final double sammenligning = 12 * 67_500d;
        final double frilansÅrsinntekt = 12 * 23_000d;

        double forventetRedusert1 = refusjonsKrav.get(0);
        double forventetRedusert2 = refusjonsKrav.get(1);

        double forventetFlyttetTilArbeidsforhold = Math.max(refusjonsKrav.get(0) - ÅRSINNTEKT.get(0), 0L) + Math.max(refusjonsKrav.get(1) - ÅRSINNTEKT.get(1), 0L);
        List<Double> forventetFordelt = List.of(Math.max(refusjonsKrav.get(0), ÅRSINNTEKT.get(0)), Math.max(refusjonsKrav.get(1), ÅRSINNTEKT.get(1)));

        double forventetBrukersAndel1 = forventetFordelt.get(0) - forventetRedusert1;
        double forventetBrukersAndel2 = forventetFordelt.get(1) - forventetRedusert2;


        double fordeltÅrsinntektFL = frilansÅrsinntekt - forventetFlyttetTilArbeidsforhold;
        double forventetBrukersAndelFL = Math.min(fordeltÅrsinntektFL, seksG - (forventetFordelt.stream().mapToDouble(Double::doubleValue).sum()));

        final List<Double> forventetRedusert = List.of(forventetRedusert1, forventetRedusert2);
        final List<Double> forventetRedusertBrukersAndel = List.of(forventetBrukersAndel1, forventetBrukersAndel2);

        List<String> virksomhetene = List.of(ARBEIDSFORHOLD_ORGNR1, ARBEIDSFORHOLD_ORGNR2);
        List<BigDecimal> månedsinntekter = ÅRSINNTEKT.stream().map((v) -> BigDecimal.valueOf(v / 12)).collect(Collectors.toList());

        // Arrange
        var im1 = verdikjedeTestHjelper.opprettInntektsmeldingMedRefusjonskrav(Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), månedsinntekter.get(0),
                BigDecimal.valueOf(refusjonsKrav.get(0) / 12));
        var im2 = verdikjedeTestHjelper.opprettInntektsmeldingMedRefusjonskrav(Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR2), månedsinntekter.get(1),
                BigDecimal.valueOf(refusjonsKrav.get(1) / 12));
        List<InntektsmeldingDto> inntektsmeldinger = List.of(im1, im2);

        InntektArbeidYtelseGrunnlagDto iayGrunnlag = lagIayATogFL(koblingReferanse.getAktørId(), BigDecimal.valueOf(sammenligning / 12),
                månedsinntekter,
                BigDecimal.valueOf(frilansÅrsinntekt / 12),
                virksomhetene,
                inntektsmeldinger
        );

        var opptjeningPeriode = Periode.of(SKJÆRINGSTIDSPUNKT_BEREGNING.minusMonths(10), SKJÆRINGSTIDSPUNKT_BEREGNING.minusDays(1));

        var opptjeningAktiviteter = new OpptjeningAktiviteterDto(
                OpptjeningAktiviteterDto.nyPeriodeOrgnr(OpptjeningAktivitetType.ARBEID, opptjeningPeriode, ARBEIDSFORHOLD_ORGNR1),
                OpptjeningAktiviteterDto.nyPeriodeOrgnr(OpptjeningAktivitetType.ARBEID, opptjeningPeriode, ARBEIDSFORHOLD_ORGNR2),
                OpptjeningAktiviteterDto.nyPeriode(OpptjeningAktivitetType.FRILANS, opptjeningPeriode)
        );

        var input = lagInput(koblingReferanse, opptjeningAktiviteter, iayGrunnlag, 100);

        // Act 1: kontroller fakta om beregning
        BeregningsgrunnlagGrunnlagDto grunnlag = kjørStegOgLagreGrunnlag(lagFastsettBeregningsaktiviteterInput(input));
        input = input.medBeregningsgrunnlagGrunnlag(grunnlag);

        var aksjonspunktResultat = beregningTjenesteWrapper.getAksjonspunktUtlederFaktaOmBeregning().utledAksjonspunkterFor(lagFaktaOmBeregningInput(input), grunnlag, false);

        // Assert 1
        assertThat(aksjonspunktResultat.getBeregningAksjonspunktResultatList()).hasSize(1);
        List<FaktaOmBeregningTilfelle> faktaOmBeregningTilfeller = aksjonspunktResultat.getFaktaOmBeregningTilfeller();
        assertThat(faktaOmBeregningTilfeller).hasSize(1);
        assertThat(faktaOmBeregningTilfeller.get(0)).isEqualTo(FaktaOmBeregningTilfelle.VURDER_MOTTAR_YTELSE);

        // Act 2: foreslå beregningsgrunnlag
        BeregningsgrunnlagRegelResultat resultat = foreslåBeregningsgrunnlag.foreslåBeregningsgrunnlag(lagForeslåBeregningsgrunnlagInput(input));

        // Assert 2
        verdikjedeTestHjelper.verifiserBeregningsgrunnlagBasis(resultat, Hjemmel.F_14_7_8_40);

        BeregningsgrunnlagDto foreslåttBeregningsgrunnlag = resultat.getBeregningsgrunnlag();
        BeregningsgrunnlagPeriodeDto periode = foreslåttBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        verdikjedeTestHjelper.verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, Intervall.TIDENES_ENDE, 3);
        verdikjedeTestHjelper.verifiserBGATførAvkorting(periode, ÅRSINNTEKT, virksomhetene);
        verdikjedeTestHjelper.verifiserFLførAvkorting(periode, frilansÅrsinntekt);
        verdikjedeTestHjelper.verifiserSammenligningsgrunnlag(foreslåttBeregningsgrunnlag.getSammenligningsgrunnlag(),
                sammenligning, SKJÆRINGSTIDSPUNKT_BEREGNING.minusYears(1).withDayOfMonth(1),
                SKJÆRINGSTIDSPUNKT_BEREGNING.withDayOfMonth(1).minusDays(1), BigDecimal.valueOf(37.037037000));

        // Act 3: fordel beregningsgrunnlag
        resultat = vurderBeregningsgrunnlagTjeneste.vurderBeregningsgrunnlag(lagFordelBeregningsgrunnlagInput(input),
                nyttGrunnlag(grunnlag, foreslåttBeregningsgrunnlag, BeregningsgrunnlagTilstand.FORESLÅTT));
        assertThat(resultat.getVilkårOppfylt()).isTrue();
        BeregningsgrunnlagDto fordeltBeregningsgrunnlag = fordelBeregningsgrunnlag(input,
                resultat.getBeregningsgrunnlag(), grunnlag);
        input = input.medBeregningsgrunnlagGrunnlag(BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(grunnlag).medBeregningsgrunnlag(fordeltBeregningsgrunnlag).build(BeregningsgrunnlagTilstand.OPPDATERT_MED_REFUSJON_OG_GRADERING));


        // Act 4: fastsette beregningsgrunnlag
        var fastsattBeregningsgrunnlag = beregningTjenesteWrapper.getFullføreBeregningsgrunnlagTjeneste().fullføreBeregningsgrunnlag(input
        ).getBeregningsgrunnlag();

        // Assert 3
        verdikjedeTestHjelper.verifiserBeregningsgrunnlagBasis(fastsattBeregningsgrunnlag, Hjemmel.F_14_7_8_40);

        periode = fastsattBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        verdikjedeTestHjelper.verifiserBGATetterAvkorting(periode,
                ÅRSINNTEKT, forventetFordelt, virksomhetene, forventetFordelt, forventetRedusert, forventetRedusert, forventetRedusertBrukersAndel, false);
        verdikjedeTestHjelper.verifiserFLetterAvkorting(periode, frilansÅrsinntekt, fordeltÅrsinntektFL, forventetBrukersAndelFL, forventetBrukersAndelFL);
    }

    @Test
    public void treArbeidsforholdOgFrilansMedBgUnder6gOgRefusjonUnder6G() {

        final List<Double> ÅRSINNTEKT = List.of(12 * 13_000d, 12 * 12_000d, 12 * 8_000d);
        final List<Double> refusjonsKrav = List.of(12 * 13_000d, 12 * 9_000d, 12 * 8_000d);
        final Double sammenligning = 12 * 60000d;
        final Double frilansÅrsinntekt = 12 * 13000d;

        double forventetRedusert1 = refusjonsKrav.get(0);
        double forventetRedusert2 = refusjonsKrav.get(1);
        double forventetRedusert3 = refusjonsKrav.get(2);

        double forventetFlyttetTilArbeidsforhold = Math.max(refusjonsKrav.get(0) - ÅRSINNTEKT.get(0), 0L) + Math.max(refusjonsKrav.get(1) - ÅRSINNTEKT.get(1), 0L) + Math.max(refusjonsKrav.get(2) - ÅRSINNTEKT.get(2), 0L);
        List<Double> forventetFordelt = List.of(Math.max(refusjonsKrav.get(0), ÅRSINNTEKT.get(0)), Math.max(refusjonsKrav.get(1), ÅRSINNTEKT.get(1)), Math.max(refusjonsKrav.get(2), ÅRSINNTEKT.get(2)));

        double forventetBrukersAndel1 = forventetFordelt.get(0) - forventetRedusert1;
        double forventetBrukersAndel2 = forventetFordelt.get(1) - forventetRedusert2;
        double forventetBrukersAndel3 = forventetFordelt.get(2) - forventetRedusert3;
        double fordeltÅrsinntektFL = frilansÅrsinntekt - forventetFlyttetTilArbeidsforhold;
        double forventetBrukersAndelFL = Math.min(fordeltÅrsinntektFL, seksG - (forventetFordelt.stream().mapToDouble(Double::doubleValue).sum()));

        final List<Double> forventetRedusert = List.of(forventetRedusert1, forventetRedusert2, forventetRedusert3);
        final List<Double> forventetRedusertBrukersAndel = List.of(forventetBrukersAndel1, forventetBrukersAndel2, forventetBrukersAndel3);

        List<String> virksomhetene = List.of(ARBEIDSFORHOLD_ORGNR1, ARBEIDSFORHOLD_ORGNR2, ARBEIDSFORHOLD_ORGNR3);

        List<BigDecimal> månedsinntekter = ÅRSINNTEKT.stream().map((v) -> BigDecimal.valueOf(v / 12)).collect(Collectors.toList());

        // Arrange
        var im1 = verdikjedeTestHjelper.opprettInntektsmeldingMedRefusjonskrav(Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), månedsinntekter.get(0),
                BigDecimal.valueOf(refusjonsKrav.get(0) / 12));
        var im2 = verdikjedeTestHjelper.opprettInntektsmeldingMedRefusjonskrav(Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR2), månedsinntekter.get(1),
                BigDecimal.valueOf(refusjonsKrav.get(1) / 12));
        var im3 = verdikjedeTestHjelper.opprettInntektsmeldingMedRefusjonskrav(Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR3), månedsinntekter.get(2),
                BigDecimal.valueOf(refusjonsKrav.get(2) / 12));
        List<InntektsmeldingDto> inntektsmeldinger = List.of(im1, im2, im3);

        InntektArbeidYtelseGrunnlagDto iayGrunnlag = lagIayATogFL(koblingReferanse.getAktørId(), BigDecimal.valueOf(sammenligning / 12),
                månedsinntekter,
                BigDecimal.valueOf(frilansÅrsinntekt / 12),
                virksomhetene, inntektsmeldinger);

        var opptjeningPeriode = Periode.of(SKJÆRINGSTIDSPUNKT_BEREGNING.minusMonths(10), SKJÆRINGSTIDSPUNKT_BEREGNING.minusDays(1));

        var opptjeningAktiviteter = new OpptjeningAktiviteterDto(
                OpptjeningAktiviteterDto.nyPeriodeOrgnr(OpptjeningAktivitetType.ARBEID, opptjeningPeriode, ARBEIDSFORHOLD_ORGNR1),
                OpptjeningAktiviteterDto.nyPeriodeOrgnr(OpptjeningAktivitetType.ARBEID, opptjeningPeriode, ARBEIDSFORHOLD_ORGNR2),
                OpptjeningAktiviteterDto.nyPeriodeOrgnr(OpptjeningAktivitetType.ARBEID, opptjeningPeriode, ARBEIDSFORHOLD_ORGNR3),
                OpptjeningAktiviteterDto.nyPeriode(OpptjeningAktivitetType.FRILANS, opptjeningPeriode)
        );

        var input = lagInput(koblingReferanse, opptjeningAktiviteter, iayGrunnlag, 100);

        // Act 1: kontroller fakta om beregning
        BeregningsgrunnlagGrunnlagDto grunnlag = kjørStegOgLagreGrunnlag(lagFastsettBeregningsaktiviteterInput(input));
        input = input.medBeregningsgrunnlagGrunnlag(grunnlag);
        var aksjonspunktResultat = beregningTjenesteWrapper.getAksjonspunktUtlederFaktaOmBeregning().utledAksjonspunkterFor(lagFaktaOmBeregningInput(input), grunnlag, false);

        // Assert 1
        assertThat(aksjonspunktResultat.getBeregningAksjonspunktResultatList()).hasSize(1);
        List<FaktaOmBeregningTilfelle> faktaOmBeregningTilfeller = aksjonspunktResultat.getFaktaOmBeregningTilfeller();
        assertThat(faktaOmBeregningTilfeller).hasSize(1);
        assertThat(faktaOmBeregningTilfeller.get(0)).isEqualTo(FaktaOmBeregningTilfelle.VURDER_MOTTAR_YTELSE);

        // Act 2: foreslå beregningsgrunnlag
        BeregningsgrunnlagRegelResultat resultat = foreslåBeregningsgrunnlag.foreslåBeregningsgrunnlag(lagForeslåBeregningsgrunnlagInput(input));

        // Assert 2
        verdikjedeTestHjelper.verifiserBeregningsgrunnlagBasis(resultat, Hjemmel.F_14_7_8_40);

        BeregningsgrunnlagDto foreslåttBeregningsgrunnlag = resultat.getBeregningsgrunnlag();
        BeregningsgrunnlagPeriodeDto periode = foreslåttBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        verdikjedeTestHjelper.verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, Intervall.TIDENES_ENDE, 4);
        verdikjedeTestHjelper.verifiserBGATførAvkorting(periode, ÅRSINNTEKT, virksomhetene);
        verdikjedeTestHjelper.verifiserFLførAvkorting(periode, frilansÅrsinntekt);
        verdikjedeTestHjelper.verifiserSammenligningsgrunnlag(foreslåttBeregningsgrunnlag.getSammenligningsgrunnlag(),
                sammenligning, SKJÆRINGSTIDSPUNKT_BEREGNING.minusYears(1).withDayOfMonth(1),
                SKJÆRINGSTIDSPUNKT_BEREGNING.withDayOfMonth(1).minusDays(1), BigDecimal.valueOf(233.333333300));

        // Act 3: fordel beregningsgrunnlag
        resultat = vurderBeregningsgrunnlagTjeneste.vurderBeregningsgrunnlag(lagFordelBeregningsgrunnlagInput(input),
                nyttGrunnlag(grunnlag, foreslåttBeregningsgrunnlag, BeregningsgrunnlagTilstand.FORESLÅTT));
        assertThat(resultat.getVilkårOppfylt()).isTrue();
        BeregningsgrunnlagDto fordeltBeregningsgrunnlag = fordelBeregningsgrunnlag(input,
                resultat.getBeregningsgrunnlag(), grunnlag);
        input = input.medBeregningsgrunnlagGrunnlag(BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(grunnlag).medBeregningsgrunnlag(fordeltBeregningsgrunnlag).build(BeregningsgrunnlagTilstand.OPPDATERT_MED_REFUSJON_OG_GRADERING));

        // Act 4: fastsette beregningsgrunnlag
        var fastsattBeregningsgrunnlag = beregningTjenesteWrapper.getFullføreBeregningsgrunnlagTjeneste().fullføreBeregningsgrunnlag(input
        ).getBeregningsgrunnlag();

        // Assert 3
        verdikjedeTestHjelper.verifiserBeregningsgrunnlagBasis(fastsattBeregningsgrunnlag, Hjemmel.F_14_7_8_40);

        periode = fastsattBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        verdikjedeTestHjelper.verifiserBGATetterAvkorting(periode,
                ÅRSINNTEKT, forventetFordelt, virksomhetene, forventetFordelt, forventetRedusert, forventetRedusert, forventetRedusertBrukersAndel, false);
        verdikjedeTestHjelper.verifiserFLetterAvkorting(periode, frilansÅrsinntekt, fordeltÅrsinntektFL, fordeltÅrsinntektFL, forventetBrukersAndelFL);
    }

    @Test
    public void treArbeidsforholdOgFrilansMedBgOver6gOgRefusjonUnder6G() {

        final List<Double> ÅRSINNTEKT = List.of(12 * 13_000d, 12 * 12_000d, 12 * 22_500d);
        final List<Double> refusjonsKrav = List.of(12 * 13_000d, 12 * 9_000d, 12 * 17_781d);
        final double sammenligning = 12 * 61_000d;
        final double frilansÅrsinntekt = 12 * 14_000d;

        double forventetRedusert1 = refusjonsKrav.get(0);
        double forventetRedusert2 = refusjonsKrav.get(1);
        double forventetRedusert3 = refusjonsKrav.get(2);

        double forventetFlyttetTilArbeidsforhold = Math.max(refusjonsKrav.get(0) - ÅRSINNTEKT.get(0), 0L) + Math.max(refusjonsKrav.get(1) - ÅRSINNTEKT.get(1), 0L) + Math.max(refusjonsKrav.get(2) - ÅRSINNTEKT.get(2), 0L);
        double forventetFordeltFL = frilansÅrsinntekt - forventetFlyttetTilArbeidsforhold;
        List<Double> forventetFordelt = List.of(Math.max(refusjonsKrav.get(0), ÅRSINNTEKT.get(0)), Math.max(refusjonsKrav.get(1), ÅRSINNTEKT.get(1)), Math.max(refusjonsKrav.get(2), ÅRSINNTEKT.get(2)));

        double forventetBrukersAndel1 = 0;
        double andel = forventetFordelt.get(1) / (forventetFordelt.get(1) + forventetFordelt.get(2));
        double forventetBrukersAndel2 = (seksG - forventetRedusert1) * andel - forventetRedusert2;
        double forventetBrukersAndel3 = seksG - forventetRedusert1 - forventetBrukersAndel2 - forventetRedusert2 - forventetRedusert3;
        double forventetBrukersAndelFL = 0;

        final List<Double> avkortetBG = List.of(forventetBrukersAndel1 + forventetRedusert1, forventetBrukersAndel2 + forventetRedusert2,
                forventetBrukersAndel3 + forventetRedusert3);

        final List<Double> forventetRedusert = List.of(forventetRedusert1, forventetRedusert2, forventetRedusert3);
        final List<Double> forventetRedusertBrukersAndel = List.of(forventetBrukersAndel1, forventetBrukersAndel2, forventetBrukersAndel3);

        List<BigDecimal> månedsinntekter = ÅRSINNTEKT.stream().map((v) -> BigDecimal.valueOf(v / 12)).collect(Collectors.toList());

        List<String> virksomhetene = List.of(ARBEIDSFORHOLD_ORGNR1, ARBEIDSFORHOLD_ORGNR2, ARBEIDSFORHOLD_ORGNR3);

        // Arrange
        var im1 = verdikjedeTestHjelper.opprettInntektsmeldingMedRefusjonskrav(Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), månedsinntekter.get(0),
                BigDecimal.valueOf(refusjonsKrav.get(0) / 12));
        var im2 = verdikjedeTestHjelper.opprettInntektsmeldingMedRefusjonskrav(Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR2), månedsinntekter.get(1),
                BigDecimal.valueOf(refusjonsKrav.get(1) / 12));
        var im3 = verdikjedeTestHjelper.opprettInntektsmeldingMedRefusjonskrav(Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR3), månedsinntekter.get(2),
                BigDecimal.valueOf(refusjonsKrav.get(2) / 12));
        List<InntektsmeldingDto> inntektsmeldinger = List.of(im1, im2, im3);

        InntektArbeidYtelseGrunnlagDto iayGrunnlag = lagIayATogFL(koblingReferanse.getAktørId(), BigDecimal.valueOf(sammenligning / 12),
                månedsinntekter,
                BigDecimal.valueOf(frilansÅrsinntekt / 12),
                virksomhetene, inntektsmeldinger);

        var opptjeningPeriode = Periode.of(SKJÆRINGSTIDSPUNKT_BEREGNING.minusMonths(10), SKJÆRINGSTIDSPUNKT_BEREGNING.minusDays(1));

        var opptjeningAktiviteter = new OpptjeningAktiviteterDto(
                OpptjeningAktiviteterDto.nyPeriodeOrgnr(OpptjeningAktivitetType.ARBEID, opptjeningPeriode, ARBEIDSFORHOLD_ORGNR1),
                OpptjeningAktiviteterDto.nyPeriodeOrgnr(OpptjeningAktivitetType.ARBEID, opptjeningPeriode, ARBEIDSFORHOLD_ORGNR2),
                OpptjeningAktiviteterDto.nyPeriodeOrgnr(OpptjeningAktivitetType.ARBEID, opptjeningPeriode, ARBEIDSFORHOLD_ORGNR3),
                OpptjeningAktiviteterDto.nyPeriode(OpptjeningAktivitetType.FRILANS, opptjeningPeriode)
        );

        var input = lagInput(koblingReferanse, opptjeningAktiviteter, iayGrunnlag, 100);

        // Act 1: kontroller fakta om beregning
        BeregningsgrunnlagGrunnlagDto grunnlag = kjørStegOgLagreGrunnlag(lagFastsettBeregningsaktiviteterInput(input));
        input = input.medBeregningsgrunnlagGrunnlag(grunnlag);

        var aksjonspunktResultat = beregningTjenesteWrapper.getAksjonspunktUtlederFaktaOmBeregning().utledAksjonspunkterFor(lagFaktaOmBeregningInput(input), grunnlag, false);

        // Assert 1
        assertThat(aksjonspunktResultat.getBeregningAksjonspunktResultatList()).hasSize(1);
        List<FaktaOmBeregningTilfelle> faktaOmBeregningTilfeller = aksjonspunktResultat.getFaktaOmBeregningTilfeller();
        assertThat(faktaOmBeregningTilfeller).hasSize(1);
        assertThat(faktaOmBeregningTilfeller.get(0)).isEqualTo(FaktaOmBeregningTilfelle.VURDER_MOTTAR_YTELSE);

        // Act 2: foreslå beregningsgrunnlag
        BeregningsgrunnlagRegelResultat resultat = foreslåBeregningsgrunnlag.foreslåBeregningsgrunnlag(lagForeslåBeregningsgrunnlagInput(input));
        input = input.medBeregningsgrunnlagGrunnlag(grunnlag);

        // Assert 2
        verdikjedeTestHjelper.verifiserBeregningsgrunnlagBasis(resultat, Hjemmel.F_14_7_8_40);

        BeregningsgrunnlagDto foreslåttBeregningsgrunnlag = resultat.getBeregningsgrunnlag();
        BeregningsgrunnlagPeriodeDto periode = foreslåttBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        verdikjedeTestHjelper.verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, Intervall.TIDENES_ENDE, 4);
        verdikjedeTestHjelper.verifiserBGATførAvkorting(periode, ÅRSINNTEKT, virksomhetene);
        verdikjedeTestHjelper.verifiserFLførAvkorting(periode, frilansÅrsinntekt);
        verdikjedeTestHjelper.verifiserSammenligningsgrunnlag(foreslåttBeregningsgrunnlag.getSammenligningsgrunnlag(),
                sammenligning, SKJÆRINGSTIDSPUNKT_BEREGNING.minusYears(1).withDayOfMonth(1),
                SKJÆRINGSTIDSPUNKT_BEREGNING.withDayOfMonth(1).minusDays(1), BigDecimal.valueOf(8.196721300));

        // Act 3: fordel beregningsgrunnlag
        resultat = vurderBeregningsgrunnlagTjeneste.vurderBeregningsgrunnlag(lagFordelBeregningsgrunnlagInput(input),
                nyttGrunnlag(grunnlag, foreslåttBeregningsgrunnlag, BeregningsgrunnlagTilstand.FORESLÅTT));
        assertThat(resultat.getVilkårOppfylt()).isTrue();
        BeregningsgrunnlagDto fordeltBeregningsgrunnlag = fordelBeregningsgrunnlag(input,
                resultat.getBeregningsgrunnlag(), grunnlag);

        // Act 4: fastsette beregningsgrunnlag
        var fordeltGrunnlag = nyttGrunnlag(grunnlag, fordeltBeregningsgrunnlag, BeregningsgrunnlagTilstand.OPPDATERT_MED_REFUSJON_OG_GRADERING);
        input = input.medBeregningsgrunnlagGrunnlag(fordeltGrunnlag);
        var fastsattBeregningsgrunnlag = beregningTjenesteWrapper.getFullføreBeregningsgrunnlagTjeneste().fullføreBeregningsgrunnlag(input
        ).getBeregningsgrunnlag();

        // Assert 3
        verdikjedeTestHjelper.verifiserBeregningsgrunnlagBasis(fastsattBeregningsgrunnlag, Hjemmel.F_14_7_8_40);

        periode = fastsattBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        verdikjedeTestHjelper.verifiserBGATetterAvkorting(periode,
                ÅRSINNTEKT, forventetFordelt, virksomhetene, avkortetBG, forventetRedusert, forventetRedusert, forventetRedusertBrukersAndel, false);
        verdikjedeTestHjelper.verifiserFLetterAvkorting(periode, frilansÅrsinntekt, forventetFordeltFL, 0.0d, forventetBrukersAndelFL);
    }

    @Test
    public void bareFrilansMedBgUnder6g() {

        // Arrange
        final Double sammenligning = 12 * 14000d;
        final Double frilansÅrsinntekt = 12 * 14000d;

        double forventetBrukersAndelFL = frilansÅrsinntekt;

        InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder = lagBehandlingFL(koblingReferanse.getAktørId(),
                BigDecimal.valueOf(sammenligning / 12),
                BigDecimal.valueOf(frilansÅrsinntekt / 12),
                ARBEIDSFORHOLD_ORGNR1);


        var opptjeningPeriode = Periode.of(SKJÆRINGSTIDSPUNKT_BEREGNING.minusMonths(10), SKJÆRINGSTIDSPUNKT_BEREGNING.minusDays(1));
        var opptjeningAktiviteter = new OpptjeningAktiviteterDto(
                OpptjeningAktiviteterDto.nyPeriode(OpptjeningAktivitetType.FRILANS, opptjeningPeriode)
        );

        var iayGrunnlag = iayGrunnlagBuilder.build();
        var input = lagInput(koblingReferanse, opptjeningAktiviteter, iayGrunnlag, 100);

        // Act 1: kontroller fakta om beregning
        BeregningsgrunnlagGrunnlagDto grunnlag = kjørStegOgLagreGrunnlag(lagFastsettBeregningsaktiviteterInput(input));
        input = input.medBeregningsgrunnlagGrunnlag(grunnlag);
        var aksjonspunktResultat = beregningTjenesteWrapper.getAksjonspunktUtlederFaktaOmBeregning().utledAksjonspunkterFor(lagFaktaOmBeregningInput(input), grunnlag, false);

        // Assert 1
        assertThat(aksjonspunktResultat.getBeregningAksjonspunktResultatList()).hasSize(1);
        List<FaktaOmBeregningTilfelle> faktaOmBeregningTilfeller = aksjonspunktResultat.getFaktaOmBeregningTilfeller();
        assertThat(faktaOmBeregningTilfeller).hasSize(1);
        assertThat(faktaOmBeregningTilfeller.get(0)).isEqualTo(FaktaOmBeregningTilfelle.VURDER_MOTTAR_YTELSE);

        // Act 2: foreslå beregningsgrunnlag
        BeregningsgrunnlagRegelResultat resultat = foreslåBeregningsgrunnlag.foreslåBeregningsgrunnlag(lagForeslåBeregningsgrunnlagInput(input));

        // Assert 2
        verdikjedeTestHjelper.verifiserBeregningsgrunnlagBasis(resultat, Hjemmel.F_14_7_8_38);

        BeregningsgrunnlagDto foreslåttBeregningsgrunnlag = resultat.getBeregningsgrunnlag();
        BeregningsgrunnlagPeriodeDto periode = foreslåttBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        verdikjedeTestHjelper.verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, Intervall.TIDENES_ENDE, 1);
        verdikjedeTestHjelper.verifiserFLførAvkorting(periode, frilansÅrsinntekt);
        verdikjedeTestHjelper.verifiserSammenligningsgrunnlag(foreslåttBeregningsgrunnlag.getSammenligningsgrunnlag(),
                sammenligning, SKJÆRINGSTIDSPUNKT_BEREGNING.minusYears(1).withDayOfMonth(1),
                SKJÆRINGSTIDSPUNKT_BEREGNING.withDayOfMonth(1).minusDays(1), BigDecimal.ZERO);

        // Act 3: fordel beregningsgrunnlag
        resultat = vurderBeregningsgrunnlagTjeneste.vurderBeregningsgrunnlag(lagFordelBeregningsgrunnlagInput(input),
                nyttGrunnlag(grunnlag, foreslåttBeregningsgrunnlag, BeregningsgrunnlagTilstand.FORESLÅTT));
        assertThat(resultat.getVilkårOppfylt()).isTrue();
        BeregningsgrunnlagDto fordeltBeregningsgrunnlag = fordelBeregningsgrunnlag(input,
                resultat.getBeregningsgrunnlag(), grunnlag);
        input = input.medBeregningsgrunnlagGrunnlag(BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(grunnlag).medBeregningsgrunnlag(fordeltBeregningsgrunnlag).build(BeregningsgrunnlagTilstand.OPPDATERT_MED_REFUSJON_OG_GRADERING));

        // Act 4: fastsette beregningsgrunnlag
        var fastsattBeregningsgrunnlag = beregningTjenesteWrapper.getFullføreBeregningsgrunnlagTjeneste().fullføreBeregningsgrunnlag(input
        ).getBeregningsgrunnlag();

        // Assert 3
        verdikjedeTestHjelper.verifiserBeregningsgrunnlagBasis(fastsattBeregningsgrunnlag, Hjemmel.F_14_7_8_38);

        periode = fastsattBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        verdikjedeTestHjelper.verifiserFLetterAvkorting(periode, frilansÅrsinntekt, frilansÅrsinntekt, frilansÅrsinntekt, forventetBrukersAndelFL);
    }

    @Test
    public void bareFrilansMedBgOver6g() {

        final double sammenligning = 12 * 70000d;
        final double frilansÅrsinntekt = 12 * 70000d;

        double forventetBrukersAndelFL = Math.min(seksG, frilansÅrsinntekt);

        InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder = lagBehandlingFL(koblingReferanse.getAktørId(),
                BigDecimal.valueOf(sammenligning / 12),
                BigDecimal.valueOf(frilansÅrsinntekt / 12),
                ARBEIDSFORHOLD_ORGNR1);

        // Arrange
        var opptjeningPeriode = Periode.of(SKJÆRINGSTIDSPUNKT_BEREGNING.minusMonths(10), SKJÆRINGSTIDSPUNKT_BEREGNING.minusDays(1));
        var opptjeningAktiviteter = new OpptjeningAktiviteterDto(
                OpptjeningAktiviteterDto.nyPeriode(OpptjeningAktivitetType.FRILANS, opptjeningPeriode)
        );

        var iayGrunnlag = iayGrunnlagBuilder.build();
        var input = lagInput(koblingReferanse, opptjeningAktiviteter, iayGrunnlag, 100);

        // Act 1: kontroller fakta om beregning
        BeregningsgrunnlagGrunnlagDto grunnlag = kjørStegOgLagreGrunnlag(lagFastsettBeregningsaktiviteterInput(input));
        input = input.medBeregningsgrunnlagGrunnlag(grunnlag);
        var aksjonspunktResultat = beregningTjenesteWrapper.getAksjonspunktUtlederFaktaOmBeregning().utledAksjonspunkterFor(lagFaktaOmBeregningInput(input), grunnlag, false);

        // Assert 1
        assertThat(aksjonspunktResultat.getBeregningAksjonspunktResultatList()).hasSize(1);
        List<FaktaOmBeregningTilfelle> faktaOmBeregningTilfeller = aksjonspunktResultat.getFaktaOmBeregningTilfeller();
        assertThat(faktaOmBeregningTilfeller).hasSize(1);
        assertThat(faktaOmBeregningTilfeller.get(0)).isEqualTo(FaktaOmBeregningTilfelle.VURDER_MOTTAR_YTELSE);

        // Act 2: foreslå beregningsgrunnlag
        BeregningsgrunnlagRegelResultat resultat = foreslåBeregningsgrunnlag.foreslåBeregningsgrunnlag(lagForeslåBeregningsgrunnlagInput(input));

        // Assert 2
        verdikjedeTestHjelper.verifiserBeregningsgrunnlagBasis(resultat, Hjemmel.F_14_7_8_38);

        BeregningsgrunnlagDto foreslåttBeregningsgrunnlag = resultat.getBeregningsgrunnlag();
        BeregningsgrunnlagPeriodeDto periode = foreslåttBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        verdikjedeTestHjelper.verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, Intervall.TIDENES_ENDE, 1);
        verdikjedeTestHjelper.verifiserFLførAvkorting(periode, frilansÅrsinntekt);
        verdikjedeTestHjelper.verifiserSammenligningsgrunnlag(foreslåttBeregningsgrunnlag.getSammenligningsgrunnlag(),
                sammenligning, SKJÆRINGSTIDSPUNKT_BEREGNING.minusYears(1).withDayOfMonth(1),
                SKJÆRINGSTIDSPUNKT_BEREGNING.withDayOfMonth(1).minusDays(1), BigDecimal.ZERO);

        // Act 3: fordel beregningsgrunnlag
        resultat = vurderBeregningsgrunnlagTjeneste.vurderBeregningsgrunnlag(lagFordelBeregningsgrunnlagInput(input),
                nyttGrunnlag(grunnlag, foreslåttBeregningsgrunnlag, BeregningsgrunnlagTilstand.FORESLÅTT));
        assertThat(resultat.getVilkårOppfylt()).isTrue();
        BeregningsgrunnlagDto fordeltBeregningsgrunnlag = fordelBeregningsgrunnlag(input,
                resultat.getBeregningsgrunnlag(), grunnlag);
        input = input.medBeregningsgrunnlagGrunnlag(BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(grunnlag).medBeregningsgrunnlag(fordeltBeregningsgrunnlag).build(BeregningsgrunnlagTilstand.OPPDATERT_MED_REFUSJON_OG_GRADERING));

        // Act 4: fastsette beregningsgrunnlag
        var fastsattBeregningsgrunnlag = beregningTjenesteWrapper.getFullføreBeregningsgrunnlagTjeneste().fullføreBeregningsgrunnlag(input
        ).getBeregningsgrunnlag();

        // Assert 3
        verdikjedeTestHjelper.verifiserBeregningsgrunnlagBasis(fastsattBeregningsgrunnlag, Hjemmel.F_14_7_8_38);

        periode = fastsattBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        verdikjedeTestHjelper.verifiserFLetterAvkorting(periode, frilansÅrsinntekt, frilansÅrsinntekt, seksG, forventetBrukersAndelFL);
    }

    private BeregningsgrunnlagGrunnlagDto kjørStegOgLagreGrunnlag(FastsettBeregningsaktiviteterInput input) {
        return verdikjedeTestHjelper.kjørStegOgLagreGrunnlag(input, beregningTjenesteWrapper);
    }

    private BeregningsgrunnlagDto fordelBeregningsgrunnlag(BeregningsgrunnlagInput input,
                                                           BeregningsgrunnlagDto beregningsgrunnlag,
                                                           BeregningsgrunnlagGrunnlagDto grunnlag) {
        return beregningTjenesteWrapper.getFordelBeregningsgrunnlagTjeneste().fordelBeregningsgrunnlag(input.medBeregningsgrunnlagGrunnlag(grunnlag), beregningsgrunnlag).getBeregningsgrunnlag();
    }

    private InntektArbeidYtelseGrunnlagDto lagIayATogFL(AktørId aktørId,
                                                        BigDecimal inntektSammenligningsgrunnlag,
                                                        List<BigDecimal> inntektBeregningsgrunnlag,
                                                        BigDecimal inntektFrilans,
                                                        List<String> virksomhetOrgnr, List<InntektsmeldingDto> inntektsmeldinger) {
        LocalDate fraOgMed = MINUS_YEARS_1;
        LocalDate tilOgMed = fraOgMed.plusYears(1);

        InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);

        verdikjedeTestHjelper.lagAktørArbeid(inntektArbeidYtelseBuilder, aktørId, Arbeidsgiver.virksomhet(DUMMY_ORGNR),
                fraOgMed, tilOgMed, ArbeidType.FRILANSER_OPPDRAGSTAKER_MED_MER);

        virksomhetOrgnr
                .forEach(virksomhetEntitet -> verdikjedeTestHjelper.lagAktørArbeid(inntektArbeidYtelseBuilder, aktørId, Arbeidsgiver.virksomhet(virksomhetEntitet),
                        fraOgMed, tilOgMed, ArbeidType.ORDINÆRT_ARBEIDSFORHOLD));

        for (LocalDate dt = fraOgMed; dt.isBefore(tilOgMed); dt = dt.plusMonths(1)) {
            for (int i = 0; i < virksomhetOrgnr.size(); i++) {
                verdikjedeTestHjelper.lagInntektForArbeidsforhold(inntektArbeidYtelseBuilder,
                        aktørId,
                        dt, dt.plusMonths(1), inntektBeregningsgrunnlag.get(i),
                        Arbeidsgiver.virksomhet(virksomhetOrgnr.get(i)));
            }
            verdikjedeTestHjelper.lagInntektForSammenligning(inntektArbeidYtelseBuilder, aktørId, dt, dt.plusMonths(1), inntektSammenligningsgrunnlag,
                    Arbeidsgiver.virksomhet(DUMMY_ORGNR));
            verdikjedeTestHjelper.lagInntektForArbeidsforhold(inntektArbeidYtelseBuilder, aktørId, dt, dt.plusMonths(1), inntektFrilans,
                    Arbeidsgiver.virksomhet(DUMMY_ORGNR));
            verdikjedeTestHjelper.lagInntektForOpptjening(inntektArbeidYtelseBuilder, aktørId, dt, dt.plusMonths(1), inntektFrilans,
                    DUMMY_ORGNR);
        }

        return InntektArbeidYtelseGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medInntektsmeldinger(inntektsmeldinger)
                .medData(inntektArbeidYtelseBuilder).build();
    }

    private InntektArbeidYtelseGrunnlagDtoBuilder lagBehandlingFL(AktørId aktørId,
                                                                  BigDecimal inntektSammenligningsgrunnlag,
                                                                  BigDecimal inntektFrilans, String beregningVirksomhet) {
        LocalDate fraOgMed = MINUS_YEARS_1.withDayOfMonth(1);
        LocalDate tilOgMed = fraOgMed.plusYears(1);
        InntektArbeidYtelseAggregatBuilder iayAggregatBuilder = verdikjedeTestHjelper.initBehandlingFL(inntektSammenligningsgrunnlag, inntektFrilans, beregningVirksomhet, fraOgMed, tilOgMed, aktørId, InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER));
        return InntektArbeidYtelseGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medData(iayAggregatBuilder);
    }

    private BeregningsgrunnlagInput lagInput(KoblingReferanse ref, OpptjeningAktiviteterDto opptjeningAktiviteter, InntektArbeidYtelseGrunnlagDto iayGrunnlag, int dekningsgrad) {
        return BeregningsgrunnlagInputTestUtil.lagInputMedIAYOgOpptjeningsaktiviteter(ref, opptjeningAktiviteter, iayGrunnlag, dekningsgrad, 2);
    }

    private StegProsesseringInput lagStegInput(BeregningsgrunnlagTilstand tilstand, BeregningsgrunnlagInput beregningsgrunnlagInput) {
        return new StegProsesseringInput(beregningsgrunnlagInput, tilstand);
    }

    private FaktaOmBeregningInput lagFaktaOmBeregningInput(BeregningsgrunnlagInput input) {
        return new FaktaOmBeregningInput(
                lagStegInput(BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER, input))
                .medGrunnbeløpsatser(GrunnbeløpMock.GRUNNBELØPSATSER);
    }

    private FastsettBeregningsaktiviteterInput lagFastsettBeregningsaktiviteterInput(BeregningsgrunnlagInput input) {
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
