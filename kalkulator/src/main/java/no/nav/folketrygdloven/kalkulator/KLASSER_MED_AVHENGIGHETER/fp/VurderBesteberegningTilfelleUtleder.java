package no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.fp;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.ForeldrepengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.kontrollerfakta.utledere.TilfelleUtleder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.FaktaOmBeregningTilfelle;


@ApplicationScoped
public class VurderBesteberegningTilfelleUtleder implements TilfelleUtleder {

    @Override
    public Optional<FaktaOmBeregningTilfelle> utled(BeregningsgrunnlagInput input,
                                                    BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag) {
        boolean harKunYtelse = beregningsgrunnlagGrunnlag.getBeregningsgrunnlag().orElseThrow(() -> new IllegalArgumentException("Skal ha beregningsgrunnlag"))
            .getAktivitetStatuser()
            .stream()
            .anyMatch(s -> AktivitetStatus.KUN_YTELSE.equals(s.getAktivitetStatus()));
        return ((ForeldrepengerGrunnlag) input.getYtelsespesifiktGrunnlag()).isKvalifisererTilBesteberegning() && !harKunYtelse ?
            Optional.of(FaktaOmBeregningTilfelle.VURDER_BESTEBEREGNING) : Optional.empty();
    }

}
