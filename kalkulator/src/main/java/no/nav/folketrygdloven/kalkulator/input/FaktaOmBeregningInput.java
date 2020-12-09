package no.nav.folketrygdloven.kalkulator.input;

import java.util.ArrayList;
import java.util.List;
import no.nav.folketrygdloven.beregningsgrunnlag.Grunnbeløp;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.gradering.AktivitetGradering;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.RefusjonskravDatoDto;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktiviteterDto;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand;

/** Inputstruktur for beregningsgrunnlag tjenester. */
public class FaktaOmBeregningInput extends StegProsesseringInput {


    /**
     * Grunnbeløpsatser
     */
    private List<Grunnbeløp> grunnbeløpsatser = new ArrayList<>();


    public FaktaOmBeregningInput(StegProsesseringInput input) {
        super(input);
        super.stegTilstand = BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER;
        super.stegUtTilstand = BeregningsgrunnlagTilstand.KOFAKBER_UT;
    }

    public FaktaOmBeregningInput(KoblingReferanse koblingReferanse,
                                 InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                 OpptjeningAktiviteterDto opptjeningAktiviteter,
                                 AktivitetGradering aktivitetGradering,
                                 List<RefusjonskravDatoDto> refusjonskravDatoer,
                                 YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {
        super(BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER,
                koblingReferanse, iayGrunnlag, opptjeningAktiviteter, aktivitetGradering, refusjonskravDatoer, ytelsespesifiktGrunnlag);
    }

    protected FaktaOmBeregningInput(FaktaOmBeregningInput input) {
        super(input);
        this.grunnbeløpsatser = input.getGrunnbeløpsatser();
    }

    public List<Grunnbeløp> getGrunnbeløpsatser() {
        return grunnbeløpsatser;
    }

    public FaktaOmBeregningInput medGrunnbeløpsatser(List<Grunnbeløp> grunnbeløpsatser) {
        var newInput = new FaktaOmBeregningInput(this);
        newInput.grunnbeløpsatser = grunnbeløpsatser;
        return newInput;
    }

}
