package no.nav.folketrygdloven.kalkulator.input;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.Grunnbeløp;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.gradering.AktivitetGradering;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.RefusjonskravDatoDto;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktiviteterDto;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktiviteterDto.OpptjeningPeriodeDto;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand;

/** Inputstruktur for beregningsgrunnlag tjenester. */
public class FastsettBeregningsaktiviteterInput extends StegProsesseringInput {

    /**
     * Grunnbeløpsatser
     */
    private List<Grunnbeløp> grunnbeløpsatser = new ArrayList<>();


    public FastsettBeregningsaktiviteterInput(StegProsesseringInput input) {
        super(input);
        super.stegTilstand = BeregningsgrunnlagTilstand.OPPRETTET;
        super.stegUtTilstand = BeregningsgrunnlagTilstand.FASTSATT_BEREGNINGSAKTIVITETER;
    }

    public FastsettBeregningsaktiviteterInput(KoblingReferanse koblingReferanse,
                                              InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                              OpptjeningAktiviteterDto opptjeningAktiviteter,
                                              AktivitetGradering aktivitetGradering,
                                              List<RefusjonskravDatoDto> refusjonskravDatoer,
                                              YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {
        super(BeregningsgrunnlagTilstand.FASTSATT_BEREGNINGSAKTIVITETER, koblingReferanse, iayGrunnlag, opptjeningAktiviteter, aktivitetGradering, refusjonskravDatoer, ytelsespesifiktGrunnlag);
    }

    protected FastsettBeregningsaktiviteterInput(FastsettBeregningsaktiviteterInput input) {
        super(input);
        this.grunnbeløpsatser = input.getGrunnbeløpsatser();
    }

    public List<Grunnbeløp> getGrunnbeløpsatser() {
        return grunnbeløpsatser;
    }

    public FastsettBeregningsaktiviteterInput medGrunnbeløpsatser(List<Grunnbeløp> grunnbeløpsatser) {
        var newInput = new FastsettBeregningsaktiviteterInput(this);
        newInput.grunnbeløpsatser = grunnbeløpsatser;
        return newInput;
    }


}
