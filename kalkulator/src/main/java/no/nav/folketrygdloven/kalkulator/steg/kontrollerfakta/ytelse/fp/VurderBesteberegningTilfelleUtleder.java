package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.ytelse.fp;

import java.util.Optional;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.FaktaOmBeregningTilfelleRef;
import no.nav.folketrygdloven.kalkulator.input.FaktaOmBeregningInput;
import no.nav.folketrygdloven.kalkulator.input.ForeldrepengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.utledere.TilfelleUtleder;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;


@ApplicationScoped
@FagsakYtelseTypeRef(FagsakYtelseType.FORELDREPENGER)
@FaktaOmBeregningTilfelleRef("VURDER_BESTEBEREGNING")
public class VurderBesteberegningTilfelleUtleder implements TilfelleUtleder {
    private static final Set<OpptjeningAktivitetType> AKTIVITETER_SOM_KAN_AUTOMATISK_BESTEBEREGNES = Set.of(OpptjeningAktivitetType.ARBEID, OpptjeningAktivitetType.DAGPENGER, OpptjeningAktivitetType.SYKEPENGER,
            OpptjeningAktivitetType.FORELDREPENGER, OpptjeningAktivitetType.SVANGERSKAPSPENGER);


    @Override
    public Optional<FaktaOmBeregningTilfelle> utled(FaktaOmBeregningInput input,
                                                    BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag) {
        boolean kanAutomatiskBesteberegnes = input.getOpptjeningAktiviteterForBeregning().stream()
                .allMatch(a -> AKTIVITETER_SOM_KAN_AUTOMATISK_BESTEBEREGNES.contains(a.getOpptjeningAktivitetType()));
        if (kanAutomatiskBesteberegnes) {
            return Optional.empty();
        }
        boolean harKunYtelse = beregningsgrunnlagGrunnlag.getBeregningsgrunnlag().orElseThrow(() -> new IllegalArgumentException("Skal ha beregningsgrunnlag"))
                .getAktivitetStatuser()
                .stream()
                .anyMatch(s -> AktivitetStatus.KUN_YTELSE.equals(s.getAktivitetStatus()));
        return ((ForeldrepengerGrunnlag) input.getYtelsespesifiktGrunnlag()).isKvalifisererTilBesteberegning() && !harKunYtelse && !harFjernetDagpenger(beregningsgrunnlagGrunnlag) ?
                Optional.of(FaktaOmBeregningTilfelle.VURDER_BESTEBEREGNING) : Optional.empty();
    }

    private boolean harFjernetDagpenger(BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag) {
        Optional<BeregningAktivitetAggregatDto> saksbehandletAktiviteter = beregningsgrunnlagGrunnlag.getSaksbehandletAktiviteter();
        if (saksbehandletAktiviteter.isEmpty()) {
            return false;
        }
        boolean harDagpengerIRegister = beregningsgrunnlagGrunnlag.getRegisterAktiviteter().getBeregningAktiviteter().stream()
                .anyMatch(ba -> ba.getOpptjeningAktivitetType().equals(OpptjeningAktivitetType.DAGPENGER));
        boolean harIkkeDagpengerISaksbehandlet = saksbehandletAktiviteter.get().getBeregningAktiviteter().stream()
                .noneMatch(ba -> ba.getOpptjeningAktivitetType().equals(OpptjeningAktivitetType.DAGPENGER));
        return harDagpengerIRegister && harIkkeDagpengerISaksbehandlet;
    }

}
