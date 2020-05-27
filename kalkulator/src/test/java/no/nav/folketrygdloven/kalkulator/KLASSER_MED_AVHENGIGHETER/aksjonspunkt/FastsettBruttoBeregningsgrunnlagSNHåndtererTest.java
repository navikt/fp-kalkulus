package no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.BehandlingReferanseMock;
import no.nav.folketrygdloven.kalkulator.BeregningsgrunnlagInputTestUtil;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FastsettBruttoBeregningsgrunnlagSNDto;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.BehandlingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand;


public class FastsettBruttoBeregningsgrunnlagSNHåndtererTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now().minusDays(5);
    private static final BigDecimal GRUNNBELØP = BigDecimal.valueOf(90000);
    private static final int BRUTTO_BG = 200000;
    private BehandlingReferanse behandlingReferanse = new BehandlingReferanseMock(SKJÆRINGSTIDSPUNKT);
    private BeregningsgrunnlagInput input;

    @Test
    public void skal_oppdatere_beregningsgrunnlag_med_overstyrt_verdi() {
        //Arrange
        int antallPerioder = 1;
        lagBehandlingMedBeregningsgrunnlag(antallPerioder);

        //Dto
        var fastsettBGDto = new FastsettBruttoBeregningsgrunnlagSNDto(BRUTTO_BG);

        // Act
        BeregningsgrunnlagGrunnlagDto grunnlag = FastsettBruttoBeregningsgrunnlagSNHåndterer.håndter(input, fastsettBGDto);

        //Assert
        assertBeregningsgrunnlag(grunnlag, antallPerioder);
    }

    @Test
    public void skal_oppdatere_beregningsgrunnlag_med_overstyrt_verdi_for_fleire_perioder() {
        //Arrange
        int antallPerioder = 3;
        lagBehandlingMedBeregningsgrunnlag(antallPerioder);

        //Dto
        var fastsettBGDto = new FastsettBruttoBeregningsgrunnlagSNDto(BRUTTO_BG);

        // Act
        BeregningsgrunnlagGrunnlagDto grunnlag = FastsettBruttoBeregningsgrunnlagSNHåndterer.håndter(input, fastsettBGDto);

        //Assert
        assertBeregningsgrunnlag(grunnlag, antallPerioder);
    }

    private void assertBeregningsgrunnlag(BeregningsgrunnlagGrunnlagDto grunnlag, int antallPerioder) {
        Optional<BeregningsgrunnlagDto> beregningsgrunnlag = grunnlag.getBeregningsgrunnlag();
        Assertions.assertThat(beregningsgrunnlag).as("beregningsgrunnlag").hasValueSatisfying(bg -> {
            List<BeregningsgrunnlagPeriodeDto> beregningsgrunnlagPerioder = bg.getBeregningsgrunnlagPerioder();
            Assertions.assertThat(beregningsgrunnlagPerioder).hasSize(antallPerioder);
            beregningsgrunnlagPerioder.forEach(beregningsgrunnlagPeriode -> {
                List<BeregningsgrunnlagPrStatusOgAndelDto> beregningsgrunnlagPrStatusOgAndelList = beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatusOgAndelList();
                Assertions.assertThat(beregningsgrunnlagPrStatusOgAndelList).hasSize(1);
                assertThat(beregningsgrunnlagPrStatusOgAndelList.get(0).getBruttoPrÅr().doubleValue()).isEqualTo(BRUTTO_BG);
            });
        });
    }

    private void buildBgPrStatusOgAndel(BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode) {
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
            .build(beregningsgrunnlagPeriode);
    }

    private BeregningsgrunnlagPeriodeDto buildBeregningsgrunnlagPeriode(BeregningsgrunnlagDto beregningsgrunnlag,
                                                                        LocalDate fom,
                                                                        LocalDate tom) {
        return BeregningsgrunnlagPeriodeDto.builder()
            .medBeregningsgrunnlagPeriode(fom, tom)
            .build(beregningsgrunnlag);
    }

    private void lagBehandlingMedBeregningsgrunnlag(int antallPerioder) {


        BeregningsgrunnlagDto beregningsgrunnlag = BeregningsgrunnlagDto.builder()
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .medGrunnbeløp(GRUNNBELØP)
            .build();

        for (int i = 0; i < antallPerioder; i++) {
            LocalDate fom = LocalDate.now().minusDays(20).plusDays(i*5).plusDays(i==0 ? 0 : 1);
            BeregningsgrunnlagPeriodeDto bgPeriode = buildBeregningsgrunnlagPeriode(beregningsgrunnlag,
                fom, fom.plusDays(5));
            buildBgPrStatusOgAndel(bgPeriode);
        }

        input = BeregningsgrunnlagInputTestUtil.lagInputMedBeregningsgrunnlag(behandlingReferanse, beregningsgrunnlag, BeregningsgrunnlagTilstand.FORESLÅTT);
    }
}
