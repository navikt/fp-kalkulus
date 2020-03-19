package no.nav.folketrygdloven.kalkulus.håndtering.faktaberegning;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.BeregningsgrunnlagPrStatusOgAndelEndring;

public class UtledEndringIAndelTest {

    public static final String ARBEIDSGIVER_ORGNR = "12345678";
    public static final InternArbeidsforholdRefDto ARBEIDSFORHOLD_REF = InternArbeidsforholdRefDto.nyRef();

    @Test
    public void skal_utlede_endring_for_arbeidstaker_uten_forrige_andel() {
        // Arrange
        BigDecimal inntekt = BigDecimal.TEN;
        Inntektskategori inntektskategori = Inntektskategori.FRILANSER;
        BeregningsgrunnlagPrStatusOgAndelDto nyAndel = lagArbeidstakerAndel(inntekt, inntektskategori);

        // Act
        var endring = UtledEndringIAndel.utled(nyAndel, Optional.empty());

        // Assert
        assert endring.isPresent();
        assertThat(endring.get().getAktivitetStatus()).isEqualTo(no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus.ARBEIDSTAKER);
        assertThat(endring.get().getArbeidsgiver().getIdent()).isEqualTo(ARBEIDSGIVER_ORGNR);
        assertThat(endring.get().getArbeidsforholdRef()).isEqualTo(ARBEIDSFORHOLD_REF.getReferanse());
        assertThat(endring.get().getInntektEndring().getFraInntekt()).isNull();
        assertThat(endring.get().getInntektEndring().getTilInntekt()).isEqualTo(inntekt);
        assertThat(endring.get().getInntektskategoriEndring().getFraVerdi()).isNull();
        assertThat(endring.get().getInntektskategoriEndring().getTilVerdi()).isEqualTo(new no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori(inntektskategori.getKode()));
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
        var endring = UtledEndringIAndel.utled(nyAndel, Optional.of(forrigeAndel));

        // Assert
        assert endring.isPresent();
        assertThat(endring.get().getAktivitetStatus()).isEqualTo(no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus.ARBEIDSTAKER);
        assertThat(endring.get().getArbeidsgiver().getIdent()).isEqualTo(ARBEIDSGIVER_ORGNR);
        assertThat(endring.get().getArbeidsforholdRef()).isEqualTo(ARBEIDSFORHOLD_REF.getReferanse());
        assertThat(endring.get().getInntektEndring().getFraInntekt()).isEqualTo(forrigeInntekt);
        assertThat(endring.get().getInntektEndring().getTilInntekt()).isEqualTo(inntekt);
        assertThat(endring.get().getInntektskategoriEndring().getFraVerdi()).isEqualTo(new no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori(forrigeInntektskategori.getKode()));
        assertThat(endring.get().getInntektskategoriEndring().getTilVerdi()).isEqualTo(new no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori(inntektskategori.getKode()));
    }

    @Test
    public void skal_utlede_endring_for_frilans_med_forrige_andel() {
        // Arrange
        BigDecimal inntekt = BigDecimal.TEN;
        Inntektskategori inntektskategori = Inntektskategori.FISKER;
        boolean mottarYtelse = true;
        BeregningsgrunnlagPrStatusOgAndelDto nyAndel = lagFrilanserAndel(inntekt, inntektskategori, mottarYtelse);
        Inntektskategori forrigeInntektskategori = Inntektskategori.FRILANSER;
        BigDecimal forrigeInntekt = BigDecimal.ZERO;
        boolean forrigeMottarYtelse = false;
        BeregningsgrunnlagPrStatusOgAndelDto forrigeAndel = lagFrilanserAndel(forrigeInntekt, forrigeInntektskategori, forrigeMottarYtelse);

        // Act
        var endring = UtledEndringIAndel.utled(nyAndel, Optional.of(forrigeAndel));

        // Assert
        assert endring.isPresent();
        assertThat(endring.get().getAktivitetStatus()).isEqualTo(no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus.FRILANSER);
        assertThat(endring.get().getArbeidsgiver()).isNull();
        assertThat(endring.get().getArbeidsforholdRef()).isNull();
        assertThat(endring.get().getInntektEndring().getFraInntekt()).isEqualTo(forrigeInntekt);
        assertThat(endring.get().getInntektEndring().getTilInntekt()).isEqualTo(inntekt);
        assertThat(endring.get().getInntektskategoriEndring().getFraVerdi()).isEqualTo(new no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori(forrigeInntektskategori.getKode()));
        assertThat(endring.get().getInntektskategoriEndring().getTilVerdi()).isEqualTo(new no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori(inntektskategori.getKode()));
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

    private BeregningsgrunnlagPrStatusOgAndelDto lagFrilanserAndel(BigDecimal inntekt, Inntektskategori inntektskategori, Boolean mottarYtelse) {
        return BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
                .medInntektskategori(inntektskategori)
                .medBeregnetPrÅr(inntekt)
                .medAndelsnr(1L)
                .medMottarYtelse(mottarYtelse, AktivitetStatus.FRILANSER)
                .medAktivitetStatus(AktivitetStatus.FRILANSER)
                .build();
    }

}
