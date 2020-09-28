package no.nav.folketrygdloven.kalkulator.verdikjede;

import static no.nav.folketrygdloven.kalkulator.verdikjede.BeregningsgrunnlagGrunnlagTestUtil.nyttGrunnlag;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.BeregningsgrunnlagInputTestUtil;
import no.nav.folketrygdloven.kalkulator.steg.foreslå.ForeslåBeregningsgrunnlag;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.vilkår.VurderBeregningsgrunnlagTjeneste;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.ForeldrepengerGrunnlagMapper;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapBeregningsgrunnlagFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapInntektsgrunnlagVLTilRegel;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapInntektsgrunnlagVLTilRegelFelles;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.YtelsesspesifikkRegelMapper;
import no.nav.folketrygdloven.kalkulator.modell.gradering.AktivitetGradering;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktiviteterDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.testutilities.BeregningIAYTestUtil;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.ArbeidType;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.Hjemmel;
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
        Periode opptjeningPeriode = Periode.of(SKJÆRINGSTIDSPUNKT_BEREGNING.minusYears(1), SKJÆRINGSTIDSPUNKT_BEREGNING.minusDays(1));
        var opptjeningAktiviteter = OpptjeningAktiviteterDto.fra(OpptjeningAktivitetType.MILITÆR_ELLER_SIVILTJENESTE, opptjeningPeriode);

        InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder = byggMilitærForBehandling(SKJÆRINGSTIDSPUNKT_BEREGNING.minusMonths(10), SKJÆRINGSTIDSPUNKT_BEREGNING.minusDays(1));
        var iayGrunnlag = iayGrunnlagBuilder.build();
        var input = lagInput(koblingReferanse, opptjeningAktiviteter, iayGrunnlag, 100, 3);

        // Act 1: Fastsett beregningaktiviteter og kontroller fakta beregning
        BeregningsgrunnlagGrunnlagDto grunnlag = kjørStegOgLagreGrunnlag(input);
        input = input.medBeregningsgrunnlagGrunnlag(grunnlag);
        var aksjonspunktResultat = beregningTjenesteWrapper.getAksjonspunktUtlederFaktaOmBeregning().utledAksjonspunkterFor(input,
            grunnlag, false);

        // Assert 1
        assertThat(aksjonspunktResultat.getBeregningAksjonspunktResultatList()).isEmpty();

        // Act 2: foreslå beregningsgrunnlag
        BeregningsgrunnlagRegelResultat resultat = foreslåBeregningsgrunnlag.foreslåBeregningsgrunnlag(input);

        // Assert 2
        verdikjedeTestHjelper.verifiserBeregningsgrunnlagBasis(resultat, Hjemmel.F_14_7);

        BeregningsgrunnlagDto foreslåttBeregningsgrunnlag = resultat.getBeregningsgrunnlag();
        BeregningsgrunnlagPeriodeDto periode = foreslåttBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        verdikjedeTestHjelper.verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, Intervall.TIDENES_ENDE, 1);

        // Act 3: fordel beregningsgrunnlag
        resultat = vurderBeregningsgrunnlagTjeneste.vurderBeregningsgrunnlag(input,
            nyttGrunnlag(grunnlag, foreslåttBeregningsgrunnlag, BeregningsgrunnlagTilstand.FORESLÅTT));
        assertThat(resultat.getVilkårOppfylt()).isTrue();
        assertThat(resultat.getAksjonspunkter()).isEmpty();
        BeregningsgrunnlagDto fordeltBeregningsgrunnlag = fordelBeregningsgrunnlag(koblingReferanse, grunnlag, resultat, iayGrunnlag);
        input = input.medBeregningsgrunnlagGrunnlag(BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(grunnlag).medBeregningsgrunnlag(fordeltBeregningsgrunnlag).build(BeregningsgrunnlagTilstand.OPPDATERT_MED_REFUSJON_OG_GRADERING));

        // Act 4: fastsette beregningsgrunnlag
        var fastsattBeregningsgrunnlag = beregningTjenesteWrapper.getFullføreBeregningsgrunnlagTjeneste().fullføreBeregningsgrunnlag(input
        );

        // Assert 3
        verdikjedeTestHjelper.verifiserBeregningsgrunnlagBasis(fastsattBeregningsgrunnlag, Hjemmel.F_14_7);

        periode = fastsattBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        BigDecimal treG = BigDecimal.valueOf(3 * 93634);
        assertThat(periode.getBeregnetPrÅr()).isEqualByComparingTo(treG);
        assertThat(periode.getRedusertPrÅr()).isEqualByComparingTo(treG);
        assertThat(periode.getDagsats()).isEqualTo(1080);
    }

    private BeregningsgrunnlagGrunnlagDto kjørStegOgLagreGrunnlag(BeregningsgrunnlagInput input) {
        return verdikjedeTestHjelper.kjørStegOgLagreGrunnlag(input, beregningTjenesteWrapper);
    }

    private BeregningsgrunnlagDto fordelBeregningsgrunnlag(KoblingReferanse ref, BeregningsgrunnlagGrunnlagDto grunnlag,
                                                           BeregningsgrunnlagRegelResultat resultat, InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        var input = new BeregningsgrunnlagInput(ref, iayGrunnlag, null, AktivitetGradering.INGEN_GRADERING, List.of(), null)
                .medBeregningsgrunnlagGrunnlag(grunnlag);
        return beregningTjenesteWrapper.getFordelBeregningsgrunnlagTjeneste().fordelBeregningsgrunnlag(input, resultat.getBeregningsgrunnlag()).getBeregningsgrunnlag();
    }

    private InntektArbeidYtelseGrunnlagDtoBuilder byggMilitærForBehandling(LocalDate fom, LocalDate tom) {
        return InntektArbeidYtelseGrunnlagDtoBuilder.oppdatere(Optional.empty())
            .medOppgittOpptjening(BeregningIAYTestUtil.lagAnnenAktivitetOppgittOpptjening(ArbeidType.MILITÆR_ELLER_SIVILTJENESTE, fom, tom));
    }

    private BeregningsgrunnlagInput lagInput(KoblingReferanse ref, OpptjeningAktiviteterDto opptjeningAktiviteter, InntektArbeidYtelseGrunnlagDto iayGrunnlag, int dekningsgrad, int grunnbeløpMilitærHarKravPå) {
        return BeregningsgrunnlagInputTestUtil.lagInputMedIAYOgOpptjeningsaktiviteter(ref, opptjeningAktiviteter, iayGrunnlag, dekningsgrad, grunnbeløpMilitærHarKravPå);
    }

}
