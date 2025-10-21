package no.nav.folketrygdloven.kalkulus.håndtering.faktaberegning;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAktørDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.ErMottattYtelseEndring;

class UtledErMottattYtelseEndringerTest {

    public static final String ARBEIDSGIVER_ORGNR = "89712449";

    @Test
    void skal_returnere_endring_i_mottatt_ytelse_for_frilans() {
        // Arrange
        FaktaAggregatDto fakta = FaktaAggregatDto.builder().medFaktaAktør(FaktaAktørDto.builder()
                .medHarFLMottattYtelseFastsattAvSaksbehandler(true)
                .build())
                .build();

        // Act
        List<ErMottattYtelseEndring> erMottattYtelseEndringList = UtledErMottattYtelseEndringer.utled(fakta, Optional.empty());

        // Assert
        assertThat(erMottattYtelseEndringList).hasSize(1);
        assertThat(erMottattYtelseEndringList.get(0).getAktivitetStatus()).isEqualTo(no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus.FRILANSER);
        assertThat(erMottattYtelseEndringList.get(0).getErMottattYtelseEndring().getFraVerdi()).isNull();
        assertThat(erMottattYtelseEndringList.get(0).getErMottattYtelseEndring().getTilVerdi()).isTrue();
    }

    @Test
    void skal_returnere_endring_i_mottatt_ytelse_for_frilans_med_forrige() {
        // Arrange
        FaktaAggregatDto fakta = FaktaAggregatDto.builder().medFaktaAktør(FaktaAktørDto.builder()
                .medHarFLMottattYtelseFastsattAvSaksbehandler(true)
                .build())
                .build();

        FaktaAggregatDto fakta2 = FaktaAggregatDto.builder().medFaktaAktør(FaktaAktørDto.builder()
                .medHarFLMottattYtelseFastsattAvSaksbehandler(false)
                .build())
                .build();

        // Act
        List<ErMottattYtelseEndring> erMottattYtelseEndringList = UtledErMottattYtelseEndringer.utled(fakta, Optional.of(fakta2));

        // Assert
        assertThat(erMottattYtelseEndringList).hasSize(1);
        assertThat(erMottattYtelseEndringList.get(0).getAktivitetStatus()).isEqualTo(no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus.FRILANSER);
        assertThat(erMottattYtelseEndringList.get(0).getErMottattYtelseEndring().getFraVerdi()).isFalse();
        assertThat(erMottattYtelseEndringList.get(0).getErMottattYtelseEndring().getTilVerdi()).isTrue();
    }

    @Test
    void skal_ikkje_returnere_endring_i_mottatt_ytelse_for_frilans_uten_endring() {
        // Arrange
        FaktaAggregatDto fakta = FaktaAggregatDto.builder().medFaktaAktør(FaktaAktørDto.builder()
                .medHarFLMottattYtelseFastsattAvSaksbehandler(true)
                .build())
                .build();
        FaktaAggregatDto fakta2 = FaktaAggregatDto.builder().medFaktaAktør(FaktaAktørDto.builder()
                .medHarFLMottattYtelseFastsattAvSaksbehandler(true)
                .build())
                .build();

        // Act
        List<ErMottattYtelseEndring> erMottattYtelseEndringList = UtledErMottattYtelseEndringer.utled(fakta, Optional.of(fakta2));

        // Assert
        assertThat(erMottattYtelseEndringList).isEmpty();
    }

    @Test
    void skal_returnere_endring_i_mottatt_ytelse_for_arbeidstaker() {
        // Arrange
        FaktaAggregatDto fakta = FaktaAggregatDto.builder().erstattEksisterendeEllerLeggTil(FaktaArbeidsforholdDto.builder(Arbeidsgiver.virksomhet(ARBEIDSGIVER_ORGNR), InternArbeidsforholdRefDto.nullRef())
                .medHarMottattYtelseFastsattAvSaksbehandler(true)
                .build()).build();

        // Act
        List<ErMottattYtelseEndring> erMottattYtelseEndringList = UtledErMottattYtelseEndringer.utled(fakta, Optional.empty());

        // Assert
        assertThat(erMottattYtelseEndringList).hasSize(1);
        assertThat(erMottattYtelseEndringList.get(0).getAktivitetStatus()).isEqualTo(no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus.ARBEIDSTAKER);
        assertThat(erMottattYtelseEndringList.get(0).getArbeidsgiver().getIdent()).isEqualTo(ARBEIDSGIVER_ORGNR);
        assertThat(erMottattYtelseEndringList.get(0).getErMottattYtelseEndring().getFraVerdi()).isNull();
        assertThat(erMottattYtelseEndringList.get(0).getErMottattYtelseEndring().getTilVerdi()).isTrue();
    }

    @Test
    void skal_returnere_endring_i_mottatt_ytelse_for_arbeidstaker_med_forrige() {
        // Arrange
        FaktaAggregatDto fakta = FaktaAggregatDto.builder().erstattEksisterendeEllerLeggTil(FaktaArbeidsforholdDto.builder(Arbeidsgiver.virksomhet(ARBEIDSGIVER_ORGNR), InternArbeidsforholdRefDto.nullRef())
                .medHarMottattYtelseFastsattAvSaksbehandler(true)
                .build()).build();

        FaktaAggregatDto fakta2 = FaktaAggregatDto.builder().erstattEksisterendeEllerLeggTil(FaktaArbeidsforholdDto.builder(Arbeidsgiver.virksomhet(ARBEIDSGIVER_ORGNR), InternArbeidsforholdRefDto.nullRef())
                .medHarMottattYtelseFastsattAvSaksbehandler(false)
                .build()).build();

        // Act
        List<ErMottattYtelseEndring> erMottattYtelseEndringList = UtledErMottattYtelseEndringer.utled(fakta, Optional.of(fakta2));

        // Assert
        assertThat(erMottattYtelseEndringList).hasSize(1);
        assertThat(erMottattYtelseEndringList.get(0).getAktivitetStatus()).isEqualTo(no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus.ARBEIDSTAKER);
        assertThat(erMottattYtelseEndringList.get(0).getArbeidsgiver().getIdent()).isEqualTo(ARBEIDSGIVER_ORGNR);
        assertThat(erMottattYtelseEndringList.get(0).getErMottattYtelseEndring().getFraVerdi()).isFalse();
        assertThat(erMottattYtelseEndringList.get(0).getErMottattYtelseEndring().getTilVerdi()).isTrue();
    }

}
