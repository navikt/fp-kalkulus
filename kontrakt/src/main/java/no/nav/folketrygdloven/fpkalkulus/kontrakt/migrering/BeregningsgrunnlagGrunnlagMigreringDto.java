package no.nav.folketrygdloven.fpkalkulus.kontrakt.migrering;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;

public class BeregningsgrunnlagGrunnlagMigreringDto extends BaseMigreringDto {
    @Valid
    private BeregningsgrunnlagMigreringDto beregningsgrunnlag;

    @Valid
    @NotNull
    private BeregningAktivitetAggregatMigreringDto registerAktiviteter;

    @Valid
    private BeregningAktivitetAggregatMigreringDto saksbehandletAktiviteter;

    @Valid
    private BeregningAktivitetOverstyringerMigreringDto overstyringer;

    @Valid
    private BeregningRefusjonOverstyringerMigreringDto refusjonOverstyringer;

    @Valid
    private FaktaAggregatMigreringDto faktaAggregat;

    @Valid
    @NotNull
    private BeregningsgrunnlagTilstand beregningsgrunnlagTilstand;

    public BeregningsgrunnlagGrunnlagMigreringDto(BeregningsgrunnlagMigreringDto beregningsgrunnlag,
                                                  BeregningAktivitetAggregatMigreringDto registerAktiviteter,
                                                  BeregningAktivitetAggregatMigreringDto saksbehandletAktiviteter,
                                                  BeregningAktivitetOverstyringerMigreringDto overstyringer,
                                                  BeregningRefusjonOverstyringerMigreringDto refusjonOverstyringer,
                                                  FaktaAggregatMigreringDto faktaAggregat,
                                                  BeregningsgrunnlagTilstand beregningsgrunnlagTilstand) {
        this.beregningsgrunnlag = beregningsgrunnlag;
        this.registerAktiviteter = registerAktiviteter;
        this.saksbehandletAktiviteter = saksbehandletAktiviteter;
        this.overstyringer = overstyringer;
        this.refusjonOverstyringer = refusjonOverstyringer;
        this.faktaAggregat = faktaAggregat;
        this.beregningsgrunnlagTilstand = beregningsgrunnlagTilstand;
    }

    public @Valid BeregningsgrunnlagMigreringDto getBeregningsgrunnlag() {
        return beregningsgrunnlag;
    }

    public @Valid @NotNull BeregningAktivitetAggregatMigreringDto getRegisterAktiviteter() {
        return registerAktiviteter;
    }

    public @Valid BeregningAktivitetAggregatMigreringDto getSaksbehandletAktiviteter() {
        return saksbehandletAktiviteter;
    }

    public @Valid BeregningAktivitetOverstyringerMigreringDto getOverstyringer() {
        return overstyringer;
    }

    public @Valid BeregningRefusjonOverstyringerMigreringDto getRefusjonOverstyringer() {
        return refusjonOverstyringer;
    }

    public @Valid FaktaAggregatMigreringDto getFaktaAggregat() {
        return faktaAggregat;
    }

    public @Valid @NotNull BeregningsgrunnlagTilstand getBeregningsgrunnlagTilstand() {
        return beregningsgrunnlagTilstand;
    }
}
