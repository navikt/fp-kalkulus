package no.nav.folketrygdloven.kalkulator.steg.foreslå;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelMerknad;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ResultatBeregningType;
import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.ForeldrepengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.output.BeregningAksjonspunktResultat;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningAksjonspunkt;

public class AksjonspunktUtlederForeslåBeregningTest {

    private KoblingReferanse referanse = new KoblingReferanseMock();

    @Test
    public void skalIkkeFåAksjonspunkterVed100PDekningsgrad() {
        // Act
        List<BeregningAksjonspunktResultat> aksjonspunkter = AksjonspunktUtlederForeslåBeregning.utledAksjonspunkter(lagInput(referanse), Collections.emptyList());
        // Assert
        assertThat(aksjonspunkter).isEmpty();
    }

    @Test
    public void skalIkkeFåAksjonspunkterVed80PDekningsgrad() {
        // Arrange
        var input = lagInput();
        // Act
        List<BeregningAksjonspunktResultat> aksjonspunkter = AksjonspunktUtlederForeslåBeregning.utledAksjonspunkter(input, Collections.emptyList());
        // Assert
        assertThat(aksjonspunkter).isEmpty();
    }

    @Test
    public void skalFåAksjonspunkt5042() {
        // Arrange
        RegelResultat regelResultat = lagRegelResultat("5042");
        // Act
        List<BeregningAksjonspunktResultat> aksjonspunkter = AksjonspunktUtlederForeslåBeregning.utledAksjonspunkter(lagInput(referanse), Collections.singletonList(regelResultat));
        // Assert
        var apDefs = aksjonspunkter.stream().map(BeregningAksjonspunktResultat::getBeregningAksjonspunktDefinisjon).collect(Collectors.toList());
        assertThat(apDefs).containsExactly(BeregningAksjonspunkt.FASTSETT_BEREGNINGSGRUNNLAG_SELVSTENDIG_NÆRINGSDRIVENDE);
    }

    @Test
    public void skalFåAksjonspunkt5049() {
        // Arrange
        RegelResultat regelResultat = lagRegelResultat("5049");
        // Act
        List<BeregningAksjonspunktResultat> aksjonspunkter = AksjonspunktUtlederForeslåBeregning.utledAksjonspunkter(lagInput(referanse), Collections.singletonList(regelResultat));
        // Assert
        var apDefs = aksjonspunkter.stream().map(BeregningAksjonspunktResultat::getBeregningAksjonspunktDefinisjon).collect(Collectors.toList());
        assertThat(apDefs).containsExactly(BeregningAksjonspunkt.FASTSETT_BEREGNINGSGRUNNLAG_FOR_SN_NY_I_ARBEIDSLIVET);
    }

    @Test
    public void skalFåAksjonspunkt5038() {
        // Arrange
        RegelResultat regelResultat = lagRegelResultat("5038");

        var input = lagInput();

        // Act
        List<BeregningAksjonspunktResultat> aksjonspunkter = AksjonspunktUtlederForeslåBeregning.utledAksjonspunkter(input, Collections.singletonList(regelResultat));
        // Assert
        var apDefs = aksjonspunkter.stream().map(BeregningAksjonspunktResultat::getBeregningAksjonspunktDefinisjon).collect(Collectors.toList());
        assertThat(apDefs).containsExactlyInAnyOrder(
            BeregningAksjonspunkt.FASTSETT_BEREGNINGSGRUNNLAG_ARBEIDSTAKER_FRILANS
        );
    }

    private BeregningsgrunnlagInput lagInput() {
        var foreldrepengerGrunnlag = new ForeldrepengerGrunnlag(80, false);
        return new BeregningsgrunnlagInput(referanse, null, null, null, List.of(), foreldrepengerGrunnlag);
    }

    @Test
    public void skal_ikke_få_aksjonspunkt5087_når_barnet_ikke_har_dødd() {
        // Arrange
        // Act
        List<BeregningAksjonspunktResultat> aksjonspunkter = AksjonspunktUtlederForeslåBeregning.utledAksjonspunkter(lagInput(referanse), Collections.emptyList());
        // Assert
        var apDefs = aksjonspunkter.stream().map(BeregningAksjonspunktResultat::getBeregningAksjonspunktDefinisjon).collect(Collectors.toList());
        assertThat(apDefs).isEmpty();
    }

    @Test
    public void skal_ikke_få_aksjonspunkt5087_når_ingen_barn_et_født() {
        // Arrange
        var input = lagInput();
        // Act
        List<BeregningAksjonspunktResultat> aksjonspunkter = AksjonspunktUtlederForeslåBeregning.utledAksjonspunkter(input, Collections.emptyList());
        // Assert
        var apDefs = aksjonspunkter.stream().map(BeregningAksjonspunktResultat::getBeregningAksjonspunktDefinisjon).collect(Collectors.toList());
        assertThat(apDefs).isEmpty();
    }

    @Test
    public void skal_ikke_få_aksjonspunkt5087_når_ikke_alle_barnene_døde_innen_seks_uker() {
        // Arrange
        var input = lagInput();

        // Act
        List<BeregningAksjonspunktResultat> aksjonspunkter = AksjonspunktUtlederForeslåBeregning.utledAksjonspunkter(input, Collections.emptyList());
        // Assert
        var apDefs = aksjonspunkter.stream().map(BeregningAksjonspunktResultat::getBeregningAksjonspunktDefinisjon).collect(Collectors.toList());
        assertThat(apDefs).isEmpty();
    }

    @Test
    public void skal_ikke_få_aksjonspunkt5087_når_ett_barn_døde_og_ett_barn_levde() {
        // Arrange
        var input = lagInput();

        // Act
        List<BeregningAksjonspunktResultat> aksjonspunkter = AksjonspunktUtlederForeslåBeregning.utledAksjonspunkter(input, Collections.emptyList());
        // Assert
        var apDefs = aksjonspunkter.stream().map(BeregningAksjonspunktResultat::getBeregningAksjonspunktDefinisjon).collect(Collectors.toList());
        assertThat(apDefs).isEmpty();
    }

    private BeregningsgrunnlagInput lagInput(KoblingReferanse referanse) {
        var foreldrepengerGrunnlag = new ForeldrepengerGrunnlag(100, false);
        return new BeregningsgrunnlagInput(referanse, null, null, null, List.of(), foreldrepengerGrunnlag);
    }

    private RegelResultat lagRegelResultat(String merknadKode) {
        RegelMerknad regelMerknad = new RegelMerknad(merknadKode, "blablabla");
        return new RegelResultat(ResultatBeregningType.IKKE_BEREGNET, "regelInput", "regelSporing")
            .medRegelMerknad(regelMerknad);
    }

}
