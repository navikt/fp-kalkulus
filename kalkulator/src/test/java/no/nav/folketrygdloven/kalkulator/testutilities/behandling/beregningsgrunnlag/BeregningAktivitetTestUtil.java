package no.nav.folketrygdloven.kalkulator.testutilities.behandling.beregningsgrunnlag;

import static no.nav.folketrygdloven.kalkulator.rest.MapBeregningsgrunnlagFraRestTilDomene.mapAktivitetAggregat;

import java.time.LocalDate;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetRestDto;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;

public class BeregningAktivitetTestUtil {

    public static BeregningAktivitetAggregatDto opprettBeregningAktiviteter(LocalDate skjæringstidspunkt, OpptjeningAktivitetType... opptjeningAktivitetTypes) {
        Intervall periode = Intervall.fraOgMedTilOgMed(skjæringstidspunkt.minusYears(2), skjæringstidspunkt);
        return mapAktivitetAggregat(opprettBeregningAktiviteter(skjæringstidspunkt, periode, opptjeningAktivitetTypes));
    }

    public static BeregningAktivitetAggregatRestDto opprettBeregningAktiviteter(LocalDate skjæringstidspunkt, Intervall periode, OpptjeningAktivitetType... opptjeningAktivitetTypes) {
        BeregningAktivitetAggregatRestDto.Builder builder = BeregningAktivitetAggregatRestDto.builder()
            .medSkjæringstidspunktOpptjening(skjæringstidspunkt);
        for (OpptjeningAktivitetType aktivitet : opptjeningAktivitetTypes) {
            BeregningAktivitetRestDto beregningAktivitet = BeregningAktivitetRestDto.builder()
                .medPeriode(periode)
                .medOpptjeningAktivitetType(aktivitet)
                .build();
            builder.leggTilAktivitet(beregningAktivitet);
        }
        BeregningAktivitetAggregatRestDto aggregat = builder.build();
        return aggregat;
    }

    public static BeregningAktivitetAggregatRestDto opprettBeregningAktiviteter(LocalDate skjæringstidspunkt, Intervall periode, boolean medDagpenger, boolean ekstraAktivitet) {
        if (medDagpenger) {
            return opprettBeregningAktiviteter(skjæringstidspunkt, periode, OpptjeningAktivitetType.ARBEID, OpptjeningAktivitetType.DAGPENGER);
        } else {
            if (ekstraAktivitet) {
                return opprettBeregningAktiviteter(skjæringstidspunkt, periode, OpptjeningAktivitetType.ARBEID, OpptjeningAktivitetType.SYKEPENGER);
            }
            return opprettBeregningAktiviteter(skjæringstidspunkt, periode, OpptjeningAktivitetType.ARBEID);
        }
    }
}
