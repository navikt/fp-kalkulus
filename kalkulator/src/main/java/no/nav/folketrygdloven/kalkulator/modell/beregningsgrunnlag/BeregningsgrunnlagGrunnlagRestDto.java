package no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.BeregningsgrunnlagTilstand;


public class BeregningsgrunnlagGrunnlagRestDto {

    private BeregningsgrunnlagRestDto beregningsgrunnlag;
    private BeregningAktivitetAggregatRestDto registerAktiviteter;
    private BeregningAktivitetAggregatRestDto saksbehandletAktiviteter;
    private BeregningAktivitetOverstyringerDto overstyringer;
    private BeregningRefusjonOverstyringerDto refusjonOverstyringer;
    private boolean aktiv;
    private BeregningsgrunnlagTilstand beregningsgrunnlagTilstand;

    public BeregningsgrunnlagGrunnlagRestDto() {
    }

    BeregningsgrunnlagGrunnlagRestDto(BeregningsgrunnlagGrunnlagRestDto grunnlag) {
        grunnlag.getBeregningsgrunnlag().ifPresent(this::setBeregningsgrunnlag);
        this.setRegisterAktiviteter(grunnlag.getRegisterAktiviteter());
        grunnlag.getSaksbehandletAktiviteter().ifPresent(this::setSaksbehandletAktiviteter);
        grunnlag.getOverstyring().ifPresent(this::setOverstyringer);
        grunnlag.getRefusjonOverstyringer().ifPresent(this::setRefusjonOverstyringer);
    }

    public Optional<BeregningsgrunnlagRestDto> getBeregningsgrunnlag() {
        return Optional.ofNullable(beregningsgrunnlag);
    }

    public BeregningAktivitetAggregatRestDto getRegisterAktiviteter() {
        return registerAktiviteter;
    }

    public Optional<BeregningAktivitetAggregatRestDto> getSaksbehandletAktiviteter() {
        return Optional.ofNullable(saksbehandletAktiviteter);
    }

    public Optional<BeregningAktivitetAggregatRestDto> getOverstyrteEllerSaksbehandletAktiviteter() {
        Optional<BeregningAktivitetAggregatRestDto> overstyrteAktiviteter = getOverstyrteAktiviteter();
        if (overstyrteAktiviteter.isPresent()) {
            return overstyrteAktiviteter;
        }
        return Optional.ofNullable(saksbehandletAktiviteter);
    }

    public Optional<BeregningAktivitetOverstyringerDto> getOverstyring() {
        return Optional.ofNullable(overstyringer);
    }

    private Optional<BeregningAktivitetAggregatRestDto> getOverstyrteAktiviteter() {
        if (overstyringer != null) {
            List<BeregningAktivitetRestDto> overstyrteAktiviteter = registerAktiviteter.getBeregningAktiviteter().stream()
                    .filter(beregningAktivitet -> beregningAktivitet.skalBrukes(overstyringer))
                    .collect(Collectors.toList());
            BeregningAktivitetAggregatRestDto.Builder overstyrtBuilder = BeregningAktivitetAggregatRestDto.builder()
                    .medSkjæringstidspunktOpptjening(registerAktiviteter.getSkjæringstidspunktOpptjening());
            overstyrteAktiviteter.forEach(aktivitet -> {
                BeregningAktivitetRestDto kopiert = BeregningAktivitetRestDto.kopier(aktivitet).build();
                overstyrtBuilder.leggTilAktivitet(kopiert);
            });
            return Optional.of(overstyrtBuilder.build());
        }
        return Optional.empty();
    }

    public BeregningAktivitetAggregatRestDto getGjeldendeAktiviteter() {
        return getOverstyrteAktiviteter()
                .or(this::getSaksbehandletAktiviteter)
                .orElse(registerAktiviteter);
    }

    public BeregningAktivitetAggregatRestDto getOverstyrteEllerRegisterAktiviteter() {
        Optional<BeregningAktivitetAggregatRestDto> overstyrteAktiviteter = getOverstyrteAktiviteter();
        if (overstyrteAktiviteter.isPresent()) {
            return overstyrteAktiviteter.get();
        }
        return registerAktiviteter;
    }

    public BeregningsgrunnlagTilstand getBeregningsgrunnlagTilstand() {
        return beregningsgrunnlagTilstand;
    }

    public boolean erAktivt() {
        return aktiv;
    }

    public void setAktiv(boolean aktiv) {
        this.aktiv = aktiv;
    }

    void setBeregningsgrunnlag(BeregningsgrunnlagRestDto beregningsgrunnlag) {
        this.beregningsgrunnlag = beregningsgrunnlag;
    }

    void setRegisterAktiviteter(BeregningAktivitetAggregatRestDto registerAktiviteter) {
        this.registerAktiviteter = registerAktiviteter;
    }

    void setSaksbehandletAktiviteter(BeregningAktivitetAggregatRestDto saksbehandletAktiviteter) {
        this.saksbehandletAktiviteter = saksbehandletAktiviteter;
    }

    void setBeregningsgrunnlagTilstand(BeregningsgrunnlagTilstand beregningsgrunnlagTilstand) {
        this.beregningsgrunnlagTilstand = beregningsgrunnlagTilstand;
    }

    void setOverstyringer(BeregningAktivitetOverstyringerDto overstyringer) {
        this.overstyringer = overstyringer;
    }

    public Optional<BeregningRefusjonOverstyringerDto> getRefusjonOverstyringer() {
        return Optional.ofNullable(refusjonOverstyringer);
    }

    void setRefusjonOverstyringer(BeregningRefusjonOverstyringerDto refusjonOverstyringer) {
        this.refusjonOverstyringer = refusjonOverstyringer;
    }
}
