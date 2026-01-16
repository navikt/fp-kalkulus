package no.nav.folketrygdloven.kalkulus.domene.håndtering.faktaberegning;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import no.nav.foreldrepenger.kalkulus.kontrakt.response.håndtering.ErTidsbegrensetArbeidsforholdEndring;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;

class UtledErTidsbegrensetArbeidsforholdEndringerTest {

    public static final String ARBEIDSGIVER_ORGNR = "89712449";


    @Test
    void utled_for_arbeid_med_tidsbegrenset_med_avklart_forrige() {
        // Arrange

        FaktaAggregatDto fakta = FaktaAggregatDto.builder().erstattEksisterendeEllerLeggTil(FaktaArbeidsforholdDto.builder(Arbeidsgiver.virksomhet(ARBEIDSGIVER_ORGNR), InternArbeidsforholdRefDto.nullRef())
                .medErTidsbegrensetFastsattAvSaksbehandler(true).build())
                .build();
        FaktaAggregatDto fakta2 = FaktaAggregatDto.builder().erstattEksisterendeEllerLeggTil(FaktaArbeidsforholdDto.builder(Arbeidsgiver.virksomhet(ARBEIDSGIVER_ORGNR), InternArbeidsforholdRefDto.nullRef())
                .medErTidsbegrensetFastsattAvSaksbehandler(false).build())
                .build();

        // Act
        List<ErTidsbegrensetArbeidsforholdEndring> erTidsbegrensetArbeidsforholdEndringer = UtledErTidsbegrensetArbeidsforholdEndringer.utled(fakta, Optional.of(fakta2));

        // Assert
        assertThat(erTidsbegrensetArbeidsforholdEndringer).hasSize(1);
        assertThat(erTidsbegrensetArbeidsforholdEndringer.get(0).getArbeidsgiver().getIdent()).isEqualTo(ARBEIDSGIVER_ORGNR);
        assertThat(erTidsbegrensetArbeidsforholdEndringer.get(0).getErTidsbegrensetArbeidsforholdEndring().getFraVerdi()).isFalse();
        assertThat(erTidsbegrensetArbeidsforholdEndringer.get(0).getErTidsbegrensetArbeidsforholdEndring().getTilVerdi()).isTrue();
    }

    @Test
    void utled_for_arbeid_med_tidsbegrenset() {
        // Arrange
        FaktaAggregatDto fakta = FaktaAggregatDto.builder().erstattEksisterendeEllerLeggTil(FaktaArbeidsforholdDto.builder(Arbeidsgiver.virksomhet(ARBEIDSGIVER_ORGNR), InternArbeidsforholdRefDto.nullRef())
                .medErTidsbegrensetFastsattAvSaksbehandler(true).build())
                .build();

        // Act
        List<ErTidsbegrensetArbeidsforholdEndring> erTidsbegrensetArbeidsforholdEndringer = UtledErTidsbegrensetArbeidsforholdEndringer.utled(fakta, Optional.empty());

        // Assert
        assertThat(erTidsbegrensetArbeidsforholdEndringer).hasSize(1);
        assertThat(erTidsbegrensetArbeidsforholdEndringer.get(0).getArbeidsgiver().getIdent()).isEqualTo(ARBEIDSGIVER_ORGNR);
        assertThat(erTidsbegrensetArbeidsforholdEndringer.get(0).getErTidsbegrensetArbeidsforholdEndring().getFraVerdi()).isNull();
        assertThat(erTidsbegrensetArbeidsforholdEndringer.get(0).getErTidsbegrensetArbeidsforholdEndring().getTilVerdi()).isTrue();
    }


}
