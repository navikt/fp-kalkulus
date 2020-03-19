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
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.ErMottattYtelseEndring;

class UtledErMottattYtelseEndringerTest {

    public static final String ARBEIDSGIVER_ORGNR = "89712449";

    @Test
    public void skal_returnere_endring_i_mottatt_ytelse_for_frilans() {
        // Arrange
        BeregningsgrunnlagPrStatusOgAndelDto.Builder andelBuilder = lagFrilansandel(true);
        BeregningsgrunnlagDto bg = lagBeregningsgrunnlag(andelBuilder);

        // Act
        List<ErMottattYtelseEndring> erMottattYtelseEndringList = UtledErMottattYtelseEndringer.utled(bg, Optional.empty());

        // Assert
        assertThat(erMottattYtelseEndringList.size()).isEqualTo(1);
        assertThat(erMottattYtelseEndringList.get(0).getAktivitetStatus()).isEqualTo(no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus.FRILANSER);
        assertThat(erMottattYtelseEndringList.get(0).getErMottattYtelseEndring().getFraVerdi()).isNull();
        assertThat(erMottattYtelseEndringList.get(0).getErMottattYtelseEndring().getTilVerdi()).isTrue();
    }

    @Test
    public void skal_returnere_endring_i_mottatt_ytelse_for_frilans_med_forrige() {
        // Arrange
        BeregningsgrunnlagPrStatusOgAndelDto.Builder andelBuilder = lagFrilansandel(true);
        BeregningsgrunnlagDto bg = lagBeregningsgrunnlag(andelBuilder);
        BeregningsgrunnlagPrStatusOgAndelDto.Builder andelBuilder2 = lagFrilansandel(false);
        BeregningsgrunnlagDto bg2 = lagBeregningsgrunnlag(andelBuilder2);

        // Act
        List<ErMottattYtelseEndring> erMottattYtelseEndringList = UtledErMottattYtelseEndringer.utled(bg, Optional.of(bg2));

        // Assert
        assertThat(erMottattYtelseEndringList.size()).isEqualTo(1);
        assertThat(erMottattYtelseEndringList.get(0).getAktivitetStatus()).isEqualTo(no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus.FRILANSER);
        assertThat(erMottattYtelseEndringList.get(0).getErMottattYtelseEndring().getFraVerdi()).isFalse();
        assertThat(erMottattYtelseEndringList.get(0).getErMottattYtelseEndring().getTilVerdi()).isTrue();
    }

    @Test
    public void skal_ikkje_returnere_endring_i_mottatt_ytelse_for_frilans_uten_endring() {
        // Arrange
        BeregningsgrunnlagPrStatusOgAndelDto.Builder andelBuilder = lagFrilansandel(true);
        BeregningsgrunnlagDto bg = lagBeregningsgrunnlag(andelBuilder);
        BeregningsgrunnlagPrStatusOgAndelDto.Builder andelBuilder2 = lagFrilansandel(true);
        BeregningsgrunnlagDto bg2 = lagBeregningsgrunnlag(andelBuilder2);

        // Act
        List<ErMottattYtelseEndring> erMottattYtelseEndringList = UtledErMottattYtelseEndringer.utled(bg, Optional.of(bg2));

        // Assert
        assertThat(erMottattYtelseEndringList.size()).isEqualTo(0);
    }

    @Test
    public void skal_returnere_endring_i_mottatt_ytelse_for_arbeidstaker() {
        // Arrange
        BeregningsgrunnlagPrStatusOgAndelDto.Builder andelBuilder = lagArbeidstakerAndel(true);
        BeregningsgrunnlagDto bg = lagBeregningsgrunnlag(andelBuilder);

        // Act
        List<ErMottattYtelseEndring> erMottattYtelseEndringList = UtledErMottattYtelseEndringer.utled(bg, Optional.empty());

        // Assert
        assertThat(erMottattYtelseEndringList.size()).isEqualTo(1);
        assertThat(erMottattYtelseEndringList.get(0).getAktivitetStatus()).isEqualTo(no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus.ARBEIDSTAKER);
        assertThat(erMottattYtelseEndringList.get(0).getArbeidsgiver().getIdent()).isEqualTo(ARBEIDSGIVER_ORGNR);
        assertThat(erMottattYtelseEndringList.get(0).getErMottattYtelseEndring().getFraVerdi()).isNull();
        assertThat(erMottattYtelseEndringList.get(0).getErMottattYtelseEndring().getTilVerdi()).isTrue();
    }

    @Test
    public void skal_returnere_endring_i_mottatt_ytelse_for_arbeidstaker_med_forrige() {
        // Arrange
        BeregningsgrunnlagPrStatusOgAndelDto.Builder andelBuilder = lagArbeidstakerAndel(true);
        BeregningsgrunnlagDto bg = lagBeregningsgrunnlag(andelBuilder);
        BeregningsgrunnlagPrStatusOgAndelDto.Builder andelBuilder2 = lagArbeidstakerAndel(false);
        BeregningsgrunnlagDto bg2 = lagBeregningsgrunnlag(andelBuilder2);

        // Act
        List<ErMottattYtelseEndring> erMottattYtelseEndringList = UtledErMottattYtelseEndringer.utled(bg, Optional.of(bg2));

        // Assert
        assertThat(erMottattYtelseEndringList.size()).isEqualTo(1);
        assertThat(erMottattYtelseEndringList.get(0).getAktivitetStatus()).isEqualTo(no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus.ARBEIDSTAKER);
        assertThat(erMottattYtelseEndringList.get(0).getArbeidsgiver().getIdent()).isEqualTo(ARBEIDSGIVER_ORGNR);
        assertThat(erMottattYtelseEndringList.get(0).getErMottattYtelseEndring().getFraVerdi()).isFalse();
        assertThat(erMottattYtelseEndringList.get(0).getErMottattYtelseEndring().getTilVerdi()).isTrue();
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

    private BeregningsgrunnlagPrStatusOgAndelDto.Builder lagFrilansandel(boolean mottarYtelse) {
        return BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
                .medAktivitetStatus(AktivitetStatus.FRILANSER)
                .medAndelsnr(1L)
                .medMottarYtelse(mottarYtelse, AktivitetStatus.FRILANSER);
    }

    private BeregningsgrunnlagPrStatusOgAndelDto.Builder lagArbeidstakerAndel(boolean mottarYtelse) {
        return BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(Arbeidsgiver.virksomhet(ARBEIDSGIVER_ORGNR)))
                .medAndelsnr(1L)
                .medMottarYtelse(mottarYtelse, AktivitetStatus.ARBEIDSTAKER);
    }
}
