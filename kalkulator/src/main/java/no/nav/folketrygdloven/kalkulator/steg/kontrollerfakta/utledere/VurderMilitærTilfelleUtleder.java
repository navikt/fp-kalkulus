package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.utledere;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.FaktaOmBeregningTilfelleRef;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktiviteterDto;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.FaktaOmBeregningTilfelle;

@ApplicationScoped
@FagsakYtelseTypeRef("*")
@FaktaOmBeregningTilfelleRef("VURDER_MILITÆR_SIVILTJENESTE")
public class VurderMilitærTilfelleUtleder implements TilfelleUtleder {

    @Override
    public Optional<FaktaOmBeregningTilfelle> utled(BeregningsgrunnlagInput input, BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag) {

        BeregningsgrunnlagDto beregningsgrunnlag = beregningsgrunnlagGrunnlag.getBeregningsgrunnlag().orElse(null);
        Objects.requireNonNull(beregningsgrunnlag, "beregningsgrunnlag");
        return harOppgittMilitærIOpptjeningsperioden(input.getOpptjeningAktiviteterForBeregning()) ?
            Optional.of(FaktaOmBeregningTilfelle.VURDER_MILITÆR_SIVILTJENESTE) : Optional.empty();
    }

    private static boolean harOppgittMilitærIOpptjeningsperioden(Collection<OpptjeningAktiviteterDto.OpptjeningPeriodeDto> opptjeningRelevantForBeregning) {
        return opptjeningRelevantForBeregning.stream()
            .anyMatch(opptjening -> opptjening.getOpptjeningAktivitetType().equals(OpptjeningAktivitetType.MILITÆR_ELLER_SIVILTJENESTE));
    }

}
