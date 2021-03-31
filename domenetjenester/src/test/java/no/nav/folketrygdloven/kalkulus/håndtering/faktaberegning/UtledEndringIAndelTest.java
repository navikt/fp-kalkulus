package no.nav.folketrygdloven.kalkulus.håndtering.faktaberegning;


import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;

public class UtledEndringIAndelTest {

    public static final String ARBEIDSGIVER_ORGNR = "12345678";
    public static final InternArbeidsforholdRefDto ARBEIDSFORHOLD_REF = InternArbeidsforholdRefDto.nyRef();

    @Test
    public void skal_utlede_endring_for_arbeidstaker_uten_forrige_andel() {
        // Arrange
        BigDecimal inntekt = BigDecimal.TEN;
        Inntektskategori inntektskategori = Inntektskategori.FRILANSER;
        BeregningsgrunnlagPrStatusOgAndelDto andelUtenInntektskategori = lagArbeidstakerAndel(inntekt, null);
        BeregningsgrunnlagPrStatusOgAndelDto nyAndel = lagArbeidstakerAndel(inntekt, inntektskategori);

        // Act
        var endring = UtledEndringIAndel.utled(nyAndel, Optional.of(andelUtenInntektskategori), Optional.empty());

        // Assert
        assert endring.isPresent();
        assertThat(endring.get().getAktivitetStatus()).isEqualTo(no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus.ARBEIDSTAKER);
        assertThat(endring.get().getArbeidsgiver().getIdent()).isEqualTo(ARBEIDSGIVER_ORGNR);
        assertThat(endring.get().getArbeidsforholdRef()).isEqualTo(ARBEIDSFORHOLD_REF.getReferanse());
        assertThat(endring.get().getInntektEndring().getFraInntekt()).isNull();
        assertThat(endring.get().getInntektEndring().getTilInntekt()).isEqualTo(inntekt);
        assertThat(endring.get().getInntektskategoriEndring().getFraVerdi()).isNull();
        assertThat(endring.get().getInntektskategoriEndring().getTilVerdi()).isEqualTo(inntektskategori);
    }

    @Test
    public void skal_utlede_endring_for_arbeidstaker_med_forrige_andel() {
        // Arrange
        BigDecimal inntekt = BigDecimal.TEN;
        Inntektskategori inntektskategori = Inntektskategori.FRILANSER;
        BeregningsgrunnlagPrStatusOgAndelDto nyAndel = lagArbeidstakerAndel(inntekt, inntektskategori);
        Inntektskategori forrigeInntektskategori = Inntektskategori.ARBEIDSTAKER;
        BigDecimal forrigeInntekt = BigDecimal.ZERO;
        BeregningsgrunnlagPrStatusOgAndelDto forrigeAndel = lagArbeidstakerAndel(forrigeInntekt, forrigeInntektskategori);

        // Act
        var endring = UtledEndringIAndel.utled(nyAndel, Optional.of(nyAndel), Optional.of(forrigeAndel));

        // Assert
        assert endring.isPresent();
        assertThat(endring.get().getAktivitetStatus()).isEqualTo(no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus.ARBEIDSTAKER);
        assertThat(endring.get().getArbeidsgiver().getIdent()).isEqualTo(ARBEIDSGIVER_ORGNR);
        assertThat(endring.get().getArbeidsforholdRef()).isEqualTo(ARBEIDSFORHOLD_REF.getReferanse());
        assertThat(endring.get().getInntektEndring().getFraInntekt()).isEqualTo(forrigeInntekt);
        assertThat(endring.get().getInntektEndring().getTilInntekt()).isEqualTo(inntekt);
        assertThat(endring.get().getInntektskategoriEndring().getFraVerdi()).isEqualTo(forrigeInntektskategori);
        assertThat(endring.get().getInntektskategoriEndring().getTilVerdi()).isEqualTo(inntektskategori);
    }

    @Test
    public void skal_utlede_endring_for_frilans_med_forrige_andel() {
        // Arrange
        BigDecimal inntekt = BigDecimal.TEN;
        Inntektskategori inntektskategori = Inntektskategori.FISKER;
        BeregningsgrunnlagPrStatusOgAndelDto nyAndel = lagFrilanserAndel(inntekt, inntektskategori);
        Inntektskategori forrigeInntektskategori = Inntektskategori.FRILANSER;
        BigDecimal forrigeInntekt = BigDecimal.ZERO;
        BeregningsgrunnlagPrStatusOgAndelDto forrigeAndel = lagFrilanserAndel(forrigeInntekt, forrigeInntektskategori);

        // Act
        var endring = UtledEndringIAndel.utled(nyAndel, Optional.of(nyAndel), Optional.of(forrigeAndel));

        // Assert
        assert endring.isPresent();
        assertThat(endring.get().getAktivitetStatus()).isEqualTo(no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus.FRILANSER);
        assertThat(endring.get().getArbeidsgiver()).isNull();
        assertThat(endring.get().getArbeidsforholdRef()).isNull();
        assertThat(endring.get().getInntektEndring().getFraInntekt()).isEqualTo(forrigeInntekt);
        assertThat(endring.get().getInntektEndring().getTilInntekt()).isEqualTo(inntekt);
        assertThat(endring.get().getInntektskategoriEndring().getFraVerdi()).isEqualTo(forrigeInntektskategori);
        assertThat(endring.get().getInntektskategoriEndring().getTilVerdi()).isEqualTo(inntektskategori);
    }

    private BeregningsgrunnlagPrStatusOgAndelDto lagArbeidstakerAndel(BigDecimal inntekt, Inntektskategori inntektskategori) {
        return BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
                .medInntektskategori(inntektskategori)
                .medBeregnetPrÅr(inntekt)
                .medAndelsnr(1L)
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder()
                        .medArbeidsgiver(Arbeidsgiver.virksomhet(ARBEIDSGIVER_ORGNR))
                        .medArbeidsforholdRef(ARBEIDSFORHOLD_REF))
                .build();
    }

    private BeregningsgrunnlagPrStatusOgAndelDto lagFrilanserAndel(BigDecimal inntekt, Inntektskategori inntektskategori) {
        return BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
                .medInntektskategori(inntektskategori)
                .medBeregnetPrÅr(inntekt)
                .medAndelsnr(1L)
                .medAktivitetStatus(AktivitetStatus.FRILANSER)
                .build();
    }

}
