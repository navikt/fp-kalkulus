package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;


public class SkalKunneEndreAktivitetTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT_OPPTJENING = LocalDate.of(2018, Month.MAY, 10);
    private BeregningsgrunnlagDto beregningsgrunnlag;
    private BeregningsgrunnlagPeriodeDto periode;


    @BeforeEach
    public void setUp() {
        beregningsgrunnlag = BeregningsgrunnlagDto.builder().medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_OPPTJENING).medGrunnbeløp(BigDecimal.valueOf(600000)).build();
        periode = BeregningsgrunnlagPeriodeDto.builder().medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_OPPTJENING, null)
            .build(beregningsgrunnlag);
    }

    @Test
    public void skalIkkjeKunneEndreAktivitetOmLagtTilAvSaksbehandlerOgDagpenger() {
        BeregningsgrunnlagPrStatusOgAndelDto dagpengeAndel = BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medAktivitetStatus(AktivitetStatus.DAGPENGER)
            .medLagtTilAvSaksbehandler(true)
            .build(periode);

        Boolean skalKunneEndreAktivitet = SkalKunneEndreAktivitet.skalKunneEndreAktivitet(dagpengeAndel);

        assertThat(skalKunneEndreAktivitet).isFalse();
    }

    @Test
    public void skalIkkjeKunneEndreAktivitetOmIkkjeLagtTilAvSaksbehandler() {
        BeregningsgrunnlagPrStatusOgAndelDto frilans = BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medAktivitetStatus(AktivitetStatus.FRILANSER)
            .medLagtTilAvSaksbehandler(false)
            .build(periode);

        Boolean skalKunneEndreAktivitet = SkalKunneEndreAktivitet.skalKunneEndreAktivitet(frilans);

        assertThat(skalKunneEndreAktivitet).isFalse();
    }

    @Test
    public void skalKunneEndreAktivitetOmLagtTilAvSaksbehandler() {
        BeregningsgrunnlagPrStatusOgAndelDto frilans = BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medAktivitetStatus(AktivitetStatus.FRILANSER)
            .medLagtTilAvSaksbehandler(true)
            .build(periode);

        Boolean skalKunneEndreAktivitet = SkalKunneEndreAktivitet.skalKunneEndreAktivitet(frilans);

        assertThat(skalKunneEndreAktivitet).isTrue();
    }
}
