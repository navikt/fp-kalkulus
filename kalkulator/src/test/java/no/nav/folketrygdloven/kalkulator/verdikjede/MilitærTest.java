package no.nav.folketrygdloven.kalkulator.verdikjede;

import static no.nav.folketrygdloven.kalkulator.verdikjede.BeregningsgrunnlagGrunnlagTestUtil.nyttGrunnlag;
import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.BeregningsgrunnlagInputTestUtil;
import no.nav.folketrygdloven.kalkulator.GrunnbeløpMock;
import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapBeregningsgrunnlagFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapInntektsgrunnlagVLTilRegel;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapInntektsgrunnlagVLTilRegelFelles;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.YtelsesspesifikkRegelMapper;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.ytelse.ForeldrepengerGrunnlagMapper;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.FaktaOmBeregningInput;
import no.nav.folketrygdloven.kalkulator.input.FastsettBeregningsaktiviteterInput;
import no.nav.folketrygdloven.kalkulator.input.ForeslåBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.StegProsesseringInput;
import no.nav.folketrygdloven.kalkulator.input.VurderRefusjonBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktiviteterDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.vilkår.VurderBeregningsgrunnlagTjeneste;
import no.nav.folketrygdloven.kalkulator.steg.foreslå.ForeslåBeregningsgrunnlag;
import no.nav.folketrygdloven.kalkulator.testutilities.BeregningIAYTestUtil;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.Hjemmel;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.utils.UnitTestLookupInstanceImpl;

