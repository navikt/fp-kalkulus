package no.nav.folketrygdloven.kalkulator.rest.fakta;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;

import org.junit.Before;
import org.junit.Test;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.AktivitetStatus;

public class SkalKunneEndreAktivitetTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT_OPPTJENING = LocalDate.of(2018, Month.MAY, 10);
    private BeregningsgrunnlagRestDto beregningsgrunnlag;
    private BeregningsgrunnlagPeriodeRestDto periode;


    @Before
    public void setUp() {
        beregningsgrunnlag = BeregningsgrunnlagRestDto.builder().medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_OPPTJENING).medGrunnbeløp(BigDecimal.valueOf(600000)).build();
        periode = BeregningsgrunnlagPeriodeRestDto.builder().medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_OPPTJENING, null)
            .build(beregningsgrunnlag);
    }

    @Test
    public void skalIkkjeKunneEndreAktivitetOmLagtTilAvSaksbehandlerOgDagpenger() {
        BeregningsgrunnlagPrStatusOgAndelRestDto dagpengeAndel = BeregningsgrunnlagPrStatusOgAndelRestDto.kopier()
            .medAktivitetStatus(AktivitetStatus.DAGPENGER)
            .medLagtTilAvSaksbehandler(true)
            .build(periode);

        Boolean skalKunneEndreAktivitet = SkalKunneEndreAktivitet.skalKunneEndreAktivitet(dagpengeAndel);

        assertThat(skalKunneEndreAktivitet).isFalse();
    }

    @Test
    public void skalIkkjeKunneEndreAktivitetOmIkkjeLagtTilAvSaksbehandler() {
        BeregningsgrunnlagPrStatusOgAndelRestDto frilans = BeregningsgrunnlagPrStatusOgAndelRestDto.kopier()
            .medAktivitetStatus(AktivitetStatus.FRILANSER)
            .medLagtTilAvSaksbehandler(false)
            .build(periode);

        Boolean skalKunneEndreAktivitet = SkalKunneEndreAktivitet.skalKunneEndreAktivitet(frilans);

        assertThat(skalKunneEndreAktivitet).isFalse();
    }

    @Test
    public void skalKunneEndreAktivitetOmLagtTilAvSaksbehandler() {
        BeregningsgrunnlagPrStatusOgAndelRestDto frilans = BeregningsgrunnlagPrStatusOgAndelRestDto.kopier()
            .medAktivitetStatus(AktivitetStatus.FRILANSER)
            .medLagtTilAvSaksbehandler(true)
            .build(periode);

        Boolean skalKunneEndreAktivitet = SkalKunneEndreAktivitet.skalKunneEndreAktivitet(frilans);

        assertThat(skalKunneEndreAktivitet).isTrue();
    }
}
