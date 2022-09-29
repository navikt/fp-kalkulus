package no.nav.folketrygdloven.kalkulator.steg.fortsettForeslå;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;

/**
 * Midlertidig sjekk for å se om andel med status MS / SN allerede er kjørt gjennom foreslå steg
 */
public class SnMsForeslåttSjekk {

    public static boolean snOgMsErAlleredeForeslått(BeregningsgrunnlagDto beregningsgrunnlag) {
        var erMS = beregningsgrunnlag.getAktivitetStatuser().stream()
                .anyMatch(status -> status.getAktivitetStatus().equals(AktivitetStatus.MILITÆR_ELLER_SIVIL));
        var erSN = beregningsgrunnlag.getAktivitetStatuser().stream()
                .anyMatch(status -> status.getAktivitetStatus().erSelvstendigNæringsdrivende());
        if (!erMS && !erSN) {
            return false;
        }
        var førstePeriode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        var snAndelErForeslått = førstePeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream().anyMatch(andel -> andel.getAktivitetStatus().equals(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
                && andel.getBruttoPrÅr() != null);
        var msAndelErForeslått = førstePeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream().anyMatch(andel -> andel.getAktivitetStatus().equals(AktivitetStatus.MILITÆR_ELLER_SIVIL)
                && andel.getBruttoPrÅr() != null);
        if (erSN && erMS) {
            return snAndelErForeslått && msAndelErForeslått;
        } else if (erSN) {
            return snAndelErForeslått;
        } else {
             return msAndelErForeslått;
        }
    }
}