public class MilitærTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT_BEREGNING = VerdikjedeTestHjelper.SKJÆRINGSTIDSPUNKT_OPPTJENING;

    private KoblingReferanse koblingReferanse = new KoblingReferanseMock(SKJÆRINGSTIDSPUNKT_BEREGNING);

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

    @Test
    public void militærSettesTil3G() {
        // Arrange
        var opptjeningPeriode = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING.minusYears(1), SKJÆRINGSTIDSPUNKT_BEREGNING.minusDays(1));
        var opptjeningAktiviteter = OpptjeningAktiviteterDto.fra(OpptjeningAktivitetType.MILITÆR_ELLER_SIVILTJENESTE, opptjeningPeriode);

        InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder = byggMilitærForBehandling(SKJÆRINGSTIDSPUNKT_BEREGNING.minusMonths(10), SKJÆRINGSTIDSPUNKT_BEREGNING.minusDays(1));
        var iayGrunnlag = iayGrunnlagBuilder.build();
        var input = lagInput(koblingReferanse, opptjeningAktiviteter, iayGrunnlag, 100, 3);

        // Act 1: Fastsett beregningaktiviteter og kontroller fakta beregning
        BeregningsgrunnlagGrunnlagDto grunnlag = kjørStegOgLagreGrunnlag(lagFastsettBeregningsaktiviteteterInput(input));
        input = input.medBeregningsgrunnlagGrunnlag(grunnlag);
        var avklaringsbehovResultat = beregningTjenesteWrapper.getAvklaringsbehovUtlederFaktaOmBeregning().utledAvklaringsbehovFor(lagFaktaOmBeregningInput(input),
            grunnlag, false);

        // Assert 1
        assertThat(avklaringsbehovResultat.getBeregningAvklaringsbehovResultatList()).isEmpty();

        // Act 2: foreslå beregningsgrunnlag
        BeregningsgrunnlagRegelResultat resultat = foreslåBeregningsgrunnlag.foreslåBeregningsgrunnlag(lagForeslåBeregningsgrunnlagInput(input));

        // Assert 2
        verdikjedeTestHjelper.verifiserBeregningsgrunnlagBasis(resultat, Hjemmel.F_14_7);

        BeregningsgrunnlagDto foreslåttBeregningsgrunnlag = resultat.getBeregningsgrunnlag();
        BeregningsgrunnlagPeriodeDto periode = foreslåttBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        verdikjedeTestHjelper.verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, TIDENES_ENDE, 1);

        // Act 3: Vurder vilkår og fastsett refusjon
        resultat = vurderBeregningsgrunnlagTjeneste.vurderBeregningsgrunnlag(lagVurderRefusjonBeregningsgrunnlagInput(input),
                nyttGrunnlag(grunnlag, foreslåttBeregningsgrunnlag, BeregningsgrunnlagTilstand.FORESLÅTT));
        assertThat(resultat.getVilkårOppfylt()).isTrue();
        assertThat(resultat.getAvklaringsbehov()).isEmpty();
        BeregningsgrunnlagDto vurderRefusjonGrunnlag = vurderRefusjonBeregningsgrunnlag(input, grunnlag, resultat);
        input = input.medBeregningsgrunnlagGrunnlag(BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(grunnlag).medBeregningsgrunnlag(vurderRefusjonGrunnlag).build(BeregningsgrunnlagTilstand.VURDERT_REFUSJON));

        // Act 3.5 Fordel beregningsgrunnlag
        BeregningsgrunnlagDto fordeltBeregningsgrunnlag = fordelBeregningsgrunnlag(input);
        input = input.medBeregningsgrunnlagGrunnlag(BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(grunnlag).medBeregningsgrunnlag(fordeltBeregningsgrunnlag).build(BeregningsgrunnlagTilstand.OPPDATERT_MED_REFUSJON_OG_GRADERING));

        // Act 4: fastsette beregningsgrunnlag
        var fastsattBeregningsgrunnlag = beregningTjenesteWrapper.getFullføreBeregningsgrunnlagTjeneste().fullføreBeregningsgrunnlag(input
        ).getBeregningsgrunnlag();

        // Assert 3
        verdikjedeTestHjelper.verifiserBeregningsgrunnlagBasis(fastsattBeregningsgrunnlag, Hjemmel.F_14_7);

        periode = fastsattBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        BigDecimal treG = BigDecimal.valueOf(3 * 93634);
        assertThat(periode.getBeregnetPrÅr()).isEqualByComparingTo(treG);
        assertThat(periode.getRedusertPrÅr()).isEqualByComparingTo(treG);
        assertThat(periode.getDagsats()).isEqualTo(1080);
    }

    private BeregningsgrunnlagDto vurderRefusjonBeregningsgrunnlag(BeregningsgrunnlagInput input, BeregningsgrunnlagGrunnlagDto grunnlag,
                                                                   BeregningsgrunnlagRegelResultat resultat) {
        return beregningTjenesteWrapper.getVurderRefusjonBeregningsgrunnlagtjeneste().vurderRefusjon(lagVurderRefusjonBeregningsgrunnlagInput(input.medBeregningsgrunnlagGrunnlag(grunnlag)),
                resultat.getBeregningsgrunnlag()).getBeregningsgrunnlag();
    }


    private BeregningsgrunnlagGrunnlagDto kjørStegOgLagreGrunnlag(FastsettBeregningsaktiviteterInput input) {
        return verdikjedeTestHjelper.kjørStegOgLagreGrunnlag(input, beregningTjenesteWrapper);
    }

    private BeregningsgrunnlagDto fordelBeregningsgrunnlag(BeregningsgrunnlagInput input) {
        return beregningTjenesteWrapper.getFordelBeregningsgrunnlagTjeneste().omfordelBeregningsgrunnlag(input).getBeregningsgrunnlag();
    }

    private InntektArbeidYtelseGrunnlagDtoBuilder byggMilitærForBehandling(LocalDate fom, LocalDate tom) {
        return InntektArbeidYtelseGrunnlagDtoBuilder.oppdatere(Optional.empty())
            .medOppgittOpptjening(BeregningIAYTestUtil.lagAnnenAktivitetOppgittOpptjening(ArbeidType.MILITÆR_ELLER_SIVILTJENESTE, fom, tom));
    }

    private BeregningsgrunnlagInput lagInput(KoblingReferanse ref, OpptjeningAktiviteterDto opptjeningAktiviteter, InntektArbeidYtelseGrunnlagDto iayGrunnlag, int dekningsgrad, int grunnbeløpMilitærHarKravPå) {
        return BeregningsgrunnlagInputTestUtil.lagInputMedIAYOgOpptjeningsaktiviteter(ref, opptjeningAktiviteter, iayGrunnlag, dekningsgrad, grunnbeløpMilitærHarKravPå);
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

    private VurderRefusjonBeregningsgrunnlagInput lagVurderRefusjonBeregningsgrunnlagInput(BeregningsgrunnlagInput input) {
        return new VurderRefusjonBeregningsgrunnlagInput(lagStegInput(BeregningsgrunnlagTilstand.VURDERT_REFUSJON, input))
                .medUregulertGrunnbeløp(BigDecimal.valueOf(GrunnbeløpMock.finnGrunnbeløp(SKJÆRINGSTIDSPUNKT_BEREGNING)));
    }

}
