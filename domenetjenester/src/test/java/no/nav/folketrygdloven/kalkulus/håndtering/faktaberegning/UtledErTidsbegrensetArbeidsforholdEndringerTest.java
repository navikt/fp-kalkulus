package no.nav.folketrygdloven.kalkulus.håndtering.faktaberegning;

import static no.nav.folketrygdloven.kalkulus.felles.tid.AbstractIntervall.TIDENES_ENDE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.ErTidsbegrensetArbeidsforholdEndring;

class UtledErTidsbegrensetArbeidsforholdEndringerTest {

    public static final String ARBEIDSGIVER_ORGNR = "89712449";


    @Test
    public void utled_for_arbeid_med_tidsbegrenset_med_avklart_forrige() {
        // Arrange
        BeregningsgrunnlagPrStatusOgAndelDto.Builder andelBuilder = lagArbeidstakerAndel(true);
        BeregningsgrunnlagDto bg = lagBeregningsgrunnlag(andelBuilder);
        BeregningsgrunnlagPrStatusOgAndelDto.Builder andelBuilder2 = lagArbeidstakerAndel(false);
        BeregningsgrunnlagDto bg2 = lagBeregningsgrunnlag(andelBuilder2);

        // Act
        List<ErTidsbegrensetArbeidsforholdEndring> erTidsbegrensetArbeidsforholdEndringer = UtledErTidsbegrensetArbeidsforholdEndringer.utled(bg, Optional.of(bg2));

        // Assert
        assertThat(erTidsbegrensetArbeidsforholdEndringer.size()).isEqualTo(1);
        assertThat(erTidsbegrensetArbeidsforholdEndringer.get(0).getArbeidsgiver().getIdent()).isEqualTo(ARBEIDSGIVER_ORGNR);
        assertThat(erTidsbegrensetArbeidsforholdEndringer.get(0).getErTidsbegrensetArbeidsforholdEndring().getFraVerdi()).isFalse();
        assertThat(erTidsbegrensetArbeidsforholdEndringer.get(0).getErTidsbegrensetArbeidsforholdEndring().getTilVerdi()).isTrue();
    }

    @Test
    public void utled_for_arbeid_med_tidsbegrenset() {
        // Arrange
        BeregningsgrunnlagPrStatusOgAndelDto.Builder andelBuilder = lagArbeidstakerAndel(true);
        BeregningsgrunnlagDto bg = lagBeregningsgrunnlag(andelBuilder);

        // Act
        List<ErTidsbegrensetArbeidsforholdEndring> erTidsbegrensetArbeidsforholdEndringer = UtledErTidsbegrensetArbeidsforholdEndringer.utled(bg, Optional.empty());

        // Assert
        assertThat(erTidsbegrensetArbeidsforholdEndringer.size()).isEqualTo(1);
        assertThat(erTidsbegrensetArbeidsforholdEndringer.get(0).getArbeidsgiver().getIdent()).isEqualTo(ARBEIDSGIVER_ORGNR);
        assertThat(erTidsbegrensetArbeidsforholdEndringer.get(0).getErTidsbegrensetArbeidsforholdEndring().getFraVerdi()).isNull();
        assertThat(erTidsbegrensetArbeidsforholdEndringer.get(0).getErTidsbegrensetArbeidsforholdEndring().getTilVerdi()).isTrue();
    }

    @Test
    public void skal_ikkje_utlede_tidsbegrenset_endring_for_frilans() {
        // Arrange
        BeregningsgrunnlagPrStatusOgAndelDto.Builder andelBuilder = lagFrilansAndel();
        BeregningsgrunnlagDto bg = lagBeregningsgrunnlag(andelBuilder);

        // Act
        List<ErTidsbegrensetArbeidsforholdEndring> erTidsbegrensetArbeidsforholdEndringer = UtledErTidsbegrensetArbeidsforholdEndringer.utled(bg, Optional.empty());

        // Assert
        assertThat(erTidsbegrensetArbeidsforholdEndringer.size()).isEqualTo(0);
    }

    private BeregningsgrunnlagDto lagBeregningsgrunnlag(BeregningsgrunnlagPrStatusOgAndelDto.Builder andelBuilder) {
        BeregningsgrunnlagPeriodeDto.Builder periodeDto = BeregningsgrunnlagPeriodeDto.builder()
                .leggTilBeregningsgrunnlagPrStatusOgAndel(andelBuilder)
                .medBeregningsgrunnlagPeriode(LocalDate.now(), TIDENES_ENDE);
        return BeregningsgrunnlagDto.builder()
                .medSkjæringstidspunkt(LocalDate.now())
                .leggTilBeregningsgrunnlagPeriode(periodeDto)
                .build();
    }

    private BeregningsgrunnlagPrStatusOgAndelDto.Builder lagArbeidstakerAndel(boolean erTidsbegrensetArbeidsforhold) {
        return BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(Arbeidsgiver.virksomhet(ARBEIDSGIVER_ORGNR)).medTidsbegrensetArbeidsforhold(erTidsbegrensetArbeidsforhold))
                .medAndelsnr(1L);
    }

    private BeregningsgrunnlagPrStatusOgAndelDto.Builder lagFrilansAndel() {
        return BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
                .medAktivitetStatus(AktivitetStatus.FRILANSER)
                .medAndelsnr(1L);
    }


}
