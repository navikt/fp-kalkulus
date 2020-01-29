package no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand;


public class BeregningsgrunnlagGrunnlagDto {

    private BeregningsgrunnlagDto beregningsgrunnlag;
    private BeregningAktivitetAggregatDto registerAktiviteter;
    private BeregningAktivitetAggregatDto saksbehandletAktiviteter;
    private BeregningAktivitetOverstyringerDto overstyringer;
    private BeregningRefusjonOverstyringerDto refusjonOverstyringer;
    private boolean aktiv;
    private BeregningsgrunnlagTilstand beregningsgrunnlagTilstand;

    public BeregningsgrunnlagGrunnlagDto() {
    }

    BeregningsgrunnlagGrunnlagDto(BeregningsgrunnlagGrunnlagDto grunnlag) {
        grunnlag.getBeregningsgrunnlag().ifPresent(this::setBeregningsgrunnlag);
        this.setRegisterAktiviteter(grunnlag.getRegisterAktiviteter());
        grunnlag.getSaksbehandletAktiviteter().ifPresent(this::setSaksbehandletAktiviteter);
        grunnlag.getOverstyring().ifPresent(this::setOverstyringer);
        grunnlag.getRefusjonOverstyringer().ifPresent(this::setRefusjonOverstyringer);
    }

    public Optional<BeregningsgrunnlagDto> getBeregningsgrunnlag() {
        return Optional.ofNullable(beregningsgrunnlag);
    }

    public BeregningAktivitetAggregatDto getRegisterAktiviteter() {
        return registerAktiviteter;
    }

    public Optional<BeregningAktivitetAggregatDto> getSaksbehandletAktiviteter() {
        return Optional.ofNullable(saksbehandletAktiviteter);
    }

    public Optional<BeregningAktivitetAggregatDto> getOverstyrteEllerSaksbehandletAktiviteter() {
        Optional<BeregningAktivitetAggregatDto> overstyrteAktiviteter = getOverstyrteAktiviteter();
        if (overstyrteAktiviteter.isPresent()) {
            return overstyrteAktiviteter;
        }
        return Optional.ofNullable(saksbehandletAktiviteter);
    }

    public Optional<BeregningAktivitetOverstyringerDto> getOverstyring() {
        return Optional.ofNullable(overstyringer);
    }

    private Optional<BeregningAktivitetAggregatDto> getOverstyrteAktiviteter() {
        if (overstyringer != null) {
            List<BeregningAktivitetDto> overstyrteAktiviteter = registerAktiviteter.getBeregningAktiviteter().stream()
                    .filter(beregningAktivitet -> beregningAktivitet.skalBrukes(overstyringer))
                    .collect(Collectors.toList());
            BeregningAktivitetAggregatDto.Builder overstyrtBuilder = BeregningAktivitetAggregatDto.builder()
                    .medSkjæringstidspunktOpptjening(registerAktiviteter.getSkjæringstidspunktOpptjening());
            overstyrteAktiviteter.forEach(aktivitet -> {
                BeregningAktivitetDto kopiert = BeregningAktivitetDto.kopier(aktivitet).build();
                overstyrtBuilder.leggTilAktivitet(kopiert);
            });
            return Optional.of(overstyrtBuilder.build());
        }
        return Optional.empty();
    }

    public BeregningAktivitetAggregatDto getGjeldendeAktiviteter() {
        return getOverstyrteAktiviteter()
                .or(this::getSaksbehandletAktiviteter)
                .orElse(registerAktiviteter);
    }

    public BeregningAktivitetAggregatDto getOverstyrteEllerRegisterAktiviteter() {
        Optional<BeregningAktivitetAggregatDto> overstyrteAktiviteter = getOverstyrteAktiviteter();
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

    void setBeregningsgrunnlag(BeregningsgrunnlagDto beregningsgrunnlag) {
        this.beregningsgrunnlag = beregningsgrunnlag;
    }

    void setRegisterAktiviteter(BeregningAktivitetAggregatDto registerAktiviteter) {
        this.registerAktiviteter = registerAktiviteter;
    }

    void setSaksbehandletAktiviteter(BeregningAktivitetAggregatDto saksbehandletAktiviteter) {
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
