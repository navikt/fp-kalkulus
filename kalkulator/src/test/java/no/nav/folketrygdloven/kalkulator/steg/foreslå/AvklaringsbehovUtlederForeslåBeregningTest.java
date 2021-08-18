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
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.ForeldrepengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.output.BeregningAvklaringsbehovResultat;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;

public class AvklaringsbehovUtlederForeslåBeregningTest {

    private KoblingReferanse referanse = new KoblingReferanseMock();

    @Test
    public void skalIkkeFåAvklaringsbehovVed100PDekningsgrad() {
        // Act
        List<BeregningAvklaringsbehovResultat> avklaringsbehov = AvklaringsbehovUtlederForeslåBeregning.utledAvklaringsbehov(lagInput(referanse), Collections.emptyList());
        // Assert
        assertThat(avklaringsbehov).isEmpty();
    }

    @Test
    public void skalIkkeFåAvklaringsbehovVed80PDekningsgrad() {
        // Arrange
        var input = lagInput();
        // Act
        List<BeregningAvklaringsbehovResultat> avklaringsbehov = AvklaringsbehovUtlederForeslåBeregning.utledAvklaringsbehov(input, Collections.emptyList());
        // Assert
        assertThat(avklaringsbehov).isEmpty();
    }

    @Test
    public void skalFåAvklaringsbehov5042() {
        // Arrange
        RegelResultat regelResultat = lagRegelResultat("5042");
        // Act
        List<BeregningAvklaringsbehovResultat> avklaringsbehov = AvklaringsbehovUtlederForeslåBeregning.utledAvklaringsbehov(lagInput(referanse), Collections.singletonList(regelResultat));
        // Assert
        var apDefs = avklaringsbehov.stream().map(BeregningAvklaringsbehovResultat::getBeregningAvklaringsbehovDefinisjon).collect(Collectors.toList());
        assertThat(apDefs).containsExactly(AvklaringsbehovDefinisjon.FASTSETT_BEREGNINGSGRUNNLAG_SELVSTENDIG_NÆRINGSDRIVENDE);
    }

    @Test
    public void skalFåAvklaringsbehov5049() {
        // Arrange
        RegelResultat regelResultat = lagRegelResultat("5049");
        // Act
        List<BeregningAvklaringsbehovResultat> avklaringsbehov = AvklaringsbehovUtlederForeslåBeregning.utledAvklaringsbehov(lagInput(referanse), Collections.singletonList(regelResultat));
        // Assert
        var apDefs = avklaringsbehov.stream().map(BeregningAvklaringsbehovResultat::getBeregningAvklaringsbehovDefinisjon).collect(Collectors.toList());
        assertThat(apDefs).containsExactly(AvklaringsbehovDefinisjon.FASTSETT_BEREGNINGSGRUNNLAG_FOR_SN_NY_I_ARBEIDSLIVET);
    }

    @Test
    public void skalFåAvklaringsbehov5038() {
        // Arrange
        RegelResultat regelResultat = lagRegelResultat("5038");

        var input = lagInput();

        // Act
        List<BeregningAvklaringsbehovResultat> avklaringsbehov = AvklaringsbehovUtlederForeslåBeregning.utledAvklaringsbehov(input, Collections.singletonList(regelResultat));
        // Assert
        var apDefs = avklaringsbehov.stream().map(BeregningAvklaringsbehovResultat::getBeregningAvklaringsbehovDefinisjon).collect(Collectors.toList());
        assertThat(apDefs).containsExactlyInAnyOrder(
            AvklaringsbehovDefinisjon.FASTSETT_BEREGNINGSGRUNNLAG_ARBEIDSTAKER_FRILANS
        );
    }

    private BeregningsgrunnlagInput lagInput() {
        var foreldrepengerGrunnlag = new ForeldrepengerGrunnlag(80, false);
        return new BeregningsgrunnlagInput(referanse, null, null, List.of(), foreldrepengerGrunnlag);
    }

    @Test
    public void skal_ikke_få_avklaringsbehov5087_når_barnet_ikke_har_dødd() {
        // Arrange
        // Act
        List<BeregningAvklaringsbehovResultat> avklaringsbehov = AvklaringsbehovUtlederForeslåBeregning.utledAvklaringsbehov(lagInput(referanse), Collections.emptyList());
        // Assert
        var apDefs = avklaringsbehov.stream().map(BeregningAvklaringsbehovResultat::getBeregningAvklaringsbehovDefinisjon).collect(Collectors.toList());
        assertThat(apDefs).isEmpty();
    }

    @Test
    public void skal_ikke_få_avklaringsbehov5087_når_ingen_barn_et_født() {
        // Arrange
        var input = lagInput();
        // Act
        List<BeregningAvklaringsbehovResultat> avklaringsbehov = AvklaringsbehovUtlederForeslåBeregning.utledAvklaringsbehov(input, Collections.emptyList());
        // Assert
        var apDefs = avklaringsbehov.stream().map(BeregningAvklaringsbehovResultat::getBeregningAvklaringsbehovDefinisjon).collect(Collectors.toList());
        assertThat(apDefs).isEmpty();
    }

    @Test
    public void skal_ikke_få_avklaringsbehov5087_når_ikke_alle_barnene_døde_innen_seks_uker() {
        // Arrange
        var input = lagInput();

        // Act
        List<BeregningAvklaringsbehovResultat> avklaringsbehov = AvklaringsbehovUtlederForeslåBeregning.utledAvklaringsbehov(input, Collections.emptyList());
        // Assert
        var apDefs = avklaringsbehov.stream().map(BeregningAvklaringsbehovResultat::getBeregningAvklaringsbehovDefinisjon).collect(Collectors.toList());
        assertThat(apDefs).isEmpty();
    }

    @Test
    public void skal_ikke_få_avklaringsbehov5087_når_ett_barn_døde_og_ett_barn_levde() {
        // Arrange
        var input = lagInput();

        // Act
        List<BeregningAvklaringsbehovResultat> avklaringsbehov = AvklaringsbehovUtlederForeslåBeregning.utledAvklaringsbehov(input, Collections.emptyList());
        // Assert
        var apDefs = avklaringsbehov.stream().map(BeregningAvklaringsbehovResultat::getBeregningAvklaringsbehovDefinisjon).collect(Collectors.toList());
        assertThat(apDefs).isEmpty();
    }

    private BeregningsgrunnlagInput lagInput(KoblingReferanse referanse) {
        var foreldrepengerGrunnlag = new ForeldrepengerGrunnlag(100, false);
        return new BeregningsgrunnlagInput(referanse, null, null, List.of(), foreldrepengerGrunnlag);
    }

    private RegelResultat lagRegelResultat(String merknadKode) {
        RegelMerknad regelMerknad = new RegelMerknad(merknadKode, "blablabla");
        return new RegelResultat(ResultatBeregningType.IKKE_BEREGNET, "regelInput", "regelSporing")
            .medRegelMerknad(regelMerknad);
    }

}
