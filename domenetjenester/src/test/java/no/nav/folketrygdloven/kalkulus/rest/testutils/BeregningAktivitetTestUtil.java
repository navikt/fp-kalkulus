package no.nav.folketrygdloven.kalkulus.rest.testutils;

import java.time.LocalDate;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;

public class BeregningAktivitetTestUtil {

    public static BeregningAktivitetAggregatDto opprettBeregningAktiviteter(LocalDate skjæringstidspunkt, Intervall periode, OpptjeningAktivitetType... opptjeningAktivitetTypes) {
        BeregningAktivitetAggregatDto.Builder builder = BeregningAktivitetAggregatDto.builder()
            .medSkjæringstidspunktOpptjening(skjæringstidspunkt);
        for (OpptjeningAktivitetType aktivitet : opptjeningAktivitetTypes) {
            BeregningAktivitetDto beregningAktivitet = BeregningAktivitetDto.builder()
                .medPeriode(periode)
                .medOpptjeningAktivitetType(aktivitet)
                .build();
            builder.leggTilAktivitet(beregningAktivitet);
        }
        BeregningAktivitetAggregatDto aggregat = builder.build();
        return aggregat;
    }


}
