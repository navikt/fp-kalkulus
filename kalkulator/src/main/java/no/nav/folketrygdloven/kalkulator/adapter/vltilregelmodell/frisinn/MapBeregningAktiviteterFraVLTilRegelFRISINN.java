package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.frisinn;

import java.time.LocalDate;
import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Aktivitet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapBeregningAktiviteterFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktivitetType;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivPeriode;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModell;

@ApplicationScoped
@FagsakYtelseTypeRef("FRISINN")
public class MapBeregningAktiviteterFraVLTilRegelFRISINN extends MapBeregningAktiviteterFraVLTilRegel {

    public static final String INGEN_AKTIVITET_MELDING = "Må ha aktiviteter for å sette status.";

    @Override
    public AktivitetStatusModell mapForSkjæringstidspunkt(BeregningsgrunnlagInput input) {
        LocalDate opptjeningSkjæringstidspunkt = input.getSkjæringstidspunktOpptjening();

        AktivitetStatusModell modell = new AktivitetStatusModell();
        modell.setSkjæringstidspunktForOpptjening(opptjeningSkjæringstidspunkt);

        var relevanteAktiviteter = input.getOpptjeningAktiviteterForBeregning();

        if (relevanteAktiviteter.isEmpty()) { // For enklere feilsøking når det mangler aktiviteter
            throw new IllegalStateException(INGEN_AKTIVITET_MELDING);
        } else {
            relevanteAktiviteter.forEach(opptjeningsperiode -> modell.leggTilEllerOppdaterAktivPeriode(lagAktivPeriode(input.getInntektsmeldinger(), opptjeningsperiode)));
        }


        // Legger til 12 mnd med frilans og næring rundt stp om det ikkje finnes
        if (relevanteAktiviteter.stream().noneMatch(a -> a.getType().equals(OpptjeningAktivitetType.FRILANS))) {
            modell.leggTilEllerOppdaterAktivPeriode(AktivPeriode.forFrilanser(Periode.of(opptjeningSkjæringstidspunkt.minusMonths(36), opptjeningSkjæringstidspunkt.plusMonths(6))));
        }
        if (relevanteAktiviteter.stream().noneMatch(a -> a.getType().equals(OpptjeningAktivitetType.NÆRING))) {
            modell.leggTilEllerOppdaterAktivPeriode(AktivPeriode.forAndre(Aktivitet.NÆRINGSINNTEKT, Periode.of(opptjeningSkjæringstidspunkt.minusMonths(36), opptjeningSkjæringstidspunkt.plusMonths(6))));
        }
        return modell;
    }


}
